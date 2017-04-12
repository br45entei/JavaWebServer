package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.ResourceFactory;
import com.gmail.br45entei.configuration.ConfigurationSection;
import com.gmail.br45entei.configuration.file.YamlConfiguration;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.MimeTypes;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

/** Class used to load save and read data pertaining to each domain that has
 * been assigned to this server
 *
 * @author Brian_Entei */
public final class DomainDirectory implements DisposableUUIDData {
	
	protected static final ArrayList<DomainDirectory> instances = new ArrayList<>();
	
	public static final DomainDirectory getDefault(ClientConnection reuse) {
		final String domain = AddressUtil.getLocalExternalHostName();
		DomainDirectory check = getDomainDirectoryFromDomainName(domain);
		if(check == null) {
			check = getOrCreateDomainDirectory(domain, reuse);
		}
		reuse.domainDirectory = reuse.domainDirectory == null ? check : reuse.domainDirectory;
		reuse.status.setDomainDirectory(reuse.domainDirectory);
		return check;
	}
	
	/** @return An arraylist containing all domain directory data currently
	 *         in use */
	public static final ArrayList<DomainDirectory> getInstances() {
		ArrayList<DomainDirectory> rtrn = new ArrayList<>(instances);
		rtrn.removeAll(Collections.singleton(null));
		return rtrn;
	}
	
	public final Property<Long> dateCreated = new Property<>("Date Created", Long.valueOf(System.currentTimeMillis())).setDescription("");
	public final Property<Long> lastEdited = new Property<>("Last Edited", this.dateCreated.getValue()).setDescription("");
	
	private boolean isTemporary = false;
	
	public final Property<UUID> uuid = new Property<UUID>("UUID").setDescription("This domain's randomly generated universally unique identifier, or UUID. This cannot be changed.");
	public final Property<File> folder = new Property<File>("HomeDirectory").setDescription("The root folder that will be used as a starting point for serving and looking up files. If no default file is specified, the generated page for this folder is displayed in place of the home page.");
	public final Property<String> domain = new Property<String>("Domain").setDescription("The ip address that clients will use when connecting to the server. A client may actually use a different ip address, but must use this domain's ip address in the \"host: \" header of it's request in order to connect to this domain, or a \"400 Bad Request\" response will be sent to the client for not having specified a host to connect to(if using HTTP/1.0, the server's external ip address is assumed).");
	public final Property<String> serverName = new Property<>("ServerName", JavaWebServer.SERVER_NAME).setDescription("The server name for this domain.");
	public final Property<String> displayName = new Property<String>("DisplayName").setDescription("This domain's 'display name', or the name that will be displayed in the client's webpage title.");
	public final Property<Boolean> displayLogEntries = new Property<>("DisplayLogEntries", Boolean.TRUE).setDescription("Whether or not any logs will be shown on the console(including the filesystem) for this domain while clients connect to and download files from it.");
	public final Property<String> defaultFileName = new Property<>("DefaultFileName", JavaWebServer.DEFAULT_FILE_NAME).setDescription("The file(or homepage) that is sent to connecting clients when no file is requested(i.e. \"GET / HTTP/1.1\")");
	public final Property<String> defaultFontFace = new Property<>("DefaultFontFace", JavaWebServer.defaultFontFace).setDescription("The default font that every directory page will use.");
	public final Property<String> defaultPageIcon = new Property<>("DefaultPageIcon", JavaWebServer.DEFAULT_PAGE_ICON).setDescription("The default icon(or favicon) that every directory page(in addition to the default server-wide favicon) will use.");
	public final Property<String> defaultStylesheet = new Property<>("DefaultStylesheet", JavaWebServer.DEFAULT_STYLESHEET).setDescription("The default CSS stylesheet that every generated directory page will use.");
	
	public final Property<String> pageHeaderContent = new Property<>("PageHeaderContent", "").setDescription("Any html code that should be placed within the <head> tag of every dynamically generated page from this domain(such as directory pages).");
	
	public final Property<Integer> mtu = new Property<>("NetworkMTU", Integer.valueOf(0x400)).setDescription("The maximum transmission unit, or MTU, to use when writing bytes to the output streams of clients(buffer size to use when sending clients data). Recommended setting is 1500, maximum setting should be 20480. Multiples of 512(or powers of 2) are recommended as well, such as 1024.");
	public final Property<Long> cacheMaxAge = new Property<>("CacheMaxAge", JavaWebServer.DEFAULT_CACHE_MAX_AGE).setDescription("The maximum amount of time that clients should keep server files cached.");
	
	public final Property<Boolean> areDirectoriesForbidden = new Property<>("AreDirectoriesForbidden", Boolean.FALSE).setDescription("Whether or not all directories(folders) are forbidden. Files contained within the directories will still be accessible. This allows you to better 'hide' your files without a password, or prevent clients from viewing your website's files all at once.");
	public final Property<Boolean> calculateDirectorySizes = new Property<>("CalculateDirectorySizes", new Boolean(JavaWebServer.calculateDirectorySizes)).setDescription("Whether or not folder sizes are calculated via recursive reading. Recommended setting is false, as this can be slow with old hard disk drives or with many large files, reducing server speed.");
	public final Property<Boolean> numberDirectoryEntries = new Property<>("NumberDirectoryEntries", Boolean.FALSE).setDescription("Whether or not files are numbered in the order that they are read from the local filesystem.");
	public final Property<Boolean> listDirectoriesFirst = new Property<>("ListDirectoriesFirst", Boolean.TRUE).setDescription("Whether or not directories(folders) will be listed first.");
	
	public final Property<Boolean> enableGZipCompression = new Property<>("EnableGZipCompression", Boolean.TRUE).setDescription("Whether or not Gzip will be used when clients request it");
	public final Property<Boolean> enableFileUpload = new Property<>("EnableFileUpload", Boolean.TRUE).setDescription("Whether or not authenticated users may upload files to the directory they are currently browsing");
	public final Property<Boolean> enableAlternateDirectoryListingViews = new Property<>("EnableAlternateDirectoryListingViews", Boolean.TRUE).setDescription("");
	public final Property<Boolean> enableMediaView = new Property<>("EnableMediaView", Boolean.TRUE).setDescription("Enables viewing audio, video, and image files all on the same page.");
	public final Property<Boolean> enableMediaList = new Property<>("EnableMediaList", Boolean.TRUE).setDescription("Enables viewing the media information of any music files on the same page.");
	public final Property<Boolean> enableXmlListView = new Property<>("EnableXmlListView", Boolean.TRUE).setDescription("Enables viewing a raw xml version of the directory.");
	public final Property<Boolean> enableFilterView = new Property<>("EnableFilterView", Boolean.TRUE).setDescription("Enables filtering file types in the directory.");
	public final Property<Boolean> enableSortView = new Property<>("EnableSortView", Boolean.TRUE).setDescription("Enables sorting the files by name, number, date, type, and size.");
	public final Property<Boolean> enableVLCPlaylistView = new Property<>("EnableVLCPlaylistView", Boolean.TRUE).setDescription("Enables auto-generated VLC playlists of all of the media files on the current directory page.");
	public final Property<Boolean> enableReadableFileViews = new Property<>("EnableReadableFileViews", Boolean.TRUE).setDescription("Enables a link to appear next to the file name of a file that cannot usually be viewed in the browser(i.e. *.rtf files).");
	
