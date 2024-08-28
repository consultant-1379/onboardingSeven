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
package com.ericsson.customnbiclient.serializer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;

public class S3DataSerializerDeserializer extends ByteArrayLengthHeaderSerializer {

    private final Log logger = LogFactory.getLog(this.getClass());
    private static final short MESSAGE_HEADER_SIZE = 2;

    public S3DataSerializerDeserializer() {
        super(2);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer#deserialize(java.io.InputStream)
     */
    @Override
    public byte[] deserialize(InputStream inputStream) throws IOException {
        final short messageLengthWithoutHeader = (short) (this.readHeader(inputStream) - MESSAGE_HEADER_SIZE);
        final short messageLength = (short) (messageLengthWithoutHeader + 2);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Message length is " + messageLengthWithoutHeader);
        }
        byte[] messagePart = null;
        try {
            if (messageLengthWithoutHeader > this.maxMessageSize) {
                throw new IOException("Message length " + messageLengthWithoutHeader + " exceeds max message length: " + this.maxMessageSize);
            }
            messagePart = new byte[messageLengthWithoutHeader];
            read(inputStream, messagePart, false);

            final byte[] messageLengthAsByteArray = {(byte) ((messageLength >> 8) & 0xff), (byte) (messageLength & 0xff)};

            final byte[] dataWithReaderSize = ArrayUtils.addAll(messageLengthAsByteArray, messagePart);
            return dataWithReaderSize;
        } catch (final IOException e) {
            publishEvent(e, messagePart, -1);
            throw e;
        } catch (final RuntimeException e) {
            publishEvent(e, messagePart, -1);
            throw e;
        }
    }

}
