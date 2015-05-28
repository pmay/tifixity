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

    private static int BUFFERSIZE = 100;

    public static String checksumRGB(String file) throws IOException, NoSuchAlgorithmException {
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file);
        Integer[] rgbIndexes = tiff.getRGBOffset();
        Integer[] rgbLengths = tiff.getRGBLength();

        assert(rgbIndexes.length == rgbLengths.length);

        MessageDigest md = MessageDigest.getInstance("MD5");
//        try (InputStream is = Files.newInputStream(Paths.get(file))) {
//            DigestInputStream dis = new DigestInputStream(is, md);
        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(file))) {
            ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);

            int totalBytesRead;
            int bytesRead;
            for(int j=0; j<rgbIndexes.length; j++){
                // Do not assume split data is in sequential order in the file.
                // jump to next position and read the data in
                sbc.position(rgbIndexes[j]);
                buf.clear();

                totalBytesRead=0;
                bytesRead=0;
//                StringBuilder sbbuf = new StringBuilder();
                while(totalBytesRead < rgbLengths[j]){
                    bytesRead = sbc.read(buf);
                    buf.flip();

//                    while(buf.hasRemaining()){
//                        sbbuf.append(String.format("%02x", buf.get()));
//                    }
//                    buf.rewind();

                    if(totalBytesRead+bytesRead>=rgbLengths[j]){
                        md.update(buf.array(), buf.position(), (rgbLengths[j]-totalBytesRead));
                    } else {
                        md.update(buf);
                    }

                    totalBytesRead += bytesRead;
                    buf.rewind();
                }
//                System.out.println(sbbuf.toString());
            }

//            int lastIndex = 0;
//            int curIndex = 0;
//            for(int j=0; j<rgbIndexes.length; j++){
//                // Do not assume split data is in sequential order in the file.
//                // If next split of data comes earlier in the TIFF byte stream
//                // than the last split, reset the inputstream and skip to the
//                // split index
//                if(rgbIndexes[j]<lastIndex) {
//                    is.reset();
//                    curIndex=0;
//                }
//
//                dis.skip(rgbIndexes[j]-curIndex);
//
//                for(int i=0; i<rgbLengths[j]; i++) {
//                    dis.read();
//                }
//
//                curIndex=rgbIndexes[j]+rgbLengths[j];
//                lastIndex = rgbIndexes[j];
//            }
        }

        StringBuilder digest = new StringBuilder();
        for (byte b: md.digest()){
            digest.append(String.format("%02x", b));
        }

        return digest.toString();
    }


    public static void main(String[] args){
        try {
            String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
            //String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips_split_data.tiff");
            //String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/non_sequential_rgbstrips_split_data.tiff");
            System.out.println(hash);
            //readTIFF("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
