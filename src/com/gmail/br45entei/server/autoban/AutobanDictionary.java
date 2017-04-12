package com.gmail.br45entei.server.autoban;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientOptions;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.swt.Functions;
import com.gmail.br45entei.util.PrintUtil;
import com.gmail.br45entei.util.StringUtil;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;

/** @author Brian_Entei */
public class AutobanDictionary {
	
	//unused atm:
	protected static final String[] jorgeePaths = new String[] {"/2phpmyadmin/", "/MyAdmin/", "/PMA/", "/PMA2011/", "/PMA2012/", "/admin/", "/admin/db/", "/admin/pMA/", "/admin/phpMyAdmin/", "/admin/phpmyadmin/", "/admin/sqladmin/", "/admin/sysadmin/", "/admin/web/", "/administrator/PMA/", "/administrator/admin/", "/administrator/db/", "/administrator/phpMyAdmin/", "/administrator/phpmyadmin/", "/administrator/pma/", "/administrator/web/", "/database/", "/db/", "/db/db-admin/", "/db/dbadmin/", "/db/dbweb/", "/db/myadmin/", "/db/phpMyAdmin-3/", "/db/phpMyAdmin/", "/db/phpMyAdmin3/", "/db/phpmyadmin/", "/db/phpmyadmin3/", "/db/webadmin/", "/db/webdb/", "/db/websql/", "/dbadmin/", "/myadmin/", "/mysql-admin/", "/mysql/", "/mysql/admin/", "/mysql/db/", "/mysql/dbadmin/", "/mysql/mysqlmanager/", "/mysql/pMA/", "/mysql/pma/", "/mysql/sqlmanager/", "/mysql/web/", "/mysqladmin/", "/mysqlmanager/", "/php-my-admin/", "/php-myadmin/", "/phpMyAdmin-3/", "/phpMyAdmin/", "/phpMyAdmin2/", "/phpMyAdmin3/", "/phpMyAdmin4/", "/phpMyadmin/", "/phpmanager/", "/phpmy-admin/", "/phpmy/", "/phpmyAdmin/", "/phpmyadmin/", "/phpmyadmin2/", "/phpmyadmin3/", "/phpmyadmin4/", "/phppma/", "/pma/", "/pma2011/", "/pma2012/", "/program/", "/shopdb/", "/sql/myadmin/", "/sql/php-myadmin/", "/sql/phpMyAdmin/", "/sql/phpMyAdmin2/", "/sql/phpmanager/", "/sql/phpmy-admin/", "/sql/phpmyadmin2/", "/sql/sql-admin/", "/sql/sql/", "/sql/sqladmin/", "/sql/sqlweb/", "/sql/webadmin/", "/sql/webdb/", "/sql/websql/", "/sqlmanager/"};
	
	public static final HTTPClientOptions autoBanSettings = new HTTPClientOptions();
	public static final HTTPClientOptions defaultSettings = new HTTPClientOptions();
	
	static {
		firstTimeInitialization(false);
		defaultSettings.setFrom(autoBanSettings);
		autoBanSettings.setFrom(null);
	}
	
	private static final String saveFileName = "autoban_requests.txt";
	
