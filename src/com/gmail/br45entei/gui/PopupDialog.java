package com.gmail.br45entei.gui;

import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.swt.Response;
import com.gmail.br45entei.util.CodeUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public final class PopupDialog extends Dialog {
	
	protected Response result = Response.NO_RESPONSE;
	protected Shell shell;
	private final String title;
	private Text message;
	
	/** Create the dialog.
	 * 
	 * @param parent
	 * @param style */
	public PopupDialog(Shell parent, String title, String message) {
		super(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		setText("SWT Dialog");
		this.title = title;
		createContents(message);
	}
	
	protected final void _runClock() {
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
		if(this.result != Response.NO_RESPONSE) {
			this.shell.dispose();
			return false;
		}
		if(this.shell.isDisposed()) {
			return false;
		}
		this._runClock();
		this.updateUI();
		this._runClock();
		if(this.shell.isDisposed()) {
			return false;
		}
		return true;
	}
	
	/** Open the dialog.
	 * 
	 * @return the result */
	public Response open() {
		this.shell.open();
		this.shell.layout();
		Display display = getParent().getDisplay();
		while(!this.shell.isDisposed()) {
			if(!this.mainLoop(display)) {
				
				break;
			}
		}
		return this.result;
	}
	
	private final void updateUI() {
		Functions.setTextFor(this.shell, this.title + " - " + this.getParent().getText());
		Functions.setShellImages(this.shell, Main.getDefaultShellImages());
	}
	
	/** Create contents of the dialog. */
	private void createContents(String message) {
		this.shell = new Shell(getParent(), getStyle());
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				PopupDialog.this.result = Response.CLOSE;
			}
		});
		this.shell.setSize(450, 177);
		this.shell.setText(this.title + " - " + this.getParent().getText());
		Functions.centerShell2OnShell1(getParent(), this.shell);
		this.shell.setImages(Main.getDefaultShellImages());
		
		this.message = new Text(this.shell, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		this.message.setBounds(10, 10, 424, 100);
		this.message.setText(message);
		
		Button btnDone = new Button(this.shell, SWT.NONE);
		btnDone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PopupDialog.this.result = Response.DONE;
			}
		});
		btnDone.setBounds(10, 116, 424, 23);
		btnDone.setText("Done");
		
	}
	
}
