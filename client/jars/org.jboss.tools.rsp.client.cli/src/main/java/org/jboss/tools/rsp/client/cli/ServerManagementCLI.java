/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.ServerCapabilitiesResponse;
import org.jboss.tools.rsp.client.bindings.IClientConnectionClosedListener;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class ServerManagementCLI implements InputProvider, IClientConnectionClosedListener {
	public static void main(String[] args) {
		ServerManagementCLI cli = new ServerManagementCLI();
		try {
			cli.connect(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Connected to: " + args[0] + ":" + args[1]);
		cli.readInput();
	}

	private Scanner scanner = null;
	private ServerManagementClientLauncher launcher;
	private ConcurrentLinkedQueue<InputHandler> q = new ConcurrentLinkedQueue<>();
	private StandardCommandHandler defaultHandler;

	public void connect(String host, String port) throws Exception {
		if (host == null) {
			System.out.print("Enter server host: ");
			host = nextLine();
		}
		if (port == null) {
			System.out.print("Enter server port: ");
			port = nextLine();
		}

		launcher = new ServerManagementClientLauncher(host, Integer.parseInt(port), this);
		launcher.launch();
		launcher.setListener(this);
		
		// possibly unnecessary?
		ServerCapabilitiesResponse serverCap = launcher.getServerProxy().requestServerCapabilities().get();

		Map<String, String> clientCap2 = new HashMap<String, String>();
		clientCap2.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_10_0);
		clientCap2.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(true));
		ClientCapabilitiesRequest clientCap = new ClientCapabilitiesRequest(clientCap2);
		launcher.getServerProxy().registerClientCapabilities(clientCap);

		defaultHandler = new StandardCommandHandler(launcher, this);
	}

	public void addInputRequest(InputHandler handler) {
		if (q.peek() == null) {
			String prompt = handler.getPrompt();
			if (prompt != null) {
				System.out.println(prompt);
			}
		}
		q.add(handler);
	}

	protected String nextLine() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}
		return scanner.nextLine();
	}

	private void readInput() {
		while (true) {
			if (q.peek() != null) {
				InputHandler handler = q.peek();
				String prompt = handler.getPrompt();
				if (prompt != null) {
					System.out.println(prompt);
				}
			}
			String content = nextLine();
			InputHandler h = null;
			if (q.peek() == null) {
				h = defaultHandler;
			} else {
				h = q.remove();
			}
			if (h != null) {
				if( !launcher.isConnectionActive()) {
					initShutdown();
				}
				
				final InputHandler h2 = h;
				new Thread("Handle input") {
					public void run() {
						try {
							h2.handleInput(content);
						} catch (Exception e) {
							e.printStackTrace();
							// Try to recover
						}
					}
				}.start();
			}
		}
	}

	@Override
	public void connectionClosed() {
		initShutdown();
	}
	
	private synchronized void initShutdown() {
		System.out.println("Connection with remote server has terminated.");
		System.exit(0);
	}
}
