/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.daos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.jboss.tools.rsp.api.dao.Attribute;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.junit.Test;

public class AttributesEqualityTest {

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
	private Attributes attributes1WithSupplement = new Attributes(new HashMap<String, Attribute>() {{
		put("1", intAttribute);
		put("2", stringAttribute);
		put("3", booleanAttribute);
		put("4", listAttribute);
	}});

	@SuppressWarnings("serial")
	private Attributes attributes2 = new Attributes(new HashMap<String, Attribute>() {{
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
}
