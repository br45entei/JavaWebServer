package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author Brian_Entei */
public class RangeRequest {
	public final long startBytes;
	public final long endBytes;
	public final long contentLength;
	public final long rangeLength;
	
	public RangeRequest(long startBytes, long endBytes, long contentLength) {
		this.startBytes = startBytes;
		this.endBytes = endBytes;
		this.rangeLength = (this.endBytes - this.startBytes) + 1;
		this.contentLength = contentLength;
	}
	
	public RangeRequest(final String range, final long contentLength) throws NumberFormatException {
		long startBytes = 0;
		long endBytes = 0;
		if(!range.isEmpty() && JavaWebServer.allowByteRangeRequests) {
			JavaWebServer.printlnDebug("TEST 1: range: \"" + range + "\";");
			String[] rangeSplit = range.replace("bytes=", "").split("-");
			if(rangeSplit.length == 2 || (range.endsWith("-") && rangeSplit.length == 1)) {
				JavaWebServer.printlnDebug("TEST 2: rangeSplit.length: " + rangeSplit.length);
				JavaWebServer.printlnDebug("TEST 3: content length: " + contentLength);
				
				startBytes = StringUtils.getLongFromStr(rangeSplit[0]).longValue();
				JavaWebServer.printlnDebug("TEST 4: start bytes: " + startBytes);
				if(range.endsWith("-")) {
					endBytes = contentLength - 1;
				} else {
					endBytes = StringUtils.getLongFromStr(rangeSplit[1]).longValue();
				}
				JavaWebServer.printlnDebug("TEST 5: end bytes: " + endBytes);
			}
		}
		this.startBytes = startBytes;
		this.endBytes = endBytes;
		this.contentLength = contentLength;
		this.rangeLength = (this.endBytes - this.startBytes) + 1;
	}
	
	/** @return Whether or not this range's byte offsets are valid(startBytes
	 *         must be &gt;= 0, endBytes must be &gt; startBytes, endBytes must
	 *         be &lt; contentLength) */
	public final boolean isValid() {
		return (this.startBytes >= 0 && this.startBytes < this.contentLength) && (this.endBytes > this.startBytes && this.endBytes < this.contentLength);
	}
	
	/** @param input The input stream to read from
	 * @param output The output stream to write to
	 * @param status The {@link ClientInfo} that will be updated as data is
	 *            copied over(<b>Must not be {@code null}!</b>)
	 * @return The number of written bytes, or <code>-1</code> if
	 *         {@link #isValid()} returns false
	 * @throws IOException Thrown if there was an issue copying data
	 * @author Brian_Entei
	 * @author <a href=
	 *         "http://stackoverflow.com/questions/33181702/java-implement-byte-range-serving-without-servlets-using-only-java-api/33235934#33235934">Whome</a>
	 *         on StackOverflow */
	public final long sendRangeTo(InputStream input, OutputStream output, ClientStatus status) throws IOException {
		if(!this.isValid()) {
			return -1;
		}
		status.setContentLength(this.rangeLength);
		status.markWriteTime();
		final int maxread = 24 * 1024;
		byte[] buffer = new byte[maxread];
		input.skip(this.startBytes);
		long written = 0;
		long remaining = this.rangeLength;
		while(remaining > 0) {
			if(status.isCancelled()) {
				return written;
			}
			status.checkForPause();
			if(status.isCancelled()) {
				return written;
			}
			status.isBeingWrittenTo = true;
			int read = input.read(buffer, 0, (maxread <= remaining) ? maxread : (int) remaining);
			//try {
			output.write(buffer, 0, read);
			output.flush();
			//} catch(IOException e) {
			//	if(JavaWebServer.isExceptionIgnored(e)) {
			//		return written + read;
			//	}
			//	throw e;
			//}
			remaining -= read;
			written += read;
			status.setCount(written);
			status.markWriteTime();
			
			/*final long now = System.currentTimeMillis();
			status.setLastWriteTime(now);
			status.bytesTransfered = written;
			status.isBeingWrittenTo = false;
			long elapsedTime = now - status.lastWriteTime;
			status.currentWriteAmount += read;
			if(elapsedTime >= status.updateTime) {
				status.lastWriteTime = now;
				status.lastWriteAmount = status.currentWriteAmount;
				status.currentWriteAmount = 0L;
			}*/
			if(status.isCancelled()) {
				buffer = null;
				System.gc();
				return written;
			}
		}
		buffer = null;
		System.gc();
		return written;
	}
	
}
