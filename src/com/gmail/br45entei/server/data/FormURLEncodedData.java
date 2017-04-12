package com.gmail.br45entei.server.data;

import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/** @author Brian_Entei */
public class FormURLEncodedData {
	
	/** The POST content as a String */
	public final String postRequestStr;
	/** The POST content as a HashMap */
	public final HashMap<String, String> postRequestArguments = new HashMap<>();
	
	/** @param postRequestData The raw POST data */
	public FormURLEncodedData(byte[] postRequestData) {
		this(new String(postRequestData));//.replace("%26", "%2526");//this.postRequestStr = URLDecoder.decode(new String(postReq).replace("%26", "%2526"), "UTF-8"));
	}
	
	/** @param postRequestStr The POST content in String form */
	public FormURLEncodedData(String postRequestStr) {
		if(postRequestStr.isEmpty()) {
			this.postRequestStr = "";
			return;
		}
		String[] form = postRequestStr.split(Pattern.quote("&"));
		String lastBrokenArg = null;
		int lastBrokenIndex = -1;
		for(int i = 0; i < form.length; i++) {
			String arg = form[i];
			if(!arg.contains("=")) {
				lastBrokenArg = arg;
				lastBrokenIndex = i;
				continue;
			}
			if(lastBrokenIndex != -1) {
				arg = lastBrokenArg + "&" + arg;
				lastBrokenIndex = -1;
				lastBrokenArg = null;
			}
			String[] entry = arg.split("=");
			if(entry != null) {
				String key = "";
				String value = "";
				if(entry.length > 2) {
					key = entry[0];
					value = StringUtils.stringArrayToString(entry, '=', 1);
				} else if(entry.length == 2) {
					key = entry[0];
					value = entry[1];
				} else if(entry.length == 1) {
					if(arg.endsWith("=")) {
						key = entry[0];
						value = "";
					} else if(arg.startsWith("=")) {
						key = "";
						value = entry[0];
					}
				}
				try {
					key = key != null && !key.isEmpty() ? URLDecoder.decode(key, "UTF-8") : key;
					value = value != null && !value.isEmpty() ? URLDecoder.decode(value, "UTF-8") : value;
				} catch(UnsupportedEncodingException ignored) {//This should never happen
				}
				this.postRequestArguments.put(key, value);//this.postRequestArguments.put(key.replace("%26", "&"), value.replace("%26", "&"));
			}
		}
		/*try {
			postRequestStr = URLDecoder.decode(postRequestStr, "UTF-8");
		} catch(UnsupportedEncodingException ignored) {//This should never happen
		}
		this.postRequestStr = postRequestStr;*/
		String postReqStr = "";
		HashMap<String, String> filterMap = new HashMap<>();
		HashMap<String, String> blankMap = new HashMap<>();
		for(Entry<String, String> entry : this.postRequestArguments.entrySet()) {
			String param = entry.getKey();
			String value = entry.getValue();
			if(value.trim().isEmpty()) {
				blankMap.put(param, value);
			} else {
				filterMap.put(param, value);
			}
		}
		for(Entry<String, String> entry : filterMap.entrySet()) {
			postReqStr += entry.getKey() + "=" + entry.getValue() + "&";
		}
		for(Entry<String, String> entry : blankMap.entrySet()) {
			if(filterMap.get(entry.getKey()) == null) {
				postReqStr += entry.getKey() + "&";
			}
		}
		if(postReqStr.endsWith("&")) {
			if(postReqStr.trim().equals("=&")) {
				postReqStr = "";
			} else if(postReqStr.length() > 1) {
				postReqStr = postReqStr.substring(0, postReqStr.length() - 1);
			}
			if(postReqStr.trim().equals("=")) {
				postReqStr = "";
			}
		}
		this.postRequestStr = postReqStr;
		LogUtils.ORIGINAL_SYSTEM_OUT.println("\r\n========== NEW post request str: " + this.postRequestStr + "\r\n");
	}
	
}
