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
package com.ericsson.asr.poc.kafkametrics.consumer;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import com.ericsson.asr.poc.kafkametrics.utils.PerformanceKpis;

public class RunnableConsumer implements Runnable {

    private final KafkaStream<byte[], byte[]> stream;
    private final long numberOfMessagesToConsume;
    private final PerformanceKpis consumerKpis;

    public RunnableConsumer(final KafkaStream<byte[], byte[]> stream, final long numberOfMessagesToConsume, final PerformanceKpis consumerKpis) {
        this.stream = stream;
        this.numberOfMessagesToConsume = numberOfMessagesToConsume;
        this.consumerKpis = consumerKpis;
    }

    @Override
    public void run() {
        long numberOfMessagedConsumed = 0;
        double numberOfKilobytesConsumed = 0.0;
        double totalSecs = 0.0;

        final ConsumerIterator<byte[], byte[]> it = stream.iterator();
        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfMessagesToConsume && it.hasNext(); i++) {
            final byte[] currentMessage = it.next().message();
            numberOfKilobytesConsumed += (double) currentMessage.length / (1000);
            numberOfMessagedConsumed = i;
            System.out.println(i + " - " + it.next().message().toString());
        }

        totalSecs = ((double) System.currentTimeMillis() - startTime) / 1000;
        consumerKpis.updateKpis(numberOfKilobytesConsumed, totalSecs, numberOfMessagedConsumed);
    }
}
