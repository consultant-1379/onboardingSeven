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
package com.ericsson.customnbiclient.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.customnbiclient.enpointconfiguration.EndpointPort;

public class ProtobufDataSendingExample {

    private final Log logger = LogFactory.getLog(this.getClass());
    private static SocketAddress remote = new InetSocketAddress("localhost", EndpointPort.PROTOBUF.getPort());

    private final byte[] data = {10, 4, 74, 111, 97, 111, 16, 1, 26, 27, 106, 111, 97, 111, 46, 114, 111, 100, 114, 105, 103, 117, 101, 115, 64, 101, 114, 105, 99, 115, 115, 111, 110, 46, 99, 111, 109};

    /**
     *
     * This method shows how to send protobuf bytes using a TCP socket.
     * Note that, for TCP communication purposes, we need to add 4 bytes
     * to effectively read only the bytes that are part of the message.
     * This size is added to the beginning of the message and it is read
     * by the serializer.
     *
     * @throws IOException
     */
    public void sendData() throws IOException{

        final int dataSize = data.length;
        final byte[] sizeHeader = new byte[4];
        sizeHeader[0] = (byte) (dataSize >> 24);
        sizeHeader[1] = (byte) (dataSize >> 16);
        sizeHeader[2] = (byte) (dataSize >> 8);
        sizeHeader[3] = (byte) (dataSize >> 0);

        final byte[] dataToSend = ArrayUtils.addAll(sizeHeader, data);

        try(SocketChannel socketChannel = SocketChannel.open(remote)){
            socketChannel.configureBlocking(true);
            logger.info(String.format("Number of Bytes: %d", data.length));

            socketChannel.configureBlocking(true);

            sendData(dataToSend, socketChannel);
        }
    }

    /**
     * @param data
     * @param socketChannel
     * @throws IOException
     */
    private void sendData(final byte[] data, SocketChannel socketChannel) throws IOException {
        if(socketChannel.isConnected()) {
            socketChannel.write(ByteBuffer.wrap(data));
        }else {
            while(socketChannel.isConnected() == false) {
                logger.info("Reconnecting...");
                socketChannel = SocketChannel.open(remote);
                socketChannel.configureBlocking(true);
            }
        }
    }

}
