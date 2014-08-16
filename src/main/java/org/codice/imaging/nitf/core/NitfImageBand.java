/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package org.codice.imaging.nitf.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
    Image Band.
*/
public class NitfImageBand {

    private NitfReader reader = null;

    // An enum might have been useful, but this is extensible
    private String imageRepresentation = null;
    private String imageSubcategory = null;
    private int numLUTs = 0;
    private int numEntriesLUT = 0;
    private List<NitfImageBandLUT> luts = new ArrayList<NitfImageBandLUT>();

    private static final int IREPBAND_LENGTH = 2;
    private static final int ISUBCAT_LENGTH = 6;
    private static final int IFC_LENGTH = 1;
    private static final int IMFLT_LENGTH = 3;
    private static final int NLUTS_LENGTH = 1;
    private static final int NELUT_LENGTH = 5;

    /**
        Construct from a NitfReader instance.

        @param nitfReader the reader, positioned to read an image band.
        @throws ParseException if an obviously invalid value is detected during parsing,
        or if another problem occurs during parsing (e.g. end of file).
    */
    public NitfImageBand(final NitfReader nitfReader) throws ParseException {
        reader = nitfReader;
        readIREPBAND();
        readISUBCAT();
        readIFC();
        readIMFLT();
        readNLUTS();
        if (numLUTs > 0) {
            readNELUT();
            for (int i = 0; i < numLUTs; ++i) {
                NitfImageBandLUT lut = new NitfImageBandLUT(reader.readBytesRaw(numEntriesLUT));
                luts.add(lut);
            }
        }
    }

    /**
        Get the image representation for the band (IREPBAND).
        <p>
        This provides information on the properties of this band.
        See MIL-STD-2500C Table A-3 for the interpretation of this field.

        @return the image representation for the band.
    */
    public final String getImageRepresentation() {
        return imageRepresentation;
    }

    /**
        Get the image band subcategory (ISUBCAT).
        <p>
        This provides interpretation of this band within the image
        category. See MIL-STD-2500C for the interpretation of this band.

        @return the image band subcategory.
    */
    public final String getSubCategory() {
        return imageSubcategory;
    }

    /**
        Get the number of lookup tables for an image band (NLUTSn).
        <p>
        This field shall contain the number of LUTs associated with
        the nth band of the image.
        <p>
        LUTs are allowed only if the value of the PVTYPE field is INT or B.
        <p>
        If the nth band of the image is monochromatic, this
        field can contain the value 1 or 2. If the value is 2,
        the first and second LUTs shall map respectively
        the most significant byte and the least significant
        byte of the 16 bit values. NOTE: If a system cannot
        support more than 256 different values, it may use
        only the values of the first LUT. In this case, the
        number of entries in the LUT (NELUTn) may
        exceed 256.
        <p>
        If the nth band of the image is color-coded (the value
        of the IREPBNDn field is LU), this field shall
        contain the value 3. The first, second, and third
        LUTs, in this case, shall map the image to the red,
        green, and blue display bands respectively.
        <p>
        The value 4 is reserved for future use.

        @return the number of lookup tables.
    */
    public final int getNumLUTs() {
        return numLUTs;
    }

    /**
        Get the number of entries in each lookup table (NELUTn).
        <p>
        This field shall contain the number of entries in
        each of the LUTs for the nth image band. This field
        shall be omitted if the value in NLUTSn is BCS
        zero (0x30).
        <p>
        This field will be zero if there are no entries or no
        lookup tables.

        @return the number of lookup table entries.
    */
    public final int getNumLUTEntries() {
        return numEntriesLUT;
    }

    /**
        Get a specific lookup table.

        @param lutNumber the index of the lookup table (1-base).
        @return the lookup table corresponding to the index.
    */
    public final NitfImageBandLUT getLUT(final int lutNumber) {
        return getLUTZeroBase(lutNumber - 1);
    }

    /**
        Get a specific lookup table.

        @param lutNumberZeroBase the index of the lookup table (0-base).
        @return the lookup table corresponding to the index.
    */
    public final NitfImageBandLUT getLUTZeroBase(final int lutNumberZeroBase) {
        return luts.get(lutNumberZeroBase);
    }

    private void readIREPBAND() throws ParseException {
        imageRepresentation = reader.readTrimmedBytes(IREPBAND_LENGTH);
    }

    private void readISUBCAT() throws ParseException {
        imageSubcategory = reader.readTrimmedBytes(ISUBCAT_LENGTH);
    }

    private void readIFC() throws ParseException {
        reader.skip(IFC_LENGTH);
    }

    private void readIMFLT() throws ParseException {
        reader.skip(IMFLT_LENGTH);
    }

    private void readNLUTS() throws ParseException {
        numLUTs = reader.readBytesAsInteger(NLUTS_LENGTH);
    }

    private void readNELUT() throws ParseException {
        numEntriesLUT = reader.readBytesAsInteger(NELUT_LENGTH);
    }
}
