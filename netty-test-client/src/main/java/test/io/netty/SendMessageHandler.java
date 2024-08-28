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

import org.apache.avro.Schema;
import org.apache.avro.generic.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

public class SendMessageHandler{
    private static final int BYTES_ADDED = 16;
	SchemaCacheMap schemaCacheMap;
	String cacheEntry;
	
    public SendMessageHandler(SchemaCacheMap schemaCacheMap, String cacheEntry) {	        
    	this.schemaCacheMap = schemaCacheMap;
    	this.cacheEntry = cacheEntry;
    }
    
	public void writeToChannel(Channel channel) {
        final byte[] serializedEvent = encode(generateGenericRecord(), cacheEntry);
		
		//System.out.println("message length: "+serializedEvent.length);
		
		ByteBuf buf = Unpooled.buffer().writeBytes(serializedEvent);
		channel.writeAndFlush(buf);
	}

	 private GenericRecord generateGenericRecord() {
	        final org.apache.avro.Schema avroSchema = schemaCacheMap.getAvroSchema(cacheEntry);

	        final GenericRecord genericRecord = new GenericData.Record(avroSchema);
	        genericRecord.put("_TIMESTAMP", (long) 0);
	        genericRecord.put("_NE", null);
	        genericRecord.put("TIMESTAMP_HOUR", (byte) 6);
	        genericRecord.put("TIMESTAMP_MINUTE", (byte) 40);
	        genericRecord.put("TIMESTAMP_SECOND", (byte) 5);
	        genericRecord.put("TIMESTAMP_MILLISEC", 890);
	        genericRecord.put("SCANNER_ID", (long) 2097152);
	        genericRecord.put("RBS_MODULE_ID", (byte) 0);
	        genericRecord.put("GLOBAL_CELL_ID", (long) 13056527);
	        return genericRecord;
	}
	
	public byte[] encode(final GenericRecord record, String schemaName) {
        final byte[] encodedRecord = this.serializeAvroRecord(record, record.getSchema());
  
        return prependExtraBytes(encodedRecord, schemaCacheMap.getHashId(schemaName));        
    }
	
    protected byte[] serializeAvroRecord(final GenericRecord avroRecord,
            final Schema avroSchema) {
        byte[] serializedBytes = null;

        final NettyGenericDatumWriter<GenericRecord> avroWriter = new NettyGenericDatumWriter<GenericRecord>(
                avroSchema);
        final ByteBuf buffer = ByteBufUtils.allocate();
        ByteBufEncoder byteBufEncoder = new ByteBufEncoder();
        byteBufEncoder.setBuffer(buffer);
        try {
            avroWriter.write(avroRecord, byteBufEncoder);
            byteBufEncoder.flush();
            serializedBytes = ByteBufUtils.toArray(buffer);
            buffer.release();
        } catch (final Exception e) {
			e.printStackTrace();
        }
        return serializedBytes;
    }

    public byte[] prependExtraBytes(final byte[] encodedRecord, final long hashId) {
        final ByteBuf buffer = ByteBufUtils.allocate(encodedRecord.length + BYTES_ADDED);
        //4 bytes reserved for (1 bytes - Magic byte, 1 bytes - Serialization,  1 bytes - Compression, 1 bytes - Reserved 
        buffer.writeInt(0);
        // 8 bytes - Schema ID
        buffer.writeLong(hashId);
        // 4 bytes - Event size
        buffer.writeInt(encodedRecord.length);
        buffer.writeBytes(encodedRecord);
        final byte[] result = ByteBufUtils.toArray(buffer);
        buffer.release();
        return result;
    }
}
