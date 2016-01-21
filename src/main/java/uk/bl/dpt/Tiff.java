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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing a TIFF file.
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

    /**
     * Returns the IFD at the specified index.
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public IFD getIFD(int index) throws IndexOutOfBoundsException{
        if(index < 0 || index > ifds.size()){
            throw new IndexOutOfBoundsException();
        }
        return ifds.get(index);
    }

    /**
     * Adds the specified IFD to an ArrayList of IFDs in this TIFF
     * @param ifd
     */
    public void addIFD(IFD ifd){
        if(ifds==null){
            ifds = new ArrayList<>();
        }
        if(ifd!=null) {
            ifds.add(ifd);
        }
    }

    /**
     * Returns the offsets of the RGB data splits for the first subfile (IFD) within this TIFF.
     * @return  Integer[]   offsets of the RGB data for the first subfile
     */
    public Integer[] getRGBOffset(){
        return (Integer[]) getIFD(0).getDirectory(IFDTag.StripOffsets).getValue();
    }

    /**
     * Returns the lengths of the RGB data splits for the first subfile (IFD) within this TIFF.
     * @return  Integer[]   lengths of the RGB data for the first subfile
     */
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
