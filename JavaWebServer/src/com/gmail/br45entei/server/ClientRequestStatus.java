package com.gmail.br45entei.server;

import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.util.StringUtil;

import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/** @author Brian_Entei */
public class ClientRequestStatus implements ClientStatus {
	
	private final UUID						uuid;
	private final long						startTime;
	private final Socket					client;
	private int								contentLength;
	private int								count				= 0;
	private boolean							isCancelled			= false;
	private boolean							isPaused			= false;
	private String							status				= "Receiving Data...";
	private String							fileName			= null;
	private long							lastReadTime		= System.currentTimeMillis();
	private long							lastWriteTime		= System.currentTimeMillis();
	private ArrayList<ClientRequestStatus>	list				= null;
	private long							bytesSentToServer	= 0L;
	private long							bytesSentToClient	= 0L;
	private boolean							isProxyRequest		= false;
	
	public ClientRequestStatus(Socket s, int contentLength) {
		this.uuid = UUID.randomUUID();
		this.startTime = System.currentTimeMillis();
		this.client = s;
		this.contentLength = contentLength;
	}
	
	@Override
	public final UUID getUUID() {
		return this.uuid;
	}
	
	@Override
	public final long getStartTime() {
		return this.startTime;
	}
	
	@Override
	public Socket getClient() {
		return this.client;
	}
	
	private String	clientAddress	= null;
	
	@Override
	public final String getClientAddress() {
		if(this.clientAddress == null || this.clientAddress.isEmpty()) {
			this.clientAddress = this.client != null ? StringUtil.getClientAddress((this.client.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(this.client.getRemoteSocketAddress().toString(), "/", "") : "") : "";
		}
		return this.clientAddress;
	}
	
	@Override
	public final boolean isCancelled() {
		return this.isCancelled;
	}
	
	@Override
	public final ClientRequestStatus cancel() {
		this.isCancelled = true;
		if(this.client != null) {
			try {
				this.client.close();
			} catch(Throwable ignored) {
			}
		}
		return this;
	}
	
	@Override
	public final boolean isPaused() {
		return this.isPaused;
	}
	
	@Override
	public final ClientRequestStatus pause() {
		this.isPaused = true;
		return this;
	}
	
	@Override
	public final ClientRequestStatus resume() {
		this.isPaused = false;
		return this;
	}
	
	@Override
	public final ClientRequestStatus togglePause() {
		this.isPaused = !this.isPaused;
		return this;
	}
	
	@Override
	public final int getCount() {
		return this.count;
	}
	
	public final ClientRequestStatus setCount(int count) {
		this.count = count;
		return this;
	}
	
	@Override
	public final int getContentLength() {
		return this.contentLength;
	}
	
	public final ClientRequestStatus setContentLength(int contentLength) {
		this.contentLength = contentLength;
		return this;
	}
	
	public final String getStatus() {
		return this.status;
	}
	
	public final ClientRequestStatus setStatus(String status) {
		this.status = status;
		return this;
	}
	
	public final String getFileName() {
		return this.fileName;
	}
	
	public final ClientRequestStatus setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	public final ClientRequestStatus incrementCount() {
		this.count++;
		return this;
	}
	
	@Override
	public final double getProgress() {
		double min = new Double(this.count + ".00D").doubleValue();
		double max = new Double(this.contentLength + ".00D").doubleValue();
		if(this.contentLength == 0) {
			return this.count;
		}
		return(min / max);
	}
	
	public final ClientRequestStatus addToList(ArrayList<ClientRequestStatus> list) {
		this.list = list;
		list.add(this);
		return this;
	}
	
	public final ClientRequestStatus removeFromList() {
		if(this.list != null) {
			try {
				this.list.remove(this);
				this.list = null;
			} catch(Throwable ignored) {
			}
		}
		return this;
	}
	
	public final ClientRequestStatus removeFromList(boolean doIt) {
		if(doIt) {
			return this.removeFromList();
		}
		return this;
	}
	
	@Override
	public final DomainDirectory getDomainDirectory() {
		return null;
	}
	
	public final long getLastReadTime() {
		return this.lastReadTime;
	}
	
	public final ClientRequestStatus markReadTime() {
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	public final long getLastWriteTime() {
		return this.lastWriteTime;
	}
	
	public final long getNumOfBytesSentToServer() {
		return this.bytesSentToServer;
	}
	
	public final long getNumOfBytesSentToClient() {
		return this.bytesSentToClient;
	}
	
	public final ClientRequestStatus addNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer += numOfBytesSent;
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	public final ClientRequestStatus addNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient += numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	public final ClientRequestStatus setNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	public final ClientRequestStatus setNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	public final boolean isProxyRequest() {
		return this.isProxyRequest;
	}
	
	public final ClientRequestStatus setProxyRequest(boolean isProxy) {
		this.isProxyRequest = isProxy;
		return this;
	}
	
}
