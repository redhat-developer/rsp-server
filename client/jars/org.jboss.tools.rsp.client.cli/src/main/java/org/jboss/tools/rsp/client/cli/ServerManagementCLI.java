/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ICapabilityKeys;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.client.bindings.IClientConnectionClosedListener;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class ServerManagementCLI implements InputProvider, IClientConnectionClosedListener {

	private Scanner scanner = null;
	private ServerManagementClientLauncher launcher;
	private ConcurrentLinkedQueue<InputHandler> queue = new ConcurrentLinkedQueue<>();
	private StandardCommandHandler defaultHandler;

	private void connect(String host, String port) throws Exception {
		if (host == null) {
			System.out.print("Enter server host: ");
			host = getUserInput();
		}
		if (port == null) {
			System.out.print("Enter server port: ");
			port = getUserInput();
		}

		this.launcher = launch(host, port);
		this.defaultHandler = new StandardCommandHandler(launcher, this);
	}

	private ServerManagementClientLauncher launch(String host, String port) throws IOException, InterruptedException, ExecutionException {
		ServerManagementClientLauncher launcher = new ServerManagementClientLauncher(host, Integer.parseInt(port), this);
		launcher.setListener(this);
		launcher.launch();
		ClientCapabilitiesRequest clientCapRequest = createClientCapabilitiesRequest();
		launcher.getServerProxy().registerClientCapabilities(clientCapRequest).get();
		return launcher;
	}

	private ClientCapabilitiesRequest createClientCapabilitiesRequest() {
		Map<String, String> clientCap = new HashMap<>();
		clientCap.put(ICapabilityKeys.STRING_PROTOCOL_VERSION, ICapabilityKeys.PROTOCOL_VERSION_0_10_0);
		clientCap.put(ICapabilityKeys.BOOLEAN_STRING_PROMPT, Boolean.toString(true));
		return new ClientCapabilitiesRequest(clientCap);
	}

	@Override
	public void addInputRequest(InputHandler handler) {
		if (queue.peek() == null) {
			printUserPrompt(handler);
		}
		queue.add(handler);
	}

	protected String getUserInput() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}
		return scanner.nextLine();
	}

	private void readInputs() {
		while (true) {
			if (queue.peek() != null) {
				printUserPrompt(queue.peek());
			}
			String content = getUserInput();
			InputHandler handler = getInputHandler();
			if (handler != null) {
				if (!launcher.isConnectionActive()) {
					close();
				}
				
				new Thread("Handle input") {
					@Override
					public void run() {
						try {
							handler.handleInput(content);
						} catch (Exception e) {
							e.printStackTrace();
							// Try to recover
						}
					}
				}.start();
			}
		}
	}

	private InputHandler getInputHandler() {
		InputHandler h = null;
		if (queue.peek() == null) {
			h = defaultHandler;
		} else {
			h = queue.remove();
		}
		return h;
	}

	private void printUserPrompt(InputHandler handler) {
		String prompt = handler.getPrompt();
		if (prompt != null) {
			System.out.println(prompt);
		}
	}

	@Override
	public void connectionClosed() {
		close();
	}
	
	private void close() {
		System.out.println("Connection with remote server has terminated.");
		System.exit(0);
	}

	public static void main(String[] args) {
		ServerManagementCLI cli = new ServerManagementCLI();
		try {
			cli.connect(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Connected to: " + args[0] + ":" + args[1]);
		cli.readInputs();
	}

}
