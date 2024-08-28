/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.csltroubleshooting.general;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;

import org.junit.*;

public class CslConstantsTest {

    private DateFormat underTest = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.CslConstants#getJstFormat()}.
     */
    @Test
    public void testGetJstFormat() {
        underTest = CslConstants.getJstFormat();
        assertTrue(underTest.getTimeZone().toString().contains("Japan"));
        try {
            assertTrue("2017-07-11 13:00".equals(underTest.format(underTest.parse("2017-07-11 13:00"))));
            assertFalse("201707111300".equals(underTest.format(underTest.parse("2017-07-11 13:00"))));
        } catch (final ParseException e) {
            fail("parsing failed!");
        }
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.CslConstants#getCslFileNameTimeStampFormat()}.
     */
    @Test
    public void testGetCslFileNameTimeStampFormat() {
        underTest = CslConstants.getCslFileNameTimeStampFormat();
        assertTrue(underTest.getTimeZone().toString().contains("Japan"));
        try {
            assertTrue("201707111300".equals(underTest.format(underTest.parse("201707111300"))));
            assertFalse("2017-07-11 13:00".equals(underTest.format(underTest.parse("201707111300"))));
        } catch (final ParseException e) {
            fail("parsing failed!");
        }
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.CslConstants#getCellTraceFileNameTimeStampFormat()}.
     */
    @Test
    public void testGetCellTraceFileNameTimeStampFormat() {
        underTest = CslConstants.getCellTraceFileNameTimeStampFormat();
        assertTrue(underTest.getTimeZone().toString().contains("UTC"));
        try {
            assertTrue("20170711.1300".equals(underTest.format(underTest.parse("20170711.1300"))));
            assertFalse("2017-07-11 13:00".equals(underTest.format(underTest.parse("20170711.1300"))));
        } catch (final ParseException e) {
            fail("parsing failed!");
        }
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.CslConstants#getCslRecordTimeStampFormat()}.
     */
    @Test
    public void testGetFileTimeStampFormat() {
        underTest = CslConstants.getCslRecordTimeStampFormat();
        try {
            assertTrue("20170711130000".equals(underTest.format(underTest.parse("20170711130000"))));
            assertFalse("2017-07-11 13:00".equals(underTest.format(underTest.parse("20170711130000"))));
        } catch (final ParseException e) {
            fail("parsing failed!");
        }
    }

}
