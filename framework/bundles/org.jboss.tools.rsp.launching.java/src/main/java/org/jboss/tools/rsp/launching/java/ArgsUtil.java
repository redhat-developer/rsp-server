/******************************************************************************* 
 * Copyright (c) 2011-2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.launching.java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.eclipse.debug.core.ArgumentUtils;

/**
 * @since 2.4
 */
public class ArgsUtil {

	public static final Integer NO_VALUE = Integer.valueOf(-1); 
	public static final String EQ = "="; //$NON-NLS-1$
	public static final String SPACE=" "; //$NON-NLS-1$
	public static final String VMO = "-D"; //$NON-NLS-1$
	public static final String EMPTY=""; //$NON-NLS-1$
	public static final String QUOTE="\""; //$NON-NLS-1$
	
	private ArgsUtil() {
		// Do nothing
	}
	
	public static String[] parse(String s) {
		if( s != null && s.isEmpty()) {
			return new String[] {""};
		}
		return ArgumentUtils.splitArguments(s);
	}

	public static Map<String, Object> getSystemProperties(String argString) {
		String[] args = parse(argString);
		HashMap<String, Object> map = new HashMap<>();
		
		for( int i = 0; i < args.length; i++ ) {
			if( args[i].startsWith(VMO)) {
				int eq = args[i].indexOf(EQ);
				if( eq != -1 ) {
					map.put(args[i].substring(2, eq), 
							args[i].substring(eq+1));
				} else {
					map.put(args[i], NO_VALUE);
				}
			}
		}
		return map;
	}

	public static String setSystemProperty(String arguments, String propKey, String propVal) {
		Map<String, Object> sysprops = getSystemProperties(arguments);
		if( sysprops.containsKey(propKey)) {
			String[] args = parse(arguments);
			for( int i = 0; i < args.length; i++ ) {
				if( args[i].startsWith(VMO + propKey + EQ)) {
					args[i] = VMO + propKey + EQ + propVal;
				}
			}
			return argsToString(args);
		} else {
			return arguments.trim() + SPACE + VMO + propKey + EQ + QUOTE + propVal + QUOTE;
		}
	}

	public static String getValue(String allArgs, String shortOpt, String longOpt) {
		return getValue(parse(allArgs), shortOpt, longOpt);
	}
	
	public static String getValue(String[] args, String shortOpt, String longOpt ) {
		String[] shortOpt2 = shortOpt == null ? new String[0] : new String[]{shortOpt};
		String[] longOpt2 = longOpt == null ? new String[0] : new String[]{longOpt};
		return getValue(args,shortOpt2,longOpt2);
	}

	public static String getValue(String allArgs, String[] shortOpt, String[] longOpt) {
		return getValue(parse(allArgs), shortOpt, longOpt);
	}
	
	public static String getValue(String[] args, String[] shortOpt, String[] longOpt ) {
		for( int i = 0; i < args.length; i++ ) {
			if( shortOpt != null && matchesShortArg(args[i], shortOpt) && i+1 < args.length)
				return args[i+1];
			if( longOpt != null && matchesLongArg(args[i], longOpt)) 
				return args[i].substring(args[i].indexOf(EQ) + 1);
		}
		return null;
	}

	public static boolean matchesShortArg(String needle, String[] haystack) {
		if( haystack == null )
			return false;
		return Arrays.asList(haystack).contains(needle);
	}
	
	public static boolean matchesLongArg(String needle, String[] haystack) {
		if( haystack == null )
			return false;
		for( int i = 0; i < haystack.length; i++ ) {
			if( needle.startsWith(haystack[i] + EQ) || needle.startsWith(QUOTE + haystack[i] + EQ))
				return true;
		}
		return false;
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value ) {
		if( value != null && value.contains(SPACE)) {
			boolean startsWith = value.startsWith(QUOTE);
			boolean endsWith = value.endsWith(QUOTE);
			boolean and = startsWith && endsWith;
			if( !and )
				value = QUOTE + value + QUOTE;
		}
		return setArg(allArgs, shortOpt, longOpt, value, false);
	}
	
