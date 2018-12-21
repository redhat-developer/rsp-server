/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.runtime.core.model.installer.RuntimesInstallerModel.RuntimeInstallerWrapper;
import org.junit.Before;
import org.junit.Test;

public class RuntimesInstallerModelTest {

	private RuntimesInstallerModel model;

	@Before
	public void before() {
		this.model = spy(new TestableRuntimesInstallerModel());
	}

	@Test
	public void shouldReturnNullIfTheresNoInstallerWithRequestedId() {
		// given
		doReturn(Collections.<RuntimeInstallerWrapper>emptyList()).when(model).loadInstallers();
		// when
		IRuntimeInstaller installer = model.getRuntimeInstaller("cheese");
		// then
		assertThat(installer).isNull();
	}

	@Test
	public void shouldReturnInstallerWithRequestedId() {
		// given
		IRuntimeInstaller cheddar = mock(IRuntimeInstaller.class);
		IRuntimeInstaller emmental = mock(IRuntimeInstaller.class);
		doReturn(Arrays.asList(
				new RuntimeInstallerWrapper("emmental", emmental),
				new RuntimeInstallerWrapper("cheddar", cheddar)))
		.when(model).loadInstallers();
		// when
		IRuntimeInstaller installer = model.getRuntimeInstaller("cheddar");
		// then
		assertThat(installer).isEqualTo(cheddar);
	}

	public class TestableRuntimesInstallerModel extends RuntimesInstallerModel {

		public TestableRuntimesInstallerModel() {
		}

		@Override
		protected List<RuntimeInstallerWrapper> loadInstallers() {
			return super.loadInstallers();
		}
	}
}

