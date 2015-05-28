package uk.bl.dpt;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing a TIFF file.
 * @author pmay
 */
public class Tiff {

    private ByteOrder   byteOrder   = null;
    ArrayList<IFD>      ifds        = null;

    /**
     * Default, no-arg constructor. Default to Little Endian Byte order.
     */
    public Tiff(){
        this.ifds = new ArrayList<>();
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Construct a Tiff object specifying the byte order representation
     * used in the file.
     * @param byteOrder the {@link java.nio.ByteOrder} of the Tiff file
     */
    public Tiff(ByteOrder byteOrder){
        this();
        this.byteOrder = byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder){
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder(){
        return this.byteOrder;
    }

    public int numberOfIfds(){
        return ifds.size();
    }

    public IFD getIFD(int index) throws IndexOutOfBoundsException{
        if(index < 0 || index > ifds.size()){
            throw new IndexOutOfBoundsException();
        }
        return ifds.get(index);
    }

    public void addIFD(IFD ifd){
        if(ifds==null){
            ifds = new ArrayList<>();
        }
        if(ifd!=null) {
            ifds.add(ifd);
        }
    }

    public Integer[] getRGBOffset(){
        return (Integer[]) getIFD(0).getDirectory(IFDTag.StripOffsets).getValue();
    }

    public Integer[] getRGBLength(){
        return (Integer[]) getIFD(0).getDirectory(IFDTag.StripByteCounts).getValue();
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }

        Tiff tiffObj = (Tiff) obj;
        if(this.byteOrder!=tiffObj.byteOrder){
            return false;
        }
        if(this.ifds.size()!=tiffObj.numberOfIfds()){
            return false;
        }
        IFD orig, cmp;
        for(int i=0; i<this.ifds.size(); i++){
            orig = getIFD(i);
            cmp  = tiffObj.getIFD(i);
            if(orig.getOffset()!=cmp.getOffset()){
                return false;
            }
            if(orig.numberOfDirectories()!=cmp.numberOfDirectories()){
                return false;
            }
        }
        return true;
    }
}
