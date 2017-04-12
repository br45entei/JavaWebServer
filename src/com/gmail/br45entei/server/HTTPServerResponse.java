package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

/** @author Brian_Entei */
public class HTTPServerResponse {
	
	protected final ClientConnection reuse;
	
	protected volatile String httpVersion;
	protected volatile HTTPStatusCodes statusCode = HTTPStatusCodes.HTTP_NOT_SET;
	protected final ConcurrentLinkedDeque<Header> headers = new ConcurrentLinkedDeque<>();
	protected volatile String response = "";
	protected volatile FileInfo responseFile = null;
	protected volatile byte[] responseData = null;
	
	protected volatile DomainDirectory domainDirectory = null;
	
	protected volatile boolean useGZip;
	protected volatile Charset charset;
	
	public volatile long contentLength = 0L;
	
	//===
	
	protected volatile long startBytes = 0L;
	protected volatile long endBytes = -1L;
	
	//===
	
	protected volatile String statusMessage = "";
	
	public HTTPServerResponse(final ClientConnection reuse, String httpVersion, HTTPStatusCodes statusCode, boolean useGZip, Charset charset) {
		this.reuse = reuse;
		this.setHTTPVersion(httpVersion).setStatusCode(statusCode).setUseGZip(useGZip).setCharset(charset);
		this.setHeader("Connection", reuse.allowReuse ? "keep-alive" : "close");
		if(reuse.allowReuse) {
			this.setHeader("Keep-Alive", "timeout=" + (JavaWebServer.requestTimeout / 1000));
		}
	}
	
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	public final String toString(boolean printResponse) {
		String response = this.response;
		response = response == null ? (this.responseData != null && this.responseData.length > 0 ? new String(this.responseData, StandardCharsets.UTF_8) : response) : response;
		if(response == null) {
			if(printResponse && this.responseFile != null) {
				printResponse = this.responseFile.mimeType.toLowerCase().startsWith("text/");
			}
			if(printResponse) {
				try {
					response = (this.responseFile == null ? "Response: [null]" : (FileUtil.isFileAccessible(new File(this.responseFile.filePath)) && FileUtil.getSize(new File(this.responseFile.filePath)) > 0 ? new String(FileUtil.readFileData(new File(this.responseFile.filePath)), StandardCharsets.UTF_8) : "Response: [Un-readable file]"));
				} catch(Throwable ignored) {
					response = "Response: [Un-readable file]";
				}
			} else {
				response = (this.responseFile == null ? "Response: [null]" : "Response: [" + this.responseFile.filePath + "]");
			}
		}
		return this.getLogHTTPLine() + "\r\n" + Header.headersToString(this.headers) + ("\r\n" + (response.length() > 50 && !printResponse ? "[Response length(bytes): " + (response.getBytes(StandardCharsets.UTF_8).length) + "(" + Functions.humanReadableByteCount(response.getBytes(getCharset()).length, true, 2) + ")]" : response));
	}
	
	public final String getLogHTTPLine() {
		return "\t*** " + this.getHTTPVersion() + " " + this.getStatusCode() + (this.statusMessage.isEmpty() ? "" : ": " + this.statusMessage);
	}
	
	public final String getHTTPVersion() {
		return this.httpVersion;
	}
	
	public final HTTPServerResponse setHTTPVersion(String httpVersion) {
		this.httpVersion = httpVersion;
		return this;
	}
	
	public final HTTPStatusCodes getStatusCode() {
		return this.statusCode;
	}
	
	public final HTTPServerResponse setStatusCode(HTTPStatusCodes statusCode) {
		if(statusCode == null || statusCode == HTTPStatusCodes.HTTP_NOT_SET) {
			new Throwable().printStackTrace(System.out);
		}
		this.statusCode = statusCode == null ? HTTPStatusCodes.HTTP_NOT_SET : statusCode;
		return this;
	}
	
	public final ArrayList<Header> getHeaders() {
		return new ArrayList<>(this.headers);
	}
	
	public final ConcurrentLinkedDeque<Header> getHeadersRaw() {
		return this.headers;
	}
	
	protected final String getHeaderKey(String header) {
		for(Header h : this.headers) {
			if(header.equalsIgnoreCase(h.header)) {
				return h.header;
			}
		}
		return header;
	}
	
	public final HTTPServerResponse setHeader(String header, String value) {
		return this.setHeader(new Header(header, value));
	}
	
	public final HTTPServerResponse setHeader(Header header) {
		if(header == null) {
			throw new IllegalArgumentException("Header cannot be null!");
		}
		/*if(value == null || value.isEmpty()) {
			this.headers.remove(header);
			return this;
		}
		this.headers.put(this.getHeaderKey(header.replace(": ", "")), value);
		if(header.equalsIgnoreCase("connection") && value.equalsIgnoreCase("close")) {
			this.headers.remove(this.getHeaderKey("Keep-Alive"));
		}
		return this;*/
		if(!this.headers.contains(header)) {
			this.headers.add(header);
		}
		return this;
	}
	
