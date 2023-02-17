/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model.internal.publishing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.tools.rsp.server.spi.servertype.IDeploymentAssemblyMapping;

public class DeploymentAssemblyFile {
	private Map<String, Object> fromJson;
	private ArrayList<DeploymentAssemblyMapping> mappingList;

	public DeploymentAssemblyFile(Map<String, Object> fromJson) {
		this.fromJson = fromJson;
		List mappings = fromJson == null ? null : (List)fromJson.get("mappings");
		ArrayList<DeploymentAssemblyMapping> list = new ArrayList<>();
		if( mappings != null ) {
			for( int i = 0; i < mappings.size(); i++ ) {
				Map singleMapping = (Map)mappings.get(i);
				String source = (String)singleMapping.get("source-path");
				String dest = (String)singleMapping.get("deploy-path");
				list.add(new DeploymentAssemblyMapping(source, dest));
			}
		}
		this.mappingList = list;
	}

	public IDeploymentAssemblyMapping[] getMappings() {
		return mappingList.toArray(new DeploymentAssemblyMapping[mappingList.size()]);
	}
}
