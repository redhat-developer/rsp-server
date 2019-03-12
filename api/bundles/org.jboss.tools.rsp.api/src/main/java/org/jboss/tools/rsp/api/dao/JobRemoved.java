package org.jboss.tools.rsp.api.dao;

public class JobRemoved {
	private Status status;
	private JobHandle handle;

	public JobRemoved( JobHandle handle, Status status) {
		this.handle = handle;
		this.status = status;
	}

	public JobHandle getHandle() {
		return handle;
	}

	public void setHandle(JobHandle handle) {
		this.handle = handle;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
