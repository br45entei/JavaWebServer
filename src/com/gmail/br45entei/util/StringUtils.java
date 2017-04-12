package com.gmail.br45entei.util;

import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.server.ClientConnection;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.lang3.StringEscapeUtils;
import org.mozilla.universalchardet.UniversalDetector;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

/** @author Brian_Entei */
public strictfp class StringUtils {
	private static final SecureRandom random = new SecureRandom();
	
	/** @param string The String to hash
	 * @return The resulting hash
	 * @see <a
	 *      href=
	 *      "http://stackoverflow.com/a/1660613/2398263">stackoverflow.com</a> */
	public static final long hash(String string) {
		long h = 1125899906842597L; // prime
		int len = string.length();
		
		for(int i = 0; i < len; i++) {
			h = 31 * h + string.charAt(i);
		}
		return h;
	}
	
	public static final int getRandomIntBetween(int min, int max) {
		return random.nextInt(max - min) + min;
	}
	
	public static final long shuffle(List<?> list) {
		long rtrn = random.nextLong();
		Collections.shuffle(list, new Random(rtrn));
		return rtrn;
	}
	
	public static final void shuffle(List<?> list, long seed) {
		Collections.shuffle(list, new Random(seed));
	}
	
	/** @param map The map containing the request arguments
	 * @param includeURLFormatting Whether or not the preceding <code>?</code>
	 *            is included
	 * @return The resulting String */
	public static final String requestArgumentsToString(Map<String, String> map, boolean includeURLFormatting) {
		return requestArgumentsToString(map, includeURLFormatting, (String[]) null);
	}
	
	public static final String[] stringMapToStringArray(Map<String, String> map, char keyValueSeparatorChar, char setSeparatorChar) {
		if(map == null || map.isEmpty()) {
			return new String[0];
		}
		Set<Entry<String, String>> entrySet = map.entrySet();
		String[] rtrn = new String[entrySet.size()];
		int i = 0;
		for(Entry<String, String> entry : entrySet) {
			rtrn[i] = (i == 0 ? "" : setSeparatorChar + "") + entry.getKey() + keyValueSeparatorChar + entry.getValue();
			i++;
		}
		return rtrn;
	}
	
	public static final String requestArgumentsToString(Map<String, String> map, boolean includeURLFormatting, String... exclusions) {
		if(map == null || map.isEmpty()) {
			return "";
		}
		if(exclusions == null) {
			return (includeURLFormatting ? "?" : "") + stringArrayToString(stringMapToStringArray(map, '=', includeURLFormatting ? '&' : ' '));
		}
		Map<String, String> copy = new HashMap<>(map);
		for(String exclusion : exclusions) {
			copy.remove(exclusion);
		}
		return (includeURLFormatting ? "?" : "") + stringArrayToString(stringMapToStringArray(copy, '=', includeURLFormatting ? '&' : ' '));
	}
	
	public static final String cacheValidatorTimePattern = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
	public static final Locale cacheValidatorTimeLocale = Locale.US;
	public static final TimeZone cacheValidatorTimeFormat = TimeZone.getTimeZone("GMT");
	
	public static final void main(String[] args) {
		/*String test = "<html>\r\n"//
				+ "\t<head>\r\n"//
				+ "\t\t<title>Hai ther!</title>\r\n"//
				+ "\t</head>\r\n"//
				+ "\t<body>\r\n"//
				+ "\t\t<string>This is a webpage! Do you like it?</string>\r\n"//
				+ "\t</body>\r\n"//
				+ "</html>";
		
		System.out.println(test.length());
		System.out.println("\r\n===\r\n");
		try {
			System.out.println(compressString(test, "UTF-8").length);
			System.out.println(compressString("1234567890123456789012345678901234", "UTF-8").length);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		try {
			File out = new File(System.getProperty("user.dir") + File.separatorChar + "temp-" + (new Random()).nextLong() + ".txt");
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"), true);
			pr.println("Hello, world!\r\nHow are you?");
			pr.flush();
			pr.close();
			System.out.println("Resulting charset: \"" + getDetectedEncoding(out) + "\"");
			out.deleteOnExit();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		*/
		//long millis = (4 * MILLENNIUM) + (2 * DAY) + (17 * HOUR) + (56 * MINUTE) + (49 * SECOND) + (0 * MILLISECOND);
		//long millis = (4 * MILLENNIUM) + (2 * DAY) + (0 * HOUR) + (0 * MINUTE) + (4 * SECOND) + (0 * MILLISECOND);
		//long millis = -((4 * MILLENNIUM) + (364 * DAY) + (0 * HOUR) + (0 * MINUTE) + (0 * SECOND) + (0 * MILLISECOND));
		//long millis = -1L + YEAR;
		long millis = 0L;
		System.out.println(StringUtils.getElapsedTime(millis));
	}
	
	public static final int getLengthOfLongestLineInStr(String str) {
		int count = 0;
		if(str == null) {
			return -1;
		}
		if(str.contains("\n")) {
			for(String s : str.split(Pattern.quote("\n"))) {
				final int length = s.length();
				if(length > count) {
					count = length;
				}
			}
		}
		return count + 1;
	}
	
	public static final int getNumOfLinesInStr(String str) {
		int count = 0;
		if(str == null) {
			return -1;
		}
		if(str.contains("\n")) {
			count = str.split("\\n").length;
		}
		return count + 1;
	}
	
	public static final String requestArgumentsToString(HashMap<String, String> requestArguments, String... argumentsToIgnore) {//Hmm, I like the new one better, but I'll leave this one.
		String rtrn = "?";
		final HashMap<String, String> reqArgs = new HashMap<>(requestArguments);
		for(Entry<String, String> entry : requestArguments.entrySet()) {
			if(entry.getKey() != null) {
				for(String argToIgnore : argumentsToIgnore) {
					if(entry.getKey().equals(argToIgnore)) {
						reqArgs.remove(entry.getKey());
					}
				}
			}
		}
		boolean addedAnyArguments = false;
		for(Entry<String, String> entry : reqArgs.entrySet()) {
			rtrn += (addedAnyArguments ? "&" : "") + entry.getKey() + "=" + entry.getValue();
			addedAnyArguments = true;
		}
		return rtrn.equals("?") ? "" : rtrn;
	}
	
	public static final String stringArrayToString(String... args) {
		String rtrn = "";
		if(args != null) {
			for(String str : args) {
				rtrn += str;
			}
		}
		return rtrn;
	}
	
	/** @param args The string array to copy
	 * @param seperatorChar The character to use to separate the elements in the
	 *            string array
	 * @return The resulting string */
	public static String stringArrayToString(String[] args, char seperatorChar) {
		String makeArgs = "";
		int i = 1;
		for(String curArg : args) {
			makeArgs += curArg + (i != args.length ? seperatorChar + "" : "");
			i++;
		}
		return makeArgs.trim();
	}
	
	/** @param args The string array to copy
	 * @param seperatorChar The character to use to separate the elements in the
	 *            string array
	 * @param startIndex The index to start at
	 * @return The resulting string */
	public static String stringArrayToString(String[] args, char seperatorChar, int startIndex) {
		String makeArgs = "";
		if(startIndex < args.length && startIndex >= 0) {
			for(int i = startIndex; i < args.length; i++) {
				makeArgs += args[i] + (i != (args.length - 1) ? seperatorChar + "" : "");
			}
		}
		return makeArgs.trim();
	}
	
	/** @param args The string array to copy
	 * @param seperatorChar The character to use to separate the elements in the
	 *            string array
	 * @param startIndex The index to start at
	 * @param endIndex The index to end at
	 * @return The resulting string */
	public static String stringArrayToString(String[] args, char seperatorChar, int startIndex, int endIndex) {
		String makeArgs = "";
		if(startIndex < args.length && endIndex <= args.length && startIndex >= 0 && endIndex >= 1) {
			for(int i = startIndex; i < endIndex; i++) {
				makeArgs += args[i] + (i != args.length ? seperatorChar + "" : "");
			}
		}
		return makeArgs.trim();
	}
	
	public static final boolean isStrUUID(String uuid) {
		if(uuid == null) {
			return false;
		}
		try {
			return UUID.fromString(uuid) != null;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	/** @param str The string to convert
	 * @return The resulting long value
	 * @throws NumberFormatException Thrown if the given String does not
	 *             represent a valid long value */
	public static final Long getLongFromStr(String str) throws NumberFormatException {
		return Long.valueOf(str);
	}
	
	/** @param time The time to convert
	 * @param getTimeOnly Whether or not the result should exclude the date
	 * @return The result */
	public static String getTime(long time, boolean getTimeOnly) {
		return getTime(time, getTimeOnly, false);
	}
	
	/** @param time The time to convert
	 * @param getTimeOnly Whether or not the result should exclude the date
	 * @param fileSystemSafe Whether or not the date should be file system
	 *            safe(does nothing if the date is excluded)
	 * @return The result */
	public static String getTime(long time, boolean getTimeOnly, boolean fileSystemSafe) {
		return new SimpleDateFormat(getTimeOnly ? "HH-mm-ss" : fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy'\t'h:mm:ss a").format(new Date(time));
	}
	
	public static final SimpleDateFormat getCacheValidatorTimeFormat() {
		SimpleDateFormat rtrn = new SimpleDateFormat(cacheValidatorTimePattern, cacheValidatorTimeLocale);
		rtrn.setTimeZone(cacheValidatorTimeFormat);
		return rtrn;
	}
	
	/** @param millis The amount of milliseconds that have passed since
	 *            midnight,
	 *            January 1, 1970
	 * @return The resulting string in cache format */
	public static final String getCacheTime(long millis) {
		return getCacheValidatorTimeFormat().format(new Date(millis));
	}
	
	/** @return The current time in cache format */
	public static final String getCurrentCacheTime() {
		return getCacheValidatorTimeFormat().format(new Date());
	}
	
	public static final long getLastModified(File file) {
		if(!file.exists()) {
			return 0L;
		}
		try {
			return StringUtils.getCacheValidatorTimeFormat().parse(StringUtils.getCacheTime(file.lastModified())).getTime();
		} catch(ParseException e) {
			return file.lastModified();
		}
	}
	
	public static final byte[] compressBytes(byte[] bytes) throws IOException {
		DisposableByteArrayOutputStream out = new DisposableByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(bytes);
		gzip.flush();
		gzip.close();
		byte[] compressedBytes = out.toByteArray();
		out.dispose();
		return compressedBytes;
	}
	
	public static final byte[] compressString(String str, String charsetName, ClientConnection reuse) throws IOException {
		if(str == null || str.length() == 0) {
			return new byte[0];
		}
		if(str.length() < 33) {
			reuse.printErrln("Warning! Compressing a string whose length is less than ~33 bytes results in a compressed string whose length is greater than the original string!");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes(charsetName));
		gzip.flush();
		gzip.close();
		byte[] compressedBytes = out.toByteArray();
		return compressedBytes;
	}
	
	public static final String getTextFileAsString(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String rtrn = "";
		while(br.ready()) {
			rtrn += br.readLine() + "\n";//"\r\n" causes issues for some reason, and no new line characters causes even more issues, but just "\n" works. Weird.
		}
		try {
			br.close();
		} catch(Throwable ignored) {
		}
		return rtrn;
	}
	
	public static String rtfToHtml(File file) throws IOException {
		FileReader rtf = new FileReader(file);
		JEditorPane p = new JEditorPane();
		p.setContentType("text/rtf");
		RTFEditorKit kitRtf = (RTFEditorKit) p.getEditorKitForContentType("text/rtf");
		try {
			Document d = p.getDocument();
			kitRtf.read(rtf, d, 0);
			kitRtf = null;
			HTMLEditorKit kitHtml = (HTMLEditorKit) p.getEditorKitForContentType("text/html");
			Writer writer = new StringWriter();
			kitHtml.write(writer, d, 0, d.getLength());
			return writer.toString();
		} catch(BadLocationException ignored) {
			//e.printStackTrace();
		}
		try {
			rtf.close();
		} catch(Throwable ignored) {
		}
		return "";
	}
	
	public static final String getCharsetOfBook(File file) {
		String def = Charset.defaultCharset().name();
		if(file == null || !file.exists()) {
			return def;
		}
		EpubReader epubReader = new EpubReader();
		try(FileInputStream in = new FileInputStream(file)) {
			Book book = epubReader.readEpub(in);
			Resource coverPage = book.getCoverPage();
			if(coverPage != null) {
				return coverPage.getInputEncoding();
			}
			return def;
		} catch(IOException e) {
			return def;
		}
	}
	
	public static final String getMimeTypeOfBook(File file) {
		String def = "text/html";
		if(file == null || !file.exists()) {
			return def;
		}
		EpubReader epubReader = new EpubReader();
		try(FileInputStream in = new FileInputStream(file)) {
			Book book = epubReader.readEpub(in);
			Resource coverPage = book.getCoverPage();
			if(coverPage != null) {
				return coverPage.getMediaType().getName();
			}
			return def;
		} catch(IOException e) {
			return def;
		}
	}
	
	public static final String readEpubBook(File file) {
		if(file == null || !file.exists()) {
			return "";
		}
		EpubReader epubReader = new EpubReader();
		try(FileInputStream in = new FileInputStream(file)) {
			Book book = epubReader.readEpub(in);
			String title = book.getTitle();
			String content = "";
			List<Resource> contents = book.getContents();
			for(Resource res : contents) {
				String charset = res.getInputEncoding();
				if(charset != null) {
					content += new String(res.getData(), charset) + "\r\n";
				}
			}
			return title + "\r\n" + content;
		} catch(IOException e) {
			return "";
		}
	}
	
	public static final String getDetectedEncoding(File file) throws IOException {
		if(file == null || !file.exists()) {
			return Charset.defaultCharset().name();
		}
		InputStream in = new FileInputStream(file);
		String rtrn = getDetectedEncoding(in);
		try {
			in.close();
		} catch(Throwable ignored) {
		}
		return rtrn;
	}
	
	public static final String getDetectedEncoding(InputStream is) throws IOException {
		if(is == null) {
			return Charset.defaultCharset().name();
		}
		UniversalDetector detector = new UniversalDetector(null);
		byte[] buf = new byte[4096];
		int nread;
		while((nread = is.read(buf)) > 0 && !detector.isDone()) {
			detector.handleData(buf, 0, nread);
		}
		detector.dataEnd();
		String charset = detector.getDetectedCharset();
		return charset != null ? charset : Charset.defaultCharset().name();
	}
	
	public static final boolean containsIgnoreCase(String str, String... list) {
		if(list != null && list.length != 0) {
			for(String s : Arrays.asList(list)) {
				if(s != null && s.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static final boolean containsIgnoreCase(ArrayList<String> list, String str) {
		if(list != null && !list.isEmpty()) {
			for(String s : new ArrayList<>(list)) {
				if(s != null && s.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean containsIgnoreCase(Collection<String> list, String str) {
		if(list != null && !list.isEmpty()) {
			for(String s : new ArrayList<>(list)) {
				if(s != null && s.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getStringInList(Collection<String> list, String str) {
		if(list != null && !list.isEmpty()) {
			for(String s : new ArrayList<>(list)) {
				if(s != null && s.equalsIgnoreCase(str)) {
					return s;
				}
			}
		}
		return null;
	}
	
	public static final String getUrlLinkFromFile(File urlFile) throws IOException {
		String line = "";
		if(urlFile == null || !urlFile.exists() || urlFile.isDirectory()) {
			return line;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(urlFile));
			while(br.ready()) {
				line = br.readLine();
				if(line.startsWith("URL=")) {
					line = line.substring(4);
					break;
				}
			}
		} finally {
			if(br != null) {
				br.close();
			}
		}
		return line;
	}
	
	public static final String makeFilePathURLSafe(String filePath) {
		return filePath.replace("%", "%25").replace("+", "%2b").replace("#", "%23").replace(" ", "%20");
	}
	
	public static final String encodeHTML(String str) {
		str = str.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replace("+", "%2b").replace("#", "%23").replace(" ", "%20");
		StringEscapeUtils.escapeHtml3("");
		return StringEscapeUtils.escapeHtml4(str).replace(" ", "%20");//.replaceAll("(?i)&mdash;", "—").replaceAll("(?i)&ndash;", "–").replaceAll("(?i)&micro;", "μ").replaceAll("(?i)&omega;", "Ω");
	}
	
	public static final String decodeHTML(String s) {
		final String str = s;
		s = s.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B");
		try {
			s = URLDecoder.decode(s, "UTF-8");
			return StringEscapeUtils.unescapeHtml4(s);
		} catch(Throwable ignored) {
			return StringEscapeUtils.unescapeHtml4(str);
		}
	}
	
	private static final HashMap<String, String> urlChars = new HashMap<>();
	
	static {
		urlChars.put("%20", " ");
		urlChars.put("%21", "!");
		urlChars.put("%22", "\"");
		urlChars.put("%23", "#");
		urlChars.put("%24", "$");
		urlChars.put("%25", "%");
		urlChars.put("%26", "&");
		urlChars.put("%27", "'");
		urlChars.put("%28", "(");
		urlChars.put("%29", ")");
		urlChars.put("%2a", "*");
		urlChars.put("%2b", "+");
		urlChars.put("%2c", ",");
		urlChars.put("%2d", "-");
		urlChars.put("%2e", ".");
		urlChars.put("%2f", "/");
		urlChars.put("%3a", ":");
		urlChars.put("%3b", ";");
		urlChars.put("%3c", "<");
		urlChars.put("%3d", "=");
		urlChars.put("%3e", ">");
		urlChars.put("%3f", "?");
		urlChars.put("%40", "@");
		urlChars.put("%5b", "[");
		urlChars.put("%5c", "\\");
		urlChars.put("%5d", "]");
		urlChars.put("%5e", "^");
		urlChars.put("%5f", "_");
		urlChars.put("%60", "`");
		urlChars.put("%7b", "{");
		urlChars.put("%7c", "|");
		urlChars.put("%7d", "}");
		urlChars.put("%7e", "~");
		urlChars.put("%e2%82%ac", "€");
		urlChars.put("%e2%80%9a", "‚");
		urlChars.put("%c6%92", "ƒ");
		urlChars.put("%e2%80%9e", "„");
		urlChars.put("%e2%80%a6", "…");
		urlChars.put("%e2%80%a0", "†");
		urlChars.put("%e2%80%a1", "‡");
		urlChars.put("%cb%86", "ˆ");
		urlChars.put("%e2%80%b0", "‰");
		urlChars.put("%c5%a0", "Š");
		urlChars.put("%e2%80%b9", "‹");
		urlChars.put("%c5%92", "Œ");
		urlChars.put("%c5%bd", "Ž");
		urlChars.put("%e2%80%98", "‘");
		urlChars.put("%e2%80%99", "’");
		urlChars.put("%e2%80%9c", "“");
		urlChars.put("%e2%80%9d", "”");
		urlChars.put("%e2%80%A2", "•");
		urlChars.put("%e2%80%93", "–");
		urlChars.put("%e2%80%94", "—");
		urlChars.put("%cb%9c", "˜");
		urlChars.put("%e2%84", "™");
		urlChars.put("%c5%a1", "š");
		urlChars.put("%e2%80", "›");
		urlChars.put("%c5%93", "œ");
		urlChars.put("%c5%be", "ž");
		urlChars.put("%c5%b8", "Ÿ");
		urlChars.put("%c2%a1", "¡");
		urlChars.put("%c2%a2", "¢");
		urlChars.put("%c2%a3", "£");
		urlChars.put("%c2%a4", "¤");
		urlChars.put("%c2%a5", "¥");
		urlChars.put("%c2%a6", "|");
		urlChars.put("%c2%a7", "§");
		urlChars.put("%c2%a8", "¨");
		urlChars.put("%c2%a9", "©");
		urlChars.put("%c2%aa", "ª");
		urlChars.put("%c2%ab", "«");
		urlChars.put("%c2%ac", "¬");
		urlChars.put("%c2%ad", "¯");
		urlChars.put("%c2%ae", "®");
		urlChars.put("%c2%af", "¯");
		urlChars.put("%c2%b0", "°");
		urlChars.put("%c2%b1", "±");
		urlChars.put("%c2%b2", "²");
		urlChars.put("%c2%b3", "³");
		urlChars.put("%c2%b4", "´");
		urlChars.put("%c2%b5", "µ");
		urlChars.put("%c2%b6", "¶");
		urlChars.put("%c2%b7", "·");
		urlChars.put("%c2%b8", "¸");
		urlChars.put("%c2%b9", "¹");
		urlChars.put("%c2%ba", "º");
		urlChars.put("%c2%bb", "»");
		urlChars.put("%c2%bc", "¼");
		urlChars.put("%c2%bd", "½");
		urlChars.put("%c2%be", "¾");
		urlChars.put("%c2%bf", "¿");
		urlChars.put("%c3%80", "À");
		urlChars.put("%c3%81", "Á");
		urlChars.put("%c3%82", "Â");
		urlChars.put("%c3%83", "Ã");
		urlChars.put("%c3%84", "Ä");
		urlChars.put("%c3%85", "Å");
		urlChars.put("%c3%86", "Æ");
		urlChars.put("%c3%87", "Ç");
		urlChars.put("%c3%88", "È");
		urlChars.put("%c3%89", "É");
		urlChars.put("%c3%8a", "Ê");
		urlChars.put("%c3%8b", "Ë");
		urlChars.put("%c3%8c", "Ì");
		urlChars.put("%c3%8d", "Í");
		urlChars.put("%c3%8e", "Î");
		urlChars.put("%c3%8f", "Ï");
		urlChars.put("%c3%90", "Ð");
		urlChars.put("%c3%91", "Ñ");
		urlChars.put("%c3%92", "Ò");
		urlChars.put("%c3%93", "Ó");
		urlChars.put("%c3%94", "Ô");
		urlChars.put("%c3%95", "Õ");
		urlChars.put("%c3%96", "Ö");
		urlChars.put("%c3%97", "×");
		urlChars.put("%c3%98", "Ø");
		urlChars.put("%c3%99", "Ù");
		urlChars.put("%c3%9a", "Ú");
		urlChars.put("%c3%9b", "Û");
		urlChars.put("%c3%9c", "Ü");
		urlChars.put("%c3%9d", "Ý");
		urlChars.put("%c3%9e", "Þ");
		urlChars.put("%c3%9f", "ß");
		urlChars.put("%c3%a0", "à");
		urlChars.put("%c3%a1", "á");
		urlChars.put("%c3%a2", "â");
		urlChars.put("%c3%a3", "ã");
		urlChars.put("%c3%a4", "ä");
		urlChars.put("%c3%a5", "å");
		urlChars.put("%c3%a6", "æ");
		urlChars.put("%c3%a7", "ç");
		urlChars.put("%c3%a8", "è");
		urlChars.put("%c3%a9", "é");
		urlChars.put("%c3%aa", "ê");
		urlChars.put("%c3%ab", "ë");
		urlChars.put("%c3%ac", "ì");
		urlChars.put("%c3%ad", "í");
		urlChars.put("%c3%ae", "î");
		urlChars.put("%c3%af", "ï");
		urlChars.put("%c3%b0", "ð");
		urlChars.put("%c3%b1", "ñ");
		urlChars.put("%c3%b2", "ò");
		urlChars.put("%c3%b3", "ó");
		urlChars.put("%c3%b4", "ô");
		urlChars.put("%c3%b5", "õ");
		urlChars.put("%c3%b6", "ö");
		urlChars.put("%c3%b7", "÷");
		urlChars.put("%c3%b8", "ø");
		urlChars.put("%c3%b9", "ù");
		urlChars.put("%c3%ba", "ú");
		urlChars.put("%c3%bb", "û");
		urlChars.put("%c3%bc", "ü");
		urlChars.put("%c3%bd", "ý");
		urlChars.put("%c3%be", "þ");
		urlChars.put("%c3%bf", "ÿ");
		urlChars.put("%ce%a9", "Ω");
		
	}
	
	private static final boolean isCharIllegal(String str) {
		return(str.equals("\n") || str.equals("\r") || str.equals("\t") || str.equals("\0") || str.equals("\f") || str.equals("`") || str.equals("'") || str.equals("?") || str.equals("*") || str.equals("<") || str.equals(">") || str.equals("|") || str.equals("\""));
	}
	
	public static final String encodeURLStr(String str, boolean replacePercentSymbols) {
		for(Entry<String, String> entry : urlChars.entrySet()) {
			String charr = entry.getValue();
			if(!isCharIllegal(charr)) {
				if(!charr.equals("%") || replacePercentSymbols) {
					str = str.replace(charr, entry.getKey());
				}
			}
		}
		return str;
	}
	
	/** @param value The value to test
	 * @return Whether or not it is a valid long value */
	public static final boolean isStrLong(String value) {
		try {
			Long.valueOf(value).longValue();
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	/** Compare Strings in alphabetical order */
	public static final Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
		@Override
		public int compare(String str1, String str2) {
			if(str1 == null || str2 == null) {
				return Integer.MAX_VALUE;
			}
			int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
			if(res == 0) {
				res = str1.compareTo(str2);
			}
			return res;
		}
	};
	
	//==========================
	protected static final long MILLISECOND = 1L;
	protected static final long SECOND = 1000L;
	protected static final long MINUTE = 60 * SECOND;
	protected static final long HOUR = 60 * MINUTE;
	protected static final long DAY = 24 * HOUR;
	protected static final long WEEK = 7 * DAY;
	protected static final long YEAR = 365 * DAY; //(long) (365.2395 * DAY);
	protected static final long DECADE = 10 * YEAR;
	protected static final long CENTURY = 10 * DECADE;
	protected static final long MILLENNIUM = 10 * CENTURY;
	
	/** @param millis The time in milliseconds
	 * @return The time, in String format */
	public static String getElapsedTime(long millis) {
		return getElapsedTime(millis, false);
	}
	
	/** @param millis The time in milliseconds
	 * @param showMilliseconds Whether or not to show milliseconds(...:000)
	 * @return The time, in String format */
	public static String getElapsedTime(long millis, boolean showMilliseconds) {
		boolean negative = millis < 0;
		if(negative) {
			millis = Math.abs(millis);
		}
		String rtrn = "";
		if(millis >= MILLENNIUM) {
			long millenniums = millis / MILLENNIUM;
			millis %= MILLENNIUM;
			rtrn += millenniums + " Millennium" + (millenniums == 1 ? "" : "s") + " ";
		}
		if(millis >= CENTURY) {
			long centuries = millis / CENTURY;
			millis %= CENTURY;
			rtrn += centuries + " Centur" + (centuries == 1 ? "y" : "ies") + " ";
		}
		if(millis >= YEAR) {
			long years = millis / YEAR;
			millis %= YEAR;
			rtrn += years + " Year" + (years == 1 ? "" : "s") + " ";
		}
		if(millis >= WEEK) {
			long weeks = millis / WEEK;
			millis %= WEEK;
			rtrn += weeks + " Week" + (weeks == 1 ? "" : "s") + " ";
		}
		if(millis >= DAY) {
			long days = millis / DAY;
			millis %= DAY;
			rtrn += days + " Day" + (days == 1 ? "" : "s") + " and ";
		}
		long hours = 0L;
		if(millis >= HOUR) {
			hours = millis / HOUR;
			millis %= HOUR;
		}
		long minutes = 0L;
		if(millis >= MINUTE) {
			minutes = millis / MINUTE;
			millis %= MINUTE;
		}
		long seconds = 0L;
		if(millis >= SECOND) {
			seconds = millis / SECOND;
			millis %= SECOND;
		}
		long milliseconds = 0L;
		if(millis >= MILLISECOND && showMilliseconds) {
			milliseconds = millis / MILLISECOND;
			millis %= milliseconds;
		}
		final String hourStr = (hours == 0 ? "" : hours + ":");
		final String minuteStr = (minutes == 0 ? (hours != 0 ? "00:" : (seconds != 0 || milliseconds != 0 ? "0:" : "")) : (minutes < 10 ? "0" : "") + minutes + ":");
		final String secondStr = (hours == 0 && minutes == 0 && seconds == 0 && milliseconds == 0 ? "" : (seconds < 10 ? "0" : "") + seconds);
		rtrn += hourStr + minuteStr + secondStr + (milliseconds != 0 ? ":" + (milliseconds < 100 ? (milliseconds < 10 ? "00" : "0") : "") + milliseconds : "");
		rtrn = rtrn.endsWith("and ") ? rtrn.substring(0, rtrn.length() - 4).trim() : rtrn;
		rtrn += (negative ? " Remaining" : "");
		rtrn = rtrn.replace("  ", " ").trim();
		return rtrn.trim().isEmpty() ? "0:00" : rtrn;
	}
	
	/** @param str The text to edit
	 * @return The given text with its first letter capitalized */
	public static final String captializeFirstLetter(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	/** Pass-through method for
	 * {@link org.apache.commons.lang3.StringUtils#replaceOnce(String, String, String)}
	 * 
	 * @param text The input string
	 * @param searchString The string to replace
	 * @param replacement The replacement string
	 * @return The resulting string */
	public static final String replaceOnce(String text, String searchString, String replacement) {
		return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
	}
	
}
