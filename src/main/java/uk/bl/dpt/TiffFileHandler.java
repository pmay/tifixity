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

import uk.bl.dpt.types.Rational;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TIFF File Handler for loading a TIFF into the Tifixity TIFF data model.
 */
public class TiffFileHandler {

    /**
     * Loads a TIFF file into the Tifixity data model.
     * @param file  the TIFF file to load
     * @return
     * @throws IOException
     */
    public static Tiff loadTiffFromFile(String file) throws IOException {
        Path filepath = Paths.get(file);
        return loadTiffFromFile(filepath);
    }

    /**
     * Loads a TIFF file into the Tifixity data model.
     * @param filepath  the {@link java.nio.file.Path} to a TIFF file to load
     * @return
     * @throws IOException
     */
    public static Tiff loadTiffFromFile(Path filepath) throws IOException {
        Tiff tiff = new Tiff(filepath, ByteOrder.LITTLE_ENDIAN);

        try (SeekableByteChannel sbc = Files.newByteChannel(filepath)) {
            ByteBuffer buf = ByteBuffer.allocate(8);

            // read TIFF header
            int count = sbc.read(buf);

            // check byte ordering
            buf.rewind();
            if(buf.get()==0x4D && buf.get()==0x4D){
                tiff.setByteOrder(ByteOrder.BIG_ENDIAN);
            }

            // read IFD offset
            buf.position(4);
            int ifdoffset = 0;

            while((ifdoffset=buf.order(tiff.getByteOrder()).getInt())!=0) {
                if (Tifixity.verbose) System.out.println("IFD Offset: " + ifdoffset);

                // read the IFD starting at the specified offset and add to the specified tiff
                readIFD(sbc, ifdoffset, tiff);

                // read the next IFD offset. Will be 0 if no more
                buf.flip();
                sbc.read(buf);
                buf.rewind();
            }
        }
        return tiff;
    }

    /**
     * Reads the IFD from the specified channel and loads it into the specified TIFF object.
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the IFD from
     * @param offset    the offset (in bytes) from the start of the file where the IFD is
     * @param tiff      the {@link Tiff} object to load results into
     * @throws IOException
     */
    private static void readIFD(SeekableByteChannel sbc, long offset, Tiff tiff) throws IOException {
        IFD ifd = new IFD(offset);

        ByteBuffer buf = ByteBuffer.allocate(8);
        sbc.position(offset);   // set the offset

        int count = sbc.read(buf);
        buf.rewind();
        // 2 byte count + 12 bytes per directory
        short dircount = buf.order(tiff.getByteOrder()).getShort();
        if(Tifixity.verbose) System.out.println("Dir count: "+dircount);

        // read each DirectoryEntry
        sbc.position(offset+2);
        for(int i=0; i<dircount; i++){
            readDirectory(sbc, tiff.getByteOrder(), ifd);
        }

        tiff.addIFD(ifd);
    }

    /**
     * Reads each directory from the specified byte channel and loads into the specified IFD object.
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the Directory from
     * @param byteOrder the byte order of the bytes within the file
     * @param ifd       the {@link IFD} to load the read data into
     * @throws IOException
     */
    private static void readDirectory(SeekableByteChannel sbc, ByteOrder byteOrder, IFD ifd) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(12);
        sbc.read(buf);
        buf.rewind();
        // bytes:
        //  0-1  Tag
        //  2-3  Type
        //  4-7  Count of indicated type
        //  8-11 Value offset
        int tagval = buf.order(byteOrder).getShort();

        short typeval = buf.order(byteOrder).getShort();
        IFDType type = IFDType.getType(typeval);

        int count = buf.order(byteOrder).getInt();

        // get the current position within the file
        long curPosition = sbc.position();

        // if value fits within the value offset field, then the IFDs valueOffset = curPosition
        // if it does not, then the IFDs valueOffset = bytes 8-11
        long offset = curPosition-4;        // curPosition is after reading in the 12byte IFD, so -4
        if (count*type.getNumBytes()>4){
            // value does not fit, so bytes 8-11 are a pointer
            offset = buf.order(byteOrder).getInt();
        }

        // now read the value at the specified offset in the file
        // Note: this currently means bytes 8-11 are re-read if the value fits there.
        Object[] values;
        switch(type){
            case BYTE:
            case ASCII:
                values = readArrayChar(sbc.position(offset), byteOrder, count);
                break;
            case SHORT:
                values = readArrayShort(sbc.position(offset), byteOrder, type, count);
                break;
            case RATIONAL:
                values = readArrayRational(sbc.position(offset), byteOrder, type, count);
                break;
            case LONG:
            default:
                values = readArrayLong(sbc.position(offset), byteOrder, type, count);
                break;
        }
        // add directory to IFD object
        ifd.addDirectoryEntry(tagval, type, count, offset, values);

        if(Tifixity.verbose) System.out.println(ifd.getDirectoryEntry(tagval).toString());

        // reset position in channel
        sbc.position(curPosition);
    }

    /**
     * Reads a TIFF ASCII array from the specified byte channel and returns an array of Characters.
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the character array from
     * @param byteOrder the byte order of the bytes within the file
     * @param length    the number of elements in the character array
     * @return
     * @throws IOException
     */
    private static Character[] readArrayChar(SeekableByteChannel sbc, ByteOrder byteOrder, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length);
        sbc.read(buf);
        buf.rewind();

        Character[] desc = new Character[length];

        for(int i=0; i<length; i++){
            desc[i] = (char) buf.get();
        }

        return desc;
    }

    /**
     * Reads a TIFF short array (16-bit unsigned integer) and returns an array of Integers
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the short array from
     * @param byteOrder the byte order of the bytes within the file
     * @param type      the IFDType of the elements in the array
     * @param length    the number of elements in the short array
     * @return
     * @throws IOException
     */
    private static Integer[] readArrayShort(SeekableByteChannel sbc, ByteOrder byteOrder, IFDType type, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length*2);
        sbc.read(buf);
        buf.rewind();

        Integer[] values = new Integer[length];

        for(int i=0; i<length; i++){
            values[i] = new Integer(buf.order(byteOrder).getShort());
        }

        return values;
    }

    /**
     * Reads a TIFF Long array (32-bit unsigned integer) and returns an array of Integers
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the Long array from
     * @param byteOrder the byte order of the bytes within the file
     * @param type      the IFDType of the elements in the array
     * @param length    the number of elements in the Long array
     * @return
     * @throws IOException
     */
    private static Integer[] readArrayLong(SeekableByteChannel sbc, ByteOrder byteOrder, IFDType type, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length*4);
        sbc.read(buf);
        buf.rewind();

        Integer[] values = new Integer[length];

        for(int i=0; i<length; i++){
            values[i] = buf.order(byteOrder).getInt();
        }

        return values;
    }

    /**
     *
     * @param sbc       the {@link java.nio.channels.SeekableByteChannel} to read the Rational array from
     * @param byteOrder the byte order of the bytes within the file
     * @param type      the IFDType of the elements in the array
     * @param length    the number of elements in the Rational array
     * @return
     * @throws IOException
     */
    private static Rational[] readArrayRational(SeekableByteChannel sbc, ByteOrder byteOrder, IFDType type, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length*8); //Rational = 2*Long
        sbc.read(buf);
        buf.rewind();

        Rational[] values = new Rational[length];

        for(int i=0; i<length; i++){
            values[i] = new Rational(buf.order(byteOrder).getInt(), buf.order(byteOrder).getInt());
        }

        return values;
    }
}
