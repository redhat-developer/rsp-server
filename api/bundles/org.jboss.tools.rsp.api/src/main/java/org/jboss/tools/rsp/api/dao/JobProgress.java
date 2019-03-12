package org.jboss.tools.rsp.api.dao;

public class JobProgress {
	private double pctg;
	private JobHandle handle;

	public JobProgress( JobHandle handle, double pctg) {
		this.handle = handle;
		this.pctg = pctg;
	}

	public double getPctg() {
		return pctg;
	}

	public void setPctg(double pctg) {
		this.pctg = pctg;
	}

	public JobHandle getHandle() {
		return handle;
	}

	public void setHandle(JobHandle handle) {
		this.handle = handle;
	}
}
