/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.server.model.ServerManagementModel;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.impl.ExtensionHandler;
import org.junit.Test;

public class ExtensionHandlerTest {
	@Test
	public void testAllServerStringsHaveMatchingServerType() throws IOException {
		ServerManagementModel smm = createServerManagementModel();
		ExtensionHandler.addExtensions(smm);
		ServerType[] types = smm.getServerModel().getServerTypes();
		String[] expectedTypes = IServerConstants.ALL_JBOSS_SERVERS;
		
		for( int i = 0; i < expectedTypes.length; i++ ) {
			assertTrue("server " + expectedTypes[i] + " not found.", exists(expectedTypes[i], types));
		}
		
	}
	
	private boolean exists(String id, ServerType[] types) {
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].getId().equals(id))
				return true;
		}
		return false;
	}
	
	private ServerManagementModel createServerManagementModel() throws IOException {
		File location = Files.createTempDirectory("ServerManagementModelTest").toFile();
		return new ServerManagementModel(location) {};
	}

}
