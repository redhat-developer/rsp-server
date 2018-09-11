/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.servicemgr.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ServiceManagerEnvironment {

	public static final String SHARED_INFO_KEY = "cdk.sharedinfo.serviceManagerEnvironment";

	public static final String KEY_DOCKER_HOST = "DOCKER_HOST";
	public static final String KEY_DOCKER_TLS_VERIFY = "DOCKER_TLS_VERIFY";
	public static final String KEY_DOCKER_CERT_PATH = "DOCKER_CERT_PATH";
	public static final String KEY_DOCKER_API_VERSION = "DOCKER_API_VERSION";
	public static final String IMAGE_REGISTRY_KEY = "DOCKER_REGISTRY";
	public static final String KEY_DEFAULT_IMAGE_REGISTRY = "jbt.default.DOCKER_REGISTRY";

	public static final String KEY_OPENSHIFT_HOST = "HOST";
	public static final String KEY_OPENSHIFT_PORT = "PORT";
	public static final String KEY_OPENSHIFT_CONSOLE_URL = "CONSOLE_URL";

	private static final String DOTCDK_AUTH_SCHEME = "openshift.auth.scheme";
	private static final String DOTCDK_AUTH_USERNAME = "openshift.auth.username";
	private static final String DOTCDK_AUTH_PASS = "openshift.auth.password";

	private static final String HTTPS_SCHEMA = "https://";

	public static final String DEFAULT_CDK_USER = "openshift-dev";
	public static final String DEFAULT_CDK_PASS = "devel";
	public static final String DEFAULT_MINISHIFT_USER = "developer";
	public static final String DEFAULT_MINISHIFT_PASS= "developer";
	
	
	int openshiftPort = 8443;
	String openshiftHost = "https://10.1.2.2";
	String defaultUser = DEFAULT_CDK_USER;

	private Map<String, String> env;

	public ServiceManagerEnvironment(Map<String, String> env) throws URISyntaxException {
		this.env = env;

		String osPort = env.get(KEY_OPENSHIFT_PORT);
		if (osPort != null) {
			try {
				this.openshiftPort = Integer.parseInt(osPort);
			} catch (NumberFormatException nfe) {
				// ignore, use default
			}
		}

		String osHost = env.get(KEY_OPENSHIFT_HOST);
		if (osHost == null) {
			String dockerHost = env.get(KEY_DOCKER_HOST);
			if (dockerHost != null) {
				URI url = new URI(dockerHost);
				osHost = url.getHost();
			}
		}
		if (osHost != null)
			this.openshiftHost = HTTPS_SCHEMA + osHost;
	}

	public String getOpenShiftHost() {
		return openshiftHost;
	}

	public int getOpenShiftPort() {
		return openshiftPort;
	}

	public String getDockerRegistry() {
		String dockerReg = env.get(IMAGE_REGISTRY_KEY);
		if (dockerReg == null) {
			dockerReg = env.get(KEY_DEFAULT_IMAGE_REGISTRY);
		}
		if (dockerReg != null && !dockerReg.contains("://")) {
			dockerReg = "https://" + dockerReg;
		}
		return dockerReg;
	}

	public String getAuthorizationScheme() {
		String authScheme = env.containsKey(DOTCDK_AUTH_SCHEME) ? env.get(DOTCDK_AUTH_SCHEME) : "Basic";
		return authScheme;
	}

	public void setDefaultUsername(String user) {
		defaultUser = user;
	}
	
	public String getUsername() {
		String user = env.containsKey(DOTCDK_AUTH_USERNAME) ? env.get(DOTCDK_AUTH_USERNAME) : defaultUser;
		return user;
	}

	public String getPassword() {
		String user = getUsername();
		String defPass = null;
		if(DEFAULT_CDK_USER.equals(user)) {
			defPass = DEFAULT_CDK_PASS;
		} else if( DEFAULT_MINISHIFT_USER.equals(user)){
			defPass = DEFAULT_MINISHIFT_PASS;
		}
		String pass = env.containsKey(DOTCDK_AUTH_PASS) ? env.get(DOTCDK_AUTH_PASS) : defPass;
		return pass;
	}

	public String getDockerHost() {
		return env.get(KEY_DOCKER_HOST);
	}

	public String getDockerTLSVerify() {
		return env.get(KEY_DOCKER_TLS_VERIFY);
	}

	public String getDockerCertPath() {
		return env.get(KEY_DOCKER_CERT_PATH);
	}

	public String get(String k) {
		return env.get(k);
	}

}