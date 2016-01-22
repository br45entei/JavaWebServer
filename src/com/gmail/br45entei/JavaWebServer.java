package com.gmail.br45entei;

import com.gmail.br45entei.data.BasicAuthorizationResult;
import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.media.MediaInfo;
import com.gmail.br45entei.media.MediaReader;
import com.gmail.br45entei.media.MediaReader.MediaArtwork;
import com.gmail.br45entei.server.ClientInfo;
import com.gmail.br45entei.server.ClientRequestStatus;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.HTTPStatusCodes;
import com.gmail.br45entei.server.RangeRequest;
import com.gmail.br45entei.server.RequestResult;
import com.gmail.br45entei.server.ReusedConn;
import com.gmail.br45entei.server.SocketConnectResult;
import com.gmail.br45entei.server.data.ClientConnectTime;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileData;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.Property;
import com.gmail.br45entei.server.data.RestrictedFile;
import com.gmail.br45entei.server.data.forum.ForumBoard;
import com.gmail.br45entei.server.data.forum.ForumClientResponder;
import com.gmail.br45entei.server.data.forum.ForumComment;
import com.gmail.br45entei.server.data.forum.ForumData;
import com.gmail.br45entei.server.data.forum.ForumTopic;
import com.gmail.br45entei.server.data.php.PhpResult;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.ImageUtil;
import com.gmail.br45entei.util.MapUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.ResponseUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.State;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_200;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_200_1;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_204;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_303;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_304;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_400;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_401;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_403;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_404;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_405;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_407;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_409;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_413;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_418;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_421;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_429;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_500;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_501;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_502;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_504;

/** The main Server class */
public final class JavaWebServer {//TODO Implement per-folder background color/images/textures with configurable settings(image repeats, size, etc.) based on pure HTML
	//TODO Implement per-folder default files, like with domains(configurable on the folder administration page)
	//TODO Go through and read all of the old TODO's and actually do them you lazy bum <--
	//TODO http://www.singular.co.nz/2008/07/finding-preferred-accept-encoding-header-in-csharp/ --> Meaning, stop using this: request.acceptEncoding.toLowerCase().contains("gzip") ... and actually parse the accept encoding header.
	//TODO Implement all or most of the id3v2.3.0 media tags from http://id3.org/id3v2.3.0 and the relevant ones from https://code.google.com/p/mp4v2/wiki/iTunesMetadata
	
	protected final Thread											sslThread;
	protected final Thread											adminThread;
	public static boolean											enableSSLThread					= false;
	public static boolean											enableAdminInterface			= true;
	
	public static boolean											sslStore_KeyOrTrust				= true;
	public static String											storePath						= "";
	public static String											storePassword					= "";
	
	public static final String										ThreadName						= "ServerThread";
	public static final String										SSLThreadName					= "SSLServerThread";
	public static final String										AdminThreadName					= "ServerAdminThread";
	
	public static boolean											allowByteRangeRequests			= true;
	
	private static final String										shutdownStr						= "rlXmsns_" + StringUtil.nextSessionId();																																																																																//UUID.randomUUID().toString();
																																																																																																																			
	public static final String										APPLICATION_NAME				= "JavaWebServer";
	public static final String										APPLICATION_VERSION				= "1.0";
	public static final String										COPYRIGHT_YEAR					= "2016";
	public static final String										APPLICATION_AUTHOR				= "Brian_Entei";
	
	/** The legal notice that is printed to the console. */
	public static final String										TERMINAL_NOTICE					= JavaWebServer.APPLICATION_NAME + " v." + JavaWebServer.APPLICATION_VERSION + " Copyright (C) " + JavaWebServer.COPYRIGHT_YEAR + "  " + JavaWebServer.APPLICATION_AUTHOR + "\r\nThis program comes with ABSOLUTELY NO WARRANTY.\r\nThis is free software, and you are welcome to redistribute it.\r\n\r\nFor help with command usage, type 'help' and press enter.";
	
	private static JavaWebServer									instance;
	private final Thread											currentThread;
	private final Thread											arrayListCleanupThread;
	
	public static final int											fNumberOfThreads				= (1000 * Runtime.getRuntime().availableProcessors());
	private static final ThreadPoolExecutor							fThreadPool						= ((ThreadPoolExecutor) Executors.newFixedThreadPool(fNumberOfThreads/*, namedThreadFactory*/));
	protected static final ThreadPoolExecutor						fSSLThreadPool					= ((ThreadPoolExecutor) Executors.newFixedThreadPool(fNumberOfThreads));
	protected static final ExecutorService							fAdminThreadPool				= Executors.newFixedThreadPool(20);
	
	protected static boolean										serverActive					= true;
	
	public static final String										SERVER_NAME						= "Entei_Server";
	public static final String										PROXY_SERVER_NAME				= "entei";
	/** The default "Server: " header that is sent to clients */
	public static final String										SERVER_NAME_HEADER				= SERVER_NAME + "/" + APPLICATION_VERSION;
	public static final String										DEFAULT_FILE_NAME				= "index.html";
	
	public static final String										DEFAULT_PAGE_ICON				= "/favicon.ico";
	public static final String										DEFAULT_STYLESHEET				= "/layout.css";
	
	/** The current working directory as depicted by
	 * {@code System.getProperty("user.dir")} */
	public static final File										rootDir							= new File(System.getProperty("user.dir"));
	
	public static final String										optionsFileName					= "options.txt";
	public static final String										sslOptionsFileName				= "sslOptions.txt";
	public static final String										adminOptionsFileName			= "adminOptions.txt";
	public static final String										proxyOptionsFileName			= "proxyOptions.txt";
	
	/** The concurrent list of all currently connected sockets(clients). */
	public static final ConcurrentLinkedQueue<Socket>				sockets							= new ConcurrentLinkedQueue<>();
	//private static final ConcurrentLinkedQueue<String>				sessionIDs						= new ConcurrentLinkedQueue<>();
	
	/** The name of the folder in which banned client data is stored locally. */
	public static final String										sinBinFolderName				= "SinBin";
	/** The concurrent list of banned clients, or "SinBin" */
	public static final ConcurrentLinkedQueue<NaughtyClientData>	sinBin							= new ConcurrentLinkedQueue<>();
	
	/** The default realm used when creating a new restricted file or page. */
	public static final String										DEFAULT_AUTHORIZATION_REALM		= "Forbidden File(s)";
	/** The default username used when creating a new restricted file or page. */
	public static final String										DEFAULT_AUTHORIZATION_USERNAME	= "username";
	/** The default password used when creating a new restricted file or page. */
	public static final String										DEFAULT_AUTHORIZATION_PASSWORD	= "";
	
	//=======================
	
	/** Cache-Control header that tells the client that it is viewing a private
	 * page and that it shouldn't cache it. */
	public static final String										cachePrivateMustRevalidate		= "private, max-age=0, no-cache, must-revalidate";
	
	//=======================
	
	/** The CSS table style that will be used in generated directory pages. */
	public static final String										HTML_TABLE_STYLE				= "table {\r\n" + //
																									"  display: table;\r\n" + //
																									"  border-collapse: separate;\r\n" + //
																									"  border-spacing: 2px;\r\n" + //
																									"  -webkit-border-horizontal-spacing: 2px;\r\n" + //
																									"  -webkit-border-vertical-spacing: 2px;\r\n" + //
																									"  border-color: gray;\r\n" + //
																									"  border-top-color: gray;\r\n" + //
																									"  border-right-color: gray;\r\n" + //
																									"  border-bottom-color: gray;\r\n" + //
																									"  border-left-color: gray;\r\n" + //
																									"  border-top-width: 2px;\r\n" + //
																									"  border-right-width: 2px;\r\n" + //
																									"  border-bottom-width: 2px;\r\n" + //
																									"  border-left-width: 2px;\r\n" + //
																									"}\r\n" + //
																									"tbody {\r\n" + //
																									"  display: table-row-group;\r\n" + //
																									"  vertical-align: middle;\r\n" + //
																									"  border-color: inherit;\r\n" + //
																									"  border-top-color: inherit;\r\n" + //
																									"  border-right-color: inherit;\r\n" + //
																									"  border-bottom-color: inherit;\r\n" + //
																									"  border-left-color: inherit;\r\n" + //
																									"}\r\n";
	
	/** A script that adjusts the size of iFrames in the "Media View" version of
	 * generated directory pages. */
	public static final String										AUTO_RESIZE_JAVASCRIPT			= "<script>\r\n" + //
																									"function autoResize(id) {\r\n" + //
																									"\tvar newheight;\r\n" + //
																									"\tvar newwidth;\r\n" + //
																									"\tif(document.getElementById) {\r\n" + //
																									"\t\tnewheight=document.getElementById(id).contentWindow.document .body.scrollHeight;\r\n" + //
																									"\t\tnewwidth=document.getElementById(id).contentWindow.document .body.scrollWidth;\r\n" + //
																									"\t}\r\n" + //
																									"\tdocument.getElementById(id).height= (newheight) + \"px\";\r\n" + //
																									"\tdocument.getElementById(id).width= (newwidth) + \"px\";\r\n" + //
																									"}\r\n" + //
																									"</script>";
	
	/** HTML metadata tag for "text/html" and charset UTF-8 */
	public static final String										HTML_HEADER_META_CONTENT_TYPE	= "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
	/** HTML tag letting the client know that it can resize the window to fit the
	 * screen size. */
	public static final String										HTML_HEADER_META_VIEWPORT		= "<meta name=viewport content=\"width=device-width, initial-scale=1\">";
	
	/** Three non-breaking spaces, or:
	 * <code>&amp;nbsp;&amp;nbsp;&amp;nbsp;</code> */
	public static final String										nonBreakingSpaces				= "&nbsp;&nbsp;&nbsp;";
	
	//=======================
	
