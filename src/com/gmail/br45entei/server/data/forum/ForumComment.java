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
public class ForumComment implements DisposableUUIDData, SavableData, HTMLParsableData {//(A forum thread reply)
	protected static final ArrayList<ForumComment>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all forum comments currently in use */
	public static final ArrayList<ForumComment> getInstances() {
		return new ArrayList<>(instances);
	}
	
	protected final UUID		uuid;
	protected final ForumTopic	topic;
	
	protected long				dateCreated	= System.currentTimeMillis();
	
	protected UUID				author;
	protected String			message;
	protected long				postTime;
	protected long				postNumber;
	protected long				editCount;
	protected long				lastEditTime;
	
	@Override
	public ForumComment setValuesFromHashMap(HashMap<String, String> values) {
		if(values == null || values.isEmpty()) {
			return this;
		}
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equals("message")) {
				this.message = value;
				this.lastEditTime = System.currentTimeMillis();//weeeee
			}
		}
		this.saveToFile();
		return this;
	}
	
	protected ForumComment(UUID uuid, ForumTopic topic) {
		this.uuid = uuid;
		this.topic = topic;
		this.postNumber = topic.getComments().size();
		instances.add(this);
	}
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final ForumTopic getParentTopic() {
		return this.topic;
	}
	
	public final long getNumber() {
		return this.postNumber;
	}
	
	public final String getMessage() {
		return this.message;
	}
	
	public final long getDateCreated() {
		return this.dateCreated;
	}
	
	public final long getDatePosted() {
		return this.postTime;
	}
	
	public final long getEditCount() {
		return this.editCount;
	}
	
	public final long getLastEditTime() {
		return this.lastEditTime;
	}
	
	public final UUID getAuthorAsUUID() {
		return this.author;
	}
	
	public final ForumUser getAuthor() {
		return this.topic.board.forum.getUserFromUUID(this.author);
	}
	
	//===================================================
	
	@Override
	public final File getSaveFolder() {
		File folder = new File(this.topic.getCommentFolder(), this.uuid.toString());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	@Override
	public final File getSaveFile() throws IOException {
		File rtrn = new File(this.getSaveFolder(), "comment.yml");
		if(!rtrn.exists()) {
			rtrn.createNewFile();
		}
		return rtrn;
	}
	
	@Override
	public final boolean saveToFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("author", this.author.toString());
			config.set("dateCreated", Long.valueOf(this.dateCreated));
			config.set("message", this.message);
			config.set("postTime", Long.valueOf(this.postTime));
			config.set("postNumber", Long.valueOf(this.postNumber));
			config.set("editCount", Long.valueOf(this.editCount));
			config.set("lastEditTime", Long.valueOf(this.lastEditTime));
			config.save(this.getSaveFile());
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
			this.dateCreated = config.getLong("dateCreated", this.dateCreated);
			String authorUUID = config.getString("author", "");
			if(StringUtils.isStrUUID(authorUUID)) {
				this.author = UUID.fromString(authorUUID);
			} else {
				PrintUtil.printErrln("Warning! The saved author UUID for the topic \"/forum/" + this.topic.board.forum.getName() + "/topic/" + this.topic.getTitleInURL() + "#comment-" + this.topic.getIntegerOfComment(this) + "\" was not a valid UUID string!");
				this.dispose();
				return false;
			}
			
			this.message = config.getString("message", "Comment_Message");
			this.postTime = config.getLong("postTime", this.postTime);
			this.postNumber = config.getLong("postNumber", this.postNumber);
			this.editCount = config.getLong("editCount", this.editCount);
			this.lastEditTime = config.getLong("lastEditTime", this.lastEditTime);
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	@Override
	public final void delete() {
		this.dispose();
		this.getSaveFolder().delete();
	}
	
	@Override
	public final void dispose() {
		this.author = null;
		this.message = null;
		this.postTime = 0L;
		this.editCount = 0L;
		this.lastEditTime = 0L;
		instances.remove(this);
	}
	
	@Override
	public String toHTML() {//TODO
		String rtrn = "\r\n";
		rtrn += "\r\n";
		rtrn += "\r\n";
		return rtrn;
	}
	
}
