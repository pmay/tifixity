package uk.bl.dpt;

import com.sun.jmx.remote.internal.ClientNotifForwarder;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by pmay on 03/04/2015.
 */
public class ImageLoadTest {

    private static String testTiff   = "/rgbstrips.tiff";
    private static String testTiffFullCS = "c31ffd70cb87c269d69f2b11c7e09ec4";
    private static String testTiffRGBCS = "5478865efdfc945d291b584402d34a33";

    @Test
    public void loadFileAndChecksum() {
        try {
            URL url = getClass().getResource(testTiff);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = JTifixity.checksum(f.getPath());
            assertEquals(testTiffFullCS, jtifcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void loadTestTiff() {
        try {
            URL url = getClass().getResource(testTiff);
            File f = Paths.get(url.toURI()).toFile();
            Tiff tiff = JTifixity.readTIFF(f.getPath());

            assertEquals(1, tiff.numberOfIfds());           // Only 1 IFD
            IFD ifd = tiff.getIFD(0);
            assertEquals(308L, ifd.getOffset());            // offset: 308
            assertEquals(17, ifd.numberOfDirectories());    // 17 directories
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void getRGBOffsetAndLength(){
        try{
            URL url = getClass().getResource(testTiff);
            File f = Paths.get(url.toURI()).toFile();
            Tiff tiff = JTifixity.readTIFF(f.getPath());

            assertEquals(8L, tiff.getRGBOffset());          // RGB data starts at byte 8
            assertEquals(300L, tiff.getRGBLength());        // RGB data is 300 bytes
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void checksumRGBdata() {
        try {
            URL url = getClass().getResource(testTiff);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = JTifixity.checksumRGB(f.getPath());
            assertEquals(testTiffRGBCS, rgbcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
