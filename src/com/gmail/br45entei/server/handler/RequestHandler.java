package com.gmail.br45entei.server.handler;

import com.gmail.br45entei.server.ClientConnection;

import java.io.IOException;
import java.net.Socket;

/** @author Brian_Entei */
public class RequestHandler implements IRequestHandler {
	
	@Override
	public void handleIncomingConnection(final Socket s, final boolean SSL, final int listenPort) {
		//TODO Finish me once and for all!!!!!!!!!!!!!!!!!11111
		try {
			this.HandleRequest(s, s.getInputStream(), SSL, ClientConnection.getNewConn(s));
		} catch(RuntimeException e) {
			//FileMgmt.LogException(e, "handleIncomingConnection()", "Message to be sent with error", false);
		} catch(IOException e) {
			//FileMgmt.LogException(e, "handleIncomingConnection()", "Message to be sent with error", false);
		} catch(Error e) {
			//FileMgmt.LogException(e, "handleIncomingConnection()", "Message to be sent with error", false);
		} catch(Throwable e) {
			//FileMgmt.LogException(e, "handleIncomingConnection()", "Message to be sent with error", false);
		}
	}
	
}
