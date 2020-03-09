/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.server.spi.publishing.IPublishController;

/**
 * This interface shouldn't really exist. The listDeploymentOptions 
 * method should rightly belong in IPublishController, but until 
 * such change is made, we need to use this enhanced interface.
 * 
 */
public interface IPublishControllerWithOptions extends IPublishController {
	public Attributes listDeploymentOptions();
}
