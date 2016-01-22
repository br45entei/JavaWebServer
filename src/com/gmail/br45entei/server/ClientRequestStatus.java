package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.CodeUtil;

import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/** @author Brian_Entei */
public class ClientRequestStatus implements ClientStatus {
	
	private final UUID								uuid;
	private final long								startTime;
	private volatile Socket							client;
	private volatile long							contentLength;
	private volatile long							count					= 0;
	private volatile boolean						isCancelled				= false;
	private volatile boolean						isPaused				= false;
	private volatile String							status					= "Receiving Data...";
	private volatile String							fileName				= null;
	
	private volatile long							dataTransferStartTime	= -1L;
	private volatile long							lastReadTime			= System.currentTimeMillis();
	private volatile long							lastWriteTime			= System.currentTimeMillis();
	private volatile long							lastReadAmount			= 0L;
	private volatile long							lastWriteAmount			= 0L;
	
	private volatile ArrayList<ClientRequestStatus>	list					= null;
	private volatile long							bytesSentToServer		= 0L;
	private volatile long							bytesSentToClient		= 0L;
	private volatile boolean						isProxyRequest			= false;
	
	protected final void markCompleted() {
		if(this.client != null) {
			JavaWebServer.sockets.remove(this.client);
		}
		this.client = null;
		this.removeFromList();
		this.list = null;
	}
	
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
			this.clientAddress = this.client != null ? AddressUtil.getClientAddress((this.client.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(this.client.getRemoteSocketAddress().toString(), "/", "") : "") : "";
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
	
	public final void checkPause() {
		if(this.isPaused) {
			final String oldStatus = this.status;
			while(this.isPaused) {
				this.status = "Operation paused by user.";
				CodeUtil.sleep(4L);
			}
			this.status = oldStatus;
		}
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
	public final long getCount() {
		return this.count;
	}
	
	public final ClientRequestStatus setCount(long count) {
		this.count = count;
		return this;
	}
	
	public final ClientRequestStatus setProgress(long count, long readAmount, long writeAmount) {
		this.count = count;
		final long oldReadAmt = this.lastReadAmount;
		final long oldWriteAmt = this.lastWriteAmount;
		this.lastReadAmount = readAmount;
		this.lastWriteAmount = writeAmount;
		if(this.lastReadAmount > oldReadAmt) {
			this.markReadTime();
		}
		if(this.lastWriteAmount > oldWriteAmt) {
			this.markWriteTime();
		}
		return this;
	}
	
	@Override
	public final long getContentLength() {
		return this.contentLength;
	}
	
	public final ClientRequestStatus setContentLength(long contentLength) {
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
	
	public final ClientRequestStatus markDataTransferStartTime() {
		this.dataTransferStartTime = System.currentTimeMillis();
		return this;
	}
	
	public final long getDataTransferStartTime() {
		return this.dataTransferStartTime;
	}
	
	public final long getDataTransferElapsedTime() {
		return System.currentTimeMillis() - this.dataTransferStartTime;
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
	
	public final ClientRequestStatus markWriteTime() {
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	public final long getLastReadAmount() {
		return this.lastReadAmount;
	}
	
	public final ClientRequestStatus setLastReadAmount(long amount) {
		this.lastReadAmount = amount;
		return this;
	}
	
	public final long getLastWriteAmount() {
		return this.lastWriteAmount;
	}
	
	public final ClientRequestStatus setLastWriteAmount(long amount) {
		this.lastWriteAmount = amount;
		return this;
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
