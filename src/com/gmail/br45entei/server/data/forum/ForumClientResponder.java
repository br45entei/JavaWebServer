package com.gmail.br45entei.server.data.forum;

import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_200;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_404;
import static com.gmail.br45entei.server.HTTPStatusCodes.HTTP_501;

import com.gmail.br45entei.JavaWebServer;
import com.gmail.br45entei.server.ClientConnection;
import com.gmail.br45entei.server.HTTPClientRequest;
import com.gmail.br45entei.server.HTTPServerResponse;
import com.gmail.br45entei.server.HTTPStatusCodes;
import com.gmail.br45entei.server.data.DomainDirectory;
import com.gmail.br45entei.util.AddressUtil;
import com.gmail.br45entei.util.StringUtils;
import com.gmail.br45entei.util.writer.DualPrintWriter;
import com.gmail.br45entei.util.writer.UnlockedOutputStreamWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

/** @author Brian_Entei */
public final class ForumClientResponder {
	
	/** @param s The socket
	 * @param domainDirectory The domain used to connect to this server
	 * @param request The Client's request
	 * @param response The response that the server will send
	 * @return Whether or not this method did anything
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final boolean HandleRequest(final Socket s, final ClientConnection reuse, final DomainDirectory domainDirectory, final HTTPClientRequest request, final HTTPServerResponse response) throws IOException {
		if(s == null || s.isClosed() || domainDirectory == null || request == null || response == null) {
			return false;
		}
		final String clientAddress = AddressUtil.getClientAddress((s.getRemoteSocketAddress() != null) ? StringUtils.replaceOnce(s.getRemoteSocketAddress().toString(), "/", "") : "");
		if(request.method.equalsIgnoreCase("GET") || request.method.equalsIgnoreCase("HEAD") || request.method.equalsIgnoreCase("OPTIONS")) {
			ForumData forum = ForumData.getForumDataFromRequestedFilePath(request.requestedFilePath);
			if(forum == null || !forum.isDomainAllowed(domainDirectory)) {
				return false;
			} else if(request.method.equalsIgnoreCase("OPTIONS")) {
				response.setStatusCode(HTTP_200);
				response.setHeader("Date", StringUtils.getCurrentCacheTime());
				response.setHeader("Allow", "GET,HEAD,POST,OPTIONS");
				response.setHeader("Content-Type", "text/plain; charset=UTF-8");
				response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
				response.setHeader("Content-Length", "0");
				response.setResponse((String) null);//XXX Maybe find out what content is sent with the OPTIONS protocol(Like 'httpd/unix-directory')?
				response.sendToClient(s, false);
				return true;
			}
			String requestedFilePath = FilenameUtils.normalize(StringUtils.replaceOnce(request.requestedFilePath, "/" + forum.getName(), ""));
			if(requestedFilePath == null || requestedFilePath.equals("/")) {
				requestedFilePath = "";
			}
			reuse.println("Request path for forum: \"" + requestedFilePath + "\";");
			
			response.setStatusCode(HTTP_200);//XXX TODO FIXME Make me!
			response.setHeader("Vary", "Accept-Encoding");
			response.setHeader("Content-Type", "text/html; charset=UTF-8");
			response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
			response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
			
			ForumBoard board = ForumData.getForumBoardFromRequestedFilePath(request.requestedFilePath);
			ForumTopic topic = ForumData.getForumTopicFromRequestedFilePath(request.requestedFilePath);
			if(board != null) {
				requestedFilePath = StringUtils.replaceOnce(StringUtils.replaceOnce(requestedFilePath, "/forum/", ""), board.getName(), "");
				if(requestedFilePath.equals("/")) {
					requestedFilePath = "";
				}
				reuse.println("Request path for board: \"" + requestedFilePath + "\";");
				if(topic != null) {
					requestedFilePath = StringUtils.replaceOnce(StringUtils.replaceOnce(requestedFilePath, "/topic/", ""), topic.getTitleInURL(), "");
					if(requestedFilePath.equals("/")) {
						requestedFilePath = "";
					}
					reuse.println("Request path for topic: \"" + requestedFilePath + "\";");
					response.setStatusCode(HTTP_501);
					response.setResponse("Topic pages are not yet implemented.\r\n");
					response.appendResponse("Topic title: " + topic.getTitle() + "\r\n");
					response.appendResponse("--\tMessage: " + topic.getMessage() + "\r\n");
					response.appendResponse("--\tDate created: \"" + StringUtils.getCacheTime(topic.getDateCreated()) + "\"(" + topic.getDateCreated() + ")\r\n");
					ArrayList<ForumComment> comments = topic.getComments();
					if(!comments.isEmpty()) {
						response.appendResponse("--\tComments:\r\n");
						for(ForumComment comment : comments) {
							response.appendResponse("#" + comment.getNumber() + ":\r\n");
							response.appendResponse("--\tDate created: \"" + StringUtils.getCacheTime(comment.getDateCreated()) + "\"(" + comment.getDateCreated() + ")\r\n");
							response.appendResponse("--\tDate posted: \"" + StringUtils.getCacheTime(comment.getDatePosted()) + "\"(" + comment.getDatePosted() + ")\r\n");
							response.appendResponse("--\tPost number: \"" + comment.getNumber() + "\r\n");
							response.appendResponse("--\tEdit count: \"" + comment.getEditCount() + "\r\n");
							response.appendResponse("--\tLast edit time: \"" + StringUtils.getCacheTime(comment.getLastEditTime()) + "\"(" + comment.getLastEditTime() + ")\r\n");
						}
					} else {
						response.appendResponse("--\tComments: (None)\r\n");
					}
					if(!requestedFilePath.isEmpty()) {
						response.appendResponse("\r\n\r\nThe requested path \"" + requestedFilePath + "\" does not exist.");
					}
				} else {
					response.setStatusCode(HTTP_501);
					response.setResponse("Board pages are not yet implemented.\r\n");
					response.appendResponse("Board name: " + board.getName() + "\r\n");
					response.appendResponse("--\tDescription: " + board.getDescription() + "\r\n");
					response.appendResponse("--\tDate created: \"" + StringUtils.getCacheTime(board.getDateCreated()) + "\"(" + board.getDateCreated() + ")\r\n");
					ArrayList<ForumTopic> topics = board.getTopics();
					if(!topics.isEmpty()) {
						response.appendResponse("--\tTopics:\r\n");
						int i = 1;
						for(ForumTopic curTopic : topics) {
							response.appendResponse("#" + i + ": " + curTopic.getTitle() + "\r\n");
							response.appendResponse("\t--\tAuthor UUID: " + curTopic.getAuthorAsUUID().toString() + "\r\n");
							response.appendResponse("\t--\tMessage: " + curTopic.getMessage() + "\r\n");
							response.appendResponse("\t--\tView Count: " + curTopic.getViewCount() + "\r\n");
							response.appendResponse("\t--\tLast Activity Time: \"" + StringUtils.getCacheTime(curTopic.getLastActivityTime()) + "\"(" + curTopic.getLastActivityTime() + ")\r\n");
							response.appendResponse("\t--\tIs Locked: " + curTopic.isLocked() + "\r\n");
							response.appendResponse("\t--\tIs Sticky: " + curTopic.isSticky() + "\r\n");
							response.appendResponse("\t--\tIs Private: " + curTopic.isPrivate() + "\r\n");
							i++;
						}
					} else {
						response.appendResponse("--\tTopics: (None)\r\n");
					}
					if(!requestedFilePath.isEmpty()) {
						response.appendResponse("\r\n\r\nThe requested path \"" + requestedFilePath + "\" does not exist.");
					}
				}
			} else {
				if(requestedFilePath.isEmpty()) {
					response.setResponse(forum.toHTML());
				} else {
					File requestedFile = new File(forum.getSaveFolder(), FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(request.requestedFilePath + File.separatorChar)));
					if(requestedFile.exists() && requestedFile.isFile()) {
						try {
							@SuppressWarnings("resource")
							OutputStream outStream = s.getOutputStream();
							@SuppressWarnings("resource")
							DualPrintWriter out = new DualPrintWriter(new UnlockedOutputStreamWriter(outStream, StandardCharsets.UTF_8), true);
							out.println("HTTP/1.1 200 OK");
							JavaWebServer.sendFileToClient(out, outStream, reuse, HTTPStatusCodes.HTTP_200, requestedFile, request.method, clientAddress, domainDirectory.getNetworkMTU(), false);
							outStream.flush();
						} catch(Throwable e) {
							reuse.printThrowable(e);
						}
						return true;
					}
					try {
						response.setStatusCode(HTTP_404);
						response.setHeader("Vary", "Accept-Encoding");
						response.setHeader("Content-Type", "text/html; charset=UTF-8");
						response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
						response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
						response.setResponse("<!DOCTYPE html>\r\n"//
								+ "<html>\r\n\t<head>\r\n"//
								+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
								+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
								+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
								+ "\t\t<title>404 - File not Found!!!11 - " + domainDirectory.getServerName() + "</title>\r\n"//
								+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
								+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtils.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
								+ "\t</head>\r\n"//
								+ "\t<body>\r\n"//
								+ "\t\t<h1>Error 404 - File not found</h1><hr>\r\n"//
								+ "\t\t<string>The file \"" + request.requestedFilePath + "\" does not exist.</string>\r\n"//
								+ "\t\t<hr><string><b>" + request.requestedFilePath + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b></string>\r\n"//
								+ "\t</body>\r\n</html>");
					} catch(Throwable e) {
						reuse.printThrowable(e);
						return true;
					}
				}
				/*response.setResponse("Forum pages are not yet implemented.\r\n");
				response.appendResponse("Forum name: " + forum.getName() + "\r\n");
				final ArrayList<ForumBoard> boards = forum.getBoards();
				if(!boards.isEmpty()) {
					response.appendResponse("Forum boards:\r\n");
					int i = 1;
					for(ForumBoard curBoard : boards) {
						response.appendResponse("#" + i + ": " + curBoard.getName() + "\r\n");
						response.appendResponse("--\tDescription: " + curBoard.getDescription() + "\r\n");
						response.appendResponse("--\tDate created: \"" + StringUtil.getCacheTime(curBoard.getDateCreated()) + "\"(" + curBoard.getDateCreated() + ")\r\n");
						ArrayList<ForumTopic> topics = curBoard.getTopics();
						if(!topics.isEmpty()) {
							response.appendResponse("--\tTopics:\r\n");
							int j = 1;
							for(ForumTopic curTopic : topics) {
								response.appendResponse("#" + j + ": " + curTopic.getTitle() + "\r\n");
								response.appendResponse("\t--\tAuthor UUID: " + curTopic.getAuthorAsUUID().toString() + "\r\n");
								response.appendResponse("\t--\tMessage: " + curTopic.getMessage() + "\r\n");
								response.appendResponse("\t--\tView Count: " + curTopic.getViewCount() + "\r\n");
								response.appendResponse("\t--\tLast Activity Time: \"" + StringUtil.getCacheTime(curTopic.getLastActivityTime()) + "\"(" + curTopic.getLastActivityTime() + ")\r\n");
								response.appendResponse("\t--\tIs Locked: " + curTopic.isLocked() + "\r\n");
								response.appendResponse("\t--\tIs Sticky: " + curTopic.isSticky() + "\r\n");
								response.appendResponse("\t--\tIs Private: " + curTopic.isPrivate() + "\r\n");
								j++;
							}
						} else {
							response.appendResponse("--\tTopics: (None)\r\n");
						}
						i++;
					}
				}
				if(!requestedFilePath.isEmpty()) {
					response.appendResponse("\r\n\r\nThe requested path \"" + requestedFilePath + "\" does not exist.");
				}*/
			}
			//} else if(request.protocol.equalsIgnoreCase("POST")) {
			//TODO make me!
		} else {
			return false;
			/*response.clearHeaders();
			response.setStatusCode(HTTP_501);
			response.setHeader("Vary", "Accept-Encoding");
			response.setHeader("Content-Type", "text/html; charset=UTF-8");
			response.setHeader("Server", JavaWebServer.SERVER_NAME_HEADER);
			response.setHeader("Cache-Control", "public, max-age=" + domainDirectory.getCacheMaxAge());
			response.setResponse("<!DOCTYPE html>\r\n"//
					+ "<html>\r\n\t<head>\r\n"//
					+ "\t\t" + JavaWebServer.HTML_HEADER_META_VIEWPORT + "\r\n"//
					+ "\t\t" + JavaWebServer.HTML_HEADER_META_CONTENT_TYPE + "\r\n"//
					+ "\t\t<link rel=\"shortcut icon\" href=\"" + domainDirectory.getDefaultPageIcon() + "\" type=\"image/x-icon\">\r\n"//
					+ "\t\t<title>501 - Feature not Implemented - " + domainDirectory.getServerName() + "</title>\r\n"//
					+ "\t\t<style>body{font-family:\'" + domainDirectory.getDefaultFontFace() + "\';}</style>\r\n"//
					+ (domainDirectory.doesDefaultStyleSheetExist() ? "\t\t<link rel=\"stylesheet\" href=\"" + domainDirectory.getDefaultStylesheet() + "\" type=\"text/css\" charset=\"" + StringUtil.getDetectedEncoding(domainDirectory.getDefaultStyleSheetFromFileSystem()) + "\">\r\n" : "")//
					+ "\t</head>\r\n"//
					+ "\t<body>\r\n"//
					+ "\t\t<h1>Error 501 - Not Implemented</h1><hr>\r\n"//
					+ "\t\t<string>The requested HTML method(\"" + request.protocol + "\") has not (yet) been implemented.</string>\r\n"//
					+ "\t\t<hr><string><b>" + request.requestedFilePath + " - " + request.host + (request.host.endsWith(":" + s.getLocalPort()) ? "" : ":" + s.getLocalPort()) + " --&gt; " + clientAddress + "</b></string>\r\n"//
					+ "\t</body>\r\n</html>");
			response.sendToClient(s, true);
			return true;*/
		}
		response.sendToClient(s, request.method.equalsIgnoreCase("GET"));
		return true;
	}
	
}
