package com.gmail.br45entei.util;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;

/** @author Brian_Entei */
public class AddressUtil {
	
	private static String ipAddress = null;
	
	public static final void main(String[] args) {
		/*System.out.println("[0:0:0:0:0:0:0:0]:54879: " + isAddressInValidFormat("[0:0:0:0:0:0:0:0]:54879"));
		System.out.println("[0:0:0:0:0sasdasdasd:0:0:0]:54879: " + isAddressInValidFormat("[0:0:0:0:0sasdasdasd:0:0:0]:54879"));
		System.out.println("[0:0:0:0:0:0]:54879: " + isAddressInValidFormat("[0:0:0:0:0:0]:54879"));
		System.out.println("[0:0:0:0:0:0:0:0]:54k879: " + isAddressInValidFormat("[0:0:0:0:0:0:0:0]:54k879"));
		System.out.println("[0:0:0:0:0:0:0:0]:54879.txt: " + isAddressInValidFormat("[0:0:0:0:0:0:0:0]:54879.txt"));
		System.out.println("107.129.213.19:54879: " + isAddressInValidFormat("107.129.213.19:54879"));
		System.out.println("107.129.2s.19:54879: " + isAddressInValidFormat("107.129.2s.19:54879"));
		System.out.println("107.1.2.1:54879: " + isAddressInValidFormat("107.1.2.1:54879"));
		System.out.println("107.129.213.19:54k879: " + isAddressInValidFormat("107.129.213.19:54k879"));
		System.out.println("107.129.213.19:54879.txt: " + isAddressInValidFormat("107.129.213.19:54879.txt"));
		System.out.println("someRandomFile.txt: " + isAddressInValidFormat("someRandomFile.txt"));*/
		System.out.println("[::1]:54879: " + isAddressInValidFormat("[::1]:54879"));
		System.out.println(getClientAddressNoPort("[::1]"));
		System.out.println(getClientAddressNoPort("[::1]:80"));
	}
	
	public static final boolean isAddressInValidFormat(String address) {
		String[] result = getValidHostFrom(address);
		return result != null && result.length == 2;
	}
	
	public static final String[] getValidHostFrom(String host) {
		if(host == null || host.trim().isEmpty()) {
			return null;
		}
		//if(LogUtils.secondaryOut == null) {
		//	LogUtils.secondaryOut = LogUtils.ORIGINAL_SYSTEM_OUT;
		//}
		String port = "";
		if(host.contains(":")) {
			//LogUtils.secondaryOut.println("[0]");
			//if(!host.contains("]")) {
			//	port = host.substring(host.lastIndexOf(":"), host.length());
			//	host = host.substring(0, host.lastIndexOf(":"));
			//	LogUtils.secondaryOut.println("[1]: host: " + host + "; port: " + port);
			//} else {
			String originalHost = host;
			host = getClientAddressNoPort(host);
			//LogUtils.secondaryOut.println("[2]: host: " + host);
			if(host.length() < originalHost.length()) {
				port = originalHost.substring(host.length(), originalHost.length());
				//LogUtils.secondaryOut.println("[3]: port: " + port);
			} else {
				//LogUtils.secondaryOut.println("[4]: port: " + port);
			}
			//}
		} else {
			//LogUtils.secondaryOut.println("[5]: host: " + host);
		}
		boolean isHostValid;
		
		try {
			isHostValid = InetAddress.getByName(host) != null;
			//LogUtils.secondaryOut.println("[6]: host is valid!");
		} catch(Throwable ignored) {
			isHostValid = false;
			String originalHost = host;
			try {
				host = AddressUtil.getClientAddressNoPort(host);
				isHostValid = InetAddress.getByName(host) != null;
				//LogUtils.secondaryOut.println("[6_1]: host is valid now: " + host + "(was: \"" + originalHost + "\"...)!");
			} catch(Throwable ignored1) {
				host = originalHost;
			}
			if(!isHostValid) {
				//LogUtils.secondaryOut.println("[7]: host is NOT valid: \"" + host + "\"!");
			}
		}
		if(host.isEmpty() || !isHostValid) {
			//LogUtils.secondaryOut.println("[8]: host is empty or not valid!");
			return null;
		}
		//LogUtils.secondaryOut.println("[9]: host: " + host + "; port: " + port);
		port = port.trim();
		//LogUtils.secondaryOut.println("[10]: port: " + port);
		port = port.startsWith(":") ? port.substring(1) : port;
		//LogUtils.secondaryOut.println("[11]: port: " + port);
		if(!port.isEmpty() && !StringUtil.isStrInt(port)) {
			//LogUtils.secondaryOut.println("[12]: port is not empty and is an invalid integer!");
			return null;
		}
		//LogUtils.secondaryOut.println("[13]: resulting host: \"" + host + "\"; port: \"" + port + "\";");
		return new String[] {host, port};
	}
	
