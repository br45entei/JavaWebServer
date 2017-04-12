package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.FileInfo;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

/** @author Brian_Entei */
public final class ClientStatus {
	
	private volatile UUID uuid;
	private volatile long startTime;
	private final Socket client;
	private final ClientConnection connection;
	/** The file that the client is requesting */
	private volatile FileInfo requestedFile = null;
	private volatile long contentLength;
	private volatile long count = 0;
	private volatile boolean isCancelled = false;
	private volatile boolean isCancellable = true;
	private volatile boolean isPaused = false;
	private volatile String status = "Receiving Data...";
	private volatile String fileName = null;
	
	/** The rate at which the data transfer speed is calculated */
	public volatile long updateTime = 1000L;
	
	private volatile long dataTransferStartTime = -1L;
	private volatile long lastReadTime = System.currentTimeMillis();
	private volatile long lastWriteTime = System.currentTimeMillis();
	private volatile long lastReadAmount = 0L;
	private volatile long lastWriteAmount = 0L;
	
	/** Whether or not this file is currently being written to */
	public volatile boolean isBeingWrittenTo = false;
	
	private volatile long bytesSentToServer = 0L;
	private volatile long bytesSentToClient = 0L;
	private volatile boolean isOutgoingRequest = false;
	private volatile boolean isProxyRequest = false;
	
	/** @param s The client
	 * @param s The client's registered connection
	 * @param contentLength The content length of the data that the client is
	 *            uploading to this server, if applicable */
	public ClientStatus(Socket s, ClientConnection connection) {
		this.client = s;
		this.connection = connection;
		this.clientAddress = AddressUtil.getClientAddress(s);
		this.reset(null);
	}
	
	public final void reset(FileInfo requestedFile) {
		this.startTime = System.currentTimeMillis();
		this.uuid = UUID.randomUUID();
		this.setRequestedFile(requestedFile);
	}
	
	protected final void markCompleted() {
		if(this.client != null) {
			JavaWebServer.removeSocket(this.client);
		}
		//this.removeFromList();
		this.isProxyRequest = false;
		this.isOutgoingRequest = true;
	}
	
	/** @return the connection */
	public ClientConnection getConnection() {
		return this.connection;
	}
	
	public final void checkForPause() {
		if(this.isPaused()) {
			while(this.isPaused()) {
				if(this.isCancelled()) {
					break;
				}
				Functions.sleep();
			}
		}
	}
	
	public final boolean equals(Socket socket) {
		return this.client == socket;
	}
	
	public final UUID getUUID() {
		return this.uuid;
	}
	
	public final long getStartTime() {
		return this.startTime;
	}
	
	public Socket getClient() {
		return this.client;
	}
	
	private final String clientAddress;
	
	public final String getClientAddress() {
		return this.clientAddress;
	}
	
	public final boolean isCancelled() {
		return this.isCancelled;
	}
	
	public final ClientStatus cancel() {
		if(!this.isCancellable) {
			throw new IllegalStateException("Cannot cancel a non-cancellable client status!");
		}
		this.isCancelled = true;
		if(!this.client.isClosed()) {
			try {
				this.client.close();
			} catch(Throwable ignored) {
			}
		}
		/*JavaWebServer.connectedClients.remove(this);
		JavaWebServer.connectedClientRequests.remove(this);
		JavaWebServer.connectedProxyRequests.remove(this);*/
		return this;
	}
	
	public final ClientStatus setCancelled(boolean cancel) {
		if(!this.isCancellable && cancel) {
			throw new IllegalStateException("Cannot cancel a non-cancellable client status!");
		}
		this.isCancelled = cancel;
		return this;
	}
	
	public final boolean isCancellable() {
		return this.isCancellable;
	}
	
	public final void setCancellable(boolean cancellable) {
		this.isCancellable = cancellable;
	}
	
	public final boolean isPaused() {
		return this.isPaused;
	}
	
	/** Checks to see if the request has been paused by the UI user, and
	 * performs
	 * the pausing-action until the request is resumed. */
	public final void checkPause() {
		if(this.isPaused) {
			final String oldStatus = this.status;
			while(this.isPaused && Main.isRunning()) {
				this.status = "Operation paused by user.";
				Functions.sleep();//CodeUtil.sleep(6L);//4L);
				if(this.client.isClosed()) {
					break;
				}
			}
			this.status = oldStatus;
		}
	}
	
	public final ClientStatus pause() {
		this.isPaused = true;
		return this;
	}
	
	public final ClientStatus resume() {
		this.isPaused = false;
		return this;
	}
	
	public final ClientStatus togglePause() {
		this.isPaused = !this.isPaused;
		return this;
	}
	
	public final long getCount() {
		return this.count;
	}
	
	/** @param count The current status of the data transfer to/from this server
	 *            from/to the client
	 * @return This status */
	public final ClientStatus setCount(long count) {
		this.count = count;
		return this;
	}
	
