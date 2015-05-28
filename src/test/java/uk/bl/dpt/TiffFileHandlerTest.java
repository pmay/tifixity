package uk.bl.dpt;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by pmay on 23/04/2015.
 */
public class TiffFileHandlerTest {

    private static String testTiff        = "/rgbstrips.tiff";
    private static String splitTestTiff   = "/rgbstrips_split_data.tiff";
    private static String nonSeqSplitTiff = "/non_sequential_rgbstrips_split_data.tiff";

    @Test
    public void loadFile() {
        try {
            URL url = getClass().getResource(testTiff);
            File f = Paths.get(url.toURI()).toFile();
            Tiff tiff = TiffFileHandler.loadTiffFromFile(f.getPath());

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
            Tiff tiff = TiffFileHandler.loadTiffFromFile(f.getPath());

            assertArrayEquals(new Integer[]{8}, tiff.getRGBOffset());          // RGB data starts at byte 8
            assertArrayEquals(new Integer[]{300}, tiff.getRGBLength());        // RGB data is 300 bytes
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void loadSplitRGBFile() {
        try {
            URL url = getClass().getResource(splitTestTiff);
            File f = Paths.get(url.toURI()).toFile();
            Tiff tiff = TiffFileHandler.loadTiffFromFile(f.getPath());

            assertEquals(1, tiff.numberOfIfds());           // Only 1 IFD

            Integer[] rgbOffsets = tiff.getRGBOffset();
            assertArrayEquals(new Integer[]{8, 0x1e8}, rgbOffsets);

            Integer[] rgbLength = tiff.getRGBLength();
            assertArrayEquals(new Integer[]{150, 150}, rgbLength);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void loadNonSeqSplitRGBFile() {
        try {
            URL url = getClass().getResource(nonSeqSplitTiff);
            File f = Paths.get(url.toURI()).toFile();
            Tiff tiff = TiffFileHandler.loadTiffFromFile(f.getPath());

            assertEquals(1, tiff.numberOfIfds());           // Only 1 IFD

            Integer[] rgbOffsets = tiff.getRGBOffset();
            assertArrayEquals(new Integer[]{0x1e8, 8}, rgbOffsets);

            Integer[] rgbLength = tiff.getRGBLength();
            assertArrayEquals(new Integer[]{150, 150}, rgbLength);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
