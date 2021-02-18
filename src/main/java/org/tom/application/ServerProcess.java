package org.tom.application;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.tom.servlet.*;


import static org.tom.servlet.Constants.*;
import static org.tom.session.Manager.*;

public class ServerProcess implements Runnable {

	private static final Path FAVICON_PATH = Paths.get("favicon.ico");
	private static final String DEFAULT_INDEX_HTM = "index.htm";
	private static final Path SITE_PATH = Paths.get("faces");
	private static final Path ERROR_PATH = Paths.get("faces", "links", "error.htm");
	private static final Path FAVORITE_ICON = Paths.get("faces", "icons", "FavoriteIcon.ico");
	private String docroot;

	private ServerSocket server = null;
	private Servlet servlet;
	private Path favoriteIconPath = FAVORITE_ICON;

	protected ServerProcess(ServerSocket ss,String docroot) {
		server = ss;
		// TODO : move to 'configure'
		this.docroot = docroot;
		servlet = new FacesServlet(docroot,SITE_PATH);
		servlet.setNext(new ErrorPage(docroot,ERROR_PATH));
		rerun();
	}

    public byte[] getBytes(Path path) throws IOException
    {
		Path rootPath = Paths.get(docroot);
		Path fp = rootPath.resolve(path);
        if (Files.size(fp) == 0) {
            throw new IOException("File length is zero: " + path);
        } else {
            return Files.readAllBytes(fp);
        }
    }

	private int sSLHandshakeExceptions = 1;

	public void run() {
		Socket socket;
		Context context = new Context();
		try {
			socket = server.accept();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		rerun(); // create a new thread to accept the next connection ie. replacement
		String path = DEFAULT_INDEX_HTM;

		try {
			OutputStream rawOut = socket.getOutputStream();// To catch all of the SSL E's
			context.setAttribute(WRITER_KEY, rawOut);
			try {
				Path pathC = getPathAndHeaders(context,socket);
				ensureSessionKey(context);
				findPriorSession(context);context.getAttribute(NEW_PATH_KEY);
				if(pathC.getRoot() != null) { // Don't give up the file system
					returnError(path, rawOut);
				} else if (pathC.endsWith(FAVICON_PATH)) { // Chrome always wants a favicon.ico
					returnFavicon(path, rawOut);
				} else if(pathC.getParent() != null) {
					servlet.service(context);// Chain of responsiblity
				}
			} catch (SSLHandshakeException e) {
				System.err.println("SSLHandshakeExceptions " + sSLHandshakeExceptions++ + " Message: " + e.getMessage() + " while reading " + printThread(path));
				returnException(path, rawOut, e);
			} catch (SSLException e) {
				System.err.println("SSLException " + e.getMessage() + " while reading " + printThread(path));
				returnException(path, rawOut, e);
			} catch (Exception e) {
				System.err.println("Exception " + e.getMessage() + " while reading " + printThread(path));
				returnException(path, rawOut, e);
			}
		} catch (IOException ex) {
			// eat exception (could log error to log file, but
			// write out to stdout for now).
			System.err.println("error writing response: " + ex.getMessage());
            ex.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Error closing socket");
			}
		}
	}

	protected void returnError(String path, OutputStream rawOut) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawOut)));
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body>\n");
			sb.append("<h5>Site: " + path + "</h5>\n");
			sb.append("<h5>Was a bad address!</h5>\n");
			int length = sb.length();
			writeTextHeader(out, length);
			rawOut.write(sb.toString().getBytes());
			rawOut.flush();
			System.out.println("Sent file contents for " + printThread(path));
		} catch (IOException ioe) {
			System.out.println("Failed Path Load " + printThread(path));
		}
	}

	private void returnFavicon(String path, OutputStream rawOut) {
		try {
			byte[] bytecodes = getBytes(favoriteIconPath);
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawOut)));
			writeTextHeader(out, bytecodes.length);
			rawOut.write(bytecodes);
			rawOut.flush();
			System.out.println("Sent file contents for " + printThread(path));
		} catch (IOException ioe) {
			System.out.println("Failed Path Load " + printThread(path));
		}
	}

	private void returnException(String path, OutputStream rawOut, Exception e) {
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rawOut)));
		out.println("HTTP/1.0 400 " + e.getMessage());
		out.println("Error reading file for " + printThread(path));
		out.println("Content-Type: text/html");
		out.println("\r\n");
		out.flush();
	}

	public void writeTextHeader(PrintWriter out, int length) {
		out.println("HTTP/1.0 200 OK");
		out.println("Content-Length: " + length);
		out.println("Content-Type: text/html");
		out.println("\r\n");
		out.flush();
	}

	protected String printThread(String path) {
		return path + " in thread " + Thread.currentThread().getName();
	}

	/**
	 * Create a new thread to listen.
	 */
	private void rerun() {
		(new Thread(this)).start();
	}

}