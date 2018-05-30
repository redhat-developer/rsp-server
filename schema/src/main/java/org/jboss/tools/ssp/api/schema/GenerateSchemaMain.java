/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.ssp.api.schema;


import org.jboss.tools.ssp.api.dao.Attribute;
import org.jboss.tools.ssp.api.dao.Attributes;
import org.jboss.tools.ssp.api.dao.DiscoveryPath;
import org.jboss.tools.ssp.api.dao.ServerAttributes;
import org.jboss.tools.ssp.api.dao.ServerBean;
import org.jboss.tools.ssp.api.dao.ServerHandle;
import org.jboss.tools.ssp.api.dao.ServerProcess;
import org.jboss.tools.ssp.api.dao.ServerProcessOutput;
import org.jboss.tools.ssp.api.dao.ServerStateChange;
import org.jboss.tools.ssp.api.dao.ServerType;
import org.jboss.tools.ssp.api.dao.StartServerAttributes;
import org.jboss.tools.ssp.api.dao.Status;
import org.jboss.tools.ssp.api.dao.StopServerAttributes;
import org.jboss.tools.ssp.api.dao.VMDescription;
import org.jboss.tools.ssp.api.dao.VMHandle;

public class GenerateSchemaMain {
	public static void main(String[] args) throws Exception {
		Class[] daos = getDAOClasses();
		JSONUtility json = new JSONUtility();
		json.cleanFolder();
		json.writeJsonDAOSchemas(daos);
		
		TypescriptUtility ts = new TypescriptUtility();
		ts.cleanFolder();
		ts.writeTypescriptSchemas(daos);

		SpecificationGenerator generator = new SpecificationGenerator(json, ts);
		generator.generate();
		// Write MD docs
	}
	
	private static Class[] getDAOClasses() {

		Class[] daoClasses = new Class[] {
				Status.class,
				VMDescription.class, VMHandle.class,
				DiscoveryPath.class, ServerBean.class, 

				ServerType.class,ServerHandle.class,
				ServerAttributes.class,
				Attributes.class, Attribute.class,
				ServerProcess.class, ServerProcessOutput.class,
				ServerStateChange.class, 
				StartServerAttributes.class, StopServerAttributes.class
		};
		return daoClasses;
	}

	

}
