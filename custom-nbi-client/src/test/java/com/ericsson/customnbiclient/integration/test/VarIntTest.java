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
package com.ericsson.customnbiclient.integration.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.ericsson.customnbiclient.varint.VarInt;

/**
 * See https://developers.google.com/protocol-buffers/docs/encoding#varints
 *
 * @author ezjoaro
 *
 */
public class VarIntTest {

    @Test
    public void intToVarIntTest() throws IOException{
        final long originalLongValue = 300l;
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(originalLongValue);

        final VarInt encodedVarint = new VarInt(originalLongValue);
        final byte[] varintAsByteArray = encodedVarint.encode();

        final VarInt decodedVarInt = new VarInt(varintAsByteArray, 0);
        final long longValueFromVarInt = decodedVarInt.value;

        assertEquals(longValueFromVarInt, originalLongValue);
    }
}
