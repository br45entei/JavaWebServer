package com.gmail.br45entei.util;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_401;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_404;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_408;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_421;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_500;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.data.DomainDirectory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public class ResponseUtil {
	
	public static final void send401Response(final Socket s, HTTPClientRequest request, HTTPServerResponse response, DomainDirectory domainDirectory) throws IOException {
		response.setStatusCode(HTTP_401);
		response.setHeader("Vary", "Accept-Encoding");
		response.setHeader("Date", StringUtils.getCurrentCacheTime());
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
		response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
		response.setResponse("<!DOCTYPE html>\r\n"//
				+ "<html>\r\n\t<head>\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
				+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
				+ "\t\t<title>401 - Unauthorized - " + domainDirectory.getServerName() + "</title>\r\n"//
				+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
				+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
				+ "\t</head>\r\n"//
				+ "\t<body>\r\n"//
				+ "\t\t<string>Access denied.</string>\r\n"//
				+ "\t</body>\r\n</html>");
		response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
	}
	
	public static final void send404Response(final Socket s, HTTPClientRequest request, HTTPServerResponse response, DomainDirectory domainDirectory, String pageHeader) throws IOException {
		//LogUtils.ORIGINAL_SYSTEM_OUT.print("DomainDirectory: " + domainDirectory);
		//new Throwable().printStackTrace(LogUtils.ORIGINAL_SYSTEM_OUT);
		response.setStatusCode(HTTP_404);
		response.setHeader("Vary", "Accept-Encoding");
		response.setHeader("Date", StringUtils.getCurrentCacheTime());
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
		response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
		response.setResponse("<!DOCTYPE html>\r\n"//
				+ "<html>\r\n\t<head>\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
				+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
				+ "\t\t<title>404 - File not Found!!!11 - " + domainDirectory.getServerName() + "</title>\r\n"//
				+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
				+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
				+ "\t</head>\r\n"//
				+ "\t<body>\r\n"//
				+ "\t\t<h1>Error 404 - File not found</h1><hr>\r\n"//
				+ "\t\t<string>The file \"" + request.requestedFilePath + "\" does not exist.</string><br>\r\n"//
				+ "\t\t<string>" + pageHeader + "</string>\r\n"//
				+ "\t</body>\r\n</html>");
		response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
	}
	
	public static final HTTPServerResponse send408Response(final Socket s, final ClientConnection reuse, final String clientAddress) throws IOException {
		String detectedEncodingOfStyleSheet;
		try {
			detectedEncodingOfStyleSheet = StringUtils.getDetectedEncoding(new File(FilenameUtils.normalize(JavaWebServer.homeDirectory.getAbsolutePath() + File.separatorChar + JavaWebServer.DEFAULT_STYLESHEET)));
		} catch(Throwable ignored) {
			detectedEncodingOfStyleSheet = null;
		}
		if(reuse.response == null) {
			reuse.response = new HTTPServerResponse(reuse, "HTTP/1.1", HTTP_408, false, StandardCharsets.UTF_8);
		} else {
			reuse.response.setStatusCode(HTTP_408).setCharset(StandardCharsets.UTF_8).setUseGZip(false);
			if(reuse.response.getHTTPVersion() == null || reuse.response.getHTTPVersion().trim().isEmpty()) {
				reuse.response.setHTTPVersion("HTTP/1.1");
			}
		}
		reuse.response.setHeader("Vary", "Accept-Encoding")//
				.setHeader("Date", StringUtils.getCurrentCacheTime())//
				.setHeader("Content-Type", "text/html; charset=UTF-8")//
				.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER)//
				.setHeader("Cache-Control", JavaWebServer.cachePrivateMustRevalidate)//
				.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + JavaWebServer.DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>408 - Request Timeout - " + JavaWebServer.SERVER_NAME + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + JavaWebServer.defaultFontFace + "\';}</style>\r\n"//
						+ (detectedEncodingOfStyleSheet != null ? "\t\t<link rel=\"stylesheet\" href=\"" + JavaWebServer.DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + detectedEncodingOfStyleSheet + "\">\r\n" : "")//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Error 408 Request Timeout</h1><hr>\r\n"//
						+ "\t\t<string>Your client did not send its request to this server for longer than <b>" + (JavaWebServer.requestTimeout / 1000L) + " seconds</b> and was timed out as a result.<br>Refreshing the page may fix the problem.</string><hr>\r\n"//
						+ "<b>" + (reuse.request == null || reuse.request.hostNoPort == null || reuse.request.hostNoPort.isEmpty() ? AddressUtil.getIp() : reuse.request.hostNoPort) + ":" + s.getLocalPort() + " --&gt; " + clientAddress + "</b>\r\n"//
						+ "\t</body>\r\n</html>");
		IOException exception = null;
		try {
			reuse.response.sendToClient(s, true);
		} catch(IOException e) {
			exception = e;
		}
		try {
			s.close();
		} catch(Throwable ignored) {
		}
		reuse.clearLogsNow();
		reuse.allowReuse = false;
		if(exception != null) {
			throw exception;
		}
		return reuse.response;
	}
	
	public static final void send421Response(final Socket s, final ClientConnection reuse, DomainDirectory domainDirectory) throws IOException {
		final String clientAddress = JavaWebServer.getClientAddress(s, reuse.request);
		final String pageHeader = "<hr><b>" + reuse.request.requestedFilePath + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
		reuse.response.setStatusCode(HTTP_421);
		reuse.response.setHeader("Vary", "Accept-Encoding");
		reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
		reuse.response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
		reuse.response.setHeader("Cache-Control", "public, max-age=" + (domainDirectory == null ? JavaWebServer.DEFAULT_CACHE_MAX_AGE.longValue() : domainDirectory.getCacheMaxAge()));
		reuse.response.setHeader("Location", "http://" + (domainDirectory == null ? AddressUtil.getIp() : domainDirectory.getDomain()) + reuse.request.requestedFilePath + reuse.request.requestArgumentsStr);
		reuse.response.setResponse("<!DOCTYPE html>\r\n"//
				+ "<html>\r\n\t<head>\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
				+ "\t\t<link rel=\"shortcut icon\" href=\"http://" + AddressUtil.getIp() + (domainDirectory == null ? JavaWebServer.DEFAULT_PAGE_ICON : domainDirectory.getDefaultPageIcon()) + "\" type=\"image/x-icon\">\r\n"//
				+ "\t\t<title>Error 421 Misdirected Request - " + (domainDirectory == null ? JavaWebServer.SERVER_NAME : domainDirectory.getServerName()) + "</title>\r\n"//
				+ "\t\t<style>body{font-family:\'" + (domainDirectory == null ? JavaWebServer.defaultFontFace : domainDirectory.getDefaultFontFace()) + "\';}</style>\r\n"//
				+ ((domainDirectory == null ? true : domainDirectory.doesDefaultStyleSheetExist()) ? "\t\t<link rel=\"stylesheet\" href=\"http://" + AddressUtil.getIp() + (domainDirectory == null ? JavaWebServer.DEFAULT_STYLESHEET : domainDirectory.getDefaultStylesheet()) + "\" type=\"text/css\" charset=\"" + (domainDirectory == null ? Charset.defaultCharset().name() : StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem())) + "\">\r\n" : "")//
				+ "\t</head>\r\n"//
				+ "\t<body>\r\n"//
				+ "\t\t<h1>Error 421 - Misdirected Request</h1><hr>\r\n"//
				+ "\t\t<string>The DNS Server that you/your client used to look up the ip address for the domain \"" + reuse.request.host + "\" has incorrectly returned this server's ip address(" + AddressUtil.getIp() + ") resulting in this page.<br>Reconfigure your DNS settings and try again.<br>If you are attempting to use this server as a proxy, this server's proxy setting is currently set to: Enabled: <b>" + JavaWebServer.isProxyServerEnabled() + "</b>; Password protected: <b>" + JavaWebServer.proxyRequiresAuthorization + "</b>.</string><br><br>\r\n"//
				+ "\t\t<string>Alternately, if you are a server administrator and this domain is not improperly configured, <a href=\"http://" + AddressUtil.getClientAddressNoPort(reuse.request.host) + ":" + JavaWebServer.admin_listen_port + "/?page=createDomain&domainName=" + reuse.request.host + "\" target=\"_blank\">log in</a> to create a new domain with the name \"" + reuse.request.host + "\" (thereby preventing this page from showing).</string>\r\n"//
				+ "\t\t<string>" + pageHeader + "</string>\r\n"//
				+ "\t</body>\r\n</html>");
		reuse.response.sendToClient(s, reuse.request.method.equalsIgnoreCase("GET"));
		reuse.println("\t\t*** A client has incorrectly connected to this server thinking it has connected to \"" + reuse.request.host + "\".");
		if(domainDirectory != null) {
			domainDirectory.delete();//It didn't exist before this bad request occurred, so why keep it?
			domainDirectory = null;
			System.gc();
		}
	}
	
	public static final void send500InternalServerError(final Socket s, final ClientConnection reuse, DomainDirectory domainDirectory, Throwable exception, boolean hideDirectoryStrings) {
		final String clientAddress = JavaWebServer.getClientAddress(s, reuse.request);
		final String pageHeader = "<hr><b>" + reuse.request.requestedFilePath + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
		reuse.response.setStatusCode(HTTP_500);
		reuse.response.clearHeaders();
		reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
		reuse.response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
		reuse.response.setHeader("Cache-Control", JavaWebServer.cachePrivateMustRevalidate);
		reuse.response.setHeader("Date", StringUtil.getCurrentCacheTime());
		String exceptionStr = exception == null ? null : StringUtil.throwableToStr(exception).replace(File.separator, "/");
		if(exceptionStr != null && hideDirectoryStrings && domainDirectory != null) {
			exceptionStr = StringUtils.replaceOnce(exceptionStr.replace(domainDirectory.getDirectorySafe().getAbsolutePath().replace(File.separator, "/") + "/", "/"), "//", "/");//.replace(domainDirectory.getURLPathPrefix().replace(File.separator, "/") + "/", "[1]");
		}
		try {
			reuse.response.setResponse("<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + JavaWebServer.DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
					+ "\t\t<title>500 - Internal Server Error!!!1 - " + JavaWebServer.SERVER_NAME + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + JavaWebServer.defaultFontFace + "\';}</style>\r\n"//
					+ ((domainDirectory == null ? true : domainDirectory.doesDefaultStyleSheetExist()) ? "\t\t<link rel=\"stylesheet\" href=\"" + (domainDirectory == null ? JavaWebServer.DEFAULT_STYLESHEET : domainDirectory.getDefaultStylesheet()) + "\" type=\"text/css\" charset=\"" + (domainDirectory == null ? Charset.defaultCharset().name() : StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem())) + "\">\r\n" : "")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>500 - Internal Server Error</h1><hr>\r\n"//
					+ "\t\t<string>This server encountered an unexpected error that prevented it from fulfilling the request.</string>\r\n"//
					+ (exceptionStr == null ? "\t\t<string>Unfortunately, no stack traces are available...</string>\r\n" : "\t\t<hr><b><string>Stack Trace:</string></b><br><pre>" + exceptionStr + "</pre>\r\n")//
					+ "\t\t<string>" + pageHeader + "</string>\r\n"//
					+ "\t</body>\r\n</html>");
		} catch(Throwable ignored) {
		}
		try {
			reuse.response.sendToClient(s, true);
		} catch(Throwable ignored) {
			reuse.allowReuse = false;
		}
		if(!JavaWebServer.isExceptionIgnored(exception)) {
			reuse.print("\t\t*** An unexpected exception has occurred:\r\n" + StringUtil.throwableToStr(exception, "\n"));
		}
	}
	
}
