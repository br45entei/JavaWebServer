package com.gmail.br45entei.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** A test SWT class */
public class Snippet5 {
	
	/** @param args System command arguments */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		// this button is always 400 x 400. Scroll bars appear if the window is resized to be
		// too small to show part of the button
		ScrolledComposite c1 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Button b1 = new Button(c1, SWT.PUSH);
		b1.setText("fixed size button");
		b1.setSize(400, 400);
		c1.setContent(b1);
		
		// this button has a minimum size of 400 x 400. If the window is resized to be big enough
		// to show more than 400 x 400, the button will grow in size. If the window is made too
		// small to show 400 x 400, scroll bars will appear.
		ScrolledComposite c2 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Button b2 = new Button(c2, SWT.PUSH);
		b2.setText("expanding button");
		c2.setContent(b2);
		c2.setExpandHorizontal(true);
		c2.setExpandVertical(true);
		c2.setMinWidth(400);
		c2.setMinHeight(400);
		
		shell.setSize(600, 300);
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
	
}