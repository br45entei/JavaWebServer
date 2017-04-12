package com.gmail.br45entei.server;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_NOT_SET;

import com.gmail.br45entei.ConsoleThread;
import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.server.data.ClientConnectTime;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.NaughtyClientData.BanCheckResult;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/** @author Brian_Entei */
public class ClientConnection {
	
	public final Thread thread;
	
	public static final Collection<ClientConnection> getOutgoingConnections() {
		ArrayList<ClientConnection> list = new ArrayList<>();
		for(ClientConnection connection : JavaWebServer.sockets) {
			if(connection.status.isOutgoing()) {
				list.add(connection);
			}
		}
		return list;
	}
	
	public static final Collection<ClientConnection> getIncomingConnections() {
		ArrayList<ClientConnection> list = new ArrayList<>();
		for(ClientConnection connection : JavaWebServer.sockets) {
			if(connection.status.isIncoming()) {
				list.add(connection);
			}
		}
		return list;
	}
	
	public static final Collection<ClientConnection> getProxyConnections() {
		ArrayList<ClientConnection> list = new ArrayList<>();
		for(ClientConnection connection : JavaWebServer.sockets) {
			if(connection.status.isProxyRequest()) {
				list.add(connection);
			}
		}
		return list;
	}
	
	public static final ClientConnection getConnectionFromThread(Thread thread) {//Collection<Thread> threads) {
		if(thread != null) {//if(threads != null) {
			//for(Thread thread : threads) {
			for(ClientConnection connection : JavaWebServer.sockets) {//TODO rename and move "JavaWebServer.sockets" here to "instances". everything's going to break lmao
				if(connection.thread == thread) {
					return connection;
				}
			}
			//}
		}
		return null;
	}
	
	protected static final ConcurrentHashMap<String, ClientCookies> clientCookies = new ConcurrentHashMap<>();
	
	public volatile HTTPClientRequest request = null;
	public volatile HTTPServerResponse response = null;
	
	/*public static final ReusedConn FALSE(Socket socket, HTTPStatusCodes code) {
		return new ReusedConn(socket, false, "", false, code);
	}
	
	@Deprecated
	public static final ReusedConn FALSE = new ReusedConn(null, false, "", false, HTTP_NOT_SET);*/
	
	public static final ClientConnection getNewConn(Socket socket) {
		return new ClientConnection(socket, false, "", null, false, HTTPStatusCodes.HTTP_NOT_SET);
	}
	
	public volatile Socket socket;
	private volatile StringBuilder logs = new StringBuilder();
	private volatile StringBuilder errLogs = new StringBuilder();
	
	private volatile boolean clearLogsBeforeDisplay = false;
	private volatile boolean clearErrLogsBeforeDisplay = false;
	
	private volatile boolean wereLastLogsClearedBeforeDisplay = false;
	private volatile boolean wereLastErrLogsClearedBeforeDisplay = false;
	
	public volatile boolean allowReuse = false;
	public volatile String hostUsed = "";
	public volatile DomainDirectory domainDirectory = null;
	private volatile boolean proxyConnUsed = false;
	private volatile long lastProxyConnTime = -1L;
	private volatile HTTPStatusCodes code = HTTP_NOT_SET;
	
	private volatile long lastCompletedResponse = -1L;
	private volatile long timesConnectionReused = 0L;
	public final ClientStatus status;
	
	public volatile boolean connectionSeemsMalicious = false;
	
	private ClientConnection(Socket socket, boolean reuse, String hostUsed, DomainDirectory domainDirectory, boolean proxyConnUsed, HTTPStatusCodes code) {
		this.thread = Thread.currentThread();
		this.socket = socket;
		this.allowReuse = reuse;
		this.hostUsed = hostUsed;
		this.domainDirectory = domainDirectory;
		this.proxyConnUsed = proxyConnUsed;
		this.setStatusCode(code);
		if(this.proxyConnUsed) {
			this.lastProxyConnTime = System.currentTimeMillis();
		}
		this.status = new ClientStatus(socket, this);
		JavaWebServer.sockets.add(this);
	}
	
	public final void prepareForReuse() {
		this.status.reset(null);
		this.allowReuse = true;
		this.code = HTTP_NOT_SET;
		this.printLogsNow(true);
	}
	
