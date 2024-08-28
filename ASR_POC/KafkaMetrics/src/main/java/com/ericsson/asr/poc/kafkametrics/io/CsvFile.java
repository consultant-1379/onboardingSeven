/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.asr.poc.kafkametrics.io;

import static com.ericsson.asr.poc.kafkametrics.constants.Constants.CONSUMER_PERFORMANCE_CSV;
import static com.ericsson.asr.poc.kafkametrics.constants.Constants.PRODUCER_PERFORMANCE_CSV;

import java.io.*;
import java.text.NumberFormat;
import java.util.Properties;

import com.ericsson.asr.poc.kafkametrics.utils.PerformanceKpis;

public class CsvFile {

    private final String fileName;
    private final PerformanceKpis kpis;
    private final Properties properties;

    public CsvFile(final String fileName, final PerformanceKpis kpis,
            final Properties properties) {
        this.fileName = fileName;
        this.kpis = kpis;
        this.properties = properties;
    }

    public void appendResult() {
        File file = new File(fileName);
        if (!file.exists()) {
            createEmptyFile();
        }

        appendLineToFile(file, getResult());
    }

    private void createEmptyFile() {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        appendLineToFile(file, getHeader());
    }

    private void appendLineToFile(File file, String line) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getHeader() {
        StringBuilder headerBuilder = new StringBuilder();

        if (fileName.equals(PRODUCER_PERFORMANCE_CSV)) {
            headerBuilder.append("Number Of Producers,");
            headerBuilder.append("Total Number of Records Published,");
            headerBuilder.append("Total Number of Kilobytes Published (kBs),");
            headerBuilder.append("Average Record Size (kBs/record),");
            headerBuilder.append("Time Taken to publish all Records (secs),");
            headerBuilder.append("Average Kilobyte Throughput (kBs/sec),");
            headerBuilder.append("Average Record Throughput (records/sec),");
            headerBuilder.append("Producer Properties,\n");

        } else if (fileName.equals(CONSUMER_PERFORMANCE_CSV)) {
            headerBuilder.append("Number Of Consumers,");
            headerBuilder.append("Total Number of Records Consumed,");
            headerBuilder.append("Total Number of Kilobytes Consumed (kBs),");
            headerBuilder.append("Average Record Size (kBs/record),");
            headerBuilder.append("Time Taken to publish all Records (secs),");
            headerBuilder.append("Average Kilobyte Throughput (kBs/sec),");
            headerBuilder.append("Average Record Throughput (records/sec),");
            headerBuilder.append("Consumer Properties,\n");
        }
        return headerBuilder.toString();
    }

    private String getResult() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(3);

        int numberOfInstances = kpis.getNumberOfInstances();
        double numberOfKilobytes = kpis.getNumberOfKilobytes();
        double numberOfRecords = kpis.getNumberOfRecordsSent();
        double timeTaken = kpis.getTotalSecs();
        double avgRecordSize = kpis.calculateAvgRecordSize();
        double kilobyteThroughput = kpis.calculateAvgKilobyteThroughput();
        double recordThroughput = kpis.calculateAvgRecordThroughput();

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(numberOfInstances + ",");
        resultBuilder.append(numberOfRecords + ",");
        resultBuilder.append(numberOfKilobytes + ",");
        resultBuilder.append(avgRecordSize + ",");
        resultBuilder.append(timeTaken + ",");
        resultBuilder.append(kilobyteThroughput + ",");
        resultBuilder.append(recordThroughput + ",\"");

        for (Object key : properties.keySet()) {
            resultBuilder.append((String) key);
            resultBuilder.append("=");
            resultBuilder.append(properties.getProperty((String) key));
            resultBuilder.append("\n");
        }

        resultBuilder.append("\"\n");

        return resultBuilder.toString();
    }
}