	public static String setArg(String allArgs, String shortOpt, String longOpt, String value, boolean addQuotes ) {
		String[] shortOpt2 = shortOpt == null ? new String[0] : new String[]{shortOpt};
		String[] longOpt2 = longOpt == null ? new String[0] : new String[]{longOpt};
		return setArg(allArgs, shortOpt2, longOpt2, value, addQuotes);
	}
	
	/**
	 * Replace (or add) an argument. 
	 * Parse through the "allArgs" parameter to create a list of arguments.
	 * Compare each element in allArgs until you find a match against 
	 * one of the short argument (-b value) or long argument (--host=etcetcetc)
	 * patterns. The set of short and long form arguments should be 100% interchangeable,
	 * and the caller must not have a preference which is ultimately returned. 
	 * 
	 * If a match is found, and the match is in the short-form arguments, 
	 * do not change the arg (-b value), but update the value in the next segment. 
	 * 
	 * If a match is found and it is a long form argument, replace the string
	 * (ex:  --host=localhost) with the first longOpt (--newLongOpt=127.0.0.1)
	 * 
	 * @param allArgs
	 * @param shortOpt
	 * @param longOpt An array of possible long-form options
	 * @param value The new value, or null if you want to clear the option
	 * @param addQuotes
	 * @return
	 */
	public static String setArg(String allArgs, String[] shortOpt, String[] longOpt, String value, boolean addQuotes ) {
		String rawValue = (value != null && addQuotes) ? value : getRawValue(value);
		value = (value != null && addQuotes) ? (QUOTE + value + QUOTE) : value;

		boolean found = false;
		String[] args = parse(allArgs);
		
		for( int i = 0; i < args.length && !found; i++ ) {
			if( matchesShortArg(args[i], shortOpt)) {
				if( value == null ) {
					args[i] = null;
				}
				args[i+1] = value;
				found = true;
			} else if( matchesLongArg(args[i], longOpt)) {
				if( rawValue == null ) {
					args[i] = null;
				} else {
					String newVal = args[i].startsWith(QUOTE) ? 
							(QUOTE + longOpt[0] + EQ + rawValue + QUOTE) 
							: longOpt[0] + EQ + value;
					args[i] = newVal;
				}
				found = true;
			}
		}
		
		if( found ) {
			return argsToString(args).trim();
		}
		String suffix = getSetArgAddition(shortOpt, longOpt, value);
		String ret = (argsToString(args).trim() + SPACE + suffix).trim();
		return ret;
	}
	
	private static String getSetArgAddition(String[] shortOpt, String[] longOpt, String value) {
		if( longOpt != null && longOpt.length > 0 ) 
			return longOpt[0] + EQ + value;
		else if( shortOpt != null && shortOpt.length > 0 ) 
			return shortOpt[0] + SPACE + value;
		return "";
	}
	
	private static String argsToString(String[] args) {
		StringBuilder retVal = new StringBuilder();
		for( int i = 0; i < args.length; i++ ) {
			if( args[i] != null ) {
				retVal.append(args[i]);
				retVal.append(SPACE);
			}
		}
		return retVal.toString();
	}
	
	private static String getRawValue(String original) {
		if( original != null && original.startsWith(QUOTE) && original.endsWith(QUOTE)) {
			original = original.substring(1);
			original = original.substring(0, original.length()-1);
		}
		return original;
	}
	
	public static String setFlag(String original, String flagName) {
		if( original.startsWith(flagName + SPACE ) || original.contains(SPACE + flagName + SPACE) || original.endsWith(flagName)) 
			return original;
		return original.trim() + SPACE + flagName;
	}
	
	public static String clearFlag(String original, String flagName) {
		if( original.trim().startsWith(flagName + SPACE))
			return original.trim().substring(flagName.length()).trim();
		
		if( original.contains(SPACE + flagName + SPACE)) { 
			return original.replace(SPACE + flagName + SPACE, SPACE).trim();
		}
		
		if( original.trim().endsWith(SPACE + flagName)) {
			return original.trim().substring(0, original.trim().length() - flagName.length()).trim();
		}
		if( original.trim().equals(flagName)) {
			return ""; //$NON-NLS-1$
		}
		return original.trim();
	}
}