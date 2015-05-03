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
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file);
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


    public static void main(String[] args){
        try {
            String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
            //String hash = checksumRGB("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips_split_data.tiff");
            System.out.println(hash);
            //readTIFF("C:/Users/pmay/Repos/jTifixity/src/test/resources/rgbstrips.tiff");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
