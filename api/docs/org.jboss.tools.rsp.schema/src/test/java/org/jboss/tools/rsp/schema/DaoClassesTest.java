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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.assertj.core.api.HamcrestCondition;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.tools.rsp.api.schema.DaoClasses;
import org.junit.Before;
import org.junit.Test;

import daopackage.Attribute;
import daopackage.ClientCapabilitiesRequest;
import daopackage.CreateServerResponse;
import daopackage.Status;
import daopackage.subpackage1.DiscoveryPath;

public class DaoClassesTest {

	private static final String DAOCLASSES_JAR = "/daoclasses.jar";

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

	@Test
	public void shouldDiscoverAllInPackageInJar() throws Exception {
		// given
		addJarToClasspath(getClass().getResource(DAOCLASSES_JAR));
		DaoClasses daoClasses = new TestableDaoClasses("jardaopackage");
		// when
		Class<?>[] classes = daoClasses.getAll();
		// then
		assertThat(classes).areExactly(4, new HamcrestCondition<Class<?>>(new BaseMatcher<Class<?>>() {

			@Override
			public boolean matches(Object clazz) {
				if (!(clazz instanceof Class)) {
					return false;
				}
				String className = ((Class<?>) clazz).getName();
				return Arrays.asList(
						"jardaopackage.Attribute",
						"jardaopackage.ClientCapabilitiesRequest",
						"jardaopackage.CreateServerResponse",
						"jardaopackage.Status")
						.contains(className);
			}

			@Override
			public void describeTo(Description description) {				
			}
		}));
	}

	private class TestableDaoClasses extends DaoClasses {

		public TestableDaoClasses(String daoPackage) {
			super(daoPackage);
		}
	}

	private static void addJarToClasspath(URL url) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
	    method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { url });
	}
}
