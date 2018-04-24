package org.jboss.tools.ssp.api.beans;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.lsp4j.jsonrpc.Launcher;

public class SSPAttributes {
	private HashMap<String, String> types;
	private HashMap<String, String> descriptions;
	private HashMap<String, Object> defaultVals;
	
	public SSPAttributes() {
		types = new HashMap<>();
		descriptions = new HashMap<>();
		defaultVals = new HashMap<>();
	}
	
	public Set<String> listAttributes() {
		return types.keySet();
	}
	
	public Class getAttributeType(String key) {
		String ret1 = types.get(key);
		if( ret1 != null ) {
			try {
				return Class.forName(ret1);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String getAttributeDescription(String key) {
		return descriptions.get(key);
	}
	
	public Object getAttributeDefaultValue(String key) {
		return defaultVals.get(key);
	}
	
	public void addAttribute(String key, Class t, String d, Object defaultVal) {
		types.put(key,  t.getName());
		if( d != null ) {
			descriptions.put(key,  d);
		}
		if( defaultVal != null ) {
			defaultVals.put(key, defaultVal);
		}
	}
}
