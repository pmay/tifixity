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

    public void addDirectory(IFDTag tag, IFDType type, int count, int[] value) {
        directories.put(tag, new Directory(tag, type, count, value));
    }

    public int numberOfDirectories(){
        return directories.size();
    }

    public int getTagValue(IFDTag tag){
        return directories.get(tag).value[0];
    }

    public class Directory {
        private IFDTag  tag;
        private IFDType type;
        private int     count;
        private int[]   value;

        public Directory(IFDTag tag, IFDType type, int count, int[] value){
            this.tag = tag;
            this.type = type;
            this.count = count;
            this.value = value;
        }
    }
}
