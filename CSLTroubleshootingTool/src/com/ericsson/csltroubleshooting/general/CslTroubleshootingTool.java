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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ericsson.csltroubleshooting.logging.CslLogger;
import com.ericsson.csltroubleshooting.search.CellTraceSearchProcessor;
import com.ericsson.csltroubleshooting.search.CslSearchProcessor;

/**
 * Main class for the tool!
 */
public class CslTroubleshootingTool {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static File cellTraceDirectory = null;
    private static File cslDirectory = null;
    private static String searchPattern = null;
    private static String mmes1apid = null;
    private static Date time = null;
    private static boolean isRegex = true;

    // TODO - handle CellTrace files with Time Zone in the file name.

    /**
     * Start up the tool.
     *
     * @param args
     *            input parameters from command line.
     */
    public static void main(final String[] args) {
        if (initialiseTool(args)) {
            if (args[0].equals(CslConstants.RAW_ANALYSIS)) {
                log.info("Processing input data:");
                final RawDataProcessor processor = new RawDataProcessor(cellTraceDirectory);
                final Thread thread = new Thread(processor);
                log.info("Processing started!");
                thread.start();
                while (thread.isAlive()) {
                    ;
                }
                log.info("\nProcessing complete!");
                FileManager.cleanupDecoder();
            } else if (args[0].equals(CslConstants.SEARCH_CELLTRACE_BY_PATTERN)) {
                log.info("Searching CellTrace for provided pattern");
                final CellTraceSearchProcessor processor = new CellTraceSearchProcessor(cellTraceDirectory, mmes1apid, time);
                final Thread thread = new Thread(processor);
                log.info("Processing started!");
                thread.start();
                while (thread.isAlive()) {
                    ;
                }
                log.info("\nSearching complete!");
            } else {
                log.info("Searching CSL for time and provided pattern");
                final CslSearchProcessor processor = new CslSearchProcessor(cellTraceDirectory, cslDirectory, searchPattern, isRegex);
                final Thread thread = new Thread(processor);
                log.info("Processing started!");
                thread.start();
                while (thread.isAlive()) {
                    ;
                }
                log.info("\nSearching complete!");
            }
        }
    }

    /**
     * Initialize the tool:
     * <ul>
     * <li>Set up the log</li>
     * <li>Ensure the number of arguments is at least the minimum number (2)</li>
     * <li>Set up the different procedures, depending on the provided switches</li>
     * </ul>
     *
     * @param args
     *            input arguments from command line
     * @return boolean indication of whether initialization was successful or not
     */
    private static boolean initialiseTool(final String[] args) {
        boolean isInitialisedSuccessfully = true;
        try {
            CslLogger.setup();
        } catch (SecurityException | IOException e) {
            System.err.println("Unable to initialise logs!");
            isInitialisedSuccessfully = false;
        }
        log.info("Initialising tool");
        if (args.length < 2) {
            log.warning(CslConstants.USAGE_MSG);
            isInitialisedSuccessfully = false;
        }
        if (isInitialisedSuccessfully) {
            cellTraceDirectory = new File(args[1]);
            if (isInitialisedSuccessfully = FileManager.checkDir(cellTraceDirectory)) {
                if (args[0].equals(CslConstants.RAW_ANALYSIS)) {
                    isInitialisedSuccessfully = initCellTraceDecodingAndAnalysis(args);
                } else if (args[0].equals(CslConstants.SEARCH_CELLTRACE_BY_PATTERN)) {
                    isInitialisedSuccessfully = initialiseCellTraceSearch(args);
                } else if (args[0].equals(CslConstants.SEARCH_CSL_BY_NODE_ID) || args[0].equals(CslConstants.SEARCH_CSL_BY_PATTERN)) {
                    isInitialisedSuccessfully = initialiseCslSearch(args);
                } else {
                    log.warning(CslConstants.USAGE_MSG);
                    isInitialisedSuccessfully = false;
                }
            }
        }
        return isInitialisedSuccessfully;
    }

