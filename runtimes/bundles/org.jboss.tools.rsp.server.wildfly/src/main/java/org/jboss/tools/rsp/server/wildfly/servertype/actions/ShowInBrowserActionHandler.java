/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.actions;

import java.io.ByteArrayInputStream;

import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;
import org.jboss.tools.rsp.server.generic.jee.ContextRootSupport;
import org.jboss.tools.rsp.server.generic.servertype.actions.AbstractShowInBrowserActionHandler;
import org.jboss.tools.rsp.server.wildfly.servertype.impl.WildFlyServerDelegate;

public class ShowInBrowserActionHandler extends AbstractShowInBrowserActionHandler {
	private WildFlyServerDelegate wildFlyServerDelegate;
	public ShowInBrowserActionHandler(WildFlyServerDelegate wildFlyServerDelegate) {
		super(wildFlyServerDelegate);
		this.wildFlyServerDelegate = wildFlyServerDelegate;
	}
	
	@Override
	protected String getBaseUrl() {
		return wildFlyServerDelegate.getPollURL(wildFlyServerDelegate.getServer());
	}
	
	@Override
	protected String[] getDeploymentUrls(DeployableState ds) {
		return new ContextRootSupport() {

			@Override
			public String[] getDeploymentUrls(String strat, String baseUrl, 
					String deployableOutputName, DeployableState ds) {
				String noSuffix = removeWarSuffix(deployableOutputName);
				String[] fromDescriptor = findFromDescriptor(ds);
				// Default case, nothing in descriptor, use app name
				if( fromDescriptor == null || fromDescriptor.length == 0) 
					return new String[] {append(noSuffix, baseUrl)};
				
				// Found something in descriptor. 
				return append(fromDescriptor, baseUrl);
			}
			@Override
			protected String[] getCustomWebDescriptorsRelativePath() {
				return new String[] { "jboss-web.xml",  
						"WEB-INF/jboss-web.xml"};
			}
			
			@Override
			protected String findFromWebDescriptorString(String descriptorContents) {
				XMLMemento mem = XMLMemento.createReadRoot(new ByteArrayInputStream(descriptorContents.getBytes()));
				IMemento[] children = mem.getChildren("context-root");
				if( children != null && children.length == 1 ) {
					return ((XMLMemento)children[0]).getTextData();
				}
				return null;
			}
			
		}.getDeploymentUrls(null, getBaseUrl(), getOutputName(ds.getReference()), ds);
	}
}
