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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.csltroubleshooting.general.CslConstants;
import com.ericsson.csltroubleshooting.general.FileManager;

/**
 * Search through CellTrace files for a specific combination of MMES1APID and date/time. This enables a troubleshooter to find the most appropriate
 * CellTrace file if they are starting with a CSL records and are looking for the CellTrace that corresponds with a given record (assuming they have
 * an MMES1APID and time stamp available).
 */
public class CellTraceSearchProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private String mmeS1ApId = null;
    private Date time = null;
    private ArrayList<String> sessionFiles = new ArrayList<>();
    private ArrayList<String> filesToSearch = new ArrayList<>();
    private int noSessionFiles = 0;

    /**
     * Initialize the CellTrace search operation.
     *
     * @param cellTraceDirectory
     *            directory that contains the CellTrace files
     * @param mmes1apid
     *            MMES1APID to search for
     * @param time
     *            time stamp to search for, formatted with "yyyyMMddHHmmss" as it is in the CSL records
     */
    public CellTraceSearchProcessor(final File cellTraceDirectory, final String mmes1apid, final Date time) {
        this.mmeS1ApId = mmes1apid;
        this.time = time;
        sessionFiles = FileManager.getNamesOfFiles(cellTraceDirectory, CslConstants.SESSION_EXTENSION, true);
        noSessionFiles = sessionFiles.size();
    }

    @Override
    public void run() {
        if (noSessionFiles > 0) {
            try {
                searchCellTraceSessionFiles();
            } catch (final ParseException e) {
                log.log(Level.SEVERE, "Unable to search CellTrace files!", e);
            }
        } else {
            log.warning("No session files in provided paths!");
        }

    }

    /**
     * Try and find the CellTrace file that contains the MMES1APID in an event at the provided time. It does this by searching the .session.txt files.
     *
     * First finds any CellTrace files that are aligned in time with the provided time stamp - so takes the provided time stamp and rounds down to the
     * nearest 15 minutes to find the likely ROP start time. It then checks for .session.txt files that have that ROP start time and builds a list of
     * files to then search for the MMES1APID in an event at the provided time stamp. The first file that contains that MMES1APID in an event with the
     * provided time stamp is returned as the found file.
     *
     * @throws ParseException
     */
    private void searchCellTraceSessionFiles() throws ParseException {
        getFilesToSearch();
        if (filesToSearch.size() > 0) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            String eventName = null;
            int lineNumber = 0;
            boolean found = false;
            String foundInFile = null;
            BufferedReader reader = null;
            String line = null;
            final String eventTime = "Event Time: " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
            for (final String fileToSearch : filesToSearch) {
                try {
                    reader = new BufferedReader(new FileReader(new File(fileToSearch)));
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (line.matches("Event name: .*")) {
                            eventName = line.substring(line.indexOf(" ") + 1);
                            line = reader.readLine();
                            lineNumber++;
                            if (line.equals("MMES1APID: " + mmeS1ApId)) {
                                reader.readLine();
                                line = reader.readLine();
                                lineNumber += 2;
                                if (line.contains(eventTime)) {
                                    found = true;
                                    foundInFile = fileToSearch;
                                    break;
                                }
                            }
                        }
                    }
                    reader.close();
                    if (found) {
                        break;
                    }
                } catch (final Exception e) {
                    log.log(Level.SEVERE, "Unable to search file " + fileToSearch, e);
                }
            }
            if (found) {
                log.info("Event found in " + foundInFile + " with Event Name " + eventName + " at line " + lineNumber);
            }
        } else {
            log.warning("No CellTrace files for that date and time!");
        }
    }

    /**
     * Find the session files to search - handles CellTrace(/session) file names that have and files that don't have Time zone information in the file
     * name.
     *
     * @throws ParseException
     */
    private void getFilesToSearch() throws ParseException {
        final String ropFileStartTime = getRopFileStartTime();
        for (final String fileName : sessionFiles) {
            if (CslConstants.getCellTraceFileNameTimeStampFormat()
                    .format(FileManager.getCellTraceStartRopTime(fileName.substring(fileName.lastIndexOf(File.separator) + 1)))
                    .contains(ropFileStartTime)) {
                filesToSearch.add(fileName);
            }
        }
    }

    /**
     * Get the time stamp of the start of the ROP.
     *
     * @return String representation of the time stamp of the start of the ROP so that it enables finding the likely CellTrace file that would have
     *         the provided MMES1APID and time.
     * @throws ParseException
     */
    private String getRopFileStartTime() {
        final Calendar cal = Calendar.getInstance(Locale.ROOT);
        cal.setTime(time);
        final int currentMin = cal.get(Calendar.MINUTE);
        final int mod = currentMin % 15;
        cal.add(Calendar.MINUTE, -mod);
        return "." + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : "" + cal.get(Calendar.HOUR_OF_DAY))
                + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE));
    }

}
