/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.data.MapEntry;
import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.junit.Test;

public class AttributesTest {

	private Attribute intAttribute = new Attribute(Integer.class.getSimpleName(), "", null);
	private Attribute intAttributeWithDesc = new Attribute(Integer.class.getSimpleName(), "Some int attribute", null);
	private Attribute intAttributeWithDefault = new Attribute(Integer.class.getSimpleName(), "", Integer.valueOf(10));

	private Attribute stringAttribute = new Attribute(Boolean.class.getSimpleName(), "", null);
	private Attribute booleanAttribute = new Attribute(Boolean.class.getSimpleName(), "", null);
	private Attribute listAttribute = new Attribute(List.class.getSimpleName(), "", null);

	@SuppressWarnings("serial")
	private Attributes attributes1 = new Attributes(new HashMap<String, Attribute>() {{
		put("1", intAttribute);
		put("2", stringAttribute);
		put("3", booleanAttribute);
	}});

	@SuppressWarnings("serial")
	private Attributes attributes1Copy = new Attributes(new HashMap<String, Attribute>() {{
		put("1", intAttribute);
		put("2", stringAttribute);
		put("3", booleanAttribute);
	}});

	@SuppressWarnings("serial")
	private LinkedHashMap<String, Attribute> attributes1WithSupplementMap = new LinkedHashMap<String, Attribute>() {{
		put("1", intAttribute);
		put("2", stringAttribute);
		put("3", booleanAttribute);
		put("4", listAttribute);
	}};
	private Attributes attributes1WithSupplement = new Attributes(attributes1WithSupplementMap);

	@SuppressWarnings("serial")
	private Attributes attributes2 = new Attributes(new LinkedHashMap<String, Attribute>() {{
		put("1", stringAttribute);
		put("2", intAttribute);
		put("3", booleanAttribute);
	}});

	@Test
	public void testAttributeEquality() {
		assertThat(intAttribute).isEqualTo(intAttribute);

		assertThat(intAttribute).isNotEqualTo(intAttributeWithDesc);
		assertThat(intAttribute).isNotEqualTo(intAttributeWithDefault);

		assertThat(intAttribute).isNotEqualTo(stringAttribute);
		assertThat(stringAttribute).isNotEqualTo(intAttribute);
	}

	@Test
	public void testAttributesEquality() {
		assertThat(attributes1).isEqualTo(attributes1Copy);
		assertThat(attributes1Copy).isEqualTo(attributes1);

		assertThat(attributes1).isEqualTo(new Attributes(attributes1.getAttributes()));
		
		assertThat(attributes1).isNotEqualTo(attributes2);
		assertThat(attributes2).isNotEqualTo(attributes1);

		assertThat(attributes1).isNotEqualTo(attributes1WithSupplement);
		assertThat(attributes1WithSupplement).isNotEqualTo(attributes1);
	}

	@Test
	public void getAttributesRespectsOrdering() {
		@SuppressWarnings("unchecked")
		MapEntry<String, Attribute>[] mapEntries = attributes1WithSupplementMap.entrySet().stream()
			.map(entry -> MapEntry.entry(entry.getKey(), (Attribute) entry.getValue()))
			.toArray(MapEntry[]::new);
		assertThat(attributes1WithSupplement.getAttributes()).containsExactly(mapEntries);
	}

	@Test
	public void createAttributesViaUtilityRespectsOrdering() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		util.addAttribute("1", Integer.class.getSimpleName(), null, -1);
		util.addAttribute("2", String.class.getSimpleName(), null, "smurfette");
		util.addAttribute("3", Boolean.class.getSimpleName(), null, Boolean.TRUE);

		Attributes attributes = util.toPojo();
		assertThat(attributes.getAttributes()).containsExactly(
				MapEntry.entry("1", new Attribute(Integer.class.getSimpleName(), null, -1)),
				MapEntry.entry("2", new Attribute(String.class.getSimpleName(), null, "smurfette")),
				MapEntry.entry("3", new Attribute(Boolean.class.getSimpleName(), null, Boolean.TRUE))
		);
	}
	
	@Test
	public void createAttributesViaUtilityAndMapRespectsOrdering() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility(attributes1WithSupplement);

		Attributes attributes = util.toPojo();
		@SuppressWarnings("unchecked")
		MapEntry<String, Attribute>[] mapEntries = attributes1WithSupplementMap.entrySet().stream()
				.map(entry -> MapEntry.entry(entry.getKey(), (Attribute) entry.getValue()))
				.toArray(MapEntry[]::new);
		assertThat(attributes.getAttributes()).containsExactly(mapEntries);
	}
}
