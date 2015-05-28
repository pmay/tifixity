package uk.bl.dpt;

import java.util.HashMap;

/**
 * Created by pmay on 21/04/2015.
 */
public class IFD {
    private long offset = -1;
    private HashMap<IFDTag, Directory> directories = null;

    public IFD(){
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

    public class Directory<T> {
        private IFDTag  tag;
        private IFDType type;
        private int     count;
        private T[]   value;

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
                        buf.append(String.format("%02x ", ((Integer) value[j]) & 0xFF));
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