	/** @param count The current status of the data transfer to/from this server
	 *            from/to the client
	 * @param readAmount The total amount of data read by this server
	 * @param writeAmount The total amount of data written by this server
	 * @return This status */
	public final ClientStatus setProgress(long count, long readAmount, long writeAmount) {
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
	
	/** @return the requestedFile */
	public FileInfo getRequestedFile() {
		return this.requestedFile;
	}
	
	/** @param requestedFile the requestedFile to set */
	public void setRequestedFile(FileInfo requestedFile) {
		this.requestedFile = requestedFile;
	}
	
	/** @return The content length of the data that the client is
	 *         uploading/downloading to/from this server, if applicable */
	public final long getContentLength() {
		return this.contentLength;
	}
	
	/** @param contentLength The content length of the data that the client is
	 *            uploading/downloading to/from this server, if applicable
	 * @return This status */
	public final ClientStatus setContentLength(long contentLength) {
		this.contentLength = contentLength;
		return this;
	}
	
	/** @return The current status of the request */
	public final String getStatus() {
		return this.status;
	}
	
	/** @param status The new status for this request
	 * @return This status */
	public final ClientStatus setStatus(String status) {
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
	public final ClientStatus setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	/** Adds one to this status's count(see {@link #getCount()}).
	 * 
	 * @return This status */
	public final ClientStatus incrementCount() {
		this.count++;
		return this;
	}
	
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
	/*public final ClientStatus addToList(Collection<ClientStatus> list) {
		if(this.list != null) {
			this.list.remove(this);
		}
		this.list = list;
		list.add(this);
		return this;
	}*/
	
	/** Removes this ClientInfo from the list it was added to, if there was one
	 * 
	 * @return This status
	 * @see #removeFromList(boolean) */
	/*public final ClientStatus removeFromList() {
		if(this.list != null) {
			try {
				this.list.remove(this);
				this.list = null;
			} catch(Throwable ignored) {
			}
		}
		return this;
	}*/
	
	/** Convenience method for nested calls. See {@link #removeFromList()}
	 * 
	 * @param doIt Whether or not this method should do anything.
	 * @return This status
	 * @see #addToList(ArrayList)
	 * @see #removeFromList() */
	/*public final ClientStatus removeFromList(boolean doIt) {
		if(doIt) {
			return this.removeFromList();
		}
		return this;
	}*/
	
	public final DomainDirectory getDomainDirectory() {
		return this.connection.domainDirectory;
	}
	
	public final ClientStatus setDomainDirectory(DomainDirectory domainDirectory) {
		this.connection.domainDirectory = domainDirectory == null ? DomainDirectory.getDefault(this.connection) : domainDirectory;
		return this;
	}
	
	/** @return This status */
	public final ClientStatus markDataTransferStartTime() {
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
	public final ClientStatus markReadTime() {
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final long getLastWriteTime() {
		return this.lastWriteTime;
	}
	
	/** @return This status */
	public final ClientStatus markWriteTime() {
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @return */
	public final long getLastReadAmount() {
		return this.lastReadAmount;
	}
	
	/** @param amount
	 * @return This status */
	public final ClientStatus setLastReadAmount(long amount) {
		this.lastReadAmount = amount;
		return this;
	}
	
	/** @return */
	public final long getLastWriteAmount() {
		return this.lastWriteAmount;
	}
	
	/** @param amount
	 * @return */
	public final ClientStatus setLastWriteAmount(long amount) {
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
	public final ClientStatus addNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer += numOfBytesSent;
		this.lastReadTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientStatus addNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient += numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientStatus setNumOfBytesSentToServer(long numOfBytesSent) {
		this.bytesSentToServer = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	/** @param numOfBytesSent
	 * @return This status */
	public final ClientStatus setNumOfBytesSentToClient(long numOfBytesSent) {
		this.bytesSentToClient = numOfBytesSent;
		this.lastWriteTime = System.currentTimeMillis();
		return this;
	}
	
	public final boolean canBeInAList() {
		return !this.isCancelled();// && !this.client.isClosed() && !this.client.isInputShutdown() && !this.client.isOutputShutdown();
	}
	
	public final boolean isOutgoing() {
		return !this.isProxyRequest && this.isOutgoingRequest;
	}
	
	/** @return */
	public boolean isIncoming() {
		return !this.isOutgoingRequest;
	}
	
	public final ClientStatus setIncoming(boolean incoming) {
		this.isOutgoingRequest = !incoming;
		return this;
	}
	
	/** @return */
	public final boolean isProxyRequest() {
		return this.isProxyRequest;
	}
	
	/** @param isProxy
	 * @return This status */
	public final ClientStatus setProxyRequest(boolean isProxy) {
		this.isProxyRequest = isProxy;
		this.isOutgoingRequest = true;
		return this;
	}
	
	@Override
	public final String toString() {
		FileInfo info = this.requestedFile;
		if(this.requestedFile == null) {
			if(this.connection.domainDirectory != null && this.connection.request != null && this.connection.request.requestedFilePath != null) {
				try {
					File file = this.connection.domainDirectory.getFileFromRequest(this.connection.request.requestedFilePath, this.connection.request.requestArguments, null);
					if(file != null && (file.exists() || file.getName().equalsIgnoreCase("favicon.ico") || file.getName().equalsIgnoreCase("layout.css"))) {
						info = new FileInfo(file, this.connection.domainDirectory);
					}
				} catch(IOException ignored) {
				}
			}
		}
		return "Client Info: " + this.getClientAddress() + "; Host used: \"" + (this.connection.request == null ? "[Not yet available]" : this.connection.request.host) + "\"; " + (info == null ? "[null requestedFile info]; " : info.toString() + "; ") + "Data: " + Functions.humanReadableByteCount(this.count, true, 2) + " / " + Functions.humanReadableByteCount(this.requestedFile == null ? this.contentLength : this.requestedFile.contentLength, true, 2) + (this.isIncoming() ? " received" : (this.isOutgoing() ? " sent" : "")) + "; Data Transfer Rate: " + Functions.humanReadableByteCount(this.lastWriteAmount * 5L, true, 2) + "/sec";//"\tClient: " + this.client.toString() + "\r\n\tRequest: \"" + this.clientRequest.protocolLine + "\"\r\n";
	}
	
}
