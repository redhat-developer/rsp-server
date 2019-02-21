/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.runtime.core.model.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesModel;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DownloadRuntimesModelTest {

	private final DownloadRuntime runtime11 = mockDownloadRuntime("1-1");
	private final DownloadRuntime runtime12 = mockDownloadRuntime("1-2");
	private final DownloadRuntime runtime13 = mockDownloadRuntime("1-3");
	private final DownloadRuntime runtime21 = mockDownloadRuntime("2-1");
	private final DownloadRuntime runtime31 = mockDownloadRuntime("3-1");
	private final DownloadRuntime runtime32 = mockDownloadRuntime("3-2");
	private final DownloadRuntime runtime41 = mockDownloadRuntime("4-1");

	private final IDownloadRuntimesProvider provider1 = mockDownloadRuntimeProvider("1", 
			runtime11, runtime12, runtime13);
	private final IDownloadRuntimesProvider provider2 = mockDownloadRuntimeProvider("2", 
			runtime21);
	private final IDownloadRuntimesProvider provider3 = mockDownloadRuntimeProvider("3", 
			runtime31, runtime32);
	private final IDownloadRuntimesProvider provider4 = mockDownloadRuntimeProvider("4", 
			runtime41);
	
	private IDownloadRuntimesModel model;
	private IProgressMonitor monitor;

	@Before
	public void before() {
		this.monitor = mock(IProgressMonitor.class);
		model = RuntimeCoreActivator.createDownloadRuntimesModel();
		model.addDownloadRuntimeProvider(provider1);
		model.addDownloadRuntimeProvider(provider2);
		model.addDownloadRuntimeProvider(provider3);
	}

	@Test
	public void shouldAddProviders() {
		// given
		assertThat(model.getDownloadRuntimeProviders()).doesNotContain(provider4);
		// when
		model.addDownloadRuntimeProvider(provider4);
		// then
		assertThat(model.getDownloadRuntimeProviders()).contains(provider4);
	}

	@Test
	public void shouldRemoveProviders() {
		// given
		assertThat(model.getDownloadRuntimeProviders()).contains(provider3);
		// when
		model.removeDownloadRuntimeProvider(provider3);
		// then
		assertThat(model.getDownloadRuntimeProviders()).doesNotContain(provider3);
	}

	@Test
	public void shouldQueryAllRuntimeProvidersThatWereAdded() {
		// given
		// when
		model.getOrLoadDownloadRuntimes(monitor);
		// then
		verify(provider1).getDownloadableRuntimes(any(IProgressMonitor.class));
		verify(provider2).getDownloadableRuntimes(any(IProgressMonitor.class));
		verify(provider3).getDownloadableRuntimes(any(IProgressMonitor.class));
		verify(provider4, never()).getDownloadableRuntimes(any(IProgressMonitor.class));
	}

	@Test
	public void shouldReturnAllRuntimes() {
		// given
		// when
		Map<String, DownloadRuntime> runtimes = model.getOrLoadDownloadRuntimes(monitor);
		// then
		assertThat(runtimes).containsOnly(
				MapEntry.entry(runtime11.getId(), runtime11),
				MapEntry.entry(runtime12.getId(), runtime12),
				MapEntry.entry(runtime13.getId(), runtime13),
				MapEntry.entry(runtime21.getId(), runtime21),
				MapEntry.entry(runtime31.getId(), runtime31),
				MapEntry.entry(runtime32.getId(), runtime32)
		);
	}

	@Test
	public void shouldReturnNullIfFindProviderForRuntimeWithoutLoadingBeforehand() {
		// given
		// when
		IDownloadRuntimesProvider provider = model.findProviderForRuntime(runtime13.getId());
		// then
		assertThat(provider).isNull();
	}

	@Test
	public void shouldReturnNullIfFindProviderForRuntimeForUnknownId() {
		// given
		model.getOrLoadDownloadRuntimes(monitor);
		// when
		IDownloadRuntimesProvider provider = model.findProviderForRuntime("gorgonzola");
		// then
		assertThat(provider).isNull();
	}

	@Test
	public void shouldReturnNullIfFindProviderForRuntimeForNullId() {
		// given
		model.getOrLoadDownloadRuntimes(monitor);
		// when
		IDownloadRuntimesProvider provider = model.findProviderForRuntime(null);
		// then
		assertThat(provider).isNull();
	}

	@Test
	public void shouldReturnProviderForRuntime() {
		// given
		// when
		IDownloadRuntimesProvider provider = model.findProviderForRuntime(runtime21.getId(), monitor);
		// then
		assertThat(provider).isNotNull().isEqualTo(provider2);
	}

	@Test
	public void shouldDownloadRuntimeForId() {
		// given
		// when
		DownloadRuntime runtime = model.findDownloadRuntime(runtime21.getId(), monitor);
		// then
		assertThat(runtime).isNotNull().isEqualTo(runtime21);
	}

	@Test
	public void shouldDownloadRuntimeForAlternativeId() {
		// given
		doReturn("cheddar").when(runtime13).getProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID);
		// when
		DownloadRuntime runtime = model.findDownloadRuntime("cheddar", monitor);
		// then
		assertThat(runtime).isNotNull().isEqualTo(runtime13);
	}

	@Ignore("DownloadRuntime#getProperty returns String, not String[]. "
			+ "Either the return type needs to be corrected "
			+ "or the check in DownloadRuntimeModel#findDownloadRuntime against String[] needs to be removed")
	@Test
	public void shouldDownloadRuntimeForAlternativeIds() {
		// given
		doReturn(new String[] { "gorgonzola", "cheddar", "emmental" }).when(runtime13).getProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID);
		// when
		DownloadRuntime runtime = model.findDownloadRuntime("cheddar", monitor);
		// then
		assertThat(runtime).isNotNull().isEqualTo(runtime13);
	}

	private DownloadRuntime mockDownloadRuntime(String id) {
		DownloadRuntime runtime = mock(DownloadRuntime.class);
		doReturn(id).when(runtime).getId();
		return runtime;
	}

	private IDownloadRuntimesProvider mockDownloadRuntimeProvider(String id, DownloadRuntime... downloadRuntimes) {
		IDownloadRuntimesProvider provider = mock(IDownloadRuntimesProvider.class);
		doReturn(id).when(provider).getId();
		doReturn(downloadRuntimes).when(provider).getDownloadableRuntimes(any(IProgressMonitor.class));
		return provider;
	}
}

