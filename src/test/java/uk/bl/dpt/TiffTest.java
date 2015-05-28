package uk.bl.dpt;

import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.*;

/**
 * Created by pmay on 06/05/2015.
 */
public class TiffTest {

    @Test
    public void emptyTiffTest(){
        Tiff tiff = new Tiff();
        assertEquals(ByteOrder.LITTLE_ENDIAN, tiff.getByteOrder());
        assertEquals(0, tiff.numberOfIfds());
    }

    @Test
    public void nonSplitTiff(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        tiff.addIFD(ifd);

        assertEquals(1, tiff.numberOfIfds());
        assertEquals(1, tiff.getIFD(0).numberOfDirectories());

        IFD.Directory<Integer> directory = tiff.getIFD(0).getDirectory(IFDTag.StripOffsets);
        assertArrayEquals(new Integer[]{8}, directory.getValue());
        assertEquals(new Integer(8), directory.getValue()[0]);
    }

    /**
     * Tests that the correct stripOffset is returned from the TIFF convenience method when
     * the image data is not split (i.e. it is all in one location).
     */
    @Test
    public void nonSplitRGBConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getRGBOffset();
        assertArrayEquals(new Integer[]{8}, rgbOffsets);
    }

    /**
     * Tests that the correct stripOffsets are returned from the TIFF convenience method when the
     * image data is split into two locations.
     */
    @Test
    public void splitRGBConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 2, new Integer[]{8, 0x1e8});
        tiff.addIFD(ifd);

        Integer[] rgbOffsets = tiff.getRGBOffset();
        assertArrayEquals(new Integer[]{8, 0x1e8}, rgbOffsets);
    }

    /**
     * Tests that the correct length of RGB data is retrieved from Tiff object when the data is
     * not split.
     */
    @Test
    public void nonSplitRGBLengthConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        ifd.addDirectory(IFDTag.StripByteCounts, IFDType.LONG, 1, new Integer[]{300});
        tiff.addIFD(ifd);

        Integer[] rgbLength = tiff.getRGBLength();
        assertArrayEquals(new Integer[]{300}, rgbLength);
    }

    @Test
    public void twoSplitRGBLengthConvenienceMethod(){
        Tiff tiff = new Tiff();
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8, 0x1e8});
        ifd.addDirectory(IFDTag.StripByteCounts, IFDType.LONG, 1, new Integer[]{150, 150});
        tiff.addIFD(ifd);

        Integer[] rgbLength = tiff.getRGBLength();
        assertArrayEquals(new Integer[]{150, 150}, rgbLength);
    }
}
