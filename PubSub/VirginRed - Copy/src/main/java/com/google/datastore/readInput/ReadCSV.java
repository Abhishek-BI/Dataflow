package com.google.datastore.readInput;

import org.apache.beam.sdk.values.PCollection;

public interface ReadCSV {
	
	void readList(PCollection<String> userList);

}
