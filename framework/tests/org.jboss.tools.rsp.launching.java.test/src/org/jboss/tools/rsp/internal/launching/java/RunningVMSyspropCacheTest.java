/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.internal.launching.java.RunningVMSyspropCache;
import org.junit.Before;
import org.junit.Test;

public class RunningVMSyspropCacheTest {

	private RunningVMSyspropCache cache;

	@Before
	public void before() {
		this.cache = RunningVMSyspropCache.getDefault();
		cache.clear();
	}

	@Test
	public void returnsNullIfCacheIsEmpty() {
		// given
		// when
		Map<String, String> cachedValues = cache.getCachedValues();

		assertNull(cachedValues);
	}

	@Test
	public void returnsNullIfEmptyCacheWasSet() {
		// given
		cache.setCachedValues(new HashMap<>());
		// when
		Map<String, String> cachedValues = cache.getCachedValues();

		assertNull(cachedValues);
	}

	@Test
	public void returnsValuesThatWereSet() {
		// given
		assertTrue(cache.getCachedValues() == null);
		@SuppressWarnings("serial")
		Map<String, String> cached = new HashMap<String, String>() {{
			put("blue", "papa smurf");
			put("blue too", "smurfette");
		}};
		// when
		cache.setCachedValues(cached);
		Map<String, String> cachedValues = cache.getCachedValues();
		// then
		assertThat(cached.entrySet(), equalTo(cachedValues.entrySet()));
	}

	@Test
	public void returnsNullIfKeysNotCached() {
		// given
		@SuppressWarnings("serial")
		Map<String, String> cached = new HashMap<String, String>() {{
			put("blue", "papa smurf");
			put("blue too", "smurfette");
		}};
		cache.setCachedValues(cached);
		// when
		Map<String, String> cachedValues = cache.getCachedValues( new String[] { "red", "blue" } );
		// then
		assertTrue(cachedValues == null);
	}

	@Test
	public void returnsCachedValuesIfAllKeysAreCached() {
		// given
		@SuppressWarnings("serial")
		Map<String, String> cached = new HashMap<String, String>() {{
			put("blue", "papa smurf");
			put("blue too", "smurfette");
		}};
		cache.setCachedValues(cached);
		// when
		Map<String, String> cachedValues = cache.getCachedValues(cached.keySet().toArray(new String[] {}));
		// then
		assertArrayEquals(cached.keySet().toArray(new String[] {}), 
				cachedValues.keySet().toArray(new String[] {}));
	}

}
