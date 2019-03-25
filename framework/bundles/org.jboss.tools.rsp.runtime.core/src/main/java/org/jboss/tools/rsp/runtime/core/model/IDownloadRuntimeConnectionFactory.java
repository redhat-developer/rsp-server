package org.jboss.tools.rsp.runtime.core.model;

import java.io.InputStream;
import java.net.URL;

public interface IDownloadRuntimeConnectionFactory {
	public InputStream createConnection(URL url, String user, String pass);
	public int getContentLength(URL url, String user, String pass);
}
