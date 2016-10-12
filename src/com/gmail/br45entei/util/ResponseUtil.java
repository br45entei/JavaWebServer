package com.gmail.br45entei.util;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientInfo;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.data.DomainDirectory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_404;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_408;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_421;

/** @author Brian_Entei */
public class ResponseUtil {
	
	public static final void send404Response(final Socket s, HTTPClientRequest request, HTTPServerResponse response, ClientInfo clientInfo, DomainDirectory domainDirectory, String pageHeader) throws IOException {
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
		JavaWebServer.connectedClients.remove(clientInfo);
	}
	
	public static final void send408Response(final Socket s, final String clientAddress, HTTPClientRequest request) throws IOException {
		String detectedEncodingOfStyleSheet;
		try {
			detectedEncodingOfStyleSheet = StringUtils.getDetectedEncoding(new File(FilenameUtils.normalize(JavaWebServer.homeDirectory.getAbsolutePath() + File.separatorChar + JavaWebServer.DEFAULT_STYLESHEET)));
		} catch(Throwable ignored) {
			detectedEncodingOfStyleSheet = null;
		}
		new HTTPServerResponse("HTTP/1.1", HTTP_408, false, StandardCharsets.UTF_8, request)//
		.setHeader("Vary", "Accept-Encoding")//
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
				+ "<b>" + AddressUtil.getIp() + ":" + s.getLocalPort() + " --&gt; " + clientAddress + "</b>\r\n"//
				+ "\t</body>\r\n</html>").sendToClient(s, true);
	}
	
	public static final void send421Response(final Socket s, HTTPClientRequest request, HTTPServerResponse response, ClientInfo clientInfo, DomainDirectory domainDirectory, String pageHeader) throws IOException {
		response.setStatusCode(HTTP_421);
		response.setHeader("Vary", "Accept-Encoding");
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
		response.setHeader("Cache-Control", "public, max-age=" + (domainDirectory == null ? JavaWebServer.DEFAULT_CACHE_MAX_AGE.longValue() : domainDirectory.getCacheMaxAge()));
		response.setHeader("Location", (domainDirectory == null ? AddressUtil.getIp() : domainDirectory.getDomain()) + request.requestedFilePath + request.requestArgumentsStr);
		response.setResponse("<!DOCTYPE html>\r\n"//
				+ "<html>\r\n\t<head>\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
				+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
				+ "\t\t<link rel=\"shortcut icon\" href=\"" + (domainDirectory == null ? JavaWebServer.DEFAULT_PAGE_ICON : domainDirectory.getDefaultPageIcon()) + "\" type=\"image/x-icon\">\r\n"//
				+ "\t\t<title>Error 421 Misdirected Request - " + (domainDirectory == null ? JavaWebServer.SERVER_NAME : domainDirectory.getServerName()) + "</title>\r\n"//
				+ "\t\t<style>body{font-family:\'" + (domainDirectory == null ? JavaWebServer.defaultFontFace : domainDirectory.getDefaultFontFace()) + "\';}</style>\r\n"//
				+ ((domainDirectory == null ? true : domainDirectory.doesDefaultStyleSheetExist()) ? "\t\t<link rel=\"stylesheet\" href=\"" + (domainDirectory == null ? JavaWebServer.DEFAULT_STYLESHEET : domainDirectory.getDefaultStylesheet()) + "\" type=\"text/css\" charset=\"" + (domainDirectory == null ? Charset.defaultCharset().name() : StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem())) + "\">\r\n" : "")//
				+ "\t</head>\r\n"//
				+ "\t<body>\r\n"//
				+ "\t\t<h1>Error 421 - Misdirected Request</h1><hr>\r\n"//
				+ "\t\t<string>The DNS Server that you/your client used to look up the ip address for the domain \"" + request.host + "\" has incorrectly returned this server's ip address(" + AddressUtil.getIp() + ") resulting in this page.<br>Reconfigure your DNS settings and try again.<br>If you are attempting to use this server as a proxy, this server's proxy setting is currently set to: <b>Enabled: " + JavaWebServer.isProxyServerEnabled() + "; Password protected: " + JavaWebServer.proxyRequiresAuthorization + ";</b>.</string><br>\r\n"//
				+ "\t\t<string>Alternately, if you are a server administrator and this domain is not improperly configured, <a href=\"http://" + request.hostNoPort + ":" + JavaWebServer.admin_listen_port + "/?page=createDomain&domainName=" + request.host + "\" target=\"_blank\">log in</a> to create a new domain with the name \"" + request.host + "\" (thereby preventing this page from showing).</string>\r\n"//
				+ "\t\t<string>" + pageHeader + "</string>\r\n"//
				+ "\t</body>\r\n</html>");
		response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
		JavaWebServer.println("\t\t*** A client has incorrectly connected to this server thinking it has connected to \"" + request.host + "\".");
		if(domainDirectory != null) {
			domainDirectory.delete();//It didn't exist before this bad request occurred, so why keep it?
			domainDirectory = null;
			System.gc();
		}
		JavaWebServer.connectedClients.remove(clientInfo);
	}
	
}
