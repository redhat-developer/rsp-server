package org.jboss.tools.rsp.server.spi.servertype.deploy;

import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;

public class Deployable {
	private String id;
	private String path;
	private int state;
	private int publishState;
	
	public Deployable() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getPublishState() {
		return publishState;
	}

	public void setPublishState(int publishState) {
		this.publishState = publishState;
	}

	/*
	 * Return a snapshot of this object with no logic in it
	 */
	public DeployableState toDeployableState() {
		DeployableState ds = new DeployableState();
		ds.setReference(new DeployableReference(id, path));
		ds.setPublishState(publishState);
		ds.setState(state);
		return ds;
	}
}
