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
public class ChecksumTest {

    private static String testTiff   = "/rgbstrips.tiff";
    private static String testTiffFullCS = "c31ffd70cb87c269d69f2b11c7e09ec4";
    private static String testTiffRGBCS = "5478865efdfc945d291b584402d34a33";

    private static String testTiffSplit = "/rgbstrips_split_data.tiff";
    private static String testTiffFullCS_split = "a59d833a29c12fe16d893aea0e1ee8c6";

    /**
     * Checksum basic test TIFF.
     */
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

    /**
     * Checksum RGB data of basic test TIFF
     */
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

    /**
     * Checksum split data test TIFF
     */
    @Test
    public void checksumSplitTiff(){
        try {
            URL url = getClass().getResource(testTiffSplit);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = JTifixity.checksum(f.getPath());
            assertEquals(testTiffFullCS_split, jtifcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checksum RGB data of split data test TIFF. Should be equivalent of
     * basic test TIFF (i.e. the RGB data is the same)
     */
    @Test
    public void checksumSplitRGBdata() {
        try {
            URL url = getClass().getResource(testTiffSplit);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = JTifixity.checksumRGB(f.getPath());
            assertEquals(testTiffRGBCS, rgbcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
