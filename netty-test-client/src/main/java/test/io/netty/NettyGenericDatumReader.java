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
package test.io.netty;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.*;

public class NettyGenericDatumReader<D> extends GenericDatumReader<D> {
    /**
     * Default constructor.
     *
     * @param root
     *            schema reader
     */
    public NettyGenericDatumReader(final Schema root) {
        this.setSchema(root);
    }

    /**
     * Constructor given writer's and reader's schema.
     *
     * @param writer
     *            schema writer
     * @param reader
     *            schema reader
     */
    public NettyGenericDatumReader(final Schema writer, final Schema reader) {
        super(writer, reader, GenericData.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public D read(final D reuse, final Decoder in) throws IOException {
        final ResolvingDecoder resolver = new ByteBufResolvingDecoder(
                Schema.applyAliases(this.getSchema(), this.getExpected()),
                this.getExpected(), in);

        final D result = (D) this.read(reuse, this.getExpected(), resolver);
        resolver.drain();
        return result;
    }
}