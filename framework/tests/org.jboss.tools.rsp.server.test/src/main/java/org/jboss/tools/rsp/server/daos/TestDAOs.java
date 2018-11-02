/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;


/**
 * Check each DAO class for a 0-arg constructor, 
 * and a proper getter / setter for each and every
 * non-static field. 
 */
@RunWith(value = Parameterized.class)
public class TestDAOs extends TestCase {
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() throws ClassNotFoundException, IOException {
		 Class[] c = getClasses("org.jboss.tools.rsp.api.dao");
		 ArrayList<Object[]> list = new ArrayList<>();
		 for( int i = 0; i < c.length; i++ ) {
			 list.add(new Object[] {c[i]});
		 }
		 return list;
	 }

	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	protected Class dao;

	public TestDAOs(Class dao) {
		this.dao = dao;
	}
	
	@Test
	public void testConstructor() {
		Constructor[] cs = dao.getConstructors();
		for( int i = 0; i < cs.length; i++ ) {
			if( cs[i].getParameterCount() == 0 )
				return;
		}
		fail("No 0-arg constructor for class " + dao.getName());
	}
	
	@Test
	public void testGetters() {
		Field[] all = dao.getFields();
		for( int i = 0; i < all.length; i++ ) {
			Field f = all[i];
			if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			String name = f.getName();
			if( f.getType().equals(Boolean.class)) {
				Method m = findGetter("is"+name, f);
				if( m == null ) {
					m = findGetter("get" + name, f);
				}
				if( m == null ) {
					fail("Getter for field " + name + " not found");
				}
			} else {
				Method m = findGetter("get"+name, f);
				if( m == null ) {
					fail("Getter for field " + name + " not found");
				}
			}
		}
	}
	
	@Test
	public void testSetters() {
		Field[] all = dao.getFields();
		for( int i = 0; i < all.length; i++ ) {
			Field f = all[i];
			if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			String name = f.getName();
			Method m = findSetter("set"+name, f);
			if( m == null ) {
				fail("Setter for field " + name + " not found");
			}
		}
	}
	
	
	private Method findSetter(String name, Field f) {
		Method[] all = dao.getMethods();
		for( int i = 0; i < all.length; i++ ) {
			if (all[i].getName().equalsIgnoreCase(name) 
					&& all[i].getParameterTypes().length == 1
					&& all[i].getParameterTypes()[0].equals(f.getType())) {
				return all[i];
			}
		}
		return null;
	}

	private Method findGetter(String name, Field f) {
		Method[] all = dao.getMethods();
		for( int i = 0; i < all.length; i++ ) {
			if (all[i].getName().equalsIgnoreCase(name) 
					&& all[i].getParameterTypes().length == 0
					&& all[i].getReturnType().equals(f.getType())) {
				return all[i];
			}
		}
		return null;
	}
}
