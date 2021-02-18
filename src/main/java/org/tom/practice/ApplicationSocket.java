/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tom.practice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationSocket {
	public static void main(String[] args) throws Exception {
		int PORT = 8083;
		ServerSocket server = new ServerSocket(PORT);
		boolean keepRunning = true;
		while (keepRunning) {
			System.out.println("Listening for connection on port "+PORT);
			try (Socket socket = server.accept()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				List<String> lines = new ArrayList<>();
				String line;
				String function = null;
				while((line = reader.readLine()) != null) {
					if(line.length() == 0) {
						System.out.println("Empty Header");
						break;
					} else {
						if(line.charAt(0) == 22) {
							System.out.println("Handshake");
							String newLine = line.substring(4);
							lines.add(line);
							if(function == null) {
								function = newLine;
							}
						} else {
							lines.add(line);
							if(function == null) {
								function = line;
							}							
						}
						System.out.println("Line:"+line + " length:"+line.length());
					}
				}
				if(function != null) {
					String[] method = function.split(" ");
					System.out.println("method:" + method[0]+".");
					String[] path = method[1].split("\\?");
					System.out.println("path:" + path[0]);
					String[] parameters = path[1].split(";");
					int pcount = 1;
					StringBuffer sb = new StringBuffer();
					sb.append("HTTP/1.1 200 OK\r\n\r\n");
					sb.append("@ ");
					sb.append(LocalDateTime.now() + "\n");
					sb.append("a " + method[0]	+ " has been requested on path " + path[0] + "\n");
					for(String parameter : parameters) {
						String[] entry = parameter.split("=");
						if(entry.length >1) {
							sb.append("parameter " + pcount++ + " " + entry[0] + " is " + entry[1] + "\n");
							if(entry[0].equals("close") && entry[1].equals("true")) {
								keepRunning = false;
								sb.append("Closing Application!\n");
							}
						}
					}
					socket.getOutputStream().write(sb.toString().getBytes("UTF-8"));
					System.out.println("Returning Request");
				} else {
					System.out.println("Emtpy request");					
				}
			}
		}
		server.close();
	}
}