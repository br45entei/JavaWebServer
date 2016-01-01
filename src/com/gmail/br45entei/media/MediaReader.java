package com.gmail.br45entei.media;

import com.gmail.br45entei.data.DisposableByteArrayOutputStream;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

/** @author Brian_Entei */
public class MediaReader {
	
	private static final PrintStream						out;
	private static final PrintStream						err;
	private static final PrintStream						dumpOut;
	private static final PrintStream						dumpErr;
	private static final DisposableByteArrayOutputStream	dOut;
	
	static {
		out = System.out;
		err = System.err;
		dOut = new DisposableByteArrayOutputStream();
		dumpOut = new PrintStream(dOut);
		dumpErr = new PrintStream(dOut);
	}
	
	private static final void disableLogger(Logger logger) {
		if(logger == null) {
			return;
		}
		try {
			logger.setLevel(Level.OFF);
			Handler[] handlers = logger.getHandlers();
			if(handlers != null) {
				for(Handler handler : new ArrayList<>(Arrays.asList(handlers))) {
					if(handler == null) {
						continue;
					}
					logger.removeHandler(handler);
				}
			}
		} catch(SecurityException e) {
			System.err.print("Unable to remove handler from logger \"" + logger.getName() + "\": ");
			e.printStackTrace();
		}
	}
	
	private static final void disableLogging() {
		LogManager lm = LogManager.getLogManager();
		Enumeration<String> loggers = lm.getLoggerNames();
		if(loggers != null) {
			while(loggers.hasMoreElements()) {
				String loggerName = loggers.nextElement();
				if(loggerName != null) {
					disableLogger(lm.getLogger(loggerName));
				}
			}
		}
		lm.reset();
		Logger global = Logger.getGlobal();
		disableLogger(global);
	}
	
	private static final synchronized MediaInfo readFileNoDispose(File file, boolean getArtwork) throws Throwable {
		if(file == null || !file.isFile()) {
			return null;
		}
		disableLogging();
		System.setOut(dumpOut);
		System.setErr(dumpErr);
		AudioFile afile = AudioFileIO.read(file);
		MediaInfo rtrn = afile == null ? null : new MediaInfo(afile, getArtwork);
		System.setOut(out);
		System.setErr(err);
		return rtrn;
	}
	
	public static final synchronized MediaInfo readFile(File file, boolean getArtwork) throws Throwable {
		MediaInfo rtrn = readFileNoDispose(file, getArtwork);
		dOut.dispose();
		return rtrn;
	}
	
	public static final byte[] readFileArtworkAsPNG(File file) {
		if(file == null || !file.isFile()) {
			return null;
		}
		AudioFile afile = null;
		disableLogging();
		try {
			afile = AudioFileIO.read(file);
		} catch(CannotReadException e) {
			System.err.print("Unable to read file: ");
			e.printStackTrace();
		} catch(IOException e) {
			System.err.print("An I/O Exception occurred: ");
			e.printStackTrace();
		} catch(TagException e) {
			System.err.print("Unable to read file's tag data: ");
			e.printStackTrace();
		} catch(ReadOnlyFileException e) {//???
			System.err.print("Unable to read file: File is read only: ");
			e.printStackTrace();
		} catch(InvalidAudioFrameException e) {
			System.err.print("Unable to read file's audio frame data: ");
			e.printStackTrace();
		}
		return readFileArtworkAsPNG(afile);
	}
	
	public static final boolean doesFileHaveArtwork(AudioFile afile) {
		if(afile == null) {
			return false;
		}
		List<Artwork> artworkList = afile.getTag().getArtworkList();
		return artworkList.size() > 0;
	}
	
	public static final Artwork getLargestArtworkFromAudioFile(AudioFile afile) {
		if(afile == null) {
			return null;
		}
		List<Artwork> artworkList = afile.getTag().getArtworkList();
		Artwork biggestArtwork = null;
		if(artworkList.size() > 0) {
			Artwork fallback = artworkList.get(0);
			for(final Artwork artwork : artworkList) {
				if(artwork == null) {
					continue;
				}
				if(fallback == null) {
					fallback = artwork;
				}
				if(biggestArtwork == null) {
					biggestArtwork = artwork;
					continue;
				}
				final boolean widthLarger = artwork.getWidth() > biggestArtwork.getWidth();
				final boolean heightLarger = artwork.getHeight() > biggestArtwork.getHeight();
				
				final boolean widthSameOrLarger = artwork.getWidth() >= biggestArtwork.getWidth();
				final boolean heightSameOrLarger = artwork.getHeight() >= biggestArtwork.getHeight();
				if((widthSameOrLarger && heightSameOrLarger) || (widthLarger && heightSameOrLarger) || (widthSameOrLarger && heightLarger)) {
					biggestArtwork = artwork;
				}
			}
			if(biggestArtwork == null) {
				biggestArtwork = fallback;//artworkList.get(0);
			}
		}
		return biggestArtwork;
	}
	
