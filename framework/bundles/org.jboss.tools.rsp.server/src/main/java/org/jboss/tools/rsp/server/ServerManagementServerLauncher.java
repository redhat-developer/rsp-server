/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.model.ServerPersistenceManager;
import org.jboss.tools.rsp.server.spi.client.ClientThreadLocal;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerManagementServerLauncher {
	private static final Logger LOG = LoggerFactory.getLogger(ServerManagementServerLauncher.class);

	private final ServerPersistenceManager persistenceEventManager;
	
	public static void main(String[] args) throws Exception {
		ServerManagementServerLauncher instance = new ServerManagementServerLauncher();
		instance.launch(args[0]);
		instance.shutdownOnInput();
	}

	public void shutdownOnInput() throws IOException {
		System.out.println("Enter any character to stop");
		System.in.read();
		shutdown();
	}

	protected ServerManagementServerImpl serverImpl;
	private ListenOnSocketRunnable socketRunnable;
	private ServerSocket serverSocket;
	public ServerManagementServerLauncher() {
		this.serverImpl = createImpl();
		this.persistenceEventManager = new ServerPersistenceManager(this);
	}
	
	protected ServerManagementServerImpl createImpl() {
		return new ServerManagementServerImpl(this, new ServerManagementModel());
	}
	
	public IServerManagementModel getModel() {
		return serverImpl.getModel();
	}
	
	public List<RSPClient> getClients() {
		return serverImpl.getClients();
	}
	
	public void launch(String portString) throws Exception {
		launch(Integer.parseInt(portString));
	}

	public void launch(int port) throws Exception {
		persistenceEventManager.loadState();
		// create the chat server
		startListening(port, serverImpl);
	}
	
	protected void startListening(int port, ServerManagementServerImpl server) throws IOException {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		// create the socket server
		try {
			socketRunnable = new ListenOnSocketRunnable(serverSocket, server);
			System.out.println("The server management server is running on port " + port);
			threadPool.submit(socketRunnable);
		} catch(Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t;
		}
	}
	
	private class ListenOnSocketRunnable implements Runnable {
		private ServerSocket serverSocket;
		private ServerManagementServerImpl server;
		private boolean listening = true;
		public ListenOnSocketRunnable(ServerSocket serverSocket, ServerManagementServerImpl server) {
			this.server = server;
			this.serverSocket = serverSocket;
		}
		@Override
		public void run() {
			while (isListening()) {
				oneSocket(serverSocket, server);
			}
		}
		
		public void stopListening() {
			setListening(false);
		}
		
		private synchronized boolean isListening() {
			return listening;
		}
		private synchronized void setListening(boolean listening) {
			this.listening = listening;
		}
		
	}
	
	private void oneSocket(ServerSocket serverSocket, ServerManagementServerImpl server) {
		try {
			// wait for clients to connect
			Socket socket = serverSocket.accept();
			// create a JSON-RPC connection for the accepted socket
			RSPServerSocketLauncher<RSPClient> launcher = createSocketLauncher(server,
					RSPClient.class, socket, createLoggingPrintWriter());

			// Alert the models a new client has been added before they start making requests
			Runnable removeClient = server.addClient(launcher);
			/*
			 * Start listening for incoming messages. When the JSON-RPC connection is closed
			 * disconnect the remote client from the server.
			 */
			launcher.startListening().thenRun(removeClient);
			
			// Alert the models they may begin requesting information from the client, 
			// now that we are actually listening to their responses
			server.clientAdded(launcher);
			
			LOG.info("Client " + socket.getInetAddress().getCanonicalHostName() +
							":"+ socket.getPort() + " is connected");
		} catch(IOException ioe) {
			// We shouldn't fail if we're still supposed to be listening
			if (socketRunnable != null && socketRunnable.isListening()) {
				LOG.error(ioe.getMessage(), ioe);
			}
		}
	}
	
	protected RSPServerSocketLauncher<RSPClient> createSocketLauncher(
			ServerManagementServerImpl server, Class<RSPClient> class1, 
			Socket socket,
			LoggingPrintWriter loggingPrintWriter) throws IOException {
		 return new RSPServerSocketLauncher<RSPClient>(server,
					RSPClient.class, socket, ClientThreadLocal.getStore(), 
					createLoggingPrintWriter());
	}
	
	private class LoggingStringWriter extends StringWriter {
	    public void flush() {
	    	String val = null;
	    	synchronized(this) {
	    		val = getBuffer().toString();
	    		getBuffer().setLength(0);
	    	}
	    	LOG.debug(val);
	    }
	}
	private LoggingPrintWriter createLoggingPrintWriter() {
		LoggingStringWriter sw = new LoggingStringWriter();
		LoggingPrintWriter writer = new LoggingPrintWriter(sw);
		return writer;
	}
	private static class LoggingPrintWriter extends PrintWriter {
		private StringWriter sw;
		public LoggingPrintWriter(LoggingStringWriter writer) {
			super(writer);
			this.sw = writer;
		}
		
	}
	
	public void shutdown() {
		persistenceEventManager.saveState();
		if( socketRunnable != null )
			socketRunnable.stopListening();
		closeAllConnections();
		try {
			if( serverSocket != null )
				serverSocket.close();
		} catch(IOException ioe) {
		}
		ShutdownExecutor.getExecutor().shutdown();
	}
	

	private void closeAllConnections() {
		List<SocketLauncher<RSPClient>> all = 
				serverImpl.getActiveLaunchers();
		for( SocketLauncher<RSPClient> i : all ) {
			i.close();
		}
	}

}
