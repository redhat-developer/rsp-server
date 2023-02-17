/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.jboss.tools.rsp.server.ServerTestActivator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * Check each DAO class for a 0-arg constructor, 
 * and a proper getter / setter for each and every
 * non-static field. 
 */
@RunWith(value = Parameterized.class)
public class ValidateDAOsTest {
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() throws ClassNotFoundException, IOException {
		 Class[] c = getClasses("org.jboss.tools.rsp.api.dao");
		 if( c == null || c.length == 0 ) 
			 fail("Test should find DAOs.");
		 ArrayList<Object[]> list = new ArrayList<>();
		 for( int i = 0; i < c.length; i++ ) {
			 list.add(new Object[] {c[i]});
		 }
		 return list;
	 }

	 private static Bundle getBundle(BundleContext bundleContext, String symbolicName) {
		    Bundle result = null;
		    if( bundleContext == null )
		    	return null;
		    for (Bundle candidate : bundleContext.getBundles()) {
		        if (candidate.getSymbolicName().equals(symbolicName)) {
		            if (result == null || result.getVersion().compareTo(candidate.getVersion()) < 0) {
		                result = candidate;
		            }
		        }
		    }
		    return result;
		}
	 
	private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		Bundle bund = getBundle(ServerTestActivator.getContext(), "org.jboss.tools.rsp.api");
		String loc = bund.getLocation();
		Enumeration<URL>  ents = bund.findEntries("org/jboss/tools/rsp/api/dao/", "*", false);
		ArrayList<Class> classes = new ArrayList<>();
		while(ents.hasMoreElements()) {
			URL u = ents.nextElement();
			String p = u.getPath();
			if( p.endsWith(".class")) {
				String fName =  p.substring(p.lastIndexOf("/")+1);
				fName = fName.substring(0, fName.length() - 6);
				String className = packageName + "." + fName;
				Class c = Class.forName(className);
				classes.add(c);
			}
		}
		return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	protected Class dao;

	public ValidateDAOsTest(Class dao) {
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
			if (all[i].getName().equalsIgnoreCase(name) && all[i].getParameterTypes().length == 0
					&& all[i].getReturnType().equals(f.getType())) {
				return all[i];
			}
		}
		return null;
	}
}
