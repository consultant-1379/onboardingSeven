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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.csltroubleshooting.celltrace.CellTraceDecoding;
import com.ericsson.csltroubleshooting.sessions.DetermineCslFileTimes;
import com.ericsson.csltroubleshooting.sessions.GenerateSessions;

/**
 * This class manages the decoding of raw CellTrace files, the generation of the session files and the output which indicates the likely time stamps
 * in the names of the CSL files that would align with the provided CellTrace.
 *
 * It runs on its own thread and spawns a new thread to do each of:
 * <ul>
 * <li>Decoding the raw CellTrace files</li>
 * <li>Generating the session files</li>
 * <li>Determining the likely time for the associated CSL files
 * <li>
 * </ul>
 */
public class RawDataProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private File cellTraceDirectory = null;

    /**
     * Initialize the processing by setting the directory where the raw CellTrace is located.
     *
     * @param directory
     *            input directory where the raw CellTrace is located
     */
    public RawDataProcessor(final File directory) {
        this.cellTraceDirectory = directory;
    }

    @Override
    public void run() {
        final CellTraceDecoding decoder = new CellTraceDecoding(cellTraceDirectory);
        final GenerateSessions sessions = new GenerateSessions(cellTraceDirectory);
        final DetermineCslFileTimes times = new DetermineCslFileTimes(cellTraceDirectory);
        Thread t = new Thread(decoder);
        t.start();
        try {
            t.join();
        } catch (final InterruptedException e) {
            log.log(Level.SEVERE, "Decoding interrupted!", e);
        }
        t = new Thread(sessions);
        t.start();
        try {
            t.join();
        } catch (final InterruptedException e) {
            log.log(Level.SEVERE, "Session generation interrupted!", e);
        }
        t = new Thread(times);
        t.start();
        while (t.isAlive()) {
            ;
        }
    }

}
