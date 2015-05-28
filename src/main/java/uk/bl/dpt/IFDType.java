package uk.bl.dpt;

import java.util.HashMap;

/**
 * Created by pmay on 18/04/2015.
 */
public enum IFDType {
    BYTE ((short) 1),
    ASCII ((short) 2),
    SHORT ((short) 3),
    LONG ((short) 4),
    RATIONAL ((short) 5),
    SBYTE ((short) 6),
    UNDEFINED ((short) 7),
    SSHORT ((short) 8),
    SLONG ((short) 9),
    SRATIONAL ((short) 10),
    FLOAT ((short) 11),
    DOUBLE ((short) 12);

    private static HashMap<Short, IFDType> lookup;
    private final Short bytevalue;
    IFDType(Short value){
        this.bytevalue = value;
    }

    static {
        lookup = new HashMap<Short, IFDType>();
        for(IFDType t: IFDType.values()){
            lookup.put(t.getTypeValue(), t);
        }
    }

    public static IFDType getType(Short value){
        return lookup.get(value);
    }

    public Short getTypeValue(){
        return this.bytevalue;
    }
}
