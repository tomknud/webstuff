package org.tom.application;

import java.io.*;
import java.net.*;
import java.nio.charset.*;

import javax.net.ssl.*;
import java.security.KeyStore;

/*
 * This example shows how to set up a key manager to do client
 * authentication if required by server.
 *
 * This program assumes that the client is not inside a firewall.
 * The application can be modified to connect to a server outside
 * the firewall by following SSLSocketClientWithTunneling.java.
 */
public class SSLSocketClientWithClientAuth {

    public static void main(String[] args) throws Exception {
    	// -Djavax.net.ssl.trustStore=clienttruststore.jks -Djavax.net.ssl.trustStorePassword=passphrase
//    	System.getProperty("javax.net.ssl.trustStore");
        String host = null;
        int port = -1;
        String path = null;
//        for (int i = 0; i < args.length; i++)
//            System.out.println(args[i]);

        if (args.length < 3) {
            System.out.println(
                "USAGE: java SSLSocketClientWithClientAuth " +
                "host port requestedfilepath");
            System.exit(-1);
        }

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
            path = args[2];
        } catch (IllegalArgumentException e) {
             System.out.println("USAGE: java SSLSocketClientWithClientAuth host port requestedfilepath");
             System.exit(-1);
        }

        try {

            /*
             * Set up a key manager for client authentication
             * if asked by the server.  Use the implementation's
             * default TrustStore and secureRandom routines.
             */
        	String result = getForObject(host, port, path);
		    System.out.print(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static String getFromURLTLS(String host) throws IOException, UnknownHostException {
        final URL url = new URL("https://"+host);
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        String response = null;

        try (final InputStream inputStream = connection.getInputStream()) {
            response = readAsString(inputStream);
//            System.out.println(response);
        }
		return response;
    }
    
    private static String readAsString(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }
    
	public static String getForObject(String host, int port, String path) throws IOException, UnknownHostException {
		String result;
		SSLSocketFactory factory = null;
		try {
		    SSLContext ctx;
		    TrustManagerFactory tmf;
		    KeyStore ks;
		    char[] passphrase = "passphrase".toCharArray();

		    ctx = SSLContext.getInstance("TLS");
		    tmf = TrustManagerFactory.getInstance("SunX509");

		    ks = KeyStore.getInstance("Windows-ROOT");
//			ks = KeyStore.getInstance("JKS");
		    ks.load(null, null);
//		    ks.load(new FileInputStream("testkeys"), passphrase);

		    tmf.init(ks);
		    ctx.init(null, tmf.getTrustManagers(), null);

		    factory = ctx.getSocketFactory();
		} catch (Exception e) {
		    throw new IOException(e.getMessage());
		}

		SSLSocket socket = (SSLSocket)factory.createSocket(host, port);

		/*
		 * send http request
		 *
		 * See SSLSocketClient.java for more information about why
		 * there is a forced handshake here when using PrintWriters.
		 */
		socket.startHandshake();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
//		out.println("GET " + "/" + " HTTP/1.1");
//		out.println("Host:" + " www.cnbc.com");
		out.println(":authority:" + "www.cnbc.com:443");
		out.println(":method:" + "GET");
		out.println(":path:" + "/");
		out.println(":scheme:" + "https");
		out.println("sec-fetch-dest:" + "document");
		out.println("sec-fetch-mode:" + "navigate");
		out.println("sec-fetch-site" + "none");
		out.println("sec-fetch-user" + "?1");
		out.println("upgrade-insecure-requests:" + "1");
		out.println("Connection:" +  "keep-alive");
		out.println("User-Agent:" +  "Mozilla/5.0");
		out.println("Accept:" +  "text/html");
//		out.println("Accept-Encoding:" +  "gzip");
		out.println("Accept-Language:" +  "en-US,en;q=0.9");
		out.flush();

		/*
		 * Make sure there were no surprises
		 */
		if (out.checkError())
		    System.out.println(
		        "SSLSocketClient: java.io.PrintWriter error");
		result = ripPage(socket);
		out.close();
		socket.close();
		return result;
	}

	private static String ripPage(SSLSocket socket) throws IOException {
		InputStream is = socket.getInputStream();
		byte[] asdf = is.readAllBytes();
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String inputLine;
		StringBuffer sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		in.close();
		return sb.toString();
	}
}