package org.jboss.tools.rsp.client.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class PromptAssistant {
	public ServerManagementClientLauncher launcher;
	public InputProvider provider;

	public PromptAssistant(ServerManagementClientLauncher launcher, InputProvider provider) {
		this.launcher = launcher;
		this.provider = provider;
	}



	public int selectPublishType() {
		String[] options = new String[] {"Incremental", "Full", "Clean"};
		List<String> opt2 = Arrays.asList(options);
		String ret = promptUser(opt2, "Please select a launch mode:");
		if( ret != null && opt2.contains(ret)) {
			return opt2.indexOf(ret);
		}
		System.out.println("Invalid selection.");
		return -1;
	}
	

	public String selectLaunchMode(ServerType st) throws Exception {
		List<ServerLaunchMode> modes = launcher.getServerProxy().getLaunchModes(st).get();
		List<String> collectorCollection = modes.stream()
				.map(ServerLaunchMode::getMode)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a launch mode:");
		if( ret != null && collectorCollection.contains(ret)) {
			return ret;
		}
		return null;
	}

	public ServerType chooseServerType() {
		try {
			List<ServerType> types = launcher.getServerProxy().getServerTypes().get();
			List<String> typeList = types.stream().map(ServerType::getId).collect(Collectors.toList());
			String ret = promptUser(typeList, "Please select a server type:");
			if( ret != null && typeList.contains(ret)) {
				int ind = typeList.indexOf(ret);
				return types.get(ind);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DeployableReference chooseDeployment(ServerHandle handle) throws InterruptedException, ExecutionException {
		List<DeployableState> deployables = launcher.getServerProxy().getDeployables(handle).get();
		List<String> collectorCollection = deployables.stream()
				.map(DeployableState::getReference)
				.map(DeployableReference::getId)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a deployment:");
		if( ret != null && collectorCollection.contains(ret)) {
			int ind = collectorCollection.indexOf(ret);
			return deployables.get(ind).getReference();
		}
		return null;
	}

	public ServerHandle selectServer() throws InterruptedException, ExecutionException {
		List<ServerHandle> servers = launcher.getServerProxy().getServerHandles().get();
		List<String> collectorCollection = servers.stream()
				.map(ServerHandle::getId)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a server:");
		if( ret != null && collectorCollection.contains(ret)) {
			int ind = collectorCollection.indexOf(ret);
			return servers.get(ind);
		}
		return null;
	}

	public String promptUser(List<String> list, String msg) {
		int c = 1;
		System.out.println(msg);
		for (String str : list) {
			System.out.println(c++ + ") " + str);
		}
		String choice = nextLine().trim();
		if( list.contains(choice))
			return choice;
		
		// Whatever they typed wasn't an id, so maybe it was numeric.
		int num = -1;
		try {
			num = Integer.parseInt(choice);
		} catch(NumberFormatException nfe) {
			System.out.println("User choice does not match an available option.");
			return null;
		}
		if( num -1 >= 0 && list.size() >= num) 
			return list.get(num-1);
		return null;
	}
	
	public void promptForAttributeSingleKey(CreateServerAttributesUtility attrsUtil, String k, boolean required2, HashMap<String, Object> toSend) {
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
			if( !promptBoolean("Would you like to set this value? [y/n]") ) {
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

	public Map<String, String> promptMapValue() {
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

	public List<String> promptListValue() {
		System.out.println("Please enter a list value. Send a blank line to end the list.");
		List<String> arr = new ArrayList<String>();
		String tmp = nextLine();
		while (!tmp.trim().isEmpty()) {
			arr.add(tmp);
			tmp = nextLine();
		}
		return arr;
	}

	public Object promptPrimitiveValue(String type) {
		System.out.println("Please enter a value: ");
		String val = nextLine();
		return convertType(val, type);
	}
	

	public boolean updateInvalidAttributes(CreateServerResponse result, Attributes required2, Attributes optional2, HashMap<String, Object> store ) {
		System.out.println("Error adding server: " + result.getStatus().getMessage());
		List<String> list = result.getInvalidKeys();
		System.out.println("Invalid attributes: ");
		for( int i = 0; i < list.size(); i++ ) {
			System.out.println("   " + list.get(i));
		}
		
		boolean tryAgain = promptBoolean("Would you like to correct the invalid fields and try again? y/n");
		if (!tryAgain) {
			return false;
		}

		List<String> invalid = result.getInvalidKeys();
		promptForInvalidAttributes(invalid, required2, store, true);
		promptForInvalidAttributes(invalid, optional2, store, false);
		return true;
	}
	
	public boolean promptBoolean(String msg) {
		System.out.println(msg);
		String tryAgain = nextLine();
		if (tryAgain == null || tryAgain.isEmpty() || tryAgain.toLowerCase().equals("n")) {
			return false;
		}
		return true;
	}
		
	
	void promptForAttributes(Attributes attr, HashMap<String, Object> store, boolean required2) {
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
	
	public void promptForInvalidAttributes(List<String> invalid, Attributes attr, HashMap<String, Object> store, boolean required2) {
		if (attr == null)
			return;
		
		CreateServerAttributesUtility attrsUtil = new CreateServerAttributesUtility(attr);
		if (attrsUtil != null) {
			Set<String> keys = attrsUtil.listAttributes();
			for (String k : keys) {
				if( invalid.contains(k))
					promptForAttributeSingleKey(attrsUtil, k, required2, store);
			}
		}
	}
	
	
	
	/*
	 * Conversion
	 */


	public Object workaroundDoubles(Object defaultVal, String attrType) {

		// Workaround for the problems with json transfer
		Class<?> intended = getAttributeTypeAsClass(attrType);
		if (Integer.class.equals(intended) && Double.class.equals(defaultVal.getClass())) {
			return Integer.valueOf(((Double)defaultVal).intValue());
		}
		return defaultVal;
	}
	
	public Class getAttributeTypeAsClass(String type) {
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
	
	public Object convertType(String input, String type) {
		if (ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type)) {
			return Integer.parseInt(input);
		} else if (ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) {
			return input;
		} else if (ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type)) {
			return Boolean.parseBoolean(input);
		}
		return null;
	}
	
	public String nextLine() {
		return nextLine(null, provider);
	}

	public static String nextLine(String prompt, InputProvider provider) {
		String p = (prompt == null ? "" : prompt);
		StandardPrompt sp = new StandardPrompt(p);
		provider.addInputRequest(sp);
		sp.await();
		return sp.ret;
	}

	public static class StandardPrompt implements InputHandler {
		public String prompt;
		public String ret;
		public CountDownLatch doneSignal = new CountDownLatch(1);
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
}
