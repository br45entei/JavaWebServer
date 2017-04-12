package com.gmail.br45entei;

import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.server.data.RestrictedFile;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.MapUtil;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public final class FileSortData {
	
	/** @param mimeType The MIME type to test
	 * @return Whether or not the MIME type is that of an audio file. */
	public static final boolean isMimeTypeAudio(String mimeType) {
		return mimeType.toLowerCase().startsWith("audio/") || mimeType.equalsIgnoreCase("application/ogg");
	}
	
	/** @param mimeType The MIME type to test
	 * @return Whether or not the MIME type is that of a video file. */
	public static final boolean isMimeTypeVideo(String mimeType) {
		return mimeType.toLowerCase().startsWith("video/");
	}
	
	/** @param mimeType The MIME type to test
	 * @return Whether or not the mime type is considered to be 'media'. */
	public static final boolean isMimeTypeMedia(String mimeType) {
		return isMimeTypeAudio(mimeType) || isMimeTypeVideo(mimeType);
	}
	
	public final String filter;
	public final ArrayList<String> filterExts;
	public final ArrayList<String> filterNonExts;
	public final String sort;
	public final boolean wasSortNull;
	public final boolean isSortReversed;
	public final boolean useSortView;
	public final HashMap<Integer, String> files;
	private final ArrayList<Integer> filePaths;
	private final ArrayList<Integer> folderPaths;
	public final long random;
	public final String options;
	public final boolean containsAnyArgs;
	public final boolean areThereFiles;
	public final boolean areThereDirectories;
	public final boolean areThereMediaFiles;
	public final boolean areThereImageFiles;
	public final boolean areThereTextFiles;
	
	private FileSortData() {
		this(null, -1L, null, null, null, 0, null, new ArrayList<>(), new ArrayList<>(), null, false, false, false, null, false, false, false, false, false, false);
	}
	
	public static final FileSortData connectionSevered = new FileSortData();
	
	public static final FileSortData sort(final Socket s, final ClientConnection reuse, final File requestedFolder, final DomainDirectory domainDirectory, final String fileLink, final HashMap<String, String> requestArguments, final boolean includeFolders) {
		final long startTime = System.currentTimeMillis();
		final String clientIPAddress = AddressUtil.getClientAddressNoPort(s);//s.getInetAddress().getHostAddress();
		final String pathPrefix = domainDirectory.getURLPathPrefix();//FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
		String path = FileInfo.getURLPathFor(domainDirectory, requestedFolder);
		/*String path = requestedFolder.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/');
		path = (path.trim().isEmpty() ? "/" : path);
		path = (path.startsWith("/") ? "" : "/") + path;*/
		final String filter = requestArguments.get("filter");
		String sort = requestArguments.get("sort");
		boolean wasSortNull = false;
		if(sort == null) {
			sort = "fileNames";
			wasSortNull = true;
		}
		final boolean isSortReversed = sort.startsWith("-");
		sort = isSortReversed && sort.length() > 1 ? sort.substring(1) : sort;
		final boolean useSortView = (domainDirectory.getEnableSortView() && !sort.equals("fileNames"));
		final HashMap<Integer, String> files = new HashMap<>();
		final String[] c = requestedFolder.list();
		boolean areThereFiles = false;
		boolean areThereDirectories = false;
		boolean areThereMediaFiles = false;
		boolean areThereImageFiles = false;
		boolean areThereTextFiles = false;
		final boolean ignoreThumbsdbFiles = domainDirectory.getIgnoreThumbsdbFiles();
		//XXX Filtering =====================================================================================================================================
		String options = "";
		ArrayList<String> filterExts = new ArrayList<>();
		ArrayList<String> filterNonExts = new ArrayList<>();
		if(filter != null && !filter.isEmpty()) {
			String[] split = filter.split("\\,");
			for(String ext : split) {
				if(ext.trim().isEmpty()) {
					continue;
				}
				if(ext.startsWith("-") && ext.length() > 1) {
					filterNonExts.add(ext.substring(1));
				} else if(!ext.startsWith("-")) {
					filterExts.add(ext);
				}
			}
		}
		final ArrayList<Integer> directories = new ArrayList<>();//just used to restore folderPaths in case the folders are wiped out(i.e. when "sort=numbers")
		if(c != null) {
			reuse.printlnDebug("\t\t--- SORTING: c != null;\nfilterExts: " + StringUtil.stringArrayToString(' ', filterExts) + "\nfilterNonExts: " + StringUtil.stringArrayToString(' ', filterNonExts));
			int j = 0;
			for(int i = 0; i < c.length;) {
				String fileName = c[i];
				File file = new File(requestedFolder, fileName);
				/*if(!FileUtil.isFileAccessible(file)) {
					continue;
				}*/
				boolean isHidden = RestrictedFile.isHidden(file);
				if(isHidden) {
					Boolean check = RestrictedFile.isIPAllowedFor(clientIPAddress, file);
					isHidden = check == null ? false : !check.booleanValue();
				}
				if(file.exists() && !isHidden) {
					if(ignoreThumbsdbFiles && file.isFile() && file.getName().equalsIgnoreCase("thumbs.db")) {
						j++;
						i++;
						continue;
					}
					reuse.printlnDebug("\t\t--- SORTING: file \"" + fileLink + "/" + file.getName() + "\" exists!");
					Integer key = Integer.valueOf(j);
					files.put(key, fileName);
					areThereFiles |= file.isFile();
					areThereDirectories |= file.isDirectory();
					if(file.isDirectory()) {
						directories.add(key);
					}
					final String ext = FilenameUtils.getExtension(file.getAbsolutePath());
					final String mimeType = domainDirectory.getMimeTypeForExtension(ext).toLowerCase();
					if(isMimeTypeMedia(mimeType)) {
						areThereMediaFiles = true;
					} else if(mimeType.startsWith("image/")) {
						areThereImageFiles = true;
					} else if(mimeType.startsWith("text/")) {
						areThereTextFiles = true;
					}
					j++;
				} else {
					reuse.printlnDebug("\t\t--- SORTING: file \"" + fileLink + "/" + file.getName() + "\" either doesn't exist or is hidden; not showing it! client ip address: " + clientIPAddress + "; old: " + s.getInetAddress().getHostAddress());//XXX Debug!
				}
				i++;
			}
		}
		//ArrayList<String> files = c != null ? new ArrayList<>(Arrays.asList(c)) : new ArrayList<String>();
		String requestArgs = "?";
		boolean containsAnyArgs = false;
		for(Entry<String, String> entry : requestArguments.entrySet()) {//XXX (get args without filter str)
			boolean add = true;
			if(entry.getKey().toLowerCase().equals("filter") && domainDirectory.getEnableFilterView()) {
				add = false;
			}
			if(add) {
				requestArgs += (containsAnyArgs ? "&" : "") + entry.getKey() + "=" + entry.getValue();
				containsAnyArgs = true;
			}
		}
		final String fileLinkAndReq = fileLink + (containsAnyArgs ? requestArgs : "");
		final String requestArgsAppendChar = (requestArgs.equals("?") ? requestArgs : "&");
		//XXX File Sorting ==================================================================================================================================
		boolean alreadyReversedFilePaths = false;
		long random = -1L;
		final ArrayList<Integer> filePaths = new ArrayList<>();
		final ArrayList<Integer> folderPaths = new ArrayList<>();
		if(useSortView) {// && !sort.equalsIgnoreCase("fileNames")) {
			reuse.printlnDebug("\t\t--- SORTING: useSortView && !sort.equalsIgnoreCase(\"fileNames\")");
			if(sort.equalsIgnoreCase("sizes") || sort.equalsIgnoreCase("dates") || sort.equalsIgnoreCase("types")) {
				reuse.printlnDebug("\t\t--- SORTING: sort.equalsIgnoreCase(\"sizes\") || sort.equalsIgnoreCase(\"dates\") || sort.equalsIgnoreCase(\"types\")");
				HashMap<Integer, FileInfo> fileInfos = new HashMap<>();
				for(Entry<Integer, String> entry : files.entrySet()) {
					//CodeUtil.sleep(10L);
					Integer i = entry.getKey();
					String filePath = entry.getValue();
					if(s.isClosed()) {
						return connectionSevered;
					}
					if(filePath == null || filePath.isEmpty()) {
						continue;
					}
					try {
						String newPath = path + "/" + filePath;
						newPath = newPath.startsWith("//") ? newPath.substring(1) : newPath;
						File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
						if(file.exists()) {
							fileInfos.put(i, new FileInfo(file, domainDirectory));//used for sorting
						}
					} catch(FileNotFoundException ignored) {
					} catch(Throwable e) {
						e.printStackTrace();
					}
				}
				filePaths.clear();
				while(!fileInfos.isEmpty()) {
					if(sort.equalsIgnoreCase("sizes")) {
						filePaths.add(MapUtil.getSmallestFileInfoKeyInHashMapAndRemoveIt(fileInfos));
					} else if(sort.equalsIgnoreCase("dates")) {
						filePaths.add(MapUtil.getOldestFileInfoKeyInHashMapAndRemoveIt(fileInfos));
					} else if(sort.equalsIgnoreCase("types")) {
						filePaths.add(MapUtil.getLargestMimeTypeAlphabeticallyFromFileInfoKeyInHashMapAndRemoveIt(fileInfos));
					}
				}
			} else if(sort.equalsIgnoreCase("numbers")) {
				reuse.printlnDebug("\t\t--- SORTING: sort.equalsIgnoreCase(\"numbers\")");
				filePaths.addAll(files.keySet());
			} else if(sort.equalsIgnoreCase("random")) {
				reuse.printlnDebug("\t\t--- SORTING: sort.equalsIgnoreCase(\"random\")");
				filePaths.addAll(files.keySet());
				random = StringUtil.shuffle(filePaths);
			} else if(sort.startsWith("random_") && sort.length() > 7) {
				reuse.printlnDebug("\t\t--- SORTING: sort.startsWith(\"random_\") && sort.length() > 7 --> (" + sort + ")");
				filePaths.addAll(files.keySet());
				String getRandom = sort.substring(7);
				if(StringUtil.isStrLong(getRandom)) {
					random = Long.valueOf(getRandom).longValue();
				} else {
					random = StringUtil.hash(getRandom);
				}
				StringUtil.shuffle(filePaths, random);
			}
			if((!sort.equalsIgnoreCase("types") && isSortReversed) || (sort.equalsIgnoreCase("types") && !isSortReversed)) {
				reuse.printlnDebug("\t\t--- SORTING: (!sort.equalsIgnoreCase(\"types\") && isSortReversed) || (sort.equalsIgnoreCase(\"types\") && !isSortReversed)");
				alreadyReversedFilePaths = true;
				Collections.reverse(filePaths);
			}
		} else {
			reuse.printlnDebug("\t\t--- SORTING: else from useSortView && !sort.equalsIgnoreCase(\"fileNames\")");
			filePaths.addAll(files.keySet());
		}
		if(includeFolders && domainDirectory.getListDirectoriesFirst() && sort.equalsIgnoreCase("fileNames") && ((filterExts.isEmpty() || StringUtil.containsIgnoreCase(filterExts, "directory")) && (filterNonExts.isEmpty() || !StringUtil.containsIgnoreCase(filterNonExts, "directory")))) {//XXX Sorting folders in order
			reuse.printlnDebug("\t\t--- SORTING: includeFolders && domainDirectory.getListDirectoriesFirst()");
			for(Entry<Integer, String> entry : files.entrySet()) {
				Integer i = entry.getKey();
				String filePath = entry.getValue();
				if(s.isClosed()) {
					return connectionSevered;
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				try {
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						if(file.isDirectory()) {
							folderPaths.add(i);
							filePaths.remove(i);
						} else {
							//filePaths.add(i);
						}
					}
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
			//for(String filePath : folderPaths) {
			//	filesInOrder.add(folderPath);
			//}
			//for(String filePath : filePaths) {
			//	filesInOrder.add(filePath);
			//}
		}
		//if(domainDirectory.getListDirectoriesFirst() && includeFolders && folderPaths.isEmpty() && !StringUtil.containsIgnoreCase(filterNonExts, "directory")) {
		//	folderPaths.addAll(directories);
		//}
		if(sort.equalsIgnoreCase("fileNames") && isSortReversed) {
			reuse.printlnDebug("\t\t--- SORTING: sort.equalsIgnoreCase(\"fileNames\") && isSortReversed");
			ArrayList<Integer> folderPathsCopy = new ArrayList<>(folderPaths);
			ArrayList<Integer> filePathsCopy = new ArrayList<>(filePaths);
			folderPaths.clear();
			filePaths.clear();
			Collections.reverse(folderPathsCopy);
			if(!alreadyReversedFilePaths) {
				alreadyReversedFilePaths = true;
				Collections.reverse(filePathsCopy);
			}
			if(domainDirectory.getListDirectoriesFirst()) {
				folderPaths.addAll(filePathsCopy);
				filePaths.addAll(folderPathsCopy);
			} else {
				filePaths.addAll(filePathsCopy);
			}
		}
		if(!domainDirectory.getListDirectoriesFirst()) {
			folderPaths.clear();
		}
		
		ArrayList<String> extensions = new ArrayList<>();
		for(Entry<Integer, String> entry : files.entrySet()) {
			String filePath = entry.getValue();
			if(filePath != null) {
				final String ext = FilenameUtils.getExtension(filePath).trim().toLowerCase();
				String newPath = path + "/" + filePath;
				if(newPath.startsWith("//")) {
					newPath = newPath.substring(1);
				}
				File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
				if(file.exists()) {
					Boolean addFilePath = null;
					final String mimeType = domainDirectory.getMimeTypeForExtension(ext).toLowerCase();
					final boolean isMediaFile = isMimeTypeMedia(mimeType);
					final boolean isImageFile = mimeType.startsWith("image/");
					final boolean isTextFile = mimeType.startsWith("text/");
					if(domainDirectory.getEnableFilterView()) {
						if(!filterNonExts.isEmpty()) {
							if(isTextFile && !ext.isEmpty() && StringUtil.containsIgnoreCase(filterNonExts, "textFiles")) {
								addFilePath = Boolean.FALSE;
							} else if(isImageFile && !ext.isEmpty() && StringUtil.containsIgnoreCase(filterNonExts, "imageFiles")) {
								addFilePath = Boolean.FALSE;
							} else if(isMediaFile && !ext.isEmpty() && StringUtil.containsIgnoreCase(filterNonExts, "mediaFiles")) {
								addFilePath = Boolean.FALSE;
							}
							if(!ext.isEmpty() && file.isFile() && StringUtil.containsIgnoreCase(filterNonExts, ext)) {
								addFilePath = Boolean.FALSE;
							}
						}
						if(addFilePath != Boolean.FALSE && !filterExts.isEmpty()) {
							if(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, ext)) {
								if(file.exists() ? file.isFile() : includeFolders) {
									addFilePath = Boolean.TRUE;
								}
							} else if(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "mediaFiles") && isMediaFile) {
								addFilePath = Boolean.TRUE;
							} else if(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "imageFiles") && isImageFile) {
								addFilePath = Boolean.TRUE;
							} else if(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "textFiles") && isTextFile) {
								addFilePath = Boolean.TRUE;
							} else if(StringUtil.containsIgnoreCase(filterExts, "directory") && file.isDirectory()) {
								addFilePath = Boolean.TRUE;
							} /* else {
								if(!ext.isEmpty() && !StringUtil.containsIgnoreCase(filterNonExts, ext)) {
									if(file.exists() ? file.isFile() : true) {
										addFilePath = Boolean.TRUE;
									}
								}
								if(!StringUtil.containsIgnoreCase(filterNonExts, "directory") && file.isDirectory()) {
									addFilePath = Boolean.TRUE;
								}
								}*/
						}
						/*if(StringUtil.containsIgnoreCase(filterExts, ext) && (file.exists() ? file.isFile() : includeFolders)) {
							files.add(filePath);
						} else if(StringUtil.containsIgnoreCase(filterExts, "directory") && (file.exists() && file.isDirectory())) {
							files.add(filePath);
						}*/
					} else {
						addFilePath = Boolean.TRUE;
					}
					if(addFilePath == Boolean.TRUE) {
						if(domainDirectory.getEnableFilterView() && file.isFile() && !ext.isEmpty() && StringUtil.containsIgnoreCase(filterNonExts, ext)) {
							filePaths.remove(entry.getKey());
						} else {
							if(!extensions.contains(ext)) {// && !ext.equals(filter)) {
								if(file.isFile() && !ext.isEmpty()) {
									extensions.add(ext);
									if(filter == null || !StringUtil.containsIgnoreCase(filterExts, ext)) {
										options += "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=" + ext) + "\">" + ext + "</option>\r\n";
									}
								}
							}
						}
					} else if(addFilePath == null) {
						if(!filterExts.isEmpty()) {
							if((!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "mediaFiles") && !isMediaFile) || //
									(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "imageFiles") && !isImageFile) || //
									(!ext.isEmpty() && StringUtil.containsIgnoreCase(filterExts, "textFiles") && !isTextFile) || //
									(StringUtil.containsIgnoreCase(filterExts, "directory") && !file.isDirectory()) || //
									(!StringUtil.containsIgnoreCase(filterExts, "directory") && file.isDirectory()) || //
									(!ext.isEmpty() && file.isFile() && !StringUtil.containsIgnoreCase(filterExts, ext)) || //
									(!ext.isEmpty() && file.isFile() && StringUtil.containsIgnoreCase(filterNonExts, ext))) {
								filePaths.remove(entry.getKey());
							} else {
								reuse.printlnDebug("\t\t\t--- SORTING: [!!42]: filterPaths: " + StringUtil.stringArrayToString(' ', filterExts) + "; ext: " + ext + "; isMediaFile: " + isMediaFile + "; DIRECTORY: " + file.isDirectory() + "; isImageFile: " + isImageFile + "; isTextFile: " + isTextFile + ";");
							}
						} else {
							if(domainDirectory.getEnableFilterView() && file.isFile() && !ext.isEmpty() && StringUtil.containsIgnoreCase(filterNonExts, ext)) {
								filePaths.remove(entry.getKey());
							}
							if(!extensions.contains(ext)) {// && !ext.equals(filter)) {
								if(file.isFile() && !ext.isEmpty()) {
									extensions.add(ext);
									if(filter == null || !StringUtil.containsIgnoreCase(filterExts, ext)) {
										options += "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=" + ext) + "\">" + ext + "</option>\r\n";
									}
								}
							}
						}
					} else {
						filePaths.remove(entry.getKey());
					}
					reuse.printlnDebug("\t\t\t--- SORTING: filterPaths: " + StringUtil.stringArrayToString(' ', filterExts) + "; ext: " + ext + "; isMediaFile: " + isMediaFile + "; DIRECTORY: " + file.isDirectory() + "; isImageFile: " + isImageFile + "; isTextFile: " + isTextFile + ";");
					reuse.printlnDebug("\t\t\t\tfilePaths.contains(entry.getKey()): " + Boolean.valueOf(filePaths.contains(entry.getKey())) + "; folderPaths.contains(entry.getKey()): " + Boolean.valueOf(folderPaths.contains(entry.getKey())) + ";");
				}
			}
		}
		if(!options.isEmpty() || areThereDirectories || areThereFiles) {
			final String dirOption = (areThereDirectories || StringUtil.containsIgnoreCase(filterExts, "directory")) ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=DIRECTORY") + "\">(Folders)</option>\r\n" : "";
			final String mediaOption = (areThereMediaFiles || StringUtil.containsIgnoreCase(filterExts, "mediaFiles")) ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=mediaFiles") + "\">(Media Files)</option>\r\n" : "";
			final String imageOption = (areThereImageFiles || StringUtil.containsIgnoreCase(filterExts, "imageFiles")) ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=imageFiles") + "\">(Image Files)</option>\r\n" : "";
			final String textOption = (areThereTextFiles || StringUtil.containsIgnoreCase(filterExts, "textFiles")) ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=textFiles") + "\">(Text Files)</option>\r\n" : "";
			if(!StringUtil.containsIgnoreCase(filterExts, "textFiles")) {
				options = textOption + options;
			}
			if(!StringUtil.containsIgnoreCase(filterExts, "imageFiles")) {
				options = imageOption + options;
			}
			if(!StringUtil.containsIgnoreCase(filterExts, "mediaFiles")) {
				options = mediaOption + options;
			}
			if(!StringUtil.containsIgnoreCase(filterExts, "directory")) {
				options = dirOption + options;
			}
			options = "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=") + "\">(No filter)</option>\r\n" + options;
			if((filter != null && !filter.trim().isEmpty()) && ((filter.contains(",") || filter.contains("-")) || (!StringUtil.containsIgnoreCase(filterExts, "directory") && !StringUtil.containsIgnoreCase(filterExts, "mediaFiles") && !StringUtil.containsIgnoreCase(filterExts, "imageFiles") && !StringUtil.containsIgnoreCase(filterExts, "textFiles")))) {
				options = "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=" + filter) + "\">" + filter + "</option>\r\n" + options;
			}
			if(StringUtil.containsIgnoreCase(filterExts, "textFiles")) {
				options = textOption + options;
			}
			if(StringUtil.containsIgnoreCase(filterExts, "imageFiles")) {
				options = imageOption + options;
			}
			if(StringUtil.containsIgnoreCase(filterExts, "mediaFiles")) {
				options = mediaOption + options;
			}
			if(StringUtil.containsIgnoreCase(filterExts, "directory")) {
				options = dirOption + options;
			}
		}
		if(domainDirectory.getEnableFilterView()) {
			HashMap<Integer, String> filesBackup = new HashMap<>(files);
			files.clear();
			for(Integer index : folderPaths) {
				files.put(index, filesBackup.get(index));
			}
			for(Integer index : filePaths) {
				files.put(index, filesBackup.get(index));
			}
		}
		return new FileSortData(reuse, startTime, files, filePaths, folderPaths, random, filter, filterExts, filterNonExts, sort, wasSortNull, isSortReversed, useSortView, options, containsAnyArgs, areThereFiles, areThereDirectories, areThereMediaFiles, areThereImageFiles, areThereTextFiles);
	}
	
	public FileSortData(final ClientConnection reuse, final long startTime, HashMap<Integer, String> files, ArrayList<Integer> filePaths, ArrayList<Integer> folderPaths, long random, String filter, ArrayList<String> filterExts, ArrayList<String> filterNonExts, String sort, boolean wasSortNull, boolean isSortReversed, boolean useSortView, String options, boolean containsAnyArgs, boolean areThereFiles, boolean areThereDirectories, boolean areThereMediaFiles, boolean areThereImageFiles, boolean areThereTextFiles) {
		this.files = files;
		this.filePaths = filePaths;
		this.folderPaths = folderPaths;
		this.random = random;
		this.filter = filter;
		this.filterExts = filterExts;
		this.filterNonExts = filterNonExts;
		this.sort = sort;
		this.wasSortNull = wasSortNull;
		this.isSortReversed = isSortReversed;
		this.useSortView = useSortView;
		this.options = options;
		this.containsAnyArgs = containsAnyArgs;
		this.areThereFiles = areThereFiles;
		this.areThereDirectories = areThereDirectories;
		this.areThereMediaFiles = areThereMediaFiles;
		this.areThereImageFiles = areThereImageFiles;
		this.areThereTextFiles = areThereTextFiles;
		if(startTime != -1L) {
			reuse.println("\t\t--- File sort time: " + (StringUtil.getElapsedTimeTraditional(System.currentTimeMillis() - startTime, true)));
		}
	}
	
	public final ArrayList<Integer> getFilePaths() {
		return new ArrayList<>(this.filePaths);
	}
	
	public final ArrayList<Integer> getFolderPaths() {
		return new ArrayList<>(this.folderPaths);
	}
	
	//Convenient pass-through methods:
	
	/** Returns a {@link Set} view of the keys contained in this map.
	 * The set is backed by the map, so changes to the map are
	 * reflected in the set, and vice-versa. If the map is modified
	 * while an iteration over the set is in progress (except through
	 * the iterator's own <tt>remove</tt> operation), the results of
	 * the iteration are undefined. The set supports element removal,
	 * which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
	 * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
	 * operations.
	 *
	 * @return a set view of the keys contained in this map */
	public final Set<Integer> keySet() {
		return this.files.keySet();
	}
	
	/** Returns a {@link Set} view of the mappings contained in this map.
	 * The set is backed by the map, so changes to the map are
	 * reflected in the set, and vice-versa. If the map is modified
	 * while an iteration over the set is in progress (except through
	 * the iterator's own <tt>remove</tt> operation, or through the
	 * <tt>setValue</tt> operation on a map entry returned by the
	 * iterator) the results of the iteration are undefined. The set
	 * supports element removal, which removes the corresponding
	 * mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
	 * <tt>clear</tt> operations. It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the mappings contained in this map */
	public final Set<Entry<Integer, String>> entrySet() {
		return this.files.entrySet();
	}
	
	/** Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map */
	public final int size() {
		return this.files.size();
	}
	
	/** Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that
	 * {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise
	 * it returns {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>
	 * A return value of {@code null} does not <i>necessarily</i>
	 * indicate that the map contains no mapping for the key; it's also
	 * possible that the map explicitly maps the key to {@code null}.
	 * The {@link HashMap#containsKey containsKey} operation may be used to
	 * distinguish these two cases.
	 *
	 * @see HashMap#put(Object, Object) */
	public final String get(Object key) {
		return this.files.get(key);
	}
	
	/** Removes the mapping for the specified key from this map if present.
	 *
	 * @param key key whose mapping is to be removed from the map
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>.) */
	public final String remove(Object key) {
		return this.files.remove(key);
	}
	
}
