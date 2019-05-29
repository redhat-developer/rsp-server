/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.launching.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;
import org.junit.Test;

public class MementoTest {
	
	private static final String TEST_JSON_PATH = "resources/mementoTest.json";
	private static final String TEST_XML_PATH = "resources/mementoTest.xml";

	@Test
	public void testSimpleJSON() throws IOException {
		testSimpleMemento(JSONMemento.createWriteRoot());
	}

	@Test
	public void testSimpleXML() throws IOException {
		testSimpleMemento(XMLMemento.createWriteRoot("root"));
	}

	private void testSimpleMemento(IMemento memento) {
		memento.putString("string", "stringValue");
		memento.putBoolean("boolean", true);
		memento.putInteger("integer", 123);
		
		assertEquals("stringValue", memento.getString("string"));
		assertEquals(true, memento.getBoolean("boolean"));
		assertEquals(123, memento.getInteger("integer").intValue());
	}

	@Test
	public void testChildJSON() {
		testChildMemento(JSONMemento.createWriteRoot());
	}

	@Test
	public void testChildXML() {
		testChildMemento(XMLMemento.createWriteRoot("root"));
	}

	private void testChildMemento(IMemento memento) {
		memento.putString("rootPropKey", "rootPropValue");
		IMemento childMementoOne = memento.createChild("childOne");
		childMementoOne.putString("childStringKey", "childStringValue");
		IMemento childMementoTwo = memento.createChild("childTwo");
		childMementoTwo.putString("childStringKey", "childStringValue");
		
		assertEquals(2, memento.getChildren().length);
		assertEquals(1, memento.getChildren("childOne").length);
		assertEquals("childStringValue", memento.getChildren("childOne")[0].getString("childStringKey"));
		assertEquals(1, memento.getNames().size());
		assertTrue(memento.getNames().containsAll(Arrays.asList("rootPropKey")));
	}

	@Test
	public void testFileLoadAndSaveJSON() throws IOException {
		testFileLoadSave(JSONMemento.loadMemento(new FileInputStream(new File(TEST_JSON_PATH))), TEST_JSON_PATH);
	}

	@Test
	public void testFileLoadAndSaveXML() throws IOException {
		testFileLoadSave(XMLMemento.loadMemento(new FileInputStream(new File(TEST_XML_PATH))), TEST_XML_PATH);
	}

	private void testFileLoadSave(IMemento memento, String path) throws FileNotFoundException, IOException {
		String copyFilename = path + "_copy";

		try {
			assertEquals(2, memento.getChildren().length);
			assertEquals(1, memento.getChildren("childOne").length);
			assertEquals("childStringValue", memento.getChildren("childOne")[0].getString("childStringKey"));
			assertEquals(1, memento.getNames().size());
			assertTrue(memento.getNames().containsAll(Arrays.asList("rootPropKey")));
			
			memento.saveToFile(copyFilename);
			
			String original = new String(Files.readAllBytes(Paths.get(path)));
			String o2 = original.replaceAll("\\s","");
			String actual = new String(Files.readAllBytes(Paths.get(copyFilename)));
			String a2 = actual.replaceAll("\\s","");
			
			assertEquals(o2, a2);
		} finally {
			File file = new File(copyFilename);
			if (file.exists()) {
				file.delete();
			}
		}
		
	}

}
