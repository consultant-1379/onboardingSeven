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
package com.ericsson.asr.poc.kafkametrics.main;

import static com.ericsson.asr.poc.kafkametrics.constants.Constants.CONSUMER_PERFORMANCE_CSV;
import static com.ericsson.asr.poc.kafkametrics.constants.Constants.PRODUCER_PERFORMANCE_CSV;

import java.util.Properties;

import com.ericsson.asr.poc.kafkametrics.cli.DemoCli;
import com.ericsson.asr.poc.kafkametrics.consumer.Consumer;
import com.ericsson.asr.poc.kafkametrics.io.CsvFile;
import com.ericsson.asr.poc.kafkametrics.producer.Producer;
import com.ericsson.asr.poc.kafkametrics.utils.PerformanceKpis;
import com.ericsson.asr.poc.kafkametrics.utils.Utils;

public class Demo {
    private static Producer producer;
    private static PerformanceKpis kpi;
    private static Consumer consumer;

    public static void main(String[] args) throws Exception {

        final DemoCli cli = new DemoCli(args);

        final Properties properties = Utils.readPropertiesFile(cli.getPropertiesFile());
        final String action = cli.getAction();
        final long numberOfMessages = cli.getNumberOfRecords();
        final String topicName = cli.getTopicName();
        String csvName = "";

        kpi = new PerformanceKpis(1);

        if (action.equalsIgnoreCase("producer")) {
            producer = new Producer(properties, topicName, numberOfMessages, kpi);
            producer.execute();
            producer.close();
            csvName = PRODUCER_PERFORMANCE_CSV;
        } else if (action.equalsIgnoreCase("consumer")) {
            consumer = new Consumer(properties, topicName, numberOfMessages, kpi);
            consumer.execute();
            csvName = CONSUMER_PERFORMANCE_CSV;
        }

        final CsvFile resultsFile = new CsvFile(csvName, kpi, properties);
        resultsFile.appendResult();

        // Kafka has some issues with leaving daemon threads open, need to
        // explicitly exit the program to terminate the open daemon threads.
        // System.exit(0);
    }
}
