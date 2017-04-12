package com.gmail.br45entei.server.data.file;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.fileupload.UploadContext;

/** @author Brian_Entei */
public final class JavaWebServerRequestContext implements UploadContext {
	
	private final byte[] postRequestData;
	private final Charset charset;
	private final String contentType;
	
	/** @param postRequestData The POST request data
	 * @param charset The charset used to read the request(usually UTF-8)
	 * @param contentType The request data's content type(HTTP header value) */
	public JavaWebServerRequestContext(byte[] postRequestData, Charset charset, String contentType) {
		this.postRequestData = postRequestData;
		this.charset = charset;
		this.contentType = contentType;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getCharacterEncoding() {
		return this.charset.name();
	}
	
	/** {@inheritDoc} */
	@Override
	public String getContentType() {
		return this.contentType;
	}
	
	/** {@inheritDoc} */
	@Override
	public int getContentLength() {
		return this.postRequestData.length;
	}
	
	/** {@inheritDoc} */
	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.postRequestData);
	}
	
	/** {@inheritDoc} */
	@Override
	public long contentLength() {
		return this.getContentLength();
	}
	
	/** Returns a string representation of this object.
	 *
	 * @return a string representation of this object. */
	@Override
	public String toString() {
		return format("ContentLength=%s, ContentType=%s", Long.valueOf(this.contentLength()), this.getContentType());
	}
	
}
