package com.gmail.br45entei.media;

import com.gmail.br45entei.media.MediaReader.MediaArtwork;
import com.gmail.br45entei.util.StringUtils;

import java.awt.Desktop;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;

/** @author Brian_Entei
 * @see MediaReader */
public class MediaInfo implements Closeable {//XXX http://id3.org/id3v2.3.0

	public final String			bitRate;
	public final int			bitsPerSample;
	public final String			encodingType;
	public final String			format;
	public final long			numOfSamples;
	public final String			trackLength;
	public final String			trackLengthMillis;
	public final int			trackLengthSeconds;
	public final double			trackLengthDouble;
	public final String			sampleRate;
	
	//====
	
	public final String			title;
	public final int			trackNumber;
	public final int			trackTotal;
	public final int			diskNumber;
	public final String			beatsPerMinute;
	public final String			trackLengthMillisTag;
	
	public final String			composer;
	public final String			artist;
	public final String			leadArtist;
	public final String			originalArtist;
	public final String			album;
	public final String			albumArtist;
	public final MediaArtwork	albumArtwork;
	public final String			year;
	public final String			genre;
	public final String			dateReleased;
	public final String[]		contributingArtists;
	
	public final String			comments;
	public final String			lyrics;
	public final String			description;
	public final String			contentType;
	
	public final String			copyright;
	public final String			license;
	public final String			url;
	public final String			officialURL;
	public final String			language;
	public final String			publisher;
	public final String			softwareTool;
	public final String			encodedBy;
	
	private volatile boolean	isClosed	= false;
	
