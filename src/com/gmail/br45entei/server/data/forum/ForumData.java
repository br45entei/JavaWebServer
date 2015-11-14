package com.gmail.br45entei.server.data.forum;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.configuration.file.YamlConfiguration;
import com.gmail.br45entei.server.data.DisposableUUIDData;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.HTMLParsableData;
import com.gmail.br45entei.server.data.SavableData;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

/** @author Brian_Entei */
public class ForumData implements DisposableUUIDData, SavableData, HTMLParsableData {//(The entire forum)
	protected static final ArrayList<ForumData>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all forum data currently in use */
	public static final ArrayList<ForumData> getInstances() {
		return new ArrayList<>(instances);
	}
	
	private final UUID								uuid;
	private String									name;
	protected long									dateCreated	= System.currentTimeMillis();
	
	protected String								headerImage;
	protected String								pageBackground;
	
	private final ArrayList<String>					domains		= new ArrayList<>();
	
	//====================
	
	protected final HashMap<Integer, ForumBoard>	boards		= new HashMap<>();
	protected final HashMap<Integer, ForumUser>		users		= new HashMap<>();
	
	//====================
	
	public static final ForumData getForumDataFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getForumDataFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public static final ForumData getForumDataFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(ForumData forum : ForumData.getInstances()) {
			if(forum != null && forum.uuid != null && forum.uuid.toString().equals(uuid.toString())) {
				return forum;
			}
		}
		return null;
	}
	
	public static final ForumData getForumDataFromName(String forumName) {
		for(ForumData forum : getInstances()) {
			if(forum.name.equalsIgnoreCase(forumName.replace("%20", " "))) {
				return forum;
			}
		}
		return null;
	}
	
	//============================================
	
	public final ForumBoard getForumBoardFromName(String name) {
		for(ForumBoard board : this.getBoards()) {
			if(board.getName().equalsIgnoreCase(name.replace("%20", " "))) {
				return board;
			}
		}
		return null;
	}
	
	public final ForumBoard getForumBoardFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getForumBoardFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public final ForumBoard getForumBoardFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(ForumBoard board : getBoards()) {
			if(board.uuid.toString().equals(uuid.toString())) {
				return board;
			}
		}
		return null;
	}
	
	//============================================
	
	/** @param forumName The forum whose data will be returned
	 * @return The associated data for the given domain */
	public static final ForumData getOrCreateForumData(String forumName) {
		if(forumName == null || forumName.isEmpty()) {
			throw new NullPointerException("Forum name cannot be null!");
		}
		//loadAllForumDataDataFromFile();
		ForumData forum = getForumDataFromName(forumName);
		if(forum == null) {
			forum = new ForumData(UUID.randomUUID());
			forum.loadFromFile();
			forum.name = forumName;//XXX
			forum.saveToFile();
		}
		return forum;
	}
	
	public static final String replaceUnsafeCharsInStr(String str) {
		if(str == null) {
			return str;
		}
		return str.replace(" ", "_").replace("%", "-").replace("/", "-").replace("?", "-").replace("&", "-").replace("#", "-");
	}
	
	public static final ForumData getForumDataFromRequestedFilePath(String requestedFilePath) {
		if(requestedFilePath == null || requestedFilePath.isEmpty()) {
			return null;
		}
		if(requestedFilePath.startsWith("/")) {
			requestedFilePath = requestedFilePath.substring(1);
			return getForumDataFromRequestedFilePath(requestedFilePath);
		}
		if(requestedFilePath.contains("/")) {
			int index = requestedFilePath.indexOf("/");
			return getForumDataFromRequestedFilePath(requestedFilePath.substring(0, index));
		}
		if(requestedFilePath.contains("?")) {
			int index = requestedFilePath.indexOf("?");
			return getForumDataFromRequestedFilePath(requestedFilePath.substring(0, index));
		}
		return getForumDataFromName(requestedFilePath);
	}
	
	public static final ForumBoard getForumBoardFromRequestedFilePath(String requestedFilePath) {
		if(requestedFilePath == null || requestedFilePath.isEmpty()) {
			return null;
		}
		ForumData forum = getForumDataFromRequestedFilePath(requestedFilePath);
		if(forum != null) {
			if(requestedFilePath.startsWith("/")) {
				requestedFilePath = requestedFilePath.substring(1);
			}
			if(requestedFilePath.toLowerCase().startsWith(forum.getName().toLowerCase()) && requestedFilePath.length() > forum.getName().length() + 1) {
				requestedFilePath = requestedFilePath.substring(forum.getName().length() + 1, requestedFilePath.length());
			}
			if(requestedFilePath.toLowerCase().startsWith("forum/") && requestedFilePath.length() > 6) {
				requestedFilePath = requestedFilePath.substring(6, requestedFilePath.length());
			}
			if(requestedFilePath.contains("/")) {
				requestedFilePath = requestedFilePath.substring(0, requestedFilePath.indexOf("/"));
			}
			if(requestedFilePath.contains("?")) {
				requestedFilePath = requestedFilePath.substring(0, requestedFilePath.indexOf("?"));
			}
			return forum.getForumBoardFromName(requestedFilePath);
		}
		return null;
	}
	
	public static final ForumTopic getForumTopicFromRequestedFilePath(String requestedFilePath) {
		if(requestedFilePath == null || requestedFilePath.isEmpty()) {
			return null;
		}
		final ForumBoard board = getForumBoardFromRequestedFilePath(requestedFilePath);
		if(board != null) {
			final ForumData forum = board.getParentForum();
			if(requestedFilePath.startsWith("/")) {
				requestedFilePath = requestedFilePath.substring(1);
			}
			if(requestedFilePath.toLowerCase().startsWith(forum.getName().toLowerCase()) && requestedFilePath.length() > forum.getName().length() + 1) {
				requestedFilePath = requestedFilePath.substring(forum.getName().length() + 1, requestedFilePath.length());
			}
			if(requestedFilePath.toLowerCase().startsWith("forum/") && requestedFilePath.length() > 6) {
				requestedFilePath = requestedFilePath.substring(6, requestedFilePath.length());
			}
			if(requestedFilePath.toLowerCase().startsWith(board.getName().toLowerCase()) && requestedFilePath.length() > board.getName().length() + 1) {
				requestedFilePath = requestedFilePath.substring(board.getName().length() + 1, requestedFilePath.length());
			}
			if(requestedFilePath.toLowerCase().startsWith("topic/") && requestedFilePath.length() > 6) {
				requestedFilePath = requestedFilePath.substring(6, requestedFilePath.length());
			}
			if(requestedFilePath.contains("/")) {
				requestedFilePath = requestedFilePath.substring(0, requestedFilePath.indexOf("/"));
			}
			if(requestedFilePath.contains("?")) {
				requestedFilePath = requestedFilePath.substring(0, requestedFilePath.indexOf("?"));
			}
			return board.getForumTopicFromName(requestedFilePath);
		}
		return null;
	}
	
	@Override
	public final ForumData setValuesFromHashMap(HashMap<String, String> values) {
		if(values == null || values.isEmpty()) {
			return this;
		}
		final ArrayList<String> domains = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			domains.add("");
		}
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equalsIgnoreCase("name")) {
				if(value != null && !value.isEmpty()) {
					this.name = replaceUnsafeCharsInStr(value);
				}
			} else if(pname.startsWith("AllowedDomain_")) {
				Integer num = null;
				try {
					num = new Integer(pname.substring("AllowedDomain_".length()));
				} catch(NumberFormatException ignored) {
					num = null;
				}
				if(num != null) {
					if(value != null && !value.isEmpty()) {
						if(num.intValue() < 20) {
							domains.set(num.intValue(), value);
						} else {
							domains.add(value);
						}
					}
				}
			}
		}
		this.domains.clear();
		for(String domain : domains) {
			this.domains.add(domain);
		}
		this.saveToFile();
		return this;
	}
	
	//=================================================================
	
	private ForumData(UUID uuid) {
		this.uuid = uuid;
		instances.add(this);
	}
	
	//=================================================================
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final ForumData setName(String forumName) {
		this.name = forumName;
		return this;
	}
	
	public final File getRootFolder() {
		return this.getSaveFolder();
	}
	
	public final long getDateCreated() {
		return this.dateCreated;
	}
	
	public final ArrayList<ForumBoard> getBoards() {
		final ArrayList<ForumBoard> rtrn = new ArrayList<>();
		for(Entry<Integer, ForumBoard> entry : new HashMap<>(this.boards).entrySet()) {
			rtrn.add(entry.getValue());
		}
		return rtrn;
	}
	
	public final ArrayList<String> getDomains() {
		return new ArrayList<>(this.domains);
	}
	
	public final ArrayList<ForumUser> getUsers() {
		final ArrayList<ForumUser> rtrn = new ArrayList<>();
		for(Entry<Integer, ForumUser> entry : new HashMap<>(this.users).entrySet()) {
			if(entry.getValue() != null) {
				rtrn.add(entry.getValue());
			}
		}
		return rtrn;
	}
	
	public final ForumUser getUserFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty() || !StringUtils.isStrUUID(uuid)) {
			return null;
		}
		return getUserFromUUID(UUID.fromString(uuid));
	}
	
	public final ForumUser getUserFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(ForumUser user : this.getUsers()) {
			if(user.uuid.toString().equals(uuid.toString())) {
				return user;
			}
		}
		return null;
	}
	
	public final String getHeaderImage() {
		return this.headerImage.startsWith("/") ? "/" + this.getName() + this.headerImage : this.headerImage;
	}
	
	protected final ForumData setHeaderImage(String headerImage) {
		this.headerImage = headerImage;
		return this;
	}
	
	public final String getPageBackground() {
		return this.pageBackground.startsWith("/") ? "/" + this.getName() + this.pageBackground : this.pageBackground;
	}
	
	protected final ForumData setPageBackground(String pageBackground) {
		this.pageBackground = pageBackground;
		return this;
	}
	
	//=================================================================
	
	public static final File getStaticSaveFolder() {
		File saveFolder = new File(JavaWebServer.rootDir, "ForumData");
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
		File saveFile = new File(this.getSaveFolder(), "forumData.yml");
		if(!saveFile.exists()) {
			saveFile.createNewFile();
		}
		return saveFile;
	}
	
	/** @return Whether or not this domain data's settings were saved to file
	 *         successfully */
	@Override
	public final boolean saveToFile() {
		try {
			JavaWebServer.println("\tSaving forum data \"" + this.name + "\" to file...");
			YamlConfiguration config = new YamlConfiguration();
			config.set("name", this.name);
			config.set("domains", this.domains);
			config.set("dateCreated", Long.valueOf(this.dateCreated));
			config.set("headerImage", this.headerImage);
			config.set("pageBackground", this.pageBackground);
			config.save(this.getSaveFile());
			JavaWebServer.println("\t\tForum data successfully saved!");
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** @return Whether or not this domain data's settings were loaded from
	 *         file successfully */
	@Override
	public final boolean loadFromFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			File file = this.getSaveFile();
			JavaWebServer.println("\tLoading forum data from file \"" + file.getAbsolutePath() + "\"...");
			config.load(file);
			this.name = config.getString("name", "Forum_Name");
			List<String> domains = config.getStringList("domains");
			for(String domain : domains) {
				this.domains.add(domain);
			}
			this.dateCreated = config.getLong("dateCreated", this.dateCreated);
			this.headerImage = config.getString("headerImage", "/headerImage.png");
			this.pageBackground = config.getString("pageBackground", "/pageBackground.png");
			int boardsLoaded = this.loadBoardsFromFile().size();
			if(this.name != null && !this.name.isEmpty()) {
				JavaWebServer.println("\t\tForum data \"" + this.name + "\" loaded with " + boardsLoaded + " forum board" + (boardsLoaded == 1 ? "" : "s") + ".");
			}
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	/** @return An arraylist containing all of the domain data that was
	 *         loaded from file */
	public static final ArrayList<ForumData> loadAllForumDataFromFile() {
		final ArrayList<ForumData> rtrn = new ArrayList<>();
		File saveFolder = getStaticSaveFolder();
		for(String folderName : saveFolder.list()) {
			File folder = new File(saveFolder, folderName);
			if(folder.exists() && folder.isDirectory()) {
				File file = new File(folder, "forumData.yml");
				if(file.exists()) {
					String uuidStr = folderName;
					if(Functions.isStrUUID(uuidStr)) {
						ForumData check = null;
						UUID uuid = UUID.fromString(uuidStr);
						for(ForumData curForum : getInstances()) {
							if(curForum.uuid.toString().equals(uuid.toString())) {
								check = curForum;
								break;
							}
						}
						if(check == null) {
							ForumData forum = new ForumData(uuid);
							if(forum.loadFromFile()) {
								rtrn.add(forum);
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
	
	/** Saves all of the domain data currently in use */
	public static final void saveAllForumDataToFile() {
		if(!instances.isEmpty()) {
			JavaWebServer.println("Saving all forum data...");
			for(ForumData forum : getInstances()) {
				forum.saveToFile();
			}
			JavaWebServer.println("All forum data has been saved.");
		}
	}
	
	//============================================
	
	public final boolean isDomainAllowed(DomainDirectory domain) {
		if(domain == null) {
			return false;
		}
		return this.isDomainAllowed(domain.getDomain());
	}
	
	public final boolean isDomainAllowed(String domain) {
		for(String curDomain : this.domains) {
			if(curDomain.equalsIgnoreCase(domain)) {
				return true;
			}
		}
		return false;
	}
	
	public final boolean checkIfBoardExists(ForumBoard board) {
		if(board == null || this.boards.isEmpty()) {
			return false;
		}
		for(Entry<Integer, ForumBoard> entry : new HashMap<>(this.boards).entrySet()) {
			if(entry.getValue().name.equals(board.name)) {
				return true;
			}
		}
		return false;
	}
	
	public final Integer getIntegerOfBoard(ForumBoard board) {
		if(checkIfBoardExists(board)) {
			for(Entry<Integer, ForumBoard> entry : new HashMap<>(this.boards).entrySet()) {
				if(entry.getValue().name.equals(board.name)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	public final void addBoard(ForumBoard board) {
		if(!checkIfBoardExists(board)) {
			Integer key = Integer.valueOf(this.boards.size());
			this.boards.put(key, board);
		}
	}
	
	public final void removeBoard(ForumBoard board) {
		if(checkIfBoardExists(board)) {
			Integer key = getIntegerOfBoard(board);
			if(key != null) {
				HashMap<Integer, ForumBoard> temp = new HashMap<>(this.boards);
				this.boards.clear();
				for(Entry<Integer, ForumBoard> entry : temp.entrySet()) {
					int k = entry.getKey().intValue();
					if(k > key.intValue()) {
						this.boards.put(Integer.valueOf(k - 1), entry.getValue());
					} else if(k != key.intValue()) {
						this.boards.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				throw new Error("Uhh, wat? A forum board exists, but yet doesn't?");
			}
			board.delete();
		}
	}
	
	public final void deleteBoards() {
		for(ForumBoard board : this.getBoards()) {
			board.delete();
		}
		this.boards.clear();
	}
	
	public final File getBoardFolder() {
		return new File(this.getSaveFolder(), "ForumBoards");
	}
	
	public final ArrayList<ForumBoard> loadBoardsFromFile() {
		final ArrayList<ForumBoard> rtrn = new ArrayList<>();
		File boardFolder = this.getBoardFolder();
		if(!boardFolder.exists()) {
			boardFolder.mkdirs();
			ForumBoard board = new ForumBoard(UUID.randomUUID(), this);
			board.name = "General Discussion";
			board.saveToFile();
			this.addBoard(board);
			rtrn.add(board);
			return rtrn;
		}
		for(String folderName : boardFolder.list()) {
			File curBoardFolder = new File(boardFolder, folderName);
			if(curBoardFolder.exists() && curBoardFolder.isDirectory()) {
				File boardFile = new File(curBoardFolder, "board.yml");
				if(boardFile.exists() && boardFile.isFile()) {
					if(StringUtils.isStrUUID(folderName)) {
						UUID boardUUID = UUID.fromString(folderName);
						ForumBoard board = new ForumBoard(boardUUID, this);
						board.loadFromFile();
						this.addBoard(board);
						rtrn.add(board);
					}
				}
			}
		}
		return rtrn;
	}
	
	public final ArrayList<ForumBoard> saveBoardsToFile() {
		final ArrayList<ForumBoard> rtrn = new ArrayList<>();
		File boardFolder = this.getBoardFolder();
		if(!boardFolder.exists()) {
			boardFolder.mkdirs();
		}
		for(ForumBoard board : this.getBoards()) {
			board.saveToFile();
			rtrn.add(board);
		}
		return rtrn;
	}
	
	//============================================
	
	/** Calls {@link #dispose()} and then attempts to delete any associated save
	 * files from disk. */
	@Override
	public final void delete() {
		this.deleteBoards();
		this.dispose();
		this.getSaveFolder().delete();
	}
	
	/** Disposes of this domain data and removes it from memory, thereby
	 * rendering it useless */
	@Override
	public final void dispose() {
		instances.remove(this);
		this.name = null;
		this.dateCreated = 0L;
		this.headerImage = null;
		this.pageBackground = null;
		this.domains.clear();
	}
	
	@Override
	public String toHTML() {//TODO
		String rtrn = "<!DOCTYPE html>\r\n";
		rtrn += "<html>\r\n<head><title>" + StringEscapeUtils.escapeHtml4(this.getName()) + " - " + JavaWebServer.SERVER_NAME + "</title></head>\r\n";
		rtrn += "<body background=\"" + this.getPageBackground() + "\">\r\n";
		rtrn += "<div align=\"center\"><img src=\"" + this.getHeaderImage() + "\"></div><br>\r\n";
		for(ForumBoard board : this.getBoards()) {
			final String boardName = StringEscapeUtils.escapeHtml4(board.getName());
			rtrn += "<div>\r\n<h1><a href=\"/" + this.getName() + "/forum/" + boardName + "/\">" + boardName + "</a></h1><hr>\r\n";
			rtrn += "<p>" + StringEscapeUtils.escapeHtml4(board.getDescription()) + "</p>\r\n</div><br>\r\n";
		}
		rtrn += "<br><string>Created on: " + StringUtils.getCacheTime(this.dateCreated) + "</string>\r\n";
		rtrn += "</body>\r\n</html>";
		return rtrn;
	}
	
}
