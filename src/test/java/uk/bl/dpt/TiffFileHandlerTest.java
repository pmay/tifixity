/**
 * Copyright 2016 Peter May
 * Author: Peter May
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.bl.dpt;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests relating to API access of the TiffFileHandler entity.
 *
 * Tests for:
 *  1) Loading of single-strip RGB data into TIFF object
 *  2) Reading correct RGB offset and data length from single-strip RGB data
 *  3) Reading correct RGB offset and data length from multi-strip RGB data
 *  4) Reading correct RGB offset and data length from non-sequential multi-strip RGB data
 *
 *  Todo tests:
 *  5)
 */
public class TiffFileHandlerTest {

    private static String testTiff        = "/rgbstrips.tiff";
    private static String splitTestTiff   = "/rgbstrips_split_data.tiff";
    private static String nonSeqSplitTiff = "/non_sequential_rgbstrips_split_data.tiff";

    /**
     * 1: Tests that a TIFF with a single strip of RGB data can be read into a TIFF object.
     */
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

    /**
     * 2: Tests that the RGB offset and data length can be read from a TIFF objected
     *    created from a TIFF containing a single Strip of RGB data.
     */
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

    /**
     * 3: Tests that the RGB offsets and data lengths can be read from a TIFF
     *    file with two Strips of RGB data.
     */
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

    /**
     * 4: Tests that the RGB offsets and data lengths can be read from a TIFF
     *    file containing two Strips of RGB data not in sequential order (i.e.
     *    bytes for the second half of RGB data occur before bytes for the
     *    first half of RGB data.
     */
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
