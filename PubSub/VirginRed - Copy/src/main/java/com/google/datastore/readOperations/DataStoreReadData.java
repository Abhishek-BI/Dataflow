package com.google.datastore.readOperations;

import com.google.cloud.datastore.Entity;

public interface DataStoreReadData {
	Entity readUserIdFromDataStore(Long userId, String entityKind);
	public Entity findByEmail(String email, String Kind);
	Entity findByEncryptedEmail(String email, String Kind);
}
