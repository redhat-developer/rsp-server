/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.api.dao;

public class ServerProcessOutput {
	private ServerHandle server;
	private String processId;
	private int streamType;
	private String text;
	public ServerProcessOutput() {
		
	}
	public ServerProcessOutput(ServerHandle handle, String id, int type, String text) {
		this.server = handle;
		this.processId = id;
		this.streamType = type;
		this.text = text;
	}
	public ServerHandle getServer() {
		return server;
	}
	public void setServer(ServerHandle server) {
		this.server = server;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public int getStreamType() {
		return streamType;
	}
	public void setStreamType(int streamType) {
		this.streamType = streamType;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
