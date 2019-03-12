package org.jboss.tools.rsp.api.dao;

public class JobHandle {
	public String name;
	public String id;
	public JobHandle(String name, String id) {
		this.name = name;
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
