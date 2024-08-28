package com.ericsson.asr.poc.avrotest;

import static org.junit.Assert.assertEquals;

import java.io.*;

import org.apache.avro.Schema;
import org.apache.avro.generic.*;
import org.apache.avro.io.*;
import org.junit.Test;

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

public class AvroTest {

    @Test
    public void test() throws IOException {
        Schema userSchema = new Schema.Parser().parse(new File("src\\main\\resources\\user_schema.xml"));
        Schema addressSchema = new Schema.Parser().parse(new File("src\\main\\resources\\address_schema.xml"));
        final Schema combinedSchema = new Schema.Parser().parse(new File("src\\main\\resources\\user_address_schema.xml"));

        GenericRecord userRecord = new GenericData.Record(userSchema);
        userRecord.put("name", "bob");
        userRecord.put("age", 21);

        GenericRecord addressRecord = new GenericData.Record(addressSchema);
        addressRecord.put("street", "O'Connell Street");
        addressRecord.put("city", "Dublin");

        byte[] userSerializedBytes = serializeAvroRecord(userRecord, userSchema);

        byte[] addressSerializedBytes = serializeAvroRecord(addressRecord, addressSchema);

        byte[] combinedSerializedBytes = combineByteArrays(userSerializedBytes, addressSerializedBytes);

        GenericRecord deSerializedGenericRecord = deSerializeAvroRecord(combinedSerializedBytes, combinedSchema);

        System.out.println(deSerializedGenericRecord.toString());
        assertEquals(deSerializedGenericRecord.get("name").toString(), "bob");
        assertEquals(deSerializedGenericRecord.get("age").toString(), "21");
        assertEquals(deSerializedGenericRecord.get("street").toString(), "O'Connell Street");
        assertEquals(deSerializedGenericRecord.get("city").toString(), "Dublin");
    }

    public byte[] serializeAvroRecord(GenericRecord genericRecord, Schema avroSchema) throws IOException {
        final GenericDatumWriter<GenericRecord> avroWriter = new GenericDatumWriter<GenericRecord>(avroSchema);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = null;
        encoder = EncoderFactory.get().binaryEncoder(out, encoder);

        avroWriter.write(genericRecord, encoder);
        encoder.flush();
        out.close();

        return out.toByteArray();
    }

    public byte[] combineByteArrays(byte[] userSerializedBytes, byte[] addressSerializedBytes) {
        byte[] finalSerializedBytes = new byte[userSerializedBytes.length + addressSerializedBytes.length];
        System.arraycopy(userSerializedBytes, 0, finalSerializedBytes, 0, userSerializedBytes.length);
        System.arraycopy(addressSerializedBytes, 0, finalSerializedBytes, userSerializedBytes.length, addressSerializedBytes.length);

        return finalSerializedBytes;
    }

    public GenericRecord deSerializeAvroRecord(byte[] serializedRecord, Schema avroSchema) throws IOException {
        final BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(serializedRecord, null);
        final DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(avroSchema);

        return reader.read(null, binaryDecoder);
    }

}