	public final Header getHeader(String header) {
		return Header.getHeaderFrom(this.headers, this.getHeaderKey(header.replace(": ", "")));
	}
	
	public final String getHeaderValue(String header) {
		return Header.getHeaderStrFrom(this.headers, this.getHeaderKey(header.replace(": ", "")));//this.headers.get(this.getHeaderKey(header.replace(": ", "")));
	}
	
	public final HTTPServerResponse clearHeaders() {
		this.headers.clear();
		return this;
	}
	
	public final byte[] getResponseData() {
		return this.responseData;
	}
	
	public final String getResponse() {
		return this.response;
	}
	
	public final FileInfo getResponseFile() {
		return this.responseFile;
	}
	
	public final HTTPServerResponse setResponse(byte[] data) {
		this.responseData = data;
		this.response = null;
		this.responseFile = null;
		this.useGZip = false;//this.setHeader(new Header("Content-Length", Integer.toString(data.length)));
		return this;
	}
	
	public final HTTPServerResponse setResponse(String response) {
		this.responseData = null;
		this.response = response;
		this.responseFile = null;
		return this;
	}
	
	public final HTTPServerResponse setResponse(FileInfo file) {
		this.responseData = null;
		this.response = null;
		this.responseFile = file;
		return this;
	}
	
	public final HTTPServerResponse appendResponse(String str) {
		this.response += str;
		return this;
	}
	
	public final DomainDirectory getDomainDirectory() {
		return this.domainDirectory;
	}
	
	public final HTTPServerResponse setDomainDirectory(DomainDirectory domainDirectory) {
		this.domainDirectory = domainDirectory;
		return this;
	}
	
	public final HTTPServerResponse setUseGZip(boolean useGZip) {
		this.useGZip = useGZip;
		return this;
	}
	
	public final Charset getCharset() {
		return this.charset == null ? StandardCharsets.UTF_8 : this.charset;
	}
	
	public final HTTPServerResponse setCharset(Charset charset) {
		if(charset != null) {
			this.charset = charset;
		}
		return this;
	}
	
	public final String getStatusMessage() {
		return this.statusMessage;
	}
	
