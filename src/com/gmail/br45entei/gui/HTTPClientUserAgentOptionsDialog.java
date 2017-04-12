package com.gmail.br45entei.gui;

import com.gmail.br45entei.server.HTTPClientOptions;
import com.gmail.br45entei.server.autoban.AutobanDictionary;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.Response;
import com.gmail.br45entei.swt.dialog.AbstractApplySettingsDialog;
import com.gmail.br45entei.util.StringUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class HTTPClientUserAgentOptionsDialog extends AbstractApplySettingsDialog {
	
	protected final OptionsDialog parent;
	protected Shell shell;
	private final String title = "HTTP Client User-Agent Options - ";
	
	protected Button btnApply;
	protected Button btnOk;
	protected Button btnCancel;
	
	protected final HTTPClientOptions options = new HTTPClientOptions();
	
	protected Button btnRequireUserAgent;
	protected Text txtBannedUserAgents;
	protected Text txtBannedUserAgentWholeWords;
	protected Text txtBannedClientRequestPaths;
	
	/** Create the dialog.
	 * 
	 * @param parent The parent shell
	 * @wbp.parser.constructor */
	public HTTPClientUserAgentOptionsDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM);
		this.parent = null;
	}
	
	/** Create the dialog.
	 * 
	 * @param parent The parent dialog */
	public HTTPClientUserAgentOptionsDialog(OptionsDialog parent) {
		super(parent.getShell(), SWT.DIALOG_TRIM);
		this.parent = parent;
	}
	
	@Override
	public final Shell getShell() {
		return this.shell;
	}
	
	@Override
	protected final boolean _runClock(Display display) {
		if(this.parent != null) {
			if(this.parent.isDisposed()) {
				return false;
			}
			this.shell.update();
			if(!this.parent.mainLoop(display)) {
				return false;
			}
		}
		if(this.response != Response.NO_RESPONSE) {
			this.shell.dispose();
			return false;
		}
		if(this.shell.isDisposed() || (this.parent != null && this.parent.isDisposed())) {
			return false;
		}
		if(this.parent == null) {
			if(Main.getInstance() != null) {
				if(Main.getInstance().showWindow) {
					Main.getInstance().runLoop();
				} else if(Main.getInstance().isSWTConsoleWindowOpen()) {
					Main.getInstance().getConsoleWindow().runLoop();
				} else {
					this.runClock();
				}
			}
		}
		return true;
	}
	
	/** Runs {@link Display#readAndDispatch()},
	 * then attempts to sleep. */
	@Override
	public final void runClock() {
		super.runClock();
	}
	
	@Override
	protected final void updateSettingsUI() {
		String bannedUserAgents = "";
		String bannedUserAgentWords = "";
		String bannedClientRequestPaths = "";
		for(String bannedUserAgent : this.options.bannedUserAgents) {
			bannedUserAgents += bannedUserAgent + "\n";
		}
		for(String bannedUserAgentWord : this.options.bannedUserAgentWords) {
			bannedUserAgentWords += bannedUserAgentWord + "\n";
		}
		for(String bannedClientRequestPath : this.options.bannedRequestPaths) {
			bannedClientRequestPaths += bannedClientRequestPath + "\n";
		}
		
		this.updatingSettingsUI = true;
		Functions.setTextFor(this.txtBannedUserAgents, bannedUserAgents);
		Functions.setTextFor(this.txtBannedUserAgentWholeWords, bannedUserAgentWords);
		Functions.setTextFor(this.txtBannedClientRequestPaths, bannedClientRequestPaths);
		this.updatingSettingsUI = false;
	}
	
	@Override
	protected final void updateUI() {
		super.updateUI();
		Functions.setSelectionFor(this.btnRequireUserAgent, this.options.clientMustSupplyAUserAgent);
		Functions.setEnabledFor(this.btnApply, this.canApplySettings());
		Functions.setEnabledFor(this.btnRestoreDefaults, this.canRestoreDefaultSettings());
		Functions.setEnabledFor(this.btnUndoCurrentChanges, this.canRestorePreviousSettings());
	}
	
	@Override
	protected final boolean mainLoop(Display display) {
		return super.mainLoop(display);
	}
	
	@Override
	public Response open() {
		//return super.open();
		this.createContents();
		this.restorePreviousSettings();
		this.shell.open();
		this.shell.layout();
		final Display display = super.parent.getDisplay();
		while(!this.shell.isDisposed()) {
			if(!this.mainLoop(display)) {
				break;
			}
		}
		return this.response;
	}
	
	protected Button btnRestoreDefaults;
	protected Button btnUndoCurrentChanges;
	private Button btnClearAllSettings;
	
	/** Create contents of the dialog. */
	@Override
	protected void createContents(String... args) {
		this.shell = new Shell(this.getParent(), SWT.DIALOG_TRIM);
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				HTTPClientUserAgentOptionsDialog.this.response = Response.CLOSE;
			}
		});
		this.shell.setImages(Main.getDefaultShellImages());
		this.shell.setSize(800, 600);
		this.shell.setText(this.getText());
		this.shell.setText(this.title + this.getParent().getText());
		Functions.centerShell2OnShell1(this.getParent(), this.shell);
		
		this.btnApply = new Button(this.shell, SWT.NONE);
		this.btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.applySettings();
			}
		});
		this.btnApply.setBounds(684, 537, 100, 25);
		this.btnApply.setText("Apply");
		
		this.btnOk = new Button(this.shell, SWT.NONE);
		this.btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(HTTPClientUserAgentOptionsDialog.this.applySettings()) {
					HTTPClientUserAgentOptionsDialog.this.response = Response.OK;
				}
			}
		});
		this.btnOk.setBounds(472, 537, 100, 25);
		this.btnOk.setText("OK");
		
		this.btnCancel = new Button(this.shell, SWT.NONE);
		this.btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.response = Response.CANCEL;
			}
		});
		this.btnCancel.setBounds(578, 537, 100, 25);
		this.btnCancel.setText("Cancel");
		
		this.btnRequireUserAgent = new Button(this.shell, SWT.CHECK);
		this.btnRequireUserAgent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.options.clientMustSupplyAUserAgent = !HTTPClientUserAgentOptionsDialog.this.options.clientMustSupplyAUserAgent;
			}
		});
		this.btnRequireUserAgent.setBounds(10, 10, 300, 16);
		this.btnRequireUserAgent.setText("Client browsers must supply a User-Agent header");
		
		Label label = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(10, 32, 774, 2);
		
		Label lblBannedUserAgents = new Label(this.shell, SWT.NONE);
		lblBannedUserAgents.setBounds(10, 40, 384, 15);
		lblBannedUserAgents.setText("Ban user agents exactly matching(case sensitive):");
		
		this.txtBannedUserAgents = new Text(this.shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		this.txtBannedUserAgents.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(HTTPClientUserAgentOptionsDialog.this.updatingSettingsUI) {
					return;
				}
				HTTPClientUserAgentOptionsDialog.this.options.bannedUserAgents.clear();
				for(String bannedUserAgent : HTTPClientUserAgentOptionsDialog.this.txtBannedUserAgents.getText().split(Pattern.quote("\n"))) {
					bannedUserAgent = bannedUserAgent.endsWith("\r") && bannedUserAgent.length() > 1 ? bannedUserAgent.substring(0, bannedUserAgent.length() - 1) : bannedUserAgent;
					if(!bannedUserAgent.trim().isEmpty()) {
						HTTPClientUserAgentOptionsDialog.this.options.bannedUserAgents.add(bannedUserAgent);
					}
				}
			}
		});
		this.txtBannedUserAgents.setBounds(10, 61, 384, 300);
		
		Label lblBannedUserAgent = new Label(this.shell, SWT.NONE);
		lblBannedUserAgent.setBounds(400, 40, 384, 15);
		lblBannedUserAgent.setText("Ban user agents containing the following words(case insensitive):");
		
		this.txtBannedUserAgentWholeWords = new Text(this.shell, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		this.txtBannedUserAgentWholeWords.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(HTTPClientUserAgentOptionsDialog.this.updatingSettingsUI) {
					return;
				}
				HTTPClientUserAgentOptionsDialog.this.options.bannedUserAgentWords.clear();
				for(String bannedUserAgentWord : HTTPClientUserAgentOptionsDialog.this.txtBannedUserAgentWholeWords.getText().split(Pattern.quote("\n"))) {
					bannedUserAgentWord = bannedUserAgentWord.endsWith("\r") && bannedUserAgentWord.length() > 1 ? bannedUserAgentWord.substring(0, bannedUserAgentWord.length() - 1) : bannedUserAgentWord;
					if(!bannedUserAgentWord.trim().isEmpty()) {
						HTTPClientUserAgentOptionsDialog.this.options.bannedUserAgentWords.add(bannedUserAgentWord);
					}
				}
			}
		});
		this.txtBannedUserAgentWholeWords.setBounds(400, 61, 384, 300);
		
		Label lblBannedClientRequest = new Label(this.shell, SWT.NONE);
		lblBannedClientRequest.setBounds(10, 375, 774, 15);
		lblBannedClientRequest.setText("Banned client request paths(case sensitive; prefix with '(?i)' for insensitive case):");
		
		this.txtBannedClientRequestPaths = new Text(this.shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		this.txtBannedClientRequestPaths.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(HTTPClientUserAgentOptionsDialog.this.updatingSettingsUI) {
					return;
				}
				HTTPClientUserAgentOptionsDialog.this.options.bannedRequestPaths.clear();
				ArrayList<String> invalidPaths = new ArrayList<>();
				for(String bannedClientRequestPath : HTTPClientUserAgentOptionsDialog.this.txtBannedClientRequestPaths.getText().split(Pattern.quote("\n"))) {
					bannedClientRequestPath = bannedClientRequestPath.endsWith("\r") && bannedClientRequestPath.length() > 1 ? bannedClientRequestPath.substring(0, bannedClientRequestPath.length() - 1) : bannedClientRequestPath;
					if(!bannedClientRequestPath.trim().isEmpty()) {
						if(!(bannedClientRequestPath.startsWith("(?i)") ? bannedClientRequestPath.substring("(?i)".length()) : bannedClientRequestPath).startsWith("/")) {
							invalidPaths.add(bannedClientRequestPath);
							//continue;
						}
						HTTPClientUserAgentOptionsDialog.this.options.bannedRequestPaths.add(bannedClientRequestPath);
					}
				}
				if(!invalidPaths.isEmpty()) {
					HTTPClientUserAgentOptionsDialog.this.txtBannedClientRequestPaths.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				} else {
					HTTPClientUserAgentOptionsDialog.this.txtBannedClientRequestPaths.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
				}
			}
		});
		this.txtBannedClientRequestPaths.setBounds(10, 396, 774, 127);
		
		Label label_1 = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(10, 367, 774, 2);
		
		Label label_2 = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_2.setBounds(10, 529, 774, 2);
		
		this.btnRestoreDefaults = new Button(this.shell, SWT.NONE);
		this.btnRestoreDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.restoreDefaultSettings();
			}
		});
		this.btnRestoreDefaults.setBounds(10, 537, 100, 25);
		this.btnRestoreDefaults.setText("Restore defaults");
		
		this.btnUndoCurrentChanges = new Button(this.shell, SWT.NONE);
		this.btnUndoCurrentChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.restorePreviousSettings();
			}
		});
		this.btnUndoCurrentChanges.setBounds(116, 537, 140, 25);
		this.btnUndoCurrentChanges.setText("Undo current changes");
		
		this.btnClearAllSettings = new Button(this.shell, SWT.NONE);
		this.btnClearAllSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HTTPClientUserAgentOptionsDialog.this.clearSettings();
			}
		});
		this.btnClearAllSettings.setBounds(262, 537, 100, 25);
		this.btnClearAllSettings.setText("Clear all settings");
		
	}
	
	@Override
	public final void clearSettings() {
		this.options.setFrom(null);
		this.updateSettingsUI();
		this.updateUI();
	}
	
	@Override
	public final boolean canRestoreDefaultSettings() {
		return !this.options.equals(AutobanDictionary.defaultSettings);
	}
	
	@Override
	public final void restoreDefaultSettings() {
		this.options.setFrom(AutobanDictionary.defaultSettings);
		this.updateSettingsUI();
		this.updateUI();
	}
	
	@Override
	public final boolean canRestorePreviousSettings() {
		return !this.options.equals(AutobanDictionary.autoBanSettings);
	}
	
	@Override
	public final void restorePreviousSettings() {
		this.options.setFrom(AutobanDictionary.autoBanSettings);
		this.updateSettingsUI();
		this.updateUI();
	}
	
	@Override
	public final boolean canApplySettings() {
		return !this.options.equals(AutobanDictionary.autoBanSettings);
	}
	
	@Override
	public final boolean applySettings() {
		HTTPClientOptions backup = new HTTPClientOptions(AutobanDictionary.autoBanSettings);
		ArrayList<String> invalidPaths = this.cleanInvalidRequestPaths();
		if(!this.isDisposed() && !invalidPaths.isEmpty()) {
			new PopupDialog(HTTPClientUserAgentOptionsDialog.this.shell, "Invalid request paths detected - " + HTTPClientUserAgentOptionsDialog.this.shell.getText(), "The following request paths did not start with a forward slash('/') and were ignored:\n\n" + StringUtil.stringArrayToString('\n', invalidPaths)).open();
		}
		AutobanDictionary.autoBanSettings.setFrom(this.options);
		if(!AutobanDictionary.saveToFile()) {
			AutobanDictionary.autoBanSettings.setFrom(backup);
			if(!this.isDisposed()) {
				new PopupDialog(this.shell, "Error saving autoban settings", "Unable to save autoban settings to file!\r\nReason: " + StringUtil.throwableToStrNoStackTraces(AutobanDictionary.getSaveException())).open();
			}
			return backup.equals(this.options);
		}
		return true;
	}
	
	private final ArrayList<String> cleanInvalidRequestPaths() {
		String[] split = new String[this.options.bannedRequestPaths.size()];
		this.options.bannedRequestPaths.toArray(split);
		HTTPClientUserAgentOptionsDialog.this.options.bannedRequestPaths.clear();
		ArrayList<String> invalidPaths = new ArrayList<>();
		for(String bannedClientRequestPath : split) {
			if(!bannedClientRequestPath.isEmpty()) {
				if(!(bannedClientRequestPath.startsWith("(?i)") ? bannedClientRequestPath.substring("(?i)".length()) : bannedClientRequestPath).startsWith("/")) {
					invalidPaths.add(bannedClientRequestPath);
					continue;
				}
				HTTPClientUserAgentOptionsDialog.this.options.bannedRequestPaths.add(bannedClientRequestPath);
			}
		}
		return invalidPaths;
	}
	
}
