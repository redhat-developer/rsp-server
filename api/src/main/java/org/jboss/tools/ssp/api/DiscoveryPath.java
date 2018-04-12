package org.jboss.tools.ssp.api;

public class DiscoveryPath {
	private String filepath;

	public DiscoveryPath(String filepath) {
		super();
		this.setFilepath(filepath);
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	@Override
	public int hashCode() {
		return filepath.hashCode();
	}
	
}