	private static final String ultimateHTAccessBlacklist2 = "ADSARobot|ah-ha|almaden|aktuelles|Anarchie|amzn_assoc|ASPSeek|ASSORT|ATHENS|Atomz|attach|attache|autoemailspider|BackWeb|Bandit|BatchFTP|bdfetch|big.brother|BlackWidow|bmclient|Boston Project|BravoBrian SpiderEngine MarcoPolo|Bot mailto:craftbot@yahoo.com|Buddy|Bullseye|bumblebee|capture|CherryPicker|ChinaClaw|CICC|clipping|Collector|Copier|Crescent|Crescent Internet ToolPak|Custo|cyberalert|DA$|Deweb|diagem|Digger|Digimarc|DIIbot|DISCo|DISCo Pump|DISCoFinder|Download Demon|Download Wonder|Downloader|Drip|DSurf15a|DTS.Agent|EasyDL|eCatch|ecollector|efp@gmx.net|Email Extractor|EirGrabber|email|EmailCollector|EmailSiphon|EmailWolf|Express WebPictures|ExtractorPro|EyeNetIE|FavOrg|fastlwspider|Favorites Sweeper|Fetch|FEZhead|FileHound|FlashGet WebWasher|FlickBot|fluffy|FrontPage|GalaxyBot|Generic|Getleft|GetRight|GetSmart|GetWeb!|GetWebPage|gigabaz|Girafabot|Go!Zilla|Go!Zilla|Go-Ahead-Got-It|GornKer|gotit|Grabber|GrabNet|Grafula|Green Research|grub-client|Harvest|hhjhj@yahoo|hloader|HMView|HomePageSearch|http generic|HTTrack|httpdown|httrack|ia_archiver|IBM_Planetwide|Image Stripper|Image Sucker|imagefetch|IncyWincy|Indy*Library|Indy Library|informant|Ingelin|InterGET|Internet Ninja|InternetLinkagent|Internet Ninja|InternetSeer.com|Iria|Irvine|JBH*agent|JetCar|JOC|JOC Web Spider|JustView|KWebGet|Lachesis|larbin|LeechFTP|LexiBot|lftp|libwww|likse|Link|Link*Sleuth|LINKS ARoMATIZED|LinkWalker|LWP|lwp-trivial|Mag-Net|Magnet|Mac Finder|Mag-Net|Mass Downloader|MCspider|Memo|Microsoft.URL|MIDown tool|Mirror|Missigua Locator|Mister PiX|MMMtoCrawl/UrlDispatcherLLL|^Mozilla$|Mozilla.*Indy|Mozilla.*NEWT|Mozilla*MSIECrawler|MS FrontPage*|MSFrontPage|MSIECrawler|MSProxy|multithreaddb|nationaldirectory|Navroad|NearSite|NetAnts|NetCarta|NetMechanic|netprospector|NetResearchServer|NetSpider|Net Vampire|NetZIP|NetZip Downloader|NetZippy|NEWT|NICErsPRO|Ninja|NPBot|Octopus|Offline Explorer|Offline Navigator|OpaL|Openfind|OpenTextSiteCrawler|OrangeBot|PageGrabber|Papa Foto|PackRat|pavuk|pcBrowser|PersonaPilot|Ping|PingALink|Pockey|Proxy|psbot|PSurf|puf|Pump|PushSite|QRVA|RealDownload|Reaper|Recorder|ReGet|replacer|RepoMonkey|Robozilla|Rover|RPT-HTTPClient|Rsync|Scooter|SearchExpress|searchhippo|searchterms.it|Second Street Research|Seeker|Shai|Siphon|sitecheck|sitecheck.internetseer.com|SiteSnagger|SlySearch|SmartDownload|snagger|Snake|SpaceBison|Spegla|SpiderBot|sproose|SqWorm|Stripper|Sucker|SuperBot|SuperHTTP|Surfbot|SurfWalker|Szukacz|tAkeOut|tarspider|Teleport Pro|Templeton|TrueRobot|TV33_Mercator|UIowaCrawler|UtilMind|URLSpiderPro|URL_Spider_Pro|Vacuum|vagabondo|vayala|visibilitygap|VoidEYE|vspider|Web Downloader|w3mir|Web Data Extractor|Web Image Collector|Web Sucker|Wweb|WebAuto|WebBandit|web.by.mail|Webclipping|webcollage|webcollector|WebCopier|webcraft@bea|webdevil|webdownloader|Webdup|WebEMailExtrac|WebFetch|WebGo IS|WebHook|Webinator|WebLeacher|WEBMASTERS|WebMiner|WebMirror|webmole|WebReaper|WebSauger|Website|Website eXtractor|Website Quester|WebSnake|Webster|WebStripper|websucker|webvac|webwalk|webweasel|WebWhacker|WebZIP|Wget|Whacker|whizbang|WhosTalking|Widow|WISEbot|WWWOFFLE|x-Tractor|^Xaldon WebSpider|WUMPUS|Xenu|XGET|Zeus.*Webster|Zeus";
	
	private static volatile boolean firstTimeInitialized = false;
	private static volatile boolean isSaving = false;
	
	private static final String filterKeyword(String keyword, boolean lowerCase) {
		return (lowerCase ? keyword.toLowerCase() : keyword).replace("\r\n", " ".replace("\n", " ").replace("\r", " "));
	}
	
	private static final void saveCheck() {
		if(isSaving) {
			while(isSaving) {
				Functions.sleep(10L);
			}
		}
	}
	
	public static final boolean banRequestPath(String path) {
		boolean lowerCase = false;
		if(path != null && path.startsWith("(?i)")) {
			lowerCase = true;
		}
		if(path == null || !(lowerCase ? path.substring("(?i)".length()) : path).startsWith("/")) {
			return false;
		}
		path = filterKeyword(path, lowerCase);
		saveCheck();
		return autoBanSettings.bannedRequestPaths.contains(path) ? false : autoBanSettings.bannedRequestPaths.add(path);
	}
	
