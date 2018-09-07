/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Objects;
import org.jboss.tools.rsp.api.dao.util.EqualsUtility;

public class ServerLaunchMode {
	private String mode;
	private String desc;

	public ServerLaunchMode() {

	}

	public ServerLaunchMode(String mode, String desc) {
		this.mode = mode;
		this.desc = desc;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ServerLaunchMode)) {
                return false;
            }
            ServerLaunchMode temp = (ServerLaunchMode) o;
            return EqualsUtility.areEqual(this.mode, temp.mode)
                    && EqualsUtility.areEqual(this.desc, temp.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mode, desc);
        }
}
