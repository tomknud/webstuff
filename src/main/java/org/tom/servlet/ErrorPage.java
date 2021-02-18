package org.tom.servlet;

import java.io.*;
import java.nio.file.*;
import static org.tom.servlet.Constants.*;

public class ErrorPage extends ExecutableServlet {

	public ErrorPage(String docroot, Path path) {
		super(docroot, path);
	}

	@Override
	public void process(Context context) {
		try {
			byte[] bytecodes = getBytes((Path) context.getAttribute(PATH_KEY));
			OutputStream rawOut = (OutputStream) context.getAttribute("writer");
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter((OutputStream) context.getAttribute("writer"))));
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body>\n" + "<h3>Content-Length: " + bytecodes.length + "</h3>\n");
			sb.append("<h5>page: " + (Path) context.getAttribute(PATH_KEY) + "</h5>\n");
			sb.append(new String(bytecodes));
			int length = sb.length();
			writeTextHeader(out, length);
			rawOut.write(sb.toString().getBytes());
			rawOut.flush();
		} catch (IOException ioe) {
			System.out.println("Failed Path Load " + printThread(context.getAttribute(PATH_KEY).toString()));
		}
		System.out.println("Sent file contents for " + printThread(context.getAttribute(PATH_KEY).toString()));
		
	}

}
