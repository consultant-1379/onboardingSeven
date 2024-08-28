package com.ericsson.asr.poc.kafkametrics.consumer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import com.ericsson.asr.poc.kafkametrics.utils.Utils;

public class ConsumerGroupExample {
    private final ConsumerConnector consumer;
    private final String topic;
    private final long numMessages;
    private ExecutorService executor;

    public ConsumerGroupExample(String topic, long numMessages, Properties properties) throws Exception {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        this.topic = topic;
        this.numMessages = numMessages;
    }

    public void shutdown() {
        if (consumer != null)
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

    public void run(int numThreads) {
        final Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(numThreads));
        final Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        executor = Executors.newFixedThreadPool(numThreads);

        System.out.println("Number of streams:" + streams.size());
        // now create an object to consume the messages
        int threadNumber = 0;
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber, numMessages));
            threadNumber++;
        }
    }

    public static void main(String[] args) throws Exception {
        final String topic = "test";
        final int threads = 3;
        final long numMessages = 100;        
        final String propertiesFile = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "consumer.properties";
        final Properties properties = Utils.readPropertiesFile(propertiesFile);
        final ConsumerGroupExample example = new ConsumerGroupExample(topic, numMessages, properties);
        example.run(threads);

        try {
            Thread.sleep(10000);
        } catch (final InterruptedException ie) {

        }
        example.shutdown();
    }
}