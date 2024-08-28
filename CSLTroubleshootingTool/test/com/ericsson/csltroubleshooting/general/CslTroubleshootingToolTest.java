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

import java.io.File;

import org.junit.*;

public class CslTroubleshootingToolTest {

    String[] args = { "" };
    private static File logFile = new File("CslTroubleshootingLog.txt");

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
     * Test method for {@link com.ericsson.csltroubleshooting.general.CslTroubleshootingTool#main(java.lang.String[])}.
     */
    @Test
    public void testMain() {
        CslTroubleshootingTool.main(args);
        assertTrue(logFile.exists());
    }

}
