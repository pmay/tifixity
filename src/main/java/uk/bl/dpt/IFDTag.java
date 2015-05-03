package uk.bl.dpt;

import java.util.HashMap;

/**
 * Created by pmay on 18/04/2015.
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

    private Integer getTypeValue(){
        return this.value;
    }
}
