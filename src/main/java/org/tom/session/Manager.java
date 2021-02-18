package org.tom.session;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import org.tom.header.*;
import static org.tom.header.HeaderKey.*;
import org.tom.servlet.*;
import static org.tom.servlet.Constants.*;

public class Manager {

	public static int CHUNK_LENGTH = 1024;

	private static Map<String,Context> contexts = new HashMap<>();
	
	public static Context findSession(String key) {
		return contexts.get(key);
	}

	public static void addSession(String key,Context attribute) {
		contexts.put(key, attribute);
	}
	
	public static Path getPathAndHeaders(Context context, Socket socket) throws IOException {
		InputStream inputStream = socket.getInputStream();
		byte[] bytes = new byte[CHUNK_LENGTH*1024];
		int count = inputStream.read(bytes,0,CHUNK_LENGTH*1024);
		if(count == CHUNK_LENGTH*1024) {
			// TODO : Manage large data
			throw new IOException("Way too many bytes!");
		}
		bytes[count] = 0x0D;
		bytes[count+1] = 0X0A;
		byte[] bytesT = Arrays.copyOfRange(bytes, 0, count+2);
		context.setAttribute(BYTES, bytesT);
		BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytesT)));
		String line = in.readLine();
		context.setAttribute(COMMAND, line);
		String path = DEFAULT_INDEX_HTM;
		boolean verbose = true;
		// extract class from GET line
		if (line.startsWith(GETC+" "+SLASH)) {
			line = line.substring(5, line.length() - 1).trim();
			path = getPath(context, line, path);
			if(FAVE_ICON.equals(path)) {
				verbose = false;
			}
			System.out.println("GET with path : " + path);
			if(verbose) {
				System.out.println("Get line:"+line + " length:" + line.length());
			}
		} else if (line.startsWith(POSTC+" "+SLASH)) {
			line = line.substring(6, line.length() - 1).trim();
			path = getPath(context, line, path);
			System.out.println("POST with path : " + path);
		} else {
			throw new RuntimeException("Bad Request");
		}

		// eat the rest of header
		Map<HeaderKey,Header> headers = new HashMap<>();
		context.setAttribute(HEADERS, headers);
		context.setAttribute(OUTPUT_HEADERS, new HashMap<HeaderKey, Header>());
		Map<String,String> data = new HashMap<>();
		context.setAttribute(DATA, data);
		do {
			line = in.readLine();
			if(!line.isBlank()) {
				Header header = new Header(line);
				headers.put(header.getKey(), header);
				findSessionInCookie(context, header);
			}
		} while (line.length() != 0);
		// Find Key/Value pairs in data
		while(in.ready()) {
			line = in.readLine();
			if(!line.isEmpty() && !line.isBlank()) {
				String[] variables = line.split(AMPER);
				for(String variable:variables) {
					String keyValue[] = variable.split(EQUALS);
					if(keyValue.length > 1) {
						data.put(keyValue[0], keyValue[1]);
						System.out.println(keyValue[0] + COLON + keyValue[1]);
					}
				}
			}
		}
		if (path.length() != 0) {
			Path pathC = Paths.get(path);
			return pathC;
		} else {
			System.err.println("Error getting " + printThread(" path "));
		}
		return null;
	}

	public static HeaderKey getHeaderKey(String line) {
		if(line.indexOf(COLON)>0) {
			HeaderKey headerf = null;
			for(HeaderKey header : values()) {
				try {
					if(header.getKey().equals(line.substring(0, line.indexOf(COLON))) ) {
						if(line.length() > 120) {
							int equals = line.indexOf(EQUALS);
							if(equals > 20) {
								header.setValue(line.substring(line.indexOf(COLON), equals));
							} else {
								header.setValue(line.substring(line.indexOf(COLON), line.length()));
							}
						} else {
							header.setValue(line.substring(line.indexOf(COLON), line.length()));
						}
						headerf = header;
					}
				} catch(StringIndexOutOfBoundsException sioobe) {
					System.out.println("Line Can't be processed in Headers: " + line);
				}
	 		}
			if(headerf == null) {
				System.out.println("Header key not found for : " + line);
				return NULL;
			} else {
				return headerf;
			}
		} else {
			System.out.println("Not a header: " + line);
		}
		return NULL;
	}

	@SuppressWarnings("unchecked")
	public static void ensureSessionKey(Context context) {
		if(context.getAttribute(JSESSIONID) == null || ((String)context.getAttribute(JSESSIONID)).isEmpty()) {
			String jsessionid = UUID.randomUUID().toString();
			((Map<HeaderKey, Header>) context.getAttribute(OUTPUT_HEADERS)).put(SET_COOKIE, new Header(SET_COOKIE,JSESSIONID+EQUALS+jsessionid));
			((Map<HeaderKey, Header>) context.getAttribute(HEADERS)).put(COOKIE,new Header(COOKIE,JSESSIONID+EQUALS+jsessionid));
			context.setAttribute(JSESSIONID,jsessionid);
		}
	}

	@SuppressWarnings("unchecked")
	public static void invalidateSession(Context context) {
		Map<HeaderKey, Header> outputHeaders = (Map<HeaderKey, Header>) context.getAttribute(OUTPUT_HEADERS);
		outputHeaders.put(CACHE_CONTROL, new Header(CACHE_CONTROL,"no-cache,no-store,must-revalidate"));
		outputHeaders.put(PRAGMA, new Header(PRAGMA,"no-cache"));
		outputHeaders.put(EXPIRES, new Header(EXPIRES,"0"));
		((Map<HeaderKey, Header>) context.getAttribute(OUTPUT_HEADERS)).put(SET_COOKIE, new Header(SET_COOKIE,JSESSIONID+EQUALS+""));
	}
	
	public static void findPriorSession(Context session) {
		Context priorSession = findSession((String) session.getAttribute(JSESSIONID));
		if(priorSession == null) {
			addSession((String) session.getAttribute(JSESSIONID), session);
		} else {
			session.setAttribute(HISTORY, priorSession);
			addSession((String) session.getAttribute(JSESSIONID), session);
		}
	}

	private static String findSessionInCookie(Context context, Header header) {
		String jsessionid = null;
		if(header.getKey().equals(COOKIE)) {
			int i = header.getContent().indexOf(JSESSIONID);
			if(i > 0) {
				int j = header.getContent().indexOf(SEMI, i+11);
				if(j > 0) {
					jsessionid = header.getContent().substring(i+11, j);
					context.setAttribute(JSESSIONID, jsessionid);
				} else {
					jsessionid = header.getContent().substring(i+11, header.getContent().length());
					context.setAttribute(JSESSIONID, jsessionid);
				}
			}
		}
		return jsessionid;
	}

	private static String getPath(Context context, String line, String path) {
		int index = line.indexOf(QMARK);
		if (index != -1) {
			path = line.substring(0, index);
			context.setAttribute(PATH_KEY, Paths.get(path));
		} else {
			index = line.indexOf(' ');
			if (index != -1) {
				path = line.substring(0, index);
				context.setAttribute(PATH_KEY, Paths.get(path));
			}
		}
		context.setAttribute(NEW_PATH_KEY, context.getAttribute(PATH_KEY));
		return path;
	}
	private static String printThread(String path) {
		return path + " in thread " + Thread.currentThread().getName();
	}
}