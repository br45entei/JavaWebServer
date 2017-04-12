package com.gmail.br45entei.server.data;

import com.gmail.br45entei.data.DisposableByteArrayInputStream;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.server.ClientStatus;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.PrintUtil;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/** @author Brian_Entei */
public final class MultipartFormData implements Closeable {
	
	private volatile ClientStatus status;
	
	/** The content type used to initialize this instantiation(must contain the
	 * string {@code "multipart/form-data"} and a {@code "boundary"} data value,
	 * ignoring case) */
	public final String contentType;
	
	/** A HashMap containing any form data retrieved while parsing the input
	 * data */
	public final HashMap<String, String> formData = new HashMap<>();
	/** An ArrayList containing any {@link FileDataUtil} instances that may have
	 * been
	 * created while parsing the input data
	 * 
	 * @see #close() */
	public final ArrayList<FileDataUtil> fileData = new ArrayList<>();
	
	/** @param data The data to parse
	 * @param contentType The content type sent from the client(must include the
	 *            "multipart/form-data" content type and the boundary data
	 *            value)
	 * @param status The status that will be updated as the data is parsed
	 * @throws IllegalArgumentException Thrown if the content type was not
	 *             "multipart/form-data" or if the boundary value was not
	 *             specified
	 * @throws OutOfMemoryError Thrown if the data is too large */
	public MultipartFormData(byte[] data, String contentType, ClientStatus status) throws IllegalArgumentException, OutOfMemoryError {
		this(data, contentType, status, null);
	}
	
