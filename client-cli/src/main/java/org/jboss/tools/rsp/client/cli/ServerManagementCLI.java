/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.client.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class ServerManagementCLI {
	private static final String LIST_VM = "list vm";
	private static final String ADD_VM = "add vm ";
	private static final String REMOVE_VM = "remove vm ";

	private static final String LIST_PATHS = "list paths";
	private static final String ADD_PATH = "add path ";
	private static final String REMOVE_PATH = "remove path ";
	private static final String SEARCH_PATH = "search path ";
	
	
	private static final String LIST_SERVERS = "list servers";
	private static final String ADD_SERVER = "add server";
	private static final String REMOVE_SERVER = "remove server ";
	
	private static final String START_SERVER = "start server ";
	private static final String STOP_SERVER = "stop server ";
	
	private static final String LAUNCH_COMMAND = "launch command";
	private static final String LAUNCH_LOCAL = "launch local";
	
	
	private static final String EXIT = "exit";
	private static final String SHUTDOWN = "shutdown";

	private static final String[] CMD_ARR = new String[] {
			LIST_PATHS, ADD_PATH, REMOVE_PATH, SEARCH_PATH, 
			// LIST_VM, ADD_VM, REMOVE_VM,
			LIST_SERVERS, ADD_SERVER, REMOVE_SERVER, 
			LAUNCH_COMMAND, LAUNCH_LOCAL, START_SERVER, STOP_SERVER,
			EXIT, SHUTDOWN
	};
	
	
	public static void main(String[] args) {
		ServerManagementCLI cli = new ServerManagementCLI();
		cli.connect(args[0], args[1]);
		System.out.println("Connected to: " + args[0] + ":" + args[1]);
		cli.readCommands();
	}
	
	
	
	
	private Scanner scanner = null;
	private ServerManagementClientLauncher launcher;
	
	public void connect(String host, String port) {
		if( host == null ) {
			System.out.print("Enter server host: ");
			host = nextLine();
		}
		if( port == null ) {
			System.out.print("Enter server port: ");
			port = nextLine();
		}
		
		try {
			launcher = new ServerManagementClientLauncher(host, Integer.parseInt(port));
			launcher.launch();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String nextLine() {
		if( scanner == null ) {
			 scanner = new Scanner(System.in);
		}
		return scanner.nextLine();
	}
	
	private void readCommands() {
		while (true) {
			String content = nextLine();
			try {
				processCommand(content);
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}


	private void processCommand(String s) throws Exception {
		if( s.trim().isEmpty())
			return;
		Properties p = System.getProperties();
		if( s.trim().equals(SHUTDOWN)) {
			launcher.getServerProxy().shutdown();
			System.out.println("The server has been shutdown");
			System.exit(0);
//		} else if( s.trim().equals(LIST_VM)) {
//			List<VMDescription> list = launcher.getServerProxy().getVMs().get();
//			System.out.println("VMs:");
//			if( list != null ) {
//				for( VMDescription d : list ) {
//					System.out.println(d.getId() + ": " + d.getVersion() + " @ " + d.getInstallLocation());
//				}
//			}
//		} else if( s.equals(ADD_VM.trim())) {
//			System.out.println("Syntax: add vm someName /some/path");
//		} else if( s.startsWith(ADD_VM)) {
//			String suffix = s.substring(ADD_VM.length()).trim();
//			int firstSpace = suffix.indexOf(" ");
//			if( firstSpace == -1 ) {
//				System.out.println("Syntax: add vm someName /some/path");
//			} else {
//				VMDescription desc = new VMDescription(suffix.substring(0, firstSpace).trim(), 
//						suffix.substring(firstSpace).trim(), null);
//				launcher.getServerProxy().addVM(desc);
//			}
//		} else if( s.startsWith(REMOVE_VM)) {
//			String suffix = s.substring(REMOVE_VM.length()).trim();
//			launcher.getServerProxy().removeVM(new VMHandle(suffix));
		} else if( s.trim().equals(LIST_PATHS)) {
			List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
			System.out.println("Paths:");
			if( list != null ) {
				for( DiscoveryPath dp : list) {
					System.out.println("   " + dp.getFilepath());
				}
			}
		} else if( s.startsWith(START_SERVER)) {
			String suffix = s.substring(START_SERVER.length()).trim();
			String serverId = suffix;
			ServerHandle handle = findServer(serverId);
			if( handle == null ) {
				System.out.println("Server " + serverId + " not found.");
			} else {
				String mode = selectLaunchMode(handle.getType());
				ServerAttributes sa = new ServerAttributes(handle.getType().getId(), handle.getId(), new HashMap<String,Object>());
				LaunchParameters params = new LaunchParameters(sa, mode);
				StartServerResponse stat = launcher.getServerProxy().startServerAsync(params).get();
				System.out.println(stat.getStatus().toString());
			}
		} else if( s.equals(LAUNCH_COMMAND)) {
			LaunchParameters getLaunchReq = getLaunchCommandRequest();
			if( getLaunchReq != null )
				printLocalLaunchCommandDetails(getLaunchReq);
		} else if( s.equals(LAUNCH_LOCAL)) {
			runLocalLaunchScenario(s);
		} else if( s.startsWith(STOP_SERVER)) {
			String suffix = s.substring(STOP_SERVER.length()).trim();
			StopServerAttributes ssa = new StopServerAttributes(suffix, false);
			Status stat = launcher.getServerProxy().stopServerAsync(ssa).get();
			System.out.println(stat.toString());
		} else if( s.startsWith(ADD_PATH)) {
			String suffix = s.substring(ADD_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().addDiscoveryPath(dp);
		} else if( s.startsWith(REMOVE_PATH)) {
			String suffix = s.substring(REMOVE_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().removeDiscoveryPath(dp);
		} else if( s.startsWith(SEARCH_PATH)) {
			String suffix = s.substring(SEARCH_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			List<ServerBean> beans = launcher.getServerProxy().findServerBeans(dp).get();
			System.out.println("Beans:");
			if( beans != null ) {
				for( ServerBean b : beans) {
					System.out.println("   " + b.toString());
				}
			}
		} else if( s.trim().equals(LIST_SERVERS)) {
			List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
			System.out.println(handles.size() + " servers found:");
			for( ServerHandle sh : handles ) {
				System.out.println("   " + sh.getType() + ":" + sh.getId());
			}
		} else if( s.trim().startsWith(REMOVE_SERVER)) {
			String suffix = s.substring(REMOVE_SERVER.length());
			ServerHandle sh = findServer(suffix.trim());
			if( sh != null )
				launcher.getServerProxy().deleteServer(sh);
			else
				System.out.println("Server not found: " + suffix.trim());
		} else if( s.trim().equals(ADD_SERVER)) {
			runAddServer();
		} else if( s.trim().equals(EXIT)) {
			launcher.closeConnection();
			System.exit(0);
		} else {
			showCommands();
		}
	}
	
	private void runLocalLaunchScenario(String s) throws Exception {
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
		if( det == null ) {
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
		String server = nextLine().trim();
		ServerHandle handle = findServer(server);
		if( handle == null ) {
			System.out.println("Server " + server + " not found.");
			return null;
		}
		String mode = selectLaunchMode(handle.getType());
		LaunchAttributesRequest req = new LaunchAttributesRequest(handle.getType().getId(), mode);
		
		Attributes attrs = launcher.getServerProxy().getRequiredLaunchAttributes(req).get();
		HashMap<String, Object> toSend = new HashMap<String, Object>(); 
		promptForAttributes(attrs, toSend, true);
		
		ServerAttributes servAttr = new ServerAttributes(handle.getType().getId(), handle.getId(), toSend);
		LaunchParameters getLaunchReq = 
				new LaunchParameters(servAttr, mode);
		return getLaunchReq;
	}

	private ServerHandle findServer(String id) throws Exception {
		List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
		for( ServerHandle sh : handles ) {
			if( sh.getId().equals(id)) 
				return sh;
		}
		return null;
	}
	
	private ServerType chooseServerType() {
		List<ServerType> types = null;
		try {
			types = launcher.getServerProxy().getServerTypes().get();
			
			System.out.println("What type of server do you want to create?");
			int c = 1;
			for( ServerType it : types ) {
				System.out.println("   " + c + ") " + it.getId());
				c++;
			}
			String type = nextLine().trim();
			try {
				Integer inte = Integer.parseInt(type);
				int ind = inte-1;
				if( ind >= 0 && ind < types.size() ) {
					return types.get(ind);
				}
			} catch(NumberFormatException nfe) {
				ServerType selected = null;
				for( ServerType st1 : types ) {
					if( st1.getId().equals(type)) {
						selected = st1;
					}
				}
				return selected;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void runAddServer() {

		try {
			ServerType selected = chooseServerType();
			if( selected == null ) {
				System.out.println("Canceling request.");
				return;
			}
			
			System.out.println("Please choose a unique name: ");
			String name = nextLine();
			if( name == null || name.isEmpty()) {
				System.out.println("Name cannot be empty. Canceling request.");
				return;
			}
			
			HashMap<String, Object> store = new HashMap<String, Object>();
			Attributes required2 = launcher.getServerProxy()
					.getRequiredAttributes(selected).get();
			Attributes optional2 = launcher.getServerProxy()
					.getOptionalAttributes(selected).get();
			promptForAttributes(required2, store, true);
			promptForAttributes(optional2, store, false);
			
			System.out.println("Adding Server...");
			ServerAttributes csa = new ServerAttributes(selected.getId(), name, store);
			Status result = launcher.getServerProxy().createServer(csa).get();
			if( result.isOK()) {
				System.out.println("Server Added");
			} else {
				System.out.println("Error adding server: " + result.getMessage());
			}
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
	private void promptForAttributes(Attributes attr, HashMap<String, Object> store, boolean required2) {
		if( attr == null )
			return;
		
		CreateServerAttributesUtility attrsUtil = new CreateServerAttributesUtility(attr);
		HashMap<String, Object> toSend = store;
		if( attrsUtil != null ) {
			Set<String> keys = attrsUtil.listAttributes();
			for( String k : keys ) {
				String attrType = attrsUtil.getAttributeType(k);
				Class c = getAttributeTypeAsClass(attrType);
				String reqType = c.getName();
				String reqDesc = attrsUtil.getAttributeDescription(k);
				Object defVal = attrsUtil.getAttributeDefaultValue(k);
				
				// Workaround to sending integers over json
				defVal = workaroundDoubles(defVal, attrType);
				String toPrint = "Key: " + k + "\nType: " + reqType + "\nDescription: " + reqDesc;
				if( defVal != null ) {
					toPrint += "\nDefault Value: " + defVal.toString();
				}
				System.out.println(toPrint);
				if( !required2 ) {
					System.out.println("Would you like to set this value? [y/n]");
					String val = nextLine();
					if( val == null || val.isEmpty() || val.toLowerCase().equals("n")) {
						System.out.println("Skipping");
						continue;
					}
				}
				
				String msg = null;
				if( Integer.class.equals(c) || Boolean.class.equals(c) || String.class.equals(c)) {
					msg = "Please enter a value: ";
				} else if( List.class.equals(c)) {
					msg = "Please enter a list value. Send a blank line to end the list.";
				} else if( Map.class.equals(c)) {
					msg = "Please enter a map value. Each line should read some.key=some.val.\nSend a blank line to end the map.";
				}
				System.out.println(msg);
				
				if( Integer.class.equals(c) || Boolean.class.equals(c) || String.class.equals(c)) {
					String val = nextLine();
					toSend.put(k, convertType(val, attrsUtil.getAttributeType(k)));
				} else if( List.class.equals(c)) {
					List<String> arr = new ArrayList<String>();
					String tmp = nextLine();
					while(!tmp.trim().isEmpty()) {
						arr.add(tmp);
						tmp = nextLine();
					}
					toSend.put(k, arr);
				} else if( Map.class.equals(c)) {
					Map<String, String> map = new HashMap<String, String>();
					String tmp = nextLine();
					while(!tmp.trim().isEmpty()) {
						int ind = tmp.indexOf("=");
						if( ind == -1 ) {
							System.out.println("Invalid map entry. Please try again");
						} else {
							String k1 = tmp.substring(0,  ind);
							String v1 = tmp.substring(ind+1);
							map.put(k1,v1);
						}
						tmp = nextLine();
					}
					toSend.put(k, map);
				}
			}
		}
	}
	
	private Object workaroundDoubles(Object defaultVal, String attrType) {

		// Workaround for the problems with json transfer
		Class intended = getAttributeTypeAsClass(attrType);
		if( Integer.class.equals(intended) && Double.class.equals(defaultVal.getClass())) {
			return new Integer(((Double)defaultVal).intValue());
		}
		return defaultVal;
	}
	
	private Class getAttributeTypeAsClass(String type) {
		if( ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return String.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_LIST.equals(type)) {
			return List.class;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_MAP.equals(type)) {
			return Map.class;
		}
		return null;
	}
	
	private Object convertType(String input, String type) {
		if( ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.parseInt(input);
		} else if(ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return input;
		} else if( ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.parseBoolean(input);
		}
		return null;
	}
	
	private void showCommands() {
		System.out.println("Invalid Command");
		System.out.println("Possible commands: ");
		for( int i = 0; i < CMD_ARR.length; i++ ) {
			System.out.println("   " + CMD_ARR[i]);
		}
		
	}
	
	private String selectLaunchMode(ServerType st) throws Exception {
		List<ServerLaunchMode> modes = launcher.getServerProxy().getLaunchModes(st).get();
		if( modes.size() > 0 ) {
			System.out.println("Please select a launch mode:");
			for( ServerLaunchMode slm : modes ) {
				System.out.println(slm.getMode());
			}
		}
		String mode = nextLine().trim();
		return mode;
	}
}
