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
package org.apache.avro.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.ResolvingDecoder;
import org.apache.avro.io.parsing.Symbol;
import org.apache.avro.util.Utf8;

import test.io.netty.ByteBufDecoder;

public class ByteBufResolvingDecoder extends ResolvingDecoder {

    /**
     * Default constructor.
     *
     * @param writer
     *            writer's scheme.
     * @param reader
     *            reader's scheme
     * @param decoder
     *            decoder to be used
     * @throws IOException
     *             in case of exception
     */
    public ByteBufResolvingDecoder(final Schema writer, final Schema reader,
            final Decoder decoder) throws IOException {
        super(writer, reader, decoder);
    }

    /**
     * Reads a byte-string written by Encoder.writeBytes. if old is not null and
     * has sufficient capacity to take in the bytes being read, the bytes are
     * returned in old.
     *
     * @param old
     *            buffer which should be reused if possible.
     * @return bytebuf
     * @throws IOException
     *             in case of error
     */
    public ByteBuf readBytes(final ByteBuf old) throws IOException {
        final Symbol actual = this.parser.advance(Symbol.BYTES);
        if (actual == Symbol.STRING) {
            final Utf8 string = this.in.readString(null);
            return ByteBufAllocator.DEFAULT.buffer(string.getByteLength())
                    .writeBytes(string.getBytes());
        } else {
            assert actual == Symbol.BYTES;
            return ((ByteBufDecoder) this.in).readBytes(old);
        }
    }

}
