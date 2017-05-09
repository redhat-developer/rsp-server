/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.shared;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("server")
public interface ChatServer {
	
	/**
	 * The `server/fetchMessage` request is sent by the client to fetch messages posted so far.
	 */
	@JsonRequest
	CompletableFuture<List<UserMessage>> fetchMessages();
	
	/**
	 * The `server/postMessage` notification is sent by the client to post a new message.
	 * The server should store a message and broadcast it to all clients.
	 */
	@JsonNotification
	void postMessage(UserMessage message);

}
