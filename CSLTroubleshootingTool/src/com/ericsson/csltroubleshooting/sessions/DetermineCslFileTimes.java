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
package com.ericsson.csltroubleshooting.sessions;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.csltroubleshooting.general.CslConstants;
import com.ericsson.csltroubleshooting.general.FileManager;

/**
 * Determine, for a given CellTrace file, what time stamp will be in the CSL files that would be generated given the timing of the CellTrace. Outputs
 * the time stamp of the likely CSL files for both complete and incomplete(/'99') records.
 */
public class DetermineCslFileTimes implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private File path = null;
    private int noFilesToAnalyse = 0;
    private ArrayList<String> filesToAnalyse = new ArrayList<>();

    /**
     * Initialize the operation to find the likely times of the CSL files.
     *
     * @param directory
     *            directory that contains the CellTrace files.
     */
    public DetermineCslFileTimes(final File directory) {
        this.path = directory;
        filesToAnalyse = FileManager.getNamesOfFiles(directory, CslConstants.CELLTRACE_EXTENSION, false);
        noFilesToAnalyse = filesToAnalyse.size();
    }

    @Override
    public void run() {
        if (noFilesToAnalyse > 0) {
            analyseFiles();
        } else {
            log.warning("No files to analyse in " + path.getAbsolutePath());
        }

    }

    /**
     * Analyze the CellTrace files and find the likely time stamps of the associated CSL files. Presents individual results for each CellTrace file in
     * the provided directory.
     */
    private void analyseFiles() {
        final Map<String, ArrayList<String>> dates = getDates(filesToAnalyse);
        final Iterator<Map.Entry<String, ArrayList<String>>> it = dates.entrySet().iterator();
        Entry<String, ArrayList<String>> date = null;
        ArrayList<String> temp = null;
        while (it.hasNext()) {
            date = it.next();
            temp = date.getValue();
            log.info("For CellTrace file " + date.getKey() + " look for CSL files starting around " + temp.get(0)
                    + " for complete records and CSL files starting around " + temp.get(1) + " for incomplete/'99' records");
        }
    }

    /**
     * Find the dates of the CSL files given the dates of the CellTrace. Takes the CellTrace file times and applies the JST offset to get the times of
     * the CSL files.
     *
     * @param cellTraceFiles
     *            set of CellTrace files to use for timing
     * @return Map<String, ArrayList<String>> containing the CellTrace file name and the times for the CSL files for complete and incomplete records
     */
    public static final Map<String, ArrayList<String>> getDates(final ArrayList<String> cellTraceFiles) {
        final Map<String, ArrayList<String>> dates = new HashMap<>();
        ArrayList<String> temp = null;
        final DateFormat outputDateFormat = CslConstants.getJstFormat();
        Date tempDate = null;
        final Calendar add = Calendar.getInstance();
        for (final String fileName : cellTraceFiles) {
            try {
                tempDate = FileManager.getCellTraceStartRopTime(fileName);
                add.setTime(tempDate);
                add.add(Calendar.MINUTE, 15);
                temp = new ArrayList<>();
                temp.add(outputDateFormat.format(tempDate));
                temp.add(outputDateFormat.format(add.getTime()));
                dates.put(fileName, temp);
            } catch (final ParseException e) {
                log.log(Level.SEVERE, "Unable to determine time of CSL files corresponding to " + fileName, e);
            }
        }
        return dates;
    }

}
