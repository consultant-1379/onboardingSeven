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
package com.ericsson.csltroubleshooting.celltrace;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.csltroubleshooting.general.CslConstants;
import com.ericsson.csltroubleshooting.general.FileManager;

/**
 * Decode the CellTrace files in a given directory - the decoded files will be written in the same directory as the original CellTrace, with the same
 * name and .decoded.txt extension instead of a .bin.gz extension.
 */
public class CellTraceDecoding implements Runnable {

    private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private int noFilesToDecode = 0;
    private File path = null;
    private ArrayList<String> filesToDecode = new ArrayList<>();
    private Runtime rt = Runtime.getRuntime();
    private Process process = null;

    /**
     * Initialize the decoding operation.
     *
     * @param directory
     *            directory containing the CellTrace files
     */
    public CellTraceDecoding(final File directory) {
        this.path = directory;
        filesToDecode = FileManager.getNamesOfFiles(directory, CslConstants.CELLTRACE_EXTENSION, true);
        noFilesToDecode = filesToDecode.size();
    }

    /**
     * Decode the files and write the output to the provided path.
     */
    private void decodeFiles() {
        log.info("Decoding of CellTrace started");
        for (final String fileToDecode : filesToDecode) {
            try {
                log.info("Decoding " + fileToDecode);
                process = rt.exec(CslConstants.PERL_CMD + " " + CslConstants.LTNG_CMD + " " + fileToDecode);
                writeDecodedData(fileToDecode.replace(CslConstants.CELLTRACE_EXTENSION, CslConstants.DECODED_EXTENSION));
            } catch (final IOException e) {
                log.log(Level.WARNING, "Failed to decode: " + fileToDecode, e);
            }
        }
        log.info("Decoding of CellTrace complete");
    }

    /**
     * Write the decoded data to the specified file.
     *
     * @param outputFileName
     *            name of the output file
     * @throws IOException
     *             throw an exception if the write operation fails
     */
    private void writeDecodedData(final String outputFileName) throws IOException {
        final BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        String line = null;
        String errorLine = null;
        while ((line = input.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        while ((errorLine = error.readLine()) != null) {
            log.warning(errorLine);
        }
        writer.close();
    }

    @Override
    public void run() {
        if (noFilesToDecode > 0) {
            decodeFiles();
        } else {
            log.warning("No files to decode in " + path.getAbsolutePath());
        }
    }
}
