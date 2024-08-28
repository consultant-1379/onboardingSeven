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

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.ericsson.csltroubleshooting.general.CslConstants;
import com.ericsson.csltroubleshooting.general.FileManager;
import com.ericsson.csltroubleshooting.sessions.DetermineCslFileTimes;

/**
 * Search through CSL files for a given eNodeB Id or regex. First check a set of input CellTrace files and determine their ROP times and then find CSL
 * files that would align in time. It is these CSL files that are then searched for the eNodeB Id or regex. Allows the troubleshooter to start with
 * some CellTrace and a given Node Id/regex and narrow down which CSL files to further analyze.
 */
public class CslSearchProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private File cellTracePath = null;
    private File cslPath = null;
    private String searchPattern = null;
    private ArrayList<String> cellTraceFiles = new ArrayList<>();
    private int noCellTraceFiles = 0;
    private ArrayList<String> cslFiles = new ArrayList<>();
    private int noCslFiles = 0;
    private boolean isRegex = true;

    /**
     * Initialize the CSL search operation.
     *
     * @param cellTraceDirectory
     *            directory that contains the CellTrace files
     * @param cslDirectory
     *            directory that contains the CSL files
     * @param searchPattern
     *            search pattern to look for - either an eNodeB Id or a regex
     * @param isRegex
     *            boolean indication of whether the search pattern is a regex or not
     */
    public CslSearchProcessor(final File cellTraceDirectory, final File cslDirectory, final String searchPattern, final boolean isRegex) {
        this.cellTracePath = cellTraceDirectory;
        this.cslPath = cslDirectory;
        cellTraceFiles = FileManager.getNamesOfFiles(this.cellTracePath, CslConstants.CELLTRACE_EXTENSION, false);
        cslFiles = FileManager.getNamesOfFiles(this.cslPath, CslConstants.CSV_EXTENSION, true);
        noCellTraceFiles = cellTraceFiles.size();
        noCslFiles = cslFiles.size();
        this.isRegex = isRegex;
        this.searchPattern = searchPattern;
        if (!isRegex) {
            this.searchPattern = ".*," + searchPattern + ",.*";
        }
    }

    @Override
    public void run() {
        if (noCellTraceFiles > 0 && noCslFiles > 0) {
            searchCslFiles();
        } else {
            log.warning("No CellTrace or no CSL files in provided paths!");
        }

    }

    /**
     * Search the CSL files. First check a set of input CellTrace files and determine their ROP times and then find CSL files that would align in
     * time. It is these CSL files that are then searched for the eNodeB Id or regex.
     */
    private void searchCslFiles() {
        final Map<String, ArrayList<String>> dates = DetermineCslFileTimes.getDates(cellTraceFiles);
        final Iterator<Map.Entry<String, ArrayList<String>>> it = dates.entrySet().iterator();
        Entry<String, ArrayList<String>> date = null;
        ArrayList<String> temp = null;
        final HashMap<String, HashSet<File>> matches = new HashMap<>();
        while (it.hasNext()) {
            final HashSet<File> filesMatchingDate = new HashSet<>();
            final HashSet<File> filesMatchingPattern = new HashSet<>();
            date = it.next();
            temp = date.getValue();
            log.info("Checking for CSL files that correspond to " + date.getKey() + " and " + (isRegex ? " pattern " : " eNodeBId ") + searchPattern);
            findCslFilesMatchingCellTraceDate(temp.get(0), filesMatchingDate);
            if (filesMatchingDate.size() > 0) {
                findCslFilesContainingPattern(filesMatchingDate, filesMatchingPattern);
            }
            matches.put(date.getKey(), filesMatchingPattern);
        }
        log.info("Search complete.\nFollowing files contain the " + (isRegex ? " pattern " : " eNodeBId ") + searchPattern
                + " for the dates corresponding to the CellTrace:");
        final Iterator<Map.Entry<String, HashSet<File>>> resultItr = matches.entrySet().iterator();
        Entry<String, HashSet<File>> result = null;
        final StringBuilder names = new StringBuilder();
        while (resultItr.hasNext()) {
            result = resultItr.next();
            names.append(result.getKey() + "\n");
            for (final File file : result.getValue()) {
                names.append(file.getAbsolutePath() + "\n");
            }
        }
        log.info(names.toString());
    }

    /**
     * Find the files that contain the provided pattern, either an eNodeB Id or a regex (both ultimately are managed using a Pattern).
     *
     * @param filesMatchingByTime
     *            already found set of files that align by time (between CellTrace and CSL files)
     * @param filesMatchingByPattern
     *            set of files found by this method that contain the pattern
     */
    private void findCslFilesContainingPattern(final HashSet<File> filesMatchingByTime, final HashSet<File> filesMatchingByPattern) {
        BufferedReader reader = null;
        final Pattern p = Pattern.compile(searchPattern);
        String line = null;
        log.info("Searching CSL files that match date and pattern");
        for (final File file : filesMatchingByTime) {
            log.info("Checking CSL file " + file.getName());
            try {
                reader = new BufferedReader(new FileReader(file));
                while ((line = reader.readLine()) != null) {
                    if (p.matcher(line).matches()) {
                        filesMatchingByPattern.add(file);
                        break;
                    }
                }
                reader.close();
            } catch (final Exception e) {
                log.log(Level.SEVERE, "Unable to search for file " + file.getName(), e);
            }
        }
        log.info("Found " + filesMatchingByPattern.size() + " files matching date and pattern");
    }

    /**
     * Find the CSL files that align with the CellTrace based on the date/time of the CellTrace.
     *
     * @param date
     *            date of the CellTrace
     * @param filesMatchingByDate
     *            CSL files that align based on the date
     */
    private void findCslFilesMatchingCellTraceDate(final String date, final HashSet<File> filesMatchingByDate) {
        try {
            final ArrayList<String> datesInNames = getCslFileNameDateStamps(CslConstants.getJstFormat().parse(date));
            log.info("Finding files that match by date");
            for (final String cslFile : cslFiles) {
                for (final String dateInName : datesInNames) {
                    if (cslFile.contains(dateInName)) {
                        filesMatchingByDate.add(new File(cslFile));
                    }
                }
            }
            log.info("Found " + filesMatchingByDate.size() + " files matching date");
        } catch (final ParseException e) {
            log.log(Level.SEVERE, "Unable to find CSL files", e);
        }
    }

    /**
     * Get the date stamps for the CSL files that correspond with the provided start date, itself taken from the start time of the ROP. The logic
     * applied is that start time of the ROP is offset to JST to align with the time stamp in the CSL file names. From there it is assumed that up to
     * 6 CSL files could have records generated from the CellTrace; this is based on the fact that the CSL "ROP" size if 5 minutes so for a given 15
     * minute CellTrace file there will be 3 CSL files. The extra 3 then come from the fact that the '99' records will come at up to 16 minutes after
     * the end of the ROP. Strictly this could mean up to 7 CSL files for a given 15 minute ROP, but this only outputs 6 as there will be a maximum of
     * 1 minute in the seventh...
     *
     * @param startDate
     * @return
     */
    private final ArrayList<String> getCslFileNameDateStamps(final Date startDate) {
        final ArrayList<String> names = new ArrayList<>();
        names.add(CslConstants.getCslFileNameTimeStampFormat().format(startDate));
        final Calendar add = Calendar.getInstance();
        add.setTime(startDate);
        for (int i = 0; i < 6; i++) {
            add.add(Calendar.MINUTE, 5);
            names.add(CslConstants.getCslFileNameTimeStampFormat().format(add.getTime()));
        }
        return names;
    }

}