	public final HTTPServerResponse setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		return this;
	}
	
	public final long getStartBytes() {
		return this.startBytes;
	}
	
	public final HTTPServerResponse setStartBytes(long startBytes) {
		this.startBytes = startBytes;
		return this;
	}
	
	public final long getEndBytes() {
		return this.endBytes;
	}
	
	public final HTTPServerResponse setEndBytes(long endBytes) {
		this.endBytes = endBytes;
		return this;
	}
	
	protected final boolean isRangeResponse() {
		return this.responseFile != null && this.endBytes > this.startBytes && this.startBytes >= 0;
	}
	
	public final long getContentLength() {
		if(this.isRangeResponse()) {
			return (this.endBytes - this.startBytes) + 1;
		}
		return this.responseFile != null ? Long.valueOf(this.responseFile.contentLength).longValue() : this.response.length();
	}
	
	/*public final HTTPServerResponse printStatus() {
		this.reuse.println("\t*** " + this.httpVersion + " " + this.statusCode + (this.statusMessage != null && this.statusMessage.isEmpty() ? "" : ": " + this.statusMessage));
		return this;
	}*/
	
	@SuppressWarnings("resource")
	public static final DualPrintWriter wrapSocketOutputStream(Socket s, boolean autoFlush) throws IOException {
		return new DualPrintWriter(new UnlockedOutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), autoFlush);
	}
	
	public static final void sendToClient(Socket s, final String httpVersion, HTTPStatusCodes code, Collection<Header> headers, byte[] data, boolean sendResponse) throws IOException {
		sendToClient(wrapSocketOutputStream(s, false), httpVersion, code, headers, data, sendResponse);
	}
	
	public static final void sendToClient(DualPrintWriter pr, final String httpVersion, HTTPStatusCodes code, Collection<Header> headers, byte[] data, boolean sendResponse) throws IOException {
		if(pr == null) {
			return;
		}
		if(HTTPClientRequest.debug) {
			pr.setSecondaryOut(LogUtils.ORIGINAL_SYSTEM_OUT);
		}
		pr.setLineSeparator("\r\n");
		try {
			pr.println(httpVersion + " " + code.toString());
			pr.flush();
			for(Header header : headers) {
				if(header.header.equalsIgnoreCase("Content-Length")) {
					continue;
				}
				header.println(pr);
			}
			new Header("Content-Length", Integer.toString(data.length)).println(pr);
			pr.println("");
			pr.flush();
			if(sendResponse) {
				pr.getOutputStream().write(data, 0, data.length);
				pr.getOutputStream().flush();
			}
		} catch(Throwable e) {
			//final boolean isIgnored = JavaWebServer.isExceptionIgnored(e);
			//if(!isIgnored) {
			PrintUtil.printErr("\t /!\\ \tAn exception occurred while sending the HTTP response to the client:\r\n\t/___\\\t" + StringUtil.throwableToStr(e));
			throw e;
			//}
		}
	}
	
	public final HTTPServerResponse sendToClient(Socket s, boolean sendResponse) throws IOException {
		return this.sendToClient(wrapSocketOutputStream(s, false), sendResponse);
	}
	
	private final void sendHeadersTo(DualPrintWriter pr, boolean sendResponse) {
		pr.setLineSeparator("\r\n");
		for(Header header : this.headers) {
			if(sendResponse) {
				if(!header.header.equalsIgnoreCase("Content-Length") && !header.header.equalsIgnoreCase("Content-Range") && (this.useGZip ? !header.header.equalsIgnoreCase("Content-Encoding") : true)) {
					header.println(pr);//pr.println(entry.getKey() + ": " + entry.getValue());
				}
			} else {
				header.println(pr);//pr.println(entry.getKey() + ": " + entry.getValue());
			}
		}
		pr.flush();
		this.markWriteTime();
	}
	
	public final HTTPServerResponse sendToClient(DualPrintWriter pr, boolean sendResponse) throws IOException {
		if(this.reuse.request != null) {
			this.reuse.request.setLogResult(this.toString(true));//this.request.result = this.toString();
		}
		if(pr == null) {
			return this;
		}
		if(HTTPClientRequest.debug) {
			pr.setTertiaryOut(LogUtils.ORIGINAL_SYSTEM_OUT);
			//pr.setSecondaryOut(System.out);
		}
		//boolean sentAlready = false;
		try {
			pr.setLineSeparator("\r\n");
			if(this.isRangeResponse()) {
				this.setStatusCode(HTTPStatusCodes.HTTP_206);
			}
			pr.println(this.httpVersion + " " + this.statusCode);
			pr.flush();
			//sentAlready = true;
			this.markWriteTime();
			this.sendHeadersTo(pr, sendResponse);
			if(this.response == null && this.responseFile == null) {
				if(this.responseData != null) {
					if((this.domainDirectory == null ? true : this.domainDirectory.getEnableGZipCompression()) && this.useGZip) {
						pr.println("Content-Encoding: gzip");
						byte[] r = StringUtils.compressBytes(this.responseData);
						pr.println("Content-Length: " + r.length);
						this.reuse.status.setContentLength(r.length);
						pr.println("");
						pr.flush();
						this.markWriteTime();
						if(sendResponse) {
							for(int i = 0; i < r.length; i++) {
								pr.getOutputStream().write(r[i]);
								this.markWriteTime();
							}
							pr.getOutputStream().flush();
						}
					} else {
						pr.println("Content-Length: " + this.responseData.length);
						pr.println("");
						pr.flush();
						this.markWriteTime();
						if(sendResponse) {
							for(int i = 0; i < this.responseData.length; i++) {
								pr.getOutputStream().write(this.responseData[i]);
								this.markWriteTime();
							}
							pr.getOutputStream().flush();
						}
					}
				} else {
					pr.println("");
					pr.flush();
				}
				this.markWriteTime();
			} else {
				if(this.responseFile == null) {
					if((this.domainDirectory == null ? true : this.domainDirectory.getEnableGZipCompression()) && this.useGZip && this.response.length() > 33) {
						pr.println("Content-Encoding: gzip");
						byte[] r = StringUtils.compressString(this.response, this.charset.name(), this.reuse);
						pr.println("Content-Length: " + r.length);
						pr.println("");
						pr.flush();
						this.markWriteTime();
						if(sendResponse) {
							//outStream.write(r);
							for(int i = 0; i < r.length; i++) {
								pr.getOutputStream().write(r[i]);
								this.markWriteTime();
							}
						}
					} else {
						pr.println("Content-Length: " + this.response.length());
						pr.println("");
						pr.flush();
						this.markWriteTime();
						if(sendResponse) {
							pr.println(this.response);
						}
						pr.flush();
						this.markWriteTime();
					}
					pr.flush();
					//pr.close();
					this.markWriteTime();
				} else {
					File responseFile = new File(this.responseFile.filePath);
					if(responseFile.exists() && responseFile.isFile()) {
						if(this.isRangeResponse()) {
							pr.println("Content-Length: " + this.getContentLength());
							pr.println("Content-Range: bytes " + this.startBytes + "-" + this.endBytes + "/" + this.responseFile.contentLength);
						} else {
							pr.println("Content-Length: " + this.responseFile.contentLength);
						}
						pr.println("");
						pr.flush();
						this.markWriteTime();
						if(sendResponse) {
							InputStream fileIn = responseFile.toURI().toURL().openConnection().getInputStream();
							IOException exception = null;
							try {
								if(this.isRangeResponse()) {
									RangeRequest range = new RangeRequest(this.startBytes, this.endBytes, Long.valueOf(this.responseFile.contentLength).longValue());
									range.sendRangeTo(fileIn, pr.getOutputStream(), this.reuse.status);//JavaWebServer.copyInputStreamToOutputStream(this.responseFile, fileIn, outStream, this.startBytes, this.endBytes, this.clientInfo);
								} else {
									FileInfo.copyInputStreamToOutputStream(fileIn, pr.getOutputStream(), (this.domainDirectory == null ? 1024 : this.domainDirectory.getNetworkMTU()), this.reuse.status);
								}
							} catch(IOException e) {
								exception = e;
							}
							try {
								fileIn.close();
							} catch(Throwable ignored) {
							}
							if(exception != null) {
								throw exception;
							}
						}
					} else {
						pr.println("Content-Length: 0");
						pr.println("");
						pr.flush();
						this.markWriteTime();
					}
				}
			}
			pr.flush();
			pr.getOutputStream().flush();
			//this.printStatus();
			if(pr.checkError()) {
				PrintUtil.printErrlnNow("\t /!\\ Potential error detected while sending HTTP response to client!\r\n\t/___\\");
			}
			//outStream.close();
			//pr.close();
			//s.close();
		} catch(Throwable e) {
			final boolean isIgnored = JavaWebServer.isExceptionIgnored(e);
			if(!isIgnored) {
				PrintUtil.printErr("\t /!\\ \tAn exception occurred while sending the HTTP response to the client:\r\n\t/___\\\t" + StringUtil.throwableToStr(e));
			}
			//if(!sentAlready) {
			this.notifyClientOfError500(pr, e);
			//}
			if(!isIgnored) {
				throw e;
			}
		}
		
		return this;
	}
	
	@SuppressWarnings("resource")
	public final long sendRangeTo(InputStream input, OutputStream output, ClientStatus status, long contentLength, boolean sendResponse) throws IOException {
		return this.sendRangeTo(input, new DualPrintWriter(new UnlockedOutputStreamWriter(output, StandardCharsets.UTF_8), true), status, contentLength, sendResponse);
	}
	
	public final long sendRangeTo(InputStream input, DualPrintWriter pr, ClientStatus status, long contentLength, boolean sendResponse) throws IOException {
		pr.println(HTTPStatusCodes.HTTP_206.toString());
		Header.removeHeaderFrom(this.headers, "Content-Range", "Content-Length");
		this.sendHeadersTo(pr, sendResponse);
		pr.println("Content-Length: " + this.getContentLength());
		pr.println("Content-Range: bytes " + this.startBytes + "-" + this.endBytes + "/" + this.responseFile.contentLength);
		return new RangeRequest(this.startBytes, this.endBytes, contentLength).sendRangeTo(input, pr.getOutputStream(), status);
	}
	
	@SuppressWarnings("resource")
	private final void notifyClientOfError500(DualPrintWriter pr, Throwable e) throws IOException {
		try {
			byte[] response = ("<!DOCTYPE html>\r\n<html>\r\n\t<head>\r\n\t\t<title>" + this.httpVersion + " " + HTTPStatusCodes.HTTP_500.toString() + "</title>\r\n\t</head>\r\n\t<body>\t\t<string>" + StringUtil.throwableToStr(e, "<br>\r\n") + "\t\t</string>\r\n\t</body>\r\n</html>").getBytes(StandardCharsets.UTF_8);
			pr = new DualPrintWriter(new UnlockedOutputStreamWriter(pr.getOutputStream(), this.charset), false);
			pr.setLineSeparator("\r\n");
			pr.println(this.httpVersion + " " + HTTPStatusCodes.HTTP_500.toString());
			pr.println("Server: " + JavaWebServer.SERVER_NAME_HEADER);
			pr.println("Date: " + StringUtils.getCurrentCacheTime());
			pr.println("Content-Length: " + Integer.toString(response.length));
			pr.println("");
			pr.flush();
			pr.getOutputStream().write(response);
			pr.directFlush();
		} catch(IOException e1) {
			if(HTTPClientRequest.debug) {
				//e1.addSuppressed(e);
				throw new IOException("Unable to send error 500 to client after catching IOException: " + StringUtil.throwableToStr(e1), e);
			}
		}
	}
	
	protected final void markWriteTime() {
		this.reuse.status.markWriteTime();
	}
	
}
