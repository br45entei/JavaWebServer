package com.gmail.br45entei.gui;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.Response;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class ConfirmHideWindowDialog extends Dialog {
	
	protected Response		result	= Response.NO_RESPONSE;
	protected Shell			shell;
	protected final Shell	parentShell;
	
	/** Create the dialog.
	 * 
	 * @param parent The parent shell
	 * @param style The SWT style */
	public ConfirmHideWindowDialog(Shell parent, int style) {
		super(parent, style);
		this.parentShell = parent;
		setText("SWT Dialog");
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
			if(!display.readAndDispatch()) {
				display.sleep();
			}
			if(this.result != Response.NO_RESPONSE) {
				this.shell.dispose();
				break;
			}
		}
		return this.result;
	}
	
	/** Create contents of the dialog. */
	private void createContents() {
		this.shell = new Shell(getParent(), SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.shell.setImages(new Image[] {SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-16x16.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-32x32.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-64x64.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-128x128.png")});
		this.shell.setSize(344, 180);
		this.shell.setText(JavaWebServer.APPLICATION_NAME + " " + JavaWebServer.APPLICATION_VERSION + " - Confirm Hide Window");
		Functions.centerShell2OnShell1(this.parentShell, this.shell);//Functions.centerShellOnPrimaryMonitor(this.shell);
		
		Text lblClosingThisWindow = new Text(this.shell, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		lblClosingThisWindow.setBounds(64, 10, 264, 87);
		lblClosingThisWindow.setText("Closing this window will not close the server. If you wish to close the server, type \"exit\" into the server console. To view this window again, type \"showWindow\" into the console.\r\nIf you are using the \"JavaWebServer Console Window\" and want to edit server options without having to open this window again, type \"options\" into the console.");
		
		Label lblImage = new Label(this.shell, SWT.NONE);
		lblImage.setImage(SWTResourceManager.getImage(ConfirmHideWindowDialog.class, "/assets/textures/icons/information.ico"));
		lblImage.setBounds(10, 10, 48, 87);
		
		Label lblAreYouSure = new Label(this.shell, SWT.NONE);
		lblAreYouSure.setBounds(10, 103, 318, 13);
		lblAreYouSure.setText("Are you sure you wish to hide the window?");
		
		Button btnYes = new Button(this.shell, SWT.NONE);
		btnYes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfirmHideWindowDialog.this.result = Response.YES;
			}
		});
		btnYes.setBounds(10, 122, 89, 23);
		btnYes.setText("Yes");
		
		Button btnNo = new Button(this.shell, SWT.NONE);
		btnNo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfirmHideWindowDialog.this.result = Response.NO;
			}
		});
		btnNo.setBounds(105, 122, 89, 23);
		btnNo.setText("No");
		
		Button btnShutdownTheServer = new Button(this.shell, SWT.NONE);
		btnShutdownTheServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfirmHideWindowDialog.this.result = Response.CLOSE;
			}
		});
		btnShutdownTheServer.setBounds(200, 122, 128, 23);
		btnShutdownTheServer.setText("Shutdown the Server");
		
	}
}
