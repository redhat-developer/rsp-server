package org.jboss.tools.ssp.server.spi.model.polling;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.tools.ssp.server.spi.servertype.IServer;

public abstract class WebPortPoller implements IServerStatePoller {

	private IServer server;
	private boolean canceled, done;
	private SERVER_STATE state;
	private SERVER_STATE expectedState;

	public void beginPolling(IServer server, SERVER_STATE expectedState) {
		this.server = server;
		this.canceled = done = false;
		this.expectedState = expectedState;
		this.state = SERVER_STATE.UNKNOWN;
		launchThread();
	}

	protected void launchThread() {
		Thread t = new Thread(new Runnable(){
			public void run() {
				pollerRun();
			}
		}, "Web Poller"); //$NON-NLS-1$
		t.start();
	}
	
	private synchronized void setStateInternal(boolean done, SERVER_STATE state) {
		this.done = done;
		this.state = state;
	}
	
	private void pollerRun() {
		setStateInternal(false, state);
		String url = getURL(getServer());
		if( url == null ) {
			
		}
		while(!canceled && !done) {
			boolean up = onePing(url);
			if( fromBool(up) == expectedState ) {
				setStateInternal(true, expectedState);
			}
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {} // ignore
		}
	}
	
	protected abstract String getURL(IServer server);
	
	private boolean onePing(String url) {
		URLConnection conn = null;
		try {
			URL pingUrl = new URL(url);
			conn = pingUrl.openConnection();
			((HttpURLConnection)conn).getResponseCode();
			return true;
		} catch( FileNotFoundException fnfe ) {
			return true;
		} catch (MalformedURLException e) {
			// Should NEVER happen since the URL's are hand-crafted, but whatever
//			Status s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getMessage(), e);
//			JBossServerCorePlugin.log(s);
		} catch (IOException e) {
			// Does not need to be logged
			return false;
		} finally {
			if( conn != null ) {
				((HttpURLConnection)conn).disconnect();
			}
		}
		return false;
	}
	
	public IServer getServer() {
		return server;
	}

	public synchronized boolean isComplete() throws PollingException, RequiresInfoException {
		return done;
	}

	public synchronized SERVER_STATE getState() throws PollingException, RequiresInfoException {
		return state;
	}

	private SERVER_STATE fromBool(boolean b) {
		return b ? SERVER_STATE.UP : SERVER_STATE.DOWN;
	}
	
	public void cleanup() {
	}

	public List<String> getRequiredProperties() {
		return new ArrayList<String>();
	}

	public void provideCredentials(Properties properties) {
	}

	public SERVER_STATE getCurrentStateSynchronous(IServer server) {
		String url = getURL(server);
		boolean b = onePing(url);
		SERVER_STATE ret = null;
		if( b ) {
			ret = SERVER_STATE.UP;
		} else {
			ret = SERVER_STATE.UNKNOWN;
		}
		return ret;
	}

	@Override
	public synchronized void cancel(CANCELATION_CAUSE cause) {
		canceled = true;
	}

	@Override
	public TIMEOUT_BEHAVIOR getTimeoutBehavior() {
		return IServerStatePoller.TIMEOUT_BEHAVIOR.FAIL;
	}

}
