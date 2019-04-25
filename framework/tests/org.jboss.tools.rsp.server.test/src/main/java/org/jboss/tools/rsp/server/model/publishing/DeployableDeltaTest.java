/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.publishing;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import org.assertj.core.data.MapEntry;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.server.model.internal.publishing.DeployableDelta;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;
import org.junit.Test;

public class DeployableDeltaTest {

	private static final Path DEPLOYABLE_PATH = Paths.get("tmp", "batcave");
	private static final Path BATMAN = Paths.get("batman");
	private static final Path BATMANS_CAPE = Paths.get(BATMAN.toString(), "cape");

	private DeployableReference deployable = new DeployableReference(null, DEPLOYABLE_PATH.toString());
	private DeployableDelta delta = new DeployableDelta(deployable);

	@Test
	public void shouldRegisterNewFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.CREATED));
	}
	
	@Test
	public void shouldRegisterNewNestedFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMANS_CAPE), StandardWatchEventKinds.ENTRY_CREATE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMANS_CAPE, IDeployableResourceDelta.CREATED));
	}

	@Test
	public void shouldRegisterRemovedFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.DELETED));
	}

	@Test
	public void shouldRegisterRemovedNestedFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMANS_CAPE), StandardWatchEventKinds.ENTRY_DELETE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMANS_CAPE, IDeployableResourceDelta.DELETED));
	}

	@Test
	public void shouldRegisterModifiedFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_MODIFY));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.MODIFIED));
	}

	@Test
	public void shouldRegisterModifiedNestedFolder() {
		// given
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMANS_CAPE), StandardWatchEventKinds.ENTRY_MODIFY));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMANS_CAPE, IDeployableResourceDelta.MODIFIED));
	}

	@Test
	public void shouldHaveNothingIfCreatedFollowedByDeleted() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// then
		// both events are dropped given that delete follows a create
		assertThat(delta.getResourceDeltaMap())
			.isEmpty();
	}

	@Test
	public void shouldHaveDeletedIfModifiedFollowedByDeleted() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_MODIFY));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.DELETED));
	}

	@Test
	public void shouldHaveModifiedIfDeletedFollowedByCreated() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.MODIFIED));
	}

	@Test
	public void shouldHaveModifiedIfDeletedFollowedByModified() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_MODIFY));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.MODIFIED));
	}

	@Test
	public void shouldHaveDeletedIfDeletedFollowedByDeleted() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_DELETE));
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.DELETED));
	}

	@Test
	public void shouldHaveCreatedIfCreatedFollowedByModified() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_MODIFY));
		// then
		// modify is swallowed given that it follows a create
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.CREATED));
	}

	@Test
	public void shouldHaveNothingIfCleared() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_MODIFY));
		// when
		delta.clear();
		// then
		assertThat(delta.getResourceDeltaMap())
			.isEmpty();
	}

	@Test
	public void shouldRemainUnchangedIfUnknownChange() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// when
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), new WatchEvent.Kind<Path>() {

					@Override
					public String name() {
						return null;
					}

					@Override
					public Class<Path> type() {
						return Path.class;
					}
				})
			);
		// then
		assertThat(delta.getResourceDeltaMap())
			.containsExactly(MapEntry.entry(BATMAN, IDeployableResourceDelta.CREATED));
	}
}
