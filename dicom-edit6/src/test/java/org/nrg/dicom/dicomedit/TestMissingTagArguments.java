/*
 * DicomEdit: TestMissingTagArguments
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.*;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * A function given a tag that isn't in the object leaves its target as it was, and the rest of the script still runs.
 */
public class TestMissingTagArguments {

    @Test
    public void testShiftDateByIncrementMissingTag() throws MizerException {
        assertSkipped("(0008,0012) := shiftDateByIncrement[ (0008,0012), \"30\"]\n", instanceCreationDate);
    }

    @Test
    public void testShiftDateTimeByIncrementMissingTag() throws MizerException {
        assertSkipped("(0008,0012) := shiftDateTimeByIncrement[ (0008,0012), \"30\", \"days\"]\n", instanceCreationDate);
    }

    @Test
    public void testLowercaseMissingTag() throws MizerException {
        assertSkipped("(0008,103E) := lowercase[(0008,0070)]\n", seriesDescription);
    }

    @Test
    public void testUppercaseMissingTag() throws MizerException {
        assertSkipped("(0008,103E) := uppercase[(0008,0070)]\n", seriesDescription);
    }

    @Test
    public void testSubstringMissingTag() throws MizerException {
        assertSkipped("(0008,103E) := substring[(0008,0070), 0, 2]\n", seriesDescription);
    }

    @Test
    public void testReplaceMissingTag() throws MizerException {
        assertSkipped("(0008,103E) := replace[(0008,0070), \"a\", \"b\"]\n", seriesDescription);
    }

    @Test
    public void testMatchMissingTag() throws MizerException {
        assertSkipped("(0008,103E) := match[(0008,0070), \"\\\\w+\", 0]\n", seriesDescription);
    }

    @Test
    public void testIsMatchMissingTagIsFalse() throws MizerException {
        final AnonymizationResult result = apply("ismatch[(0008,0070), \".*\"] ? (0008,103E) := \"matched\" : (0008,103E) := \"not matched\"\n");

        assertFalse(result instanceof AnonymizationResultError);
        assertEquals("not matched", result.getDicomObject().getString(seriesDescription));
    }

    @Test
    public void testEmptyDateIsSkipped() throws MizerException {
        final DicomObjectI dobj = createTestObject();
        dobj.putString(instanceCreationDate, "");

        final AnonymizationResult result = BaseScriptApplicator.getInstance(bytes("(0008,0012) := shiftDateByIncrement[ (0008,0012), \"30\"]\n")).apply(dobj);

        assertFalse(result instanceof AnonymizationResultError);
        assertEquals("", result.getDicomObject().getString(instanceCreationDate));
    }

    @Test
    public void testInvalidDateStillFails() throws MizerException {
        final DicomObjectI dobj = createTestObject();
        dobj.putString(instanceCreationDate, "not a date");

        final AnonymizationResult result = BaseScriptApplicator.getInstance(bytes("(0008,0012) := shiftDateByIncrement[ (0008,0012), \"30\"]\n")).apply(dobj);

        assertTrue(result instanceof AnonymizationResultError);
    }

    @Test
    public void testMissingTagWithWrongVRStillFails() throws MizerException {
        // Other Patient IDs isn't a date, so a script that shifts it is wrong whether or not the file has it.
        final AnonymizationResult result = apply("(0010,1000) := shiftDateByIncrement[ (0010,1000), \"30\"]\n");

        assertTrue(result instanceof AnonymizationResultError);
    }

    /**
     * Runs the statement, then a second one, against an object that lacks the statement's input tag, and checks that
     * the target is unchanged and the second statement still ran.
     */
    private void assertSkipped(final String statement, final int target) throws MizerException {
        final String before = createTestObject().getString(target);

        final AnonymizationResult result = apply(statement + "(0010,0010) := \"Anonymized\"\n");

        assertFalse(result instanceof AnonymizationResultError);
        assertEquals(before, result.getDicomObject().getString(target));
        assertEquals("Anonymized", result.getDicomObject().getString(patientName));
    }

    private AnonymizationResult apply(final String script) throws MizerException {
        return BaseScriptApplicator.getInstance(bytes(script)).apply(createTestObject());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putString(patientName, "PatientName");
        dobj.putString(studyDate, "20120919");
        dobj.putString(seriesDescription, "Series");
        return dobj;
    }

    private static final int instanceCreationDate = 0x00080012;
    private static final int studyDate = 0x00080020;
    private static final int seriesDescription = 0x0008103E;
    private static final int patientName = 0x00100010;
}
