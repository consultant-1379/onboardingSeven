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
package com.ericsson.csltroubleshooting.search;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.ericsson.csltroubleshooting.logging.CslLogger;

public class CslSearchProcessorTest {

    private static File directory = new File(
            System.getProperty("user.dir") + "/test/com/ericsson/csltroubleshooting/general/CslDecoderTestResources");
    private static File logFile = new File("CslTroubleshootingLog.txt");

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        CslLogger.setup();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        logFile.delete();
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.search.CslSearchProcessor#run()}.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testRuneNodeBSearch() throws InterruptedException, IOException {
        final CslSearchProcessor objUnderTest = new CslSearchProcessor(directory, directory, "677153", false);
        final Thread t = new Thread(objUnderTest);
        t.start();
        t.join();
        final BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String input = null;
        boolean isFound = false;
        while ((input = reader.readLine()) != null) {
            if (input.contains("csl_export_201612131515")) {
                isFound = true;
                break;
            }
        }
        reader.close();
        assertTrue(isFound);
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.search.CslSearchProcessor#run()}.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testRunRegexSearch() throws InterruptedException, IOException {
        final CslSearchProcessor objUnderTest = new CslSearchProcessor(directory, directory, ".*,0x17c6ad00,0x440f518502ee,678219,,0,10,20,.*", true);
        final Thread t = new Thread(objUnderTest);
        t.start();
        t.join();
        final BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String input = null;
        boolean isFound = false;
        while ((input = reader.readLine()) != null) {
            if (input.contains("csl_export_201612131515")) {
                isFound = true;
                break;
            }
        }
        reader.close();
        assertTrue(isFound);
    }
}
