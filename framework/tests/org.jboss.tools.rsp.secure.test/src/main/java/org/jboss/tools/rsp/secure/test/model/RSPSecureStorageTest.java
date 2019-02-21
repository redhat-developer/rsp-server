/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.secure.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.jboss.tools.rsp.secure.crypto.CryptoException;
import org.jboss.tools.rsp.secure.model.ISecureStorage.ISecureNode;
import org.jboss.tools.rsp.secure.model.RSPSecureStorage;
import org.junit.Test;

public class RSPSecureStorageTest {

	@Test
	public void testNonExistentProperties() throws IOException, CryptoException {
		File tmp = File.createTempFile("RSPSecureStorageTest", "1" + System.currentTimeMillis());
		tmp.delete();
		byte[] key = "reinventing the wheel".getBytes();
		RSPSecureStorage store = new RSPSecureStorage(tmp, key);
		store.load();

		ISecureNode node = store.getNode("testBundle");
		assertNotNull(node);
		ISecureNode inner = node.getChildNode("inner");

		assertNotNull(inner);
		assertEquals(true, inner.getBooleanProperty("dne", true));
		assertEquals(false, inner.getBooleanProperty("dne", false));
		assertEquals(1, inner.getIntegerProperty("dne", 1));
		assertEquals(-1, inner.getIntegerProperty("dne", -1));
		assertEquals("test", inner.getStringProperty("dne", "test"));
		assertEquals("test2", inner.getStringProperty("dne", "test2"));
	}

	@Test
	public void testAddProperties() throws IOException, CryptoException {
		File tmp = File.createTempFile("RSPSecureStorageTest", "2" + System.currentTimeMillis());
		tmp.delete();
		byte[] key = "reinventing the wheel".getBytes();
		RSPSecureStorage store = new RSPSecureStorage(tmp, key);
		store.load();
		
		ISecureNode node = store.getNode("testBundle");
		assertNotNull(node);
		ISecureNode inner = node.getChildNode("inner");
		assertNotNull(inner);
		
		// set boolean property
		inner.setBooleanProperty("key", true);
		assertEquals(true, inner.getBooleanProperty("key", true));
		assertEquals(true, inner.getBooleanProperty("key", false));
		
		// set integer property
		inner.setIntegerProperty("key", 5);
		assertEquals(5, inner.getIntegerProperty("key", 1));
		assertEquals(5, inner.getIntegerProperty("key", -1));
		
		inner.setStringProperty("key", "persist-me");
		assertEquals("persist-me", inner.getStringProperty("key", "test"));
		assertEquals("persist-me", inner.getStringProperty("key", "test2"));
	}

	@Test
	public void testSaveProperties() throws IOException, CryptoException {
		File tmp = File.createTempFile("RSPSecureStorageTest", "2" + System.currentTimeMillis());
		tmp.delete();
		byte[] key = "reinventwheel".getBytes();
		RSPSecureStorage store = new RSPSecureStorage(tmp, key);
		store.load();
		
		ISecureNode node = store.getNode("testBundle");
		assertNotNull(node);
		ISecureNode inner = node.getChildNode("inner");
		assertNotNull(inner);
		
		// set boolean property
		inner.setBooleanProperty("keyB", true);
		inner.setIntegerProperty("keyI", 5);
		inner.setStringProperty("keyS", "persist-me");
		try {
			store.save();
		} catch(CryptoException ioe) {
			ioe.printStackTrace();
			fail();
		}
		
		assertNotNull(tmp);
		assertTrue(tmp.exists());
		System.out.println(tmp.getAbsolutePath());
		
		
		RSPSecureStorage store2 = new RSPSecureStorage(tmp, key);
		store2.load();
		ISecureNode node2 = store2.getNode("testBundle");
		assertNotNull(node2);
		ISecureNode inner2 = node2.getChildNode("inner");
		assertNotNull(inner2);

		
		assertEquals(true, inner2.getBooleanProperty("keyB", false));
		assertEquals(5, inner2.getIntegerProperty("keyI", 1));
		assertEquals("persist-me", inner2.getStringProperty("keyS", "wrong"));

	}

	@Test
	public void testBadKey() throws IOException, CryptoException {
		File tmp = File.createTempFile("RSPSecureStorageTest", "3" + System.currentTimeMillis());
		tmp.delete();
		byte[] key = "reinventwheel".getBytes();
		RSPSecureStorage store = new RSPSecureStorage(tmp, key);
		store.load();
		
		ISecureNode node = store.getNode("testBundle");
		assertNotNull(node);
		ISecureNode inner = node.getChildNode("inner");
		assertNotNull(inner);
		
		// set boolean property
		inner.setBooleanProperty("keyB", true);
		inner.setIntegerProperty("keyI", 5);
		inner.setStringProperty("keyS", "persist-me");
		try {
			store.save();
		} catch(CryptoException ioe) {
			ioe.printStackTrace();
			fail();
		}
		
		assertNotNull(tmp);
		assertTrue(tmp.exists());
		System.out.println(tmp.getAbsolutePath());
		
		
		RSPSecureStorage store2 = new RSPSecureStorage(tmp, "differentkey".getBytes());
		try {
			store2.load();
			fail();
		} catch(CryptoException ce) {
			// expected
		}
		
		try {
			store2.getNode("testBundle");
			fail();
		} catch(CryptoException ce) {
			// expected
		}

		try {
			store2.propertyExists("testBundle/inner", "keyB");
			fail();
		} catch(CryptoException ce) {
			// expected
		}

		try {
			store2.save();
			fail();
		} catch(CryptoException ce) {
			// expected
		}
	}
}
