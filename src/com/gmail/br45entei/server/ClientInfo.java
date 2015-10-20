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
	private final Socket	client;
	private final long		startTime;
	/** The file that the client is requesting */
	public final FileInfo	requestedFile;
	/** This connection's Universally Unique Identifier */
	public final UUID		uuid;
	
	//TODO Remove the header information below, as it is already stored in HTTPClientRequest
	
	/** The HTTP protocol request that the client sent(i.e. GET, HEAD, POST,
	 * etc) */
	public final String		protocolRequest;
	/** The domain or host used to connect to this server(e.g.
	 * www.example.com or 192.168.0.1) */
	public final String		host;
	/** HTTP header sent by the client */
	public final String		connectionSetting;
	/** HTTP header sent by the client */
	public final String		cacheControl;
	/** HTTP header sent by the client */
	public final String		accept;
	/** HTTP header sent by the client */
	public final String		userAgent;
	/** HTTP header sent by the client */
	public final String		dnt;
	/** HTTP header sent by the client */
	public final String		referrerLink;
	/** HTTP header sent by the client */
	public final String		acceptEncoding;
	/** HTTP header sent by the client */
	public final String		acceptLanguage;
	/** HTTP header sent by the client */
	public final String		from;
	/** HTTP header sent by the client */
	public final String		cookie;
	/** HTTP header sent by the client */
	public final String		range;
	/** HTTP header sent by the client */
	public final String		authorization;
	/** HTTP header sent by the client */
	public final String		ifModifiedSince;
	/** HTTP header sent by the client */
	public final String		ifRange;
	/** HTTP header sent by the client */
	public final String		ifNoneMatch;
	private long			lastWriteTime	= System.currentTimeMillis();
	
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
	 * @param protocolRequest The HTTP protocol request that the client
	 *            sent(i.e. GET, HEAD, POST, etc)
	 * @param host The domain or host used to connect to this server(e.g.
	 *            www.example.com, 192.168.0.1, [::1], etc.)
	 * @param connectionSetting HTTP header sent by the client
	 * @param cacheControl HTTP header sent by the client
	 * @param accept HTTP header sent by the client
	 * @param userAgent HTTP header sent by the client
	 * @param dnt HTTP header sent by the client
	 * @param referrerLink HTTP header sent by the client
	 * @param acceptEncoding HTTP header sent by the client
	 * @param acceptLanguage HTTP header sent by the client
	 * @param from HTTP header sent by the client
	 * @param cookie HTTP header sent by the client
	 * @param range HTTP header sent by the client
	 * @param authorization HTTP header sent by the client
	 * @param ifModifiedSince A date header sent by the client used to
	 *            determine if the file has been modified since that date
	 * @param ifNoneMatch HTTP header sent by the client */
	public ClientInfo(Socket client, FileInfo requestedFile, String protocolRequest, String host, String connectionSetting, String cacheControl, String accept, String userAgent, String dnt, String referrerLink, String acceptEncoding, String acceptLanguage, String from, String cookie, String range, String authorization, String ifModifiedSince, String ifNoneMatch, String ifRange) {
		this.client = client;
		this.startTime = System.currentTimeMillis();
		this.requestedFile = requestedFile;
		this.uuid = UUID.randomUUID();
		this.protocolRequest = protocolRequest;
		this.host = host;
		this.connectionSetting = connectionSetting;
		this.cacheControl = cacheControl;
		this.accept = accept;
		this.userAgent = userAgent;
		this.dnt = dnt;
		this.referrerLink = referrerLink;
		this.acceptEncoding = acceptEncoding;
		this.acceptLanguage = acceptLanguage;
		this.from = from;
		this.cookie = cookie;
		this.range = range;
		this.authorization = authorization;
		this.ifModifiedSince = ifModifiedSince;
		this.ifNoneMatch = ifNoneMatch;
		this.ifRange = ifRange;
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
			return "\tClient: " + this.client.toString() + "\r\n\tRequest: \"" + this.protocolRequest + "\"\r\n\tRequested File: " + this.requestedFile.filePath;
		} catch(Throwable e) {
			return "\tClient: null\r\n\tRequest: \"" + this.protocolRequest + "\"\r\n\tRequested File: " + this.requestedFile.filePath;
		}
	}
	
}
