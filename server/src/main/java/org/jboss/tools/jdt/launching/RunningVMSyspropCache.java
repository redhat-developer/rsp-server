package org.jboss.tools.jdt.launching;

import java.util.HashMap;
import java.util.Map;

public class RunningVMSyspropCache {
	public static RunningVMSyspropCache cache = new RunningVMSyspropCache();
	public static RunningVMSyspropCache getDefault() {
		return cache;
	}
	
	private HashMap<String, String> cachedValues = null;
	public RunningVMSyspropCache() {
		
	}
	
	public Map<String, String> getCachedValues() {
		return new HashMap<String, String>(cachedValues);
	}
	
	public Map<String, String> getCachedValues(String[] keys) {
		if( cachedValues == null )
			return null;
		if( cachedValues.size() < keys.length )
			return null;
		
		HashMap<String, String>  ret = new HashMap<String, String>();
		for( int i = 0; i < keys.length; i++ ) {
			String v = cachedValues.get(keys[i]);
			if( v == null )
				return null;
			ret.put(keys[i],  v);
		}
		return ret;
	}
	
	public void setCachedValues(Map<String, String> vals) {
		cachedValues = new HashMap<String,String>(vals);
	}
	
}
