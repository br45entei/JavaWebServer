package com.gmail.br45entei.server.data.php;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.data.serverIO.InputStreamSSLWrapper;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.HTTPStatusCodes;
import com.gmail.br45entei.server.Header;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public final class PhpResult {
	
	public static final void main(String[] args) {
		String filePath = StringUtil.stringArrayToString(' ', args);
		if(filePath.isEmpty()) {
			System.err.println("Usage: java.exe -jar phpResult.jar path/to/the php/file.php");
			return;
		}
		File file = new File(filePath);
		if(!file.isFile()) {
			System.err.println("The file \"" + filePath + "\" does not exist.");
			return;
		}
		if(!FileUtil.isFileAccessible(file)) {
			System.err.println("The file \"" + filePath + "\" is not accessible.");
			return;
		}
		if(!FilenameUtils.getExtension(filePath).equalsIgnoreCase("php")) {
			System.err.println("The file \"" + filePath + "\" is not a *.php file.");
			return;
		}
		phpExeFilePath = "C:\\php\\php.exe";//"C:\\php\\php-cgi.exe";
		//String spoofedArgs = "?mode=install&amp;sub=requirements&amp;language=en";
		String spoofedArgs = "";//"?mode=install&language=en";//"?test1=1&test2=hi!";
		String requestArgumentsStr = spoofedArgs.replace("?", "").replace("&", " ");
		try {
			ClientConnection reuse = ClientConnection.getNewConn(null);
			HTTPClientRequest request = new HTTPClientRequest(null, null, false, reuse);
			request.requestedFilePath = "/phpBB3";
			request.postRequestData = FileUtil.readFileData(new File("E:\\Java\\Minecraft\\BlockWorlds\\Joapple\\PHP_TEST\\layout.css"));
			
			//File test = new File("E:\\Java\\Joapple\\PHP_TEST\\layout.css");
			//request.headers.add(new Header("Content-Length: " + StringUtil.longToString(FileUtil.getSize(test))));
			//request.headers.add(new Header("Cookie", "pma_lang=en; expires=Mon, 17-Apr-2017 05:56:27 GMT; Max-Age=2592000; path=/; httponly"));
			//request.headers.add(new Header("Cookie", "pma_collation_connection=utf8mb4_unicode_ci; expires=Mon, 17-Apr-2017 05:56:27 GMT; Max-Age=2592000; path=/; httponly"));
			//request.headers.add(new Header("Cookie", "phpMyAdmin=umd7n2dbi41sudfqh4lnjmlj89mkkh21; path=/; HttpOnly"));
			//request.headers.add(new Header("Cookie", "pmaUser-1=%7B%22iv%22%3A%221OcbOsy%2BQ7vlYCa9Qm45ug%3D%3D%22%2C%22mac%22%3A%22bde7c7d1a105826e4bc8380531225ca356687dc9%22%2C%22payload%22%3A%229cPAHlUW2ECIHa8zkBp5ww%3D%3D%22%7D; expires=Mon, 17-Apr-2017 05:56:27 GMT; Max-Age=2592000; path=/; httponly"));//brb bathroom lol
			//request.headers.add(new Header("Cookie", "pmaAuth-1=%7B%22iv%22%3A%22LsSy4pMevSIimSrxeXSvjQ%3D%3D%22%2C%22mac%22%3A%22b1e6c60f741da62e2baa15c9118a6401734943e0%22%2C%22payload%22%3A%22%5C%2FQF813aLVBCO5pHa47K5wR126Gr5rNlS5KIc%5C%2FSZznfEtdKoAnYl6XEWMUFs0nAsH%22%7D; path=/; httponly"));
			HTTPServerResponse response = new HTTPServerResponse(reuse, "HTTP/1.1", HTTPStatusCodes.HTTP_NOT_SET, false, StandardCharsets.UTF_8);
			execPHP(filePath, requestArgumentsStr, "localhost", reuse, request, response);
			reuse.printLogsNow();
			//phpTestFlag = !phpTestFlag;
			getLastPHPExecResponse().copyToServerResponse(response, false, false);
			String toString = "Body data:\n\n" + new String(response.getResponseData(), StandardCharsets.UTF_8);//getLastPHPExecResponse().toString();
			System.out.println("getLastPHPExecResponse() == null: " + (getLastPHPExecResponse() == null));
			System.out.println("[unCachePHPExecResponse(...);]");
			unCachePHPExecResponse(getLastPHPExecResponse());
			System.out.println("getLastPHPExecResponse() == null: " + (getLastPHPExecResponse() == null));
			System.out.println(toString);//System.out.println(result1.toString());
			System.out.flush();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	/** The path to the main PHP executable file */
	public static volatile String phpExeFilePath = "./php/php-cgi.exe";
	
	private static volatile File phpGetInfoFile = null;
	private static volatile boolean lastPhpTestFlag = false;
	
	private static volatile boolean phpTestFlag = false;
	
	public final HTTPStatusCodes code;
	public final String xPoweredBy;
	public final String location;
	public final Collection<Header> headers;
	public volatile byte[] body;
	
	public PhpResult(HTTPStatusCodes code, String xPoweredBy, String location, Collection<Header> headers, byte[] body) {
		this.code = code;
		this.xPoweredBy = xPoweredBy;
		this.location = location;
		this.headers = headers;
		this.body = StringUtil.removeTrailingNewLineCharsFrom(body);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.body);
		result = prime * result + ((this.code == null) ? 0 : this.code.hashCode());
		result = prime * result + ((this.headers == null) ? 0 : this.headers.hashCode());
		result = prime * result + ((this.xPoweredBy == null) ? 0 : this.xPoweredBy.hashCode());
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
		if(!(obj instanceof PhpResult)) {
			return false;
		}
		PhpResult other = (PhpResult) obj;
		if(!Arrays.equals(this.body, other.body)) {
			return false;
		}
		if(this.code != other.code) {
			return false;
		}
		if(this.headers == null) {
			if(other.headers != null) {
				return false;
			}
		} else if(!this.headers.equals(other.headers)) {
			return false;
		}
		if(this.xPoweredBy == null) {
			if(other.xPoweredBy != null) {
				return false;
			}
		} else if(!this.xPoweredBy.equals(other.xPoweredBy)) {
			return false;
		}
		return true;
	}
	
	public static final boolean isPhpFilePresent() {
		File phpExeFile = new File(phpExeFilePath);
		return phpExeFile.exists();
	}
	
	private static final File getPHPGetInfoFile() {
		if(phpGetInfoFile == null || phpTestFlag != lastPhpTestFlag) {
			phpGetInfoFile = new File(JavaWebServer.rootDir, "phpGetInfo.php");
			try(DualPrintWriter pr = new DualPrintWriter(new UnlockedOutputStreamWriter(new FileOutputStream(phpGetInfoFile), StandardCharsets.UTF_8), true)) {
				pr.setLineSeparator("\r\n");
				if(phpTestFlag) {
					pr.println("<?php");
					//pr.println("// Get the new response code"); //lol, comment-ception XD
					pr.println("var_dump(http_response_code());");
					pr.println("?>");
				} else {
					pr.println("<?php");
					pr.println("mixed curl_getinfo ( resource $ch [, int $opt ] )");
					pr.println("?>");
				}
				lastPhpTestFlag = phpTestFlag;
				pr.flush();
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}
		return phpGetInfoFile;
	}
	
	@Override
	public final String toString() {
		return (this.code == null ? "[null HTTPStatusCode{!}]\r\n" : "HTTP/1.1 " + this.code.toString() + "\r\n") + //
				(this.xPoweredBy == null ? "[null xPoweredBy]\r\n" : "X-Powered-By: " + this.xPoweredBy + "\r\n") + //
				(this.location == null ? "[null location]\r\n" : "Location: " + this.location + "\r\n") + //
				(this.headers == null ? "[null headers]\r\n" : Header.headersToString(this.headers) + "\r\n") + //
				(this.body == null ? "[null body]" : "[body size, in bytes: " + this.body.length + "(" + Functions.humanReadableByteCount(this.body.length, true, 2) + ")]" + (this.body.length == 0 ? "" : "\r\nBody data:\r\n\r\n") + new String(this.body, StandardCharsets.UTF_8));
	}
	
	private static final String fixValueStr(String header, String value, String host, ClientConnection reuse, String currentFolderPath) {
		currentFolderPath = currentFolderPath == null || !currentFolderPath.startsWith("/") ? "/" + (currentFolderPath == null ? "" : currentFolderPath) : currentFolderPath.toString();
		currentFolderPath = !currentFolderPath.endsWith("/") ? currentFolderPath + "/" : currentFolderPath;
		//currentFolderPath = currentFolderPath.equals("/") ? "/phpBB3/" : currentFolderPath;
		value = value.startsWith(" ") ? value.substring(1) : value;
		if(value.toLowerCase().startsWith("http:///")) {
			reuse.printlnDebug("value: " + value);
			String protocol = value.substring(0, "http://".length());
			value = value.substring(protocol.length() + 1);
			reuse.printlnDebug("value: " + value);
			String prefix = protocol + host;
			reuse.printlnDebug("prefix: " + prefix);
			reuse.printlnDebug("currentFolderPath: \"" + currentFolderPath + "\"");
			value = prefix + currentFolderPath + value;
			reuse.printlnDebug("value: " + value);
			//protocol + host + currentFolderPath + 
		} else if(value.toLowerCase().startsWith("https:///")) {
			String protocol = value.substring(0, "https://".length());
			value = value.substring(protocol.length() + 1);
			String prefix = protocol + host;
			value = prefix + currentFolderPath + value;
		} else if(value.toLowerCase().startsWith("ftp:///")) {
			String protocol = value.substring(0, "ftp://".length());
			value = value.substring(protocol.length() + 1);
			String prefix = protocol + host;
			value = prefix + currentFolderPath + value;
		}
		reuse.printlnDebug(header + ": " + value);
		return value;
	}
	
	private static final void passClientRequestToPHPCGIProcess(OutputStream out, HTTPClientRequest request) throws IOException {
		Collection<Header> headers = new ArrayList<>(request.headers);//Header.collectClientCookies(request.headers);
		Header.removeHeaderFrom(headers, "Authorization");
		Header.removeHeaderFrom(headers, "Content-Length");
		PrintStream pr = new PrintStream(out);
		pr.println(request.protocolLine);
		for(Header header : headers) {
			header.println(pr);
		}
		pr.println("Content-length: " + Integer.toString(request.postRequestData.length));
		pr.println("\r\n");
		pr.flush();
		passClientRequestDataToPHPCGIProcess(out, request);
	}
	
	private static final void passClientRequestDataToPHPCGIProcess(OutputStream out, HTTPClientRequest request) throws IOException {
		if(request.postRequestData.length > 0) {
			out.write(request.postRequestData, 0, request.postRequestData.length);
			out.flush();
		}
	}
	
	public static final void execPHP(String scriptPath, String param, final String host, final ClientConnection reuse, HTTPClientRequest request, HTTPServerResponse response) {
		String httpStatusCode = null;
		String xPoweredBy = null;
		String location = null;
		final ArrayList<Header> clientHeaders = request == null ? new ArrayList<>() : new ArrayList<>(request.headers);
		final ArrayList<Header> headers = new ArrayList<>();
		Header.removeHeaderFrom(clientHeaders, "Authorization");
		Header.removeHeaderFrom(headers, "Authorization");
		byte[] body = null;
		try(DisposableByteArrayOutputStream data = new DisposableByteArrayOutputStream()) {
			ArrayList<String> cmds = new ArrayList<>();
			cmds.add(phpExeFilePath);
			cmds.add("-f");
			cmds.add(scriptPath);
			{
				String[] split = param.split(Pattern.quote(" "));
				for(String s : split) {
					if(!s.trim().isEmpty()) {
						cmds.add(s);
					}
				}
			}
			/*if(param != null && !param.trim().isEmpty() && param.contains(" ")) {
				String[] split = param.split(Pattern.quote(" "));
				for(String s : split) {
					if(s.startsWith(" ") && s.length() > 1) {
						s = s.substring(1);
					}
					if(s.endsWith(" ") && s.length() > 1) {
						s = s.substring(0, s.length() - 1);
					}
					if(!s.trim().isEmpty()) {
						cmds.add(s);
					}
				}
			}*/
			ProcessBuilder pb = new ProcessBuilder(cmds);
			LogUtils.ORIGINAL_SYSTEM_ERR.println("\n\nPHP PROCESS COMMAND LINE:");
			for(String arg : pb.command()) {
				LogUtils.ORIGINAL_SYSTEM_ERR.println(arg);
			}
			LogUtils.ORIGINAL_SYSTEM_ERR.println("\n");
			Process p = pb.start();
			@SuppressWarnings("resource")
			OutputStream out = p.getOutputStream();
			passClientRequestToPHPCGIProcess(out, request);//passClientCookiesToPHPCGIProcess(out, clientHeaders);
			/*if(request != null && request.postRequestData != null && request.postRequestData.length > 0) {
				out.write(request.postRequestData, 0, request.postRequestData.length);
				out.flush();
			}*/
			
			//Process p = Runtime.getRuntime().exec(phpExeFilePath + " " + scriptName + (param == null || param.isEmpty() ? "" : " " + param));
			//BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedInputStream in = (BufferedInputStream) p.getInputStream();
			//long headerContentLength = -1L;
			String line;
			boolean readYet = false;
			while((line = StringUtil.readLine(in)) != null) {
				if(!line.isEmpty()) {
					if(line.contains("PHP Startup:")) {
						reuse.println("[PHP-CGI] " + line);
						continue;
					}
					if(!line.contains(":")) {//line.equalsIgnoreCase("<!DOCTYPE html>")) {//Sometimes the php script doesn't return server headers or the status code,
						//sometimes it only leaves off the status code, and sometimes it includes both... it kind of depends on the script it seems... so strange.
						data.write((line + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
						readResultFromPHPCGI(in, data, reuse);
						break;
					}
					String[] split = line.split(Pattern.quote(":"));
					String header = split[0];
					String value = fixValueStr(header, StringUtil.stringArrayToString(split, ':', 1), host, reuse, request == null ? "" : HTTPClientRequest.getRequestedPathParent(request.requestedFilePath, true));
					if(header.equalsIgnoreCase("Status")) {
						httpStatusCode = value;
						reuse.println("HTTPStatusCode: " + httpStatusCode);
						HTTPStatusCodes check = HTTPStatusCodes.fromString(httpStatusCode);
						reuse.println("Converted: " + (check == null ? "[null!]" : check.toString()));
					} else if(header.equalsIgnoreCase("X-Powered-By")) {
						xPoweredBy = value;
						reuse.println(header + ": " + value);
					} else if(header.equalsIgnoreCase("Location")) {
						location = value;
						reuse.println(header + ": " + value);
					} else {
						/*if(header.equalsIgnoreCase("Content-Length") && StringUtil.isStrLong(value)) {
							headerContentLength = Long.parseLong(value);
						}*/
						//headers.add(new Header(header, value));
						//reuse.println(header + ": " + value);
						break;
					}
					
					//reuse.println(new Header(header, value).toString());
				} else {
					reuse.printlnDebug(line);
					reuse.printlnDebug("Line was empty!");
					readResultFromPHPCGI(in, data, reuse);
					readYet = true;
					break;
				}
			}
			if(!readYet) {
				reuse.println("The PHP CGI yielded no blank space!!!1");
				if(line != null) {
					reuse.println("Putting line \"" + line + "\" back into the input stream...");
					line = line + "\n";//System.getProperty("line.separator");
					@SuppressWarnings("resource")
					InputStreamSSLWrapper input = new InputStreamSSLWrapper(in);
					input.addBytesToInternalBuffer(line.getBytes(StandardCharsets.UTF_8));
					readResultFromPHPCGI(input, data, reuse);
				} else {
					reuse.printlnDebug("No line to put back in...");
					readResultFromPHPCGI(in, data, reuse);
				}
			}
			body = data.toByteArray();
			//LogUtils.ORIGINAL_SYSTEM_ERR.println(new String(body, StandardCharsets.UTF_8));
			try {
				p.destroyForcibly();
			} catch(Throwable ignored) {
			}
			try {
				in.close();
				//out.close();
			} catch(Throwable ignored) {
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		if(httpStatusCode == null) {
			if(location != null) {
				httpStatusCode = HTTPStatusCodes.HTTP_302_1.toString();
			} else {
				httpStatusCode = HTTPStatusCodes.HTTP_200.toString();
			}
		}
		PhpResult result = new PhpResult(HTTPStatusCodes.fromString(httpStatusCode), xPoweredBy, location, headers, body);
		registerResult(result);
		if(response != null) {
			result.copyToServerResponse(response, true, true);
		}
		//return new PhpResult(HTTPStatusCodes.fromString(httpStatusCode), xPoweredBy, location, headers, body);
	}
	
	public static final void execPHP_TEST(String scriptPath, String param, final String host, final ClientConnection reuse, HTTPClientRequest request, HTTPServerResponse response) {
		String httpStatusCode = null;
		String xPoweredBy = null;
		String location = null;
		final ArrayList<Header> clientHeaders = request == null ? new ArrayList<>() : new ArrayList<>(request.headers);
		final ArrayList<Header> headers = new ArrayList<>();
		Header.removeHeaderFrom(clientHeaders, "Authorization");
		Header.removeHeaderFrom(headers, "Authorization");
		byte[] body = null;
		try(DisposableByteArrayOutputStream data = new DisposableByteArrayOutputStream()) {
			ArrayList<String> cmds = new ArrayList<>();
			cmds.add(phpExeFilePath);
			cmds.add("-f");
			cmds.add(scriptPath);
			/*{
				String[] split = param.split(Pattern.quote(" "));
				for(String s : split) {
					if(!s.trim().isEmpty()) {
						cmds.add(s);
					}
				}
			}*/
			/*if(param != null && !param.trim().isEmpty() && param.contains(" ")) {
				String[] split = param.split(Pattern.quote(" "));
				for(String s : split) {
					if(s.startsWith(" ") && s.length() > 1) {
						s = s.substring(1);
					}
					if(s.endsWith(" ") && s.length() > 1) {
						s = s.substring(0, s.length() - 1);
					}
					if(!s.trim().isEmpty()) {
						cmds.add(s);
					}
				}
			}*/
			ProcessBuilder pb = new ProcessBuilder(cmds);
			Process p = pb.start();
			@SuppressWarnings("resource")
			OutputStream out = p.getOutputStream();
			passClientRequestToPHPCGIProcess(out, request);
			
			//Process p = Runtime.getRuntime().exec(phpExeFilePath + " " + scriptName + (param == null || param.isEmpty() ? "" : " " + param));
			//BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedInputStream in = (BufferedInputStream) p.getInputStream();
			//long headerContentLength = -1L;
			String line;
			while((line = StringUtil.readLine(in)) != null) {
				if(!line.isEmpty()) {
					if(line.contains("PHP Startup:")) {
						reuse.println("[PHP-CGI] " + line);
						continue;
					}
					if(!line.contains(":")) {//line.equalsIgnoreCase("<!DOCTYPE html>")) {//Sometimes the php script doesn't return server headers or the status code,
						//sometimes it only leaves off the status code, and sometimes it includes both... it kind of depends on the script it seems... so strange.
						data.write((line + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
						readResultFromPHPCGI(in, data, reuse);
						break;
					}
					String[] split = line.split(Pattern.quote(":"));
					String header = split[0];
					String value = fixValueStr(header, StringUtil.stringArrayToString(split, ':', 1), host, reuse, request == null ? "" : HTTPClientRequest.getRequestedPathParent(request.requestedFilePath, true));
					if(header.equalsIgnoreCase("Status")) {
						httpStatusCode = value;
						reuse.printlnDebug("HTTPStatusCode: " + httpStatusCode);
						HTTPStatusCodes check = HTTPStatusCodes.fromString(httpStatusCode);
						reuse.printlnDebug("Converted: " + (check == null ? "[null!]" : check.toString()));
					} else if(header.equalsIgnoreCase("X-Powered-By")) {
						xPoweredBy = value;
						reuse.printlnDebug(header + ": " + value);
					} else if(header.equalsIgnoreCase("Location")) {
						location = value;
						reuse.printlnDebug(header + ": " + value);
					} else {
						/*if(header.equalsIgnoreCase("Content-Length") && StringUtil.isStrLong(value)) {
							headerContentLength = Long.parseLong(value);
						}*/
						headers.add(new Header(header, value));
						reuse.printlnDebug(header + ": " + value);
					}
					
					//reuse.println(new Header(header, value).toString());
				} else {
					reuse.printlnDebug(line);
					reuse.printlnDebug("Line was empty!");
					readResultFromPHPCGI(in, data, reuse);
					break;
				}
			}
			body = data.toByteArray();
			try {
				p.destroyForcibly();
			} catch(Throwable ignored) {
			}
			try {
				in.close();
				//out.close();
			} catch(Throwable ignored) {
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		if(httpStatusCode == null) {
			if(location != null) {
				httpStatusCode = HTTPStatusCodes.HTTP_302_1.toString();
			} else {
				httpStatusCode = HTTPStatusCodes.HTTP_200.toString();
			}
		}
		PhpResult result = new PhpResult(HTTPStatusCodes.fromString(httpStatusCode), xPoweredBy, location, headers, body);
		registerResult(result);
		if(response != null) {
			result.copyToServerResponse(response, true, true);
		}
		//return new PhpResult(HTTPStatusCodes.fromString(httpStatusCode), xPoweredBy, location, headers, body);
	}
	
	private static final void readResultFromPHPCGI(InputStream in, DisposableByteArrayOutputStream data, ClientConnection reuse) throws Throwable {
		/*Long getContentLength = Header.getContentLengthFrom(headers);
		if(getContentLength == null || getContentLength.longValue() == -1L) {
			getContentLength = Long.valueOf(headerContentLength);
		}
		final long contentLength = getContentLength == null ? -1L : getContentLength.longValue();
		if(contentLength > 0) {
			byte[] buf = new byte[4096];
			long count = 0;
			long remaining = contentLength - count;
			int read;
			while((read = in.read(buf, 0, Long.valueOf(remaining < buf.length ? remaining : buf.length).intValue())) >= 0) {
				count += read;
				remaining = contentLength - count;
				data.write(buf, 0, read);
				if(remaining <= 0) {
					break;
				}
			}
			reuse.println("last read: " + read + "(" + ((char) read) + ")");
			buf = new byte[0];
			System.gc();
		} else {
			Functions.sleep();
			if(in.available() > 0) {*/
		byte[] buf = new byte[4096];
		int read = -1;
		int lastRead = read;
		while((read = in.read(buf, 0, buf.length)) >= 0) {
			data.write(buf, 0, read);
			/*if(in.available() == 0) {
				Functions.sleep();
				if(in.available() == 0) {
					break;
				}
			}*/
			lastRead = read;
		}
		reuse.println("last read: " + lastRead);
		buf = new byte[0];
		System.gc();
	}
	
	private static final class CachedPhpResult {
		
		public volatile Thread thread;
		public volatile PhpResult result;
		private final long timeCached;
		
		public CachedPhpResult(PhpResult result) {
			this.thread = Thread.currentThread();
			this.result = result;
			this.timeCached = System.currentTimeMillis();
		}
		
		public final long getTimeCached() {
			return this.timeCached;
		}
		
	}
	
	private static final ConcurrentLinkedDeque<CachedPhpResult> lastPHPExecResponses = new ConcurrentLinkedDeque<>();
	
	protected static final CachedPhpResult getCachedResultForCurrentThread(PhpResult result) {
		return getCachedResultFor(Thread.currentThread(), result);
	}
	
	protected static final CachedPhpResult getCachedResultFor(Thread thread, PhpResult result) {
		for(CachedPhpResult cached : lastPHPExecResponses) {
			if(cached.thread == thread && cached.result == result) {
				return cached;
			}
		}
		return null;
	}
	
	protected static final PhpResult registerResult(PhpResult result) {
		lastPHPExecResponses.addFirst(new CachedPhpResult(result));
		return result;
	}
	
	public static final int unregisterPHPExecResponsesForCurrentThread() {
		return unregisterPHPExecResponsesFor(Thread.currentThread());
	}
	
	public static final int unregisterPHPExecResponsesFor(Thread thread) {
		int numRemoved = 0;
		for(CachedPhpResult cached : lastPHPExecResponses) {
			if(cached.thread == thread) {
				lastPHPExecResponses.remove(cached);
				cached.thread = null;
				cached.result = null;
				cached = null;
				numRemoved++;
			}
		}
		if(numRemoved > 0) {
			System.gc();
		}
		return numRemoved;
	}
	
	public static final PhpResult getLastPHPExecResponse() {
		final Thread thread = Thread.currentThread();
		long lastTimeCached = -1L;
		CachedPhpResult rtrn = null;
		for(CachedPhpResult cached : lastPHPExecResponses) {
			if(cached.thread == thread) {
				if(lastTimeCached == -1L || cached.getTimeCached() > lastTimeCached) {
					rtrn = cached;
					lastTimeCached = cached.getTimeCached();
				}
			}
		}
		return rtrn == null ? null : rtrn.result;
	}
	
	public static final void unCachePHPExecResponse(PhpResult result) {
		unCachePHPExecResponse(getCachedResultForCurrentThread(result));
	}
	
	public static final void unCachePHPExecResponse(CachedPhpResult result) {
		if(result != null) {
			lastPHPExecResponses.remove(result);
		}
	}
	
	public static final PhpResult getPHPGetInfoResult(final String host, ClientConnection reuse) {
		String httpStatusCode = null;
		String xPoweredBy = null;
		String location = null;
		final ArrayList<Header> headers = new ArrayList<>();
		byte[] body = null;
		try(DisposableByteArrayOutputStream data = new DisposableByteArrayOutputStream()) {
			Process p = Runtime.getRuntime().exec(phpExeFilePath + " " + getPHPGetInfoFile().getAbsolutePath());
			InputStream in = p.getInputStream();
			long headerContentLength = -1L;
			String line;
			while((line = StringUtil.readLine(in)) != null) {
				reuse.printlnDebug(line);
				if(!line.isEmpty()) {
					String[] split = line.split(Pattern.quote(":"));
					String header = split[0];
					String value = fixValueStr(header, StringUtil.stringArrayToString(split, ':', 1), host, reuse, "");
					if(header.equalsIgnoreCase("Status")) {
						httpStatusCode = value;
						reuse.printlnDebug("HTTPStatusCode: " + httpStatusCode);
						HTTPStatusCodes check = HTTPStatusCodes.fromString(httpStatusCode);
						reuse.printlnDebug("Converted: " + (check == null ? "[null!]" : check.toString()));
					} else if(header.equalsIgnoreCase("X-Powered-By")) {
						xPoweredBy = value;
					} else if(header.equalsIgnoreCase("Location")) {
						location = value;
					} else {
						if(header.equalsIgnoreCase("Content-Length") && StringUtil.isStrLong(value)) {
							headerContentLength = Long.parseLong(value);
						}
						headers.add(new Header(header, value));
					}
				} else {
					reuse.printlnDebug("Line was empty!");
					Long getContentLength = Header.getContentLengthFrom(headers);
					if(getContentLength == null || getContentLength.longValue() == -1L) {
						getContentLength = Long.valueOf(headerContentLength);
					}
					final long contentLength = getContentLength == null ? -1L : getContentLength.longValue();
					if(contentLength > 0) {
						byte[] buf = new byte[4096];
						long count = 0;
						long remaining = contentLength - count;
						int read;
						while((read = in.read(buf, 0, Long.valueOf(remaining < buf.length ? remaining : buf.length).intValue())) >= 0) {
							count += read;
							remaining = contentLength - count;
							data.write(buf, 0, read);
							if(remaining <= 0) {
								break;
							}
						}
						buf = new byte[0];
						System.gc();
					} else {
						Functions.sleep(10L);
						if(in.available() > 0) {
							byte[] buf = new byte[4096];
							int read;
							while((read = in.read(buf, 0, buf.length)) >= 0) {
								data.write(buf, 0, read);
								if(in.available() == 0) {
									Functions.sleep(10L);
									if(in.available() == 0) {
										break;
									}
								}
							}
							buf = new byte[0];
							System.gc();
						}
					}
					break;
				}
			}
			body = data.toByteArray();
			//input.close();
			in.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return new PhpResult(HTTPStatusCodes.fromString(httpStatusCode), xPoweredBy, location, headers, body);
	}
	
	public final void close() {
		boolean didAnything = false;
		if(this.body != null) {
			this.body = new byte[0];
			didAnything = true;
		}
		if(didAnything) {
			System.gc();
		}
	}
	
	/** @param response The HTTPServerResponse whose response will be set to
	 *            this PHP result's data
	 * @return This PhpResult */
	public final PhpResult copyToServerResponse(HTTPServerResponse response, boolean ignoreContentLength, boolean ignoreContentType) {
		response.setStatusCode(this.code == null ? HTTPStatusCodes.HTTP_200 : this.code);
		if(this.body != null && this.body.length > 0) {
			response.setResponse(this.body);
		}
		if(this.xPoweredBy != null) {
			response.setHeader("X-Powered-By", this.xPoweredBy);
		}
		if(this.location != null) {
			response.setHeader("Location", this.location);
		}
		for(Header header : this.headers) {
			if(!(ignoreContentLength && header.header.equalsIgnoreCase("Content-Length")) && !(ignoreContentType && header.header.equalsIgnoreCase("Content-Type"))) {
				response.setHeader(header);
			}
		}
		return this;
	}
	
}
