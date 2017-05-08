# The simple chat app built with Eclipse LSP4J

This project demonstrates 2 ways to use Eclipse LSP4J:
- with generic [Endpoints](https://github.com/eclipse/lsp4j/blob/master/org.eclipse.lsp4j.jsonrpc/src/main/java/org/eclipse/lsp4j/jsonrpc/Endpoint.java) and json types;
- with statically typed services.

## The Simple Chat Protocol

Clients chat through the central server.
A communication happens over JSON RPC working over plain java sockets.

### Requests

#### <a name="fetchMessages"></a> `fetchMessages`
This request is sent by the client to fetch [messages](#message).
- return: [UserMessage](#message)`[]`
- parameters: `undefined`
- error: `undefined`

### Notifications

#### <a name="postMessage"></a> `postMessage`
This notification is sent by the client to post a new [message](#message).
- return: `undefined`
- parameters: 
  - message: [UserMessage](#message)
- error: `undefined`

#### <a name="didPostMessage"></a> `didPostMessage`
This notification is sent by the server to all clients as a reaction to [postMessage](#postMessage) notification.
- parameters: 
  - message: [UserMessage](#message)
- error: `undefined`

### Types
#### <a name="message"></a> `UserMessage`
```
{
    user: string
    content: string
}
```