package uk.bl.dpt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by pmay on 06/05/2015.
 */
public class IFDTest {

    @Test
    public void emptyIFDTest(){
        IFD ifd = new IFD();
        assertEquals(0, ifd.numberOfDirectories());
    }

    /**
     * Tests that a correct stripoffset is returned for a TIFF where the image
     * data is all in one location, i.e. not split
     */
    @Test
    public void nonSplitStripOffsetTest(){
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 1, new Integer[]{8});
        assertEquals(1, ifd.numberOfDirectories());

        IFD.Directory<Integer> directory = ifd.getDirectory(IFDTag.StripOffsets);
        assertArrayEquals(new Integer[]{8}, directory.getValue());
        assertEquals(new Integer(8), directory.getValue()[0]);
    }

    /**
     * Tests that the correct stripoffset locations are returned for a TIFF whose
     * image data is split into 2 locations.
     */
    @Test
    public void twoSplitStripOffsetTest(){
        IFD ifd = new IFD();
        ifd.addDirectory(IFDTag.StripOffsets, IFDType.LONG, 2, new Integer[]{8, 0x1e8});
        assertEquals(1, ifd.numberOfDirectories());

        IFD.Directory<Integer> directory = ifd.getDirectory(IFDTag.StripOffsets);
        assertEquals(2, directory.getCount());
        assertArrayEquals(new Integer[]{8, 0x1e8}, directory.getValue());
        assertEquals(new Integer(8), directory.getValue()[0]);
        assertEquals(new Integer(0x1e8), directory.getValue()[1]);
    }
}
