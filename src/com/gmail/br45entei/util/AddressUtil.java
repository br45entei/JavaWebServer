package com.gmail.br45entei.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/** @author Brian_Entei */
public class AddressUtil {
	
	private static String	ipAddress	= null;
	
	public static final String getValidHostFor(String host) {
		if(host == null || host.trim().isEmpty()) {
			return getIp();
		}
		String port = "";
		if(host.contains(":")) {
			if(!host.contains("]")) {
				port = host.substring(host.lastIndexOf(":"), host.length());
				host = host.substring(0, host.lastIndexOf(":"));
			} else {
				/*port = host.substring(host.indexOf("]"), host.length());
				if(port.startsWith("]")) {
					port = port.substring(1);
				}
				host = host.substring(0, host.indexOf("]") + 1);*/
				return host/* + port*/;
			}
		}
		boolean isHostValid;
		try {
			isHostValid = InetAddress.getByName(host) != null;
		} catch(Throwable ignored) {
			isHostValid = false;
		}
		if(host.isEmpty() || !isHostValid) {
			host = getIp();
		}
		return host + port;
	}
	
	/** @param ip The Ipv4 address to convert
	 * @return The converted address in Ipv6, or the original ip if the
	 *         conversion failed. */
	public static final String convertIpv4ToIpv6(String ip) {
		if(ip == null || ip.isEmpty()) {
			return ip;
		}
		String port = "";
		if(ip.contains(":")) {
			port = ip.substring(ip.indexOf(":"));
			ip = ip.substring(0, ip.indexOf(":"));
		}
		String[] octets = ip.split("\\.");
		if(octets.length == 4) {
			final String s1 = Integer.toHexString(Integer.parseInt(octets[0])) + convertIntToHex(Integer.parseInt(octets[1]));
			final String s2 = Integer.toHexString(Integer.parseInt(octets[2])) + convertIntToHex(Integer.parseInt(octets[3]));
			return "[::ffff:" + (s1.startsWith("00") ? s1.substring(2) : s1) + ":" + (s2.startsWith("00") ? s2.substring(2) : s2) + "]" + port;
		}
		return ip;
	}
	
	public static final String getClientAddress(String address) {
		if(address == null || address.isEmpty()) {
			return "";
		}
		if(address.contains(":")) {
			int index1 = address.indexOf(":");
			int index2 = address.lastIndexOf(":");
			if(index1 == index2) {
				return address;//IPv4
			}
			return ("[" + address.substring(0, index2) + "]" + address.substring(index2, address.length())).replace("%10", "").replace("%11", "");//IPv6
		}
		return address;
	}
	
	public static final String getClientAddressNoPort(String address) {
		address = getClientAddress(address);
		if(address.contains("]:")) {
			return address.substring(0, address.lastIndexOf("]:")) + "]";//IPv6
		}
		if(address.contains(":")) {
			return address.substring(0, address.lastIndexOf(":"));//IPv4
		}
		return address;
	}
	
	public static final String convertIntToHex(int i) {
		String rtrn = Integer.toHexString(i);
		return rtrn.length() == 1 ? "0" + rtrn : rtrn;
	}
	
	public static final int getPortFromAddress(String address) {
		int p = -1;
		if(address == null || address.trim().isEmpty()) {
			return p;
		}
		String port = "";
		if(address.contains(":")) {
			if(!address.contains("]")) {
				port = address.substring(address.lastIndexOf(":"), address.length());
			} else {
				port = address.substring(address.indexOf("]"), address.length());
				if(port.startsWith("]")) {
					port = port.substring(1);
				}
			}
		}
		if(port.startsWith(":")) {
			port = port.substring(1);
		}
		if(StringUtil.isStrLong(port)) {
			p = Long.valueOf(port).intValue();
		}
		return p;
	}
	
	/** @return This machine's external ip address(or local, if the external could
	 *         not be determined) */
	public static final String getIp() {
		if(ipAddress == null) {
			try {
				URL whatismyip = new URL("http://checkip.amazonaws.com");
				try(BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()))) {
					ipAddress = in.readLine();
				}
			} catch(Throwable e) {
				System.err.println("Unable to retrieve this machine's external ip: " + e.getMessage());//PrintUtil.printThrowable(e);
				try {
					ipAddress = InetAddress.getLocalHost().toString();
				} catch(UnknownHostException e1) {
					ipAddress = "";
				}
			}
		}
		if(ipAddress == null) {
			ipAddress = "";
		}
		return ipAddress;
	}
	
	/** @return One of the many available local ip addresses or the external ip
	 *         address of this machine, chosen randomly */
	public static final String getALocalIP() {
		final int random = StringUtil.getRandomIntBetween(0, 8);
		String rtrn = getIp();
		switch(random) {
		case 0:
		default:
			return "localhost";
		case 1:
			return "127.0.0.1";
		case 2:
			return convertIpv4ToIpv6("127.0.0.1");//http://[::ffff:7f00:1]/ where 7f = 127 and 00 = 0, and the last one = 01
		case 3:
			return "[::1]";
		case 4:
			return rtrn;
		case 5:
			return convertIpv4ToIpv6(getIp());
		case 6:
			try {
				rtrn = InetAddress.getLocalHost().getHostAddress();
			} catch(UnknownHostException ignored) {
				ignored.printStackTrace();
			}
			return rtrn;
		case 7:
			try {
				rtrn = convertIpv4ToIpv6(InetAddress.getLocalHost().getHostAddress());
			} catch(UnknownHostException ignored) {
				ignored.printStackTrace();
			}
			return rtrn;
		case 8:
			try {
				rtrn = InetAddress.getLocalHost().getHostName();
			} catch(UnknownHostException ignored) {
				ignored.printStackTrace();
			}
			return rtrn;
		}
	}
	
	public static final boolean isDomainLocalHost(String domain) {
		if(domain == null || domain.isEmpty()) {
			return false;
		}
		domain = domain.trim();
		if(domain.equals("127.0.0.1") || domain.equals("[::ffff:7f00:1]") || domain.equalsIgnoreCase("localhost") || domain.equals("[::1]") || domain.equals("[0:0:0:0:0:0:0:1]") || domain.equalsIgnoreCase(getIp()) || domain.equalsIgnoreCase(convertIpv4ToIpv6(getIp()))) {
			return true;
		}
		InetAddress localHost = null;
		try {
			localHost = InetAddress.getLocalHost();
			return domain.equals(localHost.getHostAddress()) || domain.equals(convertIpv4ToIpv6(localHost.getHostAddress())) || domain.equals(localHost.getHostName());
		} catch(UnknownHostException ignored) {
			return false;
		}
	}
	
}
