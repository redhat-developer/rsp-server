/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.itests.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.SocketLauncher;
import org.jboss.tools.rsp.client.cli.ServerManagementCLI;

/**
 *
 * @author jrichter
 */
public class DummyClientLauncher {

    private DummyClient myClient;
    private SocketLauncher<RSPServer> launcher;
    private Socket socket;
    private String host;
    private int port;
    private boolean connectionOpen = false;

    public DummyClientLauncher(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void launch() throws UnknownHostException, IOException {
        DummyClient client = new DummyClient();
        this.socket = new Socket(host, port);
        this.launcher = new SocketLauncher<>(client, RSPServer.class, socket);
        launcher.startListening().thenRun(() -> clientClosed());
        client.initialize(launcher.getRemoteProxy(), new ServerManagementCLI());
        myClient = client;
        connectionOpen = true;
    }

    private void clientClosed() {
        this.myClient = null;
        connectionOpen = false;
    }

    public void closeConnection() {
        if (launcher != null) {
            launcher.close();
        }
    }

    public DummyClient getClient() {
        return this.myClient;
    }

    public boolean isConnectionActive() {
        return connectionOpen;
    }

    public RSPServer getServerProxy() {
        if (myClient != null) {
            return myClient.getProxy();
        }
        return null;
    }
}
