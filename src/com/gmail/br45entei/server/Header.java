package com.gmail.br45entei.server;

import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.writer.DualPrintWriter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/** @author Brian_Entei */
public class Header {
	
	public static final Header BLANK_LINE = new Header("", "");
	public static final Header EOS_REACHED = new Header((String) null, (String) null);
	
	public static final boolean isBlank(Header header) {
		return header != null && header.equals(BLANK_LINE);
	}
	
	/** @param headers */
	public static final Collection<Header> collectSetCookies(Collection<Header> headers) {
		if(headers == null) {
			return null;
		}
		ArrayList<Header> list = new ArrayList<>();
		for(Header header : headers) {
			if(header.header.equalsIgnoreCase("set-cookie")) {
				list.add(header);
			}
		}
		return list;
	}
	
	/** @param headers */
	public static final Collection<Header> collectClientCookies(Collection<Header> headers) {
		if(headers == null) {
			return null;
		}
		ArrayList<Header> list = new ArrayList<>();
		for(Header header : headers) {
			if(header.header.equalsIgnoreCase("cookie")) {
				list.add(header);
			}
		}
		return list;
	}
	
	public static final Header getHeaderFrom(Collection<Header> headers, String targetHeaderKey) {
		return getHeaderFrom(targetHeaderKey, headers.toArray(new Header[headers.size()]));
	}
	
	public static final String getHeaderStrFrom(Collection<Header> headers, String targetHeaderKey) {
		return getHeaderStrFrom(targetHeaderKey, headers.toArray(new Header[headers.size()]));
	}
	
	public static final String getValueFrom(Collection<Header> headers, String targetHeaderValue) {
		return getValueFrom(targetHeaderValue, headers.toArray(new Header[headers.size()]));
	}
	
	public static final boolean collectionContainsHeader(Collection<Header> headers, String targetHeaderKey) {
		return getHeaderFrom(targetHeaderKey, headers.toArray(new Header[headers.size()])) != null;
	}
	
	public static final boolean collectionContainsHeader(Collection<Header> headers, Header targetHeader) {
		if(headers != null && targetHeader != null) {
			for(Header header : headers) {
				if(header.equals(targetHeader)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** @param headers The collection of headers
	 * @param headersToRemove The header(s) to remove
	 * @return A collection containing all of the headers that were removed */
	public static final Collection<Header> removeHeaderFrom(Collection<Header> headers, String... headersToRemove) {
		Collection<Header> toRemove = new ArrayList<>();
		if(headers != null && headersToRemove != null && headersToRemove.length > 0) {
			for(String headerToRemove : headersToRemove) {
				if(headerToRemove == null) {
					continue;
				}
				for(Header header : headers) {
					if(header.header == null) {
						continue;
					}
					if(header.header.equalsIgnoreCase(headerToRemove)) {
						toRemove.add(header);
					}
				}
			}
			for(Header removeMe : toRemove) {
				headers.remove(removeMe);
			}
		}
		return toRemove;
	}
	
	public static final String getHeaderStrFrom(String targetHeaderKey, Header... headers) {
		if(headers != null && targetHeaderKey != null) {
			for(Header header : headers) {
				if(targetHeaderKey.equalsIgnoreCase(header.header)) {
					return header.header;
				}
			}
		}
		return null;
	}
	
	public static final Header getHeaderFrom(String targetHeaderKey, Header... headers) {
		if(headers != null && targetHeaderKey != null) {
			for(Header header : headers) {
				if(targetHeaderKey.equalsIgnoreCase(header.header)) {
					return header;
				}
			}
		}
		return null;
	}
	
	public static final String getValueFrom(String targetHeaderValue, Header... headers) {
		if(headers != null && targetHeaderValue != null) {
			for(Header header : headers) {
				if(targetHeaderValue.equalsIgnoreCase(header.value)) {
					return header.value;
				}
			}
		}
		return null;
	}
	
	public static final Long getContentLengthFrom(Collection<Header> headers) throws NumberFormatException {
		return getContentLengthFrom(headers.toArray(new Header[headers.size()]));
	}
	
	public static final Long getContentLengthFrom(Header... headers) throws NumberFormatException {
		if(headers != null) {
			for(Header header : headers) {
				if("Content-Length".equalsIgnoreCase(header.value)) {
					return Long.valueOf(header.value);
				}
			}
		}
		return null;
	}
	
	public static final String headersToString(Collection<Header> headers) {
		String list = null;
		if(headers != null) {
			list = "";
			for(Header header : headers) {
				list += header.toString() + "\r\n";
			}
		}
		return list;
	}
	
	public static final String headersToString(Header... headers) {
		String list = null;
		if(headers != null) {
			list = "";
			for(Header header : headers) {
				list += header.toString() + "\r\n";
			}
		}
		return list;
	}
	
	public final String header;
	public volatile String value;
	
	public Header(String header, long value) {
		this(header, StringUtil.longToString(value));
	}
	
	public Header(String header, double value) {
		this(header, StringUtil.doubleToString(value));
	}
	
	public Header(String header, float value) {
		this(header, StringUtil.floatToString(value));
	}
	
	public Header(String header, int value) {
		this(header, Integer.valueOf(value));
	}
	
	public Header(String header, short value) {
		this(header, Short.toString(value));
	}
	
	public Header(String header, char value) {
		this(header, Character.valueOf(value));
	}
	
	public Header(String header, byte value) {
		this(header, Byte.valueOf(value));
	}
	
	public Header(String header, boolean value) {
		this(header, Boolean.valueOf(value));
	}
	
	public Header(String header, Object value) {
		this.header = header;
		this.value = value == null ? null : value.toString();
	}
	
	public Header(String header, String value) {
		this.header = header;
		this.value = value;
	}
	
	public Header(String line) {
		if(line.contains(":")) {
			String[] split = line.split(Pattern.quote(":"));
			this.header = StringUtil.fixConcatenatedString(split[0], ":", " ", " ", "", "");
			this.value = StringUtil.stringArrayToString(split, ':', 1);//StringUtil.fixConcatenatedString(StringUtil.stringArrayToString(split, ':', 1), ":", " ", " ", "", "");
			if(this.value.startsWith(" ") && this.value.length() > 1) {
				this.value = this.value.substring(1);
			}
			return;
		}
		throw new IllegalArgumentException("Line \"" + line + "\" contained no colon(the ':' character)!");
	}
	
	public Header(String line, Collection<Header> listToAddTo) {
		this(line);
		if(listToAddTo != null) {
			listToAddTo.add(this);
		}
	}
	
	public final void println(PrintStream pr) {
		pr.println(this.header + ": " + this.value);
	}
	
	public final void println(DualPrintWriter pr) {
		pr.println(this.header + ": " + this.value);
	}
	
	@Override
	public String toString() {
		return this.header + ": " + this.value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.header == null) ? 0 : this.header.hashCode());
		//result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Header)) {
			return false;
		}
		Header other = (Header) obj;
		if(this.header == null) {
			if(other.header != null) {
				return false;
			}
		} else if(!this.header.equals(other.header)) {
			return false;
		}
		if(this.value == null) {
			if(other.value != null) {
				return false;
			}
		} else if(!this.value.equals(other.value)) {
			return false;
		}
		return true;
	}
	
}
