/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.eclipse.jdt.core;

import org.jboss.tools.rsp.eclipse.jdt.internal.compiler.classfmt.CompilerOptions;

public class JavaCore {

	private JavaCore() {
	}

	/**
	 * Compares two given versions of the Java platform. The versions being compared must both be
	 * one of the supported values mentioned in
	 * {@link #COMPILER_CODEGEN_TARGET_PLATFORM COMPILER_CODEGEN_TARGET_PLATFORM},
	 * both values from {@link #COMPILER_COMPLIANCE},  or both values from {@link #COMPILER_SOURCE}.
	 *
	 * @param first first version to be compared
	 * @param second second version to be compared
	 * @return the value {@code 0} if both versions are the same;
	 * 			a value less than {@code 0} if <code>first</code> is smaller than <code>second</code>; and
	 * 			a value greater than {@code 0} if <code>first</code> is higher than <code>second</code>
	 * @since 3.12
	 */
	public static int compareJavaVersions(String first, String second) {
		return Long.compare(CompilerOptions.versionToJdkLevel(first), CompilerOptions.versionToJdkLevel(second));
	}
}
