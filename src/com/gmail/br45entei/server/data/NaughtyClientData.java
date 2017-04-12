package com.gmail.br45entei.server.data;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public class NaughtyClientData {
	
	public final String clientIp;
	public volatile long inSinBinUntil = 0L;
	public volatile String banReason = "";
	public volatile int warnCount = 0;
	public volatile long numTimesKickedAfterBan = 0;
	
	public volatile boolean sameIpHasBeenUsingProxyConnections = false;
	
	//Temporary variables; to be lost on restart =======
	
	public volatile int numTimesBlankAuthorizationUsed = 0;
	
	//==================================================
	
	private final ConcurrentLinkedDeque<FailedAuthentication> failedAuthentications = new ConcurrentLinkedDeque<>();
	
	public NaughtyClientData(String ip) {
		for(NaughtyClientData naughty : new ArrayList<>(JavaWebServer.sinBin)) {
			if(naughty.clientIp.equalsIgnoreCase(ip)) {
				throw new IllegalStateException("Cannot create more than one instantation of NaughtyClientData for a given ip!");
			}
		}
		this.clientIp = ip;
	}
	
	public final void logFailedAuthentication(String username, String password) {
		FailedAuthentication failedAuth = new FailedAuthentication(username, password);
		this.failedAuthentications.addLast(failedAuth);
	}
	
	public final int getNumSuperFastAuthTimes() {
		ArrayList<FailedAuthentication> failedAuths = new ArrayList<>(this.failedAuthentications);
		failedAuths.sort(FailedAuthentication.comparator);
		long lastFailedTime = -1;
		int numSuperFastAuthTimes = 0;
		for(FailedAuthentication failedAuth : failedAuths) {
			if(lastFailedTime == -1) {
				lastFailedTime = failedAuth.authenticationTime;
				continue;
			}
			if(failedAuth.authenticationTime - lastFailedTime > 1500L) {
				numSuperFastAuthTimes++;
			}
			lastFailedTime = failedAuth.authenticationTime;
		}
		return numSuperFastAuthTimes;
	}
	
	public final int getNumFailedAuthentications() {
		return this.failedAuthentications.size();
	}
	
	public final boolean failedAuthenticationsSeemMalicious() {
		final int numOfSuperFastAuthTimesRequired = 20;
		return this.getNumSuperFastAuthTimes() >= numOfSuperFastAuthTimesRequired;
	}
	
	public final boolean shouldBeBanned() {
		if(!this.canBeBanned()) {
			return false;
		}
		return this.inSinBinUntil != 0L && this.banReason != null && !this.banReason.trim().isEmpty();
	}
	
	public final boolean canBeBanned() {
		//TODO ip white-listing can be added here later too!
		return this.sameIpHasBeenUsingProxyConnections && !JavaWebServer.proxyEnableSinBinConnectionLimit;
	}
	
	public final boolean isBanned() {
		if(this.inSinBinUntil == -1L) {// && (this.banReason != null && !this.banReason.isEmpty())) {
			return true;
		}
		if(this.inSinBinUntil > 0L) {
			long timeLeftUntilBanLift = System.currentTimeMillis() - this.inSinBinUntil;
			if(timeLeftUntilBanLift > 0) {
				return true;
			}
		}
		if(!this.canBeBanned()) {
			return false;
		}
		return this.warnCount > 3;
	}
	
	private final void readProperty(String property) {
		if(property.trim().isEmpty() || property.startsWith("#")) {
			return;
		}
		String[] entry = property.split(Pattern.quote("="));
		if(entry.length >= 2) {
			String pname = entry[0];
			String pvalue = StringUtils.stringArrayToString(entry, '=', 1);
			pvalue = pvalue.equals("null") ? "" : pvalue;
			if(pname.equalsIgnoreCase("bannedUntil")) {
				if(StringUtils.isStrLong(pvalue)) {
					this.inSinBinUntil = Long.valueOf(pvalue).longValue();
				}
			} else if(pname.equalsIgnoreCase("banReason")) {
				this.banReason = pvalue;
			} else if(pname.equalsIgnoreCase("warnCount")) {
				if(StringUtils.isStrLong(pvalue)) {
					this.warnCount = Long.valueOf(pvalue).intValue();
				}
			} else if(pname.equalsIgnoreCase("numTimesKickedAfterBan")) {
				if(StringUtils.isStrLong(pvalue)) {
					this.numTimesKickedAfterBan = Long.parseLong(pvalue);
				}
			}
		}
		if(this.banReason.trim().isEmpty() && this.inSinBinUntil == -1L) {
			this.dispose(false);
		}
	}
	
	public final void loadFromFile(File file) {
		if(file.exists() && file.isFile()) {
			String data = FileUtil.readFileBR(file);
			String[] split = data.split(Pattern.quote("\r\n"));
			this.inSinBinUntil = 0L;
			this.banReason = "";
			this.warnCount = 0;
			this.numTimesKickedAfterBan = 0;
			this.failedAuthentications.clear();
			int numFailedAuthentications = -1;
			int numFailedAuthenticationsRead = 0;
			ArrayList<FailedAuthentication> failedAuthentications = new ArrayList<>();
			for(String property : split) {
				if(this.isDisposed()) {
					return;
				}
				if(numFailedAuthentications == -1 || numFailedAuthentications == numFailedAuthenticationsRead) {
					if(property.startsWith("failedAuthentications: ")) {
						String getNumFailedAuths = property.substring("failedAuthentications: ".length());
						if(!StringUtil.isStrInt(getNumFailedAuths)) {
							return;
						}
						numFailedAuthentications = Integer.parseInt(getNumFailedAuths);
						continue;
					}
					this.readProperty(property);
					if(this.isDisposed()) {
						return;
					}
				} else {//(we're reading failed authentication lines now)
					FailedAuthentication failedAuth = FailedAuthentication.readFrom(property);
					if(failedAuth == null) {
						this.readProperty(property);//did someone edit the file manually and maybe stick a 'normal' property in the middle of the failed authentications?
						continue;
					}
					numFailedAuthenticationsRead++;
					if(!failedAuthentications.contains(failedAuth)) {
						failedAuthentications.add(failedAuth);
					}
				}
			}
			if(!failedAuthentications.isEmpty()) {
				Collections.sort(failedAuthentications, FailedAuthentication.comparator);
				this.failedAuthentications.addAll(failedAuthentications);
			}
			if(this.banReason == null || this.banReason.trim().isEmpty()) {
				this.dispose(true);
			}
		}
	}
	
	public static final String getClientIPFromFileName(File file) {
		if(file != null && file.getName().endsWith(".txt")) {
			return StringUtil.undoFilesystemSafeReplaced(FilenameUtils.getBaseName(file.getName()));
		}
		return null;
	}
	
	public final String getFileName() {
		return StringUtil.makeStringFilesystemSafeReplaced(this.clientIp) + ".txt";
	}
	
	public final boolean saveToFile() {
		File file = new File(new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName), this.getFileName());
		try(PrintStream pr = new PrintStream(new FileOutputStream(file), true)) {
			pr.println("bannedUntil=" + StringUtil.longToString(this.inSinBinUntil));
			pr.println("banReason=" + this.banReason);
			pr.println("warnCount=" + this.warnCount);
			pr.println("numTimesKickedAfterBan=" + StringUtil.longToString(this.numTimesKickedAfterBan));
			if(!this.failedAuthentications.isEmpty()) {
				final int numSuperFastAuthTimes = this.getNumSuperFastAuthTimes();
				if(numSuperFastAuthTimes > 0) {
					pr.println("");
					pr.println("# This client has attempted to authenticate \"super-fast\" " + numSuperFastAuthTimes + " time" + (numSuperFastAuthTimes == 1 ? "" : "s") + " according to the data below:");
				}
				pr.println("failedAuthentications: " + this.failedAuthentications.size());
				for(FailedAuthentication failedAuth : this.failedAuthentications) {
					failedAuth.saveTo(pr);
				}
			}
			pr.flush();
			return true;
		} catch(IOException e) {
			LogUtils.error("Unable to save banned client data:", e);
			return false;
		}
	}
	
	public final boolean deleteSaveFile() {
		File file = new File(new File(JavaWebServer.rootDir, JavaWebServer.sinBinFolderName), this.getFileName());
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
	
	/** Pardons this banned client */
	public int[] dispose() {
		return this.dispose(true);
	}
	
	/** Pardons this banned client */
	public int[] dispose(boolean purge) {
		int[] rtrn = purge ? JavaWebServer.purgeDeadClientIpConnectionsFromSocketList(this.clientIp) : new int[] {0, 0, 0};
		try {
			JavaWebServer.sinBin.remove(this);
			this.deleteSaveFile();
			this.inSinBinUntil = 0L;
			this.banReason = "";
			this.warnCount = 0;
			this.numTimesKickedAfterBan = 0;
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return rtrn;
	}
	
	public final boolean isDisposed() {
		return !JavaWebServer.sinBin.contains(this);
	}
	
	public static final class FailedAuthentication implements Comparable<FailedAuthentication> {
		
		public static final Comparator<FailedAuthentication> comparator = new Comparator<FailedAuthentication>() {
			@Override
			public int compare(FailedAuthentication failedAuth1, FailedAuthentication failedAuth2) {
				return failedAuth1.compareTo(failedAuth2);
			}
		};
		
		public final long authenticationTime;
		public final String credsUsed;
		
		private FailedAuthentication(long authenticationTime, String credsUsed) {
			this.authenticationTime = authenticationTime;
			this.credsUsed = credsUsed == null ? "" : credsUsed;
		}
		
		public FailedAuthentication(String username, String password) {
			this.authenticationTime = System.currentTimeMillis();
			this.credsUsed = (username == null ? "" : username) + ":" + (password == null ? "" : password);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (this.authenticationTime ^ (this.authenticationTime >>> 32));
			result = prime * result + ((this.credsUsed == null) ? 0 : this.credsUsed.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(!(obj instanceof FailedAuthentication)) {
				return false;
			}
			FailedAuthentication other = (FailedAuthentication) obj;
			if(this.authenticationTime != other.authenticationTime) {
				return false;
			}
			if(this.credsUsed == null) {
				if(other.credsUsed != null) {
					return false;
				}
			} else if(!this.credsUsed.equals(other.credsUsed)) {
				return false;
			}
			return true;
		}
		
		public final void saveTo(PrintStream pr) {
			pr.println("FailedAuthentication[" + this.authenticationTime + "=" + this.credsUsed + "]");
			pr.flush();
		}
		
		public static final FailedAuthentication readFrom(String line) {
			if(line == null || line.trim().isEmpty() || !line.startsWith("FailedAuthentication[") || !line.endsWith("]") || !line.contains("=")) {
				return null;
			}
			line = line.substring("FailedAuthentication[".length(), line.length() - 1);
			String[] split = line.split(Pattern.quote("="));
			return StringUtil.isStrLong(split[0]) ? new FailedAuthentication(Long.parseLong(split[0]), StringUtil.stringArrayToString(split, '=', 1)) : null;
		}
		
		@Override
		public int compareTo(FailedAuthentication o) {
			return Long.valueOf(this.authenticationTime - (o == null ? 0 : o.authenticationTime)).intValue();
		}
		
	}
	
	/** @param ip The ip address of the suspected banned client to use
	 * @return The ban status of the given client if it is still listed as
	 *         banned, or null otherwise */
	public static final NaughtyClientData getNaughtyClientDataFor(String ip) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		for(NaughtyClientData naughty : new ArrayList<>(JavaWebServer.sinBin)) {
			if(naughty.clientIp.equalsIgnoreCase(ip)) {
				return naughty;
			}
		}
		return null;
	}
	
	/** Checks if the given client should still be banned before returning the
	 * saved data. If the client is no longer supposed to be banned, it is
	 * pardoned and null is returned.
	 * 
	 * @param ip The ip address of the suspected banned client to use
	 * @return The ban status of the given client if it is still banned, or null
	 *         otherwise. */
	public static final NaughtyClientData getBannedClient(String ip) {
		ip = ip.startsWith("/") ? ip.substring(1) : ip;
		for(NaughtyClientData naughty : new ArrayList<>(JavaWebServer.sinBin)) {
			if(naughty.clientIp.equalsIgnoreCase(ip)) {
				if(naughty.inSinBinUntil == -1L) {
					return naughty;
				}
				if(naughty.warnCount <= 0) {
					JavaWebServer.sinBin.remove(naughty);
					continue;
				}
				long timeLeftUntilBanLift = System.currentTimeMillis() - naughty.inSinBinUntil;
				if(timeLeftUntilBanLift > 0) {
					return naughty;
				}
				return null;
			}
		}
		return null;
	}
	
	public static final NaughtyClientData getOrCreateNewNaughtyClientData(String ip) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		NaughtyClientData naughty = getNaughtyClientDataFor(ip);
		if(naughty == null) {
			naughty = new NaughtyClientData(ip);
			JavaWebServer.sinBin.add(naughty);
		}
		return naughty;
	}
	
	/** @param ip The ip address of the client to check
	 * @return The {@link ClientConnectTime} of the given client. If */
	public static final ClientConnectTime getClientConnectTimeFor(String ip) {
		ClientConnectTime clientData = new ClientConnectTime();
		int numberOfConnections = 1;
		clientData.lastConnectionTime = System.currentTimeMillis();
		for(ClientConnection conn : JavaWebServer.sockets) {
			@SuppressWarnings("resource")
			Socket s = conn == null ? null : conn.socket;
			if(s != null && !s.isClosed()) {
				final String curIp = AddressUtil.getClientAddressNoPort(s);//AddressUtil.getClientAddressNoPort((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
				if(curIp.equalsIgnoreCase(ip)) {
					numberOfConnections++;
				}
			} // else if(conn != null) {
				//	JavaWebServer.removeSocket(conn);
				//}
		}
		clientData.numberOfConnections = numberOfConnections;
		return clientData;
	}
	
	public static final synchronized void incrementWarnCountFor(String ip) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		NaughtyClientData check = NaughtyClientData.getNaughtyClientDataFor(ip);
		if(check != null) {
			ClientConnectTime cct = getClientConnectTimeFor(ip);
			check.inSinBinUntil = System.currentTimeMillis() + 300000 + ((6 - cct.numberOfConnections) * 300000);//300000 = 5 minutes, 3600000 = one hour, etc.
			check.warnCount++;
			if(check.isBanned() && (check.banReason == null || check.banReason.trim().isEmpty())) {
				check.banReason = "Too many connected clients at the same time";
			}
			check.saveToFile();
		} else {
			check = new NaughtyClientData(ip);
			check.warnCount++;
			JavaWebServer.sinBin.add(check);
			check.saveToFile();
		}
	}
	
	public final boolean incrementTimesKicked() {
		this.numTimesKickedAfterBan++;
		return this.saveToFile();
	}
	
	public static final class BanCheckResult {
		
		private final boolean isBanned;
		private final NaughtyClientData data;
		
		public BanCheckResult(NaughtyClientData data, boolean isBanned) {
			this.isBanned = isBanned;
			this.data = data;
		}
		
		public final NaughtyClientData getData() {
			return this.data;
		}
		
		public final boolean isBanned() {
			return this.isBanned;
		}
		
	}
	
	/** @param ip The ip address of the client to check
	 * @param newConnection Whether or not the client is connecting to this
	 *            server for the first time(default value is true)
	 * @return True if the client was already banned, or was just banned as a
	 *         result of checking it's open traffic to this server.<br>
	 *         If the client appears to have more than 20 open connections to
	 *         this server and is attempting another one within 250 milliseconds
	 *         of the last one, it will be banned for 5 minutes for 'dos-ing',
	 *         or 'denial-of-service-ing' this server. This is a work in
	 *         progress, and may ban clients that load multiple files from a
	 *         single webpage normally.(Usually, any one device will only open 6
	 *         concurrent connections to the same server.) */
	public static final BanCheckResult isIpBanned(String ip, boolean newConnection, Boolean hasBeenUsingProxyConnections) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		NaughtyClientData check = NaughtyClientData.getNaughtyClientDataFor(ip);//getBannedClient(ip);
		if(check != null) {
			hasBeenUsingProxyConnections = Boolean.valueOf(check.sameIpHasBeenUsingProxyConnections);
			if(check.isBanned()) {
				return new BanCheckResult(check, true);
			}
		}
		/*if(newConnection) {
			ClientConnectTime clientData = getClientConnectTimeFor(ip);
			if(clientData.seemsMalicious() && (check != null ? check.canBeBanned() : true)) {
				if(hasBeenUsingProxyConnections == Boolean.FALSE) {
					incrementWarnCountFor(ip);
				}
			}
			return isIpBanned(ip, false, hasBeenUsingProxyConnections);
		}
		return new BanCheckResult(check, false);*/
		if(!newConnection && hasBeenUsingProxyConnections == Boolean.FALSE) {
			incrementWarnCountFor(ip);
		}
		check = check != null ? check : NaughtyClientData.getNaughtyClientDataFor(ip);
		return new BanCheckResult(check, check == null ? false : check.isBanned());
	}
	
	public static final void logFailedAuthentication(String ip, String username, String password) {
		ip = AddressUtil.getClientAddressNoPort(ip);
		NaughtyClientData naughty = NaughtyClientData.getOrCreateNewNaughtyClientData(ip);
		if(username.isEmpty() && password.isEmpty()) {
			if(naughty.numTimesBlankAuthorizationUsed++ <= 3) {
				naughty.saveToFile();
				return;
			}
		}
		naughty.logFailedAuthentication(username, password);
		if(naughty.failedAuthenticationsSeemMalicious() && !naughty.isBanned()) {
			naughty.banReason = "Client made 20 or more failed authentication attempts within a short period of time!";
			naughty.inSinBinUntil = System.currentTimeMillis() + 300000 + (naughty.getNumFailedAuthentications() * (3600000 * 18));//3600000 = 1 hour, 18 hours * num times failed to authenticate --> 20 failed authentications times 18 hours = 15 days minimum ban time, so half a month? sure.
		}
		naughty.saveToFile();
	}
	
	/** @param clientAddress
	 * @param reuse
	 * @return */
	public static Boolean hasBeenUsingProxyConnections(String clientAddress, ClientConnection reuse) {
		NaughtyClientData checkNaughty = getNaughtyClientDataFor(clientAddress);//don't use getOrCreate... here!
		Boolean hasBeenUsingProxyConnections = Boolean.FALSE;
		if(reuse.isReused()) {
			if(checkNaughty != null) {
				checkNaughty.sameIpHasBeenUsingProxyConnections |= reuse.wasProxyConnectionUsed();
				if(checkNaughty.sameIpHasBeenUsingProxyConnections) {
					//(math and I don't agree lmao, I'll leave this here): 10 minutes; 1 second = 1000; 60 seconds = 60000; 10 * 60000 = 600000; 600000 = 10 minutes.
					if(System.currentTimeMillis() - reuse.getLastTimeProxyConnectionUsed() > 600000) {
						reuse.setProxyConnectionUsed(false);
						checkNaughty.sameIpHasBeenUsingProxyConnections = false;
					}
				}
			} else {
				checkNaughty = getOrCreateNewNaughtyClientData(clientAddress);
				checkNaughty.sameIpHasBeenUsingProxyConnections = reuse.wasProxyConnectionUsed();
			}
			checkNaughty.saveToFile();
		} else {
			if(checkNaughty == null) {
				checkNaughty = getOrCreateNewNaughtyClientData(clientAddress);
				reuse.setProxyConnectionUsed(false);
				checkNaughty.sameIpHasBeenUsingProxyConnections = false;
			}
		}
		return hasBeenUsingProxyConnections;
	}
	
}
