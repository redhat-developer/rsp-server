/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util;

public class URLUtil {

    /**
     * Create a URL string which is safe for all valid versions of IP.
     *  
     * For example, given a host 192.168.1.1, scheme http, and path my/folder, 
     * this will return  http://192.168.1.1/my/folder 
     * 
     * @param scheme  A scheme to connect over
     * @param host    A host
     * @param path    A path
     * @return
     */
    public static String createSafeURLString(String scheme, String host, String path) {
    	return createSafeURLString(scheme, host, -1, path);
    }
    
    /**
     * Create a URL string which is safe for all valid versions of IP.
     *  
     * For example, given a host 5a:55:4f:e6:e7:ea, scheme http, port 8080, and path my/folder, 
     * this will return  http://[5a:55:4f:e6:e7:ea]:8080/my/folder 
     * 
     * @param scheme  A scheme to connect over
     * @param host    A host
     * @param port    A port to connect over, or -1 if none is to be set
     * @param path    A path
     * @return
     */
    public static String createSafeURLString(String scheme, String host, int port, String path) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(scheme);
    	sb.append("://"); //$NON-NLS-1$
    	sb.append(IP6Util.formatPossibleIpv6Address(host));
    	if( port != -1 ) {
    		sb.append(":"); //$NON-NLS-1$
    		sb.append(port);
    	}
    	if( path != null ) {
    		if( !path.startsWith("/")) //$NON-NLS-1$
    			sb.append("/"); //$NON-NLS-1$
    		sb.append(path);
    	}
    	return sb.toString();
    }

}
