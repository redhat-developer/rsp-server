/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class DeployableState {
	private DeployableReference reference;
	private int state;
	private int publishState;
	
	/* required for gson reflective instantiation */
	public DeployableState() {
	}

	public DeployableState(DeployableReference reference, int state, int publishState) {
		this.reference = reference;
		this.state = state;
		this.publishState = publishState;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getPublishState() {
		return publishState;
	}

	public void setPublishState(int publishState) {
		this.publishState = publishState;
	}

	public DeployableReference getReference() {
		return reference;
	}

	public void setReference(DeployableReference reference) {
		this.reference = reference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + publishState;
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		result = prime * result + state;
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
		DeployableState other = (DeployableState) obj;
		if (publishState != other.publishState)
			return false;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
}
