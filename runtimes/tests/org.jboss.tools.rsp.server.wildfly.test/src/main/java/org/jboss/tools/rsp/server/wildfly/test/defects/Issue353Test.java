/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.wildfly.test.defects;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.IJBossPublishController;
import org.jboss.tools.rsp.server.wildfly.servertype.publishing.WildFlyPublishController;
import org.junit.Test;

public class Issue353Test extends AbstractMockServerTest {
	@Test
	public void testRemoveDeployment() {
		final IServer s = createServer(IServerConstants.RUNTIME_WILDFLY_140);

		DeployableReference refWithOptions = null;
		DeployableReference refNoOptions = null;
		try {
			Path f = Files.createTempDirectory("some.amazing.prefix");
			String asString = f.toFile().getAbsolutePath();
			refWithOptions = new DeployableReference("some.label", asString);
			refNoOptions = new DeployableReference("some.label", asString);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME, "out.war");
			refWithOptions.setOptions(map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WildFlyServerDelegate del = new WildFlyServerDelegate(s) {
			public IStatus canRemoveDeployable(DeployableReference reference) {
				return super.canRemoveDeployable(reference);
			}
			protected IJBossPublishController getOrCreatePublishController() {
				return new WildFlyPublishController(s, null) {
					public IStatus canRemoveDeployable(DeployableReference ref) {
						if( ref.getOptions() == null ) {
							throw new RuntimeException("Test failed");
						}
						return Status.OK_STATUS;
					}
				};
			}
		};
		
		try {
			del.getServerPublishModel().addDeployable(refWithOptions);
			del.canRemoveDeployable(refNoOptions);
		} catch(RuntimeException re) {
			fail();
		}
	}
}
