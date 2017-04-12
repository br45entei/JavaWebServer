package com.gmail.br45entei.server;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_204;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_400;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_413;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_500;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_501;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_598;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.data.serverIO.InputStreamSSLWrapper;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FormURLEncodedData;
import com.gmail.br45entei.server.data.MultipartFormData;
import com.gmail.br45entei.server.logging.RequestLog;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.ResponseUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;
import com.gmail.br45entei.util.exception.CancelledRequestException;
import com.gmail.br45entei.util.exception.TimeoutException;

import java.io.ByteArrayOutputStream;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public class HTTPClientRequest {
	private volatile Throwable encounteredException = null;
	
	public static volatile boolean debug = false;
	
	public final long creationTime;
	private volatile String result = "";
	public volatile DomainDirectory domainDirectory = null;
	
	private volatile Socket client;
	protected final ClientConnection reuse;
	private final String clientIpAddress;
	private final String clientIpAddressNoPort;
	private volatile InputStream in;
	private volatile boolean isFinished = false;
	private volatile boolean isCancelled = false;
	private volatile boolean isPostRequest = false;
	//private final ClientRequestStatus status;
	
	private volatile boolean isCompleted = false;
	
	//public volatile String requestLogs = "";
	
	public volatile String protocolLine = "";
	
	public volatile String method = "";
	public volatile String requestedFilePath = "";
	public volatile String requestedServerAddress = "";
	public volatile String version = "";
	
	public volatile String upgrade = "";
	
	public final ConcurrentLinkedDeque<Header> headers = new ConcurrentLinkedDeque<>();
	public final ArrayList<String> cookies = new ArrayList<>();
	
	public volatile boolean isProxyRequest = false;
	
	public volatile String protocol = "";
	public volatile String host = "";
	
	public volatile String upgradeInsecureRequests = "";
	
	public volatile String hostNoPort = "";
	public volatile String http2Host = "";
	public volatile String connectionSetting = "";
	public volatile String cacheControl = "";
	public volatile String accept = "";
	public volatile String userAgent = "";
	public volatile String dnt = "";
	public volatile String refererLink = "";
	public volatile String acceptEncoding = "";
	public volatile String acceptLanguage = "";
	public volatile String from = "";
	public volatile String range = "";
	public volatile String authorization = "";
	public volatile String ifModifiedSince = "";
	public volatile String ifNoneMatch = "";
	public volatile String ifRange = "";
	
	public volatile String icy_MetaData = "";
	
	public volatile String xForwardedFor = "";
	public volatile String via = "";
	public volatile ForwardedHeaderData forwardedHeader = new ForwardedHeaderData("");
	public volatile String proxyConnection = "";
	public volatile String proxyAuthorization = "";
	//POST
	public volatile String contentLength = "";
	public volatile String contentType = "";
	private volatile FormURLEncodedData formURLEncodedData;
	private volatile MultipartFormData multiPartFormData;
	public volatile String origin = "";
	//public String							postRequestStr			= "";
	public volatile byte[] postRequestData = new byte[0];
	
	public volatile String requestArgumentsStr = "";
	public final HashMap<String, String> requestArguments = new HashMap<>();
	
	private volatile Thread originalThread = null;
	
	public static final String getRequestedPathLink(String protocol, String host, String requestedFilePath, boolean removeDoubleSlashes) {
		boolean hasFile = FilenameUtils.getExtension(requestedFilePath).isEmpty();
		if(removeDoubleSlashes) {
			while(requestedFilePath.endsWith("//")) {
				requestedFilePath = requestedFilePath.endsWith("//") ? requestedFilePath.substring(0, requestedFilePath.length() - 1) : requestedFilePath;
			}
		}
		if(!hasFile && !requestedFilePath.endsWith("/")) {
			requestedFilePath += "/";
		}
		if(!requestedFilePath.startsWith("/")) {
			requestedFilePath = "/" + requestedFilePath;
		}
		return protocol + host + requestedFilePath;
	}
	
	public static final String getRequestedPathParentLink(String protocol, String host, String requestedFilePath, boolean removeDoubleSlashes) {
		return protocol + host + getRequestedPathParent(requestedFilePath, removeDoubleSlashes);
	}
	
	public static final String getRequestedPathParent(String requestedFilePath, boolean removeDoubleSlashes) {
		boolean hasFile = !FilenameUtils.getExtension(requestedFilePath).isEmpty();
		if(removeDoubleSlashes) {
			while(requestedFilePath.endsWith("//")) {
				requestedFilePath = requestedFilePath.endsWith("//") ? requestedFilePath.substring(0, requestedFilePath.length() - 1) : requestedFilePath;
			}
		}
		if(!hasFile && !requestedFilePath.endsWith("/")) {
			requestedFilePath += "/";
		}
		if(hasFile) {
			String fileName = FilenameUtils.getName(requestedFilePath);
			requestedFilePath = requestedFilePath.replace(fileName, "");
		}
		if(!requestedFilePath.startsWith("/")) {
			requestedFilePath = "/" + requestedFilePath;
		}
		return requestedFilePath;
	}
	
	public final String getRequestedPathLink(String protocol, boolean removeDoubleSlashes) {
		String requestedFilePath = this.requestedFilePath;
		if(removeDoubleSlashes) {
			while(requestedFilePath.endsWith("//")) {
				requestedFilePath = requestedFilePath.endsWith("//") ? requestedFilePath.substring(0, requestedFilePath.length() - 1) : requestedFilePath;
			}
		}
		if(!requestedFilePath.endsWith("/")) {
			requestedFilePath += "/";
		}
		return protocol + this.host + requestedFilePath;
	}
	
	public final String getRequestedPathLink(boolean removeDoubleSlashes) {
		return this.getRequestedPathLink(this.protocol, removeDoubleSlashes);
	}
	
	public final boolean isFinished() {
		return this.isFinished || this.clientRequestAccepted;
	}
	
	/*protected final void println(String str) {
		if(this.originalThread == null) {
			this.requestLogs += StringUtils.replaceOnce(PrintUtil.getResultingPrintln(str), "_RequestThread", "");
		} else {
			this.reuse.println(str, this.originalThread);//PrintUtil.println(str, this.originalThread);
		}
	}
	
	protected final void printlnDebug(String str) {
		if(debug) {
			println(str);
		}
	}*/
	
	//public final HashMap<String, String>	postRequestArguments	= new HashMap<>();
	
	private static final ArrayList<Header> readRequestHeader(InputStream in, ClientStatus status) throws IOException, SSLHandshakeException {
		ArrayList<Header> headers = new ArrayList<>();
		String line;
		while((line = readLine(in, status)) != null) {
			if(line.contains(":")) {
				headers.add(new Header(line));
			}
			if(line.isEmpty()) {
				headers.add(Header.BLANK_LINE);
				return headers;
			}
		}
		headers.add(Header.EOS_REACHED);
		return headers;
	}
	
	private static final String readLine(InputStream in, ClientStatus status) throws IOException, SSLHandshakeException {
		if(in == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int read;
		boolean readBytes = false;
		long count = 0;
		while((read = in.read()) != -1) {//SSLHandshakeException happens here... ??? weird.
			count++;
			long origCount = status.getCount();
			status.incrementCount();
			status.markReadTime();
			byte[] r0 = new byte[] {(byte) read};
			String s = new String(r0);
			if(s.equals("\n")) {
				if(readBytes) {
					break;
				}
				return "";
			}
			readBytes = true;
			addToDebugFile(r0, origCount < read);
			baos.write(read);
			//if line.length() > 16038 break; ? any way to do this efficiently?
		}
		if(baos.size() == 0 && read == -1) {
			if(debug) {
				if(read == -1) {
					System.err.println("count: " + StringUtil.longToString(count) + ";");
					int check = in.read();
					System.err.println("wtf: " + Integer.toString(check) + "(" + new String(new byte[] {(byte) check}, StandardCharsets.UTF_8) + ")");
				}
			}
			return readBytes ? "" : null;
		} else if(baos.size() == 0 && read != -1) {
			if(debug) {
				System.err.println("count: " + StringUtil.longToString(count) + "; read != -1! read: " + Integer.toString(read) + "(" + new String(new byte[] {(byte) read}, StandardCharsets.UTF_8) + ")");
			}
			@SuppressWarnings("resource")
			InputStreamSSLWrapper wrappedIn = new InputStreamSSLWrapper(in);
			return readLine(wrappedIn.addByteToInternalBuffer(read), status);
		}
		String rtrn = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		rtrn = rtrn.endsWith("\r") ? rtrn.substring(0, rtrn.length() - 1) : rtrn;
		//System.out.println("Read line: \"" + rtrn + "\";");
		return rtrn;
	}
	
	protected static final class ReqHolder {
		private volatile HTTPClientRequest request = null;
		public volatile Throwable e0;
		public volatile IOException e1;
		public volatile NumberFormatException e2;
		public volatile UnsupportedEncodingException e3;
		public volatile OutOfMemoryError e4;
		public volatile CancellationException e5;
		
		public final void setValue(HTTPClientRequest request) {
			this.request = request;
		}
		
		public final HTTPClientRequest getValue() {
			return this.request;
		}
		
	}
	
	private volatile boolean clientRequestAccepted = false;
	
	public final boolean clientRequestAccepted() {
		return this.clientRequestAccepted;
	}
	
	public final RequestResult acceptClientRequestSafe(long timeout) {
		RequestResult result = null;
		try {
			try {
				this.acceptClientRequest(timeout);
				this.reuse.setStatusCode(HTTP_501);
				if(this.protocolLine == null || this.protocolLine.trim().isEmpty()) {
					throw new IOException("Client sent no data.");
				}
				this.reuse.response = new HTTPServerResponse(this.reuse, this.version, HTTP_501, this.acceptEncoding.toLowerCase().contains("gzip"), StandardCharsets.UTF_8);
				this.reuse.status.setIncoming(false);
			} catch(NumberFormatException e) {
				this.encounteredException = e;
				this.reuse.response = new HTTPServerResponse(this.reuse, "HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("No Content-Length Header defined.").setResponse((String) null).sendToClient(this.client, false);
				result = new RequestResult(this.reuse.completeResponse(this.reuse.response.getStatusCode(), false), e);
				throw e;
			} catch(UnsupportedEncodingException e) {
				this.encounteredException = e;
				this.reuse.response = new HTTPServerResponse(this.reuse, "HTTP/1.1", HTTP_400, false, StandardCharsets.UTF_8).setStatusMessage("Unsupported Url Encoding").setResponse((String) null).sendToClient(this.client, false);
				result = new RequestResult(this.reuse.completeResponse(this.reuse.response.getStatusCode(), false), e);
				throw e;
			} catch(OutOfMemoryError e) {
				this.encounteredException = e;
				this.reuse.response = new HTTPServerResponse(this.reuse, "HTTP/1.1", HTTP_413, false, StandardCharsets.UTF_8).setStatusMessage("Request data too large").setResponse((String) null).sendToClient(this.client, false);
				result = new RequestResult(this.reuse.completeResponse(this.reuse.response.getStatusCode(), false), e);
				throw e;
			} catch(CancellationException e) {
				this.encounteredException = e;
				this.reuse.response = new HTTPServerResponse(this.reuse, "HTTP/1.1", HTTP_204, false, StandardCharsets.UTF_8).setStatusMessage(e.getMessage()).setResponse((String) null).sendToClient(this.client, false);
				result = new RequestResult(this.reuse.completeResponse(this.reuse.response.getStatusCode(), false), e);
				throw e;
			} catch(TimeoutException e) {
				if(this.encounteredException != null) {
					return new RequestResult(this.reuse, this.encounteredException);
				}
				this.encounteredException = e;
				if(this.clientRequestAccepted || this.isFinished) {
					for(int i = 0; i < 20; i++) {
						LogUtils.ORIGINAL_SYSTEM_OUT.println("Debug_4ca36fgTR31: Deja vu?! HTTPClientRequest.java");
					}
					new Throwable("Debug_4ca36fgTR31: Deja vu?! HTTPClientRequest.java").printStackTrace();
					return result;//Strangely enough, this seems to happen...
				}
				this.reuse.response = ResponseUtil.send408Response(this.client, this.reuse, this.getClientIPAddress());
				result = new RequestResult(this.reuse.completeResponse(this.reuse.response.getStatusCode(), false), e);
				throw e;
			} catch(IOException ex) {
				Exception e = ex;
				this.encounteredException = e;
				if(e.getMessage() != null) {
					if(e.getMessage().equalsIgnoreCase("Client sent no data.")) {
						e = new TimeoutException("Client sent no data.", ex);
						this.cancel();
						if(this.reuse.response != null) {
							this.reuse.response.setStatusCode(HTTP_598);
						}
						return new RequestResult(this.reuse.completeResponse(HTTP_598, false), e);
					}
					if(e.getMessage().equals("Socket closed")) {
						e = new CancelledRequestException("Socket closed");
					}
					result = new RequestResult(this.reuse.completeResponse(HTTP_598, false), e);
				}
				if(!debug) {
					LogUtils.ORIGINAL_SYSTEM_ERR.print(StringUtil.throwableToStr(e, "\n"));
				}
				throw e;
			}
		} catch(Throwable e) {
			this.reuse.status.setIncoming(false);
			if(result != null && e == result.exception) {//wtf wtf wtf wtf howcanthishappen wtf ........ if this if statement were not here there would be a "Self suppression not permitted" error below. wtffffff
				return result;
			}
			this.encounteredException = e;
			this.reuse.printErrln("\t /!\\ \tFailed to respond to client request: \"" + this.getClientIPAddress() + "\"\r\n\t/___\\\tCause: " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
			if(result != null && result.exception != null) {
				e.addSuppressed(result.exception);
			}
			try {
				ResponseUtil.send500InternalServerError(this.client, this.reuse, this.domainDirectory, e, true);
			} catch(Throwable e1) {
				e1.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
			}
			result = new RequestResult(this.reuse.completeResponse(HTTP_500, false), e);
		}
		this.reuse.status.setIncoming(false);
		return result;
	}
	
	private final void acceptClientRequest(long timeout) throws IOException, NumberFormatException, TimeoutException, OutOfMemoryError, CancellationException, Throwable {
		this.reuse.status.setIncoming(true);
		final Thread originalThread = Thread.currentThread();
		this.clientRequestAccepted = false;
		long startTime = System.currentTimeMillis();
		final ReqHolder req = new ReqHolder();
		req.setValue(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HTTPClientRequest.this.parseRequest(HTTPClientRequest.this.reuse, originalThread);
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
				} catch(Throwable e) {
					req.e0 = e;
				}
			}
		}, Thread.currentThread().getName() + "_RequestThread").start();
		while(!this.reuse.status.isCancelled()) {
			if((System.currentTimeMillis() - startTime) > timeout) {
				if(!this.isPostRequest && (System.currentTimeMillis() - this.reuse.status.getLastReadTime()) >= timeout) {
					if(this.multiPartFormData != null) {
						this.multiPartFormData.close();
					}
					this.multiPartFormData = null;
					this.reuse.println("\t /!\\\t Warning:\n\t/___\\\t Client request just timed out!");
					break;
				}
			}
			Functions.sleep(10L);
			this.reuse.status.setIncoming(true);
			this.reuse.status.setProxyRequest(false);
			if(this.isFinished()) {
				this.originalThread = null;
				return;
			}
			if(req.e0 != null) {
				throw req.e0;
			} else if(req.e1 != null) {
				throw req.e1;
			} else if(req.e2 != null) {
				throw req.e2;
			} else if(req.e3 != null) {
				throw req.e3;
			} else if(req.e4 != null) {
				throw req.e4;
			} else if(req.e5 != null) {
				throw req.e5;
			}
		}
		this.originalThread = null;
		if(this.reuse.status.isCancelled()) {
			this.isCancelled = true;
			this.reuse.status.setIncoming(false);
			throw new CancellationException("Request was cancelled.");
		}
		if(!this.isFinished()) {
			this.isCancelled = true;
			this.reuse.status.setIncoming(false);
			throw new TimeoutException("Request from client took longer than \"" + (timeout / 1000L) + "\" seconds.");
		}
		return;
	}
	
	public HTTPClientRequest(final Socket s, final InputStream in, final boolean https, final ClientConnection reuse) {
		this.client = s;
		this.creationTime = System.currentTimeMillis();
		this.clientIpAddress = AddressUtil.getClientAddress(s);
		this.clientIpAddressNoPort = AddressUtil.getClientAddressNoPort(this.clientIpAddress);
		this.in = in;
		this.protocol = https ? "https://" : "http://";
		this.reuse = reuse;
		this.reuse.request = this;
		this.reuse.status.setIncoming(true);
		this.reuse.status.setStatus("Waiting for " + (this.reuse.isReused() ? "next " : "") + "client request...");
	}
	
	public final String getClientIPAddress() {
		return this.clientIpAddress;
	}
	
	public final String getClientIPAddressNoPort() {
		return this.clientIpAddressNoPort;
	}
	
	private static final File debugFile = new File(JavaWebServer.rootDir, "request_Debug.txt");
	private static volatile FileOutputStream debFos = null;
	
	public static final void addToDebugFile(byte[] bytes, boolean start) {
		addToDebugFile(bytes, 0, bytes.length, start);
	}
	
	public static final void addToDebugFile(byte[] bytes, int offset, int length, boolean start) {
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
				if(start) {
					debFos.write("\r\n".getBytes(StandardCharsets.UTF_8));
				}
				debFos.write(bytes, offset, length);
				debFos.flush();
			} catch(Throwable ignored) {
			}
		}
	}
	
	private final void parseProtocolLine(final String protocolLine) throws IOException {
		this.protocolLine = protocolLine;
		String[] split = this.protocolLine.split("\\s");
		if(split.length == 3) {
			this.method = split[0];
			this.requestedFilePath = split[1];
			this.version = split[2];
			if(this.version == null || this.version.isEmpty() || !(this.version.startsWith("HTTP/") || this.version.startsWith("HTCPCP/"))) {
				try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(this.reuse.status.getClient().getOutputStream(), StandardCharsets.UTF_8), true)) {
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
			this.reuse.println("\t--- Client request: " + this.protocolLine, this.originalThread);
			
			if(this.requestedFilePath.startsWith("http://") || this.requestedFilePath.startsWith("https://")) {
				final int startIndex = this.requestedFilePath.startsWith("http://") ? 7 : 8;
				this.protocol = (startIndex == 7 ? "http://" : "https://");
				
				int endIndex = this.requestedFilePath.replace("//", "--").indexOf("/");
				endIndex = endIndex == -1 ? this.requestedFilePath.length() : endIndex;
				
				this.http2Host = this.requestedFilePath.substring(startIndex, endIndex);
				this.host = this.http2Host;
				this.hostNoPort = AddressUtil.getClientAddressNoPort(this.host);//this.host.contains(":") ? this.host.substring(0, this.host.lastIndexOf(":")) : this.host;
				if(this.host.endsWith(":80") && this.host.length() > 3) {
					this.host = this.host.substring(0, this.host.length() - ":80".length());
				}
				this.reuse.println("Before: " + this.requestedFilePath, this.originalThread);
				this.requestedFilePath = this.requestedFilePath.substring(endIndex);
				this.reuse.println("After: " + this.requestedFilePath, this.originalThread);
				this.isProxyRequest = (!this.http2Host.isEmpty() ? DomainDirectory.getDomainDirectoryFromDomainName(this.http2Host) == null : false) || AddressUtil.getPortFromAddress(this.requestedServerAddress) == 443;
				if(this.isProxyRequest) {
					this.requestedServerAddress = this.http2Host;
					this.reuse.printlnDebug("requestedServerAddress: " + this.requestedServerAddress, this.originalThread);
				}
			} else if(this.method.equalsIgnoreCase("CONNECT")) {
				this.requestedServerAddress = split[1];
				this.protocol = (this.requestedFilePath.startsWith("http://") ? "http://" : "https://");
				this.isProxyRequest = true;
			}
			
			if(this.requestedFilePath.contains("?") && this.requestedFilePath.length() >= 2) {
				this.reuse.status.setStatus("Parsing client request arguments...");
				String check = this.requestedFilePath.substring(0, this.requestedFilePath.indexOf("?"));
				this.requestArgumentsStr = this.requestedFilePath.substring(this.requestedFilePath.indexOf("?"), this.requestedFilePath.length());
				final String original = this.requestArgumentsStr;
				try {
					this.requestArgumentsStr = StringUtils.decodeHTML(original);
				} catch(Throwable ignored) {
					this.requestArgumentsStr = original;
				}
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
							if(arg.endsWith("=") || !arg.contains("=")) {
								this.requestArguments.put(entry[0], "");
							} else if(arg.startsWith("=")) {
								this.requestArguments.put("", entry[0]);
							}
						} else if(entry.length == 1) {
							this.requestArguments.put(entry[0], "");
						}
					}
				}
				
			}
			if(!this.requestArguments.isEmpty()) {
				this.reuse.println("\t\tRequest Arguments: \"" + this.requestArgumentsStr + "\"", this.originalThread);
			} else {
				if(!this.requestArgumentsStr.trim().isEmpty()) {
					this.reuse.println("\t\tRequest Arguments: \"" + this.requestArgumentsStr.trim() + "\"", this.originalThread);
				}
			}
		} else {
			this.reuse.println("\t--- Bad Client request: \"" + this.protocolLine + "\"", this.originalThread);
		}
	}
	
	protected final void markReadBytes(long contentLength, long count, long readAmount) {
		this.reuse.status.setContentLength(contentLength);
		this.reuse.status.setCount(count);
		this.reuse.status.setLastReadAmount(readAmount);
	}
	
	protected final void parseRequest(ClientConnection reuse, Thread originalThread) throws IOException, NumberFormatException, OutOfMemoryError, CancellationException, SSLHandshakeException {
		this.originalThread = originalThread;
		if(this.client.isClosed() || this.client.isInputShutdown() || this.client.isOutputShutdown()) {
			throw new IOException("The socket was closed locally BEFORE we could even read data from the client!");
		}
		final String protocolLine = readLine(this.in, this.reuse.status);
		if(protocolLine == null) {
			this.isFinished = true;
			throw new IOException("Client closed connection.");
		}
		reuse.printlnDebug("Read protocol line:\"" + protocolLine + "\"");
		this.parseProtocolLine(protocolLine);
		if(debug) {
			LogUtils.ORIGINAL_SYSTEM_OUT.println(protocolLine);
		}
		ArrayList<Header> headers = readRequestHeader(this.in, this.reuse.status);
		if(headers.isEmpty()) {
			this.isFinished = true;
			throw new IOException("Client sent no headers!");
		}
		if(Header.collectionContainsHeader(headers, Header.EOS_REACHED)) {
			this.isFinished = true;
			throw new IOException("Client input stream EOS reached while reading headers!");
		}
		long headerContentLength = -1;
		for(Header header : headers) {
			String line = header.toString();//TODO Remove this line and use header.header and header.value as appropriate below!
			if(debug && header != Header.BLANK_LINE && header != Header.EOS_REACHED) {
				header.println(LogUtils.ORIGINAL_SYSTEM_OUT);
			}
			/*if(this.reuse.status == null) {
				this.isFinished = true;
				throw new CancellationException("Request status is null! How did that happen?");
			}*/
			this.reuse.status.setStatus("Receiving client request...");
			this.reuse.status.markReadTime();
			if(this.reuse.status.isCancelled()) {
				break;
			}
			if(!Header.isBlank(header)) {
				if(header.header.equalsIgnoreCase("Cookie")) {
					this.reuse.printlnDebug("Cookie received: " + header.value, this.originalThread);
					for(String cookie : header.value.split(Pattern.quote(";"))) {
						this.cookies.add(cookie.trim());
						this.reuse.printlnDebug("Cookie sub data: " + cookie, this.originalThread);
					}
				} else {
					this.headers.add(header);
				}
				if(line.toLowerCase().startsWith("upgrade: ")) {
					this.upgrade = line.substring("Upgrade: ".length());
				} else if(line.toLowerCase().startsWith("host: ")) {
					this.host = line.substring("host: ".length());
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
				} else if(line.toLowerCase().startsWith("upgrade-insecure-requests: ")) {
					this.upgradeInsecureRequests = line.substring("Upgrade-Insecure-Requests: ".length());
				} else if(line.toLowerCase().startsWith("connection: ")) {
					this.connectionSetting = line.substring("Connection: ".length());
				} else if(line.toLowerCase().startsWith("cache-control: ")) {
					this.cacheControl = line.substring("Cache-Control: ".length());
				} else if(line.toLowerCase().startsWith("pragma: ") && line.toLowerCase().endsWith("no-cache")) {
					this.cacheControl = "no-cache";
				} else if(line.toLowerCase().startsWith("accept: ")) {
					this.accept = line.substring("Accept: ".length());
				} else if(line.toLowerCase().startsWith("user-agent: ")) {
					this.userAgent = line.substring("User-Agent: ".length());
					this.reuse.println("\t\tUser Agent: " + this.userAgent, this.originalThread);
				} else if(line.toLowerCase().startsWith("dnt: ")) {
					this.dnt = line.substring("DNT: ".length());
				} else if(line.toLowerCase().startsWith("referer: ")) {
					this.refererLink = line.substring("Referer: ".length());
				} else if(line.toLowerCase().startsWith("accept-encoding: ")) {
					this.acceptEncoding = line.substring("Accept-Encoding: ".length());
				} else if(line.toLowerCase().startsWith("accept-language: ")) {
					this.acceptLanguage = line.substring("Accept-Language: ".length());
				} else if(line.toLowerCase().startsWith("from: ")) {
					this.from = line.substring("From: ".length());
				} else if(line.toLowerCase().startsWith("cookie: ")) {//Do nothing
				} else if(line.toLowerCase().startsWith("range: ")) {
					this.range = line.substring("Range: ".length());
				} else if(line.toLowerCase().startsWith("authorization: ")) {
					this.authorization = line.substring("Authorization: ".length());
				} else if(line.toLowerCase().startsWith("if-modified-since: ")) {
					this.ifModifiedSince = line.substring("If-Modified-Since: ".length());
				} else if(line.toLowerCase().startsWith("if-none-match: ")) {
					this.ifNoneMatch = line.substring("If-None-Match: ".length());
				} else if(line.toLowerCase().startsWith("if-range: ")) {
					this.ifRange = line.substring("If-Range: ".length());
				} else if(line.toLowerCase().startsWith("icy-metadata: ")) {
					this.icy_MetaData = line.substring("Icy-MetaData: ".length());
				} else if(line.toLowerCase().startsWith("client-ip: ")) {
					this.xForwardedFor = line.substring("Client-IP: ".length());
				} else if(line.toLowerCase().startsWith("x-forwarded-for: ")) {
					this.forwardedHeader = new ForwardedHeaderData(line.substring("X-Forwarded-For: ".length()));
					this.xForwardedFor = this.forwardedHeader.clientIP;
					if(this.via == null || this.via.trim().isEmpty()) {
						this.via = this.forwardedHeader.proxyIP;
					}
					Header via = Header.getHeaderFrom(headers, "via");
					if(via != null && !this.headers.contains(via)) {
						this.headers.add(via);
					} else if(!Header.collectionContainsHeader(this.headers, "Via")) {
						this.headers.add(new Header("Via", this.via));
					}
					Header xForwardedFor = Header.getHeaderFrom(headers, "X-Forwarded-For");
					if(xForwardedFor != null) {
						this.headers.add(xForwardedFor);
					} else if(!Header.collectionContainsHeader(this.headers, "X-Forwarded-For")) {
						this.headers.add(new Header("X-Forwarded-For", this.xForwardedFor));
					}
				} else if(line.toLowerCase().startsWith("via: ")) {
					if(this.via == null || this.via.trim().isEmpty()) {
						this.via = line.substring("via: ".length());
					}
				} else if(line.toLowerCase().startsWith("forwarded: ")) {
					this.forwardedHeader = new ForwardedHeaderData(line.substring("Forwarded: ".length()));
					this.xForwardedFor = this.forwardedHeader.clientIP;
					if(this.via == null || this.via.trim().isEmpty()) {
						this.via = this.forwardedHeader.proxyIP;
					}
					Header via = Header.getHeaderFrom(headers, "via");//StringUtils.getStringInList(this.headers, "via");
					if(via != null && !this.headers.contains(via)) {
						this.headers.add(via);
					} else if(!Header.collectionContainsHeader(this.headers, "Via")) {
						this.headers.add(new Header("Via", this.via));
					}
					Header xForwardedFor = Header.getHeaderFrom(headers, "X-Forwarded-For");
					if(xForwardedFor != null) {
						this.headers.add(xForwardedFor);
					} else if(!Header.collectionContainsHeader(this.headers, "X-Forwarded-For")) {
						this.headers.add(new Header("X-Forwarded-For", this.xForwardedFor));
					}
				} else if(line.toLowerCase().startsWith("proxy-connection: ")) {
					this.proxyConnection = line.substring("Proxy-Connection: ".length());
				} else if(line.toLowerCase().startsWith("proxy-authorization: ")) {
					this.proxyAuthorization = line.substring("Proxy-Authorization: ".length());
				} else if(line.toLowerCase().startsWith("content-length: ")) {
					this.contentLength = line.substring("Content-Length: ".length());
					if(debug) {
						LogUtils.ORIGINAL_SYSTEM_ERR.println("content length header: \"" + line + "\"; contentLength header: " + this.contentLength);
					}
					if(StringUtil.isStrLong(this.contentLength)) {
						headerContentLength = Long.parseLong(this.contentLength);
					}
				} else if(line.toLowerCase().startsWith("content-type: ")) {
					this.contentType = line.substring("Content-Type: ".length());
				} else if(line.toLowerCase().startsWith("origin: ")) {
					this.origin = line.substring("Origin: ".length());
				} else if(!line.isEmpty()) {
					//if(debug) {
					this.reuse.println("\t /!\\ Unimplemented header: \"" + line + "\"\r\n\t/___\\", this.originalThread);
					//}
				}
			} else {//XXX line empty!
				if(debug) {
					LogUtils.ORIGINAL_SYSTEM_OUT.println();
				}
				if(this.isProxyRequest && !JavaWebServer.isProxyServerEnabled()) {
					this.formURLEncodedData = new FormURLEncodedData("");
					this.multiPartFormData = null;
					JavaWebServer.printlnDebug("Test 1");
					this.postRequestData = new byte[0];
					break;
				}
				if(this.method.equalsIgnoreCase("POST")) {
					this.isPostRequest = true;
				}
				//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_0");
				try(DisposableByteArrayOutputStream data = new DisposableByteArrayOutputStream()) {
					//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_1");
					Long getContentLength = Header.getContentLengthFrom(headers);
					if(getContentLength == null || getContentLength.longValue() == -1L) {
						getContentLength = Long.valueOf(headerContentLength);
						//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_2");
					}
					final long contentLength = getContentLength == null ? -1L : getContentLength.longValue();
					if(debug) {
						LogUtils.ORIGINAL_SYSTEM_ERR.println("Content-Length header at POST read time: " + getContentLength);
					}
					//LogUtils.ORIGINAL_SYSTEM_ERR.println("Resulting content-length: " + StringUtil.longToString(contentLength));
					if(contentLength > 0) {
						//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_3");
						byte[] buf = new byte[4096];
						long count = 0;
						long remaining = contentLength - count;
						int read = 0;
						this.markReadBytes(contentLength, count, read);
						while((read = this.in.read(buf, 0, Long.valueOf(remaining < buf.length ? remaining : buf.length).intValue())) >= 0) {
							//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_4");
							this.reuse.status.markReadTime();
							count += read;
							remaining = contentLength - count;
							this.markReadBytes(contentLength, count, read);
							this.reuse.status.checkForPause();
							data.write(buf, 0, read);
							this.reuse.status.markWriteTime();
							if(remaining <= 0 || this.isCancelled) {
								//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_5");
								break;
							}
							this.reuse.status.checkForPause();
						}
						reuse.printlnDebug("[0]last read: " + read);
						buf = new byte[0];
						//System.gc();
					} else {
						//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_6");
						long readWaitStartTime = System.currentTimeMillis();
						boolean timedOut = false;
						if(this.in.available() == 0) {
							while(this.in.available() == 0) {
								Functions.sleep();
								if(System.currentTimeMillis() - readWaitStartTime >= 250L) {//JavaWebServer.requestTimeout) {
									//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_8");
									timedOut = true;
									break;
								}
							}
						}
						this.markReadBytes(-1L, 0L, 0L);
						if(!timedOut) {
							//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_9");
							byte[] buf = new byte[4096];
							long count = 0;
							int read;
							timedOut = false;
							readWaitStartTime = System.currentTimeMillis();
							while((read = this.in.read(buf, 0, buf.length)) >= 0) {
								//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_10");
								count += read;
								this.markReadBytes(-1L, count, read);
								this.reuse.status.checkForPause();
								data.write(buf, 0, read);
								this.reuse.status.markWriteTime();
								if(this.isCancelled) {
									//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_11");
									break;
								}
								this.reuse.status.checkForPause();
								if(this.in.available() == 0) {
									//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_12");
									while(this.in.available() == 0) {
										Functions.sleep();
										if(System.currentTimeMillis() - readWaitStartTime >= JavaWebServer.requestTimeout) {
											//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_13");
											timedOut = true;
											break;
										}
									}
									if(timedOut) {
										//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_14");
										break;
									}
								}
							}
							reuse.printlnDebug("[1]last read: " + read);
							if(read >= 0) {
								LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_15");
								read = this.in.read(buf, 0, buf.length);
								data.write(buf, 0, read);
							}
							reuse.printlnDebug("[1_1]last read: " + read);
							buf = new byte[0];
							//System.gc();
						} else {
							//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_16");
							this.markReadBytes(0L, 0L, 0L);
						}
					}
					//LogUtils.ORIGINAL_SYSTEM_ERR.println("POST_17: " + data.size());
					this.postRequestData = data.toByteArray();
					if(debug) {
						LogUtils.ORIGINAL_SYSTEM_ERR.println("Post request data length: " + Integer.toString(this.postRequestData.length));
						if(this.postRequestData.length > 16384) {
							byte[] limitedData = new byte[16384];
							System.arraycopy(this.postRequestData, 0, limitedData, 0, limitedData.length);
							LogUtils.ORIGINAL_SYSTEM_ERR.write(limitedData);
						} else {
							LogUtils.ORIGINAL_SYSTEM_ERR.write(this.postRequestData);
						}
						LogUtils.ORIGINAL_SYSTEM_ERR.flush();
					}
					if(this.contentType.toLowerCase().contains("multipart/form-data")) {
						this.reuse.status.setStatus("Parsing client multipart/form-data...");
						try {
							reuse.printlnDebug("Test 0: MultipartFormData");
							this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.reuse.status, this);
							reuse.printlnDebug("Test 1");
						} catch(IllegalArgumentException e) {
							e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
							if(debug) {
								reuse.printThrowable(e);
							}
							this.multiPartFormData = null;
							reuse.printlnDebug("Test 2");
						}
						if(this.multiPartFormData == null) {
							reuse.printlnDebug("Test 3: Failure");
						} else {
							reuse.printlnDebug("Test 4: Pass!");
						}
					} else {
						reuse.printlnDebug("Test 5");
					}
					if(this.contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
						reuse.printlnDebug("Test 6: FormURLEncodedData");
						this.reuse.status.setStatus("Parsing client POST data...");
						this.formURLEncodedData = new FormURLEncodedData(this.postRequestData);
						/*if(!this.formURLEncodedData.postRequestArguments.isEmpty() && this.requestedFilePath.contains(".php")) {
							if(this.requestArguments.isEmpty()) {
								this.requestArgumentsStr = this.formURLEncodedData.postRequestStr;
							} else {
								this.requestArgumentsStr = this.requestArgumentsStr + (this.requestArgumentsStr.endsWith("&") ? "" : "&") + this.formURLEncodedData.postRequestStr;
							}
						}
						LogUtils.ORIGINAL_SYSTEM_OUT.println("new request arguments str: " + this.requestArgumentsStr);*/
					} else {
						this.formURLEncodedData = new FormURLEncodedData("");
					}
				}
				/*
				if(this.method.equalsIgnoreCase("POST") && !this.contentLength.isEmpty()) {
					JavaWebServer.printlnDebug("Test: Method was POST!");
					this.reuse.status.setStatus("Receiving client POST data...");
					this.isPostRequest = true;
					final int contentLength = Integer.parseInt(this.contentLength);//final long contentLength = new Long(this.contentLength).longValue();
					this.reuse.status.setContentLength(contentLength);
					this.reuse.status.setCount(0);
					this.reuse.status.setLastReadAmount(1);
					/*this.postRequestStr = readLine(in, new Long(this.contentLength).longValue());*/
				/*
					//If the majority of browsers fix the issue where they don't display data sent from servers while they are uploading files, then I could put a check here to see if the client is uploading multipart/form-data and then check if they are authenticated. For now, bandwidth will have to be wasted and the client will complete the file upload only to find out that they require authorization...
					//this.postRequestData = new byte[contentLength];
					//final int mtu = 2048;
					byte[] buf = new byte[contentLength];//byte[] buf = new byte[mtu];
					int count = 0;//long count = 0;
					int read;
					
					int remaining = contentLength - count;//long remaining = contentLength - count;
					this.reuse.status.setStatus("Receiving client POST data: [0] / " + contentLength + "(" + Functions.humanReadableByteCount(remaining, true, 2) + " remaining);");
					try(DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream()) {
						JavaWebServer.printlnDebug("Test: POST - RECEIVE DATA");
						this.reuse.status.markDataTransferStartTime();
						this.reuse.status.setCancellable(false);
						while((read = this.in.read(buf, 0, Math.min(remaining, buf.length))) != -1 && !this.reuse.status.isCancelled()) {
							count += read;
							remaining = contentLength - count;
							this.reuse.status.setStatus("Receiving client POST data: " + count + " / " + contentLength + "(" + Functions.humanReadableByteCount(remaining, true, 2) + " remaining);");
							this.reuse.status.markReadTime();
							this.reuse.status.setCount(count);
							this.reuse.status.setLastReadAmount(read);
							this.reuse.status.checkForPause();
							baos.write(buf, 0, read);
							if(remaining == 0) {
								break;
							}
							*/
				/*
							if(remaining < mtu) {
								buf = new byte[(int) remaining];
								read = this.in.read(buf, 0, buf.length);
								while(remaining > 0) {
									count += read;
									remaining = contentLength - count;
									this.reuse.status.markReadTime();
									this.reuse.status.setCount(count);
									this.reuse.status.setLastReadAmount(read);
									this.reuse.status.checkForPause();
									baos.write(buf, 0, read);
									if(remaining <= 0) {
										break;
									}
									read = this.in.read(buf, 0, 1);
								}
								break;
							}*/
				/*
						}
						this.postRequestData = baos.getBytesAndDispose();
						addToDebugFile(this.postRequestData);
						LogUtils.ORIGINAL_SYSTEM_OUT.write(this.postRequestData);
						LogUtils.ORIGINAL_SYSTEM_OUT.flush();
						this.reuse.status.setCancellable(true);
					} catch(Throwable e) {
						PrintUtil.printlnNow("Failed to read client request data: " + StringUtil.throwableToStr(e, "\n"));
						this.postRequestData = new byte[0];
						System.gc();
						throw e;
					}
					
					*/
				/*
					for(int j = 0; j < this.postRequestData.length; j++) {
						CodeUtil.sleep(8L);
						checkForPause();
						if(this.reuse.status.isCancelled() || this.isCancelled) {
							this.postRequestData = new byte[0];
							break;
						}
						this.postRequestData[j] = (byte) this.in.read();
						this.reuse.status.markReadTime();
						this.reuse.status.incrementCount();
					}*/
				/*
					if(this.reuse.status.isCancelled() || this.isCancelled) {
						this.formURLEncodedData = new FormURLEncodedData("");
						if(this.multiPartFormData != null) {
							this.multiPartFormData.close();
						}
						this.multiPartFormData = null;
						JavaWebServer.printlnDebug("Test 2");
						this.postRequestData = new byte[0];
						System.gc();
						this.isFinished = true;
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
							this.reuse.status.setStatus("Parsing client multipart/form-data...");
							try {
								this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.reuse.status, this);
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
						this.reuse.status.setStatus("Parsing client POST data...");
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
				if(!this.isProxyRequest) {//this kills off data to be sent to proxy connections using the GET protocol verb!
					this.postRequestData = new byte[0];
					System.gc();
				}*/
				break;
			}
		}
		//String println = new String(this.postRequestData, StandardCharsets.UTF_8);
		//LogUtils.ORIGINAL_SYSTEM_ERR.println(println.substring(0, Math.min(16384, println.length())));
		/*if(this.formURLEncodedData == null) {
			this.formURLEncodedData = new FormURLEncodedData("");
		}
		if(this.method != null && this.method.equals("POST")) {
			if(this.contentType != null && this.contentType.contains("multipart/form-data")) {
				if(this.multiPartFormData == null) {
					if(this.postRequestData != null && this.postRequestData.length > 0) {
						this.multiPartFormData = new MultipartFormData(this.postRequestData, this.contentType, this.reuse.status, this);
					} else {
						System.out.println("Test: POST - Client sent POST data; but the data is null! Wtf? isProxyRequest: " + this.isProxyRequest);
						System.out.println("contentLength header: " + this.contentLength);
						System.out.println("contentType header: " + this.contentType);
						System.out.println("request body data length: " + (this.postRequestData != null ? this.postRequestData.length : -1));
					}
				}
			}
		}*/
		if(this.forwardedHeader != null && (this.xForwardedFor == null || this.xForwardedFor.trim().isEmpty()) && this.forwardedHeader.clientIP != null && !this.forwardedHeader.clientIP.trim().isEmpty()) {
			this.xForwardedFor = this.forwardedHeader.clientIP;
		}
		if(this.xForwardedFor == null) {
			this.xForwardedFor = (this.forwardedHeader == null || this.forwardedHeader.clientIP == null || this.forwardedHeader.clientIP.trim().isEmpty()) ? "" : this.forwardedHeader.clientIP;
		}
		if(this.forwardedHeader == null) {
			this.forwardedHeader = new ForwardedHeaderData(this.xForwardedFor == null ? "" : this.xForwardedFor);
		}
		this.reuse.status.setStatus("Preparing client request for processing...");
		if(this.formURLEncodedData == null) {
			reuse.printThrowable(new Throwable("FormURLEncodedData was null! Setting it to a blank value to prevent a potential NPE..."), true);
			this.formURLEncodedData = new FormURLEncodedData("");
		}
		this.isFinished = true;
		this.clientRequestAccepted = true;
	}
	
	/** @return the formURLEncodedData */
	public final FormURLEncodedData getFormURLEncodedData() {
		return this.formURLEncodedData;
	}
	
	/** @return the multiPartFormData */
	public final MultipartFormData getMultiPartFormData() {
		return this.multiPartFormData;
	}
	
	public final HTTPClientRequest setLogResult(String result) {
		this.result = result == null ? "" : result;
		return this;
	}
	
	public final void saveRequestLog() {
		if(this.requestedFilePath != null && (this.requestedFilePath.toLowerCase().endsWith("/favicon.ico") || this.requestedFilePath.toLowerCase().endsWith("/favicon.png") || this.requestedFilePath.toLowerCase().endsWith("/layout.css"))) {
			return;
		}
		String result = this.result == null ? "" : "\r\n" + this.result;
		if(this.domainDirectory == null || this.domainDirectory.getDisplayLogEntries()) {
			RequestLog log = new RequestLog(this.reuse).setResult(result);
			if(log.seemsComplete()) {
				log.saveToFile();
			}
		}
	}
	
	public final void markCompleted() {
		this.markCompleted(false);
	}
	
	public final void markCompleted(boolean saveLog) {
		if(this.isCompleted) {
			return;
		}
		/*result = result == null ? "" : "\r\n" + result;
		if(this.result == null || this.result.trim().isEmpty()) {
			this.result = result;
		} else {
			this.result += result;
		}*/
		if(saveLog) {
			this.saveRequestLog();
		}
		//this.result = null;
		this.domainDirectory = null;
		this.reuse.status.markCompleted();
		this.formURLEncodedData = null;
		this.multiPartFormData = null;
		this.in = null;
		this.client = null;
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
	
	private volatile String toStringStr = null;
	
	@Override
	public final String toString() {
		if(this.toStringStr != null) {
			return this.toStringStr;
		}
		String str = this.protocolLine + "\r\n";
		for(Header header : this.headers) {
			str += header.toString() + "\r\n";
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
		this.reuse.status.cancel();
	}
	
	public static final class ForwardedHeaderData {
		public final String clientIP;
		public final String protocol;
		public final String proxyIP;
		
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
						final String pname = splitEquals[0].trim();
						final String pvalue = StringUtils.stringArrayToString(splitEquals, '=', 1).trim();
						if(pname.equalsIgnoreCase("for")) {
							clientIP = pvalue;
						} else if(pname.equalsIgnoreCase("proto")) {
							protocol = pvalue;
						} else if(pname.equalsIgnoreCase("by")) {
							proxyIP = pvalue;
						}
					}
					CodeUtil.sleep(4L);
				}
			} else if(!headerValue.trim().isEmpty() && headerValue.indexOf(".") != headerValue.lastIndexOf(".")) {
				clientIP = AddressUtil.getClientAddressNoPort(headerValue.trim());
			}
			this.clientIP = clientIP;
			this.protocol = protocol;
			this.proxyIP = proxyIP;
		}
		
	}
	
}
