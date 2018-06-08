/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.launching.test;

import org.jboss.tools.ssp.launching.VMInstallRegistry;
import org.junit.Before;
import org.junit.Test;

public class VMInstallRegistryTest {

	private VMInstallRegistry vmRegistry;

	@Before
	public void before() {
		this.vmRegistry = VMInstallRegistry.getDefault(); 
	}
	
	@Test(expected=RuntimeException.class)
	public void addNullVmThrows() {
		vmRegistry.addVMInstall(null);
	}

}
