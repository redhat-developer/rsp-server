/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/

package org.jboss.tools.rsp.server.spi.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class AlphanumComparatorTest {

	@Test
	public void testLastSegmentNumeric() {
		String[] pre = new String[] { "a.b.2", "a.b.1", "a.b.3"};
		String[] post = new String[] { "a.b.3", "a.b.2", "a.b.1"};
		testSorted(pre,post);
	}
	@Test
	public void testLastSegmentNumericHundreds() {
		String[] pre = new String[] { "a.b.200", "a.b.1", "a.b.3"};
		String[] post = new String[] { "a.b.200", "a.b.3", "a.b.1"};
		testSorted(pre,post);
	}
	@Test
	public void testLastSegmentNumericRogueZero() {
		String[] pre = new String[] { "a.b.200", "a.b.0201", "a.b.0105"};
		String[] post = new String[] { "a.b.0201", "a.b.200", "a.b.0105"};
		testSorted(pre,post);
	}

	@Test
	public void testLastSegmentNumericRogueZeros() {
		// This is a tough one to decide how to implement, but, 
		// if two segments are essentially the same (ie 0100 vs 100) 
		// then there must be leading zeros. In this case, the longer 
		// one is marked as smaller, as it would be if there was a string comparison
		// instead of a numeric comparison. 
		String[] pre = new String[] { "a.b.100.f.5", "a.b.0100.f.3", "a.b.00100.f.4"};
		String[] post = new String[] { "a.b.100.f.5", "a.b.0100.f.3", "a.b.00100.f.4"};
		testSorted(pre,post);
	}

	
	@Test
	public void testCDKStrings() {
		String[] pre = new String[] { "cdk v3.7.0", "cdk v3.2.0", "cdk v3.4.2", "cdk v3.4.1", "cdk v3.4.7"};
		String[] post = new String[] { "cdk v3.7.0", "cdk v3.4.7", "cdk v3.4.2", "cdk v3.4.1", "cdk v3.2.0"};
		testSorted(pre,post);
	}

	private void testSorted(String[] preSort, String[] expected) {
		AlphanumComparator c = new AlphanumComparator();
		List<String> l = Arrays.asList(preSort);
		Collections.sort(l,c);
		assertEquals(expected.length, l.size());
		for( int i = 0; i < expected.length; i++ ) {
			assertEquals(expected[i], l.get(i));
		}
	}
}
