package com.gmail.br45entei.gui;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.data.Property;
import com.gmail.br45entei.server.data.NaughtyClientData;
import com.gmail.br45entei.server.data.php.PhpResult;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.Response;
import com.gmail.br45entei.util.CodeUtil;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class OptionsDialog extends Dialog {
	
	protected Response response = Response.NO_RESPONSE;
	protected Shell shell;
	protected Text homeDirectory;
	protected Button btnRestoreDefaults;
	protected Button btnCalculateDir;
	protected Text directoryPageFontFace;
	protected Text phpCGI_File;
	protected Spinner requestTimeout;
	protected Spinner threadPoolSize;
	protected Spinner vlcNetworkCaching;
	protected Spinner serverListenPort;
	protected Text sslStorePath;
	protected Spinner sslListenPort;
	protected Button enableSSLhttps;
	protected Text sslStorePassword;
	protected Spinner adminListenPort;
	protected Button enableAdministrationInterface;
	protected Text adminUsername;
	protected Text adminPassword;
	protected Text proxyUsername;
	protected Text proxyPassword;
	protected Button btnEnableOverrideThreadPool;
	protected Composite proxySettings;
	protected Composite adminSettings;
	protected Composite sslSettings;
	protected Button btnTerminateDeadProxy;
	protected Button btnTerminateDeadProxyConnectionTimeouts;
	
	protected volatile HTTPClientUserAgentOptionsDialog httpClientOptionsDialog = null;
	
	/** @param args System command arguments */
	public static final void main(String[] args) {
		@SuppressWarnings("unused")
		Display display = Display.getDefault();
		OptionsDialog dialog = new OptionsDialog(new Shell(), SWT.DIALOG_TRIM);
		Response response = dialog.open();
		System.out.println("Response: " + response.name());
	}
	
	/** Create the dialog.
	 * 
	 * @param parent The parent shell
	 * @param style The SWT Style */
	public OptionsDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}
	
	public final Shell getShell() {
		return this.shell;
	}
	
	public final void dispose() {
		this.shell.dispose();
	}
	
	public final boolean isDisposed() {
		return this.shell.isDisposed();
	}
	
	/** Runs {@link Display#readAndDispatch()},
	 * then attempts to sleep. */
	protected final void runClock() {
		if(this.shell.isDisposed()) {
			return;
		}
		//this.exitCheck();
		if(this.shell.isVisible()) {
			if(!this.shell.getDisplay().readAndDispatch()) {
				CodeUtil.sleep(1L);//display.sleep();
			}
			return;
		}
		CodeUtil.sleep(10L);
	}
	
	protected final boolean mainLoop(Display display) {
		if(!display.readAndDispatch()) {
			display.sleep();
		}
		if(this.response != Response.NO_RESPONSE) {
			this.shell.dispose();
			return false;
		}
		if(this.shell.isDisposed()) {
			return false;
		}
		if(Main.getInstance() != null) {
			if(Main.getInstance().showWindow) {
				Main.getInstance().runLoop();
			} else if(Main.getInstance().isSWTConsoleWindowOpen()) {
				Main.getInstance().getConsoleWindow().runLoop();
			} else {
				this.runClock();
			}
		}
		if(this.shell.isDisposed()) {
			return false;
		}
		return true;
	}
	
	/** Open the dialog.
	 * 
	 * @return the result */
	public Response open() {
		createContents();
		this.shell.open();
		this.shell.layout();
		Display display = getParent().getDisplay();
		while(!this.shell.isDisposed()) {
			if(!this.mainLoop(display)) {
				break;
			}
		}
		return this.response;
	}
	
	/** Create contents of the dialog. */
	private void createContents() {
		this.shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				OptionsDialog.this.response = Response.CLOSE;
			}
		});
		this.shell.setImages(new Image[] {SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-16x16.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-32x32.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-64x64.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-128x128.png")});
		this.shell.setSize(480, 415);
		this.shell.setText(getText());
		this.shell.setText(JavaWebServer.APPLICATION_NAME + " " + JavaWebServer.APPLICATION_VERSION + " - Options");
		Functions.centerShell2OnShell1(getParent(), this.shell);
		
		TabFolder tabFolder = new TabFolder(this.shell, SWT.NONE);
		tabFolder.setToolTipText("");
		tabFolder.setBounds(10, 64, 454, 289);
		
		TabItem tbtmServerOptions = new TabItem(tabFolder, SWT.NONE);
		tbtmServerOptions.setToolTipText("Options related to the internal HTTP server");
		tbtmServerOptions.setText("Server Options");
		
		Composite contents = new Composite(tabFolder, SWT.NONE);
		contents.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmServerOptions.setControl(contents);
		
		Button lblHomeDirectory = new Button(contents, SWT.NONE);
		lblHomeDirectory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(OptionsDialog.this.shell);
				dialog.setFilterPath(JavaWebServer.homeDirectory.getAbsolutePath());
				dialog.setText("Choose the default home directory");
				dialog.setMessage("When a client makes a top-level server request(such as \"GET / HTTP/1.1\"), the server needs to have a base-folder to read from.\r\nThis setting decides the default home directory for the entire server, whereas other domains(configurable in the administration interface) may override this setting with their own home directory.\r\nThe default home directory is a folder called \"htdocs\" located in this server's current working directory.");
				String path = dialog.open();
				if(path != null) {
					File check = new File(path);
					if(check.exists() && check.isDirectory()) {
						JavaWebServer.homeDirectory = check;
						OptionsDialog.this.homeDirectory.setText(path);
					}
				}
			}
		});
		lblHomeDirectory.setToolTipText("When a client makes a top-level server request(such as \"GET / HTTP/1.1\"), the server needs to have a base-folder to read from.\r\nThis setting decides the default home directory for the entire server, whereas other domains(configurable in the administration interface) may override this setting with their own home directory.\r\nThe default home directory is a folder called \"htdocs\" located in this server's current working directory.");
		lblHomeDirectory.setBounds(0, 0, 100, 19);
		lblHomeDirectory.setText("Home Directory");
		
		this.homeDirectory = new Text(contents, SWT.BORDER | SWT.READ_ONLY);
		this.homeDirectory.setToolTipText("Home Directory");
		this.homeDirectory.setBounds(106, 0, 340, 19);
		this.homeDirectory.setText(JavaWebServer.homeDirectory.getAbsolutePath());
		
		this.btnCalculateDir = new Button(contents, SWT.CHECK);
		this.btnCalculateDir.setSelection(JavaWebServer.calculateDirectorySizes);
		this.btnCalculateDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.calculateDirectorySizes = OptionsDialog.this.btnCalculateDir.getSelection();
			}
		});
		this.btnCalculateDir.setToolTipText("When a client requests a directory, a html page is generated with a list of all of the files in that directory.\r\nThis option decides whether or not the server should calculate the sizes of any\r\nsubdirectories within that directory, and any subdirectories of those subdirectories.\r\nI would recommend leaving this setting set to false,\r\nas recursively calculating directory structures can take a long time,\r\nespecially with traditional hard disk drives/slow computers.\r\nDefault value is false.");
		this.btnCalculateDir.setBounds(0, 25, 152, 16);
		this.btnCalculateDir.setText("Calculate Directory Sizes");
		
		Label lblDirectoryPageFontface = new Label(contents, SWT.NONE);
		lblDirectoryPageFontface.setToolTipText("When a client requests a directory, a html page is generated with a list of all of the files in that directory.\r\nThis option decides what font will be used for that generated page.\r\nThe default font is \"Times New Roman\".");
		lblDirectoryPageFontface.setBounds(0, 47, 140, 19);
		lblDirectoryPageFontface.setText("Directory Page Font-Face:");
		
		this.directoryPageFontFace = new Text(contents, SWT.BORDER);
		this.directoryPageFontFace.setText(JavaWebServer.defaultFontFace);
		this.directoryPageFontFace.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.defaultFontFace = OptionsDialog.this.directoryPageFontFace.getText();
			}
		});
		this.directoryPageFontFace.setToolTipText("Directory Page Font-Face");
		this.directoryPageFontFace.setBounds(146, 47, 185, 19);
		
		Button btnPhpCgiExecutable = new Button(contents, SWT.NONE);
		btnPhpCgiExecutable.setToolTipText("The full path to the PHP CGI executable file. On Windows, this is usually the file named \"php-cgi.exe\". On other platforms, this is usually the file named \"bin/php-cgi\".\r\nIf no php cgi file is set, clients requesting a php file will download the file instead of viewing the generated html page.");
		btnPhpCgiExecutable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(OptionsDialog.this.shell);
				dialog.setFilterPath(PhpResult.isPhpFilePresent() ? new File(PhpResult.phpExeFilePath).getParent() : PhpResult.phpExeFilePath);
				dialog.setText("Choose the PHP-CGI Executable file");
				String path = dialog.open();
				if(path != null) {
					File check = new File(path);
					if(check.exists() && check.isFile()) {
						PhpResult.phpExeFilePath = path;
						OptionsDialog.this.phpCGI_File.setText(PhpResult.phpExeFilePath);
					}
				}
			}
		});
		btnPhpCgiExecutable.setBounds(0, 72, 120, 19);
		btnPhpCgiExecutable.setText("PHP-CGI Executable");
		
		this.phpCGI_File = new Text(contents, SWT.BORDER | SWT.READ_ONLY);
		this.phpCGI_File.setToolTipText("PHP-CGI Executable");
		this.phpCGI_File.setBounds(126, 72, 320, 19);
		this.phpCGI_File.setText(PhpResult.phpExeFilePath);
		
		this.requestTimeout = new Spinner(contents, SWT.BORDER);
		this.requestTimeout.setToolTipText("Request Timeout");
		this.requestTimeout.setIncrement(1000);
		this.requestTimeout.setMaximum(180000);
		this.requestTimeout.setMinimum(10000);
		this.requestTimeout.setSelection(JavaWebServer.requestTimeout);
		this.requestTimeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.requestTimeout = OptionsDialog.this.requestTimeout.getSelection();
			}
		});
		this.requestTimeout.setBounds(106, 97, 64, 21);
		
		Label lblRequestTimeout = new Label(contents, SWT.NONE);
		lblRequestTimeout.setToolTipText("The time(in milliseconds) that this server will be willing to wait before declaring that a client has timed out in it's request.\r\nDefault value is 30 seconds(30000).");
		lblRequestTimeout.setBounds(0, 97, 100, 21);
		lblRequestTimeout.setText("Request Timeout:");
		
		ExpandBar expandBar = new ExpandBar(contents, SWT.NONE);
		expandBar.setToolTipText("It is not recommended to change these values if you do not know what they do.");
		expandBar.setBounds(0, 157, 446, 106);
		
		ExpandItem xpndtmAdvanced = new ExpandItem(expandBar, SWT.NONE);
		xpndtmAdvanced.setText("Advanced Server Options");
		
		Composite contents1 = new Composite(expandBar, SWT.NONE);
		contents1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		xpndtmAdvanced.setControl(contents1);
		xpndtmAdvanced.setHeight(xpndtmAdvanced.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		Label lblOverrideThreadPool = new Label(contents1, SWT.NONE);
		lblOverrideThreadPool.setToolTipText("Overrides the default setting for the internal server thread pools.\r\nThere is one thread pool per type of service(HTTP, HTTPS, PROXY, etc).\r\nThread pools contain the threads that perform operations for connecting clients.\r\nThis setting overrides the default of 1000 times the number of processors (cpu cores).\r\nMinimum value is 20, maximum value is 10000.");
		lblOverrideThreadPool.setBounds(0, 0, 150, 21);
		lblOverrideThreadPool.setText("Override Thread Pool Size:");
		
		final SelectionAdapter toggleOverrideListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent doNotUse) {
				OptionsDialog.this.threadPoolSize.setEnabled(OptionsDialog.this.btnEnableOverrideThreadPool.getSelection());
				if(!OptionsDialog.this.threadPoolSize.isEnabled()) {
					OptionsDialog.this.threadPoolSize.setSelection(JavaWebServer.fNumberOfThreads);
					JavaWebServer.overrideThreadPoolSize = -1;
					OptionsDialog.this.btnEnableOverrideThreadPool.setText("Disabled");
					OptionsDialog.this.btnEnableOverrideThreadPool.setToolTipText("Enable the override");
				} else {
					JavaWebServer.overrideThreadPoolSize = OptionsDialog.this.threadPoolSize.getSelection();
					OptionsDialog.this.btnEnableOverrideThreadPool.setText("Enabled");
					OptionsDialog.this.btnEnableOverrideThreadPool.setToolTipText("Disable the override");
				}
				JavaWebServer.updateThreadPoolSizes();
			}
		};
		
		this.btnEnableOverrideThreadPool = new Button(contents1, SWT.CHECK);
		this.btnEnableOverrideThreadPool.setSelection(JavaWebServer.overrideThreadPoolSize != -1);
		this.btnEnableOverrideThreadPool.addSelectionListener(toggleOverrideListener);
		this.btnEnableOverrideThreadPool.setText(JavaWebServer.overrideThreadPoolSize != -1 ? "Enabled" : "Disabled");
		this.btnEnableOverrideThreadPool.setToolTipText(JavaWebServer.overrideThreadPoolSize != -1 ? "Disable the override" : "Enable the override");
		this.btnEnableOverrideThreadPool.setBounds(272, 0, 69, 21);
		
		this.threadPoolSize = new Spinner(contents1, SWT.BORDER);
		this.threadPoolSize.setEnabled(JavaWebServer.overrideThreadPoolSize != -1);
		this.threadPoolSize.setToolTipText("Override Thread Pool Size");
		this.threadPoolSize.setMaximum(10000);
		this.threadPoolSize.setMinimum(20);
		this.threadPoolSize.setSelection(JavaWebServer.overrideThreadPoolSize != -1 ? JavaWebServer.overrideThreadPoolSize : JavaWebServer.fNumberOfThreads);
		this.threadPoolSize.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.overrideThreadPoolSize = OptionsDialog.this.btnEnableOverrideThreadPool.getEnabled() ? OptionsDialog.this.threadPoolSize.getSelection() : -1;
				JavaWebServer.updateThreadPoolSizes();
			}
		});
		this.threadPoolSize.setBounds(156, 0, 110, 21);
		
		Label lblVlcNetworkCaching = new Label(contents1, SWT.NONE);
		lblVlcNetworkCaching.setToolTipText("Allows you to edit the `<vlc:option>network-caching=XXXX</vlc:option>` value that is sent to VLC Media Player via the xspf playlist feature");
		lblVlcNetworkCaching.setBounds(0, 27, 150, 15);
		lblVlcNetworkCaching.setText("VLC Network Caching:");
		
		this.vlcNetworkCaching = new Spinner(contents1, SWT.BORDER);
		this.vlcNetworkCaching.setIncrement(250);
		final Property<Boolean> deb1 = new Property<>();
		this.vlcNetworkCaching.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(deb1.getValue() == Boolean.TRUE) {
					return;
				}
				deb1.setValue(Boolean.TRUE);
				if(OptionsDialog.this.vlcNetworkCaching.getSelection() == 0) {
					OptionsDialog.this.vlcNetworkCaching.setSelection(-1);
				}
				if(OptionsDialog.this.vlcNetworkCaching.getSelection() == 249) {
					OptionsDialog.this.vlcNetworkCaching.setSelection(250);
				}
				JavaWebServer.VLC_NETWORK_CACHING_MILLIS = OptionsDialog.this.vlcNetworkCaching.getSelection();
				deb1.setValue(Boolean.FALSE);
			}
		});
		this.vlcNetworkCaching.setToolTipText("Set VLC Network Caching");
		this.vlcNetworkCaching.setMaximum(10000);
		this.vlcNetworkCaching.setMinimum(-1);
		this.vlcNetworkCaching.setSelection(JavaWebServer.VLC_NETWORK_CACHING_MILLIS);
		this.vlcNetworkCaching.setBounds(156, 27, 110, 21);
		
		Label lblServerListenPort = new Label(contents, SWT.NONE);
		lblServerListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblServerListenPort.setToolTipText("The port that this server will bind to and listen to incoming HTTP requests.\r\nThis setting requires that you restart the server before it takes effect.\r\nDefault value is 80.");
		lblServerListenPort.setBounds(0, 124, 100, 21);
		lblServerListenPort.setText("Server Listen Port:");
		
		this.serverListenPort = new Spinner(contents, SWT.BORDER);
		this.serverListenPort.setToolTipText("Server Listen Port");
		this.serverListenPort.setMaximum(65535);
		this.serverListenPort.setMinimum(1);
		this.serverListenPort.setSelection(JavaWebServer.listen_port);
		this.serverListenPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.listen_port = OptionsDialog.this.serverListenPort.getSelection();
			}
		});
		this.serverListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		this.serverListenPort.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		this.serverListenPort.setBounds(106, 124, 64, 21);
		
		Label label = new Label(contents, SWT.SEPARATOR | SWT.VERTICAL);
		label.setBounds(176, 97, 2, 48);
		
		Button btnHttpClientOptions = new Button(contents, SWT.NONE);
		btnHttpClientOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(OptionsDialog.this.httpClientOptionsDialog == null) {
					HTTPClientUserAgentOptionsDialog dialog = new HTTPClientUserAgentOptionsDialog(OptionsDialog.this);
					OptionsDialog.this.httpClientOptionsDialog = dialog;
					Response response = dialog.open();
					OptionsDialog.this.httpClientOptionsDialog = null;
					dialog.dispose();
					if(response == Response.OK) {
						dialog.applySettings();
					}
				} else {
					OptionsDialog.this.httpClientOptionsDialog.setFocus();
				}
			}
		});
		btnHttpClientOptions.setBounds(184, 97, 147, 25);
		btnHttpClientOptions.setText("HTTP client options...");
		
		TabItem tbtmSslhttpsOptions = new TabItem(tabFolder, SWT.NONE);
		tbtmSslhttpsOptions.setToolTipText("Options related to the internal SSL server");
		tbtmSslhttpsOptions.setText("SSL(HTTPS) Options");
		
		Composite contents2 = new Composite(tabFolder, SWT.NONE);
		contents2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmSslhttpsOptions.setControl(contents2);
		
		this.enableSSLhttps = new Button(contents2, SWT.CHECK);
		this.enableSSLhttps.setSelection(JavaWebServer.enableSSLThread);
		
		this.enableSSLhttps.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean enabled = OptionsDialog.this.enableSSLhttps.getSelection();
				JavaWebServer.enableSSLThread = enabled;
				OptionsDialog.this.sslSettings.setVisible(enabled);
			}
		});
		this.enableSSLhttps.setToolTipText("Whether or not SSL(HTTPS) is enabled.\r\nThis setting requires that you restart the server in order for changes to take effect.\r\nDefault value is false.");
		this.enableSSLhttps.setBounds(0, 0, 13, 16);
		
		Label lblEnableSslhttps = new Label(contents2, SWT.NONE);
		lblEnableSslhttps.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				OptionsDialog.this.enableSSLhttps.setSelection(!OptionsDialog.this.enableSSLhttps.getSelection());
				JavaWebServer.enableSSLThread = OptionsDialog.this.enableSSLhttps.getSelection();
				OptionsDialog.this.sslSettings.setVisible(OptionsDialog.this.enableSSLhttps.getSelection());
			}
		});
		lblEnableSslhttps.setToolTipText("Whether or not SSL(HTTPS) is enabled.\r\nThis setting requires that you restart the server in order for changes to take effect.\r\nDefault value is false.");
		lblEnableSslhttps.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblEnableSslhttps.setBounds(19, 0, 108, 13);
		lblEnableSslhttps.setText("Enable SSL(HTTPS)");
		
		this.sslSettings = new Composite(contents2, SWT.NONE);
		this.sslSettings.setBounds(0, 22, 446, 241);
		this.sslSettings.setVisible(this.enableSSLhttps.getSelection());
		
		Label lblSslListenPort = new Label(this.sslSettings, SWT.NONE);
		lblSslListenPort.setBounds(0, 0, 92, 21);
		lblSslListenPort.setToolTipText("The port that this server will bind to and listen to incoming SSL(HTTPS) requests.\r\nThis setting requires that you restart the server before it takes effect.\r\nDefault value is 443.");
		lblSslListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblSslListenPort.setText("SSL Listen Port:");
		
		this.sslListenPort = new Spinner(this.sslSettings, SWT.BORDER);
		this.sslListenPort.setBounds(98, 0, 65, 21);
		this.sslListenPort.setMaximum(65535);
		this.sslListenPort.setMinimum(1);
		this.sslListenPort.setSelection(JavaWebServer.ssl_listen_port);
		this.sslListenPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.ssl_listen_port = OptionsDialog.this.sslListenPort.getSelection();
			}
		});
		this.sslListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		this.sslListenPort.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		this.sslListenPort.setToolTipText("SSL Listen Port");
		
		Label lblSslStoreType = new Label(this.sslSettings, SWT.NONE);
		lblSslStoreType.setBounds(0, 27, 92, 16);
		lblSslStoreType.setToolTipText("The type of SSL certificate store to use when handling incoming SSL connections.\r\nThis setting requires that you restart the server before it takes effect.\r\nDefault value is \"Key Store\".");
		lblSslStoreType.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblSslStoreType.setText("SSL Store Type:");
		
		Composite keyOrTrustArea = new Composite(this.sslSettings, SWT.NONE);
		keyOrTrustArea.setBounds(98, 27, 170, 16);
		keyOrTrustArea.setToolTipText("SSL Store Type");
		
		final Button btnKeyStore = new Button(keyOrTrustArea, SWT.RADIO);
		btnKeyStore.setToolTipText("SSL Key Store Type");
		btnKeyStore.setSelection(JavaWebServer.sslStore_KeyOrTrust);
		btnKeyStore.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnKeyStore.setBounds(0, 0, 83, 16);
		btnKeyStore.setText("Key Store");
		
		Button btnTrustStore = new Button(keyOrTrustArea, SWT.RADIO);
		btnTrustStore.setToolTipText("SSL Trust Store Type");
		btnTrustStore.setSelection(!JavaWebServer.sslStore_KeyOrTrust);
		btnTrustStore.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnTrustStore.setBounds(87, 0, 83, 16);
		btnTrustStore.setText("Trust Store");
		
		Button sslStoreFile_1 = new Button(this.sslSettings, SWT.NONE);
		sslStoreFile_1.setBounds(0, 49, 92, 19);
		sslStoreFile_1.setToolTipText("The full path to the SSL Store file.\r\nIf left blank, the server will most likely encounter SSL handshake issues when clients connect.\r\n");
		sslStoreFile_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		sslStoreFile_1.setText("SSL Store File");
		
		this.sslStorePath = new Text(this.sslSettings, SWT.BORDER | SWT.READ_ONLY);
		this.sslStorePath.setBounds(98, 49, 348, 19);
		this.sslStorePath.setToolTipText("SSL Store File");
		this.sslStorePath.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		this.sslStorePath.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		this.sslStorePath.setText(JavaWebServer.storePath);
		
		Label lblSslStorePassword = new Label(this.sslSettings, SWT.NONE);
		lblSslStorePassword.setBounds(0, 74, 102, 13);
		lblSslStorePassword.setToolTipText("The password for the SSL Store file.\r\nIf left blank or entered incorrectly, the server will most likely encounter SSL handshake issues when clients connect.");
		lblSslStorePassword.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblSslStorePassword.setText("SSL Store Password:");
		
		this.sslStorePassword = new Text(this.sslSettings, SWT.BORDER | SWT.PASSWORD);
		this.sslStorePassword.setBounds(108, 74, 338, 19);
		this.sslStorePassword.setToolTipText("SSL Store Password");
		this.sslStorePassword.setText(JavaWebServer.storePassword);
		this.sslStorePassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.storePassword = OptionsDialog.this.sslStorePassword.getText();
			}
		});
		this.sslStorePassword.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		this.sslStorePassword.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		
		sslStoreFile_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(OptionsDialog.this.shell);
				File sslStoreFile = new File(JavaWebServer.storePath);
				dialog.setFilterPath(!JavaWebServer.storePath.isEmpty() && sslStoreFile.exists() ? sslStoreFile.getParent() : JavaWebServer.storePath);
				dialog.setText("Choose the SSL Store File");
				String path = dialog.open();
				if(path != null) {
					File check = new File(path);
					if(check.exists() && check.isFile()) {
						JavaWebServer.storePath = path;
						OptionsDialog.this.sslStorePath.setText(JavaWebServer.storePath);
					}
				}
			}
		});
		
		btnKeyStore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.sslStore_KeyOrTrust = true;
			}
		});
		
		btnTrustStore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.sslStore_KeyOrTrust = false;
			}
		});
		
		TabItem tbtmAdministrationInterface = new TabItem(tabFolder, SWT.NONE);
		tbtmAdministrationInterface.setToolTipText("Options related to the \"Administration Interface\" feature");
		tbtmAdministrationInterface.setText("Admin Interface");
		
		Composite contents3 = new Composite(tabFolder, SWT.NONE);
		contents3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmAdministrationInterface.setControl(contents3);
		
		this.enableAdministrationInterface = new Button(contents3, SWT.CHECK);
		this.enableAdministrationInterface.setSelection(JavaWebServer.enableAdminInterface);
		this.enableAdministrationInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean enable = OptionsDialog.this.enableAdministrationInterface.getSelection();
				JavaWebServer.enableAdminInterface = enable;
				OptionsDialog.this.adminSettings.setVisible(enable);
			}
		});
		this.enableAdministrationInterface.setToolTipText("Whether or not the Administration Interface is enabled.\r\nThis setting requires that you restart the server in order for changes to take effect.\r\nDefault value is true.");
		this.enableAdministrationInterface.setBounds(0, 0, 13, 16);
		
		Label lblEnableAdministrationInterface = new Label(contents3, SWT.NONE);
		lblEnableAdministrationInterface.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				OptionsDialog.this.enableAdministrationInterface.setSelection(!OptionsDialog.this.enableAdministrationInterface.getSelection());
				JavaWebServer.enableAdminInterface = OptionsDialog.this.enableAdministrationInterface.getSelection();
				OptionsDialog.this.adminSettings.setVisible(OptionsDialog.this.enableAdministrationInterface.getSelection());
			}
		});
		lblEnableAdministrationInterface.setToolTipText("Whether or not the Administration Interface is enabled.\r\nThis setting requires that you restart the server in order for changes to take effect.\r\nDefault value is true.");
		lblEnableAdministrationInterface.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblEnableAdministrationInterface.setBounds(19, 0, 180, 13);
		lblEnableAdministrationInterface.setText("Enable Administration Interface");
		
		this.adminSettings = new Composite(contents3, SWT.NONE);
		this.adminSettings.setBounds(0, 22, 446, 241);
		this.adminSettings.setVisible(this.enableAdministrationInterface.getSelection());
		
		Label lblAdministrationListenPort = new Label(this.adminSettings, SWT.NONE);
		lblAdministrationListenPort.setBounds(0, 0, 150, 21);
		lblAdministrationListenPort.setToolTipText("The port that this server will bind to and listen to incoming server administration requests.\r\nThis setting requires that you restart the server before it takes effect.\r\nDefault value is 9727.");
		lblAdministrationListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblAdministrationListenPort.setText("Administration Listen Port:");
		
		this.adminListenPort = new Spinner(this.adminSettings, SWT.BORDER);
		this.adminListenPort.setBounds(156, 0, 67, 21);
		this.adminListenPort.setMaximum(65535);
		this.adminListenPort.setMinimum(1);
		this.adminListenPort.setSelection(JavaWebServer.admin_listen_port);
		this.adminListenPort.setToolTipText("Administration Listen Port");
		this.adminListenPort.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		this.adminListenPort.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		
		Label lblAdministrationUsername = new Label(this.adminSettings, SWT.NONE);
		lblAdministrationUsername.setBounds(0, 27, 150, 19);
		lblAdministrationUsername.setToolTipText("The username that will be used for authorization when clients attempt to administrate this server.\r\nDefault value is \"Administrator\".");
		lblAdministrationUsername.setText("Administration Username:");
		
		this.adminUsername = new Text(this.adminSettings, SWT.BORDER);
		this.adminUsername.setBounds(156, 27, 134, 19);
		this.adminUsername.setText(JavaWebServer.getAdminUsername());
		this.adminUsername.setToolTipText("Administration Username");
		
		Label lblAdministrationPassword = new Label(this.adminSettings, SWT.NONE);
		lblAdministrationPassword.setBounds(0, 52, 150, 19);
		lblAdministrationPassword.setToolTipText("The password that will be used for authorization when clients attempt to administrate this server.\r\nDefault value is \"password\", and should therefore be changed.");
		lblAdministrationPassword.setText("Administration Password:");
		
		this.adminPassword = new Text(this.adminSettings, SWT.BORDER | SWT.PASSWORD);
		this.adminPassword.setBounds(156, 52, 134, 19);
		this.adminPassword.setText(JavaWebServer.getAdminPassword());
		this.adminPassword.setToolTipText("Administration Password");
		this.adminPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.setAdminPassword(OptionsDialog.this.adminPassword.getText());
			}
		});
		this.adminUsername.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.setAdminUsername(OptionsDialog.this.adminUsername.getText());
			}
		});
		this.adminListenPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.admin_listen_port = OptionsDialog.this.adminListenPort.getSelection();
			}
		});
		
		TabItem tbtmProxyServerOptions = new TabItem(tabFolder, SWT.NONE);
		tbtmProxyServerOptions.setToolTipText("Options related to the internal proxy server");
		tbtmProxyServerOptions.setText("Proxy Server Options");
		
		Composite contents4 = new Composite(tabFolder, SWT.NONE);
		contents4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmProxyServerOptions.setControl(contents4);
		
		final Button btnEnableProxyServer = new Button(contents4, SWT.CHECK);
		btnEnableProxyServer.setToolTipText("Whether or not the proxy server is enabled.\r\nDefault value is false.");
		btnEnableProxyServer.setSelection(JavaWebServer.isProxyServerEnabled());
		btnEnableProxyServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.setProxyServerEnabled(btnEnableProxyServer.getSelection());
				OptionsDialog.this.proxySettings.setVisible(btnEnableProxyServer.getSelection());
			}
		});
		btnEnableProxyServer.setBounds(0, 0, 13, 16);
		
		Label lblEnableProxyServer = new Label(contents4, SWT.NONE);
		lblEnableProxyServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				btnEnableProxyServer.setSelection(!btnEnableProxyServer.getSelection());
				JavaWebServer.setProxyServerEnabled(btnEnableProxyServer.getSelection());
				OptionsDialog.this.proxySettings.setVisible(btnEnableProxyServer.getSelection());
			}
		});
		lblEnableProxyServer.setToolTipText("Whether or not the proxy server is enabled.\r\nDefault value is false.");
		lblEnableProxyServer.setBounds(19, 0, 115, 13);
		lblEnableProxyServer.setText("Enable Proxy Server");
		
		this.proxySettings = new Composite(contents4, SWT.NONE);
		this.proxySettings.setVisible(btnEnableProxyServer.getSelection());
		this.proxySettings.setBounds(0, 22, 446, 241);
		
		final Button btnSendProxyHeaders = new Button(this.proxySettings, SWT.CHECK);
		btnSendProxyHeaders.setBounds(0, 0, 198, 16);
		btnSendProxyHeaders.setToolTipText("Whether or not proxy headers are sent to the server along with the client's request.\r\nSetting this to false makes this proxy server \"anonymous\".\r\nDefault value is true.");
		btnSendProxyHeaders.setSelection(JavaWebServer.sendProxyHeadersWithRequest);
		btnSendProxyHeaders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.sendProxyHeadersWithRequest = btnSendProxyHeaders.getSelection();
			}
		});
		btnSendProxyHeaders.setText("Send Proxy Headers With Request");
		
		final Button btnProxyRequiresAuthorization = new Button(this.proxySettings, SWT.CHECK);
		btnProxyRequiresAuthorization.setBounds(0, 22, 198, 16);
		btnProxyRequiresAuthorization.setToolTipText("Whether or not clients making requests to this proxy server have to authenticate themselves before their requests will be processed.\r\nDefault value is true.");
		btnProxyRequiresAuthorization.setSelection(JavaWebServer.proxyRequiresAuthorization);
		btnProxyRequiresAuthorization.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean enable = btnProxyRequiresAuthorization.getSelection();
				JavaWebServer.proxyRequiresAuthorization = enable;
				OptionsDialog.this.proxyUsername.setEnabled(enable);
				OptionsDialog.this.proxyPassword.setEnabled(enable);
			}
		});
		btnProxyRequiresAuthorization.setText("Proxy Requires Authorization");
		
		Label lblProxyUsername = new Label(this.proxySettings, SWT.NONE);
		lblProxyUsername.setBounds(0, 44, 95, 19);
		lblProxyUsername.setToolTipText("The username that will be used for authorization when clients attempt to make requests.\r\nDefault value is \"Proxy User\".");
		lblProxyUsername.setText("Proxy Username:");
		
		this.proxyUsername = new Text(this.proxySettings, SWT.BORDER);
		this.proxyUsername.setBounds(101, 44, 130, 19);
		this.proxyUsername.setText(JavaWebServer.getProxyUsername());
		this.proxyUsername.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.setProxyUsername(OptionsDialog.this.proxyUsername.getText());
			}
		});
		this.proxyUsername.setToolTipText("Proxy Username");
		this.proxyUsername.setEnabled(btnProxyRequiresAuthorization.getSelection());
		
		Label lblProxyPassword = new Label(this.proxySettings, SWT.NONE);
		lblProxyPassword.setBounds(0, 69, 95, 19);
		lblProxyPassword.setToolTipText("The password that will be used for authorization when clients attempt to make requests.\r\nDefault value is \"password\", and should therefore be changed.");
		lblProxyPassword.setText("Proxy Password:");
		
		this.proxyPassword = new Text(this.proxySettings, SWT.BORDER | SWT.PASSWORD);
		this.proxyPassword.setBounds(101, 69, 130, 19);
		this.proxyPassword.setText(JavaWebServer.getProxyPassword());
		this.proxyPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				JavaWebServer.setProxyPassword(OptionsDialog.this.proxyPassword.getText());
			}
		});
		this.proxyPassword.setToolTipText("Proxy Password");
		this.proxyPassword.setEnabled(btnProxyRequiresAuthorization.getSelection());
		
		this.btnTerminateDeadProxy = new Button(this.proxySettings, SWT.CHECK);
		this.btnTerminateDeadProxy.setBounds(0, 102, 231, 16);
		this.btnTerminateDeadProxy.setText("Terminate dead proxy connections");
		this.btnTerminateDeadProxy.setToolTipText("Terminate proxy connections when they haven't been active for longer than the request timeout(i.e. the connection is still open but the client and server are done passing data, but didn't close the connection for some reason)");
		this.btnTerminateDeadProxy.setSelection(JavaWebServer.proxyTerminateDeadConnections);
		
		this.btnTerminateDeadProxyConnectionTimeouts = new Button(this.proxySettings, SWT.CHECK);
		this.btnTerminateDeadProxyConnectionTimeouts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JavaWebServer.proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached = OptionsDialog.this.btnTerminateDeadProxyConnectionTimeouts.getSelection();
			}
		});
		this.btnTerminateDeadProxyConnectionTimeouts.setBounds(0, 124, 436, 16);
		this.btnTerminateDeadProxyConnectionTimeouts.setText("Terminate proxy connections after the connection timeout has been reached");
		this.btnTerminateDeadProxyConnectionTimeouts.setToolTipText("Terminate potentially dead proxy connections when they haven't been active for longer than the request timeout(i.e. the connection is still open, and only one of the client or server are trying to pass data, but the other isn't responding anymore for some reason). This is less common and should generally be left set to false unless you start having hundreds of open connections sitting around eating up server threads and resources.");
		this.btnTerminateDeadProxyConnectionTimeouts.setSelection(JavaWebServer.proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached);
		this.btnTerminateDeadProxyConnectionTimeouts.setEnabled(this.btnTerminateDeadProxy.getSelection());
		
		Button btnEnableConnectionLimit = new Button(this.proxySettings, SWT.CHECK);
		btnEnableConnectionLimit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean check = JavaWebServer.proxyEnableSinBinConnectionLimit;
				JavaWebServer.proxyEnableSinBinConnectionLimit = btnEnableConnectionLimit.getSelection();
				if(!check && check != JavaWebServer.proxyEnableSinBinConnectionLimit) {//if it was enabled before and it's disabled now, then:
					for(NaughtyClientData data : JavaWebServer.sinBin) {//TODO get all the same stuff collected into either naughtyClientData class or some other class that is used to manage general client banning
						data.sameIpHasBeenUsingProxyConnections = false;
						data.saveToFile();
					}
				}
			}
		});
		btnEnableConnectionLimit.setToolTipText("Whether or not the sinbin automatic banning of client ip addresses based on excessive number of open connections to this server applies to proxy connections.\r\n\r\nNot recommended since one computer can make 20 or even hundreds of open connections to the internet at once, quickly resulting in this server incorrectly banning the client for \"Too many connected clients at the same time\" or similar.");
		btnEnableConnectionLimit.setBounds(0, 146, 436, 16);
		btnEnableConnectionLimit.setText("Enable connection limit for proxy connections(not recommended)");
		btnEnableConnectionLimit.setSelection(JavaWebServer.proxyEnableSinBinConnectionLimit);
		
		Label label_1 = new Label(this.proxySettings, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(0, 94, 446, 2);
		
		this.btnTerminateDeadProxy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OptionsDialog.this.btnTerminateDeadProxyConnectionTimeouts.setEnabled(OptionsDialog.this.btnTerminateDeadProxy.getSelection());
				if(!OptionsDialog.this.btnTerminateDeadProxy.getSelection()) {
					OptionsDialog.this.btnTerminateDeadProxyConnectionTimeouts.setSelection(false);
				}
				JavaWebServer.proxyTerminateDeadConnections = OptionsDialog.this.btnTerminateDeadProxyConnectionTimeouts.getSelection();
				JavaWebServer.proxyTerminatePotentiallyDeadConnectionsWhenRequestTimeoutReached = OptionsDialog.this.btnTerminateDeadProxyConnectionTimeouts.getSelection();
			}
		});
		
		Label lblImage = new Label(this.shell, SWT.NONE);
		lblImage.setImage(SWTResourceManager.getImage(OptionsDialog.class, "/assets/textures/icons/question.ico"));
		lblImage.setBounds(10, 10, 48, 48);
		
		Label lblMsg = new Label(this.shell, SWT.WRAP);
		lblMsg.setText("Pick a category of options to change. Most changes will take effect immediately. Options in red require that the server be restarted before their changes can take effect.");
		lblMsg.setBounds(64, 10, 370, 48);
		
		this.btnRestoreDefaults = new Button(this.shell, SWT.NONE);
		this.btnRestoreDefaults.setBounds(10, 359, 112, 23);
		this.btnRestoreDefaults.setText("Restore Defaults(NYI)");
		
		Button btnDone = new Button(this.shell, SWT.NONE);
		btnDone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OptionsDialog.this.response = Response.DONE;
				JavaWebServer.saveOptionsToFile(true);
			}
		});
		btnDone.setBounds(352, 359, 112, 23);
		btnDone.setText("Done");
	}
}
