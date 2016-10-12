package com.gmail.br45entei.server.data.file;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public class DirectoryData extends DirectoryFile {
	
	private final File		source;
	private final String	zipReference;
	private final boolean	usesZipReference;
	
	/** @param path */
	protected DirectoryData(String path) {
		super(path.replace("\\", "/"));
		path = this.path;
		if(path.startsWith("jar:/file:/")) {
			path = path.substring(11);
		} else if(path.startsWith("file:/")) {
			path = path.substring(6);
		}
		if(path.startsWith("//")) {
			path = path.substring(2);
		}
		String zipReference = null;
		boolean usesZipReference = false;
		if(path.contains("!/")) {//Zip file path.
			usesZipReference = true;
			zipReference = path.substring(path.lastIndexOf("!/") + 1);
		}
		File file = new File(path);
		if(!file.exists()) {
			this.source = file;
			this.zipReference = null;
			this.usesZipReference = false;
			return;
		}
		if(file.isFile()) {
			if(!usesZipReference) {
				throw new IllegalArgumentException("Path must denote a valid zip or jar file, or a valid directory(folder)!");
			}
			this.source = file;
			//TODO Implement reading the path to the folder within the archive here
		} else {
			this.source = file;
		}
		this.zipReference = zipReference;
		this.usesZipReference = usesZipReference;
	}
	
	/** @see com.gmail.br45entei.server.data.file.DirectoryFile#isFile() */
	@Override
	public boolean isFile() {
		return false;
	}
	
	/** @see com.gmail.br45entei.server.data.file.DirectoryFile#isDirectory() */
	@Override
	public boolean isDirectory() {
		return this.exists();
	}
	
	/** @see com.gmail.br45entei.server.data.file.DirectoryFile#exists() */
	@Override
	public boolean exists() {
		if(this.source != null && this.source.exists()) {
			if(this.usesZipReference) {
				return this.zipReferenceExists();
			}
			return this.source.isDirectory();
		}
		return false;
	}
	
	private final boolean zipReferenceExists() {
		if(this.source == null || (this.usesZipReference ? false : (this.zipReference == null || this.zipReference.isEmpty()))) {
			return false;
		}
		if(this.usesZipReference && (this.zipReference.trim().equals("/") || this.zipReference.trim().equals("\\"))) {
			return this.source.isFile();//TODO And maybe check that the file is a valid formatted zip file?
		}
		try(FileInputStream input = new FileInputStream(this.source)) {
			ZipInputStream zip = new ZipInputStream(input);
			ZipEntry entry;
			
			final String sourcePath = FilenameUtils.normalize(this.source.getAbsolutePath());
			String ref = this.zipReference.replace("\\", "/");
			ref = ref.startsWith("/") ? ref : "/" + ref;
			ref = ref.endsWith("/") ? ref.substring(0, ref.length() - 1) : ref;
			final String expectedPath = sourcePath + "!" + FilenameUtils.normalize(ref);
			while((entry = zip.getNextEntry()) != null) {
				
			}
			return true;
		} catch(Throwable ignored) {
		}
		return false;
	}
	
}
