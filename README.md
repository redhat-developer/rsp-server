# The simple chat app built with Eclipse LSP4J

This project demonstrates 2 ways to use Eclipse LSP4J with [Endpoints](https://github.com/eclipse/lsp4j/blob/master/org.eclipse.lsp4j.jsonrpc/src/main/java/org/eclipse/lsp4j/jsonrpc/Endpoint.java) and [service objects](https://github.com/eclipse/lsp4j/blob/master/documentation/jsonrpc.md#service-objects).

## The Simple Chat Protocol

Clients chat through the central server.
A communication happens over JSON RPC working over plain java sockets.

### Types
#### UserMessage
A message posted by a user.
```json
UserMessage: {
  "user": "string",
  "content": "string"
}
```

### Requests
#### `fetchMessages`
This request is sent by the client to fetch exising messages from the server.
- result: `UserMessage[]`
- params: `undefined`
- error: `undefined`

### Notifications
#### `postMessage`
This notification is sent by the client to post a new message.
- params: `UserMessage`

#### `didPostMessage`
This notification is sent by the server to all clients as a reaction to `postMessage` notification.
- params: `UserMessage`

## License
[MIT](https://github.com/TypeFox/lsp4j-chat-app/blob/master/License.txt)
