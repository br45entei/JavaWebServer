package com.gmail.br45entei.media;

import com.gmail.br45entei.media.MediaReader.MediaArtwork;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.StringUtils;

import java.awt.Desktop;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.TyerTdatAggregatedFrame;
import org.jaudiotagger.tag.images.Artwork;

/** @author Brian_Entei
 * @see MediaReader */
@SuppressWarnings("javadoc")
public class MediaInfo implements Closeable {//XXX http://id3.org/id3v2.3.0

	public final String				bitRate;
	public final int				bitsPerSample;
	public final String				encodingType;
	public final String				format;
	public final long				numOfSamples;
	public final String				trackLength;
	public final String				trackLengthMillis;
	public final int				trackLengthSeconds;
	public final double				trackLengthDouble;
	public final String				sampleRate;
	
	//====
	
	public final String				title;					//TRCK Track, or "track title"
	public final int				trackNumber;			//TRKN
	public final int				trackTotal;			//TXXX Description="TRACKTOTAL"; Value="[some_integer]"
	public final int				diskNumber;
	public final String				beatsPerMinute;
	public final String				trackLengthMillisTag;
	
	public final String				composer;
	public final String				author;
	public final String				artist;
	public final String				leadArtist;
	public final String				originalArtist;
	public final String				album;
	public final String				albumArtist;
	protected final MediaArtwork	albumArtwork;
	public final String				year;					//TYER
	public final String				genre;					//TCON content type, or genre. Supposed to be a numerical string.
	public final String				dateReleased;
	public final String				recordingTime;
	public final String[]			contributingArtists;
	
	public final String				comments;				//COMM
	public final String				lyrics;
	public final String				description;
	public final String				popularimeter;
	public final String				contentType;
	
	public final String				copyright;
	public final String				license;
	public final String				vendor;
	public final String				url;
	public final String				officialURL;
	public final String				language;
	public final String				publisher;
	public final String				softwareTool;
	public final String				encodedBy;				//TENC
	public final String				encoderSettings;		//TSSE
	public final String				encodingTime;			//TDEN
	//TMED (media type)
	
	private volatile boolean		hasArtwork	= false;
	
	private volatile boolean		isClosed	= false;
	
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
		int trackNumber = -1;
		int trackTotal = -1;
		int diskNumber = -1;
		String beatsPerMinute = "";
		String trackLengthMillisTag = "";
		
		String composer = "";
		String author = "";
		String artist = "";
		String leadArtist = "";
		String originalArtist = "";
		String album = "";
		String albumArtist = "";
		String year = "";
		String genre = "";
		String dateReleased = "";
		String recordingTime = "";
		ArrayList<String> conArtists = new ArrayList<>();
		
		String comments = "";
		String lyrics = "";
		String description = "";
		String popularimeter = "";
		String contentType = "";
		
		String copyright = "";
		String license = "";
		String vendor = "";
		String url = "";
		String officialURL = "";
		String language = "";
		String publisher = "";
		String softwareTool = "";
		String encodedBy = "";
		String encoderSettings = "";
		String encodingTime = "";
		