	public final boolean isIncomingConnectionBanned() {
		final String clientAddress = AddressUtil.getClientAddress(this.socket);
		final String clientAddressNoPort = AddressUtil.getClientAddressNoPort(clientAddress);
		//final String clientIP = AddressUtil.getClientAddressNoPort(clientAddress);//clientAddress == null ? null : clientAddress.substring(0, clientAddress.contains(":") ? clientAddress.lastIndexOf(":") - 1 : clientAddress.length());
		BanCheckResult banResult = NaughtyClientData.isIpBanned(clientAddressNoPort, !this.allowReuse, NaughtyClientData.hasBeenUsingProxyConnections(clientAddressNoPort, this));
		if(banResult.isBanned()) {
			NaughtyClientData naughty = banResult.getData();
			String banReason = naughty != null ? naughty.banReason : null;
			if(naughty == null || !naughty.shouldBeBanned()) {
				if(naughty != null) {
					naughty.dispose();
				} else {
					try {
						ConsoleThread.handleInput("sinbin pardon " + clientAddressNoPort, Main.getConsoleThread());
					} catch(IOException ignored) {
					}
				}
			} else {
				this.allowReuse = false;
				try(OutputStream out = this.socket.getOutputStream()) {
					final String response = "<html><body><string>Your external IP address was <b>banned</b> for:</string><br><pre>" + banReason + "</pre><br><string>Too bad.<br>¯\\_(\u30C4)_/¯</string></body></html>";
					out.write(("HTTP/1.1 1337 You got banned!!1\r\nServer: " + JavaWebServer.SERVER_NAME_HEADER + "\r\nDate: " + StringUtil.getCurrentCacheTime() + "\r\nCache-Control: public; max-age=999999999999999(it's ovar 9,000!!1)\r\nConnection: close(that means go away!)\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: " + Integer.toString(response.getBytes(StandardCharsets.UTF_8).length) + "\r\nInfo: If your ip seems to have been banned by mistake, then stop whatever happened to cause this ban result from happening! Also, why are you reading these headers anyway? lmao\r\n\r\n" + response).getBytes(StandardCharsets.UTF_8));
					//       out.write(("HTTP/1.1 403 Forbidden\r\nServer: " + JavaWebServer.SERVER_NAME_HEADER + "\r\nDate: " + StringUtil.getCurrentCacheTime() + "\r\nCache-Control: public; max-age=999999999999999(it's ovar 9,000!!1)\r\nConnection: close(that means go away!)\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: " + Integer.toString(response.getBytes(StandardCharsets.UTF_8).length) + "\r\nInfo: If your ip seems to have been banned by mistake, then stop whatever happened to cause this ban result from happening! Also, why are you reading these headers anyway? lmao\r\n\r\n" + response).getBytes(StandardCharsets.UTF_8));
					out.flush();
				} catch(IOException ignored) {
				}
				boolean println = true;
				naughty.incrementTimesKicked();
				println = naughty.numTimesKickedAfterBan <= 20;
				
				if(println) {
					this.println("[" + StringUtil.longToString(naughty.numTimesKickedAfterBan) + "] " + "Kicked client with ip: " + clientAddressNoPort + "\r\n\tReason: " + (banReason != null && !banReason.trim().isEmpty() ? "\"" + banReason + "\"" : "Unknown(no ban data found!)"));
					PrintUtil.clearLogs();
					PrintUtil.clearErrLogs();
					this.printLogsNow(true);
				}
				this.closeSocket();
				return true;//new RequestResult(this.completeResponse(HTTP_1337, false), null);
			}
		}
		final ClientConnectTime cct = NaughtyClientData.getClientConnectTimeFor(clientAddress);
		final boolean connectionSeemsMalicious = cct.seemsMalicious();
		if(connectionSeemsMalicious) {
			NaughtyClientData.incrementWarnCountFor(clientAddress);
		}
		return false;
	}
	
	public final boolean isReused() {
		return this.timesConnectionReused > 0;
	}
	
	public final boolean equals(Socket socket) {
		return this.socket == socket;
	}
	
	public final ClientConnection completeResponse(HTTPStatusCodes code, boolean reuse) {
		this.lastCompletedResponse = System.currentTimeMillis();
		this.timesConnectionReused++;
		this.allowReuse = reuse;
		this.setStatusCode(code);
		//this.printLogsNow();
		return this;
	}
	
