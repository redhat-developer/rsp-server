package org.jboss.tools.ssp.api.beans;

public class VMDescription {
	private String id;
	private String installLocation;
	private String version;
	
	public VMDescription(String id, String il, String v) {
		this.id = id;
		this.installLocation = il;
		this.version = v;
	}

	public String getId() {
		return id;
	}

	public String getInstallLocation() {
		return installLocation;
	}

	public String getVersion() {
		return version;
	}
}
