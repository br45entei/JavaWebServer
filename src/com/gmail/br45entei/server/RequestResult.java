package com.gmail.br45entei.server;

import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.exception.TimeoutException;

public final class RequestResult {
	public final ClientConnection connection;
	public final Throwable exception;
	
	private volatile boolean printLogs = false;
	
	public RequestResult(ClientConnection reuse, Throwable exception) {
		if(reuse == null) {
			throw new NullPointerException("ClientConnection reuse cannot be null!");
		}
		this.connection = reuse;
		this.exception = exception;
		/*if(this.response != null) {
			if(this.response.getStatusMessage() != null && !reuse.wereLastLogsClearedBeforeDisplay()) {
				reuse.println(this.response.getStatusMessage());
				reuse.printLogsNow(true);
			}
		}*/
	}
	
	private volatile StackTraceElement[] stackTrace = null;
	private volatile long lastQuery = -1L;
	
	public final String getLogHTTPLine() {
		if(this.lastQuery == -1L) {// || System.currentTimeMillis() - this.lastQuery >= 100L) {
			this.lastQuery = System.currentTimeMillis();
			this.stackTrace = Thread.currentThread().getStackTrace();
		} else {
			this.connection.println("Duplication detected!\n" + StringUtil.stackTraceElementsToStr(this.stackTrace, "\n"));
			this.stackTrace = Thread.currentThread().getStackTrace();
			this.connection.println("Deja Vu:\n" + StringUtil.stackTraceElementsToStr(this.stackTrace, "\n"));
		}
		boolean success = this.connection.getStatusCode().name().startsWith("HTTP_2");
		boolean redirect = this.connection.getStatusCode().name().startsWith("HTTP_3");
		boolean clientError = this.connection.getStatusCode().name().startsWith("HTTP_4");
		boolean serverError = this.connection.getStatusCode().name().startsWith("HTTP_5");
		//boolean bannedClient = result.reuse.getStatusCode().name().equals("HTTP_1337"); //lmao
		String desc;
		if(success) {
			desc = "Success";
		} else if(redirect) {
			desc = "Redirect";
		} else if(clientError) {
			desc = "Client Error";
		} else if(serverError) {
			desc = "Server/Network Error";
		} else {
			desc = "???";
		}
		return "\t*** " + this.getRequest().version + " " + this.connection.getStatusCode().toString() + "(" + desc + ")" + (!this.getRequest().isFinished() || !this.getRequest().clientRequestAccepted() ? (this.exception == null || !(this.exception instanceof TimeoutException) ? " - " + StringUtil.throwableToStrNoStackTraces(this.exception, "\n") : " - [" + this.exception.getMessage() + "]") : (this.getResponse() == null ? " - [No response given to client?]" : (this.getResponse().getStatusMessage() == null || this.getResponse().getStatusMessage().trim().isEmpty() ? "" : " - " + this.getResponse().getStatusMessage()).trim()));
	}
	
	public final HTTPClientRequest getRequest() {
		return this.connection.request;
	}
	
	public final HTTPServerResponse getResponse() {
		return this.connection.response;
	}
	
	/** @return Whether or not the request was a proxy request */
	public final boolean wasProxyRequest() {
		return this.connection.request != null ? this.connection.request.isProxyRequest : false;
	}
	
	/*@SuppressWarnings("unused")
	private*/public final void markCompleted1(boolean saveLog) {
		if(this.connection.request != null) {
			this.connection.request.setLogResult("Request ended normally at: " + StringUtil.getSystemTime(false, false, true));
			this.connection.request.markCompleted(saveLog);
			this.connection.request = null;
			System.gc();
		}
	}
	
	public final RequestResult setPrintLogs(boolean printLogs) {
		this.printLogs = printLogs;
		return this;
	}
	
	public final boolean printLogs() {
		return this.printLogs;
	}
	
	@Override
	public final String toString() {
		return (this.connection.request == null ? "[null request]" : "Request: " + this.connection.request.toString()) + "\r\n" + //
				(this.connection == null ? "[null reuse]" : "Reuse: " + this.connection.toString()) + "\r\n" + //
				(this.connection.response == null ? "[null response]" : "Response: " + this.connection.response.toString()) + "\r\n" + //
				(this.exception == null ? "[null exception]" : "Exception: " + StringUtil.throwableToStrNoStackTraces(this.exception)) + "\r\n" + //
				"printLogs: " + this.printLogs;
	}
	
	/** @param reusedConn */
	/*public void setReusedConnFrom(ReusedConn reusedConn) {
		this.reuse.socket = reusedConn.socket;
		this.reuse.copyTo(reusedConn);//take the goodies from this request result and apply them to the result that's stored in the sockets array in JavaWebServer, but keep the original socket variable's value! confusing!
		reusedConn.checkStatusCode();
	}*/
	
}
