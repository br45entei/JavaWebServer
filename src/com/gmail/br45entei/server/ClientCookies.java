package com.gmail.br45entei.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

/** @author Brian_Entei */
public class ClientCookies {
	
	public final String ip;
	public final ConcurrentLinkedDeque<Header> cookies = new ConcurrentLinkedDeque<>();
	public final ConcurrentLinkedDeque<Header> setCookies = new ConcurrentLinkedDeque<>();
	
	public ClientCookies(String ip, Collection<Header> cookies) {
		this.ip = ip;
		this.cookies.addAll(Header.collectClientCookies(cookies));
		this.setCookies.addAll(Header.collectSetCookies(cookies));
		//that way it filters out any other non-cookie junk lol
		//hmm wait...
		//there's cookies that the client gets from the server
		//then there's cookies that php gives to the server to give to the client.... ohhhhh
		// so I listen to those set-cookie headers and, hmm wait, hmm
		//waiting.. lol i back with food :D ah nice XD
		//one sec lemme close door xD
		//ok ok there XD
		//anyway, back to my confused state XD
		//hmmmm 'cause I don't need to save the cookies that the client already has right? I mean... /confuse XD
		//oh! so
		//so hmm, so I save the cookie headers that the client presents to my server, but then how do I pass cookies to php cgi?
		//hmmm
	}
	
}
