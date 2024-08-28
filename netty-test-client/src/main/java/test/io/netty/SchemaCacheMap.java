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

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;

import test.io.netty.RegistryItem;

public class SchemaCacheMap {
	private final RabinHashFunction hashFunction;
	private final Map<String, RegistryItem> cacheWithSchemaName;
	private final Map<Long, RegistryItem> cacheWithSchemaId;
    private static SchemaCacheMap instance = null;

	public SchemaCacheMap() {
		hashFunction = new RabinHashFunction();
		cacheWithSchemaName = new HashMap<String, RegistryItem>();
		cacheWithSchemaId = new HashMap<Long, RegistryItem>();
	}

    public static SchemaCacheMap getInstance() {
        if (instance == null) {
            instance = new SchemaCacheMap();
        }
        return instance;
    }
    
	public void updateAvroSchemaCache(String schemaName, Schema avroSchema) {
		long hashId = hashFunction.hash(schemaName);
		final RegistryItem registryItem = new RegistryItem(schemaName, hashId,
				null, avroSchema);
		cacheWithSchemaId.put(hashId, registryItem);
		cacheWithSchemaName.put(schemaName, registryItem);
        //System.out.println(hashId+"------"+registryItem+"------"+schemaName+"------"+avroSchema);
	}

	
	public Schema getAvroSchema(long hashId) {
		final RegistryItem registryItem = cacheWithSchemaId.get(hashId);
		return registryItem.getSchema();
	}

	
	public Schema getAvroSchema(String schemaName) {
		Schema schema = null;
		RegistryItem item = cacheWithSchemaName.get(schemaName);
		if(item != null){
			schema = item.getSchema();
		}
		return schema;
	}

	public long getHashId(String schemaName) {
		return cacheWithSchemaName.get(schemaName).getId();
	}
}
