/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.publishing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Map;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.server.model.internal.publishing.DeployableDelta;
import org.jboss.tools.rsp.server.model.internal.publishing.DeploymentAssemblyFile;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta;
import org.jboss.tools.rsp.server.spi.servertype.IDeployableResourceDelta.DELTA_TYPE;
import org.junit.Test;

import com.google.gson.Gson;

public class DeployableDeltaTest {

	private static final Path DEPLOYABLE_PATH = Paths.get("tmp", "batcave");
	private static final Path BATMAN = Paths.get("batman");
	private static final Path CAPE = Paths.get("cape");
	private static final Path BATMANS_CAPE = Paths.get(BATMAN.toString(), "cape");
	private static final Path BATMAN_FULL = DEPLOYABLE_PATH.resolve(BATMAN);
	private static final Path BATMANS_CAPE_FULL = DEPLOYABLE_PATH.resolve(BATMANS_CAPE);

	private DeployableReference deployable = new DeployableReference(null, DEPLOYABLE_PATH.toString());
	private DeployableDelta delta = new DeployableDelta(deployable);

	@Test
	public void shouldRegisterReference() {
		DeployableReference reference = delta.getReference();
		assertThat(reference).isEqualTo(this.deployable);
	}

	private void assertDeltaTypeForPath(DeployableDelta delta, Path p, DELTA_TYPE type) {
		Map<Path, IDeployableResourceDelta> map = delta.getResourceDeltaMap();
		assertTrue(map.containsKey(p));
		IDeployableResourceDelta rDelta = map.get(p);
		assertEquals(rDelta.getDeltaType(), type);
	}

	@Test
	public void shouldRegisterNewFolder() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldRegisterNewFolderWithAssembly1() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldRegisterNewFolderWithAssembly2() {
		String json = "{\"mappings\": [{\"source-path\": \"" + BATMAN + "\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertDeltaTypeForPath(delta, CAPE, DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldRegisterNewFolderDifDeployPathWithAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"" + BATMAN + "\",\"deploy-path\": \"/extra/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertDeltaTypeForPath(delta, Paths.get("extra", "cape"), DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldRegisterNewNestedFolder() {
		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.CREATED);
	}


	@Test
	public void shouldRegisterNewNestedFolderAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.CREATED);
	}
	
	@Test
	public void shouldRegisterRemovedFolder() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}
	
	@Test
	public void shouldRegisterRemovedFolderAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}
	
	@Test
	public void shouldRegisterRemovedNestedFolder() {
		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.DELETED);
	}

	@Test
	public void shouldRegisterRemovedNestedFolderAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.DELETED);
	}
	
	@Test
	public void shouldRegisterModifiedFolder() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}

	@Test
	public void shouldRegisterModifiedFolderAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}
	
	@Test
	public void shouldRegisterModifiedNestedFolder() {
		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.MODIFIED);
	}

	@Test
	public void shouldRegisterModifiedNestedFolderAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.MODIFIED);
	}
	@Test
	public void shouldRegister2ndChange() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		assertEquals(delta.getResourceDeltaMap().size(), 2);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldRegister2ndChangeAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMANS_CAPE_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertEquals(delta.getResourceDeltaMap().size(), 2);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
		assertDeltaTypeForPath(delta, BATMANS_CAPE, DELTA_TYPE.CREATED);
	}
	@Test
	public void shouldHaveNothingIfCreatedFollowedByDeleted() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		assertEquals(delta.getResourceDeltaMap().size(), 0);
	}

	@Test
	public void shouldHaveNothingIfCreatedFollowedByDeletedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		assertEquals(delta.getResourceDeltaMap().size(), 0);
	}
	@Test
	public void shouldHaveDeletedIfModifiedFollowedByDeleted() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}

	@Test
	public void shouldHaveDeletedIfModifiedFollowedByDeletedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}
	@Test
	public void shouldHaveModifiedIfDeletedFollowedByCreated() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}

	@Test
	public void shouldHaveModifiedIfDeletedFollowedByCreatedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}
	@Test
	public void shouldHaveModifiedIfDeletedFollowedByModified() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}

	@Test
	public void shouldHaveModifiedIfDeletedFollowedByModifiedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.MODIFIED);
	}
	@Test
	public void shouldHaveDeletedIfDeletedFollowedByDeleted() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE));
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}

	@Test
	public void shouldHaveDeletedIfDeletedFollowedByDeletedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_DELETE), asObj);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.DELETED);
	}
	@Test
	public void shouldHaveCreatedIfCreatedFollowedByModified() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE));
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		// modify is swallowed given that it follows a create
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
	}

	@Test
	public void shouldHaveCreatedIfCreatedFollowedByModifiedAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"/\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_CREATE), asObj);
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		// modify is swallowed given that it follows a create
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
	}
	

	@Test
	public void shouldHaveCreatedIfCreatedFollowedByModifiedDifferentSourceAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"test5\",\"deploy-path\": \"out1\"}, {\"source-path\": \"test6\",\"deploy-path\": \"out1\"} ]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);
		Path test5 = DEPLOYABLE_PATH.resolve("test5");
		Path test6 = DEPLOYABLE_PATH.resolve("test6");
		Path test5File1 = test5.resolve("test.file");
		Path test6File1 = test6.resolve("test.file");
		Path testFile = Paths.get("out1").resolve("test.file");
		delta.registerChange(new FileWatcherEvent(test5File1, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		delta.registerChange(new FileWatcherEvent(test6File1, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		// modify is swallowed given that it follows a create
		Map<Path, IDeployableResourceDelta> map = delta.getResourceDeltaMap();
		assertTrue(map.containsKey(testFile));
		IDeployableResourceDelta rDelta = map.get(testFile);
		assertEquals(rDelta.getDeltaType(), DELTA_TYPE.MODIFIED);
		assertEquals(rDelta.getSourcePath(), test6File1);

	}
	
	@Test
	public void shouldHaveNothingIfCleared() {
		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY));
		delta.clear();
		assertThat(delta.getResourceDeltaMap()).isEmpty();
	}
	
	@Test
	public void shouldHaveNothingIfClearedWithAssembly() {
		String json = "{\"mappings\": [{\"source-path\": \"" + BATMANS_CAPE + "\",\"deploy-path\": \"/\"}]}";
		Map<String, Object> assemblyAsJson = new Gson().fromJson(json, Map.class);
		DeploymentAssemblyFile asObj = new DeploymentAssemblyFile(assemblyAsJson);

		delta.registerChange(new FileWatcherEvent(BATMAN_FULL, StandardWatchEventKinds.ENTRY_MODIFY), asObj);
		delta.clear();
		assertThat(delta.getResourceDeltaMap()).isEmpty();
	}


	@Test
	public void shouldRemainUnchangedIfUnknownChange() {
		// given
		delta.registerChange(
				new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), StandardWatchEventKinds.ENTRY_CREATE));
		// when
		delta.registerChange(new FileWatcherEvent(DEPLOYABLE_PATH.resolve(BATMAN), new WatchEvent.Kind<Path>() {

			@Override
			public String name() {
				return null;
			}

			@Override
			public Class<Path> type() {
				return Path.class;
			}
		}));
		// then
		assertEquals(delta.getResourceDeltaMap().size(), 1);
		assertDeltaTypeForPath(delta, BATMAN, DELTA_TYPE.CREATED);
	}
}
