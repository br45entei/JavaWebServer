package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

/** @author Brian_Entei */
public class HTTPServerResponse {
	
	protected String						httpVersion;
	protected String						statusCode;
	protected final HashMap<String, String>	headers			= new HashMap<>();
	protected String						response		= "";
	protected FileInfo						responseFile	= null;
	
	protected DomainDirectory				domainDirectory	= null;
	
	protected boolean						useGZip;
	protected Charset						charset;
	
	protected ClientInfo					clientInfo		= null;
	
	//===
	
	protected String						statusMessage	= "";
	
	public HTTPServerResponse(String httpVersion, HTTPStatusCodes statusCode, boolean useGZip, Charset charset) {
		this.setHTTPVersion(httpVersion).setStatusCode(statusCode).setUseGZip(useGZip).setCharset(charset);
	}
	
	public final String getHTTPVersion() {
		return this.httpVersion;
	}
	
	public final HTTPServerResponse setHTTPVersion(String httpVersion) {
		this.httpVersion = httpVersion;
		return this;
	}
	
	public final String getStatusCode() {
		return this.statusCode;
	}
	
	public final HTTPServerResponse setStatusCode(HTTPStatusCodes statusCode) {
		this.statusCode = statusCode.toString();
		return this;
	}
	
	protected final String getHeaderKey(String header) {
		for(String key : this.headers.keySet()) {
			if(header.equalsIgnoreCase(key)) {
				return key;
			}
		}
		return header;
	}
	
	public final HTTPServerResponse setHeader(String header, String value) {
		this.headers.put(this.getHeaderKey(header.replace(": ", "")), value);
		if(header.equalsIgnoreCase("connection") && value.equalsIgnoreCase("close")) {
			this.headers.remove(this.getHeaderKey("Keep-Alive"));
		}
		return this;
	}
	
	public final String getHeaderValue(String header) {
		return this.headers.get(this.getHeaderKey(header.replace(": ", "")));
	}
	
	public final HTTPServerResponse clearHeaders() {
		this.headers.clear();
		return this;
	}
	
	public final HTTPServerResponse setResponse(String response) {
		this.response = response;
		return this;
	}
	
	public final HTTPServerResponse appendResponse(String str) {
		this.response += str;
		return this;
	}
	
	public final String getResponse() {
		return this.response;
	}
	
	public final FileInfo getResponseFile() {
		return this.responseFile;
	}
	
	public final HTTPServerResponse setResponse(FileInfo file) {
		this.responseFile = file;
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
		return this.charset;
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
	
	public final void sendToClient(Socket s, boolean sendResponse) throws IOException {
		if(s == null) {
			return;
		}
		final OutputStream outStream = s.getOutputStream();
		@SuppressWarnings("resource")
		final PrintWriter pr = new PrintWriter(new OutputStreamWriter(outStream, this.charset), true);
		JavaWebServer.println("\t*** " + this.httpVersion + " " + this.statusCode + (this.statusMessage != null && this.statusMessage.isEmpty() ? "" : ": " + this.statusMessage));
		pr.println(this.httpVersion + " " + this.statusCode);
		this.markWriteTime();
		for(Entry<String, String> entry : this.headers.entrySet()) {
			if(sendResponse) {
				if(!entry.getKey().equalsIgnoreCase("Content-Length") && (this.useGZip ? !entry.getKey().equalsIgnoreCase("Content-Encoding") : true)) {
					pr.println(entry.getKey() + ": " + entry.getValue());
					this.markWriteTime();
				}
			} else {
				pr.println(entry.getKey() + ": " + entry.getValue());
				this.markWriteTime();
			}
		}
		if(this.response == null && this.responseFile == null) {
			pr.println("");
			this.markWriteTime();
		} else {
			if(this.responseFile == null) {
				if((this.domainDirectory == null ? true : this.domainDirectory.getEnableGZipCompression()) && this.useGZip && this.response.length() > 33) {
					pr.println("Content-Encoding: gzip");
					byte[] r = StringUtil.compressString(this.response, this.charset.name());
					pr.println("Content-Length: " + r.length);
					pr.println("");
					this.markWriteTime();
					if(sendResponse) {
						//outStream.write(r);
						for(int i = 0; i < r.length; i++) {
							outStream.write(r[i]);
							this.markWriteTime();
						}
					}
				} else {
					pr.println("Content-Length: " + this.response.length());
					pr.println("");
					this.markWriteTime();
					if(sendResponse) {
						pr.println(this.response);
					}
					this.markWriteTime();
				}
				pr.flush();
				//pr.close();
				this.markWriteTime();
			} else {
				File responseFile = new File(this.responseFile.filePath);
				if(responseFile.exists() && responseFile.isFile()) {
					pr.println("Content-Length: " + this.responseFile.contentLength);
					pr.println("");
					this.markWriteTime();
					if(sendResponse) {
						InputStream fileIn = responseFile.toURI().toURL().openConnection().getInputStream();
						IOException exception = null;
						try {
							JavaWebServer.copyInputStreamToOutputStream(this.responseFile, fileIn, outStream, (this.domainDirectory == null ? 1024 : this.domainDirectory.getNetworkMTU()), this.clientInfo);
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
					this.markWriteTime();
				}
			}
		}
		pr.flush();
		outStream.flush();
		//outStream.close();
		//pr.close();
		//s.close();
	}
	
	/** @param clientInfo The ClientInfo to use */
	public final void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}
	
	protected final void markWriteTime() {
		if(this.clientInfo != null) {
			this.clientInfo.setLastWriteTime(System.currentTimeMillis());
		}
	}
	
}
