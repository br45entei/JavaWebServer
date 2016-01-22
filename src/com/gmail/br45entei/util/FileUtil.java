package com.gmail.br45entei.util;

import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.server.data.FileInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

/** @author Brian_Entei */
public class FileUtil {
	
	public static final byte[] readFileData(File file) {
		DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream();
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] buf = new byte[2048];
			int read;
			while((read = fis.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, read);
			}
		} catch(IOException ignored) {
		}
		byte[] bytes = baos.getBytesAndDispose();
		baos.close();
		return bytes;
	}
	
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
	
	public static final String readFile(File file, Charset charset) {
		if(charset == null) {
			return readFile(file);
		}
		byte[] bytes = readFileData(file);
		String rtrn = new String(bytes, charset);
		bytes = null;
		System.gc();
		return rtrn;
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
