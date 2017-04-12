package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.DisposableByteArrayInputStream;
import com.gmail.br45entei.server.ClientStatus;
import com.gmail.br45entei.server.MimeTypes;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public final class FileInfo {
	
	public final File file;
	public final DomainDirectory domainDirectory;
	
	/** The amount of bytes that this file is composed of */
	public volatile long contentLength;
	/** This file's mime type */
	public final String mimeType;
	/** This file's name */
	public final String fileName;
	/** The full path to this file */
	public final String filePath;
	/** The last time this file was modified */
	public final String lastModified;
	/** The last time this file was modified(in milliseconds) */
	public final long lastModifiedLong;
	
	public static final String getMIMETypeFor(File file, DomainDirectory domainDirectory) {
		final String mimeType;
		final String fileName = FilenameUtils.getName(file.getAbsolutePath());
		if(file.exists() && file.isFile()) {
			if(fileName.equals("package-list")) {
				if(domainDirectory != null) {
					mimeType = domainDirectory.getMimeTypeForExtension("txt");
				} else {
					mimeType = MimeTypes.getMimeTypeForExtension(".txt");
				}
			} else {
				if(domainDirectory != null) {
					mimeType = domainDirectory.getMimeTypeForExtension(FilenameUtils.getExtension(fileName));
				} else {
					mimeType = MimeTypes.getMimeTypeForExtension("." + FilenameUtils.getExtension(fileName));
				}
			}
		} else if(file.exists() && file.isDirectory()) {
			mimeType = "Directory";
		} else {
			mimeType = "UNKNOWN";
		}
		return mimeType;
	}
	
	/** @param file The file that this instantiation represents
	 * @param domainDirectory The DomainDirectory that was used to connect
	 *            to this server
	 * @throws IOException Thrown if an I/O exception occurs */
	public FileInfo(File file, DomainDirectory domainDirectory) throws IOException {
		if(file == null) {
			throw new NullPointerException("\"File\" can't be null!");
		}
		this.domainDirectory = domainDirectory;
		final boolean calculateDirectorySizes = this.domainDirectory != null ? this.domainDirectory.getCalculateDirectorySizes() : JavaWebServer.calculateDirectorySizes;
		String filePath = file.getAbsolutePath();
		if(filePath.startsWith("." + File.separator)) {
			filePath = File.separator + filePath.substring(("." + File.separator).length());//Take the dang dot out...
		}
		if(filePath.contains("." + File.separator)) {
			filePath = filePath.replace("." + File.separator, "");
		}
		this.filePath = filePath;
		this.fileName = FilenameUtils.getName(this.filePath);
		this.file = new File(this.filePath);
		this.lastModifiedLong = FileUtil.getLastModified(this.file);
		if(this.file.isFile()) {
			this.contentLength = FileUtil.getSize(this.file);
			this.lastModified = StringUtils.getTime(this.lastModifiedLong, false);
		} else if(this.file.isDirectory()) {
			this.contentLength = calculateDirectorySizes ? FileUtils.sizeOfDirectory(this.file) : 0L;
			this.lastModified = StringUtils.getTime(this.lastModifiedLong, false);
		} else {
			this.contentLength = -1L;
			this.lastModified = "UNKNOWN";
		}
		this.mimeType = getMIMETypeFor(this.file, domainDirectory);
	}
	
	public final String getURLPath() {
		final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(this.domainDirectory.getDirectorySafe().getAbsolutePath() + File.separatorChar));
		//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
		//println("pathPrefix: \"" + pathPrefix + "\"");
		
		String path = this.filePath.replace(pathPrefix, "").replace('\\', '/');
		if(path.equalsIgnoreCase(pathPrefix.replace('\\', '/'))) {
			path = "/";
		}
		path = (path.trim().isEmpty() ? "/" : path);
		path = (path.startsWith("/") ? "" : "/") + path;
		return path;
	}
	
	public static final String getURLPathFor(DomainDirectory domainDirectory, File requestedFile) {
		final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectorySafe().getAbsolutePath() + File.separatorChar));
		String path = requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/');
		if(path.equalsIgnoreCase(pathPrefix.replace('\\', '/'))) {
			path = "/";
		}
		path = (path.trim().isEmpty() ? "/" : path);
		path = (path.startsWith("/") ? "" : "/") + path;
		return path;
	}
	
	@Override
	public final String toString() {
		return "\tRequested file: \"" + this.fileName + "\";";
	}
	
	/** @param input The input stream
	 * @param output The output stream
	 * @param mtu The network mtu to be used
	 * @param status The ClientInfo to use
	 * @return The amount of data copied
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final long copyInputStreamToOutputStream(InputStream input, OutputStream output, int mtu, ClientStatus status) throws IOException {
		byte[] buffer = new byte[mtu <= 0 ? 4096 : mtu];
		long count = 0;
		int n = 0;
		if(status != null && status.isCancelled()) {
			return count;
		}
		if(status != null) {
			status.markWriteTime();
		}
		while((n = input.read(buffer)) != -1) {
			if(status != null && status.isCancelled()) {
				return count;
			}
			if(status != null && status.isPaused()) {
				while(status.isPaused()) {
					Functions.sleep();
				}
				if(status.isCancelled()) {
					return count;
				}
			}
			if(status != null) {
				status.isBeingWrittenTo = true;
			}
			output.write(buffer, 0, n);
			output.flush();
			count += n;
			if(status != null) {
				status.markWriteTime();
				status.setCount(count);
				status.isBeingWrittenTo = false;
				//long currentTime = System.currentTimeMillis();
				//long elapsedTime = currentTime - status.getLastWriteTime();
				//status.currentWriteAmount += n;
				//if(elapsedTime >= status.updateTime) {
				//status.lastWriteTime = currentTime;
				//status.lastWriteAmount = status.currentWriteAmount;
				//status.currentWriteAmount = 0L;
				//}
				if(status.isCancelled()) {
					return count;
				}
			}
		}
		if(status != null) {
			status.isBeingWrittenTo = false;
		}
		return count;
	}
	
	/** @param input The input stream
	 * @param output The output stream
	 * @param mtu The network mtu to be used
	 * @param status The ClientInfo to use
	 * @return The amount of data copied
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final long copyDataToOutputStream(byte[] data, OutputStream output, int mtu, ClientStatus status) throws IOException {
		byte[] buffer = new byte[mtu <= 0 ? 4096 : mtu];
		long count = 0;
		int n = 0;
		if(status != null && status.isCancelled()) {
			return count;
		}
		if(status != null) {
			status.setContentLength(data.length);
			status.markWriteTime();
		}
		try(DisposableByteArrayInputStream input = new DisposableByteArrayInputStream(data)) {
			while((n = input.read(buffer)) != -1) {
				if(status != null && status.isCancelled()) {
					return count;
				}
				if(status != null && status.isPaused()) {
					while(status.isPaused()) {
						Functions.sleep();
					}
					if(status.isCancelled()) {
						return count;
					}
				}
				if(status != null) {
					status.isBeingWrittenTo = true;
				}
				output.write(buffer, 0, n);
				output.flush();
				count += n;
				if(status != null) {
					status.markWriteTime();
					status.setCount(count);//status.bytesTransfered = count;
					status.isBeingWrittenTo = false;
					/*long currentTime = System.currentTimeMillis();
					long elapsedTime = currentTime - status.lastWriteTime;
					status.currentWriteAmount += n;
					if(elapsedTime >= status.updateTime) {
						status.lastWriteTime = currentTime;
						status.lastWriteAmount = status.currentWriteAmount;
						status.currentWriteAmount = 0L;
					}*/
					if(status.isCancelled()) {
						return count;
					}
				}
			}
		}
		if(status != null) {
			status.isBeingWrittenTo = false;
		}
		return count;
	}
	
}
