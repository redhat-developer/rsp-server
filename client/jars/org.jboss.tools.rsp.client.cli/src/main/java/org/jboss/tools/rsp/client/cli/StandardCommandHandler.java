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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
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

	private static final String EXIT = "exit";
	private static final String SHUTDOWN = "shutdown";

	private static final String[] CMD_ARR = new String[] {
			LIST_PATHS, ADD_PATH, REMOVE_PATH, SEARCH_PATH, 
			LIST_SERVERS, LIST_SERVER_TYPES, 
			LIST_SERVERTYPE_ATTRIBUTES_REQUIRED, LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL,
			ADD_SERVER, REMOVE_SERVER, 
			LAUNCH_COMMAND, LAUNCH_LOCAL, START_SERVER, STOP_SERVER,
			EXIT, SHUTDOWN
	};

	private ServerManagementClientLauncher launcher;
	private InputProvider provider;
	public StandardCommandHandler(ServerManagementClientLauncher launcher, InputProvider provider) {
		this.launcher = launcher;
		this.provider = provider;
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
		} else if (s.trim().equals(LIST_PATHS)) {
			List<DiscoveryPath> list = launcher.getServerProxy().getDiscoveryPaths().get();
			System.out.println("Paths:");
			if (list != null) {
				for( DiscoveryPath dp : list) {
					System.out.println("   " + dp.getFilepath());
				}
			}
		} else if (s.startsWith(START_SERVER)) {
			String suffix = s.substring(START_SERVER.length()).trim();
			String serverId = suffix;
			ServerHandle handle = findServer(serverId);
			if (handle == null) {
				System.out.println("Server " + serverId + " not found.");
			} else {
				String mode = selectLaunchMode(handle.getType());
				ServerAttributes sa = new ServerAttributes(handle.getType().getId(), handle.getId(), new HashMap<String,Object>());
				LaunchParameters params = new LaunchParameters(sa, mode);
				StartServerResponse stat = launcher.getServerProxy().startServerAsync(params).get();
				System.out.println(stat.getStatus().toString());
			}
		} else if (s.equals(LAUNCH_COMMAND)) {
			LaunchParameters getLaunchReq = getLaunchCommandRequest();
			if( getLaunchReq != null )
				printLocalLaunchCommandDetails(getLaunchReq);
		} else if (s.equals(LAUNCH_LOCAL)) {
			runLocalLaunchScenario();
		} else if (s.startsWith(STOP_SERVER)) {
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
		} else if (s.startsWith(ADD_PATH)) {
			String suffix = s.substring(ADD_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().addDiscoveryPath(dp);
		} else if (s.startsWith(REMOVE_PATH)) {
			String suffix = s.substring(REMOVE_PATH.length());
			DiscoveryPath dp = new DiscoveryPath(suffix.trim());
			launcher.getServerProxy().removeDiscoveryPath(dp);
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
		} else if (s.trim().equals(LIST_SERVER_TYPES)) {
			List<ServerType> handles = launcher.getServerProxy().getServerTypes().get();
			System.out.println(handles.size() + " servers found:");
			for( ServerType sh : handles ) {
				System.out.println("   " + sh.getId() + ": " + sh.getVisibleName());
			}
						
		} else if (s.trim().equals(LIST_SERVERTYPE_ATTRIBUTES_REQUIRED)) {
			ServerType st = chooseServerType();
			if (st != null) {
				Attributes attr = launcher.getServerProxy().getRequiredAttributes(st).get();
				printAttr(attr);
			}
		} else if (s.trim().equals(LIST_SERVERTYPE_ATTRIBUTES_OPTIONAL)) {
			ServerType st = chooseServerType();
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
		} else if (s.trim().equals(EXIT)) {
			launcher.closeConnection();
			System.exit(0);
		} else {
			showCommands();
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
		String server = nextLine().trim();
		ServerHandle handle = findServer(server);
		if (handle == null) {
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
		for (ServerHandle sh : handles) {
			if (sh.getId().equals(id))
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
			for (ServerType it : types) {
				System.out.println("   " + c + ") " + it.getId());
				c++;
			}
			String type = nextLine().trim();
			try {
				Integer inte = Integer.parseInt(type);
				int ind = inte-1;
				if (ind >= 0 && ind < types.size()) {
					return types.get(ind);
				}
			} catch (NumberFormatException nfe) {
				ServerType selected = null;
				for (ServerType st1 : types) {
					if (st1.getId().equals(type)) {
						selected = st1;
					}
				}
				return selected;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void runAddServer() {

		try {
			ServerType selected = chooseServerType();
			if (selected == null) {
				System.out.println("Canceling request.");
				return;
			}
			
			System.out.println("Please choose a unique name: ");
			String name = nextLine();
			if (name == null || name.isEmpty()) {
				System.out.println("Name cannot be empty. Canceling request.");
				return;
			}
			
			HashMap<String, Object> store = new HashMap<>();
			Attributes required2 = launcher.getServerProxy()
					.getRequiredAttributes(selected).get();
			Attributes optional2 = launcher.getServerProxy()
					.getOptionalAttributes(selected).get();
			promptForAttributes(required2, store, true);
			promptForAttributes(optional2, store, false);
			
			System.out.println("Adding Server...");
			ServerAttributes csa = new ServerAttributes(selected.getId(), name, store);
			CreateServerResponse result = launcher.getServerProxy().createServer(csa).get();
			if (result.getStatus().isOK()) {
				System.out.println("Server Added");
			} else {
				while(updateInvalidAttributes(result, required2, optional2, store)) {
					System.out.println("Adding Server...");
					csa = new ServerAttributes(selected.getId(), name, store);
					CreateServerResponse r6 = launcher.getServerProxy().createServer(csa).get();
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
	
	
	private boolean updateInvalidAttributes(CreateServerResponse result, Attributes required2, Attributes optional2, HashMap<String, Object> store ) {
		System.out.println("Error adding server: " + result.getStatus().getMessage());
		List<String> list = result.getInvalidKeys();
		System.out.println("Invalid attributes: ");
		for( int i = 0; i < list.size(); i++ ) {
			System.out.println("   " + list.get(i));
		}
		
		System.out.println("Would you like to correct the invalid fields and try again? y/n");
		String tryAgain = nextLine();
		if( tryAgain.equalsIgnoreCase("y") || tryAgain.equalsIgnoreCase("yes")) {
			List<String> invalid = result.getInvalidKeys();
			promptForInvalidAttributes(invalid, required2, store, true);
			promptForInvalidAttributes(invalid, optional2, store, false);
			return true;
		}
		return false;
	}
	
	private void promptForAttributes(Attributes attr, HashMap<String, Object> store, boolean required2) {
		if (attr == null)
			return;
		
		CreateServerAttributesUtility attrsUtil = new CreateServerAttributesUtility(attr);
		HashMap<String, Object> toSend = store;
		if (attrsUtil != null) {
			Set<String> keys = attrsUtil.listAttributes();
			for (String k : keys) {
				promptForAttributeSingleKey(attrsUtil, k, required2, toSend);
			}
		}
	}
	
	private void promptForInvalidAttributes(List<String> invalid, Attributes attr, HashMap<String, Object> store, boolean required2) {
		if (attr == null)
			return;
		
		CreateServerAttributesUtility attrsUtil = new CreateServerAttributesUtility(attr);
		HashMap<String, Object> toSend = store;
		if (attrsUtil != null) {
			Set<String> keys = attrsUtil.listAttributes();
			for (String k : keys) {
				promptForAttributeSingleKey(attrsUtil, k, required2, toSend);
			}
		}
	}
	
	private void promptForAttributeSingleKey(CreateServerAttributesUtility attrsUtil, String k, boolean required2, HashMap<String, Object> toSend) {
		String attrType = attrsUtil.getAttributeType(k);
		Class c = getAttributeTypeAsClass(attrType);
		String reqType = c.getName();
		if (c == null) {
			System.out.println("unknown attribute type " + attrType + ". Aborting.");
		}
		String reqDesc = attrsUtil.getAttributeDescription(k);
		Object defVal = attrsUtil.getAttributeDefaultValue(k);
		
		// Workaround to sending integers over json
		defVal = workaroundDoubles(defVal, attrType);
		String toPrint = "Key: " + k + "\nType: " + reqType + "\nDescription: " + reqDesc;
		if (defVal != null) {
			toPrint += "\nDefault Value: " + defVal.toString();
		}
		System.out.println(toPrint);
		if (!required2) {
			System.out.println("Would you like to set this value? [y/n]");
			String val = nextLine();
			if (val == null || val.isEmpty() || val.toLowerCase().equals("n")) {
				System.out.println("Skipping");
				return;
			}
		}
		
        Object value = null;
		if (Integer.class.equals(c) || Boolean.class.equals(c) || String.class.equals(c)) {
			value = promptPrimitiveValue(attrsUtil.getAttributeType(k));
		} else if (List.class.equals(c)) {
			value = promptListValue();
		} else if (Map.class.equals(c)) {
			value = promptMapValue();
		}
		toSend.put(k, value);
	}

	private Map<String, String> promptMapValue() {
		System.out.println("Please enter a map value. Each line should read some.key=some.val.\nSend a blank line to end the map.");
		Map<String, String> map = new HashMap<>();
		String tmp = nextLine();
		while (!tmp.trim().isEmpty()) {
			int ind = tmp.indexOf("=");
			if (ind == -1) {
				System.out.println("Invalid map entry. Please try again");
			} else {
				String k1 = tmp.substring(0,  ind);
				String v1 = tmp.substring(ind+1);
				map.put(k1,v1);
			}
			tmp = nextLine();
		}
		return map;
	}

	private List<String> promptListValue() {
		System.out.println("Please enter a list value. Send a blank line to end the list.");
		List<String> arr = new ArrayList<String>();
		String tmp = nextLine();
		while (!tmp.trim().isEmpty()) {
			arr.add(tmp);
			tmp = nextLine();
		}
		return arr;
	}

	private Object promptPrimitiveValue(String type) {
		System.out.println("Please enter a value: ");
		String val = nextLine();
		return convertType(val, type);
	}

	private Object workaroundDoubles(Object defaultVal, String attrType) {

		// Workaround for the problems with json transfer
		Class<?> intended = getAttributeTypeAsClass(attrType);
		if (Integer.class.equals(intended) && Double.class.equals(defaultVal.getClass())) {
			return Integer.valueOf(((Double)defaultVal).intValue());
		}
		return defaultVal;
	}
	
	private Class getAttributeTypeAsClass(String type) {
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.class;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.class;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return String.class;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_LIST.equals(type)) {
			return List.class;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_MAP.equals(type)) {
			return Map.class;
		}
		return null;
	}
	
	private Object convertType(String input, String type) {
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.parseInt(input);
		} else if (ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return input;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.parseBoolean(input);
		}
		return null;
	}
	
	private void showCommands() {
		System.out.println("Invalid Command");
		System.out.println("Possible commands: ");
		for (int i = 0; i < CMD_ARR.length; i++) {
			System.out.println("   " + CMD_ARR[i]);
		}
	}
	
	private String selectLaunchMode(ServerType st) throws Exception {
		List<ServerLaunchMode> modes = launcher.getServerProxy().getLaunchModes(st).get();
		if (!modes.isEmpty()) {
			System.out.println("Please select a launch mode:");
			for (ServerLaunchMode slm : modes) {
				System.out.println(slm.getMode());
			}
		}
		String mode = nextLine().trim();
		return mode;
	}

	public String nextLine() {
		return nextLine(null);
	}

	public String nextLine(String prompt) {
		String p = (prompt == null ? "" : prompt);
		StandardPrompt sp = new StandardPrompt(p);
		provider.addInputRequest(sp);
		sp.await();
		return sp.ret;
	}

	private static class StandardPrompt implements InputHandler {
		private String prompt;
		private String ret;
		private CountDownLatch doneSignal = new CountDownLatch(1);
		public StandardPrompt(String prompt) {
			this.prompt = prompt;
		}
		@Override
		public String getPrompt() {
			return prompt;
		}

		@Override
		public void handleInput(String line) throws Exception {
			this.ret = line;
			doneSignal.countDown();
		}

		public void await() {
			try {
				doneSignal.await();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public String getPrompt() {
		return "Please enter a command.\n";
	}
}
