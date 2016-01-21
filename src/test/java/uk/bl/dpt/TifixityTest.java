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
 * Tests relating to the checksumming functionality provided by Tifixity
 *
 * Tests for:
 *  1) File and Image MD5 checks for single strip TIFF
 *  2) Image MD5 check for sequential 2-strip TIFF with strips referenced in sequential order
 *  3) Image MD5 check for sequential 2-strip TIFF with strips referenced in reverse order
 *  4) Image MD5 check for non-sequential 2-strip TIFF with strips referenced in sequential order
 *  5) Image MD5 check for non-sequential 2-strip TIFF with strips referenced in reverse order
 */
public class TifixityTest {

    // 1: TIFF containing single Strip of RGB data
    private static String singleStrip               = "/T_one_strip.tiff";
    private static String singleStrip_CS            = "d987f537e0e252df1ebb0427a2bd83c6";
    private static String singleStrip_CS_RGB        = "1d4808fbbc37c098520c4e927cccf332";

    /**
     * Check MD5 of entire Single Strip TIFF.
     */
    @Test
    public void checkSingleStrip_MD5() {
        try {
            URL url = getClass().getResource(singleStrip);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumFile(f.getPath());
            assertEquals(singleStrip_CS, jtifcs);
        } catch (Exception e){
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 2: TIFF containing two Strips of RGB data in sequential order (i.e. within the file
    //    the Strips are ordered by top half of image followed by bottom half) and the
    //    Strips are referenced in Strip order.
    //    Image looks correct.
    private static String twoStrips_seq_normal      = "/T_two_strips_seq.tiff";
    private static String twoStrips_seq_normal_CS   = "cf657425a19bb9d96d8b631ba259c636";
    // RGB CS same as singleStrip_CS_RGB

    /**
     * Check RGB data MD5 in a sequential two-Split split-ordered TIFF.
     * Expect MD5 == single strip RGB MD5
     */
    @Test
    public void checkTwoSplitSeqRGB_MD5(){
        try {
            URL url = getClass().getResource(twoStrips_seq_normal);
            File f = Paths.get(url.toURI()).toFile();
            String jtifcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStrip_CS_RGB, jtifcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 3: TIFF containing two Strips of RGB data in sequential order (i.e. within the file
    //    the Strips are ordered by top half of image followed by bottom half) and the
    //    Strips are referenced in reverse Strip order (i.e. 2nd Strip comes first)
    //    Image looks incorrect.
    private static String twoStrips_seq_reverse        = "/T_two_strips_seq_reverse.tiff";
    private static String twoStrips_seq_reverse_CS     = "52d28f6d2051ba80256fb5087fe006bd";
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 4: TIFF containing two Strips of RGB data in non-sequential order (i.e. within the
    //    file the Strips are ordered by bottom half of image followed by top half) and the
    //    Strips are referenced in Strip order (i.e. bottom of image appears at the top).
    //    Image looks incorrect.
    private static String twoStrips_nonseq_normal        = "/T_two_strips_non_seq.tiff";
    private static String twoStrips_nonseq_normal_CS     = "de7df0b6a10ba6e893baaa836f22aca1";
    private static String twoStrips_nonseq_normal_CS_RGB = "b06b65a47188153c37c8229a95b931db";

    /**
     * Check RGB data MD5 in a non-sequential two-Split split-ordered
     * TIFF.
     */
    @Test
    public void checkTwoSplitNonSeqRGB_MD5(){
        try {
            URL url = getClass().getResource(twoStrips_nonseq_normal);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(twoStrips_nonseq_normal_CS_RGB, rgbcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 5: TIFF containing two Strips of RGB data in non-sequential order (i.e. within the
    //    file the Strips are ordered by bottom half of image followed by top half) and the
    //    Strips are referenced in reverse Strip order (i.e. 2nd Strip comes first)
    //    Image looks correct.
    private static String twoStrips_nonseq_reverse       = "/T_two_strips_non_seq_reverse.tiff";
    private static String twoStrips_nonseq_reverse_CS    = "5cebae87db850f9884fde4d40ccc2e63";
    // RGB CS same as singleStrip_CS_RGB

    /**
     * Check RGB data MD5 in a non-sequential two-Split reverse split-ordered
     * TIFF.
     * Expect MD5 == single strip RGB MD5
     */
    @Test
    public void checkTwoSplitNonSeqReverseRGB_MD5(){
        try {
            URL url = getClass().getResource(twoStrips_nonseq_reverse);
            File f = Paths.get(url.toURI()).toFile();
            String rgbcs = Tifixity.checksumImage(f.getPath(), 0);
            assertEquals(singleStrip_CS_RGB, rgbcs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
