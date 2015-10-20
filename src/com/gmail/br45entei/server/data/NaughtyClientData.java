/**
 * 
 */
package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FileDeleteStrategy;

/** @author Brian_Entei */
public class NaughtyClientData {
	
	public final UUID		uuid;
	public volatile String	clientIp		= "";
	public volatile long	inSinBinUntil	= -1L;
	public volatile String	banReason		= "";
	public volatile int		warnCount		= 0;
	
	public NaughtyClientData(UUID uuid) {
		this.uuid = uuid;
	}
	
	public final boolean isBanned() {
		if(this.inSinBinUntil == -1L) {
			return true;
		}
		long timeLeftUntilBanLift = System.currentTimeMillis() - this.inSinBinUntil;
		if(timeLeftUntilBanLift > 0) {
			return true;
		}
		return this.warnCount > 3;
	}
	
	public final void loadFromFile(File file) {
		if(file.exists() && file.isFile()) {
			String data = FileUtil.readFile(file);
			String[] split = data.split(Pattern.quote("\r\n"));
			for(String property : split) {
				String[] entry = property.split(Pattern.quote("="));
				if(entry.length >= 2) {
					String pname = entry[0];
					String pvalue = StringUtil.stringArrayToString(entry, '=', 1);
					if(pname.equalsIgnoreCase("ipAddress")) {
						this.clientIp = pvalue;
					} else if(pname.equalsIgnoreCase("bannedUntil")) {
						if(StringUtil.isStrLong(pvalue)) {
							this.inSinBinUntil = Long.valueOf(pvalue).longValue();
						}
					} else if(pname.equalsIgnoreCase("banReason")) {
						this.banReason = pvalue;
					} else if(pname.equalsIgnoreCase("warnCount")) {
						if(StringUtil.isStrLong(pvalue)) {
							this.warnCount = Long.valueOf(pvalue).intValue();
						}
					}
				}
			}
		}
	}
	
	public final boolean saveToFile() {
		File file = new File(new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName), this.uuid.toString() + ".txt");
		try(PrintStream pr = new PrintStream(new FileOutputStream(file), true)) {
			pr.println("ipAddress=" + this.clientIp);
			pr.println("bannedUntil=" + this.inSinBinUntil);
			pr.println("banReason=" + this.banReason);
			pr.println("warnCount=" + this.warnCount);
			return true;
		} catch(IOException e) {
			LogUtils.error("Unable to save banned client data:", e);
			return false;
		}
	}
	
	public final boolean deleteSaveFile() {
		File file = new File(new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName), this.uuid.toString() + ".txt");
		if(file.exists()) {
			try {
				FileDeleteStrategy.FORCE.delete(file);
				return true;
			} catch(Throwable ignored) {
				file.deleteOnExit();
				return false;
			}
		}
		return true;
	}
	
}
