package com.gmail.br45entei;

import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.RestrictedFile;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.CodeUtil.EnumOS;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public class ConsoleThread extends Thread {
	private volatile boolean			isRunning			= false;
	/** Whether or not the exit command was run in the console */
	public volatile boolean				exitCommandWasRun	= false;
	
	protected volatile BufferedReader	br;
	
	private volatile boolean			allowConsoleMode	= true;
	
	public ConsoleThread(String[] args) {
		this.allowConsoleMode = (!StringUtils.containsIgnoreCase("noconsole", args) && !StringUtils.containsIgnoreCase("nogui", args));
	}
	
	public final boolean allowConsoleMode() {
		return this.allowConsoleMode;
	}
	
	@Override
	public final synchronized void start() {
		this.isRunning = true;
		super.start();
	}
	
	/** Tells this thread that it should stop running. */
	public final void stopThread() {
		this.isRunning = false;
		try {
			this.interrupt();
		} catch(Throwable ignored) {
		}
	}
	
	private static final void checkInput() {
		
	}
	
	@Override
	public final void run() {
		/*if(System.console() == null) {
			LogUtils.setConsoleMode(this.allowConsoleMode);
			return;
		}*/
		this.br = new BufferedReader(new InputStreamReader(System.in));
		LogUtils.setConsoleMode(this.allowConsoleMode);
		while(this.isRunning) {
			try {
				if(!this.isRunning) {
					break;
				}
				handleInput(this.br, this);
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}
		LogUtils.setConsoleMode(false);
	}
	
	private volatile int	numOfTimesUserAttemptedToExitWhileShutdownInProgress	= 0x0;
	
	private static final void handleInput(BufferedReader br, ConsoleThread console) throws IOException {
		handleInput(br.readLine(), console);
	}
	
	/** @param input The input to handle
	 * @param console The console thread handling the input
	 * @throws IOException Thrown if there was an error reading further data
	 *             from the console's standard-in stream */
	public static final void handleInput(final String input, ConsoleThread console) throws IOException {
		if(input == null) {
			return;
		}
		if(input.trim().isEmpty()) {
			PrintUtil.printlnNow("What'cha just pressin' enter for? Type a command to do somethin'.");
			return;
		}
		if(input.equalsIgnoreCase("\"help\" for help.") || input.equalsIgnoreCase("\"help\" for help") || input.equalsIgnoreCase("help for help.")) {
			PrintUtil.printlnNow("Okay smartypants. You got me. I meant for you to type the word help, not all the words following the word \"Type\".");//Trololololol
			return;
		}
		String command = "";
		final String[] args;
		String mkArgs = "";
		for(String arg : input.split(" ")) {
			if(command.isEmpty()) {
				command = arg;
			} else {
				mkArgs += arg + " ";
			}
		}
		mkArgs = mkArgs.trim();
		if(mkArgs.isEmpty()) {
			args = new String[0];
		} else {
			args = mkArgs.split(" ");
		}
		
		if(PrintUtil.getSecondaryOut() != null) {
			PrintUtil.getSecondaryOut().println(LogUtils.getCarriageReturnConsolePrefix() + input);
		}
		if(LogUtils.getTertiaryOut() != null) {
			LogUtils.getTertiaryOut().println(LogUtils.getCarriageReturnConsolePrefix() + input);
		}
		
		if(command.equalsIgnoreCase("sinbin")) {
			if(args.length == 0) {
				int numOfBannedIps = JavaWebServer.sinBin.size();
				if(numOfBannedIps > 0) {
					PrintUtil.println("Listing all blocked ips...");
					int i = 1;
					for(NaughtyClientData data : JavaWebServer.sinBin) {
						if(data != null && data.isBanned()) {
							PrintUtil.println("==[" + i + "]: IP: \"" + data.clientIp + "\"; Banned until: \"" + StringUtil.getElapsedTime(data.inSinBinUntil) + "\"; Reason: \"" + data.banReason + "\";");
						}
						i++;
					}
				} else {
					PrintUtil.println("The Sin Bin is currently empty.");
				}
			} else if(args.length >= 2) {
				if(args[0].equalsIgnoreCase("pardon")) {
					String ipToPardon = StringUtil.stringArrayToString(args, ' ', 1);
					NaughtyClientData data = JavaWebServer.getBannedClient(ipToPardon);
					if(data != null) {
						String ip = data.clientIp;
						data.dispose();
						PrintUtil.println("Ip address \"" + ip + "\" successfully pardoned.");
					} else {
						PrintUtil.println("That ip address is not in the Sin Bin. Type \"/sinbin\" to view its' contents.");
					}
				} else {
					PrintUtil.println("Invalid flag \"" + args[0] + "\".");
					PrintUtil.println("Usage: /sinbin or /sinbin [pardon|clear] [ip address...]");
				}
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("clear")) {
					int numOfBannedIps = JavaWebServer.sinBin.size();
					if(numOfBannedIps > 0) {
						PrintUtil.printlnNow("Are you sure you want to remove " + (numOfBannedIps == 1 ? "the remaining banned client" : "all " + numOfBannedIps + " banned clients") + "?");
						String response = console.br.readLine().trim();
						if(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("yeah") || response.equalsIgnoreCase("y")) {
							PrintUtil.println("Pardoning all banned clients...");
							for(NaughtyClientData data : JavaWebServer.sinBin) {
								data.dispose();
							}
							PrintUtil.println("Pardoned " + (numOfBannedIps - JavaWebServer.sinBin.size()) + " of " + numOfBannedIps + " banned clients.");
						} else {
							PrintUtil.println("Operation canceled.");
						}
					} else {
						PrintUtil.println("The Sin Bin is already empty.");
					}
				} else {
					PrintUtil.println("Invalid flag \"" + args[0] + "\".");
					PrintUtil.println("Usage: /sinbin or /sinbin [pardon|clear] [ip address...]");
				}
			} else {
				PrintUtil.println("Usage: /sinbin or /sinbin [pardon|clear] [ip address...]");
			}
		} else if(command.equalsIgnoreCase("threadcount")) {
			final Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
			Set<Thread> threads = stackTraces.keySet();
			PrintUtil.println("There " + (threads.size() == 1 ? "is" : "are") + " " + threads.size() + " thread" + (threads.size() == 1 ? "" : "s") + " running.");
			final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
			int i = 1;
			for(Thread thread : threads) {
				long cpuTime = tmxb.getThreadCpuTime(thread.getId()) / 1000000;
				PrintUtil.println("Thread #" + (i++) + ": Name: \"" + thread.getName() + "\"; State: " + thread.getState().toString() + "; cpu time: " + StringUtils.getElapsedTime(cpuTime, true));
			}
			if(args.length >= 1) {
				String filePath = StringUtils.stringArrayToString(args, ' ');
				File file = new File(filePath);
				try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), true)) {
					for(Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
						Thread thread = entry.getKey();
						long cpuTime = tmxb.getThreadCpuTime(thread.getId()) / 1000000;
						pr.print("=====Thread: name: \"" + thread.getName() + "\"; state: \"" + thread.getState().toString() + "\"; CPU Time: " + StringUtils.getElapsedTime(cpuTime, true) + "; StackTrace:\r\n" + LogUtils.stackTraceElementsToStr(entry.getValue()) + "\r\n");
					}
					pr.flush();
					if(Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().open(file);
							PrintUtil.println("\tFile \"" + file.getName() + "\" opened.");
						} catch(Throwable e) {
							PrintUtil.printErrln("\tUnable to open file \"" + file.getName() + "\": " + e.getMessage());
						}
					} else {
						PrintUtil.println("\tYou can view the output of this command in the file \"" + file.getAbsolutePath() + "\".");
					}
				} catch(Throwable e) {
					PrintUtil.printErrln("Unable to save thread stack traces to file: " + e.getMessage());
				}
			}
		} else if(command.equalsIgnoreCase("cls")) {
			try {
				if(CodeUtil.getOSType().equals(EnumOS.WINDOWS)) {
					Runtime.getRuntime().exec("cls");
				} else {
					Runtime.getRuntime().exec("clear");
				}
			} catch(Throwable e) {
				PrintUtil.println(" /!\\Unable to clear the screen.");
				PrintUtil.println("/___\\");
			}
		} else if(command.equalsIgnoreCase("showwindow")) {
			if(args.length == 0) {
				if(!Main.getInstance().showWindow) {
					Main.getInstance().showWindow = true;
					PrintUtil.println("\tWindow displayed.");
				} else {
					PrintUtil.println("\tThe window is already being displayed.");
				}
			} else {
				PrintUtil.println("\tCommand usage: \"showWindow\"");
			}
		} else if(command.equalsIgnoreCase("hidewindow")) {
			if(args.length == 0) {
				if(Main.getInstance().showWindow) {
					Main.getInstance().showWindow = false;
					PrintUtil.println("\tWindow hidden.");
				} else {
					PrintUtil.println("\tThe window has already been hidden.");
				}
			} else {
				PrintUtil.println("\tCommand usage: \"hideWindow\"");
			}
		} else if(command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {
			if(args.length == 0) {
				if(!console.exitCommandWasRun && !JavaWebServer.getInstance().isShuttingDown()) {//JavaWebServer.getInstance().shutdown();
					console.exitCommandWasRun = true;
					LogUtils.printConsole();
				} else {
					if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress <= 4) {
						PrintUtil.println("\tThe server is already shutting down. Give it a sec!");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 5) {
						PrintUtil.println("\tYou don't seem to listen to me, do you? I've told you four or five times already,");
						PrintUtil.println("\tthe server has been told to shut down and is in the process of doing so! Just wait a while.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 6) {
						PrintUtil.println("\tIf it means that much to you, just find the java process and kill it already. Jeez.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 7) {
						PrintUtil.println("\t\"Exit exit exit exit.\" Is that all you can say? What? You mean the server is frozen? Oh dear.");
						PrintUtil.println("\tType exit again and I'll call \"System.exit(-1);\" for you.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress > 7) {
						PrintUtil.printlnNow("\tCalling \"System.exit(-1);\" now... hope you meant it!");
						PrintUtil.printlnNow("\tThere! That oughta do it.");
						System.exit(-1);
						PrintUtil.printlnNow("\tOkay, I called it! Wait, how is this being displayed if I called it?! Um...");//lol
					}
					console.numOfTimesUserAttemptedToExitWhileShutdownInProgress++;
				}
			} else {
				PrintUtil.println("\tCommand usage: \"exit\"");
			}
		} else if(command.equalsIgnoreCase("restrictfile")) {
			if(args.length >= 1) {
				String filePath = Functions.getElementsFromStringArrayAtIndexesAsString(args, 0, (args.length >= 3 && args[args.length - 2].equalsIgnoreCase("-allowedIP") ? (args.length - 2) : args.length));
				File file = new File(filePath);
				if(!file.exists()) {
					PrintUtil.println("\tThe file \r\n\t\"" + filePath + "\"\r\n\t does not exist.\r\n\tPlease check the path and try again.");
					return;
				}
				RestrictedFile restrictedFile = RestrictedFile.getOrCreateRestrictedFile(file);
				final String restrictedFilePath = FilenameUtils.normalize(restrictedFile.getRestrictedFile().getAbsolutePath());
				PrintUtil.println("\tFile \"" + restrictedFilePath + "\" has been restricted.");
				if(args.length >= 3 && args[args.length - 2].equalsIgnoreCase("-allowedIP")) {
					final String ip = args[args.length - 1];
					restrictedFile.addIPAddress(ip);
					PrintUtil.println("\tAdded IP address \"" + ip + "\" to restricted file \"" + restrictedFilePath + "\"'s whitelist.\r\n\tAnyone with that external IP address may now access the file.");
				}
			} else {
				PrintUtil.println("\tCommand usage: \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\"");
			}
		} else if(command.equalsIgnoreCase("viewSource")) {
			File src = ResourceFactory.getResourceFromStreamAsFile(JavaWebServer.rootDir, "src.zip");
			if(src.exists()) {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(src);
						PrintUtil.println("\tFile \"" + src.getName() + "\" opened.");
					} catch(Throwable e) {
						PrintUtil.printErrln("\tUnable to open file \"" + src.getName() + "\": " + e.getMessage());
					}
				} else {
					PrintUtil.println("\tYou can find the source code of this server located in the file \"" + src.getAbsolutePath() + "\".");
				}
			}
		} else if(command.equalsIgnoreCase("help") || command.equals("?")) {
			if(args.length == 0) {
				PrintUtil.println("\tHelp for Command \"threadcount\": \"threadcount\" Displays all active threads.");
				PrintUtil.println("\tHelp for Command \"cls\": \"cls\" Attempts to clear the console.");
				PrintUtil.println("\tHelp for Command \"showwindow\": \"showwindow\" Shows the GUI if it is hidden.");
				PrintUtil.println("\tHelp for Command \"hidewindow\": \"hidewindow\" Hides the GUI if it is being displayed.");
				PrintUtil.println("\tHelp for Command \"exit\": \"exit\" Saves all data to file and shuts down this server.");
				PrintUtil.println("\tHelp for Command \"restrictFile\": \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\" Restricts a file and/or adds an ip address to its whitelist, only allowing ip addresses in the whitelist to access the file.");
				PrintUtil.println("\tHelp for Command \"viewSource\": \"viewSource\" extracts \"src.zip\" in the root directory then attempts to open it with the operating system's default program.");
				PrintUtil.println("\tHelp for Command \"help\": \"help\" -OR- \"help [command]\"");
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("threadcount")) {
					PrintUtil.println("\tHelp for Command \"threadcount\": \"threadcount\" Displays all active threads.");
				} else if(args[0].equalsIgnoreCase("cls")) {
					PrintUtil.println("\tHelp for Command \"cls\": \"cls\" Attempts to clear the console.");
				} else if(args[0].equalsIgnoreCase("showwindow")) {
					PrintUtil.println("\tHelp for Command \"showwindow\": \"showwindow\" Shows the GUI if it is hidden");
				} else if(args[0].equalsIgnoreCase("hidewindow")) {
					PrintUtil.println("\tHelp for Command \"hidewindow\": \"hidewindow\" Hides the GUI if it is being displayed");
				} else if(args[0].equalsIgnoreCase("exit")) {
					PrintUtil.println("\tHelp for Command \"exit\": \"exit\" Saves all data to file and shuts down this server.");
				} else if(args[0].equalsIgnoreCase("restrictfile")) {
					PrintUtil.println("\tHelp for Command \"restrictFile\": \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\" Restricts a file and/or adds an ip address to its whitelist, only allowing ip addresses in the whitelist to access the file.");
				} else if(args[0].equalsIgnoreCase("viewSource")) {
					PrintUtil.println("\tHelp for Command \"viewSource\": \"viewSource\" extracts \"src.zip\" in the root directory then attempts to open it with the operating system's default program.");
				} else if(args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
					PrintUtil.println("\tYou silly, you just used the help command!");
					PrintUtil.println("\tHelp for Command \"help\": \"help\" -OR- \"help [command]\"");
				} else {
					PrintUtil.println("\tNo help found for command \"" + args[0] + "\".");
				}
			} else {
				PrintUtil.println("\tCommand usage: \"help\" -OR- \"help [command]\"");
			}
		} else {
			PrintUtil.println("\tUnknown command. Type \"help\" for help.");
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
	}
	
}
