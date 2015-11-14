package com.gmail.br45entei.server.data.forum;

import com.gmail.br45entei.configuration.file.YamlConfiguration;
import com.gmail.br45entei.server.data.DisposableUUIDData;
import com.gmail.br45entei.server.data.HTMLParsableData;
import com.gmail.br45entei.server.data.SavableData;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/** @author Brian_Entei */
public class ForumTopic implements DisposableUUIDData, SavableData, HTMLParsableData {//(A forum thread)
	protected static final ArrayList<ForumTopic>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all forum topics currently in use */
	public static final ArrayList<ForumTopic> getInstances() {
		return new ArrayList<>(instances);
	}
	
	protected final UUID							uuid;
	protected final ForumBoard						board;
	
	protected long									dateCreated			= System.currentTimeMillis();
	
	protected UUID									author;
	protected String								title;
	protected String								message;
	protected final HashMap<Integer, ForumComment>	comments			= new HashMap<>();
	protected boolean								isLocked			= false;
	protected boolean								isSticky			= false;
	protected boolean								isPrivate			= false;
	protected long									viewCount			= 0L;
	protected long									lastActivityTime	= -1L;
	
	@Override
	public ForumTopic setValuesFromHashMap(HashMap<String, String> values) {
		if(values == null || values.isEmpty()) {
			return this;
		}
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equals("title")) {
				this.title = value;
			} else if(pname.equals("message")) {
				this.message = value;
			} else if(pname.equals("isLocked")) {
				this.isLocked = Boolean.valueOf(value).booleanValue();
			} else if(pname.equals("isSticky")) {
				this.isSticky = Boolean.valueOf(value).booleanValue();
			} else if(pname.equals("isPrivate")) {
				this.isPrivate = Boolean.valueOf(value).booleanValue();
			}
		}
		this.saveToFile();
		return this;
	}
	
	protected ForumTopic(UUID uuid, ForumBoard board) {
		this.uuid = uuid;
		this.board = board;
		instances.add(this);
	}
	
	public final String getTitleInURL() {
		return ForumData.replaceUnsafeCharsInStr(getTitle());
	}
	
	//=======================================================
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final ForumBoard getParentBoard() {
		return this.board;
	}
	
	public final long getDateCreated() {
		return this.dateCreated;
	}
	
	public final UUID getAuthorAsUUID() {
		return this.author;
	}
	
	public final ForumUser getAuthor() {
		return this.board.forum.getUserFromUUID(this.author);
	}
	
	public final String getTitle() {
		return this.title;
	}
	
	public final String getMessage() {
		return this.message;
	}
	
	public final boolean isLocked() {
		return this.isLocked;
	}
	
	protected final ForumTopic setLocked(boolean locked) {
		this.isLocked = locked;
		return this;
	}
	
	public final boolean isSticky() {
		return this.isSticky;
	}
	
	protected final ForumTopic setSticky(boolean sticky) {
		this.isSticky = sticky;
		return this;
	}
	
	public final boolean isPrivate() {
		return this.isPrivate;
	}
	
	protected final ForumTopic setPrivate(boolean _private) {
		this.isPrivate = _private;
		return this;
	}
	
	public final long getViewCount() {
		return this.viewCount;
	}
	
	protected final ForumTopic setViewCount(long viewCount) {
		this.viewCount = viewCount;
		return this;
	}
	
	public final long getLastActivityTime() {
		return this.lastActivityTime;
	}
	
	protected final ForumTopic setLastActivityTime(long activityTime) {
		this.lastActivityTime = activityTime;
		return this;
	}
	
	//=======================================================
	
	public final ForumComment getForumCommentByNumber(long number) {
		for(ForumComment comment : this.getComments()) {
			if(comment.getNumber() == number) {
				return comment;
			}
		}
		return null;
	}
	
	public final ForumComment getForumCommentFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getForumCommentFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public final ForumComment getForumCommentFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(ForumComment comment : getComments()) {
			if(comment.uuid.toString().equals(uuid.toString())) {
				return comment;
			}
		}
		return null;
	}
	
	//===================================================
	
	public final ArrayList<ForumComment> getComments() {
		final ArrayList<ForumComment> rtrn = new ArrayList<>();
		for(Entry<Integer, ForumComment> entry : new HashMap<>(this.comments).entrySet()) {
			if(entry.getValue() != null) {
				rtrn.add(entry.getValue());
			}
		}
		return rtrn;
	}
	
	public final boolean checkIfCommentExists(ForumComment comment) {
		if(comment == null || this.comments.isEmpty()) {
			return false;
		}
		for(ForumComment curComment : this.getComments()) {
			if(curComment.uuid.toString().equals(comment.uuid.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public final Integer getIntegerOfComment(ForumComment comment) {
		if(checkIfCommentExists(comment)) {
			for(Entry<Integer, ForumComment> entry : new HashMap<>(this.comments).entrySet()) {
				if(entry.getValue().uuid.toString().equals(comment.uuid.toString())) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	public final void addComment(ForumComment topic) {
		if(!checkIfCommentExists(topic)) {
			Integer key = Integer.valueOf(this.comments.size());
			this.comments.put(key, topic);
		}
	}
	
	public final void removeComment(ForumComment topic) {
		if(checkIfCommentExists(topic)) {
			Integer key = getIntegerOfComment(topic);
			if(key != null) {
				HashMap<Integer, ForumComment> temp = new HashMap<>(this.comments);
				this.comments.clear();
				for(Entry<Integer, ForumComment> entry : temp.entrySet()) {
					int k = entry.getKey().intValue();
					if(k > key.intValue()) {
						this.comments.put(Integer.valueOf(k - 1), entry.getValue());
					} else if(k != key.intValue()) {
						this.comments.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				throw new Error("Uhh, wat? A forum comment exists, but yet doesn't?");
			}
			topic.delete();
		}
	}
	
	public final File getCommentFolder() {
		return new File(this.getSaveFolder(), "ForumComments");
	}
	
	protected final ArrayList<ForumComment> loadCommentsFromFile() {
		final ArrayList<ForumComment> rtrn = new ArrayList<>();
		File commentFolder = this.getCommentFolder();
		if(!commentFolder.exists()) {
			commentFolder.mkdirs();
			return rtrn;
		}
		for(String folderName : commentFolder.list()) {
			File curCommentFolder = new File(commentFolder, folderName);
			if(curCommentFolder.exists() && curCommentFolder.isDirectory()) {
				File commentFile = new File(curCommentFolder, "comment.yml");
				if(commentFile.exists() && commentFile.isFile()) {
					if(StringUtils.isStrUUID(folderName)) {
						UUID commentUUID = UUID.fromString(folderName);
						ForumComment comment = new ForumComment(commentUUID, this);
						this.addComment(comment);
						comment.loadFromFile();
						rtrn.add(comment);
					}
				}
			}
		}
		return rtrn;
	}
	
	protected final ArrayList<ForumComment> saveCommentsToFile() {
		final ArrayList<ForumComment> rtrn = new ArrayList<>();
		File commentFolder = this.getCommentFolder();
		if(!commentFolder.exists()) {
			commentFolder.mkdirs();
		}
		for(ForumComment comment : this.getComments()) {
			comment.saveToFile();
			rtrn.add(comment);
		}
		return rtrn;
	}
	
	//=======================================================
	
	public final void deleteComments() {
		for(ForumComment comment : this.getComments()) {
			comment.delete();
		}
		this.comments.clear();
	}
	
	@Override
	public final File getSaveFolder() {
		File folder = new File(this.board.getTopicFolder(), this.uuid.toString());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	@Override
	public final File getSaveFile() throws IOException {
		File rtrn = new File(this.getSaveFolder(), "topic.yml");
		if(!rtrn.exists()) {
			rtrn.createNewFile();
		}
		return rtrn;
	}
	
	@Override
	public final boolean saveToFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("title", this.title);//TODO
			config.set("dateCreated", Long.valueOf(this.dateCreated));
			config.set("author", this.author.toString());
			config.set("message", this.message);
			config.set("isLocked", Boolean.valueOf(this.isLocked));
			config.set("isSticky", Boolean.valueOf(this.isSticky));
			config.set("isPrivate", Boolean.valueOf(this.isPrivate));
			config.set("viewCount", Long.valueOf(this.viewCount));
			config.set("lastActivityTime", Long.valueOf(this.lastActivityTime));
			config.save(this.getSaveFile());
			this.saveCommentsToFile();
			return true;
		} catch(Throwable e) {
			PrintUtil.printThrowable(e);
			return false;
		}
	}
	
	@Override
	public final boolean loadFromFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			File file = this.getSaveFile();
			config.load(file);
			this.title = config.getString("title", "Topic Title");
			this.dateCreated = config.getLong("dateCreated", this.dateCreated);
			String authorUUID = config.getString("author", "");
			if(StringUtils.isStrUUID(authorUUID)) {
				this.author = UUID.fromString(authorUUID);
			} else {
				PrintUtil.printErrln("Warning! The saved author UUID for the topic \"/forum/" + this.board.forum.getName() + "/topic/" + this.getTitleInURL() + "\" was not a valid UUID string!");
				this.dispose();
				return false;
			}
			
			this.message = config.getString("message", "Topic_Message");
			this.isLocked = config.getBoolean("isLocked", this.isLocked);
			this.isSticky = config.getBoolean("isSticky", this.isSticky);
			this.isPrivate = config.getBoolean("isPrivate", this.isPrivate);
			this.viewCount = config.getLong("viewCount", this.viewCount);
			this.lastActivityTime = config.getLong("lastActivityTime", this.lastActivityTime);
			this.loadCommentsFromFile();
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	/** @see com.gmail.br45entei.server.data.DisposableUUIDData#dispose() */
	@Override
	public final void dispose() {
		this.author = null;
		this.title = null;
		this.message = null;
		this.comments.clear();
		this.isLocked = false;
		this.isSticky = false;
		this.isPrivate = false;
		this.viewCount = 0L;
		this.lastActivityTime = 0L;
		instances.remove(this);
	}
	
	/** @see com.gmail.br45entei.server.data.DisposableUUIDData#delete() */
	@Override
	public void delete() {
		this.deleteComments();
		this.dispose();
		this.getSaveFolder().delete();
	}
	
	@Override
	public String toHTML() {//TODO
		String rtrn = "\r\n";
		rtrn += "\r\n";
		rtrn += "\r\n";
		return rtrn;
	}
	
}