	protected MediaInfo(AudioFile afile, boolean getArtwork) {
		final AudioHeader header = afile.getAudioHeader();
		final Tag audioTag = afile.getTagOrCreateDefault();//.getTag();
		if(afile.getTag() == null) {
			afile.setTag(audioTag);
		}
		
		this.bitRate = header.getBitRate();
		this.bitsPerSample = header.getBitsPerSample();
		this.encodingType = header.getEncodingType();
		this.format = header.getFormat();
		Long samples = header.getNoOfSamples();
		this.numOfSamples = samples != null ? samples.longValue() : -1L;
		this.trackLengthDouble = header.getPreciseTrackLength();
		this.trackLength = StringUtils.getElapsedTime(Math.round(this.trackLengthDouble) * 1000L);
		this.trackLengthMillis = StringUtils.getElapsedTime(Math.round(this.trackLengthDouble * 1000L), true);
		this.sampleRate = header.getSampleRate();
		this.trackLengthSeconds = header.getTrackLength();
		
		//=========
		String title = "";
		int trackNumber = 0;
		int trackTotal = 0;
		int diskNumber = 0;
		String beatsPerMinute = "";
		String trackLengthMillisTag = "";
		
		String composer = "";
		String artist = "";
		String leadArtist = "";
		String originalArtist = "";
		String album = "";
		String albumArtist = "";
		String year = "";
		String genre = "";
		String dateReleased = "";
		ArrayList<String> conArtists = new ArrayList<>();
		
		String comments = "";
		String lyrics = "";
		String description = "";
		String contentType = "";
		
		String copyright = "";
		String license = "";
		String url = "";
		String officialURL = "";
		String language = "";
		String publisher = "";
		String softwareTool = "";
		String encodedBy = "";
		
		Iterator<TagField> tags = audioTag.getFields();
		//byte[] data = new byte[0];
		while(tags.hasNext()) {
			TagField tag = tags.next();
			if(tag != null) {
				if(!tag.isBinary()) {
					final String id = tag.getId();
					String tagText = tag.toString().trim();
					if(tagText.startsWith("Text=\"") && tagText.endsWith("\";")) {
						tagText = tagText.substring(6, tagText.length() - 2).trim();
					}
					if(!tagText.isEmpty()) {
						if(id.equals("TT2") || id.equals("TIT2") || id.equalsIgnoreCase("?nam")) {
							title = tagText;
						} else if(id.equals("TRCK") || id.equalsIgnoreCase("trkn")) {
							if(StringUtils.isStrLong(tagText)) {
								trackNumber = Long.valueOf(tagText).intValue();
							}
						} else if(id.equalsIgnoreCase("disk")) {
							if(StringUtils.isStrLong(tagText)) {
								diskNumber = Long.valueOf(tagText).intValue();
							}
						} else if(id.equals("TBPM")) {
							beatsPerMinute = tagText;
						} else if(id.equals("TLEN")) {
							trackLengthMillisTag = tagText;
						} else if(id.equals("TXXX")) {
							if(StringUtils.isStrLong(tagText)) {
								trackTotal = Long.valueOf(tagText).intValue();
							}
						} else if(id.equals("TCOM") || id.equalsIgnoreCase("?wrt")) {
							composer = tagText;
						} else if(id.equals("TPE1") || id.equalsIgnoreCase("?ART")) {
							artist = tagText;
						} else if(id.equals("TP1")) {
							leadArtist = tagText;
						} else if(id.equals("TOPE")) {
							originalArtist = tagText;
						} else if(id.equals("TAL") || id.equals("TALB") || id.equalsIgnoreCase("?alb")) {
							album = tagText;
						} else if(id.equals("aART")) {
							albumArtist = tagText;
						} else if(id.startsWith("TPE")) {
							conArtists.add(tagText);
						} else if(id.equals("TDRC") || id.equals("TYER")) {//TDRC is used by VLC, but is not listed on http://id3.org/id3v2.3.0 for some reason
							year = tagText;
						} else if(id.equals("TCON") || id.equalsIgnoreCase("?gen") || id.equalsIgnoreCase("gnre")) {
							if(id.equalsIgnoreCase("gnre")) {
								genre += "[" + tagText + "]\r\n";
							} else {
								genre += tagText + "\r\n";
							}
						} else if(id.equalsIgnoreCase("?day")) {
							dateReleased = tagText;
						} else if(id.equalsIgnoreCase("?cmt") || id.equals("COMM") || id.equalsIgnoreCase("com")) {
							comments += tagText + "\r\n";
						} else if(id.equals("USLT")) {
							lyrics = tagText;
						} else if(id.equalsIgnoreCase("desc")) {
							description = tagText;
						} else if(id.equals("TCO")) {
							contentType = tagText;
						} else if(id.equals("TCOP") || id.equalsIgnoreCase("cprt")) {
							copyright = tagText;
						} else if(id.equals("WCOP")) {
							license = tagText;
						} else if(id.equals("WXXX")) {
							url = tagText;
						} else if(id.equals("WOAF")) {
							officialURL = tagText;
						} else if(id.equals("TLAN")) {
							language = tagText;
						} else if(id.equals("TPUB")) {
							publisher = tagText;
						} else if(id.equalsIgnoreCase("?too")) {
							softwareTool = tagText;
						} else if(id.equals("TEN") || id.equals("TENC") || id.equalsIgnoreCase("?enc")) {
							encodedBy = tagText;
						} else {
							System.out.println("Unimplemented Media Tag: \"" + id + "\"!");
						}
						//System.out.println((tag.isCommon() ? "" : "Uncommon") + "Tag[" + tag.getId() + "]: " + tagText);
					} else {
						//System.out.println("[Ignoring blank tag]: " + (tag.isCommon() ? "" : "Uncommon") + "Tag[" + tag.getId() + "]");
					}
				} else {
					/*//System.out.println("BinaryTag[" + tag.getId() + "]: " + tag.toString());
					if(tag.getId().equals("APIC") && data.length == 0) {
						try {
							data = tag.getRawContent();
						} catch(UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}*/
				}
			} else {
				System.out.println("Ignoring null tag...");
			}
		}
		this.title = title;
		this.trackNumber = trackNumber;
		this.trackTotal = trackTotal;
		this.diskNumber = diskNumber;
		this.beatsPerMinute = beatsPerMinute;
		this.trackLengthMillisTag = trackLengthMillisTag;
		
		this.composer = composer;
		this.artist = artist;
		this.leadArtist = leadArtist;
		this.originalArtist = originalArtist;
		this.album = album;
		this.albumArtist = albumArtist;
		this.year = year;
		this.genre = genre.trim();
		this.dateReleased = dateReleased;
		this.contributingArtists = conArtists.toArray(new String[conArtists.size()]);
		
		this.comments = comments.trim();
		this.lyrics = lyrics;
		this.description = description;
		this.contentType = contentType;
		
		this.copyright = !copyright.isEmpty() ? "Copyright ? " + copyright : "";
		this.license = license;
		this.url = url;
		this.officialURL = officialURL;
		this.language = language;
		this.publisher = publisher;
		this.softwareTool = softwareTool;
		this.encodedBy = encodedBy;
		MediaArtwork albumArtwork = null;
		if(getArtwork) {
			Artwork artwork = MediaReader.getLargestArtworkFromAudioFile(afile);
			albumArtwork = artwork != null ? new MediaArtwork(artwork) : null;
		}
		this.albumArtwork = albumArtwork;//data;
		/*if(this.albumArtwork.length > 0) {//XXX Debug/Test, remove or comment me!
			//System.out.println("\tSuccessfully retrieved album artwork.");
			final String folder = System.getProperty("user.dir") + File.separatorChar;
			final String name = "output";
			final String ext = ".bmp";//png"; //jpg"; //jfif";
			File output = new File(folder + name + ext);
			int nameCount = 0;
			while(output.exists()) {
				String n = name + "_" + nameCount++;
				output = new File(folder + n + ext);
			}
			try(FileOutputStream out = new FileOutputStream(output)) {
				out.write(data);
				out.flush();
				try {
					Desktop.getDesktop().open(output);//FIXME Resulting file is in unrecognized format...
				} catch(Throwable ignored) {
				}
			} catch(IOException e) {
				System.err.print("Unable to save image: ");
				e.printStackTrace();
			}
		}*/
	}
	
