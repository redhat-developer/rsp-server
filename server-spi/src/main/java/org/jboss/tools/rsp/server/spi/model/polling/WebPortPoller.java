/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.spi.model.polling;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.tools.rsp.server.spi.servertype.IServer;

public abstract class WebPortPoller extends AbstractPoller implements IServerStatePoller {

	private String name;

	@Override
	protected SERVER_STATE onePing(IServer server) {
		return onePing(getURL(server));
	}
	
	protected abstract String getURL(IServer server);
	
	private SERVER_STATE onePing(String url) {
		URLConnection conn = null;
		try {
			URL pingUrl = new URL(url);
			conn = pingUrl.openConnection();
			((HttpURLConnection)conn).getResponseCode();
			return SERVER_STATE.UP;
		} catch( FileNotFoundException fnfe ) {
			return SERVER_STATE.UP;
		} catch (MalformedURLException e) {
			// Should NEVER happen since the URL's are hand-crafted, but whatever
//			Status s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getMessage(), e);
//			JBossServerCorePlugin.log(s);
		} catch (IOException e) {
			// Does not need to be logged
			return SERVER_STATE.DOWN;
		} finally {
			if( conn != null ) {
				((HttpURLConnection)conn).disconnect();
			}
		}
		return SERVER_STATE.DOWN;
	}
	
	@Override
	protected String getThreadName() {
		return "Web Poller: " + name;
	}

	public WebPortPoller(String string) {
		super();
		this.name = string;
	}
}
