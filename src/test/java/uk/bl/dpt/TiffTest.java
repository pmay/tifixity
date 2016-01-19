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

import java.nio.ByteOrder;

import static org.junit.Assert.*;

/**
 * Tests relating to the TIFF Class.
 *
 * Tests for:
 *   1) creating an empty TIFF object
 *   2) creating a TIFF with a single RGB strip and retrieval of RGB strip data (StripOffset, StripByteCounts)
 *   3) creating a TIFF with a single RGB strip and retrieval of RGB strip data (StripOffset, StripByteCounts)
 *      via convenience methods
 *   4) creating a TIFF with two RGB strips and retrieval of RGB strip data (StripOffsets, StripByteCounts)
 *      via convenience methods
 *
 * Todo tests:
 *   5)
 */
public class TiffTest {

    /**
     * 1: Tests that an empty TIFF object can be created.
     */
    @Test
    public void emptyTiffTest(){
        Tiff tiff = new Tiff();
        assertEquals(ByteOrder.LITTLE_ENDIAN, tiff.getByteOrder());
        assertEquals(0, tiff.numberOfIfds());
    }

    /**
     * 2: Tests that a simple TIFF object with a single Strip of RGB data can
     * be created and retrieved.
     */
    @Test
    public void nonSplitRGBTiff(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        ifd.addDirectory(IFDTag.StripByteCounts, IFDType.LONG, 1, new Integer[]{300});
        tiff.addIFD(ifd);

        assertEquals(1, tiff.numberOfIfds());
        assertEquals(2, tiff.getIFD(0).numberOfDirectories());

        IFD.Directory<Integer> directory = tiff.getIFD(0).getDirectory(IFDTag.StripOffsets);
        assertArrayEquals(new Integer[]{8}, directory.getValue());
        assertEquals(new Integer(8), directory.getValue()[0]);

        directory = tiff.getIFD(0).getDirectory(IFDTag.StripByteCounts);
        assertArrayEquals(new Integer[]{300}, directory.getValue());
        assertEquals(new Integer(300), directory.getValue()[0]);
    }

    /**
     * 3: Tests that the correct stripOffset is returned from the TIFF convenience method when
     * the image data is not split (i.e. it is all in one location).
     */
    @Test
    public void nonSplitRGBConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        ifd.addDirectory(IFDTag.StripByteCounts, IFDType.LONG, 1, new Integer[]{300});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getRGBOffset();
        assertArrayEquals(new Integer[]{8}, rgbOffsets);

        Integer[] rgbByteCounts = tiff.getRGBLength();
        assertArrayEquals(new Integer[]{300}, rgbByteCounts);
    }

    /**
     * 4: Tests that the correct stripOffsets are returned from the TIFF convenience method when the
     * image data is split into two locations.
     */
    @Test
    public void twoSplitRGBConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 2, new Integer[]{8, 0x1e8});
        ifd.addDirectory(IFDTag.StripByteCounts, IFDType.LONG, 1, new Integer[]{150, 150});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getRGBOffset();
        assertArrayEquals(new Integer[]{8, 0x1e8}, rgbOffsets);

        Integer[] rgbLengths = tiff.getRGBLength();
        assertArrayEquals(new Integer[]{150, 150}, rgbLengths);
    }

}
