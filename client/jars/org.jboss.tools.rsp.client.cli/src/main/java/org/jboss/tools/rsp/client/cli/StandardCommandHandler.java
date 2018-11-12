/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ModifyDeployableRequest;
import org.jboss.tools.rsp.api.dao.PublishServerRequest;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class StandardCommandHandler implements InputHandler {

	private static final String LIST_PATHS = "list paths";
	private static final String ADD_PATH = "add path ";
	private static final String REMOVE_PATH = "remove path ";
	private static final String SEARCH_PATH = "search path ";

	private static final String LIST_SERVERS = "list servers";
	private static final String LIST_SERVER_TYPES = "list servertypes";
	private static final String LIST_SERVERTYPE_ATTRIBUTES_REQUIRED = "list attributes required";
	private static final String LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL = "list attributes optional";

	private static final String ADD_SERVER = "add server";
	private static final String REMOVE_SERVER = "remove server ";

	private static final String START_SERVER = "start server ";
	private static final String STOP_SERVER = "stop server ";

	private static final String LAUNCH_COMMAND = "launch command";
	private static final String LAUNCH_LOCAL = "launch local";

	
	/*
	 * Publishing
	 */
	private static final String LIST_DEPLOYMENTS = "list deployments";
	private static final String ADD_DEPLOYMENT = "add deployment";
	private static final String REMOVE_DEPLOYMENT = "remove deployment";
	private static final String PUBLISH = "publish server";
	
	
	private static final String EXIT = "exit";
	private static final String SHUTDOWN = "shutdown";

	private static final String[] CMD_ARR = new String[] {
			LIST_PATHS, ADD_PATH, REMOVE_PATH, SEARCH_PATH, 
			LIST_SERVERS, LIST_SERVER_TYPES, 
			LIST_SERVERTYPE_ATTRIBUTES_REQUIRED, LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL,
			ADD_SERVER, REMOVE_SERVER, 
			LAUNCH_COMMAND, LAUNCH_LOCAL, START_SERVER, STOP_SERVER,
			LIST_DEPLOYMENTS, ADD_DEPLOYMENT, REMOVE_DEPLOYMENT, PUBLISH,
			EXIT, SHUTDOWN
	};

	private ServerManagementClientLauncher launcher;
	private InputProvider provider;
	private PromptAssistant assistant;
	public StandardCommandHandler(ServerManagementClientLauncher launcher, InputProvider provider) {
		this.launcher = launcher;
		this.provider = provider;
		this.assistant = new PromptAssistant(launcher, provider);
	}

	@Override
	public void handleInput(String line) throws Exception {
		processCommand(line);
	}

	private void processCommand(String s) throws Exception {
		if (s == null
				|| s.isEmpty()) {
			return;
		}

		if (s.trim().equals(SHUTDOWN)) {
			launcher.getServerProxy().shutdown();
			System.out.println("The server has been shutdown");
			System.exit(0);
		} else if (s.startsWith(ADD_PATH)) {
			String suffix = s.substring(ADD_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().addDiscoveryPath(dp);
		} else if (s.startsWith(REMOVE_PATH)) {
			String suffix = s.substring(REMOVE_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().removeDiscoveryPath(dp);
		} else if (s.trim().equals(LIST_PATHS)) {
			List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
			System.out.println("Paths:");
			if (list != null) {
				for( DiscoveryPath dp : list) {
					System.out.println("   " + dp.getFilepath());
				}
			}
		} else if (s.startsWith(SEARCH_PATH)) {
			String suffix = s.substring(SEARCH_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			List<ServerBean> beans = launcher.getServerProxy().findServerBeans(dp).get();
			System.out.println("Beans:");
			if (beans != null) {
				for( ServerBean b : beans) {
					System.out.println("   " + b.toString());
				}
			}

		} else if (s.startsWith(START_SERVER) || s.startsWith(START_SERVER.substring(0, START_SERVER.length()-2))) {
			runStartServer(s);
		} else if (s.equals(LAUNCH_COMMAND)) {
			LaunchParameters getLaunchReq = getLaunchCommandRequest();
			if( getLaunchReq != null )
				printLocalLaunchCommandDetails(getLaunchReq);
		} else if (s.equals(LAUNCH_LOCAL)) {
			runLocalLaunchScenario();
		} else if (s.startsWith(STOP_SERVER)) {
			runStopServer(s);
		} else if (s.trim().equals(LIST_SERVER_TYPES)) {
			List<ServerType> handles = launcher.getServerProxy().getServerTypes().get();
			System.out.println(handles.size() + " servers found:");
			for( ServerType sh : handles ) {
				System.out.println("   " + sh.getId() + ": " + sh.getVisibleName());
			}
						
		} else if (s.trim().equals(LIST_SERVERTYPE_ATTRIBUTES_REQUIRED)) {
			ServerType st = assistant.chooseServerType();
			if (st != null) {
				Attributes attr = launcher.getServerProxy().getRequiredAttributes(st).get();
				printAttr(attr);
			}
		} else if (s.trim().equals(LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL)) {
			ServerType st = assistant.chooseServerType();
			if( st != null ) {
				Attributes attr = launcher.getServerProxy().getOptionalAttributes(st).get();
				printAttr(attr);
			}
		} else if (s.trim().equals(LIST_SERVERS)) {
			List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
			System.out.println(handles.size() + " servers found:");
			for( ServerHandle sh : handles ) {
				System.out.println("   " + sh.getType().getId() + ":" + sh.getId());
			}
		} else if (s.trim().startsWith(REMOVE_SERVER)) {
			String suffix = s.substring(REMOVE_SERVER.length());
			ServerHandle sh = findServer(suffix.trim());
			if (sh != null)
				launcher.getServerProxy().deleteServer(sh);
			else
				System.out.println("Server not found: " + suffix.trim());
		} else if (s.trim().equals(ADD_SERVER)) {
			runAddServer();
		} else if (s.trim().equals(LIST_DEPLOYMENTS)) {
			runListDeployment();
		} else if (s.trim().equals(ADD_DEPLOYMENT)) {
			runAddDeployment();
		} else if (s.trim().equals(REMOVE_DEPLOYMENT)) {
			runRemoveDeployment();
		} else if (s.trim().equals(PUBLISH)) {
			runPublish();
		} else if (s.trim().equals(EXIT)) {
			launcher.closeConnection();
			System.exit(0);
		} else {
			showCommands();
		}
	}

	private void runStopServer(String s) throws Exception {
		String suffix = s.substring(STOP_SERVER.length()).trim();
		String trimmed = suffix.trim();
		if( trimmed.length() == 0 ) {
			System.out.println("Syntax: stop server servername [boolean:force]");
		} else {
			String[] split = trimmed.split(" ");
			boolean force = false;
			if( split.length == 2 ) {
				force = Boolean.parseBoolean(split[1]);
			}
			StopServerAttributes ssa = new StopServerAttributes(split[0], force);
			Status stat = launcher.getServerProxy().stopServerAsync(ssa).get();
			System.out.println(stat.toString());
		}
	}
	
	private void runStartServer(String s) throws Exception {
		String suffix = s.substring(START_SERVER.trim().length()).trim();
		String serverId = suffix;
		ServerHandle selected = null;
		if( serverId.isEmpty()) {
			selected = assistant.selectServer();
			if( selected != null ) {
				serverId = selected.getId();
			}
		}
		
		if( serverId == null || serverId.isEmpty()) {
			System.out.println("No server chosen.");
			return;
		}
		
		if( selected == null ) {
			selected = findServer(serverId);
		}
		if (selected == null) {
			System.out.println("Server " + serverId + " not found.");
		} else {
			String mode = assistant.selectLaunchMode(selected.getType());
			ServerAttributes sa = new ServerAttributes(selected.getType().getId(), selected.getId(), new HashMap<String,Object>());
			LaunchParameters params = new LaunchParameters(sa, mode);
			StartServerResponse stat = launcher.getServerProxy().startServerAsync(params).get();
			System.out.println(stat.getStatus().toString());
		}
	}
	
	
	private void runPublish() {
		try {
			ServerHandle server = assistant.selectServer();
			if( server != null ) {
				int publishType = assistant.selectPublishType();
				if( publishType != -1 ) {
					Status stat = launcher.getServerProxy().publish(new PublishServerRequest(server, publishType)).get();
					System.out.println(stat.toString());
				}
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}

	private void runRemoveDeployment() {
		try {
			ServerHandle server = assistant.selectServer();
			if( server != null ) {
				DeployableReference ref = assistant.chooseDeployment(server);
				if( ref != null ) {
					ModifyDeployableRequest req = new ModifyDeployableRequest(server, ref);
					Status ret = launcher.getServerProxy().removeDeployable(req).get();
					System.out.println(ret.toString());
				}
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}


	
	private void runListDeployment() {
		try {
			ServerHandle server = assistant.selectServer();
			if( server != null ) {
				List<DeployableState> deployables = launcher.getServerProxy().getDeployables(server).get();
				System.out.println(deployables.size() + " deployments found:");
				int c = 1;
				for( DeployableState ds : deployables ) {
					int pubState = ds.getPublishState();
					String pubStateString = null;
					if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_ADD) pubStateString = "[add]";
					else if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_FULL) pubStateString = "[full]";
					else if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL) pubStateString = "[inc]";
					else if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_NONE) pubStateString = "[none]";
					else if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_REMOVE) pubStateString = "[remove]";
					else if( pubState == ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN) pubStateString = "[unknown]";
					System.out.println(c++ + ") " + ds.getReference().getLabel() + " " + pubStateString); // TODO add run state?
				}
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}
	private void runAddDeployment() {
		try {
			ServerHandle server = assistant.selectServer();
			if( server != null ) {
				System.out.println("Please enter a filesystem path of your deployment:");
				String filePath = assistant.nextLine().trim();
				if( new File(filePath).exists()) {
					DeployableReference ref = new DeployableReference(filePath, filePath);
					ModifyDeployableRequest req = new ModifyDeployableRequest(server, ref);
					Status ret = launcher.getServerProxy().addDeployable(req).get();
					System.out.println(ret.toString());
				}
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}

	private void printAttr(Attributes attr) {
		Map<String, Attribute> map = attr.getAttributes();
		Iterator<String> kit = map.keySet().iterator();
		while (kit.hasNext()) {
			String key = kit.next();
			Attribute val = map.get(key);
			System.out.println(key);
			System.out.println("    type=" + val.getType());
			System.out.println("    desc=" + val.getDescription());
			System.out.println("    defaultVal=" + val.getDefaultVal());
		}
	}

	private void runLocalLaunchScenario() throws Exception {
		LaunchParameters getLaunchReq = getLaunchCommandRequest();
		printLocalLaunchCommandDetails(getLaunchReq);
		
		// This CLI will not actually launch this server locally. 
		// We are just stubbing this out for now. 
		System.out.println("We wont actually run this from the client here in this CLI.");
		System.out.println("This Proof-of-concept will just simulate running it.");
		

		ServerStartingAttributes ssa = new ServerStartingAttributes(getLaunchReq, false);
		Status status1 = launcher.getServerProxy().serverStartingByClient(ssa).get();
		System.out.println(status1.toString());
		Status status2 = launcher.getServerProxy().serverStartedByClient(getLaunchReq).get();
		System.out.println(status2.toString());

	}
	private void printLocalLaunchCommandDetails(LaunchParameters getLaunchReq) throws Exception {
		CommandLineDetails det = launcher.getServerProxy().getLaunchCommand(getLaunchReq).get();
		if (det == null) {
			System.out.println("The SSP returned no launch command for this request.");
			return;
		}
		String[] cmdline = det.getCmdLine();
		String wd = det.getWorkingDir();
		String[] envp = det.getEnvp();
		
		System.out.println("Got it.");
		System.out.println("command: " + String.join(" ", cmdline));
	}

	private LaunchParameters getLaunchCommandRequest() throws Exception {
		System.out.println("Which server would you like to run?");
		List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
		for( ServerHandle sh : handles ) {
			System.out.println("   " + sh.getId());
		}
		String server = assistant.nextLine().trim();
		ServerHandle handle = findServer(server);
		if (handle == null) {
			System.out.println("Server " + server + " not found.");
			return null;
		}
		String mode = assistant.selectLaunchMode(handle.getType());
		LaunchAttributesRequest req = new LaunchAttributesRequest(handle.getType().getId(), mode);
		
		Attributes attrs = launcher.getServerProxy().getRequiredLaunchAttributes(req).get();
		HashMap<String, Object> toSend = new HashMap<String, Object>(); 
		assistant.promptForAttributes(attrs, toSend, true);
		
		ServerAttributes servAttr = new ServerAttributes(handle.getType().getId(), handle.getId(), toSend);
		LaunchParameters getLaunchReq = 
				new LaunchParameters(servAttr, mode);
		return getLaunchReq;
	}

	private ServerHandle findServer(String id) throws Exception {
		List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
		for (ServerHandle sh : handles) {
			if (sh.getId().equals(id))
				return sh;
		}
		return null;
	}
	
	
	private void runAddServer() {

		try {
			ServerType selected = assistant.chooseServerType();
			if (selected == null) {
				System.out.println("Canceling request.");
				return;
			}
			
			System.out.println("Please choose a unique name: ");
			String name = assistant.nextLine();
			if (name == null || name.isEmpty()) {
				System.out.println("Name cannot be empty. Canceling request.");
				return;
			}
			
			HashMap<String, Object> store = new HashMap<>();
			Attributes required2 = launcher.getServerProxy()
					.getRequiredAttributes(selected).get();
			Attributes optional2 = launcher.getServerProxy()
					.getOptionalAttributes(selected).get();
			assistant.promptForAttributes(required2, store, true);
			assistant.promptForAttributes(optional2, store, false);
			
			System.out.println("Adding Server...");
			ServerAttributes csa = new ServerAttributes(selected.getId(), name, store);
			CreateServerResponse result = launcher.getServerProxy().createServer(csa).get();
			if (result.getStatus().isOK()) {
				System.out.println("Server Added");
			} else {
				while(assistant.updateInvalidAttributes(result, required2, optional2, store)) {
					System.out.println("Adding Server...");
					csa = new ServerAttributes(selected.getId(), name, store);
					result = launcher.getServerProxy().createServer(csa).get();
					if (result.getStatus().isOK()) {
						System.out.println("Server Added");
						return;
					}
				}
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
	
	private void showCommands() {
		System.out.println("Invalid Command");
		System.out.println("Possible commands: ");
		for (int i = 0; i < CMD_ARR.length; i++) {
			System.out.println("   " + CMD_ARR[i]);
		}
	}


	@Override
	public String getPrompt() {
		return "Please enter a command.\n";
	}
}
