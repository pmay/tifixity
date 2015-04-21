package uk.bl.dpt;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pmay on 20/04/2015.
 */
public class Tiff {

    private ByteOrder   byteOrder   = null;
    ArrayList<IFD>      ifds        = null;

    /**
     * Default, no-arg constructor
     */
    public Tiff(){
        this.ifds = new ArrayList<>();
    }

    public Tiff(ByteOrder byteOrder){
        this();
        this.byteOrder = byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder){
        this.byteOrder = byteOrder;
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

    public long getRGBOffset(){
        return ifds.get(0).getTagValue(IFDTag.StripOffsets);
    }

    public long getRGBLength(){
        return ifds.get(0).getTagValue(IFDTag.StripByteCounts);
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
