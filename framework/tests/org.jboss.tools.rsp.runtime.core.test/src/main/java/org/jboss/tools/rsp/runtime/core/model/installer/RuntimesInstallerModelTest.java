/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.RuntimesInstallerModel.RuntimeInstallerWrapper;
import org.junit.Test;

public class RuntimesInstallerModelTest {

	@Test
	public void shouldReturnNullIfTheresNoInstallerWithRequestedId() {
		RuntimesInstallerModel model = new TestableRuntimesInstallerModel(Collections.<RuntimeInstallerWrapper>emptyList());
		IRuntimeInstaller installer = model.getRuntimeInstaller("cheese");
		assertThat(installer).isNull();
	}

	@Test
	public void shouldReturnInstallerWithRequestedId() {
		// given
		IRuntimeInstaller cheddar = mock(IRuntimeInstaller.class);
		IRuntimeInstaller emmental = mock(IRuntimeInstaller.class);
		List<RuntimeInstallerWrapper> l = Arrays.asList(
				new RuntimeInstallerWrapper("emmental", emmental),
				new RuntimeInstallerWrapper("cheddar", cheddar));
		RuntimesInstallerModel model = new TestableRuntimesInstallerModel(l);
		IRuntimeInstaller installer = model.getRuntimeInstaller("cheddar");
		assertThat(installer).isEqualTo(cheddar);
	}

	public class TestableRuntimesInstallerModel extends RuntimesInstallerModel {
		private List<RuntimeInstallerWrapper> installers2;

		public TestableRuntimesInstallerModel(List<RuntimeInstallerWrapper>  installers) {
			this.installers2 = installers;
		}

		@Override
		public List<RuntimeInstallerWrapper> loadInstallers() {
			return installers2;
		}
	}
}