	public static final boolean banUserAgent(String userAgent) {
		if(userAgent == null || userAgent.isEmpty()) {
			return false;
		}
		userAgent = filterKeyword(userAgent, false);
		if(userAgent.trim().equals("^")) {
			return false;
		}
		saveCheck();
		return autoBanSettings.bannedUserAgents.contains(userAgent) ? false : autoBanSettings.bannedUserAgents.add(userAgent);
	}
	
	public static final boolean banUserAgentWord(String userAgentWord) {
		if(userAgentWord == null || userAgentWord.isEmpty()) {
			return false;
		}
		userAgentWord = filterKeyword(userAgentWord, true);
		saveCheck();
		return autoBanSettings.bannedUserAgentWords.contains(userAgentWord) ? false : autoBanSettings.bannedUserAgentWords.add(userAgentWord);
	}
	
	public static final boolean unbanRequestPath(String path) {
		if(path == null || !path.startsWith("/")) {
			return false;
		}
		path = filterKeyword(path, false);
		saveCheck();
		return autoBanSettings.bannedRequestPaths.remove(path);
	}
	
	public static final boolean unbanUserAgent(String userAgent) {
		if(userAgent == null || userAgent.isEmpty()) {
			return false;
		}
		userAgent = filterKeyword(userAgent, false);
		if(userAgent.trim().equals("^")) {
			return false;
		}
		saveCheck();
		return autoBanSettings.bannedUserAgents.remove(userAgent);
	}
	
	public static final boolean unbanUserAgentWord(String userAgentWord) {
		if(userAgentWord == null || userAgentWord.isEmpty()) {
			return false;
		}
		userAgentWord = filterKeyword(userAgentWord, true);
		saveCheck();
		return autoBanSettings.bannedUserAgentWords.remove(userAgentWord);
	}
	
	public static final void firstTimeInitialization(boolean saveToFile) {
		banRequestPath("/../");
		banUserAgentWord("Jorgee");
		String[] jorgeePaths = new String[] {"/PMA/", "/PMA2011/", "/PMA2012/", "/administrator/PMA/", "/administrator/admin/", "/administrator/db/", "/administrator/phpMyAdmin/", "/administrator/phpmyadmin/", "/administrator/pma/", "/administrator/web/", "/pma/", "/pma2011/", "/pma2012/", "/shopdb/"};
		for(String jorgeePath : jorgeePaths) {
			banRequestPath("(?i)" + jorgeePath);
		}
		banUserAgent("^Java");
		banUserAgent("^Jakarta");
		banUserAgentWord("User-Agent");
		banUserAgentWord("compatible ;");
		banUserAgent("Mozilla");//If the user agent is ONLY this word, it is banned. If it starts with it, then it's fine.
		banUserAgent("panscient.com");
		banUserAgent("IBM EVV");
		banUserAgent("Bork-edition");
		banUserAgent("Fetch API Request");
		banUserAgent("WEP Search");
		banUserAgent("Wells Search II");
		banUserAgent("Missigua Locator");
		banUserAgent("ISC Systems iRc Search 2.1");
		banUserAgent("Microsoft URL Control");
		banUserAgent("Indy Library");
		for(String userAgent : ultimateHTAccessBlacklist2.split(Pattern.quote("|"))) {
			banUserAgent(userAgent);
		}
		if(saveToFile) {
			saveToFile();
			firstTimeInitialized = true;
		}
	}
	
	//================================================================================================================================================================================================
	
	private static final File getSaveFile() throws IOException {
		File file = new File(JavaWebServer.rootDir, saveFileName);
		if(!file.exists()) {
			file.createNewFile();
			firstTimeInitialization(true);
		}
		return file;
	}
	
	private static volatile IOException saveException = null;
	
	public static final IOException getSaveException() {
		IOException exception = saveException;
		saveException = null;
		return exception;
	}
	
