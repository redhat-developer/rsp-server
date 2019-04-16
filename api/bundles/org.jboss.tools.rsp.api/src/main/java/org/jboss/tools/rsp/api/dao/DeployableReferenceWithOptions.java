/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

import java.util.Map;

import org.jboss.tools.rsp.api.dao.util.EqualsUtility;
import org.jboss.tools.rsp.api.dao.util.Optional;

public class DeployableReferenceWithOptions {
	private DeployableReference reference;
	@Optional
	private Map<String, Object> options;

	public DeployableReferenceWithOptions() {
	}

	public DeployableReferenceWithOptions(DeployableReference ref, Map<String, Object> options) {
		this.reference = ref;
		this.options = options;
	}

	public DeployableReference getReference() {
		return reference;
	}

	public void setReference(DeployableReference reference) {
		this.reference = reference;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reference== null) ? 0 : reference.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeployableReferenceWithOptions other = (DeployableReferenceWithOptions) obj;
		return EqualsUtility.areEqual(reference, other.reference) && EqualsUtility.areEqual(options, other.options);
	}

}
