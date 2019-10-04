/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jboss.tools.rsp.api.RSPServer;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.ServerManagementServerImpl;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.model.RemoteEventManager;
import org.jboss.tools.rsp.server.model.ServerModel;
import org.jboss.tools.rsp.server.spi.filewatcher.IFileWatcherService;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.util.DataLocationSysProp;
import org.jboss.tools.rsp.server.util.TestServerUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerActionTest {

	private static final String SERVER_FILENAME = "serverActionTest1";
	private static final String SERVERS_DIR = "ServerActionTest";
	private static final String SERVER_ID = "ServerActionTestServerId";
	private static final String SERVER_TYPE = "ServerActionTestWonka";

	private static final DataLocationSysProp dataLocation = new DataLocationSysProp();

	@BeforeClass
	public static void beforeClass() {
		dataLocation.backup().set("ServerActionTest");
	}

	@AfterClass
	public static void afterClass() {
		dataLocation.restore();
	}

	private ServerModel sm;
	private Path serversDir;
	private IServer server;
	private RSPServer rsp;
	
	private void customSetup(Function<IServer, IServerDelegate> delFunc) throws IOException {
		this.serversDir = Files.createTempDirectory(SERVERS_DIR);
		IServerManagementModel managementModel = createServerManagementModel(null);
		this.sm = TestServerUtils.createServerModel(
				SERVER_FILENAME, serversDir, 
				TestServerUtils.getServerWithoutDeployablesString(SERVER_ID, SERVER_TYPE),
				delFunc, SERVER_TYPE, 
				managementModel, null);
		when(managementModel.getServerModel()).thenReturn(sm);

		this.server = sm.getServer(SERVER_ID);
		this.rsp = new ServerManagementServerImpl(null, managementModel) {
			protected RemoteEventManager createRemoteEventManager() {
				return null;
			}
		};
	}

	@Test
	public void testListNullParam() throws Exception {
		customSetup(ServerActionTestDelegate::new);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(null);
		assertNotNull(ret);
		assertNotNull(ret.get());
		assertNotNull(ret.get().getStatus());
		assertFalse(ret.get().getStatus().isOK());
	}

	
	@Test
	public void testListMissingServer() throws Exception {
		customSetup(ServerActionTestDelegate::new);
		ServerHandle sh = new ServerHandle("missingName", null);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		assertNotNull(ret.get());
		assertNotNull(ret.get().getStatus());
		assertFalse(ret.get().getStatus().isOK());
	}

	
	@Test
	public void testListNullServerType() throws Exception {
		customSetup(ServerActionTestDelegate::new);
		ServerHandle sh = new ServerHandle(SERVER_ID, null);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		assertNotNull(ret.get());
		assertNotNull(ret.get().getStatus());
		assertFalse(ret.get().getStatus().isOK());
	}

	@Test
	public void testListServerTypeMissingId() throws Exception {
		customSetup(ServerActionTestDelegate::new); 
		ServerType st = new ServerType("Blah", null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		assertNotNull(ret.get());
		assertNotNull(ret.get().getStatus());
		assertFalse(ret.get().getStatus().isOK());
	}

	@Test
	public void testListServerTypeAndIdFound() throws Exception {
		customSetup(ServerActionTestDelegate::new); 
		ServerType st = new ServerType(SERVER_TYPE, null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		ListServerActionResponse resp = ret.get();
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertTrue(resp.getStatus().isOK());
		assertNotNull(resp.getWorkflows());
		assertTrue(resp.getWorkflows().size() == 0);
	}

	@Test
	public void testListException() throws Exception {
		customSetup(ServerActionTestDelegateWithException::new);
		ServerType st = new ServerType(SERVER_TYPE, null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		ListServerActionResponse resp = ret.get();
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertFalse(resp.getStatus().isOK());
	}

	@Test
	public void testListDelegateReplies() throws Exception {
		customSetup(ServerActionTestDelegate2::new); 
		ServerType st = new ServerType(SERVER_TYPE, null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		ListServerActionResponse resp = ret.get();
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertTrue(resp.getStatus().isOK());
		assertNotNull(resp.getWorkflows());
		assertTrue(resp.getWorkflows().size() == 0);
	}


	@Test
	public void testListDelegateOneAction() throws Exception {
		customSetup(ServerActionTestDelegateOneAction::new); 
		ServerType st = new ServerType(SERVER_TYPE, null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		ListServerActionResponse resp = ret.get();
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertTrue(resp.getStatus().isOK());
		assertNotNull(resp.getWorkflows());
		assertTrue(resp.getWorkflows().size() == 1);
		List<ServerActionWorkflow> list = resp.getWorkflows();
		assertNotNull(list);
		assertTrue(list.size() == 1);

		ServerActionWorkflow actionWrapper = list.get(0);
		assertTrue(actionWrapper.getActionId().equals(ServerActionTestDelegateOneAction.ACTION_ID));
		
		WorkflowResponse action1 = actionWrapper.getActionWorkflow();
		assertNotNull(action1.getItems());
		assertTrue(action1.getItems().size() == 1);
		WorkflowResponseItem i1 = action1.getItems().get(0);
		assertNotNull(i1);
		assertNotNull(i1.getPrompt());
		assertNotNull(i1.getPrompt().getResponseType());
	}


	@Test
	public void testListDelegateOneActionException() throws Exception {
		customSetup(ServerActionTestDelegateListOkExecuteException::new); 
		ServerType st = new ServerType(SERVER_TYPE, null, null);
		ServerHandle sh = new ServerHandle(SERVER_ID, st);
		CompletableFuture<ListServerActionResponse> ret = 
				rsp.listServerActions(sh);
		assertNotNull(ret);
		ListServerActionResponse resp = ret.get();
		assertNotNull(resp);
		assertNotNull(resp.getStatus());
		assertTrue(resp.getStatus().isOK());
		assertNotNull(resp.getWorkflows());
		
		List<ServerActionWorkflow> list = resp.getWorkflows();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		ServerActionWorkflow actionWrapper = list.get(0);
		assertTrue(actionWrapper.getActionId().equals(ServerActionTestDelegateOneAction.ACTION_ID));
		WorkflowResponse action1 = actionWrapper.getActionWorkflow();
		assertTrue(resp.getWorkflows().size() == 1);
		assertNotNull(action1.getItems());
		assertTrue(action1.getItems().size() == 1);
		WorkflowResponseItem i1 = action1.getItems().get(0);
		assertNotNull(i1);
		assertNotNull(i1.getPrompt());
		assertNotNull(i1.getPrompt().getResponseType());
		
		ServerActionRequest serverActionRequest = new ServerActionRequest();
		serverActionRequest.setActionId(ServerActionTestDelegateOneAction.ACTION_ID);
		serverActionRequest.setData(new HashMap<>());
		serverActionRequest.setServerId(sh.getId());
		CompletableFuture<WorkflowResponse> actionResult = rsp.executeServerAction(serverActionRequest);
		assertNotNull(actionResult);
		WorkflowResponse workResp = actionResult.get();
		assertNotNull(workResp);
		assertNotNull(workResp.getStatus());
		assertFalse(workResp.getStatus().isOK());
		
	}

	
	private class ServerActionTestDelegate extends AbstractServerDelegate {
		public ServerActionTestDelegate(IServer server) {
			super(server);
		}
	}

	private class ServerActionTestDelegateOneAction extends AbstractServerDelegate {
		public static final String SINGLE_PROMPT_ID  = "action1.action.id";
		public static final String ACTION_ID = "test.action1";
		public static final String ACTION_LABEL = "action label";
		public static final String SINGLE_PROMPT_LABEL = "Prompt Label";
		public ServerActionTestDelegateOneAction(IServer server) {
			super(server);
		}
		public ListServerActionResponse listServerActions() {
			ListServerActionResponse ret = new ListServerActionResponse();
			ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
			List<ServerActionWorkflow> workflows = new ArrayList<>();
			ret.setWorkflows(workflows);
			
			// Create one action
			WorkflowResponse oneWorkflow = new WorkflowResponse();
			List<WorkflowResponseItem> itemList = new ArrayList<>();
			WorkflowResponseItem item1 = new WorkflowResponseItem();
			item1.setId(SINGLE_PROMPT_ID);
			item1.setLabel(SINGLE_PROMPT_LABEL);
			WorkflowPromptDetails pDetails = new WorkflowPromptDetails();
			pDetails.setResponseSecret(false);
			pDetails.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_STRING);
			item1.setPrompt(pDetails);
			itemList.add(item1);
			oneWorkflow.setItems(itemList);
			
			ServerActionWorkflow actionWorkflow = new ServerActionWorkflow(
					ACTION_ID, ACTION_LABEL, oneWorkflow);
			workflows.add(actionWorkflow);
			ret.setWorkflows(workflows);
			return ret;
		}
	}

	private class ServerActionTestDelegate2  extends AbstractServerDelegate {
		public ServerActionTestDelegate2(IServer server) {
			super(server);
		}
		public ListServerActionResponse listServerActions() {
			ListServerActionResponse ret = new ListServerActionResponse();
			ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
			ret.setWorkflows(new ArrayList<>());
			return ret;
		}
	}
	private class ServerActionTestDelegateWithException  extends AbstractServerDelegate {
		public ServerActionTestDelegateWithException(IServer server) {
			super(server);
		}
		public ListServerActionResponse listServerActions() {
			throw new NullPointerException();
		}
	}

	private class ServerActionTestDelegateListOkExecuteException  extends ServerActionTestDelegateOneAction {
		public ServerActionTestDelegateListOkExecuteException(IServer server) {
			super(server);
		}
		@Override
		public WorkflowResponse executeServerAction(ServerActionRequest req) {
			throw new NullPointerException();
		}
	}


	private IServerManagementModel createServerManagementModel(IFileWatcherService service) {
		IServerManagementModel model = mock(IServerManagementModel.class);
		if( service != null ) 
			service.start();
		
		when(model.getFileWatcherService()).thenReturn(service);
		return model; 
	}
}
