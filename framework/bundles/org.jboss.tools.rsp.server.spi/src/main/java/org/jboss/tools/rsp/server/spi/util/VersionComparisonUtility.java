/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.spi.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VersionComparisonUtility {
	private VersionComparisonUtility() {
		
	}
	
	public static boolean isJavaCompatible(String vmiVersion, String min, String max) {
		return isGreaterThanOrEqualTo(vmiVersion, min) && 
				isLessThanOrEqualTo(vmiVersion, max);
	}
	
	public static boolean isGreaterThanOrEqualTo(String vmi, String test) {
		if( test == null )
			return true;
		String[] splitVmi = vmi.split("\\.");
		String[] splitTest = test.split("\\.");
		int vmiMajor = Integer.parseInt(splitVmi[0]);
		int testMajor = Integer.parseInt(splitTest[0]);
		if( vmiMajor < testMajor ) return false;
		if( vmiMajor > testMajor ) return true;
		
		// Majors are equal. 
		if( splitTest.length <= 1 || splitTest[1].isEmpty())
			return true;
		int vmiMinor = Integer.parseInt(splitVmi[1]);
		int testMinor = Integer.parseInt(splitTest[1]);
		if( vmiMinor < testMinor ) return false;
		return true;
	}

	public static boolean isLessThanOrEqualTo(String vmi, String test) {
		if( test == null )
			return true;

		String[] splitVmi = vmi.split("\\.");
		String[] splitTest = test.split("\\.");
		int vmiMajor = Integer.parseInt(splitVmi[0]);
		int testMajor = Integer.parseInt(splitTest[0]);
		if( vmiMajor > testMajor ) return false;
		if( vmiMajor < testMajor ) return true;
		
		if( splitTest.length <= 1 || splitTest[1].isEmpty())
			return true;

		// Majors are equal. 
		int vmiMinor = Integer.parseInt(splitVmi[1]);
		int testMinor = Integer.parseInt(splitTest[1]);
		if( vmiMinor > testMinor ) return false;
		return true;
	}

	public static void sort(List<String> versions) {
		Collections.sort(versions, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				String[] arg0Split = arg0.split("\\.");
				String[] arg1Split = arg1.split("\\.");
				int max = arg0Split.length < arg1Split.length ? arg0Split.length : arg1Split.length;
				for( int i = 0; i < max; i++ ) {
					Integer arg0Segment = Integer.parseInt(arg0Split[i]);
					Integer arg1Segment = Integer.parseInt(arg1Split[i]);
					if( arg0Segment.intValue() > arg1Segment.intValue() ) {
						return 1;
					}
					if( arg0Segment.intValue() < arg1Segment.intValue() ) {
						return -1;
					}
				}
				// We've reached an end. 
				if( arg0Split.length > arg1Split.length )
					return 1; // Tie
				else if( arg0Split.length < arg1Split.length )
					return -1; // Tie
				else
					return 0;
			}
		});
	}
}
