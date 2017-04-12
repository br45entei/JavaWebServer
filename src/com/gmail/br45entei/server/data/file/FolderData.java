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
public final class FolderData {
	
	protected static final ConcurrentLinkedDeque<FolderData> dataToSaveLater = new ConcurrentLinkedDeque<>();
	
	@SuppressWarnings("unused")
	private static final void saveLater(FolderData data) {
		dataToSaveLater.add(data);
	}
	
	private static final Thread saveLaterThread = new Thread("FolderDataSaveLaterThread") {
		@Override
		public final void run() {
			while(JavaWebServer.serverActive()) {
				if(!dataToSaveLater.isEmpty()) {
					FolderData data = dataToSaveLater.pop();
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
	
	private static final ConcurrentLinkedDeque<FolderData> instances = new ConcurrentLinkedDeque<>();
	
	public static final FolderData getByPath(String path) {
		for(FolderData data : instances) {
			if(data.savePath.equals(path)) {
				return data;
			}
		}
		return null;
	}
	
	private final String savePath;
	
	public final Property<String> folderPath = new Property<>("folderPath", "");
	public final Property<String> defaultFileName = new Property<>("defaultFileName", ""); //JavaWebServer.DEFAULT_FILE_NAME);
	public final Property<Long> timesAccessed = new Property<>("timesAccessed", Long.valueOf(0L));
	
	public final HashMap<String, Property<?>> getPropertiesAsHashMap() {
		final HashMap<String, Property<?>> rtrn = new HashMap<>();
		rtrn.put(this.defaultFileName.getName(), this.defaultFileName);
		return rtrn;
	}
	
	public final FolderData setValuesFromHashMap(HashMap<String, String> values) {
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equalsIgnoreCase(this.defaultFileName.getName())) {
				this.defaultFileName.setValue(value);
			}
		}
		//this.lastEdited.setValue(Long.valueOf(System.currentTimeMillis()));
		this.saveToFile();
		return this;
	}
	
	public static final String getSavePathFor(File folder) {
		if(folder == null || !folder.isDirectory()) {
			return null;
		}
		return StringUtil.makeStringFilesystemSafe(FilenameUtils.normalize(folder.getAbsolutePath()));
	}
	
	public static final FolderData getFolderData(File folder, boolean createIfNotExist) {
		if(folder == null || !folder.isDirectory()) {
			return null;
		}
		String path = getSavePathFor(folder);
		loadFolderData(false, false);
		FolderData data = getByPath(path);
		if(data == null && createIfNotExist) {
			data = new FolderData(path);
			data.defaultFileName.setValue("");
			data.folderPath.setValue(FilenameUtils.normalize(folder.getAbsolutePath()));
			data.saveToFile();
			JavaWebServer.println("\tCreated new folder properties record for: " + data.savePath);
			instances.add(data);
		}
		return data;
	}
	
	public final FolderData incrementTimesAccessed() {
		this.timesAccessed.setValue(Long.valueOf(this.timesAccessed.getValue().longValue() + 1L));
		this.saveToFile();
		return this;
	}
	
	public final File getDefaultFile() {
		String fileName = this.defaultFileName.getValue();
		if(fileName != null && !fileName.isEmpty() && StringUtil.isFileSystemSafe(fileName)) {
			File folder = new File(this.folderPath.getValue());
			if(folder.isDirectory()) {
				File file = new File(folder, fileName);
				return file.exists() ? file : null;
			}
		}
		return null;
	}
	
	public static final void loadFolderData(boolean clear, boolean reload) {
		if(clear) {
			instances.clear();
			reload = true;
		}
		File folder = getSaveFolder();
		for(String fileName : folder.list()) {
			File file = new File(folder, fileName);
			if(file.isFile() && fileName.endsWith(".txt")) {
				String savePath = fileName.substring(0, fileName.length() - 4);
				FolderData check = getByPath(savePath);
				if(check == null || reload) {
					if(check != null && reload) {
						instances.remove(check);
					} else if(check != null && !reload) {
						continue;
					}
					FolderData data = new FolderData(savePath);
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
	
	public static final void saveFolderData() {
		for(FolderData data : instances) {
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
				if(pname.equalsIgnoreCase("folderPath")) {
					this.folderPath.setValue(value);
				} else if(pname.equalsIgnoreCase("defaultFileName")) {
					this.defaultFileName.setValue(value);
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
			dataToSaveLater.add(this);
		} catch(Throwable e) {
			PrintUtil.printThrowable(e);
			PrintUtil.printErrToConsole();
		}
	}
	
	protected final void _saveToFile() throws IOException {
		try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getSaveFile()), StandardCharsets.UTF_8), true)) {
			pr.println("folderPath=" + this.folderPath.getValue());
			pr.println("defaultFileName=" + (this.defaultFileName.getValue() == null ? "" : this.defaultFileName.getValue()));
			pr.println("timesAccessed=" + this.timesAccessed.getValue().toString());
			pr.flush();
		}
	}
	
	public static final File getSaveFolder() {
		File folder = new File(JavaWebServer.rootDir + File.separator + "FolderData" + File.separator);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	private FolderData(String savePath) {
		this.savePath = savePath;
	}
	
	public final File getSaveFile() {
		return new File(getSaveFolder(), this.savePath + ".txt");
	}
	
	public final String toHTML(int numTabs, String postPath) {
		return this.toString().replaceAll(Pattern.quote("[TABS]"), Matcher.quoteReplacement(new String(new char[numTabs]).replace("\0", "\t"))).replaceAll(Pattern.quote("[POST_PATH]"), Matcher.quoteReplacement(postPath));
	}
	
	@Override
	public final String toString() {//<button onclick=\"document.getElementById('defaultFileName').value='" + fileName + "';\">Set default</button>
		final String resetButton = "<button onclick=\"" + JavaWebServer.checkVisibleJavascript + "if(document.getElementById('folderProperties') !== null && document.getElementById('defaultFileName') !== null) {\r\n\tdocument.getElementById('defaultFileName').value='';\r\n\tif(!checkVisible(document.getElementById('folderProperties'))) {\r\n\t\tdocument.getElementById('folderProperties').scrollIntoView();\r\n\t}\r\n} else {\r\n\talert('Could not find folder properties form!');\r\n}\">Reset</button>";
		//final String resetButton = "<button onclick=\"" + JavaWebServer.checkVisibleJavascript + "if(document.getElementById('folderProperties') !== null && document.getElementById('defaultFileName') !== null) {document.getElementById('defaultFileName').value=''; if(!checkVisible(document.getElementById('folderProperties'))) {document.getElementById('folderProperties').scrollIntoView();}} else {alert('Could not find folder properties form!');}\">Reset</button>";
		final String s = "[TABS]";
		String response = s + "<hr><form action=\"[POST_PATH]\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n";
		response += s + "\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n";
		response += s + "\t\t<tbody>\r\n";
		response += s + "\t\t\t<tr><td><b>Folder path:</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><textarea readonly=\"\" style=\"width: 340px;\" rows=\"1\" name=\"noresize\" title=\"This folder's absolute path.\">" + StringUtil.encodeHTML(this.folderPath.getValue()) + "</textarea></td></tr>\r\n";
		response += s + "\t\t\t<tr><td><b>Times accessed:&nbsp</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><textarea readonly=\"\" style=\"width: 340px;\" rows=\"1\" name=\"noresize\" title=\"The number of times that this directory has been accessed by a client.\">" + this.timesAccessed.getValue().toString() + "</textarea></td></tr>\r\n";
		response += s + "\t\t\t<tr><td><b>Default file:&nbsp;</b></td><td>&nbsp;&nbsp;&nbsp;</td><td><input type=\"text\" id=\"defaultFileName\" name=\"defaultFileName\" value=\"" + StringUtil.encodeHTML(this.defaultFileName.getValue()) + "\" style=\"width: 340px;\" title=\"The default file that will be loaded when this folder is requested by a client.\" readonly=\"\"></td></tr>\r\n";
		//response += s + "\t\t\t<tr><td><b>NAME</b></td><td>&nbsp;&nbsp;&nbsp;</td><td>VALUE</td></tr>\r\n";
		response += s + "\t\t</tbody>\r\n";
		response += s + "\t</table>\r\n";
		response += s + "\t<input type=\"submit\" value=\"Submit Changes\">\r\n";
		return response + s + "</form>" + resetButton + "<hr>\r\n";
	}
	
	public final boolean delete() {
		instances.remove(this);
		return FileDeleteStrategy.FORCE.deleteQuietly(this.getSaveFile());
	}
	
	public static final void main(String[] args) {
		String path = "C:\\Java\\Brian_Entei\\BrianPerms\\javadoc\\";
		FolderData data = new FolderData(StringUtil.makeStringFilesystemSafe(path));
		data.timesAccessed.setValue(Long.valueOf(45783L));
		data.folderPath.setValue(path);
		System.out.println(data.toHTML(0, "/BrianPerms/Javadoc"));
	}
	
}
