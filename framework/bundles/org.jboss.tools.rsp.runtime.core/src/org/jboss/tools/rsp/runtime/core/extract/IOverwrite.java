/*******************************************************************************
  * Copyright (c) 2009 - 2013 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.jboss.tools.rsp.runtime.core.extract;

import java.io.File;

public interface IOverwrite {
	
	public static final int YES = 0;
	public static final int ALL = 1;
	public static final int NO = 2;
	public static final int NO_ALL = 3;
	public static final int CANCEL = 4;
    
    int overwrite(File file);
}
