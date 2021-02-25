package org.jboss.tools.rsp.server.generic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.TemplateExtensionModelUtility;
import org.junit.Test;

public class TemplateExtensionModelUtilityTest {
	@Test
	public void testModelMerge() {
		
		String def = getServerDefinition();
		JSONMemento mem = JSONMemento.createReadRoot(new ByteArrayInputStream(def.getBytes()));
		JSONMemento result = TemplateExtensionModelUtility.generateEffectiveMemento(mem);
		
		assertNull(result.getChild("templates"));
		assertNotNull(result.getChild("serverTypes"));
		assertTrue(result.getChildren().length == 1);
		JSONMemento serverTypes = result.getChild("serverTypes");
		assertNotNull(serverTypes);
		JSONMemento[] allServerTypes = serverTypes.getChildren();
		assertNotNull(allServerTypes);
		assertEquals(allServerTypes.length, 1);
		JSONMemento wonka = allServerTypes[0];
		assertNotNull(wonka.getString("template"));
		
		JSONMemento[] kiddos = wonka.getChildren();
		assertNotNull(kiddos);
		assertEquals(kiddos.length, 3);
		
		JSONMemento kiddo = wonka.getChild("kiddo");
		assertNotNull(kiddo);
		JSONMemento kiddoTest = kiddo.getChild("test");
		assertNotNull(kiddoTest);
		String overrideMeVal = kiddoTest.getString("overrideMe");
		assertNotNull(overrideMeVal);
		assertEquals(overrideMeVal, "overriddenValue");
		assertNotNull(kiddoTest.getString("onlyInTemplate"));
		assertEquals(kiddoTest.getString("onlyInTemplate"), "templateVal");
		assertNotNull(kiddoTest.getString("onlyInDefinition"));
		assertEquals(kiddoTest.getString("onlyInDefinition"), "onlyDefinedVal");
		
		JSONMemento kiddo2 = wonka.getChild("kiddo2");
		assertNotNull(kiddo2);
		assertNotNull(kiddo2.getString("kiddo2key"));
		assertEquals(kiddo2.getString("kiddo2key"), "kiddo2val");
		JSONMemento kiddo2Grand = kiddo2.getChild("grandkiddo");
		assertNotNull(kiddo2Grand);
		assertNotNull(kiddo2Grand.getString("grandkiddoname"));
		assertEquals(kiddo2Grand.getString("grandkiddoname"), "grandkiddoname");

	
		JSONMemento kiddo3 = wonka.getChild("kiddo3");
		assertNotNull(kiddo3);
		assertNotNull(kiddo3.getString("kiddo3key"));
		assertEquals(kiddo3.getString("kiddo3key"), "kiddo3val");
		JSONMemento kiddo3Grand = kiddo3.getChild("grandkiddo3");
		assertNotNull(kiddo3Grand);
		assertNotNull(kiddo3Grand.getString("grandkiddoname3"));
		assertEquals(kiddo3Grand.getString("grandkiddoname3"), "grandkiddoname3");

	}
	
	
	private String getServerDefinition() {
		return "{\n"
				+ "  \"templates\": {\n"
				+ "    \"wonka.template\": {\n"
				+ "      \"kiddo\": {\n"
				+ "        \"test\": {\n"
				+ "          \"overrideMe\": \"kiddoTemplateVal\",\n"
				+ "          \"onlyInTemplate\": \"templateVal\"\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"kiddo2\": {\n"
				+ "        \"kiddo2key\": \"kiddo2val\",\n"
				+ "        \"grandkiddo\": {\n"
				+ "          \"grandkiddoname\": \"grandkiddoname\"\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  },\n"
				+ "  \"serverTypes\": {\n"
				+ "    \"wonka1\": {\n"
				+ "      \"template\": \"wonka.template\",\n"
				+ "      \"kiddo\": {\n"
				+ "        \"test\": {\n"
				+ "          \"overrideMe\": \"overriddenValue\",\n"
				+ "          \"onlyInDefinition\": \"onlyDefinedVal\"\n"
				+ "        }\n"
				+ "      },\n"
				+ "      \"kiddo3\": {\n"
				+ "        \"kiddo3key\": \"kiddo3val\",\n"
				+ "        \"grandkiddo3\": {\n"
				+ "          \"grandkiddoname3\": \"grandkiddoname3\"\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
	}
}
