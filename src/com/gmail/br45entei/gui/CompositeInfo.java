package com.gmail.br45entei.gui;

import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.ClientStatus;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtils;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/** Custom Composite class that displays various client data transfer
 * information
 *
 * @author Brian_Entei */
public final class CompositeInfo extends Composite {
	protected static final int width = 403;
	protected static final int height = 84;
	
	protected final Shell shell;
	protected final ClientStatus status;
	protected final UUID uuid;
	protected final ConnectionType type;
	protected boolean isInList = false;
	
	protected final boolean pausable;
	protected final Label lblActiveConnection;
	protected final Button btnCancelTransfer;
	protected final ProgressBar progressBar;
	protected final Button btnPauseTransfer;
	
	/** @param shell The shell
	 * @param parent The parent
	 * @param style The SWT style
	 * @param info The ClientInfo */
	protected CompositeInfo(Shell shell, Composite parent, int style, ClientStatus status) {
		this(shell, parent, style, status, !status.isProxyRequest());
	}
	
	public static enum ConnectionType {
		INCOMING,
		OUTGOING,
		PROXY,
		UNKNOWN;
	}
	
	protected CompositeInfo(Shell shell, Composite parent, int style, ClientStatus status, boolean pausable) {
		super(parent, style);
		this.shell = shell;
		this.status = status;
		this.uuid = status.getUUID();//status != null ? status.getUUID() : null;
		this.pausable = pausable;
		this.type = status.isIncoming() ? ConnectionType.INCOMING : (status.isOutgoing() ? ConnectionType.OUTGOING : (status.isProxyRequest() ? ConnectionType.PROXY : ConnectionType.UNKNOWN));
		
		int numOfCurrentClientInfos = (this.type == ConnectionType.OUTGOING ? ClientConnection.getOutgoingConnections() : (this.type == ConnectionType.INCOMING ? ClientConnection.getIncomingConnections() : (this.type == ConnectionType.PROXY ? ClientConnection.getProxyConnections() : new java.util.ArrayList<ClientConnection>()))).size() - 1;//(outgoing ? Main.getCurrentOutgoingClientInfos() : Main.getCurrentIncomingClientInfos()).size() - 1;
		numOfCurrentClientInfos = (numOfCurrentClientInfos < 0 ? 0 : numOfCurrentClientInfos);
		
		this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.setBounds(10, 10 + (height * numOfCurrentClientInfos), width, height);
		
		this.lblActiveConnection = new Label(this, SWT.WRAP | SWT.NO_BACKGROUND);
		this.lblActiveConnection.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.lblActiveConnection.setFont(SWTResourceManager.getFont("Segoe UI", 6, SWT.NORMAL));
		this.lblActiveConnection.moveAbove(this);
		
		this.btnCancelTransfer = new Button(this, SWT.NONE);
		this.btnCancelTransfer.setText("Cancel Transfer");
		this.btnCancelTransfer.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CompositeInfo.this.status.cancel();
				if(status.getDomainDirectory() != null && status.getDomainDirectory().getDisplayLogEntries()) {
					if(status.getRequestedFile() != null) {
						PrintUtil.printlnNow("Cancelled active transfer for client: " + status.getRequestedFile().toString());
					} else {
						PrintUtil.printlnNow("Cancelled active transfer for client: " + status.toString());
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
		});
		this.btnCancelTransfer.setToolTipText("Terminates the connection between\r\nthis server and the client.");
		this.btnCancelTransfer.moveAbove(this);
		
		this.progressBar = new ProgressBar(this, SWT.NONE);
		this.progressBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		this.progressBar.moveAbove(this);
		
		this.btnPauseTransfer = new Button(this, SWT.NONE);
		this.btnPauseTransfer.setImage(SWTResourceManager.getImage(Main.class, "/assets/textures/icons/pause.ico"));
		this.btnPauseTransfer.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CompositeInfo.this.status.togglePause();
				CompositeInfo.this.btnPauseTransfer.setToolTipText((CompositeInfo.this.status.isPaused() ? "Resume" : "Pause") + " the data transfer between\r\nthis server and the client.");
				CompositeInfo.this.btnPauseTransfer.setImage(SWTResourceManager.getImage(Main.class, CompositeInfo.this.status.isPaused() ? "/assets/textures/icons/play.ico" : "/assets/textures/icons/pause.ico"));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
		});
		this.btnPauseTransfer.setToolTipText("Pause the data transfer between\r\nthis server and the client.");
		this.btnPauseTransfer.moveAbove(this);
		
		this.updateUI();
	}
	
	protected final void updateUI() {
		if(this.isDisposed()) {
			return;
		}
		int width = this.getParent().getSize().x - 20;//this.shell.getSize().x - 57;
		Functions.setSizeFor(this, new Point((width < CompositeInfo.width ? CompositeInfo.width : width), CompositeInfo.height));
		if(!this.status.getClient().isClosed() && this.status.isBeingWrittenTo) {
			this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		} else if(!this.status.isBeingWrittenTo) {
			this.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		} else {
			this.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		}
		
		this.updateLabel();
		this.updateButtons();
		this.updateProgressBar();
	}
	
	private final void updateLabel() {
		if(this.isDisposed()) {
			return;
		}
		this.lblActiveConnection.setBounds(10, 5, this.getSize().x - 24, 37);//lblActiveConnection.setBounds(10, 5, 379, 37);
		String labelStr = this.status.toString();
		this.status.updateTime = 1000L / 5L;//200 milliseconds
		labelStr += "; Status: \"" + this.status.getStatus() + "\";";
		if(this.status.isProxyRequest()) {
			labelStr += "; Last C-to-S write time: " + (this.status.getLastReadTime() <= 0L ? "n/a" : StringUtils.getElapsedTime(System.currentTimeMillis() - this.status.getLastReadTime()));
			labelStr += "; Last S-to-C write time: " + (this.status.getLastWriteTime() <= 0L ? "n/a" : StringUtils.getElapsedTime(System.currentTimeMillis() - this.status.getLastWriteTime()));
		} else {
			if(this.status.getFileName() != null) {
				labelStr += " File name: \"" + this.status.getFileName() + "\";";
			}
			final long timeTaken = this.status.getDataTransferElapsedTime();
			final long bytesPerSecond = this.status.getLastReadAmount() * 5L;
			labelStr += "Data Transfer Rate: " + Functions.humanReadableByteCount(bytesPerSecond, true, 2) + "/sec";
			final long currentData = this.status.getCount();
			final long length = this.status.getContentLength();
			
			if(bytesPerSecond != 0 && currentData != 0) {
				final long secondsRemaining1 = (length - currentData) / bytesPerSecond;
				final long secondsRemaining2 = (timeTaken / currentData) * (length - currentData);//(TimeTaken.TotalSeconds / totalBytesCopied) * (totalFileSizeToCopy - totalBytesCopied);
				
				final double averageSecondsRemaining = ((secondsRemaining1 + secondsRemaining2) / 2L) / 10.0D;
				labelStr += "; Estimated Time Remaining: " + StringUtils.getElapsedTime(Math.round(averageSecondsRemaining * 1000.0D));
			}
			labelStr += "; Last read time: " + (this.status.getLastReadTime() <= 0L ? "n/a" : StringUtils.getElapsedTime(System.currentTimeMillis() - this.status.getLastReadTime()));
			labelStr += "; Last write time: " + (this.status.getLastWriteTime() <= 0L ? "n/a" : StringUtils.getElapsedTime(System.currentTimeMillis() - this.status.getLastWriteTime()));
		}
		labelStr = labelStr.replace("&", "&&") + "; Elapsed time: " + StringUtils.getElapsedTime(System.currentTimeMillis() - this.status.getStartTime());
		Functions.setTextFor(this.lblActiveConnection, labelStr);
	}
	
	private final void updateButtons() {
		if(this.isDisposed()) {
			return;
		}
		Functions.setBoundsFor(this.btnCancelTransfer, new Rectangle(this.getSize().x - 137, 47, 103, 23));//btnCancelTransfer.setBounds(266, 47, 103, 23);
		Functions.setBoundsFor(this.btnPauseTransfer, new Rectangle(this.getSize().x - 34, 47, 23, 23));//btnPauseTransfer.setBounds(369, 47, 23, 23);
		Functions.setEnabledFor(this.btnPauseTransfer, this.pausable);
	}
	
	private final void updateProgressBar() {
		if(this.isDisposed()) {
			return;
		}
		Functions.setBoundsFor(this.progressBar, new Rectangle(10, 49, this.getSize().x - 153, 21));//progressBar.setBounds(10, 49, 250, 21);
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum(100);
		//double min = new Double(this.status.bytesTransfered + ".00D").doubleValue();
		//double max = new Double(this.status.contentLength + ".00D").doubleValue();
		double result = this.status.getProgress() * 100.00D;//(min / max) * 100.00D;
		this.progressBar.setSelection(new Long(Math.round(result)).intValue());
		//
		//this.progressBar.setState(SWT.NORMAL);
		long elapsedTime = System.currentTimeMillis() - this.status.getLastWriteTime();
		boolean isPaused = true;
		if(elapsedTime >= 32000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		} else if(elapsedTime >= 16000L && elapsedTime < 32000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_MAGENTA));
		} else if(elapsedTime >= 8000L && elapsedTime < 16000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
		} else if(elapsedTime >= 4000L && elapsedTime < 8000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		} else if(elapsedTime >= 2000L && elapsedTime < 4000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW));
		} else if(elapsedTime >= 1000L && elapsedTime < 2000L) {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
		} else {
			this.progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
			isPaused = false;
		}
		if(isPaused) {
			//this.status.setLastWriteAmount(0L);
			if(this.progressBar.getState() != SWT.PAUSED) {
				this.progressBar.setState(SWT.PAUSED);
			}
		} else {
			if(this.progressBar.getState() != SWT.NORMAL) {
				this.progressBar.setState(SWT.NORMAL);
			}
		}
		
	}
	
}
