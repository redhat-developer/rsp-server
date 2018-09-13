/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.test.servertype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.jboss.tools.rsp.launching.LaunchingCore;
import org.jboss.tools.rsp.server.wildfly.servertype.JBossVMRegistryDiscovery;
import org.junit.Before;
import org.junit.Test;

public class JBossVMRegistryDiscoveryTest {

	private IVMInstallRegistry registry;
	private JBossVMRegistryDiscoveryTestExt discovery;

	private class JBossVMRegistryDiscoveryTestExt extends JBossVMRegistryDiscovery {
		public IVMInstallRegistry getDefaultRegistry() {
			return registry;
		}
		public String getNewVmName2(String base, IVMInstallRegistry reg) {
			return super.getNewVmName(base, reg);
		}
	}
	
	@Before
	public void before() {
		this.registry = new VMInstallRegistry();
		this.discovery = new JBossVMRegistryDiscoveryTestExt();
	}

	@Test
	public void registryFound() {
		IVMInstallRegistry ret = discovery.getDefaultRegistry();
		assertThat(ret).isEqualTo(registry);
	}

	@Test
	public void activeVMAdded() {
		assertThat(discovery.getDefaultRegistry().getVMs()).hasSize(0);
		discovery.getDefaultRegistry().addActiveVM();
		assertThat(discovery.getDefaultRegistry().getVMs()).hasSize(1);
		assertNotNull(discovery.getDefaultRegistry().getDefaultVMInstall());
		discovery.getDefaultRegistry().removeVMInstall(discovery.getDefaultRegistry().getDefaultVMInstall());
		assertThat(discovery.getDefaultRegistry().getVMs()).hasSize(0);
	}
	
	@Test
	public void findVMInstall() {
		assertThat(discovery.getDefaultRegistry().getVMs()).hasSize(0);
		assertNull(discovery.getDefaultRegistry().getDefaultVMInstall());
		discovery.getDefaultRegistry().addActiveVM();
		assertThat(discovery.getDefaultRegistry().getVMs()).hasSize(1);
		String home = System.getProperty("java.home");
		assertNotNull(discovery.findVMInstall(home));
		registry.removeVMInstall(registry.getDefaultVMInstall());
		assertNull(discovery.findVMInstall(home));
	}
	
	@Test
	public void addMultipleVMs() {
		String home = System.getProperty("java.home");
		IVMInstall ivmi = createVMI("vm1", home);
		registry.addVMInstall(ivmi);
		String name2 = discovery.getNewVmName2("vm1", registry);
		assertEquals(name2, "vm1 (1)");
		IVMInstall ivmi2 = createVMI("vm1 (1)", home);
		registry.addVMInstall(ivmi2);
		String name3 = discovery.getNewVmName2("vm1 (2)", registry);
		assertEquals(name3, "vm1 (2)");
	}
	
	@Test 
	public void ensureVMInstallAddedTest() {
		assertThat(registry.getVMs()).hasSize(0);
		assertFalse(discovery.ensureVMInstallAdded(null, registry));
		registry.addActiveVM();
		assertTrue(discovery.ensureVMInstallAdded(null, registry));
		registry.removeVMInstall(registry.getDefaultVMInstall());
		
		File dataFolder = LaunchingCore.getDataLocation();
		File dne = new File(dataFolder, "doesnotexist");
		assertFalse(discovery.ensureVMInstallAdded(dne.getAbsolutePath(), registry));
		try {
			new FileOutputStream(dne).close();
		} catch(IOException ioe) {
			fail();
		}
		assertFalse(discovery.ensureVMInstallAdded(dne.getAbsolutePath(), registry));
		dne.delete();
		assertFalse(discovery.ensureVMInstallAdded(dataFolder.getAbsolutePath(), registry));
		
		// Now try with javahome that does exist
		String home = System.getProperty("java.home");
		assertThat(registry.getVMs()).hasSize(0);
		assertTrue(discovery.ensureVMInstallAdded(home, registry));
		assertThat(registry.getVMs()).hasSize(1);
		assertTrue(discovery.ensureVMInstallAdded(home, registry));
		assertThat(registry.getVMs()).hasSize(1);
	}

	private IVMInstall createVMI(String id, String path) {
		IVMInstall ivmi = StandardVMType.getDefault().createVMInstall(id);
		String home = System.getProperty("java.home");
		ivmi.setInstallLocation(new File(home));
		return ivmi;		
	}
	
}
