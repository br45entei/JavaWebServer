package com.gmail.br45entei.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class SocketConnectResult {
	public final Socket			s;
	public final String			failReason;
	public final boolean		failedToConnect;
	public final InputStream	in;
	public final OutputStream	out;
	
	public SocketConnectResult(String address, int port) {
		Socket server = new Socket();
		InetSocketAddress addr = new InetSocketAddress(address, port);
		boolean failedToConnect = addr.isUnresolved();
		String failReason = "Server address is unresolved";
		InputStream in;
		OutputStream out;
		try {
			try {
				server.close();
			} catch(Throwable ignored) {
			}
			server = new Socket(address, port);
			failedToConnect = false;
			failReason = "";
			in = server.getInputStream();
			out = server.getOutputStream();
		} catch(Throwable e) {
			try {
				server.close();
			} catch(Throwable ignored) {
			}
			failedToConnect = true;
			failReason = e.getClass().getSimpleName() + ": " + e.getMessage();
			in = null;
			out = null;
		}
		this.s = server;
		this.in = in;
		this.out = out;
		this.failReason = failReason;
		this.failedToConnect = failedToConnect;
	}
	
}