	public final Property<Boolean> ignoreThumbsdbFiles = new Property<>("IgnoreThumbsdbFiles", Boolean.TRUE).setDescription("Enables automatic exclusion of any \"Thumbs.db\" files.");
	
	public final HashMap<String, Property<?>> getPropertiesAsHashMap() {
		final HashMap<String, Property<?>> rtrn = new HashMap<>();
		rtrn.put(this.uuid.getName(), this.uuid);
		rtrn.put(this.folder.getName(), this.folder);
		rtrn.put(this.domain.getName(), this.domain);
		rtrn.put(this.serverName.getName(), this.serverName);
		rtrn.put(this.displayName.getName(), this.displayName);
		rtrn.put(this.displayLogEntries.getName(), this.displayLogEntries);
		rtrn.put(this.defaultFileName.getName(), this.defaultFileName);
		rtrn.put(this.defaultFontFace.getName(), this.defaultFontFace);
		rtrn.put(this.defaultPageIcon.getName(), this.defaultPageIcon);
		rtrn.put(this.defaultStylesheet.getName(), this.defaultStylesheet);
		rtrn.put(this.pageHeaderContent.getName(), this.pageHeaderContent);
		rtrn.put(this.cacheMaxAge.getName(), this.cacheMaxAge);
		rtrn.put(this.numberDirectoryEntries.getName(), this.numberDirectoryEntries);
		rtrn.put(this.listDirectoriesFirst.getName(), this.listDirectoriesFirst);
		rtrn.put(this.enableAlternateDirectoryListingViews.getName(), this.enableAlternateDirectoryListingViews);
		rtrn.put(this.enableMediaView.getName(), this.enableMediaView);
		rtrn.put(this.enableXmlListView.getName(), this.enableXmlListView);
		rtrn.put(this.enableFilterView.getName(), this.enableFilterView);
		rtrn.put(this.enableSortView.getName(), this.enableSortView);
		rtrn.put(this.enableVLCPlaylistView.getName(), this.enableVLCPlaylistView);
		rtrn.put(this.enableReadableFileViews.getName(), this.enableReadableFileViews);
		rtrn.put(this.ignoreThumbsdbFiles.getName(), this.ignoreThumbsdbFiles);
		return rtrn;
	}
	
