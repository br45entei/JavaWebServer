package com.gmail.br45entei.server.handler;

import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.RequestResult;

import java.io.InputStream;
import java.net.Socket;

/** @author Brian_Entei */
public interface IRequestHandler {
	
	/** @param s The client socket
	 * @param SSL Whether or not this was called from the SSL thread pool
	 * @param listenPort The listen port that was used to accept the client
	 *            socket */
	public void handleIncomingConnection(final Socket s, final boolean SSL, final int listenPort);
	
	/** @param s The client socket
	 * @param in The socket's input stream
	 * @param https Whether or not this request is using SSL
	 * @param client The client object for this request and any additional
	 *            requests using the same connection
	 * @return The result from the request. <b>This cannot be
	 *         {@code null}!!!1</b>
	 * @throws Error
	 * @throws RuntimeException
	 * @throws Throwable */
	default RequestResult HandleRequest(final Socket s, final InputStream in, final boolean https, final ClientConnection client) throws Error, RuntimeException, Throwable {
		return null;
	}
	
}
