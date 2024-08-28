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

import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;


public class NettyGenericDatumWriter<D> extends GenericDatumWriter<D> {

    final ByteBuffer DEFAULT = ByteBuffer.wrap(new byte[0]);

    /**
     * Default constructor.
     *
     * @param root
     *            schema
     */
    public NettyGenericDatumWriter(final Schema root) {
        this.setSchema(root);
    }

    protected void writeDefault(final Schema schema, final Object datum,
            final Encoder out) throws IOException {
        try {
            switch (schema.getType()) {
            case RECORD:
                this.writeRecord(schema, datum, out);
                break;
            case ENUM:
                this.writeEnum(schema, datum, out);
                break;
            case ARRAY:
                this.writeArray(schema, datum, out);
                break;
            case MAP:
                this.writeMap(schema, datum, out);
                break;
            case UNION:
                final int index = this.resolveUnion(schema, datum);
                out.writeIndex(index);
                this.write(schema.getTypes().get(index), datum, out);
                break;
            case FIXED:
                this.writeFixed(schema, datum, out);
                break;
            case STRING:
                this.writeString(schema, "", out);
                break;
            case BYTES:
                this.writeBytes(this.DEFAULT, out);
                break;
            case INT:
                out.writeInt((0));
                break;
            case LONG:
                out.writeLong(0);
                break;
            case FLOAT:
                out.writeFloat(0);
                break;
            case DOUBLE:
                out.writeDouble(0);
                break;
            case BOOLEAN:
                out.writeBoolean(false);
                break;
            case NULL:
                out.writeNull();
                break;
            default:
                this.error(schema, datum);
            }
        } catch (final NullPointerException e) {
            throw this.npe(e, " of " + schema.getFullName());
        }
    }

    @Override
    @Deprecated
    protected void write(final Schema schema, final Object datum,
            final Encoder out) throws IOException {
        if (datum == null) {
            this.writeDefault(schema, datum, out);
        } else {
            super.write(schema, datum, out);
        }

    }

    private void error(final Schema schema, final Object datum) {
        throw new AvroTypeException("Not a " + schema + ": " + datum);
    }

    @Override
    protected void writeBytes(final Object datum, final Encoder out)
            throws IOException {
        if (datum instanceof ByteBuffer) {
            out.writeBytes((ByteBuffer) datum);
        } else if (datum instanceof ByteBuf && out instanceof ByteBufEncoder) {
            ((ByteBufEncoder) out).writeBytes((ByteBuf) datum);
        } else {
            throw new AvroTypeException("can't write bytes! type: "
                    + datum.getClass().getCanonicalName());
        }
    }
}
