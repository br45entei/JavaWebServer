package com.gmail.br45entei.server;

/** @author Brian_Entei */
public class ReusedConn {
	
	public static final ReusedConn	FALSE		= new ReusedConn(false, "");
	
	public volatile boolean			reuse		= false;
	public volatile String			hostUsed	= "";
	
	public ReusedConn(boolean reuse, String hostUsed) {
		this.reuse = reuse;
		this.hostUsed = hostUsed;
	}
	
}
