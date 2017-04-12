package com.gmail.br45entei;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_102;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_1337;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_200;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_200_1;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_204;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_206;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_303;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_304;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_308;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_400;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_401;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_403;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_404;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_405;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_407;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_409;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_416;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_418;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_429;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_499;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_500;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_501;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_502;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_503;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_504;

import com.gmail.br45entei.data.BasicAuthorizationResult;
import com.gmail.br45entei.data.serverIO.InputStreamSSLWrapper;
import com.gmail.br45entei.data.serverIO.SocketWrapper;
import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.media.MediaInfo;
import com.gmail.br45entei.media.MediaReader;
import com.gmail.br45entei.media.MediaReader.MediaArtwork;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.HTTPStatusCodes;
import com.gmail.br45entei.server.Header;
import com.gmail.br45entei.server.RangeRequest;
import com.gmail.br45entei.server.RequestResult;
import com.gmail.br45entei.server.SocketConnectResult;
import com.gmail.br45entei.server.autoban.AutobanDictionary;
import com.gmail.br45entei.server.data.ClientConnectTime;
import com.gmail.br45entei.server.data.Credential;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileDataUtil;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.Property;
import com.gmail.br45entei.server.data.RestrictedFile;
import com.gmail.br45entei.server.data.file.FileData;
import com.gmail.br45entei.server.data.file.FolderData;
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
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.MapUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.ResponseUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;
import com.gmail.br45entei.util.ThreadUtils;
import com.gmail.br45entei.util.ThreadUtils.ExecutorGroup;
import com.gmail.br45entei.util.ThreadedSecureRandom;
import com.gmail.br45entei.util.exception.CancelledRequestException;
import com.gmail.br45entei.util.exception.TimeoutException;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

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
import java.lang.Thread.State;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/** The main Server class */
public final class JavaWebServer {//TODO Implement per-folder background color/images/textures with configurable settings(image repeats, size, etc.) based on pure HTML
	//TODO Implement per-folder default files, like with domains(configurable on the folder administration page)
	//TODO Go through and read all of the old TODO's and actually do them you lazy bum <--
	//TODO http://www.singular.co.nz/2008/07/finding-preferred-accept-encoding-header-in-csharp/ --> Meaning, stop using this: reuse.request.acceptEncoding.toLowerCase().contains("gzip") ... and actually parse the accept encoding header.
	//TODO Implement all or most of the id3v2.3.0 media tags from http://id3.org/id3v2.3.0 and the relevant ones from https://code.google.com/p/mp4v2/wiki/iTunesMetadata
	
	protected final Thread sslThread;
	protected final Thread adminThread;
	/** Whether or not the SSL thread pool is enabled. Changing this value will
	 * require restarting the server for changes to take effect. */
	public static volatile boolean enableSSLThread = false;
	/** Whether or not the Administration Interface is enabled for this server.
	 * Changing this value will require restarting the server for changes to
	 * take effect. */
	public static volatile boolean enableAdminInterface = true;
	
	/** If the type of certificate is a "key" store, set this to true.
	 * Otherwise,
	 * set to false to use a "trust" store certificate. */
	public static volatile boolean sslStore_KeyOrTrust = true;
	/** The absolute path to the SSL store file(SSL certificate) */
	public static volatile String storePath = "";
	/** The SSL store password(SSL certificate password) */
	public static volatile String storePassword = "";
	
	public static final String ephemeralDHKeySize = "2048";
	
	public static final String enabledTLSProtocols = "TLSv1,TLSv1.1,TLSv1.2";//,TLSv1.3";//enabling 1.3 ahead of time yields: javax.net.ssl.SSLException: Received fatal alert: protocol_version
	
	public static final String TLS_DisabledAlgorithms = "MD5, SHA1, DSA, DH, EDH, DHE, RC4, RSA keySize < 4096";//TODO See if "DH, DHE, EDH" even do anything. I have a feeling that they don't... however, it's working like this, so I'm afraid to try XD
	
	/** The enabled TLS protocols */
	public static final String[] TLSProtocols = new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"};//, "TLSv1.3"};//enabling this ahead of time yields an illegal argument exception!
	
	/*public static final void main(String[] args) {
		System.out.println(StringUtil.getCacheTime(System.currentTimeMillis() - (60000 * 6)));
	}*/
	
	/** Whether or not byte range requests are supported for normal file
	 * requests */
	public static boolean allowByteRangeRequests = true;
	
	public static volatile boolean httpResponseLogsIncluded = true;
	public static volatile boolean httpsResponseLogsIncluded = true;
	public static volatile boolean adminResponseLogsIncluded = false;
	
	private static final String shutdownStr = "rlXmsns_" + ThreadedSecureRandom.nextSessionId(); //UUID.randomUUID().toString();
	
	/** The name of this application. */
	public static final String APPLICATION_NAME = "JavaWebServer";
	/** The major version of this application. */
	public static final String APPLICATION_VERSION_MAJOR = "1.0";
	
	/** The minor version of this application. */
	public static final String APPLICATION_VERSION_MINOR = "50";
	
	/** This application's version. */
	public static final String APPLICATION_VERSION = JavaWebServer.APPLICATION_VERSION_MAJOR + "_" + JavaWebServer.APPLICATION_VERSION_MINOR;
	
	/** The year in which this software is declared copyrighted. */
	public static final String COPYRIGHT_YEAR = "2016";
	/** The author of this server software. */
	public static final String APPLICATION_AUTHOR = "Brian_Entei";
	
