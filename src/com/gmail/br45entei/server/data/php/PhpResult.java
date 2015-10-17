package com.gmail.br45entei.server.data.php;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public final class PhpResult {
	/** The path to the main PHP executable file */
	public static String	phpExeFilePath	= "./php/php-cgi.exe";
	
	public final String		headers;
	public final String		body;
	
	public PhpResult(String headers, String body) {
		this.headers = headers;
		this.body = body;
	}
	
	public static final boolean isPhpFilePresent() {
		File phpExeFile = new File(phpExeFilePath);
		return phpExeFile.exists();
	}
	
	public static final PhpResult execPHP(String scriptName, String param, boolean ignoreContentLengthLine, boolean ignoreContentTypeLine) {
		String headers = "";
		String body = "";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(phpExeFilePath + " " + scriptName + (param == null || param.isEmpty() ? "" : " " + param));
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			boolean lineEmpty = false;
			while((line = input.readLine()) != null) {
				boolean swappedThisTime = false;
				if(!lineEmpty && line.isEmpty()) {
					lineEmpty = true;
					swappedThisTime = true;
				}
				if(!swappedThisTime) {
					if(!lineEmpty) {
						if(!(ignoreContentLengthLine && line.toLowerCase().startsWith("content-length:")) && !(ignoreContentTypeLine && line.toLowerCase().startsWith("content-type:"))) {
							if(!line.isEmpty()) {
								headers += line + "\r\n";
							}
						}
					} else {
						body += line + "\r\n";
					}
				}
			}
			input.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return new PhpResult(headers.trim(), body);
	}
	
}
