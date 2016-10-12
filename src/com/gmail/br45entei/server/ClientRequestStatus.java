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
	private volatile boolean						isCancellable			= true;
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
	
	/** @param s The client
	 * @param contentLength The content length of the data that the client is
	 *            uploading to this server, if applicable */
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
		if(!this.isCancellable) {
			throw new IllegalStateException("Cannot cancel a non-cancellable Status!");
		}
		this.isCancelled = true;
		if(this.client != null) {
			try {
				this.client.close();
			} catch(Throwable ignored) {
			}
		}
		return this;
	}
	
	public final boolean isCancellable() {
		return this.isCancellable;
	}
	
	public final void setCancellable(boolean cancellable) {
		this.isCancellable = cancellable;
	}
	
	@Override
	public final boolean isPaused() {
		return this.isPaused;
	}
	
	/** Checks to see if the request has been paused by the UI user, and performs
	 * the pausing-action until the request is resumed. */
	public final void checkPause() {
		if(this.isPaused) {
			final String oldStatus = this.status;
			while(this.isPaused) {
				this.status = "Operation paused by user.";
				CodeUtil.sleep(6L);//4L);
				if(this.client == null || this.client.isClosed()) {
					break;
				}
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
	
	/** @param count The current status of the data transfer to/from this server
	 *            from/to the client
	 * @return This status */
	public final ClientRequestStatus setCount(long count) {
		this.count = count;
		return this;
	}
	
	/** @param count The current status of the data transfer to/from this server
	 *            from/to the client
	 * @param readAmount The total amount of data read by this server
	 * @param writeAmount The total amount of data written by this server
	 * @return This status */
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
	
	/** @param contentLength The content length of the data that the client is
	 *            uploading/downloading to/from this server, if applicable
	 * @return This status */
	public final ClientRequestStatus setContentLength(long contentLength) {
		this.contentLength = contentLength;
		return this;
	}
	
	/** @return The current status of the request */
	public final String getStatus() {
		return this.status;
	}
	
	/** @param status The new status for this request
	 * @return This status */
	public final ClientRequestStatus setStatus(String status) {
		this.status = status;
		return this;
	}
	
	/** @return The name of the file being transferred, if applicable */
	public final String getFileName() {
		return this.fileName;
	}
	
	/** @param fileName The name of the file currently being transferred, if
	 *            applicable
	 * @return This status */
	public final ClientRequestStatus setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	/** Adds one to this status's count(see {@link #getCount()}).
	 * 
	 * @return This status */
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
	
	/** @param list The list to add this status to
	 * @return This status
	 * @see #removeFromList()
	 * @see #removeFromList(boolean) */
	public final ClientRequestStatus addToList(ArrayList<ClientRequestStatus> list) {
		this.list = list;
		list.add(this);
		return this;
	}
	
	/** Removes this ClientInfo from the list it was added to, if there was one
	 * 
	 * @return This status
	 * @see #removeFromList(boolean) */
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
	
	/** Convenience method for nested calls. See {@link #removeFromList()}
	 * 
	 * @param doIt Whether or not this method should do anything.
	 * @return This status
	 * @see #addToList(ArrayList)
	 * @see #removeFromList() */
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
	
	/** @return This status */
	public final ClientRequestStatus markDataTransferStartTime() {
		this.dataTransferStartTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final long getDataTransferStartTime() {
		return this.dataTransferStartTime;
	}
	
	/** @return */
	public final long getDataTransferElapsedTime() {
		return System.currentTimeMillis() - this.dataTransferStartTime;
	}
	
	/** @return */
	public final long getLastReadTime() {
		return this.lastReadTime;
	}
	
	/** @return This status */
	public final ClientRequestStatus markReadTime() {
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final long getLastWriteTime() {
		return this.lastWriteTime;
	}
	
	/** @return This status */
	public final ClientRequestStatus markWriteTime() {
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final long getLastReadAmount() {
		return this.lastReadAmount;
	}
	
	/** @param amount
	 * @return This status */
	public final ClientRequestStatus setLastReadAmount(long amount) {
		this.lastReadAmount = amount;
		return this;
	}
	
	/** @return */
	public final long getLastWriteAmount() {
		return this.lastWriteAmount;
	}
	
	/** @param amount
	 * @return */
	public final ClientRequestStatus setLastWriteAmount(long amount) {
		this.lastWriteAmount = amount;
		return this;
	}
	
	/** @return */
	public final long getNumOfBytesSentToServer() {
		return this.bytesSentToServer;
	}
	
	/** @return */
	public final long getNumOfBytesSentToClient() {
		return this.bytesSentToClient;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientRequestStatus addNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer += numOfBytesSent;
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientRequestStatus addNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient += numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientRequestStatus setNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientRequestStatus setNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final boolean isProxyRequest() {
		return this.isProxyRequest;
	}
	
	/** @param isProxy
	 * @return This status */
	public final ClientRequestStatus setProxyRequest(boolean isProxy) {
		this.isProxyRequest = isProxy;
		return this;
	}
	
}
