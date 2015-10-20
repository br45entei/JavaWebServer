package com.gmail.br45entei.server;

import java.io.IOException;

public final class RequestResult {
	public final HTTPClientRequest	request;
	public final boolean			reuse;
	public final IOException		exception;
	
	public RequestResult(HTTPClientRequest request, boolean reuse, IOException exception) {
		this.request = request;
		this.reuse = reuse;
		this.exception = exception;
	}
	
	/** @return Whether or not the request was a proxy request */
	public final boolean wasProxyRequest() {
		return this.request != null ? this.request.isProxyRequest : false;
	}
	
}
