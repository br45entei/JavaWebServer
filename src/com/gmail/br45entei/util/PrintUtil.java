package com.gmail.br45entei.util;

import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.Thread.State;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

/** @author Brian_Entei */
public class PrintUtil {
	
	protected static final HashMap<Thread, String> printLogs = new HashMap<>();
	protected static final HashMap<Thread, String> printErrLogs = new HashMap<>();
	
	protected static final ArrayList<Thread> printLogsToClearOnDisplay = new ArrayList<>();
	protected static final ArrayList<Thread> printErrLogsToClearOnDisplay = new ArrayList<>();
	
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
	
	protected static final String newLine = "<NEW_LINE>";
	
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
	
	/** Calls {@link #println(String)} and then calls {@link #printToConsole()}.
	 * 
	 * @param str The text to log
	 * @see #println(String)
	 * @see #printToConsole() */
	public static final void printlnNow(String str, Thread t) {
		println(str, t);
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
		print(str, t);
	}
	
	public static final void print(String str, Thread t) {
		String s = getLogsForThread(t, true);
		printLogs.put(t, s + LogUtils.getLoggerPrefix(LogUtils.LogType.INFO, t) + str);
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
	
	public static final void println(String str, Thread t) {
		print(str + newLine, t);
	}
	
	public static final String getResultingPrint(String str) {
		if(str.contains("\n")) {
			String rtrn = "";
			String[] split = str.split(Pattern.quote("\n"));
			for(int i = 0; i < split.length; i++) {
				String line = split[i];
				if(line.endsWith("\r") && line.length() > 1) {
					line = line.substring(0, line.length() - 1);
				}
				if(line.trim().isEmpty()) {
					continue;
				}
				rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.INFO) + line + ((i + 1) == split.length ? "" : "\r\n");
			}
			/*try(BufferedReader br = new BufferedReader(new StringReader(str))) {
				String line;
				while((line = br.readLine()) != null) {
					if(line.endsWith("\r") && line.length() > 1) {
						line = line.substring(0, line.length() - 1);
					}
					if(line.trim().isEmpty()) {
						continue;
					}
					rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.INFO) + line + (br.ready() ? "\r\n" : "");
				}
			} catch(Throwable wtf) {
				wtf.printStackTrace();
			}*/
			return rtrn;
		}
		return str.trim().isEmpty() ? "" : LogUtils.getLoggerPrefix(LogUtils.LogType.INFO) + str;
	}
	
	public static final String getResultingPrintln(String str) {
		return PrintUtil.getResultingPrint(str) + "\n";
	}
	
