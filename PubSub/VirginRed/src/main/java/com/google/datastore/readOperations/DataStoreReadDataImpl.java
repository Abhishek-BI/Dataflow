package com.google.datastore.readOperations;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

public class DataStoreReadDataImpl implements DataStoreReadData{

	private static DatastoreOptions options = DatastoreOptions.newBuilder()
			.setProjectId("virgin-red-test2").build();
	private static Datastore datastore = options.getService();
	
	@Override
	public Entity readUserIdFromDataStore(Long userId, String entityKind) {
		try {
			Entity entity = getKey(userId, entityKind);
			return entity;
		} catch(Exception e) {
			System.err.println("Exception found while getting entity "+e.getMessage());
		}
		return null;
	}
	
	private Entity getKey(Long identifier, String entityKind)  {
		KeyFactory keyFactory = datastore.newKeyFactory().setKind(entityKind);
		Key key = keyFactory.newKey(identifier);
		return datastore.get(key);
	}

	@Override
	public Entity findByEmail(String email, String kind) {
//		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//		Query q = new Query(kind).addFilter("email", FilterOperator.EQUAL, email);
//		PreparedQuery pQuery = ds.prepare(q);
//		QueryResultIterator<Entity> list = pQuery.asQueryResultIterator();
//		if (list != null ) {
//			List<Entity> myList = Lists.newArrayList(list);  
//			if (myList.isEmpty() ) {
//				System.out.println("List is empty");
//				return null;
//			}
//			if (myList.size() > 1) {
//				System.out.println("More then one recoreds found");
//			}
//			return myList.get(0);
//		}
		return null;
	}

	@Override
	public Entity findByEncryptedEmail(String email, String kind) {
		return findByEmail(email, kind);
	}
	
	
	
}
