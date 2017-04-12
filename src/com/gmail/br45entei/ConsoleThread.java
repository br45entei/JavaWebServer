package com.gmail.br45entei;

import com.gmail.br45entei.gui.Main;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.RestrictedFile;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.AddressUtil;
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
	
	private static volatile ConsoleThread instance;
	
	private volatile boolean isRunning = false;
	/** Whether or not the exit command was run in the console */
	public volatile boolean exitCommandWasRun = false;
	
	protected volatile BufferedReader br;
	
	private volatile boolean allowConsoleMode = true;
	
	public ConsoleThread(String[] args) {
		instance = this;
		this.allowConsoleMode = (!StringUtils.containsIgnoreCase("noconsole", args) && !StringUtils.containsIgnoreCase("nogui", args));
		this.setName("Console" + this.getName());
		this.setDaemon(true);
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
	
	@SuppressWarnings("unused")
	private static final void checkInput() {
		
	}
	
	private final void loopUntilServerDeath() {
		LogUtils.setConsoleMode(this.allowConsoleMode);
		while(this.isRunning && Main.isRunning()) {
			Functions.sleep();
		}
		LogUtils.setConsoleMode(false);
	}
	
	@Override
	public final void run() {
		if(!Main.isRunning()) {
			while(!Main.isRunning()) {
				Functions.sleep();
			}
		}
		if(System.console() == null) {
			loopUntilServerDeath();
			return;
		}
		this.br = new BufferedReader(new InputStreamReader(System.in));
		LogUtils.setConsoleMode(this.allowConsoleMode);
		while(this.isRunning && Main.isRunning()) {
			try {
				if(!this.isRunning) {
					break;
				}
				handleInput(this.br, this);
				if(!LogUtils.isConsolePresent()) {
					LogUtils.printConsole();
				}
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}
		LogUtils.setConsoleMode(false);
	}
	
	private volatile int numOfTimesUserAttemptedToExitWhileShutdownInProgress = 0x0;
	
	private static final void println(String str) {
		if(Thread.currentThread() == instance || Thread.currentThread() == Main.getSWTThread()) {
			PrintUtil.println(str);
		}
	}
	
	private static final void printlnNow(String str) {
		if(Thread.currentThread() == instance || Thread.currentThread() == Main.getSWTThread()) {
			PrintUtil.printlnNow(str);
		}
	}
	
	private static final void printErrln(String str) {
		if(Thread.currentThread() == instance || Thread.currentThread() == Main.getSWTThread()) {
			PrintUtil.printErrln(str);
		}
	}
	
	@SuppressWarnings("unused")
	private static final void printErrlnNow(String str) {
		if(Thread.currentThread() == instance || Thread.currentThread() == Main.getSWTThread()) {
			PrintUtil.printErrlnNow(str);
		}
	}
	
	private static final void handleInput(BufferedReader br, ConsoleThread console) throws IOException {
		try {
			handleInput(br.readLine(), console);
		} catch(IOException e) {
			if("Not enough storage is available to process this command".equals(e.getMessage())) {
				if(console != null && Thread.currentThread() == console) {
					console.loopUntilServerDeath();
					return;
				}
			}
			throw e;
		}
	}
	
	private static volatile boolean nextInputFromSWTIsAnswer = false;
	private static volatile String nextSWTThreadInput = null;
	
	/** @param input The input to handle
	 * @param console The console thread handling the input
	 * @throws IOException Thrown if there was an error reading further data
	 *             from the console's standard-in stream */
	public static final void handleInput(final String input, ConsoleThread console) throws IOException {
		if(input == null) {
			return;
		}
		if(nextInputFromSWTIsAnswer && Thread.currentThread() == Main.getSWTThread()) {
			if(input.trim().isEmpty()) {
				return;
			}
			nextSWTThreadInput = input;
			nextInputFromSWTIsAnswer = false;
			return;
		}
		if(input.equalsIgnoreCase("\"help\" for help.") || input.equalsIgnoreCase("\"help\" for help") || input.equalsIgnoreCase("help for help.")) {
			printlnNow("Okay smartypants. You got me. I meant for you to type the word help, not all the words following the word \"Type\".");//Trololololol
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
			PrintUtil.getSecondaryOut().println(LogUtils.getConsolePrefixChar() + input);
		}
		if(LogUtils.getTertiaryOut() != null) {
			LogUtils.getTertiaryOut().println(LogUtils.getConsolePrefixChar() + input);
		}
		
		if(command.equalsIgnoreCase("debug")) {
			println(" /!\\\t Debug mode is now: " + (Boolean.valueOf(HTTPClientRequest.debug = !HTTPClientRequest.debug) == Boolean.TRUE ? "ON" : "OFF"));
			println("/___\\");
		} else if(command.equalsIgnoreCase("socketFix")) {
			final long startTime = System.currentTimeMillis();
			int[] closedRemoved = JavaWebServer.purgeDeadClientIpConnectionsFromSocketList();
			int numClosed = closedRemoved[0];
			int numRemoved = closedRemoved[1];
			int numReusedConnectionsClosed = closedRemoved[2];
			if(numRemoved > 0) {
				println("Removed " + numRemoved + " socket" + (numRemoved == 1 ? "" : "s") + //
						(numClosed > 0 ? (numRemoved > numClosed ? "(Only c" : "and c") + "losed " + numClosed + "socket" + (numClosed == 1 ? "" : "s") : "") + (numRemoved > numClosed ? ")" : "") + //
						"." + //
						(numReusedConnectionsClosed > 0 ? " Additionally, " + numReusedConnectionsClosed + " reused connection" + (numReusedConnectionsClosed == 1 ? "" : "s") + " w" + (numReusedConnectionsClosed == 1 ? "" : "as") + " closed." : "")//
				);
			} else {
				println("No stuck socket connections found to purge.");
				println("If there seems to be some stuck in the SWT window, then the connected clients deque needs to be purged instead.");
			}
			println("Command elapsed time: " + StringUtil.getElapsedTimeTraditional(System.currentTimeMillis() - startTime, true));
		} else if(command.equalsIgnoreCase("sinbin")) {
			if(args.length == 0) {
				int numOfBannedIps = JavaWebServer.sinBin.size();
				if(numOfBannedIps > 0) {
					println("Listing all blocked ips...");
					int i = 1;
					for(NaughtyClientData data : JavaWebServer.sinBin) {
						if(data != null && data.isBanned()) {
							println("==[" + i + "]: IP: \"" + data.clientIp + "\"; Banned until: \"" + StringUtil.getElapsedTime(data.inSinBinUntil) + "\"; Reason: \"" + data.banReason + "\";");
						}
						i++;
					}
				} else {
					println("The Sin Bin is currently empty.");
				}
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("cleanup")) {
					for(NaughtyClientData data : JavaWebServer.sinBin) {
						if(!data.shouldBeBanned()) {
							if(data.canBeBanned()) {
								data.dispose(false);
							}
						}
					}
				} else if(args[0].equalsIgnoreCase("clear")) {
					int numOfBannedIps = JavaWebServer.sinBin.size();
					if(numOfBannedIps > 0) {
						printlnNow("Are you sure you want to remove " + (numOfBannedIps == 1 ? "the remaining banned client" : "all " + numOfBannedIps + " banned clients") + "?");
						String response = "";//String response = console.br.readLine().trim();
						if(Thread.currentThread() == Main.getSWTThread()) {
							while(nextInputFromSWTIsAnswer) {
								Main.getInstance().runLoop();
								if(!nextInputFromSWTIsAnswer) {
									if(nextSWTThreadInput == null || nextSWTThreadInput.trim().isEmpty()) {
										nextInputFromSWTIsAnswer = true;
										continue;
									}
								} else {
									if(nextSWTThreadInput != null && nextSWTThreadInput.trim().isEmpty()) {
										Main.getInstance().runLoop();
										nextInputFromSWTIsAnswer = true;
										continue;
									}
									continue;
								}
								if(nextSWTThreadInput != null) {
									response = nextSWTThreadInput;
									nextSWTThreadInput = null;
									nextInputFromSWTIsAnswer = false;
									break;
								}
							}
						} else {
							response = console.br.readLine().trim();
						}
						
						if(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("yeah") || response.equalsIgnoreCase("y")) {
							println("Pardoning all banned clients...");
							for(NaughtyClientData data : JavaWebServer.sinBin) {
								data.dispose();
							}
							println("Pardoned " + (numOfBannedIps - JavaWebServer.sinBin.size()) + " of " + numOfBannedIps + " banned clients.");
						} else {
							println("Operation canceled. (" + response + ")");
						}
					} else {
						println("The Sin Bin is already empty.");
					}
				} else {
					if(!(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("pardon") || args[0].equalsIgnoreCase("clear"))) {
						println("Invalid flag \"" + args[0] + "\".");
					}
					println("Usage: /sinbin or /sinbin [ban|pardon|clear|cleanup] [ipAddress] [banReason...]");
				}
			} else if(args.length >= 2) {
				if(args[0].equalsIgnoreCase("ban")) {
					String ipToBan = args[1];
					final String banReason = StringUtil.stringArrayToString(args, ' ', 2).trim();
					if(banReason.isEmpty()) {
						println("\"banReason\" cannot be empty.");
						println("Usage: /sinbin or /sinbin [ban|pardon|clear|cleanup] [ipAddress] [banReason...]");
					} else {
						NaughtyClientData data = NaughtyClientData.getBannedClient(ipToBan);
						if(data != null) {
							String ip = data.clientIp;
							println("Ip address \"" + ip + "\" was already banned!");
						} else {
							if(AddressUtil.isAddressInValidFormat(args[1])) {
								args[1] = AddressUtil.getClientAddressNoPort(args[1]);
								data = new NaughtyClientData(args[1]);
								data.inSinBinUntil = -1L;
								if(args.length > 2) {
									data.banReason = banReason;
								}
								println("Successfully banned ip \"" + data.clientIp + "\"" + (data.banReason.isEmpty() ? "" : " for \"" + data.banReason + "\"") + ".");
								JavaWebServer.sinBin.add(data);
								data.saveToFile();
							} else {
								println("The address \"" + args[1] + "\" is not valid. Please check it and try again.");
							}
						}
					}
				} else if(args[0].equalsIgnoreCase("pardon")) {
					String ipToPardon = StringUtil.stringArrayToString(args, ' ', 1);
					ipToPardon = AddressUtil.getClientAddressNoPort(ipToPardon);
					NaughtyClientData data = NaughtyClientData.getBannedClient(ipToPardon);
					if(data != null) {
						String ip = data.clientIp;
						int[] closedRemoved = data.dispose();
						int numClosed = closedRemoved[0];
						int numRemoved = closedRemoved[1];
						int numReusedConnectionsClosed = closedRemoved[2];
						println("Ip address \"" + ip + "\" successfully pardoned.");
						if(numRemoved > 0) {
							println("Removed " + numRemoved + " socket" + (numRemoved == 1 ? "" : "s") + //
									(numClosed > 0 ? (numRemoved > numClosed ? "(Only c" : "and c") + "losed " + numClosed + "socket" + (numClosed == 1 ? "" : "s") : "") + (numRemoved > numClosed ? ")" : "") + //
									"." + //
									(numReusedConnectionsClosed > 0 ? " Additionally, " + numReusedConnectionsClosed + " reused connection" + (numReusedConnectionsClosed == 1 ? "" : "s") + " w" + (numReusedConnectionsClosed == 1 ? "" : "as") + " closed." : ""));
						}
					} else {
						println("That ip address is not banned. Type \"/sinbin\" to view banned ip addresses.");
					}
				} else if(args[0].equalsIgnoreCase("delete")) {
					String ipToPardon = StringUtil.stringArrayToString(args, ' ', 1);
					ipToPardon = AddressUtil.getClientAddressNoPort(ipToPardon);
					NaughtyClientData data = NaughtyClientData.getBannedClient(ipToPardon);
					int numDeleted = 0;
					if(data != null) {
						String ip = data.clientIp;
						int[] closedRemoved = data.dispose();
						numDeleted++;
						int numClosed = closedRemoved[0];
						int numRemoved = closedRemoved[1];
						int numReusedConnectionsClosed = closedRemoved[2];
						println("Ip address \"" + ip + "\" successfully pardoned.");
						if(numRemoved > 0) {
							println("Removed " + numRemoved + " socket" + (numRemoved == 1 ? "" : "s") + //
									(numClosed > 0 ? (numRemoved > numClosed ? "(Only c" : "and c") + "losed " + numClosed + "socket" + (numClosed == 1 ? "" : "s") : "") + (numRemoved > numClosed ? ")" : "") + //
									"." + //
									(numReusedConnectionsClosed > 0 ? " Additionally, " + numReusedConnectionsClosed + " reused connection" + (numReusedConnectionsClosed == 1 ? "" : "s") + " w" + (numReusedConnectionsClosed == 1 ? "" : "as") + " closed." : ""));
						}
					}
					for(NaughtyClientData curData : JavaWebServer.sinBin) {
						if(curData.clientIp.equalsIgnoreCase(ipToPardon)) {
							curData.dispose(numDeleted == 0);
							numDeleted++;
						}
					}
					if(numDeleted > 0) {
						println("Deleted " + numDeleted + " client " + (numDeleted == 1 ? "address" : (numDeleted > 1 ? "duplicate addresses" : "addresses")) + ".");
					} else {
						println("That ip address is not in the Sin Bin. Type \"/sinbin\" to view its' contents.");
					}
				} else {
					println("Invalid flag \"" + args[0] + "\".");
					println("Usage: /sinbin or /sinbin [ban|pardon|clear|cleanup] [ipAddress] [banReason...]");
				}
			} else {
				println("Usage: /sinbin or /sinbin [ban|pardon|clear|cleanup] [ipAddress] [banReason...]");
			}
		} else if(command.equalsIgnoreCase("threadcount")) {
			final Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
			Set<Thread> threads = stackTraces.keySet();
			println("There " + (threads.size() == 1 ? "is" : "are") + " " + threads.size() + " thread" + (threads.size() == 1 ? "" : "s") + " running.");
			final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
			int i = 1;
			for(Thread thread : threads) {
				long cpuTime = tmxb.getThreadCpuTime(thread.getId()) / 1000000;
				println("Thread #" + (i++) + ": Name: \"" + thread.getName() + "\"; State: " + thread.getState().toString() + "; cpu time: " + StringUtils.getElapsedTime(cpuTime, true));
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
							println("\tFile \"" + file.getName() + "\" opened.");
						} catch(Throwable e) {
							printErrln("\tUnable to open file \"" + file.getName() + "\": " + e.getMessage());
						}
					} else {
						println("\tYou can view the output of this command in the file \"" + file.getAbsolutePath() + "\".");
					}
				} catch(Throwable e) {
					printErrln("Unable to save thread stack traces to file: " + e.getMessage());
				}
			}
		} else if(command.equalsIgnoreCase("cls")) {
			if(Thread.currentThread() == Main.getSWTThread() && Main.getInstance().getConsoleWindow() != null) {
				Main.getInstance().getConsoleWindow().cls();
			} else {
				try {
					if(CodeUtil.getOSType().equals(EnumOS.WINDOWS)) {
						Runtime.getRuntime().exec("cls");
					} else {
						Runtime.getRuntime().exec("clear");
					}
				} catch(Throwable ignored) {
					println(" /!\\Unable to clear the screen.");
					println("/___\\");
				}
			}
		} else if(command.equalsIgnoreCase("showwindow")) {
			if(args.length == 0) {
				if(!Main.getInstance().showWindow) {
					Main.getInstance().showWindow = true;
					println("\tWindow displayed.");
				} else {
					println("\tThe window is already being displayed.");
				}
			} else {
				println("\tCommand usage: \"showWindow\"");
			}
		} else if(command.equalsIgnoreCase("hidewindow")) {
			if(args.length == 0) {
				if(Main.getInstance().showWindow) {
					Main.getInstance().showWindow = false;
					println("\tWindow hidden.");
				} else {
					println("\tThe window has already been hidden.");
				}
			} else {
				println("\tCommand usage: \"hideWindow\"");
			}
		} else if(command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {
			if(args.length == 0) {
				if(!console.exitCommandWasRun && !JavaWebServer.isShuttingDown()) {//JavaWebServer.getInstance().shutdown();
					console.exitCommandWasRun = true;
					LogUtils.printConsole();
				} else if(Main.getSWTThread().getState() != State.TERMINATED) {
					if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress <= 4) {
						println("\tThe server is already shutting down. Give it a sec!");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 5) {
						println("\tYou don't seem to listen to me, do you? I've told you four or five times already,");
						println("\tthe server has been told to shut down and is in the process of doing so! Just wait a while.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 6) {
						println("\tIf it means that much to you, just find the java process for this server and kill it already. Jeez.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress == 7) {
						println("\t\"Exit exit exit exit.\" Is that all you can say? What? You mean the server is frozen? Oh dear.");
						println("\tType exit again and I'll call \"System.exit(-1);\" for you.");
					} else if(console.numOfTimesUserAttemptedToExitWhileShutdownInProgress > 7) {
						printlnNow("\tCalling \"System.exit(-1);\" now... hope you meant it!");
						printlnNow("\tThere! That oughta do it.");
						System.exit(-1);
						printlnNow("\tOkay, I called it! Wait, how is this being displayed if I called it?! Um...");//lol <--I actually had this print one time... I kid you not. e.e
					}
					console.numOfTimesUserAttemptedToExitWhileShutdownInProgress++;
				}
				if(Main.getSWTThread().getState() == State.TERMINATED) {
					println("\tThe main SWT thread has died... o.e");
					printlnNow("\tWe should, uh, shut down now. eheh ...");
					//JavaWebServer.connectedClients.clear();
					try {
						for(ClientConnection connection : JavaWebServer.sockets) {//getConnectedClients(false)) {
							connection.status.cancel();
						}
					} catch(RuntimeException | Error ignored) {
					} catch(Throwable ignored) {
					}
					JavaWebServer.shutdown();
					return;
				}
			} else if(args.length == 1 && args[0].equalsIgnoreCase("now")) {
				if(JavaWebServer.isShuttingDown()) {
					System.exit(-1);
				} else {
					try {
						for(ClientConnection connection : JavaWebServer.sockets) {//getConnectedClients(false)) {
							connection.status.cancel();
						}
					} catch(RuntimeException | Error ignored) {
					} catch(Throwable ignored) {
					}
					Thread killServer = new Thread("KillServerThread") {
						@Override
						public final void run() {
							JavaWebServer.shutdown();
						}
					};
					killServer.setDaemon(true);
					killServer.start();
				}
			} else {
				println("\tCommand usage: \"exit\"");
			}
		} else if(command.equalsIgnoreCase("restrictfile")) {
			if(args.length >= 1) {
				String filePath = Functions.getElementsFromStringArrayAtIndexesAsString(args, 0, (args.length >= 3 && args[args.length - 2].equalsIgnoreCase("-allowedIP") ? (args.length - 2) : args.length));
				File file = new File(filePath);
				if(!file.exists()) {
					println("\tThe file \r\n\t\"" + filePath + "\"\r\n\t does not exist.\r\n\tPlease check the path and try again.");
					return;
				}
				RestrictedFile restrictedFile = RestrictedFile.getOrCreateRestrictedFile(file);
				final String restrictedFilePath = FilenameUtils.normalize(restrictedFile.getRestrictedFile().getAbsolutePath());
				println("\tFile \"" + restrictedFilePath + "\" has been restricted.");
				if(args.length >= 3 && args[args.length - 2].equalsIgnoreCase("-allowedIP")) {
					final String ip = args[args.length - 1];
					restrictedFile.addIPAddress(ip);
					println("\tAdded IP address \"" + ip + "\" to restricted file \"" + restrictedFilePath + "\"'s whitelist.\r\n\tAnyone with that external IP address may now access the file.");
				}
			} else {
				println("\tCommand usage: \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\"");
			}
		} else if(command.equalsIgnoreCase("viewSource")) {
			File src = ResourceFactory.getResourceFromStreamAsFile(JavaWebServer.rootDir, "src.zip");
			if(src.exists()) {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(src);
						println("\tFile \"" + src.getName() + "\" opened.");
					} catch(Throwable e) {
						printErrln("\tUnable to open file \"" + src.getName() + "\": " + e.getMessage());
					}
				} else {
					println("\tYou can find the source code of this server located in the file \"" + src.getAbsolutePath() + "\".");
				}
			}
		} else if(command.equalsIgnoreCase("help") || command.equals("?")) {
			if(args.length == 0) {
				println("\tHelp for Command \"threadcount\": \"threadcount\" Displays all active threads.");
				println("\tHelp for Command \"cls\": \"cls\" Attempts to clear the console.");
				println("\tHelp for Command \"showwindow\": \"showwindow\" Shows the GUI if it is hidden.");
				println("\tHelp for Command \"hidewindow\": \"hidewindow\" Hides the GUI if it is being displayed.");
				println("\tHelp for Command \"exit\": \"exit\" Saves all data to file and shuts down this server.");
				println("\tHelp for Command \"restrictFile\": \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\" Restricts a file and/or adds an ip address to its whitelist, only allowing ip addresses in the whitelist to access the file.");
				println("\tHelp for Command \"viewSource\": \"viewSource\" extracts \"src.zip\" in the root directory then attempts to open it with the operating system's default program.");
				println("\tHelp for Command \"help\": \"help\" -OR- \"help [command]\"");
			} else if(args.length == 1) {
				if(args[0].equalsIgnoreCase("threadcount")) {
					println("\tHelp for Command \"threadcount\": \"threadcount\" Displays all active threads.");
				} else if(args[0].equalsIgnoreCase("cls")) {
					println("\tHelp for Command \"cls\": \"cls\" Attempts to clear the console.");
				} else if(args[0].equalsIgnoreCase("showwindow")) {
					println("\tHelp for Command \"showwindow\": \"showwindow\" Shows the GUI if it is hidden");
				} else if(args[0].equalsIgnoreCase("hidewindow")) {
					println("\tHelp for Command \"hidewindow\": \"hidewindow\" Hides the GUI if it is being displayed");
				} else if(args[0].equalsIgnoreCase("exit")) {
					println("\tHelp for Command \"exit\": \"exit\" Saves all data to file and shuts down this server.");
				} else if(args[0].equalsIgnoreCase("restrictfile")) {
					println("\tHelp for Command \"restrictFile\": \"restrictFile C:\\Some\\file\\path.txt\" -OR- \"restrictFile C:\\Some\\file\\path.txt -allowedIP 0.0.0.0\" Restricts a file and/or adds an ip address to its whitelist, only allowing ip addresses in the whitelist to access the file.");
				} else if(args[0].equalsIgnoreCase("viewSource")) {
					println("\tHelp for Command \"viewSource\": \"viewSource\" extracts \"src.zip\" in the root directory then attempts to open it with the operating system's default program.");
				} else if(args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
					println("\tYou silly, you just used the help command!");
					println("\tHelp for Command \"help\": \"help\" -OR- \"help [command]\"");
				} else {
					println("\tNo help found for command \"" + args[0] + "\".");
				}
			} else {
				println("\tCommand usage: \"help\" -OR- \"help [command]\"");
			}
		} else if(command.equalsIgnoreCase("savesettings") && args.length == 0) {
			JavaWebServer.saveOptionsToFile(true);
		} else if(command.equalsIgnoreCase("loadsettings") && args.length == 0) {
			JavaWebServer.loadOptionsFromFile(true);
		} else {
			println("\tUnknown command. Type \"help\" for help.");
		}
		PrintUtil.printToConsole();
		PrintUtil.printErrToConsole();
	}
	
}
