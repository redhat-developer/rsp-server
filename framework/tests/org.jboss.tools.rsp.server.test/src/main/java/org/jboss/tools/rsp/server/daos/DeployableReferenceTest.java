/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.junit.Before;
import org.junit.Test;

public class DeployableReferenceTest {

	private DeployableReference reference;

	private static final String LABEL = "papa-smurf";
	private static final String PATH = "/in/da/house";
	
	@Before	
	public void before() {
		this.reference = new DeployableReference(LABEL, PATH);
	}

	@Test
	public void equalIfLabelAndPathAreEqual() {
		DeployableReference reference2 = new DeployableReference(LABEL, PATH);
		assertEquals(reference, reference2);
	}

	@Test
	public void nonEqualIfDifferentPath() {
		DeployableReference reference2 = new DeployableReference(LABEL, "/in/da/woods");
		assertNotEquals(reference, reference2);
	}

	@Test
	public void nonEqualIfDifferentLabel() {
		DeployableReference reference2 = new DeployableReference("smurfette", PATH);
		assertNotEquals(reference, reference2);
	}
}
