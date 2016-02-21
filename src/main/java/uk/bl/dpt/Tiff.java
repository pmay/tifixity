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
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Class representing a TIFF file.
 */
public class Tiff {

    private Path        file        = null;
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

    /**
     * Construct a Tiff object for the specified Tiff file path and the
     * specified byte order representation.
     * @param file      the {@link java.nio.file.Path} of the Tiff file
     * @param byteOrder the {@link java.nio.ByteOrder} of the Tiff file
     */
    public Tiff(Path file, ByteOrder byteOrder){
        this(byteOrder);
        this.file = file;
    }

    /**
     * Sets the byte order of this TIFF file
     * @param byteOrder
     */
    public void setByteOrder(ByteOrder byteOrder){
        this.byteOrder = byteOrder;
    }

    /**
     * Returns the byte order of this TIFF file
     * @return
     */
    public ByteOrder getByteOrder(){
        return this.byteOrder;
    }

    /**
     * Returns the Path for the actual Tiff file
     * @return
     */
    public Path getFilePath(){
        return this.file;
    }


    /**
     * Returns the number of IFDs in this TIFF file
     * @return
     */
    public int numberOfIFDs(){
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
     * @param subFile       the subfile index (IFD) to get the RGB offset from
     * @return  Integer[]   offsets of the RGB data for the first subfile
     */
    public Integer[] getImageDataOffsets(int subFile) throws IndexOutOfBoundsException {
        return (Integer[]) getIFD(subFile).getDirectoryEntry(IFDTag.StripOffsets).getValue();
    }

    /**
     * Returns the lengths of the RGB data splits for the first subfile (IFD) within this TIFF.
     * @param subFile       the sub file index (IFD) to get the strip Length from
     * @return  Integer[]   lengths of the RGB data for the first subfile
     */
    public Integer[] getImageDataLengths(int subFile) throws IndexOutOfBoundsException {
        return (Integer[]) getIFD(subFile).getDirectoryEntry(IFDTag.StripByteCounts).getValue();
    }


    /**
     * Returns the compression value associated for the specified subfile within this TIFF
     * @param subfile
     * @return
     */
    public Integer getCompression(int subfile){
        return (Integer) getIFD(0).getDirectoryEntry(IFDTag.Compression).getValue()[0];
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
        if(this.ifds.size()!=tiffObj.numberOfIFDs()){
            return false;
        }
        IFD orig, cmp;
        for(int i=0; i<this.ifds.size(); i++){
            orig = getIFD(i);
            cmp  = tiffObj.getIFD(i);
            if(orig.getOffset()!=cmp.getOffset()){
                return false;
            }
            if(orig.numberOfDirectoryEntries()!=cmp.numberOfDirectoryEntries()){
                return false;
            }
        }
        return true;
    }
}
