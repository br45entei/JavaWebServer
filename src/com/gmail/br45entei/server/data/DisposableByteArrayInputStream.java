package com.gmail.br45entei.server.data;

import java.io.ByteArrayInputStream;

/** @author Brian_Entei */
public class DisposableByteArrayInputStream extends ByteArrayInputStream {
	
	public DisposableByteArrayInputStream(byte[] buf) {
		super(buf);
	}
	
	public DisposableByteArrayInputStream(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}
	
	public final void readToBuf(byte[] buf) {
		this.read(buf, 0, buf.length);
	}
	
	/** Wipes this ByteArrayInputStream's byte[] array and resets the counter */
	public final void dispose() {
		this.buf = new byte[0];
		this.pos = 0;
		this.count = 0;
		this.mark = 0;
		System.gc();
	}
	
}