    /**
     * Initialize the CellTrace search option:
     * <ul>
     * <li>Validates that 4 arguments have been provided at command line (switch, path to CellTrace, MMES1APID and time stamp)</li>
     * <li>Check that there are .session.txt files in that directory</li>
     * <li>Validate that the MMES1APID is a valid value, either a decimal or hexadecimal value</li>
     * <li>Validate that the time stamp, essentially ensure that it matches the format of the time stamp as used in the CSL records themselves</li>
     * </ul>
     *
     * The MMES1APId is set to a value "<decimal>(<hexadecimal>)" where the hexadecimal is prepended with "0x"
     *
     * @param args
     *            input arguments from command line
     * @return boolean indication of whether initialization was successful or not
     */
    private static boolean initialiseCellTraceSearch(final String[] args) {
        boolean isInitialisedSuccessfully = true;
        if (args.length != 4) {
            log.warning(CslConstants.USAGE_MSG);
            isInitialisedSuccessfully = false;
        }
        if (isInitialisedSuccessfully) {
            if (isInitialisedSuccessfully) {
                if (FileManager.countFilesInDirectoryWithExtension(cellTraceDirectory.listFiles(), CslConstants.SESSION_EXTENSION) == 0) {
                    log.warning("No .session files in the provided directories!");
                    isInitialisedSuccessfully = false;
                }
                if (isInitialisedSuccessfully) {
                    isInitialisedSuccessfully = isValidMmeS1Apid(args[2]);
                    if (isInitialisedSuccessfully) {
                        isInitialisedSuccessfully = isValidCslRecordTimeStamp(args[3]);
                        if (isInitialisedSuccessfully) {
                            try {
                                time = CslConstants.getCslRecordTimeStampFormat().parse(args[3]);
                            } catch (final ParseException e) {
                                log.log(Level.SEVERE, "Unable to search for CellTrace", e);
                            }
                        }
                    }
                }
            }
        }
        return isInitialisedSuccessfully;
    }

    /**
     * Validate that the time stamp is formatted according to the mask for the time stamps in the CSL records.
     *
     * @param timeToValidate
     *            time stamp to validate
     * @return boolean indication as to whether time stamp is valid or not
     */
    private static boolean isValidCslRecordTimeStamp(final String timeToValidate) {
        boolean isInitialisedSuccessfully = true;
        try {
            if (timeToValidate.length() != 14) {
                isInitialisedSuccessfully = false;
            }
            if (isInitialisedSuccessfully) {
                CslConstants.getCslRecordTimeStampFormat().parse(timeToValidate);
            }
        } catch (final ParseException e) {
            isInitialisedSuccessfully = false;
        }
        if (!isInitialisedSuccessfully) {
            log.warning("Please enter a valid date and time!");
            log.warning(CslConstants.USAGE_MSG);
        }
        return isInitialisedSuccessfully;
    }

    /**
     * Validate that the MMES1APID is valid - either a decimal value or a hexadecimal value and then construct a string representation for this for
     * ease of search in the session files.
     *
     * @param mmeS1ApIdToValidate
     *            MMES1APID to validate
     * @return boolean indication as to whether MMES1APID is valid or not
     */
    private static boolean isValidMmeS1Apid(final String mmeS1ApIdToValidate) {
        boolean isInitialisedSuccessfully = true;
        final boolean isHex = mmeS1ApIdToValidate.startsWith("0x");
        if (isHex) {
            isInitialisedSuccessfully = NumberFormatHandling.validateHex(mmeS1ApIdToValidate.substring(2));
            mmes1apid = NumberFormatHandling.getDecimal(mmeS1ApIdToValidate.substring(2)) + "(" + mmeS1ApIdToValidate + ")";
        } else {
            isInitialisedSuccessfully = NumberFormatHandling.validateDecimal(mmeS1ApIdToValidate);
            mmes1apid = mmeS1ApIdToValidate + NumberFormatHandling.getHex(mmeS1ApIdToValidate) + ")";
        }
        if (!isInitialisedSuccessfully) {
            log.warning("Please enter a valid MMES1APID!");
            log.warning(CslConstants.USAGE_MSG);
        }
        return isInitialisedSuccessfully;
    }

