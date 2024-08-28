package com.ericsson.asr.poc.kafkametrics.producer;

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

import static com.ericsson.asr.poc.kafkametrics.constants.Constants.s1InitialContextSetup;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.ericsson.asr.poc.kafkametrics.utils.PerformanceKpis;

public class Producer extends KafkaProducer<String, byte[]> {
    private final long numberOfMessagesToSend;
    private final String topicName;
    private final PerformanceKpis producerKpis;

    public Producer(final Properties properties, final String topicName, final long numberOfMessagesToSend, final PerformanceKpis producerKpis) {
        super(properties);
        this.topicName = topicName;
        this.numberOfMessagesToSend = numberOfMessagesToSend;
        this.producerKpis = producerKpis;
    }

    public void execute() {
        final long startTime = System.currentTimeMillis();

        for (long i = 0; i < numberOfMessagesToSend; i++) {
            final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, String.valueOf(i), s1InitialContextSetup);
            send(record);
        }

        final double totalSecs = ((double) System.currentTimeMillis() - startTime) / 1000;
        final double numberOfKilobytesSent = numberOfMessagesToSend * ((double) s1InitialContextSetup.length / (1000));

        producerKpis.updateKpis(numberOfKilobytesSent, totalSecs, numberOfMessagesToSend);
    }
}
