package uk.bl.dpt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by pmay on 23/04/2015.
 */
public class TiffFileHandler {

    public static Tiff loadTiffFromFile(String file) throws IOException {
        Tiff tiff = new Tiff(ByteOrder.LITTLE_ENDIAN);

        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(file))) {
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
            int ifdoffset = buf.order(tiff.getByteOrder()).getInt();
            System.out.println("IFD Offset: "+ifdoffset);

            // read the IFD starting at the specified offset and add to the specified tiff
            readIFD(sbc, ifdoffset, tiff);

            return tiff;
        }
    }

    private static void readIFD(SeekableByteChannel sbc, long offset, Tiff tiff) throws IOException {
        IFD ifd = new IFD(offset);

        ByteBuffer buf = ByteBuffer.allocate(8);
        sbc.position(offset);   // set the offset

        int count = sbc.read(buf);
        buf.rewind();
        // 2 byte count + 12 bytes per directory
        short dircount = buf.order(tiff.getByteOrder()).getShort();
        System.out.println("Dir count: "+dircount);

        // read each Directory
        sbc.position(offset+2);
        for(int i=0; i<dircount; i++){
            readDirectory(sbc, tiff.getByteOrder(), ifd);
        }

        tiff.addIFD(ifd);
    }

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
        IFDTag tag = IFDTag.getTag(tagval);

        short typeval = buf.order(byteOrder).getShort();
        IFDType type = IFDType.getType(typeval);

        int count = buf.order(byteOrder).getInt();
        //int[] value = new int[count];

        Integer value[] = {buf.order(byteOrder).getInt()};

        long curPosition = sbc.position();

        if (count>1) {
            // value is a pointer
            Object[] values;
            switch(type){
                case ASCII:
                    values = readArrayChar(sbc.position(value[0]), byteOrder, count);
                    break;
                case SHORT:
                    values = readArrayShort(sbc.position(value[0]), byteOrder, type, count);
                    break;
                case LONG:
                default:
                    values = readArrayLong(sbc.position(value[0]), byteOrder, type, count);
                    break;
            }
            // add directory to IFD object
            ifd.addDirectory(tag, type, count, values);

            //System.out.println("Tag: "+tag+" ("+tagval+")\tType: "+type+" ("+typeval+")\tCount: "+count+"\tValue: ");//+printHexIntArray(values));
        } else {
            // add directory to IFD object
            ifd.addDirectory(tag, type, count, value);
            //System.out.println("Tag: "+tag+" ("+tagval+")\tType: "+type+" ("+typeval+")\tCount: "+count+"\tValue: "+printHexIntArray(value));
        }

        System.out.println(ifd.getDirectory(tag).toString());



        // reset position in channel
        sbc.position(curPosition);
    }

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

    private static String printHexIntArray(Integer[] value){
        Integer[] valCopy = value.clone();
        StringBuffer sb = new StringBuffer();
        //sb.append("(").append(value).append(")");

        for(int j=0; j<valCopy.length; j++) {
            for (int i = 0; i < 4; i++) {
                sb.insert(0, String.format("%02x ", valCopy[j] & 0xFF));
                valCopy[j] = valCopy[j] >> 8;
            }
            sb.insert(0, ", ");
        }

        return sb.toString();
    }
}
