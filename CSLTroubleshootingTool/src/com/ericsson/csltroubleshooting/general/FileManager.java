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

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains methods for managing files used in the tool.
 */
public class FileManager {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Get the list of files in a directory that end with the provided extension; list either their full absolute paths or just their names, depending
     * on the value of @param isAbsolutePath
     *
     * @param directory
     *            directory where files are located
     * @param extension
     *            file name extension, exclude any files that don't end with this
     * @param isAbsolutePath
     *            return absolute path (true) or just the name (false)
     * @return ArrayList<String> object with the paths to/names of the files that meet the criteria
     */
    public static final ArrayList<String> getNamesOfFiles(final File directory, final String extension, final boolean isAbsolutePath) {
        final File[] files = directory.listFiles();
        final ArrayList<String> foundFiles = new ArrayList<>();
        for (final File file : files) {
            if (file.getName().endsWith(extension)) {
                foundFiles.add((isAbsolutePath ? file.getAbsolutePath() : file.getName()));
            }
        }
        return foundFiles;
    }

    /**
     * Count number of files, from a provided list of files, with a given extension.
     *
     * @param files
     *            files to check
     * @param extension
     *            expected extension
     * @return int indicating how many of the files have the extension
     */
    public static int countFilesInDirectoryWithExtension(final File[] files, final String extension) {
        int noFiles = 0;
        for (final File file : files) {
            if (file.getName().endsWith(extension)) {
                noFiles++;
            }
        }
        return noFiles;
    }

    /**
     * Check if the provided path corresponds to a directory
     *
     * @param directory
     *            directory to check
     * @param message
     *            message to print if not a directory
     * @return boolean indication of whether this is a directory or not
     */
    public static boolean checkDir(final File directory) {
        final boolean isDir = directory.isDirectory();
        if (!isDir) {
            log.severe(directory + " is not a valid directory");
        }
        return isDir;
    }

    /**
     * Delete files in a directory with a given extension.
     *
     * @param files
     *            files to check
     * @param extension
     *            extension of files to delete
     */
    public static void deleteExistingFiles(final File[] files, final String extension) {
        for (final File file : files) {
            if (file.getName().endsWith(extension)) {
                file.delete();
            }
        }
    }

    /**
     * Clean up the decoder once execution of the tool is complete.
     */
    public static void cleanupDecoder() {
        File fileToDelete = null;
        final Iterator<Entry<String, String>> it = CslConstants.RESOURCES.entrySet().iterator();
        Entry<String, String> resource = null;
        while (it.hasNext()) {
            resource = it.next();
            fileToDelete = new File(resource.getValue());
            fileToDelete.delete();
        }
        for (int i = (CslConstants.DIRECTORIES.length - 1); i >= 0; i--) {
            fileToDelete = new File(CslConstants.DIRECTORIES[i]);
            fileToDelete.delete();
        }
    }

    /**
     * Setup the decoder - extract all the required resources from within the jar file to the local file system so that the decoder will execute.
     *
     * @return boolean indication of whether setup of the decoder was successful or not
     */
    public static boolean setupDecoder() {
        log.warning("Setting up decoder");
        File directory = null;
        for (final String dir : CslConstants.DIRECTORIES) {
            directory = new File(dir);
            directory.mkdir();
        }
        boolean isSetup = true;
        int length = 0;
        Entry<String, String> resource = null;
        try {
            final Iterator<Entry<String, String>> it = CslConstants.RESOURCES.entrySet().iterator();
            while (it.hasNext()) {
                final byte[] b = new byte[2048];
                resource = it.next();
                log.log(Level.INFO, "Extracting " + resource.getKey());
                final InputStream input = CslTroubleshootingTool.class.getResourceAsStream(CslConstants.INPUT_RESOURCES_DIR + resource.getKey());
                final OutputStream output = new FileOutputStream(resource.getValue().toString());
                while ((length = input.read(b)) != -1) {
                    output.write(b, 0, length);
                }
                input.close();
                output.close();
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to setup decoder: ", e);
            isSetup = false;
        }
        return isSetup;
    }

    /**
     * Get the ROP start date and time from a CellTrace file in UTC time - accounts for files that have no time zone info in the filename and files
     * that have time zone in the file name.
     *
     * @param fileName
     *            name of the file to check
     * @return the start date and time of the ROP
     * @throws ParseException
     *             if the date cannot be parsed, throw a ParseException
     */
    public static Date getCellTraceStartRopTime(final String fileName) throws ParseException {
        Date ropStartDateTime = null;
        if (fileName.indexOf("-") == 14) {
            ropStartDateTime = CslConstants.getCellTraceFileNameTimeStampFormat()
                    .parse(fileName.substring(fileName.indexOf("A") + 1, fileName.indexOf("-")));
        } else {
            final int timeZone = Integer.parseInt(fileName.substring(fileName.indexOf("+") + 1, fileName.indexOf("+") + 3));
            final Calendar cal = Calendar.getInstance();
            cal.setTime(
                    CslConstants.getCellTraceFileNameTimeStampFormat().parse(fileName.substring(fileName.indexOf("A") + 1, fileName.indexOf("-"))));
            cal.add(Calendar.HOUR, -timeZone);
            ropStartDateTime = cal.getTime();
        }
        return ropStartDateTime;
    }
}
