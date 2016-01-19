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
 * Enumeration of TIFF v6 IFD Directory Tags
 */
public enum IFDTag {
    NewSubfileType (254),
    SubfileType (255),
    ImageWidth (256),
    ImageLength (257),
    BitsPerSample (258),
    Compression (259),
    PhotometricInterpretation (262),
    Thresholding (263),
    CellWidth (264),
    CellLength (265),
    FillOrder (266),
    DocumentName (269),                 // TIFF 6.0 Section 12
    ImageDescription (270),
    Make (271),
    Model (272),
    StripOffsets (273),
    Orientation (274),
    SamplesPerPixel (277),
    RowsPerStrip (278),
    StripByteCounts (279),
    MinSampleValue (280),
    MaxSampleValue (281),
    XResolution (282),
    YResolution (283),
    PlanarConfiguration (284),
    FreeOffsets (288),
    FreeByteCounts (289),
    GrayResponseUnit (290),
    GrayResponseCurve (291),
    ResolutionUnit (296),
    Software (305),
    DateTime (306),
    Artist (315),
    HostComputer (316),
    ColorMap (320),
    ExtraSamples (338),
    Copyright (33432);

    private static HashMap<Integer, IFDTag> lookup;
    private final Integer value;

    IFDTag(Integer value){
        this.value = value;
    }

    static {
        lookup = new HashMap<Integer, IFDTag>();
        for(IFDTag t: IFDTag.values()){
            lookup.put(t.getTypeValue(), t);
        }
    }

    public static IFDTag getTag(Integer value){
        return lookup.get(value);
    }

    public Integer getTypeValue(){
        return this.value;
    }
}
