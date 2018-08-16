/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Based on code in org.eclipse.jdt
 *
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.internal.compiler.classfmt;

public class CompilerOptions {
	public static final String VERSION_JSR14 = "jsr14"; //$NON-NLS-1$
	public static final String VERSION_CLDC1_1 = "cldc1.1"; //$NON-NLS-1$

	public static long versionToJdkLevel(String versionID) {
		String version = versionID;
		// verification is optimized for all versions with same length and same "1." prefix
		if (version != null && version.length() > 0) {
			if (version.length() >= 3 && version.charAt(0) == '1' && version.charAt(1) == '.') {
				switch (version.charAt(2)) {
					case '1':
						return ClassFileConstants.JDK1_1;
					case '2':
						return ClassFileConstants.JDK1_2;
					case '3':
						return ClassFileConstants.JDK1_3;
					case '4':
						return ClassFileConstants.JDK1_4;
					case '5':
						return ClassFileConstants.JDK1_5;
					case '6':
						return ClassFileConstants.JDK1_6;
					case '7':
						return ClassFileConstants.JDK1_7;
					case '8':
						return ClassFileConstants.JDK1_8;
					default:
						return 0; // unknown
				}
			} else {
				switch (version.charAt(0)) {
					case '9':
						return ClassFileConstants.JDK9;
					// No default - let it go through the remaining checks.
				}
			}
		}
		if (VERSION_JSR14.equals(versionID)) {
			return ClassFileConstants.JDK1_4;
		}
		if (VERSION_CLDC1_1.equals(versionID)) {
			return ClassFileConstants.CLDC_1_1;
		}
		return 0; // unknown
	}
}
