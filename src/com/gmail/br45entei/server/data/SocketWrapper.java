package com.gmail.br45entei.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/** @author Brian_Entei */
public class SocketWrapper extends Socket {
	
	private final Socket		socket;
	private InputStreamReader	inWrapper;
	
	public SocketWrapper(Socket socket) throws IOException {
		this.socket = socket;
		this.inWrapper = new InputStreamReader(this.socket.getInputStream());
	}
	
	@Override
	public final InputStream getInputStream() {
		return this.inWrapper;
	}
	
	public final void insertByte(int b) {
		this.inWrapper.addByteToInternalBuffer(b);
	}
	
	public final void insertBytes(byte[] bytes) {
		this.inWrapper.addBytesToInternalBuffer(bytes);
	}
	
	public final void dispose() {
		if(this.inWrapper == null) {
			return;
		}
		this.inWrapper.dispose();
		this.inWrapper = null;
		System.gc();
	}
	
	//'Pass-through' methods:
	
	@Override
	public final synchronized void close() throws IOException {
		this.dispose();
		this.socket.close();
	}
	
	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		this.socket.connect(endpoint);
	}
	
	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		this.socket.connect(endpoint, timeout);
	}
	
	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		this.socket.bind(bindpoint);
	}
	
	@Override
	public InetAddress getInetAddress() {
		return this.socket.getInetAddress();
	}
	
	@Override
	public InetAddress getLocalAddress() {
		return this.socket.getLocalAddress();
	}
	
	@Override
	public int getPort() {
		return this.socket.getPort();
	}
	
	@Override
	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
	
	@Override
	public SocketAddress getRemoteSocketAddress() {
		return this.socket.getRemoteSocketAddress();
	}
	
	@Override
	public SocketAddress getLocalSocketAddress() {
		return this.socket.getLocalSocketAddress();
	}
	
	@Override
	public SocketChannel getChannel() {
		return this.socket.getChannel();
	}
	
	@Override
	public final OutputStream getOutputStream() throws IOException {
		return this.socket.getOutputStream();
	}
	
	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		this.socket.setTcpNoDelay(on);
	}
	
	@Override
	public boolean getTcpNoDelay() throws SocketException {
		return this.socket.getTcpNoDelay();
	}
	
	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {
		this.socket.setSoLinger(on, linger);
	}
	
	@Override
	public int getSoLinger() throws SocketException {
		return this.socket.getSoLinger();
	}
	
	@Override
	public void sendUrgentData(int data) throws IOException {
		this.socket.sendUrgentData(data);
	}
	
	@Override
	public void setOOBInline(boolean on) throws SocketException {
		this.socket.setOOBInline(on);
	}
	
	@Override
	public boolean getOOBInline() throws SocketException {
		return this.socket.getOOBInline();
	}
	
	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		this.socket.setSoTimeout(timeout);
	}
	
	@Override
	public synchronized int getSoTimeout() throws SocketException {
		return this.socket.getSoTimeout();
	}
	
	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException {
		this.socket.setSendBufferSize(size);
	}
	
	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		return this.socket.getSendBufferSize();
	}
	
	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		this.socket.setReceiveBufferSize(size);
	}
	
	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		return this.socket.getReceiveBufferSize();
	}
	
	@Override
	public void setKeepAlive(boolean on) throws SocketException {
		this.socket.setKeepAlive(on);
	}
	
	@Override
	public boolean getKeepAlive() throws SocketException {
		return this.socket.getKeepAlive();
	}
	
	@Override
	public void setTrafficClass(int tc) throws SocketException {
		this.socket.setTrafficClass(tc);
	}
	
	@Override
	public int getTrafficClass() throws SocketException {
		return this.socket.getTrafficClass();
	}
	
	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		this.socket.setReuseAddress(on);
	}
	
	@Override
	public boolean getReuseAddress() throws SocketException {
		return this.socket.getReuseAddress();
	}
	
	@Override
	public void shutdownInput() throws IOException {
		this.socket.shutdownInput();
	}
	
	@Override
	public void shutdownOutput() throws IOException {
		this.socket.shutdownOutput();
	}
	
	@Override
	public String toString() {
		return this.socket.toString();
	}
	
	@Override
	public boolean isConnected() {
		return this.socket.isConnected();
	}
	
	@Override
	public boolean isBound() {
		return this.socket.isBound();
	}
	
	@Override
	public boolean isClosed() {
		return this.socket.isClosed();
	}
	
	@Override
	public boolean isInputShutdown() {
		return this.socket.isInputShutdown();
	}
	
	@Override
	public boolean isOutputShutdown() {
		return this.socket.isOutputShutdown();
	}
	
	@Override
	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
		this.socket.setPerformancePreferences(connectionTime, latency, bandwidth);
	}
	
}
