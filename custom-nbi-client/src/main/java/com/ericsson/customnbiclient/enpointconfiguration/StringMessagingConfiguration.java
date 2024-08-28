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

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class StringMessagingConfiguration {

    private final byte newLineChar = 0x0A;
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicLong totalBytesTransfered = new AtomicLong();
    private final Logger logger = LoggerFactory.getLogger(StringMessagingConfiguration.class);

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        final TcpNetServerConnectionFactory tcpNetServerConnectionFactory = new TcpNetServerConnectionFactory(EndpointPort.STRING.getPort());
        final ByteArraySingleTerminatorSerializer serializer = new ByteArraySingleTerminatorSerializer(newLineChar);
        tcpNetServerConnectionFactory.setSerializer(serializer);
        tcpNetServerConnectionFactory.setSingleUse(true);
        tcpNetServerConnectionFactory.setDeserializer(serializer);


        return tcpNetServerConnectionFactory;
    }

    @Bean
    public TcpReceivingChannelAdapter inboundAdapter() {
        final TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();

        adapter.setOutputChannel(toSA());
        adapter.setConnectionFactory(serverConnectionFactory());
        return adapter;
    }

    @Bean
    public MessageChannel toSA() {
        return new DirectChannel();
    }

    @ServiceActivator(inputChannel = "toSA")
    public void endDirectFlow(Message<?> message) throws UnsupportedEncodingException {
        counter.incrementAndGet();
        final byte[] payload = (byte[]) message.getPayload();
        totalBytesTransfered.addAndGet(payload.length);
        //        logger.info(new String(payload, "UTF-8"));
    }

    @Scheduled(fixedRate = 10000)
    public void printStatistics(){
        logger.info(String.format("Total Messages: %d",counter.get()));
        logger.info(String.format("Total Bytes Transfered: %d", totalBytesTransfered.get()));
    }

}
