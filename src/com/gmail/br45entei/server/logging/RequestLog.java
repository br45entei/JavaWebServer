package com.gmail.br45entei.server.logging;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/** @author Brian_Entei */
public class RequestLog {
	
	private final ClientConnection reuse;
	
	private final long creationTime;
	private volatile byte[] requestData = new byte[0];
	private final String clientIP;
	private final String clientRequest;
	private final String clientUserAgent;
	private final byte[] serverResponse;
	
	private final String requestedHost;
	
	private volatile byte[] resultData = new byte[0];
	
	public RequestLog(final ClientConnection reuse) {
		this.reuse = reuse;
		final HTTPClientRequest request = this.reuse.request;
		final HTTPServerResponse response = this.reuse.response;
		if(request == null) {
			throw new IllegalArgumentException("Request cannot be null!");
		}
		if(request.isCompleted()) {
			throw new IllegalArgumentException("Request has already been completed!\r\n" + request.toString());
		}
		this.creationTime = request.creationTime;
		this.requestData = request.toString().getBytes(StandardCharsets.UTF_8);
		this.clientIP = request.getClientIPAddressNoPort();
		this.clientRequest = request.protocolLine;
		this.clientUserAgent = request.userAgent;
		this.requestedHost = request.host == null || request.host.trim().isEmpty() ? request.http2Host : request.host.trim();
		String r = response == null ? "[null server response]" : (response.getResponse() != null ? response.toString(true) : null);
		if(r == null) {
			FileInfo rFile = response != null ? response.getResponseFile() : null;
			if(rFile != null) {
				r = "Requested file \"" + rFile.filePath + "\" served to client.";
			} else {
				r = response == null ? "[No server response was given]" : response.toString(true);
			}
		}
		this.serverResponse = r.getBytes(StandardCharsets.UTF_8);
	}
	
	public final boolean seemsComplete() {
		boolean flag1 = this.clientIP != null;
		boolean flag2 = flag1 && !this.clientIP.trim().isEmpty();
		boolean flag3 = this.clientRequest != null;
		boolean flag4 = flag3 && !this.clientRequest.trim().isEmpty();
		return(flag2 && flag4);
	}
	
	public final RequestLog setResult(String result) {
		this.resultData = result.getBytes(StandardCharsets.UTF_8);
		return this;
	}
	
	private static final File getSaveFolder() {
		File folder = new File(JavaWebServer.rootDir, "logs" + File.separatorChar + "requests");
		if(!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}
	
	private final File getSaveFile() throws IOException {
		final String fileName = "request_" + this.clientIP.replace(":", ";") + "_" + StringUtil.getTime(this.creationTime, false, true, true) + ".log.gz";
		this.reuse.println("\t--- Saved request log as: " + fileName);
		File file = new File(getSaveFolder(), fileName);
		if(!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	public final void saveToFile() {
		try {
			File save = this.getSaveFile();
			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(save), 512, false);
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
			pr.println("IP: " + this.clientIP);
			pr.println("Time and Date: " + StringUtil.getTime(this.creationTime, false, false, true));
			pr.println("Request: " + this.clientRequest);
			pr.println("Requested host: " + this.requestedHost);
			pr.println("User-Agent: " + this.clientUserAgent);
			pr.println("");
			if(this.requestData.length > 0) {
				pr.println("Full request data, up to 16384 bytes(16KB):");
				pr.flush();
				out.write(this.requestData, 0, Math.min(this.requestData.length, 16384));
				out.flush();
				//pr.println("");
			}
			pr.println("Result from server:");
			pr.flush();
			//if(this.serverResponse != null && this.serverResponse.length > 0) {
			out.write(this.serverResponse, 0, Math.min(this.serverResponse.length, 16384));//only save up to 16384 bytes of the response, we don't want excessive copies of files in the logs...
			/*} else {
				if(this.reuse.response != null) {
					byte[] r = this.reuse.response.toString(true).getBytes(StandardCharsets.UTF_8);
					out.write(r, 0, Math.min(r.length, 16384));
				} else {
					out.write("[null]".getBytes(StandardCharsets.UTF_8));
				}
			}*/
			if(this.resultData.length > 0) {
				out.write("\r\n".getBytes(StandardCharsets.UTF_8));
			}
			out.write(this.resultData);
			out.flush();
			pr.close();
		} catch(Throwable e) {
			System.err.print("Failed to save request log to file system: ");
			e.printStackTrace();
		}
	}
	
}
