/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.io.File;
import java.util.Arrays;
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
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.DownloadSingleRuntimeRequest;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
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
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class StandardCommandHandler implements InputHandler {

	private enum Commands {
		SHUTDOWN("shutdown") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				launcher.getServerProxy().shutdown();
				System.out.println("The server has been shutdown");
				System.exit(0);
			}
		},
		ADD_PATH("add path") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command);
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				String suffix = command.substring(this.command.length());
				DiscoveryPath dp = new DiscoveryPath(suffix.trim());
				launcher.getServerProxy().addDiscoveryPath(dp);
			}
		},
		LIST_PATHS("list paths") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
				System.out.println("Paths:");
				if (list != null) {
					for( DiscoveryPath dp : list) {
						System.out.println("   " + dp.getFilepath());
					}
				}
			}
		},
		SEARCH_PATH("search path") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command);
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				String suffix = command.substring(this.command.length());
				DiscoveryPath dp = new DiscoveryPath(suffix.trim());
				List<ServerBean> beans = launcher.getServerProxy().findServerBeans(dp).get();
				System.out.println("Beans:");
				if (beans != null) {
					for( ServerBean b : beans) {
						System.out.println("   " + b.toString());
					}
				}
			}
		},
		REMOVE_PATH("remove path") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command);
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				String suffix = command.substring(this.command.length());
				DiscoveryPath dp = new DiscoveryPath(suffix.trim());
				launcher.getServerProxy().removeDiscoveryPath(dp);
			}
		},
		START_SERVER("start server") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command) 
						|| command.startsWith(this.command.substring(0, this.command.length()-2));
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				String suffix = command.substring(this.command.length()).trim();
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
					selected = findServer(serverId, launcher);
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
		},
		LAUNCH_COMMAND("launch command") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				LaunchParameters getLaunchReq = getLaunchCommandRequest(launcher, assistant);
				if( getLaunchReq != null )
					printLocalLaunchCommandDetails(getLaunchReq, launcher);
			}
		},
		LAUNCH_LOCAL("launch local") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				LaunchParameters getLaunchReq = getLaunchCommandRequest(launcher, assistant);
				printLocalLaunchCommandDetails(getLaunchReq, launcher);
				
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
		},
		STOP_SERVER("stop server") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command) 
						|| command.equals(this.command.trim());
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				String suffix = command.substring(this.command.length());
				String trimmed = suffix.trim();
				
				if( trimmed.length() == 0 ) {
					ServerHandle selected = assistant.selectServer();
					if( selected != null ) {
						trimmed = selected.getId();
					} else {
						System.out.println("Syntax: stop server servername [boolean:force]");
						return;
					}
				}
				String[] split = trimmed.split(" ");
				boolean force = false;
				if( split.length == 2 ) {
					force = Boolean.parseBoolean(split[1]);
				}
				StopServerAttributes ssa = new StopServerAttributes(split[0], force);
				Status stat = launcher.getServerProxy().stopServerAsync(ssa).get();
				System.out.println(stat.toString());
			}
		},
		LIST_SERVER_TYPES("list servertypes") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				List<ServerType> handles = launcher.getServerProxy().getServerTypes().get();
				System.out.println(handles.size() + " servers found:");
				for( ServerType sh : handles ) {
					System.out.println("   " + sh.getId() + ": " + sh.getVisibleName());
				}
			}
		},
		LIST_SERVERTYPE_ATTRIBUTES_REQUIRED("list attributes required") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				ServerType st = assistant.chooseServerType();
				if (st != null) {
					Attributes attr = launcher.getServerProxy().getRequiredAttributes(st).get();
					printAttr(attr);
				}
			}
		},
		LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL("list attributes optional") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				ServerType st = assistant.chooseServerType();
				if( st != null ) {
					Attributes attr = launcher.getServerProxy().getOptionalAttributes(st).get();
					printAttr(attr);
				}
			}
		},
		LIST_SERVERS("list servers") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
				System.out.println(handles.size() + " servers found:");
				for( ServerHandle sh : handles ) {
					System.out.println("   " + sh.getType().getId() + ":" + sh.getId());
				}
			}
		},
		ADD_SERVER("add server") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
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
					
					Map<String, Object> store = new HashMap<>();
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
				}			}
		},
		REMOVE_SERVER("remove server") {
			@Override
			public boolean isMatching(String command) {
				return command.startsWith(this.command);
			}

			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
				String suffix = command.substring(this.command.length());
				ServerHandle sh = findServer(suffix.trim(), launcher);
				if (sh != null)
					launcher.getServerProxy().deleteServer(sh);
				else
					System.out.println("Server not found: " + suffix.trim());
			}
		},
		LIST_DEPLOYMENTS("list deployments") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				try {
					ServerHandle server = assistant.selectServer();
					if( server != null ) {
						List<DeployableState> deployables = launcher.getServerProxy().getDeployables(server).get();
						System.out.println(deployables.size() + " deployments found:");
						int c = 1;
						for( DeployableState ds : deployables ) {
							int pubState = ds.getPublishState();
							String pubStateString = getPublishState(pubState);
							int runState = ds.getState();
							String runStateString = getRunState(runState);
							System.out.println(c++ + ") " + ds.getReference().getLabel() + " " + 
									runStateString + " " + pubStateString); // TODO add run state?
						}
					}
				} catch(InterruptedException | ExecutionException ioe) {
					ioe.printStackTrace();
				}
			}

			private String getPublishState(int publishState) {
				switch (publishState) {
				case ServerManagementAPIConstants.PUBLISH_STATE_ADD:
					return "[add]";
				case ServerManagementAPIConstants.PUBLISH_STATE_FULL:
					return "[full]";
				case ServerManagementAPIConstants.PUBLISH_STATE_INCREMENTAL:
					return "[inc]";
				case ServerManagementAPIConstants.PUBLISH_STATE_NONE:
					return "[none]";
				case ServerManagementAPIConstants.PUBLISH_STATE_REMOVE:
					return "[remove]";
				case ServerManagementAPIConstants.PUBLISH_STATE_UNKNOWN:
					return "[unknown]";
				default:
					return String.valueOf(publishState);
				}
			}

			private String getRunState(int runState) {
				switch (runState) {
				case ServerManagementAPIConstants.STATE_STARTING:
					return "[starting]";
				case ServerManagementAPIConstants.STATE_STARTED:
					return "[started]";
				case ServerManagementAPIConstants.STATE_STOPPING:
					return "[stopping]";
				case ServerManagementAPIConstants.STATE_STOPPED:
					return "[stopped]";
				case ServerManagementAPIConstants.STATE_UNKNOWN:
					return "[unknown]";
				default:
					return String.valueOf(runState);
				}
			}

		},
		ADD_DEPLOYMENT("add deployment") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
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
		},
		REMOVE_DEPLOYMENT("remove deployment") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
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
		},
		PUBLISH("publish server") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
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
		},

		LIST_RUNTIMES("list runtimes") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				try {
					ListDownloadRuntimeResponse resp = launcher.getServerProxy().listDownloadableRuntimes().get();
					List<DownloadRuntimeDescription> list = resp.getRuntimes();
					for( DownloadRuntimeDescription d : list ) {
						System.out.println(d);
					}
				} catch(InterruptedException | ExecutionException ioe) {
					ioe.printStackTrace();
				}
			}
		},

		DOWNLOAD_RUNTIME("download runtime") {
			private boolean validateResponse(WorkflowResponse resp) {
				if( resp == null || resp.getStatus() == null) {
					System.out.println("The server has returned an empty response.");
					return false;
				}
				int statusSev = resp.getStatus().getSeverity();
				if( statusSev == Status.OK) {
					// All done
					System.out.println("The runtime is downloading.");
					return false;
				} 
				
				if( statusSev == Status.CANCEL || statusSev == Status.ERROR ) {
					System.out.println("The download runtime workflow has failed.");
					System.out.println(resp.getStatus().getMessage());
					return false;
				}
				return true;
			}
			
			private Map<String, Object> displayPromptsSeekInput(WorkflowResponse resp, PromptAssistant asst) {

				HashMap<String, Object> toSend = new HashMap<>();
				
				List<WorkflowResponseItem> respItems = resp.getItems();
				for( WorkflowResponseItem item : respItems ) {
					System.out.println("Item: " + item.getId());
					if( item.getLabel() != null)
						System.out.println("Label: " + item.getLabel());
					if( item.getContent() != null )
						System.out.println("Content:\n" + item.getContent());
					
					String type = item.getResponseType();
					if( type != null && !ServerManagementAPIConstants.ATTR_TYPE_NONE.equals(type)) {
						// Prompt for input
						asst.promptForAttributeSingleKey(type, null, null, item.getId(), true, toSend);
					}
				}
				return toSend;
			}
			
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				try {
					DownloadRuntimeDescription dlrt = assistant.selectDownloadRuntime();
					DownloadSingleRuntimeRequest req = new DownloadSingleRuntimeRequest();
					req.setDownloadRuntimeId(dlrt.getId());
					WorkflowResponse resp = launcher.getServerProxy().downloadRuntime(req).get();
					boolean done = false;
					while( !done ) {
						boolean continueWorklow = validateResponse(resp);
						if( !continueWorklow )
							return;
						Map<String, Object> toSend = displayPromptsSeekInput(resp, assistant);
						DownloadSingleRuntimeRequest req2 = new DownloadSingleRuntimeRequest();
						req2.setRequestId(resp.getRequestId());
						req2.setDownloadRuntimeId(dlrt.getId());
						req2.setData(toSend);
						resp = launcher.getServerProxy().downloadRuntime(req2).get();
					}
				} catch(InterruptedException | ExecutionException ioe) {
					ioe.printStackTrace();
				}
			}
		},

		EXIT("exit") {
			@Override
			public void execute(String command, ServerManagementClientLauncher launcher, PromptAssistant assistant) {
				launcher.closeConnection();
				System.exit(0);
			}
		};
		
		protected String command;

		Commands(String command) {
			this.command = command;
		}

		public boolean isMatching(String command) {
			return command.equals(this.command);
		}

		public abstract void execute(String command,  ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception;

		public String getCommand() {
			return command;
		}
		
		protected LaunchParameters getLaunchCommandRequest(ServerManagementClientLauncher launcher, PromptAssistant assistant) throws Exception {
			System.out.println("Which server would you like to run?");
			List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
			for( ServerHandle sh : handles ) {
				System.out.println("   " + sh.getId());
			}
			String server = assistant.nextLine().trim();
			ServerHandle handle = findServer(server, launcher);
			if (handle == null) {
				System.out.println("Server " + server + " not found.");
				return null;
			}
			String mode = assistant.selectLaunchMode(handle.getType());
			LaunchAttributesRequest req = new LaunchAttributesRequest(handle.getType().getId(), mode);
			
			Attributes attrs = launcher.getServerProxy().getRequiredLaunchAttributes(req).get();
			HashMap<String, Object> toSend = new HashMap<>(); 
			assistant.promptForAttributes(attrs, toSend, true);
			
			ServerAttributes servAttr = new ServerAttributes(handle.getType().getId(), handle.getId(), toSend);
			LaunchParameters getLaunchReq = 
					new LaunchParameters(servAttr, mode);
			return getLaunchReq;
		}

		protected ServerHandle findServer(String id, ServerManagementClientLauncher launcher) throws Exception {
			List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
			for (ServerHandle sh : handles) {
				if (sh.getId().equals(id))
					return sh;
			}
			return null;
		}

		protected void printAttr(Attributes attr) {
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

		protected void printLocalLaunchCommandDetails(LaunchParameters getLaunchReq, ServerManagementClientLauncher launcher) throws Exception {
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

	}
	
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
				|| s.trim().isEmpty()) {
			showCommands();
			return;
		}

		String command = s.trim();
		Commands known = getCommand(command);
		if (known == null) {
			showCommands();
			return;
		}
		known.execute(command, launcher, assistant);
	}

	private Commands getCommand(String command) {
		return Arrays.stream(Commands.values())
				.filter(known -> known.isMatching(command))
				.findFirst()
				.orElse(null);
	}

	private void showCommands() {
		System.out.println("Invalid Command");
		System.out.println("Possible commands: ");
		Arrays.stream(Commands.values())
			.forEach(command -> System.out.println("   " + command.getCommand()));
	}


	@Override
	public String getPrompt() {
		return "Please enter a command.\n";
	}
}
