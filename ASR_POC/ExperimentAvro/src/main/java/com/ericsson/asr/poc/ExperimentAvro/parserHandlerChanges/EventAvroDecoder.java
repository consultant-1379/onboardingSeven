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
package com.ericsson.oss.mediation.parsers.decoders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;

import com.ericsson.oss.mediation.avro.GenericRecordWrapper;
import com.ericsson.oss.mediation.parsers.handler.EventHeaderRecordHandler;
import com.ericsson.oss.mediation.parsersapi.base.generic.param.decoder.GenericParameterDecoder;
import com.ericsson.oss.mediation.parsersapi.base.meta.schema.Event;
import com.ericsson.oss.mediation.parsersapi.base.meta.schema.EventParameter;
import com.ericsson.oss.mediation.parsersapi.base.meta.schema.Schema;
import com.ericsson.poc.utils.AvroSchemaCache;

public class EventAvroDecoder extends EventDecoder {
	private static final String AVSC = ".avsc";
	private static final String TIME_STAMP = "timestamp";
	private static final String SUB_NETWORK = "subNetwork";
	private static final String NE = "ne";
	private static final String NAME = "name";
	private static final String VERSION = "version";
	private static final String AVRO_SCHEMA_PATH = "avro";
	private org.apache.avro.Schema avroSchema = null;
	private BinaryEncoder encoder = null;
	private long hashId;
	private final AvroSchemaCache avroSchemaCache;
	private static long EVENT_COUNTER;

	/**
	 * @param headerRecordHandler
	 */
	public EventAvroDecoder(final EventHeaderRecordHandler headerRecordHandler) {
		super(headerRecordHandler);
		this.avroSchemaCache = new AvroSchemaCache();
	}

