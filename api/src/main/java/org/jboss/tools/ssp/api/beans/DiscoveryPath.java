package org.jboss.tools.ssp.api.beans;

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
	
	public boolean equals(Object other) {
		if( other instanceof DiscoveryPath) {
			String fp = ((DiscoveryPath)other).getFilepath();
			if( this.getFilepath() == null ) 
				return fp == null;
			return this.getFilepath().equals(fp);
		}
		return false;
	}
	
}
