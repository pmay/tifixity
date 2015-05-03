package uk.bl.dpt;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by pmay on 23/04/2015.
 */
public class TiffFileHandlerTest {

    private static String testTiff   = "/rgbstrips.tiff";

    @Test
    public void loadFileAndChecksum() {
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

            assertEquals(8L, tiff.getRGBOffset());          // RGB data starts at byte 8
            assertEquals(300L, tiff.getRGBLength());        // RGB data is 300 bytes
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