	private static final String COPYRIGHT_SYMBOL = new String("\u00A9".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	
	/** The legal notice that is printed to the console. */
	public static final String TERMINAL_NOTICE = new String((JavaWebServer.APPLICATION_NAME + " v." + JavaWebServer.APPLICATION_VERSION + " Copyright " + COPYRIGHT_SYMBOL + " " + JavaWebServer.COPYRIGHT_YEAR + " " + JavaWebServer.APPLICATION_AUTHOR + "\nThis program comes with ABSOLUTELY NO WARRANTY.\nThis is free software, and you are welcome to redistribute it.\n\nFor help with command usage, type 'help' and press enter.\n").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	
	private static volatile JavaWebServer instance;
	private final Thread arrayListCleanupThread;
	
	/** The prefix of the server thread pool threads */
	public static final String ThreadName = "ServerThread";
	/** The prefix of the SSL thread pool threads */
	public static final String SSLThreadName = "SSLServerThread";
	/** The prefix of the Administration thread pool threads */
	public static final String AdminThreadName = "ServerAdminThread";
	
	public static final ExecutorGroup fThreadExecutor = new ExecutorGroup(ThreadName + "_", "_", ThreadUtils.getDefaultThreadGroup());
	public static final ExecutorGroup fSSLThreadExecutor = new ExecutorGroup(SSLThreadName + "_", "_", ThreadUtils.getDefaultThreadGroup());
	public static final ExecutorGroup fAdminThreadExecutor = new ExecutorGroup(AdminThreadName + "_", "_", ThreadUtils.getDefaultThreadGroup());
	
	/** The recommended number of normal server threads, or the number of cores
	 * that the host machine has multiplied by 1000.<br>
	 * For example, a server with 4 cores would get 4000 recommended server
	 * threads. This can be changed in the options menu UI or by setting
	 * {@link #overrideThreadPoolSize} and then running
	 * {@link #updateThreadPoolSizes()}. */
	public static final int fNumberOfThreads = (1000 * Runtime.getRuntime().availableProcessors());
	private static final ThreadPoolExecutor fThreadPool = fThreadExecutor.getExecutor(fNumberOfThreads);//((ThreadPoolExecutor) Executors.newFixedThreadPool(fNumberOfThreads/*, namedThreadFactory*/));
	protected static final ThreadPoolExecutor fSSLThreadPool = fSSLThreadExecutor.getExecutor(fNumberOfThreads);//((ThreadPoolExecutor) Executors.newFixedThreadPool(fNumberOfThreads));
	protected static final ExecutorService fAdminThreadPool = fAdminThreadExecutor.getExecutor(20);//Executors.newFixedThreadPool(20);
	
	protected static volatile boolean serverActive = true;
	
	public static final boolean isRunning() {
		return serverActive;
	}
	
	/** The name of this server that will be used in normal client requests and
	 * administration requests */
	public static final String SERVER_NAME = "Entei_Server";
	/** The name of this server that will be used when clients issue a proxy
	 * request(only if the proxy functionality is enabled, otherwise the request
	 * is rejected or marked as a mis-directed request(HTTP 421) */
	public static final String PROXY_SERVER_NAME = "entei";
	/** The default "Server: " header that is sent to clients
	 * 
	 * @see DomainDirectory#getServerName() */
	public static final String SERVER_NAME_HEADER = SERVER_NAME + "/" + APPLICATION_VERSION;
	/** The default file name that will be served to the client when it requests
	 * the home directory(usually just "/", or sometimes "/htdocs").
	 * 
	 * @see DomainDirectory#getDefaultFileName() */
	public static final String DEFAULT_FILE_NAME = "index.html";
	
	/** The server-wide default page icon(usually a .ico or .png)
	 * 
	 * @see DomainDirectory#getDefaultPageIcon() */
	public static final String DEFAULT_PAGE_ICON = "/favicon.ico";
	/** The server-wide default stylesheet(CSS)
	 * 
	 * @see DomainDirectory#getDefaultStylesheet()
	 * @see DomainDirectory#getDefaultStyleSheetFromFileSystem() */
	public static final String DEFAULT_STYLESHEET = "/layout.css";
	
	/** The current working directory as depicted by
	 * {@code System.getProperty("user.dir")} */
	public static final File rootDir = new File(System.getProperty("user.dir"));
	
	/** The name of the general server options file */
	public static final String optionsFileName = "options.txt";
	/** The name of the HTTPS(SSL) options file */
	public static final String sslOptionsFileName = "sslOptions.txt";
	/** The name of the Administration options file */
	public static final String adminOptionsFileName = "adminOptions.txt";
	/** The name of the Proxy options file */
	public static final String proxyOptionsFileName = "proxyOptions.txt";
	
	/** The concurrent list of all currently connected sockets(clients). */
	public static final ConcurrentLinkedQueue<ClientConnection> sockets = new ConcurrentLinkedQueue<>();
	//private static final ConcurrentLinkedQueue<String>				sessionIDs						= new ConcurrentLinkedQueue<>();
	
	public static final boolean isConnected(final Socket socket) {
		purgeDeadClientIpConnectionsFromSocketList();
		if(socket != null) {
			for(ClientConnection connection : sockets) {
				if(connection.equals(socket)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** The name of the folder in which banned client data is stored locally. */
	public static final String sinBinFolderName = "SinBin";
	/** The concurrent list of banned clients, or "SinBin" */
	public static final ConcurrentLinkedQueue<NaughtyClientData> sinBin = new ConcurrentLinkedQueue<>();
	private static Thread sinBinUpkeepThread;
	
	/** The default realm used when creating a new restricted file or page. */
	public static final String DEFAULT_AUTHORIZATION_REALM = "Forbidden File(s)";
	/** The default username used when creating a new restricted file or
	 * page. */
	public static final String DEFAULT_AUTHORIZATION_USERNAME = "username";
	/** The default password used when creating a new restricted file or
	 * page. */
	public static final String DEFAULT_AUTHORIZATION_PASSWORD = "";
	
	//=======================
	
	/** Cache-Control header that tells the client that it is viewing a private
	 * page and that it shouldn't cache it. */
	public static final String cachePrivateMustRevalidate = "private, max-age=0, no-cache, must-revalidate";
	
	//=======================
	
	/** The CSS table style that will be used in generated directory pages. */
	public static final String HTML_TABLE_STYLE = "table {\r\n" + //
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
	public static final String AUTO_RESIZE_JAVASCRIPT = "<script type='text/javascript'>\r\n" + //
			"function autoResize(id) {\r\n" + //
			"\tvar newheight;\r\n" + //
			"\tvar newwidth;\r\n" + //
			"\tif(document.getElementById) {\r\n" + //
			"\t\tnewheight=document.getElementById(id).contentWindow.document.body.scrollHeight;\r\n" + //
			"\t\tnewwidth=document.getElementById(id).contentWindow.document.body.scrollWidth;\r\n" + //
			"\t}\r\n" + //
			"\tdocument.getElementById(id).height= (newheight) + \"px\";\r\n" + //
			"\tdocument.getElementById(id).width= (newwidth) + \"px\";\r\n" + // 
			"}\r\n" + //
			"</script>";
	
	/** HTML metadata tag for "text/html" and charset UTF-8 */
	public static final String HTML_HEADER_META_CONTENT_TYPE = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
	/** HTML tag letting the client know that it can resize the window to fit
	 * the
	 * screen size. */
	public static final String HTML_HEADER_META_VIEWPORT = "<meta name=viewport content=\"width=device-width, initial-scale=1\">";
	
	/** An in-line script that allows for checking if an element is visible on
	 * the page(useful for only scrolling to an element if it is off screen,
	 * etc.) */
	public static final String checkVisibleJavascript = "function checkVisible(elm) {\r\n" + //
			"\tvar rect = elm.getBoundingClientRect();\r\n" + //
			"\tvar viewHeight = Math.max(document.documentElement.clientHeight, window.innerHeight);\r\n" + //
			"\treturn !(rect.bottom < 0 || rect.top - viewHeight >= 0);\r\n" + //
			"}\r\n";
	
	/** Three non-breaking spaces, or:
	 * <code>&amp;nbsp;&amp;nbsp;&amp;nbsp;</code> */
	public static final String nonBreakingSpaces = "&nbsp;&nbsp;&nbsp;";
	
	//=======================
	
	static {
		if(!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}
	
	//=======================
	
	public static final int[] purgeDeadClientIpConnectionsFromSocketList(String... ips) {
		int numSocketsClosed = 0;
		int numSocketsRemoved = 0;
		int numReusedConnsRemoved = 0;
		for(ClientConnection conn : sockets) {
			if(conn == null) {
				continue;//Yep...
			}
			Socket s = conn.socket;
			if(s != null && !s.isClosed()) {
				final String curIp = AddressUtil.getClientAddressNoPort(s);
				if(ips != null && ips.length >= 0) {
					for(String ip : ips) {
						if(curIp.equalsIgnoreCase(ip)) {
							numReusedConnsRemoved += removeSocket(conn);
							numSocketsRemoved++;
							try {
								s.close();
								numSocketsClosed++;
							} catch(Throwable ignored) {
							}
						}
					}
				}
			}
			if(s == null) {
				numReusedConnsRemoved += removeSocket(conn);
			}
			if(s != null && s.isClosed()) {
				if(System.currentTimeMillis() - conn.getLastCompletedResponseTime() >= JavaWebServer.requestTimeout) {
					numReusedConnsRemoved += removeSocket(conn);
					numSocketsRemoved++;
				}
			}
		}
		return new int[] {numSocketsClosed, numSocketsRemoved, numReusedConnsRemoved};
	}
	
	//=======================
	
	/** The default amount of time, in seconds, that a requested resource should
	 * remain in the cache of a connected client */
	public static final Long DEFAULT_CACHE_MAX_AGE = Long.valueOf(604800L);
	
	public static volatile int VLC_NETWORK_CACHING_MILLIS = 500;
	
	//Options:
	/** The socket port that this server will listen on */
	public static volatile int listen_port = 0x50;
	/** The socket port that this server's SSL thread will listen on */
	public static volatile int ssl_listen_port = 0x1bb;
	/** The socket port that this server's admin thread will listen on */
	public static volatile int admin_listen_port = 0x25ff;
	/** The default or fallback home directory to read from when parsing
	 * incoming
	 * client requests */
	public static volatile File homeDirectory = new File(rootDir, "." + File.separatorChar + "htdocs");
	/** The default or fallback value used to determine whether or not the
	 * server
	 * should calculate and send directory sizes to the client in directory
	 * listings */
	public static volatile boolean calculateDirectorySizes = false;
	/** The default or fallback font face that is used for the directory listing
	 * page that is sent to the client */
	public static volatile String defaultFontFace = "Times New Roman";
	/** The time(in milliseconds) to wait before a client will be timed out */
	public static volatile int requestTimeout = 30000;
	/** If not set to -1, this overrides the number of threads used in the
	 * thread
	 * pool which listens to incoming connections. If this is set to -1, the
	 * default value of
	 * {@code 1000 * Runtime.getRuntime().availableProcessors()} is used.
	 * Otherwise, if value this is less than 1, it is ignored. */
	public static volatile int overrideThreadPoolSize = -1;
	
	/** Whether or not per-client console logging is enabled. Major errors/stack
	 * traces and general server logs will appear even if this is set to
	 * false. */
	public static volatile boolean enableConsoleLogging = true;
	
	protected static volatile ServerSocket socket = null;
	
	/** The authorization realm used when clients attempt to administrate this
	 * server. */
	public static final String adminAuthorizationRealm = "Server Administration";
	protected static volatile String adminUsername = "Administrator";
	protected static volatile String adminPassword = "password";
	//protected static ConcurrentHashMap<Thread, HttpDigestAuthorization>	serverAdministrationAuths	= new ConcurrentHashMap<>();
	
	protected static volatile boolean enableProxyServer = false;
	/** Whether or not this server should send proxy headers such as "Via:" and
	 * "(X-)Forwarded-For:" to remote servers when making HTTP requests. */
	public static volatile boolean sendProxyHeadersWithRequest = true;
	/** Whether or not using this server as a proxy requires a username and
	 * password. */
	public static volatile boolean proxyRequiresAuthorization = true;
	/** The authorization realm used for clients attempting to use this server
	 * as
	 * a proxy. */
	public static final String proxyAuthroizationRealm = SERVER_NAME_HEADER + " Proxy";
	protected static volatile String proxyUsername = "Proxy User";
	protected static volatile String proxyPassword = "password";
	
	public static volatile boolean proxyTerminateDeadConnections = true;
	public static volatile boolean proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached = false;
	public static volatile boolean proxyEnableSinBinConnectionLimit = false;
	
	/** Whether or not we should use cookies to help authenticate and hold a
	 * client's session */
	public static volatile boolean useCookieAuthentication = false;
	
	/** @param clientUser The username that the client sent
	 * @param clientPass The password that the client sent
	 * @return Whether or not the provided credentials match this server's
	 *         administration credentials; only the password is
	 *         case-sensitive. */
	public static final boolean areCredentialsValidForAdministration(String clientUser, String clientPass) {
		return (JavaWebServer.adminUsername.equalsIgnoreCase(clientUser)) && (JavaWebServer.adminPassword.equals(clientPass));
	}
	
	/** @param s The socket
	 * @param request The client's HTTP request
	 * @return The client's remote ip address and port */
	public static final String getClientAddress(Socket s, HTTPClientRequest request) {
		String rtrn = AddressUtil.getClientAddress(s);
		if(request != null && request.xForwardedFor != null && !request.xForwardedFor.isEmpty()) {
			printlnDebug("\tIdentified client behind proxy: " + request.xForwardedFor);
			rtrn = request.xForwardedFor;
		}
		return rtrn;
	}
	
	/** @param s The socket
	 * @param request The client's HTTP request
	 * @return The client's remote ip address without any port number */
	public static final String getClientAddressNoPort(Socket s, HTTPClientRequest request) {
		String rtrn = AddressUtil.getClientAddressNoPort(s);
		if(request != null && request.xForwardedFor != null && !request.xForwardedFor.isEmpty()) {
			printlnDebug("\tIdentified client behind proxy: " + request.xForwardedFor);
			rtrn = request.xForwardedFor;
		}
		return rtrn;
	}
	
	/** @param request The client's HTTP request
	 * @return The client's remote ip address and port */
	public static final String getClientAddress(HTTPClientRequest request) {
		String rtrn = request.getClientIPAddress();
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
		if(Main.getSWTThread() == Thread.currentThread()) {
			return;
		}
		throw new IllegalAccessError("Not a valid server thread!");
	}
	
	/** @return True if the SSL thread is enabled and active. */
	public static final boolean isSSLThreadEnabled() {
		return enableSSLThread && instance.sslThread.isAlive();
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
	
	/** @param enableProxyServer Whether or not Proxy server functionality
	 *            should
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
	
	protected static final void issueCommand(String command) {
		try {
			LogUtils.ORIGINAL_SYSTEM_OUT.println(Thread.currentThread().getName() + " -->" + command);
			ConsoleThread.handleInput(command, Main.getConsoleThread());
			LogUtils.printConsole();
		} catch(IOException e) {
			PrintUtil.printThrowable(e);
			//PrintUtil.printErrToConsole();
		}
	}
	
	//====
	
	public static void loadOptionsFromFile(boolean loadOtherData) {
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
								printlnDebug(key + "=" + (key.toLowerCase().contains("password") ? "********" : value));
								if(key.equalsIgnoreCase("homedirectory")) {
									//if(value.startsWith(".")) {
									//	value = FilenameUtils.normalize(rootDir.getAbsolutePath() + File.separatorChar + value.substring(1));
									//}
									homeDirectory = new File(value);
									if(!homeDirectory.exists()) {
										homeDirectory.mkdirs();
									}
									PrintUtil.println("Set home directory to: \"" + homeDirectory.getAbsolutePath() + "\"!");
								} else if(key.equalsIgnoreCase("calculatedirectorysizes")) {
									calculateDirectorySizes = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set option \"calculateDirectorySizes\" to \"" + calculateDirectorySizes + "\"!");
								} else if(key.equalsIgnoreCase("directoryPageFontFace")) {
									defaultFontFace = value;
									PrintUtil.println("Set option \"directoryPageFontFace\" to \"" + defaultFontFace + "\"!");
								} else if(key.equalsIgnoreCase("phpExeFilePath")) {
									PhpResult.phpExeFilePath = value;
									PrintUtil.println("Set the main PHP executable file path to \"" + PhpResult.phpExeFilePath + "\"!");
								} else if(key.equalsIgnoreCase("requestTimeout")) {
									final int oldRequestTimeout = requestTimeout;
									try {
										requestTimeout = Integer.valueOf(value).intValue();
										if(requestTimeout < 0 || requestTimeout > 120000) {
											PrintUtil.printErrln("Option \"requestTimeout\" was not set to an acceptable value(must be between 0 and 120000): " + requestTimeout);
											requestTimeout = oldRequestTimeout;
										} else {
											PrintUtil.println("Set the server's request timeout to \"" + (requestTimeout / 1000L) + "\" seconds!");
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"requestTimeout\" was not set to a valid long value: " + value);
										requestTimeout = oldRequestTimeout;
									}
								} else if(key.equalsIgnoreCase("overrideThreadPoolSize")) {
									final int oldOverrideThreadPoolSize = overrideThreadPoolSize;
									try {
										overrideThreadPoolSize = Integer.valueOf(value).intValue();
										if(overrideThreadPoolSize < 0 && overrideThreadPoolSize != -1) {
											PrintUtil.printErrln("Option \"overrideThreadPoolSize\" was not set to an acceptable value(must be greater than zero.): " + overrideThreadPoolSize);
											overrideThreadPoolSize = oldOverrideThreadPoolSize;
										} else if(overrideThreadPoolSize != -1) {
											updateThreadPoolSizes();
											PrintUtil.println("Set the server's thread pool size to \"" + overrideThreadPoolSize + "\"!");
										} else {
											PrintUtil.println("Left the server's thread pool size set to the system-dependent default of 1000 times the number of available processors as listed above.");
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"overrideThreadPoolSize\" was not set to a valid integer value: " + value);
										overrideThreadPoolSize = oldOverrideThreadPoolSize;
									}
								} else if(key.equalsIgnoreCase("VLC_NETWORK_CACHING_MILLIS")) {
									final int oldVLCNetworkCachingMillis = VLC_NETWORK_CACHING_MILLIS;
									try {
										VLC_NETWORK_CACHING_MILLIS = Integer.valueOf(value).intValue();
										if(VLC_NETWORK_CACHING_MILLIS != -1 && (VLC_NETWORK_CACHING_MILLIS < 0 || VLC_NETWORK_CACHING_MILLIS > 10000)) {
											PrintUtil.printErrln("Option \"VLC_NETWORK_CACHING_MILLIS\" was not set to an acceptable value(must be greater than five-hundred[500] and less than ten-thousand[10000].): " + VLC_NETWORK_CACHING_MILLIS);
											VLC_NETWORK_CACHING_MILLIS = oldVLCNetworkCachingMillis;
										} else {
											PrintUtil.println("Set option \"VLC_NETWORK_CACHING_MILLIS\" to \"" + VLC_NETWORK_CACHING_MILLIS + (VLC_NETWORK_CACHING_MILLIS == -1 ? "(Disabled)" : "") + "\"!");
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"VLC_NETWORK_CACHING_MILLIS\" was not set to a valid integer value: " + value);
										VLC_NETWORK_CACHING_MILLIS = oldVLCNetworkCachingMillis;
									}
								} else if(key.equalsIgnoreCase("listenPort")) {
									final int oldListen_Port = listen_port;
									try {
										listen_port = Integer.valueOf(value).intValue();
										if(listen_port < 0 || listen_port > 65535) {
											PrintUtil.printErrln("Option \"listenPort\" was not set to a valid port number(must be between 0 and 65535): " + listen_port);
											listen_port = oldListen_Port;
										} else {
											PrintUtil.println("Set the server's listening port to \"" + listen_port + "\"!");
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"listenPort\" was not set to a valid integer value: " + value);
										listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("enableConsoleLogging")) {
									enableConsoleLogging = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set option \"enableConsoleLogging\" to \"" + enableConsoleLogging + "\"!");
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
			AutobanDictionary.loadFromFile();
			if(loadOtherData) {
				RestrictedFile.loadAllRestrictedFileDataFromFile();
				DomainDirectory.loadAllDomainDirectoryDataFromFile();
				ForumData.loadAllForumDataFromFile();
				PrintUtil.println("# of ForumDatas: " + ForumData.getInstances().size() + ";\r\n# of ForumBoards: " + ForumBoard.getInstances().size() + ";");
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
									PrintUtil.println("Set SSL functionality to \"" + (enableSSLThread ? "Enabled" : "Disabled") + "\"!");
								} else if(key.equalsIgnoreCase("sslListenPort")) {
									final int oldListen_Port = ssl_listen_port;
									try {
										ssl_listen_port = Integer.valueOf(value).intValue();
										if(ssl_listen_port < 0 || ssl_listen_port > 65535) {
											PrintUtil.printErrln("Option \"sslListenPort\" was not set to a valid port number(must be between 0 and 65535): " + ssl_listen_port);
											ssl_listen_port = oldListen_Port;
										} else {
											if(ssl_listen_port == listen_port || ssl_listen_port == admin_listen_port) {
												PrintUtil.printErrln("The SSL Listen Port was set to a port that is already assigned: " + value + "; defaulting to " + oldListen_Port + ".");
												ssl_listen_port = oldListen_Port;
											} else {
												PrintUtil.println("Set the server's ssl listening port to \"" + ssl_listen_port + "\"!");
											}
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"sslListenPort\" was not set to a valid integer value: " + value);
										ssl_listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("sslStore_KeyOrTrust")) {
									sslStore_KeyOrTrust = value.equalsIgnoreCase("key");
								} else if(key.equalsIgnoreCase("storePath")) {
									storePath = value;
									PrintUtil.println("Set the SSL Store path to \"" + storePath + "\"!");
								} else if(key.equalsIgnoreCase("storePassword")) {
									storePassword = value;
									PrintUtil.println("Set the SSL Store password to \"********\"!");
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
									PrintUtil.println("Set option \"enableAdminInterface\" to \"" + enableAdminInterface + "\"!");
								} else if(key.equalsIgnoreCase("adminListenPort")) {
									final int oldListen_Port = admin_listen_port;
									try {
										admin_listen_port = Integer.valueOf(value).intValue();
										if(admin_listen_port < 0 || admin_listen_port > 65535) {
											PrintUtil.printErrln("Option \"adminListenPort\" was not set to a valid port number(must be between 0 and 65535): " + admin_listen_port);
											admin_listen_port = oldListen_Port;
										} else {
											if(admin_listen_port == listen_port) {
												PrintUtil.printErrln("The Server Administration listening port was set to a port that is already assigned: " + value + "; defaulting to " + oldListen_Port + ".");
												admin_listen_port = oldListen_Port;
											} else {
												PrintUtil.println("Set the Server Administration listening port to \"" + admin_listen_port + "\"!");
											}
										}
									} catch(NumberFormatException ignored) {
										PrintUtil.printErrln("Option \"adminListenPort\" was not set to a valid integer value: " + value);
										admin_listen_port = oldListen_Port;
									}
								} else if(key.equalsIgnoreCase("adminUsername")) {
									adminUsername = value;
									PrintUtil.println("Set the Server Administration username to \"" + adminUsername + "\"!");
								} else if(key.equalsIgnoreCase("adminPassword")) {
									adminPassword = value;
									PrintUtil.println("Set the Server Administration password to \"********\"!");
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
									PrintUtil.println("Set the Server Proxy state to \"" + (enableProxyServer ? "Enabled" : "Disabled") + "\"!");
								} else if(key.equalsIgnoreCase("sendProxyHeadersWithRequest")) {
									sendProxyHeadersWithRequest = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set option \"sendProxyHeadersWithRequest\" to \"" + sendProxyHeadersWithRequest + "\"!");
								} else if(key.equalsIgnoreCase("proxyRequiresAuthorization")) {
									proxyRequiresAuthorization = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set option \"proxyRequiresAuthorization\" to \"" + proxyRequiresAuthorization + "\"!");
								} else if(key.equalsIgnoreCase("proxyUsername")) {
									proxyUsername = value;
									PrintUtil.println("Set the Server Proxy username to \"" + proxyUsername + "\"!");
								} else if(key.equalsIgnoreCase("proxyPassword")) {
									proxyPassword = value;
									PrintUtil.println("Set the Server Proxy password to \"********\"!");
								} else if(key.equalsIgnoreCase("proxyTerminateDeadConnections")) {
									proxyTerminateDeadConnections = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set Server Proxy parameter 'proxyTerminateDeadConnections' to \"" + proxyTerminateDeadConnections + "\"!");
								} else if(key.equalsIgnoreCase("proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached")) {
									proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set Server Proxy parameter 'proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached' to \"" + proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached + "\"!");
								} else if(key.equalsIgnoreCase("proxyEnableSinBinConnectionLimit")) {
									proxyEnableSinBinConnectionLimit = Boolean.valueOf(value).booleanValue();
									PrintUtil.println("Set Server Proxy password to \"********\"!");
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
		long numUUIDFilesLoaded = 0;
		for(String fileName : folder.list()) {
			File file = new File(folder, fileName);
			if(fileName.endsWith(".txt") && fileName.length() > 5) {
				String baseName = fileName.substring(0, fileName.length() - 4);
				if(StringUtils.isStrUUID(baseName)) {
					byte[] data = StringUtil.readFile(file);
					data = data == null ? new byte[0] : data;
					String fileData = new String(data, StandardCharsets.UTF_8);
					String[] split = fileData.split(Pattern.quote("\n"));
					String foundIP = null;
					for(String s : split) {
						s = s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
						if(s.startsWith("ipAddress=")) {
							foundIP = s.substring("ipAddress=".length());
							break;
						}
					}
					numUUIDFilesLoaded++;
					if(foundIP != null) {
						try {
							NaughtyClientData naughty = new NaughtyClientData(foundIP);
							sinBin.add(naughty);
							naughty.loadFromFile(file);
							if(naughty.isDisposed()) {
								//naughty.deleteSaveFile();
								continue;
							}
						} catch(IllegalStateException ignored) {
							System.err.println("Warning: Attempted to load duplicate banned client data from old UUID-based saved file data for ip: " + foundIP);
						}
					}
				} else if(AddressUtil.isAddressInValidFormat(baseName)) {
					try {
						NaughtyClientData naughty = new NaughtyClientData(baseName);
						sinBin.add(naughty);
						naughty.loadFromFile(file);
						if(naughty.isDisposed()) {
							naughty.deleteSaveFile();
							continue;
						}
					} catch(IllegalStateException ignored) {
						System.err.println("Warning: Attempted to load duplicate banned client data from file for ip: " + baseName);
					}
				}
			}
		}
		if(numUUIDFilesLoaded > 0) {
			println("Found and loaded(or attempted to load) from " + numUUIDFilesLoaded + " old UUID-based banned client file" + (numUUIDFilesLoaded == 1 ? "" : "s") + ".");
			File UUIDArchiveFolder = new File(folder, "UUID-Based-Files");
			if(!UUIDArchiveFolder.exists()) {
				UUIDArchiveFolder.mkdirs();
			}
			for(String fileName : folder.list()) {
				File file = new File(folder, fileName);
				if(fileName.endsWith(".txt") && fileName.length() > 5) {
					String baseName = fileName.substring(0, fileName.length() - 4);
					if(StringUtils.isStrUUID(baseName)) {
						try {
							FileUtils.moveToDirectory(file, UUIDArchiveFolder, true);
						} catch(IOException ignored) {
							if(!FileDeleteStrategy.FORCE.deleteQuietly(file)) {
								file.deleteOnExit();
							}
						}
					}
				}
			}
		}
		if(sinBinUpkeepThread == null) {
			sinBinUpkeepThread = new Thread() {
				@Override
				public final void run() {
					while(serverActive) {
						for(NaughtyClientData data : sinBin) {
							if(data.canBeBanned() && (!data.isBanned() || !data.shouldBeBanned())) {
								int[] closedRemoved = data.dispose();
								int numClosed = closedRemoved[0];
								int numRemoved = closedRemoved[1];
								int numReusedConnectionsClosed = closedRemoved[2];
								if(numRemoved > 0) {
									println("Removed " + numRemoved + " socket" + (numRemoved == 1 ? "" : "s") + //
									(numClosed > 0 ? (numRemoved > numClosed ? "(Only c" : "and c") + "losed " + numClosed + "socket" + (numClosed == 1 ? "" : "s") : "") + (numRemoved > numClosed ? ")" : "") + //
									" from the socket deque." + //
									(numReusedConnectionsClosed > 0 ? " Additionally, " + numReusedConnectionsClosed + " reused connection" + (numReusedConnectionsClosed == 1 ? "" : "s") + " w" + (numReusedConnectionsClosed == 1 ? "" : "as") + " closed." : "")//
									);
								} else {
									//println("No stuck socket connections found to purge.");
									//println("If there seems to be some stuck in the SWT window, then the connected clients deque needs to be purged instead.");
								}
							}
							//CodeUtil.sleep(10L);
						}
						CodeUtil.sleep(250L);
					}
				}
			};
			sinBinUpkeepThread.setDaemon(false);
			sinBinUpkeepThread.start();
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
			pr.println("proxyTerminateDeadConnections=" + proxyTerminateDeadConnections);
			pr.println("proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached=" + proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached);
			pr.println("proxyEnableSinBinConnectionLimit=" + proxyEnableSinBinConnectionLimit);
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
	
	public static final void saveOptionsToFile(boolean saveOtherData) {
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
			pr.println("VLC_NETWORK_CACHING_MILLIS=" + VLC_NETWORK_CACHING_MILLIS);
			pr.println("listenPort=" + listen_port);
			pr.println("enableConsoleLogging=" + enableConsoleLogging);
			pr.flush();
			pr.close();
			saveSSLOptionsToFile();
			saveAdminOptionsToFile();
			saveProxyOptionsToFile();
			saveSinBinToFile();
			AutobanDictionary.saveToFile();
			if(saveOtherData) {
				RestrictedFile.saveAllRestrictedFileDataToFile();
				DomainDirectory.saveAllDomainDirectoryDataToFile();
				ForumData.saveAllForumDataToFile();
				FolderData.saveFolderData();
				FileData.saveFileData();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	/** @return The Server instance */
	public static final JavaWebServer getInstance() {
		return instance;
	}
	
	private static volatile boolean isShuttingDown = false;
	
	/** @return Whether or not the {@link #shutdown()} method has been
	 *         called. */
	public static final boolean isShuttingDown() {
		return isShuttingDown;
	}
	
	/** Tells the server that it should shut down as soon as possible.<br>
	 * <br>
	 * Note that since {@link System#exit(int)} is called at the end of this
	 * method, this method never returns normally. */
	public static final void shutdown() {
		if(isShuttingDown) {
			return;
		}
		final Thread currentThread = Thread.currentThread();
		//final Main main = Main.getInstance();
		final boolean isCurrentThreadSWT = Main.getSWTThread() == currentThread;
		if(isCurrentThreadSWT) {
			try {
				if(socket != null) {
					socket.close();
				}
			} catch(Throwable ignored) {
			}
			return;
		}
		isShuttingDown = true;
		PrintUtil.printlnNow("Shutting down server...");
		if(!getConnectedClients(true).isEmpty()) {
			PrintUtil.printlnNow("Client transfers detected - Waiting up to 60 seconds on client transfers to complete...");
			boolean didBreak = false;
			long startTime = System.currentTimeMillis();
			while(!getConnectedClients(true).isEmpty()) {
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
				ArrayList<ClientConnection> remaining = getConnectedClients(false);
				if(!remaining.isEmpty()) {
					PrintUtil.printlnNow("60 second time-out; Cancelling remaining client transfers...");
					for(ClientConnection connection : remaining) {
						if(connection.status.isCancellable()) {
							connection.status.cancel();
						}
						connection.status.resume();
					}
					//connectedClients.clear();
				} else {
					PrintUtil.printlnNow("60 second time-out; Remaining client transfers appear to be cancelled already...");
				}
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
		if(currentThread != Main.getSWTThread() && Main.getSWTThread() != null) {//i.e. if this shutdown method was not called from the gui thread(Main.java)
			PrintUtil.printlnNow("Interrupting main server thread...");
			try {
				Main.getSWTThread().interrupt();
			} catch(Throwable e) {
				PrintUtil.printThrowable(e);
				PrintUtil.printErrToConsole();
			}
		}
		try {
			//List<Runnable> tasks = fThreadPool.shutdownNow();
			//if(tasks != null && !tasks.isEmpty()) {
			PrintUtil.printlnNow("Waiting on remaining active thread tasks to terminate with a " + (5 + (enableSSLThread ? 5 : 0) + (enableAdminInterface ? 5 : 0)) + "-second time-out...");
			PrintUtil.printErrToConsole();
			if(isCurrentThreadSWT) {
				Main.getInstance().runLoop();
			}
			boolean tasksTerminatedBeforeTimeout = false;
			
			for(int i = 0; i < 5; i++) {
				try {
					tasksTerminatedBeforeTimeout |= fThreadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
				} catch(InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
				if(isCurrentThreadSWT) {
					Main.getInstance().runLoop();
				}
				if(tasksTerminatedBeforeTimeout) {
					break;
				}
			}
			if(enableSSLThread) {
				for(int i = 0; i < 5; i++) {
					try {
						tasksTerminatedBeforeTimeout |= fSSLThreadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
					} catch(InterruptedException ignored) {
						Thread.currentThread().interrupt();
					}
					if(isCurrentThreadSWT) {
						Main.getInstance().runLoop();
					}
					if(tasksTerminatedBeforeTimeout) {
						break;
					}
				}
			}
			if(enableAdminInterface) {
				for(int i = 0; i < 5; i++) {
					try {
						tasksTerminatedBeforeTimeout |= fAdminThreadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
					} catch(InterruptedException ignored) {
						Thread.currentThread().interrupt();
					}
					if(isCurrentThreadSWT) {
						Main.getInstance().runLoop();
					}
					if(tasksTerminatedBeforeTimeout) {
						break;
					}
				}
			}
			if(tasksTerminatedBeforeTimeout) {
				PrintUtil.printlnNow("Active thread tasks terminated before time-out; Continuing with shutdown...");
			} else {
				PrintUtil.printlnNow("Active thread tasks did not terminate before time-out; Continuing with shutdown...");
			}
			PrintUtil.printErrToConsole();
		} catch(Throwable e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
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
			/*for(int i = 0; i < 10; i++) {
				try {
					Thread.sleep(100L);
				} catch(Throwable ignored) {
				}
			}*/
			Functions.sleep(1000L);//I'm not sure if this is actually needed anymore, as I believe this was a hack-ish way to fix the main thread freezing right before shutdown, but that may have already been fixed, so idk...
		}
		try {
			Main.shutdown();
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
		isShuttingDown = false;
		System.exit(0);
	}
	
	private static volatile PrintWriter logWriter;
	private static volatile File logFile;
	
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
			PrintUtil.println("Set up log file writer successfully.");
		} catch(Throwable e) {
			logWriter = null;
			PrintUtil.setSecondaryOut(null);
			PrintUtil.setSecondaryErr(null);
			PrintUtil.printErrln("Unable to set up log file writer:");
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
			public final void run() {
				while(serverActive) {
					/*try {
						for(ClientStatus status : new ArrayList<>(connectedClients)) {
							try {
								if(status.getClient() != null) {
									if(status.getClient().isClosed()) {
										connectedClients.remove(status);
									}
								} else {
									connectedClients.remove(status);
								}
							} catch(Throwable ignored) {
							}
							Functions.sleep();
						}
					} catch(Throwable ignored) {
					}
					//CodeUtil.sleep(333L);
					try {
						for(ClientStatus status : new ArrayList<>(connectedClientRequests)) {
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
							Functions.sleep();
						}
					} catch(Throwable ignored) {
					}
					//CodeUtil.sleep(333L);
					try {
						for(ClientStatus status : new ArrayList<>(connectedProxyRequests)) {
							try {
								if(enableProxyServer) {
									if(status.isCancelled()) {
										connectedProxyRequests.remove(status);
									} else if(status.getClient() != null) {
										if(status.getClient().isClosed()) {
											connectedProxyRequests.remove(status);
										} else if(!status.isProxyRequest()) {
											connectedProxyRequests.remove(status);
											//connectedClientRequests.add(status);
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
							Functions.sleep();
						}
					} catch(Throwable ignored) {
					}*/
					Functions.sleep();//CodeUtil.sleep(333L);
				}
			}
		}, "ArrayListCleanupThread");
		this.arrayListCleanupThread.setDaemon(true);
		setupLogWriter();
		this.adminThread = new Thread(new Runnable() {
			@Override
			public final void run() {
				try(ServerSocket socket = new ServerSocket(admin_listen_port)) {
					PrintUtil.println("\tAdmin socket created on port " + admin_listen_port);
					PrintUtil.println("\tTo access the administration interface,\r\n\tvisit any domain or ip address pointing to\r\n\tthis machine using the admin listen port\r\n\tin a web browser using the admin credentials.");
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
							@SuppressWarnings("resource")
							@Override
							public final void run() {
								final ClientConnection REUSE = ClientConnection.getNewConn(s);
								Socket sock = s;
								REUSE.println("New Connection: " + AddressUtil.getClientAddress(sock));
								RequestResult result = null;
								try {
									//sock.setTcpNoDelay(true);
									boolean https = false;
									final InputStreamSSLWrapper in = new InputStreamSSLWrapper(s.getInputStream());
									if(in.isNextByteClientHello() && isSSLThreadEnabled()) {//Enables HTTP/HTTPS on one port!
										SocketWrapper wrappedS = new SocketWrapper(s);
										sock = wrappedS.wrapSSL(TLSProtocols);
										in.setSource(sock.getInputStream());
										https = true;
										Thread.currentThread().setName("(SSL) " + Thread.currentThread().getName());
									}
									REUSE.socket = sock;
									if(REUSE.isIncomingConnectionBanned()) {
										return;
									}
									result = HandleAdminRequest(sock, in, https, REUSE);//HandleAdminRequest(s);
									REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
									if(!isExceptionIgnored(result.exception)) {
										REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										REUSE.allowReuse = false;
									}
									if(result.exception instanceof TimeoutException) {
										REUSE.allowReuse = false;
									}
									if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
										REUSE.allowReuse = false;
									}
									HTTPClientRequest req = result.getRequest();
									if(req != null) {
										req.setLogResult(result.exception == null ? "Port " + Integer.toString(admin_listen_port) + (https ? " SSL" : " Plaintext") + " Administration Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
										req.markCompleted(adminResponseLogsIncluded);
									}
									printRequestLogsAndGC(result);
									if(REUSE.allowReuse) {
										//REUSE.println("Reused Connection: " + AddressUtil.getClientAddress(sock) + (result.getRequest() != null && result.getRequest().xForwardedFor != null && !result.getRequest().xForwardedFor.isEmpty() ? "(Forwarded for: " + result.getRequest().xForwardedFor + ")" : ""));
										//long startTime = System.currentTimeMillis();
										while((result = HandleAdminRequest(sock, in, https, REUSE)).connection.allowReuse) {
											REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
											if(!isExceptionIgnored(result.exception)) {
												REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
												REUSE.allowReuse = false;
											}
											if(result.exception instanceof TimeoutException) {
												REUSE.allowReuse = false;
											}
											if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
												REUSE.allowReuse = false;
											}
											/*if(System.currentTimeMillis() - startTime < 250L) {//FIXME There's a strange bug/glitch that causes CPU usage to spike when a request starts looping for some reason, so this is a temporary hack/workaround until I can find it...
												REUSE.allowReuse = false;
												break;
											}*/
											req = result.getRequest();
											if(req != null) {
												req.setLogResult(result.exception == null ? "Port " + Integer.toString(admin_listen_port) + (https ? " SSL" : " Plaintext") + " Administration Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
												req.markCompleted(adminResponseLogsIncluded);
											}
											printRequestLogsAndGC(result);
											if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null || !REUSE.allowReuse) {
												REUSE.allowReuse = false;
												break;
											}
											//startTime = System.currentTimeMillis();
											if(REUSE.isIncomingConnectionBanned()) {
												return;
											}
										}
										printRequestLogsAndGC(result);
									} else {//No such thing as a proxy request for the administration port.
										if(!isExceptionIgnored(result.exception)) {
											REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										} else {
											if(result.exception != null) {
												REUSE.printlnDebug("An ignored exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
											} else {
												REUSE.printlnDebug("The client socket was closed prematurely!!!1");
											}
										}
									}
									try {
										in.close();
									} catch(Throwable ignored) {
									}
									printRequestLogsAndGC(REUSE.allowReuse ? result : null);//do I remove this due to Deja-Vu-ness?
								} catch(Error | RuntimeException e) {
									if(s.isClosed() && s.isInputShutdown()) {
										REUSE.clearAllLogsNow();
										PrintUtil.clearLogs();
										PrintUtil.clearErrLogs();
									} else {
										PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
										printRequestLogsAndGC(result);
									}
								} catch(Throwable e) {
									if(!isExceptionIgnored(e)) {
										e.printStackTrace(System.out);
										PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
									}
									printRequestLogsAndGC(result);
								}
								try {
									s.close();
								} catch(Throwable ignored) {
								}
								removeSocket(REUSE);
								//serverAdministrationAuths.remove(Thread.currentThread());
								System.gc();
								//Thread.currentThread().setName(tName);
								PrintUtil.printToConsole();
								PrintUtil.printErrToConsole();
							}
						};
						fAdminThreadPool.execute(task);
					}
					JavaWebServer.shutdown();
				} catch(BindException e) {
					PrintUtil.printErrln(" /!\\ \tUnable to bind to admin port " + admin_listen_port + ":\r\n/___\\\t" + e.getMessage());
					PrintUtil.printErrToConsole();
					JavaWebServer.shutdown();
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		}, AdminThreadName);
		Runnable oldSSLCode = new Runnable() {
			@Override
			public final void run() {
				if(sslStore_KeyOrTrust) {
					System.setProperty("javax.net.ssl.keyStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.keyStorePassword", storePassword);
				} else {
					System.setProperty("javax.net.ssl.trustStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.trustStorePassword", storePassword);
				}
				try(SSLServerSocket socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(ssl_listen_port)) {
					socket.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"});
					PrintUtil.printlnNow("\tServer SSL socket created on port " + ssl_listen_port);
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
							break;
						}
						fSSLThreadPool.execute(new Runnable() {
							@Override
							public final void run() {
								final ClientConnection REUSE = ClientConnection.getNewConn(s);
								REUSE.println("New Connection: " + AddressUtil.getClientAddress(s));
								boolean wasProxyRequest = false;
								RequestResult result = null;
								try {
									//s.setTcpNoDelay(true);
									if(REUSE.isIncomingConnectionBanned()) {
										return;
									}
									@SuppressWarnings("resource")
									InputStream in = s.getInputStream();
									result = HandleRequest(s, in, true, REUSE);
									REUSE.checkStatusCode();
									if(!isExceptionIgnored(result.exception)) {
										REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										REUSE.allowReuse = false;
									}
									if(result.exception instanceof TimeoutException) {
										REUSE.allowReuse = false;
									}
									if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown() || result.exception != null) {
										REUSE.allowReuse = false;
									}
									wasProxyRequest = result.wasProxyRequest();
									if(!wasProxyRequest) {
										HTTPClientRequest req = result.getRequest();
										if(req != null) {
											req.setLogResult(result.exception == null ? "Port " + Integer.toString(ssl_listen_port) + " SSL Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
											req.markCompleted(httpsResponseLogsIncluded);
										}
									}
									printRequestLogsAndGC(result);
									if(REUSE.allowReuse) {
										//REUSE.println("Reused Connection: " + AddressUtil.getClientAddress(s) + (result.getRequest() != null && result.getRequest().xForwardedFor != null && !result.getRequest().xForwardedFor.isEmpty() ? "(Forwarded for: " + result.getRequest().xForwardedFor + ")" : ""));
										while((result = HandleRequest(s, in, true, REUSE)).connection.allowReuse) {
											REUSE.checkStatusCode();
											if(!isExceptionIgnored(result.exception)) {
												REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
												REUSE.allowReuse = false;
											}
											if(result.exception instanceof TimeoutException) {
												REUSE.allowReuse = false;
											}
											if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown() || result.exception != null) {
												REUSE.allowReuse = false;
											}
											HTTPClientRequest req = result.getRequest();
											if(req != null) {
												req.setLogResult(result.exception == null ? "Port " + Integer.toString(ssl_listen_port) + " SSL Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
												req.markCompleted(httpsResponseLogsIncluded);
											}
											printRequestLogsAndGC(result);
											if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown() || result.exception != null || !REUSE.allowReuse) {
												REUSE.allowReuse = false;
												break;
											}
											if(REUSE.isIncomingConnectionBanned()) {
												printRequestLogsAndGC(result);
												removeSocket(REUSE);
												return;
											}
										}
										wasProxyRequest = result.wasProxyRequest();
										if(!wasProxyRequest) {
											printRequestLogsAndGC(REUSE.allowReuse ? result : null);
										}
									} else if(!wasProxyRequest) {//If it was a proxy request, the request may still be going on while this section of code is running, so we'll let that thread deal with cleanup.
										if(!isExceptionIgnored(result.exception)) {
											REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										} else {
											if(result.exception != null) {
												REUSE.printErrDebug(" /!\\ \tAn ignored exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
											} else {
												REUSE.printlnDebug("The client socket was closed prematurely!!!1");
											}
										}
									}
									printRequestLogsAndGC(REUSE.allowReuse ? result : null);
								} catch(SSLHandshakeException e) {
									boolean printedError = false;
									final String msg = e.getMessage();
									if(msg != null) {
										if(msg.equals("Remote host closed connection during handshake") || msg.equals("no cipher suites in common")) {
											PrintUtil.printErrln("Failed to initialize SSL handshake: " + msg);
											printedError = true;
										}
									}
									if(!printedError) {
										PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\r\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
									}
									printRequestLogsAndGC(result);
								} catch(SocketException e) {
									if(s.isClosed() && s.isInputShutdown()) {
										REUSE.clearAllLogsNow();
										PrintUtil.clearLogs();
										PrintUtil.clearErrLogs();
									} else {
										final String msg = e.getMessage();
										if(HTTPClientRequest.debug || (msg != null && !msg.equals("Socket Closed") && !msg.equals("Software caused connection abort: recv failed"))) {
											PrintUtil.println("\t /!\\ \tFailed to respond to client request:\r\n\t/___\\\t" + e.getMessage());
										} else {
											PrintUtil.clearLogs();
											PrintUtil.clearErrLogs();
										}
									}
									printRequestLogsAndGC(result);
								} catch(Error | RuntimeException e) {
									if(s.isClosed() && s.isInputShutdown()) {
										REUSE.clearAllLogsNow();
										PrintUtil.clearLogs();
										PrintUtil.clearErrLogs();
									} else {
										e.printStackTrace(System.err);
										PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\r\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
									}
									printRequestLogsAndGC(result);
								} catch(Throwable e) {
									if((e instanceof IOException && !isExceptionIgnored(e)) || !(e instanceof IOException)) {
										boolean printedError = false;
										final String msg = e.getMessage();
										if(msg != null) {
											if(msg.equals("insert ignored error message here")) {
												PrintUtil.printErrln("Failed to initialize SSL handshake: " + msg);
												printedError = true;
											}
										}
										if(!printedError) {
											PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\r\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
										}
									}
									printRequestLogsAndGC(result);
								}
								if(!wasProxyRequest) {
									try {
										s.close();
									} catch(Throwable ignored) {
									}
								}
								removeSocket(REUSE);
								//Thread.currentThread().setName(tName);
								printRequestLogsAndGC(null);
							}
						});
					}
				} catch(BindException e) {
					PrintUtil.printErr(" /!\\ \tWarning! Unable to bind to ssl port " + ssl_listen_port + "!\n/___\\\tStack trace: " + e.getMessage());
					PrintUtil.printErrToConsole();
					//JavaWebServer.getInstance().shutdown();
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		};
		oldSSLCode.getClass();//shaddup unused object blah blah
		Runnable newSSLCode = new Runnable() {
			@Override
			public final void run() {
				if(sslStore_KeyOrTrust) {
					System.setProperty("javax.net.ssl.keyStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.keyStorePassword", storePassword);
				} else {
					System.setProperty("javax.net.ssl.trustStore", storePath.replace("\\", "/"));
					System.setProperty("javax.net.ssl.trustStorePassword", storePassword);
				}
				try(ServerSocket socket = new ServerSocket(ssl_listen_port)) {
					PrintUtil.printlnNow("\tServer SSL socket created on port " + ssl_listen_port);
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
						fSSLThreadPool.execute(new Runnable() {
							@SuppressWarnings("resource")
							@Override
							public final void run() {
								final ClientConnection REUSE = ClientConnection.getNewConn(s);
								boolean wasProxyRequest = false;
								Socket sock = s;
								REUSE.println("New Connection: " + AddressUtil.getClientAddress(sock));
								RequestResult result = null;
								try {
									boolean https = false;
									final InputStreamSSLWrapper in = new InputStreamSSLWrapper(s.getInputStream());
									if(in.isNextByteClientHello() && isSSLThreadEnabled()) {//Enables HTTP/HTTPS on one port!
										SocketWrapper wrappedS = new SocketWrapper(s);
										sock = wrappedS.wrapSSL(TLSProtocols);
										in.setSource(sock.getInputStream());
										https = true;
									} else {
										Thread.currentThread().setName("(!PlainText!) " + Thread.currentThread().getName());
									}
									REUSE.socket = sock;
									//sock.setTcpNoDelay(true);
									if(REUSE.isIncomingConnectionBanned()) {
										removeSocket(REUSE);
										return;
									}
									result = HandleRequest(sock, in, https, REUSE);
									REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
									if(!isExceptionIgnored(result.exception)) {
										REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\r\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										REUSE.allowReuse = false;
									}
									if(result.exception instanceof TimeoutException) {
										REUSE.allowReuse = false;
									}
									if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
										REUSE.allowReuse = false;
									}
									wasProxyRequest = result.wasProxyRequest();
									if(!wasProxyRequest) {
										HTTPClientRequest req = result.getRequest();
										if(req != null) {
											req.setLogResult(result.exception == null ? "Port " + Integer.toString(listen_port) + (https ? " SSL" : " Plaintext") + " Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
											req.markCompleted(httpsResponseLogsIncluded);
										}
									}
									printRequestLogsAndGC(result);
									if(REUSE.allowReuse) {
										//REUSE.println("Reused Connection: " + AddressUtil.getClientAddress(sock) + (result.getRequest() != null && result.getRequest().xForwardedFor != null && !result.getRequest().xForwardedFor.isEmpty() ? "(Forwarded for: " + result.getRequest().xForwardedFor + ")" : ""));
										while((result = HandleRequest(sock, in, https, REUSE)).connection.allowReuse) {
											REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
											if(!isExceptionIgnored(result.exception)) {
												REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
												REUSE.allowReuse = false;
											}
											if(result.exception instanceof TimeoutException) {
												REUSE.allowReuse = false;
											}
											if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
												REUSE.allowReuse = false;
											}
											HTTPClientRequest req = result.getRequest();
											if(req != null) {
												req.setLogResult(result.exception == null ? "Port " + Integer.toString(listen_port) + (https ? " SSL" : " Plaintext") + " Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
												req.markCompleted(httpsResponseLogsIncluded);
											}
											printRequestLogsAndGC(result);
											if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null || !REUSE.allowReuse) {
												REUSE.allowReuse = false;
												break;
											}
											if(REUSE.isIncomingConnectionBanned()) {
												removeSocket(REUSE);
												return;
											}
										}
										wasProxyRequest = result.wasProxyRequest();
									} else if(!wasProxyRequest) {//If it was a proxy request, the request may still be going on while this section of code is running, so we'll let that thread deal with cleanup.
										if(!isExceptionIgnored(result.exception)) {
											REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										} else {
											if(result.exception != null) {
												REUSE.printErrDebug(" /!\\ \tAn ignored exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
											} else {
												REUSE.printlnDebug("The client socket was closed prematurely!!!1");
											}
										}
									}
									printRequestLogsAndGC(REUSE.allowReuse ? result : null);
								} catch(SSLHandshakeException e) {
									PrintUtil.printErrln("Failed to initialize SSL handshake: " + e.getMessage());
									printRequestLogsAndGC(result);
								} catch(SocketException e) {
									if(s.isClosed() && s.isInputShutdown()) {
										REUSE.clearAllLogsNow();
										PrintUtil.clearLogs();
										PrintUtil.clearErrLogs();
									} else {
										final String msg = e.getMessage();
										if(HTTPClientRequest.debug || (msg != null && !msg.equals("Socket Closed") && !msg.equals("Software caused connection abort: recv failed"))) {
											PrintUtil.println(" /!\\ \tFailed to respond to client request:\n/___\\\t" + e.getMessage());
										} else {
											PrintUtil.clearLogs();
											PrintUtil.clearErrLogs();
										}
									}
									printRequestLogsAndGC(result);
								} catch(Error | RuntimeException e) {
									e.printStackTrace(System.err);
									PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
									printRequestLogsAndGC(result);
								} catch(Throwable e) {
									boolean printedError = false;
									final String msg = e.getMessage();
									if(msg != null) {
										if(msg.equals("Remote host closed connection during handshake")) {
											PrintUtil.printErrln("Failed to initialize SSL handshake: " + msg);
											printedError = true;
										}
									}
									if(!printedError) {
										PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
									}
									printRequestLogsAndGC(result);
								}
								if(!wasProxyRequest) {
									try {
										s.close();
									} catch(Throwable ignored) {
									}
								}
								removeSocket(REUSE);
								//Thread.currentThread().setName(tName);
								printRequestLogsAndGC(null);
							}
						});
					}
				} catch(BindException e) {
					PrintUtil.printErrln(" /!\\ \tUnable to bind to ssl port " + ssl_listen_port + ":\r\n/___\\\t" + e.getMessage());
					PrintUtil.printErrToConsole();
					JavaWebServer.shutdown();
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		};
		this.sslThread = new Thread(newSSLCode, SSLThreadName);
	}
	
	private static final File sessionLockFile = new File(rootDir, "session.lock");
	private static PrintWriter sessionWriter;
	
	private static final void checkSessionFileLock() {
		try {
			if(!sessionLockFile.exists()) {
				sessionLockFile.createNewFile();
			} else {
				PrintUtil.println("Session lock file already exists! Was the server not properly shut down(or is another one still running)?");
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
			PrintUtil.println("Session lock initialized.");
			return;
		} catch(Throwable ignored) {
			PrintUtil.printErrln("Fatal error: Unable to initialize session lock(is there another instance of this web server already running in the current directory?)!");
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			JavaWebServer.shutdown();
			return;
		}
	}
	
	/** @param threadRootName unused */
	protected static final void replaceCurrentThreadName(String threadRootName) {
		//final String tName = Thread.currentThread().getName();
		//Thread.currentThread().setName(tName.replace("pool-", threadRootName + "_").replace("-thread-", "_"));
	}
	
	protected static final void printReuseLogsAndGC(ClientConnection reuse) {
		if(reuse != null) {
			reuse.printLogsNow(true);
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
		}
		if(PhpResult.unregisterPHPExecResponsesForCurrentThread() <= 0) {
			System.gc();
		}
	}
	
	private static final void printResultLogs(RequestResult result) {
		if(result != null) {
			boolean printLogs = true;
			if(result.connection.domainDirectory != null) {
				printLogs = result.connection.domainDirectory.displayLogEntries.getValue() == Boolean.TRUE;
			} //else if(result.clientInfo != null && result.clientInfo.domainDirectory != null) {
				//printLogs = result.clientInfo.domainDirectory.displayLogEntries.getValue() == Boolean.TRUE;
				//}
				/*if(!printLogs && result.getRequest() != null) {
					result.getRequest().requestLogs = "";
				}
				if(result.getRequest() != null && !result.getRequest().requestLogs.isEmpty()) {
					if(!result.reuse.willLogsBeClearedBeforeDisplay()) {
						PrintUtil.println(result.getRequest().requestLogs);
					}
					result.getRequest().requestLogs = "";
				}*/
			if(printLogs) {
				if(!result.connection.wereLastLogsClearedBeforeDisplay() && result.getRequest() != null) {
					boolean printStatusMessage = true;
					printStatusMessage &= (HTTPClientRequest.debug || result.connection.getStatusCode() != HTTPStatusCodes.HTTP_408);
					if(printStatusMessage && result.getResponse() != null && result.getResponse().getStatusMessage() != null && !result.getResponse().getStatusMessage().trim().isEmpty()) {
						final String statusMessage = result.getResponse().getStatusMessage();
						printStatusMessage &= !statusMessage.equals("Bad protocol request: \"\"");
					}
					if(printStatusMessage) {
						result.connection.println(result.getLogHTTPLine());
						FileInfo requestedFile = result.connection.status.getRequestedFile();
						if(requestedFile == null && result.connection.domainDirectory != null) {
							try {
								File file = result.connection.domainDirectory.getFileFromRequest(result.getRequest().requestedFilePath, result.getRequest().requestArguments, null);
								requestedFile = new FileInfo(file, result.connection.domainDirectory);
							} catch(Throwable ignored) {
							}
						}
						if(isExceptionIgnored(result.exception) && !result.connection.getStatusCode().name().startsWith("HTTP_5")) {
							if(requestedFile != null && result.connection.getStatusCode().name().startsWith("HTTP_2")) {
								result.connection.println("\t\t\tServed file \r\n\t\t\t\"" + FilenameUtils.normalize(requestedFile.filePath) + "\"\r\n\t\t\t to client \"" + AddressUtil.getClientAddress(result.connection.socket) + "\" successfully.");
							} else if(result.exception == null) {
								result.connection.println("\t\t\tResponse served to client successfully.");
							}
						}
					}
				}
				result.connection.printLogsNow(true);
				if(result.exception != null) {
					if(!isExceptionIgnored(result.exception)) {
						PrintUtil.printErr(StringUtil.throwableToStr(result.exception, "\n") + "\n");
					}
				}
			} else {
				result.connection.clearAllLogsBeforeDisplay();
				result.connection.printLogsNow(true);
			}
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
		}
	}
	
	protected static final void printRequestLogsAndGC(RequestResult result) {
		printResultLogs(result);
		if(result != null) {
			if(result.printLogs()) {
				printReuseLogsAndGC(result.connection);
				//PrintUtil.printlnNow(result.toString());
				//return;
			}
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		if(PhpResult.unregisterPHPExecResponsesForCurrentThread() <= 0) {
			System.gc();
		}
	}
	
	protected static final void finalizeLogs(final boolean printLogs, final RequestResult result, final ClientConnection REUSE) {
		if(printLogs) {
			if(result != null) {
				printRequestLogsAndGC(result);
			} else {
				printReuseLogsAndGC(REUSE);
			}
		} else {
			if(PhpResult.unregisterPHPExecResponsesForCurrentThread() <= 0) {
				System.gc();
			}
		}
	}
	
	/** @param conn The ReusedConn representing the socket to remove
	 * @return The number of {@link ClientConnection}s that were removed as a
	 *         result */
	public static final int removeSocket(ClientConnection conn) {
		if(conn != null) {
			int removed = removeSocket(conn.socket);
			return (removed == -1 ? 0 : removed) + (sockets.remove(conn) ? 1 : 0);
		}
		return -1;
	}
	
	/** @param socket The socket to remove
	 * @return The number of {@link ClientConnection}s that were removed as a
	 *         result */
	public static final int removeSocket(Socket socket) {
		if(socket != null) {
			int removed = 0;
			for(ClientConnection conn : sockets) {
				if(conn.socket == socket) {
					sockets.remove(conn);
					removed++;
				}
			}
			return removed;
		}
		return -1;
	}
	
	/** @param args System command arguments */
	public static final void sysMain(String[] args) {
		//LogUtils.getOut().print("Test Line! 123\n456\n");
		final Thread socketListUpdaterThread = new Thread() {
			@Override
			public final void run() {
				this.setName("SocketListUpdater" + this.getName());
				while(serverActive) {
					try {
						for(ClientConnection conn : sockets) {
							if(conn != null) {
								if(conn.status.isCancelled()) {// || (client.isClosed() || (client.isInputShutdown() && client.isOutputShutdown()))) {//If the socket is closed or dead, or the request was cancelled
									try {
										conn.closeSocket();
									} catch(Throwable ignored) {
									}
									removeSocket(conn);
								}
							}
						}
					} catch(Throwable e) {
						LogUtils.ORIGINAL_SYSTEM_ERR.print("Failed to iterate over connected sockets queue: ");
						e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
					}
					Functions.sleep(10L);
				}
			}
		};
		socketListUpdaterThread.setDaemon(true);
		socketListUpdaterThread.start();
		instance = new JavaWebServer();
		checkSessionFileLock();
		PrintUtil.println("Available server threads(1000 * # of processors): " + fNumberOfThreads);
		loadOptionsFromFile(true);
		if(homeDirectory == null) {
			homeDirectory = new File(rootDir, "htdocs");
			homeDirectory.mkdirs();
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		//instance.arrayListCleanupThread.start();
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
			socket = new ServerSocket(listen_port);
			PrintUtil.println("\tServer socket created on port " + listen_port);
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
					@SuppressWarnings("resource")
					public final void run() {
						final ClientConnection REUSE = ClientConnection.getNewConn(s);
						boolean wasProxyRequest = false;
						Socket sock = s;
						REUSE.println("New Connection: " + AddressUtil.getClientAddress(sock));
						RequestResult result = null;
						boolean printLogs = true;
						try {
							//sock.setTcpNoDelay(true);
							/*final com.gmail.br45entei.server.data.InputStreamReader in = new com.gmail.br45entei.server.data.InputStreamReader(s.getInputStream());
							if(in.isNextByteClientHello() && isSSLThreadEnabled()) {//Enables HTTP/HTTPS on one port!
								SocketWrapper wrappedS = new SocketWrapper(s);
								wrappedS.insertByte(com.gmail.br45entei.server.data.InputStreamReader.sslClientHello);
								SSLSocketFactory sslSf = JavaWebServer.getSSLSocketFactory();
								SSLSocket sslSocket = (SSLSocket) sslSf.createSocket(wrappedS, null, wrappedS.getPort(), false);
								sslSocket.setUseClientMode(false);
								sock = sslSocket;
								sslSocket.startHandshake();
								in.setSource(sock.getInputStream());
							}*/
							boolean https = false;
							final InputStreamSSLWrapper in = new InputStreamSSLWrapper(s.getInputStream());
							if(in.isNextByteClientHello() && isSSLThreadEnabled()) {//Enables HTTP/HTTPS on one port!
								SocketWrapper wrappedS = new SocketWrapper(s);
								sock = wrappedS.wrapSSL(TLSProtocols);
								in.setSource(sock.getInputStream());
								https = true;
								Thread.currentThread().setName("(SSL) " + Thread.currentThread().getName());
							}
							REUSE.socket = sock;
							if(REUSE.isIncomingConnectionBanned()) {
								removeSocket(REUSE);
								return;
							}
							result = HandleRequest(sock, in, https, REUSE);
							REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
							if(!isExceptionIgnored(result.exception)) {
								REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
								REUSE.allowReuse = false;
							}
							if(result.exception instanceof TimeoutException) {
								REUSE.allowReuse = false;
							}
							if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
								REUSE.allowReuse = false;
							}
							boolean saveLog = true;
							wasProxyRequest = result.wasProxyRequest();
							if(!wasProxyRequest) {//If it was a proxy request, the request may still be going on while this section of code is running, so we'll let that thread deal with cleanup.
								HTTPClientRequest req = result.getRequest();
								if(req != null) {
									req.setLogResult(result.exception == null ? "Port " + Integer.toString(listen_port) + (https ? " SSL" : " Plaintext") + " Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
									req.markCompleted(saveLog && httpResponseLogsIncluded);
									saveLog = false;
								}
							}
							if(result.exception != null) {
								REUSE.allowReuse = false;
							}
							printRequestLogsAndGC(result);
							printLogs = false;
							if(REUSE.allowReuse) {
								//REUSE.println((REUSE.isReused() ? "Reused" : "New") + " Connection: " + AddressUtil.getClientAddress(sock) + (result.getRequest() != null && result.getRequest().xForwardedFor != null && !result.getRequest().xForwardedFor.isEmpty() ? "(Forwarded for: " + result.getRequest().xForwardedFor + ")" : ""));
								printLogs = true;
								while((result = HandleRequest(sock, in, https, REUSE)).connection.allowReuse) {
									REUSE.checkStatusCode();//result.setReusedConnFrom(REUSE);
									if(!isExceptionIgnored(result.exception)) {
										REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
										REUSE.allowReuse = false;
									}
									if(result.exception instanceof TimeoutException) {
										REUSE.allowReuse = false;
									}
									if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null) {
										REUSE.allowReuse = false;
									}
									HTTPClientRequest req = result.getRequest();
									if(req != null) {
										req.setLogResult(result.exception == null ? "Port " + Integer.toString(listen_port) + (https ? " SSL" : " Plaintext") + " Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : " /!\\ \tAn exception occurred while handling the request:\r\n/___\\\t" + Functions.throwableToStr(result.exception));
										req.markCompleted(httpResponseLogsIncluded);
									}
									printRequestLogsAndGC(result);
									printLogs = false;
									if(sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || result.exception != null || !REUSE.allowReuse) {
										REUSE.allowReuse = false;
										break;
									}
									if(REUSE.isIncomingConnectionBanned()) {
										finalizeLogs(true, result, REUSE);
										removeSocket(REUSE);
										return;
									}
								}
								if(!REUSE.allowReuse) {
									REUSE.checkStatusCode();
								}
								wasProxyRequest = result.wasProxyRequest();
								if(!wasProxyRequest) {
									printRequestLogsAndGC(result);
									printLogs = false;
								}
							} else if(!wasProxyRequest) {//If it was a proxy request, the request may still be going on while this section of code is running, so we'll let that thread deal with cleanup.
								if(!isExceptionIgnored(result.exception)) {
									REUSE.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
								} else {
									if(result.exception != null) {
										REUSE.printErrDebug(" /!\\ \tAn ignored exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(result.exception, "\n"));
									} else {
										REUSE.printlnDebug("The client socket was closed prematurely for some unknown reason!!!1");
									}
								}
							}
						} catch(Error | RuntimeException e) {
							PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\r\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
							finalizeLogs(printLogs, result, REUSE);
						} catch(Throwable e) {
							if(!isExceptionIgnored(e)) {
								e.printStackTrace(System.out);
								PrintUtil.printErr(" /!\\ \tAn unhandled exception occurred while handling the request:\n/___\\\t" + StringUtil.throwableToStr(e, "\n"));
							}
						}
						if(!wasProxyRequest) {
							try {
								s.close();
							} catch(Throwable ignored) {
							}
							removeSocket(REUSE);
						}
						//serverAdministrationAuths.remove(Thread.currentThread());
						//Thread.currentThread().setName(tName);
						finalizeLogs(printLogs, result, REUSE);
					}
				};
				fThreadPool.execute(task);
			}
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			JavaWebServer.shutdown();
		} catch(BindException e) {
			PrintUtil.printToConsole();
			PrintUtil.printErrlnNow(" /!\\ \tFatal error: Unable to bind to port " + listen_port + "!!1\n/___\\\tStack trace: " + e.getMessage());
			JavaWebServer.shutdown();
		} catch(SocketException e) {
			if(socket != null && !socket.isClosed()) {
				PrintUtil.printErrlnNow(" /!\\ \tFatal error: Failed to listen for next incoming client connection!\n/___\\\tStack trace: " + e.getMessage());
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
		JavaWebServer.shutdown();
	}
	
	/** @deprecated Not for use outside of {@link JavaWebServer#shutdown()}!
	 * @return An array list containing any clients currently connected to this
	 *         server */
	@Deprecated
	private static final ArrayList<ClientConnection> getConnectedClients(boolean getCancelled) {
		final ArrayList<ClientConnection> list = new ArrayList<>();
		for(ClientConnection connection : sockets) {
			if(connection.request == null && !getCancelled) {
				continue;
			}
			if(getCancelled || !connection.status.isCancelled()) {
				list.add(connection);
			}
		}
		return list;
	}
	
	/** An array list containing any clients currently connected to this
	 * server */
	//public static final ConcurrentLinkedDeque<ClientStatus> connectedClients = new ConcurrentLinkedDeque<>();
	/** An array list containing any clients that are making a request to this
	 * server */
	
	//public static final ConcurrentLinkedDeque<ClientStatus> connectedClientRequests = new ConcurrentLinkedDeque<>();
	/** An array list containing any clients connecting to other servers through
	 * this one */
	//public static final ConcurrentLinkedDeque<ClientStatus> connectedProxyRequests = new ConcurrentLinkedDeque<>();
	
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
	
	private static final HTTPStatusCodes serveFileToClient(final Socket s, final ClientConnection reuse, boolean https, final OutputStream outStream, final DomainDirectory domainDirectory, final FileInfo info) throws Throwable {
		try {
			HTTPStatusCodes code = _serveFileToClient(s, reuse, https, outStream, domainDirectory, info);
			//connectedClients.remove(clientInfo);
			if(!domainDirectory.getDisplayLogEntries()) {
				reuse.clearAllLogsBeforeDisplay();
			}
			return code;
		} catch(Error | RuntimeException e) {
			ResponseUtil.send500InternalServerError(s, reuse, domainDirectory, e, true);
			reuse.printLogsNow(true);
			/*if(!isExceptionIgnored(e)) {
				LogUtils.println(e);
			}
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();*/
			//connectedClients.remove(clientInfo);
			throw e;
		} catch(Throwable e) {
			ResponseUtil.send500InternalServerError(s, reuse, domainDirectory, e, false);//true);
			reuse.printLogsNow(true);
			/*if(!isExceptionIgnored(e)) {
				LogUtils.println(e);
			}
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();*/
			//connectedClients.remove(clientInfo);
			throw e;
		}
	}
	
	/** Locates and serves the requested file to the client. Also serves
	 * generated-on-the-fly content such as directory data, file structure,
	 * media views, etc.
	 * 
	 * @param s The client
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
	 * @see #HandleRequest(Socket, InputStream, boolean, ClientConnection)
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	@SuppressWarnings("resource")
	private static final HTTPStatusCodes _serveFileToClient(final Socket s, final ClientConnection reuse, boolean https, final OutputStream outStream, final DomainDirectory domainDirectory, final FileInfo info) throws Throwable {
		final String clientIPAddress = AddressUtil.getClientAddress(s);
		final ClientConnectTime cct = NaughtyClientData.getClientConnectTimeFor(clientIPAddress);
		final boolean connectionSeemsMalicious = cct.seemsMalicious();
		
		final String httpProtocol = (https ? "https://" : "http://");
		final boolean isFileRestrictedOrHidden = RestrictedFile.isFileForbidden(info.file) || RestrictedFile.isFileRestricted(info.file, s) || RestrictedFile.isHidden(info.file, true);
		
		String reqFilePath = FilenameUtils.normalize(info.file.getAbsolutePath());
		String wrkDirPath = FilenameUtils.normalize(JavaWebServer.rootDir.getAbsolutePath());
		
		//final File homeDirectory = domainDirectory.getDirectory();
		
		//URL fileURL = requestedFile.toURI().toURL();
		//final URLConnection conn = fileURL.openConnection();
		//final long contentLength = conn.getContentLengthLong();
		
		final String clientAddress = reuse.request.xForwardedFor.isEmpty() ? clientIPAddress : reuse.request.xForwardedFor;
		
		final String fileLink = httpProtocol + reuse.request.host + (s.getLocalPort() != ssl_listen_port && https ? reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort() : "") + StringUtils.encodeHTML(domainDirectory.replacePathWithAlias(reuse.request.requestedFilePath)).replace(" ", "%20");
		
		final boolean isHTTP1 = reuse.request.version.equalsIgnoreCase("HTTP/1.0");
		final String versionToUse = isHTTP1 ? reuse.request.version : "HTTP/1.1";
		
		//==
		
		final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectorySafe().getAbsolutePath());
		
		final String pathPrefix = domainDirectory.getURLPathPrefix();
		//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
		//println("pathPrefix: \"" + pathPrefix + "\"");
		
		final String path = info.getURLPath();
		final String pagePrefix = createPathLinksFor(path, domainDirectory.getDirectorySafe().getName(), httpProtocol + reuse.request.host, false, true, reuse.request.requestArgumentsStr)/*(path.startsWith("/") ? path : "/" + path)*/ + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress;
		final String pageHeader = "<h1>" + pagePrefix + " </h1><hr>";
		
		final boolean isLayoutOrFavicon = reuse.request.requestedFilePath.equalsIgnoreCase("/favicon.ico") || reuse.request.requestedFilePath.equalsIgnoreCase(domainDirectory.getDefaultPageIcon()) || reuse.request.requestedFilePath.equalsIgnoreCase("/layout.css") || reuse.request.requestedFilePath.equalsIgnoreCase(domainDirectory.getDefaultStylesheet());
		final boolean isReqCurrentWrkDir = (reqFilePath.equalsIgnoreCase(wrkDirPath) || reqFilePath.startsWith(wrkDirPath)) && !(reqFilePath.toLowerCase().startsWith(homeDirPath.toLowerCase()));
		
		//==
		
		String administrateFileCheck = reuse.request.requestArguments.get("administrateFile");
		final boolean administrateFile = administrateFileCheck != null ? (administrateFileCheck.equals("1") || administrateFileCheck.equalsIgnoreCase("true")) : false;
		String modifyFileCheck = reuse.request.requestArguments.get("modifyFile");
		final boolean modifyFile = modifyFileCheck != null ? (modifyFileCheck.equals("1") || modifyFileCheck.equalsIgnoreCase("true")) : false;
		
		final String folderName = FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(info.file.getAbsolutePath() + File.separatorChar));
		if(administrateFile || modifyFile) {
			DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
			boolean administrated;
			try {
				administrated = handleAdministrateFile(s, reuse, clientAddress, versionToUse, httpProtocol, outStream, out, domainDirectory, folderName, pagePrefix, modifyFile ? false : administrateFile, info);
			} catch(Throwable e) {
				ResponseUtil.send500InternalServerError(s, reuse, domainDirectory, e, true);
				throw e;
			}
			if(administrated) {
				/*if(requestedFile.isFile()) {
					FileData.getFileData(requestedFile, true).incrementTimesAccessed();
				} else if(requestedFile.isDirectory()) {
					FolderData.getFolderData(requestedFile, true).incrementTimesAccessed();
				}*/
				return reuse.response.getStatusCode();
			}
			reuse.response.setStatusCode(HTTP_500).setStatusMessage("Error administrating file(s)!");
			out.println(versionToUse + " 500 Internal Server Error");
			out.println("Vary: Accept-Encoding");
			out.println("Server: " + SERVER_NAME_HEADER);
			out.println("Date: " + StringUtils.getCurrentCacheTime());
			out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
			out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
			out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
			if(reuse.allowReuse) {
				out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
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
					+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error 500 - Error Administrating File</h1><hr>\r\n"//
					+ "\t\t<string>Something went wrong when attempting to administrate your file(s). Please try again!</string><hr>\r\n"//
					+ "\t\t<string>" + pagePrefix + "</string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(reuse.request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
				out.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(responseStr, "UTF-8", reuse);
				out.println("Content-Length: " + r.length);
				out.println("");
				if(reuse.request.method.equalsIgnoreCase("GET")) {
					outStream.write(r);
					reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
					reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				r = null;
				System.gc();
			} else {
				out.println("Content-Length: " + responseStr.length());
				out.println("");
				if(reuse.request.method.equalsIgnoreCase("GET")) {
					out.println(responseStr);
					reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
					reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
			}
			out.flush();
			if(s.isClosed() || !reuse.allowReuse) {
				out.close();
			}
			//connectedClients.remove(clientInfo);//XXX ???
			return reuse.response.getStatusCode();
		} else if(!isLayoutOrFavicon && isReqCurrentWrkDir) {//If the requested file is(or is a child of) the current working directory of this server(and is not the intended folder according to the domain data used), then we should require administration credentials before allowing the client to view sensitive server files.
			final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(reuse.request, false);
			final String authCookie = authResult.authorizedCookie;
			if(!authResult.passed()) {
				String[] credsUsed = BasicAuthorizationResult.getUsernamePasswordFromBasicAuthorizationHeader(reuse.request.authorization);
				NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), credsUsed[0], credsUsed[1]);
				reuse.response.setStatusCode(HTTP_401);
				reuse.response.setHeader("Vary", "Accept-Encoding");
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
				reuse.response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
				reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
				reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
				if(authCookie != null) {
					reuse.response.setHeader("Set-Cookie", authCookie);
				}
				reuse.response.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
						+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
						+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Authorization Required</h1><hr>\r\n"//
						+ "\t\t<string title=\"In order to be able to administrate files on this server, you must be logged in as the administrator.\">You need permission to do that.</string><hr>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				reuse.response.sendToClient(s, true);
				//connectedClients.remove(clientInfo);
				return reuse.response.getStatusCode();
			}
		}
		
		//==
		
		@SuppressWarnings("unused")
		final String homeLink = httpProtocol + (reuse.request.host + "/");
		final String parentFileLink;
		final File parentFile = info.file.getParentFile();
		//final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectory().getAbsolutePath());
		if(parentFile != null && !FilenameUtils.normalize(info.file.getAbsolutePath()).equals(homeDirPath)) {
			String parentRequestPath = FilenameUtils.normalize(parentFile.getAbsolutePath()).replace(homeDirPath, "").replace("\\", "/");
			parentRequestPath = (parentRequestPath.startsWith("/") ? "" : "/") + parentRequestPath;
			parentFileLink = StringUtils.encodeHTML(httpProtocol + (reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(parentRequestPath))));
		} else {
			parentFileLink = null;
		}
		@SuppressWarnings("unused")
		final boolean isHomeDir = parentFileLink == null;
		final String previousPageLink = (!reuse.request.refererLink.isEmpty() ? reuse.request.refererLink : null);
		
		String autoplayCheck = reuse.request.requestArguments.get("autoplay");
		final boolean autoplay = autoplayCheck != null ? (autoplayCheck.equals("1") || autoplayCheck.equalsIgnoreCase("true")) : false;
		
		//==
		
		if(info.file.isDirectory() && !reuse.request.requestedFilePath.endsWith("//")) {
			FolderData data = FolderData.getFolderData(info.file, false);
			if(data != null) {
				File file = data.getDefaultFile();
				if(file != null) {
					String requestedFilePath = reuse.request.requestedFilePath + (reuse.request.requestedFilePath.endsWith("/") ? "" : "/") + file.getName() + (file.isDirectory() ? "/" : "");
					reuse.response.setStatusCode(HTTP_303).setStatusMessage("File re-direct: " + requestedFilePath).setHeader("Location", reuse.request.protocol + reuse.request.host + requestedFilePath).setResponse((String) null).sendToClient(s, false);
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				}
			}
		}
		
		if(info.file.isFile()) {
			final FolderData folderData = FolderData.getFolderData(parentFile, true);
			FileData.getFileData(info.file, true).incrementTimesAccessed();
			if(!info.file.getName().equalsIgnoreCase("layout.css") && !info.file.getName().equalsIgnoreCase("favicon.ico") && !info.file.getName().equalsIgnoreCase("favicon.png")) {
				folderData.incrementTimesAccessed();
			}
			final String fileExt = FilenameUtils.getExtension(info.fileName);
			if(fileExt.equalsIgnoreCase("php") && PhpResult.isPhpFilePresent()) {
				reuse.response.setStatusMessage("PHP file: Non-POST request");
				reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				reuse.response.setHTTPVersion(versionToUse);
				PhpResult.execPHP(info.file.getAbsolutePath(), reuse.request.requestArgumentsStr.replace("?", "").replace("&", " "), reuse.request.host, reuse, reuse.request, reuse.response);
				reuse.response.clearHeaders();
				PhpResult phpResponse = PhpResult.getLastPHPExecResponse().copyToServerResponse(reuse.response, false, false);//response, true, false);
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
				if(reuse.response.getHeader("Content-Type") == null) {
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					if(reuse.response.getResponseData() != null && !new String(reuse.response.getResponseData(), StandardCharsets.UTF_8).toLowerCase().contains("<html")) {
						reuse.response.setHeader("Content-Type", "text/plain; charset=UTF-8");
					}
				}
				//reuse.response.setResponse(phpResponse.body);
				if(HTTPClientRequest.debug) {
					reuse.println(reuse.response.toString(true));//PrintUtil.printlnNow(reuse.response.toString(true));
					//PrintUtil.printToConsole();
				}
				try {
					final boolean sendResponse = !reuse.request.method.equalsIgnoreCase("HEAD") && !(reuse.response.getStatusCode().name().startsWith("HTTP_3"));
					HTTPServerResponse.sendToClient(s, reuse.response.getHTTPVersion(), reuse.response.getStatusCode(), reuse.response.getHeaders(), phpResponse.body, sendResponse);
					//reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD") && !(reuse.response.getStatusCode().name().startsWith("HTTP_3")));//TODO Create a HTTPStatusCodes.shouldSendResponseBody(HTTPStatusCodes code) and use it here!
				} catch(Throwable e) {
					e.printStackTrace();
				}
				phpResponse.close();
				return reuse.response.getStatusCode();
			} else if(fileExt.equalsIgnoreCase("php")) {
				reuse.printErrln("\t*** Warning! Requested file is a php file, but the php setting in the\r\n\t\"" + rootDir.getAbsolutePath() + File.separatorChar + optionsFileName + "\"\r\n\tfile either refers to a non-existant file or is not defined.\r\n\tTo fix this, type phpExeFilePath= and then the complete path to the main php\r\n\texecutable file(leaving no space after the equal symbol).\r\n\tYou can download php binaries here: http://php.net/downloads.php\r\n\tIf this is not corrected, any php files requested by incoming clients will be downloaded rather than executed.");
				String check = reuse.request.requestArguments.get("download");
				if(check != null && (check.equals("1") || check.equalsIgnoreCase("true"))) {
					final String charset = StringUtils.getDetectedEncoding(info.file);
					reuse.response.setStatusMessage("PHP file: PHP-CGI not defined!");
					reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
					reuse.response.setHTTPVersion(versionToUse);
					reuse.response.setHeader("Content-Type", "text/html; charset=" + charset);
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
					reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
					reuse.response.setHeader("Accept-Ranges", "none");
					reuse.response.setResponse(FileUtil.readFileData(info.file));
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				}
				reuse.response.setStatusMessage("PHP file - Service Unavailable");
				reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_503);
				reuse.response.setHTTPVersion(versionToUse);
				reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
				reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
				reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
				reuse.response.setHeader("Accept-Ranges", "none");
				reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
						"<html>\r\n\t<head>\r\n" + //
						"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
						"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
						"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
						"\t\t<title>503 Service Unavailable - " + info.file.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
						"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
						(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
						AUTO_RESIZE_JAVASCRIPT + //
						(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
						"\t</head>\r\n" + //
						"\t<body>\r\n" + //
						"\t\t" + pageHeader + "\r\n" + //
						"\t\t<string>The requested file is a PHP file, and this server is not currently configured to run PHP scripts.</string>\r\n" + //
						"\t</body>\r\n</html>");
				reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
				//connectedClients.remove(clientInfo);
				return reuse.response.getStatusCode();
			}
			final long lastModified = StringUtils.getLastModified(info.file);
			boolean modifiedSince = false;
			if(!reuse.request.ifModifiedSince.isEmpty()) {
				try {
					long clientModifiedReq = StringUtils.getCacheValidatorTimeFormat().parse(reuse.request.ifModifiedSince).getTime();
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
			if(!reuse.request.ifRange.isEmpty()) {
				try {
					long clientModifiedReq = StringUtils.getCacheValidatorTimeFormat().parse(reuse.request.ifRange).getTime();
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
			final long contentLength = info.contentLength;
			final String contentRange = "0-" + (contentLength - 1) + "/" + info.contentLength;
			if(!reuse.request.range.isEmpty() && allowByteRangeRequests) {
				RangeRequest check = null;
				try {
					check = new RangeRequest(reuse.request.range, contentLength);
				} catch(Throwable ignored) {
				}
				if(check != null && (check.startBytes > check.endBytes)) {//RFC 2616 section 14.35.1
					ifRange = false;
				}
				if(ifRange) {
					try {
						RangeRequest range = new RangeRequest(reuse.request.range, contentLength);
						if(range.isValid()) {
							//reuse.println("\t*** 206 Partial Content(Byte range) Requested: " + range.startBytes + "-" + range.endBytes + " /" + contentLength + (reuse.request.ifRange.isEmpty() ? "(No \"If-Range\" header sent)" : "(\"If-Range\" requirement met)"));
							//@SuppressWarnings("resource")
							DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
							out.println("HTTP/1.1 206 Partial Content");
							out.println(new Header("Vary: Accept-Encoding", reuse.response.getHeadersRaw()));
							out.println(new Header("Content-Type: " + info.mimeType + "; charset=" + StringUtils.getDetectedEncoding(info.file), reuse.response.getHeadersRaw()));
							out.println(new Header("Server: " + SERVER_NAME_HEADER, reuse.response.getHeadersRaw()));
							out.println(new Header("Cache-Control: " + (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())), reuse.response.getHeadersRaw()));
							out.println(new Header("Date: " + StringUtils.getCurrentCacheTime(), reuse.response.getHeadersRaw()));
							out.println(new Header("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()), reuse.response.getHeadersRaw()));
							out.println(new Header("Content-Length: " + range.rangeLength, reuse.response.getHeadersRaw()));//info.contentLength, reuse.response.getHeadersRaw()));
							out.println(new Header("Accept-Ranges: bytes", reuse.response.getHeadersRaw()));
							out.println(new Header("Content-Range: bytes " + range.startBytes + "-" + range.endBytes + "/" + contentLength, reuse.response.getHeadersRaw()));
							out.println("");
							out.flush();
							if(reuse.request.method.equalsIgnoreCase("GET")) {
								InputStream fileIn = new FileInputStream(info.file);
								IOException exception = null;
								try {
									long sentBytes = range.sendRangeTo(fileIn, outStream, reuse.status);//copyInputStreamToOutputStream(info, fileIn, outStream, range.startBytes, range.endBytes, clientInfo);
									reuse.printlnDebug("DEBUG: Sent Bytes matches length: " + ((sentBytes == range.rangeLength) ? "true" : "false; Sent bytes: " + sentBytes));
									//reuse.println("\t\t\tSent file \r\n\t\t\t\"" + FilenameUtils.normalize(info.file.getAbsolutePath()) + "\"\r\n\t\t\t to client \"" + clientAddress + "\" successfully.");
								} catch(IOException e) {
									if(!isExceptionIgnored(e)) {
										reuse.println("\t /!\\ \tFailed to send file \r\n\t/___\\\t\"" + FilenameUtils.normalize(info.file.getAbsolutePath()) + "\"\r\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
									}
									exception = e;
								}
								try {
									fileIn.close();
								} catch(Throwable e) {
									PrintUtil.printThrowable(e);
								}
								if(exception != null) {
									throw exception;
								}
							}
							reuse.response.setStatusCode(HTTP_206).setStatusMessage(range.startBytes + "-" + range.endBytes + " /" + contentLength + (reuse.request.ifRange.isEmpty() ? "(No \"If-Range\" header sent)" : "(\"If-Range\" requirement met)"));
							return reuse.response.getStatusCode();
						}
					} catch(NumberFormatException ignored) {
					}
					//reuse.println("\t*** HTTP/1.1 416 Requested Range Not Satisfiable(\"" + reuse.request.range + "\")");
					//@SuppressWarnings("resource")
					DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
					out.println("HTTP/1.1 416 Requested Range Not Satisfiable");
					out.println("Content-Range: " + contentRange);
					out.println("");
					out.flush();
					reuse.response.setStatusCode(HTTP_416).setStatusMessage("(\"" + reuse.request.range + "\")");
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				}
				modifiedSince = true;
			}
			String mediaCheck = reuse.request.requestArguments.get("mediaInfo");
			final boolean mediaInfo = mediaCheck != null ? mediaCheck.equals("1") || mediaCheck.equalsIgnoreCase("true") : false;
			String artworkCheck = reuse.request.requestArguments.get("artwork");
			final boolean artwork = artworkCheck != null ? artworkCheck.equals("1") || artworkCheck.equalsIgnoreCase("true") : false;
			if(modifiedSince || (mediaInfo/* TODO && domainDirectory.getEnableMediaInfoParsing()*/)) {
				reuse.response.setHTTPVersion(versionToUse);
				reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				reuse.response.setHeader("Vary", "Accept-Encoding");
				
				String autoCloseCheck = reuse.request.requestArguments.get("autoClose");
				boolean autoClose = autoCloseCheck != null ? (autoCloseCheck.equals("1") || autoCloseCheck.equalsIgnoreCase("true")) : false;
				String check = reuse.request.requestArguments.get("displayFile");
				boolean displayFile = check != null ? check.equals("1") || check.equalsIgnoreCase("true") : false;
				
				String ext = FilenameUtils.getExtension(info.file.getAbsolutePath());
				
				if(displayFile && domainDirectory.getEnableReadableFileViews()) {
					if(info.mimeType.equalsIgnoreCase("text/rtf") || info.mimeType.equalsIgnoreCase("application/rtf")) {
						final String charset = StringUtils.getDetectedEncoding(info.file);
						reuse.response.setHeader("Content-Type", "text/html; charset=" + charset);
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
						reuse.response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
						reuse.response.setResponse(StringUtils.rtfToHtml(info.file));
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						//connectedClients.remove(clientInfo);
						return reuse.response.getStatusCode();
					} else if(info.mimeType.equalsIgnoreCase("application/epub+zip")) {
						Charset charset = null;
						try {
							charset = Charset.forName(StringUtils.getCharsetOfBook(info.file));
						} catch(Throwable ignored) {
							charset = StandardCharsets.UTF_8;
						}
						reuse.response.setHeader("Content-Type", "text/html; charset=" + charset);
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
						reuse.response.setHeader("Accept-Ranges", "none");
						reuse.response.setResponse(StringUtils.readEpubBook(info.file));
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						//connectedClients.remove(clientInfo);
						return reuse.response.getStatusCode();
					} else if(info.mimeType.startsWith("text/")) {
						Charset charset = null;
						try {
							charset = Charset.forName(StringUtils.getDetectedEncoding(info.file));
						} catch(Throwable ignored) {
							charset = StandardCharsets.UTF_8;
						}
						reuse.response.setHeader("Content-Type", "text/plain; charset=" + charset);//The whole point of 'displayFile' is to get the browser to display the file instead of attempting to download it.
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
						reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
						reuse.response.setHeader("Accept-Ranges", "none");
						reuse.response.setResponse(FileUtil.readFileData(info.file));
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						//connectedClients.remove(clientInfo);
						return reuse.response.getStatusCode();
					}
				} else if(mediaInfo) {
					//TODO if(domainDirectory.getEnableMediaInfoParsing()) {
					if(FileSortData.isMimeTypeMedia(info.mimeType)) {
						if(artwork) {
							String failReason = "Failed to read artwork image data!!1";
							MediaArtwork data = null;
							try {
								data = MediaReader.readFile(info.file, true).getAlbumArtwork();
							} catch(Throwable e) {
								failReason = "Failed to read artwork data:\r\n<pre>" + StringUtil.throwableToStr(e) + "</pre>";
							}
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", "no-cache, must-revalidate");
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
							reuse.response.setHeader("Accept-Ranges", "none");
							if(data != null) {
								reuse.response.setHeader("Content-Type", data.mimeType);
								reuse.response.setHeader("Vary", "");//Remove the existing 'Vary' header
								reuse.response.setResponse(data.getData());
								reuse.response.setUseGZip(false);//The data is already compressed, this will only slow it down!
								data.close();
							} else {
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
										"<html>\r\n\t<head>\r\n" + //
										"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
										"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
										"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
										"\t\t<title>" + info.file.getName() + " - Media Artwork - " + domainDirectory.getServerName() + "</title>\r\n" + //
										"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
										(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
										AUTO_RESIZE_JAVASCRIPT + //
										(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
										"\t</head>\r\n" + //
										"\t<body>\r\n" + //
										"\t\t" + pageHeader + "<br>\r\n" + //
										"\t\t" + failReason + "<br>\r\n" + //
										"\t\t<a href=\"" + fileLink + "\">Click to " + (FileSortData.isMimeTypeAudio(info.mimeType) ? "listen" : "watch") + "</a>\r\n" + //
										(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
										(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
										"\t</body>\r\n</html>");
							}
						} else {
							String mInfo = MediaReader.getMediaInfoHTMLFor(info.file, domainDirectory.getDirectorySafe().getAbsolutePath());
							if(mInfo.contains("{0}")) {//Unable to parse media tags for file.
								mInfo = mInfo.replace("{0}", StringUtil.encodeHTML(reuse.request.requestedFilePath));
							}
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Expires", StringUtils.getCacheTime(System.currentTimeMillis() + 300000L));//5 minutes from now
							reuse.response.setHeader("Cache-Control", "no-cache, must-revalidate");
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
							reuse.response.setHeader("Accept-Ranges", "none");
							reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
									"<html>\r\n\t<head>\r\n" + //
									"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
									"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
									"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
									"\t\t<title>" + info.file.getName() + " - Media Info - " + domainDirectory.getServerName() + "</title>\r\n" + //
									"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
									(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
									AUTO_RESIZE_JAVASCRIPT + //
									(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
									"\t</head>\r\n" + //
									"\t<body>\r\n" + //
									"\t\t" + pageHeader + "<br>\r\n" + //
									"\t\t" + mInfo + "\r\n" + //
									(autoplay ? "\t\t<" + (FileSortData.isMimeTypeVideo(info.mimeType) ? "video" : "audio") + " autoplay controls=\"\" preload=\"auto\" name=\"" + info.file.getName() + "\"><source src=\"" + fileLink + "\" type=\"" + info.mimeType + "\"></" + (FileSortData.isMimeTypeVideo(info.mimeType) ? "video" : "audio") + ">" : "\t\t<a href=\"" + fileLink + "\">Click to " + (FileSortData.isMimeTypeAudio(info.mimeType) ? "listen" : "watch") + "</a>&nbsp;\r\n") + //
									(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
									(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
									"\t</body>\r\n</html>");
						}
						//} else {
						//(Tell the client media info parsing is not enabled for this domain)
						//}
					} else {
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Expires", StringUtils.getCacheTime(System.currentTimeMillis() + 60000L));//1 minute from now
						reuse.response.setHeader("Cache-Control", "no-cache, must-revalidate");
						reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
						reuse.response.setHeader("Accept-Ranges", "none");
						reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
								"<html>\r\n\t<head>\r\n" + //
								"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
								"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
								"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
								"\t\t<title>" + info.file.getName() + " - Media Info - " + domainDirectory.getServerName() + "</title>\r\n" + //
								"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
								(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
								AUTO_RESIZE_JAVASCRIPT + //
								(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
								"\t</head>\r\n" + //
								"\t<body>\r\n" + //
								"\t\t" + pageHeader + "<br>\r\n" + //
								"\t\t<string>The file &quot;" + reuse.request.requestedFilePath + "&quot; is not a media file(audio/***, video/***, etc.) or has an unrecognized MIME type.<br>The file's MIME type is:&nbsp;<pre>" + info.mimeType + "</pre></string>\r\n" + //
								"\t\t<a href=\"" + fileLink + "\">Open/download file</a>&nbsp;\r\n" + //
								(previousPageLink == null ? "" : "\t\t<a href=\"" + previousPageLink + "\">Back to previous page</a>&nbsp;\r\n") + //
								(parentFileLink == null ? "" : "\t\t<a href=\"" + parentFileLink + "\">Back to " + (parentFile != null ? parentFile.getName() : "[parent file]") + "</a>\r\n") + //
								"\t</body>\r\n</html>");
					}
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//s.close();
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				}
				String line = "";
				if(ext.equals("url") && !(line = StringUtils.getUrlLinkFromFile(info.file)).isEmpty()) {
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
					reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
					reuse.response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
					reuse.response.setHeader("Access-Control-Allow-Origin", "*");
					long random = ThreadedSecureRandom.get().nextLong();
					reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
							"<html>\r\n\t<head>\r\n" + //
							"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
							"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
							"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
							"\t\t<title>" + info.file.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
							"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
							(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
							AUTO_RESIZE_JAVASCRIPT + //
							(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
							"\t</head>\r\n" + //
							"\t<body>\r\n" + //
							"\t\t" + pageHeader + "\r\n" + //
							(reuse.request.refererLink.isEmpty() ? "" : "\t\t<a href=\"" + reuse.request.refererLink + "\">Back to previous page</a><br>\r\n") + //
							"\t\t<iframe src=\"" + line + "\" sandbox=\"allow-forms allow-scripts\" frameborder=\"1\" width=\"100%\" height=\"85%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe><hr>\r\n" + //
							"\t\t<a href=\"" + line + "\" target=\"_blank\">[link to url]</a>\r\n" + //
							"\t</body>\r\n</html>");
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//s.close();
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				} else if(info.mimeType.startsWith("text/")) {// && !info.mimeType.equalsIgnoreCase("text/html") && !info.mimeType.equalsIgnoreCase("text/xml") && !info.mimeType.equalsIgnoreCase("text/css")
					final String charset = StringUtils.getDetectedEncoding(info.file);
					reuse.response.setHeader("Content-Type", info.mimeType + "; charset=" + charset);
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
					reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
					reuse.response.setHeader("Accept-Ranges", "none");
					reuse.response.setResponse(StringUtils.getTextFileAsString(info.file));
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//s.close();
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				} else if(autoClose && FileSortData.isMimeTypeMedia(info.mimeType)) {
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
					reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
					reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
					reuse.response.setHeader("Accept-Ranges", (allowByteRangeRequests ? "bytes" : "none"));
					long random = ThreadedSecureRandom.get().nextLong();
					reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
							"<html>\r\n\t<head>\r\n" + //
							"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
							"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
							"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
							"\t\t<title>" + info.file.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
							"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
							(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
							AUTO_RESIZE_JAVASCRIPT + //
							(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
							"\t</head>\r\n" + //
							"\t<body>\r\n" + //
							"\t\t" + pageHeader + "\r\n" + //
							"<video controls=\"\" autoplay=\"metadata\" name=\"media\" onended=\"window.close();\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"><source src=\"" + fileLink + "\" type=\"" + info.mimeType + "\"></video>" + //
							"\t</body>\r\n</html>");
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//s.close();
					//connectedClients.remove(clientInfo);
					return reuse.response.getStatusCode();
				}
				reuse.response.setHTTPVersion(versionToUse);
				reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
				String mimeType = info.mimeType;
				if(ext.equalsIgnoreCase("m4a") && reuse.request.userAgent.contains("VLC/")) {
					mimeType = "audio/x-m4a";//XXX Fix for VLC when streaming m4a files, tested and works!
				}
				reuse.response.setHeader("Content-Type", mimeType);
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
				reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
				reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
				reuse.response.setHeader("Accept-Ranges", (allowByteRangeRequests ? "bytes" : "none"));
				if(reuse.request.icy_MetaData.equals("1") && FileSortData.isMimeTypeMedia(mimeType)) {//reuse.request.userAgent.contains("VLC/")) {
					String failReason = null;
					try(MediaInfo data = MediaReader.readFile(info.file, true)) {//No potential NPE here, Java is cool like that :D
						if(data != null) {
							if(data.beatsPerMinute != null && !data.beatsPerMinute.trim().isEmpty()) {
								reuse.response.setHeader("icy-br", data.beatsPerMinute.replace("\r\n", "").replace("\r", " ").replace("\n", ""));
							}
							if(data.genre != null && !data.genre.trim().isEmpty()) {
								String genre = data.genre.replace("\r\n", "").replace("\r", " ").replace("\n", "");
								reuse.response.setHeader("icy-genre", genre);
							}
						}
					} catch(Throwable e) {
						failReason = " /!\\ \tFailed to read artwork data:\r\n/___\\\t" + StringUtil.throwableToStrNoStackTraces(e);
					}
					if(failReason != null) {
						reuse.printErrln(failReason);
					}
				}
				reuse.response.setResponse(info);
				try {
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//reuse.println("\t\t\tSent file \r\n\t\t\t\"" + FilenameUtils.normalize(info.file.getAbsolutePath()) + "\"\r\n\t\t\t to client \"" + clientAddress + "\" successfully.");
				} catch(Throwable e) {
					if(!isExceptionIgnored(e)) {
						reuse.println("\t /!\\ \tFailed to send file \r\n\t/___\\\t\"" + FilenameUtils.normalize(info.file.getAbsolutePath()) + "\"\r\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
					}
				}
				//s.close();
				//connectedClients.remove(clientInfo);
				return reuse.response.getStatusCode();
			}
			reuse.response.setStatusCode(HTTP_304);
			reuse.response.setHeader("Vary", "Accept-Encoding");
			reuse.response.setHeader("Server", SERVER_NAME_HEADER);
			reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
			reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(info.file.lastModified()));
			reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
			reuse.response.setHeader("Accept-Ranges", allowByteRangeRequests ? "bytes" : "none");
			reuse.response.setResponse((String) null);
			reuse.response.sendToClient(s, false);
			//s.close();
			//connectedClients.remove(clientInfo);
			return reuse.response.getStatusCode();
		}
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
		final String HTTP_Code = connectionSeemsMalicious ? "429 Too Many Requests" : "200 OK";
		reuse.response.setStatusCode(connectionSeemsMalicious ? HTTP_429 : HTTP_200);
		
		reuse.response.setStatusMessage("Directory listing");//reuse.println("\t*** " + versionToUse + " " + HTTP_Code + " - Directory listing");//XXX Directory Listing
		FolderData.getFolderData(info.file, true).incrementTimesAccessed();
		out.println(versionToUse + " " + HTTP_Code);
		out.println("Vary: Accept-Encoding");
		out.println("Server: " + SERVER_NAME_HEADER);
		out.println("Date: " + StringUtils.getCurrentCacheTime());
		out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
		out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(info.file, s) ? cachePrivateMustRevalidate : (info.file.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
		out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
		if(reuse.allowReuse) {
			out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
		}
		out.println("Accept-Ranges: none");
		//String[] c = requestedFile.list();
		String responseStr;
		
		final String greenBorder = "<p class=\"green_border\">";
		final String brdrEnd = "</p>";
		
		String xspfCheck = reuse.request.requestArguments.get("xspf");
		final boolean xspf = reuse.request.userAgent.contains("VLC/") || (xspfCheck != null ? (xspfCheck.equals("1") || xspfCheck.equalsIgnoreCase("true")) : false);
		String xmlCheck = reuse.request.requestArguments.get("xml");
		final boolean xml = xmlCheck != null ? (xmlCheck.equals("1") || xmlCheck.equalsIgnoreCase("true")) : false;
		String mediaViewCheck = reuse.request.requestArguments.get("mediaView");
		final boolean mediaViewFlag = mediaViewCheck != null ? (mediaViewCheck.equals("1") || mediaViewCheck.equalsIgnoreCase("true")) : false;
		String mediaListCheck = reuse.request.requestArguments.get("mediaList");
		final boolean mediaListFlag = mediaListCheck != null ? (mediaListCheck.equals("1") || mediaListCheck.equalsIgnoreCase("true")) : false;
		
		final boolean includeFolders = xml || !(xspf || mediaViewFlag || mediaListFlag);
		
		final FileSortData files = FileSortData.sort(s, reuse, info.file, domainDirectory, fileLink, reuse.request.requestArguments, includeFolders);
		if(files == FileSortData.connectionSevered) {
			reuse.response.setStatusCode(HTTP_499);
			out.close();
			//connectedClients.remove(clientInfo);
			return reuse.response.getStatusCode();
		}
		final ArrayList<Integer> folderPaths = files.getFolderPaths();
		final ArrayList<Integer> filePaths = files.getFilePaths();
		
		String linkTarget = reuse.request.requestArguments.get("target");
		linkTarget = (linkTarget != null && !linkTarget.isEmpty() ? " target=\"" + linkTarget + "\"" : "");
		
		reuse.status.setContentLength(files.size());
		final String randomLink = domainDirectory.getEnableFilterView() ? MapUtil.getRandomLinkFor(files.files, path, pathPrefix, domainDirectory, httpProtocol, reuse) : null;
		final String requestArgsNoSort = StringUtils.requestArgumentsToString(reuse.request.requestArguments, "sort");
		
		final boolean enableMediaInfo = files.areThereMediaFiles;//TODO && domainDirectory.getEnableMediaInfoParsing();
		final boolean filterFlag = files.filter != null && !files.filter.trim().isEmpty();
		
		final String addFilterStr = (filterFlag ? "&filter=" + files.filter : "");
		final String addSortStr = (info.file.isDirectory() && !files.wasSortNull ? "&sort=" + (files.isSortReversed ? "-" : "") + files.sort : "");
		final boolean isSortRandom = (info.file.isDirectory() && !files.wasSortNull && files.sort.startsWith("random_") && files.sort.length() > 7);
		final boolean isFolderRoot = FilenameUtils.normalize(info.file.getAbsolutePath()).equalsIgnoreCase(homeDirPath);
		
		final boolean normalViewFlag = !mediaViewFlag && !mediaListFlag;
		
		final String normalView = "\t\t<td>" + (normalViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "\">Normal View</a></b>" + (normalViewFlag ? brdrEnd : "") + "</td>\r\n";
		final String mediaView = (domainDirectory.getEnableMediaView() ? "\t\t<td>" + (mediaViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "?mediaView=1" + addFilterStr + "\">Media View</a></b>" + (mediaViewFlag ? brdrEnd : "") + "</td>\r\n" : "");
		final String mediaList = (domainDirectory.getEnableMediaList() ? "\t\t<td>" + (mediaListFlag && !mediaViewFlag ? greenBorder : "") + "<b><a href=\"" + fileLink + "?mediaList=1" + addSortStr + addFilterStr + "\">Media List</a><a href=\"" + fileLink + "?mediaList=1&autoplay=1" + addSortStr + addFilterStr + "\">[autoplay]</a></b>" + (mediaListFlag && !mediaViewFlag ? brdrEnd : "") + "</td>\r\n" : "");
		final String xmlView = (domainDirectory.getEnableXmlListView() ? "\t\t<td><b><a href=\"" + fileLink + "?xml=1" + addFilterStr + "\">Xml View</a></b></td>\r\n" : "");
		final String xspfView = (domainDirectory.getEnableVLCPlaylistView() && files.areThereMediaFiles ? "\t\t<td><b><a href=\"" + fileLink + "?xspf=1" + addFilterStr + (isSortRandom ? addSortStr : "") + "\" download=\"" + (isFolderRoot ? domainDirectory.getDomain().replace(":", ";") : FilenameUtils.getName(info.file.getAbsolutePath())) + ".xspf\" title=\"Download and save as '*.xspf', then open with VLC\">VLC Playlist View</a></b></td>\r\n" : "");
		final String filterView = (domainDirectory.getEnableFilterView() && !files.options.isEmpty()) ? "\t\t<td>" + (filterFlag ? greenBorder : "") + "<b>Filter view:&nbsp;</b><select onChange=\"window.location.href=this.value\" title=\"Filter list\">\r\n"//
				+ files.options + "\t\t</select>" + (filterFlag ? brdrEnd : "") + "</td>\r\n" : (domainDirectory.getEnableFilterView() ? "<td><b>Filter view: (No files to filter)</b></td>\r\n" : "");
		final String randomLinkView = (randomLink != null ? "\t\t<td><b><a href=\"" + randomLink + "\" rel=\"nofollow\"" + linkTarget + " title=\"(You may need to refresh the page for the next random file to appear)\">Open random file</a></b>&nbsp;</td>\r\n" : "");
		final String randomView = (files.areThereFiles || files.areThereDirectories ? "\t\t<td><string>[<a href=\"" + fileLink + requestArgsNoSort + (requestArgsNoSort.isEmpty() ? "?" : "&") + "sort=random\" rel=\"nofollow\" target=\"_blank\" title=\"Click to open a new view with folder content randomized\">Random view</a>]</string></td>\r\n" : "");
		final String bodyHeader = pageHeader + "\r\n" + (domainDirectory.getEnableAlternateDirectoryListingViews() ? "<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\"><tbody>" + normalView + xmlView + mediaView + (files.areThereMediaFiles ? mediaList : "") + xspfView + filterView + randomLinkView + randomView + "</tbody></table><hr>\r\n" : "");
		
		final String backLink = fileLink + (addSortStr.isEmpty() ? "" : "?" + addSortStr.substring(1)) + (addFilterStr.isEmpty() ? "" : (addSortStr.isEmpty() ? "?" : "&") + addFilterStr.substring(1));
		
		if((xspf && domainDirectory.getEnableVLCPlaylistView()) || reuse.request.userAgent.contains("VLC/")) {
			out.println("Content-Type: " + (reuse.request.userAgent.contains("VLC/") ? "application/xspf+xml" : "application/xml; charset=UTF-8"));//XXX Adding the charset information causes VLC to not recognize the file as *.xspf, making the whole point moot when streaming the playlist to VLC(downloaded and saved versions work fine)
			responseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			responseStr += "<playlist xmlns=\"http://xspf.org/ns/0/\" xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\" version=\"1\">\r\n";
			responseStr += "\t<title>Playlist</title>\r\n";
			responseStr += "\t<trackList>\r\n";
			int numOfMediaFiles = 0;
			String trackList = "";
			
			Set<Entry<Integer, String>> entrySet = files.entrySet();
			int count = 0;
			reuse.status.setContentLength(files.size());
			reuse.status.setCount(0L);
			for(Entry<Integer, String> entry : entrySet) {
				//CodeUtil.sleep(10L);
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				//final ArrayList<Integer> filePaths = files.getFilePaths();
				final String filePath;
				if(files.useSortView) {
					if(!filePaths.isEmpty()) {
						filePath = files.get(filePaths.remove(0));
					} else {
						continue;
					}
				} else {
					filePath = entry.getValue();
				}
				reuse.status.checkForPause();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(reuse.status.isCancelled()) {
					reuse.response.setStatusCode(HTTP_503).setStatusMessage("Client request cancelled");
					out.close();
					s.close();
					return reuse.response.getStatusCode();
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
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
						final String ext = FilenameUtils.getExtension(file.getAbsolutePath());
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringEscapeUtils.escapeXml10(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						
						final String mimeType = domainDirectory.getMimeTypeForExtension(ext).toLowerCase();
						if(FileSortData.isMimeTypeMedia(mimeType)) {
							final MediaInfo media = MediaReader.readFile(file, false);
							trackList += "\t\t<track>\r\n";
							trackList += "\t\t\t<location>" + StringEscapeUtils.escapeXml10(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias((newPath.startsWith("/") ? "" : "/") + newPath))) + "</location>\r\n";
							trackList += "\t\t\t<title>" + StringEscapeUtils.escapeXml10(media != null && media.title != null && !media.title.isEmpty() ? media.title : FilenameUtils.getName(file.getAbsolutePath())) + "</title>\r\n";
							trackList += media != null ? (media.artist != null && !media.artist.isEmpty() ? "\t\t\t<creator>" + StringEscapeUtils.escapeXml10(media.artist) + "</creator>\r\n" : "") : "";
							trackList += media != null ? (media.album != null && !media.album.isEmpty() ? "\t\t\t<album>" + StringEscapeUtils.escapeXml10(media.album) + "</album>\r\n" : "") : "";
							trackList += media != null ? (media.hasArtwork() ? "\t\t\t<image>" + StringEscapeUtils.escapeXml10(curFileLink + "?mediaInfo=1&artwork=1") + "</image>\r\n" : "") : "";
							trackList += media != null ? "\t\t\t<duration>" + Math.round(media.trackLengthDouble * 1000L) + "</duration>\r\n" : "";
							trackList += "\t\t\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\r\n";
							trackList += "\t\t\t\t<vlc:id>" + numOfMediaFiles + "</vlc:id>\r\n";
							if(JavaWebServer.VLC_NETWORK_CACHING_MILLIS >= 250 && JavaWebServer.VLC_NETWORK_CACHING_MILLIS <= 10000) {
								trackList += "\t\t\t\t<vlc:option>network-caching=" + Integer.toString(JavaWebServer.VLC_NETWORK_CACHING_MILLIS) + "</vlc:option>\r\n";
							}
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
				reuse.status.markWriteTime();
				reuse.status.setCount(count++);
				/*clientInfo.bytesTransfered++;
				clientInfo.lastWriteTime = System.currentTimeMillis();
				clientInfo.lastWriteAmount = 1;
				clientInfo.currentWriteAmount = 1;*/
				//CodeUtil.sleep(10L);
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
			responseStr += "\t<last-modified>" + StringEscapeUtils.escapeXml10(StringUtils.getCacheTime(info.file.lastModified())) + "</last-modified>\r\n";
			responseStr += "\t<subfilesnum>" + files.size() + "</subfilesnum>\r\n";
			responseStr += "\t<src>" + StringEscapeUtils.escapeXml10(backLink) + "</src>\r\n";
			responseStr += "\t<client>" + StringEscapeUtils.escapeXml10(clientAddress) + "</client>\r\n";
			
			String fileTable = "";
			reuse.status.setContentLength(files.size());
			reuse.status.setCount(0L);
			int count = 0;
			for(Entry<Integer, String> entry : files.entrySet()) {
				String filePath = entry.getValue();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				reuse.status.checkForPause();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(reuse.status.isCancelled()) {
					reuse.response.setStatusCode(HTTP_503).setStatusMessage("Client request cancelled");
					out.close();
					s.close();
					return reuse.response.getStatusCode();
				}
				String backup = fileTable;
				try {
					String newPath = path + "/" + filePath;
					newPath = newPath.startsWith("//") ? newPath.substring(1) : newPath;
					File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						FileInfo curInfo = new FileInfo(file, domainDirectory);//contentLength used
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringEscapeUtils.escapeXml10(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
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
				reuse.status.markWriteTime();
				reuse.status.setCount(count++);
				/*clientInfo.bytesTransfered++;
				clientInfo.lastWriteTime = System.currentTimeMillis();
				clientInfo.lastWriteAmount = 1;
				clientInfo.currentWriteAmount = 1;*/
			}
			
			responseStr += "\t<subfiles>\r\n" + fileTable + "\t</subfiles>\r\n";
			responseStr += "</directory>";
		} else if(mediaViewFlag && domainDirectory.getEnableMediaView()) {
			boolean autoplayFlag = autoplay;
			out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			
			String fileTable = "";
			int numOfPlayableMediaFiles = 0;
			for(Entry<Integer, String> entry : files.entrySet()) {
				String filePath = entry.getValue();
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				String newPath = path + "/" + filePath;
				newPath = newPath.startsWith("//") ? newPath.substring(1) : newPath;
				final File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
				if(file.exists() && FileSortData.isMimeTypeMedia(FileInfo.getMIMETypeFor(file, domainDirectory))) {
					numOfPlayableMediaFiles++;
				}
			}
			int currentMediaFile = 0;
			int count = 0;
			reuse.status.setContentLength(files.size());
			reuse.status.setCount(0L);
			for(Entry<Integer, String> entry : files.entrySet()) {
				Integer i = entry.getKey();
				String filePath = entry.getValue();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				reuse.status.checkForPause();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(reuse.status.isCancelled()) {
					reuse.response.setStatusCode(HTTP_503).setStatusMessage("Client request cancelled");
					out.close();
					s.close();
					return reuse.response.getStatusCode();
				}
				if(filePath == null || filePath.isEmpty()) {
					continue;
				}
				try {
					String newPath = path + "/" + filePath;
					newPath = newPath.startsWith("//") ? newPath.substring(1) : newPath;
					final File file = new File(FilenameUtils.normalize(pathPrefix + newPath));
					if(file.exists()) {
						final String mimeType = FileInfo.getMIMETypeFor(file, domainDirectory);
						final String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String fileName = FilenameUtils.getName(file.getAbsolutePath());
						final String ext = FilenameUtils.getExtension(file.getAbsolutePath());
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						final String line1 = "\t\t<string>" + (domainDirectory.getNumberDirectoryEntries() ? "<b>[" + (i.intValue() + 1) + "]:</b> " : "") + "</string><a href=\"" + curFileLink + "\" target=\"_blank\">" + fileName + "</a><br>\r\n";
						final long random = ThreadedSecureRandom.get().nextLong();
						final String lineEnd = "<hr>\r\n";
						if(FileSortData.isMimeTypeMedia(mimeType)) {
							fileTable += line1 + //
									"\t\t<video controls=\"\" id=\"video_" + currentMediaFile + "\" preload=\"auto\" " + (autoplayFlag ? "autoplay=\"autoplay\"" : "") + "name=\"" + fileName + "\">\r\n" + //
									"\t\t\t<source src=\"" + curFileLink + "\" type=\"" + mimeType + "\">\r\n\t\t</video>" + lineEnd;
							if(currentMediaFile < numOfPlayableMediaFiles) {
								fileTable += "\t\t<script type='text/javascript'>\r\n" + //
										"document.getElementById('video_" + (currentMediaFile++) + "').addEventListener('ended',playNextMedia,false);\r\n" + //
										"\tfunction playNextMedia(e) {\r\n" + //
										"\t\tif(document.getElementById('video_" + currentMediaFile + "') !== null && document.getElementById('video_" + currentMediaFile + "').paused) {\r\n" + //
										"\t\t\tdocument.getElementById('video_" + currentMediaFile + "').play();\r\n" + //
										"\t\t}\r\n" + //
										"\t}\r\n" + //
										"</script>\r\n";
							}
							if(autoplayFlag) {
								autoplayFlag = false;//prevents multiple videos/songs from playing at the same time on the same page
							}
						} else if(!ext.equals("lnk") && mimeType.startsWith("text/") && !ext.equals("url")) {
							fileTable += line1 + //
									"\t\t<iframe src=\"" + curFileLink + "\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
						} else if(mimeType.startsWith("image/")) {
							Dimension imgSize = ImageUtil.getImageSizeFromFile(file);
							fileTable += line1 + "\t\t<img style=\"-webkit-user-select: none\" src=\"" + curFileLink + "\"" + (imgSize != null ? "width=\"" + imgSize.getWidth() + "\" height=\"" + imgSize.getHeight() + "\"" : "") + ">" + lineEnd;
						} else if(mimeType.equals("application/pdf")) {
							fileTable += line1 + "\t\t<embed name=\"plugin\" width=\"100%\" src=\"" + curFileLink + "\" type=\"" + mimeType + "\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\">" + lineEnd;
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
								fileTable += line1 + "\t\t<iframe src=\"" + line + "\" sandbox=\"allow-forms allow-scripts\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
							} catch(Throwable ignored) {
								fileTable = backup + line1 + "\t\t<iframe src=\"" + curFileLink + "\" frameborder=\"1\" width=\"100%\" id=\"" + random + "\" onload=\"autoResize('" + random + "');\"></iframe>" + lineEnd;
							}
						}
					}
				} catch(FileNotFoundException ignored) {
				} catch(Throwable e) {
					e.printStackTrace();
				}
				reuse.status.markWriteTime();
				reuse.status.setCount(count++);
				/*clientInfo.bytesTransfered++;
				clientInfo.lastWriteTime = System.currentTimeMillis();
				clientInfo.lastWriteAmount = 1;
				clientInfo.currentWriteAmount = 1;*/
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
					(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
					"\t</head>\r\n" + //
					"\t<body>\r\n" + //
					"\t\t" + bodyHeader + "\r\n" + //
					(fileTable.isEmpty() ? "\t\t<string>No media content to display.</string>&nbsp;<a href=\"" + backLink + "\">Back to " + info.file.getName() + "</a><br>\r\n" : "<string>Listing media files(text, images, audio, video, etc.) in <a href=\"" + backLink + "\" title=\"(Click to go back to " + info.file.getName() + ")\">" + info.file.getName() + "</a>:</string><br>" + fileTable) + //
					"\t</body>\r\n" + //
					"</html>";
		} else if(mediaListFlag && domainDirectory.getEnableMediaList()) {
			out.println("Expires: " + StringUtil.getCacheTime(System.currentTimeMillis() + 300000L));//5 minutes from now
			out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
			int count = 0;
			int j = 0;
			String fileTable = (files.random == -1L ? "" : "\t\t<string>[<a href=\"" + fileLink + requestArgsNoSort + (requestArgsNoSort.isEmpty() ? "?" : "&") + "sort=random_" + files.random + "\" rel=\"nofollow\" title=\"(Permanent link to this randomly generated page)\">Permalink to random list</a>]</string><hr>\r\n");
			Set<Entry<Integer, String>> entrySet = files.entrySet();
			final int amt = entrySet.size();
			reuse.status.setContentLength(amt);
			reuse.status.setCount(0L);
			for(Entry<Integer, String> entry : entrySet) {
				reuse.status.markWriteTime();
				reuse.status.setCount(count++);
				final boolean endOfLoop = count == amt;
				//CodeUtil.sleep(10L);
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				//final ArrayList<Integer> filePaths = files.getFilePaths();
				final Integer i;
				final String filePath;
				if(files.useSortView) {
					if(!filePaths.isEmpty()) {
						filePath = files.get((i = filePaths.remove(0)));
					} else {
						continue;
					}
				} else {
					i = entry.getKey();
					filePath = entry.getValue();
				}
				reuse.status.checkForPause();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(reuse.status.isCancelled()) {
					reuse.response.setStatusCode(HTTP_503).setStatusMessage("Client request cancelled");
					out.close();
					s.close();
					return reuse.response.getStatusCode();
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
						final String mimeType = FileInfo.getMIMETypeFor(file, domainDirectory);
						if(!FileSortData.isMimeTypeMedia(mimeType)) {
							continue;
						}
						final boolean isVideo = FileSortData.isMimeTypeVideo(mimeType);
						final String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String fileName = FilenameUtils.getName(file.getAbsolutePath());
						final String fileReqPath = StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath));
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host + fileReqPath);
						fileTable += "" + (domainDirectory.getNumberDirectoryEntries() ? "<b>[" + (i.intValue() + 1) + "]:</b> " : "")//
								+ "\t\t<button title=\"Click to show/hide media content\" type=\"button\" onclick=\""//
								+ "var spoiler = document.getElementById('spoiler_" + Integer.toString(j) + "');\r\n"//
								+ "if(spoiler.style.display=='none') {\r\n"//
								+ "	spoiler.style.display=''\r\n"//
								+ "} else {\r\n"//
								+ "	spoiler.style.display='none'\r\n"//
								+ "}\r\n\"><a href=\"" + curFileLink + "\" target=\"_blank\" name=\"" + fileName + "\">" + fileName + "</a></button>\r\n";//lmao
						fileTable += "\t\t<div id=\"spoiler_" + Integer.toString(j) + "\" style=\"\">\r\n" + //
								"\t\t\t" + MediaReader.getMediaInfoHTMLFor(file, domainDirectory.getDirectorySafe().getAbsolutePath()).replace("{0}", fileReqPath) + "\r\n" + //
								"\t\t\t<" + (isVideo ? "video" : "audio") + (autoplay ? " id=\"autoplay_" + j + "\"" : "") + " controls=\"\" " + (autoplay && j == 0 ? "autoplay=\"autoplay\" " : "") + "preload=\"" + (autoplay ? (j == 0 ? "auto" : "metadata") : "metadata") + "\" name=\"" + fileName + "\">\r\n" + //
								"\t\t\t<source src=\"" + curFileLink + "\" type=\"" + mimeType + "\">\r\n\t\t</" + (isVideo ? "video" : "audio") + ">" + //
								"\t\t</div><hr>\r\n";
						if(autoplay && !endOfLoop) {//XXX mediaList + autoplay = smiley-face
							//fileTable += "\t\t<script type='text/javascript'>\r\n	var video = document.getElementsByTagName('video')[" + j + "];\r\n	video.onended = function(e) {\r\n		document.getElementsByTagName('video')[" + (j + 1) + "].play();\r\n	};\r\n</script>\r\n";
							fileTable += ""//
									+ "\t\t<script type='text/javascript'>\r\n"//
									+ "var video = document.getElementById('autoplay_" + j + "');\r\n"//
									+ "video.onended = function(e) {\r\n"//
									+ "	document.getElementById('autoplay_" + Integer.toString(j + 1) + "').play();\r\n"//
									+ "};\r\n</script>\r\n";
						}
						j++;
					}
				} catch(Throwable e) {
					e.printStackTrace();
				}
				/*clientInfo.bytesTransfered++;
				clientInfo.lastWriteTime = System.currentTimeMillis();
				clientInfo.lastWriteAmount = 1;
				clientInfo.currentWriteAmount = 1;*/
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
					(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
					"\t</head>\r\n" + //
					"\t<body>\r\n" + //
					"\t\t" + bodyHeader + "\r\n" + //
					(fileTable.isEmpty() ? "\t\t<string>No media content to display.</string>&nbsp;<a href=\"" + backLink + "\">Back to " + info.file.getName() + "</a><br>\r\n" : "<string>Listing tag information for media files in <a href=\"" + backLink + "\" title=\"(Click to go back to " + info.file.getName() + ")\">" + info.file.getName() + "</a>:</string><br>" + fileTable) + //
					"\t</body>\r\n" + //
					"</html>";
		} else {
			out.println("Content-Type: text/html; charset=UTF-8");
			
			String fileTable = "\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n" + //TODO File table settings - Make these configurable per-domain!
					"\t\t\t<tbody>\r\n";
			
			RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(info.file);
			res = res == null ? RestrictedFile.getRestrictedFile(info.file) : res;
			final boolean canUsersModifyFiles = res != null ? res.canUsersModifyFiles() : false;
			
			if(domainDirectory.getEnableSortView()) {
				final String link = fileLink + requestArgsNoSort + (requestArgsNoSort.isEmpty() ? "?" : "&");
				final String triangleStr = (files.isSortReversed ? "&nbsp;\u25B2" : "&nbsp;\u25BC");
				String numberSortLink = "<b><a href=\"" + link + "sort=" + (files.sort.equalsIgnoreCase("numbers") && !files.isSortReversed ? "-" : "") + "numbers\" color=\"#000000\"" + linkTarget + ">#" + (files.sort.equalsIgnoreCase("numbers") ? triangleStr : "") + "</a></b>";
				String fileNameSortLink = "<b><a href=\"" + link + "sort=" + (files.sort.equalsIgnoreCase("fileNames") && !files.isSortReversed ? "-" : "") + "fileNames\" color=\"#000000\"" + linkTarget + ">File Name" + (files.sort.equalsIgnoreCase("fileNames") ? triangleStr : "") + "</a></b>";
				String sizeSortLink = "<b><a href=\"" + link + "sort=" + (files.sort.equalsIgnoreCase("sizes") && !files.isSortReversed ? "-" : "") + "sizes\" color=\"#000000\"" + linkTarget + ">Size" + (files.sort.equalsIgnoreCase("sizes") ? triangleStr : "") + "</a></b>";
				String dateSortLink = "<b><a href=\"" + link + "sort=" + (files.sort.equalsIgnoreCase("dates") && !files.isSortReversed ? "-" : "") + "dates\" color=\"#000000\"" + linkTarget + ">Date" + (files.sort.equalsIgnoreCase("dates") ? triangleStr : "") + "</a></b>";
				String typeSortLink = "<b><a href=\"" + link + "sort=" + (files.sort.equalsIgnoreCase("types") && !files.isSortReversed ? "-" : "") + "types\" color=\"#000000\"" + linkTarget + ">Type" + (files.sort.equalsIgnoreCase("types") ? triangleStr : "") + "</a></b>";
				
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + numberSortLink + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td>" + fileNameSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + sizeSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + dateSortLink + "</td><td>" + nonBreakingSpaces + "</td><td>" + typeSortLink + "</td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td><b>Media Information</b></td>" : "") + (canUsersModifyFiles ? "<td>" + nonBreakingSpaces + "</td><td><b>(Management)</b></td>" : "") + "</tr>\r\n";
			} else {
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td><b>#</b></td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><b>File Name</b></td><td>" + nonBreakingSpaces + "</td><td><b>Size</b></td><td>" + nonBreakingSpaces + "</td><td><b>Date</b></td><td>" + nonBreakingSpaces + "</td><td><b>Type</b></td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td><b>Media Information</b></td>" : "") + (canUsersModifyFiles ? "<td>" + nonBreakingSpaces + "</td><td><b>(Management)</b></td>" : "") + "</tr>\r\n";
			}
			
			if(parentFileLink != null) {
				RestrictedFile parentRes = RestrictedFile.getSpecificRestrictedFile(parentFile);
				parentRes = parentRes == null ? RestrictedFile.getRestrictedFile(parentFile) : parentRes;
				FileInfo curInfo = new FileInfo(parentFile, domainDirectory);//contentLength used
				fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>(-)</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + parentFileLink + (!files.wasSortNull ? "?sort=" + (files.isSortReversed ? "-" : "") + files.sort : "") + "\"" + linkTarget + "><b>../(Up)</b></a></td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.mimeType + (RestrictedFile.isFileForbidden(parentFile) ? "\t\t(Forbidden)" : "") + "</td>" + (enableMediaInfo ? "</td><td>" + nonBreakingSpaces + "</td><td>(-)</td>" : "") + ((canUsersModifyFiles) ? "<td>" + nonBreakingSpaces + "</td><td>" + (parentRes != null && parentRes.canUsersModifyFiles() ? getManagementStrForFile(parentFileLink, curInfo.fileName, parentRes, false) : "(-)") + "</td></tr>\r\n" : "") + "</tr>\r\n";
			}
			
			/*long random = -1L;
			if(files.useSortView && !files.sort.equalsIgnoreCase("fileNames")) {//XXX Sorting
				if(files.sort.equalsIgnoreCase("sizes") || files.sort.equalsIgnoreCase("dates") || files.sort.equalsIgnoreCase("types")) {
					HashMap<Integer, FileInfo> fileInfos = new HashMap<>();
					for(Entry<Integer, String> entry : files.entrySet()) {
						//CodeUtil.sleep(10L);
						Integer i = entry.getKey();
						String filePath = entry.getValue();
						if(s.isClosed()) {
							reuse.response.setStatusCode(HTTP_499);
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
								fileInfos.put(i, new FileInfo(file, domainDirectory));//used for sorting
							}
						} catch(FileNotFoundException ignored) {
						} catch(Throwable e) {
							e.printStackTrace();
						}
					}
					filePaths.clear();
					folderPaths.clear();
					while(!fileInfos.isEmpty()) {
						if(files.sort.equalsIgnoreCase("sizes")) {
							filePaths.add(MapUtil.getSmallestFileInfoKeyInHashMapAndRemoveIt(fileInfos));
						} else if(files.sort.equalsIgnoreCase("dates")) {
							filePaths.add(MapUtil.getOldestFileInfoKeyInHashMapAndRemoveIt(fileInfos));
						} else if(files.sort.equalsIgnoreCase("types")) {
							filePaths.add(MapUtil.getLargestMimeTypeAlphabeticallyFromFileInfoKeyInHashMapAndRemoveIt(fileInfos));
						}
					}
					
				} else if(files.sort.equalsIgnoreCase("numbers")) {
					filePaths.addAll(files.keySet());
				} else if(files.sort.equalsIgnoreCase("random")) {
					filePaths.addAll(files.keySet());
					random = StringUtils.shuffle(filePaths);
				} else if(files.sort.startsWith("random_") && files.sort.length() > 7) {
					filePaths.addAll(files.keySet());
					String getRandom = files.sort.substring(7);
					if(StringUtils.isStrLong(getRandom)) {
						random = Long.valueOf(getRandom).longValue();
					} else {
						random = StringUtils.hash(getRandom);
					}
					StringUtils.shuffle(filePaths, random);
				}
				if((!files.sort.equalsIgnoreCase("types") && files.isSortReversed) || (files.sort.equalsIgnoreCase("types") && !files.isSortReversed)) {
					Collections.reverse(filePaths);
				}
			} else if(domainDirectory.getListDirectoriesFirst()) {//XXX Sorting folders and files in order
				for(Entry<Integer, String> entry : files.entrySet()) {
					//CodeUtil.sleep(10L);
					Integer i = entry.getKey();
					String filePath = entry.getValue();
					if(s.isClosed()) {
						reuse.response.setStatusCode(HTTP_499);
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
			if(domainDirectory.getListDirectoriesFirst() && files.sort.equalsIgnoreCase("fileNames") && files.isSortReversed) {
				ArrayList<Integer> folderPathsCopy = new ArrayList<>(folderPaths);
				ArrayList<Integer> filePathsCopy = new ArrayList<>(filePaths);
				folderPaths.clear();
				filePaths.clear();
				Collections.reverse(folderPathsCopy);
				Collections.reverse(filePathsCopy);
				folderPaths.addAll(filePathsCopy);
				filePaths.addAll(folderPathsCopy);
			}*/
			fileTable = (files.random == -1L ? fileTable : "\t\t<string>[<a href=\"" + fileLink + requestArgsNoSort + (requestArgsNoSort.isEmpty() ? "?" : "&") + "sort=random_" + files.random + "\" rel=\"nofollow\" title=\"(Permanent link to this randomly generated page)\">Permalink to random list</a>]</string><hr>\r\n" + fileTable);
			int count = 0;
			reuse.status.setContentLength(files.size());
			reuse.status.setCount(0L);
			for(Entry<Integer, String> entry : files.entrySet()) {
				//CodeUtil.sleep(10L);
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				final Integer i;
				final String filePath;
				if(files.useSortView) {
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
				reuse.status.checkForPause();
				if(s.isClosed()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					return reuse.response.getStatusCode();
				}
				if(reuse.status.isCancelled()) {
					reuse.response.setStatusCode(HTTP_499);
					out.close();
					s.close();
					return reuse.response.getStatusCode();
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
						FileInfo curInfo = new FileInfo(file, domainDirectory);//contentLength used
						String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
						final String curFileLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
						String mimeType = curInfo.mimeType;
						String extraViewStr = "";
						if(domainDirectory.getEnableReadableFileViews()) {
							if(curInfo.mimeType.equalsIgnoreCase("application/epub+zip") || curInfo.mimeType.equalsIgnoreCase("application/rtf") || curInfo.mimeType.equalsIgnoreCase("text/rtf")) {
								extraViewStr = nonBreakingSpaces + "<a href=\"" + curFileLink + "?displayFile=1\"" + linkTarget + "><b>*** Readable View ***</b></a>";
							}
						}
						mimeType += (RestrictedFile.isFileForbidden(file) ? "\t\t(Forbidden)" : "");
						RestrictedFile curRes = RestrictedFile.getSpecificRestrictedFile(file);
						curRes = curRes == null ? RestrictedFile.getRestrictedFile(file) : curRes;
						fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + (i.intValue() + 1) + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + curFileLink + (file.isDirectory() && !files.wasSortNull ? "?sort=" + (files.isSortReversed ? "-" : "") + files.sort : "") + "\"" + linkTarget + " name=\"" + curInfo.fileName + "\">" + curInfo.fileName + "</a>" + extraViewStr + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + mimeType + "</td>" + (enableMediaInfo && FileSortData.isMimeTypeMedia(curInfo.mimeType) ? "</td><td>" + nonBreakingSpaces + "</td><td><b><a href=\"" + curFileLink + "?mediaInfo=1\" title=\"(Opens in new tab/window)\" target=\"_blank\">Media Tags</a></b></td>" : "") + (canUsersModifyFiles ? "<td>" + nonBreakingSpaces + "</td><td>" + (curRes != null && curRes.canUsersModifyFiles() ? getManagementStrForFile(curFileLink, curInfo.fileName, curRes, false) : "(-)") + "</td></tr>\r\n" : "") + "</tr>\r\n";
					}
				} catch(FileNotFoundException ignored) {
				} catch(Throwable e) {
					e.printStackTrace();
				}
				reuse.status.markWriteTime();
				reuse.status.setCount(count++);
				/*clientInfo.bytesTransfered++;
				clientInfo.lastWriteTime = System.currentTimeMillis();
				clientInfo.lastWriteAmount = 1;
				clientInfo.currentWriteAmount = 1;*/
			}
			fileTable += "\t\t\t</tbody>\r\n" + //
					"\t\t</table>\r\n";
			
			String administrateForm = "";
			final String administrateParamStr = (files.containsAnyArgs ? "&" : "?") + "administrateFile=1";
			if(domainDirectory.getEnableFileUpload()) {
				administrateForm += "\t\t<button title=\"Administrate this folder\" onclick=\"window.location=" + (isSSLThreadEnabled() ? "'https://'" + reuse.request.host : "window.location.protocol") + " + window.location.pathname.split('#')[0].split('?')[0] + '" + administrateParamStr + "'\">Administrate</button>\r\n";
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
			
			final String adminLink = enableAdminInterface ? (isSSLThreadEnabled() ? "https://" : reuse.request.protocol) + reuse.request.hostNoPort + ":" + admin_listen_port : null;
			final String adminAnchor = (adminLink != null ? "<b><a href=\"" + adminLink + "\" target=\"_blank\" rel=\"nofollow\" title=\"Change server-wide settings\">Server Administration</a></b>" : "");
			
			responseStr = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
					+ "\t\t<title>" + folderName + " - " + domainDirectory.getServerName() + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
					+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
					+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t" + bodyHeader + "\r\n" + fileTable + administrateForm + adminAnchor + "\r\n"//
					+ "\t</body>\r\n</html>";
		}
		if(reuse.request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
			out.println("Content-Encoding: gzip");
			byte[] r = StringUtils.compressString(responseStr, "UTF-8", reuse);
			out.println("Content-Length: " + r.length);
			out.println("");
			if(reuse.request.method.equalsIgnoreCase("GET")) {
				outStream.write(r);
				reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
			} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
				reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
			}
			r = null;
		} else {
			out.println("Content-Length: " + responseStr.length());
			out.println("");
			if(reuse.request.method.equalsIgnoreCase("GET")) {
				out.println(responseStr);
				reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
			} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
				reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
			}
		}
		out.flush();
		if(reuse.response.getStatusCode() == HTTPStatusCodes.HTTP_NOT_SET) {
			IllegalStateException debug = new IllegalStateException(reuse.response.getStatusCode().toString() + " Not a status code");
			debug.printStackTrace(System.out);
			System.out.flush();
			debug.printStackTrace(System.err);
			System.err.flush();
		}
		//connectedClients.remove(clientInfo);
		return reuse.response.getStatusCode();
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
	 * @param reqFileName The name of the file requested
	 * @param pageHeader The page address header for standard server responses
	 * @param clientInfo The client information(deprecated)
	 * @return Whether or not this method did anything. If <code>false</code> is
	 *         returned, then the request should be treated as a normal client
	 *         request with
	 *         {@link #serveFileToClient(Socket, boolean, boolean, OutputStream, HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)}
	 *         through {@link #HandleRequest(Socket, InputStream, boolean)}.
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ClientConnection)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #getManagementStrForFile(String, String)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	private static final boolean handleAdministrateFile(final Socket s, final ClientConnection reuse, final String clientAddress, final String versionToUse, final String httpProtocol, final OutputStream outStream, final DualPrintWriter out, final DomainDirectory domainDirectory, String reqFileName, String pageHeader, boolean administrateOrModify, final FileInfo info) throws Throwable {
		if(reuse.request == null || info == null || !info.file.exists() || domainDirectory == null || out == null) {
			return false;
		}
		final String flag = administrateOrModify ? "?administrateFile=1" : "?modifyFile=1";
		reuse.response.setStatusCode(HTTP_500);
		boolean send200OK = true;
		
		RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(info.file);
		res = res == null ? RestrictedFile.getRestrictedFile(info.file) : res;
		
		final String adminLink = enableAdminInterface ? (isSSLThreadEnabled() ? "https://" : reuse.request.protocol) + reuse.request.hostNoPort + ":" + admin_listen_port : null;
		final String adminAnchor = (adminLink != null ? "<b><a href=\"" + adminLink + "\" target=\"_blank\" rel=\"nofollow\" title=\"Change server-wide settings\">Server Administration</a></b>" : "");
		
		final String fileLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host + reuse.request.requestedFilePath);
		
		final String homeLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host);
		final String homeAdminLink = homeLink + "/?administrateFile=1";
		final String logoutLink = StringUtils.encodeHTML(reuse.request.getRequestedPathLink(httpProtocol, true)) + "?administrateFile=1&logout=1";
		final String logoutAnchor = "\t\t<hr><a href=\"" + logoutLink + "\" class=\"alignright\" style=\"display: none;\">Log out</a>\r\n";
		
		String logoutCheck = reuse.request.requestArguments.get("logout");
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
				out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
				out.println("Cache-Control: " + cachePrivateMustRevalidate);
				out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
				if(reuse.allowReuse) {
					out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
				}
				final boolean wasLoggedIn = isLoggedIn(reuse.request);
				removeClientSessionID(reuse.request);//Remove the server-side session id, invalidating the client's copy
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
				authResult = authenticateBasicForServerAdministration(reuse.request, useCookieAuthentication);//final AuthorizationResult authResult = serverAdministrationAuth.authenticate(reuse.request.authorization, new String(reuse.request.postRequestData), reuse.request.protocol, reuse.request.host, clientIP, reuse.request.cookies);
				final boolean passed;
				if(!administrateOrModify) {
					if(res != null && res.canUsersModifyFiles()) {
						passed = true;
					} else {
						passed = false;
					}
				} else {
					passed = authResult.passed();
					authCookie = authResult.authorizedCookie != null ? authResult.authorizedCookie : authCookie;
				}
				if(!passed) {//!areCredentialsValidForAdministration(clientUser, clientPass)) {
					send200OK = false;
					String[] credsUsed = BasicAuthorizationResult.getUsernamePasswordFromBasicAuthorizationHeader(reuse.request.authorization);
					NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), credsUsed[0], credsUsed[1]);
					out.println(versionToUse + " 401 Authorization Required");
					out.println("Vary: Accept-Encoding");
					out.println("Server: " + SERVER_NAME_HEADER);
					out.println("Date: " + StringUtils.getCurrentCacheTime());
					out.println(authResult.resultingAuthenticationHeader);//"WWW-Authenticate: Basic realm=\"" + adminAuthorizationRealm + "\"");
					out.println("Last-Modified: " + StringUtils.getCacheTime(info.lastModifiedLong));
					out.println("Cache-Control: " + cachePrivateMustRevalidate);
					out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
					if(reuse.allowReuse) {
						out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
					}
					if(authCookie != null) {
						out.println("Set-Cookie: " + authCookie);
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
					if(reuse.request.method.equalsIgnoreCase("POST")) {
						reuse.response.setStatusCode(HTTP_303).setUseGZip(reuse.request.acceptEncoding.contains("gzip")).setCharset(StandardCharsets.UTF_8).clearHeaders();
						HashMap<String, String> values = reuse.request.getFormURLEncodedData().postRequestArguments;
						if(values.isEmpty()) {
							reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
							return true;
						}
						values.remove("noresize");
						final String folderData = reuse.request.requestArguments.get("folderData");
						if(folderData != null) {
							FolderData data = FolderData.getByPath(folderData);
							if(data != null) {
								data.setValuesFromHashMap(values);
								reuse.response.setStatusMessage("Admin Page(POST_FOLDER_PROPERTIES)").setHeader("Location", reuse.request.getRequestedPathLink(true) + "?administrateFile=1").setResponse((String) null).sendToClient(s, false);
								return true;
							}
							reuse.response.setStatusMessage("Admin Page(POST_FOLDER_PROPERTIES_ERROR: FolderData[" + folderData + "] == null!)").setStatusCode(HTTP_404);
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Date", StringUtil.getCurrentCacheTime());
							reuse.response.setHeader("Location", reuse.request.getRequestedPathLink(true) + "?administrateFile=1").setResponse("<!DOCTYPE html><html><head><title>Error 404 - Folder data not found</title></head><body><string>The folder data &quot;" + folderData + "&quot; was not found.</string><br><string>Has that folder been visited yet?</string></body></html>").sendToClient(s, true);
							return true;
						}
						reuse.response.setStatusMessage("Admin Page(File Administration: POST_UNKNOWN)").setHeader("Location", reuse.request.getRequestedPathLink(true) + "?page=domains").setResponse((String) null).sendToClient(s, false);
						return true;
					}
					final String pathPrefix = domainDirectory.getURLPathPrefix();
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = info.getURLPath();
					
					String uploadForm = "";
					
					if(domainDirectory.getEnableFileUpload()) {
						uploadForm += "\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "\" enctype=\"multipart/form-data\" method=\"post\">\r\n";
						uploadForm += "\t\t\t<string>Select files from your computer to be uploaded to this directory:</string><br>\r\n";
						uploadForm += "\t\t\t<input type=\"file\" name=\"files\" multiple>\r\n";
						uploadForm += "\t\t\t<input type=\"submit\" value=\"Upload\">\r\n";
						uploadForm += "\t\t</form>";
					} else {
						uploadForm += "\t\t<string>This domain does not enable file uploads.</string>";//\r\n";
						//uploadForm += "\t\t<string>Click <a href=\"" + reuse.request.referrerLink + "\">here</a> to go back to the previous page.</string>";
					}
					final File defaultFile = domainDirectory.getFileFromRequest("/", new HashMap<String, String>(), reuse);
					final String defaultFilePath = defaultFile != null ? FilenameUtils.normalize(defaultFile.getAbsolutePath()) : null;
					
					//final boolean isDefault = FilenameUtils.normalize(requestedFile.getAbsolutePath()).equalsIgnoreCase(defaultFilePath);
					//RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(requestedFile);
					//final boolean hidden = res == null ? false : res.isHidden.getValue().booleanValue();
					String renameCheck = reuse.request.requestArguments.get("renameFile");
					String deleteCheck = reuse.request.requestArguments.get("deleteFile");
					String moveCheck = reuse.request.requestArguments.get("moveFile");
					String restrictCheck = reuse.request.requestArguments.get("restrictFile");
					
					String downloadCheck = reuse.request.requestArguments.get("download");
					
					final boolean renameFile = renameCheck != null ? renameCheck.equals("1") || renameCheck.equalsIgnoreCase("true") : false;
					String renameTo = reuse.request.requestArguments.get("renameTo");
					final boolean deleteFile = deleteCheck != null ? deleteCheck.equals("1") || deleteCheck.equalsIgnoreCase("true") : false;
					final boolean moveFile = moveCheck != null ? moveCheck.equals("1") || moveCheck.equalsIgnoreCase("true") : false;
					final boolean restrictFile = restrictCheck != null ? restrictCheck.equals("1") || restrictCheck.equalsIgnoreCase("true") : false;
					
					final boolean downloadFile = downloadCheck != null ? downloadCheck.equals("1") || downloadCheck.equalsIgnoreCase("true") : false;
					
					final String parentFileLink;
					final File parentFile = info.file.getParentFile();
					final String homeDirPath = FilenameUtils.normalize(domainDirectory.getDirectorySafe().getAbsolutePath());
					if(parentFile != null && !FilenameUtils.normalize(info.file.getAbsolutePath()).equals(homeDirPath)) {
						String parentRequestPath = FilenameUtils.normalize(parentFile.getAbsolutePath()).replace(homeDirPath, "").replace("\\", "/");
						parentRequestPath = (parentRequestPath.startsWith("/") ? "" : "/") + parentRequestPath;
						parentFileLink = StringUtils.encodeHTML(httpProtocol + (reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(parentRequestPath))));
					} else {
						parentFileLink = null;
					}
					
					//final boolean containsAnyArgs = reuse.request.requestArguments.size() > 0;
					String fileOrFolder = info.file.isFile() ? "file" : "folder";
					
					if(deleteFile) {
						/*String deleteParamStr = (containsAnyArgs ? "&" : "?") + flag + "=1&deleteConfirm=";
						final String confirmButton = "<button title=\"Confirm deletion\" onclick=\"window.location.href=window.location.href.split('#')[0].split('?')[0] + '" + deleteParamStr + "1'\">Yes</button>";
						final String cancelButton = "<button title=\"Cancel deletion\" onclick=\"window.location.href=window.location.href.split('#')[0].split('?')[0] + '" + deleteParamStr + "0'\">No</button>";
						*/
						
						FileDeleteStrategy.FORCE.deleteQuietly(info.file);
						final boolean exists = info.file.exists();
						final String actionAnchors;
						if(exists) {
							actionAnchors = "<b><a href=\"" + fileLink + flag + "&deleteFile=1\" rel=\"nofollow\" title=\"Try the delete operation again\">Retry</a>&nbsp;<a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + flag + "\" rel=\"nofollow\" title=\"Cancel the delete operation\">Cancel</a></b>";
						} else {
							actionAnchors = "<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + (administrateOrModify ? flag + "\" rel=\"nofollow\" title=\"Return to the administration page\"" : "\" rel=\"nofollow\" title=\"Return to &quot;" + (parentFile != null ? parentFile.getName() : domainDirectory.getDirectorySafe().getName()) + "&quot;\"") + ">Done</a></b>";
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
								+ "\t\t" + confirmButton + nonBreakingSpaces + cancelButton + "\r\n"//*/
								+ "\t</body>\r\n</html>";
					} else if(renameFile) {
						if(FilenameUtils.normalize(info.file.getAbsolutePath()).equals(homeDirPath)) {
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
									+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + flag + "\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a></b>\r\n"//
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
									out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
									out.println("Cache-Control: " + cachePrivateMustRevalidate);
									out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
									if(reuse.allowReuse) {
										out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
									}
									out.println("Location: " + fileLink + flag + "&renameFile=1");//"Location: " + reuse.request.referrerLink);
									out.println("");
									out.flush();
									reuse.response.setStatusCode(HTTP_303);
									//connectedClients.remove(clientInfo);
									return true;
								}
								boolean success = false;
								boolean alreadyExists = false;
								if(parentFile != null && parentFile.exists()) {
									File renameToFile = new File(parentFile, renameTo);
									alreadyExists = renameToFile.exists();
									success = info.file.renameTo(renameToFile);
								}
								if(success) {//responseStr = "Hai ther! You tried to rename to \"" + renameTo + "\" from \"" + reqFileName + "\"!";
									send200OK = false;
									out.println(versionToUse + " 303 See Other");
									out.println("Vary: Accept-Encoding");
									out.println("Server: " + SERVER_NAME_HEADER);
									out.println("Date: " + StringUtils.getCurrentCacheTime());
									out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
									out.println("Cache-Control: " + cachePrivateMustRevalidate);
									out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
									if(reuse.allowReuse) {
										out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
									}
									out.println("Location: " + parentFileLink + (administrateOrModify ? flag : "") + "#" + renameTo);
									out.println("");
									out.flush();
									reuse.response.setStatusCode(HTTP_303);
									//connectedClients.remove(clientInfo);
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
										+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + flag + "\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a>&nbsp;<a href=\"" + fileLink + flag + "&renameFile=1\" rel=\"nofollow\" title=\"Try again with a different name\">Choose a different name</a>&nbsp;<a href=\"" + fileLink + flag + "&renameFile=1&renameTo=" + encodedRenameTo + "\" rel=\"nofollow\" title=\"Try again with the same name\">Retry</a></b>\r\n"//
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
										+ "\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "\" method=\"get\" enctype=\"application/x-www-form-urlencoded\">"//
										+ "\t\t\t<input type=\"hidden\" name=\"" + flag + "\" value=\"1\">\r\n"//
										+ "\t\t\t<input type=\"hidden\" name=\"renameFile\" value=\"1\">\r\n"//
										+ "\t\t\t<input type=\"text\" name=\"renameTo\" value=\"" + reqFileName + "\" size=\"42\" title=\"The new name for this " + fileOrFolder + "\"><br>\r\n"//
										+ "\t\t\t<input type=\"submit\" value=\"Rename\" title=\"Rename this file\">\r\n"//
										+ "\t\t</form><br>\r\n"//
										+ "\t\t<b><a href=\"" + (parentFileLink != null ? parentFileLink : fileLink) + flag + "\" rel=\"nofollow\" title=\"Cancel the rename operation\">Cancel</a></b>\r\n"//
										+ "\t</body>\r\n</html>";
							}
						}
					} else if(moveFile || restrictFile) {
						//TODO Implement me!
						responseStr = "<!DOCTYPE html><html><head><link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\"><title>You hax. - " + domainDirectory.getServerName() + "</title><style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>" + (domainDirectory.doesDefaultStyleSheetExist() ? "<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">" : "") + "</head><body><string>You typed that url didn't you.</string></body></html>";
					} else {
						if(info.file.isDirectory()) {//XXX administrateFile=1 -- Folders
							final FolderData data = FolderData.getFolderData(info.file, true);
							String fileTable = "";
							String[] c = info.file.list();
							final HashMap<Integer, String> files = new HashMap<>();
							if(c != null) {
								int j = 0;
								for(int i = 0; i < c.length; i++) {
									//CodeUtil.sleep(8L);
									boolean doIt = true;
									String fileName = c[i];
									File file = new File(info.file, fileName);
									if(!file.exists()) {
										doIt = false;
									} /* else {
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
								fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>(-)</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td><a href=\"" + parentFileLink + flag + "\"><b>../(Up)</b></a></td><td>" + nonBreakingSpaces + "</td><td>" + info.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + info.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + info.mimeType + (RestrictedFile.isFileForbidden(parentFile) ? "\t\t(Forbidden)" : "") + "</td><td>" + nonBreakingSpaces + "</td><td>(-)</td></tr>\r\n";
							}
							for(Entry<Integer, String> entry : files.entrySet()) {
								//CodeUtil.sleep(10L);
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
								File file = new File(info.file, fileName);
								if(FileUtil.isFileAccessible(file)) {
									final boolean isDefaultFileForDomain = FilenameUtils.normalize(file.getAbsolutePath()).equalsIgnoreCase(defaultFilePath);
									RestrictedFile curRes = RestrictedFile.getSpecificRestrictedFile(file);
									curRes = curRes == null ? RestrictedFile.getRestrictedFile(file) : curRes;
									final boolean isHidden = curRes == null ? false : curRes.isHidden.getValue().booleanValue();
									String newPath = path + "/" + fileName;
									if(newPath.startsWith("//")) {
										newPath = newPath.substring(1);
									}
									String unAliasedPath = (newPath.startsWith("/") ? "" : "/") + newPath;
									final String curFileLink = StringUtils.encodeHTML(httpProtocol + reuse.request.host + StringUtils.makeFilePathURLSafe(domainDirectory.replacePathWithAlias(unAliasedPath)));
									String managementStr = "<button onclick=\"" + JavaWebServer.checkVisibleJavascript + "if(document.getElementById('folderProperties') !== null && document.getElementById('defaultFileName') !== null) {\r\n\tdocument.getElementById('defaultFileName').value='" + fileName + "';\r\n\tif(!checkVisible(document.getElementById('folderProperties'))) {\r\n\t\tdocument.getElementById('folderProperties').scrollIntoView();\r\n\t}\r\n} else {\r\n\talert('Could not find folder properties form!');\r\n}\">Set default</button>" + (curRes != null ? getManagementStrForFile(curFileLink, fileName, curRes, administrateOrModify) : "");
									FileInfo curInfo = new FileInfo(file, domainDirectory);//contentLength used
									String mimeType = curInfo.mimeType + (RestrictedFile.isFileForbidden(file) ? "\t\t(Forbidden)" : "");
									String name = "<string" + (isDefaultFileForDomain ? " title=\"Default landing page for domain &quot;" + domainDirectory.getDomain() + "&quot;\"" : "") + ">" + fileName + (isHidden ? "&nbsp;<b><i>{Hidden " + (file.isFile() ? "File" : "Folder") + "}</i></b>" : "") + "</string>";
									name = isDefaultFileForDomain ? "<b>" + name + "</b>" : name;
									if(file.isDirectory()) {
										name = "<a href=\"" + curFileLink + flag + "\" rel=\"nofollow\" name=\"" + fileName + "\">" + name + "</a>";
									} else {
										name = "<i><a href=\"" + curFileLink + flag + "\" rel=\"nofollow\" name=\"" + fileName + "\">" + name + "</a></i>";
									}
									fileTable += "\t\t\t\t<tr>" + (domainDirectory.getNumberDirectoryEntries() ? "<td>" + (i.intValue() + 1) + "</td><td>" + nonBreakingSpaces + "</td>" : "") + "<td>" + name + "</td><td>" + (file.isDirectory() ? nonBreakingSpaces : "<a href=\"" + curFileLink + flag + "&download=1\" download=\"" + fileName + "\" target=\"_blank\" rel=\"nofollow\">Download</a>") + "</td><td>" + curInfo.contentLength + "</td><td>" + nonBreakingSpaces + "</td><td>" + curInfo.lastModified + "</td><td>" + nonBreakingSpaces + "</td><td>" + mimeType + "</td><td>" + nonBreakingSpaces + "</td><td>" + managementStr + "</td></tr>\r\n";
								}
							}
							fileTable += "\t\t\t</tbody>\r\n";
							fileTable += "\t\t</table>\r\n";
							final String folderDataHTML = data.toHTML(2, reuse.request.getRequestedPathLink(true) + "?administrateFile=1&folderData=" + data.getSavePath());
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
									+ "\t\t<hr><h2 id=\"folderProperties\">Folder properties:</h2>\r\n"//
									+ folderDataHTML//
									+ "\t\t<h2>Upload files to \"" + reqFileName + "\":</h2><hr>\r\n" + uploadForm + "<hr>\r\n"//
									+ "\t\t<h2>Directory Tree:</h2>\r\n"//
									+ fileTable + "<hr>\r\n"//
									+ "\t\t<b><a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a></b>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "\r\n"//
									+ "\t</body>\r\n</html>";
						} else if(info.file.isFile()) {//XXX administrateFile=1 -- Files
							if(downloadFile) {
								out.println("HTTP/1.1 200 OK");
								sendFileToClient(out, outStream, reuse, HTTP_200, info.file, reuse.request.method, clientAddress, 4096, RestrictedFile.isFileRestricted(info.file, s));
								//connectedClients.remove(clientInfo);
								return true;
							}
							final FileData data = FileData.getFileData(info.file, true);
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
									+ "\t\t<h2>File properties:</h2>\r\n"//
									+ data.toHTML(2)//
									+ "\t\t<hr><b>" + (parentFileLink != null ? "<a href=\"" + parentFileLink + flag + "\" rel=\"nofollow\">(../Up)</a>&nbsp;" : "") + "<a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "</b><br>"//
									+ (!reuse.request.refererLink.isEmpty() ? "\t\t<a href=\"" + reuse.request.refererLink + "\" rel=\"nofollow\">Back to previous page</a>\r\n" : "")//
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
									+ "\t\t<hr><b>" + (parentFileLink != null ? "<a href=\"" + parentFileLink + flag + "\" rel=\"nofollow\">(../Up)</a>&nbsp;" : "") + "<a href=\"" + fileLink + "\" title=\"View the normal version of this page without administration tools\">Normal view</a>" + (adminAnchor.isEmpty() ? "" : "&nbsp;" + adminAnchor) + "</b><br>"//
									+ (!reuse.request.refererLink.isEmpty() ? "\t\t<a href=\"" + reuse.request.refererLink + "\" rel=\"nofollow\">Back to previous page</a>\r\n" : "")//
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
				out.println("Last-Modified: " + StringUtils.getCacheTime(info.file.lastModified()));
				out.println("Cache-Control: " + cachePrivateMustRevalidate);
				out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
				if(reuse.allowReuse) {
					out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
				}
				if(authCookie != null) {
					out.println("Set-Cookie: " + authCookie);
				}
				out.println("Content-Type: text/html; charset=UTF-8");//" + info.mimeType);
				reuse.response.setStatusCode(HTTP_200);
			}
			
			if(reuse.request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
				out.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(responseStr, "UTF-8", reuse);
				out.println("Content-Length: " + r.length);
				out.println("");
				out.flush();
				if(reuse.request.method.equalsIgnoreCase("GET")) {
					outStream.write(r);
					outStream.flush();
					reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
					reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				r = null;
			} else {
				out.println("Content-Length: " + responseStr.length());
				out.println("");
				if(reuse.request.method.equalsIgnoreCase("GET") || reuse.request.method.equalsIgnoreCase("POST")) {
					out.println(responseStr);
					reuse.println("\t\tSent directory tree to client \"" + clientAddress + "\".");
				} else if(reuse.request.method.equalsIgnoreCase("HEAD")) {
					reuse.println("\t\tThe client's request was a HEAD request.\r\n\t\t\tDid not send directory tree to client \"" + clientAddress + "\".");
				}
				out.flush();
			}
			//connectedClients.remove(clientInfo);
			return true;
		} catch(Throwable e) {
			ResponseUtil.send500InternalServerError(s, reuse, domainDirectory, e, true);
			throw e;
		}
		//return false;
	}
	
	/** Generates file management links with the given file link and name(i.e.
	 * Rename, Delete, Move, Restrict, etc.).
	 * 
	 * @param fileLink The absolute link to the file in question
	 * @param fileName The name of the file in question
	 * @return The generated string
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	private static final String getManagementStrForFile(String fileLink, String fileName, RestrictedFile res, boolean administrateOrModify) {//XXX Folder/File Administration Management Links
		String flag = administrateOrModify ? "administrateFile" : "modifyFile";
		return res.canModifyFiles.getValue().booleanValue() ? "<a href=\"" + fileLink + "?" + flag + "=1&renameFile=1\" rel=\"nofollow\" title=\"Rename this file\">Rename</a>"//
				+ "&nbsp;<b><u><i><string title=\"This feature is not implemented yet.\">Copy</string></u>"//
				+ "&nbsp;<a href=\"" + fileLink + "?" + flag + "=1&deleteFile=1\" rel=\"nofollow\" title=\"Delete this file\" onclick=\"return confirm('Are you sure you want to delete the file &quot;" + StringUtils.makeFilePathURLSafe(fileName) + "&quot;? This cannot be undone.');\">Delete</a>"//
				+ "&nbsp;<b><u><i><string title=\"This feature is not implemented yet.\">Move</string></u>"//
				+ (administrateOrModify ? "&nbsp;<u><string title=\"This feature is not implemented yet.\">Restrict</string></i></u></b>" : "")//
				: "";//
	}
	
	/** Parses the client's incoming administration request, then returns the
	 * result.
	 * 
	 * @param s The client
	 * @param in The client's input stream
	 * @param reuse Whether or not this is a reused connection
	 * @return The result of parsing the client's request
	 * @see #HandleRequest(Socket, InputStream, boolean)
	 * @see #HandleRequest(Socket, InputStream, boolean, ClientConnection)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	protected static final RequestResult HandleAdminRequest(final Socket s, final InputStream in, final boolean https, ClientConnection reuse) {
		try {
			return _HandleAdminRequest(s, in, https, reuse);
		} catch(Throwable e) {
			//e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_OUT);
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
			throw e;
		}
	}
	
	private static final RequestResult _HandleAdminRequest(final Socket s, final InputStream in, final boolean https, ClientConnection reuse) {
		reuse.status.setIncoming(true);
		reuse.status.setProxyRequest(false);
		reuse.domainDirectory = DomainDirectory.getDefault(reuse);
		if(s == null || s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
			//serverAdministrationAuths.remove(Thread.currentThread());
			return new RequestResult(reuse.completeResponse(HTTP_499, false), null);
		}
		/*if(!reuse) {
			serverAdministrationAuths.put(Thread.currentThread(), new HttpDigestAuthorization(adminAuthorizationRealm, adminUsername, adminPassword));
		}*/
		final File adminFolder = new File(rootDir, "admin");
		if(!adminFolder.exists()) {
			adminFolder.mkdirs();
		}
		final String clientAddress = AddressUtil.getClientAddress(s);
		reuse.allowReuse = false;//This gets set back to true down below if Keep-Alive is requested by the client!
		try {
			if(reuse.isReused()) {
				reuse.println("Reused Connection: " + clientAddress);
			}
			if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
				reuse.println("\t\tIncoming connection from \"" + clientAddress + "\" was interrupted before the client could send any data.");
				return new RequestResult(reuse.completeResponse(HTTP_499, false), new IOException("Incoming connection from \"" + clientAddress + "\" was interrupted before the client could send any data."));
			}
			if(in.available() < 1) {
				reuse.println("\tWaiting on client to send " + (reuse.isReused() ? "next " : "") + "request...");
			}
			reuse.request = new HTTPClientRequest(s, in, https, reuse);
			RequestResult result = reuse.request.acceptClientRequestSafe(requestTimeout);
			if(reuse.response == null) {
				if(result != null) {
					return result;
				}
				return new RequestResult(reuse.completeResponse(HTTPStatusCodes.HTTP_598, false), null);
			}
			reuse.status.setIncoming(false);
			//reuse.status.setStatus("Request Accepted! Parsing request...");
			reuse.response.setDomainDirectory(reuse.domainDirectory);
			reuse.printlnDebug("Request Accepted! result: " + (result == null ? "[null]" : "non-null! response.toString(): " + (result.getResponse())) + "; request.clientRequestAccepted(): " + reuse.request.clientRequestAccepted());
			if(result != null && !reuse.request.clientRequestAccepted()) {
				/*if(!reuse.request.requestLogs.isEmpty()) {
					reuse.appendLog(reuse.request.requestLogs);
					reuse.request.requestLogs = "";
				}*/
				reuse.println(reuse.response.getStatusMessage());
				reuse.request.cancel();
				return result;
			}
			if(result != null && HTTPClientRequest.debug) {
				reuse.println(result.toString());
			}
			if(result != null) {
				if(reuse.getStatusCode() == HTTPStatusCodes.HTTP_408 || reuse.getStatusCode() == HTTP_500) {
					return result;
				}
			}
			if(!reuse.request.xForwardedFor.isEmpty()) {
				reuse.println("\tIdentified client behind proxy: " + reuse.request.xForwardedFor);
			}
			final String originalHost = reuse.request.host;
			boolean hostDefined = originalHost != null && !originalHost.trim().isEmpty();
			//reuse.request.host = AddressUtil.getClientAddress(reuse.request.host);//AddressUtil.getValidHostFor(originalHost, reuse);
			if(!hostDefined && reuse.request.version.equalsIgnoreCase("HTTP/1.1")) {
				reuse.response.setStatusCode(HTTP_400).setStatusMessage("No host header defined: \"" + originalHost + "\"").setResponse((String) null).sendToClient(s, false);
				reuse.request.cancel();
				return new RequestResult(reuse.completeResponse(HTTP_400, false), null);
			}
			if(reuse.request.method.isEmpty()) {
				reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + reuse.request.protocolLine + "\"").setResponse((String) null).sendToClient(s, false);
				if(!HTTPClientRequest.debug) {
					reuse.clearAllLogsBeforeDisplay();
				}
				reuse.request.cancel();
				return new RequestResult(reuse.completeResponse(HTTP_400, false), null);
			}
			if(!hostDefined) {
				reuse.request.host = AddressUtil.getIp();
			}
			reuse.hostUsed = reuse.request.host + (!hostDefined ? "[Client sent no host header]" : "");
			/*reuse.println("\t--- Client request: " + reuse.request.protocolLine);
			if(!reuse.request.requestArguments.isEmpty()) {
				reuse.println("\t\tRequest Arguments: \"" + reuse.request.requestArgumentsStr + "\"");
			}*/
			reuse.allowReuse = reuse.request.connectionSetting.toLowerCase().contains("keep-alive");
			if(reuse.request.connectionSetting.toLowerCase().contains("close")) {
				reuse.allowReuse = false;
			}
			s.setKeepAlive(reuse.allowReuse);
			
			if(reuse.request.method.equalsIgnoreCase("brew")) {
				if(reuse.request.version.toUpperCase().startsWith("HTCPCP/")) {
					reuse.response.setStatusCode(HTTP_418).setStatusMessage("Making coffee is okay.").setUseGZip(reuse.request.acceptEncoding.toLowerCase().contains("gzip")).setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your coffee is ready!\r\nDon't drink too much in a day.\r\n").sendToClient(s, true);
				} else {
					reuse.response.setStatusCode(HTTP_418).setStatusMessage("Making tea is fun!").setUseGZip(reuse.request.acceptEncoding.toLowerCase().contains("gzip")).setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your tea is ready! Enjoy.\r\n").sendToClient(s, true);
				}
				//s.close();
				return new RequestResult(reuse.completeResponse(HTTP_418, false), null);
			}
			
			String path = reuse.request.requestedFilePath;
			path = (path.trim().isEmpty() ? "/" : path);
			path = (path.startsWith("/") ? "" : "/") + path;
			final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
			
			if(!reuse.request.protocolLine.isEmpty()) {
				if(reuse.request.method.isEmpty()) {
					reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad protocolRequest: \"" + reuse.request.protocolLine + "\"").setResponse((String) null).sendToClient(s, false);
					return new RequestResult(reuse.completeResponse(HTTP_400, false), null);
				}
				//final HttpDigestAuthorization serverAdministrationAuth = getOrCreateAuthForCurrentThread();
				final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(reuse.request, useCookieAuthentication);//final AuthorizationResult authResult = serverAdministrationAuth.authenticate(reuse.request.authorization, new String(reuse.request.postRequestData), reuse.request.protocol, reuse.request.host, clientIP, reuse.request.cookies);
				if(!authResult.passed()) {
					String[] credsUsed = BasicAuthorizationResult.getUsernamePasswordFromBasicAuthorizationHeader(reuse.request.authorization);
					NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), credsUsed[0], credsUsed[1]);
					reuse.response.setStatusCode(HTTP_401);//.setStatusMessage(authResult.message);
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
					reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
					reuse.response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
					if(authResult.authorizedCookie != null) {
						reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
					}
					reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), true), null);
				}
				if(authResult.authorizedCookie != null) {
					reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
				}
				if(reuse.request.method.equalsIgnoreCase("GET") || reuse.request.method.equalsIgnoreCase("HEAD")) {
					if(reuse.request.requestedFilePath.equals("/")) {
						HTTPStatusCodes statusCodeToSend = null;
						final String restrictedFileNote = "<br><string><b>Note:&nbsp;</b>A restricted file that does not have at least one username and password will result in \"" + HTTP_403 + "\" being sent to any client that tries to view it.<br>Restricted files that do have both a username and a password will result in the client being prompted to log in.<br><br>Alternately, if a restricted file has any &quot;AllowedIp&quot;s set, any client with one of those ip addresses will be able to connect as if the restriction did not exist.<br>If one of the allowed ips are set to the string &quot;any&quot;, then any client will be able to connect regardless(useful if you have a file/folder located within another restricted file).</string>";
						final String currentPageLink = reuse.request.getRequestedPathLink(false) + reuse.request.requestArgumentsStr;
						final String mainPageLink = reuse.request.protocol + reuse.request.host + "/";
						final String mainPageNoAdminPortLink = mainPageLink.replace(":" + admin_listen_port, "");
						final String mainPageHrefLink = "<a href=\"" + mainPageLink + "\">Main Page</a>";
						final String backToPrevPageLink = (reuse.request.refererLink.isEmpty() ? mainPageLink : (reuse.request.refererLink.equals(currentPageLink) ? mainPageLink : reuse.request.refererLink));
						final String backToPreviousPageHrefLink = "<a href=\"" + backToPrevPageLink + "\">Back to " + (backToPrevPageLink.equals(mainPageLink) ? "Main" : "Previous") + " Page</a>";
						
						final String pageHrefLinks = mainPageHrefLink + (mainPageLink.equals(backToPrevPageLink) ? "" : "&nbsp;" + backToPreviousPageHrefLink);
						
						final String shutdown = reuse.request.requestArguments.get(shutdownStr);
						final String page = reuse.request.requestArguments.get("page");
						final String edit = reuse.request.requestArguments.get("edit");
						
						final String domainUUID = reuse.request.requestArguments.get("domain");
						final String restrictedFileUUID = reuse.request.requestArguments.get("restrictedFile");
						final String forumUUID = reuse.request.requestArguments.get("forum");
						final String boardUUID = reuse.request.requestArguments.get("board");
						final String topicUUID = reuse.request.requestArguments.get("topic");
						final String commentUUID = reuse.request.requestArguments.get("comment");
						
						final String deleteDomainUUID = reuse.request.requestArguments.get("deleteDomain");
						final String deleteRestrictedFileUUID = reuse.request.requestArguments.get("deleteResFile");
						final String deleteForumUUID = reuse.request.requestArguments.get("deleteForum");
						final String deleteBoardUUID = reuse.request.requestArguments.get("deleteBoard");
						final String deleteTopicUUID = reuse.request.requestArguments.get("deleteTopic");
						final String deleteCommentUUID = reuse.request.requestArguments.get("deleteComment");
						
						final String domainError = reuse.request.requestArguments.get("domainError");
						final String createDomainError = reuse.request.requestArguments.get("createDomainError");
						String getHomeDirError = reuse.request.requestArguments.get("homeDirectoryError");
						final String homeDirectoryError = getHomeDirError != null ? StringUtils.decodeHTML(getHomeDirError) : null;
						final String domainAlreadyExistsError = reuse.request.requestArguments.get("domainAlreadyExistsError");
						
						final String restrictedFileError = reuse.request.requestArguments.get("restrictedFileError");
						final String createResFileError = reuse.request.requestArguments.get("createResFileError");
						final String resFileAlreadyExistsError = reuse.request.requestArguments.get("resFileAlreadyExistsError");
						
						final String forumError = reuse.request.requestArguments.get("forumError");
						final String boardError = reuse.request.requestArguments.get("boardError");
						final String topicError = reuse.request.requestArguments.get("topicError");
						final String commentError = reuse.request.requestArguments.get("commentError");
						
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
							reuse.response.setResponse(unableToCreateDomain);
						} else if(domainError != null) {
							reuse.response.setResponse(domainDoesNotExist);
						} else if(domainAlreadyExistsError != null) {
							reuse.response.setResponse(domainAlreadyExists);
						} else if(restrictedFileError != null) {
							reuse.response.setResponse(restrictedFileDoesNotExist);
						} else if(createResFileError != null) {
							reuse.response.setResponse(unableToCreateResFile);
						} else if(resFileAlreadyExistsError != null) {
							reuse.response.setResponse(resFileAlreadyExists);
						} else if(forumError != null) {
							reuse.response.setResponse(forumDoesNotExist);
						} else if(boardError != null) {
							reuse.response.setResponse(boardDoesNotExist);
						} else if(topicError != null) {
							reuse.response.setResponse(topicDoesNotExist);
						} else if(commentError != null) {
							reuse.response.setResponse(commentDoesNotExist);
						} else {
							if(shutdown != null && (shutdown.equals("1") || shutdown.equalsIgnoreCase("true")) && reuse.request.method.equalsIgnoreCase("GET")) {
								reuse.response.setStatusCode(HTTP_200);
								reuse.response.setStatusMessage("Admin Page(Server Shutdown)");
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
								reuse.response.setHeader("Last-Modified", StringUtils.getCurrentCacheTime());
								reuse.response.setHeader("Accept-Ranges", "none");
								reuse.response.setResponse("<!DOCTYPE html>\r\n");
								reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
								reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
								reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
								reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
								reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
								reuse.response.appendResponse("\t</head>\r\n");
								reuse.response.appendResponse("\t<body bgcolor=\"#99E699\">\r\n");
								reuse.response.appendResponse("\t\t<h1>The server has been told to shut down successfully.</h1><hr>\r\n");
								reuse.response.appendResponse("\t</body>\r\n</html>");
								reuse.response.sendToClient(s, true);
								try {
									s.close();
								} catch(Throwable ignored) {
								}
								JavaWebServer.shutdown();
								return new RequestResult(reuse.completeResponse(HTTP_200, false), null);
							} else if(deleteDomainUUID != null) {
								DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(deleteDomainUUID);
								if(domain != null) {
									String domainName = domain.getDomain();
									domain.delete();
									reuse.response.setStatusMessage("Admin Page(Domain \"" + domainName + "\" Deleted)").setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=domains").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								reuse.response.setResponse(domainDoesNotExist);
							} else if(deleteRestrictedFileUUID != null) {
								RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(deleteRestrictedFileUUID);
								if(res != null) {
									String resFilePath = res.getRestrictedFile().getAbsolutePath();
									res.delete();
									reuse.response.setStatusMessage("Admin Page(Restricted File \"" + resFilePath + "\" Unrestricted)").setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=restrictedFiles").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								reuse.response.setResponse(restrictedFileDoesNotExist);
							} else if(page != null) {
								if(page.equals("restrictedFiles")) {//XXX Page = "restrictedFiles"
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Server Administration - Restricted Files</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									//
									reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t<tbody>\r\n");
									for(RestrictedFile res : RestrictedFile.getInstances()) {
										if(res.getUUID() != null) {
											final String uuid = res.getUUID().toString();
											String filePath = res.getRestrictedFile().getAbsolutePath();
											reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?restrictedFile=" + uuid + "\">" + filePath + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteResFile=" + uuid + "\" onClick=\"return confirm('Are you sure you want to remove the restriction from the file &quot;" + filePath + "&quot;? This cannot be undone.');\">Remove Restriction</a></td></tr>\r\n");
										}
									}
									reuse.response.appendResponse("\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t<a href=\"" + mainPageLink + "?page=createRestrictedFile\" title=\"Allows you to restrict a file server-wide\">Restrict a file</a>\r\n");
									//
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("domains")) {//XXX Page = "domains"
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Server Administration - Domain Settings</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t<string>Select a domain to manage, or click \"Delete\" to delete it.</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t<tbody>\r\n");
									for(DomainDirectory domain : DomainDirectory.getInstances()) {
										if(domain.uuid.getValue() != null) {
											final String uuid = domain.uuid.getValue().toString();
											reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?domain=" + uuid + "\">" + domain.getDisplayName() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteDomain=" + uuid + "\" onClick=\"return confirm('Are you sure you want to delete the domain &quot;" + domain.getDomain() + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
										}
									}
									
									reuse.response.appendResponse("\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t<a href=\"" + mainPageLink + "?page=createDomain\" title=\"Allows you to create a domain which may then be used to connect to this server\">Create a new domain</a>\r\n");
									//
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("createRestrictedFile")) {//XXX Create Restricted File
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Restrict a File:</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									reuse.response.appendResponse("\t\t<string>Properties for new restricted file:</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?createResFile=" + UUID.randomUUID() + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									RestrictedFile res = RestrictedFile.createNewRestrictedFile(UUID.randomUUID());
									res.remove();
									res.setRestrictedFile(new File(homeDirectory, "some/file/or/folder.path"), true);
									final String booleanOptionOn = "\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>FilePath:</b></td><td><input type=\"text\" name=\"FilePath\" value=\"" + res.getRestrictedFile().getAbsolutePath() + "\" size=\"50\"></td><td></td><td></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthRealm:</b></td><td><input type=\"text\" name=\"AuthRealm\" value=\"" + res.getAuthorizationRealm() + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IsHidden:</b></td><td><select name=\"" + res.isHidden.getName() + "\" title=\"" + res.isHidden.getDescription() + "\">" + (res.isHidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>UsersCanModifyFiles:</b></td><td><select name=\"" + res.canModifyFiles.getName() + "\" title=\"" + res.canModifyFiles.getDescription() + "\">" + (res.canModifyFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									int iPsMax = res.getAllowedIPAddresses().size() > 20 ? res.getAllowedIPAddresses().size() : 20;
									for(int i = 0; i < iPsMax; i++) {
										String value = (i < res.getAllowedIPAddresses().size() ? res.getAllowedIPAddresses().get(i) : "");
										reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedIp_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedIp_" + i + "\" value=\"" + value + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									}
									int authMax = 20;
									for(int i = 0; i < authMax;) {
										reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + i + "\" value=\"" + (i == 0 ? DEFAULT_AUTHORIZATION_USERNAME : "") + "\" size=\"25\"></td><td><b>AuthPassword_" + (i + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + i + "\" value=\"" + (i == 0 ? DEFAULT_AUTHORIZATION_PASSWORD : "") + "\" size=\"25\"></td></tr>\r\n");
										i++;
										if(i == res.getAuthorizationCredentials().size() && i < authMax) {
											for(int j = i; j < authMax; j++) {
												reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (j + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + j + "\" value=\"\" size=\"25\"></td><td><b>AuthPassword_" + (j + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + j + "\" value=\"\" size=\"25\"></td></tr>\r\n");
											}
										}
									}
									//
									reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Restrict\" title=\"Restrict this file\">\r\n");
									reuse.response.appendResponse("\t\t</form>\r\n");
									reuse.response.appendResponse("\t\t" + restrictedFileNote + "\r\n");
									//
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("createDomain")) {//XXX Create Domain
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Create a Domain:</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									reuse.response.appendResponse("\t\t<string>Properties for new domain:</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?createDomain=" + UUID.randomUUID() + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									final boolean noHomeDir = homeDirectoryError != null;
									final String homeDirPrefix = noHomeDir ? "<p class=\"red_border\">" : "";
									final String homeDirSuffix = noHomeDir ? "</p>" : "";
									
									DomainDirectory domain = DomainDirectory.getTemporaryDomain();
									String getDomainName = reuse.request.requestArguments.get("domainName");
									final String domainName = getDomainName == null ? AddressUtil.getALocalIP()/*InetAddress.getLocalHost().getHostAddress()*/ : getDomainName;
									final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCurrentCacheTime() + "</td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCurrentCacheTime() + "</td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Domain/Ip:</b></td><td><input type=\"text\" name=\"" + domain.domain.getName() + "\" value=\"" + domainName + "\" size=\"42\" title=\"This is the address which will be used to connect to this domain\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>NetworkMTU:</b></td><td><input type=\"number\" step=\"512\" min=\"512\" max=\"819200\" name=\"" + domain.mtu.getName() + "\" value=\"" + domain.mtu.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>CacheMaxAge:</b></td><td><input type=\"number\" min=\"0\" name=\"" + domain.cacheMaxAge.getName() + "\" value=\"" + domain.cacheMaxAge.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AreDirectoriesForbidden:</b></td><td><select name=\"" + domain.areDirectoriesForbidden.getName() + "\">" + (domain.areDirectoriesForbidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"" + domain.calculateDirectorySizes.getName() + "\">" + (domain.calculateDirectorySizes.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFileName:</b></td><td><input type=\"text\" name=\"" + domain.defaultFileName.getName() + "\" value=\"" + domain.defaultFileName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"" + domain.defaultFontFace.getName() + "\" value=\"" + domain.defaultFontFace.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultPageIcon:</b></td><td><input type=\"text\" name=\"" + domain.defaultPageIcon.getName() + "\" value=\"" + domain.defaultPageIcon.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultStylesheet:</b></td><td><input type=\"text\" name=\"" + domain.defaultStylesheet.getName() + "\" value=\"" + domain.defaultStylesheet.getValue() + "\" size=\"42\"></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>PageHeaderContent:</b></td><td><textarea name=\"" + domain.pageHeaderContent.getName() + "\" title=\"" + domain.pageHeaderContent.getDescription() + "\">" + domain.pageHeaderContent.getValue() + "</textarea></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayName:</b></td><td><input type=\"text\" name=\"" + domain.displayName.getName() + "\" value=\"" + domainName + "\" size=\"42\" title=\"The display name for this domain(used when viewing domains in the administration interface)\"></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayLogEntries:</b></td><td><select name=\"" + domain.displayLogEntries.getName() + "\">" + (domain.displayLogEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td>" + homeDirPrefix + "<input type=\"text\" name=\"" + domain.folder.getName() + "\" value=\"" + (noHomeDir ? homeDirectoryError : domain.folder.getValue().getAbsolutePath()) + "\" size=\"42\">" + homeDirSuffix + "</td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableGZipCompression:</b></td><td><select name=\"" + domain.enableGZipCompression.getName() + "\">" + (domain.enableGZipCompression.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFileUploads:</b></td><td><select name=\"" + domain.enableFileUpload.getName() + "\" title=\"Whether or not authenticated users may upload files to the directory they are currently browsing\">" + (domain.enableFileUpload.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableAlternateDirectoryListingViews:</b></td><td><select name=\"" + domain.enableAlternateDirectoryListingViews.getName() + "\">" + (domain.enableAlternateDirectoryListingViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFilterView:</b></td><td><select name=\"" + domain.enableFilterView.getName() + "\">" + (domain.enableFilterView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableSortViews:</b></td><td><select name=\"" + domain.enableSortView.getName() + "\">" + (domain.enableSortView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableMediaView:</b></td><td><select name=\"" + domain.enableMediaView.getName() + "\">" + (domain.enableMediaView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableReadableFileViews:</b></td><td><select name=\"" + domain.enableReadableFileViews.getName() + "\">" + (domain.enableReadableFileViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableVLCPlaylistView:</b></td><td><select name=\"" + domain.enableVLCPlaylistView.getName() + "\">" + (domain.enableVLCPlaylistView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableXmlListView:</b></td><td><select name=\"" + domain.enableXmlListView.getName() + "\">" + (domain.enableXmlListView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IgnoreThumbsdbFiles:</b></td><td><select name=\"" + domain.ignoreThumbsdbFiles.getName() + "\">" + (domain.ignoreThumbsdbFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>ListDirectoriesFirst:</b></td><td><select name=\"" + domain.listDirectoriesFirst.getName() + "\">" + (domain.listDirectoriesFirst.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>NumberDirectoryEntries:</b></td><td><select name=\"" + domain.numberDirectoryEntries.getName() + "\">" + (domain.numberDirectoryEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>ServerName:</b></td><td><input type=\"text\" name=\"" + domain.serverName.getName() + "\" value=\"" + domain.serverName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									//
									reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Create\" title=\"Create a new Domain\">\r\n");
									reuse.response.appendResponse("\t\t</form>\r\n");
									//
									
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else if(page.equals("forums")) {//XXX Page = "forums"
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Server Administration - Forum Settings</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t<string>Select a forum to manage, or click \"Delete\" to delete it.<br><b>Note: </b>Forums are not yet implemented. Please check back in a newer version!</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t<tbody>\r\n");
									for(ForumData forum : ForumData.getInstances()) {
										if(forum.getUUID() != null) {
											final String forumName = forum.getName();
											final String uuid = forum.getUUID().toString();
											reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + uuid + "\">" + forumName + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?deleteForum=" + uuid + "\" onClick=\"return confirm('Are you sure you want to delete the forum &quot;" + forumName + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
										}
									}
									reuse.response.appendResponse("\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t</table><hr>\r\n");
									//
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else {
									reuse.response.setResponse(pageDoesNotExist);
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
															reuse.response.setResponse("<!DOCTYPE html>\r\n");
															reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
															reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
															reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
															reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
															reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
															reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
															reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
															reuse.response.appendResponse("\t</head>\r\n");
															reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
															reuse.response.appendResponse("\t\t<h1>Comment: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "/#comment-" + comment.getNumber() + "\" title=\"" + topic.getTitle() + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "/#comment-" + comment.getNumber() + "</a></h1><hr>\r\n");
															reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
															reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
															reuse.response.appendResponse("\t\t<string>Edit comment #" + comment.getNumber() + ":</string>\r\n");
															//
															reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&comment=" + commentUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
															reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
															reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
															//
															reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(comment.getDateCreated()) + "</td></tr>\r\n");
															reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Posted:</b></td><td>" + StringUtils.getCacheTime(comment.getDatePosted()) + "</td></tr>\r\n");
															reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Author:</b></td><td>" + comment.getAuthor() + "</td></tr>\r\n");
															reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Message:</b></td><td><textarea rows=\"" + msgLineAmt + "\" cols=\"50\" name=\"message\" size=\"42\">" + msg + "</textarea></td></tr>\r\n");
															reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCacheTime(comment.getLastEditTime()) + "</td></tr>\r\n");
															
															reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
															reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
															//
															reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
															reuse.response.appendResponse("\t\t</form>\r\n");
															reuse.response.appendResponse("\t</body>\r\n</html>");
														} else {
															reuse.response.setResponse(commentDoesNotExist);
														}
													} else {
														if(deleteCommentUUID != null) {
															ForumComment comment = topic.getForumCommentFromUUID(deleteCommentUUID);
															if(comment != null) {
																reuse.response.setStatusMessage("Admin Page(Forum Comment #" + comment.getNumber() + " Deleted)");
																topic.removeComment(comment);
																topic.saveToFile();
																reuse.response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&edit=comments").setResponse((String) null).sendToClient(s, false);
																return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
															}
															reuse.response.setResponse(boardDoesNotExist);
														} else {
															final String msg = topic.getMessage();
															long numOfLines = StringUtils.getNumOfLinesInStr(msg) + 1;
															final long msgLineAmt = (msg == null || msg.isEmpty() ? 4 : numOfLines >= 4 ? numOfLines : 4);
															if(edit == null) {
																reuse.response.setResponse("<!DOCTYPE html>\r\n");
																reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
																reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
																reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
																reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
																reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
																reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
																reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
																reuse.response.appendResponse("\t</head>\r\n");
																reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
																reuse.response.appendResponse("\t\t<h1>Topic: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "\" title=\"" + topic.getTitle() + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "</a></h1><hr>\r\n");
																reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
																reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
																reuse.response.appendResponse("\t\t<string>Edit board \"" + boardName + "\":</string>\r\n");
																//
																reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
																reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
																reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
																//
																
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(board.getDateCreated()) + "</td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>TopicTitle:</b></td><td><input type=\"text\" name=\"title\" value=\"" + topic.getTitle() + "\" size=\"42\"></td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b># of Comments:&nbsp;" + topic.getComments().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&edit=comments" + "\">Edit comments</a></td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>TopicMessage:</b></td><td><textarea rows=\"" + msgLineAmt + "\" cols=\"50\" name=\"message\" size=\"42\">" + msg + "</textarea></td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IsLocked:</b></td><td><input type=\"text\" name=\"isLocked\" value=\"" + topic.isLocked() + "\" size=\"8\"></td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IsSticky:</b></td><td><input type=\"text\" name=\"isSticky\" value=\"" + topic.isSticky() + "\" size=\"8\"></td></tr>\r\n");
																reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IsPrivate:</b></td><td><input type=\"text\" name=\"isPrivate\" value=\"" + topic.isPrivate() + "\" size=\"8\"></td></tr>\r\n");
																
																reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
																reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
																//
																reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
																reuse.response.appendResponse("\t\t</form>\r\n");
																reuse.response.appendResponse("\t</body>\r\n</html>");
															} else if(edit.equalsIgnoreCase("comments")) {
																reuse.response.setResponse("<!DOCTYPE html>\r\n");
																reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
																reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
																reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
																reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
																reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
																reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
																reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
																reuse.response.appendResponse("\t</head>\r\n");
																reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
																reuse.response.appendResponse("\t\t<h1>Topic: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "/topic/" + topic.getTitleInURL() + "</a></h1><hr>\r\n");
																reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
																reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
																reuse.response.appendResponse("\t\t<string>Edit comments for topic \"" + topic.getTitle() + "\":</string>\r\n");
																//
																reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
																reuse.response.appendResponse("\t\t\t<tbody>\r\n");
																//
																for(ForumComment comment : topic.getComments()) {
																	reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&comment=" + comment.getUUID().toString() + "\">Comment #" + comment.getNumber() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topicUUID + "&deleteComment=" + comment.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete comment #" + comment.getNumber() + "? This cannot be undone.');\">Delete</a></td></tr>\r\n");
																}
																reuse.response.appendResponse("\t\t\t</tbody>\r\n");
																reuse.response.appendResponse("\t\t</table><hr>\r\n");
																//
																
																reuse.response.appendResponse("\t</body>\r\n</html>");
															}
														}
													}
												} else {
													reuse.response.setResponse(topicDoesNotExist);
												}
											} else {
												if(deleteTopicUUID != null) {
													ForumTopic topic = board.getForumTopicFromUUID(deleteTopicUUID);
													if(topic != null) {
														reuse.response.setStatusMessage("Admin Page(Forum Topic \"" + topic.getTitle() + "\" Deleted)");
														board.removeTopic(topic);
														board.saveToFile();
														reuse.response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&edit=topics").setResponse((String) null).sendToClient(s, false);
														return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
													}
													reuse.response.setResponse(boardDoesNotExist);
												} else {
													final String desc = board.getDescription();
													long numOfLines = StringUtils.getNumOfLinesInStr(desc) + 1;
													final long descLineAmt = (desc == null || desc.isEmpty() ? 4 : numOfLines >= 4 ? numOfLines : 4);
													if(edit == null) {
														reuse.response.setResponse("<!DOCTYPE html>\r\n");
														reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
														reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
														reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
														reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
														reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
														reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
														reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
														reuse.response.appendResponse("\t</head>\r\n");
														reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
														reuse.response.appendResponse("\t\t<h1>Board: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "</a></h1><hr>\r\n");
														reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
														reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
														reuse.response.appendResponse("\t\t<string>Edit board \"" + boardName + "\":</string>\r\n");
														//
														reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?forum=" + forumUUID + "&board=" + boardUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
														reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
														reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
														//
														
														reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(board.getDateCreated()) + "</td></tr>\r\n");
														reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>BoardName:</b></td><td><input type=\"text\" name=\"name\" value=\"" + boardName + "\" size=\"42\"></td></tr>\r\n");
														reuse.response.appendResponse("\t\t\t\t\t<tr><td><b># of Forum Topics:&nbsp;" + board.getTopics().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&edit=topics" + "\">Edit topics</a></td></tr>\r\n");
														reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>BoardDescription:</b></td><td><textarea rows=\"" + descLineAmt + "\" cols=\"50\" name=\"description\" size=\"42\">" + desc + "</textarea></td></tr>\r\n");
														
														reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
														reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
														//
														reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
														reuse.response.appendResponse("\t\t</form>\r\n");
														reuse.response.appendResponse("\t</body>\r\n</html>");
													} else if(edit.equalsIgnoreCase("topics")) {
														reuse.response.setResponse("<!DOCTYPE html>\r\n");
														reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
														reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
														reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
														reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
														reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
														reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
														reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
														reuse.response.appendResponse("\t</head>\r\n");
														reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
														reuse.response.appendResponse("\t\t<h1>Board: <a href=\"" + mainPageNoAdminPortLink + forumName + "/forum/" + boardName + "\" title=\"" + boardName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "/forum/" + board.getName() + "</a></h1><hr>\r\n");
														reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
														reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
														reuse.response.appendResponse("\t\t<string>Edit topics for board \"" + boardName + "\":</string>\r\n");
														//
														reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
														reuse.response.appendResponse("\t\t\t<tbody>\r\n");
														//
														for(ForumTopic topic : board.getTopics()) {
															reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&topic=" + topic.getUUID().toString() + "\">" + topic.getTitle() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + boardUUID + "&deleteTopic=" + topic.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete the forum topic &quot;" + topic.getTitle().replace("'", "-").replace("\"", "-") + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
														}
														reuse.response.appendResponse("\t\t\t</tbody>\r\n");
														reuse.response.appendResponse("\t\t</table><hr>\r\n");
														//
														
														reuse.response.appendResponse("\t</body>\r\n</html>");
													}
												}
											}
											
										} else {
											reuse.response.setResponse(boardDoesNotExist);
										}
									} else {
										if(deleteBoardUUID != null) {
											ForumBoard board = forum.getForumBoardFromUUID(deleteBoardUUID);
											if(board != null) {
												reuse.response.setStatusMessage("Admin Page(Forum Board \"" + board.getName() + "\" Deleted)");
												forum.removeBoard(board);
												forum.saveToFile();
												reuse.response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?forum=" + forumUUID + "&edit=boards").setResponse((String) null).sendToClient(s, false);
												return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
											}
											reuse.response.setResponse(boardDoesNotExist);
										} else {
											if(edit == null) {
												reuse.response.setResponse("<!DOCTYPE html>\r\n");
												reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
												reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
												reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
												reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
												reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
												reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
												reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
												reuse.response.appendResponse("\t</head>\r\n");
												reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
												reuse.response.appendResponse("\t\t<h1>Forum: <a href=\"" + mainPageNoAdminPortLink + forumName + "\" title=\"" + forumName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "</a></h1><hr>\r\n");
												reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
												reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
												reuse.response.appendResponse("\t\t<string>Properties for forum \"" + forumName + "\":</string>\r\n");
												//
												reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?forum=" + forumUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
												reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
												reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
												//
												reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(forum.getDateCreated()) + "</td></tr>\r\n");
												reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>ForumName:</b></td><td><input type=\"text\" name=\"name\" value=\"" + forum.getName() + "\" size=\"42\"></td></tr>\r\n");
												reuse.response.appendResponse("\t\t\t\t\t<tr><td><b># of Forum Boards:&nbsp;" + forum.getBoards().size() + "</b></td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&edit=boards" + "\">Edit boards</a></td></tr>\r\n");
												//
												ArrayList<String> domains = forum.getDomains();
												int domainsMax = domains.size() > 20 ? domains.size() : 20;
												for(int i = 0; i < domainsMax; i++) {
													String value = (i < domains.size() ? domains.get(i) : "");
													reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedDomain_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedDomain_" + i + "\" value=\"" + value + "\" size=\"25\"></td></tr>\r\n");
												}
												//
												reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
												reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
												reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
												reuse.response.appendResponse("\t\t</form>\r\n");
												//
												reuse.response.appendResponse("\t</body>\r\n</html>");
											} else if(edit.equalsIgnoreCase("boards")) {
												reuse.response.setResponse("<!DOCTYPE html>\r\n");
												reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
												reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
												reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
												reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
												reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
												reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
												reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
												reuse.response.appendResponse("\t</head>\r\n");
												reuse.response.appendResponse("\t<body bgcolor=\"#FFCC80\">\r\n");
												reuse.response.appendResponse("\t\t<h1>Forum: <a href=\"" + mainPageNoAdminPortLink + forumName + "\" title=\"" + forumName + "\" target=\"_blank\">" + mainPageNoAdminPortLink + forumName + "</a></h1><hr>\r\n");
												reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
												reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
												reuse.response.appendResponse("\t\t<string>Edit boards for forum \"" + forumName + "\":</string>\r\n");
												//
												reuse.response.appendResponse("\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
												reuse.response.appendResponse("\t\t\t<tbody>\r\n");
												//
												for(ForumBoard board : forum.getBoards()) {
													reuse.response.appendResponse("\t\t\t\t<tr><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&board=" + board.getUUID().toString() + "\">" + board.getName() + "</a></td><td>" + nonBreakingSpaces + "</td><td><a href=\"" + mainPageLink + "?forum=" + forumUUID + "&deleteBoard=" + board.getUUID().toString() + "\" onClick=\"return confirm('Are you sure you want to delete the forum board &quot;" + board.getName() + "&quot;? This cannot be undone.');\">Delete</a></td></tr>\r\n");
												}
												reuse.response.appendResponse("\t\t\t</tbody>\r\n");
												reuse.response.appendResponse("\t\t</table><hr>\r\n");
												//
												
												reuse.response.appendResponse("\t</body>\r\n</html>");
											}
										}
									}
								} else {
									reuse.response.setResponse(forumDoesNotExist);
								}
							} else if(deleteForumUUID != null) {
								ForumData forum = ForumData.getForumDataFromUUID(deleteForumUUID);
								if(forum != null) {
									reuse.response.setStatusMessage("Admin Page(Forum Data \"" + forum.getName() + "\" Deleted)");
									forum.delete();
									reuse.response.setStatusCode(HTTP_303).setHeader("Location", mainPageLink + "?page=forums").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								reuse.response.setResponse(boardDoesNotExist);
							} else if(restrictedFileUUID != null) {//XXX Restricted file Properties
								RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(restrictedFileUUID);
								if(res != null) {
									final String filePath = res.getRestrictedFile().getAbsolutePath();
									final String fileName = res.getRestrictedFile().getName();
									final String booleanOptionOn = "\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#FFFF99\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Restricted File: " + filePath + "</h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									reuse.response.appendResponse("\t\t<string>Properties for Restricted File \"" + fileName + "\":</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?restrictedFile=" + restrictedFileUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthRealm:</b></td><td><input type=\"text\" name=\"AuthRealm\" value=\"" + res.getAuthorizationRealm() + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IsHidden:</b></td><td><select name=\"" + res.isHidden.getName() + "\" title=\"" + res.isHidden.getDescription() + "\">" + (res.isHidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>UsersCanModifyFiles:</b></td><td><select name=\"" + res.canModifyFiles.getName() + "\" title=\"" + res.canModifyFiles.getDescription() + "\">" + (res.canModifyFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									int iPsMax = res.getAllowedIPAddresses().size() > 20 ? res.getAllowedIPAddresses().size() : 20;
									for(int i = 0; i < iPsMax; i++) {
										String value = (i < res.getAllowedIPAddresses().size() ? res.getAllowedIPAddresses().get(i) : "");
										reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AllowedIp_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AllowedIp_" + i + "\" value=\"" + value + "\" size=\"25\"></td><td></td><td></td></tr>\r\n");
									}
									int authMax = res.getAuthorizationCredentials().size() > 20 ? res.getAuthorizationCredentials().size() : 20;
									int i = 0;
									ArrayList<Credential> creds = new ArrayList<>(res.getAuthorizationCredentials());
									if(creds.isEmpty()) {
										creds = new ArrayList<>();
										for(int index = 0; index < authMax; index++) {
											creds.add(new Credential());
										}
									}
									for(Credential cred : creds) {
										String username = cred.getUsername();
										String password = cred.getPassword();
										reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (i + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + i + "\" value=\"" + username + "\" size=\"25\"></td><td><b>AuthPassword_" + (i + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + i + "\" value=\"" + password + "\" size=\"25\"></td></tr>\r\n");
										i++;
										if(i == res.getAuthorizationCredentials().size() && i < authMax) {
											for(int j = i; j < authMax; j++) {
												reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AuthUsername_" + (j + 1) + ":</b></td><td><input type=\"text\" name=\"AuthUsername_" + j + "\" value=\"\" size=\"25\"></td><td><b>AuthPassword_" + (j + 1) + ":</b></td><td><input type=\"password\" name=\"AuthPassword_" + j + "\" value=\"\" size=\"25\"></td></tr>\r\n");
											}
										}
									}
									//
									reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
									reuse.response.appendResponse("\t\t</form>\r\n");
									reuse.response.appendResponse("\t\t" + restrictedFileNote + "\r\n");
									//
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else {
									reuse.response.setResponse(restrictedFileDoesNotExist);
								}
							} else if(domainUUID != null) {//XXX Domain Properties
								DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(domainUUID);
								if(domain != null) {
									final String domainName = domain.getDomain();
									reuse.response.setResponse("<!DOCTYPE html>\r\n");
									reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
									reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
									reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
									reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
									reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
									reuse.response.appendResponse("\t</head>\r\n");
									reuse.response.appendResponse("\t<body bgcolor=\"#D1E0EF\">\r\n");
									reuse.response.appendResponse("\t\t<h1>Domain: <a href=\"" + reuse.request.protocol + domainName + "/\" title=\"" + domainName + "\" target=\"_blank\">" + reuse.request.protocol + domainName + "/</a></h1><hr>\r\n");
									reuse.response.appendResponse("\t\t" + pageLinkHeader + "\r\n");
									reuse.response.appendResponse("\t\t" + pageHrefLinks + "<hr>\r\n");
									reuse.response.appendResponse("\t\t<string>Properties for domain \"" + domainName + "\":</string>\r\n");
									//
									reuse.response.appendResponse("\t\t<form action=\"" + reuse.request.getRequestedPathLink(true) + "?domain=" + domainUUID + "\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
									reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
									reuse.response.appendResponse("\t\t\t\t<tbody>\r\n");
									//
									final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
									final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
									//
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Date Created:</b></td><td>" + StringUtils.getCacheTime(domain.dateCreated.getValue().longValue()) + "</td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>Last Edited:</b></td><td>" + StringUtils.getCacheTime(domain.lastEdited.getValue().longValue()) + "</td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>NetworkMTU:</b></td><td><input type=\"number\" step=\"512\" min=\"512\" max=\"819200\" name=\"" + domain.mtu.getName() + "\" value=\"" + domain.mtu.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>CacheMaxAge:</b></td><td><input type=\"number\" min=\"0\" name=\"" + domain.cacheMaxAge.getName() + "\" value=\"" + domain.cacheMaxAge.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>AreDirectoriesForbidden:</b></td><td><select name=\"" + domain.areDirectoriesForbidden.getName() + "\">" + (domain.areDirectoriesForbidden.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"" + domain.calculateDirectorySizes.getName() + "\">" + (domain.calculateDirectorySizes.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFileName:</b></td><td><input type=\"text\" name=\"" + domain.defaultFileName.getName() + "\" value=\"" + domain.defaultFileName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"" + domain.defaultFontFace.getName() + "\" value=\"" + domain.defaultFontFace.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultPageIcon:</b></td><td><input type=\"text\" name=\"" + domain.defaultPageIcon.getName() + "\" value=\"" + domain.defaultPageIcon.getValue() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DefaultStylesheet:</b></td><td><input type=\"text\" name=\"" + domain.defaultStylesheet.getName() + "\" value=\"" + domain.defaultStylesheet.getValue() + "\" size=\"42\"></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>PageHeaderContent:</b></td><td><textarea name=\"" + domain.pageHeaderContent.getName() + "\" title=\"" + domain.pageHeaderContent.getDescription() + "\">" + domain.pageHeaderContent.getValue() + "</textarea></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayName:</b></td><td><input type=\"text\" name=\"" + domain.displayName.getName() + "\" value=\"" + domain.displayName.getValue() + "\" size=\"42\"></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>DisplayLogEntries:</b></td><td><select name=\"" + domain.displayLogEntries.getName() + "\" title=\"" + domain.displayLogEntries.getDescription() + "\">" + (domain.displayLogEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td><input type=\"text\" name=\"" + domain.folder.getName() + "\" value=\"" + domain.folder.getValue().getAbsolutePath() + "\" size=\"42\"></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableGZipCompression:</b></td><td><select name=\"" + domain.enableGZipCompression.getName() + "\" title=\"" + domain.enableGZipCompression.getDescription() + "\">" + (domain.enableGZipCompression.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFileUploads:</b></td><td><select name=\"" + domain.enableFileUpload.getName() + "\" title=\"" + domain.enableFileUpload.getDescription() + "\">" + (domain.enableFileUpload.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableAlternateDirectoryListingViews:</b></td><td><select name=\"" + domain.enableAlternateDirectoryListingViews.getName() + "\">" + (domain.enableAlternateDirectoryListingViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableFilterView:</b></td><td><select name=\"" + domain.enableFilterView.getName() + "\">" + (domain.enableFilterView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableSortViews:</b></td><td><select name=\"" + domain.enableSortView.getName() + "\">" + (domain.enableSortView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableMediaView:</b></td><td><select name=\"" + domain.enableMediaView.getName() + "\">" + (domain.enableMediaView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableReadableFileViews:</b></td><td><select name=\"" + domain.enableReadableFileViews.getName() + "\">" + (domain.enableReadableFileViews.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableVLCPlaylistView:</b></td><td><select name=\"" + domain.enableVLCPlaylistView.getName() + "\">" + (domain.enableVLCPlaylistView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>EnableXmlListView:</b></td><td><select name=\"" + domain.enableXmlListView.getName() + "\">" + (domain.enableXmlListView.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>IgnoreThumbsdbFiles:</b></td><td><select name=\"" + domain.ignoreThumbsdbFiles.getName() + "\">" + (domain.ignoreThumbsdbFiles.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>ListDirectoriesFirst:</b></td><td><select name=\"" + domain.listDirectoriesFirst.getName() + "\">" + (domain.listDirectoriesFirst.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>NumberDirectoryEntries:</b></td><td><select name=\"" + domain.numberDirectoryEntries.getName() + "\">" + (domain.numberDirectoryEntries.getValue().booleanValue() ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
									reuse.response.appendResponse("\t\t\t\t\t<tr><td><b>ServerName:</b></td><td><input type=\"text\" name=\"" + domain.serverName.getName() + "\" value=\"" + domain.serverName.getValue() + "\" size=\"42\"></td></tr>\r\n");
									//
									reuse.response.appendResponse("\t\t\t\t</tbody>\r\n");
									reuse.response.appendResponse("\t\t\t</table><hr>\r\n");
									reuse.response.appendResponse("\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
									reuse.response.appendResponse("\t\t</form>\r\n");
									//
									
									reuse.response.appendResponse("\t</body>\r\n</html>");
								} else {
									reuse.response.setResponse(domainDoesNotExist);
								}
							} else {//XXX Server Settings (Main Page)
								reuse.response.setResponse("<!DOCTYPE html>\r\n");
								reuse.response.appendResponse("<html>\r\n\t<head>\r\n");
								reuse.response.appendResponse("\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n");
								reuse.response.appendResponse("\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n");
								reuse.response.appendResponse("\t\t<link rel=\"shortcut icon\" href=\"" + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n");
								reuse.response.appendResponse("\t\t<title>Server Administration - " + SERVER_NAME + "</title>\r\n");
								reuse.response.appendResponse("\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n");
								reuse.response.appendResponse("\t\t<link rel=\"stylesheet\" href=\"" + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"UTF-8\">\r\n");
								reuse.response.appendResponse("\t</head>\r\n");
								reuse.response.appendResponse("\t<body bgcolor=\"#99E699\">\r\n");
								reuse.response.appendResponse("\t\t<div class=\"wrapper\">\r\n");
								reuse.response.appendResponse("\t\t\t<h1>Server Administration - Server Settings</h1><hr>\r\n");
								reuse.response.appendResponse("\t\t\t" + pageLinkHeader + "\r\n");
								reuse.response.appendResponse("\t\t\t<string>Change settings for this server:</string>\r\n");
								//
								reuse.response.appendResponse("\t\t\t<form action=\"/serverSettings\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">\r\n");
								reuse.response.appendResponse("\t\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\">\r\n");
								reuse.response.appendResponse("\t\t\t\t\t<tbody>\r\n");
								//
								final String booleanOptionOn = "\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n";
								final String booleanOptionOff = "\r\n\t\t\t\t\t\t<option value=\"false\">false</option>\r\n\t\t\t\t\t\t<option value=\"true\">true</option>\r\n";
								//
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>HomeDirectory:</b></td><td><input type=\"text\" name=\"homeDirectory\" value=\"" + homeDirectory.getAbsolutePath() + "\" size=\"42\" title=\"The default or fallback home directory to read from when parsing incoming client requests\"></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>CalculateDirectorySizes:</b></td><td><select name=\"calculateDirectorySizes\" title=\"The default or fallback value used to determine whether or not the server should calculate and send directory sizes to the client in directory listings(very resource intensive and time consuming, I recommend leaving this set to false)\">" + (calculateDirectorySizes ? booleanOptionOn : booleanOptionOff) + "</select></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>DefaultFontFace:</b></td><td><input type=\"text\" name=\"defaultFontFace\" value=\"" + defaultFontFace + "\" size=\"42\" title=\"The default or fallback font face that is used for the directory listing page that is sent to the client(e.g. Times New Roman)\"></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>Php-CGI_FilePath:</b></td><td><input type=\"text\" name=\"phpExeFilePath\" value=\"" + PhpResult.phpExeFilePath + "\" size=\"42\" title=\"The path to the main PHP executable file\"></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>RequestTimeout:</b></td><td><input type=\"number\" step=\"1\" min=\"10000\" max=\"60000\" name=\"requestTimeout\" value=\"" + requestTimeout + "\" size=\"42\" title=\"The time(in milliseconds) to wait before a client will be timed out\"></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>AdminAuthorizationRealm:</b></td><td><textarea readonly rows=\"1\" name=\"noresize\" title=\"The authorization realm for this administration interface\">" + adminAuthorizationRealm + "</textarea></td></tr>\r\n");
								reuse.response.appendResponse("\t\t\t\t\t\t<tr><td><b>AdminAuthorizationUsername:</b></td><td><textarea readonly rows=\"1\" name=\"noresize\" title=\"The username for this administration interface\">" + adminUsername + "</textarea></td></tr>\r\n");
								//
								reuse.response.appendResponse("\t\t\t\t\t</tbody>\r\n");
								reuse.response.appendResponse("\t\t\t\t</table><hr>\r\n");
								reuse.response.appendResponse("\t\t\t\t<input type=\"submit\" value=\"Submit Changes\" title=\"Send changes to the server\">\r\n");
								reuse.response.appendResponse("\t\t\t</form>\r\n");
								//
								reuse.response.appendResponse("\t\t\t<div class=\"push\"></div>\r\n");
								reuse.response.appendResponse("\t\t</div>\r\n");
								reuse.response.appendResponse("\t\t<div class=\"footer\" align=\"center\">\r\n");
								reuse.response.appendResponse("\t\t\t<table border=\"2\" cellpadding=\"3\" cellspacing=\"1\"><tbody><tr><td><p class=\"red_border\"><a href=\"" + mainPageLink + "?" + shutdownStr + "=1" + "\" onClick=\"return confirm('Are you sure you want to shut down the server?');\">Shut down the server</a></p></td></tr></tbody></table>\r\n");
								reuse.response.appendResponse("\t\t\t<string>To change the administration credentials, you must edit the \"" + optionsFileName + "\" file manually and then restart the server.</string>\r\n");
								reuse.response.appendResponse("\t\t</div>\r\n");
								reuse.response.appendResponse("\t</body>\r\n</html>");
							}
						}
						if(reuse.response.getResponse().equals(domainDoesNotExist) || reuse.response.getResponse().equals(unableToCreateDomain) || reuse.response.getResponse().equals(restrictedFileDoesNotExist) || reuse.response.getResponse().equals(unableToCreateResFile) || reuse.response.getResponse().equals(pageDoesNotExist) || reuse.response.getResponse().equals(forumDoesNotExist) || reuse.response.getResponse().equals(boardDoesNotExist) || reuse.response.getResponse().equals(topicDoesNotExist) || reuse.response.getResponse().equals(commentDoesNotExist)) {
							reuse.response.setStatusCode(HTTP_404);
						} else if(reuse.response.getResponse().equals(domainAlreadyExists) || reuse.response.getResponse().equals(resFileAlreadyExists)) {
							reuse.response.setStatusCode(HTTP_409);
						} else {
							@SuppressWarnings("null")
							//Because I'm not currently using statusCodeToSend, but may use it later.
							boolean send200 = statusCodeToSend == null;
							reuse.response.setStatusCode(send200 ? HTTP_200 : statusCodeToSend);
						}
						reuse.response.setStatusMessage("Admin Page");
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
						reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Last-Modified", StringUtils.getCurrentCacheTime());
						reuse.response.setHeader("Accept-Ranges", "none");
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					} else if(reuse.request.requestedFilePath.equalsIgnoreCase(DEFAULT_PAGE_ICON)) {//XXX Layout.css and favicon.ico resource to file
						//reuse.println("\t*** HTTP/1.1 200 OK - Admin file");
						@SuppressWarnings("resource")
						OutputStream outStream = s.getOutputStream();
						@SuppressWarnings("resource")
						DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
						out.println("HTTP/1.1 200 OK");
						if(authResult.authorizedCookie != null) {
							out.println("Set-Cookie: " + authResult.authorizedCookie);
						}
						File file = ResourceFactory.getResourceFromStreamAsFile(adminFolder, "textures/icons/favicon.ico");
						out.flush();
						sendFileToClient(out, outStream, reuse, HTTP_200, file, reuse.request.method, clientAddress, 0x400, false);
						reuse.response.setStatusMessage("Admin file");
					} else if(reuse.request.requestedFilePath.equalsIgnoreCase(DEFAULT_STYLESHEET)) {
						//reuse.println("\t*** HTTP/1.1 200 OK - Admin file");
						@SuppressWarnings("resource")
						OutputStream outStream = s.getOutputStream();
						@SuppressWarnings("resource")
						DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
						out.println("HTTP/1.1 200 OK");
						if(authResult.authorizedCookie != null) {
							out.println("Set-Cookie: " + authResult.authorizedCookie);
						}
						File file = ResourceFactory.getResourceFromStreamAsFile(adminFolder, "files/layout.css", "layout.css");
						out.flush();
						sendFileToClient(out, outStream, reuse, HTTP_200, file, reuse.request.method, clientAddress, 0x400, false);
						reuse.response.setStatusMessage("Admin file");
					} else {
						File requestedFile = new File(adminFolder, StringUtils.decodeHTML(reuse.request.requestedFilePath.substring(1)));
						if(requestedFile.exists() && requestedFile.isFile()) {
							//reuse.println("\t*** HTTP/1.1 200 OK - Admin file");
							@SuppressWarnings("resource")
							OutputStream outStream = s.getOutputStream();
							@SuppressWarnings("resource")
							DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, "UTF-8"), true);
							out.println("HTTP/1.1 200 OK");
							if(authResult.authorizedCookie != null) {
								out.println("Set-Cookie: " + authResult.authorizedCookie);
							}
							out.flush();
							sendFileToClient(out, outStream, reuse, HTTP_200, requestedFile, reuse.request.method, clientAddress, 0x400, true);
							reuse.response.setStatusMessage("Admin file");
						} else {
							reuse.response.setStatusMessage("Admin Page");
							reuse.response.setStatusCode(HTTP_404);
							reuse.response.setHeader("Vary", "Accept-Encoding");
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
									+ "\t\t<string>The file \"" + reuse.request.requestedFilePath + "\" does not exist.</string><br>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						}
					}
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
				} else if(reuse.request.method.equalsIgnoreCase("POST")) {
					if(reuse.request.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						final String createDomainUUID = reuse.request.requestArguments.get("createDomain");
						final String domainUUID = reuse.request.requestArguments.get("domain");
						final String createResFileUUID = reuse.request.requestArguments.get("createResFile");
						final String restrictedFileUUID = reuse.request.requestArguments.get("restrictedFile");
						final String forumUUID = reuse.request.requestArguments.get("forum");
						final String boardUUID = reuse.request.requestArguments.get("board");
						final String topicUUID = reuse.request.requestArguments.get("topic");
						final String commentUUID = reuse.request.requestArguments.get("comment");
						final HashMap<String, String> values = reuse.request.getFormURLEncodedData().postRequestArguments;
						if(reuse.request.requestedFilePath.equals("/serverSettings") || reuse.request.requestedFilePath.equals("/serverSettings/")) {
							if(values.isEmpty()) {
								reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
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
							reuse.response.setStatusMessage("Admin Page(POST - Server Settings)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.protocol + reuse.request.host).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
						} else if(createResFileUUID != null) {//XXX Create Restricted File (POST)
							if(StringUtils.isStrUUID(createResFileUUID)) {
								if(values.isEmpty()) {
									reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								final UUID uuid = UUID.fromString(createResFileUUID);
								final RestrictedFile res = RestrictedFile.createNewRestrictedFile(uuid);
								final String filePathKey = "FilePath";
								final String filePath = FilenameUtils.normalize(values.get(filePathKey));
								final File file = filePath != null ? new File(filePath) : null;
								reuse.printlnDebug("file == null: " + (file == null) + "; file exists: " + (file != null ? file.exists() + "" : "N/A"));
								if(file != null && file.exists()) {
									RestrictedFile check = RestrictedFile.getSpecificRestrictedFile(file);
									if(check == null) {
										values.remove(filePathKey);
										res.setRestrictedFile(file);
										res.setValuesFromHashMap(values, reuse);
										reuse.response.setStatusMessage("Admin Page(POST: File \"" + FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(res.getRestrictedFile().getAbsolutePath() + File.separatorChar)) + "\" restricted)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?page=restrictedFiles").setResponse((String) null).sendToClient(s, false);
										return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
									}
									res.delete();
									reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?resFileAlreadyExistsError=" + check.getUUID().toString()).setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								res.delete();
							}
							reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?createResFileError=" + createResFileUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
						} else if(createDomainUUID != null) {//XXX Create Domain (POST)
							if(StringUtils.isStrUUID(createDomainUUID)) {
								if(values.isEmpty()) {
									reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
								}
								UUID uuid = UUID.fromString(createDomainUUID);
								DomainDirectory domain = DomainDirectory.createNewDomainDirectory(uuid);
								String domainName = values.get(domain.domain.getName());
								String homeDirectoryPath = values.get("HomeDirectory");
								File homeDirectory = new File(homeDirectoryPath);
								if(!homeDirectory.exists()) {
									reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?page=createDomain&domainName=" + values.get("Domain") + "&homeDirectoryError=" + homeDirectoryPath).setResponse((String) null).sendToClient(s, false);
									domain.delete();
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								if(domainName != null && !domainName.isEmpty()) {
									DomainDirectory check = DomainDirectory.getDomainDirectoryFromDomainName(domainName);
									if(check == null) {
										values.remove(domain.domain.getName());
										domain.domain.setValue(domainName);
										domain.setValuesFromHashMap(values, reuse);
										domain.add();
										reuse.response.setStatusMessage("Admin Page(POST: Domain \"" + domain.getDomain() + "\" created)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?page=domains").setResponse((String) null).sendToClient(s, false);
										return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
									}
									domain.delete();
									reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?domainAlreadyExistsError=" + check.getUUID().toString()).setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
								}
								domain.delete();
							}
							reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?createDomainError=" + createDomainUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
						} else if(domainUUID != null) {
							DomainDirectory domain = DomainDirectory.getDomainDirectoryFromUUID(domainUUID);
							if(domain != null) {
								if(values.isEmpty()) {
									reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
								}
								domain.setValuesFromHashMap(values, reuse);
								reuse.response.setStatusMessage("Admin Page(POST_DOMAIN_DIRECTORY)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?domain=" + domainUUID).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
							}
							reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?domainError=" + domainUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
						} else if(restrictedFileUUID != null) {
							RestrictedFile res = RestrictedFile.getRestrictedFileFromUUID(restrictedFileUUID);
							if(res != null) {
								if(values.isEmpty()) {
									reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
								}
								res.setValuesFromHashMap(values, reuse);
							} else {
								reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?restrictedFileError=" + restrictedFileUUID).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
							}
						} else if(forumUUID != null) {
							ForumData forum = ForumData.getForumDataFromUUID(forumUUID);
							if(forum != null) {
								if(values.isEmpty()) {
									reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad postRequestString: \"" + reuse.request.getFormURLEncodedData().postRequestStr + "\"").setResponse((String) null).sendToClient(s, false);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
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
														reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?commentError=" + commentUUID).setResponse((String) null).sendToClient(s, false);
														return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
													}
												} else {
													topic.setValuesFromHashMap(values);
												}
											} else {
												reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?topicError=" + topicUUID).setResponse((String) null).sendToClient(s, false);
												return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
											}
										} else {
											board.setValuesFromHashMap(values);
										}
									} else {
										reuse.response.setStatusMessage("Admin Page(POST_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?boardError=" + boardUUID).setResponse((String) null).sendToClient(s, false);
										return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
									}
								} else {
									forum.setValuesFromHashMap(values);
								}
								reuse.response.setStatusMessage("Admin Page(POST_FORUM)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + reuse.request.requestArgumentsStr).setResponse((String) null).sendToClient(s, false);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
							}
							reuse.response.setStatusMessage("Admin Page(POST_FORUM_ERROR)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + "?restrictedFileError=" + restrictedFileUUID).setResponse((String) null).sendToClient(s, false);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
						}
						reuse.response.setStatusMessage("Admin Page(POST_UNKNOWN)").setStatusCode(HTTP_303).setHeader("Location", reuse.request.getRequestedPathLink(true) + reuse.request.requestArgumentsStr).setResponse((String) null).sendToClient(s, false);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
					} else if(reuse.request.contentType.toLowerCase().contains("multipart/form-data")) {
						/*if(reuse.request.multiPartFormData != null) {
							if(!reuse.request.multiPartFormData.fileData.isEmpty()) {
								for(FileData data : reuse.request.multiPartFormData.fileData) {
									try {
										data.writeToFile(adminFolder);//For testing purposes only.
									} catch(IOException e) {
										PrintUtil.printThrowable(e);
									}
								}
								reuse.response.setStatusCode(HTTP_201);
								reuse.response.setHeader("Vary", "Accept-Encoding");
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								reuse.response.setHeader("Location", "/" + reuse.request.multiPartFormData.fileData.get(0).fileName);
								reuse.response.setHeader("Content-Length", "0");
								reuse.response.setResponse((String) null);
								reuse.response.sendToClient(s, false);
							} else {
								reuse.response.setStatusCode(HTTP_204);
								reuse.response.setHeader("Vary", "Accept-Encoding");
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								reuse.response.setHeader("Content-Length", "0");
								reuse.response.setResponse((String) null);
								reuse.response.sendToClient(s, false);
							}
							int i = 0;
							for(Entry<String, String> entry : reuse.request.multiPartFormData.formData.entrySet()) {
								PrintUtil.printlnNow("[" + i + "]: \"" + entry.getKey() + " = " + entry.getValue() + "\";");
								i++;
							}
							s.close();
							return;
						}*/
					} else {
						reuse.println("Unknown post request content type: \"" + reuse.request.contentType + "\"...");
					}
				}
				reuse.response.setStatusCode(HTTP_501);
				reuse.response.setHeader("Vary", "Accept-Encoding");
				reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
				reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
						+ "\t\t<string>The HTML method your client used(\"" + reuse.request.method + "\") or one of its functions is not implemented for this page.</string><br>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				reuse.response.sendToClient(s, true);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), null);
			}
			reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + reuse.request.protocolLine + "\"").setResponse((String) null).sendToClient(s, false);
			try {
				in.close();
			} catch(Throwable ignored) {
			}
			s.close();
			return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), null);
		} catch(IOException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equalsIgnoreCase("Client sent no data.")) {
					if(reuse.request != null) {
						reuse.request.cancel();
					}
					return new RequestResult(reuse.completeResponse(HTTP_499, false), null);
				}
			}
			reuse.printErrln("\t /!\\ \tFailed to respond to client request: \"" + clientAddress + "\"\r\n\t/___\\\tCause: " + e.getClass().getName() + ": " + e.getMessage());
			//e.printStackTrace();
			return new RequestResult(reuse.completeResponse(HTTP_500, false), e);
		}
	}
	
	/** <b>NOTE:</b> This method does NOT send the provided status code to<br>
	 * the client! This allows for you to send additional headers before<br>
	 * this method sends the remaining normal server headers to the client.<br>
	 * Be sure to send the status code before calling this method, or the<br>
	 * client will not understand the server response!
	 * 
	 * @param out PrintWriter wrapping outStream(for sending text)
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
	public static final void sendFileToClient(DualPrintWriter out, OutputStream outStream, ClientConnection reuse, HTTPStatusCodes code, File file, String method, String clientAddress, int mtu, boolean privateCache) throws IOException {
		reuse.response.setStatusCode(code);
		//FileInfo info = new FileInfo(file, null);
		final DomainDirectory domainDirectory = reuse.domainDirectory;
		final String filePath = FilenameUtils.normalize(file.getAbsolutePath());
		final String mimeType = domainDirectory.getMimeTypeForExtension(FilenameUtils.getExtension(filePath));
		final boolean isAdminResponse = reuse.socket.getLocalPort() == JavaWebServer.admin_listen_port;
		out.println("Content-Type: " + mimeType);
		out.println("Server: " + SERVER_NAME_HEADER);
		out.println("Cache-Control: " + (privateCache ? cachePrivateMustRevalidate : "public, max-age=604800"));
		out.println("Date: " + StringUtils.getCurrentCacheTime());
		out.println("Last-Modified: " + StringUtils.getCacheTime(file.lastModified()));
		out.println("Accept-Ranges: none");
		out.println("Content-Length: " + FileUtil.getSize(file));
		out.println("");
		out.flush();
		if(!method.equalsIgnoreCase("HEAD")) {
			try(InputStream fileIn = file.toURI().toURL().openConnection().getInputStream()) {
				FileInfo.copyInputStreamToOutputStream(fileIn, outStream, mtu, reuse.status);//1024, reuse.status);
				reuse.println("\t\t\tSent " + (isAdminResponse ? "admin " : "") + "file \n\t\t\t\"" + filePath + "\"\n\t\t\t to client \"" + clientAddress + "\" successfully.");
			} catch(Throwable e) {
				reuse.printErrln("\t /!\\ \tFailed to send " + (isAdminResponse ? "admin " : "") + "file \n\t/___\\\t\"" + filePath + "\"\n\t\t to client \"" + clientAddress + "\": " + e.getMessage());
			}
		}
	}
	
	/** <b>NOTE:</b> This method does NOT send the provided status code to<br>
	 * the client! This allows for you to send additional headers before<br>
	 * this method sends the remaining normal server headers to the client.<br>
	 * Be sure to send the status code before calling this method, or the<br>
	 * client will not understand the server response!
	 * 
	 * @param out PrintWriter wrapping outStream(for sending text)
	 * @param outStream OutputStream of Socket(for sending file bytes)
	 * @param data The requested data
	 * @param method The HTTP Method
	 * @param clientAddress The client's ip address
	 * @param mtu The networking MTU
	 * @param privateCache Whether or not the file should be private, indicating
	 *            no caching client-side.
	 * @param clientInfo The client's information(may be null)
	 * @throws IOException Thrown if there was an issue sending the file to the
	 *             client */
	public static final void sendBytesToClient(DualPrintWriter out, OutputStream outStream, ClientConnection reuse, HTTPStatusCodes code, byte[] data, String contentType, long lastModified, String method, String clientAddress, int mtu, boolean privateCache) throws IOException {
		reuse.response.setStatusCode(code);
		final boolean isAdminResponse = reuse.socket.getLocalPort() == JavaWebServer.admin_listen_port;
		out.println("Content-Type: " + contentType);
		out.println("Server: " + SERVER_NAME_HEADER);
		out.println("Cache-Control: " + (privateCache ? cachePrivateMustRevalidate : "public, max-age=604800"));
		out.println("Date: " + StringUtils.getCurrentCacheTime());
		out.println("Last-Modified: " + StringUtils.getCacheTime(lastModified));
		out.println("Accept-Ranges: none");
		out.println("Content-Length: " + Integer.toString(data.length));
		out.println("");
		out.flush();
		if(!method.equalsIgnoreCase("HEAD")) {
			try {
				FileInfo.copyDataToOutputStream(data, outStream, mtu, reuse.status);//1024, clientInfo);
				reuse.println("\t\t\tSent " + (isAdminResponse ? "admin " : "") + "data to client \"" + clientAddress + "\" successfully.");
			} catch(Throwable e) {
				reuse.printErrln("\t /!\\ \tFailed to send " + (isAdminResponse ? "admin " : "") + "data to client \"" + clientAddress + "\": " + e.getMessage());
			}
		}
	}
	
	/** Assembles the client's request data, attempts to connect to the client's
	 * requested server, then sends the client's original request to the remote
	 * server.<br>
	 * If {@link #sendProxyHeadersWithRequest} is set to true, then
	 * <code>Via:</code> and <code>X-Forwarded-For:</code> headers are sent
	 * along with the client's existing request headers, identifying this
	 * connection as a proxy, and identifying the client to the remote
	 * server.<br>
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
	 * @see #HandleRequest(Socket, InputStream, boolean, ClientConnection)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo) */
	protected static final String handleProxyRequest(final Socket client, final ClientConnection reuse, final InputStream clientIn, final OutputStream clientOut, final HTTPClientRequest request, boolean connect) throws Throwable {
		reuse.status.setProxyRequest(true);
		reuse.status.setIncoming(false);
		if(!enableProxyServer) {
			throw new IllegalStateException("Method \"handleProxyRequest\" called when proxy server is disabled!");
		}
		if(reuse.request.isCompleted()) {
			throw new IllegalArgumentException("'request' has already been completed(marked for garbage collection)!");
		}
		if(client == null || client.isClosed() || client.isInputShutdown() || client.isOutputShutdown()) {
			throw new SocketException("Socket is closed.");
		}
		if(!reuse.request.isProxyRequest) {
			throw new IllegalArgumentException("Client request was not a proxy request.");
		}
		reuse.status.setProxyRequest(true);
		String clientAddress = StringUtils.replaceOnce(client.getRemoteSocketAddress().toString(), "/", "");
		final int clientPort = AddressUtil.getPortFromAddress(clientAddress);
		clientAddress = clientAddress.replace(":" + clientPort, "");
		
		String serverAddress = reuse.request.requestedServerAddress;
		int serverPort = AddressUtil.getPortFromAddress(serverAddress);
		if(serverPort == -1) {
			serverPort = reuse.request.protocolLine.toLowerCase().startsWith("https://") ? 443 : 80;
		} else {
			serverAddress = serverAddress.replace(":" + serverPort, "");//serverAddress.substring(0, serverAddress.length() - (":" + serverPort).length());
		}
		
		String getProxyAddress = AddressUtil.getIp();
		final String proxyAddress = getProxyAddress.isEmpty() ? AddressUtil.getClientAddress(StringUtils.replaceOnce(client.getLocalSocketAddress().toString(), "/", "")) : getProxyAddress + ":" + client.getLocalPort();
		final String ipDescription = "<s>" + serverAddress + ":" + serverPort + "</s> --&gt; " + proxyAddress + " --&gt; " + clientAddress + (clientPort != -1 ? ":" + clientPort : "");
		
		String clientResponse;
		try {
			clientResponse = new String(Base64.getDecoder().decode(reuse.request.proxyAuthorization.replace("Basic", "").trim()));
		} catch(IllegalArgumentException ignored) {
			clientResponse = "";
		}
		String[] creds = clientResponse.split(":");
		String clientUser = creds.length == 2 ? creds[0] : "";
		String clientPass = creds.length == 2 ? creds[1] : "";
		if(proxyRequiresAuthorization && !(proxyUsername.equalsIgnoreCase(clientUser) && proxyPassword.equals(clientPass))) {
			final HTTPStatusCodes code = HTTP_407;
			reuse.println("[PROXY] " + reuse.request.version + " " + code);
			PrintStream co = new PrintStream(clientOut);
			co.println(reuse.request.version + " " + code);
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
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
					+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
					+ "\t\t<title>" + code.getName() + " - " + code.getValue() + " - " + SERVER_NAME + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error " + code.getName() + " - " + code.getValue() + "</h1><hr>\r\n"//
					+ "\t\t<string>This proxy requires a username and password.</string><br>\r\n"//
					+ "\t\t<hr><string><b>" + ipDescription + "</b></string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(reuse.request.acceptEncoding.contains("gzip") && response.length() > 33) {
				co.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(response, "UTF-8", reuse);
				co.println("Content-Length: " + r.length);
				co.println("");
				co.write(r);
				r = null;
			} else {
				co.println("Content-Length: " + response.length());
				co.println("");
				co.println(response);
			}
			reuse.status.setIncoming(true);
			reuse.status.setProxyRequest(false);
			return serverAddress;
		}
		//reuse.status.setProxyRequest(true);
		//reuse.status.addToList(connectedProxyRequests);
		DomainDirectory check = DomainDirectory.getDomainDirectoryFromDomainName(serverAddress);
		if(check != null) {
			reuse.request.domainDirectory = reuse.request.domainDirectory == null ? check : reuse.request.domainDirectory;
			if(!check.getDisplayLogEntries()) {
				PrintUtil.clearLogsBeforeDisplay();
				PrintUtil.clearErrLogsBeforeDisplay();
			}
		}
		reuse.println("[PROXY] Server address: " + serverAddress + "; Port: " + serverPort);
		reuse.status.setStatus("[PROXY] Attempting to connect to server \"" + serverAddress + ":" + serverPort + "\"...");
		final SocketConnectResult result = new SocketConnectResult(serverAddress, serverPort);
		final Socket server = result.s;
		
		if(!result.failedToConnect) {
			if(connect || serverPort == 443) {
				reuse.status.setStatus("[PROXY] Connection to server \"" + serverAddress + ":" + serverPort + "\" was successful.");
				PrintStream co = new PrintStream(clientOut);
				co.println(reuse.request.version + " " + HTTP_200_1);
				co.println("");
				reuse.println("[PROXY] " + reuse.request.version + " " + HTTP_200_1);
			} else {
				reuse.status.setStatus("[PROXY] Connection to server \"" + serverAddress + ":" + serverPort + "\" was successful. Relaying client's request to server...");
				reuse.println("[PROXY] Relaying client request headers to requested server...");
				PrintStream so = new PrintStream(result.out);
				so.println(reuse.request.version.toUpperCase().startsWith("HTTP/2.") ? reuse.request.protocolLine : (reuse.request.method + " " + (reuse.request.requestedFilePath.trim().isEmpty() ? "/" : reuse.request.requestedFilePath) + " " + reuse.request.version));
				if(sendProxyHeadersWithRequest) {
					so.println("X-Forwarded-For: " + clientAddress);
					final String serverVia = reuse.request.version.toUpperCase().replace("HTTP/", "") + " " + PROXY_SERVER_NAME + " (" + SERVER_NAME_HEADER + ")";
					//Set<String> keySet = reuse.request.headers.keySet();
					if(Header.collectionContainsHeader(reuse.request.headers, "Via")) {//if(StringUtils.containsIgnoreCase(keySet, "Via")) {
						//String viaKey = StringUtils.getStringInList(keySet, "Via");
						//String viaValue = reuse.request.headers.get(viaKey);
						Header via = Header.getHeaderFrom(reuse.request.headers, "Via");
						so.println(via.header.trim() + ": " + (via.value == null || via.value.isEmpty() ? serverVia : via.value + ", " + serverVia));
					} else {
						so.println("Via: " + serverVia);
					}
				}
				reuse.status.setContentLength(reuse.request.headers.size());
				reuse.status.setCount(0);
				for(Header header : reuse.request.headers) {
					if(header.value != null) {
						if(!sendProxyHeadersWithRequest) {
							if(header.value.trim().toLowerCase().startsWith("proxy")) {
								continue;
							}
						}
						if(header.value.trim().equalsIgnoreCase("Proxy-Authorization")) {
							continue;
						}
						so.println(header.toString());
					}
					reuse.status.incrementCount();
				}
				so.println("");
				if(reuse.request.postRequestData.length > 0) {
					reuse.println("[PROXY] Relaying client's POST request data to requested server...");
					reuse.status.setContentLength(reuse.request.postRequestData.length);
					reuse.status.setCount(0);
					for(int i = 0; i < reuse.request.postRequestData.length; i++) {
						so.write(reuse.request.postRequestData[i]);
						reuse.status.incrementCount();
					}
				}
				so.flush();
				reuse.println("[PROXY] Client request sent to requested server successfully.");
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
				public final void run() {
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
								reuse.status.addNumOfBytesSentToServer(clientRead);
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
				public final void run() {
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
								reuse.status.addNumOfBytesSentToClient(serverRead);
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
				reuse.status.setIncoming(false);
				reuse.status.setProxyRequest(true);
				//CodeUtil.sleep(10L);
				if(clientException.getValue() != null) {
					throw clientException.getValue();
				}
				if(serverException.getValue() != null) {
					throw serverException.getValue();
				}
				reuse.status.setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Client MTU: " + clientMTU + "; Server MTU: " + serverMTU + "; Data: Client --> Server: " + Functions.humanReadableByteCount(reuse.status.getNumOfBytesSentToServer(), true, 2) + "; Server --> Client: " + Functions.humanReadableByteCount(reuse.status.getNumOfBytesSentToClient(), true, 2));
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
				Functions.sleep();
			}
		} else {
			reuse.status.setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Unable to connect to server: " + result.failReason);
			final HTTPStatusCodes code = result.failReason.toLowerCase().contains("timed out") ? HTTP_504 : HTTP_502;
			PrintUtil.printlnNow("[PROXY] " + reuse.request.version + " " + code);
			PrintStream co = new PrintStream(clientOut);
			co.println(reuse.request.version + " " + code);
			co.println("Server: " + SERVER_NAME_HEADER);
			co.println("Date: " + StringUtils.getCurrentCacheTime());
			co.println("Cache-Control: " + cachePrivateMustRevalidate);
			co.println("Vary: Accept-Encoding");
			co.println("Content-Type: text/html; charset=UTF-8");
			String response = "<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
					+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
					+ "\t\t<title>" + code.getName() + " - " + code.getValue() + " - " + SERVER_NAME + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error " + code.getName() + " - " + code.getValue() + "</h1><hr>\r\n"//
					+ "\t\t<string>The proxy was unable to connect to \"" + serverAddress + ":" + serverPort + "\".</string><br>\r\n"//
					+ "\t\t<string>Reason: <b>" + result.failReason + "</b></string>\r\n"//
					+ "\t\t<hr><string><b>" + ipDescription + "</b></string>\r\n"//
					+ "\t</body>\r\n</html>";
			if(reuse.request.acceptEncoding.contains("gzip") && response.length() > 33) {
				co.println("Content-Encoding: gzip");
				byte[] r = StringUtils.compressString(response, "UTF-8", reuse);
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
		reuse.status.setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; Closing down connections...");
		try {
			client.close();
		} catch(Throwable ignored) {
		}
		try {
			server.close();
		} catch(Throwable ignored) {
		}
		reuse.status.setStatus("[PROXY] Server: \"" + serverAddress + ":" + serverPort + "\"; All done!");
		reuse.status.setIncoming(true);
		reuse.status.setProxyRequest(false);
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
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	protected static final RequestResult HandleRequest(final Socket s, final InputStream in, boolean https, final ClientConnection reuse) {
		try {
			return _HandleRequest(s, in, https, reuse);
		} catch(Throwable e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
			throw e;
		}
	}
	
	/** Parses the client's incoming server request(via
	 * {@link #HandleRequest(Socket, InputStream, boolean, ClientConnection)}),
	 * then
	 * returns the result.
	 * 
	 * @param s The client
	 * @param in The client's input stream
	 * @param https HTTPS(<code>true</code>) or HTTP(<code>false</code>)
	 * @return The result of parsing the client's request.
	 * @see #HandleRequest(Socket, InputStream, boolean, ClientConnection)
	 * @see #serveFileToClient(Socket, boolean, boolean, OutputStream,
	 *      HTTPServerResponse, ClientInfo, DomainDirectory, HTTPClientRequest)
	 * @see #HandleAdminRequest(Socket, InputStream, ClientConnection)
	 * @see #handleAdministrateFile(HTTPClientRequest, File, String, String,
	 *      String, OutputStream, PrintWriter, DomainDirectory, boolean, String,
	 *      String, ClientInfo)
	 * @see #handleProxyRequest(Socket, InputStream, OutputStream,
	 *      HTTPClientRequest, boolean) */
	@SuppressWarnings("resource")
	private static final RequestResult _HandleRequest(Socket s, final InputStream in, boolean https, final ClientConnection reuse) {
		if(s == null || s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
			printlnDebug("_HandleRequest Socket closed!!!!!1111111111111111111111111111111111");
			//serverAdministrationAuths.remove(Thread.currentThread());
			return new RequestResult(reuse.completeResponse(HTTP_499, false), null);
		}
		PrintUtil.clearLogsBeforeDisplay();
		PrintUtil.clearErrLogsBeforeDisplay();
		final Socket sock = s;
		if(HTTPClientRequest.debug) {
			s = new Socket() {
				private volatile boolean isClosed = false;
				//if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
				
				@Override
				public final void setKeepAlive(boolean on) throws SocketException {
					sock.setKeepAlive(on);
				}
				
				@Override
				public final boolean isClosed() {
					return this.isClosed;
				}
				
				@Override
				public final boolean isInputShutdown() {
					return sock.isInputShutdown();
				}
				
				@Override
				public final boolean isOutputShutdown() {
					return sock.isOutputShutdown();
				}
				
				@Override
				public final InetAddress getInetAddress() {
					return sock.getInetAddress();
				}
				
				@Override
				public final synchronized void close() throws IOException {
					this.isClosed = true;
					Error e = new Error("WHAT DOING?!");
					e.printStackTrace();
					try {
						sock.close();
					} catch(IOException ignored) {
					}
					throw e;
				}
				
				@Override
				public final OutputStream getOutputStream() throws IOException {
					return sock.getOutputStream();
				}
				
				@Override
				public final InputStream getInputStream() throws IOException {
					return sock.getInputStream();
				}
				
				@Override
				public final SocketAddress getRemoteSocketAddress() {
					return sock.getRemoteSocketAddress();
				}
				
			};
		}
		/*if(!reuse.isReused()) {
			serverAdministrationAuths.put(Thread.currentThread(), new HttpDigestAuthorization(adminAuthorizationRealm, adminUsername, adminPassword));
		}*/
		DomainDirectory domainDirectory = null;
		final String clientAddressOriginal = AddressUtil.getClientAddress(s);
		String getClientAddress = clientAddressOriginal;
		final boolean responseLogsIncluded = https ? httpsResponseLogsIncluded : httpResponseLogsIncluded;
		Throwable exception = null;
		reuse.allowReuse = false;//This gets set back to true down below if Keep-Alive is requested by the client!
		try {
			if(reuse.isReused()) {
				reuse.println("Reused Connection: " + clientAddressOriginal);
			}
			if(s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
				reuse.println("\t\tIncoming connection from \"" + clientAddressOriginal + "\" was interrupted before the client could send any data.");
				return new RequestResult(reuse.completeResponse(HTTP_499, false), new IOException("Incoming connection from \"" + clientAddressOriginal + "\" was interrupted before the client could send any data."));
			}
			if(in.available() < 1) {
				reuse.println("\tWaiting on client to send " + (reuse.isReused() ? "next " : "") + "request...");
			}
			reuse.request = new HTTPClientRequest(s, in, https, reuse);
			RequestResult result = reuse.request.acceptClientRequestSafe(requestTimeout);
			domainDirectory = reuse.request.host == null || reuse.request.host.trim().isEmpty() ? null : DomainDirectory.getDomainDirectoryFromDomainName(reuse.request.host);
			reuse.domainDirectory = domainDirectory;
			//connectedClients.add(clientInfo);
			reuse.printlnDebug("Request Accepted! result: " + (result == null ? "[null]" : "non-null! response.toString(): " + (result.getResponse())) + "; request.clientRequestAccepted(): " + reuse.request.clientRequestAccepted());
			if(result != null && !reuse.request.clientRequestAccepted()) {
				/*if(!reuse.request.requestLogs.isEmpty()) {
					reuse.appendLog(reuse.request.requestLogs);
					reuse.request.requestLogs = "";
				}*/
				reuse.request.cancel();
				return result;
			}
			if(result != null) {//XXX && HTTPClientRequest.debug) {
				reuse.println(result.toString());
			}
			if(result != null) {
				if(reuse.getStatusCode() == HTTPStatusCodes.HTTP_408 || reuse.getStatusCode() == HTTP_500) {
					//connectedClients.remove(clientInfo);
					return result;
				}
				if(!sockets.contains(reuse)) {
					sockets.add(reuse);
				}
			}
			if(!reuse.request.xForwardedFor.isEmpty()) {
				reuse.println("\tIdentified client behind proxy: " + reuse.request.xForwardedFor);
			}
			final String clientAddress = getClientAddress(reuse.request);
			//final String clientIP = getClientIPNoPort(request);//clientAddress == null ? null : clientAddress.substring(0, clientAddress.contains(":") ? clientAddress.lastIndexOf(":") - 1 : clientAddress.length());
			final String reqHost = (reuse.request.host.isEmpty() && reuse.request.isProxyRequest ? reuse.request.http2Host : reuse.request.host).trim();
			final String host;
			if(!reuse.isReused() || (reuse.hostUsed == null || reuse.hostUsed.isEmpty())) {
				String setHost = reqHost == null ? null : reqHost;
				if(setHost == null || setHost.trim().isEmpty() || setHost.trim().equals("null")) {
					setHost = null;
				}
				host = setHost;
				if(host != null) {
					if(!host.isEmpty()) {
						reuse.println("\t--- Client connected using host: " + reqHost);
					} else {
						if(reuse.request.version.equalsIgnoreCase("HTTP/1.1")) {
							reuse.println("\t--- Client connected using HTTP/1.1, but did not specify a host!");
							if(reuse.request.userAgent != null && reuse.request.userAgent.trim().equals("Telesphoreo")) {
								reuse.println("\t--- Client is using Telesphoreo, which is a silly user agent that");
								reuse.println("\t\thasn't been updated to follow HTTP/1.1 protocol, even though");
								reuse.println("\t\tit seems to still want to be able to use HTTP/1.1. </rant>");
							}
						}
					}
				} else {
					reuse.println("\t--- Client connected without sending a host header...");
				}
			} else {
				if(!reqHost.isEmpty() && !reuse.hostUsed.equalsIgnoreCase(reqHost)) {
					reuse.println("\t--- Client reused existing connection, but used a different\n\t\thost(weird, but we'll try to roll with it): " + reqHost);
					host = reqHost;
				} else if(reqHost.isEmpty() && !reuse.hostUsed.isEmpty()) {
					reuse.println("\t--- Client originally connected using host: \"" + reuse.hostUsed + "\";\n\t\tbut did not send a host header this time.");
					host = reuse.hostUsed;
				} else {
					host = reqHost;
				}
			}
			reuse.hostUsed = host;
			if(reuse.request.host.isEmpty()) {
				reuse.request.host = AddressUtil.getIp();
			}
			/*if(!reuse.request.requestLogs.isEmpty()) {
				reuse.appendLog(reuse.request.requestLogs);
				reuse.request.requestLogs = "";
			}*/
			if(reuse.request.isProxyRequest) {
				if(enableProxyServer) {
					reuse.status.setIncoming(false);
					reuse.status.setProxyRequest(true);
					final HTTPClientRequest req = reuse.request;
					final Socket socket = s;
					Thread proxyThread = new Thread(new Runnable() {
						@Override
						public final void run() {
							Throwable err = null;
							try {
								String serverAddress = handleProxyRequest(socket, reuse, in, socket.getOutputStream(), req, req.method.equalsIgnoreCase("CONNECT"));
								PrintUtil.printlnNow("[PROXY] Successfully handled proxy request between client \"" + clientAddress + "\" and server \"" + serverAddress + "\".");
								PrintUtil.printErrToConsole();
							} catch(Throwable e) {
								err = e;
								PrintUtil.printToConsole();
								PrintUtil.printErrlnNow("[PROXY] Failed to handle proxy request for client \"" + clientAddress + "\": " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
								if(e instanceof NullPointerException) {
									e.printStackTrace();
								}
							}
							req.setLogResult(err == null ? "Proxy Request ended normally at: " + StringUtil.getSystemTime(false, false, true) : Functions.throwableToStr(err));
							req.markCompleted(responseLogsIncluded);
							removeSocket(socket);
						}
					}, Thread.currentThread().getName().replace("Server", "Proxy"));
					PrintUtil.moveLogsToThreadAndClear(proxyThread);
					PrintUtil.moveErrLogsToThreadAndClear(proxyThread);
					proxyThread.setDaemon(true);
					proxyThread.start();
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(HTTP_102, false), exception);
				}
				final String pageHeader = "<hr><b>" + reuse.request.requestedFilePath + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + (reuse.request.xForwardedFor.isEmpty() ? "(Forwarded for: " + reuse.request.xForwardedFor + ")" : "") + "</b>";
				if(reuse.request.method.equalsIgnoreCase("CONNECT")) {
					reuse.response.setStatusCode(HTTP_405);//(HTTP/1.1 405 Method Not Allowed)
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
					reuse.response.setHeader("Date", StringUtil.getCurrentCacheTime());
					reuse.response.setResponse("<!DOCTYPE html>\r\n"//
							+ "<html>\r\n\t<head>\r\n"//
							+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
							+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
							+ "\t\t<link rel=\"shortcut icon\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_PAGE_ICON + "\" type=\"image/x-icon\">\r\n"//
							+ (DomainDirectory.doesStyleSheetExist(DEFAULT_STYLESHEET) ? "\t\t<link rel=\"stylesheet\" href=\"" + reuse.request.protocol + AddressUtil.getIp() + DEFAULT_STYLESHEET + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(DomainDirectory.getStyleSheetFromFileSystem(DEFAULT_STYLESHEET)) + "\">\r\n" : "")//
							+ "\t\t<title>405 - Method Not Allowed - " + SERVER_NAME + "</title>\r\n"//
							+ "\t\t<style>body{font-family:\'" + defaultFontFace + "\';}</style>\r\n"//
							+ "\t</head>\r\n"//
							+ "\t<body>\r\n"//
							+ "\t\t<h1>Error 405 - Method Not Allowed</h1>\r\n"//
							+ "\t\t<string>Your client's request was a valid proxy request, but this server's proxy functionality is not active.</string>\r\n"//
							+ "\t\t" + pageHeader + "\r\n"//
							+ "\t</body>\r\n</html>");
					reuse.response.sendToClient(s, true);
				} else {
					ResponseUtil.send421Response(s, reuse, domainDirectory);
				}
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
			}
			reuse.status.setProxyRequest(false);
			reuse.status.setIncoming(false);
			boolean hostDefined = host != null && !host.trim().isEmpty();
			//reuse.request.host = AddressUtil.getClientAddress(reuse.request.host);//AddressUtil.getValidHostFor(host, reuse);
			if(!hostDefined && reuse.request.version.equalsIgnoreCase("HTTP/1.1")) {
				reuse.response.clearHeaders().setUseGZip(false).setStatusCode(HTTP_400).setStatusMessage("No host header defined: \"" + host + "\"").setResponse((String) null).sendToClient(s, false);
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
			}
			if(reuse.request.method.isEmpty()) {
				reuse.response.clearHeaders().setUseGZip(false).setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + reuse.request.protocolLine + "\"").setResponse((String) null).sendToClient(s, false);
				if(!HTTPClientRequest.debug) {
					reuse.clearAllLogsBeforeDisplay();
				}
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
			}
			/*reuse.println("\t--- Client request: " + reuse.request.protocolLine);
			if(!reuse.request.requestArgumentsStr.isEmpty()) {
				reuse.println("\t\tRequest Arguments: \"" + reuse.request.requestArgumentsStr + "\"");
			}*/
			reuse.allowReuse = reuse.request.connectionSetting.toLowerCase().contains("keep-alive");
			if(reuse.request.connectionSetting.toLowerCase().contains("close")) {
				reuse.allowReuse = false;
			}
			s.setKeepAlive(reuse.allowReuse);
			
			if(reuse.request.method.equalsIgnoreCase("brew")) {
				reuse.response.clearHeaders().setUseGZip(reuse.request.acceptEncoding.toLowerCase().contains("gzip")).setStatusCode(HTTP_501);
				if(reuse.request.version.toUpperCase().startsWith("HTCPCP/")) {
					reuse.response.setStatusCode(HTTP_418).setStatusMessage("Making coffee is okay.").setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your coffee is ready!\r\nDon't drink too much in a day.\r\n").sendToClient(s, true);
				} else {
					reuse.response.setStatusCode(HTTP_418).setStatusMessage("Making tea is fun!").setHeader("Date", StringUtils.getCurrentCacheTime()).setHeader("Server", SERVER_NAME_HEADER).setHeader("Content-Type", "text/plain; charset=UTF-8").setHeader("Cache-Control", cachePrivateMustRevalidate).setResponse("Your tea is ready! Enjoy.\r\n").sendToClient(s, true);
				}
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
			}
			
			final boolean domainIsLocalHost = AddressUtil.isDomainLocalHost(reuse.request.host);
			final DomainDirectory testDomain = DomainDirectory.getDomainDirectoryFromDomainName(reuse.request.host);
			if(testDomain != null) {
				domainDirectory = testDomain;
			} else if(domainIsLocalHost) {
				domainDirectory = DomainDirectory.getOrCreateDomainDirectory(reuse.request.host, reuse);
			} else {
				domainDirectory = DomainDirectory.getTemporaryDomain();
				domainDirectory.domain.setValue(reuse.request.host);
				domainDirectory.displayName.setValue(reuse.request.host);
				reuse.println("\t--- Using temporary domain: " + domainDirectory.getDomain());
			}
			reuse.request.domainDirectory = domainDirectory;
			if(!domainDirectory.getDisplayLogEntries()) {
				reuse.clearAllLogsBeforeDisplay();
			}
			reuse.domainDirectory = domainDirectory;
			reuse.response.setDomainDirectory(reuse.domainDirectory);//response.setDomainDirectory(domainDirectory);
			reuse.printlnDebug("Request Accepted! result: " + (result == null ? "[null]" : "non-null! response.toString(): " + (result.getResponse())) + "; request.clientRequestAccepted(): " + reuse.request.clientRequestAccepted());
			File homeDirectory = domainDirectory.getDirectorySafe();
			if(homeDirectory == null || !homeDirectory.exists() || !homeDirectory.isDirectory()) {
				homeDirectory = JavaWebServer.homeDirectory;
				reuse.println("\t\t /!\\ Unable to get home directory for domain\r\n\t\t/___\\\t\"" + reuse.request.host + "\"; using home directory as specified in the '" + optionsFileName + "' file.");
			} else {
				reuse.println("\t\tHome directory for domain \"" + domainDirectory.getDomain()/*reuse.request.host*/ + "\" is: \"" + homeDirectory.getAbsolutePath() + "\"!");
			}
			reuse.response.clearHeaders().setUseGZip(reuse.request.acceptEncoding.toLowerCase().contains("gzip") && domainDirectory.getEnableGZipCompression()).setStatusCode(HTTP_501);
			if(reuse.request.requestedFilePath.startsWith("/../")) {//idiots be trying to hack, not that this would even work...
				String reason = "Autobanned for issuing request beginning with '/../'";
				reuse.response.setStatusCode(HTTP_1337).setStatusMessage(reason).setResponse("<!DOCTYPE html>\r\n<http>\r\n\t<title>That won't work.</title>\r\n\t<body>\r\n\t\t<string>Nice try.</string>\r\n\t</body>\r\n</html>").sendToClient(s, true);
				JavaWebServer.issueCommand("sinbin ban " + AddressUtil.getClientAddressNoPort(clientAddress) + " " + reason);
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
			}
			/* else if(reuse.request.requestedFilePath.toLowerCase().startsWith("/phpmyadmin/scripts/setup.php")) {//Just stop already... -_- TODO maybe remove this and add an autoban_requests.txt file where each line is a request that will get you banned?
				String reason = "Autobanned for requesting: " + reuse.request.requestedFilePath;
				reuse.response.setStatusCode(HTTP_1337).setStatusMessage(reason).setResponse("<!DOCTYPE html>\r\n<http>\r\n\t<title>That won't work.</title>\r\n\t<body>\r\n\t\t<string>Nice try.</string>\r\n\t</body>\r\n</html>").sendToClient(s, true);
				JavaWebServer.issueCommand("sinbin ban " + AddressUtil.getClientAddressNoPort(clientAddress) + " " + reason);
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(response.getStatusCode(), false, domainDirectory), null);
			} else {*/
			AutobanDictionary.BanType banType = AutobanDictionary.isRequestProhibied(reuse.request, reuse);
			if(banType != AutobanDictionary.BanType.ALLOWED) {
				if(banType == AutobanDictionary.BanType.BLANK_USER_AGENT) {
					reuse.response.setStatusCode(HTTP_400);
					reuse.response.setStatusMessage("No User-Agent header received!");
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
					reuse.response.setResponse("<!DOCTYPE html>\r\n"//
							+ "<html>\r\n\t<head>\r\n"//
							+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
							+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
							+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
							+ "\t\t<title>400 - Bad Request - " + domainDirectory.getServerName() + "</title>\r\n"//
							+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
							+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
							+ "\t</head>\r\n"//
							+ "\t<body>\r\n"//
							+ "\t\t<h1>Error 400 - Bad Request</h1><hr>\r\n"//
							+ "\t\t<string>This server requires a non-empty User-Agent header, but your browser did not sent one.</string>\r\n"//
							+ "\t</body>\r\n</html>");
					reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
				} else if(banType.isBanned()) {
					String reason = "Autobanned for" + (banType == AutobanDictionary.BanType.BANNED_REQUEST_PATH ? " requesting: " + reuse.request.requestedFilePath : ": Bad User-Agent: \"" + reuse.request.userAgent + "\"!");
					reuse.response.setStatusCode(HTTP_308).setStatusMessage(reason).setHeader("Location", "http://localhost/").setHeader("Content-Type", "text/plain").setResponse(banType == AutobanDictionary.BanType.BANNED_REQUEST_PATH ? "That's an awfully specific path you just asked for. Tsk tsk." : "Yeeeah, erm, well, you see... we don't serve malicious spam bots. Sorry :/").sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
					JavaWebServer.issueCommand("sinbin ban " + AddressUtil.getClientAddressNoPort(clientAddress) + " " + reason);
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
				}
			}
			if(!reuse.request.protocolLine.isEmpty()) {
				final File requestedFile = domainDirectory.getFileFromRequest(reuse.request.requestedFilePath, reuse.request.requestArguments, reuse);//requestedFile = new File(homeDirectory, htmlToText(domainDirectory.replaceAliasWithPath(arg)));//arg
				final boolean isFileRestrictedOrHidden = RestrictedFile.isFileForbidden(requestedFile) || RestrictedFile.isFileRestricted(requestedFile, s) || RestrictedFile.isHidden(requestedFile, true);
				if(ForumClientResponder.HandleRequest(s, reuse, domainDirectory, reuse.request, reuse.response)) {
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
				}
				if(reuse.request.method.equalsIgnoreCase("OPTIONS")) {
					String path = FileInfo.getURLPathFor(domainDirectory, requestedFile);
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					
					if(((requestedFile != null && requestedFile.exists()) || reuse.request.requestedFilePath.equals("*")) && (testDomain != null || domainIsLocalHost)) {
						if(requestedFile != null) {
							if(domainDirectory.areDirectoriesForbidden() && requestedFile.isDirectory()) {
								reuse.response.setStatusCode(HTTP_403);
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
								reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
								reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
								//connectedClients.remove(clientInfo);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
							}
							/*//final FileInfo info = new FileInfo(requestedFile, domainDirectory);
							reuse.println("\t\tRequested file: \"" + requestedFile.getAbsolutePath() + "\"");
							if(RestrictedFile.isFileRestricted(requestedFile, s)) {
								RestrictedFile restrictedFile = RestrictedFile.getRestrictedFile(requestedFile);
								if(restrictedFile != null) {
									if(restrictedFile.isPasswordProtected()) {
										String clientResponse;
										try {
											clientResponse = new String(Base64.getDecoder().decode(reuse.request.authorization.replace("Basic", "").trim()));
										} catch(IllegalArgumentException ignored) {
											clientResponse = "";
										}
										String[] creds = clientResponse.split(":");
										String clientUser = creds.length == 2 ? creds[0] : "";
										String clientPass = creds.length == 2 ? creds[1] : "";
										if(!restrictedFile.areCredentialsValid(clientUser, clientPass)) {
											logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), clientUser, clientPass);
											reuse.response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "(client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
											reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
											reuse.response.setHeader("Server", SERVER_NAME_HEADER);
											reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
											reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
											reuse.response.setHeader("WWW-Authenticate", "Basic realm=\"" + restrictedFile.getAuthorizationRealm() + "\"");
											reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
											reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
											//connectedClients.remove(clientInfo);
											return new RequestResult(reuse.completeResponse(response.getStatusCode(), reuse.allowReuse), exception);
										}
									} else {
										reuse.response.setStatusCode(HTTP_403);
										reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
										reuse.response.setHeader("Server", SERVER_NAME_HEADER);
										reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
										reuse.response.setResponse("<!DOCTYPE html>\r\n"//
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
										reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
										//connectedClients.remove(clientInfo);
										return new RequestResult(reuse.completeResponse(response.getStatusCode(), reuse.allowReuse), exception);
									}
								}
							}*/
							RestrictedFile res = RestrictedFile.getSpecificRestrictedFile(requestedFile);
							res = res == null ? RestrictedFile.getRestrictedFile(requestedFile) : res;
							final boolean isDirectory = requestedFile.isDirectory();
							final boolean isFileRestricted;
							final boolean isIPAllowed;
							final boolean isFileForbidden;
							if(res == null) {
								isFileRestricted = false;
								isIPAllowed = true;
								isFileForbidden = false;
							} else {
								isFileRestricted = res.isPasswordProtected();
								isIPAllowed = !res.isIPAllowed(s);
								isFileForbidden = res.isFileForbidden();//If false, then there either is a password, or the file is actually not restricted.
							}
							reuse.response.setStatusCode(reuse.connectionSeemsMalicious ? HTTP_429 : HTTP_200);
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader("Content-Type", "text/plain; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Content-Length", "0");
							String allowHeader = "GET,HEAD," + (isDirectory ? "POST," : "") + "OPTIONS";
							if(isFileForbidden || isFileRestricted) {
								allowHeader = isIPAllowed ? allowHeader : "OPTIONS";
							}
							reuse.response.setHeader("Allow", allowHeader);//"GET,HEAD,POST,OPTIONS");
							reuse.response.setResponse((String) null);//TODO Maybe find out what content is sent with the OPTIONS method(Like 'httpd/unix-directory')?
							reuse.response.sendToClient(s, false);
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						} else if(reuse.request.requestedFilePath.equals("*")) {
							reuse.response.setStatusCode(reuse.connectionSeemsMalicious ? HTTP_429 : HTTP_200);
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader("Content-Type", "text/plain; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Content-Length", "0");
							reuse.response.setHeader("Allow", "GET,HEAD,POST," + (isProxyServerEnabled() ? "CONNECT," : "") + "OPTIONS");
							reuse.response.setResponse((String) null);//TODO Maybe find out what content is sent with the OPTIONS method(Like 'httpd/unix-directory')?
							reuse.response.sendToClient(s, false);
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						} else {
							ResponseUtil.send404Response(s, reuse.request, reuse.response, domainDirectory, pageHeader);//(The body of this response won't be sent since it is an OPTIONS request.)
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
					} else if(testDomain == null && !domainIsLocalHost) {
						ResponseUtil.send421Response(s, reuse, domainDirectory);
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					}
					ResponseUtil.send404Response(s, reuse.request, reuse.response, domainDirectory, pageHeader);
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
				} else if(reuse.request.method.equalsIgnoreCase("GET") || reuse.request.method.equalsIgnoreCase("HEAD")) {
					if(!reuse.request.requestedFilePath.startsWith("/")) {
						reuse.response.setStatusCode(HTTP_400);
						reuse.response.setStatusMessage("Client sent invalid protocol request: Path not prefixed by '/'!");
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
						reuse.response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>400 - Bad Request - " + domainDirectory.getServerName() + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
								+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
								+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 400 - Bad Request</h1><hr>\r\n"//
								+ "\t\t<string>Your client sent the following invalid HTTP request:</string><br>\r\n\t\t<pre>" + reuse.request.protocolLine + "</pre><br>\r\n\t\t<string>Request paths <b>must</b> be prefixed by a forward-slash(the '/' character).</string>\r\n"//
								+ "\t</body>\r\n</html>");
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
					}
					@SuppressWarnings("unused")
					final String pathPrefix = domainDirectory.getURLPathPrefix();
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					final String path = FileInfo.getURLPathFor(domainDirectory, requestedFile);
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					String administrateFileCheck = reuse.request.requestArguments.get("administrateFile");
					final boolean administrateFile = administrateFileCheck != null ? (administrateFileCheck.equals("1") || administrateFileCheck.equalsIgnoreCase("true")) : false;
					//String modifyFileCheck = reuse.request.requestArguments.get("modifyFile");
					//final boolean modifyFile = modifyFileCheck != null ? (modifyFileCheck.equals("1") || modifyFileCheck.equalsIgnoreCase("true")) : false;
					
					if(HTTPClientRequest.debug) {
						System.err.println("requestedFile != null && requestedFile.exists() && (testDomain != null || domainIsLocalHost)");
						System.err.println(String.valueOf(requestedFile != null) + " " + (requestedFile == null ? "[null] " : requestedFile.exists() + " ") + ((testDomain != null) + " || " + domainIsLocalHost));
						if(requestedFile != null) {
							System.err.println(requestedFile.getAbsolutePath());
						}
					}
					if(requestedFile != null && requestedFile.exists() && (testDomain != null || domainIsLocalHost)) {
						if(domainDirectory.areDirectoriesForbidden() && requestedFile.isDirectory() && !administrateFile && !requestedFile.equals(domainDirectory.getDirectorySafe())) {
							reuse.response.setStatusCode(HTTP_403);
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							reuse.response.setResponse("<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
									+ "\t</head>\r\n"//
									+ "\t<body>\r\n"//
									+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						reuse.println("\t\tRequested file: \"" + requestedFile.getAbsolutePath() + "\"");
						final FileInfo info = new FileInfo(requestedFile, domainDirectory);
						reuse.status.setRequestedFile(info);
						String layout = domainDirectory.getDefaultStylesheet();
						String favicon = domainDirectory.getDefaultPageIcon();
						layout = layout.startsWith("/") ? layout : "/" + layout;
						favicon = favicon.startsWith("/") ? favicon : "/" + favicon;
						boolean isExempt = administrateFile || reuse.request.requestedFilePath.equalsIgnoreCase(layout) || reuse.request.requestedFilePath.equalsIgnoreCase(favicon);
						
						if(RestrictedFile.isFileRestricted(requestedFile, s) && !isExempt) {
							reuse.printlnDebug("layout: \"" + layout + "\"; favicon: \"" + favicon + "\";\r\nrequestedFilePath: \"" + reuse.request.requestedFilePath + "\";");
							RestrictedFile restrictedFile = RestrictedFile.getSpecificRestrictedFile(requestedFile);
							restrictedFile = restrictedFile == null ? RestrictedFile.getRestrictedFile(requestedFile) : restrictedFile;
							boolean usedAPassword = false;
							if(restrictedFile != null) {
								if(restrictedFile.isPasswordProtected()) {
									String clientResponse;
									try {
										clientResponse = new String(Base64.getDecoder().decode(reuse.request.authorization.replace("Basic", "").trim()));
									} catch(IllegalArgumentException ignored) {
										clientResponse = "";
									}
									String[] creds = clientResponse.split(":");
									String clientUser = creds.length == 2 ? creds[0] : "";
									String clientPass = creds.length == 2 ? creds[1] : "";
									if(!restrictedFile.areCredentialsValid(clientUser, clientPass)) {
										NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), clientUser, clientPass);
										reuse.response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "(client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
										reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
										reuse.response.setHeader("Server", SERVER_NAME_HEADER);
										reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
										reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
										reuse.response.setHeader("WWW-Authenticate", "Basic realm=\"" + restrictedFile.getAuthorizationRealm() + "\"");
										reuse.response.setResponse("<!DOCTYPE html>\r\n"//
												+ "<html>\r\n\t<head>\r\n"//
												+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
												+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
												+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
												+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
												+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
												+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
												+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
												+ "\t</head>\r\n"//
												+ "\t<body>\r\n"//
												+ "\t\t<h1>Error 401 - Authorization Required</h1>\r\n"//
												+ "\t\t<string>" + pageHeader + "</string>\r\n"//
												+ "\t</body>\r\n</html>");
										reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
										//connectedClients.remove(clientInfo);
										return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
									}
									usedAPassword = true;
								}
								boolean exempt = false;
								if(!usedAPassword && RestrictedFile.isHidden(requestedFile, true)) {
									exempt = usedAPassword;
									RestrictedFile specificRes = RestrictedFile.getSpecificRestrictedFile(requestedFile);
									if(specificRes != null) {
										if(specificRes.isIPAllowed(s)) {
											exempt = true;
										}
									}
									/*if(!exempt) {
										ResponseUtil.send404Response(s, request, response, domainDirectory, pageHeader);
										return new RequestResult(reuse.completeResponse(response.getStatusCode(), reuse.allowReuse), exception);
									}*/
								}
								if(!exempt) {
									reuse.response.setStatusCode(HTTP_403);
									reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
									reuse.response.setHeader("Server", SERVER_NAME_HEADER);
									reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
									reuse.response.setResponse("<!DOCTYPE html>\r\n"//
											+ "<html>\r\n\t<head>\r\n"//
											+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
											+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
											+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
											+ "\t\t<title>403 - Forbidden - " + domainDirectory.getServerName() + "</title>\r\n"//
											+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
											+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
											+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
											+ "\t</head>\r\n"//
											+ "\t<body>\r\n"//
											+ "\t\t<h1>Error 403 - Forbidden</h1><hr>\r\n"//
											+ "\t\t<string>You do not have permission to view this content.</string>\r\n"//
											+ "\t\t<string>" + pageHeader + "</string>\r\n"//
											+ "\t</body>\r\n</html>");
									reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
									//connectedClients.remove(clientInfo);
									return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
								}
							}
						} else {
							reuse.printlnDebug("Requested file \"" + reuse.request.requestedFilePath + "\" is not restricted/ip address is exempt!");
						}
						//clientInfo = new ClientInfo(s, info, request);
						try {
							//FIXME Make the response object here reflect the actual HTTPStatusCode when returned! This will require reworking serveFileToClient(and by extension, handleAdministrateFile).
							//FIXME Done, but now it needs to be tested.
							//FIXME Done AGAIN, and should be once and for all this time. Still needs testing though, lmao
							reuse.setStatusCode(serveFileToClient(s, reuse, https, s.getOutputStream(), domainDirectory, info));
						} catch(Error | RuntimeException e) {
							exception = e;
							//connectedClients.remove(clientInfo);
						} catch(Throwable e) {
							exception = e;
							//connectedClients.remove(clientInfo);
						}
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					} else if(testDomain == null && !domainIsLocalHost) {
						ResponseUtil.send421Response(s, reuse, domainDirectory);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
					}
					ResponseUtil.send404Response(s, reuse.request, reuse.response, domainDirectory, pageHeader);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					/*out.println("HTTP/1.0 200");//XXX Working example; keep this here for reference and debugging!
					out.println("Vary: Accept-Encoding");
					out.println("Content-Type: text/html; charset=UTF-8");
					out.println("Server: " + SERVER_NAME_HEADER);
					out.println("Date: " + getCurrentCacheTime());
					out.println("Cache-Control: " + cachePrivateMustRevalidate);
					String response = "<!DOCTYPE html>\r\n<html>\r\n<head>\r\n<title>My Web Server</title>\r\n</head>\r\n<body>\r\n<h1>Welcome to my Web Server!</h1>\r\n\tYour client sent the following data: <pre>" + reuse.request.toString(true) + "</pre>\r\n\t</body>\r\n</html>";
					out.println("Content-Length: " + response.length());
					out.println("");
					out.println(response);
					out.flush();
					out.close();
					s.close();
					return;*/
				} else if(reuse.request.method.equalsIgnoreCase("POST")) {
					if(!reuse.request.requestedFilePath.startsWith("/")) {
						reuse.response.setStatusCode(HTTP_400);
						reuse.response.setStatusMessage("Client sent invalid protocol request: Path not prefixed by '/'!");
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
						reuse.response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>400 - Bad Request - " + domainDirectory.getServerName() + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
								+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
								+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 400 - Bad Request</h1><hr>\r\n"//
								+ "\t\t<string>Your client sent the following invalid HTTP request:</string><br>\r\n\t\t<pre>" + reuse.request.protocolLine + "</pre><br>\r\n\t\t<string>Request paths <b>must</b> be prefixed by a forward-slash(the '/' character).</string>\r\n"//
								+ "\t</body>\r\n</html>");
						reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
					}
					@SuppressWarnings("unused")
					final String pathPrefix = domainDirectory.getURLPathPrefix();
					//println("rootDir.getAbsolutePath(): \"" + rootDir.getAbsolutePath() + File.separatorChar + "\"");
					//println("pathPrefix: \"" + pathPrefix + "\"");
					String path = FileInfo.getURLPathFor(domainDirectory, requestedFile);
					final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
					
					if((requestedFile == null || !requestedFile.exists())) {
						reuse.response.setStatusCode(HTTP_404);
						reuse.response.setHeader("Vary", "Accept-Encoding");
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", "no-cache, must-revalidate");
						reuse.response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>404 - File not Found!!!11 - " + domainDirectory.getServerName() + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
								+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
								+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 404 - File not found</h1><hr>\r\n"//
								+ "\t\t<string>The file \"" + reuse.request.requestedFilePath + "\" does not exist.</string><br>\r\n"//
								+ "\t\t<string>" + pageHeader + "</string>\r\n"//
								+ "\t</body>\r\n</html>");
						reuse.response.sendToClient(s, true);
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					}
					if(reuse.request.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						if(requestedFile.getName().toLowerCase().endsWith(".php")) {
							if(PhpResult.isPhpFilePresent()) {
								reuse.response.setStatusMessage("PHP file: POST request");
								reuse.response.setStatusCode(reuse.connectionSeemsMalicious ? HTTP_429 : HTTP_200);
								String requestArgumentsStr = reuse.request.requestArgumentsStr.replace("?", "").replace("&", " ");
								requestArgumentsStr += (requestArgumentsStr.endsWith(" ") ? "" : " ") + (reuse.request.getFormURLEncodedData().postRequestStr.startsWith("?") ? reuse.request.getFormURLEncodedData().postRequestStr.substring(1) : reuse.request.getFormURLEncodedData().postRequestStr).replace("&", " ");
								PhpResult.execPHP(requestedFile.getAbsolutePath(), requestArgumentsStr, reuse.request.host, reuse, reuse.request, reuse.response);
								/*PhpResult phpResponse = PhpResult.getLastPHPExecResponse().copyToServerResponse(response, false, false);//response, true, false);
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
								reuse.response.setResponse(phpResponse.body);
								reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
								//connectedClients.remove(clientInfo);
								phpResponse.close();*/
								reuse.response.clearHeaders();
								PhpResult phpResponse = PhpResult.getLastPHPExecResponse().copyToServerResponse(reuse.response, false, false);//response, true, false);
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
								Collection<Header> cookiesToSet = Header.collectSetCookies(phpResponse.headers);
								for(Header cookieTest : cookiesToSet) {
									cookieTest.println(LogUtils.ORIGINAL_SYSTEM_OUT);
								}
								reuse.response.sendToClient(s, !(reuse.response.getStatusCode().name().startsWith("HTTP_3")));//TODO Create a HTTPStatusCodes.shouldSendResponseBody(HTTPStatusCodes code) and use it here!
								//connectedClients.remove(clientInfo);
								phpResponse.close();
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
							}
							reuse.printErrln("\t*** Warning! Requested file is a php file, but the php setting in the\r\n\t\"" + rootDir.getAbsolutePath() + File.separatorChar + optionsFileName + "\"\r\n\tfile either refers to a non-existant file or is not defined.\r\n\tTo fix this, type phpExeFilePath= and then the complete path to the main php\r\n\texecutable file(leaving no space after the equal symbol).\r\n\tYou can download php binaries here: http://php.net/downloads.php\r\n\tIf this is not corrected, any php files requested by incoming clients will be downloaded rather than executed.");
							String check = reuse.request.requestArguments.get("download");
							if(check != null && (check.equals("1") || check.equalsIgnoreCase("true"))) {
								final String charset = StringUtils.getDetectedEncoding(requestedFile);
								reuse.response.setStatusMessage("PHP file: PHP-CGI not defined!");
								reuse.response.setStatusCode(reuse.connectionSeemsMalicious ? HTTP_429 : HTTP_200);
								reuse.response.setHeader("Content-Type", "text/html; charset=" + charset);
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile, s) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
								reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
								reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
								reuse.response.setHeader("Accept-Ranges", "none");
								reuse.response.setResponse(FileUtil.readFileData(requestedFile));
								reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
								//connectedClients.remove(clientInfo);
								//s.close();
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
							}
							reuse.response.setStatusMessage("PHP file - Service Unavailable");
							reuse.response.setStatusCode(reuse.connectionSeemsMalicious ? HTTP_429 : HTTP_503);
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", (RestrictedFile.isFileRestricted(requestedFile, s) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader("Last-Modified", StringUtils.getCacheTime(requestedFile.lastModified()));
							reuse.response.setHeader("Accept-Ranges", "none");
							reuse.response.setResponse("<!DOCTYPE html>\r\n" + //
									"<html>\r\n\t<head>\r\n" + //
									"\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n" + //
									"\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n" + //
									"\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n" + //
									"\t\t<title>503 Service Unavailable - " + requestedFile.getName() + " - " + domainDirectory.getServerName() + "</title>\r\n" + //
									"\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n" + //
									(domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "") + //
									AUTO_RESIZE_JAVASCRIPT + //
									(isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n") + //
									"\t</head>\r\n" + //
									"\t<body>\r\n" + //
									"\t\t" + pageHeader + "\r\n" + //
									"\t\t<string>The requested file is a PHP file, and this server is not currently configured to run PHP scripts.</string>\r\n" + //
									"\t</body>\r\n</html>");
							reuse.response.sendToClient(s, !reuse.request.method.equalsIgnoreCase("HEAD"));
							//connectedClients.remove(clientInfo);
							//s.close();
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						String clientResponse;
						try {
							clientResponse = new String(Base64.getDecoder().decode(reuse.request.authorization.replace("Basic", "").trim().getBytes()));
						} catch(IllegalArgumentException ignored) {
							clientResponse = "";
						}
						String[] creds = clientResponse.split(":");
						String clientUser = creds.length == 2 ? creds[0] : "";
						String clientPass = creds.length == 2 ? creds[1] : "";
						final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(reuse.request, false);//AuthorizationResult authResult = serverAdministrationAuth.authenticate(reuse.request.authorization, new String(reuse.request.postRequestData), reuse.request.protocol, reuse.request.host, clientIP, reuse.request.cookies);
						if(!authResult.passed()) {
							NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), clientUser, clientPass);
							reuse.response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "(client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
							if(authResult.authorizedCookie != null) {
								reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
							}
							reuse.response.setResponse("<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
									+ "\t</head>\r\n"//
									+ "\t<body>\r\n"//
									+ "\t\t<h1>Error 401 - Authorization Required</h1><hr>\r\n"//
									+ "\t\t<string title=\"In order to be able to POST to this server, you must be able to administrate it.\">You need permission to do that.</string>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							reuse.response.sendToClient(s, true);
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						if(authResult.authorizedCookie != null) {
							reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
						}
						String administrateFile = reuse.request.requestArguments.get("administrateFile");
						if(administrateFile != null && (administrateFile.equals("1") || administrateFile.equalsIgnoreCase("true"))) {
							final String pagePrefix = createPathLinksFor(path, domainDirectory.getDirectorySafe().getName(), reuse.request.protocol + reuse.request.host, false, true, reuse.request.requestArgumentsStr)/*(path.startsWith("/") ? path : "/" + path)*/ + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress;
							OutputStream outStream = s.getOutputStream();
							DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
							boolean administrated;
							try {
								administrated = handleAdministrateFile(s, reuse, clientAddress, reuse.request.version, reuse.request.protocol, outStream, out, domainDirectory, FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(requestedFile.getAbsolutePath() + File.separatorChar)), pagePrefix, true, new FileInfo(requestedFile, domainDirectory));
							} catch(Throwable e) {
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), e);
							}
							if(!administrated) {
								reuse.response.setStatusCode(HTTP_500).setStatusMessage("Error administrating file(s)!");
								out.println(reuse.request.version + " 500 Internal Server Error");
								out.println("Vary: Accept-Encoding");
								out.println("Server: " + SERVER_NAME_HEADER);
								out.println("Date: " + StringUtils.getCurrentCacheTime());
								out.println("Last-Modified: " + StringUtils.getCacheTime(requestedFile.lastModified()));
								out.println("Cache-Control: " + (RestrictedFile.isFileRestricted(requestedFile, s) ? cachePrivateMustRevalidate : (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge())));
								out.println("Connection: " + (reuse.allowReuse ? "keep-alive" : "close"));
								if(reuse.allowReuse) {
									out.println("Keep-Alive: timeout=" + (JavaWebServer.requestTimeout / 1000));
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
										+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
										+ "\t</head>\r\n"//
										+ "\t<body>\r\n"//
										+ "\t\t<h1>Error 500 - Error Administrating File</h1><hr>\r\n"//
										+ "\t\t<string>Something went wrong when attempting to administrate your file(s). Please try again!</string><hr>\r\n"//
										+ "\t\t<string>" + pagePrefix + "</string>\r\n"//
										+ "\t</body>\r\n</html>";
								if(reuse.request.acceptEncoding.contains("gzip") && responseStr.length() > 33 && domainDirectory.getEnableGZipCompression()) {
									out.println("Content-Encoding: gzip");
									byte[] r = StringUtils.compressString(responseStr, "UTF-8", reuse);
									out.println("Content-Length: " + r.length);
									out.println("");
									if(!reuse.request.method.equalsIgnoreCase("HEAD")) {
										outStream.write(r);
									}
									r = null;
									System.gc();
								} else {
									out.println("Content-Length: " + responseStr.length());
									out.println("");
									if(!reuse.request.method.equalsIgnoreCase("HEAD")) {
										out.println(responseStr);
									}
								}
							} else {
								//reuse.println("\t*** " + reuse.request.version + " " + reuse.response.getStatusCode().toString() + " - File administration[1]" + (reuse.response.getStatusMessage().isEmpty() ? "" : ": " + reuse.response.getStatusMessage()));//println("\t*** " + reuse.request.version + " - File administration[1]");
								reuse.response.setStatusMessage("File administration[1]" + (reuse.response.getStatusMessage().isEmpty() ? "" : ": " + reuse.response.getStatusMessage()));
							}
							out.flush();
							if(s.isClosed() || !reuse.allowReuse) {
								out.close();
							}
						} else {
							NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), clientUser, clientPass);
							ResponseUtil.send401Response(s, reuse.request, reuse.response, domainDirectory);
						}
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					} else if(reuse.request.contentType.toLowerCase().contains("multipart/form-data")) {//XXX multipart/form-data
						//HttpDigestAuthorization serverAdministrationAuth = getOrCreateAuthForCurrentThread();
						String clientResponse;
						try {
							clientResponse = new String(Base64.getDecoder().decode(reuse.request.authorization.replace("Basic", "").trim().getBytes()));
						} catch(IllegalArgumentException ignored) {
							clientResponse = "";
						}
						String[] creds = clientResponse.split(":");
						String clientUser = creds.length == 2 ? creds[0] : "";
						String clientPass = creds.length == 2 ? creds[1] : "";
						final BasicAuthorizationResult authResult = authenticateBasicForServerAdministration(reuse.request, false);//AuthorizationResult authResult = serverAdministrationAuth.authenticate(reuse.request.authorization, new String(reuse.request.postRequestData), reuse.request.protocol, reuse.request.host, clientIP, reuse.request.cookies);
						if(!authResult.passed()) {
							NaughtyClientData.logFailedAuthentication(reuse.request.getClientIPAddressNoPort(), clientUser, clientPass);
							reuse.response.setStatusCode(HTTP_401).setStatusMessage(clientResponse.isEmpty() ? "" : "(client attempted to authenticate using the following creds: \"" + (areCredentialsValidForAdministration(clientUser, clientPass) ? "[ADMINISTRATION_CREDS]" : clientResponse) + "\")");
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", cachePrivateMustRevalidate);
							reuse.response.setHeader("Date", StringUtils.getCurrentCacheTime());
							reuse.response.setHeader(authResult.resultingAuthenticationHeader.split(Pattern.quote(":"))[0].trim(), StringUtil.stringArrayToString(authResult.resultingAuthenticationHeader.split(Pattern.quote(":")), ':', 1));//"WWW-Authenticate", "Basic realm=\"" + adminAuthorizationRealm + "\"");
							if(authResult.authorizedCookie != null) {
								reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
							}
							reuse.response.setResponse("<!DOCTYPE html>\r\n"//
									+ "<html>\r\n\t<head>\r\n"//
									+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
									+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
									+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
									+ "\t\t<title>401 - Authorization Required - " + domainDirectory.getServerName() + "</title>\r\n"//
									+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
									+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
									+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
									+ "\t</head>\r\n"//
									+ "\t<body>\r\n"//
									+ "\t\t<h1>Error 401 - Authorization Required</h1><hr>\r\n"//
									+ "\t\t<string title=\"In order to be able to upload files to this server, you must be able to administrate it.\">You need permission to do that.</string>\r\n"//
									+ "\t\t<string>" + pageHeader + "</string>\r\n"//
									+ "\t</body>\r\n</html>");
							reuse.response.sendToClient(s, true);
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						if(authResult.authorizedCookie != null) {
							reuse.response.setHeader("Set-Cookie", authResult.authorizedCookie);
						}
						if(reuse.request.getMultiPartFormData() == null) {
							reuse.println("\t /!\\ Client sent multipart/form-data, but the request.multiPartFormData object was null!\r\n\t/___\\");
							reuse.response.setStatusCode(HTTP_500);
							reuse.response.setHeader("Vary", "Accept-Encoding");
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
							reuse.response.setHeader("Content-Length", "0");
							reuse.response.setResponse((String) null);
							reuse.response.sendToClient(s, false);
							System.gc();
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						if(!reuse.request.getMultiPartFormData().fileData.isEmpty()) {
							if(!domainDirectory.getEnableFileUpload()) {
								reuse.response.setStatusCode(HTTP_204);
								reuse.response.setHeader("Vary", "Accept-Encoding");
								reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
								reuse.response.setHeader("Server", SERVER_NAME_HEADER);
								reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
								reuse.response.setHeader("Content-Length", "0");
								reuse.response.setResponse((String) null);
								reuse.response.sendToClient(s, false);
								reuse.request.getMultiPartFormData().close();
								//reuse.request.multiPartFormData = null;
								System.gc();
								//connectedClients.remove(clientInfo);
								return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
							}
							if(requestedFile.isDirectory()) {
								for(FileDataUtil data : reuse.request.getMultiPartFormData().fileData) {
									try {
										data.writeFileToFolder(requestedFile);
									} catch(IOException e) {
										PrintUtil.printThrowable(e);
									}
								}
							}
							reuse.response.setStatusCode(HTTP_303);
							reuse.response.setStatusMessage("Client posted " + reuse.request.getMultiPartFormData().fileData.size() + " file" + (reuse.request.getMultiPartFormData().fileData.size() == 1 ? "" : "s") + " in the requested folder.");
							reuse.response.setHeader("Vary", "Accept-Encoding");
							reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
							reuse.response.setHeader("Server", SERVER_NAME_HEADER);
							reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
							reuse.response.setHeader("Location", /*reuse.request.protocol + domainDirectory.getDomain() + */reuse.request.requestedFilePath/**/ + "?administrateFile=1"/**/ + (reuse.request.getMultiPartFormData().fileData.size() >= 1 ? "#" + reuse.request.getMultiPartFormData().fileData.get(0).fileName : ""));
							reuse.response.setHeader("Content-Length", "0");
							reuse.response.setResponse((String) null);
							reuse.response.sendToClient(s, false);
							reuse.request.getMultiPartFormData().close();
							//reuse.request.multiPartFormData = null;
							System.gc();
							//connectedClients.remove(clientInfo);
							return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
						}
						reuse.response.setStatusCode(HTTP_204);
						reuse.response.setHeader("Vary", "Accept-Encoding");
						reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
						reuse.response.setHeader("Server", SERVER_NAME_HEADER);
						reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
						reuse.response.setHeader("Content-Length", "0");
						reuse.response.setResponse((String) null);
						reuse.response.sendToClient(s, false);
						if(HTTPClientRequest.debug) {
							int i = 0;
							for(Entry<String, String> entry : reuse.request.getMultiPartFormData().formData.entrySet()) {
								PrintUtil.printlnNow("[" + i + "]: " + entry.getKey() + " = " + entry.getValue() + ";");
								i++;
							}
						}
						reuse.request.getMultiPartFormData().close();
						//reuse.request.multiPartFormData = null;
						System.gc();
						//connectedClients.remove(clientInfo);
						return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
					}
					reuse.response.setStatusCode(HTTP_501);
					reuse.response.setStatusMessage("[0]: " + reuse.request.protocolLine);
					reuse.response.setHeader("Vary", "Accept-Encoding");
					reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
					reuse.response.setHeader("Server", SERVER_NAME_HEADER);
					reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
					reuse.response.setHeader("Content-Length", "0");
					reuse.response.setResponse((String) null);
					reuse.response.sendToClient(s, false);
					//connectedClients.remove(clientInfo);
					return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
				}
				
				String path = reuse.request.requestedFilePath;
				path = (path.trim().isEmpty() ? "/" : path);
				path = (path.startsWith("/") ? "" : "/") + path;
				final String pageHeader = "<hr><b>" + (path.startsWith("/") ? path : "/" + path) + " - " + reuse.request.host + (reuse.request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b>";
				
				reuse.response.setStatusCode(HTTP_501);
				reuse.response.setStatusMessage("[1]: " + reuse.request.protocolLine);
				reuse.response.setHeader("Vary", "Accept-Encoding");
				reuse.response.setHeader("Content-Type", "text/html; charset=UTF-8");
				reuse.response.setHeader("Server", SERVER_NAME_HEADER);
				reuse.response.setHeader("Cache-Control", (requestedFile.isDirectory() ? "no-cache, must-revalidate" : "public, max-age=" + domainDirectory.getCacheMaxAge()));
				reuse.response.setResponse("<!DOCTYPE html>\r\n"//
						+ "<html>\r\n\t<head>\r\n"//
						+ "\t\t" + HTML_HEADER_META_VIEWPORT + "\r\n"//
						+ "\t\t" + HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
						+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
						+ "\t\t<title>501 - Feature not Implemented - " + domainDirectory.getServerName() + "</title>\r\n"//
						+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
						+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
						+ (isFileRestrictedOrHidden ? "" : "\t\t" + domainDirectory.getPageHeaderContent() + "\r\n")//
						+ "\t</head>\r\n"//
						+ "\t<body>\r\n"//
						+ "\t\t<h1>Error 501 - Not Implemented</h1><hr>\r\n"//
						+ "\t\t<string>The requested HTML method(\"" + reuse.request.method + "\") has not yet been implemented.</string>\r\n"//
						+ "\t\t<string>" + pageHeader + "</string>\r\n"//
						+ "\t</body>\r\n</html>");
				reuse.response.sendToClient(s, true);
				//connectedClients.remove(clientInfo);
				return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), reuse.allowReuse), exception);
			}
			reuse.response.setStatusCode(HTTP_400).setStatusMessage("Bad protocol request: \"" + reuse.request.protocolLine + "\"").setResponse((String) null).sendToClient(s, false);
			//connectedClients.remove(clientInfo);
			if(!domainDirectory.getDisplayLogEntries()) {
				reuse.clearAllLogsBeforeDisplay();
			}
			return new RequestResult(reuse.completeResponse(reuse.response.getStatusCode(), false), exception);
		} catch(IOException e) {
			exception = e;
			reuse.printErrln("\t /!\\ \tFailed to respond to client request: \"" + getClientAddress + "\"\r\n\t/___\\\tCause: " + e.getClass().getName() + ": " + e.getMessage());
		}
		if(domainDirectory != null && !domainDirectory.getDisplayLogEntries()) {
			reuse.clearAllLogsBeforeDisplay();
		}
		return new RequestResult(reuse.completeResponse(HTTP_500, false), exception);
	}
	
	/** @param e The IOException to check
	 * @return True if it can be ignored */
	public static final boolean isExceptionIgnored(Throwable e) {
		if(e == null) {
			return true;
		}
		if(e.getMessage() != null) {
			final String errMsg = e.getMessage();
			if(errMsg != null && e instanceof CancelledRequestException) {
				return !HTTPClientRequest.debug;
			}
			if(errMsg != null && e instanceof TimeoutException) {
				return !HTTPClientRequest.debug;
			}
			if(errMsg != null && (errMsg.equalsIgnoreCase("Socket Closed")/* || errMsg.equalsIgnoreCase("Client sent no data.")*/ || errMsg.equalsIgnoreCase("Software caused connection abort: recv failed") || errMsg.equalsIgnoreCase("Software caused connection abort: socket write error") || errMsg.equalsIgnoreCase("Connection reset by peer: socket write error"))) {
				return true;
			}
		}
		if(e.getCause() != null) {
			return isExceptionIgnored(e.getCause());
		}
		return false;
	}
	
}
