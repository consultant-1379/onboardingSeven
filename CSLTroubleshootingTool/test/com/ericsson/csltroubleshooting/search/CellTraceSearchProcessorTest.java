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
import java.text.ParseException;

import org.junit.*;

import com.ericsson.csltroubleshooting.general.*;
import com.ericsson.csltroubleshooting.logging.CslLogger;

public class CellTraceSearchProcessorTest {

    private static File directory = new File(
            System.getProperty("user.dir") + "/test/com/ericsson/csltroubleshooting/general/CslDecoderTestResources");
    private static File logFile = new File("CslTroubleshootingLog.txt");

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        CslLogger.setup();
        FileManager.setupDecoder();
        final Thread t = new Thread(new RawDataProcessor(directory));
        t.start();
        t.join();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        logFile.delete();
        FileManager.cleanupDecoder();
        final File[] outputFiles = directory.listFiles();
        FileManager.deleteExistingFiles(outputFiles, CslConstants.DECODED_EXTENSION);
        FileManager.deleteExistingFiles(outputFiles, CslConstants.SESSION_EXTENSION);
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.search.CellTraceSearchProcessor#run()}.
     *
     * @throws ParseException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testRun() throws ParseException, InterruptedException, IOException {
        final CellTraceSearchProcessor objUnderTest = new CellTraceSearchProcessor(directory, "123178010(0x7578c1a)",
                CslConstants.getCslRecordTimeStampFormat().parse("20161213062654"));
        final Thread t = new Thread(objUnderTest);
        t.start();
        t.join();
        final BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String input = null;
        boolean isFound = false;
        while ((input = reader.readLine()) != null) {
            if (input.contains("Event found in")) {
                isFound = true;
                break;
            }
        }
        reader.close();
        assertTrue(isFound);
    }

}
