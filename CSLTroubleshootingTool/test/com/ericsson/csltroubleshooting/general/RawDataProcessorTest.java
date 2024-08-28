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

import java.io.*;
import java.util.ArrayList;

import org.junit.*;

import com.ericsson.csltroubleshooting.logging.CslLogger;

public class RawDataProcessorTest {

    private static File directory = new File(
            System.getProperty("user.dir") + "/test/com/ericsson/csltroubleshooting/general/CslDecoderTestResources");
    private static File tmpDir = new File(System.getProperty("user.dir") + "csltesttemp");
    private static File logFile = new File("CslTroubleshootingLog.txt");

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        CslLogger.setup();
        FileManager.setupDecoder();
        tmpDir.mkdir();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        (new File("CslTroubleshootingLog.txt")).delete();
        FileManager.cleanupDecoder();
        final File[] outputFiles = directory.listFiles();
        FileManager.deleteExistingFiles(outputFiles, CslConstants.DECODED_EXTENSION);
        FileManager.deleteExistingFiles(outputFiles, CslConstants.SESSION_EXTENSION);
        tmpDir.delete();
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.RawDataProcessor#run()} and decoding, session generation and printing out the
     * likely CSL file times. Expects 2 CellTrace files in the directory and should therefore produce 2 decoded files and 2 sessions files.
     *
     * @throws InterruptedException
     */
    @Test
    public void testRun() throws InterruptedException {
        final RawDataProcessor objUnderTest = new RawDataProcessor(directory);
        final Thread t = new Thread(objUnderTest);
        t.start();
        t.join();
        ArrayList<String> filesInDir = FileManager.getNamesOfFiles(directory, CslConstants.DECODED_EXTENSION, true);
        assertTrue(filesInDir.size() == 2);
        assertTrue(filesInDir.get(0).contains("A20161213"));
        filesInDir = FileManager.getNamesOfFiles(directory, CslConstants.SESSION_EXTENSION, true);
        assertTrue(filesInDir.size() == 2);
        assertTrue(filesInDir.get(0).contains("A20161213"));
    }

    @Test
    public void testRunWithNoFiles() throws InterruptedException {
        final RawDataProcessor objUnderTest = new RawDataProcessor(tmpDir);
        final Thread t = new Thread(objUnderTest);
        t.start();
        t.join();
        assertTrue(true);
    }

    @Test
    public void testDecodedFileContent() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(new File(directory.getAbsolutePath()
                + "/A20161213.0615-0630_SubNetwork=ONMeContext=ENB244_celltracefile_DUL1_1" + CslConstants.DECODED_EXTENSION)));
        String input = null;
        boolean isDecoded = false;
        while ((input = reader.readLine()) != null) {
            if (input.contains("LTEEvent {")) {
                isDecoded = true;
                break;
            }
        }
        assertTrue(isDecoded);
        reader.close();
    }

    @Test
    public void testSessionFileContent() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(new File(directory.getAbsolutePath()
                + "/A20161213.0615-0630_SubNetwork=ONMeContext=ENB244_celltracefile_DUL1_1" + CslConstants.SESSION_EXTENSION)));
        String input = null;
        boolean hasSessions = false;
        while ((input = reader.readLine()) != null) {
            if (input.contains("GCI")) {
                hasSessions = true;
                break;
            }
        }
        assertTrue(hasSessions);
        reader.close();
    }

    @Test
    public void testCslFileTimes() throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(logFile));
        String input = null;
        int instances = 0;
        while ((input = reader.readLine()) != null) {
            if (input.contains("files starting around 2016-12-13 15:15 for complete records")
                    || input.contains("files starting around 2016-12-13 15:00 for complete records")) {
                instances++;
            }
        }
        assertTrue(instances == 4);
        reader.close();
    }
}