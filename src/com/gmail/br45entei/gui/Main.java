package com.gmail.br45entei.gui;

import com.gmail.br45entei.ConsoleThread;
import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.ResourceFactory;
import com.gmail.br45entei.server.ClientInfo;
import com.gmail.br45entei.server.ClientRequestStatus;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.Response;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.LogUtils;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtils;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class Main {
	protected static boolean	enableWindowThread	= true;
	
	//protected Display			display;
	protected Shell				shell;
	private static Main			instance;
	private Thread				currentThread;
	protected Thread			serverThread;
	protected ConsoleThread		consoleThread;
	
	protected boolean			isADialogOpen		= false;
	
	private boolean				isEnabled			= true;
	
	private final ConsoleWindow	consoleWindow;
	private boolean				showConsoleWindow	= false;
	
	/** Whether or not the window is supposed to be displayed. */
	public boolean				showWindow			= true;
	private Label				lblActiveConnections;
	private ScrolledComposite	outgoingScrollArea;
	private Composite			outgoingActiveConnections;
	private ScrolledComposite	incomingScrollArea;
	private Composite			incomingActiveConnections;
	private TabFolder			tabFolder;
	private TabItem				tbOutgoingConnections;
	private TabItem				tbtmIncomingConnections;
	private TabItem				tbtmProxyConnections;
	private Label				lblNoOutgoingConnections;
	private Label				lblNoIncomingConnections;
	private ScrolledComposite	proxyScrollArea;
	private Composite			activeProxyConnections;
	private Label				lblNoProxyConnections;
	
	public final Thread getThread() {
		return this.currentThread;
	}
	
	public final ConsoleThread getConsoleThread() {
		return this.consoleThread;
	}
	
	public static final Main getInstance() {
		return instance;
	}
	
	public final boolean isEnabled() {
		return this.isEnabled;
	}
	
	public final boolean isSWTConsoleWindowOpen() {
		return this.showConsoleWindow;
	}
	
	public final ConsoleWindow getConsoleWindow() {
		return this.consoleWindow != null ? this.consoleWindow : null;
	}
	
	/** Launch the application.
	 * 
	 * @param args System command arguments */
	public static void main(final String[] args) {
		ResourceFactory.getResourceFromStreamAsFile(JavaWebServer.rootDir, "legal/LICENSE.txt");
		LogUtils.replaceSystemOut();
		LogUtils.replaceSystemErr();
		try {
			Main window = new Main(args);
			instance = window;
			PrintUtil.printlnNow(JavaWebServer.TERMINAL_NOTICE);
			if(enableWindowThread) {
				window.showWindow = !StringUtils.containsIgnoreCase("nogui", args);
				window.open(args);
			} else {
				window.serverThread.start();
				window.consoleThread.start();
				while(JavaWebServer.serverActive()) {
					window.exitCheck();
					CodeUtil.sleep(10L);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Main(final String[] args) {
		instance = this;
		this.showConsoleWindow = System.console() == null || StringUtils.containsIgnoreCase("console", args);
		this.consoleWindow = new ConsoleWindow();
		if(this.showConsoleWindow) {
			this.consoleWindow.createContents();
			this.consoleWindow.shell.open();
			this.consoleWindow.shell.layout();
			LogUtils.setTertiaryOutStream(this.consoleWindow.out);
			LogUtils.setTertiaryErrStream(this.consoleWindow.out);
		}
		this.serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				JavaWebServer.sysMain(args);
			}
		}, JavaWebServer.ThreadName);
		this.consoleThread = new ConsoleThread();
	}
	
	/** Open the window.
	 * 
	 * @param args System command arguments
	 * @wbp.parser.entryPoint */
	private void open(final String[] args) {
		@SuppressWarnings("unused")
		Display display = Display.getDefault();
		this.createContents();
		if(this.showWindow) {
			this.shell.open();
		}
		this.shell.layout();
		this.serverThread.start();
		this.consoleThread.start();
		while(!this.shell.isDisposed()) {
			if(!this.isEnabled) {
				break;
			}
			this.runLoop();
		}
		if(!this.shell.isDisposed()) {
			this.shell.dispose();
		}
		System.out.println("End of main window thread.");
	}
	
	public final void runLoop() {
		if(this.shell.isDisposed()) {
			return;
		}
		this.runClock();
		this.updateUI();
		if(this.showConsoleWindow) {
			this.consoleWindow.runLoop();
		}
	}
	
	protected final void exitCheck() {
		if(this.consoleThread.exitCommandWasRun) {
			this.consoleThread.exitCommandWasRun = false;
			JavaWebServer.getInstance().shutdown();
		}
	}
	
	/** Runs {@link Display#readAndDispatch()},
	 * then attempts to sleep. */
	protected final void runClock() {
		if(this.shell.isDisposed()) {
			return;
		}
		this.exitCheck();
		if(this.shell.isVisible()) {
			if(!this.shell.getDisplay().readAndDispatch()) {
				//display.sleep();
				CodeUtil.sleep(1L);
			}
			return;
		}
		CodeUtil.sleep(10L);
	}
	
	/** Keeps window events updated such as active server/client transfers */
	protected final void updateUI() {
		if(this.shell.isDisposed()) {
			return;
		}
		if(this.showWindow) {
			if(!this.shell.isVisible()) {
				this.shell.setVisible(true);
				this.shell.open();
			}
		} else if(!this.showWindow) {
			if(this.shell.isVisible()) {
				this.shell.setVisible(false);
			}
			while(!this.showWindow) {
				this.runClock();
				if(this.showConsoleWindow) {
					this.consoleWindow.runLoop();
				}
			}
		}
		if(!this.shell.isDisposed()) {
			ArrayList<ClientInfo> clientInfos = getCurrentOutgoingClientInfos();
			boolean ranClock = false;
			for(ClientInfo info : clientInfos) {
				if(info != null) {
					this.getCompositeForOutgoingConnection(info).isInList = true;//updates the composite and it's children.
					ranClock = true;
				}
			}
			final boolean noActiveConnections = this.outgoingActiveConnections.getChildren().length <= 1;//There's always a label in there, so 1 instead of zero
			if(noActiveConnections) {
				if(!this.lblNoOutgoingConnections.isVisible()) {
					this.lblNoOutgoingConnections.setVisible(true);
				}
			} else {
				if(this.lblNoOutgoingConnections.isVisible()) {
					this.lblNoOutgoingConnections.setVisible(false);
				}
				for(Control control : this.outgoingActiveConnections.getChildren()) {
					if(control instanceof CompositeInfo) {
						CompositeInfo info = (CompositeInfo) control;
						if(!info.isInList) {
							info.dispose();
						} else {
							info.isInList = false;
						}
					}
				}
			}
			ArrayList<ClientRequestStatus> clientRequestStatuses = getCurrentIncomingClientInfos();
			for(ClientRequestStatus info : clientRequestStatuses) {
				if(info != null) {
					this.getCompositeForConnection(info, this.incomingActiveConnections).isInList = true;
					ranClock = true;
				}
			}
			final boolean noActiveIncomingConnections = this.incomingActiveConnections.getChildren().length <= 1;
			if(noActiveIncomingConnections) {
				if(!this.lblNoIncomingConnections.isVisible()) {
					this.lblNoIncomingConnections.setVisible(true);
				}
			} else {
				if(this.lblNoIncomingConnections.isVisible()) {
					this.lblNoIncomingConnections.setVisible(false);
				}
				for(Control control : this.incomingActiveConnections.getChildren()) {
					if(control instanceof CompositeInfo) {
						CompositeInfo info = (CompositeInfo) control;
						if(!info.isInList) {
							info.dispose();
						} else {
							info.isInList = false;
						}
					}
				}
			}
			ArrayList<ClientRequestStatus> clientProxyStatuses = getCurrentProxyClientInfos();
			for(ClientRequestStatus info : clientProxyStatuses) {
				if(info != null) {
					this.getCompositeForConnection(info, this.activeProxyConnections).isInList = true;
					ranClock = true;
				}
			}
			final boolean noActiveProxyConnections = this.activeProxyConnections.getChildren().length <= 1;
			if(noActiveProxyConnections) {
				if(!this.lblNoProxyConnections.isVisible()) {
					this.lblNoProxyConnections.setVisible(true);
				}
			} else {
				if(this.lblNoProxyConnections.isVisible()) {
					this.lblNoProxyConnections.setVisible(false);
				}
				for(Control control : this.activeProxyConnections.getChildren()) {
					if(control instanceof CompositeInfo) {
						CompositeInfo info = (CompositeInfo) control;
						if(!info.isInList) {
							info.dispose();
						} else {
							info.isInList = false;
						}
					}
				}
			}
			if(!ranClock) {
				this.runClock();//Prevents the window from freezing after being resized
			}
			
			final int numOfOutgoingComposites = getNumOfCompositeInfosInComposite(this.outgoingActiveConnections);
			final int numOfIncomingComposites = getNumOfCompositeInfosInComposite(this.incomingActiveConnections);
			final int numOfProxyComposites = getNumOfCompositeInfosInComposite(this.activeProxyConnections);
			
			final String newOutgoingText = "Outgoing Connections" + (numOfOutgoingComposites > 0 ? "[" + numOfOutgoingComposites + "]" : "");
			final String newIncomingText = "Incoming Connections" + (numOfIncomingComposites > 0 ? "[" + numOfIncomingComposites + "]" : "");
			final String newProxyText = JavaWebServer.isProxyServerEnabled() ? "Proxy Connections" + (numOfProxyComposites > 0 ? "[" + numOfProxyComposites + "]" : "") : "(Proxy Server Disabled)";
			if(!this.tbOutgoingConnections.getText().equals(newOutgoingText)) {
				this.tbOutgoingConnections.setText(newOutgoingText);
			}
			if(!this.tbtmIncomingConnections.getText().equals(newIncomingText)) {
				this.tbtmIncomingConnections.setText(newIncomingText);
			}
			if(!this.tbtmProxyConnections.getText().equals(newProxyText)) {
				this.tbtmProxyConnections.setText(newProxyText);
			}
			
			this.tabFolder.setSize(this.shell.getSize().x - 23, this.shell.getSize().y - 124);
			this.outgoingScrollArea.setSize(this.tabFolder.getSize().x - 10, this.tabFolder.getSize().y - 30);//this.shell.getSize().x - 33, this.shell.getSize().y - 154);//this.shell.getSize().x - 23, this.shell.getSize().y - 124);//...y - 118);
			this.incomingScrollArea.setSize(this.tabFolder.getSize().x - 10, this.tabFolder.getSize().y - 30);
			this.proxyScrollArea.setSize(this.tabFolder.getSize().x - 10, this.tabFolder.getSize().y - 30);
			//this.activeConnections.setSize(422, 10 + (this.connectionHeight * getCurrentClientInfos().size()));//this.activeConnections.setBounds(/*10, 64*/0, 0, 422, /*642 + */10 + (this.connectionHeight * getCurrentClientInfos().size()));
			
			final int width = this.tabFolder.getSize().x - 30;//this.shell.getSize().x - 20;
			final int newWidth = width < 415 ? 415 : width;//width < 420 ? 420 : width;
			this.outgoingActiveConnections.setSize(noActiveConnections ? width : newWidth, 20 + (CompositeInfo.height * getCurrentOutgoingClientInfos().size()));
			this.incomingActiveConnections.setSize(noActiveIncomingConnections ? width : newWidth, 20 + (CompositeInfo.height * getCurrentIncomingClientInfos().size()));
			this.activeProxyConnections.setSize(noActiveProxyConnections ? width : newWidth, 20 + (CompositeInfo.height * getCurrentProxyClientInfos().size()));
			this.lblActiveConnections.setBounds(64, 10, this.shell.getSize().x - 92, 48);
		}
	}
	
	public final void shutdown() {
		this.isEnabled = false;
		this.consoleThread.stopThread();
	}
	
	/** Create contents of the window. */
	@SuppressWarnings("unused")
	protected void createContents() {
		this.shell = new Shell(SWT.SHELL_TRIM);
		this.shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
				if(!Main.this.isADialogOpen) {
					Main.this.isADialogOpen = true;
					Response response = new ConfirmHideWindowDialog(Main.this.shell, SWT.NORMAL).open();
					Main.this.isADialogOpen = false;
					if(response == Response.YES) {
						Main.this.showWindow = false;
					} else if(response == Response.NO) {
						Main.this.showWindow = true;
					} else if(response == Response.CLOSE) {
						JavaWebServer.getInstance().shutdown();
					}
				}
			}
		});
		this.shell.setImages(new Image[] {SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-16x16.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-32x32.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-64x64.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-128x128.png")});
		this.shell.setSize(465, 760);
		this.shell.setText(JavaWebServer.APPLICATION_NAME + " Version " + JavaWebServer.APPLICATION_VERSION + " - Made by Brian_Entei");
		Functions.centerShellOnPrimaryMonitor(this.shell);
		
		this.currentThread = Thread.currentThread();
		
		Menu menu = new Menu(this.shell, SWT.BAR);
		this.shell.setMenuBar(menu);
		
		MenuItem mntmfile = new MenuItem(menu, SWT.CASCADE);
		mntmfile.setText("&File");
		
		Menu menu_1 = new Menu(mntmfile);
		mntmfile.setMenu(menu_1);
		
		MenuItem mntmHideWindow = new MenuItem(menu_1, SWT.NONE);
		mntmHideWindow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!Main.this.isADialogOpen) {
					Main.this.isADialogOpen = true;
					if(new ConfirmHideWindowDialog(Main.this.shell, SWT.NORMAL).open() == Response.YES) {
						Main.this.showWindow = false;
					}
					Main.this.isADialogOpen = false;
				}
			}
		});
		mntmHideWindow.setText("&Hide Window(Server stays active)");
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmShutDownServer = new MenuItem(menu_1, SWT.NONE);
		mntmShutDownServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.getInstance().shutdown();
			}
		});
		mntmShutDownServer.setText("&Shut down server");
		
		MenuItem mntmoptions = new MenuItem(menu, SWT.NONE);
		mntmoptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!Main.this.isADialogOpen) {
					Main.this.isADialogOpen = true;
					new OptionsDialog(Main.this.shell, SWT.DIALOG_TRIM).open();
					Main.this.isADialogOpen = false;
				}
			}
		});
		mntmoptions.setText("&Options");
		
		MenuItem mntmabout = new MenuItem(menu, SWT.NONE);
		mntmabout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!Main.this.isADialogOpen) {
					Main.this.isADialogOpen = true;
					new AboutDialog(Main.this.shell, SWT.DIALOG_TRIM).open();
					Main.this.isADialogOpen = false;
				}
			}
		});
		mntmabout.setText("&About...");
		
		this.lblActiveConnections = new Label(this.shell, SWT.WRAP);
		this.lblActiveConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.lblActiveConnections.setBounds(64, 10, this.shell.getSize().x - 92, 48);//this.lblActiveConnections.setBounds(64, 10, 368, 48);
		this.lblActiveConnections.setText("Active Connections - The following connections represent active connections and/or file transfers. Click cancel transfer next to a connection to sever its transfer, or pause to pause it.");
		
		Label lblImage = new Label(this.shell, SWT.NONE);
		lblImage.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblImage.setImage(SWTResourceManager.getImage(Main.class, "/assets/textures/icons/internet.ico"));
		lblImage.setBounds(10, 10, 48, 48);
		
		this.tabFolder = new TabFolder(this.shell, SWT.NONE);
		this.tabFolder.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.tabFolder.setBounds(3, 64, 437, 640);
		
		this.tbOutgoingConnections = new TabItem(this.tabFolder, SWT.NONE);
		this.tbOutgoingConnections.setText("Outgoing Connections");
		
		this.outgoingScrollArea = new ScrolledComposite(this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		this.tbOutgoingConnections.setControl(this.outgoingScrollArea);
		this.outgoingScrollArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.outgoingScrollArea.setMinSize(200, 200);
		
		this.outgoingActiveConnections = new Composite(this.outgoingScrollArea, SWT.NONE);
		this.outgoingActiveConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.outgoingActiveConnections.setBounds(10, 64, this.shell.getSize().x - 40, 20 + (CompositeInfo.height * getCurrentOutgoingClientInfos().size()));//this.activeConnections.setBounds(10, 64, 420, 642);
		
		this.lblNoOutgoingConnections = new Label(this.outgoingActiveConnections, SWT.NONE);
		this.lblNoOutgoingConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.lblNoOutgoingConnections.setBounds(0, 0, 425, 13);
		this.lblNoOutgoingConnections.setText("There are no outgoing connections at this time.");
		this.outgoingScrollArea.setContent(this.outgoingActiveConnections);
		
		this.tbtmIncomingConnections = new TabItem(this.tabFolder, SWT.NONE);
		this.tbtmIncomingConnections.setText("Incoming Connections");
		
		this.incomingScrollArea = new ScrolledComposite(this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		this.tbtmIncomingConnections.setControl(this.incomingScrollArea);
		this.incomingScrollArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.incomingScrollArea.setMinSize(200, 200);
		
		this.incomingActiveConnections = new Composite(this.incomingScrollArea, SWT.NONE);
		this.incomingActiveConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.incomingActiveConnections.setBounds(10, 64, this.shell.getSize().x - 40, 20 + (CompositeInfo.height * getCurrentIncomingClientInfos().size()));//this.activeConnections.setBounds(10, 64, 420, 642);
		
		this.lblNoIncomingConnections = new Label(this.incomingActiveConnections, SWT.NONE);
		this.lblNoIncomingConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.lblNoIncomingConnections.setBounds(0, 0, 425, 13);
		this.lblNoIncomingConnections.setText("There are no incoming connections at this time.");
		this.incomingScrollArea.setContent(this.incomingActiveConnections);
		
		this.tbtmProxyConnections = new TabItem(this.tabFolder, SWT.NONE);
		this.tbtmProxyConnections.setText("Proxy Connections");
		
		this.proxyScrollArea = new ScrolledComposite(this.tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		this.proxyScrollArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.tbtmProxyConnections.setControl(this.proxyScrollArea);
		
		this.activeProxyConnections = new Composite(this.proxyScrollArea, SWT.NONE);
		this.activeProxyConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.activeProxyConnections.setBounds(10, 64, this.shell.getSize().x - 40, 20 + (CompositeInfo.height * getCurrentProxyClientInfos().size()));//this.activeConnections.setBounds(10, 64, 420, 642);
		
		this.lblNoProxyConnections = new Label(this.activeProxyConnections, SWT.NONE);
		this.lblNoProxyConnections.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.lblNoProxyConnections.setText("There are no proxy connections at this time.");
		this.lblNoProxyConnections.setBounds(0, 0, 425, 13);
		this.proxyScrollArea.setContent(this.activeProxyConnections);
		
	}
	
	protected static final ArrayList<ClientInfo> getCurrentOutgoingClientInfos() {
		return new ArrayList<>(JavaWebServer.connectedClients);
	}
	
	protected static final ArrayList<ClientRequestStatus> getCurrentIncomingClientInfos() {
		return new ArrayList<>(JavaWebServer.connectedClientRequests);
	}
	
	private static final int getNumOfCompositeInfosInComposite(Composite parent) {
		int num = 0;
		for(Control child : parent.getChildren()) {
			if(child instanceof CompositeInfo) {
				num++;
			}
		}
		return num;
	}
	
	protected static final ArrayList<ClientRequestStatus> getCurrentProxyClientInfos() {
		return new ArrayList<>(JavaWebServer.connectedProxyRequests);
	}
	
	private CompositeInfo getCompositeForOutgoingConnection(final ClientInfo info) {
		for(Control control : this.outgoingActiveConnections.getChildren()) {
			this.runClock();
			if(control instanceof CompositeInfo) {
				CompositeInfo connection = (CompositeInfo) control;
				if(connection.uuid.equals(info.uuid)) {
					this.updateCompositeInfoLocations(this.outgoingActiveConnections.getChildren());
					connection.updateUI();
					return connection;
				}
			}
			this.runClock();
		}
		CompositeInfo connection = new CompositeInfo(this.shell, this.outgoingActiveConnections, SWT.BORDER | SWT.EMBEDDED, info, true);
		this.updateCompositeInfoLocations(this.outgoingActiveConnections.getChildren());
		return connection;
	}
	
	private CompositeInfo getCompositeForConnection(final ClientRequestStatus info, Composite parent) {
		for(Control control : parent.getChildren()) {
			if(control instanceof CompositeInfo) {
				CompositeInfo connection = (CompositeInfo) control;
				if(connection.uuid.equals(info.getUUID())) {
					this.updateCompositeInfoLocations(parent.getChildren());
					connection.updateUI();
					return connection;
				}
			}
			this.runClock();
		}
		CompositeInfo connection = new CompositeInfo(this.shell, parent, SWT.BORDER | SWT.EMBEDDED, info, false, parent != this.activeProxyConnections);
		this.updateCompositeInfoLocations(parent.getChildren());
		return connection;
	}
	
	private final void updateCompositeInfoLocations(Control[] controls) {
		int i = 0;
		for(Control control : controls) {
			if(control != null && control instanceof CompositeInfo) {
				CompositeInfo info = (CompositeInfo) control;
				info.setLocation(10, 10 + (CompositeInfo.height * i));//info.setBounds(10, 10 + (CompositeInfo.height * i), 403, CompositeInfo.height);
				i++;
			}
		}
		this.runClock();
	}
}
