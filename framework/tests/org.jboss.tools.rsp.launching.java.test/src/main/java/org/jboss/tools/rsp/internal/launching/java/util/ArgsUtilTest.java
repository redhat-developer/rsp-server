/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.rsp.internal.launching.java.util;

import org.jboss.tools.rsp.launching.java.ArgsUtil;

import junit.framework.TestCase;

public class ArgsUtilTest extends TestCase {
	private static String QUOTE = "\"";
	private static String EQ = "=";

	public void testParse() {
		assertEquals(1, ArgsUtil.parse("").length);
		assertEquals(1, ArgsUtil.parse("a").length);
		assertEquals(1, ArgsUtil.parse("abc ").length);
		assertEquals(1, ArgsUtil.parse(" abcde ").length);
		assertEquals(1, ArgsUtil.parse("   abcde ").length);
		assertEquals(1, ArgsUtil.parse("   abcde   ").length);
		assertEquals(2, ArgsUtil.parse("a b").length);
		assertEquals(2, ArgsUtil.parse("a  b").length);
		assertEquals(2, ArgsUtil.parse(" a  b").length);
		assertEquals(2, ArgsUtil.parse(" a b  ").length);
		assertEquals(2, ArgsUtil.parse("-b test  ").length);
		assertEquals(2, ArgsUtil.parse("-b --host=someval  ").length);
		assertEquals(2, ArgsUtil.parse("-b \"--host=some val\"").length);
		assertEquals(3, ArgsUtil.parse("-b val --host=someval").length);
		assertEquals(2, ArgsUtil.parse("-b --host=\"some val\"").length);
		assertEquals(2, ArgsUtil.parse("-b \"--host=some val\"").length);
	}
	
	public void testShortArgMatch() {
		assertTrue(ArgsUtil.matchesShortArg("-b", split("-b -c -d -f")));
		assertFalse(ArgsUtil.matchesShortArg("-b", split("-c opt d two -f")));
		assertFalse(ArgsUtil.matchesShortArg("-b", split("b")));
		assertFalse(ArgsUtil.matchesShortArg("-b", split("b opt")));
		assertTrue(ArgsUtil.matchesShortArg("-b", split("-b opt")));

		assertTrue(ArgsUtil.matchesShortArg("-host", split("-host -c -d -f")));
		assertFalse(ArgsUtil.matchesShortArg("-host", split("-c val d two -f")));
		assertFalse(ArgsUtil.matchesShortArg("-host", split("host")));
		assertFalse(ArgsUtil.matchesShortArg("-host", split("host val")));
		assertTrue(ArgsUtil.matchesShortArg("-host", split("-host val")));
	}

	public void testLongArgMatch() {
		assertTrue(ArgsUtil.matchesLongArg("--host=localhost", split("--host --longopt1 -longopt2 -f")));
		assertFalse(ArgsUtil.matchesLongArg("-host=localhost", split("--host --longopt1 -longopt2 -f")));
		assertTrue(ArgsUtil.matchesLongArg("-longopt2=v1", split("--host --longopt1 -longopt2 -f")));
		assertFalse(ArgsUtil.matchesLongArg("-longopt2 v1", split("--host --longopt1 -longopt2 -f")));
	}
	
