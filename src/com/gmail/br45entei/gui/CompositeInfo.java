package com.gmail.br45entei.gui;

import com.gmail.br45entei.server.ClientInfo;
import com.gmail.br45entei.server.ClientRequestStatus;
import com.gmail.br45entei.server.ClientStatus;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.StringUtil;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/** Custom Composite class that displays various client data transfer information
 *
 * @author Brian_Entei */
public final class CompositeInfo extends Composite {
	protected static final int		width		= 403;
	protected static final int		height		= 84;
	
	protected final Shell			shell;
	protected final ClientStatus	status;
	protected final UUID			uuid;
	protected boolean				isInList	= false;
	
	protected final boolean			pausable;
	protected final Label			lblActiveConnection;
	protected final Button			btnCancelTransfer;
	protected final ProgressBar		progressBar;
	protected final Button			btnPauseTransfer;
	
	/** @param shell The shell
	 * @param parent The parent
	 * @param style The SWT style
	 * @param info The ClientInfo */
	protected CompositeInfo(Shell shell, Composite parent, int style, ClientStatus status, boolean outgoing) {
		this(shell, parent, style, status, outgoing, true);
	}
	
	protected CompositeInfo(Shell shell, Composite parent, int style, ClientStatus status, boolean outgoing, boolean pausable) {
		super(parent, style);
		this.shell = shell;
		this.status = status;
		this.uuid = status.getUUID();
		this.pausable = pausable;
		
		int numOfCurrentClientInfos = (outgoing ? Main.getCurrentOutgoingClientInfos() : Main.getCurrentIncomingClientInfos()).size() - 1;
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
				if(CompositeInfo.this.status instanceof ClientInfo) {
					ClientInfo info = (ClientInfo) CompositeInfo.this.status;
					if(info.getDomainDirectory().getDisplayLogEntries()) {
						System.out.println("Cancelled active transfer for client: " + info.requestedFile.toString());
					}
					while(true) {
						try {
							info.removeFromList();
							break;
						} catch(Throwable ignored) {
						}
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
		this.setSize((width < CompositeInfo.width ? CompositeInfo.width : width), CompositeInfo.height);
		if(this.status instanceof ClientInfo) {
			ClientInfo info = (ClientInfo) this.status;
			if(!info.getClient().isClosed() && info.requestedFile.isBeingWrittenTo) {
				this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			} else if(!info.requestedFile.isBeingWrittenTo) {
				this.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
			} else {
				this.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		} else {
			this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
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
		String labelStr;
		if(this.status instanceof ClientInfo) {
			ClientInfo info = (ClientInfo) this.status;
			labelStr = "Client Info: " + info.getClientAddress() + "; Host used: \"" + info.host + "\"; " + info.requestedFile.toString() + "; Data: " + Functions.humanReadableByteCount(info.requestedFile.bytesTransfered, true, 2) + " / " + Functions.humanReadableByteCount(new Long(info.requestedFile.contentLength).longValue(), true, 2) + "; Data Transfer Rate: " + Functions.humanReadableByteCount(info.requestedFile.lastWriteAmount * 5L, true, 2) + "/sec";
			info.requestedFile.updateTime = 1000L / 5L;
		} else {
			labelStr = "Client Info: " + this.status.getClientAddress() + "; Data: " + Functions.humanReadableByteCount(this.status.getCount(), true, 2) + " / " + Functions.humanReadableByteCount(new Long(this.status.getContentLength()).longValue(), true, 2) + " uploaded;";
			if(this.status instanceof ClientRequestStatus) {
				ClientRequestStatus status = (ClientRequestStatus) this.status;
				labelStr += " Status: \"" + status.getStatus() + "\";";
				if(status.getFileName() != null) {
					labelStr += " File name: \"" + status.getFileName() + "\";";
				}
			}
		}
		labelStr = labelStr.replace("&", "&&") + " Elapsed time: " + StringUtil.getElapsedTime(System.currentTimeMillis() - this.status.getStartTime());
		if(this.status instanceof ClientInfo) {
			labelStr += "; Last write time: " + StringUtil.getElapsedTime(System.currentTimeMillis() - ((ClientInfo) this.status).getLastWriteTime());
		} else if(this.status instanceof ClientRequestStatus) {
			final ClientRequestStatus status = (ClientRequestStatus) this.status;
			if(status.isProxyRequest()) {
				labelStr += "; Last C-to-S write time: " + StringUtil.getElapsedTime(System.currentTimeMillis() - status.getLastReadTime());
				labelStr += "; Last S-to-C write time: " + StringUtil.getElapsedTime(System.currentTimeMillis() - status.getLastWriteTime());
			} else {
				labelStr += "; Last read time: " + StringUtil.getElapsedTime(System.currentTimeMillis() - status.getLastReadTime());
			}
		}
		this.lblActiveConnection.setText(labelStr);
	}
	
	private final void updateButtons() {
		if(this.isDisposed()) {
			return;
		}
		this.btnCancelTransfer.setBounds(this.getSize().x - 137, 47, 103, 23);//btnCancelTransfer.setBounds(266, 47, 103, 23);
		this.btnPauseTransfer.setBounds(this.getSize().x - 34, 47, 23, 23);//btnPauseTransfer.setBounds(369, 47, 23, 23);
		if(this.pausable) {
			if(!this.btnPauseTransfer.isEnabled()) {
				this.btnPauseTransfer.setEnabled(true);
			}
		} else {
			if(this.btnPauseTransfer.isEnabled()) {
				this.btnPauseTransfer.setEnabled(false);
			}
		}
	}
	
	private final void updateProgressBar() {
		if(this.isDisposed()) {
			return;
		}
		this.progressBar.setBounds(10, 49, this.getSize().x - 153, 21);//progressBar.setBounds(10, 49, 250, 21);
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum(100);
		//double min = new Double(this.status.requestedFile.bytesTransfered + ".00D").doubleValue();
		//double max = new Double(this.status.requestedFile.contentLength + ".00D").doubleValue();
		double result = this.status.getProgress() * 100.00D;//(min / max) * 100.00D;
		this.progressBar.setSelection(new Long(Math.round(result)).intValue());
		//
		this.progressBar.setState(SWT.NORMAL);
		if(this.status instanceof ClientInfo) {
			ClientInfo info = (ClientInfo) this.status;
			long elapsedTime = System.currentTimeMillis() - info.requestedFile.lastWriteTime;
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
				info.requestedFile.lastWriteAmount = 0L;
			}
		}
	}
	
}
