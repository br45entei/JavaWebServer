/**
 * 
 */
package com.gmail.br45entei.util;

import java.util.Map.Entry;

/** @author Brian_Entei */
public class CodeUtil {
	
	/** @return Whether or not a 64 bit system was detected */
	public static boolean isJvm64bit() {
		for(String s : new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"}) {
			String s1 = System.getProperty(s);
			if((s1 != null) && s1.contains("64")) {
				return true;
			}
		}
		return false;
	}
	
	/** Enum class differentiating types of operating systems
	 * 
	 * @author Brian_Entei */
	public static enum EnumOS {
		/** Linux or other similar Unix-type operating systems */
		LINUX,
		/** Salaries operating systems */
		SOLARIS,
		/** Windows operating systems */
		WINDOWS,
		/** Mac/OSX */
		OSX,
		/** An unknown operating system */
		UNKNOWN;
	}
	
	/** @return The type of operating system that java is currently running
	 *         on */
	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.OSX : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
	}
	
	/** @param millis The amount of time in milliseconds to attempt to sleep
	 *            for */
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Throwable ignored) {
		}
	}
	
	public static final <K, V> Entry<K, V> createBlankEntry(final K key, final V value) {
		return new Entry<K, V>() {
			
			V val = value;
			
			@Override
			public K getKey() {
				return key;
			}
			
			@Override
			public V getValue() {
				return this.val;
			}
			
			@Override
			public V setValue(V value) {
				V oldVal = this.val;
				this.val = value;
				return oldVal;
			}
			
		};
	}
	
}
