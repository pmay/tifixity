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
 * Enumeration of TIFF v6 IFD DirectoryEntry Tags
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
    DocumentName (269),                 // ext; TIFF 6.0 Section 12
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
    PageName (285),                     // ext
    FreeOffsets (288),
    FreeByteCounts (289),
    GrayResponseUnit (290),
    GrayResponseCurve (291),
    ResolutionUnit (296),
    PageNumber (297),                   // ext
    Software (305),
    DateTime (306),
    Artist (315),
    HostComputer (316),
    Predictor (317),                    // ext; TIFF 6.0 Section 14
    WhitePoint (318),                   // ext; TIFF 6.0 Section 20
    PrimaryChromaticities (319),        // ext; TIFF 6.0 Section 20
    ColorMap (320),
    ExtraSamples (338),
    Copyright (33432),
    UNKNOWN (-1);

    private static HashMap<Integer, IFDTag> lookup;
    private final Integer value;

    IFDTag (Integer value){
        this.value = value;
    }

    static {
        lookup = new HashMap<>();
        for(IFDTag t: IFDTag.values()){
            lookup.put(t.getTagValue(), t);
        }
    }

    /**
     * Returns the IFDTag associated with the specified value or UNKNOWN if not found.
     * @param value the id of the IFDTag to get
     * @return
     */
    public static IFDTag getTag(Integer value){
        IFDTag tag = lookup.get(value);
        if(tag==null){
            tag = IFDTag.UNKNOWN;
        }
        return tag;
    }

    /**
     * Returns the value associated with this IFDTag object.
     * @return
     */
    public Integer getTagValue(){
        return this.value;
    }
}
