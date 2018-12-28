package com.google;

import java.io.Serializable;

import org.apache.beam.sdk.transforms.DoFn;

import com.google.cloud.datastore.Entity;
import com.google.datastore.readOperations.DataStoreReadData;
import com.google.datastore.readOperations.DataStoreReadDataImpl;

public class EnhanceWithDataStore extends DoFn<String, Entity> {


	@ProcessElement
	public void processElement(ProcessContext c) throws Exception {
		String entityName = "VirginRedUser";
		Long userId = Long.valueOf(c.element());
		DataStoreReadData readData = new DataStoreReadDataImpl();
		Entity entity = readData.readUserIdFromDataStore(userId, entityName);
		if (entity != null) {
			c.output(entity);
		}
	}
	
	
}
