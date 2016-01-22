/**
 * 
 */
package com.gmail.br45entei.server;

import com.gmail.br45entei.server.data.DomainDirectory;

import java.net.Socket;
import java.util.UUID;

/** @author Brian_Entei */
public interface ClientStatus {
	
	/** @return This status's UUID. */
	public UUID getUUID();
	
	/** @return The time at which the client made the request */
	public long getStartTime();
	
	/** @return This status's client socket. */
	public Socket getClient();
	
	/** @return The client socket's address */
	public String getClientAddress();
	
	/** @return This request's count divided by this request's content
	 *         length(usually a 0 to 1 value). */
	public double getProgress();
	
	/** @return This status's current byte count. */
	public long getCount();
	
	/** @return This status's content length. */
	public long getContentLength();
	
	/** @return Whether or not this status has been cancelled. */
	public boolean isCancelled();
	
	/** @return This status. */
	public ClientStatus cancel();
	
	/** @return Whether or not this status has been paused. */
	public boolean isPaused();
	
	/** @return This status. */
	public ClientStatus pause();
	
	/** @return This status. */
	public ClientStatus resume();
	
	/** @return This status. */
	public ClientStatus togglePause();
	
	/** @return This status's domain directory(may be null) */
	public DomainDirectory getDomainDirectory();
	
}
