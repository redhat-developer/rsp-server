/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.schema;


import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.ClientCapabilitiesRequest;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.LaunchAttributesRequest;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerBean;
import org.jboss.tools.rsp.api.dao.ServerCapabilitiesResponse;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerLaunchMode;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.ServerType;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.Status;
import org.jboss.tools.rsp.api.dao.StopServerAttributes;
import org.jboss.tools.rsp.api.dao.StringPrompt;
import org.jboss.tools.rsp.api.dao.VMDescription;
import org.jboss.tools.rsp.api.dao.VMHandle;

public class GenerateSchemaMain {
	public static void main(String[] args) throws Exception {
		String baseDir = args.length > 0 ? args[0] : ".";
		Class[] daos = getDAOClasses();
		JSONUtility json = new JSONUtility(baseDir);
		json.cleanFolder();
		json.writeJsonDAOSchemas(daos);
		
		TypescriptUtility ts = new TypescriptUtility(baseDir);
		ts.cleanFolder();
		ts.writeTypescriptSchemas(daos);

		SpecificationGenerator generator = new SpecificationGenerator(json, ts, baseDir);
		generator.generate();
		// Write MD docs
	}
	
	private static Class[] getDAOClasses() {

		Class[] daoClasses = new Class[] {
				Attribute.class,
				Attributes.class,
				ServerCapabilitiesResponse.class,
				ClientCapabilitiesRequest.class,
				CommandLineDetails.class,
				DiscoveryPath.class,
				LaunchAttributesRequest.class,
				LaunchParameters.class,
				DeployableState.class,
				ServerAttributes.class,
				ServerBean.class,
				ServerHandle.class,
				ServerLaunchMode.class,
				ServerProcess.class,
				ServerProcessOutput.class,
				ServerStartingAttributes.class,
				ServerState.class,
				ServerType.class,
				StartServerResponse.class,
				Status.class,
				StopServerAttributes.class,
				StringPrompt.class,
				VMDescription.class,
				VMHandle.class,
		};
		return daoClasses;
	}

	

}