		Iterator<TagField> tags = audioTag.getFields();
		//byte[] data = new byte[0];
		while(tags.hasNext()) {
			TagField tag = tags.next();
			if(tag != null) {
				if(!tag.isBinary()) {
					String id = tag.getId();
					id = id.startsWith("WM/") ? id.substring(3) : id;
					String originalText;
					try {
						originalText = new String(tag.getRawContent());
					} catch(Throwable ignored) {
						originalText = tag.toString();
					}
					String tagText = tag.toString().trim();
					if(tagText.startsWith("Text=\"") && tagText.endsWith("\";")) {
						tagText = tagText.substring(6, tagText.length() - 2).trim();
					}
					if(!tagText.isEmpty()) {
						if(id.equals("TT2") || id.equals("TIT2") || id.equalsIgnoreCase("©nam") || id.equalsIgnoreCase("TITLE")) {
							title = tagText;
						} else if(id.equals("TRCK") || id.equalsIgnoreCase("trkn") || id.equalsIgnoreCase("TRACKNUMBER") || id.equalsIgnoreCase("Track")) {
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
						} else if(id.equals("TRACKTOTAL")) {
							if(StringUtils.isStrLong(tagText)) {
								trackTotal = Long.valueOf(tagText).intValue();
							} else {
								System.out.println("[Ignoring invalid tag]: " + (tag.isCommon() ? "" : "Uncommon") + "Tag[" + tag.getId() + "]: \"" + tagText + "\";");
							}
						} else if(id.equals("TCOM") || id.equalsIgnoreCase("©wrt")) {
							composer = tagText;
						} else if(id.equals("AUTHOR")) {
							author = tagText;
						} else if(id.equals("TPE1") || id.equalsIgnoreCase("©ART") || id.equalsIgnoreCase("ARTIST")) {
							if(id.equals("TPE1")) {
								String[] split = originalText.split(Pattern.quote("ÿþ"));
								for(String s : split) {
									s = s.trim();
									if(s.startsWith("TPE1")) {
										continue;
									}
									artist += s + "; ";
								}
								artist = artist.trim();
							} else {
								artist += tagText + "; ";
							}
						} else if(id.equals("TP1")) {
							leadArtist = tagText;
						} else if(id.equals("TOPE")) {
							originalArtist = tagText;
						} else if(id.equals("TAL") || id.equals("TALB") || id.equalsIgnoreCase("©alb") || id.equalsIgnoreCase("ALBUM")) {
							album = tagText;
						} else if(id.equalsIgnoreCase("aART")) {
							albumArtist = tagText;
						} else if(id.startsWith("TPE")) {
							conArtists.add(tagText);
						} else if(id.equals("TYER") || id.equalsIgnoreCase("YEAR")) {
							year = tagText;
						} else if(id.equals("TCON") || id.equalsIgnoreCase("©gen") || id.equalsIgnoreCase("gnre") || id.equalsIgnoreCase("GENRE")) {
							if(id.equalsIgnoreCase("gnre")) {
								genre += "[" + tagText + "]\r\n";
							} else {
								genre += tagText + "\r\n";
							}
						} else if(id.equalsIgnoreCase("©day") || id.equals("DATE") || id.equals("TYERTDAT")) {
							if(id.equals("TYERTDAT")) {
								if(tag instanceof TyerTdatAggregatedFrame) {
									TyerTdatAggregatedFrame yearDateTag = (TyerTdatAggregatedFrame) tag;
									dateReleased = yearDateTag.getContent();
								}
							} else {
								dateReleased = tagText;
							}
						} else if(id.equals("TDRC")) {
							recordingTime = tagText;
						} else if(id.equalsIgnoreCase("©cmt") || id.equals("COMM") || id.equalsIgnoreCase("com")) {
							comments += tagText + "\r\n";
						} else if(id.equals("USLT")) {
							lyrics = tagText;
						} else if(id.equalsIgnoreCase("desc")) {
							description = tagText;
						} else if(id.equals("POPM")) {
							popularimeter = tagText;
						} else if(id.equals("TCO")) {
							contentType = tagText;
						} else if(id.equals("TCOP") || id.equalsIgnoreCase("cprt")) {
							copyright = tagText;
						} else if(id.equals("WCOP")) {
							license = tagText;
						} else if(id.equalsIgnoreCase("vendor")) {
							vendor = tagText;
						} else if(id.equals("WXXX")) {
							url = tagText;
						} else if(id.equals("WOAF")) {
							officialURL = tagText;
						} else if(id.equals("TLAN")) {
							language = tagText;
						} else if(id.equals("TPUB")) {
							publisher = tagText;
						} else if(id.equalsIgnoreCase("©too")) {
							softwareTool = tagText;
						} else if(id.equals("TEN") || id.equals("TENC") || id.equalsIgnoreCase("©enc") || id.equalsIgnoreCase("ENCODER")) {
							encodedBy = tagText;
						} else if(id.equals("TSSE")) {
							encoderSettings = tagText;
						} else if(id.equals("TDEN")) {
							encodingTime = tagText;
						} else if(id.equals("TXXX")) {//User Defined Tags
							if(tagText.startsWith("Description=\"")) {
								String[] split = tagText.split(Pattern.quote(";"));
								if(split.length > 1) {//Meaning there are more than one semi-colon. Consider: {Description="Text";} only has one, so splitting it will yield: {Description="Text"}.
									String desc = "";
									String text = "";
									for(String entry : split) {
										entry = entry.trim();//Do not remove.
										if(entry == null || entry.isEmpty()) {//A NPE happened once, believe it or not...
											continue;
										}
										String[] param = entry.split(Pattern.quote("="));
										String pname = param[0];
										String pvalue = StringUtils.stringArrayToString(param, '=', 1);//(Everything after the first equals symbol in 'param'.)
										if(pvalue.startsWith("\"") && pvalue.endsWith("\"") && pvalue.length() >= 2) {
											pvalue = pvalue.substring(1, pvalue.length() - 1);
										}
										if(pname.equalsIgnoreCase("Description")) {
											desc = pvalue;
										} else if(pname.equalsIgnoreCase("Text")) {
											text = pvalue;
										} else if(!desc.isEmpty() && !text.isEmpty()) {
											break;
										}
									}
									if(!desc.isEmpty()) {
										if(desc.equals("TRACKTOTAL")) {
											if(StringUtils.isStrLong(text)) {
												trackTotal = Long.valueOf(text).intValue();
											} else {
												System.out.println("[Ignoring invalid tag]: " + "User-Defined-Tag[" + tag.getId() + "]: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\"; Reason: \"" + text + "\" is not a valid integer value.");
											}
										} else if(desc.equalsIgnoreCase("LYRICS")) {
											lyrics = text;
										} else {
											System.out.println("Unimplemented User-Defined Tag: \"" + desc + "\"! Value: \"" + text.replace("\r", "").replace("\n", " ").trim() + "\";");
										}
									} else {
										System.out.println("Unimplemented/unparsable User-Defined Tag: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\"");
									}
								} else {
									System.out.println("Unrecognized/unparsable User-Defined Tag: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\"");
								}
								/*int index1 = tagText.indexOf("\"");
								int index2 = tagText.indexOf("\";");
								if(index1 != -1 && index2 != -1) {
									String subID = tagText.substring(index1 + 1, index2);
									if((index2 + 3) < tagText.length() - 2) {
										String subText = tagText.substring(index2 + 3, tagText.length() - 2);
										if(subText.startsWith("Text=\"")) {
											subText = subText.substring(6);
										}
										if(subID.equals("TRACKTOTAL")) {
											if(StringUtils.isStrLong(subText)) {
												trackTotal = Long.valueOf(subText).intValue();
											} else {
												System.out.println("[Ignoring invalid  tag]: " + "User-Defined-Tag[" + tag.getId() + "]: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\";");
											}
										} else if(subID.equalsIgnoreCase("LYRICS")) {
											lyrics = subText;
										} else {
											System.out.println("Unimplemented User-Defined Tag: \"" + subID + "\"! Value: \"" + subText.replace("\r", "").replace("\n", " ").trim() + "\";");
										}
									} else {
										System.out.println("Unrecognized/unparsable User-Defined Tag: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\"");
									}
								}*/
							} else {
								System.out.println("Unimplemented User-Defined Tag: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\"");
							}
						} else {
							if(!id.startsWith("----:com.apple.iTunes")) {
								System.out.println("Unimplemented Media Tag: \"" + id + "\"! Value: \"" + tagText.replace("\r", "").replace("\n", " ").trim() + "\";");
							}
						}
						//System.out.println((tag.isCommon() ? "" : "Uncommon") + "Tag[" + tag.getId() + "]: " + tagText);
					} else {
						System.out.println("[Ignoring blank tag]: " + (tag.isCommon() ? "" : "Uncommon") + "Tag[" + tag.getId() + "]: \"" + tagText + "\";");
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
		this.author = author;
		this.artist = artist;
		this.leadArtist = leadArtist;
		this.originalArtist = originalArtist;
		this.album = album;
		this.albumArtist = albumArtist;
		this.year = year;
		this.genre = genre.trim();
		this.dateReleased = dateReleased;
		this.recordingTime = recordingTime;
		this.contributingArtists = conArtists.toArray(new String[conArtists.size()]);
		
		this.comments = comments.trim();
		this.lyrics = lyrics;
		this.description = description;
		this.popularimeter = popularimeter;
		this.contentType = contentType;
		
		this.copyright = !copyright.isEmpty() ? "Copyright " + (copyright.trim().startsWith("℗") ? "" : "© ") + (copyright.trim().toUpperCase().startsWith("(C)") ? copyright.trim().substring(3) : copyright) : "";
		this.license = license;
		this.vendor = vendor;
		this.url = url;
		this.officialURL = officialURL;
		this.language = language;
		this.publisher = publisher;
		this.softwareTool = softwareTool;
		this.encodedBy = encodedBy;
		this.encoderSettings = encoderSettings;
		this.encodingTime = encodingTime;
		this.hasArtwork = MediaReader.doesFileHaveArtwork(afile);
		MediaArtwork albumArtwork = null;
		if(this.hasArtwork && getArtwork) {
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
	
	private static final String appendStrIfNotSuffixedTwice(String str, String suffix) {
		return str.endsWith(suffix + suffix) ? str : str + suffix;
	}
	
	/** @return True if the file has any artwork attached, regardless of whether
	 *         or not the reader was told to retrieve it. */
	public final boolean hasArtwork() {
		return this.hasArtwork;
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
		
		rtrn += this.title.isEmpty() ? "" : "Title: " + this.title + lineSep;
		rtrn += this.trackNumber == -1 ? "" : "TrackNumber: " + this.trackNumber + lineSep;
		rtrn += this.trackTotal == -1 ? "" : "TrackTotal: " + this.trackTotal + lineSep;
		rtrn += this.diskNumber == -1 ? "" : "Disk Number: " + this.diskNumber + lineSep;
		rtrn += this.beatsPerMinute.isEmpty() ? "" : "BPM: " + this.beatsPerMinute + lineSep;
		rtrn += this.trackLengthMillisTag.isEmpty() ? "" : "Track Length Millis[Tag]: " + this.trackLengthMillisTag + lineSep;
		rtrn = appendStrIfNotSuffixedTwice(rtrn, lineSep);
		
		rtrn += this.composer.isEmpty() ? "" : "Composer: " + this.composer + lineSep;
		rtrn += this.author.isEmpty() ? "" : "Author: " + this.author + lineSep;
		rtrn += this.artist.isEmpty() ? "" : "Artist: " + this.artist + lineSep;
		rtrn += this.leadArtist.isEmpty() ? "" : "Lead Artist: " + this.leadArtist + lineSep;
		rtrn += this.originalArtist.isEmpty() ? "" : "Original Artist: " + this.originalArtist + lineSep;
		rtrn += this.album.isEmpty() ? "" : "Album: " + this.album + lineSep;
		rtrn += this.albumArtist.isEmpty() ? "" : "Album Artist: " + this.albumArtist + lineSep;
		rtrn += this.year.isEmpty() ? "" : "Year: " + this.year + lineSep;
		rtrn += this.genre.isEmpty() ? "" : "Genre: " + this.genre + lineSep;
		rtrn += this.dateReleased.isEmpty() ? "" : "Date Released: " + this.dateReleased + lineSep;
		rtrn += this.recordingTime.isEmpty() ? "" : "Recording Time: " + this.recordingTime + lineSep;
		rtrn += this.contributingArtists.length == 0 ? "" : "Contributing Artists: " + StringUtils.stringArrayToString(this.contributingArtists, ' ') + lineSep;
		rtrn = appendStrIfNotSuffixedTwice(rtrn, lineSep);
		
		rtrn += this.comments.isEmpty() ? "" : "Comments: " + this.comments + lineSep;
		rtrn += this.lyrics.isEmpty() ? "" : "Lyrics: " + this.lyrics + lineSep;
		rtrn += this.description.isEmpty() ? "" : "Description: " + this.description + lineSep;
		rtrn += this.popularimeter.isEmpty() ? "" : "Popularimeter: " + this.popularimeter + lineSep;
		rtrn += this.contentType.isEmpty() ? "" : "Content Type: " + this.contentType + lineSep;
		rtrn = appendStrIfNotSuffixedTwice(rtrn, lineSep);
		
		rtrn += this.copyright.isEmpty() ? "" : "Copyright: " + this.copyright + lineSep;
		rtrn += this.license.isEmpty() ? "" : "License: " + this.license + lineSep;
		rtrn += this.vendor.isEmpty() ? "" : "Vendor: " + this.vendor + lineSep;
		rtrn += this.url.isEmpty() ? "" : "URL: " + this.url + lineSep;
		rtrn += this.officialURL.isEmpty() ? "" : "Official URL: " + this.officialURL + lineSep;
		rtrn += this.language.isEmpty() ? "" : "Language: " + this.language + lineSep;
		rtrn += this.publisher.isEmpty() ? "" : "Publisher: " + this.publisher + lineSep;
		rtrn += this.softwareTool.isEmpty() ? "" : "Software Tool Used: " + this.softwareTool + lineSep;
		rtrn += this.encodedBy.isEmpty() ? "" : "Encoded by: " + this.encodedBy + lineSep;
		rtrn += this.encoderSettings.isEmpty() ? "" : "Encoder Settings: " + this.encoderSettings + lineSep;
		rtrn += this.encodingTime.isEmpty() ? "" : "Encoding Time: " + this.encodingTime + lineSep;
		
		rtrn = appendStrIfNotSuffixedTwice(rtrn, lineSep) + "Artwork:" + (this.albumArtwork != null ? "" : " [None attached]") + lineSep;//XXX (DEBUG) // + "<pre>" + new String(this.albumArtwork, StandardCharsets.US_ASCII) + "</pre>";
		return rtrn;
	}
	
	/** @return The media source's artwork, if any, or <code>null</code>
	 *         otherwise. */
	public final MediaArtwork getAlbumArtwork() {
		return this.albumArtwork;
	}
	
	/** Closes any opened resources associated with this media(i.e. file handles,
	 * network streams) */
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
	
	/** @return Whether or not this resource has been closed */
	public final boolean isClosed() {
		return this.isClosed;
	}
	
	/** @param args System command arguments */
	public static final void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Usage: java.exe -jar jarFile.jar path/to/media File.ext");
			return;
		}
		File file = new File(StringUtil.stringArrayToString(args, ' '));
		try {
			MediaInfo info = MediaReader.readFile(file, true);
			if(info != null) {
				MediaArtwork artwork = info.getAlbumArtwork();
				if(artwork != null) {
					File output = new File("C:\\example\\output." + artwork.mimeType.trim().replace("image/", ""));
					FileOutputStream out = new FileOutputStream(output);
					out.write(artwork.getData());
					out.flush();
					out.close();
					artwork.close();
					Desktop.getDesktop().open(output);
				}
				info.close();
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
}