	public final synchronized void appendLog(String str) {
		if(HTTPClientRequest.debug && str.trim().endsWith("*** HTTP/1.1 200 OK")) {
			this.logs.append(StringUtil.throwableToStr(new Throwable(), "\n"));
		}
		this.logs.append(str);
	}
	
	public final synchronized void appendErrLog(String str) {
		this.errLogs.append(str);
	}
	
	public final synchronized void print(String str) {
		this.appendLog(PrintUtil.getResultingPrint(str));
	}
	
	public final synchronized void println(String str) {
		if(str.startsWith("Get: array(0) {")) {
			new Throwable().printStackTrace();
		}
		this.appendLog(PrintUtil.getResultingPrintln(str));
	}
	
	public final synchronized void printDebug(String str) {
		if(HTTPClientRequest.debug) {
			this.print(str);
		}
	}
	
	public final synchronized void printlnDebug(String str) {
		if(HTTPClientRequest.debug) {
			this.println(str);
		}
	}
	
	public final synchronized void printErr(String str) {
		this.appendErrLog(PrintUtil.getResultingPrintErr(str));
	}
	
	public final synchronized void printErrln(String str) {
		this.appendErrLog(PrintUtil.getResultingPrintErrln(str));
	}
	
	public final synchronized void printErrDebug(String str) {
		if(HTTPClientRequest.debug) {
			this.printErr(str);
		}
	}
	
	public final synchronized void printErrlnDebug(String str) {
		if(HTTPClientRequest.debug) {
			this.printErrln(str);
		}
	}
	
	public final synchronized void print(String str, Thread t) {
		this.appendLog(PrintUtil.getResultingPrint(str, t));
	}
	
	public final synchronized void println(String str, Thread t) {
		this.appendLog(PrintUtil.getResultingPrintln(str, t));
	}
	
	public final synchronized void printDebug(String str, Thread t) {
		if(HTTPClientRequest.debug) {
			this.print(str, t);
		}
	}
	
	public final synchronized void printlnDebug(String str, Thread t) {
		if(HTTPClientRequest.debug) {
			this.println(str, t);
		}
	}
	
	public final synchronized void printErr(String str, Thread t) {
		this.appendErrLog(PrintUtil.getResultingPrintErr(str, t));
	}
	
	public final synchronized void printErrln(String str, Thread t) {
		this.appendErrLog(PrintUtil.getResultingPrintErrln(str, t));
	}
	
	public final synchronized void printErrDebug(String str, Thread t) {
		if(HTTPClientRequest.debug) {
			this.printErr(str, t);
		}
	}
	
	public final synchronized void printErrlnDebug(String str, Thread t) {
		if(HTTPClientRequest.debug) {
			this.printErrln(str, t);
		}
	}
	
	public final synchronized void printThrowable(Throwable e) {
		this.printThrowable(e, true);
	}
	
	public final synchronized void printThrowable(Throwable e, boolean printStackTraces) {
		this.printErrln(printStackTraces ? StringUtil.throwableToStr(e, "\n") : StringUtil.throwableToStrNoStackTraces(e, "\n"));
	}
	
	public final synchronized void printLogsNow() {
		this.printLogsNow(true);
	}
	
	public final synchronized void printLogsNow(boolean clearLogs) {
		if(this.clearLogsBeforeDisplay) {
			this.logs = new StringBuilder();
			this.wereLastLogsClearedBeforeDisplay = true;
			this.clearLogsBeforeDisplay = false;
		} else {
			this.wereLastLogsClearedBeforeDisplay = false;
		}
		String logs = this.logs.toString();
		
		if(this.clearErrLogsBeforeDisplay) {
			this.errLogs = new StringBuilder();
			this.wereLastErrLogsClearedBeforeDisplay = true;
			this.clearErrLogsBeforeDisplay = false;
		} else {
			this.wereLastErrLogsClearedBeforeDisplay = false;
		}
		String errLogs = this.errLogs.toString();
		
		if(!logs.isEmpty()) {
			PrintUtil.printlnToWriter(logs);
		}
		if(!errLogs.isEmpty()) {
			PrintUtil.printErrlnToWriter(errLogs);
		}
		if(clearLogs) {
			this.clearAllLogsNow();
		}
	}
	
