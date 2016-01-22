package com.gmail.br45entei.server;

import java.io.IOException;

public final class RequestResult {
	private HTTPClientRequest	request;
	public final ReusedConn		reuse;
	public final IOException	exception;
	
	public RequestResult(HTTPClientRequest request, ReusedConn reuse, IOException exception) {
		this.request = request;
		this.reuse = reuse;
		this.exception = exception;
	}
	
	public final HTTPClientRequest getRequest() {
		return this.request;
	}
	
	/** @return Whether or not the request was a proxy request */
	public final boolean wasProxyRequest() {
		return this.request != null ? this.request.isProxyRequest : false;
	}
	
	public final void markCompleted() {
		if(this.request != null) {
			this.request.markCompleted();
			this.request = null;
			System.gc();
		}
	}
	
}
