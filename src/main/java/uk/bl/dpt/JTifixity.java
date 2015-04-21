package uk.bl.dpt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by pmay on 05/04/2015.
 */
public class JTifixity {

    private static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    /**
     * Returns the file and RGB checksum for the specified file.
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String checksum(String file) throws NoSuchAlgorithmException, IOException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        MessageDigest partialmd = MessageDigest.getInstance("MD5");

        try (InputStream is = Files.newInputStream(Paths.get(file))) {
            DigestInputStream dis = new DigestInputStream(is, md);
            DigestInputStream pdis = new DigestInputStream(dis, partialmd);

            int count;
            while ((count=dis.read()) > -1) {
                count=pdis.read();
            }
        }

        StringBuilder digest = new StringBuilder();
        for (byte b: md.digest()){
            digest.append(String.format("%02x", b));
        }

//        digest.append("\n");
//        for (byte b: partialmd.digest()){
//            digest.append(String.format("%02x", b));
//        }
        return digest.toString();
    }

    public static String checksumRGB(String file) throws IOException, NoSuchAlgorithmException {
        Tiff tiff = readTIFF(file);
        long rgbfrom = tiff.getRGBOffset();
        long rgbto   = rgbfrom+tiff.getRGBLength(); // [StripOffsets, StripByteCounts)

        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(Paths.get(file))) {
            DigestInputStream dis = new DigestInputStream(is, md);

            // initially turn off digest function
            dis.on(false);

            int index=0;
            while (dis.read() > -1) {
                index++;
                if (index==rgbfrom){
                    dis.on(true);
                } else if (index==rgbto){
                    dis.on(false);
                }
            }
        }

        StringBuilder digest = new StringBuilder();
        for (byte b: md.digest()){
            digest.append(String.format("%02x", b));
        }

        return digest.toString();
    }

    public static Tiff readTIFF(String file) throws IOException{
        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(file))) {
            ByteBuffer buf = ByteBuffer.allocate(8);

            // read TIFF header
            int count = sbc.read(buf);

            // check byte ordering
            buf.rewind();
            if(buf.get()==0x4D && buf.get()==0x4D){
                byteOrder = ByteOrder.BIG_ENDIAN;
            }
            System.out.println("Byte Order: "+byteOrder);

            // read IFD offset
            buf.position(4);
            int ifdoffset = buf.order(byteOrder).getInt();
            System.out.println("IFD Offset: "+ifdoffset);

            // read the IFD starting at the specified offset
            IFD ifd = readIFD(sbc, ifdoffset);

            Tiff tiff = new Tiff(byteOrder);
            tiff.addIFD(ifd);
            return tiff;
        }
    }

    private static IFD readIFD(SeekableByteChannel sbc, long offset) throws IOException {
        IFD ifd = new IFD(offset);

        ByteBuffer buf = ByteBuffer.allocate(8);
        sbc.position(offset);   // set the offset

        int count = sbc.read(buf);
        buf.rewind();
        // 2 byte count + 12 bytes per directory
        short dircount = buf.order(byteOrder).getShort();
        System.out.println("Dir count: "+dircount);

        // read each Directory
        sbc.position(offset+2);
        for(int i=0; i<dircount; i++){
            readDirectory(sbc, ifd);
        }

        return ifd;
    }

    private static void readDirectory(SeekableByteChannel sbc, IFD ifd) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(12);
        sbc.read(buf);
        buf.rewind();
        // bytes:
        //  0-1  Tag
        //  2-3  Type
        //  4-7  Count of indicated type
        //  8-11 Value offset
        int tagval = buf.order(byteOrder).getShort();
        IFDTag tag = IFDTag.getTag(tagval);

        short typeval = buf.order(byteOrder).getShort();
        IFDType type = IFDType.getType(typeval);

        int count = buf.order(byteOrder).getInt();

        int value = buf.order(byteOrder).getInt();

        System.out.println("Tag: "+tag+"\tType: "+type+" ("+typeval+")\tCount: "+count+"\tValue: "+printHexInt(value));

        // add directory to IFD object
        ifd.addDirectory(tag, type, count, value);
    }

    private static String printHexInt(int value){
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(value).append(")");

        for(int i = 0; i < 4; i++) {
            sb.insert(0, String.format("%02x ", value & 0xFF));
            value = value>>8;
        }

        return sb.toString();
    }

    public static void main(String[] args){
        try {
            String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
            System.out.println(hash);
            //readTIFF("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
        } catch (Exception e){

        }
    }
}
