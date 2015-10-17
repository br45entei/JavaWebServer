/**
 * 
 */
package com.gmail.br45entei.server.data;

import com.gmail.br45entei.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

/** @author Brian_Entei */
public class FormURLEncodedData {
	
	/** The POST content as a String */
	public final String						postRequestStr;
	/** The POST content as a HashMap */
	public final HashMap<String, String>	postRequestArguments	= new HashMap<>();
	
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
		String[] form = postRequestStr.split("&");
		for(String arg : form) {
			String[] entry = arg.split("=");
			if(entry != null) {
				String key = "";
				String value = "";
				if(entry.length > 2) {
					key = entry[0];
					value = StringUtil.stringArrayToString(entry, '=', 1);
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
		try {
			postRequestStr = URLDecoder.decode(postRequestStr, "UTF-8");
		} catch(UnsupportedEncodingException ignored) {//This should never happen
		}
		this.postRequestStr = postRequestStr;
	}
}
