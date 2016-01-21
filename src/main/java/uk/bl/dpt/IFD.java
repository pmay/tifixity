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

import java.util.HashMap;

/**
 * Class representing a TIFF IFD.
 *
 * TIFF v6, pg 14:
 * An Image File Directory (IFD) consists of a 2-byte count of the number of directory
 * entries (i.e., the number of fields), followed by a sequence of 12-byte field
 * entries, followed by a 4-byte offset of the next IFD (or 0 if none). (Do not forget to
 * write the 4 bytes of 0 after the last IFD.)
 *
 * There must be at least 1 IFD in a TIFF file and each IFD must have at least one entry.
 */
public class IFD {
    private long offset = -1;
    private HashMap<IFDTag, Directory> directories = null;

    protected IFD(){
        directories = new HashMap();
    }

    public IFD(long offset){
        this();
        this.offset = offset;
    }

    public long getOffset(){
        return offset;
    }

    public void setOffset(long offset){
        this.offset = offset;
    }

    public void addDirectory(IFDTag tag, IFDType type, int count, Object[] value) {
        switch(type){
            case ASCII:
                directories.put(tag, new Directory<Character>(tag, type, count, (Character[]) value));
                break;
            default:
                directories.put(tag, new Directory<Integer>(tag, type, count, (Integer[]) value));
        }
    }

    public int numberOfDirectories(){
        return directories.size();
    }

//    public int getTagValue(IFDTag tag){
//        return directories.get(tag).value[0];
//    }

    public Directory getDirectory(IFDTag tag){
        return directories.get(tag);
    }

    /**
     * Class representing a 12-byte field entry in the IFD.
     *
     * TIFF v6, pg 14:
     * Each 12-byte IFD entry has the following format:
     *   - Bytes 0-1 The Tag that identifies the field.
     *   - Bytes 2-3 The field Type.
     *   - Bytes 4-7 The number of values, Count of the indicated Type.
     *   - Bytes 8-11 The Value Offset, the file offset (in bytes) of the Value for the field.
     *                The Value is expected to begin on a word boundary; the corresponding
     *                Value Offset will thus be an even number. This file offset may
     *                point anywhere in the file, even after the image data.
     * @param <T>
     */
    public class Directory<T> {
        private IFDTag  tag;
        private IFDType type;
        private int     count;
        private T[]     value;

        /**
         * Construct a new Directory entry
         * @param tag
         * @param type
         * @param count
         * @param value
         */
        public Directory(IFDTag tag, IFDType type, int count, T[] value){
            this.tag = tag;
            this.type = type;
            this.count = count;
            this.value = value;
        }

        public int getCount(){
            return count;
        }

        public T[] getValue(){
            return value;
        }

        public String toString(){
            StringBuffer buf = new StringBuffer("Tag: ").append(tag).append(" (").append(tag.getTypeValue()).append(")");
            buf.append("\tType: ").append(type).append(" (").append(type.getTypeValue()).append(")");
            buf.append("\tCount: ").append(count);
            buf.append("\tValue: ");

            for(int j=0; j<value.length; j++){
                switch(type) {
                    case ASCII:
                        buf.append((Character) value[j]);
                        break;
                    default:
                        buf.append(String.format("%02x", ((Integer) value[j]) & 0xFF));
                        buf.append(" (").append((Integer) value[j]).append(")");

                        if (j+1<value.length){
                            buf.append(", ");
                        }
                }
            }

            return buf.toString();
        }

//        private static String printHexIntArray(Integer[] value){
//            Integer[] valCopy = value.clone();
//            StringBuffer sb = new StringBuffer();
//            //sb.append("(").append(value).append(")");
//
//            for(int j=0; j<valCopy.length; j++) {
//                for (int i = 0; i < 4; i++) {
//                    sb.insert(0, String.format("%02x ", valCopy[j] & 0xFF));
//                    valCopy[j] = valCopy[j] >> 8;
//                }
//                sb.insert(0, ", ");
//            }
//
//            return sb.toString();
//        }
    }
}
