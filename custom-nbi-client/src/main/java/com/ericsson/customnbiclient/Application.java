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
package com.ericsson.customnbiclient;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ericsson.customnbiclient.example.ProtobufDataSendingExample;
import com.ericsson.customnbiclient.example.S3DataSendingExample;

@SpringBootApplication
@ComponentScan
@EnableIntegration
@IntegrationComponentScan
@EnableScheduling
public class Application  implements CommandLineRunner {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("serverConnectionFactoryForS3")
    private AbstractServerConnectionFactory connectionFactory;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Application running");
        final S3DataSendingExample s3dataSendingExample = new S3DataSendingExample();
        final ProtobufDataSendingExample protobufDataSendingExample = new ProtobufDataSendingExample();

        try {
            s3dataSendingExample.sendMultipleMessagesUsingTheSameConnection();
            protobufDataSendingExample.sendData();
        } catch(final IOException e) {
            logger.error("Failed to send data", e);
        }
    }

}
