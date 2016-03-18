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
 * Enumeration of TIFF v6 IFD DirectoryEntry Types
 */
public enum IFDType {
    BYTE ((short) 1, (short) 1),
    ASCII ((short) 2, (short) 1),
    SHORT ((short) 3, (short) 2),
    LONG ((short) 4, (short) 4),
    RATIONAL ((short) 5, (short) 8),
    SBYTE ((short) 6, (short) 1),
    UNDEFINED ((short) 7, (short) 1),
    SSHORT ((short) 8, (short) 2),
    SLONG ((short) 9, (short) 4),
    SRATIONAL ((short) 10, (short) 8),
    FLOAT ((short) 11, (short) 4),
    DOUBLE ((short) 12, (short) 8);

    private static HashMap<Short, IFDType> lookup;
    private final Short bytevalue;                  // value assigned in the TIFF spec
    private final Short numBytes;                   // number of bytes 1 unit of this type takes

    IFDType(Short value, Short numBytes){
        this.bytevalue = value;
        this.numBytes  = numBytes;
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

    public Short getNumBytes(){
        return this.numBytes;
    }
}
