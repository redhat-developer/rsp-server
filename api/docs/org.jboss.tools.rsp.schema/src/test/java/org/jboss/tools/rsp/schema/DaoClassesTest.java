/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.jboss.tools.rsp.api.schema.DaoClasses;
import org.junit.Before;
import org.junit.Test;

import daopackage.Attribute;
import daopackage.ClientCapabilitiesRequest;
import daopackage.CreateServerResponse;
import daopackage.Status;
import daopackage.subpackage1.DiscoveryPath;

public class DaoClassesTest {

	private TestableDaoClasses daoClasses;

	@Before
	public void before() {
		this.daoClasses = new TestableDaoClasses("daopackage");
	}

	@Test
	public void shouldDiscoverAllInPackage() throws IOException {
		// given
		// when
		Class<?>[] classes = daoClasses.getAll();
		// then
		assertThat(classes).containsExactlyInAnyOrder(
				Attribute.class,
				ClientCapabilitiesRequest.class,
				CreateServerResponse.class,
				Status.class);
	}

	@Test
	public void shouldNotDiscoverInSubpackage() throws IOException {
		// given
		// when
		Class<?>[] classes = daoClasses.getAll();
		// then
		assertThat(classes).doesNotContain(
				DiscoveryPath.class);
	}

	private class TestableDaoClasses extends DaoClasses {

		public TestableDaoClasses(String daoPackage) {
			super(daoPackage);
		}

	}

}
