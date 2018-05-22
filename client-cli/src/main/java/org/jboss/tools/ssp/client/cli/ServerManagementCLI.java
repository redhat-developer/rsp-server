/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.client.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.ssp.api.ServerManagementAPIConstants;
import org.jboss.tools.ssp.api.dao.CreateServerAttributes;
import org.jboss.tools.ssp.api.dao.DiscoveryPath;
import org.jboss.tools.ssp.api.dao.ServerAttributes;
import org.jboss.tools.ssp.api.dao.ServerBean;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.api.dao.ServerType;
import org.jboss.tools.ssp.api.dao.StartServerAttributes;
import org.jboss.tools.ssp.api.dao.Status;
import org.jboss.tools.ssp.api.dao.StopServerAttributes;
import org.jboss.tools.ssp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.ssp.client.bindings.ServerManagementClientLauncher;

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
	
	
	private static final String EXIT = "exit";
	private static final String SHUTDOWN = "shutdown";

	private static final String[] CMD_ARR = new String[] {
			LIST_PATHS, ADD_PATH, REMOVE_PATH, SEARCH_PATH, 
			// LIST_VM, ADD_VM, REMOVE_VM,
			LIST_SERVERS, ADD_SERVER, REMOVE_SERVER, START_SERVER, STOP_SERVER,
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
			StartServerAttributes ssa = new StartServerAttributes(suffix, "run");
			Status stat = launcher.getServerProxy().startServerAsync(ssa).get();
			System.out.println(stat.toString());
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
	
	private ServerHandle findServer(String id) throws Exception {
		List<ServerHandle> handles = launcher.getServerProxy().getServerHandles().get();
		for( ServerHandle sh : handles ) {
			if( sh.getId().equals(id)) 
				return sh;
		}
		return null;
	}
	
	private void runAddServer() {
		List<String> types = null;
		try {
			types = launcher.getServerProxy().getServerTypes().get();
			System.out.println("What type of server do you want to create?");
			for( String it : types ) {
				System.out.println("   " + it);
			}
			String type = nextLine().trim();
			
			System.out.println("Please choose a unique name: ");
			String name = nextLine();
			
			CreateServerAttributes required2 = launcher.getServerProxy()
					.getRequiredAttributes(new ServerType(type)).get();
			CreateServerAttributesUtility required = new CreateServerAttributesUtility(required2);
			HashMap<String, Object> toSend = new HashMap<>();
			if( required != null ) {
				Set<String> keys = required.listAttributes();
				for( String k : keys ) {
					String attrType = required.getAttributeType(k);
					Class c = getAttributeTypeAsClass(attrType);
					String reqType = c.getName();
					String reqDesc = required.getAttributeDescription(k);
					Object defVal = required.getAttributeDefaultValue(k);
					
					// Workaround to sending integers over json
					defVal = workaroundDoubles(defVal, attrType);
					
					
					StringBuffer sb = new StringBuffer();
					sb.append("Key: ");
					sb.append(k);
					sb.append("\nType: ");
					sb.append(reqType);
					sb.append("\nDescription: ");
					sb.append(reqDesc);
					if( defVal != null ) {
						sb.append("\nDefault Value: ");
						sb.append(defVal.toString());
					}
					
					if( Integer.class.equals(c) || Boolean.class.equals(c) || String.class.equals(c)) {
						// Simple
						sb.append("\nPlease enter a value: ");
						System.out.println(sb.toString());
						String val = nextLine();
						toSend.put(k, convertType(val, required.getAttributeType(k)));
					} else if( List.class.equals(c)) {
						sb.append("\nPlease enter a list value. Send a blank line to end the list.");
						System.out.println(sb.toString());
						List<String> arr = new ArrayList<String>();
						String tmp = nextLine();
						while(!tmp.trim().isEmpty()) {
							arr.add(tmp);
							tmp = nextLine();
						}
						toSend.put(k, arr);
					} else if( Map.class.equals(c)) {
						sb.append("\nPlease enter a map value. Each line should read some.key=some.val.\nSend a blank line to end the map.");
						System.out.println(sb.toString());
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
				System.out.println("Adding Server...");
				ServerAttributes csa = new ServerAttributes(type, name, toSend);
				Status result = launcher.getServerProxy().createServer(csa).get();
				if( result.isOK()) {
					System.out.println("Server Added");
				} else {
					System.out.println("Error adding server: " + result.getMessage());
				}
			}
			
		} catch(InterruptedException | ExecutionException ioe) {
			ioe.printStackTrace();
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
}