	public void testReplace() {
		String allArgs = "";
		allArgs = ArgsUtil.setArg(allArgs, "-h", null, "new");
		assertTrue(ArgsUtil.parse(allArgs).length == 2);
		assertTrue(ArgsUtil.getValue(allArgs, "-h", null).equals("new"));
		allArgs = ArgsUtil.setArg(allArgs, "-h", null, "correct");
		assertTrue(ArgsUtil.parse(allArgs).length == 2);
		assertTrue(ArgsUtil.getValue(allArgs, "-h", null).equals("correct"));

		allArgs = ArgsUtil.setArg(allArgs, null, "--two", "newtwo");
		assertTrue(ArgsUtil.parse(allArgs).length == 3);
		
		// opt was set as long opt,  --two=newtwo.   Search for short opt fails
		assertTrue(ArgsUtil.getValue(allArgs, "--two", null) == null);
		
		// clear long opt
		allArgs = ArgsUtil.setArg(allArgs, null, "--two", null);
		assertTrue(ArgsUtil.parse(allArgs).length == 2);
		

		// test replacement of short args
		allArgs = ArgsUtil.setArg(allArgs, new String[]{"-h", "-o"}, new String[]{}, "twoOpt", false);
		assertTrue(ArgsUtil.parse(allArgs).length == 2); // no change
		
		assertTrue(ArgsUtil.getValue(allArgs, "-h", null).equals("twoOpt"));
		assertTrue(ArgsUtil.getValue(allArgs, new String[]{"-h", "-o"}, null).equals("twoOpt"));
		assertTrue(ArgsUtil.getValue(allArgs, new String[]{"-o", "-h"}, null).equals("twoOpt"));
		
		// test clear of short arg
		allArgs = ArgsUtil.setArg(allArgs, new String[]{"-h", "-o"}, new String[]{}, null, false);
		assertTrue(ArgsUtil.parse(allArgs).length == 1); // no change
		
		allArgs = ArgsUtil.setArg(allArgs, null, "--three", "three");
		assertTrue(ArgsUtil.getValue(allArgs, null, "--three").equals("three"));
		
		allArgs = ArgsUtil.setArg(allArgs, null, "--three", "threea");
		assertTrue(ArgsUtil.getValue(allArgs, null, "--three").equals("threea"));
		
		// already has --three,  test replace, ensure --four is the new arg 
		// since it is the first of the two acceptable replacements 
		assertTrue(allArgs.contains("--three"));
		allArgs = ArgsUtil.setArg(allArgs, null, new String[]{"--four", "--three"}, "three_b", false);
		assertTrue(allArgs.contains("--four"));
		assertTrue(ArgsUtil.getValue(allArgs, null, "--three") == null);
		assertTrue(ArgsUtil.getValue(allArgs, null, "--four").equals("three_b"));
		assertTrue(ArgsUtil.getValue(allArgs, null, new String[]{"--three", "--four"}).equals("three_b"));
		
	}
	
	
	public void testOuterQuoteMultipleSet() {
		String argId = "-Dtest";
		String folder = "my folder";
		String args = QUOTE + argId + EQ + folder + QUOTE;
		String args2 = ArgsUtil.setArg(args, null, argId, folder);
		assertTrue(args2.trim().equals(args));

		String args3 = ArgsUtil.setArg(args2, null, argId, folder + "2");
		assertTrue(args3.trim().equals(QUOTE + argId + EQ + folder + "2" + QUOTE));
	}

	public void testInnerQuotesMultipleSet() {
		String argId = "-Dtest";
		String folder = "my folder";
		String args = argId + EQ + QUOTE + folder + QUOTE;
		String args2 = ArgsUtil.setArg(args, null, argId, folder + "2");
		assertTrue(args2.trim().equals(argId + EQ + QUOTE + folder + "2" + QUOTE));
		args2 = ArgsUtil.setArg(args2, null, argId, folder + "3");
		assertTrue(args2.trim().equals(argId + EQ + QUOTE + folder + "3" + QUOTE));
	}
	
	public void testSetToNulLWithQuotes() {
		String argId = "-Dtest";
		String folder = "my folder";
		
		String args = QUOTE + argId + EQ + folder + QUOTE;
		String args2 = ArgsUtil.setArg(args, null, argId, null, true);
		assertTrue(args2.trim().equals(""));
	}

	// Just for testing, simply split this string into a bunch of options. 
	// So I don't need to make new arrays all the time... 
	public String[] split(String val) {
		return val.split(" ");
	}
	
	public void testSettingFlag() {
		String original = "-Dsome.name=yes -server -Darg2=no";
		String[] asArray = ArgsUtil.parse(original);
		assertEquals(3, asArray.length);
		String cleared = ArgsUtil.clearFlag(original, "-server");
		assertEquals("-Dsome.name=yes -Darg2=no", cleared);
		String set = ArgsUtil.setFlag(cleared, "-ser3");
		assertEquals("-Dsome.name=yes -Darg2=no -ser3", set);
	}
}