	public EventAvroDecoder(final EventHeaderRecordHandler headerRecordHandler,
			final String schemaRegistrUrl) {
		super(headerRecordHandler);
		this.avroSchemaCache = new AvroSchemaCache();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ericsson.oss.mediation.parsers.decoders.EventDecoder#getDecodedEvent
	 * (com.ericsson.oss.mediation.parsersapi.base.meta.schema.Schema,
	 * com.ericsson.oss.mediation.parsersapi.base.meta.schema.Event, byte[])
	 */
	@Override
	public Object getDecodedEvent(final Schema schema, final Event event,
			final byte[] unpackedData) {

		final GenericRecordWrapper avroRecordWrapper = this.generateGenericRecordWrapper(schema,
				event, unpackedData);
		final byte[] serializedAvroRecord = this.serializeGenericRecord(avroRecordWrapper);
		return serializedAvroRecord;
	}

	/**
	 * @param schema
	 * @param event
	 * @param unpackedData
	 * @return
	 */
	public GenericRecordWrapper generateGenericRecordWrapper(final Schema schema,
			final Event event, final byte[] unpackedData) {
		this.initializeAvroSchema(schema, event);
		final GenericRecord avroRecord = this.generateGenericRecord(schema, event,
				unpackedData);
		final GenericRecordWrapper avroRecordWrapper = new GenericRecordWrapper(this.hashId, avroRecord);

		return avroRecordWrapper;
	}

	private byte[] serializeGenericRecord(final GenericRecordWrapper avroRecord) {
		byte[] serializedAvroRecord = this.serializeAvroRecord(avroRecord,
				this.avroSchema);
		serializedAvroRecord = this.addSchemaInfoToAvroRecord(serializedAvroRecord,
				this.avroSchema);
		return serializedAvroRecord;
	}

	public GenericRecord generateGenericRecord(final Schema schema,
			final Event event, final byte[] unpackedData) {
		GenericRecord avroRecord = this.getAvroRecordRepresentingEvent(this.avroSchema);

		if (avroRecord == null) {
			logger.warn("could not instantiate Avro Record");
			return null;
		}

		try {
			avroRecord = this.setFieldsInAvroRecord(schema, event, unpackedData,
					avroRecord);
		} catch (final ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
			this.logWarning(schema, event, arrayIndexOutOfBoundsException);
		} catch (final Exception exception) {
			this.logWarning(schema, event, exception);
			return null;
		}
		return avroRecord;
	}

	public GenericRecord setFieldsInAvroRecord(final Schema schema,
			final Event event, final byte[] unpackedData,
			final GenericRecord avroRecord) {
		// Track the offset in the record data, start at the bit position after
		// the EVENT_ID position in events without
		// event IDs as fields, at position 0 in events with event ID as a field
		int offset = this.getEventOffSet();
		avroRecord.put(TIME_STAMP,
				this.getEventTimeStamp(event, unpackedData, offset));
		avroRecord.put(NE, this.headerRecordHandler.getNeLogicalName());

		try {
			GenericParameterDecoder parameterDecoder = null;
			for (final EventParameter eventParameter : event.getParameterList()) {
				parameterDecoder = eventParameter.getParameterDecoder();

				Object decodedValue = parameterDecoder.getDecodeValue(
						unpackedData, offset, eventParameter);

				avroRecord.put(eventParameter.getName(), decodedValue);
				offset = parameterDecoder.adjustOffset(offset, eventParameter);
			}
		} catch (final Exception exception) {
			logger.error("could not process data of event {}.{}",
					schema.getName(), event.getName(), exception);
			return null;
		}
		//logger.info(avroRecord.toString());
		return avroRecord;
	}

	private byte[] addSchemaInfoToAvroRecord(final byte[] serializedAvroRecord,
			final org.apache.avro.Schema avroSchema) {
		final byte[] overallSchemaId = this.getOverallSchemaId(
				serializedAvroRecord.length, this.hashId);
		final byte[] serializedRecordWithIdentifier = this.combineSerializedRecordWithId(
				serializedAvroRecord, overallSchemaId);
		return serializedRecordWithIdentifier;
	}

	private byte[] getOverallSchemaId(final int sizeOfSerializedObject,
			final long hashId) {
		final byte[] schemaIdentifier = {
				(byte) (sizeOfSerializedObject >>> 24),
				(byte) (sizeOfSerializedObject >>> 16),
				(byte) (sizeOfSerializedObject >>> 8),
				(byte) sizeOfSerializedObject, (byte) (hashId >>> 56),
				(byte) (hashId >>> 48), (byte) (hashId >>> 40),
				(byte) (hashId >>> 32), (byte) (hashId >>> 24),
				(byte) (hashId >>> 16), (byte) (hashId >>> 8), (byte) hashId };

		return schemaIdentifier;
	}

	private byte[] combineSerializedRecordWithId(final byte[] serializedAvroRecord,
			final byte[] schemaIdentifier) {
		final byte[] serializedRecordWithIdentifer = new byte[schemaIdentifier.length
		                                                      + serializedAvroRecord.length];
		System.arraycopy(schemaIdentifier, 0, serializedRecordWithIdentifer, 0,
				schemaIdentifier.length);
		System.arraycopy(serializedAvroRecord, 0,
				serializedRecordWithIdentifer, schemaIdentifier.length,
				serializedAvroRecord.length);
		return serializedRecordWithIdentifer;
	}

	private GenericRecord getAvroRecordRepresentingEvent(
			final org.apache.avro.Schema avroSchema) {

		final GenericRecord avroRecord = new GenericData.Record(avroSchema);

		return avroRecord;
	}

	private byte[] serializeAvroRecord(final GenericRecordWrapper avroRecord,
			final org.apache.avro.Schema avroSchema) {
		byte[] serializedBytes = null;

		final GenericDatumWriter<GenericRecord> avroWriter = new DefaultGenericDatumWriter<GenericRecord>(
				avroSchema);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.encoder = EncoderFactory.get().binaryEncoder(out, this.encoder);

		try {
			avroWriter.write(avroRecord, this.encoder);
			this.encoder.flush();
			out.close();
			serializedBytes = out.toByteArray();
			//logger.info("Successfully serialized object");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serializedBytes;
	}

	public void initializeAvroSchema(final Schema schema, final Event event) {
		final String schemaName = AVRO_SCHEMA_PATH + File.separator
				+ schema.getAvroPackageName() + File.separator + event.getName()+ AVSC;
		final String cacheEntry = schema.getAvroPackageName() + "."
				+ event.getName();
		this.loadAvroSchema(schemaName, cacheEntry);
	}

	/**
	 * @param event
	 * @param schemaName
	 */
	public void loadAvroSchema(final String schemaName, final String cacheEntry) {
		try {
			org.apache.avro.Schema avroSch = this.avroSchemaCache
					.getAvroSchema(cacheEntry);
			if (avroSch == null) {
				avroSch = this.parseNewAvroSchema(schemaName);
				this.avroSchemaCache.updateAvroSchemaCache(cacheEntry, avroSch);
			}
			this.hashId = this.avroSchemaCache.getHashId(cacheEntry);
			this.setAvroSchema(avroSch);
		} catch (final Exception e) {
			final org.apache.avro.Schema avroSch = this.parseNewAvroSchema(schemaName);
			this.avroSchemaCache.updateAvroSchemaCache(cacheEntry, avroSch);

			this.hashId = this.avroSchemaCache.getHashId(cacheEntry);
			this.setAvroSchema(avroSch);
			// The Avro Schema for this event was not found
			logger.warn("could not instantiate Avro Schema: {}", e);
		}
	}

	private org.apache.avro.Schema parseNewAvroSchema(final String schemaName) {
		final org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
		final org.apache.avro.Schema schema;
		try {
			if (new File(schemaName).exists()) {
				schema = parser.parse(new File(schemaName));
			} else {
				schema = parser.parse(this.getClass().getResourceAsStream(
						File.separator + schemaName));
			}
			//logger.info("Avro Schema: " + schema.toString());
			return schema;
		} catch (final IOException e) {
			// The Avro Schema file for this event could not be read
			logger.warn("could not parse Avro Schema: {}", e);
			return null;
		}
	}

	public org.apache.avro.Schema getAvroSchema() {
		return this.avroSchema;
	}

	public void setAvroSchema(final org.apache.avro.Schema avroSchema) {
		this.avroSchema = avroSchema;
	}

	public Object generateExperimentalRecord(final Schema schema,
			final Event event, final byte[] unpackedData){
		
		this.initializeAvroSchema(schema, event);
		int offset = this.getEventOffSet();

		int noOfSchemaFields = this.avroSchema.getFields().size();

		int totalIntermediateArraySize = noOfSchemaFields * 2;
		int position, lengthOfFinalArray = 0;

		byte[][] interArray = new byte[totalIntermediateArraySize][];

		try {
			GenericParameterDecoder parameterDecoder = null;

			long timeStamp = this.getEventTimeStamp(event, unpackedData, offset);

			for (final EventParameter eventParameter : event.getParameterList()) {
				parameterDecoder = eventParameter.getParameterDecoder();
				position = avroSchema.getField(eventParameter.getName()).pos();
				byte[] decodedValue = parameterDecoder.getExperimentalDecodeValue(
						unpackedData, offset, eventParameter);

				if(decodedValue == null){
					decodedValue = new byte[0];
				}
				interArray[position * 2 + 1] = decodedValue;
				lengthOfFinalArray += decodedValue.length;
				interArray[position * 2] = getLengthByteArray(decodedValue.length);
				offset = parameterDecoder.adjustOffset(offset, eventParameter);
			}

			position = avroSchema.getField(TIME_STAMP).pos();
			interArray[position * 2 + 1] = ByteBuffer.allocate(8).putLong(timeStamp).array();
			interArray[position * 2] = getLengthByteArray(8);

			lengthOfFinalArray += Long.SIZE;

			byte[] stringArray = null;

			position = avroSchema.getField(NE).pos();
			stringArray = 	this.headerRecordHandler.getNeLogicalName().getBytes();
			interArray[position *2 + 1] = stringArray;
			interArray[position * 2] = getLengthByteArray(stringArray.length);
			lengthOfFinalArray += stringArray.length;

			lengthOfFinalArray += noOfSchemaFields * 2;
			byte[] finalArray = new byte[lengthOfFinalArray];

			int finalArrayPos = 0;
			for(byte[] tempArray: interArray ){	
				for(byte b: tempArray){
					finalArray[finalArrayPos] = b;
					finalArrayPos ++;
				}
			}

			//deserializeExperimentalAvro(finalArray);
//			if(EVENT_COUNTER == 0){
//				logger.info("Received first event");
//			}else if(EVENT_COUNTER%5000 == 0){
//				logger.info("5000 events received: "+EVENT_COUNTER);
//			}
//			
//			EVENT_COUNTER ++;
			return finalArray;
		} catch (final Exception exception) {
			logger.error("could not process data of event {}.{}",
					schema.getName(), event.getName(), exception);
			return null;
		}



	}

	private GenericRecord deserializeExperimentalAvro(byte[] serializedObject){

		final GenericRecord avroRecord = new GenericData.Record(avroSchema);

		int offset = 0;
		int length = 0;
		int lengthRecord = 2;
		Object decodedObject;

		for(Field f: avroSchema.getFields()){
			length = ((serializedObject[offset] & 0xff) << 8) | (serializedObject[offset + 1] & 0xff);
			offset += lengthRecord;
			if(length > 0){
				decodedObject = decodePayload(Arrays.copyOfRange(serializedObject, offset, offset + length), f.schema());
			}else{
				decodedObject = null;
			}

			offset += length;
			avroRecord.put(f.name(), decodedObject);
		}

		return avroRecord;
	}

	private byte[] getLengthByteArray(int length){
		byte[] lengthArray = ByteBuffer.allocate(2).putShort((short)length).array();
		return lengthArray;
	}

	private Object decodePayload(byte[] payload, org.apache.avro.Schema schema){

		try {
			switch (schema.getType()) {
			case STRING:  return new String(payload);  
			case BYTES:   return payload;           
			case INT:     return bytesToInt(payload); 
			case LONG:    return bytesToLong(payload);      
			case FLOAT:   return Float.intBitsToFloat(bytesToInt(payload));     
			case DOUBLE:  return Double.longBitsToDouble(bytesToLong(payload));   
			case BOOLEAN: return getBooleanFromByteArray(payload);
			default: throw new Exception(schema.getType().getName());
			}
		} catch (final Exception e) {
			logger.warn("Could not convert ",e);
		}
		return null;
	}

	public int bytesToInt(byte[] bytes){
		int res = 0;
		if (bytes == null)
			return res;


		for (int i = 0; i < bytes.length; i++)
		{
			res = (res << 8) + (bytes[i] & 0xff);
		}
		return res;
	}

	public long bytesToLong(byte[] bytes){
		long res = 0;
		if (bytes == null)
			return res;


		for (int i = 0; i < bytes.length; i++)
		{
			res = (res << 8) + (bytes[i] & 0xff);
		}
		return res;
	}

	private Boolean getBooleanFromByteArray(byte[] payload){
		int value = 0;
		for (int i = 0; i < payload.length; i++)
		{
			value = (value << 8) + (payload[i] & 0xff);
		}

		if(value == 0){
			return false;
		}

		return true;
	}
}