	public final DomainDirectory setValuesFromHashMap(HashMap<String, String> values, ClientConnection reuse) {
		for(Entry<String, String> entry : values.entrySet()) {
			String pname = entry.getKey();
			String value = entry.getValue();
			if(pname.equalsIgnoreCase(this.folder.getName())) {
				File check = new File(FilenameUtils.normalize(value));
				if(check.isDirectory()) {
					this.folder.setValue(check);
					reuse.println("\t--- Domain \"" + this.getDomain() + "\"'s home directory is now: \"" + this.getDirectorySafe().getAbsolutePath() + "\"!");
				} else {
					reuse.println("\t--- Unable to set domain \"" + this.getDomain() + "\"'s home directory to: \"" + FilenameUtils.normalize(value) + "\"! Reason: File does not exist or is not a directory!");
				}
			} else if(pname.equalsIgnoreCase(this.serverName.getName())) {
				this.serverName.setValue(value);
			} else if(pname.equalsIgnoreCase(this.displayName.getName())) {
				if(value != null && !value.trim().isEmpty()) {
					this.displayName.setValue(value);
				}
			} else if(pname.equalsIgnoreCase(this.displayLogEntries.getName())) {
				this.displayLogEntries.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.defaultFileName.getName())) {
				this.defaultFileName.setValue(value);
			} else if(pname.equalsIgnoreCase(this.defaultFontFace.getName())) {
				this.defaultFontFace.setValue(value);
			} else if(pname.equalsIgnoreCase(this.defaultPageIcon.getName())) {
				this.defaultPageIcon.setValue(value);
			} else if(pname.equalsIgnoreCase(this.defaultStylesheet.getName())) {
				this.defaultStylesheet.setValue(value);
			} else if(pname.equalsIgnoreCase(this.pageHeaderContent.getName())) {
				this.pageHeaderContent.setValue(value);
			} else if(pname.equalsIgnoreCase(this.mtu.getName())) {
				try {
					Integer v = Integer.valueOf(value);
					if(v.intValue() > 0 && v.intValue() % 512 == 0) {
						this.mtu.setValue(v);
					}
				} catch(NumberFormatException ignored) {
				}
			} else if(pname.equalsIgnoreCase(this.cacheMaxAge.getName())) {
				try {
					Long v = new Long(value);
					this.cacheMaxAge.setValue(v);
				} catch(NumberFormatException ignored) {
				}
			} else if(pname.equalsIgnoreCase(this.areDirectoriesForbidden.getName())) {
				this.areDirectoriesForbidden.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.calculateDirectorySizes.getName())) {
				this.calculateDirectorySizes.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.numberDirectoryEntries.getName())) {
				this.numberDirectoryEntries.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.listDirectoriesFirst.getName())) {
				this.listDirectoriesFirst.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableGZipCompression.getName())) {
				this.enableGZipCompression.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableFileUpload.getName())) {
				this.enableFileUpload.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableAlternateDirectoryListingViews.getName())) {
				this.enableAlternateDirectoryListingViews.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableMediaView.getName())) {
				this.enableMediaView.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableXmlListView.getName())) {
				this.enableXmlListView.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableFilterView.getName())) {
				this.enableFilterView.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableSortView.getName())) {
				this.enableSortView.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableVLCPlaylistView.getName())) {
				this.enableVLCPlaylistView.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.enableReadableFileViews.getName())) {
				this.enableReadableFileViews.setValue(Boolean.valueOf(value));
			} else if(pname.equalsIgnoreCase(this.ignoreThumbsdbFiles.getName())) {
				this.ignoreThumbsdbFiles.setValue(Boolean.valueOf(value));
			}
		}
		this.lastEdited.setValue(Long.valueOf(System.currentTimeMillis()));
		this.saveToFile();
		return this;
	}
	
	private final HashMap<String, String> pathAliases = new HashMap<>();
	
	private final HashMap<String, String> mimeTypes = new HashMap<>();
	
	public static final DomainDirectory getDomainDirectoryFromUUID(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			return null;
		}
		if(StringUtils.isStrUUID(uuid)) {
			return getDomainDirectoryFromUUID(UUID.fromString(uuid));
		}
		return null;
	}
	
	public static final DomainDirectory getDomainDirectoryFromUUID(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		for(DomainDirectory domain : DomainDirectory.getInstances()) {
			if(domain != null && !domain.isTemporary && domain.uuid != null && domain.uuid.getValue().toString().equals(uuid.toString())) {
				return domain;
			}
		}
		return null;
	}
	
	/** @param domain The domain whose data will be returned
	 * @return The associated data for the given domain, or null if it does not
	 *         exist */
	public static final DomainDirectory getDomainDirectoryFromDomainName(String domain) {
		if(domain == null || domain.trim().isEmpty()) {
			throw new NullPointerException("Domain cannot be null or empty!");
		}
		//loadAllDomainDirectoryDataFromFile();
		for(DomainDirectory domainDirectory : getInstances()) {
			if(domainDirectory.domain.getValue() != null && !domainDirectory.isTemporary) {
				String check = domainDirectory.domain.getValue();
				if(check.equalsIgnoreCase(domain)) {
					return domainDirectory;
				} else if((check + ":" + JavaWebServer.listen_port).equalsIgnoreCase(domain)) {
					return domainDirectory;
				} else if((check + ":" + JavaWebServer.ssl_listen_port).equalsIgnoreCase(domain)) {
					return domainDirectory;
				} else if((check + ":" + JavaWebServer.admin_listen_port).equalsIgnoreCase(domain)) {
					return domainDirectory;
				}
			}
		}
		return null;
	}
	
	public static final DomainDirectory createNewDomainDirectory(final UUID uuid) {
		if(uuid == null) {
			throw new IllegalArgumentException("UUID cannot be null!");
		}
		return new DomainDirectory(uuid);
	}
	
	/** @param domain The domain whose data will be returned
	 * @return The associated data for the given domain */
	public static DomainDirectory getOrCreateDomainDirectory(String domain, ClientConnection reuse) {
		if(domain == null || domain.trim().isEmpty()) {
			throw new NullPointerException("Domain cannot be null or empty!");
		}
		DomainDirectory domainDirectory = getDomainDirectoryFromDomainName(domain);
		if(domainDirectory == null) {
			domainDirectory = new DomainDirectory(UUID.randomUUID(), JavaWebServer.homeDirectory, domain, JavaWebServer.SERVER_NAME, JavaWebServer.DEFAULT_FILE_NAME, JavaWebServer.defaultFontFace, JavaWebServer.DEFAULT_PAGE_ICON, JavaWebServer.DEFAULT_STYLESHEET);
			domainDirectory.loadFromFile();
			domainDirectory.saveToFile();
			instances.add(domainDirectory);
			if(reuse != null) {
				reuse.println("Just automaticaly created domain \"" + domainDirectory.getDomain() + "\" from requested non-existant: \"" + domain + "\"");
			}
		}
		return domainDirectory;
	}
	
	private DomainDirectory(UUID uuid) {
		this.uuid.setValue(uuid).lockValue();
	}
	
	private DomainDirectory(UUID uuid, File folder, String domain, String serverName, String defaultFileName, String defaultFontFace, String defaultPageIcon, String defaultStylesheet) {
		this(uuid);
		this.folder.setValue(folder);
		this.domain.setValue(domain);
		this.serverName.setValue(serverName);
		this.displayName.setValue(domain);
		this.defaultFileName.setValue(defaultFileName);
		this.defaultFontFace.setValue(defaultFontFace);
		this.defaultPageIcon.setValue(defaultPageIcon);
		this.defaultStylesheet.setValue(defaultStylesheet);
	}
	
	@Override
	public final UUID getUUID() {
		return this.uuid.getValue();
	}
	
	/** @return The home directory for this domain */
	public final File getDirectory() {
		return this.folder.getValue();
	}
	
	/** @return The home directory for this domain */
	public final File getDirectorySafe() {
		if(this.getDirectory() == null) {
			return null;
		}
		return new File(FilenameUtils.normalize(this.getDirectory().getAbsolutePath()));
	}
	
	/** @param folder The new home directory for this domain
	 * @return This domain data */
	public final DomainDirectory setDirectory(File folder) {
		if(folder == null) {
			throw new NullPointerException("Folder cannot be null!");
		}
		if(!folder.exists() || folder.isFile()) {
			throw new IllegalArgumentException("The folder \"" + folder + "\" does not exist or is not a directory.");
		}
		this.folder.setValue(folder);
		this.saveToFile();
		return this;
	}
	
	/** @return The domain representing this data */
	public final String getDomain() {
		return this.domain.getValue();
	}
	
	/** @return The server name for this domain */
	public final String getServerName() {
		return this.serverName.getValue();
	}
	
	/** @param serverName The server name to set for this domain
	 * @return This domain data */
	public final DomainDirectory setServerName(String serverName) {
		if(serverName == null || serverName.isEmpty()) {
			throw new IllegalArgumentException("'serverName' cannot be null or empty!");
		}
		this.serverName.setValue(serverName);
		return this;
	}
	
	/** @return The display name for this domain(used when viewing domains in
	 *         the
	 *         administration interface) */
	public final String getDisplayName() {
		return this.displayName.getValue();
	}
	
	/** @param displayName The display name to set for this domain(used when
	 *            viewing domains in the administration interface)
	 * @return This domain data */
	public final DomainDirectory setDisplayName(String displayName) {
		if(displayName == null || displayName.isEmpty()) {
			throw new IllegalArgumentException("'displayName' cannot be null or empty!");
		}
		this.displayName.setValue(displayName);
		return this;
	}
	
	/** @return Whether or not any logs will be shown for this domain */
	public final boolean getDisplayLogEntries() {
		return this.displayLogEntries.getValue().booleanValue();
	}
	
	/** @param displayLogEntries Whether or not any logs will be shown for this
	 *            domain
	 * @return This domain data */
	public final DomainDirectory setDisplayLogEntries(boolean displayLogEntries) {
		this.displayLogEntries.setValue(Boolean.valueOf(displayLogEntries));
		return this;
	}
	
	/** @return The default file name which is loaded and sent to the client
	 *         if present in the home directory */
	public final String getDefaultFileName() {
		return this.defaultFileName.getValue();
	}
	
	/** @param defaultFileName The default file name to set for this domain
	 * @return This domain data */
	public final DomainDirectory setDefaultFileName(String defaultFileName) {
		if(defaultFileName == null || defaultFileName.isEmpty()) {
			throw new IllegalArgumentException("'defaultFileName' cannot be null or empty!");
		}
		this.defaultFileName.setValue(defaultFileName);
		return this;
	}
	
	/** @return The default font face for this domain */
	public final String getDefaultFontFace() {
		return this.defaultFontFace.getValue();
	}
	
	/** @param defaultFontFace The default font face to set for this domain
	 * @return This domain data */
	public final DomainDirectory setDefaultFontFace(String defaultFontFace) {
		if(defaultFontFace == null || defaultFontFace.isEmpty()) {
			throw new IllegalArgumentException("'defaultFontFace' cannot be null or empty!");
		}
		this.defaultFontFace.setValue(defaultFontFace);
		return this;
	}
	
	/** @return The default page icon for this domain(can be a file name or a
	 *         file path relative to the home directory of this domain) */
	public final String getDefaultPageIcon() {
		return this.defaultPageIcon.getValue();
	}
	
	/** @param defaultPageIcon The default page icon to set for this domain
	 * @return This domain data */
	public final DomainDirectory setDefaultPageIcon(String defaultPageIcon) {
		if(defaultPageIcon == null || defaultPageIcon.isEmpty()) {
			throw new IllegalArgumentException("'defaultPageIcon' cannot be null or empty!");
		}
		this.defaultPageIcon.setValue(defaultPageIcon);
		return this;
	}
	
	/** @return The default style sheet stored on the local filesystem, or
	 *         null if there isn't one(or it is represented by an external
	 *         url) */
	public final File getDefaultStyleSheetFromFileSystem() {
		String stylesheetlink = this.getDefaultStylesheet();
		if(stylesheetlink == null || stylesheetlink.startsWith("http")) {
			return null;
		}
		return new File(FilenameUtils.normalize(this.getDirectorySafe().getAbsolutePath() + File.separatorChar + stylesheetlink));
	}
	
	/** @return Whether or not the default style sheet exists on the local
	 *         filesystem(or it is represented by an external url). */
	public final boolean doesDefaultStyleSheetExist() {
		String stylesheetlink = this.getDefaultStylesheet();
		if(stylesheetlink == null) {
			return false;
		}
		if(stylesheetlink.startsWith("http")) {
			return true;
		}
		return new File(FilenameUtils.normalize(this.getDirectorySafe().getAbsolutePath() + File.separatorChar + stylesheetlink)).exists();
	}
	
	public static final File getStyleSheetFromFileSystem(String stylesheet) {
		if(stylesheet == null || stylesheet.startsWith("http")) {
			return null;
		}
		return new File(FilenameUtils.normalize(JavaWebServer.homeDirectory.getAbsolutePath() + File.separatorChar + stylesheet));
	}
	
	/** @param stylesheet The stylesheet to check
	 * @return Whether or not the stylesheet exists on the local filesystem(or
	 *         it is represented by an external url). */
	public static final boolean doesStyleSheetExist(String stylesheet) {
		if(stylesheet == null) {
			return false;
		}
		if(stylesheet.startsWith("http")) {
			return true;
		}
		return new File(FilenameUtils.normalize(JavaWebServer.homeDirectory.getAbsolutePath() + File.separatorChar + stylesheet)).exists();
	}
	
	/** @return The default style sheet for this domain(can be a file name or
	 *         a file path relative to the home directory of this domain, or
	 *         a complete link) */
	public final String getDefaultStylesheet() {
		return this.defaultStylesheet.getValue();
	}
	
	/** @param defaultStylesheet The default style sheet to set for this
	 *            domain
	 * @return This domain data */
	public final DomainDirectory setDefaultStylesheet(String defaultStylesheet) {
		if(defaultStylesheet == null || defaultStylesheet.isEmpty()) {
			throw new IllegalArgumentException("'defaultStylesheet' cannot be null or empty!");
		}
		this.defaultStylesheet.setValue(defaultStylesheet);
		return this;
	}
	
	/** @return The html tag content that will be placed in the {@code <head>}
	 *         tag
	 *         of dynamically generated web pages from this domain(such as a
	 *         directory page). */
	public final String getPageHeaderContent() {
		return this.pageHeaderContent.getValue();
	}
	
	/** @param pageHeaderContent The html tag content that will be placed in the
	 *            {@code <head>} tag of dynamically generated web pages from
	 *            this domain(such as a directory page).
	 * @return This domain data */
	public final DomainDirectory setPageHeaderContent(String pageHeaderContent) {
		this.pageHeaderContent.setValue(pageHeaderContent == null ? "" : pageHeaderContent);
		return this;
	}
	
	/** @return The network MTU to be used when sending data to clients */
	public final int getNetworkMTU() {
		return this.mtu.getValue().intValue();
	}
	
	/** @param mtu The network MTU to be used when sending data to clients for
	 *            this domain
	 * @return This domain data */
	public final DomainDirectory setNetworkMTU(int mtu) {
		this.mtu.setValue(Integer.valueOf(mtu));
		return this;
	}
	
	/** @return The max-age HTTP header setting for client-side file
	 *         caching(this should be a number value, in seconds) for this
	 *         domain */
	public final long getCacheMaxAge() {
		return this.cacheMaxAge.getValue().longValue();
	}
	
	/** @param cacheMaxAge The max-age HTTP header setting to set for this
	 *            domain
	 * @return This domain data */
	public final DomainDirectory setCacheMaxAge(long cacheMaxAge) {
		this.cacheMaxAge.setValue(Long.valueOf(cacheMaxAge));
		return this;
	}
	
	public final boolean areDirectoriesForbidden() {
		return this.areDirectoriesForbidden.getValue().booleanValue();
	}
	
	public final DomainDirectory setDirectoriesForbidden(boolean areDirectoriesForbidden) {
		this.areDirectoriesForbidden.setValue(Boolean.valueOf(areDirectoriesForbidden));
		return this;
	}
	
	/** @return Whether or not directory sizes will be calculated */
	public final boolean getCalculateDirectorySizes() {
		return this.calculateDirectorySizes.getValue().booleanValue();
	}
	
	/** @param calculateDirectorySizes Whether or not directory sizes will be
	 *            calculated for this domain
	 * @return This domain data */
	public final DomainDirectory setCalculateDirectorySizes(boolean calculateDirectorySizes) {
		this.calculateDirectorySizes.setValue(new Boolean(calculateDirectorySizes));
		return this;
	}
	
	/** @return Whether or not directory listing pages will have their
	 *         entries numbered */
	public final boolean getNumberDirectoryEntries() {
		return this.numberDirectoryEntries.getValue().booleanValue();
	}
	
	/** @param numberDirectoryEntries Whether or not directory listing pages
	 *            will have their entries numbered for this domain
	 * @return This domain data */
	public final DomainDirectory setNumberDirectoryEntries(boolean numberDirectoryEntries) {
		this.numberDirectoryEntries.setValue(new Boolean(numberDirectoryEntries));
		return this;
	}
	
	/** @return Whether or not folders will be listed first in the directory
	 *         listing pages */
	public final boolean getListDirectoriesFirst() {
		return this.listDirectoriesFirst.getValue().booleanValue();
	}
	
	/** @param listDirectoriesFirst Whether or not folders will be listed
	 *            first in the directory listing pages for this domain
	 * @return This domain data */
	public final DomainDirectory setListDirectoriesFirst(boolean listDirectoriesFirst) {
		this.listDirectoriesFirst.setValue(Boolean.valueOf(listDirectoriesFirst));
		return this;
	}
	
	public final boolean getEnableGZipCompression() {
		return this.enableGZipCompression.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableGZipCompression(boolean enableGZipCompression) {
		this.enableGZipCompression.setValue(new Boolean(enableGZipCompression));
		return this;
	}
	
	public final boolean getEnableFileUpload() {
		return this.enableFileUpload.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableFileUpload(boolean enableFileUpload) {
		this.enableFileUpload.setValue(Boolean.valueOf(enableFileUpload));
		return this;
	}
	
	public final boolean getEnableAlternateDirectoryListingViews() {
		return this.enableAlternateDirectoryListingViews.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableAlternateDirectoryListingViews(boolean enableAlternateDirectoryListingViews) {
		this.enableAlternateDirectoryListingViews.setValue(new Boolean(enableAlternateDirectoryListingViews));
		return this;
	}
	
	public final boolean getEnableMediaView() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableMediaView.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableMediaView(boolean enableMediaView) {
		this.enableMediaView.setValue(Boolean.valueOf(enableMediaView));
		return this;
	}
	
	public final boolean getEnableMediaList() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableMediaList.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableMediaList(boolean enableMediaList) {
		this.enableMediaList.setValue(Boolean.valueOf(enableMediaList));
		return this;
	}
	
	public final boolean getEnableXmlListView() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableXmlListView.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableXmlListView(boolean enableXmlListView) {
		this.enableXmlListView.setValue(Boolean.valueOf(enableXmlListView));
		return this;
	}
	
	public final boolean getEnableFilterView() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableFilterView.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableFilterView(boolean enableFilterView) {
		this.enableFilterView.setValue(Boolean.valueOf(enableFilterView));
		return this;
	}
	
	public final boolean getEnableSortView() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableSortView.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableSortView(boolean enableSortView) {
		this.enableSortView.setValue(Boolean.valueOf(enableSortView));
		return this;
	}
	
	public final boolean getEnableVLCPlaylistView() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableVLCPlaylistView.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableVLCPlaylistView(boolean enableVLCPlaylistView) {
		this.enableVLCPlaylistView.setValue(Boolean.valueOf(enableVLCPlaylistView));
		return this;
	}
	
	public final boolean getEnableReadableFileViews() {
		return this.getEnableAlternateDirectoryListingViews() && this.enableReadableFileViews.getValue().booleanValue();
	}
	
	public final DomainDirectory setEnableReadableVFileViews(boolean enableReadableFileViews) {
		this.enableReadableFileViews.setValue(Boolean.valueOf(enableReadableFileViews));
		return this;
	}
	
	public final boolean getIgnoreThumbsdbFiles() {
		return this.ignoreThumbsdbFiles.getValue().booleanValue();
	}
	
	public final DomainDirectory setIgnoreThumbsdbFiles(boolean ignoreThumbsdbFiles) {
		this.ignoreThumbsdbFiles.setValue(Boolean.valueOf(ignoreThumbsdbFiles));
		return this;
	}
	
	//======================================================================
	
	public final File getFileFromRequest(String requestedFilePath, HashMap<String, String> requestArguments, ClientConnection reuse) {
		String administrateFileCheck = requestArguments.get("administrateFile");
		final boolean administrateFile = administrateFileCheck != null ? (administrateFileCheck.equals("1") || administrateFileCheck.equalsIgnoreCase("true")) : false;
		
		File homeDirectory = this.getDirectorySafe();
		File requestedFile = new File(homeDirectory, StringUtils.decodeHTML(this.replaceAliasWithPath(requestedFilePath)));
		if(requestedFilePath.equals("/")) {
			if(!administrateFile) {
				requestedFile = new File(homeDirectory, this.getDefaultFileName());
				if(!requestedFile.exists()) {
					requestedFile = homeDirectory;
				}
			} else {
				return homeDirectory;
			}
		}
		if((requestedFile == null || !requestedFile.exists())) {
			if(requestedFilePath.equalsIgnoreCase(JavaWebServer.DEFAULT_PAGE_ICON)) {
				requestedFilePath = this.getDefaultPageIcon();
				requestedFile = new File(homeDirectory, StringUtils.decodeHTML(this.replaceAliasWithPath(requestedFilePath)));
			} else if(requestedFilePath.equalsIgnoreCase(JavaWebServer.DEFAULT_STYLESHEET)) {
				File check = this.getDefaultStyleSheetFromFileSystem();
				requestedFile = check.exists() ? check : requestedFile;
			}
		}
		if(requestedFile == null || !requestedFile.exists()) {
			String alias = this.getAliasForPath(requestedFilePath);
			if(!alias.isEmpty()) {
				File oldRequestedFile = requestedFile;
				File folder = new File(alias);
				if(folder.exists()) {
					requestedFile = new File(folder, this.replaceAliasWithPath(requestedFilePath));
					if(reuse != null) {
						reuse.println("New requested file: " + requestedFile.getAbsolutePath());
						reuse.println("Requested file exists: " + requestedFile.exists());
					}
					if(!requestedFile.exists()) {
						requestedFile = oldRequestedFile;
					}
				}
			}
		}
		return requestedFile;
	}
	
	/** @param ext The file extension
	 * @return The MIME-Type for the given extension, or the
	 *         {@link MimeTypes#DEFAULT_MIME_TYPE} */
	public final String getMimeTypeForExtension(String ext) {
		ext = ext.startsWith(".") ? ext.substring(1) : ext;
		//sun.net.www.MimeTable.loadTable().getContentTypeFor("name" + ext);
		String mimeType = this.mimeTypes.get(ext);
		if(mimeType == null) {
			ext = "." + ext;
			for(Entry<String, String> entry : MimeTypes.MIME_Types.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(ext)) {
					mimeType = entry.getValue();
					break;
				}
			}
		}
		return mimeType != null ? mimeType : MimeTypes.DEFAULT_MIME_TYPE;
	}
	
	//======================================================================
	
	/** @param alias The alias to use
	 * @param path The path to be represented by the given alias
	 * @return This domain data */
	public final DomainDirectory setAlias(String alias, String path, ClientConnection reuse) {
		if(alias == null || alias.isEmpty()) {
			throw new IllegalArgumentException("Alias cannot be null or empty!");
		}
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path cannot be null or empty!");
		}
		reuse.println("\t\t*** New alias for domain \"" + this.domain + "\": " + alias + " = " + path);
		this.pathAliases.put(alias, path);
		return this;
	}
	
	/** @param alias The alias to remove
	 * @return This domain data */
	public final DomainDirectory removeAlias(String alias, ClientConnection reuse) {
		String path = this.pathAliases.get(alias);
		if(path != null && !path.isEmpty()) {
			this.pathAliases.remove(alias);
			reuse.println("*** The following alias for domain \"" + this.domain + "\" was just removed: " + alias + " = " + path);
		}
		return this;
	}
	
	/** @param alias The alias or path containing the alias to replace
	 * @return The resulting string with the first instance of the alias
	 *         replaced, if any */
	//======================================================================
	
	public final String replaceAliasWithPath(String alias) {
		for(Entry<String, String> entry : this.pathAliases.entrySet()) {
			String curAlias = entry.getKey();
			String path = entry.getValue();
			if(curAlias.equalsIgnoreCase(alias)) {
				return path;
			} else if(alias.startsWith(curAlias)) {
				return StringUtils.replaceOnce(alias, curAlias, path);
			}
		}
		return alias;
	}
	
	/** @param path The path to replace with the first matching alias
	 * @return The resulting string with the first instance of the path
	 *         defined by the found alias(see
	 *         {@link #replaceAliasWithPath(String)}) replaced with the
	 *         alias if it was found(see {@link #getAliasForPath(String)}) */
	public final String replacePathWithAlias(String path) {
		String alias = getAliasForPath(path);
		String pathFromAlias = replaceAliasWithPath(alias);
		for(String curPath : path.split("/")) {
			curPath = "/" + curPath;
			if(curPath.equalsIgnoreCase(pathFromAlias)) {
				return StringUtils.replaceOnce(path, curPath, alias);
			}
		}
		return path;//StringUtils.replaceOnce(path, pathFromAlias, alias);
	}
	
	/** @param path The path to use when searching for an alias with that
	 *            path defined
	 * @return The first alias that was found with the given path(or the
	 *         beginning of the given path) defined, if any. If no alias was
	 *         found, this returns an empty string({@code ""}). */
	public final String getAliasForPath(String path) {
		for(Entry<String, String> entry : this.pathAliases.entrySet()) {
			String alias = entry.getKey();
			String curPath = entry.getValue();
			if(curPath.equalsIgnoreCase(path)) {
				return alias;
			} else if(path.startsWith(curPath) || path.startsWith(alias)) {
				return alias;
			}
		}
		return "";
	}
	
	//=================================================================
	
	protected final File getMimeTypesFile() throws IOException {
		File mimeTypesFile = new File(getSaveFolder(), this.uuid.getValue().toString() + "_mimeTypes.yml");
		if(!mimeTypesFile.exists()) {
			mimeTypesFile.createNewFile();
		}
		return mimeTypesFile;
	}
	
	//=================================================================
	
	protected static final File getSaveFolder() {
		File saveFolder = new File(JavaWebServer.rootDir, "DomainDirectoryData");
		if(!saveFolder.exists()) {
			saveFolder.mkdirs();
		}
		return saveFolder;
	}
	
	protected final File getSaveFile() throws IOException {
		File saveFile = new File(getSaveFolder(), this.uuid.getValue().toString() + ".yml");
		if(!saveFile.exists()) {
			saveFile.createNewFile();
		}
		return saveFile;
	}
	
	/** @return Whether or not this domain data's settings were saved to file
	 *         successfully */
	public final boolean saveToFile() {
		if(this.folder.getValue() == null || this.domain.getValue() == null) {
			this.dispose();
			return false;
		}
		try {
			if(this.getDisplayLogEntries()) {
				JavaWebServer.println("\tSaving domain directory data for folder \"" + this.folder.getValue().getAbsolutePath() + "\" and domain \"" + this.domain.getValue() + "\"...");
			}
			YamlConfiguration config = new YamlConfiguration();
			config.set("dateCreated", this.dateCreated.getValue());
			config.set("lastEdited", this.lastEdited.getValue());
			config.set("folder", this.folder.getValue() != null ? this.folder.getValue().getAbsolutePath() : JavaWebServer.homeDirectory.getAbsolutePath());
			config.set("domain", this.domain.getValue());
			config.set("serverName", this.serverName.getValue());
			config.set("displayName", this.displayName.getValue());
			config.set("displayLogEntries", this.displayLogEntries.getValue());
			config.set("defaultFileName", this.defaultFileName.getValue());
			config.set("defaultFontFace", this.defaultFontFace.getValue());
			config.set("defaultPageIcon", this.defaultPageIcon.getValue());
			config.set("defaultStylesheet", this.defaultStylesheet.getValue());
			config.set("pageHeaderContent", this.pageHeaderContent.getValue());
			config.set("cacheMaxAge", this.cacheMaxAge.getValue());
			config.set("areDirectoriesForbidden", this.areDirectoriesForbidden.getValue());
			config.set("calculateDirectorySizes", this.calculateDirectorySizes.getValue());
			config.set("numberDirectoryEntries", this.numberDirectoryEntries.getValue());
			config.set("listDirectoriesFirst", this.listDirectoriesFirst.getValue());
			config.set("enableGZipCompression", this.enableGZipCompression.getValue());
			config.set("enableFileUpload", this.enableFileUpload.getValue());
			config.set("enableAlternateDirectoryListingViews", this.enableAlternateDirectoryListingViews.getValue());
			config.set("enableMediaView", this.enableMediaView.getValue());
			config.set("enableXmlListView", this.enableXmlListView.getValue());
			config.set("enableFilterView", this.enableFilterView.getValue());
			config.set("enableSortViews", this.enableSortView.getValue());
			config.set("enableVLCPlaylistView", this.enableVLCPlaylistView.getValue());
			config.set("enableReadableFileViews", this.enableReadableFileViews.getValue());
			config.set("ignoreThumbsdbFiles", this.ignoreThumbsdbFiles.getValue());
			
			ConfigurationSection root = config.getConfigurationSection("pathAliases");
			if(root == null) {
				root = config.createSection("pathAliases");
			}
			for(Entry<String, String> entry : this.pathAliases.entrySet()) {
				root.set(entry.getKey(), entry.getValue());
			}
			config.save(this.getSaveFile());
			
			try {
				YamlConfiguration mConfig = new YamlConfiguration();
				for(Entry<String, String> entry : this.mimeTypes.entrySet()) {
					mConfig.set(entry.getKey(), entry.getValue());
				}
				mConfig.save(this.getMimeTypesFile());
			} catch(Throwable e) {
				e.printStackTrace();
			}
			if(this.getDisplayLogEntries()) {
				JavaWebServer.println("\t\tDomain directory data successfully saved for domain \"" + this.domain.getValue() + "\".");
			}
			return true;
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
		/*try {
			File file = this.getSaveFile();
			PrintWriter pr = new PrintWriter(file);
			pr.println("folder=" + this.folder.getAbsolutePath());
			pr.println("domain=" + this.domain);
			pr.println("serverName=" + this.serverName);
			pr.println("defaultFileName=" + this.defaultFileName);
			pr.println("defaultFontFace=" + this.defaultFontFace);
			pr.println("defaultPageIcon=" + this.defaultPageIcon);
			pr.println("defaultStylesheet=" + this.defaultStylesheet);
			pr.println("cacheMaxAge=" + this.cacheMaxAge);
			if(!this.pathAliases.isEmpty()) {
				pr.println("\r\naliases:");
				for(Entry<String, String> entry : this.pathAliases.entrySet()) {
					pr.println("---" + entry.getKey() + "=" + entry.getValue());
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
	
	/** @return Whether or not this domain data's settings were loaded from
	 *         file successfully */
	public final boolean loadFromFile() {
		try {
			YamlConfiguration config = new YamlConfiguration();
			File file = this.getSaveFile();
			config.load(file);
			this.displayLogEntries.setValue(Boolean.valueOf(config.getBoolean("displayLogEntries", this.displayLogEntries.getValue().booleanValue())));
			if(this.getDisplayLogEntries()) {
				JavaWebServer.println("\tLoading domain directory data from file \"" + file.getName() + "\"...");
			}
			this.dateCreated.setValue(Long.valueOf(config.getLong("dateCreated", this.dateCreated.getValue().longValue())));
			this.lastEdited.setValue(Long.valueOf(config.getLong("lastEdited", this.dateCreated.getValue().longValue())));
			this.folder.setValue(new File(config.getString("folder", this.folder.getValue() != null ? this.folder.getValue().getAbsolutePath() : JavaWebServer.homeDirectory.getAbsolutePath())));
			this.domain.setValue(config.getString("domain", this.domain.getValue()));
			this.serverName.setValue(config.getString("serverName", this.serverName.getValue()));
			this.displayName.setValue(config.getString("displayName", (this.displayName.getValue() == null || this.displayName.getValue().trim().isEmpty()) ? this.domain.getValue() : this.displayName.getValue()));
			this.defaultFileName.setValue(config.getString("defaultFileName", this.defaultFileName.getValue()));
			this.defaultFontFace.setValue(config.getString("defaultFontFace", this.defaultFontFace.getValue()));
			this.defaultPageIcon.setValue(config.getString("defaultPageIcon", this.defaultPageIcon.getValue()));
			this.defaultStylesheet.setValue(config.getString("defaultStylesheet", this.defaultStylesheet.getValue()));
			this.pageHeaderContent.setValue(config.getString("pageHeaderContent", this.pageHeaderContent.getValue()));
			this.cacheMaxAge.setValue(Long.valueOf(config.getLong("cacheMaxAge", this.cacheMaxAge.getValue().longValue())));
			this.areDirectoriesForbidden.setValue(Boolean.valueOf(config.getBoolean("areDirectoriesForbidden", this.areDirectoriesForbidden.getValue().booleanValue())));
			this.calculateDirectorySizes.setValue(Boolean.valueOf(config.getBoolean("calculateDirectorySizes", this.calculateDirectorySizes.getValue().booleanValue())));
			this.numberDirectoryEntries.setValue(Boolean.valueOf(config.getBoolean("numberDirectoryEntries", this.numberDirectoryEntries.getValue().booleanValue())));
			this.listDirectoriesFirst.setValue(Boolean.valueOf(config.getBoolean("listDirectoriesFirst", this.listDirectoriesFirst.getValue().booleanValue())));
			this.enableGZipCompression.setValue(Boolean.valueOf(config.getBoolean("enableGZipCompression", this.enableGZipCompression.getValue().booleanValue())));
			this.enableFileUpload.setValue(Boolean.valueOf(config.getBoolean("enableFileUpload", this.enableFileUpload.getValue().booleanValue())));
			this.enableAlternateDirectoryListingViews.setValue(Boolean.valueOf(config.getBoolean("enableAlternateDirectoryListingViews", this.enableAlternateDirectoryListingViews.getValue().booleanValue())));
			this.enableMediaView.setValue(Boolean.valueOf(config.getBoolean("enableMediaView", this.enableMediaView.getValue().booleanValue())));
			this.enableXmlListView.setValue(Boolean.valueOf(config.getBoolean("enableXmlListView", this.enableXmlListView.getValue().booleanValue())));
			this.enableFilterView.setValue(Boolean.valueOf(config.getBoolean("enableFilterView", this.enableFilterView.getValue().booleanValue())));
			this.enableSortView.setValue(Boolean.valueOf(config.getBoolean("enableSortViews", this.enableSortView.getValue().booleanValue())));
			this.enableVLCPlaylistView.setValue(Boolean.valueOf(config.getBoolean("enableVLCPlaylistView", this.enableVLCPlaylistView.getValue().booleanValue())));
			this.enableReadableFileViews.setValue(Boolean.valueOf(config.getBoolean("enableReadableFileViews", this.enableReadableFileViews.getValue().booleanValue())));
			this.ignoreThumbsdbFiles.setValue(Boolean.valueOf(config.getBoolean("ignoreThumbsdbFiles", this.ignoreThumbsdbFiles.getValue().booleanValue())));
			ConfigurationSection root = config.getConfigurationSection("pathAliases");
			if(root != null) {
				this.pathAliases.clear();
				for(String key : root.getKeys(false)) {
					String value = root.getString(key);
					this.pathAliases.put(key, value);
				}
			}
			try {
				YamlConfiguration mConfig = new YamlConfiguration();
				mConfig.load(this.getMimeTypesFile());
				
				this.mimeTypes.clear();
				for(String key : mConfig.getKeys(false)) {
					if(key != null) {
						String value = mConfig.getString(key);
						key = (key.startsWith(".") ? "" : ".") + key;
						if(value != null) {
							this.mimeTypes.put(key, value);
						}
					}
				}
			} catch(Throwable ignored) {
			}
			
			if(this.folder.getValue() != null && this.folder.getValue().exists()) {
				if(this.getDisplayLogEntries()) {
					JavaWebServer.println("\t\tDomain directory data successfully loaded for folder \"" + this.folder.getValue().getAbsolutePath() + "\" and domain \"" + this.domain.getValue() + "\"!");
				}
				try {
					String fileName = FilenameUtils.getName(this.getDefaultPageIcon());
					File favicon = new File(this.getDirectorySafe(), fileName);
					if(!favicon.exists()) {
						ResourceFactory.getResourceFromStreamAsFile(this.getDirectorySafe(), "textures/icons/favicon.ico", fileName);
						if(favicon.exists()) {
							if(this.getDisplayLogEntries()) {
								JavaWebServer.println("\t\t\tCreated default page icon for domain \"" + this.domain.getValue() + "\"\r\n\t\t\tat: \"" + favicon.getAbsolutePath() + "\".");
							}
						}
					}
				} catch(Throwable ignored) {
				}
				File styleSheet = this.getDefaultStyleSheetFromFileSystem();
				if(styleSheet != null && !styleSheet.exists()) {
					ResourceFactory.getResourceFromStreamAsFile(styleSheet.getParentFile(), "files/layout.css", styleSheet.getName());
					/*try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(styleSheet), "UTF-8"), true)) {
						pr.println("* {\r\n" + "margin: 0;\r\n" + "}\r\n" + "html, body {\r\n" + "height: 100%;\r\n" + "}\r\n" + ".wrapper {\r\n" + "min-height: 100%;\r\n" + "height: auto !important;\r\n" + "height: 100%;\r\n" + "margin: 0 auto -4em;\r\n" + "}");
					} catch(Throwable ignored) {
					}*/
					if(styleSheet.exists()) {
						if(this.getDisplayLogEntries()) {
							JavaWebServer.println("\t\t\tCreated default stylesheet for domain \"" + this.domain.getValue() + "\"\r\n\t\t\tat: \"" + styleSheet.getAbsolutePath() + "\".");
						}
					}
				}
			}
			JavaWebServer.println("\tLoaded domain \"" + this.getDisplayName() + "\" from file!");
			return true;
		} catch(Throwable ignored) {
			JavaWebServer.println("\tFailed to load domain from file \"" + this.uuid.getValue().toString() + ".yml\"!");
			return false;
		}
		
		/*BufferedReader br = null;
		try {
			File file = this.getSaveFile();
			println("\tLoading domain directory data from file \"" + file.getAbsolutePath() + "\"...");
			br = new BufferedReader(new FileReader(file));
			this.pathAliases.clear();
			while(br.ready()) {
				String line = br.readLine();
				String[] args = line.split("=");
				if(args.length == 2 && !line.startsWith("---")) {
					if(args[0].equalsIgnoreCase("folder")) {
						this.folder = new File(args[1]);
					} else if(args[0].equalsIgnoreCase("domain")) {
						this.domain = args[1];
					} else if(args[0].equalsIgnoreCase("serverName")) {
						this.serverName = args[1];
					} else if(args[0].equalsIgnoreCase("defaultFileName")) {
						this.defaultFileName = args[1];
					} else if(args[0].equalsIgnoreCase("defaultFontFace")) {
						this.defaultFontFace = args[1];
					} else if(args[0].equalsIgnoreCase("defaultPageIcon")) {
						this.defaultPageIcon = args[1];
					} else if(args[0].equalsIgnoreCase("defaultStylesheet")) {
						this.defaultStylesheet = args[1];
					} else if(args[0].equalsIgnoreCase("cacheMaxAge")) {
						this.cacheMaxAge = args[1];
					} else if(!args[0].equalsIgnoreCase("aliases:")) {
						println("\tIgnoring malformed line: \"" + line + "\"");
					}
				} else {
					if(line.startsWith("---") && line.length() > 3) {
						String[] alias = line.replace("---", "").split("=");
						if(alias.length == 2) {
							this.setAlias(alias[0], alias[1]);
						} else {
							println("\tIgnoring malformed line: \"" + line + "\"");
						}
					} else if(!args[0].equalsIgnoreCase("aliases:") && !line.isEmpty()) {
						println("\tIgnoring malformed line: \"" + line + "\"");
					}
				}
			}
			if(this.folder != null && this.folder.exists()) {
				println("\t\tDomain directory data loaded for folder \"" + this.folder.getAbsolutePath() + "\" and domain \"" + this.domain + "\".");
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
	
	/** @return An arraylist containing all of the domain data that was
	 *         loaded from file */
	public static final ArrayList<DomainDirectory> loadAllDomainDirectoryDataFromFile() {
		final ArrayList<DomainDirectory> rtrn = new ArrayList<>();
		File saveFolder = getSaveFolder();
		for(String fileName : saveFolder.list()) {
			File file = new File(saveFolder, fileName);
			if(file.exists() && FilenameUtils.getExtension(file.getAbsolutePath()).equals("yml")) {
				String uuidStr = FilenameUtils.getBaseName(file.getAbsolutePath());
				if(Functions.isStrUUID(uuidStr)) {
					DomainDirectory check = null;
					UUID uuid = UUID.fromString(uuidStr);
					for(DomainDirectory curDomainDir : getInstances()) {
						if(curDomainDir.uuid.toString().equals(uuid.toString())) {
							check = curDomainDir;
							break;
						}
					}
					if(check == null) {
						DomainDirectory domainDirectory = new DomainDirectory(uuid);
						if(domainDirectory.loadFromFile()) {
							if(getDomainDirectoryFromDomainName(domainDirectory.getDomain()) == null) {
								rtrn.add(domainDirectory);
								instances.add(domainDirectory);
							} else {
								PrintUtil.println(" /!\\ \tWarning!\n/___\\\tDuplicate domain directory data detected: \"" + domainDirectory.toString() + "\"!");
							}
						}
					} else {
						check.loadFromFile();
						if(getDomainDirectoryFromDomainName(check.getDomain()) != null) {
							PrintUtil.println(" /!\\ \tWarning!\n/___\\\tDuplicate domain directory data detected: \"" + check.toString() + "\"!");
						} else {
							PrintUtil.println(" /!\\ \tWarning!\n/___\\\tThe following domain directory data uses a UUID that is already taken: \"" + check.toString() + "\"!");
						}
					}
				}
			}
		}
		return rtrn;
	}
	
	/** Saves all of the domain data currently in use */
	public static final void saveAllDomainDirectoryDataToFile() {
		if(!instances.isEmpty()) {
			JavaWebServer.println("Saving all domain directory data...");
			for(DomainDirectory res : getInstances()) {
				res.saveToFile();
			}
			JavaWebServer.println("Domain directory data has been saved.");
		}
	}
	
	/** Calls {@link #dispose()} and then attempts to delete any associated save
	 * files from disk. */
	@Override
	public final void delete() {
		this.dispose();
		try {
			this.getSaveFile().delete();
			this.getMimeTypesFile().delete();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Disposes of this domain data and removes it from memory, thereby
	 * rendering it useless */
	@Override
	public final void dispose() {
		this.remove();
		this.folder.setValue(null);
		this.domain.setValue(null);
		this.displayName.setValue(null);
	}
	
	public final void add() {
		if(instances.contains(this)) {
			throw new IllegalStateException();
		}
		instances.add(this);
	}
	
	public final void remove() {
		instances.remove(this);
	}
	
	/** @return A temporary Domain object */
	public static final DomainDirectory getTemporaryDomain() {
		DomainDirectory temp = new DomainDirectory(UUID.randomUUID(), JavaWebServer.homeDirectory, "TEMPORARY_DOMAIN", JavaWebServer.SERVER_NAME, JavaWebServer.DEFAULT_FILE_NAME, JavaWebServer.defaultFontFace, JavaWebServer.DEFAULT_PAGE_ICON, JavaWebServer.DEFAULT_STYLESHEET);
		temp.isTemporary = true;
		temp.remove();
		return temp;
	}
	
	/** @return */
	public String getURLPathPrefix() {
		return FilenameUtils.normalize(FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(this.getDirectorySafe().getAbsolutePath() + File.separatorChar)).replace('\\', '/'));
	}
	
	@Override
	public final String toString() {
		new Throwable().printStackTrace();
		return this.domain.getValue() + "(" + this.getUUID() + "): " + this.getDirectorySafe().getAbsolutePath();
	}
	
}
