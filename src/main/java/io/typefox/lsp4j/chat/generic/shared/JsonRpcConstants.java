/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io). All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
package io.typefox.lsp4j.chat.generic.shared;

public interface JsonRpcConstants {
	String POST_MESSAGE = "postMessage";
	String FETCH_MESSAGES = "fetchMessages";
	String DID_POST_MESSAGE = "didPostMessage";
	
	String MESSAGE_USER = "user";
	String MESSAGE_CONTENT = "content";
}
