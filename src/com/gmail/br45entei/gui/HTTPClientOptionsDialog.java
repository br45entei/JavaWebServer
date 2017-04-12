package com.gmail.br45entei.gui;

import com.gmail.br45entei.swt.Response;
import com.gmail.br45entei.swt.dialog.AbstractDialog;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** @author Brian_Entei */
public class HTTPClientOptionsDialog extends AbstractDialog {
	
	protected Response result;
	protected Shell shell;
	
	/** Create the dialog.
	 * 
	 * @param parent
	 * @param style */
	public HTTPClientOptionsDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}
	
	/** Open the dialog.
	 * 
	 * @return the result */
	@Override
	public Response open() {
		createContents();
		this.shell.open();
		this.shell.layout();
		Display display = getParent().getDisplay();
		while(!this.shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return this.result;
	}
	
	@Override
	public Shell getShell() {
		return this.shell;
	}
	
	/** Create contents of the dialog. */
	@Override
	protected void createContents(String... args) {
		this.shell = new Shell(getParent(), getStyle());
		this.shell.setSize(450, 300);
		this.shell.setText(getText());
		
	}
	
	@Override
	protected boolean _runClock(Display display) {
		// TODO Auto-generated method stub (42!)
		return false;
	}
	
	@Override
	protected void runClock() {
		// TODO Auto-generated method stub (42!)
		
	}
	
	@Override
	protected void updateUI() {
		// TODO Auto-generated method stub (42!)
		
	}
	
	@Override
	protected boolean mainLoop(Display display) {
		// TODO Auto-generated method stub (42!)
		return false;
	}
	
}
