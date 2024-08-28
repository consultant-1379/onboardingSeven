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

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Setup logging for the tool. All log files will be logged to the working directory in a file called CslTroubleshootingLog.txt with the log getting
 * overwritten with every run of the tool.
 */
public class CslLogger {

    private static final String LOG_FILE_NAME = "CslTroubleshootingLog.txt";

    /**
     * Setup the log file - starts a new file with each run of the tool.
     *
     * @throws SecurityException
     *             thrown if the permissions are not set to enable setting up the log
     * @throws IOException
     *             throw if the file cannot be opened
     */
    static public void setup() throws SecurityException, IOException {
        final File logDir = new File(System.getProperty("user.dir"));
        final File[] logFiles = logDir.listFiles();
        for (final File logFile : logFiles) {
            if (logFile.getName().startsWith(LOG_FILE_NAME)) {
                logFile.delete();
            }
        }
        final FileHandler logFile = new FileHandler(LOG_FILE_NAME);
        logFile.setFormatter(new SimpleFormatter());
        final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        log.addHandler(logFile);
    }
}
