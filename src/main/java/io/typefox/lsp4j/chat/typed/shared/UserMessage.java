package io.typefox.lsp4j.chat.typed.shared;

public class UserMessage {

	private String user;
	private String content;

	public UserMessage(String user, String message) {
		super();
		this.user = user;
		this.content = message;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
