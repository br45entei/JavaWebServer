package com.gmail.br45entei.server;

import com.gmail.br45entei.data.DisposableByteArrayInputStream;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.swt.Functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** @author Brian_Entei */
public class MultipartFormData {
	
	private final String contentType;
	//private volatile ClientStatus status = null;
	
	private final String boundary;
	private final String boundaryNext;
	//private final String					boundaryEnd;
	
	public MultipartFormData(InputStream in, String contentType, long contentLength) throws IOException, IllegalArgumentException {//, ClientStatus status) throws IOException, IllegalArgumentException {
		//this.status = status;
		//this.status.setStatus("Parsing request data...");
		this.contentType = contentType;
		if(!this.contentType.toLowerCase().contains("multipart/form-data")) {
			throw new IllegalArgumentException("Request is not of the type \"multipart/form-data\"!");
		}
		String boundary = null;
		String[] split = this.contentType.split(";");
		for(String s : split) {
			s = s.trim();
			if(s.toLowerCase().startsWith("boundary=")) {
				boundary = s.substring("boundary=".length());
			}
		}
		if(boundary == null) {
			throw new IllegalArgumentException("Request does not contain a \"boundary\" value!");
		}
		this.boundary = boundary;
		this.boundaryNext = "--" + this.boundary;
		//this.boundaryEnd = this.boundaryNext + "--";
		long count = 0L;
		
		int read;
		byte[] buf = new byte[2048];
		
		while(count < contentLength) {
			read = in.read(buf, 0, buf.length);
			if(read == -1) {
				break;
			}
			count += read;
			if(count >= contentLength) {
				break;
			}
			byte[] data = Arrays.copyOf(buf, read);//Use only the read bytes, not potential leftovers from before.
			DisposableByteArrayInputStream bais = new DisposableByteArrayInputStream(data);
			byte[] boundaryCheck = new byte[this.boundaryNext.length()];
			bais.read(boundaryCheck);
			String b = new String(boundaryCheck, StandardCharsets.UTF_8);
			if(b.equals(this.boundaryNext)) {
				boundaryCheck = new byte[2];
				bais.read(boundaryCheck);
				b = new String(boundaryCheck, StandardCharsets.UTF_8);
				if(b.equals("--")) {//It's an end boundary.
					data = null;
					bais.close();
					break;
				} // else {//It's a 'next' boundary, meaning there's (more) data past this point, so we need to start a new form data.
					//}
					//TODO
				
			} else {//Currently reading file data.
				//TODO
			}
			data = null;
			bais.close();
		}
	}
	
	public static final class FormData {
		
		private final ClientConnection reuse;
		
		private final String name;
		private final String fileName;
		private volatile DisposableByteArrayOutputStream fileData = null;
		
		public FormData(String name, String fileName, ClientConnection reuse) {
			this.reuse = reuse;
			this.name = name;
			this.fileName = fileName;
		}
		
		public final void appendData(byte[] data) {
			this.fileData.write(data, 0, data.length);
		}
		
		public final void writeToFile(File folder) throws IOException {
			if(folder == null || folder.isFile()) {
				throw new IllegalArgumentException("File argument for method \"writeToFile\" must be a directory!");
			}
			if(!folder.exists()) {
				folder.mkdirs();
			}
			byte[] data = this.fileData.getBytesAndDispose();
			final int size = data.length;
			File file = new File(folder, this.fileName);
			this.reuse.println("\tWriting FormData \"" + this.name + "\"'s file named \"" + this.fileName + "\" to disk...");
			try(FileOutputStream fos = new FileOutputStream(file, true)) {
				fos.write(data, 0, data.length);
				fos.flush();
			} catch(IOException e) {
				data = null;
				System.gc();
				throw e;
			} finally {
				data = null;
				System.gc();
			}
			if(file.isFile()) {//If it exists and is a file.
				this.reuse.println("\tOperation successful. Wrote " + Functions.humanReadableByteCount(size, true, 2) + " bytes to disk.");
			} else {
				this.reuse.println("\tOperation failed! File does not appear to exist.(???)");
			}
		}
		
		public final void writeToFileAndDispose1(File folder) throws IOException {
			this.writeToFile(folder);
			this.dispose1();
		}
		
		public final void dispose1() {
			this.reuse.println("Multipart-FormData dispose()");
			if(this.fileData != null) {
				this.fileData.dispose();
			}
			this.fileData = null;
			System.gc();
		}
		
	}
	
}