	public static final boolean saveToFile() {
		if(isSaving) {
			return false;
		}
		isSaving = true;
		try(DualPrintWriter pr = new DualPrintWriter(new UnlockedOutputStreamWriter(new FileOutputStream(getSaveFile()), StandardCharsets.UTF_8), true)) {
			pr.setLineSeparator("\r\n");
			pr.println("clientMustSupplyAUserAgent: " + Boolean.toString(autoBanSettings.clientMustSupplyAUserAgent));
			pr.println("bannedRequestPaths: " + autoBanSettings.bannedRequestPaths.size());
			for(String bannedRequestPath : autoBanSettings.bannedRequestPaths) {
				pr.println(bannedRequestPath);
			}
			pr.println("bannedUserAgents: " + autoBanSettings.bannedUserAgents.size());
			for(String bannedUserAgent : autoBanSettings.bannedUserAgents) {
				pr.println(bannedUserAgent);
			}
			pr.println("bannedUserAgentWords: " + autoBanSettings.bannedUserAgentWords.size());
			for(String bannedUserAgentWord : autoBanSettings.bannedUserAgentWords) {
				pr.println(bannedUserAgentWord);
			}
			pr.flush();
			//isSaving = false;
			return true;
		} catch(IOException e) {
			saveException = e;//PrintUtil.printThrowable(e);
			//isSaving = false;
			return false;
		} finally {
			isSaving = false;
		}
	}
	
