package org.jboss.tools.rsp.server.spi.jobs;

public class SimpleJob implements IJob {
	private String name;
	private String id;
	public SimpleJob(String name, String id) {
		this.name = name;
		this.id = id;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public double getProgress() {
		return -1;
	}
}
