package com.gmail.br45entei.server.data;

import java.io.ByteArrayOutputStream;

/** @author Brian_Entei */
public class DisposableByteArrayOutputStream extends ByteArrayOutputStream {
	
	/** Wipes this ByteArrayOutputStream's byte[] array and resets the counter */
	public final void dispose() {
		this.buf = new byte[0];
		this.count = 0;
		System.gc();
	}
	
}
