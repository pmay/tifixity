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
import uk.bl.dpt.types.Rational;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
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

    private static final String testTiff        = "/rgbstrips.tiff";
    private static final String splitTestTiff   = "/rgbstrips_split_data.tiff";
    private static final String nonSeqSplitTiff = "/non_sequential_rgbstrips_split_data.tiff";

    /**
     * 1: Tests that a TIFF with a single strip of RGB data can be read into a TIFF object.
     */
    @Test
    public void loadFile() {
        try {
            URL url = getClass().getResource(testTiff);
            Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(url.toURI()));

            assertEquals(1, tiff.numberOfIFDs());           // Only 1 IFD
            IFD ifd = tiff.getIFD(0);
            assertEquals(308L, ifd.getOffset());            // offset: 308
            assertEquals(17, ifd.numberOfDirectoryEntries());    // 17 directories
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
            Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(url.toURI()));

            assertArrayEquals(new Integer[]{8}, tiff.getImageDataOffsets(0));          // RGB data starts at byte 8
            assertArrayEquals(new Integer[]{300}, tiff.getImageDataLengths(0));        // RGB data is 300 bytes
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
            Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(url.toURI()));

            assertEquals(1, tiff.numberOfIFDs());           // Only 1 IFD

            Integer[] rgbOffsets = tiff.getImageDataOffsets(0);
            assertArrayEquals(new Integer[]{8, 0x1e8}, rgbOffsets);

            Integer[] rgbLength = tiff.getImageDataLengths(0);
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
            Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(url.toURI()));

            assertEquals(1, tiff.numberOfIFDs());           // Only 1 IFD

            Integer[] rgbOffsets = tiff.getImageDataOffsets(0);
            assertArrayEquals(new Integer[]{0x1e8, 8}, rgbOffsets);

            Integer[] rgbLength = tiff.getImageDataLengths(0);
            assertArrayEquals(new Integer[]{150, 150}, rgbLength);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 5: Check Single Strip TIFF full IFD
    private static final String singleStrip = "/T_one_strip.tiff";

    @Test
    public void checkSingleStripIFD(){
        try{
            URL url = getClass().getResource(singleStrip);
            Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(url.toURI()));

            assertEquals(1, tiff.numberOfIFDs());               // Only 1 IFD
            IFD ifd = tiff.getIFD(0);

            assertEquals(17, ifd.numberOfDirectoryEntries());
            assertEquals(0, ifd.getDirectoryEntry(IFDTag.NewSubfileType).getValue()[0]);
            assertEquals(318, ifd.getDirectoryEntry(IFDTag.NewSubfileType).getValueOffset());

            assertEquals(10, ifd.getDirectoryEntry(IFDTag.ImageWidth).getValue()[0]);
            assertEquals(330, ifd.getDirectoryEntry(IFDTag.ImageWidth).getValueOffset());

            assertEquals(10, ifd.getDirectoryEntry(IFDTag.ImageLength).getValue()[0]);
            assertEquals(342, ifd.getDirectoryEntry(IFDTag.ImageLength).getValueOffset());

            assertArrayEquals(new Integer[]{8,8,8}, ifd.getDirectoryEntry(IFDTag.BitsPerSample).getValue());
            assertEquals(518, ifd.getDirectoryEntry(IFDTag.BitsPerSample).getValueOffset());

            assertEquals(1, ifd.getDirectoryEntry(IFDTag.Compression).getValue()[0]);
            assertEquals(366, ifd.getDirectoryEntry(IFDTag.Compression).getValueOffset());

            assertEquals(2, ifd.getDirectoryEntry(IFDTag.PhotometricInterpretation).getValue()[0]);
            assertEquals(378, ifd.getDirectoryEntry(IFDTag.PhotometricInterpretation).getValueOffset());

            assertArrayEquals(new Character[]{'C',':','\\','P','r','o','j','e','c','t','s','\\',
                                              'D','P','T','\\','W','o','r','k','s','p','a','c','e','s','\\',
                                              'T','i','f','f','D','a','t','a','C','h','e','c','k','s','u','m','\\',
                                              't','e','s','t','\\','r','g','b','s','t','r','i','p','s','.','t','i','f','f','\0'},
                              ifd.getDirectoryEntry(IFDTag.DocumentName).getValue());
            assertEquals(524, ifd.getDirectoryEntry(IFDTag.DocumentName).getValueOffset());

            assertArrayEquals(new Character[]{'C','r','e','a','t','e','d',' ','w','i','t','h',' ','G','I','M','P','\0'},
                              ifd.getDirectoryEntry(IFDTag.ImageDescription).getValue());
            assertEquals(588, ifd.getDirectoryEntry(IFDTag.ImageDescription).getValueOffset());

            assertEquals(8, ifd.getDirectoryEntry(IFDTag.StripOffsets).getValue()[0]);
            assertEquals(414, ifd.getDirectoryEntry(IFDTag.StripOffsets).getValueOffset());

            assertEquals(1, ifd.getDirectoryEntry(IFDTag.Orientation).getValue()[0]);
            assertEquals(426, ifd.getDirectoryEntry(IFDTag.Orientation).getValueOffset());

            assertEquals(3, ifd.getDirectoryEntry(IFDTag.SamplesPerPixel).getValue()[0]);
            assertEquals(438, ifd.getDirectoryEntry(IFDTag.SamplesPerPixel).getValueOffset());

            assertEquals(64, ifd.getDirectoryEntry(IFDTag.RowsPerStrip).getValue()[0]);
            assertEquals(450, ifd.getDirectoryEntry(IFDTag.RowsPerStrip).getValueOffset());

            assertEquals(300, ifd.getDirectoryEntry(IFDTag.StripByteCounts).getValue()[0]);
            assertEquals(462, ifd.getDirectoryEntry(IFDTag.StripByteCounts).getValueOffset());

            Rational e = new Rational(72,1);
            Rational r = (Rational) ifd.getDirectoryEntry(IFDTag.XResolution).getValue()[0];
            assertEquals(new Rational(72,1), r);
            assertEquals(606, ifd.getDirectoryEntry(IFDTag.XResolution).getValueOffset());

            assertEquals(new Rational(72,1), ifd.getDirectoryEntry(IFDTag.YResolution).getValue()[0]);
            assertEquals(614, ifd.getDirectoryEntry(IFDTag.YResolution).getValueOffset());

            assertEquals(1, ifd.getDirectoryEntry(IFDTag.PlanarConfiguration).getValue()[0]);
            assertEquals(498, ifd.getDirectoryEntry(IFDTag.PlanarConfiguration).getValueOffset());

            assertEquals(2, ifd.getDirectoryEntry(IFDTag.ResolutionUnit).getValue()[0]);
            assertEquals(510, ifd.getDirectoryEntry(IFDTag.ResolutionUnit).getValueOffset());
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }
}
