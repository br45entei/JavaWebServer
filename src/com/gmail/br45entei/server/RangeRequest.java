package com.gmail.br45entei.server;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.util.StringUtil;

/** @author Brian_Entei */
public class RangeRequest {
	public final long	startBytes;
	public final long	endBytes;
	public final long	contentLength;
	public final long	rangeLength;
	
	public RangeRequest(final String range, final long contentLength) throws NumberFormatException {
		long startBytes = 0;
		long endBytes = 0;
		if(!range.isEmpty() && JavaWebServer.allowByteRangeRequests) {
			JavaWebServer.printlnDebug("TEST 1: range: \"" + range + "\";");
			String[] rangeSplit = range.replace("bytes=", "").split("-");
			if(rangeSplit.length == 2 || (range.endsWith("-") && rangeSplit.length == 1)) {
				JavaWebServer.printlnDebug("TEST 2: rangeSplit.length: " + rangeSplit.length);
				JavaWebServer.printlnDebug("TEST 3: content length: " + contentLength);
				
				startBytes = StringUtil.getLongFromStr(rangeSplit[0]).longValue();
				JavaWebServer.printlnDebug("TEST 4: start bytes: " + startBytes);
				if(range.endsWith("-")) {
					endBytes = contentLength - 1;
				} else {
					endBytes = StringUtil.getLongFromStr(rangeSplit[1]).longValue();
				}
				JavaWebServer.printlnDebug("TEST 5: end bytes: " + endBytes);
			}
		}
		this.startBytes = startBytes;
		this.endBytes = endBytes;
		this.contentLength = contentLength;
		this.rangeLength = (this.endBytes - this.startBytes) + 1;
	}
	
	public final boolean isValid() {
		return (this.startBytes >= 0 && this.startBytes < this.contentLength) && (this.endBytes > this.startBytes && this.endBytes <= this.contentLength);
	}
	
}