	public static final boolean loadFromFile() {
		saveCheck();
		try(FileInputStream in = new FileInputStream(getSaveFile())) {
			if(firstTimeInitialized) {
				firstTimeInitialized = false;
				return true;
			}
			autoBanSettings.clientMustSupplyAUserAgent = false;
			autoBanSettings.bannedRequestPaths.clear();
			autoBanSettings.bannedUserAgents.clear();
			autoBanSettings.bannedUserAgentWords.clear();
			String line;
			while((line = StringUtil.readLine(in)) != null) {
				if(line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				if(line.startsWith("clientMustSupplyAUserAgent: ")) {
					autoBanSettings.clientMustSupplyAUserAgent = Boolean.parseBoolean(line.substring("clientMustSupplyAUserAgent: ".length()));
				} else if(line.startsWith("bannedRequestPaths: ")) {
					final String numBannedRequestPaths = line.substring("bannedRequestPaths: ".length());
					if(!StringUtil.isStrInt(numBannedRequestPaths)) {
						PrintUtil.printErrln("Invalid number of entries for \"bannedRequestPaths\": '" + numBannedRequestPaths + "' is not a valid positive integer value!");
						PrintUtil.printErrln("Cannot finish reading \"" + saveFileName + "\"...");
						return false;
					}
					for(int i = 0; i < Integer.parseInt(numBannedRequestPaths); i++) {
						line = StringUtil.readLine(in);
						if(line == null) {
							break;
						}
						if(line.isEmpty() || line.startsWith("#")) {
							continue;
						}
						banRequestPath(line);
					}
				} else if(line.startsWith("bannedUserAgents: ")) {
					final String numBannedUserAgents = line.substring("bannedUserAgents: ".length());
					if(!StringUtil.isStrInt(numBannedUserAgents)) {
						PrintUtil.printErrln("Invalid number of entries for \"bannedUserAgents\": '" + numBannedUserAgents + "' is not a valid positive integer value!");
						PrintUtil.printErrln("Cannot finish reading \"" + saveFileName + "\"...");
						return false;
					}
					for(int i = 0; i < Integer.parseInt(numBannedUserAgents); i++) {
						line = StringUtil.readLine(in);
						if(line == null) {
							break;
						}
						if(line.isEmpty() || line.startsWith("#")) {
							continue;
						}
						banUserAgent(line);
					}
				} else if(line.startsWith("bannedUserAgentWords: ")) {
					final String numBannedUserAgentWords = line.substring("bannedUserAgentWords: ".length());
					if(!StringUtil.isStrInt(numBannedUserAgentWords)) {
						PrintUtil.printErrln("Invalid number of entries for \"bannedUserAgentWords\": '" + numBannedUserAgentWords + "' is not a valid positive integer value!");
						PrintUtil.printErrln("Cannot finish reading \"" + saveFileName + "\"...");
						return false;
					}
					for(int i = 0; i < Integer.parseInt(numBannedUserAgentWords); i++) {
						line = StringUtil.readLine(in);
						if(line == null) {
							break;
						}
						if(line.isEmpty() || line.startsWith("#")) {
							continue;
						}
						banUserAgentWord(line);
					}
				} else {
					PrintUtil.printErrln(" /!\\ \tIgnoring invalid line:\" " + (line.length() > 32 ? line.substring(0, 32) + "..." : line) + "\"\r\n/___\\\t" + (line.length() > 33 ? line.substring(32, Math.min(64, line.length())) + (line.length() > 64 ? "..." : "") : ""));
				}
				if(line == null) {
					break;
				}
			}
			return true;
		} catch(IOException e) {
			PrintUtil.printThrowable(e);
			return false;
		}
	}
	
	//================================================================================================================================================================================================
	
	private static final boolean isRequestPathBanned(String requestedFilePath, final DomainDirectory domainDirectory, final ClientConnection reuse) {
		if(domainDirectory != null) {
			File requestedFile = domainDirectory.getFileFromRequest(requestedFilePath, new HashMap<String, String>(), reuse);
			if(requestedFile != null && requestedFile.exists()) {//if(AddressUtil.isDomainLocalHost(domainDirectory.domain.getValue()) || DomainDirectory.getDefault(reuse) == domainDirectory) {
				return false;
			}
		}
		if(!requestedFilePath.trim().equals("/")) {//Don't want to accidentally autoban clients for just "GET / HTTP/1.1" now do we?!
			if(!requestedFilePath.endsWith("/")) {
				requestedFilePath += "/";
			}
			for(String bannedRequestPath : autoBanSettings.bannedRequestPaths) {
				if(bannedRequestPath.startsWith("(?i)")) {
					bannedRequestPath = bannedRequestPath.substring("(?i)".length());
					if(!bannedRequestPath.endsWith("/")) {
						bannedRequestPath += "/";
					}
					if(bannedRequestPath.equalsIgnoreCase(requestedFilePath) || bannedRequestPath.toLowerCase().startsWith(requestedFilePath.toLowerCase())) {
						return true;
					}
				} else {
					if(bannedRequestPath.equals(requestedFilePath) || bannedRequestPath.startsWith(requestedFilePath)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static final BanType isRequestProhibied(HTTPClientRequest request, final ClientConnection reuse) {
		if(request != null) {
			if(request.domainDirectory != null) {
				File requestedFile = request.domainDirectory.getFileFromRequest(request.requestedFilePath, new HashMap<String, String>(), reuse);
				if(requestedFile != null && requestedFile.exists()) {//if(AddressUtil.isDomainLocalHost(request.domainDirectory.domain.getValue()) || DomainDirectory.getDefault(reuse) == request.domainDirectory) {
					return BanType.ALLOWED;
				}
			}
			if(request.requestedFilePath != null && !request.requestedFilePath.isEmpty()) {
				if(!request.requestedFilePath.trim().startsWith("/")) {//If the request path doesn't even start with a / then it is a bad HTTP request anyways...
					return BanType.BAD_REQUEST_PATH;
				}
				if(request.requestedFilePath.trim().startsWith("//")) {
					String requestedFilePath = request.requestedFilePath.trim();
					while(requestedFilePath.startsWith("//")) {
						requestedFilePath = requestedFilePath.substring(1);
					}
					if(isRequestPathBanned(requestedFilePath, request.domainDirectory, reuse)) {
						return BanType.BANNED_REQUEST_PATH;
					}
				}
				if(isRequestPathBanned(request.requestedFilePath, request.domainDirectory, reuse)) {
					return BanType.BANNED_REQUEST_PATH;
				}
			}
			if(request.userAgent != null) {
				if(request.userAgent.trim().isEmpty() && autoBanSettings.clientMustSupplyAUserAgent) {
					return BanType.BLANK_USER_AGENT;
				}
				for(String bannedUserAgentWord : autoBanSettings.bannedUserAgentWords) {
					if(request.userAgent.toLowerCase().contains(bannedUserAgentWord.toLowerCase())) {
						return BanType.BANNED_USER_AGENT;
					}
				}
				for(String bannedUserAgent : autoBanSettings.bannedUserAgents) {
					if(bannedUserAgent.startsWith("^")) {
						bannedUserAgent = bannedUserAgent.substring(1);
						if(request.userAgent.startsWith(bannedUserAgent)) {
							return BanType.BANNED_USER_AGENT;
						}
						continue;
					}
					if(bannedUserAgent.equals(request.userAgent)) {
						return BanType.BANNED_USER_AGENT;
					}
				}
			} else if(autoBanSettings.clientMustSupplyAUserAgent) {
				return BanType.BLANK_USER_AGENT;
			}
		}
		return BanType.ALLOWED;//Nothing wrong here, serve away!
	}
	
	public static enum BanType {
		ALLOWED,
		BANNED_USER_AGENT,
		BANNED_REQUEST_PATH,
		BLANK_USER_AGENT,
		BAD_REQUEST_PATH;
		
		public final boolean isBanned() {
			return this == BanType.BANNED_REQUEST_PATH || this == BanType.BANNED_USER_AGENT;
		}
		
	}
	
}