	public static final String getValidHostFor(String host, ClientConnection reuse) {
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
			if(reuse != null) {
				reuse.println("\t--- Detected unresolved host: " + host);//host = getIp();
			}
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
			if(octets[0].contains("/")) {
				octets[0] = octets[0].substring(octets[0].indexOf("/"));
			}
			final String s1 = Integer.toHexString(Integer.parseInt(octets[0])) + convertIntToHex(Integer.parseInt(octets[1]));
			final String s2 = Integer.toHexString(Integer.parseInt(octets[2])) + convertIntToHex(Integer.parseInt(octets[3]));
			return "[::ffff:" + (s1.startsWith("00") ? s1.substring(2) : s1) + ":" + (s2.startsWith("00") ? s2.substring(2) : s2) + "]" + port;
		}
		return ip;
	}
	
	public static final String getClientAddress(Socket s) {
		SocketAddress a = s.getRemoteSocketAddress();
		if(a instanceof InetSocketAddress) {
			InetSocketAddress addr = (InetSocketAddress) a;
			String host = addr.getHostName();
			int port = addr.getPort();
			if(host.contains(":")) {//IPv6
				return "[" + host + "]:" + port;
			}
			return host + ":" + port;
		}
		return getClientAddress((a != null) ? StringUtils.replaceOnce(a.toString(), "/", "") : "");
	}
	
	public static final String getClientAddress(String address) {
		if(address == null || address.isEmpty()) {
			return "";
		}
		if(address.equals("[:1]")) {
			throw new Error();
		}
		if(address.contains(":")) {
			int index1 = address.indexOf(":");
			int index2 = address.lastIndexOf(":");
			if(index1 == index2) {
				return address;//IPv4
			}
			final boolean containsBrackets = address.contains("[") && address.contains("]");
			if(!containsBrackets) {
				return "[" + address.replace("%10", "").replace("%11", "") + "]";//IPv6 with no port supplied
			}
			//return ((containsBrackets ? "" : "[") + address.substring(0, index2) + (containsBrackets ? "" : "]") + address.substring(index2, address.length())).replace("%10", "").replace("%11", "");//IPv6
		}
		return address;
	}
	
	public static final String getClientAddressNoPort(Socket s) {
		return getClientAddressNoPort(getClientAddress(s));
	}
	
	public static final String getClientAddressNoPort(String address) {
		address = getClientAddress(address);
		if(address.contains(":")) {
			int index1 = address.indexOf(":");
			int index2 = address.lastIndexOf(":");
			if(index1 == index2) {
				return address.substring(0, address.lastIndexOf(":"));//IPv4
			}
			final boolean containsBrackets = address.contains("[") && address.contains("]");
			if(!containsBrackets) {
				return "[" + address.replace("%10", "").replace("%11", "") + "]";//IPv6 with no port supplied
			}
			if(address.contains("]:")) {
				return address.substring(0, address.lastIndexOf("]:")) + "]";//IPv6
			}
			//return ((containsBrackets ? "" : "[") + address.substring(0, index2) + (containsBrackets ? "" : "]")).replace("%10", "").replace("%11", "");// + address.substring(index2, address.length())).replace("%10", "").replace("%11", "");//IPv6
		}
		/*if(address.contains("]:")) {
			return address.substring(0, address.lastIndexOf("]:")) + "]";//IPv6
		} else if(address.length() - address.replace(":", "").length() >= 2) {//(See if there are multiple colons, then remove any brackets and re-add them)
			address = "[" + address.replace("[", "").replace("]", "") + "]";
		} else if(address.contains(":")) {
			return address.substring(0, address.lastIndexOf(":"));//IPv4
		}*/
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
		if(StringUtils.isStrLong(port)) {
			p = Long.valueOf(port).intValue();
		}
		return p;
	}
	
	/** @return This machine's external ip address(or local, if the external
	 *         could
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
	
	public static final String getLocalExternalHostName() {
		String ip = getIp();
		try {
			if(ip.isEmpty()) {
				return InetAddress.getLocalHost().getHostAddress();
			}
			return InetAddress.getByName(ip).getHostAddress();
		} catch(UnknownHostException e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
			return "localhost";
		}
	}
	
	/** @return One of the many available local ip addresses or the external ip
	 *         address of this machine, chosen randomly */
	public static final String getALocalIP() {
		final int random = StringUtils.getRandomIntBetween(0, 8);
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
	
	public static final boolean isDomainExternalIp(String domain) {
		if(domain == null || domain.isEmpty()) {
			return false;
		}
		domain = domain.trim();
		final String ip = getIp();
		final String ipv6 = convertIpv4ToIpv6(ip);
		return domain.equalsIgnoreCase(ip) || domain.equalsIgnoreCase(ip + ":" + JavaWebServer.listen_port) || domain.equalsIgnoreCase(ip + ":" + JavaWebServer.ssl_listen_port) || domain.equalsIgnoreCase(ip + ":" + JavaWebServer.admin_listen_port) || //
				domain.equalsIgnoreCase(ipv6) || domain.equalsIgnoreCase(ipv6 + ":" + JavaWebServer.listen_port) || domain.equalsIgnoreCase(ipv6 + ":" + JavaWebServer.ssl_listen_port) || domain.equalsIgnoreCase(ipv6 + ":" + JavaWebServer.admin_listen_port);
	}
	
	public static final boolean isDomainLocalHost(String domain) {
		if(domain == null || domain.isEmpty()) {
			return false;
		}
		domain = domain.trim();
		if(domain.equals("127.0.0.1") || domain.equals("[::ffff:7f00:1]") || domain.equalsIgnoreCase("localhost") || domain.equals("[::1]") || domain.equals("[0:0:0:0:0:0:0:1]") || isDomainExternalIp(domain)) {
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
