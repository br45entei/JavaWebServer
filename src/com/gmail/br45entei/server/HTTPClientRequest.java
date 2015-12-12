package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.BasicAuthorizationResult;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FormURLEncodedData;
import com.gmail.br45entei.server.data.MultipartFormData;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class HTTPClientRequest {
	public static boolean					debug					= false;
	
	//private final Socket					client;
	private final InputStream				in;
	private boolean							isFinished				= false;
	private boolean							isCancelled				= false;
	private boolean							isPostRequest			= false;
	public final ClientRequestStatus		status;
	
	public String							requestLogs				= "";
	
	public String							protocolRequest			= "";
	
	public String							method					= "";
	public String							requestedFilePath		= "";
	public String							requestedServerAddress	= "";
	public String							version					= "";
	
	public String							upgrade					= "";
	
	public final HashMap<String, String>	headers					= new HashMap<>();
	public final ArrayList<String>			cookies					= new ArrayList<>();
	
	public boolean							isProxyRequest			= false;
	
	public String							protocol				= "";
	public String							host					= "";
	public String							hostNoPort				= "";
	public String							http2Host				= "";
	public String							connectionSetting		= "";
	public String							cacheControl			= "";
	public String							accept					= "";
	public String							userAgent				= "";
	public String							dnt						= "";
	public String							referrerLink			= "";
	public String							acceptEncoding			= "";
	public String							acceptLanguage			= "";
	public String							from					= "";
	public String							range					= "";
	public String							authorization			= "";
	public String							ifModifiedSince			= "";
	public String							ifNoneMatch				= "";
	public String							ifRange					= "";
	public String							xForwardedFor			= "";
	public String							via						= "";
	public ForwardedHeaderData				forwardedHeader			= new ForwardedHeaderData("");
	public String							proxyConnection			= "";
	public String							proxyAuthorization		= "";
	//POST
	public String							contentLength			= "";
	public String							contentType				= "";
	public FormURLEncodedData				formURLEncodedData;
	public MultipartFormData				multiPartFormData;
	public String							origin					= "";
	//public String							postRequestStr			= "";
	public byte[]							postRequestData			= new byte[0];
	
	public String							requestArgumentsStr		= "";
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
	
	private static final String readLine(InputStream in, ClientRequestStatus status) throws IOException {
		if(in == null) {
			return null;
		}
		String line = "";
		int read;
		while((read = in.read()) != -1) {
			status.incrementCount();
			status.markReadTime();
			String r = new String(new byte[] {(byte) read});
			line += r;
			if(r.equals("\n") || read == -1) {
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
	
	private static final byte[] readBytes(InputStream in, int numOfBytes) throws IOException {
		if(in == null) {
			return null;
		}
		if(numOfBytes == 0) {
			return new byte[0];
		}
		if(numOfBytes < 0) {
			throw new IndexOutOfBoundsException("\"numOfBytes\" must not be negative: " + numOfBytes);
		}
		final byte[] bytes = new byte[numOfBytes];
		int read;
		int readAmt = 0x0;
		while((read = in.read()) != -1) {
			if(read == -1 || readAmt >= numOfBytes) {
				break;
			}
			bytes[readAmt] = Integer.valueOf(read).byteValue();
			readAmt++;
		}
		return bytes;
	}
	
	protected static final class ReqHolder {
		private HTTPClientRequest			request	= null;
		public IOException					e1;
		public NumberFormatException		e2;
		public UnsupportedEncodingException	e3;
		public OutOfMemoryError				e4;
		public CancellationException		e5;
		
		public final void setValue(HTTPClientRequest request) {
			this.request = request;
		}
		
		public final HTTPClientRequest getValue() {
			return this.request;
		}
		
	}
	
	public final void acceptClientRequest(long timeout, boolean wasReused) throws IOException, NumberFormatException, UnsupportedEncodingException, TimeoutException, OutOfMemoryError, CancellationException {
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
					req.e3 = e;
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
						this.multiPartFormData.finalize();
					}
					this.multiPartFormData = null;
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
			} else if(req.e3 != null) {
				this.status.removeFromList();
				throw req.e3;
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
		this.in = in;
		this.status = new ClientRequestStatus(s, 0);
	}
	
	protected final void parseRequest() throws IOException, NumberFormatException, UnsupportedEncodingException, OutOfMemoryError, CancellationException {
		final String clientAddress = this.status.getClientAddress();
		//final String clientIP = AddressUtil.getClientAddressNoPort(clientAddress);
		int i = 0;
		while(!this.status.isCancelled()) {
			final String line = readLine(this.in, this.status);
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
			//PrintUtil.printlnNow("Read line: \"" + line + "\"");
			if(line.isEmpty()) {
				if(this.isProxyRequest && !JavaWebServer.isProxyServerEnabled()) {
					this.formURLEncodedData = new FormURLEncodedData("");
					this.multiPartFormData = null;
					this.postRequestData = new byte[0];
					break;
				}
				if(this.method.equalsIgnoreCase("POST") && !this.contentLength.isEmpty()) {
					this.status.setStatus("Receiving client POST data...");
					this.isPostRequest = true;
					final int contentLength = new Long(this.contentLength).intValue();
					this.status.setContentLength(contentLength);
					this.status.setCount(0);
					/*this.postRequestStr = readLine(in, new Long(this.contentLength).longValue());*/
					//If the majority of browsers fix the issue where they don't display data sent from servers while they are uploading files, then I could put a check here to see if the client is uploading multipart/form-data and then check if they are authenticated. For now, bandwidth will have to be wasted and the client will complete the file upload only to find out that they require authorization...
					this.postRequestData = new byte[contentLength];
					for(int j = 0; j < this.postRequestData.length; j++) {
						if(this.status.isPaused()) {
							String lastStatus = this.status.getStatus();
							while(this.status.isPaused()) {
								this.status.setStatus("Operation paused by user.");
								if(this.status.isCancelled()) {
									break;
								}
								this.status.markReadTime();
								try {
									Thread.sleep(1L);
								} catch(Throwable ignored) {
								}
							}
							this.status.setStatus(lastStatus);
						}
						if(this.status.isCancelled() || this.isCancelled) {
							this.postRequestData = new byte[0];
							break;
						}
						this.postRequestData[j] = (byte) this.in.read();
						this.status.markReadTime();
						this.status.incrementCount();
					}
					if(this.status.isCancelled() || this.isCancelled) {
						this.formURLEncodedData = new FormURLEncodedData("");
						if(this.multiPartFormData != null) {
							this.multiPartFormData.finalize();
						}
						this.multiPartFormData = null;
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
					if(!this.isProxyRequest) {//We don't want to mess with the proxy requests' post data.
						if(this.contentType.toLowerCase().contains("multipart/form-data")) {
							this.status.setStatus("Parsing client multipart/form-data...");
							BasicAuthorizationResult authResult = JavaWebServer.authenticateBasicForServerAdministration(this, JavaWebServer.useCookieAuthentication);
							if(!authResult.passed() && JavaWebServer.useCookieAuthentication) {
								authResult = JavaWebServer.authenticateBasicForServerAdministration(this, false);
							}
							if(authResult.passed()) {//if(JavaWebServer.getOrCreateAuthForCurrentThread().authenticate(this.authorization, new String(this.postRequestData), this.protocol, this.host, clientIP, this.cookies).passed()) {
								try {
									this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.status, this);
								} catch(IllegalArgumentException e) {
									PrintUtil.printThrowable(e);//e.printStackTrace();
									this.multiPartFormData = null;
								}
							} else {
								this.multiPartFormData = null;
							}
						}
					}
					if(this.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						this.status.setStatus("Parsing client POST data...");
						this.formURLEncodedData = new FormURLEncodedData(this.postRequestData);
					}
				} else {
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
				} else if(this.protocolRequest.toUpperCase().startsWith("POST")) {
					if(line.toLowerCase().startsWith("content-length: ")) {
						this.contentLength = line.substring("Content-Length:".length()).trim();
					} else if(line.toLowerCase().startsWith("content-type: ")) {
						this.contentType = line.substring("Content-Type:".length()).trim();
					} else if(line.toLowerCase().startsWith("origin: ")) {
						this.origin = line.substring("Origin:".length()).trim();
					} else if(line.isEmpty()) {
						//this.postRequestStr += readLine(in);
					}
				} else if(!line.isEmpty()) {
					//if(debug) {
					println("\t /!\\ Unimplemented header: \"" + line + "\"\r\n\t/___\\");
					//}
				}
			}
			i++;
		}
		this.status.setStatus("Preparing client request for processing...");
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
				}
			}
			this.clientIP = clientIP;
			this.protocol = protocol;
			this.proxyIP = proxyIP;
		}
		
	}
	
}
