package com.ericsson.asr.poc.kafkametrics.consumer;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class ConsumerTest implements Runnable {
    private final KafkaStream<byte[], byte[]> stream;
    private final int threadNumber;
    private final long numMessages;

    public ConsumerTest(KafkaStream<byte[], byte[]> stream, int threadNumber, long numMessages) {
        this.threadNumber = threadNumber;
        this.stream = stream;
        System.out.println(stream.toString());
        this.numMessages = numMessages;
    }

    public void run() {
        long messagesRead = 0;
        final ConsumerIterator<byte[], byte[]> it = stream.iterator();

        System.out.println("Stream Size: " + it.size());
        System.out.println("messagesRead: " + messagesRead);
        System.out.println("numMessages: " + numMessages);
        while (it.hasNext() && messagesRead < numMessages) {
            System.out.println("Thread " + threadNumber + ": " + new String(it.next().message()));
            messagesRead++;
        }

        System.out.println("messagesRead: " + messagesRead);
        System.out.println("Shutting down Thread: " + threadNumber);
    }
}