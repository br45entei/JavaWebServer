package com.gmail.br45entei;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/** @author Brian_Entei */
public class ResourceFactory {
	
	/** Attempts to load a file based on the given file name and parent folder.<br>
	 * If the file does not exist, this will try to create the file and create
	 * it's data from the jar's assets folder.<br>
	 * If the file does exist, this will return that file as is.
	 * 
	 * @param folder File
	 * @param path String
	 * @return The resulting file */
	public static File getResourceFromStreamAsFile(File folder, String path) {
		return getResourceFromStreamAsFile(folder, path, FilenameUtils.getName(path));
	}
	
	/** Attempts to load a file based on the given file name and parent folder.<br>
	 * If the file does not exist, this will try to create the file and create
	 * it's data from the jar's assets folder.<br>
	 * If the file does exist, this will return that file as is.
	 * 
	 * @param folder File
	 * @param path String
	 * @param fileName The name of the resulting file
	 * @return The resulting file */
	public static File getResourceFromStreamAsFile(File folder, String path, String fileName) {
		path = path.startsWith("/") ? path : "/assets/" + path;
		File output = new File(folder, fileName);
		try {
			if(!output.exists()) {
				output.createNewFile();
			} else {
				/*@SuppressWarnings("resource")
				PrintWriter writer = new PrintWriter(output);
				writer.print("");
				try {writer.close();} catch(Throwable ignored) {}*/
				return output;
			}
			OutputStream out = new FileOutputStream(output);
			IOUtils.copy(ResourceFactory.getResourceAsStream(path), out);
			try {
				out.close();
			} catch(Throwable ignored) {
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	/** Attempts to load an InputStream based on the given file name.<br>
	 * If the file does not exist within the assets folder, this will return
	 * null.<br>
	 * If the file does exist, this will return the input stream for that file.
	 * 
	 * @param path String
	 * @return The resulting input stream(can be null) */
	public static InputStream getResourceAsStream(String path) {
		path = path.startsWith("/") ? path : "/assets/" + path;
		//LogUtils.info("Loading file: \"" + path + "\"");
		return ResourceFactory.class.getResourceAsStream(path);
	}
	
	/** @param zipFile The .zip file to un-zip
	 * @param folder The destination folder to un-zip into.
	 * @return Whether or not the un-zip operation was successful.
	 * @throws IOException Thrown if an error occurred while un-zipping the
	 *             files. */
	public static boolean unZipFile(File zipFile, File folder) throws IOException {
		if(folder == null || zipFile == null) {
			return false;
		}
		if(!zipFile.exists()) {
			return false;
		}
		if(!folder.exists()) {
			folder.mkdirs();
		}
		byte[] buf = new byte[1024];
		ZipInputStream zipinputstream = null;
		ZipEntry zipentry;
		zipinputstream = new ZipInputStream(new FileInputStream(zipFile));
		zipentry = zipinputstream.getNextEntry();
		while(zipentry != null) {
			String folderPath = folder.getAbsolutePath();
			if(!folderPath.endsWith("" + File.separatorChar)) {
				folderPath += File.separatorChar;
			}
			String entryName = folderPath + zipentry.getName();
			entryName = entryName.replace('/', File.separatorChar).replace('\\', File.separatorChar);
			FileOutputStream fileoutputstream = null;
			File newFile = new File(entryName);
			if(zipentry.isDirectory()) {
				newFile.mkdirs();
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
				continue;
			}
			File parentFolder = newFile.getParentFile();
			if(!parentFolder.exists()) {
				parentFolder.mkdirs();
			}
			try {
				if(!newFile.exists()) {
					newFile.createNewFile();
				}
				fileoutputstream = new FileOutputStream(newFile);
				int n;
				while((n = zipinputstream.read(buf, 0, 1024)) > -1) {
					fileoutputstream.write(buf, 0, n);
				}
				fileoutputstream.close();
			} catch(IOException e) {
				//LogUtils.error("Unable to write to file \"" + newFile.getAbsolutePath() + "\":", e);
			}
			if(fileoutputstream != null) {
				fileoutputstream.close();
			}
			zipinputstream.closeEntry();
			zipentry = zipinputstream.getNextEntry();
		}
		zipinputstream.close();
		return true;
	}
	
}
