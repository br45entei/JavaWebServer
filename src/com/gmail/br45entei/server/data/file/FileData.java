package com.gmail.br45entei.server.data.file;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.data.Property;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public final class FileData {
	
	protected static final ConcurrentLinkedDeque<FileData> dataToSaveLater = new ConcurrentLinkedDeque<>();
	
	private static final void saveLater(FileData data) {
		dataToSaveLater.add(data);
	}
	
	private static final Thread saveLaterThread = new Thread("FolderDataSaveLaterThread") {
		
		@Override
		public final void run() {
			while(JavaWebServer.serverActive()) {
				if(!dataToSaveLater.isEmpty()) {
					FileData data = dataToSaveLater.pop();
					while(JavaWebServer.serverActive()) {
						try {
							data._saveToFile();
							break;
						} catch(FileNotFoundException ignored) {
						} catch(Throwable e) {
							PrintUtil.printThrowable(e);
							PrintUtil.printErrToConsole();
						}
						CodeUtil.sleep(10L);
					}
				}
				CodeUtil.sleep(10L);
			}
		}
		
	};
	
	static {
		saveLaterThread.setDaemon(true);
		saveLaterThread.start();
	}
	
	private static final ConcurrentLinkedDeque<FileData> instances = new ConcurrentLinkedDeque<>();
	
	public static final FileData getByPath(String path) {
		for(FileData data : instances) {
			if(data.savePath.equals(path)) {
				return data;
			}
		}
		return null;
	}
	
	private final String savePath;
	
	public final Property<String> filePath = new Property<>("filePath", "");
	public final Property<Long> timesAccessed = new Property<>("timesAccessed", Long.valueOf(0L));
	
	public final HashMap<String, Property<?>> getPropertiesAsHashMap() {
		final HashMap<String, Property<?>> rtrn = new HashMap<>();
		this.filePath.toString();//shut up about how this method can be static already! XD
		//rtrn.put(this.defaultFileName.getName(), this.defaultFileName);
		return rtrn;
	}
	
	@SuppressWarnings("unused")
	public final FileData setValuesFromHashMap(HashMap<String, String> values) {
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			/*if(pname.equalsIgnoreCase(this.defaultFileName.getName())) {
				this.defaultFileName.setValue(value);
			}*/
		}
		//this.lastEdited.setValue(Long.valueOf(System.currentTimeMillis()));
		this.saveToFile();
		return this;
	}
	
	public static final String getSavePathFor(File file) {
		if(file == null || !file.isFile()) {
			return null;
		}
		return StringUtil.makeStringFilesystemSafe(FilenameUtils.normalize(file.getAbsolutePath()));
	}
	
	public static final FileData getFileData(File file, boolean createIfNotExist) {
		if(file == null || !file.isFile()) {
			return null;
		}
		String path = getSavePathFor(file);
		loadFileData(false, false);
		FileData data = getByPath(path);
		if(data == null && createIfNotExist) {
			data = new FileData(path);
			data.filePath.setValue(FilenameUtils.normalize(file.getAbsolutePath()));
			data.saveToFile();
			JavaWebServer.println("\tCreated new file properties record for: " + data.savePath);
			instances.add(data);
		}
		return data;
	}
	
	public final FileData incrementTimesAccessed() {
		this.timesAccessed.setValue(Long.valueOf(this.timesAccessed.getValue().longValue() + 1L));
		this.saveToFile();
		return this;
	}
	
	public static final void loadFileData(boolean clear, boolean reload) {
		if(clear) {
			instances.clear();
			reload = true;
		}
		File folder = getSaveFolder();
		for(String fileName : folder.list()) {
			File file = new File(folder, fileName);
			if(file.isFile() && fileName.endsWith(".txt")) {
				String savePath = fileName.substring(0, fileName.length() - 4);
				FileData check = getByPath(savePath);
				if(check == null || reload) {
					if(check != null && reload) {
						instances.remove(check);
					} else if(check != null && !reload) {
						continue;
					}
					FileData data = new FileData(savePath);
					try {
						data.loadFromFile();
					} catch(Throwable e) {
						PrintUtil.printErr("Failed to load FolderData(\"" + savePath + "\"): ");
						PrintUtil.printThrowable(e);
						PrintUtil.printErrToConsole();
						continue;
					}
					instances.add(data);
				}
			}
		}
	}
	
	public static final void saveFileData() {
		for(FileData data : instances) {
			data.saveToFile();
		}
	}
	
	public final String getSavePath() {
		return this.savePath;
	}
	
	public final void loadFromFile() throws IOException {
		try(FileInputStream in = new FileInputStream(this.getSaveFile())) {
			String line;
			while((line = StringUtil.readLine(in)) != null) {
				if(line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				String[] split = line.split(Pattern.quote("="));
				String pname = split[0];
				String value = StringUtil.stringArrayToString(split, '=', 1);
				value = value.equals("null") ? "" : value;
				boolean isLong = StringUtil.isStrLong(value);
				if(pname.equalsIgnoreCase("filePath")) {
					this.filePath.setValue(value);
				} else if(pname.equalsIgnoreCase("timesAccessed") && isLong) {
					this.timesAccessed.setValue(Long.valueOf(value));
				}
			}
		}
	}
	
	public final void saveToFile() {
		try {
			this._saveToFile();
		} catch(FileNotFoundException ignored) {
			saveLater(this);
		} catch(Throwable e) {
			PrintUtil.printThrowable(e);
			PrintUtil.printErrToConsole();
		}
	}
	
	protected final void _saveToFile() throws IOException {
		try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getSaveFile()), StandardCharsets.UTF_8), true)) {
			pr.println("filePath=" + this.filePath.getValue());
			pr.println("timesAccessed=" + this.timesAccessed.getValue().toString());
			pr.flush();
		}
	}
	
	public static final File getSaveFolder() {
		File folder = new File(JavaWebServer.rootDir + File.separator + "FileData" + File.separator);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	private FileData(String savePath) {
		this.savePath = savePath;
	}
	
	public final File getSaveFile() {
		return new File(getSaveFolder(), this.savePath + ".txt");
	}
	
	public final String toHTML(int numTabs) {//, String postPath) {
		return this.toString().replaceAll(Pattern.quote("[TABS]"), Matcher.quoteReplacement(new String(new char[numTabs]).replace("\0", "\t")));//.replaceAll(Pattern.quote("[POST_PATH]"), Matcher.quoteReplacement(postPath));
	}
	
	@Override
	public final String toString() {//<button onclick=\"document.getElementById('defaultFileName').value='" + fileName + "';\">Set default</button>
		final String s = "[TABS]";
		String response = s + "<hr>\r\n";//"<hr><form action=\"[POST_PATH]\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n";
		response += s + "<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n";
		response += s + "\t<tbody>\r\n";
		response += s + "\t\t<tr><td><b>File path:</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><textarea readonly=\"\" style=\"width: 340px;\" rows=\"1\" name=\"noresize\" title=\"This file's absolute path.\">" + StringUtil.encodeHTML(this.filePath.getValue()) + "</textarea></td></tr>\r\n";
		response += s + "\t\t<tr><td><b>Times accessed:&nbsp</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><textarea readonly=\"\" style=\"width: 340px;\" rows=\"1\" name=\"noresize\" title=\"The number of times that this file has been accessed by a client.\">" + this.timesAccessed.getValue().toString() + "</textarea></td></tr>\r\n";
		//response += s + "\t\t\t<tr><td><b>NAME</b></td><td>&nbsp;&nbsp;&nbsp;</td><td>VALUE</td></tr>\r\n";
		response += s + "\t</tbody>\r\n";
		response += s + "</table>\r\n";
		//response += s + "\t<input type=\"submit\" value=\"Submit Changes\">\r\n";
		return response;//return response + s + "</form>\r\n";
	}
	
	public final boolean delete() {
		instances.remove(this);
		return FileDeleteStrategy.FORCE.deleteQuietly(this.getSaveFile());
	}
	
	public static final void main(String[] args) {
		String path = "C:\\Java\\Brian_Entei\\BrianPerms\\javadoc\\index.html";
		FileData data = new FileData(StringUtil.makeStringFilesystemSafe(path));
		data.timesAccessed.setValue(Long.valueOf(45783L));
		data.filePath.setValue(path);
		System.out.println(data.toHTML(0));//, "/BrianPerms/Javadoc/index.html"));
	}
	
}