	static {
		if(!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}
	
	//=======================
	
	/** @param ip The ip address of the suspected banned client to use
	 * @return The ban status of the given client if it is still listed as
	 *         banned, or null otherwise */
	public static final NaughtyClientData getNaughtyClientDataFor(String ip) {
		for(NaughtyClientData naughty : new ArrayList<>(sinBin)) {
			if(naughty.clientIp.equalsIgnoreCase(ip)) {
				return naughty;
			}
		}
		return null;
	}
	
	/** Checks if the given client should still be banned before returning the
	 * saved data. If the client is no longer supposed to be banned, it is
	 * pardoned and null is returned.
	 * 
	 * @param ip The ip address of the suspected banned client to use
	 * @return The ban status of the given client if it is still banned, or null
	 *         otherwise. */
	public static final NaughtyClientData getBannedClient(String ip) {
		for(NaughtyClientData naughty : new ArrayList<>(sinBin)) {
			if(naughty.clientIp.equalsIgnoreCase(ip)) {
				if(naughty.inSinBinUntil == -1L) {
					return naughty;
				}
				if(naughty.warnCount <= 0) {
					sinBin.remove(naughty);
					continue;
				}
				long timeLeftUntilBanLift = System.currentTimeMillis() - naughty.inSinBinUntil;
				if(timeLeftUntilBanLift > 0) {
					return naughty;
				}
				return null;
			}
		}
		return null;
	}
	
	/** @param ip The ip address of the client to check
	 * @return The {@link ClientConnectTime} of the given client. If */
	public static final ClientConnectTime getClientConnectTimeFor(String ip) {
		ClientConnectTime clientData = new ClientConnectTime();
		int numberOfConnections = 1;
		clientData.lastConnectionTime = System.currentTimeMillis();
		for(Socket s : new ArrayList<>(sockets)) {
			if(s != null && !s.isClosed()) {
				final String curIp = AddressUtil.getClientAddressNoPort((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
				if(curIp.equalsIgnoreCase(ip)) {
					numberOfConnections++;
				}
			}
		}
		clientData.numberOfConnections = numberOfConnections;
		return clientData;
	}
	
	protected static final synchronized void incrementWarnCountFor(String ip) {
		NaughtyClientData check = getNaughtyClientDataFor(ip);
		if(check == null) {
			ClientConnectTime cct = getClientConnectTimeFor(ip);
			NaughtyClientData naughty = new NaughtyClientData(UUID.randomUUID());
			naughty.banReason = "Too many connected clients at the same time";
			naughty.inSinBinUntil = System.currentTimeMillis() + 300000 + ((6 - cct.numberOfConnections) * 300000);//300000 = 5 minutes, 3600000 = one hour, etc.
			naughty.clientIp = ip;
			naughty.warnCount++;
			naughty.saveToFile();
			sinBin.add(naughty);
		} else {
			check.warnCount++;
			check.saveToFile();
		}
	}
	
	/** @param ip The ip address of the client to check
	 * @param newConnection Whether or not the client is connecting to this
	 *            server for the first time(default value is true)
	 * @return True if the client was already banned, or was just banned as a
	 *         result of checking it's open traffic to this server.<br>
	 *         If the client appears to have more than 20 open connections to
	 *         this server and is attempting another one within 250 milliseconds
	 *         of the last one, it will be banned for 5 minutes for 'dos-ing',
	 *         or 'denial-of-service-ing' this server. This is a work in
	 *         progress, and may ban clients that load multiple files from a
	 *         single webpage normally.(Usually, any one device will only open 6
	 *         concurrent connections to the same server.) */
	public static final boolean isIpBanned(String ip, boolean newConnection) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		NaughtyClientData check = getNaughtyClientDataFor(ip);//getBannedClient(ip);
		if(check != null) {
			if(check.isBanned()) {
				return true;
			}
		}
		if(newConnection) {
			ClientConnectTime clientData = getClientConnectTimeFor(ip);
			if(clientData.seemsMalicious()) {
				incrementWarnCountFor(ip);
			}
			return isIpBanned(ip, false);
		}
		return false;
	}
	
	//=======================
	
	/** The default amount of time, in seconds, that a requested resource should
	 * remain in the cache of a connected client */
	public static final Long			DEFAULT_CACHE_MAX_AGE		= Long.valueOf(604800L);
	
	//Options:
	/** The socket port that this server will listen on */
	public static int					listen_port					= 0x50;
	/** The socket port that this server's ssl thread will listen on */
	public static int					ssl_listen_port				= 0x1bb;
	/** The socket port that this server's admin thread will listen on */
	public static int					admin_listen_port			= 0x25ff;
	/** The default or fallback home directory to read from when parsing incoming
	 * client requests */
	public static File					homeDirectory				= new File(rootDir, "." + File.separatorChar + "htdocs");
	/** The default or fallback value used to determine whether or not the server
	 * should calculate and send directory sizes to the client in directory
	 * listings */
	public static boolean				calculateDirectorySizes		= false;
	/** The default or fallback font face that is used for the directory listing
	 * page that is sent to the client */
	public static String				defaultFontFace				= "Times New Roman";
	/** The time(in milliseconds) to wait before a client will be timed out */
	public static int					requestTimeout				= 30000;
	/** If not set to -1, this overrides the number of threads used in the thread
	 * pool which listens to incoming connections. If this is set to -1, the
	 * default value of
	 * {@code 1000 * Runtime.getRuntime().availableProcessors()} is used.
	 * Otherwise, if value this is less than 1, it is ignored. */
	public static int					overrideThreadPoolSize		= -1;
	
	/** Whether or not per-client console logging is enabled. Major errors/stack
	 * traces and general server logs will appear even if this is set to false. */
	public static boolean				enableConsoleLogging		= true;
	
	protected ServerSocket				socket						= null;
	
	/** The authorization realm used when clients attempt to administrate this
	 * server. */
	public static final String			adminAuthorizationRealm		= "Server Administration";
	protected static volatile String	adminUsername				= "Administrator";
	protected static volatile String	adminPassword				= "password";
	//protected static ConcurrentHashMap<Thread, HttpDigestAuthorization>	serverAdministrationAuths	= new ConcurrentHashMap<>();
	
	protected static boolean			enableProxyServer			= false;
	/** Whether or not this server should send proxy headers such as "Via:" and
	 * "(X-)Forwarded-For:" to remote servers when making HTTP requests. */
	public static boolean				sendProxyHeadersWithRequest	= true;
	/** Whether or not using this server as a proxy requires a username and
	 * password. */
	public static boolean				proxyRequiresAuthorization	= true;
	/** The authorization realm used for clients attempting to use this server as
	 * a proxy. */
	public static final String			proxyAuthroizationRealm		= SERVER_NAME_HEADER + " Proxy";
	protected static String				proxyUsername				= "Proxy User";
	protected static String				proxyPassword				= "password";
	
	/** Whether or not we should use cookies to help authenticate and hold a
	 * client's session */
	public static volatile boolean		useCookieAuthentication		= false;
	
	/** @param clientUser The username that the client sent
	 * @param clientPass The password that the client sent
	 * @return Whether or not the provided credentials match this server's
	 *         administration credentials; only the password is case-sensitive. */
	public static final boolean areCredentialsValidForAdministration(String clientUser, String clientPass) {
		return (JavaWebServer.adminUsername.equalsIgnoreCase(clientUser)) && (JavaWebServer.adminPassword.equals(clientPass));
	}
	
	/** @param request The client's HTTP request
	 * @return The client's remote ip address and port */
	public static final String getClientAddress(HTTPClientRequest request) {
		String rtrn = request.getStatus().getClientAddress();
		if(!request.xForwardedFor.isEmpty()) {
			printlnDebug("\tIdentified client behind proxy: " + request.xForwardedFor);
			rtrn = request.xForwardedFor;
		}
		return rtrn;
	}
	
	/** @param request The client's request
	 * @return The client's remote ip address without any port number */
	public static final String getClientIPNoPort(HTTPClientRequest request) {
		return AddressUtil.getClientAddressNoPort(getClientAddress(request));
	}
	
	/** @param request The client's request
	 * @return The domain/ip address that the client used to connect to this
	 *         server(not the client's remote ip! See
	 *         {@link #getClientAddress(HTTPClientRequest)} for that). */
	public static final String getClientDomain(HTTPClientRequest request) {
		return(request.host.trim().isEmpty() ? request.http2Host : request.host);
	}
	
	/** @param request The client's request
	 * @return Whether or not the client's session ID was removed, effectively
	 *         logging the client out(if {@link #isLoggedIn(HTTPClientRequest)}
	 *         returns false, this will do so as well). */
	public static final boolean removeClientSessionID(HTTPClientRequest request) {
		return BasicAuthorizationResult.removeSessionIDForClient(getClientIPNoPort(request), getClientDomain(request));
	}
	
	/** @param request The client's request
	 * @return Whether or not the client is currently logged in(may return false
	 *         if cookies are not being used) */
	public static final boolean isLoggedIn(HTTPClientRequest request) {
		return BasicAuthorizationResult.isLoggedIn(getClientIPNoPort(request), getClientDomain(request));
	}
	
	/** @param request The client's request
	 * @param useCookies Whether or not cookies should be used to authenticate
	 *            and hold a client's session
	 * @return The authentication result */
	public static final BasicAuthorizationResult authenticateBasicForServerAdministration(HTTPClientRequest request, boolean useCookies) {
		if(useCookies) {
			return BasicAuthorizationResult.authenticateBasic(request.authorization, adminAuthorizationRealm, adminUsername, adminPassword, getClientIPNoPort(request), getClientDomain(request), request.cookies);
		}
		return BasicAuthorizationResult.authenticateBasic(request.authorization, adminAuthorizationRealm, adminUsername, adminPassword);
	}
	
	private static final void checkThreadAccess() throws IllegalAccessError {
		final Thread thread = Thread.currentThread();
		if(instance != null) {
			if(instance.currentThread == thread) {
				return;
			}
			if(Main.getInstance() != null) {
				if(Main.getInstance().getThread() == thread) {
					return;
				}
			}
			throw new IllegalAccessError("Not a valid server thread!");
		}
	}
	
	//====
	
	/** Updates all of the thread pools' core sizes. */
	public static final void updateThreadPoolSizes() {
		checkThreadAccess();
		if(overrideThreadPoolSize != -1) {
			fThreadPool.setCorePoolSize(overrideThreadPoolSize);
			fSSLThreadPool.setCorePoolSize(overrideThreadPoolSize);
		} else {
			fThreadPool.setCorePoolSize(fNumberOfThreads);
			fSSLThreadPool.setCorePoolSize(fNumberOfThreads);
		}
	}
	
	/** @return The HTTP thread pool's core size. */
	public static final int getfThreadPoolCoreSize() {
		return fThreadPool.getCorePoolSize();
	}
	
	/** @return The HTTPS thread pool's core size. */
	public static final int getfSSLThreadPoolCoreSize() {
		if(enableSSLThread) {
			return fSSLThreadPool.getCorePoolSize();
		}
		return 0;
	}
	
	//====
	
	/** @return The Administration username */
	public static final String getAdminUsername() {
		return adminUsername;
	}
	
	/** @return The Administration password */
	public static final String getAdminPassword() {
		checkThreadAccess();
		return adminPassword;
	}
	
	/** @param username The Administration username(case insensitive) */
	public static final void setAdminUsername(String username) {
		checkThreadAccess();
		adminUsername = username;
	}
	
	/** @param password The Administration password */
	public static final void setAdminPassword(String password) {
		checkThreadAccess();
		adminPassword = password;
	}
	
	//====
	
	/** @return The username that clients must use in order to use the proxy
	 *         features */
	public static final String getProxyUsername() {
		return proxyUsername;
	}
	
	/** @return The password that clients must use in order to use the proxy
	 *         features */
	public static final String getProxyPassword() {
		checkThreadAccess();
		return proxyPassword;
	}
	
	/** @param username The username(case insensitive) that clients must use in
	 *            order to use the proxy features */
	public static final void setProxyUsername(String username) {
		checkThreadAccess();
		proxyUsername = username;
	}
	
	/** @param password The password that clients must use in order to use the
	 *            proxy features */
	public static final void setProxyPassword(String password) {
		checkThreadAccess();
		proxyPassword = password;
	}
	
	//====
	
	/** @return Whether or not the server is active. */
	public static final boolean serverActive() {
		return serverActive;
	}
	
	/** @return Whether or not Proxy server functionality is enabled. */
	public static final boolean isProxyServerEnabled() {
		return enableProxyServer;
	}
	
	/** @param enableProxyServer Whether or not Proxy server functionality should
	 *            be enabled */
	public static final void setProxyServerEnabled(boolean enableProxyServer) {
		checkThreadAccess();
		JavaWebServer.enableProxyServer = enableProxyServer;
	}
	
	//====
	
	/** @param str The string to print */
	public static final void print(String str) {
		if(enableConsoleLogging) {
			PrintUtil.print(str);
		}
	}
	
	/** @param str The string to print */
	public static final void printlnDebug(String str) {
		if(enableConsoleLogging && HTTPClientRequest.debug) {
			PrintUtil.println(str);
		}
	}
	
	/** @param str The string to print */
	public static final void println(String str) {
		if(enableConsoleLogging) {
			PrintUtil.println(str);
		}
	}
	
	/** @param str The string to print */
	public static final void printErrln(String str) {
		if(enableConsoleLogging) {
			PrintUtil.printErrln(str);
		}
	}
	
	//====
	
	private static void loadOptionsFromFile(boolean loadOtherData) {
		try {
			File optionsFile = new File(rootDir, optionsFileName);
			boolean optionsExists = optionsFile.exists();
			if(!optionsExists) {
				/*PrintWriter pr = new PrintWriter(optionsFile);
				pr.println("homeDirectory=." + File.separatorChar + "htdocs");
				pr.println("calculateDirectorySizes=false");
				pr.println("directoryPageFontFace=" + defaultFontFace);
				pr.println("phpExeFilePath=" + PhpResult.phpExeFilePath);
				pr.println("requestTimeout=" + requestTimeout);
				pr.println("overrideThreadPoolSize=" + overrideThreadPoolSize);
				pr.println("listenPort=" + listen_port);
				pr.println("enableAdminInterface=" + enableAdminInterface);
				pr.println("adminListenPort=" + admin_listen_port);
				pr.println("adminUsername=" + adminUsername);
				pr.println("adminPassword=" + adminPassword);
				pr.close();*/
				saveOptionsToFile(false);
			}
			if(optionsFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(optionsFile));
				while(br.ready()) {
					String line = br.readLine();
					String[] Args = line.split("=");
					for(int i = 0; i < Args.length;) {
						if((i + 1) < Args.length) {
							String key = Args[i].trim();
							i++;
							String value = Args[i].trim();
							if(!key.isEmpty() && !value.isEmpty()) {
								printlnDebug(key + "=" + (key.equalsIgnoreCase("adminPassword") || key.equalsIgnoreCase("proxyPassword") ? "********" : value));
								if(key.equalsIgnoreCase("homedirectory")) {
									//if(value.startsWith(".")) {
									//	value = FilenameUtils.normalize(rootDir.getAbsolutePath() + File.separatorChar + value.substring(1));
									//}
									homeDirectory = new File(value);
									if(!homeDirectory.exists()) {
										homeDirectory.mkdirs();
									}
									println("Set home directory to: \"" + homeDirectory.getAbsolutePath() + "\"!");
								} else if(key.equalsIgnoreCase("calculatedirectorysizes")) {
									calculateDirectorySizes = Boolean.valueOf(value).booleanValue();
									println("Set option \"calculateDirectorySizes\" to \"" + calculateDirectorySizes + "\"!");
								} else if(key.equalsIgnoreCase("directoryPageFontFace")) {
									defaultFontFace = value;
									println("Set option \"directoryPageFontFace\" to \"" + defaultFontFace + "\"!");
								} else if(key.equalsIgnoreCase("phpExeFilePath")) {
									PhpResult.phpExeFilePath = value;
									println("Set the main PHP executable file path to \"" + PhpResult.phpExeFilePath + "\"!");
								} else if(key.equalsIgnoreCase("requestTimeout")) {
									final int oldRequestTimeout = requestTimeout;
									try {
										requestTimeout = Integer.valueOf(value).intValue();
										if(requestTimeout < 0 || requestTimeout > 120000) {
											printErrln("Option \"requestTimeout\" was not set to an acceptable value(must be between 0 and 120000): " + requestTimeout);
											requestTimeout = oldRequestTimeout;
										} else {
											println("Set the server's request timeout to \"" + (requestTimeout / 1000L) + "\" seconds!");
										}
									} catch(NumberFormatException ignored) {
										printErrln("Option \"requestTimeout\" was not set to a valid long value: " + value);
										requestTimeout = oldRequestTimeout;
									}
								} else if(key.equalsIgnoreCase("overrideThreadPoolSize")) {
									final int oldOverrideThreadPoolSize = overrideThreadPoolSize;
									try {
										overrideThreadPoolSize = Integer.valueOf(value).intValue();
										if(overrideThreadPoolSize < 0 && overrideThreadPoolSize != -1) {
											printErrln("Option \"overrideThreadPoolSize\" was not set to an acceptable value(must be greater than zero.): " + overrideThreadPoolSize);
											overrideThreadPoolSize = oldOverrideThreadPoolSize;
										} else if(overrideThreadPoolSize != -1) {
											updateThreadPoolSizes();
											println("Set the server's thread pool size to \"" + overrideThreadPoolSize + "\"!");
										} else {
											println("Left the server's thread pool size set to the system-dependent default of 1000 times the number of available processors as listed above.");
										}
									} catch(NumberFormatException ignored) {
										printErrln("Option \"overrideThreadPoolSize\" was not set to a valid integer value: " + value);
										overrideThreadPoolSize = oldOverrideThreadPoolSize;
									}
								} else if(key.equalsIgnoreCase("listenPort")) {
									final int oldListen_Port = listen_port;
									try {
										listen_port = Integer.valueOf(value).intValue();
										if(listen_port < 0 || listen_port > 65535) {
											printErrln("Option \"listenPort\" was not set to a valid port number(must be between 0 and 65535): " + listen_port);
											listen_port = oldListen_Port;
										} else {
											println("Set the server's listening port to \"" + listen_port + "\"!");
										}
									} catch(NumberFormatException ignored) {
										printErrln("Option \"listenPort\" was not set to a valid integer value: " + value);
										listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("enableConsoleLogging")) {
									enableConsoleLogging = Boolean.valueOf(value).booleanValue();
									println("Set option \"enableConsoleLogging\" to \"" + enableConsoleLogging + "\"!");
								}
							}
						}
						i++;
					}
				}
				br.close();
			}
			loadSSLOptionsFromFile();
			loadAdminOptionsFromFile();
			loadProxyOptionsFromFile();
			loadSinBinFromFile();
			if(loadOtherData) {
				RestrictedFile.loadAllRestrictedFileDataFromFile();
				DomainDirectory.loadAllDomainDirectoryDataFromFile();
				ForumData.loadAllForumDataFromFile();
				println("# of ForumDatas: " + ForumData.getInstances().size() + ";\r\n# of ForumBoards: " + ForumBoard.getInstances().size() + ";");
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void loadSSLOptionsFromFile() {
		try {
			File optionsFile = new File(rootDir, sslOptionsFileName);
			boolean optionsExists = optionsFile.exists();
			if(!optionsExists) {
				saveSSLOptionsToFile();
			}
			if(optionsFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(optionsFile));
				while(br.ready()) {
					String line = br.readLine();
					String[] Args = line.split("=");
					for(int i = 0; i < Args.length;) {
						if((i + 1) < Args.length) {
							String key = Args[i].trim();
							i++;
							String value = Args[i].trim();
							if(!key.isEmpty() && !value.isEmpty()) {
								printlnDebug(key + "=" + (key.equalsIgnoreCase("storePassword") ? "********" : value));
								if(key.equalsIgnoreCase("enableSSL")) {
									enableSSLThread = Boolean.valueOf(value).booleanValue();
									println("Set SSL functionality to \"" + (enableSSLThread ? "Enabled" : "Disabled") + "\"!");
								} else if(key.equalsIgnoreCase("sslListenPort")) {
									final int oldListen_Port = ssl_listen_port;
									try {
										ssl_listen_port = Integer.valueOf(value).intValue();
										if(ssl_listen_port < 0 || ssl_listen_port > 65535) {
											printErrln("Option \"sslListenPort\" was not set to a valid port number(must be between 0 and 65535): " + ssl_listen_port);
											ssl_listen_port = oldListen_Port;
										} else {
											if(ssl_listen_port == listen_port || ssl_listen_port == admin_listen_port) {
												printErrln("The SSL Listen Port was set to a port that is already assigned: " + value + "; defaulting to " + oldListen_Port + ".");
												ssl_listen_port = oldListen_Port;
											} else {
												println("Set the server's ssl listening port to \"" + ssl_listen_port + "\"!");
											}
										}
									} catch(NumberFormatException ignored) {
										printErrln("Option \"sslListenPort\" was not set to a valid integer value: " + value);
										ssl_listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("sslStore_KeyOrTrust")) {
									sslStore_KeyOrTrust = value.equalsIgnoreCase("key");
								} else if(key.equalsIgnoreCase("storePath")) {
									storePath = value;
									println("Set the SSL Store path to \"" + storePath + "\"!");
								} else if(key.equalsIgnoreCase("storePassword")) {
									storePassword = value;
									println("Set the SSL Store password to \"********\"!");
								}
							}
						}
						i++;
					}
				}
				br.close();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void loadAdminOptionsFromFile() {
		try {
			File optionsFile = new File(rootDir, adminOptionsFileName);
			boolean optionsExists = optionsFile.exists();
			if(!optionsExists) {
				saveAdminOptionsToFile();
			}
			if(optionsFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(optionsFile));
				while(br.ready()) {
					String line = br.readLine();
					String[] Args = line.split("=");
					for(int i = 0; i < Args.length;) {
						if((i + 1) < Args.length) {
							String key = Args[i].trim();
							i++;
							String value = Args[i].trim();
							if(!key.isEmpty() && !value.isEmpty()) {
								printlnDebug(key + "=" + (key.equalsIgnoreCase("adminPassword") ? "********" : value));
								if(key.equalsIgnoreCase("enableAdminInterface")) {
									enableAdminInterface = Boolean.valueOf(value).booleanValue();
									println("Set option \"enableAdminInterface\" to \"" + enableAdminInterface + "\"!");
								} else if(key.equalsIgnoreCase("adminListenPort")) {
									final int oldListen_Port = admin_listen_port;
									try {
										admin_listen_port = Integer.valueOf(value).intValue();
										if(admin_listen_port < 0 || admin_listen_port > 65535) {
											printErrln("Option \"adminListenPort\" was not set to a valid port number(must be between 0 and 65535): " + admin_listen_port);
											admin_listen_port = oldListen_Port;
										} else {
											if(admin_listen_port == listen_port) {
												printErrln("The Server Administration listening port was set to a port that is already assigned: " + value + "; defaulting to " + oldListen_Port + ".");
												admin_listen_port = oldListen_Port;
											} else {
												println("Set the Server Administration listening port to \"" + admin_listen_port + "\"!");
											}
										}
									} catch(NumberFormatException ignored) {
										printErrln("Option \"adminListenPort\" was not set to a valid integer value: " + value);
										admin_listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("adminUsername")) {
									adminUsername = value;
									println("Set the Server Administration username to \"" + adminUsername + "\"!");
								} else if(key.equalsIgnoreCase("adminPassword")) {
									adminPassword = value;
									println("Set the Server Administration password to \"********\"!");
								}
							}
						}
						i++;
					}
				}
				br.close();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void loadProxyOptionsFromFile() {
		try {
			File optionsFile = new File(rootDir, proxyOptionsFileName);
			boolean optionsExists = optionsFile.exists();
			if(!optionsExists) {
				saveProxyOptionsToFile();
			}
			if(optionsFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(optionsFile));
				while(br.ready()) {
					String line = br.readLine();
					String[] Args = line.split("=");
					for(int i = 0; i < Args.length;) {
						if((i + 1) < Args.length) {
							String key = Args[i].trim();
							i++;
							String value = Args[i].trim();
							if(!key.isEmpty() && !value.isEmpty()) {
								printlnDebug(key + "=" + (key.equalsIgnoreCase("proxyPassword") ? "********" : value));
								if(key.equalsIgnoreCase("enableProxyServer")) {
									enableProxyServer = Boolean.valueOf(value).booleanValue();
									println("Set the Server Proxy state to \"" + (enableProxyServer ? "Enabled" : "Disabled") + "\"!");
								} else if(key.equalsIgnoreCase("sendProxyHeadersWithRequest")) {
									sendProxyHeadersWithRequest = Boolean.valueOf(value).booleanValue();
									println("Set option \"sendProxyHeadersWithRequest\" to \"" + sendProxyHeadersWithRequest + "\"!");
								} else if(key.equalsIgnoreCase("proxyRequiresAuthorization")) {
									proxyRequiresAuthorization = Boolean.valueOf(value).booleanValue();
									println("Set option \"proxyRequiresAuthorization\" to \"" + proxyRequiresAuthorization + "\"!");
								} else if(key.equalsIgnoreCase("proxyUsername")) {
									proxyUsername = value;
									println("Set the Server Proxy username to \"" + proxyUsername + "\"!");
								} else if(key.equalsIgnoreCase("proxyPassword")) {
									proxyPassword = value;
									println("Set the Server Proxy password to \"********\"!");
								}
							}
						}
						i++;
					}
				}
				br.close();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void loadSinBinFromFile() {
		sinBin.clear();
		File folder = new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName);
		if(!folder.exists()) {
			folder.mkdirs();
			return;
		}
		for(String fileName : folder.list()) {
			File file = new File(folder, fileName);
			if(fileName.endsWith(".txt") && fileName.length() > 5) {
				String getUUID = fileName.substring(0, fileName.length() - 4);
				if(StringUtils.isStrUUID(getUUID)) {
					NaughtyClientData naughty = new NaughtyClientData(UUID.fromString(getUUID));
					naughty.loadFromFile(file);
					sinBin.add(naughty);
				}
			}
		}
	}
	
	private static final void saveSinBinToFile() {
		File folder = new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		for(NaughtyClientData naughty : new ArrayList<>(sinBin)) {
			if(naughty.isBanned() || naughty.warnCount > 1) {//if(isIpBanned(naughty.clientIp, false) || naughty.warnCount > 1) {
				naughty.saveToFile();
			} else {
				naughty.deleteSaveFile();
			}
		}
	}
	
	private static final void saveProxyOptionsToFile() {
		try {
			File optionsFile = new File(rootDir, proxyOptionsFileName);
			PrintWriter pr = new PrintWriter(optionsFile);
			pr.println("enableProxyServer=" + enableProxyServer);
			pr.println("sendProxyHeadersWithRequest=" + sendProxyHeadersWithRequest);
			pr.println("proxyRequiresAuthorization=" + proxyRequiresAuthorization);
			pr.println("proxyUsername=" + proxyUsername);
			pr.println("proxyPassword=" + proxyPassword);
			pr.flush();
			pr.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void saveAdminOptionsToFile() {
		try {
			File optionsFile = new File(rootDir, adminOptionsFileName);
			PrintWriter pr = new PrintWriter(optionsFile);
			pr.println("enableAdminInterface=" + enableAdminInterface);
			pr.println("adminListenPort=" + admin_listen_port);
			pr.println("adminUsername=" + adminUsername);
			pr.println("adminPassword=" + adminPassword);
			pr.flush();
			pr.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void saveSSLOptionsToFile() {
		try {
			File optionsFile = new File(rootDir, sslOptionsFileName);
			PrintWriter pr = new PrintWriter(optionsFile);
			pr.println("enableSSL=" + enableSSLThread);
			pr.println("sslListenPort=" + ssl_listen_port);
			pr.println("sslStore_KeyOrTrust=" + (sslStore_KeyOrTrust ? "Key" : "Trust"));
			pr.println("storePath=" + storePath);
			pr.println("storePassword=" + storePassword);
			pr.flush();
			pr.close();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static final void saveOptionsToFile(boolean saveOtherData) {
		try {
			File optionsFile = new File(rootDir, optionsFileName);
			PrintWriter pr = new PrintWriter(optionsFile);
			String homeDirPath = homeDirectory.getAbsolutePath();
			if(homeDirPath.contains("." + File.separatorChar)) {
				homeDirPath = homeDirPath.substring(homeDirPath.indexOf("." + File.separatorChar), homeDirPath.length());
			}
			pr.println("homeDirectory=" + homeDirPath);
			pr.println("calculateDirectorySizes=" + calculateDirectorySizes);
			pr.println("directoryPageFontFace=" + defaultFontFace);
			pr.println("phpExeFilePath=" + PhpResult.phpExeFilePath);
			pr.println("requestTimeout=" + requestTimeout);
			pr.println("overrideThreadPoolSize=" + overrideThreadPoolSize);
			pr.println("listenPort=" + listen_port);
			pr.println("enableConsoleLogging=" + enableConsoleLogging);
			pr.flush();
			pr.close();
			saveSSLOptionsToFile();
			saveAdminOptionsToFile();
			saveProxyOptionsToFile();
			saveSinBinToFile();
			if(saveOtherData) {
				RestrictedFile.saveAllRestrictedFileDataToFile();
				DomainDirectory.saveAllDomainDirectoryDataToFile();
				ForumData.saveAllForumDataToFile();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	/** @return The Server instance */
	public static final JavaWebServer getInstance() {
		return instance;
	}
	
	private boolean	isShuttingDown	= false;
	
	/** @return Whether or not the {@link #shutdown()} method has been called. */
	public final boolean isShuttingDown() {
		return this.isShuttingDown;
	}
	
	/** Tells the server that it should shut down as soon as possible.<br>
	 * <br>
	 * Note that since {@link System#exit(int)} is called at the end of this
	 * method, this method never returns normally. */
	public final void shutdown() {
		if(this.isShuttingDown) {
			return;
		}
		final Thread currentThread = Thread.currentThread();
		final Main main = Main.getInstance();
		final boolean isCurrentThreadSWT = (main != null ? main.getThread() == currentThread : false);
		if(isCurrentThreadSWT) {
			try {
				if(this.socket != null) {
					this.socket.close();
				}
			} catch(Throwable ignored) {
			}
			return;
		}
		this.isShuttingDown = true;
		PrintUtil.printlnNow("Shutting down server...");
		if(!connectedClients.isEmpty()) {
			PrintUtil.printlnNow("Client transfers detected - Waiting up to 60 seconds on client transfers to complete...");
			boolean didBreak = false;
			long startTime = System.currentTimeMillis();
			while(!connectedClients.isEmpty()) {
				if((System.currentTimeMillis() - startTime) >= 60000L) {
					didBreak = true;
					break;
				}
				if(isCurrentThreadSWT) {
					Main.getInstance().runLoop();
				} else {
					try {
						Thread.sleep(1L);
					} catch(Throwable ignored) {
					}
				}
			}
			if(didBreak) {
				PrintUtil.printlnNow("60 second time-out; Cancelling remaining client transfers...");
				for(ClientInfo info : new ArrayList<>(connectedClients)) {
					if(info.requestedFile != null) {
						info.requestedFile.isCancelled = true;
					}
				}
				connectedClients.clear();
			} else {
				PrintUtil.printlnNow("Client transfers complete; Continuing with shutdown...");
			}
		}
		if(isCurrentThreadSWT) {
			Main.getInstance().runLoop();
		}
		PrintUtil.printlnNow("Saving server settings to file...");
		saveOptionsToFile(true);
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		if(isCurrentThreadSWT) {
			Main.getInstance().runLoop();
		}
		if(currentThread != this.currentThread) {//i.e. if this shutdown method was called from the gui thread(Main.java)
			PrintUtil.printlnNow("Interrupting main server thread...");
			try {
				this.currentThread.interrupt();
			} catch(Throwable e) {
				PrintUtil.printThrowable(e);
			}
		}
		try {
			List<Runnable> tasks = fThreadPool.shutdownNow();
			if(tasks != null && !tasks.isEmpty()) {
				try {
					PrintUtil.printlnNow("Waiting on remaining active tasks to terminate with a 5-second time-out...");
					PrintUtil.printErrToConsole();
					if(isCurrentThreadSWT) {
						Main.getInstance().runLoop();
					}
					boolean tasksTerminatedBeforeTimeout = fThreadPool.awaitTermination(5000L, TimeUnit.MILLISECONDS);
					if(tasksTerminatedBeforeTimeout) {
						PrintUtil.printlnNow("Active tasks terminated before time-out.");
					} else {
						PrintUtil.printlnNow("Active tasks did not terminate before time-out; Continuing with shutdown...");
					}
					PrintUtil.printErrToConsole();
				} catch(InterruptedException ignored) {
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		if(isCurrentThreadSWT) {
			Main.getInstance().runLoop();
		}
		PrintUtil.printlnNow("Performing cleanup...");
		PrintUtil.printErrToConsole();
		if(isCurrentThreadSWT) {
			for(int i = 0; i < 10; i++) {
				Main.getInstance().runLoop();//Prevents the window from freezing right before exiting. Yes, this seems pointless, but idc.
			}
		} else {
			for(int i = 0; i < 10; i++) {
				try {
					Thread.sleep(100L);
				} catch(Throwable ignored) {
				}
			}
		}
		try {
			Main.getInstance().shutdown();
		} catch(Throwable ignored) {
			ignored.printStackTrace();
		}
		serverActive = false;
		unsetLogWriter();
		if(sessionLockFile != null && sessionWriter != null) {
			try {
				sessionWriter.close();
				sessionLockFile.delete();
			} catch(Throwable ignored) {
			}
		}
		PrintUtil.printlnNow("Server has shut down.");
		PrintUtil.flushStreams();
		System.exit(0);
	}
	
	private static PrintWriter	logWriter;
	private static File			logFile;
	
	private static final File getLogFolder() {
		File logFolder = new File(rootDir, "logs");
		logFolder.mkdirs();
		return logFolder;
	}
	
	private static final void setupLogWriter() {
		File yearFolder = new File(getLogFolder(), new SimpleDateFormat("yyyy").format(new Date()));
		yearFolder.mkdirs();
		String fileName = "log_" + StringUtils.getTime(System.currentTimeMillis(), false, true);
		String fileExt = ".log.gz";
		File logFile = new File(yearFolder, fileName + fileExt);
		if(logFile.exists()) {
			int i = 0;
			while(logFile.exists()) {
				logFile = new File(yearFolder, fileName + "_" + i + fileExt);
				i++;
			}
		}
		JavaWebServer.logFile = logFile;
		try {
			logWriter = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(logFile)), StandardCharsets.UTF_8), true);
			PrintUtil.setSecondaryOut(logWriter);
			PrintUtil.setSecondaryErr(logWriter);
			println("Set up log file writer successfully.");
		} catch(Throwable e) {
			logWriter = null;
			PrintUtil.setSecondaryOut(null);
			PrintUtil.setSecondaryErr(null);
			printErrln("Unable to set up log file writer:");
			PrintUtil.printThrowable(e);
		}
	}
	
	protected static final void unsetLogWriter() {
		if(logWriter != null) {
			logWriter.flush();
			logWriter.close();
			logWriter = null;
			PrintUtil.setSecondaryOut(null);
			PrintUtil.setSecondaryErr(null);
			if(logFile != null && logFile.exists()) {
				try(BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile)), StandardCharsets.UTF_8))) {
					File latest = new File(getLogFolder(), "latest.log");
					if(latest.exists()) {
						latest.delete();
					}
					PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(latest), StandardCharsets.UTF_8), true);
					while(br.ready()) {
						String line = br.readLine().trim();
						if(!line.isEmpty()) {
							pr.println(line);
						}
					}
					pr.flush();
					pr.close();
				} catch(Throwable ignored) {
				}
			}
		}
	}
	
	private JavaWebServer() {
		this.arrayListCleanupThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(serverActive) {
					try {
						for(ClientInfo info : new ArrayList<>(connectedClients)) {
							try {
								if(info.getClient() != null) {
									if(info.getClient().isClosed()) {
										connectedClients.remove(info);
									}
								} else {
									connectedClients.remove(info);
								}
							} catch(Throwable ignored) {
							}
						}
					} catch(Throwable ignored) {
					}
					CodeUtil.sleep(333L);
					try {
						for(ClientRequestStatus status : new ArrayList<>(connectedClientRequests)) {
							try {
								if(status.isCancelled()) {
									connectedClientRequests.remove(status);
								} else if(status.getClient() != null) {
									if(status.getClient().isClosed()) {
										connectedClientRequests.remove(status);
									} else if(status.isProxyRequest()) {
										connectedClientRequests.remove(status);
										connectedProxyRequests.add(status);
									}
								} else {
									connectedClientRequests.remove(status);
								}
							} catch(Throwable ignored) {
							}
						}
					} catch(Throwable ignored) {
					}
					CodeUtil.sleep(333L);
					try {
						for(ClientRequestStatus status : new ArrayList<>(connectedProxyRequests)) {
							try {
								if(enableProxyServer) {
									if(status.isCancelled()) {
										connectedProxyRequests.remove(status);
									} else if(status.getClient() != null) {
										if(status.getClient().isClosed()) {
											connectedProxyRequests.remove(status);
										} else if(!status.isProxyRequest()) {
											connectedProxyRequests.remove(status);
											connectedClientRequests.add(status);
										}
									} else {
										connectedProxyRequests.remove(status);
									}
								} else {
									if(status.getClient() != null) {
										try {
											status.getClient().close();
										} catch(Throwable ignored) {
										}
									}
									connectedProxyRequests.remove(status);
								}
							} catch(Throwable ignored) {
							}
						}
					} catch(Throwable ignored) {
					}
					CodeUtil.sleep(333L);
				}
			}
		}, "ArrayListCleanupThread");
		this.arrayListCleanupThread.setDaemon(true);
		this.currentThread = Thread.currentThread();
		setupLogWriter();
		this.adminThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try(ServerSocket socket = new ServerSocket(admin_listen_port)) {
					println("\tAdmin socket created on port " + admin_listen_port);
					println("\tTo access the administration interface,\r\n\tvisit any domain or ip address pointing to\r\n\tthis machine using the admin listen port\r\n\tin a web browser using the admin credentials.");
					PrintUtil.printToConsole();
					PrintUtil.printErrToConsole();
					while(serverActive) {
						@SuppressWarnings("resource")
						final Socket s = socket.accept();
						if(!serverActive) {
							try {
								s.close();
							} catch(Throwable ignored) {
							}
							try {
								socket.close();
							} catch(Throwable ignored) {
							}
							break;
						}
						Runnable task = new Runnable() {
							@Override
							public void run() {
								replaceCurrentThreadName(AdminThreadName);
								sockets.add(s);
								try {
									s.setTcpNoDelay(true);
									final InputStream in = s.getInputStream();
									RequestResult result = HandleAdminRequest(s, in, ReusedConn.FALSE);//HandleAdminRequest(s);
									System.gc();
									PrintUtil.printToConsole();
									PrintUtil.printErrToConsole();
									if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
										result.reuse.reuse = false;
									}
									if(result.reuse.reuse) {
										long startTime = System.currentTimeMillis();
										while((result = HandleAdminRequest(s, in, result.reuse)).reuse.reuse) {
											if(System.currentTimeMillis() - startTime < 250L) {//FIXME There's a strange bug/glitch that causes CPU usage to spike when a request starts looping for some reason, so this is a temporary hack/workaround until I can find it...
												result.reuse.reuse = false;
												System.gc();
												PrintUtil.printToConsole();
												PrintUtil.printErrToConsole();
												break;
											}
											System.gc();
											PrintUtil.printToConsole();
											PrintUtil.printErrToConsole();
											if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
												result.reuse.reuse = false;
												break;
											}
											startTime = System.currentTimeMillis();
										}
									}
									try {
										in.close();
									} catch(Throwable ignored) {
									}
								} catch(Throwable e) {
									e.printStackTrace();
								}
								try {
									s.close();
								} catch(Throwable ignored) {
								}
								sockets.remove(s);
								//serverAdministrationAuths.remove(Thread.currentThread());
								System.gc();
								//Thread.currentThread().setName(tName);
								PrintUtil.printToConsole();
								PrintUtil.printErrToConsole();
							}
						};
						fAdminThreadPool.execute(task);
					}
					JavaWebServer.getInstance().shutdown();
				} catch(BindException e) {
					printErrln(" /!\\\tUnable to bind to admin port " + admin_listen_port + ":\r\n/___\\\t" + e.getMessage());
					PrintUtil.printErrToConsole();
					JavaWebServer.getInstance().shutdown();
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		}, AdminThreadName);
		this.sslThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if(sslStore_KeyOrTrust) {
					System.setProperty("javax.net.ssl.keyStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.keyStorePassword", storePassword);
				} else {
					System.setProperty("javax.net.ssl.trustStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.trustStorePassword", storePassword);
				}
				try(SSLServerSocket socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(ssl_listen_port)) {
					println("\tServer SSL socket created on port " + ssl_listen_port);
					PrintUtil.printToConsole();
					PrintUtil.printErrToConsole();
					while(serverActive) {
						@SuppressWarnings("resource")
						final SSLSocket s = (SSLSocket) socket.accept();
						if(!serverActive) {
							try {
								s.close();
							} catch(Throwable ignored) {
							}
							try {
								socket.close();
							} catch(Throwable ignored) {
							}
							sockets.remove(s);
							break;
						}
						fSSLThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								replaceCurrentThreadName(SSLThreadName);
								sockets.add(s);
								boolean wasProxyRequest = false;
								try {
									s.setTcpNoDelay(true);
									@SuppressWarnings("resource")
									InputStream in = s.getInputStream();
									RequestResult result = HandleRequest(s, in, true);
									wasProxyRequest = result.wasProxyRequest();
									System.gc();
									PrintUtil.printToConsole();
									PrintUtil.printErrToConsole();
									if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
										result.reuse.reuse = false;
									}
									if(result.reuse.reuse) {
										while((result = HandleRequest(s, in, true, result.reuse)).reuse.reuse) {
											System.gc();
											PrintUtil.printToConsole();
											PrintUtil.printErrToConsole();
											if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
												result.reuse.reuse = false;
												break;
											}
										}
										wasProxyRequest = result.wasProxyRequest();
									}
								} catch(SSLHandshakeException e) {
									printErrln("Failed to initialize SSL handshake: " + e.getMessage());
								} catch(SocketException e) {
									final String msg = e.getMessage();
									if(HTTPClientRequest.debug || (msg != null && !msg.equals("Socket Closed") && !msg.equals("Software caused connection abort: recv failed"))) {
										println("\t /!\\\tFailed to respond to client request:\r\n\t/___\\" + e.getMessage());
									} else {
										PrintUtil.clearLogs();
										PrintUtil.clearErrLogs();
									}
								} catch(Throwable e) {
									final String msg = e.getMessage();
									if(msg != null) {
										if(msg.equals("Remote host closed connection during handshake")) {
											printErrln("Failed to initialize SSL handshake: " + msg);
										} else {
											e.printStackTrace();
										}
									} else {
										e.printStackTrace();
									}
								}
								if(!wasProxyRequest) {
									try {
										s.close();
									} catch(Throwable ignored) {
									}
								}
								sockets.remove(s);
								System.gc();
								//Thread.currentThread().setName(tName);
								PrintUtil.printToConsole();
								PrintUtil.printErrToConsole();
							}
						});
					}
				} catch(BindException e) {
					printErrln(" /!\\\tUnable to bind to ssl port " + ssl_listen_port + ":\r\n/___\\\t" + e.getMessage());
					PrintUtil.printErrToConsole();
					JavaWebServer.getInstance().shutdown();
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		}, SSLThreadName);
	}
	
	private static final File	sessionLockFile	= new File(rootDir, "session.lock");
	private static PrintWriter	sessionWriter;
	
	private static final void checkSessionFileLock() {
		try {
			if(!sessionLockFile.exists()) {
				sessionLockFile.createNewFile();
			} else {
				println("Session lock file already exists! Was the server not properly shut down(or is another one still running)?");
				try {
					if(!sessionLockFile.delete()) {
						throw new IllegalStateException("No one is going to read this. Wait, you are reading it? Juu es a spie! Shoot teh spyies! And what is up with teh une peoples that spell initialize like \"initialise\"??? I cans english good yes? Lol, jk. Srsly though, how'd you end up reading this? Nothing better to do? I feel ya.");
					}
					sessionLockFile.createNewFile();
				} catch(Throwable e) {
					throw e;
				}
			}
			sessionWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(sessionLockFile)));
			println("Session lock initialized.");
			return;
		} catch(Throwable ignored) {
			printErrln("Fatal error: Unable to initialize session lock(is there another instance of this web server already running in the current directory?)!");
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			JavaWebServer.getInstance().shutdown();
			return;
		}
	}
	
	protected static final void replaceCurrentThreadName(String threadRootName) {
		final String tName = Thread.currentThread().getName();
		Thread.currentThread().setName(tName.replace("pool-", threadRootName + "_").replace("-thread-", "_"));
	}
	
	/** @param args System command arguments */
	public static final void sysMain(String[] args) {
		final Thread socketListUpdaterThread = new Thread() {
			@Override
			public final void run() {
				while(true) {
					try {
						for(Socket client : sockets) {
							if(client != null) {//Yep.
								if(client.isClosed() || (client.isInputShutdown() && client.isOutputShutdown())) {//If the socket is closed or dead
									try {
										client.close();
									} catch(Throwable ignored) {
									}
									sockets.remove(client);
								}
							}
						}
					} catch(Throwable e) {
						System.err.print("Failed to iterate over connected sockets queue: ");
						e.printStackTrace();
					}
					Functions.sleep(10L);
				}
			}
		};
		socketListUpdaterThread.setDaemon(true);
		socketListUpdaterThread.start();
		instance = new JavaWebServer();
		checkSessionFileLock();
		println("Available server threads(1000 * # of processors): " + fNumberOfThreads);
		loadOptionsFromFile(true);
		if(homeDirectory == null) {
			homeDirectory = new File(rootDir, "htdocs");
			homeDirectory.mkdirs();
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		instance.arrayListCleanupThread.start();
		if(enableSSLThread) {
			if(storePath.isEmpty() || storePassword.isEmpty()) {
				PrintUtil.printErrlnNow(" /!\\Warning! One or both of the SSL Store settings was left blank!\r\n/___\\\tThis will most likely result in SSL handshake issues.");
			}
			instance.sslThread.start();
		}
		if(enableAdminInterface) {
			instance.adminThread.start();
		}
		try {
			instance.socket = new ServerSocket(listen_port);
			println("\tServer socket created on port " + listen_port);
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			while(serverActive) {
				@SuppressWarnings("resource")
				final Socket s = instance.socket.accept();
				if(!serverActive) {
					try {
						s.close();
					} catch(Throwable ignored) {
					}
					try {
						instance.socket.close();
					} catch(Throwable ignored) {
					}
					break;
				}
				Runnable task = new Runnable() {
					@Override
					public void run() {
						replaceCurrentThreadName(ThreadName);
						sockets.add(s);
						boolean wasProxyRequest = false;
						try {
							s.setTcpNoDelay(true);
							@SuppressWarnings("resource")
							InputStream in = s.getInputStream();
							RequestResult result = HandleRequest(s, in, false);
							wasProxyRequest = result.wasProxyRequest();
							System.gc();
							PrintUtil.printToConsole();
							PrintUtil.printErrToConsole();
							if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
								result.reuse.reuse = false;
							}
							if(result.reuse.reuse) {
								while((result = HandleRequest(s, in, false, result.reuse)).reuse.reuse) {//reuse reuse reuse... XD
									System.gc();
									PrintUtil.printToConsole();
									PrintUtil.printErrToConsole();
									if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
										result.reuse.reuse = false;
										break;
									}
								}
								wasProxyRequest = result.wasProxyRequest();
							}
							result.markCompleted();
						} catch(Throwable e) {
							e.printStackTrace();
						}
						if(!wasProxyRequest) {
							try {
								s.close();
							} catch(Throwable ignored) {
							}
							sockets.remove(s);
						}
						//serverAdministrationAuths.remove(Thread.currentThread());
						System.gc();
						//Thread.currentThread().setName(tName);
						PrintUtil.printToConsole();
						PrintUtil.printErrToConsole();
					}
				};
				fThreadPool.execute(task);
			}
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			JavaWebServer.getInstance().shutdown();
		} catch(BindException e) {
			printErrln(" /!\\\tUnable to bind to port " + listen_port + ":\r\n/___\\\t" + e.getMessage());
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			JavaWebServer.getInstance().shutdown();
		} catch(SocketException e) {
			if(instance.socket != null && !instance.socket.isClosed()) {
				printErrln(" /!\\\tFailed to listen for next incoming client connection:\r\n/___\\\t" + e.getMessage());
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		instance.shutdown();
	}
	
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
	
	/** An array list containing any clients currently connected to this server */
	public static final ArrayList<ClientInfo>			connectedClients		= new ArrayList<>();
	/** An array list containing any clients that are making a request to this
	 * server */
	public static final ArrayList<ClientRequestStatus>	connectedClientRequests	= new ArrayList<>();
	/** An array list containing any clients connecting to other servers through
	 * this one */
	public static final ArrayList<ClientRequestStatus>	connectedProxyRequests	= new ArrayList<>();
	
	/*public static final void main(String[] args) {
		final String test = "/";//Media/Music/Upload/";
		final String address = "http://redsandbox.ddns.net/";
		final boolean newWindow = true;
		final boolean named = true;
		final String params = "?mediaList=1";
		
		final String result = createPathLinksFor(test, address, newWindow, named, params);
		System.out.println(result);
	}*/
	
	private static final String createPathLinksFor(String path, String homeDirName, String httpAddressAndPort, boolean linkNewWindow, boolean linkNamed, String params) {
		if(path == null || httpAddressAndPort == null) {
			return null;
		}
		params = params == null ? "" : (params.startsWith("?") || params.trim().isEmpty() ? params : "?" + params);
		final String originalParams = params;
		if(params.toLowerCase().contains("mediainfo=")) {
			final String[] split = params.substring(1).split(Pattern.quote("&"));
			String pResult = "?";
			boolean firstTime = true;
			for(String s : split) {
				String sep = firstTime ? "?" : "&";
				if(s.isEmpty()) {
					continue;
				}
				boolean wasFirstTime = firstTime;
				firstTime = false;
				if(s.contains("=")) {
					String[] entry = s.split(Pattern.quote("="));
					String pname = entry[0];
					//String pvalue = StringUtils.stringArrayToString(entry, '=', 1);
					if(pname.equalsIgnoreCase("mediaInfo")) {
						firstTime = wasFirstTime;
						continue;
					}
				}
				pResult += sep + s;
			}
			params = pResult.equals("?") ? "" : pResult;
		}
		final String linkStart = "<a href=\"{0}" + params + "\"" + (linkNamed ? " name=\"{1}\"" : "") + (linkNewWindow ? " target=\"_blank\"" : "") + ">";
		final String linkEnd = "</a>";
		final String origLinkStart = "<a href=\"{0}" + originalParams + "\"" + (linkNamed ? " name=\"{1}\"" : "") + (linkNewWindow ? " target=\"_blank\"" : "") + ">";
		path = path.trim();
		homeDirName = homeDirName == null ? "" : homeDirName.trim();
		httpAddressAndPort = httpAddressAndPort.endsWith("/") ? httpAddressAndPort.substring(0, httpAddressAndPort.length() - 1) : httpAddressAndPort;
		if(path.equals("/")) {
			final String result = "<string>/" + linkStart.replace("{0}", httpAddressAndPort) + "./" + linkEnd + "</string>";
			return linkNamed ? result.replace("{1}", homeDirName) : result;
		}
		path = path.startsWith("/") ? path.substring(1) : path;
		String pathBuild = "/";
		String result = "<string>/" + linkStart.replace("{0}", httpAddressAndPort) + "." + linkEnd;
		result = linkNamed ? result.replace("{1}", homeDirName) : result;
		String[] split = path.split(Pattern.quote("/"));
		int i = 1;
		for(String p : split) {
			if(p.trim().isEmpty()) {
				continue;
			}
			boolean isLast = i == split.length;
			String appendStr = (isLast ? origLinkStart : linkStart).replace("{0}", httpAddressAndPort + pathBuild + p) + p + linkEnd;
			appendStr = linkNamed ? appendStr.replace("{1}", p) : appendStr;
			result += "/" + appendStr;
			pathBuild += p + "/";
			i++;
		}
		return result + "</string>";
	}
	
	/** Locates and serves the requested file to the client. Also serves
	 * generated-on-the-fly content such as directory data, file structure,
	 * media views, etc.
	 * 
	 * @param s The client
	 * @param keepAlive Whether or not keep-alive was specified
	 * @param https Whether or not the connection is https://
	 * @param outStream The OutputStream of the client
	 * @param response The response to use when responding to the client
	 * @param clientInfo The client information(deprecated)
	 * @param domainDirectory The domain that the client used to connect to this
	 *            server(the host header in the client's request)
	 * @param request The client's HTTP request
	 * @throws IOException Thrown if a network or file input/output exception
	 *             occurs.
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ReusedConn)
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	private static final void serveFileToClient(final Socket s, boolean keepAlive, boolean https, final OutputStream outStream, final HTTPServerResponse response, final ClientInfo clientInfo, final DomainDirectory domainDirectory, final HTTPClientRequest request) throws IOException {
		final String clientIPAddress = AddressUtil.getClientAddress((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
		final ClientConnectTime cct = getClientConnectTimeFor(clientIPAddress);
		final boolean connectionSeemsMalicious = cct.seemsMalicious();
		connectedClients.add(clientInfo);
		response.setClientInfo(clientInfo);
		
		final String httpProtocol = (https ? "https://" : "http://");
		final FileInfo info = clientInfo.requestedFile;
		File requestedFile = new File(info.filePath);
		
		String reqFilePath = FilenameUtils.normalize(requestedFile.getAbsolutePath());
		String wrkDirPath = FilenameUtils.normalize(JavaWebServer.rootDir.getAbsolutePath());
		
		//final File homeDirectory = domainDirectory.getDirectory();
		
		URL fileURL = requestedFile.toURI().toURL();
		final URLConnection conn = fileURL.openConnection();
		//final long contentLength = conn.getContentLengthLong();
		
		final String clientAddress = request.xForwardedFor.isEmpty() ? clientIPAddress : request.xForwardedFor;
		
		final String fileLink = StringUtils.encodeHTML(httpProtocol + (request.host + domainDirectory.replacePathWithAlias(request.requestedFilePath))).replace(" ", "%20");
		
		final boolean isHTTP1 = request.version.equalsIgnoreCase("HTTP/1.0");
		final String versionToUse = isHTTP1 ? request.version : "HTTP/1.1";
		
		//==
		
		final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath());
		
		final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
		//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
		//println("pathPrefix: \"" + pathPrefix + "\"");
		String path = requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/');
		path = (path.trim().isEmpty() ? "/" : path);
		path = (path.startsWith("/") ? "" : "/") + path;
		final String pagePrefix = createPathLinksFor(path, domainDirectory.getDirectory().getName(), httpProtocol + request.host, false, true, request.requestArgumentsStr)/*(path.startsWith("/") ? path : "/" + path)*/+ " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress;
		final String pageHeader = "<h1>" + pagePrefix + " </h1><hr>";
		
		final boolean isLayoutOrFavicon = request.requestedFilePath.equalsIgnoreCase("/favicon.ico") || request.requestedFilePath.equalsIgnoreCase(domainDirectory.getDefaultPageIcon()) || request.requestedFilePath.equalsIgnoreCase("/layout.css") || request.requestedFilePath.equalsIgnoreCase(domainDirectory.getDefaultStylesheet());
		final boolean isReqCurrentWrkDir = (reqFilePath.equalsIgnoreCase(wrkDirPath) || reqFilePath.startsWith(wrkDirPath)) && !(reqFilePath.toLowerCase().startsWith(homeDirPath.toLowerCase()));
		
		//==
		
		String administrateFileCheck = request.requestArguments.get("administrateFile");
		final boolean administrateFile = administrateFileCheck != null ? (administrateFileCheck.equals("1") || administrateFileCheck.equalsIgnoreCase("true")) : false;
		
		final String folderName = FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(requestedFile.getAbsolutePath() + File.separatorChar));
		if(administrateFile) {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
			if(handleAdministrateFile(request, requestedFile, clientAddress, versionToUse, httpProtocol, outStream, out, domainDirectory, keepAlive, folderName, pagePrefix, clientInfo)) {
				println("\t*** " + versionToUse + " - File administration");
				return;
			}
			out.println(versionToUse + " 500 Internal Server Error");
			out.println("Vary: Accept-Encoding");
			out.println("Server: " + SERVER_NAME_HEADER);
			out.println("Date: " + StringUtils.getCurrentCacheTime());
			out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
			out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
			out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
			if(keepAlive) {
				out.println("Keep-Alive: timeout=30");
			}
			out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			String responseStr = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
					+ "\t\t<title>500 Internal Server Error - " + domainDirectory.getServerName() + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
					+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error 500 - Error Administrating File</h1><hr>\r\n"//
					+ "\t\t<string>Something went wrong when attempting to administrate your file(s). Please try again!</string><hr>\r\n"//
					+ "\t\t<string>" + pagePrefix + "</string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
				out.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(responseStr, "UTF-8");
				out.println("Content-Length: " + r.length);
				out.println("");
				if(request.method.equalsIgnoreCase("GET")) {
					outStream.write(r);
					println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(request.method.equalsIgnoreCase("HEAD")) {
					println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				r = null;
				System.gc();
			} else {
				out.println("Content-Length: " + responseStr.length());
				out.println("");
				if(request.method.equalsIgnoreCase("GET")) {
					out.println(responseStr);
					println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(request.method.equalsIgnoreCase("HEAD")) {
					println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
			}
			out.flush();
			if(s.isClosed() || !keepAlive) {
				out.close();
			}
			return;
		} else if(!isLayoutOrFavicon && isReqCurrentWrkDir) {//If the requested file is(or is a child of) the current working directory of this server(and is not the intended folder according to the domain data used), then we should require administration credentials before allowing the client to view sensitive server files.
			final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(request, false);
			final String authCookie = authResult.authorizedCookie;
			if(!authResult.passed()) {
				response.setStatusCode(HTTP_401);
				response.setHeader("Vary", "Accept-Encoding");
				response.setHeader("Server", SERVER_NAME_HEADER);
				response.setHeader("Date", StringUtils.getCurrentCacheTime());
				response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
				response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
				response.setHeader("Cache-Control", cachePrivateMustRevalidate);
				response.setHeader("Connection", (keepAlive ? "keep-alive" : "close"));
				if(authCookie != null) {
					response.setHeader("Set-Cookie", authCookie);
				}
				if(keepAlive) {
					response.setHeader("Keep-Alive", "timeout=30");
				}
				response.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
						+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Authorization Required</h1><hr>\r\n"//
						+ "\t\t<string title=\"In order to be able to administrate files on this server, you must be logged in as the administrator.\">You need permission to do that.</string><hr>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				response.sendToClient(s, true);
				connectedClients.remove(clientInfo);
				//s.close();
				return;
			}
		}
		
		//==
		
		@SuppressWarnings("unused")
		final String homeLink = httpProtocol + (request.host + "/");
		final String parentFileLink;
		final File parentFile = requestedFile.getParentFile();
		//final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath());
		if(parentFile != null && !FilenameUtils.normalize(requestedFile.getAbsolutePath()).equals(homeDirPath)) {
			String parentRequestPath = FilenameUtils.normalize(parentFile.getAbsolutePath()).replace(homeDirPath, "").replace("\\", "/");
			parentRequestPath = (parentRequestPath.startsWith("/") ? "" : "/") + parentRequestPath;
			parentFileLink = StringUtils.encodeHTML(httpProtocol + (request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(parentRequestPath))));
		} else {
			parentFileLink = null;
		}
		final String previousPageLink = (!request.referrerLink.isEmpty() ? request.referrerLink : null);
		
		String autoplayCheck = request.requestArguments.get("autoplay");
		final boolean autoplay = autoplayCheck != null ? (autoplayCheck.equals("1") || autoplayCheck.equalsIgnoreCase("true")) : false;
		
		//==
		if(requestedFile.isFile()) {
			final String fileExt = FilenameUtils.getExtension(info.fileName);
			if(fileExt.equalsIgnoreCase("php") && PhpResult.isPhpFilePresent()) {
				response.setStatusMessage("PHP file");
				response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				response.setHTTPVersion(versionToUse);
				PhpResult phpResponse = PhpResult.execPHP(requestedFile.getAbsolutePath(), request.requestArgumentsStr.replace("?", "").replace("&", " "), true, false);
				for(String header : phpResponse.headers.split("\r\n")) {
					String[] split = header.split("\\: ");
					if(split.length == 2) {
						response.setHeader(split[0], split[1]);
					}
				}
				response.setHeader("Server", SERVER_NAME_HEADER);
				response.setHeader("Date", StringUtils.getCurrentCacheTime());
				response.setResponse(phpResponse.body);
				response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
				connectedClients.remove(clientInfo);
				//s.close();
				return;
			} else if(fileExt.equalsIgnoreCase("php")) {
				printErrln("\t*** Warning! Requested file is a php file, but the php setting in the\r\n\t\"" + rootDir.getAbsolutePath() + File.separatorChar + optionsFileName + "\"\r\n\tfile either refers to a non-existant file or is not defined.\r\n\tTo fix this, type phpExeFilePath= and then the complete path to the main php\r\n\texecutable file(leaving no space after the equal symbol).\r\n\tYou can download php binaries here: http://php.net/downloads.php\r\n\tIf this is not corrected, any php files requested by incoming clients will be downloaded rather than executed.");
			}
			final long lastModified = StringUtils.getLastModified(requestedFile);
			boolean modifiedSince = false;
			if(!request.ifModifiedSince.isEmpty()) {
				try {
					long clientModifiedReq = StringUtils.getCacheValidatorTimeFormat().parse(request.ifModifiedSince).getTime();
					if(lastModified > clientModifiedReq) {
						modifiedSince = true;
					}
				} catch(ParseException | NumberFormatException e) {
					//e.printStackTrace();
					modifiedSince = true;//Go ahead and send the response since we encountered an error.
				}
			} else {
				modifiedSince = true;
			}
			boolean ifRange = false;
			if(!request.ifRange.isEmpty()) {
				try {
					long clientModifiedReq = StringUtils.getCacheValidatorTimeFormat().parse(request.ifRange).getTime();
					if(lastModified > clientModifiedReq) {
						ifRange = true;
					}
				} catch(ParseException | NumberFormatException e) {
					//e.printStackTrace();
					ifRange = true;//Go ahead and send the response since we encountered an error.
				}
			} else {
				ifRange = true;
			}
			String length = info.contentLength;
			final long contentLength = StringUtils.getLongFromStr(length).longValue();
			final String contentRange = "0-" + (contentLength - 1) + "/" + info.contentLength;
			if(!request.range.isEmpty() && allowByteRangeRequests) {
				if(ifRange) {
					try {
						RangeRequest range = new RangeRequest(request.range, contentLength);
						if(range.isValid()) {
							println("\t*** 206 Partial Content(Byte range) Requested: " + range.startBytes + "-" + range.endBytes + " /" + contentLength + (request.ifRange.isEmpty() ? "(No \"If-Range\" header sent)" : "(\"If-Range\" requirement met)"));
							PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
							out.println("HTTP/1.1 206 Partial Content");
							out.println("Vary: Accept-Encoding");
							out.println("Content-Type: " + info.mimeType + "; charset=" + StringUtils.getDetectedEncoding(requestedFile));
							out.println("Server: " + SERVER_NAME_HEADER);
							out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
							out.println("Date: " + StringUtils.getCurrentCacheTime());
							out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
							out.println("Content-Length: " + range.rangeLength);//info.contentLength);
							out.println("Accept-Ranges: bytes");
							out.println("Content-Range: bytes " + range.startBytes + "-" + range.endBytes + "/" + contentLength);
							out.println("");
							out.flush();
							if(request.method.equalsIgnoreCase("GET")) {
								try {
									InputStream closeMe = conn.getInputStream();
									if(closeMe != null) {
										closeMe.close();
									}
								} catch(Throwable ignored) {
								}
								InputStream fileIn = new FileInputStream(requestedFile);
								IOException exception = null;
								try {
									long sentBytes = range.sendRangeTo(fileIn, outStream, info, clientInfo);//copyInputStreamToOutputStream(info, fileIn, outStream, range.startBytes, range.endBytes, clientInfo);
									printlnDebug("DEBUG: Sent Bytes matches length: " + ((sentBytes == range.rangeLength) ? "true" : "false; Sent bytes: " + sentBytes));
									println("\t\t\tSent file \r\n\t\t\t\"" + FilenameUtils.normalize(requestedFile.getAbsolutePath()) + "\"\r\n\t\t\t to client \"" + clientAddress + "\" successfully.");
								} catch(IOException e) {
									println("\t /!\\\tFailed to send file \r\n\t/___\\\t\"" + FilenameUtils.normalize(requestedFile.getAbsolutePath()) + "\"\r\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
									exception = e;
								}
								try {
									fileIn.close();
								} catch(Throwable e) {
									PrintUtil.printThrowable(e);
								}
								if(exception != null) {
									connectedClients.remove(clientInfo);
									throw exception;
								}
							}
							//out.close();
							//s.close();
							connectedClients.remove(clientInfo);
							return;
						}
					} catch(NumberFormatException ignored) {
					}
					println("\t*** HTTP/1.1 416 Requested Range Not Satisfiable(\"" + request.range + "\")");
					PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
					out.println("HTTP/1.1 416 Requested Range Not Satisfiable");
					out.println("Content-Range: " + contentRange);
					out.println("");
					out.flush();
					//out.close();
					//s.close();
					connectedClients.remove(clientInfo);
					return;
				}
				modifiedSince = true;
			}
			String mediaCheck = request.requestArguments.get("mediaInfo");
			final boolean mediaInfo = mediaCheck != null ? mediaCheck.equals("1") || mediaCheck.equalsIgnoreCase("true") : false;
			String artworkCheck = request.requestArguments.get("artwork");
			final boolean artwork = artworkCheck != null ? artworkCheck.equals("1") || artworkCheck.equalsIgnoreCase("true") : false;
			if(modifiedSince || (mediaInfo/* TODO && domainDirectory.getEnableMediaInfoParsing()*/)) {
				response.setHTTPVersion(versionToUse);
				response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				response.setHeader("Vary", "Accept-Encoding");
				
				String autoCloseCheck = request.requestArguments.get("autoClose");
				boolean autoClose = autoCloseCheck != null ? (autoCloseCheck.equals("1") || autoCloseCheck.equalsIgnoreCase("true")) : false;
				String check = request.requestArguments.get("displayFile");
				boolean displayFile = check != null ? check.equals("1") || check.equalsIgnoreCase("true") : false;
				
				String ext = FilenameUtils.getExtension(requestedFile.getAbsolutePath());
				
				if(displayFile && domainDirectory.getEnableReadableFileViews()) {
					if(info.mimeType.equalsIgnoreCase("text/rtf") || info.mimeType.equalsIgnoreCase("application/rtf")) {
						final String charset = StringUtils.getDetectedEncoding(requestedFile);
						response.setHeader("Content-Type", "text/html; charset=" + charset);
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
						response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
						response.setResponse(StringUtils.rtfToHtml(requestedFile));
						response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
						//s.close();
						connectedClients.remove(clientInfo);
						return;
					} else if(info.mimeType.equalsIgnoreCase("application/epub+zip")) {
						Charset charset = null;
						try {
							charset = Charset.forName(StringUtils.getCharsetOfBook(requestedFile));
						} catch(Throwable ignored) {
							charset = StandardCharsets.UTF_8;
						}
						response.setHeader("Content-Type", "text/html; charset=" + charset);
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
						response.setHeader("Accept-Ranges", "none");
						response.setResponse(StringUtils.readEpubBook(requestedFile));
						response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
						//s.close();
						connectedClients.remove(clientInfo);
						return;
					} else if(info.mimeType.startsWith("text/")) {
						Charset charset = null;
						try {
							charset = Charset.forName(StringUtils.getDetectedEncoding(requestedFile));
						} catch(Throwable ignored) {
							charset = StandardCharsets.UTF_8;
						}
						response.setHeader("Content-Type", "text/plain; charset=" + charset);//The whole point of 'displayFile' is to get the browser to display the file instead of attempting to download it.
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
						response.setHeader("Accept-Ranges", "none");
						response.setResponse(FileUtil.readFileData(requestedFile));
						response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
						//s.close();
						connectedClients.remove(clientInfo);
						return;
					}
				} else if(mediaInfo) {
					//TODO if(domainDirectory.getEnableMediaInfoParsing()) {
					if(isMimeTypeMedia(info.mimeType)) {
						if(artwork) {
							String failReason = "";
							MediaArtwork data = null;
							try {
								data = MediaReader.readFile(requestedFile, true).getAlbumArtwork();
							} catch(Throwable e) {
								failReason = "Failed to read artwork data:\r\n<pre>" + StringUtil.throwableToStr(e) + "</pre>";
							}
							response.setHeader("Server", SERVER_NAME_HEADER);
							response.setHeader("Cache-Control", "no-cache, must-revalidate");
							response.setHeader("Date", StringUtils.getCurrentCacheTime());
							response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
							response.setHeader("Accept-Ranges", "none");
							if(data != null) {
								response.setHeader("Content-Type", data.mimeType);
								response.setHeader("Vary", "");//Remove the existing 'Vary' header
								response.setResponse(data.getData());
								response.setUseGZip(false);//The data is already compressed, this will only slow it down!
								data.close();
							} else {
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setResponse("<!DOCTYPE html>\r\n" + //
								"<html>\r\n\t<head>\r\n" + //
								"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
								"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
								"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
								"\t\t<title>" + requestedFile.getName() + " - Media Artwork - " + domainDirectory.getServerName() + "</title>\r\n" + //
								"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
								(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
								AUTO_RESIZE_JAVASCRIPT + //
								"\t</head>\r\n" + //
								"\t<body>\r\n" + //
								"\t\t" + pageHeader + "<br>\r\n" + //
								(failReason.isEmpty() ? "" : "\t\t" + failReason + "<br>\r\n") + //
								"\t\t<a href=\"" + fileLink + "\">Click to " + (isMimeTypeAudio(info.mimeType) ? "listen" : "watch") + "</a>\r\n" + //
								(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
								(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
								"\t</body>\r\n</html>");
							}
						} else {
							String mInfo = MediaReader.getMediaInfoHTMLFor(requestedFile, domainDirectory.getDirectory().getAbsolutePath());
							if(mInfo.contains("{0}")) {//Unable to parse media tags for file.
								mInfo = mInfo.replace("{0}", StringUtil.encodeHTML(request.requestedFilePath));
							}
							response.setHeader("Content-Type", "text/html; charset=UTF-8");
							response.setHeader("Server", SERVER_NAME_HEADER);
							response.setHeader("Expires", StringUtils.getCacheTime(System.currentTimeMillis() + 300000L));//5 minutes from now
							response.setHeader("Cache-Control", "no-cache, must-revalidate");
							response.setHeader("Date", StringUtils.getCurrentCacheTime());
							response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
							response.setHeader("Accept-Ranges", "none");
							response.setResponse("<!DOCTYPE html>\r\n" + //
							"<html>\r\n\t<head>\r\n" + //
							"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
							"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
							"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
							"\t\t<title>" + requestedFile.getName() + " - Media Info - " + domainDirectory.getServerName() + "</title>\r\n" + //
							"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
							(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
							AUTO_RESIZE_JAVASCRIPT + //
							"\t</head>\r\n" + //
							"\t<body>\r\n" + //
							"\t\t" + pageHeader + "<br>\r\n" + //
							"\t\t" + mInfo + "\r\n" + //
							(autoplay ? "\t\t<" + (isMimeTypeVideo(info.mimeType) ? "video" : "audio") + " autoplay controls=\"\" preload=\"auto\" name=\"" + requestedFile.getName() + "\"><source src=\"" + fileLink + "\" type=\"" + info.mimeType + "\"></" + (isMimeTypeVideo(info.mimeType) ? "video" : "audio") + ">" : "\t\t<a href=\"" + fileLink + "\">Click to " + (isMimeTypeAudio(info.mimeType) ? "listen" : "watch") + "</a>&nbsp;\r\n") + //
							(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
							(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
							"\t</body>\r\n</html>");
						}
						//} else {
						//(Tell the client media info parsing is not enabled for this domain)
						//}
					} else {
						response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Expires", StringUtils.getCacheTime(System.currentTimeMillis() + 60000L));//1 minute from now
						response.setHeader("Cache-Control", "no-cache, must-revalidate");
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
						response.setHeader("Accept-Ranges", "none");
						response.setResponse("<!DOCTYPE html>\r\n" + //
						"<html>\r\n\t<head>\r\n" + //
						"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
						"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
						"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
						"\t\t<title>" + requestedFile.getName() + " - Media Info - " + domainDirectory.getServerName() + "</title>\r\n" + //
						"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
						(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
						AUTO_RESIZE_JAVASCRIPT + //
						"\t</head>\r\n" + //
						"\t<body>\r\n" + //
						"\t\t" + pageHeader + "<br>\r\n" + //
						"\t\t<string>The file &quot;" + request.requestedFilePath + "&quot; is not a media file(audio/***, video/***, etc.) or has an unrecognized MIME type.<br>The file's MIME type is:&nbsp;<pre>" + info.mimeType + "</pre></string>\r\n" + //
						"\t\t<a href=\"" + fileLink + "\">Open/download file</a>&nbsp;\r\n" + //
						(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
						(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
						"\t</body>\r\n</html>");
					}
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					//s.close();
					connectedClients.remove(clientInfo);
					return;
				}
				String line = "";
				if(ext.equals("url") && !(line = StringUtils.getUrlLinkFromFile(requestedFile)).isEmpty()) {
					response.setHeader("Content-Type", "text/html; charset=UTF-8");
					response.setHeader("Server", SERVER_NAME_HEADER);
					response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					response.setHeader("Date", StringUtils.getCurrentCacheTime());
					response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
					response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
					long random = ThreadLocalRandom.current().nextLong();
					response.setResponse("<!DOCTYPE html>\r\n" + //
					"<html>\r\n\t<head>\r\n" + //
					"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
					"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
					"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
					"\t\t<title>" + requestedFile.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
					"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
					(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
					AUTO_RESIZE_JAVASCRIPT + //
					"\t</head>\r\n" + //
					"\t<body>\r\n" + //
					"\t\t" + pageHeader + "\r\n" + //
					(request.referrerLink.isEmpty() ? "" : "\t\t<a href=\"" + request.referrerLink + "\">Back to previous page</a><br>\r\n") + //
					"\t\t<iframe src=\"" + line + "\" sandbox=\"allow-forms allow-scripts\" frameborder=\"1\" width=\"100%\" height=\"85%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe><hr>\r\n" + //
					"\t\t<a href=\"" + line + "\" target=\"_blank\">[link to url]</a>\r\n" + //
					"\t</body>\r\n</html>");
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					//s.close();
					connectedClients.remove(clientInfo);
					return;
				} else if(info.mimeType.startsWith("text/")) {// && !info.mimeType.equalsIgnoreCase("text/html") && !info.mimeType.equalsIgnoreCase("text/xml") && !info.mimeType.equalsIgnoreCase("text/css")
					final String charset = StringUtils.getDetectedEncoding(requestedFile);
					response.setHeader("Content-Type", info.mimeType + "; charset=" + charset);
					response.setHeader("Server", SERVER_NAME_HEADER);
					response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					response.setHeader("Date", StringUtils.getCurrentCacheTime());
					response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
					response.setHeader("Accept-Ranges", "none");
					response.setResponse(StringUtils.getTextFileAsString(requestedFile));
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					//s.close();
					connectedClients.remove(clientInfo);
					return;
				} else if(autoClose && isMimeTypeMedia(info.mimeType)) {
					response.setHeader("Content-Type", "text/html; charset=UTF-8");
					response.setHeader("Server", SERVER_NAME_HEADER);
					response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					response.setHeader("Date", StringUtils.getCurrentCacheTime());
					response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
					response.setHeader("Accept-Ranges", (allowByteRangeRequests ? "bytes" : "none"));
					long random = ThreadLocalRandom.current().nextLong();
					response.setResponse("<!DOCTYPE html>\r\n" + //
					"<html>\r\n\t<head>\r\n" + //
					"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
					"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
					"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
					"\t\t<title>" + requestedFile.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
					"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
					(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
					AUTO_RESIZE_JAVASCRIPT + //
					"\t</head>\r\n" + //
					"\t<body>\r\n" + //
					"\t\t" + pageHeader + "\r\n" + //
					"<video controls=\"\" autoplay=\"metadata\" name=\"media\" onended=\"window.close();\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"><source src=\"" + fileLink + "\" type=\"" + info.mimeType + "\"></video>" + //
					"\t</body>\r\n</html>");
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					//s.close();
					connectedClients.remove(clientInfo);
					return;
				}
				response.setHTTPVersion(versionToUse);
				response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				String mimeType = info.mimeType;
				if(ext.equalsIgnoreCase("m4a") && request.userAgent.toLowerCase().contains("vlc")) {
					mimeType = "audio/x-m4a";//XXX Fix for VLC when streaming m4a files, tested and works!
				}
				response.setHeader("Content-Type", mimeType);
				response.setHeader("Server", SERVER_NAME_HEADER);
				response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
				response.setHeader("Date", StringUtils.getCurrentCacheTime());
				response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
				response.setHeader("Accept-Ranges", (allowByteRangeRequests ? "bytes" : "none"));
				response.setResponse(info);
				try {
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					println("\t\t\tSent file \r\n\t\t\t\"" + FilenameUtils.normalize(requestedFile.getAbsolutePath()) + "\"\r\n\t\t\t to client \"" + clientAddress + "\" successfully.");
				} catch(Throwable e) {
					println("\t /!\\\tFailed to send file \r\n\t/___\\\t\"" + FilenameUtils.normalize(requestedFile.getAbsolutePath()) + "\"\r\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
				}
				//s.close();
				connectedClients.remove(clientInfo);
				return;
			}
			response.setStatusCode(HTTP_304);
			response.setHeader("Vary", "Accept-Encoding");
			response.setHeader("Server", SERVER_NAME_HEADER);
			response.setHeader("Date", StringUtils.getCurrentCacheTime());
			response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
			response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
			response.setHeader("Connection", (keepAlive ? "keep-alive" : "close"));
			response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
			response.setResponse((String) null);
			response.sendToClient(s, false);
			//s.close();
			connectedClients.remove(clientInfo);
			return;
		}
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
		final String HTTP_Code = connectionSeemsMalicious ? "429 Too Many Requests" : "200 OK";
		
		println("\t*** " + versionToUse + " " + HTTP_Code + " - Directory listing");//XXX Directory Listing
		out.println(versionToUse + " " + HTTP_Code);
		out.println("Vary: Accept-Encoding");
		out.println("Server: " + SERVER_NAME_HEADER);
		out.println("Date: " + StringUtils.getCurrentCacheTime());
		out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
		out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(requestedFile) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
		out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
		out.println("Accept-Ranges: none");
		if(keepAlive) {
			out.println("Keep-Alive: timeout=30");
		}
		
		final String greenBorder = "<p class=\"green_border\">";
		final String brdrEnd = "</p>";
		
		String xspfCheck = request.requestArguments.get("xspf");
		final boolean xspf = xspfCheck != null ? (xspfCheck.equals("1") || xspfCheck.equalsIgnoreCase("true")) : false;
		String xmlCheck = request.requestArguments.get("xml");
		final boolean xml = xmlCheck != null ? (xmlCheck.equals("1") || xmlCheck.equalsIgnoreCase("true")) : false;
		String mediaViewCheck = request.requestArguments.get("mediaView");
		final boolean mediaViewFlag = mediaViewCheck != null ? (mediaViewCheck.equals("1") || mediaViewCheck.equalsIgnoreCase("true")) : false;
		String mediaListCheck = request.requestArguments.get("mediaList");
		final boolean mediaListFlag = mediaListCheck != null ? (mediaListCheck.equals("1") || mediaListCheck.equalsIgnoreCase("true")) : false;
		String filter = request.requestArguments.get("filter");
		String sort = request.requestArguments.get("sort");
		boolean wasSortNull = false;
		if(sort == null) {
			sort = "fileNames";
			wasSortNull = true;
		}
		final boolean isSortReversed = sort.startsWith("-");
		sort = isSortReversed && sort.length() > 1 ? sort.substring(1) : sort;
		final boolean useSortView = (domainDirectory.getEnableSortView() && !sort.equals("fileNames"));
		
		String linkTarget = request.requestArguments.get("target");
		linkTarget = (linkTarget != null && !linkTarget.isEmpty() ? " target=\"" + linkTarget + "\"" : "");
		
		String[] c = requestedFile.list();
		final HashMap<Integer, String> files = new HashMap<>();
		if(c != null) {
			int j = 0;
			for(int i = 0; i < c.length; i++) {
				String fileName = c[i];
				File file = new File(requestedFile, fileName);
				if(file.exists() && !RestrictedFile.isHidden(file)) {
					Integer key = Integer.valueOf(j);
					files.put(key, fileName);
					j++;
				}
			}
		}
		//ArrayList<String> files = c != null ? new ArrayList<>(Arrays.asList(c)) : new ArrayList<String>();
		String responseStr;
		
		String requestArgs = "?";
		boolean containsAnyArgs = false;
		for(Entry<String, String> entry : request.requestArguments.entrySet()) {
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
		
		if(domainDirectory.getIgnoreThumbsdbFiles()) {
			for(Entry<Integer, String> entry : new HashMap<>(files).entrySet()) {
				String fileName = entry.getValue();
				if(fileName != null) {
					if(fileName.equalsIgnoreCase("thumbs.db")) {
						files.remove(entry.getKey());
					}
				}
			}
		}
		
		String options = "";
		ArrayList<String> filterExts = new ArrayList<>();
		ArrayList<String> filterNonExts = new ArrayList<>();
		if(domainDirectory.getEnableFilterView()) {
			ArrayList<String> extensions = new ArrayList<>();
			boolean areThereDirectories = false;
			boolean areThereFiles = false;
			boolean areThereMediaFiles = false;
			boolean areThereImageFiles = false;
			boolean areThereTextFiles = false;
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
						final String mimeType = domainDirectory.getMimeTypeForExtension("." + ext).toLowerCase();
						if(isMimeTypeMedia(mimeType)) {
							areThereMediaFiles = true;
						} else if(mimeType.startsWith("image/")) {
							areThereImageFiles = true;
						} else if(mimeType.startsWith("text/")) {
							areThereTextFiles = true;
						}
						if(file.isFile()) {
							areThereFiles = true;
						}
						if(!extensions.contains(ext) && !ext.equals(filter)) {
							if(file.isFile() && !ext.isEmpty()) {
								extensions.add(ext);
								options += "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=" + ext) + "\">" + ext + "</option>\r\n";
								areThereFiles = true;
							} else if(file.isDirectory()) {
								areThereDirectories = true;
							}
						} else if(!RestrictedFile.isFileRestricted(file, s.getInetAddress().getHostAddress())) {
							if(file.isDirectory()) {
								areThereDirectories = true;
							} else if(file.isFile()) {
								areThereFiles = true;
							}
						}
					}
				}
			}
			if(!options.isEmpty() || areThereDirectories || areThereFiles) {
				final String dirOption = areThereDirectories ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=DIRECTORY") + "\">(Folders)</option>\r\n" : "";
				final String mediaOption = areThereMediaFiles ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=mediaFiles") + "\">(Media Files)</option>\r\n" : "";
				final String imageOption = areThereImageFiles ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=imageFiles") + "\">(Image Files)</option>\r\n" : "";
				final String textOption = areThereTextFiles ? "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=textFiles") + "\">(Text Files)</option>\r\n" : "";
				if((filter != null && !filter.equalsIgnoreCase("textFiles")) || filter == null) {
					options = textOption + options;
				}
				if((filter != null && !filter.equalsIgnoreCase("imageFiles")) || filter == null) {
					options = imageOption + options;
				}
				if((filter != null && !filter.equalsIgnoreCase("mediaFiles")) || filter == null) {
					options = mediaOption + options;
				}
				if((filter != null && !filter.equalsIgnoreCase("directory")) || filter == null) {
					options = dirOption + options;
				}
				options = "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=") + "\">(No filter)</option>\r\n" + options;
				if(filter != null) {
					if(!filter.isEmpty() && !filter.equalsIgnoreCase("directory") && !filter.equalsIgnoreCase("mediaFiles") && !filter.equalsIgnoreCase("imageFiles") && !filter.equalsIgnoreCase("textFiles")) {
						options = "\t\t\t<option value=\"" + (fileLinkAndReq + requestArgsAppendChar + "filter=" + filter) + "\">" + filter + "</option>\r\n" + options;
					} else if(filter.equalsIgnoreCase("directory")) {
						options = dirOption + options;
					} else if(filter.equalsIgnoreCase("mediaFiles")) {
						options = mediaOption + options;
					} else if(filter.equalsIgnoreCase("imageFiles")) {
						options = imageOption + options;
					} else if(filter.equalsIgnoreCase("textFiles")) {
						options = textOption + options;
					}
				}
			}
			if(filter != null && !filter.isEmpty()) {
				String[] split = filter.split("\\,");
				for(String ext : split) {
					if(ext.startsWith("-") && ext.length() > 1) {
						filterNonExts.add(ext.substring(1));
					} else if(!ext.startsWith("-")) {
						filterExts.add(ext);
					}
				}
				ArrayList<Integer> keysToRemove = new ArrayList<>();
				for(Entry<Integer, String> entry : files.entrySet()) {
					String filePath = entry.getValue();
					String ext = FilenameUtils.getExtension(filePath);
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					final String mimeType = domainDirectory.getMimeTypeForExtension("." + ext).toLowerCase();
					final boolean isMediaFile = isMimeTypeMedia(mimeType);
					final boolean isImageFile = mimeType.startsWith("image/");
					final boolean isTextFile = mimeType.startsWith("text/");
					boolean addFilePath = false;
					
					if(!filterExts.isEmpty()) {
						if((ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterExts, ext)) {
							if(file.exists() ? file.isFile() : true) {
								addFilePath = true;
							}
						} else if((ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterExts, "mediaFiles") && isMediaFile) {
							addFilePath = true;
						} else if((ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterExts, "imageFiles") && isImageFile) {
							addFilePath = true;
						} else if((ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterExts, "textFiles") && isTextFile) {
							addFilePath = true;
						} else if(StringUtils.containsIgnoreCase(filterExts, "directory") && (file.exists() && file.isDirectory())) {
							addFilePath = true;
						}
					} else if(!filterNonExts.isEmpty()) {
						if(isTextFile && (ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterNonExts, "textFiles")) {
							addFilePath = false;
						} else if(isImageFile && (ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterNonExts, "imageFiles")) {
							addFilePath = false;
						} else if(isMediaFile && (ext != null && !ext.isEmpty()) && StringUtils.containsIgnoreCase(filterNonExts, "mediaFiles")) {
							addFilePath = false;
						} else if((ext != null && !ext.isEmpty()) && !StringUtils.containsIgnoreCase(filterNonExts, ext)) {
							if(file.exists() ? file.isFile() : true) {
								addFilePath = true;
							}
						} else if(!StringUtils.containsIgnoreCase(filterNonExts, "directory") && (file.exists() && file.isDirectory())) {
							addFilePath = true;
						}
					}
					if(!addFilePath) {
						keysToRemove.add(entry.getKey());
					}
					
					/*if(filter.equalsIgnoreCase(ext) && (file.exists() ? file.isFile() : true)) {
						files.add(filePath);
					} else if(filter.equalsIgnoreCase("directory") && (file.exists() && file.isDirectory())) {
						files.add(filePath);
					}*/
				}
				for(Integer key : keysToRemove) {
					files.remove(key);
				}
			}
		} else {
			filter = null;
		}
		
		clientInfo.requestedFile.contentLength = String.valueOf(files.size());
		final String randomLink = domainDirectory.getEnableFilterView() ? MapUtil.getRandomLinkFor(files, path, pathPrefix, domainDirectory, httpProtocol, clientInfo) : null;
		
		boolean doesDirContainMediaFiles = false;
		for(Entry<Integer, String> entry : files.entrySet()) {
			String filePath = entry.getValue();
			if(filePath == null || filePath.isEmpty()) {
				continue;
			}
			String newPath = path + "/" + filePath;
			if(newPath.startsWith("//")) {
				newPath = newPath.substring(1);
			}
			File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
			if(file.exists()) {
				if(isMimeTypeMedia(domainDirectory.getMimeTypeForExtension("." + FilenameUtils.getExtension(file.getAbsolutePath())).toLowerCase())) {
					doesDirContainMediaFiles = true;
					break;
				}
			}
		}
		final boolean enableMediaInfo = doesDirContainMediaFiles;//TODO && domainDirectory.getEnableMediaInfoParsing();
		final boolean filterFlag = filter != null && !filter.trim().isEmpty();
		
		final String addFilterStr = (filterFlag ? "&filter=" + filter : "");
		final String addSortStr = (requestedFile.isDirectory() && !wasSortNull ? "&sort=" + (isSortReversed ? "-" : "") + sort : "");
		final boolean isFolderRoot = FilenameUtils.normalize(requestedFile.getAbsolutePath()).equalsIgnoreCase(homeDirPath);
		
		final boolean normalViewFlag = !mediaViewFlag && !mediaListFlag;
		
		final String normalView = "\t\t<td>" + (normalViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "\">Normal View</a></b>" + (normalViewFlag ? brdrEnd : "") + "</td>\r\n";
		final String mediaView = (domainDirectory.getEnableMediaView() ? "\t\t<td>" + (mediaViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "?mediaView=1" + addFilterStr + "\">Media View</a></b>" + (mediaViewFlag ? brdrEnd : "") + "</td>\r\n" : "");
		final String mediaList = (domainDirectory.getEnableMediaList() ? "\t\t<td>" + (mediaListFlag && !mediaViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "?mediaList=1" + addSortStr + addFilterStr + "\">Media List</a></b>" + (mediaListFlag && !mediaViewFlag ? brdrEnd : "") + "</td>\r\n" : "");
		final String xmlView = (domainDirectory.getEnableXmlListView() ? "\t\t<td><b><a href=\"" + fileLink + "?xml=1" + addFilterStr + "\">Xml View</a></b></td>\r\n" : "");
		final String xspfView = (domainDirectory.getEnableVLCPlaylistView() && doesDirContainMediaFiles ? "\t\t<td><b><a href=\"" + fileLink + "?xspf=1" + addFilterStr + "\" download=\"" + (isFolderRoot ? domainDirectory.getDomain().replace(":", ";") : FilenameUtils.getName(requestedFile.getAbsolutePath())) + ".xspf\" title=\"Download and save as '*.xspf', then open with VLC\">VLC Playlist View</a></b></td>\r\n" : "");
		final String filterView = (domainDirectory.getEnableFilterView() && !options.isEmpty()) ? "\t\t<td>" + (filterFlag ? greenBorder : "") + "<b>Filter view:&nbsp;</b><select onChange=\"window.location.href=this.value\" title=\"Filter list\">\r\n"//
				+ options + "\t\t</select>" + (filterFlag ? brdrEnd : "") + "</td>\r\n" : (domainDirectory.getEnableFilterView() ? "<td><b>Filter view: (No files to filter)</b></td>\r\n" : "");
		final String randomLinkView = (randomLink != null ? "\t\t<td><b><a href=\"" + randomLink + "\" rel=\"nofollow\"" + linkTarget + " title=\"(You may need to refresh the page for the next random file to appear)\">Open random file</a></b>&nbsp;</td>\r\n" : "");
		final String bodyHeader = pageHeader + "\r\n" + (domainDirectory.getEnableAlternateDirectoryListingViews() ? "<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\"><tbody>" + normalView + xmlView + mediaView + (doesDirContainMediaFiles ? mediaList : "") + xspfView + filterView + randomLinkView + "</tbody></table><hr>\r\n" : "");
		
		final String backLink = fileLink + (addSortStr.isEmpty() ? "" : "?" + addSortStr.substring(1)) + (addFilterStr.isEmpty() ? "" : (addSortStr.isEmpty() ? "?" : "&") + addFilterStr.substring(1));
		
		if(xspf && domainDirectory.getEnableVLCPlaylistView()) {
			out.println("Content-Type: application/xspf+xml" + (request.userAgent.toLowerCase().contains("vlc") ? "" : "; charset=UTF-8"));//XXX Adding the charset information causes VLC to not recognize the file as *.xspf, making the whole point moot when streaming the playlist to VLC(downloaded and saved versions work fine)
			responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			responseStr += "<playlist xmlns=\"http://xspf.org/ns/0/\" xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\" version=\"1\">\r\n";
			responseStr += "\t<title>Playlist</title>\r\n";
			responseStr += "\t<trackList>\r\n";
			int numOfMediaFiles = 0;
			String trackList = "";
			for(Entry<Integer, String> entry : files.entrySet()) {
				String filePath = entry.getValue();
				if(s.isClosed()) {
					out.close();
					return;
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				if(clientInfo.requestedFile.isPaused) {
					while(clientInfo.requestedFile.isPaused) {
						try {
							Thread.sleep(1);
						} catch(Throwable ignored) {
						}
					}
					if(s.isClosed()) {
						out.close();
						return;
					}
					if(clientInfo.requestedFile.isCancelled) {
						out.close();
						s.close();
						return;
					}
				}
				String backup = trackList;
				int numBackup = numOfMediaFiles;
				try {
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringEscapeUtils.escapeXml10(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						
						final String mimeType = domainDirectory.getMimeTypeForExtension("." + FilenameUtils.getExtension(file.getAbsolutePath())).toLowerCase();
						if(isMimeTypeMedia(mimeType)) {
							final MediaInfo media = MediaReader.readFile(file, false);
							trackList += "\t\t<track>\r\n";
							trackList += "\t\t\t<location>" + StringEscapeUtils.escapeXml10(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias((newPath.startsWith("/") ? "" : "/") + newPath))) + "</location>\r\n";
							trackList += "\t\t\t<title>" + StringEscapeUtils.escapeXml10(media != null && media.title != null && !media.title.isEmpty() ? media.title : FilenameUtils.getName(file.getAbsolutePath())) + "</title>\r\n";
							trackList += media != null ? (media.artist != null && !media.artist.isEmpty() ? "\t\t\t<creator>" + StringEscapeUtils.escapeXml10(media.artist) + "</creator>\r\n" : "") : "";
							trackList += media != null ? (media.album != null && !media.album.isEmpty() ? "\t\t\t<album>" + StringEscapeUtils.escapeXml10(media.album) + "</album>\r\n" : "") : "";
							trackList += media != null ? (media.hasArtwork() ? "\t\t\t<image>" + StringEscapeUtils.escapeXml10(curFileLink + "?mediaInfo=1&artwork=1") + "</image>\r\n" : "") : "";
							trackList += media != null ? "\t\t\t<duration>" + Math.round(media.trackLengthDouble * 1000L) + "</duration>\r\n" : "";
							trackList += "\t\t\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\r\n";
							trackList += "\t\t\t\t<vlc:id>" + numOfMediaFiles + "</vlc:id>\r\n";
							trackList += "\t\t\t\t<vlc:option>network-caching=1000</vlc:option>\r\n";
							trackList += "\t\t\t</extension>\r\n";
							trackList += "\t\t</track>\r\n";
							numOfMediaFiles++;
							if(media != null) {
								media.close();
							}
						}
					}
				} catch(Throwable e) {
					e.printStackTrace();
					trackList = backup;
					numOfMediaFiles = numBackup;
				}
				clientInfo.requestedFile.bytesTransfered++;
				clientInfo.requestedFile.lastWriteTime = System.currentTimeMillis();
				clientInfo.requestedFile.lastWriteAmount = 1;
				clientInfo.requestedFile.currentWriteAmount = 1;
				CodeUtil.sleep(10L);
			}
			responseStr += trackList;
			responseStr += "\t</trackList>\r\n";
			responseStr += "\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\r\n";
			for(int i = 0; i < numOfMediaFiles; i++) {
				responseStr += "\t\t\t<vlc:item tid=\"" + i + "\"/>\r\n";
			}
			responseStr += "\t</extension>\r\n";
			responseStr += "</playlist>\r\n";
		} else if(xml && domainDirectory.getEnableXmlListView()) {
			out.println("Content-Type: text/xml; charset=UTF-8");//" + info.mimeType);
			
			responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			responseStr += "<directory>\r\n";
			responseStr += "\t<path>" + StringEscapeUtils.escapeXml10(path.startsWith("/") ? path : "/" + path) + "</path>\r\n";
			responseStr += "\t<last-modified>" + StringEscapeUtils.escapeXml10(StringUtils.getCacheTime(requestedFile.lastModified())) + "</last-modified>\r\n";
			responseStr += "\t<subfilesnum>" + files.size() + "</subfilesnum>\r\n";
			responseStr += "\t<src>" + StringEscapeUtils.escapeXml10(backLink) + "</src>\r\n";
			responseStr += "\t<client>" + StringEscapeUtils.escapeXml10(clientAddress) + "</client>\r\n";
			
			String fileTable = "";
			for(Entry<Integer, String> entry : files.entrySet()) {
				String filePath = entry.getValue();
				if(s.isClosed()) {
					out.close();
					return;
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				if(clientInfo.requestedFile.isPaused) {
					while(clientInfo.requestedFile.isPaused) {
						CodeUtil.sleep(10L);
					}
					if(s.isClosed()) {
						out.close();
						return;
					}
					if(clientInfo.requestedFile.isCancelled) {
						out.close();
						s.close();
						return;
					}
				}
				String backup = fileTable;
				try {
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						FileInfo curInfo = new FileInfo(file, domainDirectory);
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringEscapeUtils.escapeXml10(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						fileTable += "\t\t<file name=\"" + StringEscapeUtils.escapeXml10(FilenameUtils.getName(file.getAbsolutePath())) + "\">\r\n";
						fileTable += "\t\t\t<path>" + StringEscapeUtils.escapeXml10(unAliasedPath) + "</path>\r\n";
						fileTable += "\t\t\t<last-modified>" + StringUtils.getCacheTime(file.lastModified()) + "</last-modified>\r\n";
						fileTable += "\t\t\t<mime-type>" + curInfo.mimeType + "</mime-type>\r\n";
						fileTable += "\t\t\t<size>" + curInfo.contentLength + "</size>\r\n";
						fileTable += "\t\t\t<link>" + curFileLink + "</link>\r\n";
						fileTable += "\t\t</file>\r\n";
					}
				} catch(FileNotFoundException ignored) {
					fileTable = backup;
				} catch(Throwable e) {
					e.printStackTrace();
					fileTable = backup;
				}
				clientInfo.requestedFile.bytesTransfered++;
				clientInfo.requestedFile.lastWriteTime = System.currentTimeMillis();
				clientInfo.requestedFile.lastWriteAmount = 1;
				clientInfo.requestedFile.currentWriteAmount = 1;
			}
			
			responseStr += "\t<subfiles>\r\n" + fileTable + "\t</subfiles>\r\n";
			responseStr += "</directory>";
		} else if(mediaViewFlag && domainDirectory.getEnableMediaView()) {
			boolean autoplayFlag = autoplay;
			out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			
			String fileTable = "";
			for(Entry<Integer, String> entry : files.entrySet()) {
				CodeUtil.sleep(10L);
				Integer i = entry.getKey();
				String filePath = entry.getValue();
				if(s.isClosed()) {
					out.close();
					return;
				}
				if(clientInfo.requestedFile.isPaused) {
					while(clientInfo.requestedFile.isPaused) {
						try {
							Thread.sleep(1);
						} catch(Throwable ignored) {
						}
					}
					if(s.isClosed()) {
						out.close();
						return;
					}
					if(clientInfo.requestedFile.isCancelled) {
						out.close();
						s.close();
						return;
					}
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				try {
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					final File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						final FileInfo curInfo = new FileInfo(file, domainDirectory);
						final String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String mimeType = curInfo.mimeType;
						final String ext = FilenameUtils.getExtension(curInfo.filePath);
						final String fileName = FilenameUtils.getName(curInfo.filePath);
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						final String line1 = "\t\t<string>" + (domainDirectory.getNumberDirectoryEntries() ? "<b>[" + (i.intValue() + 1) + "]:</b> " : "") + "</string><a href=\"" + curFileLink + "\" target=\"_blank\">" + fileName + "</a><br>\r\n";
						final long random = ThreadLocalRandom.current().nextLong();
						final String lineEnd = "<hr>\r\n";
						if(isMimeTypeMedia(mimeType)) {
							fileTable += line1 + //
							"\t\t<video controls=\"\" preload=\"metadata\" " + (autoplayFlag ? "autoplay=\"autoplay\"" : "") + "name=\"" + fileName + "\">\r\n" + //
							"\t\t\t<source src=\"" + curFileLink + "\" type=\"" + mimeType + "\">\r\n\t\t</video>" + lineEnd;
							if(autoplayFlag) {
								autoplayFlag = false;//prevents multiple videos/songs from playing at the same time on the same page
							}
						} else if(!ext.equals("lnk") && mimeType.startsWith("text/") && !ext.equals("url")) {
							fileTable += line1 + //
							"\t\t<iframe src=\"" + curFileLink + "\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
						} else if(mimeType.startsWith("image/")) {
							Dimension imgSize = ImageUtil.getImageSizeFromFile(file);
							fileTable += line1 + //
							"\t\t<img style=\"-webkit-user-select: none\" src=\"" + curFileLink + "\"" + (imgSize != null ? "width=\"" + imgSize.getWidth() + "\" height=\"" + imgSize.getHeight() + "\"" : "") + ">" + lineEnd;
						} else if(mimeType.equals("application/pdf")) {
							fileTable += line1 + //
							"\t\t<embed name=\"plugin\" width=\"100%\" src=\"" + curFileLink + "\" type=\"" + mimeType + "\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\">" + lineEnd;
						} else if(ext.equals("url")) {
							String backup = fileTable;
							try(BufferedReader br = new BufferedReader(new FileReader(file))) {
								String line = "";
								while(br.ready()) {
									line = br.readLine();
									if(line.startsWith("URL=")) {
										line = line.substring(4);
										break;
									}
									line = "";
								}
								if(line.isEmpty()) {
									throw new Throwable();
								}
								fileTable += line1 + //
								"\t\t<iframe src=\"" + line + "\" sandbox=\"allow-forms allow-scripts\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
							} catch(Throwable ignored) {
								fileTable = backup + line1 + //
								"\t\t<iframe src=\"" + curFileLink + "\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
							}
						}
					}
				} catch(FileNotFoundException ignored) {
				} catch(Throwable e) {
					e.printStackTrace();
				}
				clientInfo.requestedFile.bytesTransfered++;
				clientInfo.requestedFile.lastWriteTime = System.currentTimeMillis();
				clientInfo.requestedFile.lastWriteAmount = 1;
				clientInfo.requestedFile.currentWriteAmount = 1;
			}
			
			responseStr = "<!DOCTYPE html>\r\n" + //
			"<html>\r\n" + //
			"\t<head>\r\n" + //
			"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
			"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
			"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
			"\t\t<title>" + folderName + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
			"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
			(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
			(fileTable.isEmpty() ? "" : AUTO_RESIZE_JAVASCRIPT) + //
			"\t</head>\r\n" + //
			"\t<body>\r\n" + //
			"\t\t" + bodyHeader + "\r\n" + //
			(fileTable.isEmpty() ? "\t\t<string>No media content to display.</string>&nbsp;<a href=\"" + backLink + "\">Back to " + requestedFile.getName() + "</a><br>\r\n" : "<string>Listing media files(text, images, audio, video, etc.) in <a href=\"" + backLink + "\" title=\"(Click to go back to " + requestedFile.getName() + ")\">" + requestedFile.getName() + "</a>:</string><br>" + fileTable) + //
			"\t</body>\r\n" + //
			"</html>";
		} else if(mediaListFlag && domainDirectory.getEnableMediaList()) {
			out.println("Expires: " + StringUtil.getCacheTime(System.currentTimeMillis() + 300000L));//5 minutes from now
			out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			
			final ArrayList<Integer> filePaths = new ArrayList<>();
			if(useSortView && !sort.equalsIgnoreCase("fileNames")) {//XXX Media List Sorting
				if(sort.equalsIgnoreCase("sizes") || sort.equalsIgnoreCase("dates") || sort.equalsIgnoreCase("types")) {
					HashMap<Integer, FileInfo> fileInfos = new HashMap<>();
					for(Entry<Integer, String> entry : files.entrySet()) {
						CodeUtil.sleep(10L);
						Integer i = entry.getKey();
						String filePath = entry.getValue();
						if(s.isClosed()) {
							out.close();
							return;
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
								fileInfos.put(i, new FileInfo(file, domainDirectory));
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
					filePaths.addAll(files.keySet());
				}
				if((!sort.equalsIgnoreCase("types") && isSortReversed) || (sort.equalsIgnoreCase("types") && !isSortReversed)) {
					Collections.reverse(filePaths);
				}
			}
			int count = 0;
			int j = 0;
			String fileTable = "";
			Set<Entry<Integer, String>> entrySet = files.entrySet();
			final int amt = entrySet.size();
			for(Entry<Integer, String> entry : entrySet) {
				count++;
				final boolean endOfLoop = count == amt;
				CodeUtil.sleep(10L);
				if(s.isClosed()) {
					out.close();
					return;
				}
				final Integer i;
				final String filePath;
				if(useSortView) {
					if(!filePaths.isEmpty()) {
						i = filePaths.remove(0);
						filePath = files.get(i);
					} else {
						continue;
					}
				} else {
					i = entry.getKey();
					filePath = entry.getValue();
				}
				if(clientInfo.requestedFile.isPaused) {
					while(clientInfo.requestedFile.isPaused) {
						try {
							Thread.sleep(1);
						} catch(Throwable ignored) {
						}
					}
					if(s.isClosed()) {
						out.close();
						return;
					}
					if(clientInfo.requestedFile.isCancelled) {
						out.close();
						s.close();
						return;
					}
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				try {
					String newPath = path + "/" + filePath;
					if(newPath.startsWith("//")) {
						newPath = newPath.substring(1);
					}
					final File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						final FileInfo curInfo = new FileInfo(file, domainDirectory);
						if(!isMimeTypeMedia(curInfo.mimeType)) {
							continue;
						}
						final boolean isVideo = isMimeTypeVideo(curInfo.mimeType);
						final String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String fileName = FilenameUtils.getName(curInfo.filePath);
						final String fileReqPath = StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath));
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + request.host + fileReqPath);
						fileTable += "\t\t<div>" + (domainDirectory.getNumberDirectoryEntries() ? "<b>[" + (i.intValue() + 1) + "]:</b> " : "") + "<a href=\"" + curFileLink + "\" target=\"_blank\" name=\"" + fileName + "\">" + fileName + "</a><br>\r\n" + //
						"\t\t\t" + MediaReader.getMediaInfoHTMLFor(file, domainDirectory.getDirectory().getAbsolutePath()).replace("{0}", fileReqPath) + "\r\n" + //
						"\t\t\t<" + (isVideo ? "video" : "audio") + (autoplay ? " id=\"autoplay_" + j + "\"" : "") + " controls=\"\" " + (autoplay && j == 0 ? "autoplay=\"autoplay\" " : "") + "preload=\"" + (autoplay ? (j == 0 ? "auto" : "none") : "metadata") + "\" name=\"" + fileName + "\">\r\n" + //
						"\t\t\t<source src=\"" + curFileLink + "\" type=\"" + curInfo.mimeType + "\">\r\n\t\t</" + (isVideo ? "video" : "audio") + ">" + //
						"\t\t</div><hr>\r\n";
						if(autoplay && !endOfLoop) {//XXX mediaList + autoplay = smiley-face
							//fileTable += "\t\t<script>\r\n	var video = document.getElementsByTagName('video')[" + j + "];\r\n	video.onended = function(e) {\r\n		document.getElementsByTagName('video')[" + (j + 1) + "].play();\r\n	};\r\n</script>\r\n";
							fileTable += "\t\t<script>\r\n	var video = document.getElementById('autoplay_" + j + "');\r\n	video.onended = function(e) {\r\n		document.getElementById('autoplay_" + (j + 1) + "').play();\r\n	};\r\n</script>\r\n";
						}
						j++;
					}
				} catch(FileNotFoundException ignored) {
				} catch(Throwable e) {
					e.printStackTrace();
				}
				clientInfo.requestedFile.bytesTransfered++;
				clientInfo.requestedFile.lastWriteTime = System.currentTimeMillis();
				clientInfo.requestedFile.lastWriteAmount = 1;
				clientInfo.requestedFile.currentWriteAmount = 1;
			}
			
			responseStr = "<!DOCTYPE html>\r\n" + //
			"<html>\r\n" + //
			"\t<head>\r\n" + //
			"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
			"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
			"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
			"\t\t<title>" + folderName + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
			"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
			(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
			(fileTable.isEmpty() ? "" : AUTO_RESIZE_JAVASCRIPT) + //
			"\t</head>\r\n" + //
			"\t<body>\r\n" + //
			"\t\t" + bodyHeader + "\r\n" + //
			(fileTable.isEmpty() ? "\t\t<string>No media content to display.</string>&nbsp;<a href=\"" + backLink + "\">Back to " + requestedFile.getName() + "</a><br>\r\n" : "<string>Listing tag information for media files in <a href=\"" + backLink + "\" title=\"(Click to go back to " + requestedFile.getName() + ")\">" + requestedFile.getName() + "</a>:</string><br>" + fileTable) + //
			"\t</body>\r\n" + //
			"</html>";
		} else {
			out.println("Content-Type: text/html; charset=UTF-8");
			
			String fileTable = "\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n" + //TODO File table settings - Make these configurable per-domain!
			"\t\t\t<tbody>\r\n";
			
			if(domainDirectory.getEnableSortView()) {
				final String reqArgsWithoutSort = StringUtils.requestArgumentsToString(request.requestArguments, "sort");
				final String link = fileLink + reqArgsWithoutSort + (reqArgsWithoutSort.isEmpty() ? "?" : "&");
				final String triangleStr = (isSortReversed ? "&nbsp;▲" : "&nbsp;▼");
				String numberSortLink = "<b><a href=\"" + link + "sort=" + (sort.equalsIgnoreCase("numbers") && !isSortReversed ? "-" : "") + "numbers\" color=\"#000000\"" + linkTarget + ">#" + (sort.equalsIgnoreCase("numbers") ? triangleStr : "") + "</a></b>";
				String fileNameSortLink = "<b><a href=\"" + link + "sort=" + (sort.equalsIgnoreCase("fileNames") && !isSortReversed ? "-" : "") + "fileNames\" color=\"#000000\"" + linkTarget + ">File Name" + (sort.equalsIgnoreCase("fileNames") ? triangleStr : "") + "</a></b>";
				String sizeSortLink = "<b><a href=\"" + link + "sort=" + (sort.equalsIgnoreCase("sizes") && !isSortReversed ? "-" : "") + "sizes\" color=\"#000000\"" + linkTarget + ">Size" + (sort.equalsIgnoreCase("sizes") ? triangleStr : "") + "</a></b>";
				String dateSortLink = "<b><a href=\"" + link + "sort=" + (sort.equalsIgnoreCase("dates") && !isSortReversed ? "-" : "") + "dates\" color=\"#000000\"" + linkTarget + ">Date" + (sort.equalsIgnoreCase("dates") ? triangleStr : "") + "</a></b>";
				String typeSortLink = "<b><a href=\"" + link + "sort=" + (sort.equalsIgnoreCase("types") && !isSortReversed ? "-" : "") + "types\" color=\"#000000\"" + linkTarget + ">Type" + (sort.equalsIgnoreCase("types") ? triangleStr : "") + "</a></b>";
				
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + numberSortLink + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td>" + fileNameSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + sizeSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + dateSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + typeSortLink + "</td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td><b>Media Information</b></td>" : "") + "</tr>\r\n";
			} else {
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td><b>#</b></td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><b>File Name</b></td><td>" + nonBreakingSpaces + "</td><td><b>Size</b></td><td>" + nonBreakingSpaces + "</td><td><b>Date</b></td><td>" + nonBreakingSpaces + "</td><td><b>Type</b></td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td><b>Media Information</b></td>" : "") + "</tr>\r\n";
			}
			
			if(parentFileLink != null) {
				FileInfo curInfo = new FileInfo(parentFile, domainDirectory);
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>(-)</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + parentFileLink + (!wasSortNull ? "?sort=" + (isSortReversed ? "-" : "") + sort : "") + "\"" + linkTarget + "><b>../(Up)</b></a></td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.mimeType + (RestrictedFile.isFileForbidden(parentFile) ? "\t\t(Forbidden)" : "") + "</td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td>(-)</td>" : "") + "</tr>\r\n";
			}
			final ArrayList<Integer> folderPaths = new ArrayList<>();
			final ArrayList<Integer> filePaths = new ArrayList<>();
			if(useSortView && !sort.equalsIgnoreCase("fileNames")) {//XXX Sorting
				if(sort.equalsIgnoreCase("sizes") || sort.equalsIgnoreCase("dates") || sort.equalsIgnoreCase("types")) {
					HashMap<Integer, FileInfo> fileInfos = new HashMap<>();
					for(Entry<Integer, String> entry : files.entrySet()) {
						CodeUtil.sleep(10L);
						Integer i = entry.getKey();
						String filePath = entry.getValue();
						if(s.isClosed()) {
							out.close();
							return;
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
								fileInfos.put(i, new FileInfo(file, domainDirectory));
							}
						} catch(FileNotFoundException ignored) {
						} catch(Throwable e) {
							e.printStackTrace();
						}
					}
					filePaths.clear();
					folderPaths.clear();
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
					filePaths.addAll(files.keySet());
				}
				if((!sort.equalsIgnoreCase("types") && isSortReversed) || (sort.equalsIgnoreCase("types") && !isSortReversed)) {
					Collections.reverse(filePaths);
				}
			} else if(domainDirectory.getListDirectoriesFirst()) {//XXX Sorting folders and files in order
				for(Entry<Integer, String> entry : files.entrySet()) {
					CodeUtil.sleep(10L);
					Integer i = entry.getKey();
					String filePath = entry.getValue();
					if(s.isClosed()) {
						out.close();
						return;
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
							} else {
								filePaths.add(i);
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
			if(domainDirectory.getListDirectoriesFirst() && sort.equalsIgnoreCase("fileNames") && isSortReversed) {
				ArrayList<Integer> folderPathsCopy = new ArrayList<>(folderPaths);
				ArrayList<Integer> filePathsCopy = new ArrayList<>(filePaths);
				folderPaths.clear();
				filePaths.clear();
				Collections.reverse(folderPathsCopy);
				Collections.reverse(filePathsCopy);
				folderPaths.addAll(filePathsCopy);
				filePaths.addAll(folderPathsCopy);
			}
			for(Entry<Integer, String> entry : files.entrySet()) {
				CodeUtil.sleep(10L);
				if(s.isClosed()) {
					out.close();
					return;
				}
				final Integer i;
				final String filePath;
				if(useSortView) {
					if(!filePaths.isEmpty()) {
						i = filePaths.remove(0);
						filePath = files.get(i);
					} else {
						//i = null;
						//filePath = null;
						continue;
					}
				} else if(domainDirectory.getListDirectoriesFirst()) {
					if(!folderPaths.isEmpty()) {
						i = folderPaths.remove(0);
						filePath = files.get(i);
					} else if(!filePaths.isEmpty()) {
						i = filePaths.remove(0);
						filePath = files.get(i);
					} else {
						//i = null;
						//filePath = null;
						continue;
					}
				} else {
					i = entry.getKey();
					filePath = entry.getValue();
				}
				if(clientInfo.requestedFile.isPaused) {
					while(clientInfo.requestedFile.isPaused) {
						CodeUtil.sleep(10L);
					}
					if(s.isClosed()) {
						out.close();
						return;
					}
					if(clientInfo.requestedFile.isCancelled) {
						out.close();
						s.close();
						return;
					}
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
						FileInfo curInfo = new FileInfo(file, domainDirectory);
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						String mimeType = curInfo.mimeType + (RestrictedFile.isFileForbidden(file) ? "\t\t(Forbidden)" : "");
						String extraViewStr = "";
						if(domainDirectory.getEnableReadableFileViews()) {
							if(curInfo.mimeType.equalsIgnoreCase("application/epub+zip") || curInfo.mimeType.equalsIgnoreCase("application/rtf") || curInfo.mimeType.equalsIgnoreCase("text/rtf")) {
								extraViewStr = nonBreakingSpaces + "<a href=\"" + curFileLink + "?displayFile=1\"" + linkTarget + "><b>*** Readable View ***</b></a>";
							}
						}
						fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + (i.intValue() + 1) + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + curFileLink + (file.isDirectory() && !wasSortNull ? "?sort=" + (isSortReversed ? "-" : "") + sort : "") + "\"" + linkTarget + " name=\"" + curInfo.fileName + "\">" + curInfo.fileName + "</a>" + extraViewStr + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + mimeType + "</td>" + (enableMediaInfo && isMimeTypeMedia(curInfo.mimeType) ? "</td><td>" + nonBreakingSpaces + "</td><td><b><a href=\"" + curFileLink + "?mediaInfo=1\" title=\"(Opens in new tab/window)\" target=\"_blank\">Media Tags</a></b></td>" : "") + "</tr>\r\n";
					}
				} catch(FileNotFoundException ignored) {
				} catch(Throwable e) {
					e.printStackTrace();
				}
				clientInfo.requestedFile.bytesTransfered++;
				clientInfo.requestedFile.lastWriteTime = System.currentTimeMillis();
				clientInfo.requestedFile.lastWriteAmount = 1;
				clientInfo.requestedFile.currentWriteAmount = 1;
			}
			fileTable += "\t\t\t</tbody>\r\n" + //
			"\t\t</table>\r\n";
			
			String administrateForm = "";
			final String administrateParamStr = (containsAnyArgs ? "&" : "?") + "administrateFile=1";
			if(domainDirectory.getEnableFileUpload()) {
				administrateForm += "\t\t<button title=\"Administrate this folder\" onclick=\"window.location.href=window.location.href.split('#')[0].split('?')[0] + '" + administrateParamStr + "'\">Administrate</button>\r\n";
			}
			/*if(domainDirectory.getEnableFileUpload()) {
				uploadForm += "\t\t<button title=\"Upload files to this directory\" type=\"button\" onclick=\"document.getElementById('spoiler').style.display=(document.getElementById('spoiler').style.display=='none' ? '' : 'none')\">Upload Files</button>\r\n";
				uploadForm += "\t\t<div id=\"spoiler\" style=\"display:none\">\r\n";
				uploadForm += "\t\t\t<form action=\"" + requestedFilePath + "\" enctype=\"multipart/form-data\" method=\"post\">\r\n";
				uploadForm += "\t\t\t\t<string>Select files from your computer to be uploaded to this directory(you will be prompted to log in as the server administrator)</string><br>\r\n";
				uploadForm += "\t\t\t\t<input type=\"file\" name=\"files\" multiple>\r\n";
				uploadForm += "\t\t\t\t<input type=\"submit\" value=\"Upload\">\r\n";
				uploadForm += "\t\t\t</form>\r\n";
				uploadForm += "\t\t</div>";
			}*/
			
			final String adminLink = enableAdminInterface ? (request.requestedFilePath.startsWith("https://") ? "https://" : "http://") + request.hostNoPort + ":" + admin_listen_port : null;
			final String adminAnchor = (adminLink != null ? "<b><a href=\"" + adminLink + "\" target=\"_blank\" rel=\"nofollow\" title=\"Change server-wide settings\">Server Administration</a></b>" : "");
			
			responseStr = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
					+ "\t\t<title>" + folderName + " - " + domainDirectory.getServerName() + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
					+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t" + bodyHeader + "\r\n" + fileTable + administrateForm + adminAnchor + "\r\n"//
					+ "\t</body>\r\n</html>";
		}
		if(request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
			out.println("Content-Encoding: gzip");
			byte[] r = StringUtils.compressString(responseStr, "UTF-8");
			out.println("Content-Length: " + r.length);
			out.println("");
			if(request.method.equalsIgnoreCase("GET")) {
				outStream.write(r);
				println("\t\tSent directory tree to client \"" + clientAddress + "\".");
			} else if(request.method.equalsIgnoreCase("HEAD")) {
				println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
			}
			r = null;
		} else {
			out.println("Content-Length: " + responseStr.length());
			out.println("");
			if(request.method.equalsIgnoreCase("GET")) {
				out.println(responseStr);
				println("\t\tSent directory tree to client \"" + clientAddress + "\".");
			} else if(request.method.equalsIgnoreCase("HEAD")) {
				println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
			}
		}
		out.flush();
		connectedClients.remove(clientInfo);
		return;
	}
	
	/*public static final HttpDigestAuthorization getOrCreateAuthForCurrentThread() {
		HttpDigestAuthorization rtrn = serverAdministrationAuths.get(Thread.currentThread());
		if(rtrn == null) {
			rtrn = new HttpDigestAuthorization(adminAuthorizationRealm, adminUsername, adminPassword);
			serverAdministrationAuths.put(Thread.currentThread(), rtrn);
		}
		return rtrn;
	}*/
	
	/** Allows the client to upload, rename, move, and delete files on the
	 * server, as well as change/set the homepage of the domain.
	 * 
	 * @param request The client's HTTP request
	 * @param requestedFile The file that the client is attempting to administer
	 * @param clientAddress The client's remote socket address(ip address of
	 *            computer used to contact this server)
	 * @param versionToUse HTTP/1.0 or HTTP/1.1
	 * @param httpProtocol HTTP or HTTPS
	 * @param outStream The client's output stream to use when sending raw byte
	 *            content
	 * @param out The PrintWriter to use when sending text
	 * @param domainDirectory The domain that the client used to connect to this
	 *            server(the host header in the client's request)
	 * @param keepAlive Whether or not keep-alive was specified
	 * @param reqFileName The name of the file requested
	 * @param pageHeader The page address header for standard server responses
	 * @param clientInfo The client information(deprecated)
	 * @return Whether or not this method did anything. If <code>false</code> is
	 *         returned, then the request should be treated as a normal client
	 *         request with
	 *         {@link #serveFileToClient(Socket, boolean, boolean, OutputStream, HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)}
	 *         through {@link #HandleRequest(Socket, InputStream, boolean)}.
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ReusedConn)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #getManagementStrForFile(String, String)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	private static final boolean handleAdministrateFile(HTTPClientRequest request, File requestedFile, final String clientAddress, final String versionToUse, final String httpProtocol, OutputStream outStream, PrintWriter out, DomainDirectory domainDirectory, boolean keepAlive, String reqFileName, String pageHeader, ClientInfo clientInfo) {
		if(request == null || requestedFile == null || !requestedFile.exists() || domainDirectory == null || out == null) {
			return false;
		}
		boolean send200OK = true;
		
		final String adminLink = enableAdminInterface ? (request.requestedFilePath.startsWith("https://") ? "https://" : "http://") + request.hostNoPort + ":" + admin_listen_port : null;
		final String adminAnchor = (adminLink != null ? "<b><a href=\"" + adminLink + "\" target=\"_blank\" rel=\"nofollow\" title=\"Change server-wide settings\">Server Administration</a></b>" : "");
		
		final String fileLink = StringUtils.encodeHTML(httpProtocol + request.host + request.requestedFilePath);
		
		final String homeLink = StringUtils.encodeHTML(httpProtocol + request.host);
		final String homeAdminLink = homeLink + "/?administrateFile=1";
		final String logoutLink = fileLink + "/?administrateFile=1&logout=1";
		final String logoutAnchor = "\t\t<hr><a href=\"" + logoutLink + "\" class=\"alignright\" style=\"display: none;\">Log out</a>\r\n";
		
		String logoutCheck = request.requestArguments.get("logout");
		final boolean logOut = logoutCheck != null ? logoutCheck.equals("1") || logoutCheck.equalsIgnoreCase("true") : false;
		String responseStr = "";
		//final HttpDigestAuthorization serverAdministrationAuth = getOrCreateAuthForCurrentThread();
		//final String clientIP = AddressUtil.getClientAddressNoPort(clientAddress);
		try {
			BasicAuthorizationResult authResult = null;
			String authCookie = null;
			if(logOut) {
				send200OK = false;
				out.println(versionToUse + " 200 OK");
				out.println("Vary: Accept-Encoding");
				out.println("Server: " + SERVER_NAME_HEADER);
				out.println("Date: " + StringUtils.getCurrentCacheTime());
				out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
				out.println("Cache-Control: " + cachePrivateMustRevalidate);
				out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
				final boolean wasLoggedIn = isLoggedIn(request);
				removeClientSessionID(request);//Remove the server-side session id, invalidating the client's copy
				if(keepAlive) {
					out.println("Keep-Alive: timeout=30");
				}
				out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
				out.flush();
				
				responseStr = "<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>File Administration Log-out - " + domainDirectory.getServerName() + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
						+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
						+ "\t</head>\r\n";
				if(wasLoggedIn) {
					responseStr += "\t<body bgcolor=\"#CCFFCC\">\r\n"//
							+ "\t\t<h1>You have successfully been logged out.<br>\r\n"//
							+ "\t\t" + pageHeader + "</h1><hr>\r\n"//
							+ "\t\t<b><a href=\"" + homeAdminLink + "\">Log in again</a>&nbsp;<a href=\"" + fileLink + "\">Return to the website</a></b>\r\n"//
							+ "\t</body>\r\n</html>";
				} else {
					responseStr += "\t<body bgcolor=\"#CC0000\">\r\n"//
							+ "\t\t<h1>You were not logged in.<br>\r\n"//
							+ "\t\t" + pageHeader + "</h1><hr>\r\n"//
							+ "\t\t<b><a href=\"" + homeAdminLink + "\">Log in</a>&nbsp;<a href=\"" + fileLink + "\">Return to the website</a></b>\r\n"//
							+ "\t</body>\r\n</html>";
				}
			} else {
				authResult = authenticateBasicForServerAdministration(request, useCookieAuthentication);//final AuthorizationResult authResult = serverAdministrationAuth.authenticate(request.authorization, new String(request.postRequestData), request.protocol, request.host, clientIP, request.cookies);
				final boolean passed = authResult.passed();
				authCookie = authResult.authorizedCookie != null ? authResult.authorizedCookie : authCookie;
				if(!passed) {//!areCredentialsValidForAdministration(clientUser, clientPass)) {
					send200OK = false;
					out.println(versionToUse + " 401 Authorization Required");
					out.println("Vary: Accept-Encoding");
					out.println("Server: " + SERVER_NAME_HEADER);
					out.println("Date: " + StringUtils.getCurrentCacheTime());
					out.println(authResult.resultingAuthenticationHeader);//"WWW-Authenticate: Basic realm=\"" + adminAuthorizationRealm + "\"");
					out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
					out.println("Cache-Control: " + cachePrivateMustRevalidate);
					out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
					if(authCookie != null) {
						out.println("Set-Cookie: " + authCookie);
					}
					if(keepAlive) {
						out.println("Keep-Alive: timeout=30");
					}
					out.println("Content-Type: text/html; charset=UTF-8");
					responseStr = "<!DOCTYPE html>\r\n"//
							+ "<html>\r\n\t<head>\r\n"//
							+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
							+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
							+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
							+ "\t\t<title>Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
							+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
							+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
							+ "\t</head>\r\n"//
							+ "\t<body>\r\n"//
							+ "\t\t<h1>Authorization Required</h1><hr>\r\n"//
							+ "\t\t<string title=\"In order to be able to administrate files on this server, you must be logged in as the administrator.\">You need permission to do that.</string><hr>\r\n"//
							+ "\t\t<string>" + pageHeader + "</string>\r\n"//
							+ "\t</body>\r\n</html>";
				} else {
					final FileInfo info = new FileInfo(requestedFile, domainDirectory);
					final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/');
					path = (path.trim().isEmpty() ? "/" : path);
					path = (path.startsWith("/") ? "" : "/") + path;
					
					String uploadForm = "";
					
					if(domainDirectory.getEnableFileUpload()) {
						uploadForm += "\t\t<form action=\"" + request.requestedFilePath + "\" enctype=\"multipart/form-data\" method=\"post\">\r\n";
						uploadForm += "\t\t\t<string>Select files from your computer to be uploaded to this directory:</string><br>\r\n";
						uploadForm += "\t\t\t<input type=\"file\" name=\"files\" multiple>\r\n";
						uploadForm += "\t\t\t<input type=\"submit\" value=\"Upload\">\r\n";
						uploadForm += "\t\t</form>";
					} else {
						uploadForm += "\t\t<string>This domain does not enable file uploads.</string>";//\r\n";
						//uploadForm += "\t\t<string>Click <a href=\"" + request.referrerLink + "\">here</a> to go back to the previous page.</string>";
					}
					final File defaultFile = domainDirectory.getFileFromRequest("/", new HashMap<String, String>());
					final String defaultFilePath = defaultFile != null ? FilenameUtils.normalize(defaultFile.getAbsolutePath()) : null;
					
					//final boolean isDefault = FilenameUtils.normalize(requestedFile.getAbsolutePath()).equalsIgnoreCase(defaultFilePath);
					//RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(requestedFile);
					//final boolean hidden = res == null ? false : res.isHidden.getValue().booleanValue();
					String renameCheck = request.requestArguments.get("renameFile");
					String deleteCheck = request.requestArguments.get("deleteFile");
					String moveCheck = request.requestArguments.get("moveFile");
					String restrictCheck = request.requestArguments.get("restrictFile");
					final boolean renameFile = renameCheck != null ? renameCheck.equals("1") || renameCheck.equalsIgnoreCase("true") : false;
					String renameTo = request.requestArguments.get("renameTo");
					final boolean deleteFile = deleteCheck != null ? deleteCheck.equals("1") || deleteCheck.equalsIgnoreCase("true") : false;
					final boolean moveFile = moveCheck != null ? moveCheck.equals("1") || moveCheck.equalsIgnoreCase("true") : false;
					final boolean restrictFile = restrictCheck != null ? restrictCheck.equals("1") || restrictCheck.equalsIgnoreCase("true") : false;
					
					final String parentFileLink;
					final File parentFile = requestedFile.getParentFile();
					final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath());
					if(parentFile != null && !FilenameUtils.normalize(requestedFile.getAbsolutePath()).equals(homeDirPath)) {
						String parentRequestPath = FilenameUtils.normalize(parentFile.getAbsolutePath()).replace(homeDirPath, "").replace("\\", "/");
						parentRequestPath = (parentRequestPath.startsWith("/") ? "" : "/") + parentRequestPath;
						parentFileLink = StringUtils.encodeHTML(httpProtocol + (request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(parentRequestPath))));
					} else {
						parentFileLink = null;
					}
					
					//final boolean containsAnyArgs = request.requestArguments.size() > 0;
					String fileOrFolder = requestedFile.isFile() ? "file" : "folder";
					
					if(deleteFile) {
						/*String deleteParamStr = (containsAnyArgs ? "&" : "?") + "administrateFile=1&deleteConfirm=";
						final String confirmButton = "<button title=\"Confirm deletion\" onclick=\"window.location.href=window.location.href.split('#')[0].split('?')[0] + '" + deleteParamStr + "1'\">Yes</button>";
						final String cancelButton = "<button title=\"Cancel deletion\" onclick=\"window.location.href=window.location.href.split('#')[0].split('?')[0] + '" + deleteParamStr + "0'\">No</button>";
						*/
						
						FileDeleteStrategy.FORCE.deleteQuietly(requestedFile);
						final boolean exists = requestedFile.exists();
						final String actionAnchors;
						if(exists) {
							actionAnchors = "<b><a href=\"" + fileLink + "?administrateFile=1&deleteFile=1\" rel=\"nofollow\" title=\"Try the delete operation again\">Retry</a>&nbsp;<a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + "?administrateFile=1\" rel=\"nofollow\" title=\"Cancel the delete operation\">Cancel</a></b>";
						} else {
							actionAnchors = "<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + "?administrateFile=1\" rel=\"nofollow\" title=\"Return to the administration page\">Done</a></b>";
						}
						responseStr = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Folder Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
								+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FF9999\">\r\n"//
								+ "\t\t<h1>Folder Administration -<br>\r\n"//
								+ "\t\t" + pageHeader + "</h1><hr>\r\n"//
								+ "\t\t<h2><font color=\"#FF0000\">Deleted " + fileOrFolder + " &quot;" + reqFileName + "&quot;</font></h2>\r\n"//
								+ logoutAnchor//
								+ "\t\t<string>" + (exists ? "Unable to delete the " + fileOrFolder + ". Please try again!(Is it a system file?)" : "Sucessfully deleted " + fileOrFolder + " &quot;" + reqFileName + "&quot;" + (parentFile != null ? " from parent folder &quot;" + parentFile.getName() + "&quot;" : "") + ".") + "</string><br>\r\n"//
								+ "\t\t" + actionAnchors + "\r\n"//
								/*+ "\t\t<string>Are you sure you wish to delete this folder?</string><br>\r\n"//
								+ "\t\t" + confirmButton + "" + nonBreakingSpaces + "" + cancelButton + "\r\n"//*/
								+ "\t</body>\r\n</html>";
					} else if(renameFile) {
						if(FilenameUtils.normalize(requestedFile.getAbsolutePath()).equals(homeDirPath)) {
							responseStr = "<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>Folder Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ "\t</head>\r\n"//
									+ "\t<body bgcolor=\"#EEEE11\">\r\n"//
									+ "\t\t<h1>Error renaming " + fileOrFolder + " -<br>\r\n"//
									+ "\t\t" + pageHeader + "</h1>\r\n"//
									+ logoutAnchor//
									+ "\t\t<h2><font color=\"#770000\">You cannot rename the home directory of a domain, as this would break the domain.</font></h2><hr>\r\n"//
									+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + "?administrateFile=1" + "\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a></b>\r\n"//
									+ "\t</body>\r\n</html>";
						} else {
							if(renameTo != null) {
								renameTo = StringUtils.decodeHTML(renameTo.replace("+", " "));
								final String encodedRenameTo = StringUtils.encodeURLStr(renameTo, true);
								if(renameTo.equals(reqFileName)) {
									send200OK = false;
									out.println(versionToUse + " 303 See Other");
									out.println("Vary: Accept-Encoding");
									out.println("Server: " + SERVER_NAME_HEADER);
									out.println("Date: " + StringUtils.getCurrentCacheTime());
									out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
									out.println("Cache-Control: " + cachePrivateMustRevalidate);
									out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
									if(keepAlive) {
										out.println("Keep-Alive: timeout=30");
									}
									out.println("Location: " + fileLink + "?administrateFile=1&renameFile=1");//"Location: " + request.referrerLink);
									out.println("");
									out.flush();
									connectedClients.remove(clientInfo);
									return true;
								}
								boolean success = false;
								boolean alreadyExists = false;
								if(parentFile != null && parentFile.exists()) {
									File renameToFile = new File(parentFile, renameTo);
									alreadyExists = renameToFile.exists();
									success = requestedFile.renameTo(renameToFile);
								}
								if(success) {//responseStr = "Hai ther! You tried to rename to \"" + renameTo + "\" from \"" + reqFileName + "\"!";
									send200OK = false;
									out.println(versionToUse + " 303 See Other");
									out.println("Vary: Accept-Encoding");
									out.println("Server: " + SERVER_NAME_HEADER);
									out.println("Date: " + StringUtils.getCurrentCacheTime());
									out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
									out.println("Cache-Control: " + cachePrivateMustRevalidate);
									out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
									if(keepAlive) {
										out.println("Keep-Alive: timeout=30");
									}
									out.println("Location: " + parentFileLink + "?administrateFile=1#" + renameTo);
									out.println("");
									out.flush();
									connectedClients.remove(clientInfo);
									return true;
								}
								responseStr = "<!DOCTYPE html>\r\n"//
										+ "<html>\r\n\t<head>\r\n"//
										+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
										+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
										+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
										+ "\t\t<title>Folder Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
										+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
										+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
										+ "\t</head>\r\n"//
										+ "\t<body bgcolor=\"#772222\">\r\n"//
										+ "\t\t<h1>Error renaming " + fileOrFolder + " -<br>\r\n"//
										+ "\t\t" + pageHeader + "</h1>\r\n"//
										+ logoutAnchor//
										+ "\t\t<string>Unable to rename " + fileOrFolder + " &quot;" + reqFileName + "&quot; to &quot;" + encodedRenameTo + "&quot;" + (alreadyExists ? ": Destination file/folder already exists. Please choose another name." : ". Is the " + fileOrFolder + " a system " + fileOrFolder + "?") + "</string>\r\n"//
										+ "\t\t<hr>\r\n"//
										+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + "?administrateFile=1\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a>&nbsp;<a href=\"" + fileLink + "?administrateFile=1&renameFile=1\" rel=\"nofollow\" title=\"Try again with a different name\">Choose a different name</a>&nbsp;<a href=\"" + fileLink + "?administrateFile=1&renameFile=1&renameTo=" + encodedRenameTo + "\" rel=\"nofollow\" title=\"Try again with the same name\">Retry</a></b>\r\n"//
										+ "\t</body>\r\n</html>";
							} else {
								responseStr = "<!DOCTYPE html>\r\n"//
										+ "<html>\r\n\t<head>\r\n"//
										+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
										+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
										+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
										+ "\t\t<title>Folder Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
										+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
										+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
										+ "\t</head>\r\n"//
										+ "\t<body bgcolor=\"#EEEE11\">\r\n"//
										+ "\t\t<h1>Folder Administration -<br>\r\n"//
										+ "\t\t" + pageHeader + "</h1>\r\n"//
										+ logoutAnchor//
										+ "\t\t<h2><font color=\"#777700\">Rename " + fileOrFolder + " &quot;" + reqFileName + "&quot;</font></h2><hr>\r\n"//
										+ "\t\t<string>Enter the new name for the " + fileOrFolder + " &quot;" + reqFileName + "&quot; and click rename:</string>\r\n"//
										+ "\t\t<form action=\"" + request.requestedFilePath + "\" method=\"get\" enctype=\"application/x-www-form-urlencoded\">"//
										+ "\t\t\t<input type=\"hidden\" name=\"administrateFile\" value=\"1\">\r\n"//
										+ "\t\t\t<input type=\"hidden\" name=\"renameFile\" value=\"1\">\r\n"//
										+ "\t\t\t<input type=\"text\" name=\"renameTo\" value=\"" + reqFileName + "\" size=\"42\" title=\"The new name for this " + fileOrFolder + "\"><br>\r\n"//
										+ "\t\t\t<input type=\"submit\" value=\"Rename\" title=\"Rename this file\">\r\n"//
										+ "\t\t</form><br>\r\n"//
										+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + "?administrateFile=1" + "\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a></b>\r\n"//
										+ "\t</body>\r\n</html>";
							}
						}
					} else if(moveFile || restrictFile) {
						//TODO Implement me!
						responseStr = "<!DOCTYPE html><html><head><link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\"><title>You hax. - " + domainDirectory.getServerName() + "</title><style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>" + (domainDirectory.doesDefaultStyleSheetExist() ? "<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">" : "") + "</head><body><string>You typed that url didn't you.</string></body></html>";
					} else {
						if(requestedFile.isDirectory()) {
							String fileTable = "";
							String[] c = requestedFile.list();
							final HashMap<Integer, String> files = new HashMap<>();
							if(c != null) {
								int j = 0;
								for(int i = 0; i < c.length; i++) {
									CodeUtil.sleep(10L);
									boolean doIt = true;
									String fileName = c[i];
									File file = new File(requestedFile, fileName);
									if(!file.exists()) {
										doIt = false;
									}/* else {
										RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(file);
										if(res != null) {
											doIt = !res.isHidden.getValue().booleanValue();//Don't update to RestrictedFile.isHidden() because this is the file administration view, not the normal view
										}
										}*/
									if(doIt) {
										Integer key = Integer.valueOf(j);
										files.put(key, fileName);
										j++;
									}
								}
							}
							fileTable += "\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n";
							fileTable += "\t\t\t<tbody>\r\n";
							fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td><b>#</b></td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><b>File Name</b></td><td>" + nonBreakingSpaces + "</td><td><b>Size</b></td><td>" + nonBreakingSpaces + "</td><td><b>Date</b></td><td>" + nonBreakingSpaces + "</td><td><b>Type</b></td><td>" + nonBreakingSpaces + "</td><td><b>(Management)</b></td></tr>\r\n";
							
							ArrayList<Integer> folderPaths = new ArrayList<>();
							ArrayList<Integer> filePaths = new ArrayList<>();
							if(domainDirectory.getListDirectoriesFirst()) {
								for(Entry<Integer, String> entry : files.entrySet()) {
									CodeUtil.sleep(10L);
									Integer i = entry.getKey();
									String filePath = entry.getValue();
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
											} else {
												filePaths.add(i);
											}
										}
									} catch(Throwable e) {
										e.printStackTrace();
									}
								}
							}
							if(parentFileLink != null) {
								fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>(-)</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + parentFileLink + "?administrateFile=1\"><b>../(Up)</b></a></td><td>" + nonBreakingSpaces + "</td><td>" + info.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + info.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + info.mimeType + (RestrictedFile.isFileForbidden(parentFile) ? "\t\t(Forbidden)" : "") + "</td><td>" + nonBreakingSpaces + "</td><td>(-)</td></tr>\r\n";
							}
							for(Entry<Integer, String> entry : files.entrySet()) {
								CodeUtil.sleep(10L);
								final Integer i;
								final String fileName;
								if(domainDirectory.getListDirectoriesFirst()) {
									if(!folderPaths.isEmpty()) {
										i = folderPaths.remove(0);
										fileName = files.get(i);
									} else if(!filePaths.isEmpty()) {
										i = filePaths.remove(0);
										fileName = files.get(i);
									} else {
										//i = null;
										//filePath = null;
										continue;
									}
								} else {
									i = entry.getKey();
									fileName = entry.getValue();
								}
								File file = new File(requestedFile, fileName);
								if(FileUtil.isFileAccessible(file)) {
									final boolean isDefaultFileForDomain = FilenameUtils.normalize(file.getAbsolutePath()).equalsIgnoreCase(defaultFilePath);
									RestrictedFile curRes = RestrictedFile.getSpecificRestrictedFile(file);
									final boolean isHidden = curRes == null ? false : curRes.isHidden.getValue().booleanValue();
									String newPath = path + "/" + fileName;
									if(newPath.startsWith("//")) {
										newPath = newPath.substring(1);
									}
									String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
									final String curFileLink = StringUtils.encodeHTML(httpProtocol + request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
									
									String managementStr = getManagementStrForFile(curFileLink, fileName);
									FileInfo curInfo = new FileInfo(file, domainDirectory);
									String mimeType = curInfo.mimeType + (RestrictedFile.isFileForbidden(file) ? "\t\t(Forbidden)" : "");
									String name = "<string" + (isDefaultFileForDomain ? " title=\"Default landing page for domain &quot;" + domainDirectory.getDomain() + "&quot;\"" : "") + ">" + fileName + (isHidden ? "&nbsp;<b><i>{Hidden " + (file.isFile() ? "File" : "Folder") + "}</i></b>" : "") + "</string>";
									name = isDefaultFileForDomain ? "<b>" + name + "</b>" : name;
									if(file.isDirectory()) {
										name = "<a href=\"" + curFileLink + "?administrateFile=1\" rel=\"nofollow\" name=\"" + fileName + "\">" + name + "</a>";
									} else {
										name = "<i><a href=\"" + curFileLink + "?administrateFile=1\" rel=\"nofollow\" name=\"" + fileName + "\">" + name + "</a></i>";
									}
									fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + (i.intValue() + 1) + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td>" + name + "</td><td>" + (file.isDirectory() ? nonBreakingSpaces : "<a href=\"" + curFileLink + "\" download=\"" + fileName + "\" target=\"_blank\" rel=\"nofollow\">Download</a>") + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + mimeType + "</td><td>" + nonBreakingSpaces + "</td><td>" + managementStr + "</td></tr>\r\n";
								}
							}
							fileTable += "\t\t\t</tbody>\r\n";
							fileTable += "\t\t</table>\r\n";
							
							responseStr = "<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>Folder Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ "\t</head>\r\n"//
									+ "\t<body bgcolor=\"#DDDDDD\">\r\n"//
									+ "\t\t<h1>Folder Administration -<br>\r\n"//
									+ "\t\t" + pageHeader + "</h1>\r\n"//
									+ logoutAnchor//
									+ "\t\t<h2>Upload files to \"" + reqFileName + "\":</h2><hr>\r\n" + uploadForm + "<hr>\r\n"//
									+ "\t\t<h2>Directory Tree:</h2>\r\n"//
									+ fileTable + "<hr>\r\n"//
									+ "\t\t<b><a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a></b>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "\r\n"//
									+ "\t</body>\r\n</html>";
						} else if(requestedFile.isFile()) {
							responseStr = "<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>File Administration - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ "\t</head>\r\n"//
									+ "\t<body bgcolor=\"#DDDDDD\">\r\n"//
									+ "\t\t<h1>File Administration -<br>\r\n"//
									+ "\t\t" + pageHeader + "</h1>\r\n"//
									+ logoutAnchor//
									+ "\t\t<string>File administration pages are not yet implemented. Try again in a later release! :)</string>\r\n"//
									+ "\t\t<hr><b>" + (parentFileLink != null ? "<a href=\"" + parentFileLink + "?administrateFile=1\" rel=\"nofollow\">(../Up)</a>&nbsp;" : "") + "<a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "</b><br>"//
									+ (!request.referrerLink.isEmpty() ? "\t\t<a href=\"" + request.referrerLink + "\" rel=\"nofollow\">Back to previous page</a>\r\n" : "")//
									+ "\t</body>\r\n</html>";
						} else {
							responseStr = "<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>File Administration Error - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ "\t</head>\r\n"//
									+ "\t<body bgcolor=\"#DDFF00\">\r\n"//
									+ "\t\t<h1>Oops! -<br>\r\n"//
									+ "\t\t" + pageHeader + "</h1>\r\n"//
									+ logoutAnchor//
									+ "\t\t<string>The server can't figure out if the object referenced in the request url is a file or folder! That shouldn't happen!!1</string>\r\n"//
									+ "\t\t<hr><b>" + (parentFileLink != null ? "<a href=\"" + parentFileLink + "?administrateFile=1\" rel=\"nofollow\">(../Up)</a>&nbsp;" : "") + "<a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "</b><br>"//
									+ (!request.referrerLink.isEmpty() ? "\t\t<a href=\"" + request.referrerLink + "\" rel=\"nofollow\">Back to previous page</a>\r\n" : "")//
									+ "\t</body>\r\n</html>";
						}
					}
				}
			}
			if(send200OK) {
				out.println(versionToUse + " 200 OK");
				out.println("Vary: Accept-Encoding");
				out.println("Server: " + SERVER_NAME_HEADER);
				out.println("Date: " + StringUtils.getCurrentCacheTime());
				out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
				out.println("Cache-Control: " + cachePrivateMustRevalidate);
				out.println("Connection: " + (keepAlive ? "keep-alive" : "close"));
				if(authCookie != null) {
					out.println("Set-Cookie: " + authCookie);
				}
				if(keepAlive) {
					out.println("Keep-Alive: timeout=30");
				}
				out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			}
			
			if(request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
				out.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(responseStr, "UTF-8");
				out.println("Content-Length: " + r.length);
				out.println("");
				out.flush();
				if(request.method.equalsIgnoreCase("GET")) {
					outStream.write(r);
					outStream.flush();
					println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(request.method.equalsIgnoreCase("HEAD")) {
					println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				r = null;
			} else {
				out.println("Content-Length: " + responseStr.length());
				out.println("");
				if(request.method.equalsIgnoreCase("GET")) {
					out.println(responseStr);
					println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(request.method.equalsIgnoreCase("HEAD")) {
					println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				out.flush();
			}
			connectedClients.remove(clientInfo);
			return true;
		} catch(IOException ignored) {
			//return false;
		}
		//out.close();
		//s.close();
		connectedClients.remove(clientInfo);
		return false;
	}
	
	/** Generates file management links with the given file link and name(i.e.
	 * Rename, Delete, Move, Restrict, etc.).
	 * 
	 * @param fileLink The absolute link to the file in question
	 * @param fileName The name of the file in question
	 * @return The generated string
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	private static final String getManagementStrForFile(String fileLink, String fileName) {//XXX Folder/File Administration Management Links
		return "<a href=\"" + fileLink + "?administrateFile=1&renameFile=1\" rel=\"nofollow\" title=\"Rename this file\">Rename</a>"//
				+ "&nbsp;<a href=\"" + fileLink + "?administrateFile=1&deleteFile=1\" rel=\"nofollow\" title=\"Delete this file\" onclick=\"return confirm('Are you sure you want to delete the file &quot;" + StringUtils.makeFilePathURLSafe(fileName) + "&quot;? This cannot be undone.');\">Delete</a>"//
				+ "&nbsp;<b><u><i><string title=\"This feature is not implemented yet.\">Move</string></u>"//
				+ "&nbsp;<u><string title=\"This feature is not implemented yet.\">Restrict</string></i></u></b>";//
	}
	
	/** Parses the client's incoming administration request, then returns the
	 * result.
	 * 
	 * @param s The client
	 * @param in The client's input stream
	 * @param reuse Whether or not this is a reused connection
	 * @return The result of parsing the client's request
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ReusedConn)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	protected static final RequestResult HandleAdminRequest(final Socket s, final InputStream in, ReusedConn reuse) {
		if(s == null || s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
			//serverAdministrationAuths.remove(Thread.currentThread());
			return new RequestResult(null, ReusedConn.FALSE, null);
		}
		/*if(!reuse) {
			serverAdministrationAuths.put(Thread.currentThread(), new HttpDigestAuthorization(adminAuthorizationRealm, adminUsername, adminPassword));
		}*/
		final File adminFolder = new File(rootDir, "admin");
		if(!adminFolder.exists()) {
			adminFolder.mkdirs();
		}
		final String clientAddress = AddressUtil.getClientAddress((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
		//final String clientIP = AddressUtil.getClientAddressNoPort(clientAddress);//clientAddress == null ? null : clientAddress.substring(0, clientAddress.contains(":") ? clientAddress.lastIndexOf(":") - 1 : clientAddress.length());
		if(isIpBanned(clientAddress, !reuse.reuse)) {
			try {
				s.close();
			} catch(IOException ignored) {
			}
			NaughtyClientData naughty = getBannedClient(clientAddress);
			println("Kicked client with ip: " + clientAddress + "\r\n\tReason: " + (naughty != null ? "\"" + naughty.banReason + "\"" : "Unknown"));
			return new RequestResult(null, ReusedConn.FALSE, null);
		}
		HTTPClientRequest request = null;
		try {
			println((reuse.reuse ? "Reused" : "New") + " Connection: " + clientAddress);
			if(in.available() < 1) {
				println("\tWaiting on client to send " + (reuse.reuse ? "next " : "") + "request...");
			}
			if(s.isClosed() || s.isInputShutdown()) {
				println("\t\tIncoming connection from \"" + clientAddress + "\" was interrupted before the client could send any data.");
				return new RequestResult(null, ReusedConn.FALSE, null);
			}
			request = new HTTPClientRequest(s, in);
			try {
				request.acceptClientRequest(requestTimeout, reuse.reuse);//request = HTTPClientRequest.getClientRequest(s, in, requestTimeout, false);//new HTTPClientRequest(s, in);
			} catch(NumberFormatException e) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("No Content-Length Header defined.").setResponse((String) null).sendToClient(s, false);
				try {
					in.close();
				} catch(Throwable ignored) {
				}
				request.cancel();
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			} catch(UnsupportedEncodingException e) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Unsupported Url Encoding").setResponse((String) null).sendToClient(s, false);
				try {
					in.close();
				} catch(Throwable ignored) {
				}
				request.cancel();
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			} catch(OutOfMemoryError e) {
				new HTTPServerResponse("HTTP/1.1", HTTP_413, false, StandardCharsets.UTF_8).setStatusMessage("Request data too large").setResponse((String) null).sendToClient(s, false);
				try {
					in.close();
				} catch(Throwable ignored) {
				}
				request.cancel();
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			} catch(CancellationException e) {
				new HTTPServerResponse("HTTP/1.1", HTTP_204, false, StandardCharsets.UTF_8).setStatusMessage(e.getMessage()).setResponse((String) null).sendToClient(s, false);
				try {
					in.close();
				} catch(Throwable ignored) {
				}
				request.cancel();
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			} catch(TimeoutException e) {
				ResponseUtil.send408Response(s, clientAddress);
				try {
					in.close();
				} catch(Throwable ignored) {
				}
				request.cancel();
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			}
			if(!request.xForwardedFor.isEmpty()) {
				println("\tIdentified client behind proxy: " + request.xForwardedFor);
			}
			final String originalHost = request.host.isEmpty() ? request.http2Host : request.host;
			boolean hostDefined = !originalHost.isEmpty();
			request.host = AddressUtil.getValidHostFor(originalHost);
			if(!hostDefined && request.version.equalsIgnoreCase("HTTP/1.1")) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("No host header defined: \"" + originalHost + "\"").setResponse((String) null).sendToClient(s, false);
				request.cancel();
				return new RequestResult(request, ReusedConn.FALSE, null);
			}
			if(request.method.isEmpty()) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Bad protocol request: \"" + request.protocolRequest + "\"").setResponse((String) null).sendToClient(s, false);
				if(!HTTPClientRequest.debug) {
					PrintUtil.clearLogs();
				}
				request.cancel();
				return new RequestResult(request, ReusedConn.FALSE, null);
			}
			println("\t--- Client request: " + request.protocolRequest);
			if(!request.requestArguments.isEmpty()) {
				printlnDebug("\t\tRequest Arguments: \"" + request.requestArgumentsStr + "\"");
			}
			reuse.reuse = request.connectionSetting.toLowerCase().contains("keep-alive");
			if(request.connectionSetting.toLowerCase().contains("close")) {
				reuse.reuse = false;
			}
			s.setKeepAlive(reuse.reuse);
			HTTPServerResponse response = new HTTPServerResponse(request.version, HTTP_501, request.acceptEncoding.toLowerCase().contains("gzip"), StandardCharsets.UTF_8).setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close");
			
			if(request.method.equalsIgnoreCase("brew")) {
				if(request.version.toUpperCase().startsWith("HTCPCP/")) {
					response.setStatusCode(HTTP_418).setStatusMessage("Making coffee is okay.").setUseGZip(request.acceptEncoding.toLowerCase().contains("gzip")).setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your coffee is ready!\r\nDon't drink too much in a day.\r\n").sendToClient(s, true);
				} else {
					response.setStatusCode(HTTP_418).setStatusMessage("Making tea is fun!").setUseGZip(request.acceptEncoding.toLowerCase().contains("gzip")).setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your tea is ready! Enjoy.\r\n").sendToClient(s, true);
				}
				s.close();
				return new RequestResult(request, ReusedConn.FALSE, null);
			}
			
			String path = request.requestedFilePath;
			path = (path.trim().isEmpty() ? "/" : path);
			path = (path.startsWith("/") ? "" : "/") + path;
			final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
			
			if(!request.protocolRequest.isEmpty()) {
				if(request.method.isEmpty()) {
					new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Bad protocolRequest: \"" + request.protocolRequest + "\"").setResponse((String) null).sendToClient(s, false);
					return new RequestResult(request, ReusedConn.FALSE, null);
				}
				//final HttpDigestAuthorization serverAdministrationAuth = getOrCreateAuthForCurrentThread();
				final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(request, useCookieAuthentication);//final AuthorizationResult authResult = serverAdministrationAuth.authenticate(request.authorization, new String(request.postRequestData), request.protocol, request.host, clientIP, request.cookies);
				if(!authResult.passed()) {
					response.setStatusCode(HTTP_401);//.setStatusMessage(authResult.message);
					response.setHeader("Content-Type", "text/html; charset=UTF-8");
					response.setHeader("Server", SERVER_NAME_HEADER);
					response.setHeader("Cache-Control", cachePrivateMustRevalidate);
					response.setHeader("Date", StringUtils.getCurrentCacheTime());
					response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
					if(authResult.authorizedCookie != null) {
						response.setHeader("Set-Cookie", authResult.authorizedCookie);
					}
					response.setResponse("<!DOCTYPE html>\r\n"//
							+ "<html>\r\n\t<head>\r\n"//
							+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
							+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
							+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
							+ "\t\t<title>401 - Authorization Required - " + SERVER_NAME + "</title>\r\n"//
							+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
							+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
							+ "\t</head>\r\n"//
							+ "\t<body>\r\n"//
							+ "\t\t<h1>Error 401 - Authorization Required:</h1><hr>\r\n"//
							//+ "\t\t<string>" + authResult.message + "</string><br>\r\n"//
							+ "\t\t<string>" + pageHeader + "</string>\r\n"//
							+ "\t</body>\r\n</html>");
					response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					return new RequestResult(request, reuse, null);
				}
				if(authResult.authorizedCookie != null) {
					response.setHeader("Set-Cookie", authResult.authorizedCookie);
				}
				if(request.method.equalsIgnoreCase("GET") || request.method.equalsIgnoreCase("HEAD")) {
					if(request.requestedFilePath.equals("/")) {
						com.gmail.br45entei.server.HTTPStatusCodes statusCodeToSend = null;
						final String restrictedFileNote = "<br><string><b>Note:&nbsp;</b>A restricted file that does not have at least one username and password will result in \"" + HTTP_403 + "\" being sent to any client that tries to view it.<br>Restricted files that do have both a username and a password will result in the client being prompted to log in.<br><br>Alternately, if a restricted file has any &quot;AllowedIp&quot;s set, any client with one of those ip addresses will be able to connect as if the restriction did not exist.<br>If one of the allowed ips are set to the string &quot;any&quot;, then any client will be able to connect regardless(useful if you have a file/folder located within another restricted file).</string>";
						final String currentPageLink = "http://" + request.host + request.requestedFilePath + request.requestArgumentsStr;
						final String mainPageLink = "http://" + request.host + "/";
						final String mainPageNoAdminPortLink = mainPageLink.replace(":" + admin_listen_port, "");
						final String mainPageHrefLink = "<a href=\"" + mainPageLink + "\">Main Page</a>";
						final String backToPrevPageLink = (request.referrerLink.isEmpty() ? mainPageLink : (request.referrerLink.equals(currentPageLink) ? mainPageLink : request.referrerLink));
						final String backToPreviousPageHrefLink = "<a href=\"" + backToPrevPageLink + "\">Back to " + (backToPrevPageLink.equals(mainPageLink) ? "Main" : "Previous") + " Page</a>";
						
						final String pageHrefLinks = mainPageHrefLink + (mainPageLink.equals(backToPrevPageLink) ? "" : "&nbsp;" + backToPreviousPageHrefLink);
						
						final String shutdown = request.requestArguments.get(shutdownStr);
						final String page = request.requestArguments.get("page");
						final String edit = request.requestArguments.get("edit");
						
						final String domainUUID = request.requestArguments.get("domain");
						final String restrictedFileUUID = request.requestArguments.get("restrictedFile");
						final String forumUUID = request.requestArguments.get("forum");
						final String boardUUID = request.requestArguments.get("board");
						final String topicUUID = request.requestArguments.get("topic");
						final String commentUUID = request.requestArguments.get("comment");
						
						final String deleteDomainUUID = request.requestArguments.get("deleteDomain");
						final String deleteRestrictedFileUUID = request.requestArguments.get("deleteResFile");
						final String deleteForumUUID = request.requestArguments.get("deleteForum");
						final String deleteBoardUUID = request.requestArguments.get("deleteBoard");
						final String deleteTopicUUID = request.requestArguments.get("deleteTopic");
						final String deleteCommentUUID = request.requestArguments.get("deleteComment");
						
						final String domainError = request.requestArguments.get("domainError");
						final String createDomainError = request.requestArguments.get("createDomainError");
						String getHomeDirError = request.requestArguments.get("homeDirectoryError");
						final String homeDirectoryError = getHomeDirError != null ? StringUtils.decodeHTML(getHomeDirError) : null;
						final String domainAlreadyExistsError = request.requestArguments.get("domainAlreadyExistsError");
						
						final String restrictedFileError = request.requestArguments.get("restrictedFileError");
						final String createResFileError = request.requestArguments.get("createResFileError");
						final String resFileAlreadyExistsError = request.requestArguments.get("resFileAlreadyExistsError");
						
						final String forumError = request.requestArguments.get("forumError");
						final String boardError = request.requestArguments.get("boardError");
						final String topicError = request.requestArguments.get("topicError");
						final String commentError = request.requestArguments.get("commentError");
						
						final String pageLinkHeader = "<div align=\"center\"><table border=\"2\" cellpadding=\"3\" cellspacing=\"1\"><tbody><tr><td><b><a href=\"" + mainPageLink + "\">Server Settings</a></b></td><td><b><a href=\"" + mainPageLink + "?page=domains\">Domain Settings</a></b></td><td><b><a href=\"" + mainPageLink + "?page=restrictedFiles\">Restricted File Settings</a></b></td><td><b><a href=\"" + mainPageLink + "?page=forums\">Forum Settings</a></b></td></tr></tbody></table></div><hr>";
						
						final String unableToCreateDomain = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>Unable to create the new domain. Please try again, and make sure the &quot;domain&quot; field is not blank.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String domainAlreadyExists = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>There is already <a href=\"" + mainPageLink + "?domain=" + domainAlreadyExistsError + "\">a domain with that name</a>.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String domainDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested domain does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String unableToCreateResFile = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>Unable to add the new restriction.<br>Please try again, and make sure the &quot;FilePath&quot; field denotes a valid file or folder path.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String resFileAlreadyExists = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>That file <a href=\"" + mainPageLink + "?restrictedFile=" + resFileAlreadyExistsError + "\">is already restricted</a>.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String restrictedFileDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested restricted file does not exist or is not restricted.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String forumDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested forum does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String boardDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested forum board does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String topicDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested forum topic does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String commentDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested forum comment does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						final String pageDoesNotExist = "<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body bgcolor=\"#FFE0D1\">\r\n"//
								+ "\t\t<h1>The requested page does not exist.</h1><hr>\r\n"//
								+ "\t\t" + pageLinkHeader + "\r\n"//
								+ "\t\t" + pageHrefLinks + "<br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>";
						if(createDomainError != null) {
							response.setResponse(unableToCreateDomain);
						} else if(domainError != null) {
							response.setResponse(domainDoesNotExist);
						} else if(domainAlreadyExistsError != null) {
							response.setResponse(domainAlreadyExists);
						} else if(restrictedFileError != null) {
							response.setResponse(restrictedFileDoesNotExist);
						} else if(createResFileError != null) {
							response.setResponse(unableToCreateResFile);
						} else if(resFileAlreadyExistsError != null) {
							response.setResponse(resFileAlreadyExists);
						} else if(forumError != null) {
							response.setResponse(forumDoesNotExist);
						} else if(boardError != null) {
							response.setResponse(boardDoesNotExist);
						} else if(topicError != null) {
							response.setResponse(topicDoesNotExist);
						} else if(commentError != null) {
							response.setResponse(commentDoesNotExist);
						} else {
							if(shutdown != null && (shutdown.equals("1") || shutdown.equalsIgnoreCase("true")) && request.method.equalsIgnoreCase("GET")) {
								response.setStatusCode(HTTP_200);
								response.setStatusMessage("Admin Page(Server Shutdown)");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								response.setHeader("Date", StringUtils.getCurrentCacheTime());
								response.setHeader("Last-Modified", StringUtils.getCurrentCacheTime());
								response.setHeader("Accept-Ranges", "none");
								response.setResponse("<!DOCTYPE html>\r\n");
								response.appendResponse("<html>\r\n\t<head>\r\n");
								response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
								response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
								response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
								response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
								response.appendResponse("\t</head>\r\n");
								response.appendResponse("\t<body bgcolor=\"#99E699\">\r\n");
								response.appendResponse("\t\t<h1>The server has been told to shut down successfully.</h1><hr>\r\n");
								response.appendResponse("\t</body>\r\n</html>");
								response.sendToClient(s, true);
								try {
									s.close();
								} catch(Throwable ignored) {
								}
								JavaWebServer.getInstance().shutdown();
								return new RequestResult(request, ReusedConn.FALSE, null);
							} else if(deleteDomainUUID != null) {
								DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(deleteDomainUUID);
								if(domain != null) {
									String domainName = domain.getDomain();
									domain.delete();
									response.setStatusMessage("Admin Page(Domain \"" + domainName + "\" Deleted)").setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=domains").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								response.setResponse(domainDoesNotExist);
							} else if(deleteRestrictedFileUUID != null) {
								RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(deleteRestrictedFileUUID);
								if(res != null) {
									String resFilePath = res.getRestrictedFile().getAbsolutePath();
									res.delete();
									response.setStatusMessage("Admin Page(Restricted File \"" + resFilePath + "\" Unrestricted)").setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=restrictedFiles").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								response.setResponse(restrictedFileDoesNotExist);
							} else if(page != null) {
								if(page.equals("restrictedFiles")) {//XXX Page = "restrictedFiles"
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									response.appendResponse("\t\t<h1>Server Administration - Restricted Files</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									//
									response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t<tbody>\r\n");
									for(RestrictedFile res : RestrictedFile.getInstances()) {
										if(res.getUUID() != null) {
											final String uuid = res.getUUID().toString();
											String filePath = res.getRestrictedFile().getAbsolutePath();
											response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?restrictedFile=" + uuid + "\">" + filePath + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteResFile=" + uuid + "\" onClick=\"return confirm('Are you sure you want to remove the restriction from the file &quot;" + filePath + "&quot;? This cannot be undone.');\">Remove Restriction</a></td></tr>\r\n");
										}
									}
									response.appendResponse("\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t</table><hr>\r\n");
									response.appendResponse("\t\t<a href=\"" + mainPageLink + "?page=createRestrictedFile\" title=\"Allows you to restrict a file server-wide\">Restrict a file</a>\r\n");
									//
									response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("domains")) {//XXX Page = "domains"
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									response.appendResponse("\t\t<h1>Server Administration - Domain Settings</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t<string>Select a domain to manage, or click \"Delete\" to delete it.</string>\r\n");
									//
									response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t<tbody>\r\n");
									for(DomainDirectory domain : DomainDirectory.getInstances()) {
										if(domain.uuid.getValue() != null) {
											final String uuid = domain.uuid.getValue().toString();
											response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?domain=" + uuid + "\">" + domain.getDisplayName() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteDomain=" + uuid + "\" onClick=\"return confirm('Are you sure you want to delete the domain &quot;" + domain.getDomain() + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
										}
									}
									
									response.appendResponse("\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t</table><hr>\r\n");
									response.appendResponse("\t\t<a href=\"" + mainPageLink + "?page=createDomain\" title=\"Allows you to create a domain which may then be used to connect to this server\">Create a new domain</a>\r\n");
									//
									response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("createRestrictedFile")) {//XXX Create Restricted File
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									response.appendResponse("\t\t<h1>Restrict a File:</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									response.appendResponse("\t\t<string>Properties for new restricted file:</string>\r\n");
									//
									response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?createResFile=" + UUID.randomUUID() + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									RestrictedFile res = RestrictedFile.createNewRestrictedFile(UUID.randomUUID());
									res.remove();
									res.setRestrictedFile(new File(homeDirectory, "some/file/or/folder.path"), true);
									final String booleanOptionOn = "\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									
									response.appendResponse("\t\t\t\t\t<tr><td><b>FilePath:</b></td><td><input type=\"text\" name=\"FilePath\" value=\"" + res.getRestrictedFile().getAbsolutePath() + "\" size=\"50\"></td><td></td><td></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>AuthRealm:</b></td><td><input type=\"text\" name=\"AuthRealm\" value=\"" + res.getAuthorizationRealm() + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>IsHidden:</b></td><td><select name=\"" + res.isHidden.getName() + "\" title=\"" + res.isHidden.getDescription() + "\">" + (res.isHidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									int iPsMax = res.getAllowedIPAddresses().size() > 20 ? res.getAllowedIPAddresses().size() : 20;
									for(int i = 0; i < iPsMax; i++) {
										String value = (i < res.getAllowedIPAddresses().size() ? res.getAllowedIPAddresses().get(i) : "");
										response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedIp_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedIp_" + i + "\" value=\"" + value + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									}
									int authMax = 20;
									for(int i = 0; i < authMax;) {
										response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + i + "\" value=\"" + (i == 0 ? DEFAULT_AUTHORIZATION_USERNAME : "") + "\" size=\"25\"></td><td><b>AuthPassword_" + (i + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + i + "\" value=\"" + (i == 0 ? DEFAULT_AUTHORIZATION_PASSWORD : "") + "\" size=\"25\"></td></tr>\r\n");
										i++;
										if(i == res.getAuthorizationCredentials().size() && i < authMax) {
											for(int j = i; j < authMax; j++) {
												response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (j + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + j + "\" value=\"\" size=\"25\"></td><td><b>AuthPassword_" + (j + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + j + "\" value=\"\" size=\"25\"></td></tr>\r\n");
											}
										}
									}
									//
									response.appendResponse("\t\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t\t</table><hr>\r\n");
									response.appendResponse("\t\t\t<input type=\"submit\" value=\"Restrict\" title=\"Restrict this file\">\r\n");
									response.appendResponse("\t\t</form>\r\n");
									response.appendResponse("\t\t" + restrictedFileNote + "\r\n");
									//
									response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("createDomain")) {//XXX Create Domain
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									response.appendResponse("\t\t<h1>Create a Domain:</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									response.appendResponse("\t\t<string>Properties for new domain:</string>\r\n");
									//
									response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?createDomain=" + UUID.randomUUID() + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									final boolean noHomeDir = homeDirectoryError != null;
									final String homeDirPrefix = noHomeDir ? "<p class=\"red_border\">" : "";
									final String homeDirSuffix = noHomeDir ? "</p>" : "";
									
									DomainDirectory domain = DomainDirectory.getTemporaryDomain();
									String getDomainName = request.requestArguments.get("domainName");
									final String domainName = getDomainName == null ? AddressUtil.getALocalIP()/*InetAddress.getLocalHost().getHostAddress()*/: getDomainName;
									final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCurrentCacheTime() + "</td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCurrentCacheTime() + "</td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>Domain/Ip:</b></td><td><input type=\"text\" name=\"" + domain.domain.getName() + "\" value=\"" + domainName + "\" size=\"42\" title=\"This is the address which will be used to connect to this domain\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>NetworkMTU:</b></td><td><input type=\"number\" step=\"512\" min=\"512\" max=\"819200\" name=\"" + domain.mtu.getName() + "\" value=\"" + domain.mtu.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>CacheMaxAge:</b></td><td><input type=\"number\" min=\"0\" name=\"" + domain.cacheMaxAge.getName() + "\" value=\"" + domain.cacheMaxAge.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>AreDirectoriesForbidden:</b></td><td><select name=\"" + domain.areDirectoriesForbidden.getName() + "\">" + (domain.areDirectoriesForbidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"" + domain.calculateDirectorySizes.getName() + "\">" + (domain.calculateDirectorySizes.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFileName:</b></td><td><input type=\"text\" name=\"" + domain.defaultFileName.getName() + "\" value=\"" + domain.defaultFileName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"" + domain.defaultFontFace.getName() + "\" value=\"" + domain.defaultFontFace.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultPageIcon:</b></td><td><input type=\"text\" name=\"" + domain.defaultPageIcon.getName() + "\" value=\"" + domain.defaultPageIcon.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultStylesheet:</b></td><td><input type=\"text\" name=\"" + domain.defaultStylesheet.getName() + "\" value=\"" + domain.defaultStylesheet.getValue() + "\" size=\"42\"></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayName:</b></td><td><input type=\"text\" name=\"" + domain.displayName.getName() + "\" value=\"" + domainName + "\" size=\"42\" title=\"The display name for this domain(used when viewing domains in the administration interface)\"></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayLogEntries:</b></td><td><select name=\"" + domain.displayLogEntries.getName() + "\">" + (domain.displayLogEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td>" + homeDirPrefix + "<input type=\"text\" name=\"" + domain.folder.getName() + "\" value=\"" + (noHomeDir ? homeDirectoryError : domain.folder.getValue().getAbsolutePath()) + "\" size=\"42\">" + homeDirSuffix + "</td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableGZipCompression:</b></td><td><select name=\"" + domain.enableGZipCompression.getName() + "\">" + (domain.enableGZipCompression.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFileUploads:</b></td><td><select name=\"" + domain.enableFileUpload.getName() + "\" title=\"Whether or not authenticated users may upload files to the directory they are currently browsing\">" + (domain.enableFileUpload.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableAlternateDirectoryListingViews:</b></td><td><select name=\"" + domain.enableAlternateDirectoryListingViews.getName() + "\">" + (domain.enableAlternateDirectoryListingViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFilterView:</b></td><td><select name=\"" + domain.enableFilterView.getName() + "\">" + (domain.enableFilterView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableSortViews:</b></td><td><select name=\"" + domain.enableSortView.getName() + "\">" + (domain.enableSortView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableMediaView:</b></td><td><select name=\"" + domain.enableMediaView.getName() + "\">" + (domain.enableMediaView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableReadableFileViews:</b></td><td><select name=\"" + domain.enableReadableFileViews.getName() + "\">" + (domain.enableReadableFileViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableVLCPlaylistView:</b></td><td><select name=\"" + domain.enableVLCPlaylistView.getName() + "\">" + (domain.enableVLCPlaylistView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableXmlListView:</b></td><td><select name=\"" + domain.enableXmlListView.getName() + "\">" + (domain.enableXmlListView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>IgnoreThumbsdbFiles:</b></td><td><select name=\"" + domain.ignoreThumbsdbFiles.getName() + "\">" + (domain.ignoreThumbsdbFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>ListDirectoriesFirst:</b></td><td><select name=\"" + domain.listDirectoriesFirst.getName() + "\">" + (domain.listDirectoriesFirst.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>NumberDirectoryEntries:</b></td><td><select name=\"" + domain.numberDirectoryEntries.getName() + "\">" + (domain.numberDirectoryEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>ServerName:</b></td><td><input type=\"text\" name=\"" + domain.serverName.getName() + "\" value=\"" + domain.serverName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									//
									response.appendResponse("\t\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t\t</table><hr>\r\n");
									response.appendResponse("\t\t\t<input type=\"submit\" value=\"Create\" title=\"Create a new Domain\">\r\n");
									response.appendResponse("\t\t</form>\r\n");
									//
									
									response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("forums")) {//XXX Page = "forums"
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
									response.appendResponse("\t\t<h1>Server Administration - Forum Settings</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t<string>Select a forum to manage, or click \"Delete\" to delete it.<br><b>Note: </b>Forums are not yet implemented. Please check back in a newer version!</string>\r\n");
									//
									response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t<tbody>\r\n");
									for(ForumData forum : ForumData.getInstances()) {
										if(forum.getUUID() != null) {
											final String forumName = forum.getName();
											final String uuid = forum.getUUID().toString();
											response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + uuid + "\">" + forumName + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteForum=" + uuid + "\" onClick=\"return confirm('Are you sure you want to delete the forum &quot;" + forumName + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
										}
									}
									response.appendResponse("\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t</table><hr>\r\n");
									//
									response.appendResponse("\t</body>\r\n</html>");
								} else {
									response.setResponse(pageDoesNotExist);
								}
							} else if(forumUUID != null) {
								ForumData forum = ForumData.getForumDataFromUUID(forumUUID);
								if(forum != null) {
									final String forumName = forum.getName();
									if(boardUUID != null) {
										ForumBoard board = forum.getForumBoardFromUUID(boardUUID);
										if(board != null) {
											final String boardName = board.getName();
											if(topicUUID != null) {
												ForumTopic topic = board.getForumTopicFromUUID(topicUUID);
												if(topic != null) {
													if(commentUUID != null) {
														ForumComment comment = topic.getForumCommentFromUUID(commentUUID);
														if(comment != null) {
															final String msg = comment.getMessage();
															long numOfLines = StringUtils.getNumOfLinesInStr(msg) + 1;
															final long msgLineAmt = (msg == null || msg.isEmpty() ? 4 : numOfLines >= 4 ? numOfLines : 4);
															response.setResponse("<!DOCTYPE html>\r\n");
															response.appendResponse("<html>\r\n\t<head>\r\n");
															response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
															response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
															response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
															response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
															response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
															response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
															response.appendResponse("\t</head>\r\n");
															response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
															response.appendResponse("\t\t<h1>Comment: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "/#comment-" + comment.getNumber() + "\" title=\"" + topic.getTitle() + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "/#comment-" + comment.getNumber() + "</a></h1><hr>\r\n");
															response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
															response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
															response.appendResponse("\t\t<string>Edit comment #" + comment.getNumber() + ":</string>\r\n");
															//
															response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&comment=" + commentUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
															response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
															response.appendResponse("\t\t\t\t<tbody>\r\n");
															//
															response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(comment.getDateCreated()) + "</td></tr>\r\n");
															response.appendResponse("\t\t\t\t\t<tr><td><b>Date Posted:</b></td><td>" + StringUtils.getCacheTime(comment.getDatePosted()) + "</td></tr>\r\n");
															response.appendResponse("\t\t\t\t\t<tr><td><b>Author:</b></td><td>" + comment.getAuthor() + "</td></tr>\r\n");
															response.appendResponse("\t\t\t\t\t<tr><td><b>Message:</b></td><td><textarea rows=\"" + msgLineAmt + "\" cols=\"50\" name=\"message\" size=\"42\">" + msg + "</textarea></td></tr>\r\n");
															response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCacheTime(comment.getLastEditTime()) + "</td></tr>\r\n");
															
															response.appendResponse("\t\t\t\t</tbody>\r\n");
															response.appendResponse("\t\t\t</table><hr>\r\n");
															//
															response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
															response.appendResponse("\t\t</form>\r\n");
															response.appendResponse("\t</body>\r\n</html>");
														} else {
															response.setResponse(commentDoesNotExist);
														}
													} else {
														if(deleteCommentUUID != null) {
															ForumComment comment = topic.getForumCommentFromUUID(deleteCommentUUID);
															if(comment != null) {
																response.setStatusMessage("Admin Page(Forum Comment #" + comment.getNumber() + " Deleted)");
																topic.removeComment(comment);
																topic.saveToFile();
																response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&edit=comments").setResponse((String) null).sendToClient(s, false);
																return new RequestResult(request, reuse, null);
															}
															response.setResponse(boardDoesNotExist);
														} else {
															final String msg = topic.getMessage();
															long numOfLines = StringUtils.getNumOfLinesInStr(msg) + 1;
															final long msgLineAmt = (msg == null || msg.isEmpty() ? 4 : numOfLines >= 4 ? numOfLines : 4);
															if(edit == null) {
																response.setResponse("<!DOCTYPE html>\r\n");
																response.appendResponse("<html>\r\n\t<head>\r\n");
																response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
																response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
																response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
																response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
																response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
																response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
																response.appendResponse("\t</head>\r\n");
																response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
																response.appendResponse("\t\t<h1>Topic: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "\" title=\"" + topic.getTitle() + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "</a></h1><hr>\r\n");
																response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
																response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
																response.appendResponse("\t\t<string>Edit board \"" + boardName + "\":</string>\r\n");
																//
																response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
																response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
																response.appendResponse("\t\t\t\t<tbody>\r\n");
																//
																
																response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(board.getDateCreated()) + "</td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b>TopicTitle:</b></td><td><input type=\"text\" name=\"title\" value=\"" + topic.getTitle() + "\" size=\"42\"></td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b># of Comments:&nbsp;" + topic.getComments().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&edit=comments" + "\">Edit comments</a></td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b>TopicMessage:</b></td><td><textarea rows=\"" + msgLineAmt + "\" cols=\"50\" name=\"message\" size=\"42\">" + msg + "</textarea></td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b>IsLocked:</b></td><td><input type=\"text\" name=\"isLocked\" value=\"" + topic.isLocked() + "\" size=\"8\"></td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b>IsSticky:</b></td><td><input type=\"text\" name=\"isSticky\" value=\"" + topic.isSticky() + "\" size=\"8\"></td></tr>\r\n");
																response.appendResponse("\t\t\t\t\t<tr><td><b>IsPrivate:</b></td><td><input type=\"text\" name=\"isPrivate\" value=\"" + topic.isPrivate() + "\" size=\"8\"></td></tr>\r\n");
																
																response.appendResponse("\t\t\t\t</tbody>\r\n");
																response.appendResponse("\t\t\t</table><hr>\r\n");
																//
																response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
																response.appendResponse("\t\t</form>\r\n");
																response.appendResponse("\t</body>\r\n</html>");
															} else if(edit.equalsIgnoreCase("comments")) {
																response.setResponse("<!DOCTYPE html>\r\n");
																response.appendResponse("<html>\r\n\t<head>\r\n");
																response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
																response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
																response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
																response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
																response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
																response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
																response.appendResponse("\t</head>\r\n");
																response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
																response.appendResponse("\t\t<h1>Topic: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "</a></h1><hr>\r\n");
																response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
																response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
																response.appendResponse("\t\t<string>Edit comments for topic \"" + topic.getTitle() + "\":</string>\r\n");
																//
																response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
																response.appendResponse("\t\t\t<tbody>\r\n");
																//
																for(ForumComment comment : topic.getComments()) {
																	response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&comment=" + comment.getUUID().toString() + "\">Comment #" + comment.getNumber() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&deleteComment=" + comment.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete comment #" + comment.getNumber() + "? This cannot be undone.');\">Delete</a></td></tr>\r\n");
																}
																response.appendResponse("\t\t\t</tbody>\r\n");
																response.appendResponse("\t\t</table><hr>\r\n");
																//
																
																response.appendResponse("\t</body>\r\n</html>");
															}
														}
													}
												} else {
													response.setResponse(topicDoesNotExist);
												}
											} else {
												if(deleteTopicUUID != null) {
													ForumTopic topic = board.getForumTopicFromUUID(deleteTopicUUID);
													if(topic != null) {
														response.setStatusMessage("Admin Page(Forum Topic \"" + topic.getTitle() + "\" Deleted)");
														board.removeTopic(topic);
														board.saveToFile();
														response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&edit=topics").setResponse((String) null).sendToClient(s, false);
														return new RequestResult(request, reuse, null);
													}
													response.setResponse(boardDoesNotExist);
												} else {
													final String desc = board.getDescription();
													long numOfLines = StringUtils.getNumOfLinesInStr(desc) + 1;
													final long descLineAmt = (desc == null || desc.isEmpty() ? 4 : numOfLines >= 4 ? numOfLines : 4);
													if(edit == null) {
														response.setResponse("<!DOCTYPE html>\r\n");
														response.appendResponse("<html>\r\n\t<head>\r\n");
														response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
														response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
														response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
														response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
														response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
														response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
														response.appendResponse("\t</head>\r\n");
														response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
														response.appendResponse("\t\t<h1>Board: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "</a></h1><hr>\r\n");
														response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
														response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
														response.appendResponse("\t\t<string>Edit board \"" + boardName + "\":</string>\r\n");
														//
														response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?forum=" + forumUUID + "&board=" + boardUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
														response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
														response.appendResponse("\t\t\t\t<tbody>\r\n");
														//
														
														response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(board.getDateCreated()) + "</td></tr>\r\n");
														response.appendResponse("\t\t\t\t\t<tr><td><b>BoardName:</b></td><td><input type=\"text\" name=\"name\" value=\"" + boardName + "\" size=\"42\"></td></tr>\r\n");
														response.appendResponse("\t\t\t\t\t<tr><td><b># of Forum Topics:&nbsp;" + board.getTopics().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&edit=topics" + "\">Edit topics</a></td></tr>\r\n");
														response.appendResponse("\t\t\t\t\t<tr><td><b>BoardDescription:</b></td><td><textarea rows=\"" + descLineAmt + "\" cols=\"50\" name=\"description\" size=\"42\">" + desc + "</textarea></td></tr>\r\n");
														
														response.appendResponse("\t\t\t\t</tbody>\r\n");
														response.appendResponse("\t\t\t</table><hr>\r\n");
														//
														response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
														response.appendResponse("\t\t</form>\r\n");
														response.appendResponse("\t</body>\r\n</html>");
													} else if(edit.equalsIgnoreCase("topics")) {
														response.setResponse("<!DOCTYPE html>\r\n");
														response.appendResponse("<html>\r\n\t<head>\r\n");
														response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
														response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
														response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
														response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
														response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
														response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
														response.appendResponse("\t</head>\r\n");
														response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
														response.appendResponse("\t\t<h1>Board: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + board.getName() + "</a></h1><hr>\r\n");
														response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
														response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
														response.appendResponse("\t\t<string>Edit topics for board \"" + boardName + "\":</string>\r\n");
														//
														response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
														response.appendResponse("\t\t\t<tbody>\r\n");
														//
														for(ForumTopic topic : board.getTopics()) {
															response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topic.getUUID().toString() + "\">" + topic.getTitle() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&deleteTopic=" + topic.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete the forum topic &quot;" + topic.getTitle().replace("'", "-").replace("\"", "-") + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
														}
														response.appendResponse("\t\t\t</tbody>\r\n");
														response.appendResponse("\t\t</table><hr>\r\n");
														//
														
														response.appendResponse("\t</body>\r\n</html>");
													}
												}
											}
											
										} else {
											response.setResponse(boardDoesNotExist);
										}
									} else {
										if(deleteBoardUUID != null) {
											ForumBoard board = forum.getForumBoardFromUUID(deleteBoardUUID);
											if(board != null) {
												response.setStatusMessage("Admin Page(Forum Board \"" + board.getName() + "\" Deleted)");
												forum.removeBoard(board);
												forum.saveToFile();
												response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&edit=boards").setResponse((String) null).sendToClient(s, false);
												return new RequestResult(request, reuse, null);
											}
											response.setResponse(boardDoesNotExist);
										} else {
											if(edit == null) {
												response.setResponse("<!DOCTYPE html>\r\n");
												response.appendResponse("<html>\r\n\t<head>\r\n");
												response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
												response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
												response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
												response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
												response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
												response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
												response.appendResponse("\t</head>\r\n");
												response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
												response.appendResponse("\t\t<h1>Forum: <a href=\"" + mainPageNoAdminPortLink + forumName + "\" title=\"" + forumName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "</a></h1><hr>\r\n");
												response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
												response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
												response.appendResponse("\t\t<string>Properties for forum \"" + forumName + "\":</string>\r\n");
												//
												response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?forum=" + forumUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
												response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
												response.appendResponse("\t\t\t\t<tbody>\r\n");
												//
												response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(forum.getDateCreated()) + "</td></tr>\r\n");
												response.appendResponse("\t\t\t\t\t<tr><td><b>ForumName:</b></td><td><input type=\"text\" name=\"name\" value=\"" + forum.getName() + "\" size=\"42\"></td></tr>\r\n");
												response.appendResponse("\t\t\t\t\t<tr><td><b># of Forum Boards:&nbsp;" + forum.getBoards().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&edit=boards" + "\">Edit boards</a></td></tr>\r\n");
												//
												ArrayList<String> domains = forum.getDomains();
												int domainsMax = domains.size() > 20 ? domains.size() : 20;
												for(int i = 0; i < domainsMax; i++) {
													String value = (i < domains.size() ? domains.get(i) : "");
													response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedDomain_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedDomain_" + i + "\" value=\"" + value + "\" size=\"25\"></td></tr>\r\n");
												}
												//
												response.appendResponse("\t\t\t\t</tbody>\r\n");
												response.appendResponse("\t\t\t</table><hr>\r\n");
												response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
												response.appendResponse("\t\t</form>\r\n");
												//
												response.appendResponse("\t</body>\r\n</html>");
											} else if(edit.equalsIgnoreCase("boards")) {
												response.setResponse("<!DOCTYPE html>\r\n");
												response.appendResponse("<html>\r\n\t<head>\r\n");
												response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
												response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
												response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
												response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
												response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
												response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
												response.appendResponse("\t</head>\r\n");
												response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
												response.appendResponse("\t\t<h1>Forum: <a href=\"" + mainPageNoAdminPortLink + forumName + "\" title=\"" + forumName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "</a></h1><hr>\r\n");
												response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
												response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
												response.appendResponse("\t\t<string>Edit boards for forum \"" + forumName + "\":</string>\r\n");
												//
												response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
												response.appendResponse("\t\t\t<tbody>\r\n");
												//
												for(ForumBoard board : forum.getBoards()) {
													response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + board.getUUID().toString() + "\">" + board.getName() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&deleteBoard=" + board.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete the forum board &quot;" + board.getName() + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
												}
												response.appendResponse("\t\t\t</tbody>\r\n");
												response.appendResponse("\t\t</table><hr>\r\n");
												//
												
												response.appendResponse("\t</body>\r\n</html>");
											}
										}
									}
								} else {
									response.setResponse(forumDoesNotExist);
								}
							} else if(deleteForumUUID != null) {
								ForumData forum = ForumData.getForumDataFromUUID(deleteForumUUID);
								if(forum != null) {
									response.setStatusMessage("Admin Page(Forum Data \"" + forum.getName() + "\" Deleted)");
									forum.delete();
									response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=forums").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								response.setResponse(boardDoesNotExist);
							} else if(restrictedFileUUID != null) {//XXX Restricted file Properties
								RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(restrictedFileUUID);
								if(res != null) {
									final String filePath = res.getRestrictedFile().getAbsolutePath();
									final String fileName = res.getRestrictedFile().getName();
									final String booleanOptionOn = "\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									response.appendResponse("\t\t<h1>Restricted File: " + filePath + "</h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									response.appendResponse("\t\t<string>Properties for Restricted File \"" + fileName + "\":</string>\r\n");
									//
									response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?restrictedFile=" + restrictedFileUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									response.appendResponse("\t\t\t\t\t<tr><td><b>AuthRealm:</b></td><td><input type=\"text\" name=\"AuthRealm\" value=\"" + res.getAuthorizationRealm() + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>IsHidden:</b></td><td><select name=\"" + res.isHidden.getName() + "\" title=\"" + res.isHidden.getDescription() + "\">" + (res.isHidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									int iPsMax = res.getAllowedIPAddresses().size() > 20 ? res.getAllowedIPAddresses().size() : 20;
									for(int i = 0; i < iPsMax; i++) {
										String value = (i < res.getAllowedIPAddresses().size() ? res.getAllowedIPAddresses().get(i) : "");
										response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedIp_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedIp_" + i + "\" value=\"" + value + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									}
									int authMax = res.getAuthorizationCredentials().size() > 20 ? res.getAuthorizationCredentials().size() : 20;
									int i = 0;
									ArrayList<Entry<String, String>> entrySet = new ArrayList<>(res.getAuthorizationCredentials().entrySet());
									if(entrySet.isEmpty()) {
										entrySet = new ArrayList<>();
										for(int index = 0; index < authMax; index++) {
											entrySet.add(CodeUtil.createBlankEntry("", ""));
										}
									}
									for(Entry<String, String> entry : entrySet) {
										String username = entry.getKey();
										String password = entry.getValue();
										response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + i + "\" value=\"" + username + "\" size=\"25\"></td><td><b>AuthPassword_" + (i + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + i + "\" value=\"" + password + "\" size=\"25\"></td></tr>\r\n");
										i++;
										if(i == res.getAuthorizationCredentials().size() && i < authMax) {
											for(int j = i; j < authMax; j++) {
												response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (j + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + j + "\" value=\"\" size=\"25\"></td><td><b>AuthPassword_" + (j + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + j + "\" value=\"\" size=\"25\"></td></tr>\r\n");
											}
										}
									}
									//
									response.appendResponse("\t\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t\t</table><hr>\r\n");
									response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
									response.appendResponse("\t\t</form>\r\n");
									response.appendResponse("\t\t" + restrictedFileNote + "\r\n");
									//
									response.appendResponse("\t</body>\r\n</html>");
								} else {
									response.setResponse(restrictedFileDoesNotExist);
								}
							} else if(domainUUID != null) {//XXX Domain Properties
								DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(domainUUID);
								if(domain != null) {
									final String domainName = domain.getDomain();
									response.setResponse("<!DOCTYPE html>\r\n");
									response.appendResponse("<html>\r\n\t<head>\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									response.appendResponse("\t</head>\r\n");
									response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									response.appendResponse("\t\t<h1>Domain: <a href=\"http://" + domainName + "/\" title=\"" + domainName + "\" target=\"_blank\">http://" + domainName + "/</a></h1><hr>\r\n");
									response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									response.appendResponse("\t\t<string>Properties for domain \"" + domainName + "\":</string>\r\n");
									//
									response.appendResponse("\t\t<form action=\"" + request.requestedFilePath + "?domain=" + domainUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									//
									response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(domain.dateCreated.getValue().longValue()) + "</td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCacheTime(domain.lastEdited.getValue().longValue()) + "</td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>NetworkMTU:</b></td><td><input type=\"number\" step=\"512\" min=\"512\" max=\"819200\" name=\"" + domain.mtu.getName() + "\" value=\"" + domain.mtu.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>CacheMaxAge:</b></td><td><input type=\"number\" min=\"0\" name=\"" + domain.cacheMaxAge.getName() + "\" value=\"" + domain.cacheMaxAge.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>AreDirectoriesForbidden:</b></td><td><select name=\"" + domain.areDirectoriesForbidden.getName() + "\">" + (domain.areDirectoriesForbidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"" + domain.calculateDirectorySizes.getName() + "\">" + (domain.calculateDirectorySizes.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFileName:</b></td><td><input type=\"text\" name=\"" + domain.defaultFileName.getName() + "\" value=\"" + domain.defaultFileName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"" + domain.defaultFontFace.getName() + "\" value=\"" + domain.defaultFontFace.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultPageIcon:</b></td><td><input type=\"text\" name=\"" + domain.defaultPageIcon.getName() + "\" value=\"" + domain.defaultPageIcon.getValue() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultStylesheet:</b></td><td><input type=\"text\" name=\"" + domain.defaultStylesheet.getName() + "\" value=\"" + domain.defaultStylesheet.getValue() + "\" size=\"42\"></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayName:</b></td><td><input type=\"text\" name=\"" + domain.displayName.getName() + "\" value=\"" + domain.displayName.getValue() + "\" size=\"42\"></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayLogEntries:</b></td><td><select name=\"" + domain.displayLogEntries.getName() + "\" title=\"" + domain.displayLogEntries.getDescription() + "\">" + (domain.displayLogEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td><input type=\"text\" name=\"" + domain.folder.getName() + "\" value=\"" + domain.folder.getValue().getAbsolutePath() + "\" size=\"42\"></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableGZipCompression:</b></td><td><select name=\"" + domain.enableGZipCompression.getName() + "\" title=\"" + domain.enableGZipCompression.getDescription() + "\">" + (domain.enableGZipCompression.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFileUploads:</b></td><td><select name=\"" + domain.enableFileUpload.getName() + "\" title=\"" + domain.enableFileUpload.getDescription() + "\">" + (domain.enableFileUpload.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableAlternateDirectoryListingViews:</b></td><td><select name=\"" + domain.enableAlternateDirectoryListingViews.getName() + "\">" + (domain.enableAlternateDirectoryListingViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFilterView:</b></td><td><select name=\"" + domain.enableFilterView.getName() + "\">" + (domain.enableFilterView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableSortViews:</b></td><td><select name=\"" + domain.enableSortView.getName() + "\">" + (domain.enableSortView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableMediaView:</b></td><td><select name=\"" + domain.enableMediaView.getName() + "\">" + (domain.enableMediaView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableReadableFileViews:</b></td><td><select name=\"" + domain.enableReadableFileViews.getName() + "\">" + (domain.enableReadableFileViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableVLCPlaylistView:</b></td><td><select name=\"" + domain.enableVLCPlaylistView.getName() + "\">" + (domain.enableVLCPlaylistView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>EnableXmlListView:</b></td><td><select name=\"" + domain.enableXmlListView.getName() + "\">" + (domain.enableXmlListView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>IgnoreThumbsdbFiles:</b></td><td><select name=\"" + domain.ignoreThumbsdbFiles.getName() + "\">" + (domain.ignoreThumbsdbFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>ListDirectoriesFirst:</b></td><td><select name=\"" + domain.listDirectoriesFirst.getName() + "\">" + (domain.listDirectoriesFirst.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>NumberDirectoryEntries:</b></td><td><select name=\"" + domain.numberDirectoryEntries.getName() + "\">" + (domain.numberDirectoryEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									response.appendResponse("\t\t\t\t\t<tr><td><b>ServerName:</b></td><td><input type=\"text\" name=\"" + domain.serverName.getName() + "\" value=\"" + domain.serverName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									//
									response.appendResponse("\t\t\t\t</tbody>\r\n");
									response.appendResponse("\t\t\t</table><hr>\r\n");
									response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
									response.appendResponse("\t\t</form>\r\n");
									//
									
									response.appendResponse("\t</body>\r\n</html>");
								} else {
									response.setResponse(domainDoesNotExist);
								}
							} else {//XXX Server Settings (Main Page)
								response.setResponse("<!DOCTYPE html>\r\n");
								response.appendResponse("<html>\r\n\t<head>\r\n");
								response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
								response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
								response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
								response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
								response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
								response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
								response.appendResponse("\t</head>\r\n");
								response.appendResponse("\t<body bgcolor=\"#99E699\">\r\n");
								response.appendResponse("\t\t<div class=\"wrapper\">\r\n");
								response.appendResponse("\t\t\t<h1>Server Administration - Server Settings</h1><hr>\r\n");
								response.appendResponse("\t\t\t" + pageLinkHeader + "\r\n");
								response.appendResponse("\t\t\t<string>Change settings for this server:</string>\r\n");
								//
								response.appendResponse("\t\t\t<form action=\"/serverSettings\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
								response.appendResponse("\t\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
								response.appendResponse("\t\t\t\t\t<tbody>\r\n");
								//
								final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
								final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
								//
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td><input type=\"text\" name=\"homeDirectory\" value=\"" + homeDirectory.getAbsolutePath() + "\" size=\"42\" title=\"The default or fallback home directory to read from when parsing incoming client requests\"></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"calculateDirectorySizes\" title=\"The default or fallback value used to determine whether or not the server should calculate and send directory sizes to the client in directory listings(very resource intensive and time consuming, I recommend leaving this set to false)\">" + (calculateDirectorySizes ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"defaultFontFace\" value=\"" + defaultFontFace + "\" size=\"42\" title=\"The default or fallback font face that is used for the directory listing page that is sent to the client(e.g. Times New Roman)\"></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>Php-CGI_FilePath:</b></td><td><input type=\"text\" name=\"phpExeFilePath\" value=\"" + PhpResult.phpExeFilePath + "\" size=\"42\" title=\"The path to the main PHP executable file\"></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>RequestTimeout:</b></td><td><input type=\"number\" step=\"1\" min=\"10000\" max=\"60000\" name=\"requestTimeout\" value=\"" + requestTimeout + "\" size=\"42\" title=\"The time(in milliseconds) to wait before a client will be timed out\"></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>AdminAuthorizationRealm:</b></td><td><textarea readonly rows=\"1\" name=\"noresize\" title=\"The authorization realm for this administration interface\">" + adminAuthorizationRealm + "</textarea></td></tr>\r\n");
								response.appendResponse("\t\t\t\t\t\t<tr><td><b>AdminAuthorizationUsername:</b></td><td><textarea readonly rows=\"1\" name=\"noresize\" title=\"The username for this administration interface\">" + adminUsername + "</textarea></td></tr>\r\n");
								//
								response.appendResponse("\t\t\t\t\t</tbody>\r\n");
								response.appendResponse("\t\t\t\t</table><hr>\r\n");
								response.appendResponse("\t\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
								response.appendResponse("\t\t\t</form>\r\n");
								//
								response.appendResponse("\t\t\t<div class=\"push\"></div>\r\n");
								response.appendResponse("\t\t</div>\r\n");
								response.appendResponse("\t\t<div class=\"footer\" align=\"center\">\r\n");
								response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\"><tbody><tr><td><p class=\"red_border\"><a href=\"" + mainPageLink + "?" + shutdownStr + "=1" + "\" onClick=\"return confirm('Are you sure you want to shut down the server?');\">Shut down the server</a></p></td></tr></tbody></table>\r\n");
								response.appendResponse("\t\t\t<string>To change the administration credentials, you must edit the \"" + optionsFileName + "\" file manually and then restart the server.</string>\r\n");
								response.appendResponse("\t\t</div>\r\n");
								response.appendResponse("\t</body>\r\n</html>");
							}
						}
						if(response.getResponse().equals(domainDoesNotExist) || response.getResponse().equals(unableToCreateDomain) || response.getResponse().equals(restrictedFileDoesNotExist) || response.getResponse().equals(unableToCreateResFile) || response.getResponse().equals(pageDoesNotExist) || response.getResponse().equals(forumDoesNotExist) || response.getResponse().equals(boardDoesNotExist) || response.getResponse().equals(topicDoesNotExist) || response.getResponse().equals(commentDoesNotExist)) {
							response.setStatusCode(HTTP_404);
						} else if(response.getResponse().equals(domainAlreadyExists) || response.getResponse().equals(resFileAlreadyExists)) {
							response.setStatusCode(HTTP_409);
						} else {
							@SuppressWarnings("null")
							//Because I'm not currently using statusCodeToSend, but may use it later.
							boolean send200 = statusCodeToSend == null;
							response.setStatusCode(send200 ? HTTP_200 : statusCodeToSend);
						}
						response.setStatusMessage("Admin Page");
						response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", cachePrivateMustRevalidate);
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Last-Modified", StringUtils.getCurrentCacheTime());
						response.setHeader("Accept-Ranges", "none");
						response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
					} else if(request.requestedFilePath.equalsIgnoreCase(DEFAULT_PAGE_ICON)) {
						println("\t*** HTTP/1.1 200 OK - Admin file");
						OutputStream outStream = s.getOutputStream();
						@SuppressWarnings("resource")
						PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"), true);
						out.println("HTTP/1.1 200 OK");
						if(authResult.authorizedCookie != null) {
							out.println("Set-Cookie: " + authResult.authorizedCookie);
						}
						File file = ResourceFactory.getResourceFromStreamAsFile(adminFolder, "textures/icons/favicon.ico");
						out.flush();
						sendFileToClient(out, outStream, file, request.method, clientAddress, 0x400, false);
					} else if(request.requestedFilePath.equalsIgnoreCase(DEFAULT_STYLESHEET)) {
						println("\t*** HTTP/1.1 200 OK - Admin file");
						OutputStream outStream = s.getOutputStream();
						@SuppressWarnings("resource")
						PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"), true);
						out.println("HTTP/1.1 200 OK");
						if(authResult.authorizedCookie != null) {
							out.println("Set-Cookie: " + authResult.authorizedCookie);
						}
						File file = ResourceFactory.getResourceFromStreamAsFile(adminFolder, "files/layout.css", "layout.css");
						out.flush();
						sendFileToClient(out, outStream, file, request.method, clientAddress, 0x400, false);
					} else {
						File requestedFile = new File(adminFolder, StringUtils.decodeHTML(request.requestedFilePath.substring(1)));
						if(requestedFile.exists() && requestedFile.isFile()) {
							println("\t*** HTTP/1.1 200 OK - Admin file");
							OutputStream outStream = s.getOutputStream();
							@SuppressWarnings("resource")
							PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"), true);
							out.println("HTTP/1.1 200 OK");
							if(authResult.authorizedCookie != null) {
								out.println("Set-Cookie: " + authResult.authorizedCookie);
							}
							out.flush();
							sendFileToClient(out, outStream, requestedFile, request.method, clientAddress, 0x400, true);
						} else {
							response.setStatusMessage("Admin Page");
							response.setStatusCode(HTTP_404);
							response.setHeader("Vary", "Accept-Encoding");
							response.setHeader("Content-Type", "text/html; charset=UTF-8");
							response.setHeader("Server", SERVER_NAME_HEADER);
							response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							response.setResponse("<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>404 - File not Found!!!11 - " + SERVER_NAME + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
									+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
									+ "\t</head>\r\n"//
									+ "\t<body>\r\n"//
									+ "\t\t<h1>Error 404 - File not found</h1><hr>\r\n"//
									+ "\t\t<string>The file \"" + request.requestedFilePath + "\" does not exist.</string><br>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
						}
					}
					return new RequestResult(request, reuse, null);
				} else if(request.method.equalsIgnoreCase("POST")) {
					if(request.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						final String createDomainUUID = request.requestArguments.get("createDomain");
						final String domainUUID = request.requestArguments.get("domain");
						final String createResFileUUID = request.requestArguments.get("createResFile");
						final String restrictedFileUUID = request.requestArguments.get("restrictedFile");
						final String forumUUID = request.requestArguments.get("forum");
						final String boardUUID = request.requestArguments.get("board");
						final String topicUUID = request.requestArguments.get("topic");
						final String commentUUID = request.requestArguments.get("comment");
						final HashMap<String, String> values = request.formURLEncodedData.postRequestArguments;
						if(request.requestedFilePath.equals("/serverSettings")) {
							if(values.isEmpty()) {
								response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
								return new RequestResult(request, reuse, null);
							}
							values.remove("noresize");
							for(Entry<String, String> entry : values.entrySet()) {
								String pname = entry.getKey();
								String value = entry.getValue();
								if(pname.equals("homeDirectory")) {
									if(value != null) {
										File homeDir = new File(value);
										if(homeDir.exists()) {
											homeDirectory = homeDir;
										}
									}
								} else if(pname.equals("calculateDirectorySizes")) {
									calculateDirectorySizes = Boolean.valueOf(value).booleanValue();
								} else if(pname.equals("defaultFontFace")) {
									if(value != null && !value.isEmpty()) {
										defaultFontFace = value;
									}
								} else if(pname.equals("phpExeFilePath")) {
									if(value != null) {
										File phpExeFile = new File(value);
										if(phpExeFile.exists()) {
											PhpResult.phpExeFilePath = phpExeFile.getAbsolutePath();
										}
									}
								} else if(pname.equals("requestTimeout")) {
									if(StringUtils.isStrLong(value)) {
										requestTimeout = Integer.valueOf(value).intValue();
									}
								}
							}
							response.setStatusMessage("Admin Page(POST - Server Settings)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(request, reuse, null);
						} else if(createResFileUUID != null) {//XXX Create Restricted File (POST)
							if(StringUtils.isStrUUID(createResFileUUID)) {
								if(values.isEmpty()) {
									response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								final UUID uuid = UUID.fromString(createResFileUUID);
								final RestrictedFile res = RestrictedFile.createNewRestrictedFile(uuid);
								final String filePathKey = "FilePath";
								final String filePath = FilenameUtils.normalize(values.get(filePathKey));
								final File file = filePath != null ? new File(filePath) : null;
								printlnDebug("file == null: " + (file == null) + "; file exists: " + (file != null ? file.exists() + "" : "N/A"));
								if(file != null && file.exists()) {
									RestrictedFile check = RestrictedFile.getSpecificRestrictedFile(file);
									if(check == null) {
										values.remove(filePathKey);
										res.setRestrictedFile(file);
										res.setValuesFromHashMap(values);
										response.setStatusMessage("Admin Page(POST: File \"" + FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(res.getRestrictedFile().getAbsolutePath() + File.separatorChar)) + "\" restricted)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?page=restrictedFiles").setResponse((String) null).sendToClient(s, false);
										return new RequestResult(request, reuse, null);
									}
									res.delete();
									response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?resFileAlreadyExistsError=" + check.getUUID().toString()).setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								res.delete();
							}
							response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?createResFileError=" + createResFileUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(request, reuse, null);
						} else if(createDomainUUID != null) {//XXX Create Domain (POST)
							if(StringUtils.isStrUUID(createDomainUUID)) {
								if(values.isEmpty()) {
									response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								UUID uuid = UUID.fromString(createDomainUUID);
								DomainDirectory domain = DomainDirectory.createNewDomainDirectory(uuid);
								String domainName = values.get(domain.domain.getName());
								String homeDirectoryPath = values.get("HomeDirectory");
								File homeDirectory = new File(homeDirectoryPath);
								if(!homeDirectory.exists()) {
									response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?page=createDomain&domainName=" + values.get("Domain") + "&homeDirectoryError=" + homeDirectoryPath).setResponse((String) null).sendToClient(s, false);
									domain.delete();
									return new RequestResult(request, reuse, null);
								}
								if(domainName != null && !domainName.isEmpty()) {
									DomainDirectory check = DomainDirectory.getDomainDirectoryFromDomainName(domainName);
									if(check == null) {
										values.remove(domain.domain.getName());
										domain.domain.setValue(domainName);
										domain.setValuesFromHashMap(values);
										response.setStatusMessage("Admin Page(POST: Domain \"" + domain.getDomain() + "\" created)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?page=domains").setResponse((String) null).sendToClient(s, false);
										return new RequestResult(request, reuse, null);
									}
									domain.delete();
									response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?domainAlreadyExistsError=" + check.getUUID().toString()).setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								domain.delete();
							}
							response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?createDomainError=" + createDomainUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(request, reuse, null);
						} else if(domainUUID != null) {
							DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(domainUUID);
							if(domain != null) {
								if(values.isEmpty()) {
									response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								domain.setValuesFromHashMap(values);
							} else {
								response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?domainError=" + domainUUID).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(request, reuse, null);
							}
						} else if(restrictedFileUUID != null) {
							RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(restrictedFileUUID);
							if(res != null) {
								if(values.isEmpty()) {
									response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								res.setValuesFromHashMap(values);
							} else {
								response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?restrictedFileError=" + restrictedFileUUID).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(request, reuse, null);
							}
						} else if(forumUUID != null) {
							ForumData forum = ForumData.getForumDataFromUUID(forumUUID);
							if(forum != null) {
								if(values.isEmpty()) {
									response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + request.formURLEncodedData.postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(request, reuse, null);
								}
								if(boardUUID != null) {
									ForumBoard board = forum.getForumBoardFromUUID(boardUUID);
									if(board != null) {
										if(topicUUID != null) {
											ForumTopic topic = board.getForumTopicFromUUID(topicUUID);
											if(topic != null) {
												if(commentUUID != null) {
													ForumComment comment = topic.getForumCommentFromUUID(commentUUID);
													if(comment != null) {
														comment.setValuesFromHashMap(values);
													} else {
														response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?commentError=" + commentUUID).setResponse((String) null).sendToClient(s, false);
														return new RequestResult(request, reuse, null);
													}
												} else {
													topic.setValuesFromHashMap(values);
												}
											} else {
												response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?topicError=" + topicUUID).setResponse((String) null).sendToClient(s, false);
												return new RequestResult(request, reuse, null);
											}
										} else {
											board.setValuesFromHashMap(values);
										}
									} else {
										response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?boardError=" + boardUUID).setResponse((String) null).sendToClient(s, false);
										return new RequestResult(request, reuse, null);
									}
								} else {
									forum.setValuesFromHashMap(values);
								}
							} else {
								response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + "?restrictedFileError=" + restrictedFileUUID).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(request, reuse, null);
							}
						}
						response.setStatusMessage("Admin Page(POST)").setStatusCode(HTTP_303).setHeader("Location", "http://" + request.host + request.requestedFilePath + request.requestArgumentsStr).setResponse((String) null).sendToClient(s, false);
						return new RequestResult(request, reuse, null);
					} else if(request.contentType.toLowerCase().contains("multipart/form-data")) {
						/*if(request.multiPartFormData != null) {
							if(!request.multiPartFormData.fileData.isEmpty()) {
								for(FileData data : request.multiPartFormData.fileData) {
									try {
										data.writeToFile(adminFolder);//For testing purposes only.
									} catch(IOException e) {
										PrintUtil.printThrowable(e);
									}
								}
								response.setStatusCode(HTTP_201);
								response.setHeader("Vary", "Accept-Encoding");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								response.setHeader("Location", "/" + request.multiPartFormData.fileData.get(0).fileName);
								response.setHeader("Content-Length", "0");
								response.setResponse((String) null);
								response.sendToClient(s, false);
							} else {
								response.setStatusCode(HTTP_204);
								response.setHeader("Vary", "Accept-Encoding");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								response.setHeader("Content-Length", "0");
								response.setResponse((String) null);
								response.sendToClient(s, false);
							}
							int i = 0;
							for(Entry<String, String> entry : request.multiPartFormData.formData.entrySet()) {
								PrintUtil.printlnNow("[" + i + "]: \"" + entry.getKey() + " = " + entry.getValue() + "\";");
								i++;
							}
							s.close();
							return;
						}*/
					} else {
						PrintUtil.println("Unknown post request content type: \"" + request.contentType + "\"...");
					}
				}
				response.setStatusCode(HTTP_501);
				response.setHeader("Vary", "Accept-Encoding");
				response.setHeader("Content-Type", "text/html; charset=UTF-8");
				response.setHeader("Server", SERVER_NAME_HEADER);
				response.setHeader("Cache-Control", cachePrivateMustRevalidate);
				response.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>501 - Feature not Implemented - " + SERVER_NAME + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
						+ "\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n"//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Error 501 - Not Implemented</h1><hr>\r\n"//
						+ "\t\t<string>The HTML method your client used(\"" + request.method + "\") or one of its functions is not implemented for this page.</string><br>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				response.sendToClient(s, true);
				return new RequestResult(request, reuse, null);
			}
			response.setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + request.protocolRequest + "\"").setResponse((String) null).sendToClient(s, false);
			try {
				in.close();
			} catch(Throwable ignored) {
			}
			s.close();
			return new RequestResult(request, ReusedConn.FALSE, null);
		} catch(IOException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equalsIgnoreCase("Client sent no data.")) {
					if(request != null) {
						request.cancel();
					}
					return new RequestResult(request, ReusedConn.FALSE, null);
				}
			}
			println("\t /!\\\tFailed to respond to client request: \"" + clientAddress + "\"\r\n\t/___\\\tCause: " + e.getMessage());
			return new RequestResult(request, ReusedConn.FALSE, e);
		}
	}
	
	/** @param out PrintWriter wrapping outStream(for sending text)
	 * @param outStream OutputStream of Socket(for sending file bytes)
	 * @param file The requested file
	 * @param method The HTTP Method
	 * @param clientAddress The client's ip address
	 * @param mtu The networking MTU
	 * @param privateCache Whether or not the file should be private, indicating
	 *            no caching client-side.
	 * @throws IOException Thrown if there was an issue sending the file to the
	 *             client */
	public static final void sendFileToClient(PrintWriter out, OutputStream outStream, File file, String method, String clientAddress, int mtu, boolean privateCache) throws IOException {
		sendFileToClient(out, outStream, file, method, clientAddress, mtu, privateCache, null);
	}
	
	/** @param out PrintWriter wrapping outStream(for sending text)
	 * @param outStream OutputStream of Socket(for sending file bytes)
	 * @param file The requested file
	 * @param method The HTTP Method
	 * @param clientAddress The client's ip address
	 * @param mtu The networking MTU
	 * @param privateCache Whether or not the file should be private, indicating
	 *            no caching client-side.
	 * @param clientInfo The client's information(may be null)
	 * @throws IOException Thrown if there was an issue sending the file to the
	 *             client */
	public static final void sendFileToClient(PrintWriter out, OutputStream outStream, File file, String method, String clientAddress, int mtu, boolean privateCache, ClientInfo clientInfo) throws IOException {
		FileInfo info = new FileInfo(file, null);
		out.println("Content-Type: " + info.mimeType);
		out.println("Server: " + SERVER_NAME_HEADER);
		out.println("Cache-Control: " + (privateCache ? cachePrivateMustRevalidate : "public, max-age=604800"));
		out.println("Date: " + StringUtils.getCurrentCacheTime());
		out.println("Last-Modified: " + StringUtils.getCacheTime(file.lastModified()));
		out.println("Accept-Ranges: none");
		out.println("Content-Length: " + info.contentLength);
		out.println("");
		out.flush();
		if(method.equalsIgnoreCase("GET")) {
			try(InputStream fileIn = file.toURI().toURL().openConnection().getInputStream()) {
				info.copyInputStreamToOutputStream(fileIn, outStream, mtu, clientInfo);//1024, clientInfo);
				println("\t\t\tSent admin file \r\n\t\t\t\"" + FilenameUtils.normalize(file.getAbsolutePath()) + "\"\r\n\t\t\t to client \"" + clientAddress + "\" successfully.");
			} catch(Throwable e) {
				println("\t /!\\\tFailed to send admin file \r\n\t/___\\\t\"" + FilenameUtils.normalize(file.getAbsolutePath()) + "\"\r\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
			}
		}
	}
	
	/** Parses the client's incoming server request(via
	 * {@link #HandleRequest(Socket, InputStream, boolean, ReusedConn)}), then
	 * returns the result.
	 * 
	 * @param s The client
	 * @param in The client's input stream
	 * @param https HTTPS(<code>true</code>) or HTTP(<code>false</code>)
	 * @return The result of parsing the client's request.
	 * @see #HandleRequest(Socket, InputStream, boolean, ReusedConn)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	protected static final RequestResult HandleRequest(final Socket s, final InputStream in, boolean https) {
		return HandleRequest(s, in, https, new ReusedConn(false, ""));
	}
	
	/** Assembles the client's request data, attempts to connect to the client's
	 * requested server, then sends the client's original request to the remote
	 * server.<br>
	 * If {@link #sendProxyHeadersWithRequest} is set to true, then
	 * <code>Via:</code> and <code>X-Forwarded-For:</code> headers are sent
	 * along with the client's existing request headers, identifying this
	 * connection as a proxy, and identifying the client to the remote server.<br>
	 * <br>
	 * If CONNECT was used instead of GET, then this method attempts to connect
	 * to the specified server(using SSL if the port is 443 or https:// is
	 * used), then simply copies data from the client to the remote server, and
	 * data from the remote server to the client.
	 * 
	 * @param client The client
	 * @param clientIn The client's input stream
	 * @param clientOut The client's output stream
	 * @param request The client's request
	 * @param connect Whether or not the client wants to CONNECT to a remote
	 *            server(<code>true</code>), or GET a webpage from one(
	 *            <code>false</code>).
	 * @return The address of the remote server that the client wanted to
	 *         communicate to, if available.
	 * @throws Throwable Thrown if there was an issue communicating with the
	 *             client, the remote server, or if something went wrong
	 *             here(i.e. The proxy server is not enabled, I dun goofed the
	 *             code, etc.)
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ReusedConn)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	protected static final String handleProxyRequest(final Socket client, final InputStream clientIn, final OutputStream clientOut, final HTTPClientRequest request, boolean connect) throws Throwable {
		if(!enableProxyServer) {
			throw new IllegalStateException("Method \"handleProxyRequest\" called when proxy server is disabled!");
		}
		request.getStatus().removeFromList();
		if(client == null || client.isClosed() || client.isInputShutdown() || client.isOutputShutdown()) {
			throw new SocketException("Socket is closed.");
		}
		if(!request.isProxyRequest) {
			throw new IllegalArgumentException("Client request was not a proxy request.");
		}
		String clientAddress = StringUtils.replaceOnce(client.getRemoteSocketAddress().toString(), "/", "");
		final int clientPort = AddressUtil.getPortFromAddress(clientAddress);
		clientAddress = clientAddress.replace(":" + clientPort, "");
		
		String serverAddress = request.requestedServerAddress;
		int serverPort = AddressUtil.getPortFromAddress(serverAddress);
		if(serverPort == -1) {
			serverPort = request.protocolRequest.toLowerCase().startsWith("https://") ? 443 : 80;
		} else {
			serverAddress = serverAddress.replace(":" + serverPort, "");//serverAddress.substring(0, serverAddress.length() - (":" + serverPort).length());
		}
		
		String getProxyAddress = AddressUtil.getIp();
		final String proxyAddress = getProxyAddress.isEmpty() ? AddressUtil.getClientAddress(StringUtils.replaceOnce(client.getLocalSocketAddress().toString(), "/", "")) : getProxyAddress + ":" + client.getLocalPort();
		final String ipDescription = "<s>" + serverAddress + ":" + serverPort + "</s> --&gt; " + proxyAddress + " --&gt; " + clientAddress + (clientPort != -1 ? ":" + clientPort : "");
		
		String clientResponse;
		try {
			clientResponse = new String(Base64.getDecoder().decode(request.proxyAuthorization.replace("Basic", "").trim()));
		} catch(IllegalArgumentException ignored) {
			clientResponse = "";
		}
		String[] creds = clientResponse.split(":");
		String clientUser = creds.length == 2 ? creds[0] : "";
		String clientPass = creds.length == 2 ? creds[1] : "";
		if(proxyRequiresAuthorization && !(proxyUsername.equalsIgnoreCase(clientUser) && proxyPassword.equals(clientPass))) {
			final HTTPStatusCodes code = HTTP_407;
			println("[PROXY] " + request.version + " " + code);
			PrintStream co = new PrintStream(clientOut);
			co.println(request.version + " " + code);
			co.println("Server: " + SERVER_NAME_HEADER);
			co.println("Date: " + StringUtils.getCurrentCacheTime());
			co.println("Cache-Control: " + cachePrivateMustRevalidate);
			co.println("Vary: Accept-Encoding");
			co.println("Connection: close");
			co.println("Proxy-Authenticate: Basic realm=\"" + proxyAuthroizationRealm + "\"");
			co.println("Content-Type: text/html; charset=UTF-8");
			String response = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"http://" + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
					+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"http://" + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
					+ "\t\t<title>" + code.getName() + " - " + code.getValue() + " - " + SERVER_NAME + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error " + code.getName() + " - " + code.getValue() + "</h1><hr>\r\n"//
					+ "\t\t<string>This proxy requires a username and password.</string><br>\r\n"//
					+ "\t\t<hr><string><b>" + ipDescription + "</b></string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(request.acceptEncoding.contains("gzip") && response.length() > 33) {
				co.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(response, "UTF-8");
				co.println("Content-Length: " + r.length);
				co.println("");
				co.write(r);
				r = null;
			} else {
				co.println("Content-Length: " + response.length());
				co.println("");
				co.println(response);
			}
			return serverAddress;
		}
		request.getStatus().setProxyRequest(true);
		request.getStatus().addToList(connectedProxyRequests);
		DomainDirectory check = DomainDirectory.getDomainDirectoryFromDomainName(serverAddress);
		if(check != null) {
			if(!check.getDisplayLogEntries()) {
				PrintUtil.clearLogsBeforeDisplay();
				PrintUtil.clearErrLogsBeforeDisplay();
			}
		}
		println("[PROXY] Server address: " + serverAddress + "; Port: " + serverPort);
		request.getStatus().setStatus("[PROXY] Attempting to connect to server \"" + serverAddress + ":" + serverPort + "\"...");
		final SocketConnectResult result = new SocketConnectResult(serverAddress, serverPort);
		final Socket server = result.s;
		
		if(!result.failedToConnect) {
			if(connect || serverPort == 443) {
				request.getStatus().setStatus("[PROXY] Connection to server \"" + serverAddress + ":" + serverPort + "\" was successful.");
				PrintStream co = new PrintStream(clientOut);
				co.println(request.version + " " + HTTP_200_1);
				co.println("");
				println("[PROXY] " + request.version + " " + HTTP_200_1);
			} else {
				request.getStatus().setStatus("[PROXY] Connection to server \"" + serverAddress + ":" + serverPort + "\" was successful. Relaying client's request to server...");
				println("[PROXY] Relaying client request headers to requested server...");
				PrintStream so = new PrintStream(result.out);
				so.println(request.version.toUpperCase().startsWith("HTTP/2.") ? request.protocolRequest : (request.method + " " + (request.requestedFilePath.trim().isEmpty() ? "/" : request.requestedFilePath) + " " + request.version));
				if(sendProxyHeadersWithRequest) {
					so.println("X-Forwarded-For: " + clientAddress);
					final String serverVia = request.version.toUpperCase().replace("HTTP/", "") + " " + PROXY_SERVER_NAME + " (" + SERVER_NAME_HEADER + ")";
					Set<String> keySet = request.headers.keySet();
					if(StringUtils.containsIgnoreCase(keySet, "Via")) {
						String viaKey = StringUtils.getStringInList(keySet, "Via");
						String viaValue = request.headers.get(viaKey);
						so.println(viaKey.trim() + ": " + (viaValue == null || viaValue.isEmpty() ? serverVia : viaValue + ", " + serverVia));
					} else {
						so.println("Via: " + serverVia);
					}
				}
				request.getStatus().setContentLength(request.headers.keySet().size());
				request.getStatus().setCount(0);
				for(Entry<String, String> entry : request.headers.entrySet()) {
					if(entry.getKey() != null) {
						if(!sendProxyHeadersWithRequest) {
							if(entry.getKey().trim().toLowerCase().startsWith("proxy")) {
								continue;
							}
						}
						if(entry.getKey().trim().equalsIgnoreCase("Proxy-Authorization")) {
							continue;
						}
						so.println(entry.getKey() + ": " + entry.getValue());
					}
					request.getStatus().incrementCount();
				}
				so.println("");
				if(request.postRequestData.length > 0) {
					println("[PROXY] Relaying client's POST request data to requested server...");
					request.getStatus().setContentLength(request.postRequestData.length);
					request.getStatus().setCount(0);
					for(int i = 0; i < request.postRequestData.length; i++) {
						so.write(request.postRequestData[i]);
						request.getStatus().incrementCount();
					}
				}
				println("[PROXY] Client request sent to requested server successfully.");
			}
			
			final Property<Throwable> clientException = new Property<>("Client Exception");
			final Property<Throwable> serverException = new Property<>("Server Exception");
			final Property<Long> clientLastReadTime = new Property<>("Client last read time", Long.valueOf(System.currentTimeMillis()));
			final Property<Long> serverLastReadTime = new Property<>("Server last read time", Long.valueOf(System.currentTimeMillis()));
			
			final Property<Boolean> clientReadInProgress = new Property<>("Client read in progress", Boolean.FALSE);
			final Property<Boolean> clientWriteInProgress = new Property<>("Client write in progress", Boolean.FALSE);
			final Property<Boolean> serverReadInProgress = new Property<>("Server read in progress", Boolean.FALSE);
			final Property<Boolean> serverWriteInProgress = new Property<>("Server write in progress", Boolean.FALSE);
			
			final int MTU = 4096;
			int getClientMTU = client.getReceiveBufferSize();
			final int clientMTU = getClientMTU > 0 ? getClientMTU : MTU;
			int getServerMTU = server.getReceiveBufferSize();
			final int serverMTU = getServerMTU > 0 ? getServerMTU : MTU;
			final Thread clientToServerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buf = new byte[clientMTU];
					try {
						while(serverActive && !(client.isClosed() || server.isClosed() || client.isInputShutdown())) {
							int clientRead;
							clientReadInProgress.setValue(Boolean.TRUE);
							while((clientRead = clientIn.read(buf)) != -1) {
								clientReadInProgress.setValue(Boolean.FALSE);
								clientLastReadTime.setValue(Long.valueOf(System.currentTimeMillis()));
								serverWriteInProgress.setValue(Boolean.TRUE);
								result.out.write(buf, 0, clientRead);
								serverWriteInProgress.setValue(Boolean.FALSE);
								clientLastReadTime.setValue(Long.valueOf(System.currentTimeMillis()));
								request.getStatus().addNumOfBytesSentToServer(clientRead);
								clientReadInProgress.setValue(Boolean.TRUE);
							}
							clientReadInProgress.setValue(Boolean.FALSE);
							serverWriteInProgress.setValue(Boolean.FALSE);
							if(clientRead == -1) {
								break;
							}
						}
					} catch(Throwable e) {
						clientException.setValue(e);
					}
					buf = null;
					clientReadInProgress.setValue(Boolean.FALSE);
					serverWriteInProgress.setValue(Boolean.FALSE);
				}
			}, Thread.currentThread().getName() + "_ProxyThread_ClientToServer");
			clientToServerThread.setDaemon(true);
			clientToServerThread.start();
			final Thread serverToClientThread = new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buf = new byte[serverMTU];
					try {
						while(serverActive && !(server.isClosed() || client.isClosed() || server.isInputShutdown())) {
							int serverRead;
							serverReadInProgress.setValue(Boolean.TRUE);
							while((serverRead = result.in.read(buf)) != -1) {
								serverReadInProgress.setValue(Boolean.FALSE);
								serverLastReadTime.setValue(Long.valueOf(System.currentTimeMillis()));
								clientWriteInProgress.setValue(Boolean.TRUE);
								clientOut.write(buf, 0, serverRead);
								clientWriteInProgress.setValue(Boolean.FALSE);
								serverLastReadTime.setValue(Long.valueOf(System.currentTimeMillis()));
								request.getStatus().addNumOfBytesSentToClient(serverRead);
								serverReadInProgress.setValue(Boolean.TRUE);
							}
							serverReadInProgress.setValue(Boolean.FALSE);
							clientWriteInProgress.setValue(Boolean.FALSE);
							if(serverRead == -1) {
								break;
							}
						}
					} catch(Throwable e) {
						serverException.setValue(e);
					}
					buf = null;
					serverReadInProgress.setValue(Boolean.FALSE);
					clientWriteInProgress.setValue(Boolean.FALSE);
				}
			}, Thread.currentThread().getName() + "_ProxyThread_ServerToClient");
			serverToClientThread.setDaemon(true);
			serverToClientThread.start();
			//long timeCheck = System.currentTimeMillis();
			while(serverActive) {
				CodeUtil.sleep(10L);
				if(clientException.getValue() != null) {
					throw clientException.getValue();
				}
				if(serverException.getValue() != null) {
					throw serverException.getValue();
				}
				request.getStatus().setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Client MTU: " + clientMTU + "; Server MTU: " + serverMTU + "; Data: Client --> Server: " + Functions.humanReadableByteCount(request.getStatus().getNumOfBytesSentToServer(), true, 2) + "; Server --> Client: " + Functions.humanReadableByteCount(request.getStatus().getNumOfBytesSentToClient(), true, 2));
				long timeElapsedSinceLastClientRead = System.currentTimeMillis() - clientLastReadTime.getValue().longValue();
				long timeElapsedSinceLastServerRead = System.currentTimeMillis() - serverLastReadTime.getValue().longValue();
				final boolean clientTimedOut = timeElapsedSinceLastClientRead >= 30000;
				final boolean serverTimedOut = timeElapsedSinceLastServerRead >= 30000;
				if(clientTimedOut && serverTimedOut) {
					//if(System.currentTimeMillis() - timeCheck >= 30000) {
					final boolean readingFromClient = clientReadInProgress.getValue() == Boolean.TRUE;
					final boolean readingFromServer = serverReadInProgress.getValue() == Boolean.TRUE;
					final boolean writingToClient = clientWriteInProgress.getValue() == Boolean.TRUE;
					final boolean writingToServer = serverWriteInProgress.getValue() == Boolean.TRUE;
					if(writingToClient || writingToServer) {
						//		timeCheck = System.currentTimeMillis();
					} else {
						if(readingFromClient && readingFromServer) {
							PrintUtil.printlnNow("[PROXY] Terminating proxy connection due to inactivity.");
						} else if(!readingFromClient || !readingFromServer) {
							PrintUtil.printlnNow("[PROXY] Terminating potentially dead proxy connection with status:\r\n[PROXY] Reading: From client: " + clientReadInProgress.getValue() + "; From server: " + serverReadInProgress.getValue() + ";\r\n[PROXY] Writing: To client: " + clientWriteInProgress.getValue() + "; To server: " + serverWriteInProgress.getValue() + ";");
						}
						break;
					}
					//}
				} else {
					//timeCheck = System.currentTimeMillis();
				}
				if(clientToServerThread.getState() == State.TERMINATED && serverToClientThread.getState() == State.TERMINATED) {
					break;
				}
				CodeUtil.sleep(200L);
			}
		} else {
			request.getStatus().setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Unable to connect to server: " + result.failReason);
			final HTTPStatusCodes code = result.failReason.toLowerCase().contains("timed out") ? HTTP_504 : HTTP_502;
			PrintUtil.printlnNow("[PROXY] " + request.version + " " + code);
			PrintStream co = new PrintStream(clientOut);
			co.println(request.version + " " + code);
			co.println("Server: " + SERVER_NAME_HEADER);
			co.println("Date: " + StringUtils.getCurrentCacheTime());
			co.println("Cache-Control: " + cachePrivateMustRevalidate);
			co.println("Vary: Accept-Encoding");
			co.println("Content-Type: text/html; charset=UTF-8");
			String response = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"http://" + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
					+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"http://" + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
					+ "\t\t<title>" + code.getName() + " - " + code.getValue() + " - " + SERVER_NAME + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error " + code.getName() + " - " + code.getValue() + "</h1><hr>\r\n"//
					+ "\t\t<string>The proxy was unable to connect to \"" + serverAddress + ":" + serverPort + "\".</string><br>\r\n"//
					+ "\t\t<string>Reason: <b>" + result.failReason + "</b></string>\r\n"//
					+ "\t\t<hr><string><b>" + ipDescription + "</b></string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(request.acceptEncoding.contains("gzip") && response.length() > 33) {
				co.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(response, "UTF-8");
				co.println("Content-Length: " + r.length);
				co.println("");
				co.write(r);
				r = null;
			} else {
				co.println("Content-Length: " + response.length());
				co.println("");
				co.println(response);
			}
		}
		request.getStatus().setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Closing down connections...");
		try {
			client.close();
		} catch(Throwable ignored) {
		}
		try {
			server.close();
		} catch(Throwable ignored) {
		}
		request.getStatus().setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; All done!");
		try {
			request.getStatus().removeFromList();
		} catch(Throwable ignored) {
		}
		return serverAddress;
	}
	
	/** Parses the client's incoming server request, then returns the result.
	 * 
	 * @param s The client
	 * @param in The client's input stream
	 * @param https HTTPS(<code>true</code>) or HTTP(<code>false</code>)
	 * @param reuse Whether or not this is a reused connection
	 * @return The result of parsing the client's request
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ReusedConn)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	protected static final RequestResult HandleRequest(final Socket s, final InputStream in, boolean https, ReusedConn reuse) {
		if(s == null || s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
			//serverAdministrationAuths.remove(Thread.currentThread());
			return new RequestResult(null, ReusedConn.FALSE, null);
		}
		/*if(!reuse) {
			serverAdministrationAuths.put(Thread.currentThread(), new HttpDigestAuthorization(adminAuthorizationRealm, adminUsername, adminPassword));
		}*/
		ClientInfo clientInfo = null;
		DomainDirectory domainDirectory = null;
		String getClientAddress = AddressUtil.getClientAddress((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
		if(isIpBanned(getClientAddress, true)) {
			try {
				s.close();
			} catch(IOException ignored) {
			}
			NaughtyClientData naughty = getBannedClient(getClientAddress);
			println("Kicked client with ip: " + getClientAddress + "\r\n\tReason: " + (naughty != null ? "\"" + naughty.banReason + "\"" : "Unknown(no ban data found!)"));
			return new RequestResult(null, ReusedConn.FALSE, null);
		}
		final ClientConnectTime cct = getClientConnectTimeFor(getClientAddress);
		final boolean connectionSeemsMalicious = cct.seemsMalicious();
		if(connectionSeemsMalicious) {
			incrementWarnCountFor(getClientAddress);
		}
		IOException ioException = null;
		HTTPClientRequest request = null;
		try {
			println((reuse.reuse ? "Reused" : "New") + " Connection: " + getClientAddress);
			if(in.available() < 1) {
				println("\tWaiting on client to send " + (reuse.reuse ? "next " : "") + "request...");
			}
			if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
				println("\t\tIncoming connection from \"" + getClientAddress + "\" was interrupted before the client could send any data.");
				return new RequestResult(null, ReusedConn.FALSE, null);
			}
			PrintUtil.clearLogsBeforeDisplay();
			PrintUtil.clearErrLogsBeforeDisplay();
			request = new HTTPClientRequest(s, in);
			try {
				request.acceptClientRequest(requestTimeout, reuse.reuse);//request = HTTPClientRequest.getClientRequest(s, in, requestTimeout, reuse);//new HTTPClientRequest(s, in);
			} catch(NumberFormatException e) {
				request.markCompleted();
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("No Content-Length Header defined.").setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close").setHeader("Content-Type", "text/plain").setResponse("Error 400: No \"Content-Length:\" header defined.").sendToClient(s, true);
				return new RequestResult(null, reuse, null);
			} catch(UnsupportedEncodingException e) {
				request.markCompleted();
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Unsupported Url Encoding").setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close").setResponse((String) null).sendToClient(s, false);
				return new RequestResult(null, reuse, null);
			} catch(OutOfMemoryError e) {
				request.markCompleted();
				new HTTPServerResponse("HTTP/1.1", HTTP_413, false, StandardCharsets.UTF_8).setStatusMessage("Request data too large").setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close").setResponse((String) null).sendToClient(s, false);
				System.gc();
				return new RequestResult(null, reuse, null);
			} catch(CancellationException e) {
				request.markCompleted();
				new HTTPServerResponse("HTTP/1.1", HTTP_204, false, StandardCharsets.UTF_8).setStatusMessage(e.getMessage()).setHeader("Connection", "close").setResponse((String) null).sendToClient(s, false);
				request.cancel();
				return new RequestResult(null, ReusedConn.FALSE, null);
			} catch(TimeoutException e) {
				request.markCompleted();
				ResponseUtil.send408Response(s, getClientAddress);
				request.cancel();
				return new RequestResult(null, ReusedConn.FALSE, null);//reuse, null);
			} catch(Throwable e) {
				request.markCompleted();
				final String message = e.getMessage();
				if(message != null) {
					if(message.equalsIgnoreCase("Client sent no data.")) {
						ResponseUtil.send408Response(s, getClientAddress);
						request.cancel();
						return new RequestResult(null, ReusedConn.FALSE, null);
					} else if(message.equalsIgnoreCase("Software caused connection abort: recv failed") || message.equalsIgnoreCase("Connection reset")) {
						request.cancel();
						return new RequestResult(null, ReusedConn.FALSE, null);
					}
				}
				new HTTPServerResponse("HTTP/1.1", HTTP_500, false, StandardCharsets.UTF_8).setStatusMessage("An unhandled exception has occurred.").setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close").setHeader("Content-Type", "text/plain").setResponse("Error 500: " + e.getMessage()).sendToClient(s, true);
				e.printStackTrace();
				request.cancel();
				return new RequestResult(null, reuse, null);
			}
			PrintUtil.unclearLogsBeforeDisplay();
			PrintUtil.unclearErrLogsBeforeDisplay();
			final String clientAddress = getClientAddress(request);
			//final String clientIP = getClientIPNoPort(request);//clientAddress == null ? null : clientAddress.substring(0, clientAddress.contains(":") ? clientAddress.lastIndexOf(":") - 1 : clientAddress.length());
			final String reqHost = (request.host.isEmpty() ? request.http2Host : request.host).trim();
			final String host;
			if(!reuse.reuse || (reuse.hostUsed == null || reuse.hostUsed.isEmpty())) {
				host = reqHost;
				if(!reqHost.isEmpty()) {
					println("\tClient connected using host: " + reqHost);
				} else if(request.version.equalsIgnoreCase("HTTP/1.1")) {
					println("\tClient connected using HTTP/1.1, but did not specify a host!");
				}
			} else {
				if(!reqHost.isEmpty() && !reuse.hostUsed.equalsIgnoreCase(reqHost)) {
					println("\tClient reused existing connection, but used a different host(weird): " + reqHost);
					host = reqHost;
				} else if(reqHost.isEmpty() && !reuse.hostUsed.isEmpty()) {
					println("\tClient originally connected using host: \"" + reuse.hostUsed + "\"; but did not send a host header this time.");
					host = reuse.hostUsed;
				} else {
					host = reqHost;
				}
			}
			reuse.hostUsed = host;
			if(!request.requestLogs.isEmpty()) {
				JavaWebServer.print(request.requestLogs);
			}
			if(request.isProxyRequest) {
				if(enableProxyServer) {
					request.getStatus().removeFromList();
					final HTTPClientRequest req = request;
					Thread proxyThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String serverAddress = handleProxyRequest(s, in, s.getOutputStream(), req, req.method.equalsIgnoreCase("CONNECT"));
								PrintUtil.printlnNow("[PROXY] Successfully handled proxy request between client \"" + clientAddress + "\" and server \"" + serverAddress + "\".");
								PrintUtil.printErrToConsole();
							} catch(Throwable e) {
								PrintUtil.printToConsole();
								PrintUtil.printErrlnNow("[PROXY] Failed to handle proxy request for client \"" + clientAddress + "\": " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
							}
							req.markCompleted();
							sockets.remove(s);
						}
					}, Thread.currentThread().getName().replace("Server", "Proxy"));
					PrintUtil.moveLogsToThreadAndClear(proxyThread);
					PrintUtil.moveErrLogsToThreadAndClear(proxyThread);
					proxyThread.setDaemon(true);
					proxyThread.start();
				} else {
					final boolean isMethodCONNECT = request.method.equalsIgnoreCase("CONNECT");
					HTTPServerResponse response = new HTTPServerResponse("HTTP/1.1", HTTP_421/*HTTP_500*/, request.acceptEncoding.toLowerCase().contains("gzip"), StandardCharsets.UTF_8);
					final String pageHeader = "<hr><b>" + request.requestedFilePath + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					if(isMethodCONNECT) {
						response.setStatusCode(HTTP_405);
						response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", cachePrivateMustRevalidate);
						response.setHeader("Date", StringUtil.getCurrentCacheTime());
						response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"http://" + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"http://" + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
								+ "\t\t<title>405 - Method Not Allowed - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 405 - Method Not Allowed</h1>\r\n"//
								+ "\t\t<string>Your client's request was a valid proxy request, but this server's proxy functionality is not active.</string>\r\n"//
								+ "\t</body>\r\n</html>");
						response.sendToClient(s, true);
					} else {
						ResponseUtil.send421Response(s, request, response, clientInfo, domainDirectory, pageHeader);
						/*response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", cachePrivateMustRevalidate);
						response.setHeader("Date", StringUtil.getCurrentCacheTime());
						response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"http://" + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
								+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"http://" + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
								+ "\t\t<title>500 - Internal Server Error - " + SERVER_NAME + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 500 - Internal Server Error</h1>\r\n"//
								+ "\t\t<string>Your client's request was a valid proxy request, but this server's proxy functionality is not active.</string>\r\n"//
								+ "\t</body>\r\n</html>");
						response.sendToClient(s, true);*/
					}
				}
				return new RequestResult(request, ReusedConn.FALSE, null);
			}
			boolean hostDefined = !host.isEmpty();
			request.host = AddressUtil.getValidHostFor(host);
			if(!hostDefined && request.version.equalsIgnoreCase("HTTP/1.1")) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("No host header defined: \"" + host + "\"").setResponse((String) null).sendToClient(s, false);
				return new RequestResult(request, reuse, null);
			}
			if(request.method.isEmpty()) {
				new HTTPServerResponse("HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Bad protocol request: \"" + request.protocolRequest + "\"").setResponse((String) null).sendToClient(s, false);
				if(!HTTPClientRequest.debug) {
					PrintUtil.clearLogs();
				}
				return new RequestResult(request, reuse, null);
			}
			reuse.reuse = request.connectionSetting.toLowerCase().contains("keep-alive");
			if(request.connectionSetting.toLowerCase().contains("close")) {
				reuse.reuse = false;
			}
			s.setKeepAlive(reuse.reuse);
			
			if(request.method.equalsIgnoreCase("brew")) {
				HTTPServerResponse response = new HTTPServerResponse(request.version, HTTP_501, request.acceptEncoding.toLowerCase().contains("gzip"), StandardCharsets.UTF_8).setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close");
				if(request.version.toUpperCase().startsWith("HTCPCP/")) {
					response.setStatusCode(HTTP_418).setStatusMessage("Making coffee is okay.").setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your coffee is ready!\r\nDon't drink too much in a day.\r\n").sendToClient(s, true);
				} else {
					response.setStatusCode(HTTP_418).setStatusMessage("Making tea is fun!").setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your tea is ready! Enjoy.\r\n").sendToClient(s, true);
				}
				return new RequestResult(request, reuse, null);
			}
			
			final boolean domainIsLocalHost = AddressUtil.isDomainLocalHost(request.host);
			final DomainDirectory testDomain = DomainDirectory.getDomainDirectoryFromDomainName(request.host);
			if(testDomain != null || domainIsLocalHost) {
				domainDirectory = DomainDirectory.getOrCreateDomainDirectory(request.host);
			} else {
				domainDirectory = DomainDirectory.getTemporaryDomain();
				domainDirectory.domain.setValue(request.host);
				domainDirectory.displayName.setValue(request.host);
			}
			if(!domainDirectory.getDisplayLogEntries()) {
				PrintUtil.clearLogsBeforeDisplay();
				PrintUtil.clearErrLogsBeforeDisplay();
			}
			File homeDirectory = domainDirectory.getDirectory();
			if(homeDirectory == null || !homeDirectory.exists() || !homeDirectory.isDirectory()) {
				homeDirectory = JavaWebServer.homeDirectory;
				println("\t\t /!\\ Unable to get home directory for domain\r\n\t\t/___\\\t\"" + request.host + "\"; using home directory as specified in the '" + optionsFileName + "' file.");
			} else {
				println("\t\tHome directory for domain \"" + request.host + "\" is: \"" + homeDirectory.getAbsolutePath() + "\"!");
			}
			final HTTPServerResponse response = new HTTPServerResponse(request.version, HTTP_501, request.acceptEncoding.toLowerCase().contains("gzip") && domainDirectory.getEnableGZipCompression(), StandardCharsets.UTF_8).setHeader("Keep-Alive", "timeout=30").setHeader("Connection", reuse.reuse ? "keep-alive" : "close");
			response.setDomainDirectory(domainDirectory);
			
			if(!request.protocolRequest.isEmpty()) {
				final File requestedFile = domainDirectory.getFileFromRequest(request.requestedFilePath, request.requestArguments);//requestedFile = new File(homeDirectory, htmlToText(domainDirectory.replaceAliasWithPath(arg)));//arg
				if(ForumClientResponder.HandleRequest(s, domainDirectory, request, response)) {
					return new RequestResult(request, reuse, null);
				}
				if(request.method.equalsIgnoreCase("OPTIONS")) {
					final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = requestedFile != null ? requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/') : request.requestedFilePath;
					path = (path.trim().isEmpty() ? "/" : path);
					path = (path.startsWith("/") ? "" : "/") + path;
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					
					if(((requestedFile != null && requestedFile.exists()) || request.requestedFilePath.equals("*")) && (testDomain != null || domainIsLocalHost)) {
						if(requestedFile != null) {
							if(domainDirectory.areDirectoriesForbidden() && requestedFile.isDirectory()) {
								response.setStatusCode(HTTP_403);
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								response.setResponse("<!DOCTYPE html>\r\n"//
										+ "<html>\r\n\t<head>\r\n"//
										+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
										+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
										+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
										+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
										+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
										+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
										+ "\t</head>\r\n"//
										+ "\t<body>\r\n"//
										+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
										+ "\t\t<string>" + pageHeader + "</string>\r\n"//
										+ "\t</body>\r\n</html>");
								response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
								connectedClients.remove(clientInfo);
								return new RequestResult(request, reuse, null);
							}
							//final FileInfo info = new FileInfo(requestedFile, domainDirectory);
							println("\t\tRequested file: \"" + requestedFile.getAbsolutePath() + "\"");
							if(RestrictedFile.isFileRestricted(requestedFile, s.getInetAddress().getHostAddress())) {
								RestrictedFile restrictedFile = RestrictedFile.getRestrictedFile(requestedFile);
								if(restrictedFile != null) {
									if(restrictedFile.isPasswordProtected()) {
										String clientResponse;
										try {
											clientResponse = new String(Base64.getDecoder().decode(request.authorization.replace("Basic", "").trim()));
										} catch(IllegalArgumentException ignored) {
											clientResponse = "";
										}
										String[] creds = clientResponse.split(":");
										String clientUser = creds.length == 2 ? creds[0] : "";
										String clientPass = creds.length == 2 ? creds[1] : "";
										if(!restrictedFile.areCredentialsValid(clientUser, clientPass)) {
											response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "\r\n\t\t*** (client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
											response.setHeader("Content-Type", "text/html; charset=UTF-8");
											response.setHeader("Server", SERVER_NAME_HEADER);
											response.setHeader("Cache-Control", cachePrivateMustRevalidate);
											response.setHeader("Date", StringUtils.getCurrentCacheTime());
											response.setHeader("WWW-Authenticate", "Basic realm=\"" + restrictedFile.getAuthorizationRealm() + "\"");
											response.setResponse("<!DOCTYPE html>\r\n"//
													+ "<html>\r\n\t<head>\r\n"//
													+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
													+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
													+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
													+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
													+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
													+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
													+ "\t</head>\r\n"//
													+ "\t<body>\r\n"//
													+ "\t\t<h1>Error 401 - Authorization Required</h1>\r\n"//
													+ "\t\t<string>" + pageHeader + "</string>\r\n"//
													+ "\t</body>\r\n</html>");
											response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
											connectedClients.remove(clientInfo);
											return new RequestResult(request, reuse, null);
										}
									} else {
										response.setStatusCode(HTTP_403);
										response.setHeader("Content-Type", "text/html; charset=UTF-8");
										response.setHeader("Server", SERVER_NAME_HEADER);
										response.setHeader("Cache-Control", cachePrivateMustRevalidate);
										response.setResponse("<!DOCTYPE html>\r\n"//
												+ "<html>\r\n\t<head>\r\n"//
												+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
												+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
												+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
												+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
												+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
												+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
												+ "\t</head>\r\n"//
												+ "\t<body>\r\n"//
												+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
												+ "\t\t<string>" + pageHeader + "</string>\r\n"//
												+ "\t</body>\r\n</html>");
										response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
										connectedClients.remove(clientInfo);
										return new RequestResult(request, reuse, null);
									}
								}
							}
						}
						response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setHeader("Allow", "GET,HEAD," + (request.requestedFilePath.equals("*") ? "POST," : "") + "CONNECT,OPTIONS");//"GET,HEAD,POST,OPTIONS");
						response.setHeader("Content-Type", "text/plain; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Content-Length", "0");
						response.setResponse((String) null);//TODO Maybe find out what content is sent with the OPTIONS method(Like 'httpd/unix-directory')?
						response.sendToClient(s, false);
						connectedClients.remove(clientInfo);
						return new RequestResult(request, reuse, null);
					} else if(testDomain == null && !domainIsLocalHost) {
						ResponseUtil.send421Response(s, request, response, clientInfo, domainDirectory, pageHeader);
						return new RequestResult(request, reuse, null);
					}
					ResponseUtil.send404Response(s, request, response, clientInfo, domainDirectory, pageHeader);
					return new RequestResult(request, reuse, null);
				} else if(request.method.equalsIgnoreCase("GET") || request.method.equalsIgnoreCase("HEAD")) {
					final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = requestedFile != null ? requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/') : request.requestedFilePath;
					path = (path.trim().isEmpty() ? "/" : path);
					path = (path.startsWith("/") ? "" : "/") + path;
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					String administrateFileCheck = request.requestArguments.get("administrateFile");
					final boolean administrateFile = administrateFileCheck != null ? (administrateFileCheck.equals("1") || administrateFileCheck.equalsIgnoreCase("true")) : false;
					
					if(requestedFile != null && requestedFile.exists() && (testDomain != null || domainIsLocalHost)) {
						if(domainDirectory.areDirectoriesForbidden() && requestedFile.isDirectory() && !administrateFile && !requestedFile.equals(domainDirectory.getDirectory())) {
							response.setStatusCode(HTTP_403);
							response.setHeader("Content-Type", "text/html; charset=UTF-8");
							response.setHeader("Server", SERVER_NAME_HEADER);
							response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							response.setResponse("<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ "\t</head>\r\n"//
									+ "\t<body>\r\n"//
									+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
							connectedClients.remove(clientInfo);
							return new RequestResult(request, reuse, null);
						}
						final FileInfo info = new FileInfo(requestedFile, domainDirectory);
						println("\t\tRequested file: \"" + requestedFile.getAbsolutePath() + "\"");
						String layout = domainDirectory.getDefaultStylesheet();
						String favicon = domainDirectory.getDefaultPageIcon();
						layout = layout.startsWith("/") ? layout : "/" + layout;
						favicon = favicon.startsWith("/") ? favicon : "/" + favicon;
						boolean isExempt = administrateFile || request.requestedFilePath.equalsIgnoreCase(layout) || request.requestedFilePath.equalsIgnoreCase(favicon);
						
						if(RestrictedFile.isFileRestricted(requestedFile, s.getInetAddress().getHostAddress()) && !isExempt) {
							printlnDebug("layout: \"" + layout + "\"; favicon: \"" + favicon + "\";\r\nrequestedFilePath: \"" + request.requestedFilePath + "\";");
							RestrictedFile restrictedFile = RestrictedFile.getRestrictedFile(requestedFile);
							if(restrictedFile != null) {
								if(restrictedFile.isPasswordProtected()) {
									String clientResponse;
									try {
										clientResponse = new String(Base64.getDecoder().decode(request.authorization.replace("Basic", "").trim()));
									} catch(IllegalArgumentException ignored) {
										clientResponse = "";
									}
									String[] creds = clientResponse.split(":");
									String clientUser = creds.length == 2 ? creds[0] : "";
									String clientPass = creds.length == 2 ? creds[1] : "";
									if(!restrictedFile.areCredentialsValid(clientUser, clientPass)) {
										response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "\r\n\t\t*** (client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
										response.setHeader("Content-Type", "text/html; charset=UTF-8");
										response.setHeader("Server", SERVER_NAME_HEADER);
										response.setHeader("Cache-Control", cachePrivateMustRevalidate);
										response.setHeader("Date", StringUtils.getCurrentCacheTime());
										response.setHeader("WWW-Authenticate", "Basic realm=\"" + restrictedFile.getAuthorizationRealm() + "\"");
										response.setResponse("<!DOCTYPE html>\r\n"//
												+ "<html>\r\n\t<head>\r\n"//
												+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
												+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
												+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
												+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
												+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
												+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
												+ "\t</head>\r\n"//
												+ "\t<body>\r\n"//
												+ "\t\t<h1>Error 401 - Authorization Required</h1>\r\n"//
												+ "\t\t<string>" + pageHeader + "</string>\r\n"//
												+ "\t</body>\r\n</html>");
										response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
										connectedClients.remove(clientInfo);
										return new RequestResult(request, reuse, null);
									}
								} else {
									response.setStatusCode(HTTP_403);
									response.setHeader("Content-Type", "text/html; charset=UTF-8");
									response.setHeader("Server", SERVER_NAME_HEADER);
									response.setHeader("Cache-Control", cachePrivateMustRevalidate);
									response.setResponse("<!DOCTYPE html>\r\n"//
											+ "<html>\r\n\t<head>\r\n"//
											+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
											+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
											+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
											+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
											+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
											+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
											+ "\t</head>\r\n"//
											+ "\t<body>\r\n"//
											+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
											+ "\t\t<string>" + pageHeader + "</string>\r\n"//
											+ "\t</body>\r\n</html>");
									response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
									connectedClients.remove(clientInfo);
									return new RequestResult(request, reuse, null);
								}
							}
						}
						clientInfo = new ClientInfo(s, info, request);
						IOException exception = null;
						try {
							serveFileToClient(s, reuse.reuse, https, s.getOutputStream(), response, clientInfo, domainDirectory, request);
						} catch(IOException e) {
							reuse.reuse = false;
							exception = e;
						}
						connectedClients.remove(clientInfo);
						if(!domainDirectory.getDisplayLogEntries()) {
							PrintUtil.clearLogs();
							PrintUtil.clearErrLogs();
						}
						return new RequestResult(request, reuse, exception);
					} else if(testDomain == null && !domainIsLocalHost) {
						ResponseUtil.send421Response(s, request, response, clientInfo, domainDirectory, pageHeader);
						return new RequestResult(request, reuse, null);
					}
					ResponseUtil.send404Response(s, request, response, clientInfo, domainDirectory, pageHeader);
					return new RequestResult(request, reuse, null);
					/*out.println("HTTP/1.0 200");//XXX Working example; keep this here for reference and debugging!
					out.println("Vary: Accept-Encoding");
					out.println("Content-Type: text/html; charset=UTF-8");
					out.println("Server: " + SERVER_NAME_HEADER);
					out.println("Date: " + getCurrentCacheTime());
					out.println("Cache-Control: " + cachePrivateMustRevalidate);
					String response = "<!DOCTYPE html>\r\n<html>\r\n<head>\r\n<title>My Web Server</title>\r\n</head>\r\n<body>\r\n<h1>Welcome to my Web Server!</h1>\r\n\tYour client sent the following data: \"" + request + "\"\r\n\t</body>\r\n</html>";
					out.println("Content-Length: " + response.length());
					out.println("");
					out.println(response);
					out.flush();
					out.close();
					s.close();
					return;*/
				} else if(request.method.equalsIgnoreCase("POST")) {
					final String pathPrefix = FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath() + File.separatorChar));
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = requestedFile != null ? requestedFile.getAbsolutePath().replace(pathPrefix, "").replace('\\', '/') : request.requestedFilePath;
					path = (path.trim().isEmpty() ? "/" : path);
					path = (path.startsWith("/") ? "" : "/") + path;
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					
					if((requestedFile == null || !requestedFile.exists())) {
						response.setStatusCode(HTTP_404);
						response.setHeader("Vary", "Accept-Encoding");
						response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", "no-cache, must-revalidate");
						response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
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
						response.sendToClient(s, true);
						connectedClients.remove(clientInfo);
						return new RequestResult(request, reuse, null);
					}
					if(request.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
						PhpResult phpResponse = PhpResult.execPHP(requestedFile.getAbsolutePath(), request.formURLEncodedData.postRequestStr.replace("?", "").replace("&", " "), true, false);
						for(String header : phpResponse.headers.split("\r\n")) {
							String[] split = header.split("\\: ");
							if(split.length == 2) {
								response.setHeader(split[0], split[1]);
							}
						}
						response.setHeader("Server", SERVER_NAME_HEADER);
						response.setHeader("Date", StringUtils.getCurrentCacheTime());
						response.setResponse(phpResponse.body);
						response.sendToClient(s, true);
					} else if(request.contentType.toLowerCase().contains("multipart/form-data")) {//XXX multipart/form-data
						if(request.multiPartFormData == null) {
							//HttpDigestAuthorization serverAdministrationAuth = getOrCreateAuthForCurrentThread();
							final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(request, false);//AuthorizationResult authResult = serverAdministrationAuth.authenticate(request.authorization, new String(request.postRequestData), request.protocol, request.host, clientIP, request.cookies);
							if(!authResult.passed()) {
								response.setStatusCode(HTTP_401);//.setStatusMessage(clientResponse.isEmpty() ? "" : "\r\n\t\t*** (client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								response.setHeader("Date", StringUtils.getCurrentCacheTime());
								response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
								if(authResult.authorizedCookie != null) {
									response.setHeader("Set-Cookie", authResult.authorizedCookie);
								}
								response.setResponse("<!DOCTYPE html>\r\n"//
										+ "<html>\r\n\t<head>\r\n"//
										+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
										+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
										+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
										+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
										+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
										+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
										+ "\t</head>\r\n"//
										+ "\t<body>\r\n"//
										+ "\t\t<h1>Error 401 - Authorization Required</h1><hr>\r\n"//
										+ "\t\t<string title=\"In order to be able to upload files to this server, you must be able to administrate it.\">You need permission to do that.</string>\r\n"//
										+ "\t\t<string>" + pageHeader + "</string>\r\n"//
										+ "\t</body>\r\n</html>");
								response.sendToClient(s, true);
								connectedClients.remove(clientInfo);
								return new RequestResult(request, reuse, null);
							}
							if(authResult.authorizedCookie != null) {
								response.setHeader("Set-Cookie", authResult.authorizedCookie);
							}
						} else if(request.multiPartFormData != null) {
							if(!request.multiPartFormData.fileData.isEmpty()) {
								if(!domainDirectory.getEnableFileUpload()) {
									response.setStatusCode(HTTP_204);
									response.setHeader("Vary", "Accept-Encoding");
									response.setHeader("Content-Type", "text/html; charset=UTF-8");
									response.setHeader("Server", SERVER_NAME_HEADER);
									response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
									response.setHeader("Content-Length", "0");
									response.setResponse((String) null);
									response.sendToClient(s, false);
									request.multiPartFormData.close();
									request.multiPartFormData = null;
									System.gc();
									return new RequestResult(request, reuse, null);
								}
								if(requestedFile.isDirectory()) {
									for(FileData data : request.multiPartFormData.fileData) {
										try {
											data.writeFileToFolder(requestedFile);
										} catch(IOException e) {
											PrintUtil.printThrowable(e);
										}
									}
								}
								response.setStatusCode(HTTP_303);
								response.setStatusMessage("Client posted " + request.multiPartFormData.fileData.size() + " file" + (request.multiPartFormData.fileData.size() == 1 ? "" : "s") + " in the requested folder.");
								response.setHeader("Vary", "Accept-Encoding");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
								response.setHeader("Location", /*"http://" + domainDirectory.getDomain() + */request.requestedFilePath/**/+ "?administrateFile=1"/**/+ (request.multiPartFormData.fileData.size() >= 1 ? "#" + request.multiPartFormData.fileData.get(0).fileName : ""));
								response.setHeader("Content-Length", "0");
								response.setResponse((String) null);
								response.sendToClient(s, false);
							} else {
								response.setStatusCode(HTTP_204);
								response.setHeader("Vary", "Accept-Encoding");
								response.setHeader("Content-Type", "text/html; charset=UTF-8");
								response.setHeader("Server", SERVER_NAME_HEADER);
								response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
								response.setHeader("Content-Length", "0");
								response.setResponse((String) null);
								response.sendToClient(s, false);
							}
							if(HTTPClientRequest.debug) {
								int i = 0;
								for(Entry<String, String> entry : request.multiPartFormData.formData.entrySet()) {
									PrintUtil.printlnNow("[" + i + "]: " + entry.getKey() + " = " + entry.getValue() + ";");
									i++;
								}
							}
							request.multiPartFormData.close();
							request.multiPartFormData = null;
							System.gc();
						} else {
							response.setStatusCode(HTTP_501);
							response.setHeader("Vary", "Accept-Encoding");
							response.setHeader("Content-Type", "text/html; charset=UTF-8");
							response.setHeader("Server", SERVER_NAME_HEADER);
							response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
							response.setHeader("Content-Length", "0");
							response.setResponse((String) null);
							response.sendToClient(s, false);
						}
					}
					connectedClients.remove(clientInfo);
					return new RequestResult(request, reuse, null);
				}
				
				String path = request.requestedFilePath;
				path = (path.trim().isEmpty() ? "/" : path);
				path = (path.startsWith("/") ? "" : "/") + path;
				final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
				
				response.setStatusCode(HTTP_501);
				response.setHeader("Vary", "Accept-Encoding");
				response.setHeader("Content-Type", "text/html; charset=UTF-8");
				response.setHeader("Server", SERVER_NAME_HEADER);
				response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
				response.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>501 - Feature not Implemented - " + domainDirectory.getServerName() + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
						+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Error 501 - Not Implemented</h1><hr>\r\n"//
						+ "\t\t<string>The requested HTML method(\"" + request.method + "\") has not yet been implemented.</string>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				response.sendToClient(s, true);
				connectedClients.remove(clientInfo);
				return new RequestResult(request, reuse, null);
			}
			response.setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + request.protocolRequest + "\"").setResponse((String) null).sendToClient(s, false);
			connectedClients.remove(clientInfo);
			if(!domainDirectory.getDisplayLogEntries()) {
				PrintUtil.clearLogs();
				PrintUtil.clearErrLogs();
			}
			return new RequestResult(request, reuse, null);
		} catch(IOException e) {
			ioException = e;
			final String errMsg = e.getMessage();
			if(errMsg != null && (errMsg.equalsIgnoreCase("Socket Closed") || errMsg.equalsIgnoreCase("Client sent no data.") || errMsg.equalsIgnoreCase("Software caused connection abort: recv failed"))) {
				PrintUtil.clearLogs();
				PrintUtil.clearErrLogs();
				connectedClients.remove(clientInfo);
				return new RequestResult(request, ReusedConn.FALSE, ioException);
			}
			if(HTTPClientRequest.debug) {
				println("\t /!\\\tFailed to respond to client request: \"" + getClientAddress + "\"\r\n\t/___\\\tCause: " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
			} else {
				PrintUtil.clearLogs();
				PrintUtil.clearErrLogs();
			}
		}
		if(domainDirectory != null && !domainDirectory.getDisplayLogEntries()) {
			PrintUtil.clearLogs();
			PrintUtil.clearErrLogs();
		}
		connectedClients.remove(clientInfo);
		return new RequestResult(request, ReusedConn.FALSE, ioException);
	}
	
}