	public static final byte[] readFileArtworkAsPNG(AudioFile afile) {
		if(afile == null) {
			return null;
		}
		final DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream();
		try {
			Artwork artwork = getLargestArtworkFromAudioFile(afile);
			if(artwork != null) {
				BufferedImage bi = (BufferedImage) artwork.getImage();
				ImageIO.write(bi, "png", baos);
			}
		} catch(Throwable e) {
			System.err.print("Unable to read file's image data: ");
			e.printStackTrace();
		}
		final byte[] data = baos.toByteArray();
		baos.dispose();
		try {
			baos.close();
		} catch(IOException e) {
		}
		return data;
	}
	
	public static final synchronized String getMediaInfoHTMLFor(File file, String homeDirectory) {//TODO Make this better! Maybe a button that you click that shows a little closable window with media infos in it?
		String rtrn = "<string>Unable to parse file (&quot;{0}&quot;)'s media tags.</string>";
		boolean success = false;
		String error = null;
		if(file != null && file.isFile()) {
			MediaInfo info = null;
			String filePath = FilenameUtils.normalize(file.getAbsolutePath());
			homeDirectory = homeDirectory == null ? null : FilenameUtils.normalize(homeDirectory);
			final String requestedFilePath = homeDirectory == null ? "" : StringUtils.replacePattern(filePath, "(?i)" + Pattern.quote(homeDirectory), "/").replace("\\", "/").replace("//", "/");
			
			try {
				info = readFileNoDispose(file, homeDirectory != null);
			} catch(Throwable e) {
				e.printStackTrace();
			}
			if(info != null) {
				success = true;
				rtrn = "<pre>" + info.toString() + "</pre>";
				if(homeDirectory != null) {
					MediaArtwork artwork = info.getAlbumArtwork();
					if(artwork != null) {
						rtrn += "\r\n\t\t<img style=\"-webkit-user-select: none\" src=\"" + requestedFilePath + "?mediaInfo=1&artwork=1\"" + (artwork.width > 0 ? " width=\"" + artwork.width + "\"" : "") + (artwork.height > 0 ? " height=\"" + artwork.height + "\"" : "") + "><br>\r\n";
						artwork.close();
					}
				}
				info.close();
			}
			if(dOut.size() > 0) {
				error = new String(dOut.toByteArray(), StandardCharsets.UTF_8);
				if(error.contains(file.getAbsolutePath())) {
					error = error.replace(file.getAbsolutePath(), homeDirectory != null ? requestedFilePath : file.getName());
				}
			}
			dOut.dispose();
		}
		return rtrn + (!success && error != null ? ("\r\n<br><string>Reason:&nbsp;</string><pre>" + error + "</pre>") : (error != null ? "\r\n<br><br><string>Operation completed with warnings:&nbsp;</string><pre>" + error + "</pre>" : ""));
	}
	
	/** @author Brian_Entei */
	public static final class MediaArtwork implements Closeable {
		
		private volatile boolean	isDisposed	= false;
		
		/** The album artwork, in image/png format(converted from BufferedImage
		 * using
		 * ImageIO; thanks to <a
		 * href="http://stackoverflow.com/a/34127009/2398263">edparris</a>) */
		private volatile byte[]		albumArtwork;
		public final int			width;
		public final int			height;
		public final String			mimeType;
		
		public MediaArtwork(Artwork artwork) {
			
			int width = artwork.getWidth();
			int height = artwork.getHeight();
			this.mimeType = artwork.getMimeType();
			final DisposableByteArrayOutputStream baos = new DisposableByteArrayOutputStream();
			try {
				BufferedImage bi = (BufferedImage) artwork.getImage();
				width = width <= 0 ? bi.getWidth() : width;
				height = height <= 0 ? bi.getHeight() : height;
				ImageIO.write(bi, "png", baos);
			} catch(Throwable e) {
				System.err.print("Unable to read artwork data: ");
				e.printStackTrace();
			}
			final byte[] data = baos.toByteArray();
			baos.dispose();
			try {
				baos.close();
			} catch(Throwable ignored) {
			}
			this.width = width;
			this.height = height;
			this.albumArtwork = data;
		}
		
		public final byte[] getData() {
			final byte[] rtrn = new byte[this.albumArtwork.length];
			System.arraycopy(this.albumArtwork, 0, rtrn, 0, this.albumArtwork.length);
			return rtrn;
		}
		
		@Override
		public final void close() {
			if(this.isDisposed) {
				return;
			}
			this.albumArtwork = new byte[0];
			this.isDisposed = true;
		}
		
		public final boolean isClosed() {
			return this.isDisposed;
		}
		
	}
	
}
