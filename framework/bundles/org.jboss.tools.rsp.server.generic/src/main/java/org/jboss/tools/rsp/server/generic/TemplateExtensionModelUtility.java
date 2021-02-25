/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic;

import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.launching.memento.JSONMemento;

public class TemplateExtensionModelUtility {

	public static JSONMemento generateEffectiveMemento(JSONMemento original) {
		JSONMemento templateChild = original.getChild("templates");
		if( templateChild == null || templateChild.getChildren() == null || templateChild.getChildren().length == 0 ) {
			return original;
		}
		
		JSONMemento[] templates = original.getChild("templates").getChildren();
		HashMap<String, JSONMemento> templateMap = new HashMap<>();
		for( int i = 0; i < templates.length; i++ ) {
			String name = templates[i].getNodeName();
			templateMap.put(name, templates[i]);
		}
		
		JSONMemento[] serverTypes = original.getChild("serverTypes").getChildren();
		JSONMemento effective = JSONMemento.createWriteRoot();
		JSONMemento effectiveServerTypes = effective.createChild("serverTypes");
		for( int i = 0; i < serverTypes.length; i++ ) {
			JSONMemento effectiveType = effectiveServerTypes.createChild(serverTypes[i].getNodeName());
			String templateId = serverTypes[i].getString("template"); 
			if( templateId != null && templateMap.get(templateId) != null) {
				JSONMemento templateMemento = templateMap.get(templateId);
				copyMemento(templateMemento, effectiveType);
			}
			copyMemento(serverTypes[i], effectiveType);
		}
		return effective;
	}
	
	

	public static void copyMemento(JSONMemento source, JSONMemento result) {
		List<String> attrNames = source.getNames();
		for( String s : attrNames ) {
			result.putString(s, source.getString(s));
		}
		JSONMemento[] children = source.getChildren();
		for( int i = 0; i < children.length; i++ ) {
			String nn = children[i].getNodeName();
			JSONMemento resultChild = result.getChild(nn);
			if( resultChild == null )
				resultChild = result.createChild(nn);
			copyMemento(children[i], resultChild);
		}
	}
}
