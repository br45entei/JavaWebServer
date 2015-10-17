package com.gmail.br45entei.util;

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
	
}
