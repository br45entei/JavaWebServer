package com.gmail.br45entei.util;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/** @author Brian_Entei */
public class PrintUtil {
	protected static final HashMap<Thread, String>	printLogs						= new HashMap<>();
	protected static final HashMap<Thread, String>	printErrLogs					= new HashMap<>();
	
	protected static final ArrayList<Thread>		printLogsToClearOnDisplay		= new ArrayList<>();
	protected static final ArrayList<Thread>		printErrLogsToClearOnDisplay	= new ArrayList<>();
	
	/** Clears out any logs that haven't been printed to the console yet for the
	 * current thread
	 * 
	 * @see #println(String)
	 * @see #printToConsole()
	 * @see #clearLogsBeforeDisplay() */
	public static final void clearLogs() {
		printLogs.put(Thread.currentThread(), null);
	}
	
	/** Clears out any error logs that haven't been printed to the console yet
	 * for the current thread
	 * 
	 * @see #printErrln(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void clearErrLogs() {
		printErrLogs.put(Thread.currentThread(), null);
	}
	
	/** Marks the logs for the current thread to be cleared right before
	 * {@link #printToConsole()} prints them.
	 * 
	 * @see #println(String)
	 * @see #printToConsole()
	 * @see #clearLogs()
	 * @see #unclearLogsBeforeDisplay() */
	public static final void clearLogsBeforeDisplay() {
		final Thread t = Thread.currentThread();
		if(!printLogsToClearOnDisplay.contains(t)) {
			if(isPrintlning) {
				while(isPrintlning) {
					try {
						Thread.sleep(1L);
					} catch(Throwable ignored) {
					}
				}
			}
			isPrintlning = true;
			printLogsToClearOnDisplay.add(t);
			isPrintlning = false;
		}
	}
	
	/** Reverses the action taken by {@link #clearLogsBeforeDisplay()}.
	 * 
	 * @see #println(String)
	 * @see #printToConsole()
	 * @see #clearLogs()
	 * @see #clearLogsBeforeDisplay() */
	public static final void unclearLogsBeforeDisplay() {
		final Thread t = Thread.currentThread();
		if(printLogsToClearOnDisplay.contains(t)) {
			if(isPrintlning) {
				while(isPrintlning) {
					try {
						Thread.sleep(1L);
					} catch(Throwable ignored) {
					}
				}
			}
			isPrintlning = true;
			printLogsToClearOnDisplay.remove(t);
			isPrintlning = false;
		}
	}
	
	/** Marks the error logs for the current thread to be cleared right before
	 * {@link #printErrToConsole()} prints them.
	 * 
	 * @see #printErrln(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogs()
	 * @see #unclearErrLogsBeforeDisplay() */
	public static final void clearErrLogsBeforeDisplay() {
		final Thread t = Thread.currentThread();
		if(!printErrLogsToClearOnDisplay.contains(t)) {
			if(isPrintErrlning) {
				while(isPrintErrlning) {
					try {
						Thread.sleep(1L);
					} catch(Throwable ignored) {
					}
				}
			}
			isPrintErrlning = true;
			printErrLogsToClearOnDisplay.add(t);
			isPrintErrlning = false;
		}
	}
	
	/** Reverses the action taken by {@link #clearErrLogsBeforeDisplay()}.
	 * 
	 * @see #printErrln(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogs()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void unclearErrLogsBeforeDisplay() {
		final Thread t = Thread.currentThread();
		if(printErrLogsToClearOnDisplay.contains(t)) {
			if(isPrintErrlning) {
				while(isPrintErrlning) {
					try {
						Thread.sleep(1L);
					} catch(Throwable ignored) {
					}
				}
			}
			isPrintErrlning = true;
			printErrLogsToClearOnDisplay.remove(t);
			isPrintErrlning = false;
		}
	}
	
	protected static final String getLogsForThread(Thread t, boolean replaceNull) {
		String s = printLogs.get(t);
		if(s == null && replaceNull) {
			s = "";
		}
		return s;
	}
	
	protected static final void clearLogsForThread(Thread t) {
		printLogs.put(t, null);
	}
	
	public static final void moveLogsToThreadAndClear(Thread thread) {
		Thread t = Thread.currentThread();
		String s = getLogsForThread(t, false);
		clearLogsForThread(t);
		String logs = getLogsForThread(thread, false);
		String put = s == null ? logs : (logs == null ? s : logs + newLine + s);
		printLogs.put(thread, put);
	}
	
	protected static final String	newLine	= "<NEW_LINE>";
	
	public static final void flushStreams() {
		getOut().flush();
		getErr().flush();
		if(secondaryOut != null) {
			secondaryOut.flush();
		}
		if(secondaryErr != null) {
			secondaryErr.flush();
		}
	}
	
	/** Calls {@link #println(String)} and then calls {@link #printToConsole()}.
	 * 
	 * @param str The text to log
	 * @see #println(String)
	 * @see #printToConsole() */
	public static final void printlnNow(String str) {
		println(str);
		printToConsole();
	}
	
