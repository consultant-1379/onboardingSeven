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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import com.ericsson.asr.poc.kafkametrics.utils.PerformanceKpis;

public class Consumer {

    private final ConsumerConnector consumer;
    private ExecutorService executor;
    private final long numberOfMessagesToConsume;
    private final String topicName;
    private final PerformanceKpis consumerKpis;

    public Consumer(final Properties properties, final String topicName, final long numberOfMessagesToConsume, final PerformanceKpis consumerKpis) {
        this.consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        this.numberOfMessagesToConsume = numberOfMessagesToConsume;
        this.topicName = topicName;
        this.consumerKpis = consumerKpis;
    }

    public void shutdown() {
        if (consumer != null)
            consumer.commitOffsets();
        consumer.shutdown();
        if (executor != null)
            executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            } else {
                System.out.println("Shutdown");
            }
        } catch (final InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public void execute() throws InterruptedException, ExecutionException {
        final int numberOfThreads = 2;
        // kafka has a thread pool of 10 threads for each consumer

        final Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topicName, new Integer(numberOfThreads));

        final Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topicName);

        executor = Executors.newFixedThreadPool(numberOfThreads);

        Future<?> threadExit = null;
        final List<Future<?>> threadExitList = new ArrayList<Future<?>>();

        for (final KafkaStream<byte[], byte[]> stream : streams) {
            System.out.println(stream);
            threadExit = executor.submit(new RunnableConsumer(stream, numberOfMessagesToConsume, consumerKpis));
            threadExitList.add(threadExit);
        }

        int threadsFinished = 0;

        while (threadsFinished < threadExitList.size()) {
            threadsFinished = 0;
            for (final Future<?> exit : threadExitList) {
                if (exit.isDone()) {
                    threadsFinished++;
                }
            }
        }

        for (final Future<?> exit : threadExitList) {
            if (exit.isDone()) {
                exit.get();
            }
        }

        shutdown();

        System.out.println("finished closing");
        // executor.shutdown();
        // consumer.commitOffsets();
        // consumer.shutdown();
    }
}