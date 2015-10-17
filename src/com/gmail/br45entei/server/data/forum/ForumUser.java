package com.gmail.br45entei.server.data.forum;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.configuration.file.YamlConfiguration;
import com.gmail.br45entei.server.data.DisposableUUIDData;
import com.gmail.br45entei.server.data.HTMLParsableData;
import com.gmail.br45entei.server.data.SavableData;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.PrintUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/** @author Brian_Entei */
public class ForumUser implements DisposableUUIDData, SavableData, HTMLParsableData {
	protected static final ArrayList<ForumUser>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all forum users */
	public static final ArrayList<ForumUser> getInstances() {
		return new ArrayList<>(instances);
	}
	
	protected final UUID	uuid;
	protected String		username;
	private String			password;
	
	protected File			avatar;
	protected String		signature;
	
	protected long			lastActivityTime;
	protected long			lastLogin;
	
	@Override
	public ForumUser setValuesFromHashMap(HashMap<String, String> values) {
		if(values == null || values.isEmpty()) {
			return this;
		}
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equals("")) {
				
			} else if(pname.equals("")) {
				
			}
		}
		this.saveToFile();
		return this;
	}
	
	protected ForumUser(UUID uuid) {
		if(uuid == null) {
			throw new IllegalArgumentException("UUID for new ForumUser cannot be null!");
		}
		this.uuid = uuid;
		instances.add(this);
	}
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	@Override
	public final String toString() {
		return this.username;
	}
	
	//=====================================================================
	
	public static final File getStaticSaveFolder() {
		File saveFolder = new File(ForumData.getStaticSaveFolder(), "UserData");
		if(!saveFolder.exists()) {
			saveFolder.mkdirs();
		}
		return saveFolder;
	}
	
	@Override
	public final File getSaveFolder() {
		File saveFolder = new File(getStaticSaveFolder(), this.uuid.toString());
		if(!saveFolder.exists()) {
			saveFolder.mkdirs();
		}
		return saveFolder;
	}
	
	@Override
	public final File getSaveFile() throws IOException {
		File saveFile = new File(this.getSaveFolder(), "forumUser.yml");
		if(!saveFile.exists()) {
			saveFile.createNewFile();
		}
		return saveFile;
	}
	
	/** @return Whether or not this forum user's settings were saved to file
	 *         successfully */
	@Override
	public final boolean saveToFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("username", this.username);
			config.set("password", new String(Base64.getEncoder().encode(this.password.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
			config.save(this.getSaveFile());
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** @return Whether or not this forum user's settings were loaded from
	 *         file successfully */
	@Override
	public final boolean loadFromFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.load(this.getSaveFile());
			this.username = config.getString("username", "Username");
			try {
				this.password = new String(Base64.getDecoder().decode(config.getString("password", "").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			} catch(Throwable e) {
				PrintUtil.printThrowable(e);
			}
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	/** @return An arraylist containing all of the forum users that were loaded
	 *         from file */
	protected static final ArrayList<ForumUser> loadAllForumUsersFromFile() {
		final ArrayList<ForumUser> rtrn = new ArrayList<>();
		File saveFolder = getStaticSaveFolder();
		for(String folderName : saveFolder.list()) {
			File folder = new File(saveFolder, folderName);
			if(folder.exists() && folder.isDirectory()) {
				File file = new File(folder, "forumData.yml");
				if(file.exists()) {
					String uuidStr = folderName;//FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(folderName + File.separatorChar));
					if(Functions.isStrUUID(uuidStr)) {
						ForumUser check = null;
						UUID uuid = UUID.fromString(uuidStr);
						for(ForumUser curUser : getInstances()) {
							if(curUser.uuid.toString().equals(uuid.toString())) {
								check = curUser;
								break;
							}
						}
						if(check == null) {
							ForumUser user = new ForumUser(uuid);
							if(user.loadFromFile()) {
								rtrn.add(user);
							}
						} else {
							//check.loadFromFile();
						}
					}
				}
			}
		}
		return rtrn;
	}
	
	/** Saves all of the forum users */
	protected static final void saveAllForumUsersToFile() {
		if(!instances.isEmpty()) {
			JavaWebServer.println("Saving all forum users...");
			for(ForumUser forum : getInstances()) {
				forum.saveToFile();
			}
			JavaWebServer.println("All forum users have been saved.");
		}
	}
	
	@Override
	public final void delete() {
		this.dispose();
		this.getSaveFolder().delete();
	}
	
	@Override
	public final void dispose() {
		this.username = null;
		this.password = null;
		this.avatar = null;
		this.signature = null;
		this.lastActivityTime = 0L;
		this.lastLogin = 0L;
		instances.remove(this);
	}
	
	@Override
	public String toHTML() {
		String rtrn = "\r\n";
		rtrn += "\r\n";
		rtrn += "\r\n";
		return rtrn;
	}
	
}
