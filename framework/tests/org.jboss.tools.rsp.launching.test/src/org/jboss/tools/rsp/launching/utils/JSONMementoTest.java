package org.jboss.tools.rsp.launching.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

public class JSONMementoTest {
	
	private static final String TEST_JSON_PATH = "resources/mementoTest.json";
	private static final String TEST_JSON_COPY_PATH = "resources/mementoTest2.json";
	
	@AfterClass
	public static void cleanUp() {
		File file = new File(TEST_JSON_COPY_PATH);
		if (file.exists()) {
			file.delete();
		}
	}
	
	@Test
	public void testSimpleJSON() throws IOException {
		JSONMemento memento = JSONMemento.createWriteRoot();
		memento.putString("string", "stringValue");
		memento.putBoolean("boolean", true);
		memento.putInteger("integer", 123);
		
		assertEquals("stringValue", memento.getString("string"));
		assertEquals(true, memento.getBoolean("boolean"));
		assertEquals(123, memento.getInteger("integer").intValue());
	}
	
	@Test
	public void testChildJSON() {
		JSONMemento memento = JSONMemento.createWriteRoot();
		memento.putString("rootPropKey", "rootPropValue");
		IMemento childMementoOne = memento.createChild("childOne");
		childMementoOne.putString("childStringKey", "childStringValue");
		IMemento childMementoTwo = memento.createChild("childTwo");
		childMementoTwo.putString("childStringKey", "childStringValue");
		
		assertEquals(2, memento.getChildren().length);
		assertEquals(1, memento.getChildren("childOne").length);
		assertEquals("childStringValue", memento.getChildren("childOne")[0].getString("childStringKey"));
		assertEquals(3, memento.getNames().size());
		assertTrue(memento.getNames().containsAll(Arrays.asList("rootPropKey", "childOne", "childTwo")));
	}
	
	@Test
	public void testFileLoadAndSave() throws IOException {
		JSONMemento memento = JSONMemento.loadMemento(new FileInputStream(new File(TEST_JSON_PATH)));
		
		assertEquals(2, memento.getChildren().length);
		assertEquals(1, memento.getChildren("childOne").length);
		assertEquals("childStringValue", memento.getChildren("childOne")[0].getString("childStringKey"));
		assertEquals(3, memento.getNames().size());
		assertTrue(memento.getNames().containsAll(Arrays.asList("rootPropKey", "childOne", "childTwo")));
		
		memento.saveToFile(TEST_JSON_COPY_PATH);
		
		assertArrayEquals(Files.readAllBytes(Paths.get(TEST_JSON_PATH)), Files.readAllBytes(Paths.get(TEST_JSON_COPY_PATH)));
	}

}
