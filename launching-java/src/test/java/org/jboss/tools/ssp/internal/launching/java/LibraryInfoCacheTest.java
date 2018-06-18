/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.internal.launching.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import org.jboss.tools.ssp.eclipse.jdt.internal.launching.LibraryInfo;
import org.junit.Before;
import org.junit.Test;

public class LibraryInfoCacheTest {

	private LibraryInfoCache cache;
	private File javaInstallDirectory;
	private String javaInstallPath;

	@Before
	public void before() throws IOException {
		this.cache = LibraryInfoCache.getDefault();
		this.javaInstallDirectory = File.createTempFile(LibraryInfoCache.class.getSimpleName(), null);
		this.javaInstallPath = javaInstallDirectory.getAbsolutePath();
	}

	@Test
	public void returnsNullIfGetNullInstallLocation() {
		// given
		// when
		LibraryInfo info = cache.getLibraryInfo(null);
		// then
		assertThat(info).isNull();
	}

	@Test
	public void returnsCachedEntry() {
		// given
		String javaInstallPath = "/";
		LibraryInfo info = mockLibraryInfo();
		cache.putLibraryInfo(javaInstallPath, info);
		// when
		LibraryInfo cached = cache.getLibraryInfo(javaInstallPath);
		// then
		assertThat(cached).isEqualTo(info);
	}

	@Test
	public void returnsNullIfGetUsesMissingKey() {
		// given
		LibraryInfo info = mockLibraryInfo();
		cache.putLibraryInfo("/", info);
		// when
		LibraryInfo cached = cache.getLibraryInfo("/home/");
		// then
		assertThat(cached).isNull();
	}

	@Test
	public void putNullRemovesExisting() {
		// given
		String javaInstallPath = "/";
		LibraryInfo info = mockLibraryInfo();
		cache.putLibraryInfo(javaInstallPath, info);
		LibraryInfo cached = cache.getLibraryInfo(javaInstallPath);
		assertThat(cached).isEqualTo(info);
		// when
		cache.putLibraryInfo(javaInstallPath, null);
		// then
		cached = cache.getLibraryInfo(javaInstallPath);
		assertThat(cached).isNull();
	}

	@Test
	public void putReplacesExisting() {
		// given
		String javaInstallPath = "/";
		LibraryInfo info = mockLibraryInfo();
		cache.putLibraryInfo(javaInstallPath, info);
		LibraryInfo cached = cache.getLibraryInfo(javaInstallPath);
		LibraryInfo info2 = mockLibraryInfo();
		// when
		cache.putLibraryInfo(javaInstallPath, info2);
		// then
		cached = cache.getLibraryInfo(javaInstallPath);
		assertThat(cached).isEqualTo(info2);
	}

	@Test
	public void timestampChangedWontTrackInexistantFiles() {
		// given
		// when
		boolean changed = cache.timeStampChanged("/smurfs");
		// then
		assertThat(changed).isFalse();
	}

	@Test
	public void timestampChangedReturnsTrueForNewlyCheckedFile() {
		// given
		// when
		boolean changed = cache.timeStampChanged(javaInstallDirectory.getAbsolutePath());
		// then
		assertThat(changed).isTrue();
	}

	@Test
	public void timestampChangedReturnsFalseWhenInstallLocationWasPutAgain() {
		// given
		boolean changed = cache.timeStampChanged(javaInstallPath);
		assertThat(changed).isTrue();
		// when
		cache.putLibraryInfo(javaInstallPath, mockLibraryInfo());
		// then
		changed = cache.timeStampChanged(javaInstallPath);
		assertThat(changed).isFalse();
	}

	@Test
	public void timestampChangedReturnsTrueWhenInstallLocationWasPutAgainAndWasModified() throws InterruptedException {
		// given
		boolean changed = cache.timeStampChanged(javaInstallPath);
		assertThat(changed).isTrue();
		// when
		cache.putLibraryInfo(javaInstallPath, mockLibraryInfo());
		javaInstallDirectory.setLastModified(javaInstallDirectory.lastModified() + 1000); // touch file
		// then
		changed = cache.timeStampChanged(javaInstallPath);
		assertThat(changed).isTrue();
	}

	private LibraryInfo mockLibraryInfo() {
		return mock(LibraryInfo.class);
	}

}