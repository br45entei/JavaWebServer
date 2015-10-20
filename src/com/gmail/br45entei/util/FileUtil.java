package com.gmail.br45entei.util;

import com.gmail.br45entei.server.data.FileInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/** @author Brian_Entei */
public class FileUtil {
	
	public static final String readFile(File file) {
		StringBuilder sb = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\r\n");
			}
		} catch(IOException ignored) {
		}
		return sb.toString();
	}
	
	public static final boolean isFileAccessible(final File file) {
		if(file.exists()) {
			try {
				@SuppressWarnings("unused")
				FileInfo unused = new FileInfo(file, null);
				return true;
			} catch(IOException ignored) {
			}
		}
		return false;
	}
	
}
