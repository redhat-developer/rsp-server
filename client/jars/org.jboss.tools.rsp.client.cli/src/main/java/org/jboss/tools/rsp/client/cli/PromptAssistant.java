/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
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
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.ListDeployablesResponse;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientImpl.PromptStringHandler;
import org.jboss.tools.rsp.client.bindings.ServerManagementClientLauncher;

public class PromptAssistant {
	public final ServerManagementClientLauncher launcher;
	public final InputProvider provider;

	public PromptAssistant(ServerManagementClientLauncher launcher, InputProvider provider) {
		this.launcher = launcher;
		this.provider = provider;
	}

	public int selectPublishType() {
		String[] options = new String[] {"Incremental", "Full", "Clean"};
		List<String> opt2 = Arrays.asList(options);
		String ret = promptUser(opt2, "Please select a publish mode:");
		if( ret != null && opt2.contains(ret)) {
			return opt2.indexOf(ret) + 1;
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
			List<String> typeList = types.stream().map(ServerType::getVisibleName).collect(Collectors.toList());
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
		ListDeployablesResponse deployables = launcher.getServerProxy().getDeployables(handle).get();
		List<String> collectorCollection = deployables.getStates().stream()
				.map(DeployableState::getReference)
				.map(DeployableReference::getLabel)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a deployment:");
		if( ret != null && collectorCollection.contains(ret)) {
			int ind = collectorCollection.indexOf(ret);
			return deployables.getStates().get(ind).getReference();
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
	
	public JobHandle selectJob() throws InterruptedException, ExecutionException {
		List<JobProgress> jobs = launcher.getServerProxy().getJobs().get();
		List<String> collector = new ArrayList<>();
		for( JobProgress jp : jobs ) {
			collector.add(jp.getHandle().getId() + " [" + jp.getHandle().getName() + ", " + jp.getPercent() + "%]");
		}
		String prompted = promptUser(collector, "Please select a job:");
		if( prompted != null && collector.contains(prompted)) {
			int ind = collector.indexOf(prompted);
			return jobs.get(ind).getHandle();
		}
		return null;
	}

	public DownloadRuntimeDescription selectDownloadRuntime() throws InterruptedException, ExecutionException {
		ListDownloadRuntimeResponse resp = launcher.getServerProxy().listDownloadableRuntimes().get();
		List<DownloadRuntimeDescription> rts = resp.getRuntimes();
		List<String> collectorCollection = rts.stream()
				.map(DownloadRuntimeDescription::getName)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a server:");
		if( ret != null && collectorCollection.contains(ret)) {
			int ind = collectorCollection.indexOf(ret);
			return rts.get(ind);
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

	public void promptForAttributeSingleKey(CreateServerAttributesUtility attrsUtil, String k, boolean required2, Map<String, Object> toSend) {
		String attrType = attrsUtil.getAttributeType(k);
		String reqDesc = attrsUtil.getAttributeDescription(k);
		Object defVal = attrsUtil.getAttributeDefaultValue(k);
		boolean secret = attrsUtil.isAttributeSecret(k);
		promptForAttributeSingleKey(attrType, reqDesc, defVal, k, secret, required2, toSend);
	}
		
	public void promptForAttributeSingleKey(
			String attrType, String reqDesc, Object defVal,
			String k, boolean secret, boolean required2, 
			Map<String, Object> toSend) {
		Class<?> c = getAttributeTypeAsClass(attrType);
		if (c == null) {
			System.out.println("unknown attribute type " + attrType + ". Aborting.");
			return;
		}
		String reqType = c.getName();
		
		// Workaround to sending integers over json
		defVal = workaroundDoubles(defVal, attrType);
		String toPrint = "Key: " + k + "\nType: " + reqType;
		if( reqDesc != null ) 
			toPrint += "\nDescription: " + reqDesc;
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
			value = promptPrimitiveValue(attrType, secret);
		} else if (List.class.equals(c)) {
			value = promptListValue();
		} else if (Map.class.equals(c)) {
			value = promptMapValue();
		}
		toSend.put(k, value);
	}

	public Map<String, String> promptMapValue() {
		System.out.println("Please enter a map value. Each line should read some.key=some.val.\nEnter a blank line to end the map.");
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

	public String promptMultiLineString() {
		System.out.println("Please enter a multi-line String value. \nEnter a blank line to finish.");
		List<String> tmp = readMultipleLines(null, provider, false);
		return String.join("\n", tmp);
	}

	public List<String> promptListValue() {
		System.out.println("Please enter a list value. Enter a blank line to end the list.");
		List<String> arr = new ArrayList<>();
		String tmp = nextLine();
		while (!tmp.trim().isEmpty()) {
			arr.add(tmp);
			tmp = nextLine();
		}
		return arr;
	}

	public Object promptPrimitiveValue(String type, boolean secret) {
		System.out.println("Please enter a value: ");
		String val = nextLine(secret);
		return convertType(val, type);
	}

	public boolean updateInvalidAttributes(CreateServerResponse result, Attributes required2, Attributes optional2, Map<String, Object> store ) {
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
		return tryAgain != null 
				&& !tryAgain.isEmpty()
				&& !tryAgain.toLowerCase().equals("n");
	}

	public void promptForAttributes(Attributes attr, Map<String, Object> store, boolean required2) {
		if (attr == null)
			return;
		
		CreateServerAttributesUtility attrsUtil = new CreateServerAttributesUtility(attr);
		Map<String, Object> toSend = store;
		if (attrsUtil != null) {
			Set<String> keys = attrsUtil.listAttributes();
			for (String k : keys) {
				promptForAttributeSingleKey(attrsUtil, k, required2, toSend);
			}
		}
	}
	
	public void promptForInvalidAttributes(List<String> invalid, Attributes attr, Map<String, Object> store, boolean required2) {
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
	
	public Class<?> getAttributeTypeAsClass(String type) {
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
		return nextLine(null, provider, false);
	}

	public String nextLine(boolean secret) {
		return nextLine(null, provider, secret);
	}

	public static String nextLine(String prompt, InputProvider provider, boolean secret) {
		String p = (prompt == null ? "" : prompt);
		StandardPrompt sp = new StandardPrompt(p, secret);
		provider.addInputRequest(sp);
		sp.await();
		return sp.ret;
	}

	public static List<String> readMultipleLines(String prompt, InputProvider provider, boolean secret) {
		String p = (prompt == null ? "" : prompt);
		MultiLinePrompt sp = new MultiLinePrompt(p, secret);
		provider.addInputRequest(sp);
		sp.await();
		return sp.ret;
	}

	public static class MultiLinePrompt extends StandardPrompt {
		private List<String> ret = new ArrayList<String>();
		public MultiLinePrompt(String prompt, boolean isSecret) {
			super(prompt, isSecret);
			doneSignal = new CountDownLatch(1);
		}
		@Override
		public void handleInput(String line) throws Exception {
			ret.add(line);
			if( line.trim().isEmpty()) {
				setDone();
				doneSignal.countDown();
			}
		}
		@Override
		public void await() {
			try {
				doneSignal.await();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}

	}
	
	public static class StandardPrompt extends PromptStringHandler {

		private String ret;
		protected CountDownLatch doneSignal = new CountDownLatch(1);

		public StandardPrompt(String prompt, boolean isSecret) {
			super(prompt, isSecret);
		}
		
		@Override
		public void handleInput(String line) throws Exception {
			this.ret = line;
			doneSignal.countDown();
			setDone();
		}

		public void await() {
			try {
				doneSignal.await();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	public ServerActionWorkflow selectServerAction(ListServerActionResponse resp2) {
		List<String> collectorCollection = resp2.getWorkflows().stream()
				.map(ServerActionWorkflow::getActionLabel)
				.collect(Collectors.toList());
		String ret = promptUser(collectorCollection, "Please select a server action:");
		if( ret != null && collectorCollection.contains(ret)) {
			int ind = collectorCollection.indexOf(ret);
			return resp2.getWorkflows().get(ind);
		}
		return null;
	}
}
