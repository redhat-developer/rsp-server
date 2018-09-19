package org.jboss.tools.rsp.api.dao;

public class StringPrompt {
	private int code;
	private String prompt;
	
	public StringPrompt(int code, String prompt) {
		this.code = code;
		this.prompt = prompt;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
}
