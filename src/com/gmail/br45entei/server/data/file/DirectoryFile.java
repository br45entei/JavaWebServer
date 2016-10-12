package com.gmail.br45entei.server.data.file;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public abstract class DirectoryFile {
	
	private static final String									fileExt			= ".txt";
	private static final ConcurrentLinkedQueue<DirectoryFile>	instances		= new ConcurrentLinkedQueue<>();
	
	protected final String										path;
	private volatile long										lastAccessed	= 0L;
	private volatile long										timesAccessed	= 0L;
	
	protected DirectoryFile(String path) {
		this.path = path;
		instances.add(this);
	}
	
	private static final int pathHashCode(String path) {
		return 31 * 1 + ((path == null) ? 0 : path.hashCode());
	}
	
	/** @return The last time, in milliseconds, that this file or folder was
	 *         accessed by any client. */
	public final long lastAccessed() {
		return this.lastAccessed;
	}
	
	protected final void markAccessed() {
		this.lastAccessed = System.currentTimeMillis();
		this.timesAccessed++;
	}
	
	/** @return The total number of times that this file or folder has been
	 *         accessed by clients. */
	public final long timesAccessed() {
		return this.timesAccessed;
	}
	
	/** Resets this {@link #DirectoryFile}'s statistics, then saves it to file. */
	public final void resetStats() {
		this.lastAccessed = 0L;
		this.timesAccessed = 0L;
		this.saveToFile();
	}
	
	/** @return Whether or not this data represents a file */
	public abstract boolean isFile();
	
	/** @return Whether or not this data represents a folder */
	public abstract boolean isDirectory();
	
	/** @return Whether or not the file or folder exists */
	public abstract boolean exists();
	
	/** @return Saves this {@link #DirectoryFile}'s statistics and data to file. */
	public final boolean saveToFile() {
		try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(JavaWebServer.rootDir, "DirectoryData" + File.separatorChar + pathHashCode(this.path) + ".txt")), StandardCharsets.UTF_8), true)) {
			pr.println("path=" + this.path);
			pr.println("lastAccessed=" + this.lastAccessed);
			pr.println("timesAccessed=" + this.timesAccessed);
			pr.flush();
			return true;
		} catch(Throwable ignored) {
		}
		return false;
	}
	
	/** Disposes of this {@link #DirectoryFile} */
	public void dispose() {
		this.resetStats();
		instances.remove(this);
	}
	
	/** Deletes this {@link #DirectoryFile} from memory and the local filesystem. */
	public final void delete() {
		this.dispose();
		File file = new File(JavaWebServer.rootDir, "DirectoryData" + File.separatorChar + pathHashCode(this.path) + ".txt");
		FileDeleteStrategy.FORCE.deleteQuietly(file);
	}
	
	/** @return An array list containing files that appear to be */
	public static final File[] getFilesToLoad() {
		final File folder = new File(JavaWebServer.rootDir, "DirectoryData");
		if(!folder.exists()) {
			folder.mkdirs();
			return new File[0];
		}
		ArrayList<File> rtrn = new ArrayList<>();
		for(String fileName : folder.list()) {
			File file = new File(folder, fileName);
			if(file.isFile()) {//(If it exists and is a file)
				String ext = "." + FilenameUtils.getExtension(fileName);
				String name = FilenameUtils.getBaseName(fileName);
				if(fileExt.equalsIgnoreCase(ext) && StringUtil.isStrInt(name) && !name.contains(" ")) {
					rtrn.add(file);
				}
			}
		}
		return rtrn.toArray(new File[rtrn.size()]);
	}
	
}
