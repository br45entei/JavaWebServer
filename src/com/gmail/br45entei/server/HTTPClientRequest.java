package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FormURLEncodedData;
import com.gmail.br45entei.server.data.MultipartFormData;
import com.gmail.br45entei.server.logging.RequestLog;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class HTTPClientRequest {
	public static boolean					debug					= false;
	
	public final long						creationTime;
	public volatile String					result					= "";
	public volatile DomainDirectory			domainDirectory			= null;
	
	//private final Socket					client;
	private final String					clientIpAddress;
	private volatile InputStream			in;
	private volatile boolean				isFinished				= false;
	private volatile boolean				isCancelled				= false;
	private volatile boolean				isPostRequest			= false;
	private volatile ClientRequestStatus	status;
	
	private volatile boolean				isCompleted				= false;
	
	public volatile String					requestLogs				= "";
	
	public volatile String					protocolRequest			= "";
	
	public volatile String					method					= "";
	public volatile String					requestedFilePath		= "";
	public volatile String					requestedServerAddress	= "";
	public volatile String					version					= "";
	
	public volatile String					upgrade					= "";
	
	public final HashMap<String, String>	headers					= new HashMap<>();
	public final ArrayList<String>			cookies					= new ArrayList<>();
	
	public volatile boolean					isProxyRequest			= false;
	
	public volatile String					protocol				= "";
	public volatile String					host					= "";
	public volatile String					hostNoPort				= "";
	public volatile String					http2Host				= "";
	public volatile String					connectionSetting		= "";
	public volatile String					cacheControl			= "";
	public volatile String					accept					= "";
	public volatile String					userAgent				= "";
	public volatile String					dnt						= "";
	public volatile String					referrerLink			= "";
	public volatile String					acceptEncoding			= "";
	public volatile String					acceptLanguage			= "";
	public volatile String					from					= "";
	public volatile String					range					= "";
	public volatile String					authorization			= "";
	public volatile String					ifModifiedSince			= "";
	public volatile String					ifNoneMatch				= "";
	public volatile String					ifRange					= "";
	public volatile String					xForwardedFor			= "";
	public volatile String					via						= "";
	public volatile ForwardedHeaderData		forwardedHeader			= new ForwardedHeaderData("");
	public volatile String					proxyConnection			= "";
	public volatile String					proxyAuthorization		= "";
	//POST
	public volatile String					contentLength			= "";
	public volatile String					contentType				= "";
	public volatile FormURLEncodedData		formURLEncodedData;
	public volatile MultipartFormData		multiPartFormData;
	public volatile String					origin					= "";
	//public String							postRequestStr			= "";
	public volatile byte[]					postRequestData			= new byte[0];
	
	public volatile String					requestArgumentsStr		= "";
	public final HashMap<String, String>	requestArguments		= new HashMap<>();
	
	private final boolean isFinished() {
		return this.isFinished;
	}
	
	protected final void println(String str) {
		this.requestLogs += str + "\r\n";
	}
	
	protected final void printlnDebug(String str) {
		if(debug) {
			println(str);
		}
	}
	
	//public final HashMap<String, String>	postRequestArguments	= new HashMap<>();
	
	private static final String readLine(InputStream in, ClientRequestStatus status) throws IOException, SSLHandshakeException {
		if(in == null) {
			return null;
		}
		String line = "";
		int read;
		while((read = in.read()) != -1) {//SSLHandshakeException happens here... ??? weird.
			status.incrementCount();
			status.markReadTime();
			byte[] r0 = new byte[] {(byte) read};
			String r = new String(r0);
			line += r;
			addToDebugFile(r0);
			if(r.equals("\n") || read == -1 || line.length() > 16038) {
				break;
			}
		}
		if(read == -1 && line.isEmpty()) {
			return null;
		}
		line = line.trim();
		//System.out.println("Read line: \"" + line + "\";");
		return line;
	}
	
	protected static final class ReqHolder {
		private HTTPClientRequest		request	= null;
		public IOException				e1;
		public NumberFormatException	e2;
		//public UnsupportedEncodingException	e3;
		public OutOfMemoryError			e4;
		public CancellationException	e5;
		
		public final void setValue(HTTPClientRequest request) {
			this.request = request;
		}
		
		public final HTTPClientRequest getValue() {
			return this.request;
		}
		
	}
	
	public final void acceptClientRequest(long timeout, boolean wasReused) throws IOException, NumberFormatException, TimeoutException, OutOfMemoryError, CancellationException {
		long startTime = System.currentTimeMillis();
		final ReqHolder req = new ReqHolder();
		req.setValue(this);
		this.status.addToList(JavaWebServer.connectedClientRequests);
		this.status.setStatus("Waiting for " + (wasReused ? "next " : "") + "client request...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HTTPClientRequest.this.parseRequest();
				} catch(UnsupportedEncodingException e) {
					//req.e3 = e;
				} catch(IOException e) {
					req.e1 = e;
				} catch(NumberFormatException e) {
					req.e2 = e;
				} catch(OutOfMemoryError e) {
					req.e4 = e;
				} catch(CancellationException e) {
					req.e5 = e;
				}
				System.gc();
			}
		}, Thread.currentThread().getName() + "_RequestThread").start();
		while(!this.status.isCancelled()) {
			if((System.currentTimeMillis() - startTime) > timeout) {
				if(!this.isPostRequest && (System.currentTimeMillis() - this.status.getLastReadTime()) >= timeout) {
					if(this.multiPartFormData != null) {
						this.multiPartFormData.close();
					}
					this.multiPartFormData = null;
					JavaWebServer.printlnDebug("Test 0");
					System.gc();
					break;
				}
			}
			try {
				Thread.sleep(10L);
			} catch(Throwable ignored) {
			}
			if(this.isFinished()) {
				this.status.removeFromList();
				return;
			}
			if(req.e1 != null) {
				this.status.removeFromList();
				throw req.e1;
			} else if(req.e2 != null) {
				this.status.removeFromList();
				throw req.e2;
				/*} else if(req.e3 != null) {
					this.status.removeFromList();
					throw req.e3;*/
			} else if(req.e4 != null) {
				this.status.removeFromList();
				throw req.e4;
			} else if(req.e5 != null) {
				this.status.removeFromList();
				throw req.e5;
			}
		}
		if(this.status.isCancelled()) {
			this.isCancelled = true;
			this.status.removeFromList();
			System.gc();
			throw new CancellationException("Request was cancelled.");
		}
		if(!this.isFinished()) {
			this.isCancelled = true;
			this.status.removeFromList();
			System.gc();
			throw new TimeoutException("Request from client took longer than \"" + (timeout / 1000L) + "\" seconds.");
		}
		this.status.removeFromList();
		System.gc();
		return;
	}
	
	public HTTPClientRequest(Socket s, InputStream in) {
		this.creationTime = System.currentTimeMillis();
		String remSocAddr = s.getRemoteSocketAddress().toString().trim();
		this.clientIpAddress = AddressUtil.getClientAddressNoPort(remSocAddr.startsWith("/") ? remSocAddr.substring(1).trim() : remSocAddr);
		this.in = in;
		this.status = new ClientRequestStatus(s, 0);
	}
	
	public final String getClientIPAddress() {
		return this.clientIpAddress;
	}
	
	private final void checkForPause() {
		if(this.status.isPaused()) {
			String lastStatus = this.status.getStatus();
			while(this.status.isPaused()) {
				this.status.setStatus("Operation paused by user.");
				if(this.status.isCancelled()) {
					break;
				}
				CodeUtil.sleep(8L);
			}
			this.status.setStatus(lastStatus);
		}
	}
	
	private static final File					debugFile	= new File(JavaWebServer.rootDir, "request_Debug.txt");
	private static volatile FileOutputStream	debFos		= null;
	
	public static final void addToDebugFile(byte[] bytes) {
		if(debug) {
			if(!debugFile.exists()) {
				try {
					debugFile.createNewFile();
				} catch(Throwable ignored) {
					return;
				}
			}
			try {
				if(debFos == null) {
					debFos = new FileOutputStream(debugFile, true);
				}
				debFos.write(bytes);
				debFos.flush();
			} catch(Throwable ignored) {
			}
		}
	}
	
	protected final void parseRequest() throws IOException, NumberFormatException, OutOfMemoryError, CancellationException, SSLHandshakeException {
		//final String clientAddress = this.status.getClientAddress();
		//final String clientIP = AddressUtil.getClientAddressNoPort(clientAddress);
		int i = 0;
		while(!this.status.isCancelled()) {
			CodeUtil.sleep(8L);
			if(this.status == null) {
				PrintUtil.printlnNow("Fatal error: Request status object rendered null...");
				break;
			}
			final String line = readLine(this.in, this.status);
			if(this.status == null) {
				throw new CancellationException("Request status is null! How did that happen?");
			}
			this.status.setStatus("Receiving client request...");
			this.status.markReadTime();
			if(line == null) {
				if(i == 0) {
					throw new IOException("Client sent no data.");
				}
				break;
			}
			if(this.status.isCancelled()) {
				break;
			}
			JavaWebServer.printlnDebug("Read line: \"" + line + "\"");
			if(line.isEmpty()) {
				if(this.isProxyRequest && !JavaWebServer.isProxyServerEnabled()) {
					this.formURLEncodedData = new FormURLEncodedData("");
					this.multiPartFormData = null;
					JavaWebServer.printlnDebug("Test 1");
					this.postRequestData = new byte[0];
					break;
				}
				if(this.method.equalsIgnoreCase("POST") && !this.contentLength.isEmpty()) {
					JavaWebServer.printlnDebug("Test: Method was POST!");
					this.status.setStatus("Receiving client POST data...");
					this.isPostRequest = true;
					final long contentLength = new Long(this.contentLength).longValue();
					this.status.setContentLength(contentLength);
					this.status.setCount(0);
					this.status.setLastReadAmount(1);
					/*this.postRequestStr = readLine(in, new Long(this.contentLength).longValue());*/
					//If the majority of browsers fix the issue where they don't display data sent from servers while they are uploading files, then I could put a check here to see if the client is uploading multipart/form-data and then check if they are authenticated. For now, bandwidth will have to be wasted and the client will complete the file upload only to find out that they require authorization...
					//this.postRequestData = new byte[contentLength];
					final int mtu = 2048;
					byte[] buf = new byte[mtu];
					long count = 0;
					int read;
					
					long remaining = contentLength - count;
					this.status.setStatus("Receiving client POST data: [0] / " + contentLength + "(" + Functions.humanReadableByteCount(remaining, true, 2) + " remaining);");
					try(DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream()) {
						JavaWebServer.printlnDebug("Test: POST - RECEIVE DATA");
						this.status.markDataTransferStartTime();
						this.status.setCancellable(false);
						while((read = this.in.read(buf, 0, buf.length)) != -1) {
							count += read;
							remaining = contentLength - count;
							this.status.setStatus("Receiving client POST data: " + count + " / " + contentLength + "(" + Functions.humanReadableByteCount(remaining, true, 2) + " remaining);");
							this.status.markReadTime();
							this.status.setCount(count);
							this.status.setLastReadAmount(read);
							checkForPause();
							baos.write(buf, 0, read);
							if(remaining < mtu) {
								buf = new byte[(int) remaining];
								read = this.in.read(buf, 0, buf.length);
								while(remaining > 0) {
									count += read;
									remaining = contentLength - count;
									this.status.markReadTime();
									this.status.setCount(count);
									this.status.setLastReadAmount(read);
									checkForPause();
									baos.write(buf, 0, read);
									if(remaining <= 0) {
										break;
									}
									read = this.in.read(buf, 0, 1);
								}
								break;
							}
						}
						this.postRequestData = baos.getBytesAndDispose();
						addToDebugFile(this.postRequestData);
						this.status.setCancellable(true);
					} catch(IOException e) {
						PrintUtil.printlnNow("Failed to read client request data: " + StringUtil.throwableToStr(e));
						this.postRequestData = new byte[0];
						System.gc();
					}
					
					/*for(int j = 0; j < this.postRequestData.length; j++) {
						CodeUtil.sleep(8L);
						checkForPause();
						if(this.status.isCancelled() || this.isCancelled) {
							this.postRequestData = new byte[0];
							break;
						}
						this.postRequestData[j] = (byte) this.in.read();
						this.status.markReadTime();
						this.status.incrementCount();
					}*/
					if(this.status.isCancelled() || this.isCancelled) {
						this.formURLEncodedData = new FormURLEncodedData("");
						if(this.multiPartFormData != null) {
							this.multiPartFormData.close();
						}
						this.multiPartFormData = null;
						JavaWebServer.printlnDebug("Test 2");
						this.postRequestData = new byte[0];
						System.gc();
						throw new CancellationException("Request was cancelled.");
					}
					if(debug) {
						String s = new String(this.postRequestData);
						if(s.length() < 20000) {
							printlnDebug("\t\tpostRequestStr:\r\n" + s);
						} else {
							printlnDebug("\t\tpostRequestStr is too long to print here.");
						}
						File test = new File(JavaWebServer.rootDir, "test-" + StringUtils.getTime(System.currentTimeMillis(), false, true) + ".deleteme.log");
						FileOutputStream out = new FileOutputStream(test);
						out.write(this.postRequestData);
						out.flush();
						out.close();
					}
					if(!this.isProxyRequest) {//We don't want to mess with the proxy requests' post data. That would kinda be, well, snooping.
						if(this.contentType.toLowerCase().contains("multipart/form-data")) {
							this.status.setStatus("Parsing client multipart/form-data...");
							try {
								this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.status, this);
								JavaWebServer.printlnDebug("Test 3");
							} catch(IllegalArgumentException e) {
								PrintUtil.printThrowable(e);//e.printStackTrace();
								this.multiPartFormData = null;
								JavaWebServer.printlnDebug("Test 4");
							}
							if(this.multiPartFormData == null) {
								JavaWebServer.printlnDebug("Test 6: Failure");
							} else {
								JavaWebServer.printlnDebug("Test 6: Pass!");
							}
						} else {
							JavaWebServer.printlnDebug("Test 7");
						}
					} else {
						JavaWebServer.printlnDebug("Test 8");
					}
					if(this.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						this.status.setStatus("Parsing client POST data...");
						this.formURLEncodedData = new FormURLEncodedData(this.postRequestData);
					}
				} else {
					JavaWebServer.printlnDebug("Test: POST - Method was not POST!");
					DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream();
					byte[] buf = new byte[1024];
					try {
						int read;
						while((read = this.in.available()) > 0) {
							read = this.in.read(buf);
							baos.write(buf, 0, read);
						}
					} catch(Throwable ignored) {
					}
					buf = null;
					this.postRequestData = baos.toByteArray();
					baos.dispose();
					baos.close();
					baos = null;
					System.gc();
				}
				/*if(!this.isProxyRequest) {
					this.postRequestData = new byte[0];
					System.gc();
				}*/
				break;
			}
			if(i == 0 /*&& (line.toUpperCase().startsWith("OPTIONS") || line.toUpperCase().startsWith("GET") || line.toUpperCase().startsWith("HEAD") || line.toUpperCase().startsWith("POST") || line.toUpperCase().startsWith("PUT") || line.toUpperCase().startsWith("DELETE") || line.toUpperCase().startsWith("TRACE") || line.toUpperCase().startsWith("CONNECT") || line.toUpperCase().startsWith("BREW"))*/) {
				this.protocolRequest = line;
				String[] split = this.protocolRequest.split("\\s");
				if(split.length == 3) {
					this.method = split[0];
					this.requestedFilePath = split[1];
					this.version = split[2];
					if(this.version == null || this.version.isEmpty() || !(this.version.startsWith("HTTP/") || this.version.startsWith("HTCPCP/"))) {
						try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(this.status.getClient().getOutputStream(), StandardCharsets.UTF_8), true)) {
							pr.println("HTTP/1.1 400 Bad Request");
							pr.println("Connection: close");
							pr.println("");
							pr.flush();
						} catch(IOException e) {
							throw e;
						} catch(Throwable ignored) {
						}
						return;
					}
					println("\t--- Client request: " + this.protocolRequest);
					
					if(this.requestedFilePath.startsWith("http://") || this.requestedFilePath.startsWith("https://")) {
						final int startIndex = this.requestedFilePath.startsWith("http://") ? 7 : 8;
						this.protocol = (startIndex == 7 ? "http://" : "https://");
						
						int endIndex = this.requestedFilePath.replace("//", "--").indexOf("/");
						endIndex = endIndex == -1 ? this.requestedFilePath.length() : endIndex;
						
						this.http2Host = this.requestedFilePath.substring(startIndex, endIndex);
						this.host = this.http2Host;
						this.requestedFilePath = this.requestedFilePath.substring(endIndex);
						this.isProxyRequest = (!this.http2Host.isEmpty() ? DomainDirectory.getDomainDirectoryFromDomainName(this.http2Host) == null : false) || AddressUtil.getPortFromAddress(this.requestedServerAddress) == 443;
						if(this.isProxyRequest) {
							this.requestedServerAddress = this.http2Host;
							printlnDebug("requestedServerAddress: " + this.requestedServerAddress);
						}
					} else if(this.method.equalsIgnoreCase("CONNECT")) {
						this.requestedServerAddress = split[1];
						this.protocol = (this.requestedFilePath.startsWith("http://") ? "http://" : "https://");
						this.isProxyRequest = true;
					}
					
					if(this.requestedFilePath.contains("?") && this.requestedFilePath.length() >= 2) {
						this.status.setStatus("Parsing client request arguments...");
						String check = this.requestedFilePath.substring(0, this.requestedFilePath.indexOf("?"));
						this.requestArgumentsStr = this.requestedFilePath.substring(this.requestedFilePath.indexOf("?"), this.requestedFilePath.length());
						this.requestedFilePath = check;
						String[] argSplit = this.requestArgumentsStr.replaceFirst("\\?", "").split("&");
						for(String arg : argSplit) {
							String[] entry = arg.split("=");
							if(entry != null) {
								if(entry.length > 2) {
									this.requestArguments.put(entry[0], StringUtils.stringArrayToString(entry, '=', 1));
								} else if(entry.length == 2) {
									this.requestArguments.put(entry[0], entry[1]);
								} else if(entry.length == 1) {
									if(arg.endsWith("=")) {
										this.requestArguments.put(entry[0], "");
									} else if(arg.startsWith("=")) {
										this.requestArguments.put("", entry[0]);
									}
								}
							}
						}
					}
					if(!this.requestArguments.isEmpty()) {
						println("\t\tRequest Arguments: \"" + this.requestArgumentsStr + "\"");
					}
				} else {
					println("\t--- Bad Client request: \"" + this.protocolRequest + "\"");
				}
			} else {
				if(!line.isEmpty()) {
					String[] split = line.split(Pattern.quote(":"));
					if(split.length >= 2) {
						final String hName = split[0];
						final String hValue = StringUtil.stringArrayToString(split, ':', 1);
						if(hName.equalsIgnoreCase("Cookie")) {
							JavaWebServer.printlnDebug("Cookie received: " + hValue);
							for(String cookie : hValue.split(Pattern.quote(";"))) {
								this.cookies.add(cookie.trim());
								JavaWebServer.printlnDebug("Cookie sub data: " + cookie);
							}
						} else {
							this.headers.put(hName, hValue.trim());
						}
					}
				}
				if(line.toLowerCase().startsWith("upgrade: ")) {
					this.upgrade = line.substring("Upgrade:".length()).trim();
				} else if(line.toLowerCase().startsWith("host: ")) {
					this.host = line.substring("host:".length()).trim();
					this.hostNoPort = AddressUtil.getClientAddressNoPort(this.host);//this.host.contains(":") ? this.host.substring(0, this.host.lastIndexOf(":")) : this.host;
					if(this.host.endsWith(":80") && this.host.length() > 3) {
						this.host = this.host.substring(0, this.host.length() - ":80".length());
					}
					if(!this.method.equalsIgnoreCase("CONNECT") && !this.isProxyRequest) {
						if(!this.version.trim().toUpperCase().startsWith("HTTP/2.")) {
							if(!this.host.isEmpty() ? DomainDirectory.getDomainDirectoryFromDomainName(this.host) == null : false) {
								this.requestedServerAddress = this.host;
								this.isProxyRequest = JavaWebServer.isProxyServerEnabled();
							}
						}
					}
				} else if(line.toLowerCase().startsWith("connection: ")) {
					this.connectionSetting = line.substring("Connection:".length()).trim();
				} else if(line.toLowerCase().startsWith("cache-control: ")) {
					this.cacheControl = line.substring("Cache-Control:".length()).trim();
				} else if(line.toLowerCase().startsWith("pragma: ") && line.toLowerCase().endsWith("no-cache")) {
					this.cacheControl = "no-cache";
				} else if(line.toLowerCase().startsWith("accept: ")) {
					this.accept = line.substring("Accept:".length()).trim();
				} else if(line.toLowerCase().startsWith("user-agent: ")) {
					this.userAgent = line.substring("User-Agent:".length()).trim();
					println("\t\tUser Agent: " + this.userAgent);
				} else if(line.toLowerCase().startsWith("dnt: ")) {
					this.dnt = line.substring("DNT:".length()).trim();
				} else if(line.toLowerCase().startsWith("referer: ")) {
					this.referrerLink = line.substring("Referer:".length()).trim();
				} else if(line.toLowerCase().startsWith("accept-encoding: ")) {
					this.acceptEncoding = line.substring("Accept-Encoding:".length()).trim();
				} else if(line.toLowerCase().startsWith("accept-language: ")) {
					this.acceptLanguage = line.substring("Accept-Language:".length()).trim();
				} else if(line.toLowerCase().startsWith("from: ")) {
					this.from = line.substring("From:".length()).trim();
				} else if(line.toLowerCase().startsWith("cookie: ")) {//Do nothing
				} else if(line.toLowerCase().startsWith("range: ")) {
					this.range = line.substring("Range:".length()).trim();
				} else if(line.toLowerCase().startsWith("authorization: ")) {
					this.authorization = line.substring("Authorization:".length()).trim();
				} else if(line.toLowerCase().startsWith("if-modified-since: ")) {
					this.ifModifiedSince = line.substring("If-Modified-Since:".length()).trim();
				} else if(line.toLowerCase().startsWith("if-none-match: ")) {
					this.ifNoneMatch = line.substring("If-None-Match:".length()).trim();
				} else if(line.toLowerCase().startsWith("if-range: ")) {
					this.ifRange = line.substring("If-Range:".length()).trim();
				} else if(line.toLowerCase().startsWith("client-ip: ")) {
					this.xForwardedFor = line.substring("Client-IP:".length()).trim();
				} else if(line.toLowerCase().startsWith("x-forwarded-for: ")) {
					this.xForwardedFor = line.substring("X-Forwarded-For:".length()).trim();
				} else if(line.toLowerCase().startsWith("via: ")) {
					this.via = line.substring("via:".length()).trim();
				} else if(line.toLowerCase().startsWith("forwarded: ")) {
					this.forwardedHeader = new ForwardedHeaderData(line.substring("forwarded:".length()).trim());
					this.xForwardedFor = this.forwardedHeader.clientIP;
					this.via = this.forwardedHeader.proxyIP;
					
					String viaKey = StringUtils.getStringInList(this.headers.keySet(), "via");
					this.headers.put(viaKey != null ? viaKey : "via", this.via);
					String xForwardedForKey = StringUtils.getStringInList(this.headers.keySet(), "x-forwarded-for");
					this.headers.put(xForwardedForKey != null ? xForwardedForKey : "x-forwarded-for", this.xForwardedFor);
				} else if(line.toLowerCase().startsWith("proxy-connection: ")) {
					this.proxyConnection = line.substring("Proxy-Connection:".length()).trim();
				} else if(line.toLowerCase().startsWith("proxy-authorization: ")) {
					this.proxyAuthorization = line.substring("Proxy-Authorization:".length()).trim();
				} else if(line.toLowerCase().startsWith("content-length: ")) {
					this.contentLength = line.substring("Content-Length:".length()).trim();
					System.out.println("content length line: \"" + line + "\"; contentLength header: " + this.contentLength);
				} else if(line.toLowerCase().startsWith("content-type: ")) {
					this.contentType = line.substring("Content-Type:".length()).trim();
				} else if(line.toLowerCase().startsWith("origin: ")) {
					this.origin = line.substring("Origin:".length()).trim();
				} else if(!line.isEmpty()) {
					//if(debug) {
					println("\t /!\\ Unimplemented header: \"" + line + "\"\r\n\t/___\\");
					//}
				}
			}
			i++;
		}
		if(this.method != null && this.method.equals("POST")) {
			if(this.contentType != null && this.contentType.contains("multipart/form-data")) {
				if(this.multiPartFormData == null) {
					if(this.postRequestData != null && this.postRequestData.length > 0) {
						this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.status, this);
					} else {
						System.out.println("Test: POST - Client sent POST data; but the data is null! Wtf? isProxyRequest: " + this.isProxyRequest);
						System.out.println("contentLength header: " + this.contentLength);
						System.out.println("contentType header: " + this.contentType);
						System.out.println("request body data length: " + (this.postRequestData != null ? this.postRequestData.length : -1));
					}
				}
			}
		}
		if(this.status != null) {
			this.status.setStatus("Preparing client request for processing...");
		}
		if(this.formURLEncodedData == null) {
			this.formURLEncodedData = new FormURLEncodedData("");
		}
		if(!this.isProxyRequest) {
			this.postRequestData = new byte[0];
		}
		this.isFinished = true;
		if(!debug) {
			PrintUtil.clearLogs();
			PrintUtil.clearErrLogs();
		} else {
			PrintUtil.printToConsole();
			PrintUtil.printErrToConsole();
		}
	}
	
	public final ClientRequestStatus getStatus() {
		return this.status;
	}
	
	public final void markCompleted(String result) {
		if(this.isCompleted) {
			return;
		}
		if(result != null) {
			this.result += "\r\n" + result;
		}
		if(this.domainDirectory == null || this.domainDirectory.getDisplayLogEntries()) {
			RequestLog log = new RequestLog(this).setResult(this.result);
			if(log.seemsComplete()) {
				log.saveToFile();
			}
		}
		this.result = null;
		this.domainDirectory = null;
		if(this.status != null) {
			this.status.markCompleted();
			this.status = null;
		}
		this.postRequestData = null;
		this.formURLEncodedData = null;
		this.multiPartFormData = null;
		this.in = null;
		this.headers.clear();
		this.cookies.clear();
		this.requestArguments.clear();
		this.requestArgumentsStr = null;
		this.isCompleted = true;
		System.gc();
	}
	
	public final boolean isCompleted() {
		return this.isCompleted;
	}
	
	private volatile String	toStringStr	= null;
	
	@Override
	public final String toString() {
		if(this.toStringStr != null) {
			return this.toStringStr;
		}
		String str = this.protocolRequest + "\r\n";
		for(Entry<String, String> entry : this.headers.entrySet()) {
			String header = entry.getKey();
			String value = entry.getValue();
			str += header + ": " + value + "\r\n";
		}
		str += "\r\n";
		if(this.postRequestData != null && this.postRequestData.length > 0) {
			String s = new String(this.postRequestData);
			if(s.length() < 20000) {
				str += s;
			}
		}
		this.toStringStr = str;
		return this.toStringStr;
	}
	
	public final void cancel() {
		this.isCancelled = true;
		if(this.status != null) {
			this.status.cancel();
		}
	}
	
	public static final class ForwardedHeaderData {
		public final String	clientIP;
		public final String	protocol;
		public final String	proxyIP;
		
		public ForwardedHeaderData(String headerValue) {
			String clientIP = "";
			String protocol = "";
			String proxyIP = "";
			String[] splitComma = headerValue.split(Pattern.quote(";"));
			if(splitComma.length >= 1) {
				for(String property : splitComma) {
					property = property.trim();
					String[] splitEquals = property.split(Pattern.quote("="));
					if(splitEquals.length >= 2) {
						final String pname = splitEquals[0];
						final String pvalue = StringUtils.stringArrayToString(splitEquals, '=', 1);
						if(pname.trim().equalsIgnoreCase("for")) {
							clientIP = pvalue.trim();
						} else if(pname.trim().equalsIgnoreCase("proto")) {
							protocol = pvalue.trim();
						} else if(pname.trim().equalsIgnoreCase("by")) {
							proxyIP = pvalue.trim();
						}
					}
					CodeUtil.sleep(4L);
				}
			}
			this.clientIP = clientIP;
			this.protocol = protocol;
			this.proxyIP = proxyIP;
		}
		
	}
	
}