	public static final String getResultingPrint(String str, Thread t) {
		if(str.contains("\n")) {
			String rtrn = "";
			String[] split = str.split(Pattern.quote("\n"));
			for(int i = 0; i < split.length; i++) {
				String line = split[i];
				if(line.endsWith("\r") && line.length() > 1) {
					line = line.substring(0, line.length() - 1);
				}
				if(line.trim().isEmpty()) {
					continue;
				}
				rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.INFO, t) + line + ((i + 1) == split.length ? "" : "\r\n");
			}
			/*try(BufferedReader br = new BufferedReader(new StringReader(str))) {
				String line;
				while((line = br.readLine()) != null) {
					if(line.endsWith("\r") && line.length() > 1) {
						line = line.substring(0, line.length() - 1);
					}
					if(line.trim().isEmpty()) {
						continue;
					}
					rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.INFO) + line + (br.ready() ? "\r\n" : "");
				}
			} catch(Throwable wtf) {
				wtf.printStackTrace();
			}*/
			return rtrn;
		}
		return str.trim().isEmpty() ? "" : LogUtils.getLoggerPrefix(LogUtils.LogType.INFO, t) + str;
	}
	
	public static final String getResultingPrintln(String str, Thread t) {
		return PrintUtil.getResultingPrint(str, t) + "\n";
	}
	
	public static final String getResultingPrintErr(String str) {
		if(str.contains("\n")) {
			String rtrn = "";
			String[] split = str.split(Pattern.quote("\n"));
			for(int i = 0; i < split.length; i++) {
				String line = split[i];
				if(line.endsWith("\r") && line.length() > 1) {
					line = line.substring(0, line.length() - 1);
				}
				if(line.trim().isEmpty()) {
					continue;
				}
				rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR) + line + ((i + 1) == split.length ? "" : "\r\n");
			}
			/*try(BufferedReader br = new BufferedReader(new StringReader(str))) {
				String line;
				while((line = br.readLine()) != null) {
					if(line.endsWith("\r") && line.length() > 1) {
						line = line.substring(0, line.length() - 1);
					}
					if(line.trim().isEmpty()) {
						continue;
					}
					rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR) + line + (br.ready() ? "\r\n" : "");
				}
			} catch(Throwable wtf) {
				wtf.printStackTrace();
			}*/
			return rtrn;
		}
		return str.trim().isEmpty() ? "" : LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR) + str;
	}
	
	public static final String getResultingPrintErrln(String str) {
		return getResultingPrintErr(str) + "\n";
	}
	
	public static final String getResultingPrintErr(String str, Thread t) {
		if(str.contains("\n")) {
			String rtrn = "";
			String[] split = str.split(Pattern.quote("\n"));
			for(int i = 0; i < split.length; i++) {
				String line = split[i];
				if(line.endsWith("\r") && line.length() > 1) {
					line = line.substring(0, line.length() - 1);
				}
				if(line.trim().isEmpty()) {
					continue;
				}
				rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR, t) + line + ((i + 1) == split.length ? "" : "\r\n");
			}
			/*try(BufferedReader br = new BufferedReader(new StringReader(str))) {
				String line;
				while((line = br.readLine()) != null) {
					if(line.endsWith("\r") && line.length() > 1) {
						line = line.substring(0, line.length() - 1);
					}
					if(line.trim().isEmpty()) {
						continue;
					}
					rtrn += LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR) + line + (br.ready() ? "\r\n" : "");
				}
			} catch(Throwable wtf) {
				wtf.printStackTrace();
			}*/
			return rtrn;
		}
		return str.trim().isEmpty() ? "" : LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR, t) + str;
	}
	
	public static final String getResultingPrintErrln(String str, Thread t) {
		return getResultingPrintErr(str, t) + "\n";
	}
	
	public static final String getUnprintedLogs(LogUtils.LogType type) {
		if(type == LogUtils.LogType.ERROR) {
			return getErrLogsForThread(Thread.currentThread(), true);
		}
		return getLogsForThread(Thread.currentThread(), true);
	}
	
	private static boolean isPrintlning = false;
	
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
	public static final void printErrlnNow(String str, Thread t) {
		printErrln(str, t);
		printErrToConsole();
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
		printErrLogs.put(t, s + LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR, t) + str);
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
	public static final void printErr(String str, Thread t) {
		String s = getErrLogsForThread(t, true);
		printErrLogs.put(t, s + LogUtils.getLoggerPrefix(LogUtils.LogType.ERROR, t) + str);
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
	
	/** Adds the given text(appended with a new line) to the error log queue for
	 * later printing with {@link #printErrToConsole()}
	 * 
	 * @param str The text to log
	 * @see #printErr(String)
	 * @see #printErrlnNow(String)
	 * @see #printErrToConsole()
	 * @see #clearErrLogs()
	 * @see #clearErrLogsBeforeDisplay() */
	public static final void printErrln(String str, Thread t) {
		printErr(str + newLine, t);
	}
	
	private static boolean isPrintErrlning = false;
	
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
	
	private static DualPrintWriter out;
	private static DualPrintWriter err;
	
	@SuppressWarnings("resource")
	private static final DualPrintWriter getOut() {
		if(out == null) {
			out = new DualPrintWriter(new UnlockedOutputStreamWriter(LogUtils.getOut()/*System.out*/, StandardCharsets.UTF_8), true);
			out.setLineSeparator("\n");
		}
		return out;
	}
	
	@SuppressWarnings("resource")
	private static final DualPrintWriter getErr() {
		if(err == null) {
			err = new DualPrintWriter(new UnlockedOutputStreamWriter(LogUtils.getErr()/*System.err*/, StandardCharsets.UTF_8), true);
			err.setLineSeparator("\n");
		}
		return err;
	}
	
	private static PrintWriter secondaryOut;
	private static PrintWriter secondaryErr;
	
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
	
	private static final ConcurrentHashMap<DualPrintWriter, Boolean> printMap = new ConcurrentHashMap<>();
	private static final ConcurrentLinkedDeque<Thread> threadsWaitingToPrintln = new ConcurrentLinkedDeque<>();
	
	private static final boolean isPrintWriterPrinting(DualPrintWriter out) {
		return printMap.get(out) == Boolean.TRUE;
	}
	
	private static final void markPrintWriterAsPrinting(DualPrintWriter out, boolean printing) {
		if(printing) {
			if(!isPrintWriterPrinting(out)) {
				printMap.put(out, Boolean.TRUE);
				return;
			}
			//throw new Error("[0] This isn't supposed to be able to happen!");
			//... and yet: 
			/*java.lang.Error: [0] This isn't supposed to be able to happen!
			at com.gmail.br45entei.util.PrintUtil.markPrintWriterAsPrinting(PrintUtil.java:496)
			at com.gmail.br45entei.util.PrintUtil.printlnToWriter(PrintUtil.java:565)
			at com.gmail.br45entei.util.PrintUtil.printlnToWriter(PrintUtil.java:472)
			at com.gmail.br45entei.server.ClientConnection.printLogsNow(ClientConnection.java:155)
			at com.gmail.br45entei.JavaWebServer.printResultLogs(JavaWebServer.java:1949)
			at com.gmail.br45entei.JavaWebServer.printRequestLogsAndGC(JavaWebServer.java:1988)
			at com.gmail.br45entei.JavaWebServer$7.run(JavaWebServer.java:2166)
			at java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
			at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
			at java.lang.Thread.run(Unknown Source)*/
		}
		if(isPrintWriterPrinting(out)) {
			printMap.remove(out);
			return;
		}
		throw new Error("[1] This isn't supposed to be able to happen!");
	}
	
	private static final void performPrintln(DualPrintWriter out, PrintWriter secondaryOut, LogUtils.LogType logType, String str, boolean bulk) {
		if(!str.trim().isEmpty()) {
			final boolean doesStrStartWithLoggerPrefix = LogUtils.doesStrStartWithLoggerPrefix(str);
			str = doesStrStartWithLoggerPrefix ? str : LogUtils.getLoggerPrefix(logType) + str;
			out.println(LogUtils.carriageReturn() + str);
			if(!doesStrStartWithLoggerPrefix) {
				if(!bulk) {
					LogUtils.printConsole();
				}
				if(secondaryOut != null) {
					secondaryOut.println(str);
				}
			} else {
				if(!bulk) {
					LogUtils.printConsole();
				}
				if(secondaryOut != null) {
					secondaryOut.println(str);
				}
			}
		}
	}
	
	private static final void waitYourTurnMrThread() {
		while(isPrintWriterPrinting(out)) {
			if(Thread.currentThread() == Main.getSWTThread()) {
				Main.getInstance().runLoop();
			} else {
				Functions.sleep();
			}
		}
	}
	
	private static final void waitForOtherThreadsToFinish() {
		long lastSecond = System.currentTimeMillis();
		while(threadsWaitingToPrintln.size() > 1) {
			long now = System.currentTimeMillis();
			if(now - lastSecond >= 1000L) {
				lastSecond = now;
				for(Thread thread : threadsWaitingToPrintln) {
					if(thread.getState() == State.TERMINATED) {
						threadsWaitingToPrintln.remove(thread);
					}
				}
			}
			if(Thread.currentThread() == threadsWaitingToPrintln.peek()) {
				threadsWaitingToPrintln.remove(Thread.currentThread());
				return;
			}
			if(Thread.currentThread() == Main.getSWTThread()) {
				Main.getInstance().runLoop();
			} else {
				Functions.sleep();
			}
		}
	}
	
	private static final void printlnToWriter(DualPrintWriter out, PrintWriter secondaryOut, LogUtils.LogType logType, String str) {
		if(isPrintWriterPrinting(out)) {
			threadsWaitingToPrintln.addLast(Thread.currentThread());
			waitYourTurnMrThread();
			//threadsWaitingToPrintln.remove(Thread.currentThread());
		}
		try {
			waitForOtherThreadsToFinish();
			markPrintWriterAsPrinting(out, true);
			if(str.contains("\n")) {
				try(BufferedReader br = new BufferedReader(new StringReader(str))) {
					String line;
					while((line = br.readLine()) != null) {
						if(line.endsWith("\r") && line.length() > 1) {
							line = line.substring(0, line.length() - 1);
						}
						if(line.trim().isEmpty()) {
							continue;
						}
						performPrintln(out, secondaryOut, logType, line.trim(), true);
						LogUtils.printConsole();
					}
				} catch(Throwable wtf) {
					wtf.printStackTrace();
				}
			} else {
				performPrintln(out, secondaryOut, logType, str, false);
			}
		} catch(Error | RuntimeException e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
		} catch(Throwable e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
		}
		try {
			if(isPrintWriterPrinting(out)) {
				markPrintWriterAsPrinting(out, false);
			}
		} catch(Error | RuntimeException e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
		} catch(Throwable e) {
			e.printStackTrace(LogUtils.ORIGINAL_SYSTEM_ERR);
		}
		if(threadsWaitingToPrintln.contains(Thread.currentThread())) {
			threadsWaitingToPrintln.remove(Thread.currentThread());
		}
	}
	
	/** Prints the throwable to the error PrintStream(s) using the
	 * {@link Throwable#printStackTrace(java.io.PrintStream)} method.
	 * 
	 * @param e The Throwable to print */
	public static final void printThrowable(Throwable e) {
		getErr().println(StringUtil.throwableToStr(e, "\n"));//e.printStackTrace(getErr());
		LogUtils.printConsole();
		if(secondaryErr != null) {
			e.printStackTrace(secondaryErr);
		}
	}
	
}
