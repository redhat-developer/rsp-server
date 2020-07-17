/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.actions;

import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;

public class GenericServerShowInBrowserActionHandler extends AbstractShowInBrowserActionHandler {
	private GenericServerBehavior genericServerBehavior;
	public GenericServerShowInBrowserActionHandler(GenericServerBehavior genericServerBehavior) {
		super(genericServerBehavior);
		this.genericServerBehavior = genericServerBehavior;
	}
	
	@Override
	protected String[] getDeploymentUrls(DeployableState ds) {
		String strat = getDeploymentStrategy();
		String deployableOutputName = getOutputName(ds.getReference());
		
		String[] ret = genericServerBehavior.getDeploymentUrls(strat, getBaseUrl(), 
				deployableOutputName, ds);
		if( ret != null )
			return ret;
		
		// Return default implementations
		String depName = deployableOutputName;
		if( "appendDeploymentNameRemoveSuffix".equals(strat)) {
			if (depName.indexOf(".") > 0)
				depName = depName.substring(0, depName.lastIndexOf("."));
		} else if( "appendDeploymentName".equals(strat)) {
			// Do nothing
		}
		String ret1 = getBaseUrl() + "/" + depName;
		return new String[] {ret1};
	}
	
	@Override
	protected String getBaseUrl() {
		JSONMemento mem = genericServerBehavior.getActionsJSON().getChild(ACTION_SHOW_IN_BROWSER_JSON_ID);
		String ret = mem.getString("baseUrl");
		try {
			ret = genericServerBehavior.applySubstitutions(ret);
		} catch(CoreException ce) {
			// TODO log
		}
		return ret;
	}

	private String getDeploymentStrategy() {
		JSONMemento mem = genericServerBehavior.getActionsJSON().getChild(ACTION_SHOW_IN_BROWSER_JSON_ID);
		return mem.getString("deploymentStrategy");
	}

}
