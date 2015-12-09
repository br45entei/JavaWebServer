package com.gmail.br45entei.util;

import com.gmail.br45entei.server.ClientInfo;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public class MapUtil {
	
	private static final String getRandomFileFromHashMap(final HashMap<Integer, String> files) {
		ArrayList<String> list = new ArrayList<>(files.values());
		return list.get(StringUtils.getRandomIntBetween(0, list.size()));
	}
	
	public static final String getRandomLinkFor(final HashMap<Integer, String> files, final String path, final String pathPrefix, final DomainDirectory domainDirectory, final String httpProtocol, final ClientInfo clientInfo) {
		if(files.size() == 0) {
			return null;
		}
		String randomFilePath = getRandomFileFromHashMap(files);
		String newPath = path + "/" + randomFilePath;
		if(newPath.startsWith("//")) {
			newPath = newPath.substring(1);
		}
		File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
		ArrayList<File> failedFiles = new ArrayList<>();
		while(!FileUtil.isFileAccessible(file)) {//Check if the file can be displayed on the web page
			if(!failedFiles.contains(file)) {
				failedFiles.add(file);
			}
			if(files.size() == 0 || failedFiles.size() >= files.size()) {//Check for infinite loop(hasn't happened yet, but it could happen in a system folder with all of the files in use by the os...!)
				return null;
			}
			randomFilePath = getRandomFileFromHashMap(files);
			newPath = path + "/" + randomFilePath;
			if(newPath.startsWith("//")) {
				newPath = newPath.substring(1);
			}
			file = new File(FilenameUtils.normalize(pathPrefix + newPath));
		}
		
		if(file.exists()) {
			String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
			return StringUtils.encodeHTML(httpProtocol + clientInfo.clientRequest.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
		}
		return null;
	}
	
	public static final Integer getOldestFileInfoKeyInHashMapAndRemoveIt(HashMap<Integer, FileInfo> infos) {
		Integer oldestInfoKey = null;
		if(infos != null && !infos.isEmpty()) {
			long oldestSize = Long.MAX_VALUE;
			for(Entry<Integer, FileInfo> entry : infos.entrySet()) {
				FileInfo info = entry.getValue();
				if(info != null) {
					if(info.lastModifiedLong < oldestSize) {
						oldestInfoKey = entry.getKey();
						oldestSize = info.lastModifiedLong;
					}
				}
			}
			if(oldestInfoKey != null) {
				infos.remove(oldestInfoKey);
			}
		}
		return oldestInfoKey;
	}
	
	public static final Integer getSmallestFileInfoKeyInHashMapAndRemoveIt(HashMap<Integer, FileInfo> infos) {
		Integer smallestInfoKey = null;
		if(infos != null && !infos.isEmpty()) {
			long smallestSize = Long.MAX_VALUE;
			for(Entry<Integer, FileInfo> entry : infos.entrySet()) {
				FileInfo info = entry.getValue();
				if(info != null && StringUtils.isStrLong(info.contentLength)) {
					long size = Long.valueOf(info.contentLength).longValue();
					if(size < smallestSize) {
						smallestInfoKey = entry.getKey();
						smallestSize = size;
					}
				}
			}
			if(smallestInfoKey != null) {
				infos.remove(smallestInfoKey);
			}
		}
		return smallestInfoKey;
	}
	
	public static final Integer getLargestMimeTypeAlphabeticallyFromFileInfoKeyInHashMapAndRemoveIt(HashMap<Integer, FileInfo> infos) {
		Integer largestMimeTypeAlphabeticallyInfoKey = null;
		if(infos != null && !infos.isEmpty()) {
			String largestMimeTypeAlphabetically = "";
			for(Entry<Integer, FileInfo> entry : infos.entrySet()) {
				FileInfo info = entry.getValue();
				if(info != null) {
					String mimeType = info.mimeType.toLowerCase();
					int size = mimeType.compareTo(largestMimeTypeAlphabetically);
					if(size > 0) {
						largestMimeTypeAlphabeticallyInfoKey = entry.getKey();
						largestMimeTypeAlphabetically = mimeType;
					}
				}
			}
			if(largestMimeTypeAlphabeticallyInfoKey != null) {
				infos.remove(largestMimeTypeAlphabeticallyInfoKey);
			}
		}
		return largestMimeTypeAlphabeticallyInfoKey;
	}
	
}
