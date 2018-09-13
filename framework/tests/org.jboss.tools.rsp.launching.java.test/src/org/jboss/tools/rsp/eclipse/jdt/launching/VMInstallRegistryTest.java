/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.launching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallChangedListener;
import org.jboss.tools.rsp.eclipse.jdt.launching.VMInstallRegistry;
import org.junit.Before;
import org.junit.Test;

public class VMInstallRegistryTest {

	private VMInstallRegistry registry;
	private IVMInstall vm;
	private IVMInstallChangedListener listener;

	@Before
	public void before() {
		this.registry = new VMInstallRegistry();
		this.vm = mockVMInstall("some-id", null);
		this.listener = mock(IVMInstallChangedListener.class);
	}

	@Test(expected = RuntimeException.class)
	public void addNullVmThrows() {
		registry.addVMInstall(null);
	}

	@Test(expected = RuntimeException.class)
	public void addVmTwiceThrows() {
		registry.addVMInstall(vm);
		registry.addVMInstall(vm);
	}

	@Test
	public void shouldFindVmAfterAddingIt() {
		// given
		// when
		registry.addVMInstall(vm);
		IVMInstall registeredVm = registry.findVMInstall(vm.getId());
		// then
		assertThat(registeredVm).isNotNull();
	}

	@Test
	public void shouldFindVMByInstallLocation() {
		// given
		File installLocation = new File(System.getProperty("user.home"));
		IVMInstall vm = mockVMInstall("other-id", installLocation);
		registry.addVMInstall(vm);
		// when
		IVMInstall found = registry.findVMInstall(installLocation);
		// then
		assertThat(found).isEqualTo(vm);
	}

	@Test
	public void shouldAddActiveVM() {
		// given
		int numOfVMs = registry.getVMs().length;
		// when
		registry.addActiveVM();
		// then
		assertThat(registry.getVMs().length).isEqualTo(numOfVMs + 1);
	}

	@Test
	public void shouldFireVmAddedWhenAddingVm() {
		// given
		registry.addListener(listener);
		// when
		registry.addVMInstall(vm);
		// then
		verify(listener).vmAdded(vm);
	}

	@Test
	public void shouldNotFindVmAfterRemovingIt() {
		// given
		registry.addVMInstall(vm);
		// when
		registry.removeVMInstall(vm.getId());
		// then
		IVMInstall registeredVm = registry.findVMInstall(vm.getId());
		assertThat(registeredVm).isNull();
	}

	@Test
	public void shouldFireVmRemovedWhenRemovingVm() {
		// given
		registry.addListener(listener);
		registry.addVMInstall(vm);
		// when
		registry.removeVMInstall(vm);
		// then
		verify(listener).vmRemoved(vm);
	}

	@Test
	public void shouldNotFireVmRemovedWhenRemovingNonExistingVm() {
		// given
		registry.addListener(listener);
		// when remove inexistent
		registry.removeVMInstall(vm);
		// then
		verify(listener, never()).vmRemoved(vm);
	}

	@Test
	public void shouldGetAllVmsAfterAddingThem() {
		// given
		registry.addVMInstall(vm);
		IVMInstall vm2 = mockVMInstall("other-id", null);
		registry.addVMInstall(vm2);
		IVMInstall vm3 = mockVMInstall("even-different-id", null);
		registry.addVMInstall(vm3);
		// when
		IVMInstall[] vms = registry.getVMs();
		// then
		assertThat(vms).contains(vm, vm2, vm3);
	}
	
	private IVMInstall mockVMInstall(String id, File installLocation) {
		IVMInstall vm = mock(IVMInstall.class);
		doReturn(id).when(vm).getId();
		doReturn(installLocation).when(vm).getInstallLocation();
		return vm;
	}

}
