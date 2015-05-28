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

    private static String testTiff                  = "/rgbstrips.tiff";
    private static String testTiffFullCS            = "c31ffd70cb87c269d69f2b11c7e09ec4";
    private static String testTiffRGBCS             = "5478865efdfc945d291b584402d34a33";

    private static String testTiffSplit             = "/rgbstrips_split_data.tiff";
    private static String testTiffFullCS_split      = "a59d833a29c12fe16d893aea0e1ee8c6";

    private static String testNonSeqTiffSplit       = "/non_sequential_rgbstrips_split_data.tiff";
    private static String testNonSeqRGBCS           = "427de87be70c968f257c1c870833435b";
    private static String testTiffFullCS_split_ns   = "5b885e76221c532d4057d16564ad38e1";

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

    /**
     * Tests the RGB checksum when the rgb data is in non-sequential splits.
     */
    @Test
    public void checksumNonSequentialSplitRGBdata(){
        try {
            URL url = getClass().getResource(testNonSeqTiffSplit);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = JTifixity.checksumRGB(f.getPath());
            assertEquals(testNonSeqRGBCS, rgbcs);
            //assertEquals(testNonSeqRGBCS, rgbcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
