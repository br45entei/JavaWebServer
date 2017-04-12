package com.gmail.br45entei.server;

import com.gmail.br45entei.swt.dialog.DialogOptions;

import java.util.concurrent.ConcurrentLinkedDeque;

/** @author Brian_Entei */
public final class HTTPClientOptions extends DialogOptions {
	
	public HTTPClientOptions() {
	}
	
	public HTTPClientOptions(HTTPClientOptions copy) {
		this.setFrom(copy);
	}
	
	@Override
	public final void setFrom(Object c) {
		if(c == null || !(c instanceof HTTPClientOptions)) {
			c = new HTTPClientOptions();
		}
		HTTPClientOptions copy = (HTTPClientOptions) c;
		this.clientMustSupplyAUserAgent = copy.clientMustSupplyAUserAgent;
		this.bannedRequestPaths.clear();
		this.bannedRequestPaths.addAll(copy.bannedRequestPaths);
		this.bannedUserAgents.clear();
		this.bannedUserAgents.addAll(copy.bannedUserAgents);
		this.bannedUserAgentWords.clear();
		this.bannedUserAgentWords.addAll(copy.bannedUserAgentWords);
	}
	
	public volatile boolean clientMustSupplyAUserAgent = false;
	
	public final ConcurrentLinkedDeque<String> bannedRequestPaths = new ConcurrentLinkedDeque<>();
	public final ConcurrentLinkedDeque<String> bannedUserAgents = new ConcurrentLinkedDeque<>();
	public final ConcurrentLinkedDeque<String> bannedUserAgentWords = new ConcurrentLinkedDeque<>();
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.bannedRequestPaths == null) ? 0 : stringCollectionToString(this.bannedRequestPaths).hashCode());
		result = prime * result + ((this.bannedUserAgentWords == null) ? 0 : stringCollectionToString(this.bannedUserAgentWords).hashCode());
		result = prime * result + ((this.bannedUserAgents == null) ? 0 : stringCollectionToString(this.bannedUserAgents).hashCode());
		result = prime * result + (this.clientMustSupplyAUserAgent ? 1231 : 1237);
		return result;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof HTTPClientOptions)) {
			return false;
		}
		HTTPClientOptions other = (HTTPClientOptions) obj;
		if(this.clientMustSupplyAUserAgent != other.clientMustSupplyAUserAgent) {
			return false;
		}
		if(!stringCollectionToString(this.bannedRequestPaths).equals(stringCollectionToString(other.bannedRequestPaths))) {
			return false;
		}
		if(!stringCollectionToString(this.bannedUserAgents).equals(stringCollectionToString(other.bannedUserAgents))) {
			return false;
		}
		if(!stringCollectionToString(this.bannedUserAgentWords).equals(stringCollectionToString(other.bannedUserAgentWords))) {
			return false;
		}
		return true;
	}
	
}
