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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/** @author Brian_Entei */
public class AboutDialog extends Dialog {
	
	protected Response result = Response.NO_RESPONSE;
	protected Shell shell;
	protected final Shell parentShell;
	
	/** Create the dialog.
	 * 
	 * @param parent The parent shell
	 * @param style the SWT style */
	public AboutDialog(Shell parent, int style) {
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
		//Display display = getParent().getDisplay();
		while(!this.shell.isDisposed()) {
			//if(!display.readAndDispatch()) {
			//	display.sleep();
			//}
			Main.getInstance().runLoop();
			if(this.result != Response.NO_RESPONSE) {
				this.shell.close();
				break;
			}
		}
		return this.result;
	}
	
	/** Create contents of the dialog. */
	private void createContents() {
		this.shell = new Shell(getParent(), SWT.CLOSE | SWT.TITLE);
		this.shell.setImages(new Image[] {SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-16x16.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-32x32.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-64x64.png"), SWTResourceManager.getImage(Main.class, "/assets/textures/title/Entei-128x128.png")});
		this.shell.setSize(315, 165);
		this.shell.setText("About \"" + JavaWebServer.APPLICATION_NAME + "\"");
		Functions.centerShell2OnShell1(this.parentShell, this.shell);//Functions.centerShellOnPrimaryMonitor(this.shell);
		
		Label lblImage = new Label(this.shell, SWT.NONE);
		lblImage.setImage(SWTResourceManager.getImage(AboutDialog.class, "/assets/textures/icons/information.ico"));
		lblImage.setBounds(10, 10, 48, 88);
		
		Label lblDesc = new Label(this.shell, SWT.WRAP);
		lblDesc.setBounds(64, 10, 235, 88);
		lblDesc.setText("Entei Server Version " + JavaWebServer.APPLICATION_VERSION + "\r\nAuthor: Brian Entei\r\nE-mail: br45entei@gmail.com\r\nThis is a beta release of an experimental\r\nweb server written using Java.");
		
		Button btnDone = new Button(this.shell, SWT.NONE);
		btnDone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutDialog.this.result = Response.DONE;
			}
		});
		btnDone.setBounds(10, 104, 289, 23);
		btnDone.setText("Done");
		
	}
	
}
