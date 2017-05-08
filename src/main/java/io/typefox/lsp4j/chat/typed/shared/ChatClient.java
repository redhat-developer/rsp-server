package io.typefox.lsp4j.chat.typed.shared;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public interface ChatClient {
	
	@JsonNotification
	void didPostMessage(UserMessage message);

}