    /**
     * Initialize the CSL search option:
     * <ul>
     * <li>Validates that 4 arguments have been provided at command line (switch, path to CellTrace, path to CSL files and search pattern (NodeId or
     * regex))</li>
     * <li>Validates that the CellTrace directory exists</li>
     * <li>Check that there are .session.txt files in that directory</li>
     * <li>Validate that the MMES1APID is a valid value, either a decimal or hexadecimal value</li>
     * <li>Validate that the time stamp, essentially ensure that it matches the format of the time stamp as used in the CSL records themselves</li>
     * </ul>
     *
     * The MMES1APId is set to a value "<decimal>(<hexadecimal>)" where the hexadecimal is prepended with "0x"
     *
     * @param args
     *            input arguments from command line
     * @return boolean indication of whether initialization was successful or not
     */
    private static boolean initialiseCslSearch(final String[] args) {
        boolean isInitialised = true;
        if (args.length != 4) {
            log.warning(CslConstants.USAGE_MSG);
            isInitialised = false;
        }
        if (isInitialised) {
            cslDirectory = new File(args[2]);
            if (isInitialised = FileManager.checkDir(cslDirectory)) {
                if (FileManager.countFilesInDirectoryWithExtension(cellTraceDirectory.listFiles(), CslConstants.CELLTRACE_EXTENSION) == 0
                        || FileManager.countFilesInDirectoryWithExtension(cslDirectory.listFiles(), CslConstants.CSV_EXTENSION) == 0) {
                    log.warning("No CellTrace or no CSL files in the provided directories!");
                    isInitialised = false;
                }
                if (isInitialised) {
                    searchPattern = args[3];
                    if (args[1].equals(CslConstants.SEARCH_CSL_BY_NODE_ID)) {
                        isRegex = false;
                        if (!(isInitialised = NumberFormatHandling.validateDecimal(searchPattern))) {
                            log.warning(CslConstants.USAGE_MSG);
                        }
                    } else {
                        try {
                            Pattern.compile(searchPattern);
                        } catch (final PatternSyntaxException e) {
                            log.severe("Please enter a valid regex!");
                            isInitialised = false;
                        }
                    }
                }
            }
        }
        return isInitialised;
    }

    /**
     * Initialize CellTrace decoding and analysis:
     * <ul>
     * <li>Ensure that no previously decoded/analyzed files exist in the directory and give options to the user if they do</li>
     * <li>Setup the decoder</li>
     * </ul>
     *
     * @param args
     *            input arguments from command line
     * @return boolean indication of whether initialization was successful or not
     */
    private static boolean initCellTraceDecodingAndAnalysis(final String[] args) {
        boolean isInitialised = true;
        if (isInitialised) {
            boolean validInput = false;
            final Console c = System.console();
            final File[] files = cellTraceDirectory.listFiles();
            for (final File file : files) {
                if (file.getName().endsWith(CslConstants.DECODED_EXTENSION) || file.getName().endsWith(CslConstants.SESSION_EXTENSION)) {
                    if (validInput) {
                        break;
                    }
                    while (true) {
                        log.warning("Previously processed files in this directory, these will be removed if you proceed.");
                        final String input = c.readLine("Do you wish to proceed [y/n]:");
                        if (input.equals("n")) {
                            isInitialised = false;
                            validInput = true;
                            break;
                        } else if (input.equals("y")) {
                            validInput = true;
                            FileManager.deleteExistingFiles(files, CslConstants.DECODED_EXTENSION);
                            FileManager.deleteExistingFiles(files, CslConstants.SESSION_EXTENSION);
                            break;
                        } else {
                            log.severe("Please enter a valid value!");
                        }
                    }
                }
            }
            if (FileManager.countFilesInDirectoryWithExtension(files, CslConstants.CELLTRACE_EXTENSION) == 0) {
                log.warning("No CellTrace files in that directory! Please enter a directory that contains valid encoded CellTrace files");
                isInitialised = false;
            }
            if (isInitialised) {
                isInitialised = FileManager.setupDecoder();
            }
        }
        return isInitialised;
    }
}
