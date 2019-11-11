/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.impl;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.spi.filewatcher.FileWatcherEvent;
import org.jboss.tools.rsp.server.spi.publishing.IFullPublishRequiredCallback;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildFlyFullPublishRequiredCallback implements IFullPublishRequiredCallback {
	private static final Logger LOG = LoggerFactory.getLogger(WildFlyFullPublishRequiredCallback.class);

    /**
     * The compiled default restart pattern to avoid multiple compilations
     */
    protected static Pattern defaultRestartPattern = Pattern.compile(getDefaultModuleRestartPattern(),
                    Pattern.CASE_INSENSITIVE);

    private static String getDefaultModuleRestartPattern() {
            return IJBossServerAttributes.WILDFLY_PUBLISH_RESTART_PATTERN_DEFAULT;
    }
    

	private WildFlyServerDelegate wildFlyServerDelegate;

	public WildFlyFullPublishRequiredCallback(WildFlyServerDelegate wildFlyServerDelegate) {
		this.wildFlyServerDelegate = wildFlyServerDelegate;
	}

	private IServer getServer() {
		return wildFlyServerDelegate.getServer();
	}
	
	@Override
	public boolean requiresFullPublish(FileWatcherEvent event) {
		Pattern pattern = getPattern();
		return pattern == null ? false : pattern.matcher(event.getPath().toString()).find(); 
	}

    private Pattern getPattern(){
    	String pattern = getServer().getAttribute(IJBossServerAttributes.WILDFLY_PUBLISH_RESTART_PATTERN_KEY, 
				IJBossServerAttributes.WILDFLY_PUBLISH_RESTART_PATTERN_DEFAULT);
    	if( getDefaultModuleRestartPattern().equals(pattern))
    		return defaultRestartPattern;
        if( pattern != null ) {
            try {
            	return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } catch(PatternSyntaxException pse) {
    			LOG.error(NLS.bind("Error loading module restart pattern for server {1}", getServer().getName()), pse);
            }
        }
        return null;
    }
}
