package com.gmail.br45entei.server.data;

import com.gmail.br45entei.data.DisposableByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

/** @author Brian_Entei */
public class InputStreamReader extends InputStream {
	
	/** The first byte sent when a client connects to a server attempting to use
	 * an TLS(HTTPS over SSL) connection */
	public static final int								sslClientHello			= 0x16;
	private volatile InputStream						source;
	private volatile DisposableByteArrayOutputStream	baos					= new DisposableByteArrayOutputStream();
	
	private volatile int								lastInternallyReadByte	= 0;
	
	//private volatile long								deleteme		= 0L;
	
	/** @param source The InputStream to read from */
	public InputStreamReader(InputStream source) {
		this.source = source;
	}
	
	public final void setSource(InputStream source) {
		this.source = null;
		this.baos.dispose();
		this.source = source;
	}
	
	public final synchronized void addByteToInternalBuffer(int b) {
		this.baos.write(b);
	}
	
	public final synchronized void addBytesToInternalBuffer(byte[] bytes) {
		this.baos.write(bytes, 0, bytes.length);
	}
	
	private final int internalRead() throws IOException {
		if(this.source == null || this.baos == null) {
			return -1;
		}
		int read = this.source.read();
		this.lastInternallyReadByte = read;
		this.baos.write(read);
		return read;
	}
	
	public final int getLastInternallyReadByte() {
		return this.lastInternallyReadByte;
	}
	
	public final boolean isInternalBufferEmpty() {
		if(this.baos == null) {
			return true;
		}
		return this.baos.isEmpty();
	}
	
	/** For convenient use in future implementations of 'InputStreamReader'
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException */
	protected final int internalRead(byte[] b, int off, int len) throws IOException {
		if(b == null) {
			throw new NullPointerException();
		} else if(off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if(len == 0) {
			return 0;
		}
		int c = this.internalRead();
		if(c == -1) {
			return -1;
		}
		b[off] = (byte) c;
		int i = 1;
		for(; i < len; i++) {
			c = this.internalRead();
			if(c == -1) {
				break;
			}
			b[off + i] = (byte) c;
		}
		return i;
	}
	
	/** @param b The byte to check with
	 * @return True if the next int is equal to the given byte(converted to int
	 *         using ' & 0xFF')
	 * @throws IOException Thrown if there was an error reading the next byte
	 *             from the underlying socket */
	public final boolean isNextByte(byte b) throws IOException {
		return this.internalRead() == (b & 0xFF);
	}
	
	/** @param b The byte to check with
	 * @return True if the next byte is equal to the given integer
	 * @throws IOException Thrown if there was an error reading the next byte
	 *             from the underlying socket */
	public final boolean isNextByte(int b) throws IOException {
		return this.internalRead() == b;
	}
	
	/** If the result of this method is {@code false}, then the read byte is
	 * added to the internal buffer.
	 * 
	 * @return True if the next byte is the
	 *         {@link InputStreamReader#sslClientHello}, or '22'
	 * @throws IOException Thrown if there was an error reading the next byte
	 *             from the underlying socket */
	public final boolean isNextByteClientHello() throws IOException {
		final boolean result = this.isNextByte(sslClientHello);
		if(!result) {
			this.baos.dispose();
			this.baos.write(this.lastInternallyReadByte);
		}
		return result;
	}
	
	@Override
	public final synchronized void mark(int readlimit) {
		if(this.source != null) {
			this.source.mark(readlimit);
		}
	}
	
	@Override
	public final synchronized void reset() throws IOException {
		if(this.source != null) {
			this.source.reset();
		}
	}
	
	@Override
	public final boolean markSupported() {
		return this.source.markSupported();
	}
	
	@Override
	public final int available() throws IOException {
		if(this.source == null) {
			return -1;
		}
		return this.source.available();
	}
	
	@Override
	public final int read(byte[] b, int off, int len) throws IOException {
		if(b == null) {
			throw new NullPointerException();
		} else if(off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if(len == 0) {
			return 0;
		}
		
		int c = this.read();
		if(c == -1) {
			return -1;
		}
		b[off] = (byte) c;
		
		int i = 1;
		try {
			for(; i < len; i++) {
				c = this.read();
				if(c == -1) {
					break;
				}
				b[off + i] = (byte) c;
			}
		} catch(IOException ee) {
		}
		return i;
	}
	
	@Override
	public int read() throws IOException {
		if(this.baos == null || this.source == null) {
			return -1;
		}
		if(this.baos.available() == 0 || this.baos.isEmpty()) {
			return this.source.available() > 0 ? this.source.read() : -1;
		}
		//System.out.println("HAY! Num: " + (this.deleteme++) + "; this.baos.available(): " + this.baos.available());
		//return this.baos.read();
		int read = this.baos.read();
		if(read == -1) {
			read = this.source.available() > 0 ? this.source.read() : -1;
		}
		return read;
	}
	
	/** Removes this InputStreamReader's reference to its' source InputStream,
	 * and disposes of this InputStreamReader's internal byte buffer. */
	public final void dispose() {
		this.baos.dispose();
		this.baos = null;
		this.source = null;
		System.gc();
	}
	
	@Override
	public final void close() throws IOException {
		this.dispose();
		this.source.close();
	}
	
}
