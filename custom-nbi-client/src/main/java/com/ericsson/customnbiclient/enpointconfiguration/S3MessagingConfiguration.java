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
package com.ericsson.customnbiclient.enpointconfiguration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;

import com.ericsson.customnbiclient.serializer.S3DataSerializerDeserializer;

@Configuration
public class S3MessagingConfiguration {

    private final Logger logger = LoggerFactory.getLogger(S3MessagingConfiguration.class);
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicLong totalBytesTransfered = new AtomicLong();

    @Bean(name="flowForS3")
    public IntegrationFlow flow() {
        return IntegrationFlows.from(Tcp.inboundAdapter(serverConnectionFactory())).handle(this::handleMessage).get();
    }

    private void handleMessage(Message<?> msg) {
        counter.incrementAndGet();
        final byte[] sensorMessage = (byte[]) msg.getPayload();
        totalBytesTransfered.addAndGet(sensorMessage.length);
        //        printMessage(msg);
    }

    private void printMessage(Message<?> msg) {
        logger.info("Got message!");
        final byte[] sensorMessage = (byte[]) msg.getPayload();
        final StringBuilder message = new StringBuilder("");
        for (final byte element : sensorMessage) {
            message.append( element + " " );
        }
        logger.info(message.toString());
    }

    @Scheduled(fixedRate = 10000)
    public void printStatistics(){
        logger.info(String.format("Total Messages: %d",counter.get()));
        logger.info(String.format("Total Bytes Transfered: %d",totalBytesTransfered.get()));
    }

    @Bean(name="serverConnectionFactoryForS3")
    public AbstractServerConnectionFactory serverConnectionFactory() {
        final TcpNetServerConnectionFactory tcpNetServerConnectionFactory = new TcpNetServerConnectionFactory(EndpointPort.S3.getPort());
        final S3DataSerializerDeserializer serializer = new S3DataSerializerDeserializer();
        tcpNetServerConnectionFactory.setSerializer(serializer);
        tcpNetServerConnectionFactory.setDeserializer(serializer);

        return tcpNetServerConnectionFactory;
    }



}
