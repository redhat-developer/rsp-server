/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.util;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.model.ServerModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.model.IServerModelListener;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class TestServerUtils {

	private TestServerUtils() {
	}

	public static ServerModel createServerModel(String serverFilename, String serversDir, String content, IServerType type) {
		return createServerModel(serverFilename, Paths.get(serversDir), content, type, null);
	}

	public static ServerModel createServerModel(String serverFilename, Path serversDir, String content, IServerType type, 
			IServerModelListener listener) {
		IServerManagementModel managementModel = mock(IServerManagementModel.class);
		return createServerModel(serverFilename, serversDir, content, type, managementModel , listener);
	}

	public static ServerModel createServerModel(String serverFilename, Path serversDir, String content, 
			Function<IServer, IServerDelegate> serverDelegateProvider, String serverType) {
		IServerManagementModel managementModel = mock(IServerManagementModel.class);
		return createServerModel(serverFilename, serversDir, content, serverDelegateProvider, serverType, managementModel, null);
	}

	public static ServerModel createServerModel(String serverFilename, Path serversDir, String content, 
			Function<IServer, IServerDelegate> serverDelegateProvider, String serverType, IServerManagementModel managementModel, 
			IServerModelListener listener) {
		IServerType type = TestServerUtils.createServerType(serverType, serverDelegateProvider);
		return createServerModel(serverFilename, serversDir, content, type, managementModel, listener);
	}

	public static ServerModel createServerModel(String serverFilename, Path serversDir, String content, IServerType type, 
			IServerManagementModel managementModel, IServerModelListener listener) {
		ServerModel sm = new ServerModel(managementModel);
		if( listener != null)
			sm.addServerModelListener(listener);
		sm.addServerType(type);
		createServerFile(serverFilename, serversDir, content);
		sm.loadServers(serversDir.toFile());
		return sm;

	}

	public static IServerType createServerType(String typeId, Function<IServer, IServerDelegate> delegateProvider) {
		return new TestServerType(typeId, typeId + ".name", typeId + ".desc", delegateProvider);
	}


	public static IServerType createServerType(String typeId, 
			Function<IServer, IServerDelegate> delegateProvider,
			Attributes required) {
		return new TestServerType(typeId, typeId + ".name", typeId + ".desc", delegateProvider) {
			@Override
			public Attributes getRequiredAttributes() {
				return required;
			}
		};
	}
	
	public static File createServerFile(String filename, Path directory, String content) {
		Path s1 = null;
		try {
			s1 = directory.resolve(filename);
			Files.write(s1, content.getBytes());
		} catch (IOException e) {
			if (s1 != null && s1.toFile().exists()) {
				s1.toFile().delete();
				s1.toFile().getParentFile().delete();
			}
			fail();
		}
		return s1.toFile();
	}

	public static String getServerWithoutDeployablesString(String name, String type) {
		return "{id:\"" + name + "\", id-set:\"true\", " 
				+ "org.jboss.tools.rsp.server.typeId=\"" + type
				+ "\"}\n";
	}

	public static String getServerWithDeployablesString(String name, String type) {
		String contents = "{\n" + 
				"  \"id-set\": \"true\",\n" + 
				"  \"org.jboss.tools.rsp.server.typeId\": \"" + type  + "\",\n" + 
				"  \"id\": \"" + name + "\",\n" + 
				"  \"deployables\": {\n" + 
				"    \"deployable\": {\n" + 
				"      \"label\": \"some.name\",\n" + 
				"      \"path\": \"/tmp/serverdeployabletest_deployments1557855048044620815/hello-world-war-1.0.0.war\"\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n" + 
				"";
		return contents;
	}

	public static boolean isOk(IStatus status) {
		return status != null && status.isOK();
	}

}