	/** @param data The data to parse
	 * @param contentType The content type sent from the client(must include the
	 *            "multipart/form-data" content type and the boundary data
	 *            value)
	 * @param status The status that will be updated as the data is parsed
	 * @param request
	 * @throws IllegalArgumentException Thrown if the content type was not
	 *             "multipart/form-data" or if the boundary value was not
	 *             specified
	 * @throws OutOfMemoryError Thrown if the data is too large */
	@SuppressWarnings("resource")
	public MultipartFormData(byte[] data, String contentType, ClientStatus status, HTTPClientRequest request) throws IllegalArgumentException, OutOfMemoryError {
		this.status = status;
		this.contentType = contentType;
		if(!this.contentType.toLowerCase().contains("multipart/form-data")) {
			throw new IllegalArgumentException("Request is not of the type \"multipart/form-data\"!");
		}
		this.status.setStatus("Parsing request data...");
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
		DisposableByteArrayInputStream in = new DisposableByteArrayInputStream(data);
		final int contentLength = data.length;
		data = null;
		String contentDisposition = null;
		String fileName = null;
		String fileContentType = null;
		boolean isFile = false;
		boolean isFormData = false;
		String name = null;
		String value = null;
		boolean wasPrevLineEmpty = false;
		DisposableByteArrayOutputStream fileData = new DisposableByteArrayOutputStream();
		this.status.setStatus("Preparing to parse data as 'multipart/form-data'...");
		this.status.setCount(0);
		this.status.setContentLength(contentLength);
		String line;
		while((line = readLine(in, status)) != null) {
			status.markReadTime();
			this.status.setStatus("Parsing current 'multipart/form-data'...");
			line = line.trim();
			final boolean isLineEmpty = line.isEmpty();
			final boolean isLineBoundary = line.equals("--" + boundary);
			final boolean isLineEndBoundary = line.equals("--" + boundary + "--");
			if(isLineBoundary || isLineEndBoundary) {//Reset and go on to the next data or stop if the end boundary is found.
				contentDisposition = null;
				fileName = null;
				fileContentType = null;
				isFile = false;
				isFormData = false;
				name = null;
				value = null;
				wasPrevLineEmpty = false;
				if(!isLineEndBoundary) {
					if(HTTPClientRequest.debug) {
						PrintUtil.println("<boundary>");
					}
					continue;
				}
				if(HTTPClientRequest.debug) {
					PrintUtil.println("Reached end boundary with the following data left over: \"" + readLine(in, status) + "\";");
				}
				status.markReadTime();
				break;
			}
			if(HTTPClientRequest.debug) {
				PrintUtil.println((wasPrevLineEmpty ? "data" : "line") + ": \"" + line + "\";");
			}
			if(line.toLowerCase().startsWith("content-disposition: ")) {
				contentDisposition = line.substring("Content-Disposition: ".length());
				String[] split1 = contentDisposition.split(Pattern.quote(";"));
				for(String s : split1) {
					status.markReadTime();
					s = s.trim();
					if(s.contains("=")) {
						String[] entry = s.split(Pattern.quote("="));
						if(entry.length == 2) {
							String key = entry[0];
							String val = entry[1];
							if(key.equalsIgnoreCase("filename")) {
								isFile = true;
								fileName = val;
								status.setFileName(fileName);
								if(fileName != null && fileName.startsWith("\"") && fileName.endsWith("\"") && fileName.length() > 2) {
									fileName = fileName.substring(1, fileName.length() - 1);
								}
								if(fileName != null && fileName.equals("\"\"")) {
									fileName = "";
								}
							} else if(key.equalsIgnoreCase("name")) {
								isFormData = true;
								name = val;
							} else {
								LogUtils.ORIGINAL_SYSTEM_ERR.println("Unimplemented key: \"" + key + "\";");
							}
						}
					}
				}
			} else if(line.toLowerCase().startsWith("content-type: ")) {
				fileContentType = line.substring("content-type: ".length());
			} else if(isLineEmpty && isFile) {//hopefully we've found the data part of the form data here
				while(Main.isRunning()) {
					status.setStatus("Collecting current file data...");
					status.markReadTime();
					in.mark(0);//(the zero here does nothing according to the javadocs for ByteArrayInputStream)
					if(nextByteIsNextBoundary(in, boundary, status)) {
						status.markReadTime();
						in.reset();
						break;
					}
					in.reset();
					int r = in.read();//If not, go back and read more data...
					status.markReadTime();
					status.incrementCount();
					if(status.isPaused()) {
						while(status.isPaused()) {
							try {
								Thread.sleep(1L);
							} catch(Throwable ignored) {
							}
						}
					}
					if(status.isCancelled()) {
						fileData.close();
						in.close();
						throw new IllegalArgumentException("Request was cancelled.");
					}
					if(r == -1) {
						break;
					}
					//XXX The following 'redundant' code fixes an issue where two bytes are added to the end of the uploaded file each time it is uploaded by preventing "\r\n" to be written. Without this, the file eventually becomes corrupted/the checksum changes because it ends with additional line feed(s) (\r\n)
					in.mark(0);
					final byte rb = Integer.valueOf(r).byteValue();
					byte[] r0 = new byte[] {rb, Integer.valueOf(in.read()).byteValue()};
					if(new String(r0).equals("\r\n")) {
						if(nextByteIsNextBoundary(in, boundary, status)) {//...ensuring that the next two bytes aren't newline characters prior to the next boundary...
							status.markReadTime();
							in.reset();
							break;
						}
					}
					in.reset();
					HTTPClientRequest.addToDebugFile(new byte[] {rb}, false);
					fileData.write(r);//...and then write that data here
					status.markReadTime();
				}
				if(fileContentType != null && fileData.size() != 0 && fileName != null) {
					status.setStatus("Storing file data for later use...");
					this.fileData.add(new FileDataUtil(fileData, fileName, fileContentType));
					fileData = new DisposableByteArrayOutputStream();
				} else {//if(HTTPClientRequest.debug) {
					LogUtils.ORIGINAL_SYSTEM_ERR.println("\t /!\\Failed to record collected file data: one of the following is either null or empty(meaning \"0\"): fileContentType: \"" + fileContentType + "\"; fileData.size(): \"" + fileData.size() + "\"; fileName: \"" + fileName + "\";\r\n\t/___\\");
				}
			} else if(wasPrevLineEmpty && isFormData) {//if(!isLineEmpty && isFormData) {
				value = line;
				if(HTTPClientRequest.debug) {
					LogUtils.ORIGINAL_SYSTEM_OUT.println("Set form value to: \"" + value + "\"!");
				}
			} else if(!isLineEmpty) {
				if(HTTPClientRequest.debug) {
					LogUtils.ORIGINAL_SYSTEM_OUT.println("Unimplemented line: \"" + line + "\"!");
				}
			}
			if(isFormData && name != null && value != null) {
				status.setStatus("");
				this.formData.put(name, value);
				LogUtils.ORIGINAL_SYSTEM_OUT.println("name: \"" + name + "\"; value: \"" + value + "\";");
				//isFormData = false;
				//name = null;
				//value = null;
			}
			wasPrevLineEmpty = isLineEmpty;
			status.markReadTime();
		}
		fileData.close();
		fileData = null;
		in.close();
		in = null;
		System.gc();
		status.markReadTime();
	}
	
