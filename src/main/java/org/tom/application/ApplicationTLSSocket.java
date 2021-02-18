package org.tom.application;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;

import java.io.*;
import java.net.*;
import java.security.*;

import javax.net.*;
import javax.net.ssl.*;

/* ClassFileServer.java -- a simple file server that can server
 * Http get request in both clear and secure channel
 *
 * The ClassFileServer implements a ClassServer that
 * reads files from the file system. See the
 * doc for the "Main" method for how to run this
 * server.
 */

public class ApplicationTLSSocket {

    private static int DefaultServerPort = 2001;

    /**
     * Main method to create the class server that reads
     * files. This takes two command line arguments, the
     * port on which the server accepts requests and the
     * root of the path. To start up the server: <br><br>
     *
     * <code>   java ClassFileServer <port> <path>
     * </code><br><br>
     *
     * <code>   new ClassFileServer(port, docroot);
     * </code>
     */
    public static void main(String args[])
    {
        System.out.println("USAGE: java ClassFileServer port docroot [TLS [true]]");
        System.out.println("");
        System.out.println(
            "If the third argument is TLS, it will start as\n" +
            "a TLS/SSL file server, otherwise, it will be\n" +
            "an ordinary file server. \n" +
            "If the fourth argument is true,it will require\n" +
            "client authentication as well.");

        int port = DefaultServerPort;
        String docroot = "";

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        if (args.length >= 2) {
            docroot = args[1];
        }
        String type = "PlainSocket";
        if (args.length >= 3) {
            type = args[2];
        }
        new ApplicationTLSSocket().process(args, port, docroot, type);
    }

	private void process(String[] args, int port, String docroot, String type) {
		try {
            ServerSocketFactory ssfTLS = getServerSocketFactory(type);
            ServerSocket ss = ssfTLS.createServerSocket(port);
            javax.net.ssl.SSLServerSocket a;
            if (args.length >= 4 && args[3].equals("true")) {
                ((SSLServerSocket)ss).setNeedClientAuth(true);
            }
            // TODO : allocate this better
            new ServerProcess(ss, docroot);
        } catch (IOException e) {
            System.err.println("Unable to start ClassServer: " + e.getMessage());
        }
	}

    private ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS") || type.equals("tls")) {
            SSLServerSocketFactory ssf = null;
            try {
                // set up key manager to do server authentication
                char[] passphrase = "passphrase".toCharArray();
                SSLContext ctx = SSLContext.getInstance("TLSv1.3");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(newInputStream(get("testkeys")), passphrase);
                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }

}