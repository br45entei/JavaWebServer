package com.gmail.br45entei.server.data;

import com.gmail.br45entei.data.DisposableByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Class used to store the byte content of one file so that it may be
 * later read from/written to file.
 *
 * @author Brian_Entei */
public final class FileDataUtil implements Closeable {
	private final DisposableByteArrayOutputStream	fileData;
	/** The file's name */
	public final String								fileName;
	/** The file's content-type(does not decide how the file is written; may
	 * be null) */
	public final String								contentType;
	
	private static final DisposableByteArrayOutputStream fromBytes(byte[] data) {
		DisposableByteArrayOutputStream rtrn = new DisposableByteArrayOutputStream();
		try {
			rtrn.write(data);
		} catch(IOException ignored) {//This should never happen
			System.exit(-1);
		}
		return rtrn;
	}
	
	/** @param data The data
	 * @param fileName The name of the file
	 * @param contentType The file's content-type(does not decide how the
	 *            file is written; may be null) */
	public FileDataUtil(byte[] data, String fileName, String contentType) {
		this(fromBytes(data), fileName, contentType);
	}
	
	/** @param fileData The data
	 * @param fileName The name of the file
	 * @param contentType The file's content-type(does not decide how the
	 *            file is written; may be null) */
	public FileDataUtil(DisposableByteArrayOutputStream fileData, String fileName, String contentType) {
		this.fileData = fileData;
		this.fileName = fileName;
		this.contentType = contentType;
	}
	
	/** @param folder The parent folder which will be used when creating the
	 *            file
	 * @return The newly created file
	 * @throws IOException Thrown if something went wrong when either
	 *             creating the file or writing the data to the file */
	public final File writeFileToFolder(File folder) throws IOException {
		return writeFileToFolder(folder, true);
	}
	
	public final File writeFileToFolder(File folder, boolean wipeWhenDone) throws FileNotFoundException, IOException {
		if(!folder.exists()) {
			folder.mkdirs();
		}
		String fileName = checkFileName(this.fileName);
		File file = new File(folder, fileName);
		try(FileOutputStream out = new FileOutputStream(file)) {
			this.fileData.writeTo(out);
			if(wipeWhenDone) {
				this.close();
			}
			out.flush();
		}
		return file;
	}
	
	private static final int[]	illegalChars	= {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
	
	static {
		Arrays.sort(illegalChars);
	}
	
	public static String cleanFileName(String badFileName) {
		StringBuilder cleanName = new StringBuilder();
		int len = badFileName.codePointCount(0, badFileName.length());
		for(int i = 0; i < len; i++) {
			int c = badFileName.codePointAt(i);
			if(Arrays.binarySearch(illegalChars, c) < 0) {
				cleanName.appendCodePoint(c);
			}
		}
		return cleanName.toString();
	}
	
	private static final String checkFileName(String fileName) {
		return cleanFileName(fileName);
	}
	
	/** @return An InputStream containing this file's data */
	public final InputStream getFileDataAsInputStream() {
		return new ByteArrayInputStream(this.fileData.getBytesAndDispose());
	}
	
	@Override
	public final void close() {
		if(this.fileData != null) {
			this.fileData.close();
		}
	}
	
}