	@Override
	public final String toString() {
		final String lineSep = System.getProperty("line.separator");
		String rtrn = "BitRate: " + this.bitRate + lineSep;
		rtrn += "BitsPerSample: " + this.bitsPerSample + lineSep;
		rtrn += "EncodingType: " + this.encodingType + lineSep;
		rtrn += "Format: " + this.format + lineSep;
		rtrn += "#OfSamples: " + this.numOfSamples + lineSep;
		rtrn += "TrackLength: " + this.trackLength + lineSep;
		rtrn += "TrackLengthPrecise: " + this.trackLengthMillis + lineSep;
		rtrn += "TrackLengthSeconds: " + this.trackLengthSeconds + lineSep;
		rtrn += "TrackLengthDouble: " + new BigDecimal(this.trackLengthDouble).toPlainString() + lineSep;
		rtrn += "SampleRate: " + this.sampleRate + lineSep + lineSep;
		
		rtrn += "Title: " + this.title + lineSep;
		rtrn += "TrackNumber: " + this.trackNumber + lineSep;
		rtrn += "TrackTotal: " + this.trackTotal + lineSep;
		rtrn += "Disk Number: " + this.diskNumber + lineSep;
		rtrn += "BPM: " + this.beatsPerMinute + lineSep;
		rtrn += "Track Length Millis[Tag]: " + this.trackLengthMillisTag + lineSep + lineSep;
		
		rtrn += "Composer: " + this.composer + lineSep;
		rtrn += "Artist: " + this.artist + lineSep;
		rtrn += "Lead Artist: " + this.leadArtist + lineSep;
		rtrn += "Original Artist: " + this.originalArtist + lineSep;
		rtrn += "Album: " + this.album + lineSep;
		rtrn += "Album Artist: " + this.albumArtist + lineSep;
		rtrn += "Year: " + this.year + lineSep;
		rtrn += "Genre: " + this.genre + lineSep;
		rtrn += "Date Released: " + this.dateReleased + lineSep;
		rtrn += "Contributing Artists: " + StringUtils.stringArrayToString(this.contributingArtists, ' ') + lineSep + lineSep;
		
		rtrn += "Comments: " + this.comments + lineSep;
		rtrn += "Lyrics: " + this.lyrics + lineSep;
		rtrn += "Description: " + this.description + lineSep;
		rtrn += "Content Type: " + this.contentType + lineSep + lineSep;
		
		rtrn += "Copyright: " + this.copyright + lineSep;
		rtrn += "License: " + this.license + lineSep;
		rtrn += "URL: " + this.url + lineSep;
		rtrn += "Official URL: " + this.officialURL + lineSep;
		rtrn += "Language: " + this.language + lineSep;
		rtrn += "Publisher: " + this.publisher + lineSep;
		rtrn += "Software Tool Used: " + this.softwareTool + lineSep;
		rtrn += "Encoded by: " + this.encodedBy + lineSep;
		
		rtrn += "Artwork:" + lineSep;// + "<pre>" + new String(this.albumArtwork, StandardCharsets.US_ASCII) + "</pre>";
		return rtrn;
	}
	
	public final MediaArtwork getAlbumArtwork() {
		return this.albumArtwork;
	}
	
	@Override
	public final void close() {
		if(this.isClosed) {
			return;
		}
		if(this.albumArtwork != null) {
			this.albumArtwork.close();
		}
		this.isClosed = true;
	}
	
	public final boolean isClosed() {
		return this.isClosed;
	}
	
	public static final void main(String[] args) {
		File file = new File("C:\\example\\file.mp3");
		File output = new File("C:\\example\\output.png");
		try {
			MediaInfo info = MediaReader.readFile(file, true);
			FileOutputStream out = new FileOutputStream(output);
			out.write(info.albumArtwork.getData());
			out.flush();
			out.close();
			info.close();
			Desktop.getDesktop().open(output);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
}