package com.gmail.br45entei.server.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/** @author Brian_Entei */
public interface SavableData {
	
	public File getSaveFolder();
	
	public File getSaveFile() throws IOException;
	
	public boolean saveToFile();
	
	public boolean loadFromFile();
	
	public Object setValuesFromHashMap(HashMap<String, String> values);
	
}