	/** Reads the next two bytes, constructs a new String via<br>
	 * <b>{@code new} {@link String#String(byte[])}</b>, and then checks to see
	 * if it is equal<br>
	 * to {@code "--"}. If so, proceeds to read {@code boundary.length()}
	 * bytes<br>
	 * and convert them to String and then checks if that String is equal to
	 * the<br>
	 * boundary.<br>
	 * Otherwise returns false.<br>
	 * <br>
	 * Note that this method does not call
	 * {@link ByteArrayInputStream#mark(int)} or
	 * {@link ByteArrayInputStream#reset()}, so to preserve your data stream for
	 * proper reading, call {@code in.mark(0)} before calling this method and
	 * then call {@code in.reset()} afterwards if necessary.
	 * 
	 * @param in The ByteArrayInputStream
	 * @param boundary The multipart/form-data boundary
	 * @param status The status to listen to in case it is paused or cancelled
	 * @return Whether or not the next byte that is read in the given
	 *         ByteArrayInputStream is the beginning of the next
	 *         multipart/form-data boundary */
	public static final boolean nextByteIsNextBoundary(ByteArrayInputStream in, String boundary, ClientStatus status) {
		if(status.isPaused()) {
			while(status.isPaused()) {
				try {
					Thread.sleep(1L);
				} catch(Throwable ignored) {
				}
			}
		}
		if(status.isCancelled()) {
			throw new IllegalArgumentException("Request was cancelled.");
		}
		String line = new String(new byte[] {Integer.valueOf(in.read()).byteValue(), Integer.valueOf(in.read()).byteValue()});//read the next two bytes(the next two might be two dashes "--" which could indicate the next boundary is coming up)
		if(line.startsWith("--")) {//Check to see if we are coming up on the next boundary
			byte[] r = new byte[boundary.length()];
			for(int i = 0; i < r.length; i++) {
				r[i] = Integer.valueOf(in.read()).byteValue();
			}
			line = new String(r);
			if(line.equals(boundary)) {//if(line.toLowerCase().contains(boundary.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	/** Clears the {@link #fileData} ArrayList and the {@link #formData}
	 * HashMap */
	@Override
	public final void close() {
		for(FileDataUtil data : this.fileData) {
			data.close();
		}
		this.fileData.clear();
		this.formData.clear();
		this.status = null;
		System.gc();
	}
	
	private static final String readLine(ByteArrayInputStream in, ClientStatus status) {
		String rtrn = "";
		byte read;
		while((read = Integer.valueOf(in.read()).byteValue()) != -1) {
			status.incrementCount();
			status.checkPause();
			if(status.isCancelled()) {
				throw new IllegalArgumentException("Request was cancelled.");
			}
			byte[] r0 = new byte[] {read};
			String r = new String(r0);
			HTTPClientRequest.addToDebugFile(r0, false);
			if(r.equals("\n")) {
				break;
			}
			rtrn += r;
		}
		if(rtrn.isEmpty() && read == -1) {
			return null;
		}
		return rtrn;
	}
	
}
