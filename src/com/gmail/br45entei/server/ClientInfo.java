package com.gmail.br45entei.server;

import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.util.AddressUtil;

import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/** Class used to hold information regarding connected clients
 *
 * @author Brian_Entei */
public final class ClientInfo implements ClientStatus {
	/** The client */
	private final Socket			client;
	private final long				startTime;
	/** The file that the client is requesting */
	public final FileInfo			requestedFile;
	
	public final HTTPClientRequest	clientRequest;
	/** This connection's Universally Unique Identifier */
	public final UUID				uuid;
	
	private long					lastWriteTime	= System.currentTimeMillis();
	
	/** @return The last time data was written to the client, in milliseconds */
	public long getLastWriteTime() {
		return this.lastWriteTime;
	}
	
	/** @param lastWriteTime The last time data was written to the client, in
	 *            milliseconds */
	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	
	private ArrayList<ClientInfo>	list	= null;
	
	/** @param client The client
	 * @param requestedFile The file that the client is requesting
	 * @param request The request sent by the client */
	public ClientInfo(Socket client, FileInfo requestedFile, HTTPClientRequest request) {
		this.client = client;
		this.startTime = System.currentTimeMillis();
		this.requestedFile = requestedFile;
		this.clientRequest = request;
		this.uuid = UUID.randomUUID();
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
	public final Socket getClient() {
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
	public final double getProgress() {
		double min = new Double(this.requestedFile.bytesTransfered + ".00D").doubleValue();
		double max = new Double(this.requestedFile.contentLength + ".00D").doubleValue();
		return(min / max);
	}
	
	@Override
	public final int getCount() {
		return Long.valueOf(this.requestedFile.bytesTransfered).intValue();
	}
	
	@Override
	public final int getContentLength() {
		try {
			return Integer.valueOf(this.requestedFile.contentLength).intValue();
		} catch(NumberFormatException ignored) {
			return 0;
		}
	}
	
	@Override
	public final boolean isCancelled() {
		return this.requestedFile.isCancelled;
	}
	
	@Override
	public final ClientInfo cancel() {
		this.requestedFile.isCancelled = true;
		return this;
	}
	
	@Override
	public final boolean isPaused() {
		return this.requestedFile.isPaused;
	}
	
	@Override
	public final ClientInfo pause() {
		this.requestedFile.isPaused = true;
		return this;
	}
	
	@Override
	public final ClientInfo resume() {
		this.requestedFile.isPaused = false;
		return this;
	}
	
	@Override
	public final ClientInfo togglePause() {
		this.requestedFile.isPaused = !this.requestedFile.isPaused;
		return this;
	}
	
	public final ClientInfo addToList(ArrayList<ClientInfo> list) {
		this.list = list;
		list.add(this);
		return this;
	}
	
	public final ClientInfo removeFromList() {
		if(this.list != null) {
			this.list.remove(this);
			this.list = null;
		}
		return this;
	}
	
	@Override
	public final DomainDirectory getDomainDirectory() {
		return this.requestedFile.domainDirectory;
	}
	
	@Override
	public final String toString() {
		try {
			return "\tClient: " + this.client.toString() + "\r\n\tRequest: \"" + this.clientRequest.protocolRequest + "\"\r\n\tRequested File: " + this.requestedFile.filePath;
		} catch(Throwable e) {
			return "\tClient: null\r\n\tRequest: \"" + this.clientRequest.protocolRequest + "\"\r\n\tRequested File: " + this.requestedFile.filePath;
		}
	}
	
}
