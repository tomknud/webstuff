package org.tom.servlet;

import static org.tom.servlet.Constants.*;

import java.io.*;
import java.nio.file.*;

public class MyPage implements Page {
	
	public MyPage(Context session) {
	}

	@Override
	public void apply(Context context) {
		System.out.println("apply : Link to Java");
	}

	@Override
	public void execute(Context context) {
		System.out.println("execute : Link to Java");
	}

	@Override
	public void encode(Context context) {
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

    public byte[] getBytes(Path path) throws IOException
    {
		Path rootPath = Paths.get("/here");
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
