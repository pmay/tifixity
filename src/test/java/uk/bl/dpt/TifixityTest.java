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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

/**
 * Tests relating to the checksumming functionality provided by Tifixity
 *
 * Tests for:
 *  0) Invalid file name
 *  1) File and Image MD5 checks for single strip TIFF
 *  2) Image MD5 check for sequential 2-strip TIFF with strips referenced in sequential order
 *  3) Image MD5 check for sequential 2-strip TIFF with strips referenced in reverse order
 *  4) Image MD5 check for non-sequential 2-strip TIFF with strips referenced in sequential order
 *  5) Image MD5 check for non-sequential 2-strip TIFF with strips referenced in reverse order
 *  6) Image MD5 check for compressed single strip TIFF
 *  7) Image MD5 check for single strip bilevel TIFF
 *  8) Image MD5 check for two subfile single strip TIFF
 *  9) Check MD5 for non-image data for single strip TIFF.
 *  10) Check MD5 for non-image data for a two strip TIFF.
 *  11) Image MD5 check for single strip TIFF with exif metadata.
 *  12) IFD MD5 single strip Tiff
 *  13) IFD MD5 of two subfile
 */
public class TifixityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // 0: Incorrect file name
    @Test
    public void incorrectFilePath() throws IOException, NoSuchAlgorithmException {
        thrown.expect(NoSuchFileException.class);
        Tifixity.checksumImage("/missingfile.tiff", 0);
    }

    // 1: TIFF containing single Strip of RGB data
    private static String singleStrip = "/T_one_strip.tiff";
    private static String singleStrip_CS = "0c9e57b795262d11185345c9a172bb40";
    private static String singleStrip_CS_RGB = "1d4808fbbc37c098520c4e927cccf332";

    /**
     * Check MD5 of entire Single Strip TIFF.
     */
    @Test
    public void checkSingleStrip_MD5() {
        try {
            URL url = getClass().getResource(singleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String[] jtifcs = Tifixity.checksumFile(f.getPath());
            assertEquals(singleStrip_CS, jtifcs[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check MD5 of RGB data from Single Strip TIFF
     */
    @Test
    public void checkSingleStripRGB_MD5() {
        try {
            URL url = getClass().getResource(singleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStrip_CS_RGB, rgbcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2: TIFF containing two Strips of RGB data in sequential order (i.e. within the file
    //    the Strips are ordered by top half of image followed by bottom half) and the
    //    Strips are referenced in Strip order.
    //    Image looks correct.
    private static String twoStrips_seq_normal = "/T_two_strips_seq.tiff";
    private static String twoStrips_seq_normal_CS = "cf657425a19bb9d96d8b631ba259c636";
    // RGB CS same as singleStrip_CS_RGB

    /**
     * Check RGB data MD5 in a sequential two-Split split-ordered TIFF.
     * Expect MD5 == single strip RGB MD5
     */
    @Test
    public void checkTwoSplitSeqRGB_MD5() {
        try {
            URL url = getClass().getResource(twoStrips_seq_normal);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStrip_CS_RGB, jtifcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3: TIFF containing two Strips of RGB data in sequential order (i.e. within the file
    //    the Strips are ordered by top half of image followed by bottom half) and the
    //    Strips are referenced in reverse Strip order (i.e. 2nd Strip comes first)
    //    Image looks incorrect.
    private static String twoStrips_seq_reverse = "/T_two_strips_seq_reverse.tiff";
    private static String twoStrips_seq_reverse_CS = "52d28f6d2051ba80256fb5087fe006bd";
    private static String twoStrips_seq_reverse_CS_RGB = "b06b65a47188153c37c8229a95b931db";

    /**
     * Check RGB data MD5 in a sequential two-Split reverse split-ordered
     * TIFF.
     */
    @Test
    public void checkTwoSplitSeqReverseRGB_MD5() {
        try {
            URL url = getClass().getResource(twoStrips_seq_reverse);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(twoStrips_seq_reverse_CS_RGB, rgbcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4: TIFF containing two Strips of RGB data in non-sequential order (i.e. within the
    //    file the Strips are ordered by bottom half of image followed by top half) and the
    //    Strips are referenced in Strip order (i.e. bottom of image appears at the top).
    //    Image looks incorrect.
    private static String twoStrips_nonseq_normal = "/T_two_strips_non_seq.tiff";
    private static String twoStrips_nonseq_normal_CS = "de7df0b6a10ba6e893baaa836f22aca1";
    private static String twoStrips_nonseq_normal_CS_RGB = "b06b65a47188153c37c8229a95b931db";

    /**
     * Check RGB data MD5 in a non-sequential two-Split split-ordered
     * TIFF.
     */
    @Test
    public void checkTwoSplitNonSeqRGB_MD5() {
        try {
            URL url = getClass().getResource(twoStrips_nonseq_normal);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(twoStrips_nonseq_normal_CS_RGB, rgbcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 5: TIFF containing two Strips of RGB data in non-sequential order (i.e. within the
    //    file the Strips are ordered by bottom half of image followed by top half) and the
    //    Strips are referenced in reverse Strip order (i.e. 2nd Strip comes first)
    //    Image looks correct.
    private static String twoStrips_nonseq_reverse = "/T_two_strips_non_seq_reverse.tiff";
    private static String twoStrips_nonseq_reverse_CS = "5cebae87db850f9884fde4d40ccc2e63";
    // RGB CS same as singleStrip_CS_RGB

    /**
     * Check RGB data MD5 in a non-sequential two-Split reverse split-ordered
     * TIFF.
     * Expect MD5 == single strip RGB MD5
     */
    @Test
    public void checkTwoSplitNonSeqReverseRGB_MD5() {
        try {
            URL url = getClass().getResource(twoStrips_nonseq_reverse);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStrip_CS_RGB, rgbcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 6: LZW Compressed TIFF containing single Strip of RGB data
    private static String compressedSingleStrip = "/T_one_strip_compressed_lzw.tiff";
    private static String compressedSingleStrip_CS = "";
    private static String compressedSingleStrip_CS_RGB = "a92273ee43d585707089029dea022578";

    /**
     * Check MD5 of entire Compressed Single Strip TIFF.
     */
    @Test
    public void checkSingleStripCompressed_MD5() {
        try {
            URL url = getClass().getResource(compressedSingleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(compressedSingleStrip_CS_RGB, jtifcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 7: Bilevel one strip tiff
    private static String bilevelSingleStrip = "/T_one_strip_bilevel.tiff";
    private static String bilevelSingleStrip_CS = "";
    private static String bilevelSingleStrip_CS_image = "b556f323078e06f06c3e1988aaad4bc1";

    /**
     * Check MD5 of entire Compressed Single Strip TIFF.
     */
    @Test
    public void checkSingleStripBilevel_MD5() {
        try {
            URL url = getClass().getResource(bilevelSingleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(bilevelSingleStrip_CS_image, jtifcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 8: Image MD5 check for two subfile single strip TIFF
    private static String twoSubfileSingleStrip = "/T_two_subfile_single_strip.tiff";
    private static String twoSubfileSingleStrip_CS = "";
    private static String[] twoSubfileSingleStrip_CS_image = new String[]{"1d4808fbbc37c098520c4e927cccf332",
                                                                          "5702e84a1f688ad20c2fae9a9f18312b"};

    /**
     * Checks the MD5 of a TIFF containing two subfile images
     */
    @Test
    public void checkTwoSubfileSingleStrip_MD5(){
        try{
            URL url = getClass().getResource(twoSubfileSingleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String[] jtifcs = Tifixity.checksumImage(f.getPath());

            assertEquals(2, jtifcs.length);
            assertEquals(twoSubfileSingleStrip_CS_image[0], jtifcs[0]);
            assertEquals(twoSubfileSingleStrip_CS_image[1], jtifcs[1]);
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

    // 9: Checks the MD5 for the remaining non-image data within a single strip TIFF
    private static String singleStripRemainder_CS = "afcadda07883ba826540287cb4509284";

    @Test
    public void checkSingleStripRemainder_MD5(){
        try{
            URL url = getClass().getResource(singleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String[] invcs = Tifixity.checksumFile(f.getPath());

            assertEquals(singleStripRemainder_CS, invcs[1]);
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }

    // 10: Checks the MD5 for the remaining non-image data within a two strip TIFF
    private static String twoStrips_nonseq_remainder_CS = "84302a6da744c2f672b09e02196c3572";

    @Test
    public void checkTwoSplitRemainder_MD5() {
        try {
            URL url = getClass().getResource(twoStrips_nonseq_normal);
            File f = Paths.get(url.toURI()).toFile();
            String[] rgbcs = Tifixity.checksumFile(f.getPath());
            assertEquals(twoStrips_nonseq_remainder_CS, rgbcs[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 11: Image MD5 check of single strip TIFF with exif metadata
    private static String singleStripExif = "/T_one_strip_with_exif.tiff";
    private static String singleStripExif_CS = "2c4d171cf23a234e1b2d0dde354d2069";
    private static String singleStripExif_CS_RGB = "1d4808fbbc37c098520c4e927cccf332";

    /**
     * Check MD5 of image data when TIFF contains exif metadata
     */
    @Test
    public void checkSingleStripExif_MD5() {
        try {
            URL url = getClass().getResource(singleStripExif);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStripExif_CS_RGB, jtifcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 12: IFD MD5 single strip Tiff
    private static String singleStripIFD_CS = "46d59a726eb4c1945acdd333969bb5bf";

    /**
     * Check MD5 of IFD data
     */
    @Test
    public void checkSingleStripIFD_MD5() {
        try {
            URL url = getClass().getResource(singleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumIFD(f.getPath(), 0);
            assertEquals(singleStripIFD_CS, jtifcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 13: IFD MD5 of two subfile
    private static String[] twoSubfileSingleStrip_CS_ifd = new String[]{"1574fdfd58987e1f52d7d867d6f65362",
            "ebf55a51ca852fb37661836019b60244"};

    /**
     * Checks the MD5 of a TIFF containing two subfile images
     */
    @Test
    public void checkTwoSubfileSingleStrip_IFD_MD5(){
        try{
            URL url = getClass().getResource(twoSubfileSingleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String[] jtifcs = Tifixity.checksumIFDs(f.getPath());

            assertEquals(2, jtifcs.length);
            assertEquals(twoSubfileSingleStrip_CS_ifd[0], jtifcs[0]);
            assertEquals(twoSubfileSingleStrip_CS_ifd[1], jtifcs[1]);
        } catch (Exception e) {
            fail("Exception "+e);
        }
    }


}
