/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;

/**
 * A 1.1.x VM
 */
public class Standard11xVM extends StandardVM {

	public Standard11xVM(IVMInstallType type, String id) {
		super(type, id);
	}


	/**
	 * @see org.eclipse.jdt.launching.IVMInstall#getVMRunner(String)
	 */
	@Override
	public IVMRunner getVMRunner(String mode) {
		if ("run".equals(mode)) {
			return new Standard11xVMRunner(this);
		}
		return null;
	}


}

