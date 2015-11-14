package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.configuration.ConfigurationSection;
import com.gmail.br45entei.configuration.file.YamlConfiguration;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

/** Class used to load, save, and read restricted file data
 *
 * @author Brian_Entei */
public final class RestrictedFile implements DisposableUUIDData {
	protected static final ArrayList<RestrictedFile>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all of the RestrictedFiles currently
	 *         loaded and in use */
	public static final ArrayList<RestrictedFile> getInstances() {
		return new ArrayList<>(instances);
	}
	
	protected final UUID					uuid;
	protected File							file;
	
	protected String						authRealm	= JavaWebServer.DEFAULT_AUTHORIZATION_REALM;
	protected final HashMap<String, String>	authCreds	= new HashMap<>();
	
	public final Property<Boolean>			isHidden	= new Property<>("IsHidden", Boolean.FALSE).setDescription("Whether or not this directory is hidden(as in only people with the link can get to it, etc.)");
	
	protected final ArrayList<String>		allowedIPs	= new ArrayList<>();
	
	public final void setValuesFromHashMap(HashMap<String, String> values) {
		final ArrayList<String> allowedIPs = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			allowedIPs.add("");
		}
		final HashMap<String, String> authUsernames = new HashMap<>();
		final HashMap<String, String> authPasswords = new HashMap<>();
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equals("AuthRealm")) {
				if(value != null && !value.isEmpty()) {
					this.authRealm = value;
				}
			} else if(pname.equals(this.isHidden.getName())) {
				this.isHidden.setValue(Boolean.valueOf(value));
			} else if(pname.startsWith("AllowedIp_")) {
				Integer num = null;
				try {
					num = new Integer(pname.substring("AllowedIp_".length()));
				} catch(NumberFormatException ignored) {
					num = null;
				}
				if(num != null) {
					if(value != null && !value.isEmpty()) {
						if(num.intValue() < 20) {
							allowedIPs.set(num.intValue(), value);
						} else {
							allowedIPs.add(value);
						}
					}
				}
			} else if(pname.startsWith("AuthUsername_")) {
				authUsernames.put(pname, value);
			} else if(pname.startsWith("AuthPassword_")) {
				authPasswords.put(pname, value);
			} else {
				JavaWebServer.println("*** Unknown value sent: " + pname + ": " + value);
			}
		}
		this.allowedIPs.clear();
		for(String ip : allowedIPs) {
			this.allowedIPs.add(ip);
		}
		this.authCreds.clear();
		for(Entry<String, String> entry : authUsernames.entrySet()) {
			String username = entry.getValue();
			String password = authPasswords.get(entry.getKey().replace("AuthUsername_", "AuthPassword_"));
			if(username != null && !username.isEmpty() && password != null) {
				this.authCreds.put(username, password);
			}
		}
		this.saveToFile();
	}
	
	public static final RestrictedFile getRestrictedFileFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getRestrictedFileFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public static final RestrictedFile getRestrictedFileFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(RestrictedFile res : RestrictedFile.getInstances()) {
			if(res != null && res.uuid != null && res.uuid.toString().equals(uuid.toString())) {
				return res;
			}
		}
		return null;
	}
	
	public static final boolean isFileForbidden(File requestedFile) {
		RestrictedFile res = getSpecificRestrictedFile(requestedFile);
		if(res == null) {
			res = getRestrictedFile(requestedFile);
			if(res == null) {
				return false;
			}
		}
		return !res.getAllowedIPAddresses().contains("any");
	}
	
	public static final boolean isFileRestricted(File requestedFile) {
		return getRestrictedFile(requestedFile) != null;
	}
	
	public static final boolean isFileRestricted(File requestedFile, String clientIPAddress) {
		RestrictedFile restrictedFile = getSpecificRestrictedFile(requestedFile);
		restrictedFile = restrictedFile == null ? getRestrictedFile(requestedFile) : restrictedFile;
		return restrictedFile != null ? !restrictedFile.isIPAllowed(clientIPAddress) : false;
	}
	
	public static final RestrictedFile getSpecificRestrictedFile(File requestedFile) {
		if(requestedFile == null/* || !requestedFile.exists()*/) {
			return null;
		}
		RestrictedFile rtrn = null;
		for(RestrictedFile restrictedFile : RestrictedFile.getInstances()) {
			String requestedFilePath = FilenameUtils.normalize(requestedFile.getAbsolutePath());
			if(requestedFilePath == null) {
				requestedFilePath = requestedFile.getAbsolutePath();
			}
			requestedFilePath = requestedFilePath.endsWith("/") || requestedFilePath.endsWith("\\") ? requestedFilePath.substring(0, requestedFilePath.length() - 1) : requestedFilePath;
			String restrictedFilePath = (restrictedFile.getRestrictedFile() != null ? FilenameUtils.normalize(restrictedFile.getRestrictedFile().getAbsolutePath()) : null);
			restrictedFilePath = restrictedFilePath != null ? (restrictedFilePath.endsWith("/") || restrictedFilePath.endsWith("\\") ? restrictedFilePath.substring(0, restrictedFilePath.length() - 1) : restrictedFilePath) : restrictedFilePath;
			if(restrictedFilePath != null) {
				if(requestedFilePath.equalsIgnoreCase(restrictedFilePath)) {
					return restrictedFile;
				}
			}
		}
		return rtrn;
	}
	
	public static final RestrictedFile getRestrictedFile(File requestedFile) {
		if(requestedFile == null/* || !requestedFile.exists()*/) {
			return null;
		}
		RestrictedFile rtrn = null;
		for(RestrictedFile restrictedFile : RestrictedFile.getInstances()) {
			String requestedFilePath = FilenameUtils.normalize(requestedFile.getAbsolutePath());
			if(requestedFilePath == null) {
				requestedFilePath = requestedFile.getAbsolutePath();
			}
			requestedFilePath = requestedFilePath.endsWith("/") || requestedFilePath.endsWith("\\") ? requestedFilePath.substring(0, requestedFilePath.length() - 1) : requestedFilePath;
			String restrictedFilePath = (restrictedFile.getRestrictedFile() != null ? FilenameUtils.normalize(restrictedFile.getRestrictedFile().getAbsolutePath()) : null);
			restrictedFilePath = restrictedFilePath != null ? (restrictedFilePath.endsWith("/") || restrictedFilePath.endsWith("\\") ? restrictedFilePath.substring(0, restrictedFilePath.length() - 1) : restrictedFilePath) : restrictedFilePath;
			if(restrictedFilePath != null) {
				if(requestedFilePath.equalsIgnoreCase(restrictedFilePath)) {
					return restrictedFile;
				} else if(requestedFilePath.toLowerCase().startsWith(restrictedFilePath.toLowerCase())) {
					if(rtrn != null) {
						if(restrictedFile.file.getAbsolutePath().length() >= rtrn.file.getAbsolutePath().length()) {
							rtrn = restrictedFile;
						}
					} else {
						rtrn = restrictedFile;
					}
					continue;
				}
			}
		}
		return rtrn;
	}
	
	public static final RestrictedFile createNewRestrictedFile(final UUID uuid) {
		if(uuid == null) {
			throw new IllegalArgumentException("UUID cannot be null!");
		}
		return new RestrictedFile(uuid);
	}
	
	/** @param file The file
	 * @return The file associated restricted file data, or new data if
	 *         there wasn't any */
	public static RestrictedFile getOrCreateRestrictedFile(File file) {
		if(file == null) {
			throw new NullPointerException("File cannot be null!");
		}
		if(!file.exists()) {
			throw new IllegalArgumentException("The file \"" + file.getAbsolutePath() + "\" does not exist.");
		}
		//loadAllRestrictedFileDataFromFile();
		for(RestrictedFile restrictedFile : getInstances()) {
			if(restrictedFile != null) {
				if(restrictedFile.file == null) {
					try {
						restrictedFile.getSaveFile().delete();
					} catch(Throwable ignored) {
					}
					restrictedFile.dispose();
					continue;
				}
				if(FilenameUtils.equalsNormalized(restrictedFile.file.getAbsolutePath(), file.getAbsolutePath())) {
					return restrictedFile;
				}
			}
		}
		RestrictedFile restrictedFile = new RestrictedFile(file);
		restrictedFile.loadFromFile();
		return restrictedFile;
	}
	
	private RestrictedFile(UUID uuid) {
		this.uuid = uuid;
		instances.add(this);
	}
	
	private RestrictedFile(UUID uuid, File file) {
		this(uuid);
		this.file = file;
	}
	
	private RestrictedFile(File file) {
		this(UUID.randomUUID(), file);
		if(this.file == null) {
			throw new NullPointerException("File cannot be null!");
		}
		if(!this.file.exists()) {
			throw new IllegalArgumentException("File \"" + this.file.getAbsolutePath() + "\" does not exist on the file system!");
		}
	}
	
	/** @return This RestrictedFile's UUID */
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final RestrictedFile setRestrictedFile(File file, boolean ignoreFileExists) {
		if(!ignoreFileExists) {
			return setRestrictedFile(file);
		}
		if(file == null) {
			return this;
		}
		RestrictedFile check = RestrictedFile.getRestrictedFile(file);
		if(check == null) {
			this.file = file;
		}
		return this;
	}
	
	public final RestrictedFile setRestrictedFile(File file) {
		if(file == null || !file.exists()) {
			return this;
		}
		RestrictedFile check = RestrictedFile.getSpecificRestrictedFile(file);
		if(check == null) {
			this.file = file;
		}
		return this;
	}
	
	/** @return A list of all of the ip addresses that are allowed to read
	 *         from this restricted file */
	public final ArrayList<String> getAllowedIPAddresses() {
		return new ArrayList<>(this.allowedIPs);
	}
	
	/** @param ip The ip address to test
	 * @return Whether or not the given ip address is allowed to read from
	 *         this restricted file */
	public final boolean isIPAllowed(String ip) {
		if(getAllowedIPAddresses().contains("any")) {
			return true;
		}
		return getAllowedIPAddresses().contains(ip);
	}
	
	/** @param ip The ip address to add
	 * @return whether or not it was added successfully */
	public final boolean addIPAddress(String ip) {
		if(this.isIPAllowed(ip)) {
			return false;
		}
		return this.allowedIPs.add(ip);
	}
	
	/** @param ip The ip address to remove
	 * @return Whether or not it was removed successfully */
	public final boolean removeIPAddress(String ip) {
		if(!this.isIPAllowed(ip)) {
			return false;
		}
		return this.allowedIPs.remove(ip);
	}
	
	/** @return The file that this data represents */
	public final File getRestrictedFile() {
		return this.file;
	}
	
	/** @return The HTTP authentication realm for this file(if any) */
	public String getAuthorizationRealm() {
		return this.authRealm;
	}
	
	/** @param authRealm The HTTP authentication realm to set
	 * @return This restricted file data */
	public RestrictedFile setAuthorizationRealm(String authRealm) {
		if(authRealm == null) {
			throw new NullPointerException("'authRealm' cannot be null!");
		}
		this.authRealm = authRealm;
		return this;
	}
	
	//=============================================================
	
	/** @return This RestrictedFile's authorization credentials */
	public final HashMap<String, String> getAuthorizationCredentials() {
		return this.authCreds;
	}
	
	/** @param username The username sent by the client
	 * @return Whether or not the username matches one of the stored
	 *         usernames */
	public final boolean isUsernameValid(String username) {
		/*boolean rtrn = false;
		if(username == null) {
			return rtrn;
		}
		for(String key : this.authCreds.keySet()) {
			if(key.equalsIgnoreCase(username)) {
				rtrn = true;
				break;
			}
		}
		return rtrn;*/
		return this.getActualUsername(username) != null;
	}
	
	/** @param username The username sent by the client
	 * @return The stored username matching the one sent by the client, or
	 *         null if there wasn't one */
	public final String getActualUsername(String username) {
		String rtrn = null;
		if(username == null) {
			return rtrn;
		}
		for(String key : this.authCreds.keySet()) {
			if(key.equalsIgnoreCase(username)) {
				rtrn = key;
				break;
			}
		}
		return rtrn;
	}
	
	/** @param username The username sent by the client
	 * @param password The password sent by the client
	 * @return Whether or not the credentials given are correct(password is
	 *         case sensitive, username is not) */
	public final boolean areCredentialsValid(String username, String password) {
		if(this.isUsernameValid(username)) {
			String pass = this.authCreds.get(this.getActualUsername(username));
			return pass.equals(password);
		}
		return false;
	}
	
	/** @return Whether or not this RestrictedFile is password
	 *         protected(username and password must not be null or empty) */
	public final boolean isPasswordProtected() {
		for(String username : this.authCreds.keySet()) {
			String password = this.authCreds.get(username);
			if(password != null && !password.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	//=============================================================
	
	protected static final File getSaveFolder() {
		File saveFolder = new File(JavaWebServer.rootDir, "RestrictedFileData");
		if(!saveFolder.exists()) {
			saveFolder.mkdirs();
		}
		return saveFolder;
	}
	
	protected final File getSaveFile() throws IOException {
		File saveFile = new File(getSaveFolder(), this.uuid.toString() + ".yml");
		if(!saveFile.exists()) {
			saveFile.createNewFile();
		}
		return saveFile;
	}
	
	/** @return Whether or not all of this data's settings were saved to file
	 *         successfully */
	public final boolean saveToFile() {
		try {
			File file = this.getSaveFile();
			JavaWebServer.println("\tSaving restricted file data to file \"" + file.getName() + "\"...");
			YamlConfiguration config = new YamlConfiguration();
			config.set("file", this.file.getAbsolutePath());
			config.set("authRealm", this.authRealm);
			config.set("allowedIPs", this.allowedIPs);
			config.set("isHidden", this.isHidden.getValue());
			ConfigurationSection creds = config.createSection("authCredentials");
			for(Entry<String, String> entry : this.authCreds.entrySet()) {
				creds.set(entry.getKey(), entry.getValue());
			}
			config.save(file);
			JavaWebServer.println("\t\tRestricted file data successfully saved!");
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
		
		/*try {
			File file = this.getSaveFile();
			PrintWriter pr = new PrintWriter(file);
			pr.println("file=" + this.file.getAbsolutePath());
			pr.println("authRealm=" + this.authRealm);
			pr.println("authUsername=" + this.authUsername);
			pr.println("authPassword=" + this.authPassword);
			if(!this.allowedIPs.isEmpty()) {
				pr.println("allowedIPs:");
				for(String ip : this.getAllowedIPAddresses()) {
					pr.println("-" + ip);
				}
			}
			try {
				pr.close();
			} catch(Throwable ignored) {
			}
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}*/
	}
	
	/** @return Whether or not all of this data's settings was loaded from
	 *         file successfully */
	public final boolean loadFromFile() {
		try {
			File file = getSaveFile();
			YamlConfiguration config = new YamlConfiguration();
			JavaWebServer.println("\tLoading restricted file data from file \"" + file.getName() + "\"...");
			config.load(file);
			this.file = new File(config.getString("file", this.file != null ? this.file.getAbsolutePath() : ""));
			this.isHidden.setValue(Boolean.valueOf(config.getBoolean("isHidden", this.isHidden.getValue().booleanValue())));
			this.authRealm = config.getString("authRealm", this.authRealm);
			List<String> allowedIPs = config.getStringList("allowedIPs");
			if(allowedIPs != null) {
				this.allowedIPs.clear();
				for(String ip : allowedIPs) {
					this.allowedIPs.add(ip);
				}
			}
			ConfigurationSection creds = config.getConfigurationSection("authCredentials");
			if(creds != null) {
				this.authCreds.clear();
				for(String key : creds.getKeys(false)) {
					String value = creds.getString(key, null);
					if(value != null) {
						this.authCreds.put(key, value);
					}
				}
			} else {
				this.authCreds.clear();
				String username = config.getString("authUsername");
				String password = config.getString("authPassword");
				if(username != null && !username.isEmpty() && password != null) {
					this.authCreds.put(username, password);
				} else {
					this.authCreds.put(JavaWebServer.DEFAULT_AUTHORIZATION_USERNAME, JavaWebServer.DEFAULT_AUTHORIZATION_PASSWORD);
				}
			}
			if(this.file != null && this.file.exists()) {
				JavaWebServer.println("\t\tRestricted file data loaded!");
			}
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
		
		/*BufferedReader br = null;
		try {
			File file = this.getSaveFile();
			println("\tLoading restricted file data from file \"" + file.getAbsolutePath() + "\"...");
			br = new BufferedReader(new FileReader(file));
			this.allowedIPs.clear();
			while(br.ready()) {
				String line = br.readLine();
				String[] args = line.split("=");
				if(args.length == 2) {
					if(args[0].equalsIgnoreCase("file")) {
						this.file = new File(args[1]);
					} else if(args[0].equalsIgnoreCase("authRealm")) {
						this.authRealm = args[1];
					} else if(args[0].equalsIgnoreCase("authUsername")) {
						this.authUsername = args[1];
					} else if(args[0].equalsIgnoreCase("authPassword")) {
						this.authPassword = args[1];
					} else if(!args[0].equalsIgnoreCase("allowedIPs:")) {
						println("\tIgnoring malformed line: \"" + line + "\"");
					}
				} else {
					if(line.startsWith("-") && line.length() > 1) {
						this.addIPAddress(line.substring(1));
					} else if(!args[0].equalsIgnoreCase("allowedIPs:")) {
						println("\tIgnoring malformed line: \"" + line + "\"");
					}
				}
			}
			if(this.file != null && this.file.exists()) {
				println("\t\tRestricted file data loaded for file \"" + this.file.getAbsolutePath() + "\".");
			}
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(Throwable ignored) {
				}
			}
		}*/
	}
	
	/** @return An arraylist containing all of the restricted file data
	 *         loaded from file */
	public static final ArrayList<RestrictedFile> loadAllRestrictedFileDataFromFile() {
		final ArrayList<RestrictedFile> rtrn = new ArrayList<>();
		File saveFolder = getSaveFolder();
		for(String fileName : saveFolder.list()) {
			File file = new File(saveFolder, fileName);
			if(file.exists() && FilenameUtils.getExtension(file.getAbsolutePath()).equals("yml")) {
				String uuidStr = FilenameUtils.getBaseName(file.getAbsolutePath());
				if(StringUtils.isStrUUID(uuidStr)) {
					RestrictedFile check = null;
					UUID uuid = UUID.fromString(uuidStr);
					for(RestrictedFile curRestrictedFile : getInstances()) {
						if(curRestrictedFile.uuid.toString().equals(uuid.toString())) {
							check = curRestrictedFile;
							break;
						}
					}
					if(check == null) {
						RestrictedFile restrictedFile = new RestrictedFile(UUID.fromString(uuidStr));
						if(restrictedFile.loadFromFile()) {
							rtrn.add(restrictedFile);
						}
					} else {
						check.loadFromFile();
					}
				}
			}
		}
		return rtrn;
	}
	
	/** Saves all of the currently loaded restricted file data to file */
	public static final void saveAllRestrictedFileDataToFile() {
		if(!instances.isEmpty()) {
			JavaWebServer.println("Saving all restricted file data...");
			for(RestrictedFile res : getInstances()) {
				res.saveToFile();
			}
			JavaWebServer.println("Restricted file data has been saved.");
		}
	}
	
	public final void remove() {
		instances.remove(this);
	}
	
	/** Calls {@link #dispose()} and then attempts to delete any associated
	 * save
	 * files from disk. */
	@Override
	public final void delete() {
		this.dispose();
		try {
			this.getSaveFile().delete();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Disposes of this restricted file data and removes it from memory,
	 * thereby rendering it unusable. */
	@Override
	public final void dispose() {
		this.remove();
		this.allowedIPs.clear();
		this.authCreds.clear();
		this.authRealm = null;
		this.file = null;
	}
	
	/** @param file The file to check
	 * @return Whether or not the given file is hidden */
	public static final boolean isHidden(File file) {
		RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(file);
		final boolean isHiddenByName = file.getName().startsWith("$");
		if(res != null) {
			return res.isHidden.getValue().booleanValue() || isHiddenByName;
		}
		return isHiddenByName;
	}
	
}
