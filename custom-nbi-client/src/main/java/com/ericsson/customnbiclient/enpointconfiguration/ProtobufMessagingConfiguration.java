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
import org.springframework.integration.ip.tcp.serializer.TcpCodecs;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;

import com.ericsson.customnbiclient.example.protobuf.model.AddressBookProtos;
import com.google.protobuf.InvalidProtocolBufferException;

@Configuration
public class ProtobufMessagingConfiguration {

    private final Logger logger = LoggerFactory.getLogger(ProtobufMessagingConfiguration.class);
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicLong totalBytesTransfered = new AtomicLong();

    @Bean
    public IntegrationFlow flow() {
        return IntegrationFlows.from(Tcp.inboundAdapter(serverConnectionFactory())).handle(this::handleMessage).get();
    }

    private void handleMessage(Message<?> msg) {
        counter.incrementAndGet();
        final byte[] sensorMessage = (byte[]) msg.getPayload();
        totalBytesTransfered.addAndGet(sensorMessage.length);
        logger.info(String.format("Person name: %s", reconstructProtoObjectFromBytes(sensorMessage).getName()));
    }

    /**
     * @param sensorMessage
     */
    private AddressBookProtos.Person reconstructProtoObjectFromBytes(byte[] sensorMessage) {
        try {
            final AddressBookProtos.Person person = AddressBookProtos.Person.parseFrom(sensorMessage);
            return person;
        } catch (final InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Scheduled(fixedRate = 10000)
    public void printStatistics() {
        logger.info(String.format("Total Messages: %d", counter.get()));
        logger.info(String.format("Total Bytes Transfered: %d", totalBytesTransfered.get()));
    }

    @Bean(name="serverConnectionFactoryForProtobuf")
    public AbstractServerConnectionFactory serverConnectionFactory() {
        final TcpNetServerConnectionFactory tcpNetServerConnectionFactory = new TcpNetServerConnectionFactory(EndpointPort.PROTOBUF.getPort());
        tcpNetServerConnectionFactory.setSerializer(TcpCodecs.lengthHeader4());
        tcpNetServerConnectionFactory.setSingleUse(false);
        tcpNetServerConnectionFactory.setDeserializer(TcpCodecs.lengthHeader4());

        return tcpNetServerConnectionFactory;
    }

}
