/*************************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/

package org.jboss.tools.rsp.runtime.core.extract.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

public class UntarXZUtility extends UntarUtility{
	
	public UntarXZUtility(File file) {
		super(file);
	}
	
	@Override
	protected TarArchiveInputStream getTarArchiveInputStream(File file) throws IOException {
		return new TarArchiveInputStream(new XZCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));
	}

}
