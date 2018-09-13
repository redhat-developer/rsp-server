/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.server.discovery.DiscoveryPathModel;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathListener;
import org.junit.Test;

public class DiscoveryPathModelTest {
	@Test
	public void testBasicFunctionality() {
		DiscoveryPathModel dpm = new DiscoveryPathModel();
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
		DiscoveryPath dp = new DiscoveryPath("/some/path/from/user");
		dpm.addPath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(1, dpm.getPaths().size());
		dpm.removePath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
	}
	
	@Test
	public void testDuplicateAdd() {
		DiscoveryPathModel dpm = new DiscoveryPathModel();
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
		DiscoveryPath dp = new DiscoveryPath("/some/path/from/user");
		dpm.addPath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(1, dpm.getPaths().size());
		
		// add a second time
		dpm.addPath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(1, dpm.getPaths().size());

		// Add an identical path but different object
		DiscoveryPath dp2 = new DiscoveryPath("/some/path/from/user");
		dpm.addPath(dp2);
		assertNotNull(dpm.getPaths());
		assertEquals(1, dpm.getPaths().size());

		dpm.removePath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
	}

	@Test
	public void testListeners() {
		DiscoveryPathModel dpm = new DiscoveryPathModel();
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
		
		final Boolean[] added = new Boolean[] { new Boolean(false) };
		final Boolean[] removed = new Boolean[] { new Boolean(false) };
		
		IDiscoveryPathListener listener = new IDiscoveryPathListener() {
			@Override
			public void discoveryPathAdded(DiscoveryPath path) {
				added[0] = true;
			}
			@Override
			public void discoveryPathRemoved(DiscoveryPath path) {
				removed[0] = true;
			}
			
		};
		dpm.addListener(listener);
		
		assertFalse(added[0]);
		assertFalse(removed[0]);
		added[0] = false;
		removed[0] = false;
		
		DiscoveryPath dp = new DiscoveryPath("/some/path/from/user");
		dpm.addPath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(1, dpm.getPaths().size());
		assertTrue(added[0]);
		assertFalse(removed[0]);
		added[0] = false;
		removed[0] = false;
		
		dpm.removePath(dp);
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
		assertFalse(added[0]);
		assertTrue(removed[0]);
		added[0] = false;
		removed[0] = false;
	}

	@Test
	public void testPersistence() {
		DiscoveryPathModel dpm = new DiscoveryPathModel();
		assertNotNull(dpm.getPaths());
		assertEquals(0, dpm.getPaths().size());
		DiscoveryPath dp = new DiscoveryPath("/some/path/from/user");
		DiscoveryPath dp2 = new DiscoveryPath("/some/path/from/user2");
		dpm.addPath(dp);
		dpm.addPath(dp2);
		assertNotNull(dpm.getPaths());
		assertEquals(2, dpm.getPaths().size());
		
		File out = null;
		try {
			out = File.createTempFile("discoveryModelTest", System.currentTimeMillis() + "");
			out.deleteOnExit();
			dpm.saveDiscoveryPaths(out);
			String contents = new String(Files.readAllBytes(out.toPath()));
			String expected = "/some/path/from/user\n/some/path/from/user2\n";
			assertEquals(contents, expected);
		} catch(IOException ioe) {
			fail();
		}
		
		if( out == null ) {
			fail();
		}
		
		DiscoveryPathModel model2 = new DiscoveryPathModel();
		try {
			model2.loadDiscoveryPaths(out);
			assertNotNull(model2.getPaths());
			assertEquals(2, model2.getPaths().size());
		} catch(IOException ioe) {
			
		}
	}
	
	
	
}