	/** Adds the given text to the log queue for later printing with
	 * {@link #printToConsole()}
	 * 
	 * @param str The text to log
	 * @see #println(String)
	 * @see #printlnNow(String)
	 * @see #printToConsole()
	 * @see #clearLogs()
	 * @see #clearLogsBeforeDisplay() */
	public static final void print(String str) {
		Thread t = Thread.currentThread();
		String s = getLogsForThread(t, true);
		printLogs.put(t, s + LogUtils.getLoggerPrefix(LogUtils.LogType.INFO) + str);
	}
	
	/** Adds the given text(appended with a new line) to the log queue for later
	 * printing with {@link #printToConsole()}
	 * 
	 * @param str The text to log
	 * @see #print(String)
	 * @see #printlnNow(String)
	 * @see #printToConsole()
	 * @see #clearLogs()
	 * @see #clearLogsBeforeDisplay() */
	public static final void println(String str) {
		print(str + newLine);
	}
	
	private static boolean	isPrintlning	= false;
	
	/** Prints all logs for the current thread to the console in the order that
	 * they were added.
	 * 
	 * @see #println(String)
	 * @see #printlnNow(String)
	 * @see #clearLogs()
	 * @see #clearLogsBeforeDisplay() */
	public static final void printToConsole() {
		if(isPrintlning) {
			while(isPrintlning) {
				try {
					Thread.sleep(1L);
				} catch(Throwable ignored) {
				}
			}
		}
		isPrintlning = true;
		Thread t = Thread.currentThread();
		if(printLogsToClearOnDisplay.contains(t)) {
			clearLogsForThread(t);
			printLogsToClearOnDisplay.remove(t);
		}
		String s = getLogsForThread(t, false);
		if(s != null) {
			String[] split = s.split(newLine);
			for(String line : split) {
				if(!line.trim().isEmpty()) {
					printlnToWriter(line);
				}
			}
			//printlnToWriter(s);
			clearLogsForThread(t);
		}
		isPrintlning = false;
	}
	
	protected static final String getErrLogsForThread(Thread t, boolean replaceNull) {
		String s = printErrLogs.get(t);
		if(s == null && replaceNull) {
			s = "";
		}
		return s;
	}
	
	protected static final void clearErrLogsForThread(Thread t) {
		printErrLogs.put(t, null);
	}
	
	public static final void moveErrLogsToThreadAndClear(Thread thread) {
		Thread t = Thread.currentThread();
		String s = getErrLogsForThread(t, false);
		clearErrLogsForThread(t);
		String logs = getErrLogsForThread(thread, false);
		String put = s == null ? logs : (logs == null ? s : logs + newLine + s);
		printErrLogs.put(thread, put);
	}
	
	/** Calls {@link #printErrln(String)} and then calls
	 * {@link #printErrToConsole()}.
	 * 
	 * @param str The text to log
	 * @see #printErrln(String)
	 * @see #printErrToConsole() */
	public static final void printErrlnNow(String str) {
		printErrln(str);
		printErrToConsole();
	}
	
