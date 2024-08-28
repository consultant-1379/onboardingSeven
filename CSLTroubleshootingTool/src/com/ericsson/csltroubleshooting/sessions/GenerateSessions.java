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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.csltroubleshooting.general.*;

/**
 * Generate the sessions from the CellTrace - a session is a correlated list of the events that would form a record in the CSL file. This will allow
 * the troubleshooter to find a set of linked events making it (a bit) easier to relate the CSL to the CellTrace. This works by taking the already
 * decoded CellTrace (.decoded.txt) files and then creating session files (same name but with .session.txt extension).
 */
public class GenerateSessions implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private File path = null;
    private ArrayList<String> filesToProcess = new ArrayList<>();
    private int noFilesToProcess = 0;

    /**
     * Initialize the session generation operation.
     *
     * @param directory
     *            directory containing the decoded CellTrace files
     */
    public GenerateSessions(final File directory) {
        this.path = directory;
    }

    @Override
    public void run() {
        filesToProcess = FileManager.getNamesOfFiles(path, CslConstants.DECODED_EXTENSION, true);
        noFilesToProcess = filesToProcess.size();
        if (noFilesToProcess > 0) {
            generateSessions();
        } else {
            log.warning("No files to process in " + path.getAbsolutePath());
        }
    }

    /**
     * Generate the sessions from the decoded CellTrace. It does this be reading each decoded event in and then either matching to an existing session
     * or staring a new session. It assumes that each event in the file is bounded by 2 delimiters in text:
     * <ul>
     * <li>recordType 4,: indicates the start of an event in the file</li>
     * <li>EVENT_PARAM_TRACE_RECORDING_SESSION_REFERENCE: indicates the end of an event in the file
     * <li>
     * </ul>
     *
     * For each session the following is written to the file:
     * <ul>
     * <li>Global Cell Id</li>
     * <li>ENBS1APID</li>
     * <li>RAC_UE_REF
     * <li>
     * <li>Events in the session</li>
     * </ul>
     *
     * For each event in a session, the following is written to the file:
     * <ul>
     * <li>Event name</li>
     * <li>MMEA1APID (in both decimal and hex format)</li>
     * <li>GUMMEI</li>
     * <li>Event time (in the format h:m:s)</li>
     * </ul>
     */
    private void generateSessions() {
        log.info("Procesing of sessions started");
        File processedFile = null;
        final Map<String, ArrayList<String>> sessionMap = new HashMap<>();
        BufferedReader reader = null;
        String input = null;
        Event event = null;
        for (final String fileToProcess : filesToProcess) {
            processedFile = new File(fileToProcess.replace(CslConstants.DECODED_EXTENSION, CslConstants.SESSION_EXTENSION));
            try {
                log.info("Processing " + fileToProcess + " to " + processedFile.getAbsolutePath());
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileToProcess))));
                while ((input = reader.readLine()) != null) {
                    if (input.contains(CslConstants.EVENT_STRING_START_DELIMITER)) {
                        event = getEvent(reader);
                        if (sessionMap.get(event.getKey()) == null) {
                            final ArrayList<String> events = new ArrayList<>();
                            events.add("\nEvent name: " + event.getEventName() + "\nMMES1APID: " + event.getMmes1apid() + "\nGUMMEI: "
                                    + event.getGummei() + "\nEvent Time: " + event.getEventTime());
                            sessionMap.put(event.getKey(), events);
                        } else {
                            final ArrayList<String> events = sessionMap.get(event.getKey());
                            events.add("\nEvent name: " + event.getEventName() + "\nMMES1APID: " + event.getMmes1apid() + "\nGUMMEI: "
                                    + event.getGummei() + "\nEvent Time: " + event.getEventTime());
                        }
                    }

                }
                writeSessions(sessionMap, processedFile);
                reader.close();
            } catch (final Exception e) {
                log.log(Level.SEVERE, "Failed to process: " + fileToProcess, e);
                e.printStackTrace();
            }
        }
        log.info("Processing of sessions complete");
    }

    /**
     * Read an individual event from the input file
     *
     * @param reader
     *            file reader
     * @return the event that has been read in
     * @throws IOException
     *             any read failure throws an IOException
     */
    private Event getEvent(final BufferedReader reader) throws IOException {
        final StringBuffer rawEvent = new StringBuffer();
        String input = null;
        final Event event = new Event();
        while ((input = reader.readLine()) != null) {
            if (input.contains(CslConstants.EVENT_STRING_END_DELIMITER)) {
                break;
            } else {
                rawEvent.append(input + "\n");
            }
        }
        event.setEvent(rawEvent.toString());
        return event;
    }

    /**
     * Write the completed sessions to file
     *
     * @param sessions
     *            the sessions to write to the file
     * @param outputFile
     *            the output file
     * @throws UnsupportedEncodingException
     *             if the text cannot be encoded properly an UnsupportedEncodingException is thrown
     * @throws FileNotFoundException
     *             if the output file cannot be created, a FileNotFoundException is thrown
     */
    private void writeSessions(final Map<String, ArrayList<String>> sessions, final File outputFile)
            throws FileNotFoundException, UnsupportedEncodingException {
        final PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
        for (final Map.Entry<String, ArrayList<String>> entry : sessions.entrySet()) {
            writer.println(entry.getKey() + "\n" + entry.getValue());
            writer.println("------------------------------------------------------------------------------------------");
            writer.flush();
        }
        writer.close();
    }

    /**
     * Represents an event in a decoded file.
     */
    private class Event {
        private String key = new String();
        private String eventName = new String();
        private String eventTime = new String();
        private String mmes1apid = new String();
        private String gummei = new String();
        private Pattern p = null;
        private Matcher m = null;

        /**
         * Set an events values given data read in from a decoded CellTrace file.
         *
         * @param rawEvent
         *            the raw event data read in from the decoded CellTrace file
         */
        public void setEvent(final String rawEvent) {
            String temp = new String();
            String globalCellId = new String();
            String enbs1apid = new String();
            String racueref = new String();
            eventName = findAttribute("\\s{3}[A-Z0-9_]{3,}\\s{1}\\{", rawEvent, " ", "{");
            globalCellId = findAttribute("EVENT_PARAM_GLOBAL_CELL_ID\\s{1}[0-9]*,", rawEvent, " ", ",");
            enbs1apid = findAttribute("EVENT_PARAM_ENBS1APID\\s{1}[0-9]*,", rawEvent, " ", ",");
            mmes1apid = findAttribute("EVENT_PARAM_MMES1APID\\s{1}[0-9]*,", rawEvent, " ", ",");
            if (!mmes1apid.equals("unavailable")) {
                mmes1apid = mmes1apid + NumberFormatHandling.getHex(mmes1apid) + ")";
            }
            gummei = findAttribute("EVENT_PARAM_GUMMEI\\s{1}.*,", rawEvent, " ", ",");
            racueref = findAttribute("EVENT_PARAM_RAC_UE_REF\\s{1}[0-9]*,", rawEvent, " ", ",");

            // Get the event time.
            p = Pattern.compile("EVENT_PARAM_TIMESTAMP_[HOURMINUTESCD]*\\s{1}[0-9]{1,2},");
            m = p.matcher(rawEvent);
            while (m.find()) {
                temp = m.group();
                eventTime += temp.substring(temp.indexOf(" ") + 1, temp.indexOf(",")) + ":";
            }
            eventTime = eventTime.substring(0, eventTime.length() - 1);

            key = "GCI: " + globalCellId + "; ENBS1APID: " + enbs1apid + "; RAC_UE_REF: " + racueref;
        }

        /**
         * Find the value of an attribute given the provided regex, the raw data and the bounding delimiters. If the attribute cannot be found,
         * assumes it is unavailable and sets it as such.
         *
         * @param regex
         *            regex to use for searching the raw event
         * @param rawEvent
         *            the raw event
         * @param startDelim
         *            start delimiter
         * @param endDelim
         *            end delimiter
         * @return the value of the attribute
         */
        private String findAttribute(final String regex, final String rawEvent, final String startDelim, final String endDelim) {
            String temp = new String();
            p = Pattern.compile(regex);
            m = p.matcher(rawEvent);
            while (m.find()) {
                temp = m.group();
            }
            try {
                temp = temp.substring(temp.indexOf(startDelim) + 1, temp.indexOf(endDelim));
            } catch (final Exception e) {
                temp = "unavailable";
            }
            return temp;
        }

        /**
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * @return the eventName
         */
        public String getEventName() {
            return eventName;
        }

        /**
         * @return the eventTime
         */
        public String getEventTime() {
            return eventTime;
        }

        /**
         * @return the mmes1apid
         */
        public String getMmes1apid() {
            return mmes1apid;
        }

        /**
         * @return the gummei
         */
        public String getGummei() {
            return gummei;
        }
    }
}
