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
public class ForumBoard implements DisposableUUIDData, SavableData, HTMLParsableData {//(A forum section)
	protected static final ArrayList<ForumBoard>	instances	= new ArrayList<>();
	
	/** @return An arraylist containing all forum boards currently in use */
	public static final ArrayList<ForumBoard> getInstances() {
		return new ArrayList<>(instances);
	}
	
	protected final UUID							uuid;
	protected final ForumData						forum;
	
	protected long									dateCreated	= System.currentTimeMillis();
	
	protected String								name;
	protected String								description;
	protected final HashMap<Integer, ForumTopic>	topics		= new HashMap<>();
	
	@Override
	public final ForumBoard setValuesFromHashMap(HashMap<String, String> values) {
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
					this.name = ForumData.replaceUnsafeCharsInStr(value);
				}
			} else if(pname.equals("description")) {
				this.description = value;
			}
		}
		this.saveToFile();
		return this;
	}
	
	protected ForumBoard(UUID uuid, ForumData forum) {
		this.uuid = uuid;
		this.forum = forum;
		instances.add(this);
	}
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final ForumData getParentForum() {
		return this.forum;
	}
	
	public final String getName() {
		return this.name;
	}
	
	protected final ForumBoard setName(String name) {
		this.name = name;
		return this;
	}
	
	public final long getDateCreated() {
		return this.dateCreated;
	}
	
	public final String getDescription() {
		return this.description;
	}
	
	protected final ForumBoard setDescription(String description) {
		this.description = description;
		return this;
	}
	
	//===================================================
	
	public final ForumTopic getForumTopicFromName(String name) {
		for(ForumTopic topic : this.getTopics()) {
			if(topic.getTitle().equalsIgnoreCase(name.replace("%20", " ")) || topic.getTitleInURL().equalsIgnoreCase(name.replace("%20", " "))) {
				return topic;
			}
		}
		return null;
	}
	
	public final ForumTopic getForumTopicFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getForumTopicFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public final ForumTopic getForumTopicFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(ForumTopic topic : getTopics()) {
			if(topic.uuid.toString().equals(uuid.toString())) {
				return topic;
			}
		}
		return null;
	}
	
	//===================================================
	
	public final ArrayList<ForumTopic> getTopics() {
		final ArrayList<ForumTopic> rtrn = new ArrayList<>();
		for(Entry<Integer, ForumTopic> entry : this.topics.entrySet()) {
			if(entry.getValue() != null) {
				rtrn.add(entry.getValue());
			}
		}
		return rtrn;
	}
	
	public final boolean checkIfTopicExists(ForumTopic topic) {
		if(topic == null || this.topics.isEmpty()) {
			return false;
		}
		for(ForumTopic curTopic : this.getTopics()) {
			if(curTopic.uuid.toString().equals(topic.uuid.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public final Integer getIntegerOfTopic(ForumTopic topic) {
		if(checkIfTopicExists(topic)) {
			for(Entry<Integer, ForumTopic> entry : new HashMap<>(this.topics).entrySet()) {
				if(entry.getValue().uuid.toString().equals(topic.uuid.toString())) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	public final void addTopic(ForumTopic topic) {
		if(!checkIfTopicExists(topic)) {
			Integer key = Integer.valueOf(this.topics.size());
			this.topics.put(key, topic);
		}
	}
	
	public final void removeTopic(ForumTopic topic) {
		if(checkIfTopicExists(topic)) {
			Integer key = getIntegerOfTopic(topic);
			if(key != null) {
				HashMap<Integer, ForumTopic> temp = new HashMap<>(this.topics);
				this.topics.clear();
				for(Entry<Integer, ForumTopic> entry : temp.entrySet()) {
					int k = entry.getKey().intValue();
					if(k > key.intValue()) {
						this.topics.put(Integer.valueOf(k - 1), entry.getValue());
					} else if(k != key.intValue()) {
						this.topics.put(entry.getKey(), entry.getValue());
					}
				}
			} else {
				throw new Error("Uhh, wat? A forum topic exists, but yet doesn't?");
			}
			topic.delete();
		}
	}
	
	public final File getTopicFolder() {
		return new File(this.getSaveFolder(), "ForumTopics");
	}
	
	protected final ArrayList<ForumTopic> loadTopicsFromFile() {
		final ArrayList<ForumTopic> rtrn = new ArrayList<>();
		File topicFolder = this.getTopicFolder();
		if(!topicFolder.exists()) {
			topicFolder.mkdirs();
			return rtrn;
		}
		for(String folderName : topicFolder.list()) {
			File curTopicFolder = new File(topicFolder, folderName);
			if(curTopicFolder.exists() && curTopicFolder.isDirectory()) {
				File topicFile = new File(curTopicFolder, "topic.yml");
				if(topicFile.exists() && topicFile.isFile()) {
					if(StringUtils.isStrUUID(folderName)) {
						UUID topicUUID = UUID.fromString(folderName);
						ForumTopic topic = new ForumTopic(topicUUID, this);
						this.addTopic(topic);
						topic.loadFromFile();
						rtrn.add(topic);
					}
				}
			}
		}
		return rtrn;
	}
	
	protected final ArrayList<ForumTopic> saveTopicsToFile() {
		final ArrayList<ForumTopic> rtrn = new ArrayList<>();
		File topicFolder = this.getTopicFolder();
		if(!topicFolder.exists()) {
			topicFolder.mkdirs();
		}
		for(ForumTopic topic : this.getTopics()) {
			topic.saveToFile();
			rtrn.add(topic);
		}
		return rtrn;
	}
	
	//===================================================
	
	public final void deleteTopics() {
		for(ForumTopic topic : this.getTopics()) {
			topic.delete();
		}
		this.topics.clear();
	}
	
	@Override
	public final File getSaveFolder() {
		File folder = new File(this.forum.getBoardFolder(), this.uuid.toString());
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	@Override
	public final File getSaveFile() throws IOException {
		File rtrn = new File(this.getSaveFolder(), "board.yml");
		if(!rtrn.exists()) {
			rtrn.createNewFile();
		}
		return rtrn;
	}
	
	@Override
	public final boolean saveToFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("name", this.name);//TODO
			config.set("dateCreated", Long.valueOf(this.dateCreated));
			config.set("description", this.description);
			config.save(this.getSaveFile());
			this.saveTopicsToFile();
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
			this.name = config.getString("name", "Board_Name");//TODO
			this.dateCreated = config.getLong("dateCreated", this.dateCreated);
			this.description = config.getString("description", "Board Description");
			this.loadTopicsFromFile();
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	@Override
	public final void delete() {
		this.deleteTopics();
		this.dispose();
		this.getSaveFolder().delete();
	}
	
	@Override
	public final void dispose() {
		this.name = null;
		this.dateCreated = 0L;
		this.description = null;
		this.topics.clear();
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
