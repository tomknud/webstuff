package org.tom.servlet;

import java.io.*;
import java.nio.file.*;
import static org.tom.servlet.Constants.*;

public abstract class ExecutableServlet implements Servlet {
	private Servlet next;
    protected String docroot;
    protected Path path;

	public ExecutableServlet(String docroot, Path path) {
		this.docroot = docroot;
		this.path = path;
	}

	public Servlet getNext() {
		return next;
	}

	public void setNext(Servlet next) {
		this.next = next;
	}

	@Override
	public void service(Context context) {
		if(((Path)context.getAttribute(PATH_KEY)).startsWith(path)) {// gets us into 'faces'
			context.setAttribute("processing", true);
			process(context);
			context.setAttribute("processing", null);
			context.setAttribute("processed", true);
		} else if(next != null) {
			next.service(context);
		} else {
			throw new RuntimeException("No Error Page");
		}
	}

	public abstract void process(Context context);
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

}
