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
package  test.io.netty;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;


public class GenericRecordGenerator  {
    private static final String AVSC = ".avsc";
    private static final String TIME_STAMP = "_TIMESTAMP";
    private static final String NE = "_NE";
    private static final String AVRO_SCHEMA_PATH = "avro";
    private long hashId;

    public GenericRecordGenerator() {

    }


    public org.apache.avro.Schema loadAvroSchema(final String schemaName, final String cacheEntry) {
    	org.apache.avro.Schema avroSch = null;
        try {
            avroSch = SchemaCacheMap.getInstance().getAvroSchema(cacheEntry);
            if (avroSch == null) {
                avroSch = this.parseNewAvroSchema(schemaName);
                SchemaCacheMap.getInstance().updateAvroSchemaCache(cacheEntry, avroSch);
            }
            this.hashId = SchemaCacheMap.getInstance().getHashId(cacheEntry);
        } catch (final Exception e) {
           avroSch = this.parseNewAvroSchema(schemaName);
           SchemaCacheMap.getInstance().updateAvroSchemaCache(cacheEntry, avroSch);

            this.hashId = SchemaCacheMap.getInstance().getHashId(cacheEntry); 
        }
        return avroSch;
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
            return schema;
        } catch (final IOException e) {
            return null;
        }
    }
}
