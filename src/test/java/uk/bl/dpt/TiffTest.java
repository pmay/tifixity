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
 *   5) retrieving the compression of a single image TIFF file
 *   6) Two subfiles
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
        assertEquals(0, tiff.numberOfIFDs());
    }

    /**
     * 2: Tests that a simple TIFF object with a single Strip of RGB data can
     * be created and retrieved.
     */
    @Test
    public void nonSplitRGBTiff(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectoryEntry(IFDTag.StripOffsets, IFDType.LONG, 1, 0L, new Integer[]{8});
        ifd.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.LONG, 1, 0L, new Integer[]{300});
        tiff.addIFD(ifd);

        assertEquals(1, tiff.numberOfIFDs());
        assertEquals(2, tiff.getIFD(0).numberOfDirectoryEntries());

        IFD.DirectoryEntry<Integer> directoryEntry = tiff.getIFD(0).getDirectoryEntry(IFDTag.StripOffsets);
        assertArrayEquals(new Integer[]{8}, directoryEntry.getValue());
        assertEquals(new Integer(8), directoryEntry.getValue()[0]);

        directoryEntry = tiff.getIFD(0).getDirectoryEntry(IFDTag.StripByteCounts);
        assertArrayEquals(new Integer[]{300}, directoryEntry.getValue());
        assertEquals(new Integer(300), directoryEntry.getValue()[0]);
    }

    /**
     * 3: Tests that the correct stripOffset is returned from the TIFF convenience method when
     * the image data is not split (i.e. it is all in one location).
     */
    @Test
    public void nonSplitRGBConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectoryEntry(IFDTag.StripOffsets, IFDType.LONG, 1, 0L, new Integer[]{8});
        ifd.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.LONG, 1, 0L, new Integer[]{300});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getImageDataOffsets(0);
        assertArrayEquals(new Integer[]{8}, rgbOffsets);

        Integer[] rgbByteCounts = tiff.getImageDataLengths(0);
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
        ifd.addDirectoryEntry(IFDTag.StripOffsets, IFDType.LONG, 2, 0L, new Integer[]{8, 0x1e8});
        ifd.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.LONG, 1, 0L, new Integer[]{150, 150});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getImageDataOffsets(0);
        assertArrayEquals(new Integer[]{8, 0x1e8}, rgbOffsets);

        Integer[] rgbLengths = tiff.getImageDataLengths(0);
        assertArrayEquals(new Integer[]{150, 150}, rgbLengths);
    }

    /**
     * 5: Tests that the compression of a single image TIFF can be retrieved.
     */
    @Test
    public void singleImageNoCompressionRetrieval(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectoryEntry(IFDTag.StripOffsets, IFDType.LONG, 1, 0L, new Integer[]{8});
        ifd.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.LONG, 1, 0L, new Integer[]{300});
        ifd.addDirectoryEntry(IFDTag.Compression, IFDType.SHORT, 1, 0L, new Integer[]{1});
        tiff.addIFD(ifd);

        assertEquals((Integer) 1, (Integer) tiff.getCompression(0));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * 6: TIFF file with two sub files
     */
    @Test
    public void twoSubFiles(){
        Tiff tiff = new Tiff();

        IFD ifd1 = new IFD();
        ifd1.addDirectoryEntry(IFDTag.StripOffsets, IFDType.SHORT, 1, 0L, new Integer[]{8});
        ifd1.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.SHORT, 1, 0L, new Integer[]{30});

        IFD ifd2 = new IFD();
        ifd2.addDirectoryEntry(IFDTag.StripOffsets, IFDType.SHORT, 1, 0L, new Integer[]{16});
        ifd2.addDirectoryEntry(IFDTag.StripByteCounts, IFDType.SHORT, 1, 0L, new Integer[]{40});

        tiff.addIFD(ifd1);
        tiff.addIFD(ifd2);

        assertEquals((Integer) 2, (Integer) tiff.numberOfIFDs());
        assertEquals(ifd1, tiff.getIFD(0));
        assertEquals(ifd2, tiff.getIFD(1));

        thrown.expect(IndexOutOfBoundsException.class);
        tiff.getIFD(3);

        // Check Offsets
        Integer[] rgbOffsets = tiff.getImageDataOffsets(0);
        assertArrayEquals(new Integer[]{8}, rgbOffsets);

        rgbOffsets = tiff.getImageDataOffsets(1);
        assertArrayEquals(new Integer[]{16}, rgbOffsets);

        // Check lengths
        Integer[] rgbLengths = tiff.getImageDataLengths(0);
        assertArrayEquals(new Integer[]{30}, rgbLengths);
        rgbLengths = tiff.getImageDataLengths(1);
        assertArrayEquals(new Integer[]{40}, rgbLengths);
    }
}
