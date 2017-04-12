package com.gmail.br45entei.server.data;

/** @author Brian_Entei */
public class ClientConnectTime {
	
	public volatile int numberOfConnections = 0;
	public volatile long lastConnectionTime = -1L;
	
	public final boolean seemsMalicious() {
		if(this.numberOfConnections >= 24) {//7
			return (System.currentTimeMillis() - this.lastConnectionTime) <= 250L;
		}
		return false;
	}
	
}
