/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.shared;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("client")
public interface ChatClient {
	
	/**
	 * The `client/didPostMessage` is sent by the server to all clients 
	 * in a response to the `server/postMessage` notification.
	 */
	@JsonNotification
	void didPostMessage(UserMessage message);

}
