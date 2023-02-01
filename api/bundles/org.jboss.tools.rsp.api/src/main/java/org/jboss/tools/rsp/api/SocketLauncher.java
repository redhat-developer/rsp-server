/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;

public class SocketLauncher<T> implements Launcher<T> {

	private final Launcher<T> launcher;
	private Future<Void> startListeningResult;
	private Socket socket;

	public SocketLauncher(Object localService, Class<T> remoteInterface, Socket socket) throws IOException {
		this.launcher = Launcher.createLauncher(localService, remoteInterface, socket.getInputStream(),
				socket.getOutputStream());
		this.socket = socket;
	}

	public SocketLauncher(Object localService, Class<T> remoteInterface, Socket socket, PrintWriter tracing)
			throws IOException {
		Launcher<T> launcherTmp = createLauncher(createBuilder(remoteInterface), localService,remoteInterface,
				socket.getInputStream(),socket.getOutputStream(), tracing);
		this.launcher = launcherTmp;
		this.socket = socket;
	}

	public SocketLauncher(Object localService, Class<T> remoteInterface, 
			Socket socket, Builder<T> b, PrintWriter tracing)
			throws IOException {
		Launcher<T> launcherTmp = createLauncher(b, localService,remoteInterface,
				socket.getInputStream(),socket.getOutputStream(), tracing);
		this.launcher = launcherTmp;
		this.socket = socket;
	}

	protected Builder<T> createBuilder(Class<T> remoteInterface) {
		return new Builder<T>();
	}
	
	protected Launcher<T> createLauncher(Builder<T> builder, Object localService, 
			Class<T> remoteInterface, InputStream in, OutputStream out, PrintWriter tracing) {
		return builder.setLocalService(localService)
				.setRemoteInterface(remoteInterface)
				.setInput(in).setOutput(out)
				.traceMessages(tracing)
				.create();
	}
	
	public CompletableFuture<Void> startListening() {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		return CompletableFuture.runAsync(() -> {
			try {
				this.startListeningResult = this.launcher.startListening();
				startListeningResult.get();
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(ie);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			service.shutdown();
		}, service);
	}

	public T getRemoteProxy() {
		return this.launcher.getRemoteProxy();
	}


	@Override
	public RemoteEndpoint getRemoteEndpoint() {
		return this.launcher.getRemoteEndpoint();
	}

	public void close() {
		if( startListeningResult != null ) {
			startListeningResult.cancel(true);
		}
		try {
			socket.close();
		} catch(IOException ioe) {
		}
	}
	
	public Future<Void> getStartListeningResult() {
		return startListeningResult;
	}
}