	/** Adds the given text to the error log queue for later printing with
	 * {@link #printErrToConsole()}
	 * 
	 * @param str The text to log
	 * @see #printErrln(String)
	 * @see #printErrlnNow(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogs()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void printErr(String str) {
		Thread t = Thread.currentThread();
		String s = getErrLogsForThread(t, true);
		printErrLogs.put(t, s + LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR) + str);
	}
	
	/** Adds the given text(appended with a new line) to the error log queue for
	 * later printing with {@link #printErrToConsole()}
	 * 
	 * @param str The text to log
	 * @see #printErr(String)
	 * @see #printErrlnNow(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogs()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void printErrln(String str) {
		printErr(str + newLine);
	}
	
	private static boolean	isPrintErrlning	= false;
	
	/** Prints all error logs for the current thread to the console in the order
	 * that they were added.
	 * 
	 * @see #printErrln(String)
	 * @see #printErrlnNow(String)
	 * @see #clearErrLogs()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void printErrToConsole() {
		if(isPrintErrlning) {
			while(isPrintErrlning) {
				try {
					Thread.sleep(1L);
				} catch(Throwable ignored) {
				}
			}
		}
		isPrintErrlning = true;
		Thread t = Thread.currentThread();
		if(printErrLogsToClearOnDisplay.contains(t)) {
			clearErrLogsForThread(t);
			printErrLogsToClearOnDisplay.remove(t);
		}
		String s = getErrLogsForThread(t, false);
		if(s != null) {
			String[] split = s.split(newLine);
			for(String line : split) {
				printErrlnToWriter(line);
			}
			//printErrlnToWriter(s);
			clearErrLogsForThread(t);
		}
		isPrintErrlning = false;
	}
	
	//=============================================
	
	private static PrintWriter	out;
	private static PrintWriter	err;
	
	private static final PrintWriter getOut() {
		if(out == null) {
			out = new PrintWriter(new OutputStreamWriter(LogUtils.getOut()/*System.out*/, StandardCharsets.UTF_8), true);
		}
		return out;
	}
	
	private static final PrintWriter getErr() {
		if(err == null) {
			err = new PrintWriter(new OutputStreamWriter(LogUtils.getErr()/*System.err*/, StandardCharsets.UTF_8), true);
		}
		return err;
	}
	
	private static PrintWriter	secondaryOut;
	private static PrintWriter	secondaryErr;
	
	public static final PrintWriter getSecondaryOut() {
		return secondaryOut;
	}
	
	public static final PrintWriter getSecondaryErr() {
		return secondaryErr;
	}
	
	public static final PrintWriter setSecondaryOut(PrintWriter out) {
		PrintWriter old = secondaryOut;
		secondaryOut = out;
		return old;
	}
	
	public static final PrintWriter setSecondaryErr(PrintWriter err) {
		PrintWriter old = secondaryErr;
		secondaryErr = err;
		return old;
	}
	
	/** @param str The string to send to {@link System#out} in
	 *            {@link StandardCharsets#UTF_8} format */
	public static final void printlnToWriter(String str) {//TODO open LogUtils and implement a getLogPrefix(LogType type), then use that method here
		printlnToWriter(getOut(), secondaryOut, LogUtils.LogType.INFO, str);
	}
	
	/** @param str The string to send to {@link System#err} in
	 *            {@link StandardCharsets#UTF_8} format */
	public static final void printErrlnToWriter(String str) {
		printlnToWriter(getErr(), secondaryErr, LogUtils.LogType.ERROR, str);
	}
	
	//==========================
	
	private static final void printlnToWriter(PrintWriter out, PrintWriter secondaryOut, LogUtils.LogType logType, String str) {
		if(str.contains("\n")) {
			try(BufferedReader br = new BufferedReader(new StringReader(str))) {
				String line;
				while((line = br.readLine()) != null) {
					printlnToWriter(out, secondaryOut, logType, line);
				}
			} catch(Throwable wtf) {
				wtf.printStackTrace();
			}
		} else {
			if(!str.trim().isEmpty()) {
				str = LogUtils.doesStrStartWithLoggerPrefix(str) ? str : LogUtils.getLoggerPrefix(logType) + str;
				out.println(LogUtils.carriageReturn() + str);
				if(!LogUtils.doesStrStartWithLoggerPrefix(str)) {
					LogUtils.printConsole();
					if(secondaryOut != null) {
						secondaryOut.println(str);
					}
				} else {
					LogUtils.printConsole();
					if(secondaryOut != null) {
						secondaryOut.println(str);
					}
				}
			}
		}
	}
	
	/** Prints the throwable to the error PrintStream(s) using the
	 * {@link Throwable#printStackTrace(java.io.PrintStream)} method.
	 * 
	 * @param e The Throwable to print */
	public static final void printThrowable(Throwable e) {
		e.printStackTrace(getErr());
		LogUtils.printConsole();
		if(secondaryErr != null) {
			e.printStackTrace(secondaryErr);
		}
	}
	
}
