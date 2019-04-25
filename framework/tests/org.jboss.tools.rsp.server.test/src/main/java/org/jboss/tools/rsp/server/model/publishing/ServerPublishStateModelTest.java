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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.model.internal.publishing.ServerPublishStateModel;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherEventListener;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ServerPublishStateModelTest {

	private TestableServerPublishStateModel model;
	private DeployableReference deployableFile;
	private DeployableReference deployableDirectory;
	private IFileWatcherService fileWatcher;
	private DeployableReference danglingDeployable;

	@Before
	public void before() throws IOException {		
		AbstractServerDelegate delegate = mock(AbstractServerDelegate.class);
		this.fileWatcher = mock(IFileWatcherService.class);
		this.model = new TestableServerPublishStateModel(delegate, fileWatcher);

		this.deployableFile = createDeployableReference(createTempFile("deployableFile").toString());
		this.deployableDirectory = createDeployableReference(createTempDirectory("deployableDirectory").toString());
		this.danglingDeployable = createDeployableReference(createTempFile("danglingDeployable").toString());
		model.initialize(Arrays.asList(deployableFile, deployableDirectory));
	}
	
	@Test
	public void shouldInitiallyHaveUnknownServerPublishState() {
		// given
		// when
		// then
		assertThat(model.getServerPublishState()).isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
	}
	
	@Test
	public void shouldInitiallyHaveUnknownDeployablePublishState() {
		// given
		// when
		// then
		assertThat(model.getDeployableState(deployableFile).getPublishState())
			.isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
		assertThat(model.getDeployableState(deployableDirectory).getPublishState())
			.isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN);
	}

	@Test
	public void shouldInitiallyHaveUnknownDeployableState() {
		// given
		// when
		// then
		assertThat(model.getDeployableState(deployableFile).getState())
			.isEqualTo(ServerManagementAPIConstants.STATE_UNKNOWN);
		assertThat(model.getDeployableState(deployableDirectory).getState())
			.isEqualTo(ServerManagementAPIConstants.STATE_UNKNOWN);
	}

	@Test
	public void shouldAddRecursiveFileWatcherForDeploymentDirectory() {
		// given
		ArgumentCaptor<Path> deployablePathCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(fileWatcher).addFileWatcherListener(
				deployablePathCaptor.capture(), 
				any(IFileWatcherEventListener.class), 
				anyBoolean());
		// when
		// then
		verify(fileWatcher).addFileWatcherListener(
				eq(Paths.get(deployableDirectory.getPath())), 
				any(IFileWatcherEventListener.class), 
				eq(true));
	}

	@Test
	public void shouldAddNonRecursiveFileWatcherForDeploymentFile() {
		// given
		ArgumentCaptor<Path> deployablePathCaptor = ArgumentCaptor.forClass(Path.class);
		doNothing().when(fileWatcher).addFileWatcherListener(
				deployablePathCaptor.capture(), 
				any(IFileWatcherEventListener.class), 
				anyBoolean());
		// when
		// then
		verify(fileWatcher).addFileWatcherListener(
				eq(Paths.get(deployableFile.getPath())), 
				eq(model), 
				eq(false));
	}

	@Test
	public void shouldRemoveFileWatchListenerWhenRemovingADeployment() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(ServerManagementAPIConstants.PUBLISH_STATE_FULL, deployableDirectory);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(fileWatcher).removeFileWatcherListener(Paths.get(deployableDirectory.getPath()), modelSpy);
	}

	@Test
	public void shouldContainDeployables() {
		// given
		// when
		boolean containsFile = model.contains(deployableFile);
		boolean containsDirectory = model.contains(deployableDirectory);
		boolean containsDangling = model.contains(danglingDeployable);
		// then
		assertThat(containsFile).isTrue();
		assertThat(containsDirectory).isTrue();
		assertThat(containsDangling).isFalse();
	}
	
	@Test
	public void shouldAddDeployable() {
		// given
		assertThat(model.contains(danglingDeployable)).isFalse();
		// when
		model.addDeployable(danglingDeployable);
		// then
		assertThat(model.contains(danglingDeployable)).isTrue();
	}

	@Test
	public void shouldReturnErrorStatusIfRemoveDeployableThatIsntAdded() {
		// given
		assertThat(model.contains(danglingDeployable)).isFalse();
		// when
		IStatus status = model.removeDeployable(danglingDeployable);
		// then
		assertThat(status.getCode()).isEqualTo(IStatus.ERROR);
	}

	@Test
	public void shouldRemoveDeployableIfItsInAddState() {
		// given
		ServerPublishStateModel modelSpy = 
				fakeDeployableStates(ServerManagementAPIConstants.PUBLISH_STATE_ADD, deployableDirectory);
		assertThat(modelSpy.contains(deployableDirectory)).isTrue();
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		assertThat(modelSpy.contains(deployableDirectory)).isFalse();
	}

	@Test
	public void shouldSetRemoveStateToDeploymentIfItsNotInAddState() {
		// given
		DeployableState deployableState = mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_FULL, deployableDirectory);
		ServerPublishStateModel modelSpy = fakeDeployableStates(new DeployableStateEntry(deployableState));
		assertThat(modelSpy.contains(deployableDirectory)).isTrue();
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		assertThat(modelSpy.contains(deployableDirectory)).isTrue();
		verify(deployableState).setPublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);
	}

	@Test
	public void shouldUpdateServerPublishStateWhenRemovingDeployment() {
		// given
		TestableServerPublishStateModel modelSpy = spy(model);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(modelSpy).setServerPublishState(anyInt(), anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToFullIfThereIsADeploymentInStateAdd() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),
				// will remain
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD))
		);

		// when
		modelSpy.removeDeployable(deployableFile);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_FULL),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToFullIfThereIsADeploymentInStateRemove() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),
				// will remain
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE))
		);
		// when
		modelSpy.removeDeployable(deployableFile);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_FULL),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToFullIfThereIsADeploymentInStateFull() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),
				// will remain
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_FULL))
		);
		// when
		modelSpy.removeDeployable(deployableFile);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_FULL),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToUnknownIfThereIsADeploymentInStateUnknown() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),				
				// will remain
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN))
		);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToIncrementalIfThereIsADeploymentInStateIncremental() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),				
				// will remain
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL))
		);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToUnknwonIfThereAreDeploymentsInStateUnknownAndIncremental() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD)),				
				// will remain
				new DeployableStateEntry(danglingDeployable, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL)),
				new DeployableStateEntry(deployableFile, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN))
		);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN),
				anyBoolean());
	}

	@Test
	public void shouldSetServerPublishingStateToNoneIfThereAreNoDeployments() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				// will be removed
				new DeployableStateEntry(deployableDirectory, mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD))
		);
		// when
		modelSpy.removeDeployable(deployableDirectory);
		// then
		verify(modelSpy).setServerPublishState(
				eq(ServerManagementAPIConstants.PUBLISH_STATE_NONE),
				anyBoolean());
	}

	@Test
	public void shouldReturnAllDeployableStates() {
		// given
		DeployableState danglingState = mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, danglingDeployable);
		DeployableState fileState = mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN, deployableFile);
		DeployableState directoryState = mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, deployableDirectory);
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				new DeployableStateEntry(danglingState),
				new DeployableStateEntry(fileState),
				new DeployableStateEntry(directoryState)
		);
		// when
		List<DeployableState> states = modelSpy.getDeployableStates();
		// then
		assertThat(states).containsOnly(
				danglingState,
				fileState,
				directoryState);
	}

	@Test
	public void shouldReturnSpecificDeployableState() {
		// given
		DeployableState fileState = mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN, deployableFile);
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				new DeployableStateEntry(mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, danglingDeployable)),
				new DeployableStateEntry(fileState),
				new DeployableStateEntry(mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, deployableDirectory))
		);
		// when
		DeployableState state = modelSpy.getDeployableState(deployableFile);
		// then
		assertThat(state.getPublishState()).isEqualTo(fileState.getPublishState());
		assertThat(state.getReference().getPath()).isEqualTo(fileState.getReference().getPath());
	}

	@Test
	public void shouldReturnNullStateForInexistantDeployable() {
		// given
		ServerPublishStateModel modelSpy = fakeDeployableStates(
				new DeployableStateEntry(mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, danglingDeployable)),
				new DeployableStateEntry(mockDeployableState(ServerManagementAPIConstants.PUBLISH_STATE_ADD, deployableDirectory))
		);
		// when
		DeployableState state = modelSpy.getDeployableState(deployableFile);
		// then
		assertThat(state).isNull();
	}

	@Test
	public void shouldSetStateToExistingDeployableState() {
		// given
		DeployableState directoryState = mockDeployableState(
				ServerManagementAPIConstants.PUBLISH_STATE_ADD, 
				ServerManagementAPIConstants.STATE_STARTED,
				deployableDirectory);
		TestableServerPublishStateModel modelSpy = fakeDeployableStates(
				new DeployableStateEntry(mockDeployableState(
						ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, 
						danglingDeployable)),
				new DeployableStateEntry(directoryState)
		);
		// when
		modelSpy.setDeployableState(deployableDirectory, ServerManagementAPIConstants.STATE_STOPPED);
		// then
		String key = model.getKey(deployableDirectory);
		DeployableState state = modelSpy.getStates().get(key);
		assertThat(state).isNotNull();
		assertThat(state.getReference()).isEqualTo(directoryState.getReference());
		assertThat(state.getPublishState()).isEqualTo(directoryState.getPublishState());
		assertThat(state.getState()).isEqualTo(ServerManagementAPIConstants.STATE_STOPPED);
	}

	@Test
	public void shouldNotInsertNewStateIfDoesntExistWhenSettingState() {
		// given
		TestableServerPublishStateModel modelSpy = fakeDeployableStates();
		assertThat(modelSpy.getStates()).isEmpty();
		// when
		modelSpy.setDeployableState(danglingDeployable, ServerManagementAPIConstants.STATE_STARTED);
		// then
		assertThat(modelSpy.getStates()).isEmpty();
	}

	@Test
	public void shouldNotInsertNewStateIfDoesntExistWhenSettingPublishState() {
		// given
		TestableServerPublishStateModel modelSpy = fakeDeployableStates();
		assertThat(modelSpy.getStates()).isEmpty();
		// when
		modelSpy.setDeployablePublishState(danglingDeployable, ServerManagementAPIConstants.PUBLISH_STATE_FULL);
		// then
		assertThat(modelSpy.getStates()).isEmpty();
	}

	@Test
	public void shouldSetPublishStateToExistingDeployableState() {
		// given
		DeployableState directoryState = mockDeployableState(
				ServerManagementAPIConstants.PUBLISH_STATE_ADD, 
				ServerManagementAPIConstants.STATE_STARTED,
				deployableDirectory);
		TestableServerPublishStateModel modelSpy = fakeDeployableStates(
				new DeployableStateEntry(mockDeployableState(
						ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL, 
						deployableFile)),
				new DeployableStateEntry(directoryState)
		);
		// when
		modelSpy.setDeployablePublishState(deployableDirectory, ServerManagementAPIConstants.PUBLISH_STATE_FULL);
		// then
		DeployableState state = modelSpy.getStates().get(model.getKey(deployableDirectory));
		assertThat(state).isNotNull();
		assertThat(state.getReference()).isEqualTo(directoryState.getReference());
		assertThat(state.getState()).isEqualTo(directoryState.getState());
		assertThat(state.getPublishState()).isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_FULL);
		
	}

	@Test
	public void shouldSetServerPublishState() {
		// given
		// when
		model.setServerPublishState(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE, false);
		// then
		assertThat(model.getServerPublishState()).isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_REMOVE);
		// when
		model.setServerPublishState(ServerManagementAPIConstants.PUBLISH_STATE_NONE, false);
		// then
		assertThat(model.getServerPublishState()).isEqualTo(ServerManagementAPIConstants.PUBLISH_STATE_NONE);
	}
	
	private ServerPublishStateModel fakeDeployableStates(int publishState, DeployableReference deployable) {
		DeployableState deployableState = mockDeployableState(publishState, deployable);
		return fakeDeployableStates(new DeployableStateEntry(deployableState));
	}

	private TestableServerPublishStateModel fakeDeployableStates(DeployableStateEntry... states) {
		Map<String, DeployableState> deployableStates = Arrays.asList(states).stream()
				.collect(Collectors.toMap(
						state -> state.getKey(),
						state -> state.getValue()));
		return fakeDeployableStates(deployableStates);
	}

	private TestableServerPublishStateModel fakeDeployableStates(Map<String, DeployableState> states) {
		TestableServerPublishStateModel modelSpy = spy(model);
		doReturn(states).when(modelSpy).getStates();
		return modelSpy;
	}

	private DeployableState mockDeployableState(int publishState) {
		return mockDeployableState(publishState, null);
	}

	private DeployableState mockDeployableState(int publishState, DeployableReference deployable) {
		return mockDeployableState(publishState, -1, deployable);
	}

	private DeployableState mockDeployableState(int publishState, int runState, DeployableReference deployable) {
		DeployableState state = mock(DeployableState.class);
		doReturn(publishState).when(state).getPublishState();
		doReturn(deployable).when(state).getReference();
		doReturn(runState).when(state).getState();
		return state;
	}

	private DeployableReference createDeployableReference(String path) {
		DeployableReference deployable = mock(DeployableReference.class);
		doReturn(path).when(deployable).getPath();
		return deployable;
	}

	private File createTempFile(String prefix) throws IOException {
		return File.createTempFile(prefix, null);
	}

	private File createTempDirectory(String prefix) throws IOException {
		File tmpFile = createTempFile(prefix);
		tmpFile.delete();
		tmpFile.mkdir();
		return tmpFile;
	}

	private class DeployableStateEntry implements Map.Entry<String, DeployableState>{

		private DeployableReference deployable;
		private DeployableState state;

		private DeployableStateEntry(DeployableState state) {
			this(state.getReference(), state);
		}

		private DeployableStateEntry(DeployableReference deployable, DeployableState state) {
			this.deployable = deployable;
			this.state = state;
		}

		@Override
		public String getKey() {
			return model.getKey(deployable);
		}

		@Override
		public DeployableState getValue() {
			return state;
		}

		@Override
		public DeployableState setValue(DeployableState state) {
			DeployableState oldValue = this.state;
			this.state = state;
			return oldValue;
		}
	}

	public class TestableServerPublishStateModel extends ServerPublishStateModel {

		public TestableServerPublishStateModel(AbstractServerDelegate delegate, IFileWatcherService fileWatcher) {
			super(delegate, fileWatcher);
		}

		@Override
		public Map<String, DeployableState> getStates() {
			return super.getStates();
		}

		@Override
		public String getKey(DeployableReference deployable) {
			return super.getKey(deployable);
		}

	}
}

