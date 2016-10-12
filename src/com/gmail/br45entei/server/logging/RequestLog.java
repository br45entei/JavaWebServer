package com.gmail.br45entei.server.logging;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.HTTPClientRequest;
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
	
	private final long		creationTime;
	private volatile byte[]	requestData	= new byte[0];
	private final String	clientIP;
	private final String	clientRequest;
	private final String	clientUserAgent;
	
	private final String	requestedHost;
	
	private volatile byte[]	resultData	= new byte[0];
	
	public RequestLog(HTTPClientRequest request) {
		if(request == null) {
			throw new IllegalArgumentException("Request cannot be null!");
		}
		if(request.isCompleted()) {
			throw new IllegalArgumentException("Request has already been completed!\r\n" + request.toString());
		}
		this.creationTime = request.creationTime;
		this.requestData = request.toString().getBytes();
		this.clientIP = request.getClientIPAddress();
		this.clientRequest = request.protocolRequest;
		this.clientUserAgent = request.userAgent;
		this.requestedHost = request.host == null || request.host.trim().isEmpty() ? request.http2Host : request.host.trim();
	}
	
	public final boolean seemsComplete() {
		boolean flag1 = this.clientIP != null;
		boolean flag2 = flag1 && !this.clientIP.trim().isEmpty();
		boolean flag3 = this.clientRequest != null;
		boolean flag4 = flag3 && !this.clientRequest.trim().isEmpty();
		return(flag2 && flag4);
	}
	
	public final RequestLog setResult(String result) {
		this.resultData = result.getBytes();
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
		final String fileName = "request_" + this.clientIP.replace(":", ";") + "_" + StringUtil.getTime(this.creationTime, false, true) + ".log.gz";
		System.out.println("FileName: " + fileName);
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
			pr.println("Time and Date: " + StringUtil.getTime(this.creationTime, false, false));
			pr.println("Request: " + this.clientRequest);
			pr.println("Requested host: " + this.requestedHost);
			pr.println("User-Agent: " + this.clientUserAgent);
			pr.println("");
			pr.println("Full request data:");
			pr.flush();
			out.write(this.requestData);
			out.flush();
			pr.println("");
			pr.println("Result from server:");
			pr.flush();
			out.write(this.resultData);
			out.flush();
			pr.close();
		} catch(Throwable e) {
			System.err.print("Failed to save request log to file system: ");
			e.printStackTrace();
		}
	}
	
}
