package com.gmail.br45entei.gui;

import com.gmail.br45entei.ConsoleThread;
import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.DisposableByteArrayOutputStream;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.data.Property;
import com.gmail.br45entei.server.data.php.PhpResult;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.WindowsClassicThemeDetector;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class ConsoleWindow {
	
	protected Shell shell;
	protected Text txtInputfield;
	protected StyledText consoleText;
	protected String consoleFontName = "Consolas";
	protected int consoleFontSize = 8;
	protected boolean consoleFontBold = false;
	protected boolean consoleFontStrikeout = false;
	protected boolean consoleFontUnderLined = false;
	protected boolean consoleFontItalicized = false;
	private Label lblCommand;
	protected Button scrollLck;
	
	protected boolean isScrollLocked = false;
	protected boolean showDebugText = false;
	
	protected final DisposableByteArrayOutputStream out = new DisposableByteArrayOutputStream();
	protected final Property<String> outTxt = new Property<>("Console Text", "");
	protected volatile Thread outTxtUpdateThread = null;
	protected volatile boolean outTxtUpdateThreadShouldRun = true;
	
	protected final HashMap<Integer, String> inputtedCommands = new HashMap<>();
	protected final Property<Integer> selectedCommand = new Property<>("Selected Command", Integer.valueOf(0));
	
	protected final File consoleSettingsFile = new File(JavaWebServer.rootDir, "consoleFontSettings.txt");
	
	public ConsoleWindow() {
		this.resetOutTxtUpdateThread(false);
	}
	
	@SuppressWarnings("deprecation")
	private final void resetOutTxtUpdateThread(boolean startThreadNow) {
		if(this.outTxtUpdateThread != null) {
			this.outTxtUpdateThreadShouldRun = false;
			long startTime = System.currentTimeMillis();
			while(this.outTxtUpdateThread.isAlive()) {
				Main.getInstance().runLoopNoWindow();
				if(System.currentTimeMillis() - startTime >= 3000L) {
					try {
						try {
							this.outTxtUpdateThread.stop();
						} catch(Error | RuntimeException iFrigginSaidIgnored) {
							this.outTxtUpdateThread.interrupt();
						} catch(Throwable ignored) {
							this.outTxtUpdateThread.interrupt();
						}
					} catch(Error | RuntimeException iFrigginSaidIgnored) {
					} catch(Throwable ignored) {
					}
					break;
				}
			}
			this.outTxtUpdateThreadShouldRun = false;
			this.outTxtUpdateThread = null;
		}
		this.outTxtUpdateThread = new Thread(new Runnable() {
			@Override
			public final void run() {
				int lastBufferSize = 0;
				while(ConsoleWindow.this.outTxtUpdateThreadShouldRun) {
					if(ConsoleWindow.this.out.size() != lastBufferSize) {
						lastBufferSize = ConsoleWindow.this.out.size();
						try {
							final String text;
							String t = ConsoleWindow.this.out.toString().replaceAll(Pattern.quote("Â"), Matcher.quoteReplacement(""));
							Functions.sleep();
							if(LogUtils.isConsoleMode()) {
								String random = ((Object) "".toCharArray()).toString();
								t = t.replace(LogUtils.carriageReturn() + LogUtils.getConsolePrefixChar(), "").replace(LogUtils.carriageReturn() + "\n", random).replace(LogUtils.carriageReturn(), "\n").replace("\n\n", "\n").replace(random, LogUtils.carriageReturn() + "\n").replace("\n\n", "\n");
							}
							Functions.sleep();
							final int maxLines = 2000;//10000;
							final int numOfLines = StringUtils.getNumOfLinesInStr(t);
							Functions.sleep();
							if(numOfLines > maxLines) {
								final int numOfLinesToSkip = numOfLines - maxLines;
								int i = 0;
								String[] split = t.split(Pattern.quote("\n"));
								t = "";
								long startTime = System.currentTimeMillis();
								for(String s : split) {
									i++;
									if(i < numOfLinesToSkip) {
										Functions.sleep();
										startTime = System.currentTimeMillis();
										continue;
									}
									//if(i >= numOfLinesToSkip) {
									t += s + "\n";
									//}
									if(System.currentTimeMillis() - startTime >= 50L) {
										Functions.sleep();
										startTime = System.currentTimeMillis();
									}
									if(!ConsoleWindow.this.outTxtUpdateThreadShouldRun) {
										break;
									}
								}
							}
							if(!ConsoleWindow.this.outTxtUpdateThreadShouldRun) {
								break;
							}
							text = t + LogUtils.getConsolePrefixChar();
							ConsoleWindow.this.outTxt.setValue(text);
						} catch(Throwable e) {
							e.printStackTrace();
						}
					}
					Functions.sleep(20L);
				}
			}
		}, "ConsoleWindow_UpdateThread");
		this.outTxtUpdateThread.setDaemon(true);
		if(startThreadNow) {
			this.outTxtUpdateThread.start();
		}
	}
	
	/** Launch the application.
	 * 
	 * @param args System command arguments */
	public static void main(String[] args) {
		try {
			ConsoleWindow window = new ConsoleWindow();
			LogUtils.replaceSystemOut();
			LogUtils.replaceSystemErr();
			LogUtils.setTertiaryOutStream(window.out);
			LogUtils.setTertiaryErrStream(window.out);
			window.open();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/** @return This window's shell */
	public final Shell getShell() {
		return this.shell;
	}
	
	/** Open the window. */
	public void open() {
		@SuppressWarnings("unused")
		Display display = Display.getDefault();
		createContents();
		boolean showWindow = true;
		if(Main.getInstance() != null) {
			showWindow = Main.getInstance().showWindow;
		}
		if(showWindow) {
			this.shell.open();
		}
		this.shell.layout();
		while(!this.shell.isDisposed()) {
			this.runLoop();
		}
	}
	
	protected final void runLoop() {
		if(this.shell.isDisposed()) {
			return;
		}
		this.runClock();
		this.updateUI();
		this.runClock();
	}
	
	protected final void runClock() {
		if(this.shell.isDisposed()) {
			return;
		}
		if(Main.getInstance() != null) {
			Main.exitCheck();
		}
		if(this.shell.isVisible()) {
			if(!this.shell.getDisplay().readAndDispatch()) {
				//display.sleep();
				Functions.sleep();//CodeUtil.sleep(1L);
			}
			return;
		}
		CodeUtil.sleep(10L);
	}
	
	protected final String scrollLckToolTipText = "Toggles the scroll lock, which keeps the scroll bar\r\nin the same place when new text is printed to the console.\r\nUseful if you are trying to read something.\r\n";
	
	/** Create contents of the window. */
	protected void createContents() {
		this.outTxtUpdateThread.setDaemon(true);
		this.outTxtUpdateThread.start();
		this.shell = new Shell(SWT.SHELL_TRIM);
		this.shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
				JavaWebServer.shutdown();
			}
		});
		this.shell.setImages(new Image[] {SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-16x16.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-32x32.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-64x64.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-128x128.png")});
		this.shell.setSize(617, 387);
		this.shell.setText("JavaWebServer Console Window");
		this.shell.setLayout(null);
		
		this.txtInputfield = new Text(this.shell, SWT.BORDER);
		final KeyAdapter inputKeyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				try {
					if(event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
						event.doit = false;
						int size = ConsoleWindow.this.inputtedCommands.size();
						int pos = ConsoleWindow.this.selectedCommand.getValue().intValue();
						if(event.keyCode == SWT.ARROW_UP) {
							if(pos - 1 >= 0) {
								ConsoleWindow.this.selectedCommand.setValue(Integer.valueOf(pos - 1));
							}
						} else if(event.keyCode == SWT.ARROW_DOWN) {
							if(pos + 1 < size) {
								ConsoleWindow.this.selectedCommand.setValue(Integer.valueOf(pos + 1));
							}
						}
						pos = ConsoleWindow.this.selectedCommand.getValue().intValue();
						if(pos < 0 || pos >= size) {
							if(pos < 0) {
								pos = 0;
							} else if(pos >= size) {
								pos = size - 1;
							}
							ConsoleWindow.this.selectedCommand.setValue(Integer.valueOf(pos));
						}
						String text = ConsoleWindow.this.inputtedCommands.get(ConsoleWindow.this.selectedCommand.getValue());
						if(text == null) {
							text = "";
						}
						ConsoleWindow.this.txtInputfield.setText(text);
						ConsoleWindow.this.txtInputfield.setFocus();
						ConsoleWindow.this.txtInputfield.setSelection(text.length());
					} else if(event.character == SWT.CR) {
						event.doit = false;
						final String input = ConsoleWindow.this.txtInputfield.getText();
						final Integer key = Integer.valueOf(ConsoleWindow.this.inputtedCommands.size());
						String lastInput = ConsoleWindow.this.inputtedCommands.get(ConsoleWindow.this.selectedCommand.getValue());
						if(!input.equals(lastInput)) {
							ConsoleWindow.this.selectedCommand.setValue(key);
							ConsoleWindow.this.inputtedCommands.put(key, input);
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
						if(ConsoleWindow.this.showDebugText) {
							if(command.equalsIgnoreCase("done")) {
								ConsoleWindow.this.showDebugText = false;
							}
							ConsoleWindow.this.txtInputfield.setText("");
							return;
						} else if(command.equalsIgnoreCase("debug")) {
							ConsoleWindow.this.showDebugText = true;
							ConsoleWindow.this.txtInputfield.setText("");
							return;
						}
						if(command.equalsIgnoreCase("cls") && args.length == 0) {
							ConsoleWindow.this.out.dispose();
							ConsoleWindow.this.txtInputfield.setText("");
							return;
						}
						if(command.toLowerCase().startsWith("setfont")) {
							if(command.equalsIgnoreCase("setfont")) {
								if(args.length >= 1) {
									if(args.length == 1 && args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
										PrintUtil.printlnNow("Font commands:\r\nsetFont {font name]\r\nsetFontSize {size}\r\nsetFontBold {true|false}\r\nsetFontItalic {true|false}\r\nsetFontUnderline {true|false}\r\nsetFontStrikeout {true|false}\r\nsaveFontSettings");
									} else {
										ConsoleWindow.this.consoleFontName = StringUtils.stringArrayToString(args, ' ');
									}
								}
							} else if(command.equalsIgnoreCase("setfontsize")) {
								if(args.length == 1) {
									if(StringUtils.isStrLong(args[0])) {
										int size = Long.valueOf(args[0]).intValue();
										if(size >= 0) {
											ConsoleWindow.this.consoleFontSize = size;
										} else {
											PrintUtil.printlnNow("Font sizes must be positive.");
										}
									} else {
										PrintUtil.printlnNow("\"" + args[0] + "\" is not a valid integer.");
									}
								} else {
									PrintUtil.printlnNow("Usage: \"setFontSize {size}\"");
								}
							} else if(command.equalsIgnoreCase("setfontbold")) {
								if(args.length == 1) {
									ConsoleWindow.this.consoleFontBold = Boolean.valueOf(args[0]).booleanValue();
								} else {
									PrintUtil.printlnNow("Usage: \"setFontbold {true|false}\"");
								}
							} else if(command.equalsIgnoreCase("setfontitalic")) {
								if(args.length == 1) {
									ConsoleWindow.this.consoleFontItalicized = Boolean.valueOf(args[0]).booleanValue();
								} else {
									PrintUtil.printlnNow("Usage: \"setFontItalic {true|false}\"");
								}
							} else if(command.equalsIgnoreCase("setfontunderline")) {
								if(args.length == 1) {
									ConsoleWindow.this.consoleFontUnderLined = Boolean.valueOf(args[0]).booleanValue();
								} else {
									PrintUtil.printlnNow("Usage: \"setFontUnderline {true|false}\"");
								}
							} else if(command.equalsIgnoreCase("setfontstrikeout")) {
								if(args.length == 1) {
									ConsoleWindow.this.consoleFontStrikeout = Boolean.valueOf(args[0]).booleanValue();
								} else {
									PrintUtil.printlnNow("Usage: \"setFontStrikeout {true|false}\"");
								}
							} else {
								PrintUtil.printlnNow("Unknown font command. Type \"setfont ?\" for help.");
							}
							updateConsoleFont();
						} else if(command.equalsIgnoreCase("savefontsettings")) {
							if(saveConsoleSettings()) {
								PrintUtil.printlnNow("Font settings saved.");
							} else {
								PrintUtil.printlnNow("Something went wrong when saving the font settings.");
							}
						} else if(command.equalsIgnoreCase("loadfontsettings")) {
							if(loadConsoleSettings()) {
								PrintUtil.printlnNow("Font settings loaded.");
							} else {
								PrintUtil.printlnNow("Something went wrong when loading the font settings.");
							}
						} else if(command.equalsIgnoreCase("options")) {
							if(Main.getInstance() != null ? !Main.getInstance().isADialogOpen : true) {
								if(Main.getInstance() != null) {
									Main.getInstance().isADialogOpen = true;
								}
								ConsoleWindow.this.txtInputfield.setText("");
								new OptionsDialog(ConsoleWindow.this.shell, SWT.DIALOG_TRIM).open();
								if(Main.getInstance() != null) {
									Main.getInstance().isADialogOpen = false;
								}
								return;
							}
						} else {
							if(LogUtils.isConsolePresent()) {
								LogUtils.ORIGINAL_SYSTEM_OUT.print(LogUtils.carriageReturn() + LogUtils.getConsolePrefixChar() + input + "\n" + (LogUtils.carriageReturn() + LogUtils.getConsolePrefixChar()));
							}
							if(Main.getInstance() != null && !input.trim().isEmpty()) {
								try {
									ConsoleThread.handleInput(input, Main.getConsoleThread());
								} catch(Throwable e) {
									e.printStackTrace();
								}
							} else {
								try {
									ConsoleWindow.this.out.write(((LogUtils.isConsoleMode() && !input.isEmpty() ? "" : LogUtils.getConsolePrefixChar()) + input).getBytes());
								} catch(IOException ignored) {
								}
							}
						}
						ConsoleWindow.this.txtInputfield.setText("");
					}
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		};
		this.txtInputfield.addKeyListener(inputKeyAdapter);
		this.txtInputfield.setBounds(64, this.shell.getSize().y - 59, this.shell.getSize().x - 144, 19);
		
		this.lblCommand = new Label(this.shell, SWT.NONE);
		this.lblCommand.setBounds(0, this.shell.getSize().y - 56, 58, 13);
		this.lblCommand.setText("Command:");
		
		this.consoleText = new StyledText(this.shell, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		//this.consoleText.setContent(new CustomStyledTextContent());
		this.consoleText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.CR) {// || e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
					inputKeyAdapter.keyPressed(e);
					return;
				}
				if(e.character < 32 && e.character != SWT.BS) {
					return;
				}
				e.doit = false;
				String text = ConsoleWindow.this.txtInputfield.getText();
				if(e.character == SWT.BS) {
					if(!text.isEmpty()) {
						text = text.substring(0, text.length() - 1);
					}
				} else {
					text += e.character;
				}
				ConsoleWindow.this.txtInputfield.setText(text);
				//ConsoleWindow.this.txtInputfield.setFocus();
				ConsoleWindow.this.txtInputfield.setSelection(ConsoleWindow.this.txtInputfield.getText().length());
			}
		});
		this.loadConsoleSettings();
		this.updateConsoleFont();
		this.consoleText.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.consoleText.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		this.consoleText.setSize(this.shell.getSize().x - 20, this.shell.getSize().y - 62);
		
		this.scrollLck = new Button(this.shell, SWT.NONE);
		this.scrollLck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConsoleWindow.this.isScrollLocked = !ConsoleWindow.this.isScrollLocked;
				ConsoleWindow.this.scrollLck.setToolTipText(ConsoleWindow.this.scrollLckToolTipText + "Scroll Lock is " + (ConsoleWindow.this.isScrollLocked ? "on" : "off") + ".");
			}
		});
		this.scrollLck.setToolTipText(this.scrollLckToolTipText + "Scroll Lock is off.");
		this.scrollLck.setBounds(this.shell.getSize().x - 74, this.shell.getSize().y - 59, 54, 19);
		this.scrollLck.setText("Scroll Lck");
		
	}
	
	protected final boolean loadConsoleSettings() {
		if(!this.consoleSettingsFile.exists()) {
			return saveConsoleSettings();
		}
		try(BufferedReader br = new BufferedReader(new FileReader(this.consoleSettingsFile))) {
			while(br.ready()) {
				String line = br.readLine();
				String[] split = line.trim().split(Pattern.quote("="));
				if(split.length == 2) {
					String pname = split[0].trim();
					String value = split[1].trim();
					if(pname.equalsIgnoreCase("fontName")) {
						this.consoleFontName = value;
					} else if(pname.equalsIgnoreCase("fontSize")) {
						if(StringUtils.isStrLong(value)) {
							this.consoleFontSize = Long.valueOf(value).intValue();
						}
					} else if(pname.equalsIgnoreCase("fontBold")) {
						this.consoleFontBold = Boolean.valueOf(value).booleanValue();
					} else if(pname.equalsIgnoreCase("fontItalicized")) {
						this.consoleFontItalicized = Boolean.valueOf(value).booleanValue();
					} else if(pname.equalsIgnoreCase("fontStrikeout")) {
						this.consoleFontStrikeout = Boolean.valueOf(value).booleanValue();
					} else if(pname.equalsIgnoreCase("fontUnderlined")) {
						this.consoleFontUnderLined = Boolean.valueOf(value).booleanValue();
					}
				}
			}
			updateConsoleFont();
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	protected final boolean saveConsoleSettings() {
		try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.consoleSettingsFile), StandardCharsets.UTF_8), true)) {
			pr.println("fontName=" + this.consoleFontName);
			pr.println("fontSize=" + this.consoleFontSize);
			pr.println("fontBold=" + this.consoleFontBold);
			pr.println("fontItalicized=" + this.consoleFontItalicized);
			pr.println("fontStrikeout=" + this.consoleFontStrikeout);
			pr.println("fontUnderLined=" + this.consoleFontUnderLined);
			return true;
		} catch(Throwable ignored) {
			return false;
		}
	}
	
	protected final void updateConsoleFont() {
		if(this.shell.isDisposed()) {
			return;
		}
		final Font font = SWTResourceManager.getFont(this.consoleFontName, this.consoleFontSize, (this.consoleFontItalicized ? SWT.ITALIC : SWT.NORMAL), this.consoleFontStrikeout, this.consoleFontUnderLined);
		if(font != null) {
			if(this.consoleFontBold) {
				this.consoleText.setFont(SWTResourceManager.getBoldFont(font));
			} else {
				this.consoleText.setFont(font);
			}
		} else {
			PrintUtil.printErrlnNow("The font \"" + ConsoleWindow.this.consoleFontName + "\" was not found or did not load.");
		}
	}
	
	protected final void updateUI() {
		if(this.shell.isDisposed()) {
			return;
		}
		final int themeOffset = WindowsClassicThemeDetector.isThemeWindowsAero() ? 12 : 0;
		this.consoleText.setSize(this.shell.getSize().x - (8 + themeOffset), this.shell.getSize().y - (50 + themeOffset));
		this.lblCommand.setBounds(0, this.shell.getSize().y - (44 + themeOffset), 58, 13);
		this.txtInputfield.setBounds(64, this.shell.getSize().y - (47 + themeOffset), this.shell.getSize().x - (132 + themeOffset), 19);
		this.scrollLck.setBounds(this.shell.getSize().x - (62 + themeOffset), this.shell.getSize().y - (47 + themeOffset), 54, 19);
		String text = this.showDebugText ? this.getDebugText() : this.outTxt.getValue();
		if(text == null) {
			text = "";
		}
		this.setConsoleText(text);
	}
	
	private String lastDebugText = null;
	private long lastDebugTime = System.currentTimeMillis();
	
	protected final String getDebugText() {
		if(this.shell.isDisposed()) {
			return "Shell is disposed! How are you reading this?!!!1";
		}
		if((System.currentTimeMillis() - this.lastDebugTime) < 1000L && this.lastDebugText != null) {
			return this.lastDebugText;
		}
		String text = "You are viewing the debug screen.\r\nType \"done\" to return to the console view.\r\n\r\n";
		text += "Server active: " + JavaWebServer.serverActive() + "\r\n";
		text += "Server is shutting down: " + Boolean.toString(JavaWebServer.isShuttingDown()) + "\r\n";
		text += "Root directory: \"" + JavaWebServer.rootDir.getAbsolutePath() + "\"\r\n";
		text += "System-dependant number of threads per cpu(1000 times number of processors): " + JavaWebServer.fNumberOfThreads + "\r\n";
		text += "Override default thread pool size: " + JavaWebServer.overrideThreadPoolSize + "\r\n";
		text += "fThreadPool size: " + JavaWebServer.getfThreadPoolCoreSize() + "\r\n";
		text += "fSSLThreadPool size: " + JavaWebServer.getfSSLThreadPoolCoreSize() + "\r\n";
		text += "HTTP listen port: " + JavaWebServer.listen_port + "\r\n";
		text += "SSL server enabled: " + JavaWebServer.enableSSLThread + "\r\n";
		text += "SSL listen port: " + JavaWebServer.ssl_listen_port + "\r\n";
		text += "SSL Store type: \"" + (JavaWebServer.sslStore_KeyOrTrust ? "KEY" : "TRUST") + "\"\r\n";
		text += "Administration interface enabled: " + JavaWebServer.enableAdminInterface + "\r\n";
		text += "Administration interface listen port: " + JavaWebServer.admin_listen_port + "\r\n";
		text += "Default server home directory: \"" + JavaWebServer.homeDirectory.getAbsolutePath() + "\"\r\n";
		text += "Calculate directory sizes: " + JavaWebServer.calculateDirectorySizes + "\r\n";
		text += "Default font face: " + JavaWebServer.defaultFontFace + "\r\n";
		text += "PHP-CGI executable: \"" + PhpResult.phpExeFilePath + "\"\r\n";
		text += "Request time out(in milliseconds): " + JavaWebServer.requestTimeout + "\r\n";
		text += "Proxy server enabled: " + JavaWebServer.isProxyServerEnabled() + "\r\n";
		text += "Send proxy headers with requests: " + JavaWebServer.sendProxyHeadersWithRequest + "\r\n";
		text += "Proxy server requires authentication: " + JavaWebServer.proxyRequiresAuthorization + "\r\n";
		text += "\r\n============\r\n\r\nConsole Window Info:\r\n";
		text += "Console font face: \"" + this.consoleFontName + "\"\r\n";
		text += "Console font size: " + this.consoleFontSize + "\r\n";
		text += "Console input text: \"" + this.txtInputfield.getText() + "\"\r\n";
		text += "Console input text position: " + this.selectedCommand.getValue() + "\r\n";
		text += "Number of inputted commands: " + this.inputtedCommands.size() + "\r\n";
		text += "\r\n============\r\n\r\nCurrent Thread Info:\r\n";
		this.runClock();
		final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
		for(Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			text += "=====Thread: name: \"" + entry.getKey().getName() + "\"; state: \"" + entry.getKey().getState().toString() + "\"; CPU Time: " + StringUtils.getElapsedTime(tmxb.getThreadCpuTime(entry.getKey().getId()) / 1000000, true) + "; StackTrace:\r\n" + LogUtils.stackTraceElementsToStr(entry.getValue()) + "\r\n";
			this.runClock();
		}
		text += "\r\n============";
		this.lastDebugTime = System.currentTimeMillis();
		this.lastDebugText = text;
		return text;
	}
	
	protected final void setConsoleText(final String text) {
		if(this.shell.isDisposed()) {
			return;
		}
		try {
			if(!this.consoleText.getText().equals(text)) {
				final int numOfVisibleLines = Math.floorDiv(this.consoleText.getSize().y, this.consoleText.getLineHeight());
				final int originalIndex = this.consoleText.getTopIndex();
				int index = originalIndex;
				final int lineCount = this.consoleText.getLineCount();
				if(HTTPClientRequest.debug) {
					this.txtInputfield.setText("index: \"" + index + "\"; line count: \"" + lineCount + "\"; visible lines: \"" + numOfVisibleLines + "\";");
				}
				runClock();
				if(lineCount - index == numOfVisibleLines) {
					index = -1;
				}
				final Point selection = this.consoleText.getSelection();
				final int caretOffset = this.consoleText.getCaretOffset();
				//==
				//this.consoleText.setText(text);
				this.consoleText.getContent().setText(text);
				//==
				try {
					if(caretOffset == selection.x) {//Right to left text selection
						this.consoleText.setCaretOffset(caretOffset);
						this.consoleText.setSelection(selection.y, selection.x);
					} else {//Left to right text selection
						this.consoleText.setSelection(selection);
						this.consoleText.setCaretOffset(caretOffset);
					}
				} catch(IllegalArgumentException ignored) {
				}
				final int newLineCount = this.consoleText.getLineCount();
				if(index == -1) {
					index = newLineCount - 1;
				} else {
					if(newLineCount >= lineCount) {
						index = newLineCount - (lineCount - index);
					} else {
						index = newLineCount - (newLineCount - index);
					}
				}
				this.consoleText.setTopIndex(this.isScrollLocked ? originalIndex : index);
				runClock();
			}
		} catch(Throwable ignored) {
			if(HTTPClientRequest.debug) {
				ignored.printStackTrace();
			}
		}
	}
	
	/** Clears the screen */
	public void cls() {
		this.out.resetAll();
		this.resetOutTxtUpdateThread(true);
		this.out.resetAll();
	}
	
}
