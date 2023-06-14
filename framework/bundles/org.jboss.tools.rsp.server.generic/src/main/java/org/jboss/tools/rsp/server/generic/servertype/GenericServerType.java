/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.launching.java.ILaunchModes;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IServerBehaviorProvider;
import org.jboss.tools.rsp.server.spi.servertype.AbstractServerType;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericServerType extends AbstractServerType {
	public static final String LAUNCH_OVERRIDE_BOOLEAN = "args.override.boolean";
	public static final String LAUNCH_OVERRIDE_PROGRAM_ARGS = "args.program.override.string";
	public static final String JAVA_LAUNCH_OVERRIDE_VM_ARGS = "args.vm.override.string";
	public static final String LAUNCH_OVERRIDE_SHUTDOWN_PROGRAM_ARGS = "args.shutdown.program.override.string";
	public static final String JAVA_LAUNCH_OVERRIDE_SHUTDOWN_VM_ARGS = "args.shutdown.vm.override.string";

	protected Attributes required = null;
	protected Attributes optional = null;
	private String runModes;
	private JSONMemento requiredAttributes;
	private JSONMemento optionalAttributes;
	private JSONMemento staticAttributes;
	private IServerBehaviorProvider delegateProvider;
	
	public GenericServerType(String id, String name, String desc,
			String runModes, JSONMemento requiredAttributes, 
			JSONMemento optionalAttributes, JSONMemento staticAttributes,
			IServerBehaviorProvider delegateProvider) {
		super(id, name, desc);
		this.runModes = runModes;
		this.requiredAttributes = requiredAttributes;
		this.optionalAttributes = optionalAttributes;
		this.staticAttributes = staticAttributes;
		this.delegateProvider = delegateProvider;
	}

	@Override
	public IServerDelegate createServerDelegate(IServer server) {
		if( delegateProvider != null )
			return delegateProvider.createServerDelegate(getId(), server);
		return null; // TODO
	}

	@Override
	public Attributes getRequiredAttributes() {
		if(required == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			if( requiredAttributes != null ) {
				fillAttributeUtility(attrs, requiredAttributes);
			}
			required = attrs.toPojo();
		}
		return required;
	}

	@Override
	public Attributes getOptionalAttributes() {
		if (optional == null) {
			CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
			if( optionalAttributes != null ) {
				fillAttributeUtility(attrs, optionalAttributes);
			}
			this.optional = attrs.toPojo();
		}
		return optional;
	}
	
	private void fillAttributeUtility(CreateServerAttributesUtility util, JSONMemento memento) {
		JSONMemento[] attrKeys = memento.getChildren();
		for( int i = 0; i < attrKeys.length; i++ ) {
			String id = attrKeys[i].getNodeName();
			String type = attrKeys[i].getString("type");
			String desc = attrKeys[i].getString("description");
			String dVal = attrKeys[i].getString("defaultValue");
			Object dValObj = convertDefaultValue(dVal, type);
			String secret = attrKeys[i].getString("secret");
			boolean secretVal = (secret == null ? false : Boolean.valueOf(secret));
			util.addAttribute(id, type, desc, dValObj, secretVal);
		}
	}
	
	@Override
	public Attributes getRequiredLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

	@Override
	public Attributes getOptionalLaunchAttributes() {
		CreateServerAttributesUtility attrs = new CreateServerAttributesUtility();
		return attrs.toPojo();
	}

	@Override
	public ServerLaunchMode[] getLaunchModes() {
		if( runModes == null || runModes.trim().isEmpty())
			return new ServerLaunchMode[] {};
		
		String[] modeArr = runModes.split(",");
		ServerLaunchMode[] arr = new ServerLaunchMode[modeArr.length];
		for( int i = 0; i < modeArr.length; i++ ) {
			String modeId = modeArr[i];
			String modeDesc = findRunModeDescription(modeId);
			arr[i] = new ServerLaunchMode(modeId, modeDesc);
		}
		return arr;
	}

	private String findRunModeDescription(String modeId) {
		if( modeId == null || modeId.isEmpty())
			return null;
		if(ILaunchModes.RUN.equals(modeId))
				return ILaunchModes.RUN_DESC;
		if(ILaunchModes.DEBUG.equals(modeId))
				return ILaunchModes.DEBUG_DESC;
		return modeId;
	}

	private Object convertDefaultValue(String val, String type) {
		if( ServerManagementAPIConstants.ATTR_TYPE_STRING.equals(type)) 
			return val;
		if( ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FILE.equals(type)) 
			return val;
		if( ServerManagementAPIConstants.ATTR_TYPE_LOCAL_FOLDER.equals(type)) 
			return val;
		if( ServerManagementAPIConstants.ATTR_TYPE_INT.equals(type))
			return Integer.parseInt(val);
		if( ServerManagementAPIConstants.ATTR_TYPE_BOOL.equals(type))
			return Boolean.parseBoolean(val);
		// TODO list and map?? 
		return val; 
	}
	
	public Map<String, Object> getDefaults() {
		HashMap<String, Object> ret = new HashMap<>();
		if( optionalAttributes != null ) {
			JSONMemento[] attrKeys = optionalAttributes.getChildren();
			for( int i = 0; i < attrKeys.length; i++ ) {
				String type = attrKeys[i].getString("type");
				String val = attrKeys[i].getString("defaultValue");
				Object dValObj = convertDefaultValue(val, type);
				ret.put(attrKeys[i].getNodeName(),  dValObj);
			}
		}
		if( staticAttributes != null ) {
			JSONMemento[] attrKeys = staticAttributes.getChildren();
			for( int i = 0; i < attrKeys.length; i++ ) {
				String type = attrKeys[i].getString("type");
				String val = attrKeys[i].getString("value");
				Object dValObj = convertDefaultValue(val, type);
				ret.put(attrKeys[i].getNodeName(),  dValObj);
			}
		}
		return ret;
	}
}
