/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IP6Util {

    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    
    private static final String ip4PatternString = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"; //$NON-NLS-1$
	private static final String ip6StandardPatternString = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"; //$NON-NLS-1$

    static {
      try {
        VALID_IPV4_PATTERN = Pattern.compile(ip4PatternString, Pattern.CASE_INSENSITIVE);
        VALID_IPV6_PATTERN = Pattern.compile(ip6StandardPatternString, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException e) {
        //logger.severe("Unable to compile pattern", e);
      }
    }

    private IP6Util() {
    	// inhibit instantiation
    }

	public static boolean matchesIP4t(String ipAddress) {
		Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
		return m1.matches();
	}

	public static boolean matchesIP6t(String ipAddress) {
		Matcher m1 = VALID_IPV6_PATTERN.matcher(ipAddress);
		if( m1.matches() )
			return true;
		
		// We don't match the regular pattern. 
		// We can still be ip6 if we don't match ipv4 but DO match ipv6 generally
		if( matchesIP4t(ipAddress)) 
			return false;
		try {
			InetAddress addr = Inet6Address.getByName(ipAddress);
			return true;
		} catch( UnknownHostException e) {
			return false;
		}
	}
    public static String formatPossibleIpv6Address(String address) {
        if (address == null) {
            return address;
        }
        if (!address.contains(":")) { //$NON-NLS-1$
            return address;
        }
        if (address.startsWith("[") && address.endsWith("]")) { //$NON-NLS-1$  //$NON-NLS-2$
            return address;
        }
        return "[" + address + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }
}
