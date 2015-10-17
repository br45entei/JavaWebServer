package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.MimeTypes;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public final class FileInfo {
	
	public final DomainDirectory	domainDirectory;
	
	/** The rate at which the data transfer speed is calculated */
	public long						updateTime			= 1000L;
	
	/** Whether or not this file request is cancelled */
	public boolean					isCancelled			= false;
	/** Whether or not this file request is paused */
	public boolean					isPaused			= false;
	
	/** The last time any data was written to an output stream */
	public long						lastWriteTime		= 0L;
	/** The current byte amount being written to an output stream */
	public long						currentWriteAmount	= 0L;
	/** The last amount of bytes being written to an output stream */
	public long						lastWriteAmount		= 0L;
	
	/** The amount of bytes transferred to the output stream */
	public long						bytesTransfered		= 0L;
	/** The amount of bytes that this file is composed of */
	public String					contentLength;
	/** This file's mime type */
	public final String				mimeType;
	/** This file's name */
	public final String				fileName;
	/** The full path to this file */
	public final String				filePath;
	/** The last time this file was modified */
	public final String				lastModified;
	/** The last time this file was modified(in milliseconds) */
	public final long				lastModifiedLong;
	/** Whether or not this file is currently being written to */
	public boolean					isBeingWrittenTo	= false;
	
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
		this.lastModifiedLong = file.lastModified();
		if(file.exists() && file.isFile()) {
			URL fileURL = file.toURI().toURL();
			final URLConnection conn = fileURL.openConnection();
			this.contentLength = conn.getContentLengthLong() + "";
			this.lastModified = StringUtil.getTime(this.lastModifiedLong, false) + "";
			InputStream in = conn.getInputStream();
			if(in != null) {
				try {
					in.close();//XXX IMPORTANT! This allows the file to be edited by other programs/users, preventing this java program from locking up the file.
				} catch(Throwable ignored) {
				}
			}
		} else if(file.exists() && file.isDirectory()) {
			this.contentLength = calculateDirectorySizes ? FileUtils.sizeOfDirectory(file) + "" : "0";
			this.lastModified = StringUtil.getTime(this.lastModifiedLong, false);
		} else {
			this.contentLength = "0";
			this.lastModified = "UNKNOWN";
		}
		this.filePath = file.getAbsolutePath();
		this.fileName = FilenameUtils.getName(this.filePath);
		if(file.exists() && file.isFile()) {
			if(this.fileName.equals("package-list")) {
				if(this.domainDirectory != null) {
					this.mimeType = this.domainDirectory.getMimeTypeForExtension(".txt");
				} else {
					this.mimeType = MimeTypes.getMimeTypeForExtension(".txt");
				}
			} else {
				if(this.domainDirectory != null) {
					this.mimeType = this.domainDirectory.getMimeTypeForExtension("." + FilenameUtils.getExtension(this.fileName));//getMIMETypeForExtension("." + FilenameUtils.getExtension(this.fileName));
				} else {
					this.mimeType = MimeTypes.getMimeTypeForExtension("." + FilenameUtils.getExtension(this.fileName));
				}
			}
		} else if(file.exists() && file.isDirectory()) {
			this.mimeType = "Directory";
		} else {
			this.mimeType = "UNKNOWN";
		}
	}
	
	@Override
	public final String toString() {
		final double min = new Double(this.bytesTransfered + ".00D").doubleValue();
		final double max = new Double(this.contentLength + ".00D").doubleValue();
		final double result = (min / max) * 100.00D;
		return "Requested file: \"" + this.fileName + "\"; Percent complete: " + new Long(Math.round(result)).longValue();
	}
	
}
