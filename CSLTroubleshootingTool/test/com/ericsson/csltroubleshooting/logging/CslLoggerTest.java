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
package com.ericsson.csltroubleshooting.logging;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;

public class CslLoggerTest {

    File logFile = new File("CslTroubleshootingLog.txt");

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        CslLogger.setup();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        logFile.delete();
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.logging.CslLogger#setup()}.
     */
    @Test
    public void testSetup() {
        assertTrue(logFile.exists());
    }

}