	public final synchronized void clearLogsNow() {
		this.logs = new StringBuilder();
		this.clearLogsBeforeDisplay = false;
	}
	
	public final synchronized void clearErrLogsNow() {
		this.errLogs = new StringBuilder();
		this.clearErrLogsBeforeDisplay = false;
	}
	
	public final synchronized void clearAllLogsNow() {
		this.logs = new StringBuilder();
		this.errLogs = new StringBuilder();
		this.clearLogsBeforeDisplay = false;
		this.clearErrLogsBeforeDisplay = false;
	}
	
	public final void clearAllLogsBeforeDisplay() {
		this.clearLogsBeforeDisplay();
		this.clearErrLogsBeforeDisplay();
	}
	
	public final void unclearAllLogsBeforeDisplay() {
		this.unclearLogsBeforeDisplay();
		this.unclearErrLogsBeforeDisplay();
	}
	
	public final void clearLogsBeforeDisplay() {
		this.clearLogsBeforeDisplay = true;
	}
	
	public final void clearErrLogsBeforeDisplay() {
		this.clearErrLogsBeforeDisplay = true;
	}
	
	public final void unclearLogsBeforeDisplay() {
		this.clearLogsBeforeDisplay = false;
	}
	
	public final void unclearErrLogsBeforeDisplay() {
		this.clearErrLogsBeforeDisplay = false;
	}
	
	public final boolean willLogsBeClearedBeforeDisplay() {
		return this.clearLogsBeforeDisplay;
	}
	
	public final boolean willErrLogsBeClearedBeforeDisplay() {
		return this.clearErrLogsBeforeDisplay;
	}
	
	public final boolean wereLastLogsClearedBeforeDisplay() {
		return this.wereLastLogsClearedBeforeDisplay;
	}
	
	public final boolean wereLastErrLogsClearedBeforeDisplay() {
		return this.wereLastErrLogsClearedBeforeDisplay;
	}
	
	public final long getLastCompletedResponseTime() {
		return this.lastCompletedResponse;
	}
	
	public final HTTPStatusCodes getStatusCode() {
		return this.code;
	}
	
	public final ClientConnection setStatusCode(HTTPStatusCodes code) {
		this.code = code == null ? HTTPStatusCodes.HTTP_NOT_SET : code;
		return this;
	}
	
	public final void checkStatusCode() {
		if(this.code == HTTP_NOT_SET) {
			this.closeSocket();
			throw new IllegalStateException(HTTP_NOT_SET.toString());
		}
		if(!JavaWebServer.sockets.contains(this)) {
			JavaWebServer.sockets.add(this);
		}
		this.status.setIncoming(true);
	}
	
	public final boolean wasProxyConnectionUsed() {
		return this.proxyConnUsed;
	}
	
	public final ClientConnection setProxyConnectionUsed(boolean proxyConnUsed) {
		this.proxyConnUsed = proxyConnUsed;
		if(!proxyConnUsed) {
			this.lastProxyConnTime = -1L;
		} else {
			this.lastProxyConnTime = System.currentTimeMillis();
		}
		return this;
	}
	
	public final long getLastTimeProxyConnectionUsed() {
		return this.lastProxyConnTime;
	}
	
	/** @param reusedConn */
	/*public void copyTo(ReusedConn reusedConn) {
		reusedConn.reuse = this.reuse;
		reusedConn.socket = this.socket;
		reusedConn.hostUsed = this.hostUsed;
		reusedConn.proxyConnUsed = this.proxyConnUsed;
		reusedConn.lastProxyConnTime = this.lastProxyConnTime;
		reusedConn.code = this.code;
	}*/
	
	public final void closeSocket() {
		if(this.socket != null) {
			try {
				this.socket.close();
			} catch(Throwable ignored) {
			}
			this.socket = null;
		}
		if(this.status.isCancellable()) {
			this.status.cancel();
		}
	}
	
	@Override
	public final String toString() {
		return "ClientConnection[allowRe-use: " + this.allowReuse + "; hostUsed: " + this.hostUsed + "; proxyConnUsed: " + this.proxyConnUsed + "; lastProxyConnTime: " + StringUtil.longToString(this.lastProxyConnTime) + "; HTTPStatusCode: " + this.code + "]";
	}
	
}
