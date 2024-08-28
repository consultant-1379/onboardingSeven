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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.*;

public class FileManagerTest {

    private File[] files;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        final File dir = new File(System.getProperty("user.dir"));
        files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".project");
            }
        });
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.FileManager#getNamesOfFiles(java.io.File, java.lang.String, boolean)}.
     */
    @Test
    public void testGetNamesOfFiles() {
        ArrayList<String> filesUnderTest = FileManager.getNamesOfFiles(new File(System.getProperty("user.dir")), ".project", true);
        assertTrue(filesUnderTest.size() == files.length);
        assertTrue(filesUnderTest.get(0).equals(files[0].getAbsolutePath()));
        filesUnderTest = FileManager.getNamesOfFiles(new File(System.getProperty("user.dir")), ".project", false);
        assertTrue(filesUnderTest.size() == files.length);
        assertFalse(filesUnderTest.get(0).equals(files[0].getAbsolutePath()));
        assertTrue(filesUnderTest.get(0).equals(files[0].getName()));
    }

    /**
     * Test method for
     * {@link com.ericsson.csltroubleshooting.general.FileManager#countFilesInDirectoryWithExtension(java.io.File[], java.lang.String)}.
     */
    @Test
    public void testCountFilesInDirectoryWithExtension() {
        assertTrue(FileManager.countFilesInDirectoryWithExtension(files, ".project") == 1);
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.FileManager#checkDir(java.io.File[])}.
     */
    @Test
    public void testCheckDir() {
        assertTrue(FileManager.checkDir(new File(System.getProperty("user.dir"))));
        assertFalse(FileManager.checkDir(new File("blah")));
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.FileManager#deleteExistingFiles(File[], String)}.
     *
     * @throws IOException
     */
    @Test
    public void testDeleteExistingFiles() throws IOException {
        final File[] files = { new File("test.etaddea") };
        files[0].createNewFile();
        FileManager.deleteExistingFiles(files, ".etaddea");
        assertFalse(files[0].exists());
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.FileManager#setupDecoder()} and
     * {@link com.ericsson.csltroubleshooting.general.FileManager#cleanupDecoder()}.
     */
    @Test
    public void testDecoderSetupCleanDown() {
        FileManager.setupDecoder();
        assertTrue((new File("TestTools.pm")).exists());
        FileManager.cleanupDecoder();
        assertFalse((new File("TestTools.pm")).exists());
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.FileManager#getCellTraceStartRopTime(String)}.
     *
     * @throws ParseException
     */
    @Test
    public void testGettingRopStartTime() throws ParseException {
        final Date ropStartTimeNoTimeZone = FileManager
                .getCellTraceStartRopTime("A20161213.0615-0630_SubNetwork=ONMeContext=ENB244_celltracefile_DUL1_1.bin.gz");
        assertTrue(ropStartTimeNoTimeZone.compareTo(new SimpleDateFormat("yyyyMMddHHmm").parse("201612130615")) == 0);
        final Date ropStartTimeTimeZone = FileManager
                .getCellTraceStartRopTime("A20161213.1400+0800-1415+0800_SubNetwork=ONENB244_celltracefile_DUL1_1.bin.gz");
        assertTrue(ropStartTimeTimeZone.compareTo(new SimpleDateFormat("yyyyMMddHHmm").parse("201612130600")) == 0);
    }
}
