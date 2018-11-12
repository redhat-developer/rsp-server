---
title: Specification
layout: specification
sectionid: specification
toc: true
---
# Runtime Server Protocol Specification

The Runtime Server Protocol is based on version 3.x of the language server protocol.

## Base Protocol

The base protocol consists of a header and a content part (comparable to HTTP). The header and content part are
separated by a '\r\n'.

### Header Part

The header part consists of header fields. Each header field is comprised of a name and a value,
separated by ': ' (a colon and a space).
Each header field is terminated by '\r\n'.
Considering the last header field and the overall header itself are each terminated with '\r\n',
and that at least one header is mandatory, this means that two '\r\n' sequences always
immediately precede the content part of a message.

Currently the following header fields are supported:

| Header Field Name | Value Type  | Description |
|:------------------|:------------|:------------|
| Content-Length    | number      | The length of the content part in bytes. This header is required. |
| Content-Type      | string      | The mime type of the content part. Defaults to application/vscode-jsonrpc; charset=utf-8 |
{: .table .table-bordered .table-responsive}

The header part is encoded using the 'ascii' encoding. This includes the '\r\n' separating the header and content part.

### Content Part

Contains the actual content of the message. The content part of a message uses [JSON-RPC](http://www.jsonrpc.org/) to describe requests, responses and notifications. The content part is encoded using the charset provided in the Content-Type field. It defaults to `utf-8`, which is the only encoding supported right now.
Prior versions of the protocol used the string constant `utf8` which is not a correct encoding constant according to [specification](http://www.iana.org/assignments/character-sets/character-sets.xhtml)). For backwards compatibility it is highly recommended that a client and a server treats the string `utf8` as `utf-8`.

### Example:

```
Content-Length: ...\r\n
\r\n
{
	"jsonrpc": "2.0",
	"id": 1,
	"method": "server/getServerTypes",
	"params": {
		...
	}
}
```
### Base Protocol JSON structures

The following TypeScript definitions describe the base [JSON-RPC protocol](http://www.jsonrpc.org/specification):

#### Abstract Message

A general message as defined by JSON-RPC. The language server protocol always uses "2.0" as the jsonrpc version.

```typescript
interface Message {
	jsonrpc: string;
}
```
#### Request Message

A request message to describe a request between the client and the server. Every processed request must send a response back to the sender of the request.

```typescript
interface RequestMessage extends Message {

	/**
	 * The request id.
	 */
	id: number | string;

	/**
	 * The method to be invoked.
	 */
	method: string;

	/**
	 * The method's params.
	 */
	params?: Array<any> | object;
}
```

#### Response Message

Response Message sent as a result of a request. If a request doesn't provide a result value the receiver of a request still needs to return a response message to conform to the JSON RPC specification. The result property of the ResponseMessage should be set to `null` in this case to signal a successful request.

```typescript
interface ResponseMessage extends Message {
	/**
	 * The request id.
	 */
	id: number | string | null;

	/**
	 * The result of a request. This can be omitted in
	 * the case of an error.
	 */
	result?: any;

	/**
	 * The error object in case a request fails.
	 */
	error?: ResponseError<any>;
}

interface ResponseError<D> {
	/**
	 * A number indicating the error type that occurred.
	 */
	code: number;

	/**
	 * A string providing a short description of the error.
	 */
	message: string;

	/**
	 * A Primitive or Structured value that contains additional
	 * information about the error. Can be omitted.
	 */
	data?: D;
}

export namespace ErrorCodes {
	// Defined by JSON RPC
	export const ParseError: number = -32700;
	export const InvalidRequest: number = -32600;
	export const MethodNotFound: number = -32601;
	export const InvalidParams: number = -32602;
	export const InternalError: number = -32603;
	export const serverErrorStart: number = -32099;
	export const serverErrorEnd: number = -32000;
	export const ServerNotInitialized: number = -32002;
	export const UnknownErrorCode: number = -32001;

	// Defined by the protocol.
	export const RequestCancelled: number = -32800;
}
```
#### Notification Message

A notification message. A processed notification message must not send a response back. They work like events.

```typescript
interface NotificationMessage extends Message {
	/**
	 * The method to be invoked.
	 */
	method: string;

	/**
	 * The notification's params.
	 */
	params?: Array<any> | object;
}
```

#### $ Notifications and Requests

Notification and requests whose methods start with '$/' are messages which are protocol implementation dependent and might not be implementable in all clients or servers. For example if the server implementation uses a single threaded synchronous programming language then there is little a server can do to react to a '$/cancelRequest'. If a server or client receives notifications or requests starting with '$/' it is free to ignore them if they are unknown.

#### <a name="cancelRequest" class="anchor"></a> Cancellation Support (:arrow_right: :arrow_left:)

The base protocol offers support for request cancellation. To cancel a request, a notification message with the following properties is sent:

_Notification_:
* method: '$/cancelRequest'
* params: `CancelParams` defined as follows:

```typescript
interface CancelParams {
	/**
	 * The request id to cancel.
	 */
	id: number | string;
}
```

A request that got canceled still needs to return from the server and send a response back. It can not be left open / hanging. This is in line with the JSON RPC protocol that requires that every request sends a response back. In addition it allows for returning partial results on cancel. If the requests returns an error response on cancellation it is advised to set the error code to `ErrorCodes.RequestCancelled`.


## Runtime Server Protocol

The simple server protocol defines a set of JSON-RPC request, response and notification messages which are exchanged using the above base protocol. This section starts describing the basic JSON structures used in the protocol. The document uses TypeScript interfaces to describe these. Based on the basic JSON structures, the actual requests with their responses and the notifications are described.

In general, the simple server protocol supports JSON-RPC messages, however the base protocol defined here uses a convention such that the parameters passed to request/notification messages should be of `object` type (if passed at all). However, this does not disallow using `Array` parameter types in custom messages.

The protocol currently assumes that one server serves one tool. There is currently no support in the protocol to share one server between different tools. Such a sharing would require additional protocol to either lock a server to prevent concurrent changes to server state.



### The Server Interface

#### server/registerClientCapabilities

 Register client capabilities so the server knows what this client can support 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "map" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    }
  }
}</pre></td><td><pre>export interface ClientCapabilitiesRequest {
    map: { [index: string]: string };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "serverCapabilities" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "clientRegistrationStatus" : {
      "type" : "object",
      "properties" : {
        "severity" : {
          "type" : "integer"
        },
        "pluginId" : {
          "type" : "string"
        },
        "code" : {
          "type" : "integer"
        },
        "message" : {
          "type" : "string"
        },
        "trace" : {
          "type" : "string"
        },
        "ok" : {
          "type" : "boolean"
        },
        "plugin" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerCapabilitiesResponse {
    serverCapabilities: { [index: string]: string };
    clientRegistrationStatus: Status;
}

export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getDiscoveryPaths

 The `server/getDiscoveryPaths` request is sent by the client to fetch a list of discovery paths that can be searched. Discovery paths exist in the RSP model as paths suitable to be searched for server runtime installations. Additional paths may be added via the `server/addDiscoveryPath` entry point, or removed via the `server/removeDiscoveryPath` entry point. 

This endpoint takes no parameters. 

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

#### server/findServerBeans

 The `server/findServerBeans` request is sent by the client to fetch a list of server beans for the given path. The RSP model will iterate through a number of `IServerBeanTypeProvider` instances and ask them if they recognize the contents of the folder underlying the discovery path. Any providers that claim to be able to handle the given path will return an object representing the details of this recognized server runtime, its version, etc. The path parameter must be an absolute file-system path, and may not be a relative path. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "location" : {
      "type" : "string"
    },
    "typeCategory" : {
      "type" : "string"
    },
    "specificType" : {
      "type" : "string"
    },
    "name" : {
      "type" : "string"
    },
    "version" : {
      "type" : "string"
    },
    "fullVersion" : {
      "type" : "string"
    },
    "serverAdapterTypeId" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerBean {
    location: string;
    typeCategory: string;
    specificType: string;
    name: string;
    version: string;
    fullVersion: string;
    serverAdapterTypeId: string;
}</pre></td></tr></table>

#### server/addDiscoveryPath

 The `server/addDiscoveryPath` request is sent by the client to add a new path to search when discovering servers. These paths will be stored in a model, to be queried or searched later by a client. The path parameter must be an absolute file-system path, and may not be a relative path. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/removeDiscoveryPath

 The `server/removeDiscoveryPath` request is sent by the client to remove a path from the model and prevent it from being searched by clients when discovering servers in the future. The path parameter must be an absolute file-system path, and may not be a relative path. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getServerHandles

 The `server/getServerHandles` request is sent by the client to list the server adapters currently configured. A server adapter is configured when a call to `server/createServer` completes without error, or, some may be pre-configured by the server upon installation. 

This endpoint takes no parameters. 

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

#### server/getServerTypes

 The `server/getServerTypes` request is sent by the client to list the server types currently supported. The details of how many server types are supported by an RSP, or how they are registered, is implementation-specific. 

This endpoint takes no parameters. 

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "visibleName" : {
      "type" : "string"
    },
    "description" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

#### server/deleteServer

 The `server/deleteServer` request is sent by the client to delete a server from the model. This server will no longer be able to be started, shut down, or interacted with in any fashion. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getRequiredAttributes

 The `server/getRequiredAttributes` request is sent by the client to list the required attributes that must be stored on a server object of this type, such as a server-home or other required parameters. This request may return null in case of error. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "visibleName" : {
      "type" : "string"
    },
    "description" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "attributes" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "object",
        "properties" : {
          "type" : {
            "type" : "string"
          },
          "description" : {
            "type" : "string"
          },
          "defaultVal" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}</pre></td></tr></table>

#### server/getOptionalAttributes

 The `server/getOptionalAttributes` request is sent by the client to list the optional attributes that can be stored on a server object of this type. This may include things like customizing ports, or custom methods of interacting with various functionality specific to the server type.This request may return null in case of error. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "visibleName" : {
      "type" : "string"
    },
    "description" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "attributes" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "object",
        "properties" : {
          "type" : {
            "type" : "string"
          },
          "description" : {
            "type" : "string"
          },
          "defaultVal" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}</pre></td></tr></table>

#### server/createServer

 The `server/createServer` request is sent by the client to create a server in the model using the given attributes (both required and optional. This request may fail if required attributes are missing, any attributes have impossible, unexpected, or invalid values, or any error occurs while attempting to create the server adapter as requested. In the event of failure, the returend `Status` object will detail the cause of error. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "serverType" : {
      "type" : "string"
    },
    "id" : {
      "type" : "string"
    },
    "attributes" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "any"
      }
    }
  }
}</pre></td><td><pre>export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "status" : {
      "type" : "object",
      "properties" : {
        "severity" : {
          "type" : "integer"
        },
        "pluginId" : {
          "type" : "string"
        },
        "code" : {
          "type" : "integer"
        },
        "message" : {
          "type" : "string"
        },
        "trace" : {
          "type" : "string"
        },
        "ok" : {
          "type" : "boolean"
        },
        "plugin" : {
          "type" : "string"
        }
      }
    },
    "invalidKeys" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    }
  }
}</pre></td><td><pre>export interface CreateServerResponse {
    status: Status;
    invalidKeys: string[];
}

export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getLaunchModes

 The `server/getLaunchModes` request is sent by the client to get a list of launch modes that are applicable to this server type. Some servers can only be started. Others can be started, debugged, profiled, etc. Server types may come up with their own launch modes if desired. This method may return null if an error occurs on the server or the parameter is invalid. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "visibleName" : {
      "type" : "string"
    },
    "description" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "mode" : {
      "type" : "string"
    },
    "desc" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerLaunchMode {
    mode: string;
    desc: string;
}</pre></td></tr></table>

#### server/getRequiredLaunchAttributes

 The `server/getRequiredLaunchAttributes` request is sent by the client to get any additional attributes required for launch or that can customize launch behavior. Some server types may require references to a specific library, a clear decision about which of several configurations the server should be launched with, or any other required details required to successfully start up the server. This request may return null if the parameter is invalid. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "serverTypeId" : {
      "type" : "string"
    },
    "mode" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface LaunchAttributesRequest {
    serverTypeId: string;
    mode: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "attributes" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "object",
        "properties" : {
          "type" : {
            "type" : "string"
          },
          "description" : {
            "type" : "string"
          },
          "defaultVal" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}</pre></td></tr></table>

#### server/getOptionalLaunchAttributes

 The `server/getOptionalLaunchAttributes` request is sent by the client to get any optional attributes which can be used to modify the launch behavior. Some server types may allow overrides to any number of launch flags or settings, but not require these changes in order to function. This request may return null if the parameter is invalid. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "serverTypeId" : {
      "type" : "string"
    },
    "mode" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface LaunchAttributesRequest {
    serverTypeId: string;
    mode: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "attributes" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "object",
        "properties" : {
          "type" : {
            "type" : "string"
          },
          "description" : {
            "type" : "string"
          },
          "defaultVal" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface Attributes {
    attributes: { [index: string]: Attribute };
}

export interface Attribute {
    type: string;
    description: string;
    defaultVal: any;
}</pre></td></tr></table>

#### server/getLaunchCommand

 The `server/getLaunchCommand` request is sent by the client to the server to get the command which can be used to launch the server. This entry point is most often used if an editor or IDE wishes to start the server by itself, but does not know the servertype-specific command that must be launched. The parameter will include a mode the server should run in (run, debug, etc), as well as any custom attributes that may have an effect on the generation of the launch command. This request may return null if the parameter is invalid. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "mode" : {
      "type" : "string"
    },
    "params" : {
      "type" : "object",
      "properties" : {
        "serverType" : {
          "type" : "string"
        },
        "id" : {
          "type" : "string"
        },
        "attributes" : {
          "type" : "object",
          "additionalProperties" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "cmdLine" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    },
    "workingDir" : {
      "type" : "string"
    },
    "envp" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    }
  }
}</pre></td><td><pre>export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}</pre></td></tr></table>

#### server/serverStartingByClient

 The `server/serverStartingByClient` request is sent by the client to the server to inform the server that the client itself has launched the server instead of asking the RSP to do so. The parameters include both the request used to get the launch command, and a boolean as to whether the server should initiate the 'state-polling' mechanism to inform the client when the selected server has completed its startup. If the `polling` boolean is false, the client is expected to also alert the RSP when the launched server has completed its startup via the `server/serverStartedByClient` request. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "initiatePolling" : {
      "type" : "boolean"
    },
    "request" : {
      "type" : "object",
      "properties" : {
        "mode" : {
          "type" : "string"
        },
        "params" : {
          "type" : "object",
          "properties" : {
            "serverType" : {
              "type" : "string"
            },
            "id" : {
              "type" : "string"
            },
            "attributes" : {
              "type" : "object",
              "additionalProperties" : {
                "type" : "any"
              }
            }
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerStartingAttributes {
    initiatePolling: boolean;
    request: LaunchParameters;
}

export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/serverStartedByClient

 The `server/serverStartedByClient` request is sent by the client to the server to inform the server that the client itself has launched the server instead of asking the RSP to do so, AND that the startup has completed. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "mode" : {
      "type" : "string"
    },
    "params" : {
      "type" : "object",
      "properties" : {
        "serverType" : {
          "type" : "string"
        },
        "id" : {
          "type" : "string"
        },
        "attributes" : {
          "type" : "object",
          "additionalProperties" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getServerState

 Get the state of the current server 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "state" : {
      "type" : "integer"
    },
    "publishState" : {
      "type" : "integer"
    },
    "deployableStates" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "properties" : {
          "reference" : {
            "type" : "object",
            "properties" : {
              "label" : {
                "type" : "string"
              },
              "path" : {
                "type" : "string"
              }
            }
          },
          "state" : {
            "type" : "integer"
          },
          "publishState" : {
            "type" : "integer"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    deployableStates: DeployableState[];
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableState {
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface DeployableReference {
    label: string;
    path: string;
}</pre></td></tr></table>

#### server/startServerAsync

 The `server/startServerAsync` request is sent by the client to the server to start an existing server in the model. This request will cause the server to launch the server and keep organized the spawned processes, their I/O streams, and any events that must be propagated to the client. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "mode" : {
      "type" : "string"
    },
    "params" : {
      "type" : "object",
      "properties" : {
        "serverType" : {
          "type" : "string"
        },
        "id" : {
          "type" : "string"
        },
        "attributes" : {
          "type" : "object",
          "additionalProperties" : {
            "type" : "any"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface LaunchParameters {
    mode: string;
    params: ServerAttributes;
}

export interface ServerAttributes {
    serverType: string;
    id: string;
    attributes: { [index: string]: any };
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "status" : {
      "type" : "object",
      "properties" : {
        "severity" : {
          "type" : "integer"
        },
        "pluginId" : {
          "type" : "string"
        },
        "code" : {
          "type" : "integer"
        },
        "message" : {
          "type" : "string"
        },
        "trace" : {
          "type" : "string"
        },
        "ok" : {
          "type" : "boolean"
        },
        "plugin" : {
          "type" : "string"
        }
      }
    },
    "details" : {
      "type" : "object",
      "properties" : {
        "cmdLine" : {
          "type" : "array",
          "items" : {
            "type" : "string"
          }
        },
        "workingDir" : {
          "type" : "string"
        },
        "envp" : {
          "type" : "array",
          "items" : {
            "type" : "string"
          }
        },
        "properties" : {
          "type" : "object",
          "additionalProperties" : {
            "type" : "string"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface StartServerResponse {
    status: Status;
    details: CommandLineDetails;
}

export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}

export interface CommandLineDetails {
    cmdLine: string[];
    workingDir: string;
    envp: string[];
    properties: { [index: string]: string };
}</pre></td></tr></table>

#### server/stopServerAsync

 The `server/stopServerAsync` request is sent by the client to the server to stop an existing server in the model. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "force" : {
      "type" : "boolean"
    }
  }
}</pre></td><td><pre>export interface StopServerAttributes {
    id: string;
    force: boolean;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/getDeployables

 The `server/getDeployables` request is sent by the client to the server to get a list of all deployables 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns a list of the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "reference" : {
      "type" : "object",
      "properties" : {
        "label" : {
          "type" : "string"
        },
        "path" : {
          "type" : "string"
        }
      }
    },
    "state" : {
      "type" : "integer"
    },
    "publishState" : {
      "type" : "integer"
    }
  }
}</pre></td><td><pre>export interface DeployableState {
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface DeployableReference {
    label: string;
    path: string;
}</pre></td></tr></table>

#### server/addDeployable

 The `server/addDeployable` request is sent by the client to the server to add a deployable reference to a server's list of deployable items so that it can be published thereafter. @param handle @param reference @return 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "deployable" : {
      "type" : "object",
      "properties" : {
        "label" : {
          "type" : "string"
        },
        "path" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ModifyDeployableRequest {
    server: ServerHandle;
    deployable: DeployableReference;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableReference {
    label: string;
    path: string;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/removeDeployable

 The `server/removeDeployable` request is sent by the client to the server to remove a deployable reference from a server's list of deployable items so that it can be unpublished thereafter. @param handle @param reference @return 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "deployable" : {
      "type" : "object",
      "properties" : {
        "label" : {
          "type" : "string"
        },
        "path" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ModifyDeployableRequest {
    server: ServerHandle;
    deployable: DeployableReference;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableReference {
    label: string;
    path: string;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/publish

 The `server/publish` request is sent by the client to the server to instruct the server adapter to publish any changes to the backing runtime. @param request @return 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "kind" : {
      "type" : "integer"
    }
  }
}</pre></td><td><pre>export interface PublishServerRequest {
    server: ServerHandle;
    kind: number;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre>{
  "type" : "object",
  "properties" : {
    "severity" : {
      "type" : "integer"
    },
    "pluginId" : {
      "type" : "string"
    },
    "code" : {
      "type" : "integer"
    },
    "message" : {
      "type" : "string"
    },
    "trace" : {
      "type" : "string"
    },
    "ok" : {
      "type" : "boolean"
    },
    "plugin" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface Status {
    severity: number;
    pluginId: string;
    code: number;
    message: string;
    trace: string;
    ok: boolean;
    plugin: string;
}</pre></td></tr></table>

#### server/shutdown

 The `server/shutdown` notification is sent by the client to shut down the RSP itself. 

This endpoint takes no parameters. 

This endpoint returns no value

### The Client Interface

#### client/promptString

 Prompt the user for some feature 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "code" : {
      "type" : "integer"
    },
    "prompt" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface StringPrompt {
    code: number;
    prompt: string;
}</pre></td></tr></table>

This endpoint returns the following schema as a return value: 

<table><tr><th>json</th><th>typescript</th></tr>
<tr><td><pre></pre></td><td><pre></pre></td></tr></table>

#### client/discoveryPathAdded

 The `client/discoveryPathAdded` notification is sent by the server to all clients in response to the `server/addDiscoveryPath` notification. This call indicates that a discovery path has been added to the RSP model which keeps track of filesystem paths that may be searched for server runtimes. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/discoveryPathRemoved

 The `client/discoveryPathRemoved` notification is sent by the server to all clients in response to the `server/removeDiscoveryPath` notification. This call indicates that a discovery path has been removed from the RSP model which keeps track of filesystem paths that may be searched for server runtimes. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "filepath" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface DiscoveryPath {
    filepath: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverAdded

 The `client/serverAdded` notification is sent by the server to all clients in a response to the `server/createServer` notification. This notification indicates that a new server adapter has been created in the RSP model of existing servers. As mentioned above, this was most likely in response to a server/createServer notification, but is not strictly limited to this entrypoint. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverRemoved

 The `client/serverRemoved` notification is sent by the server to all clients in response to the `server/deleteServer` notification. This notification indicates that a server adapter has been removed from the RSP model of existing servers. As mentioned above, this was most likely in response to a server/deleteServer notification, but is not strictly limited to this entrypoint. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverAttributesChanged

 The `client/serverRemoved` notification is sent by the server to all clients when any server has had one of its attributes changed. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "id" : {
      "type" : "string"
    },
    "type" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "visibleName" : {
          "type" : "string"
        },
        "description" : {
          "type" : "string"
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverStateChanged

 The `client/serverStateChanged` notification is sent by the server to all clients when any server has had its state change. Possible values include: `0` representing an unknown state `1` representing starting `2` representing started `3` representing stopping `4` representing stopped 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "state" : {
      "type" : "integer"
    },
    "publishState" : {
      "type" : "integer"
    },
    "deployableStates" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "properties" : {
          "reference" : {
            "type" : "object",
            "properties" : {
              "label" : {
                "type" : "string"
              },
              "path" : {
                "type" : "string"
              }
            }
          },
          "state" : {
            "type" : "integer"
          },
          "publishState" : {
            "type" : "integer"
          }
        }
      }
    }
  }
}</pre></td><td><pre>export interface ServerState {
    server: ServerHandle;
    state: number;
    publishState: number;
    deployableStates: DeployableState[];
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface DeployableState {
    reference: DeployableReference;
    state: number;
    publishState: number;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}

export interface DeployableReference {
    label: string;
    path: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverProcessCreated

 The `client/serverProcessCreated` notification is sent by the server to all clients when any server has launched a new process which can be monitored. This notification is most often sent in response to a call to `server/startServerAsync` which will typically launch a process to run the server in question. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "processId" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverProcessTerminated

 The `client/serverProcessTerminated` notification is sent by the server to all clients when any process associated with a server has been terminated. This notification is most often sent as a result of a call to `server/stopServerAsync`, which should shut down a given server and cause all of that server's processes to terminate after some time. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "processId" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerProcess {
    server: ServerHandle;
    processId: string;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value#### client/serverProcessOutputAppended

 The `client/serverProcessOutputAppended` notification is sent by the server to all clients when any process associated with a server generated output on any of its output streams. This notification may be sent as a result of anything that causes a given server process to emit output, such as a change in configuration, a deployment, an error, normal logging, or any other number of possibilities. 

This endpoint takes the following json schemas as parameters: 

<table><tr><th>Param #</th><th>json</th><th>typescript</th></tr>
<tr><td>0</td><td><pre>{
  "type" : "object",
  "properties" : {
    "server" : {
      "type" : "object",
      "properties" : {
        "id" : {
          "type" : "string"
        },
        "type" : {
          "type" : "object",
          "properties" : {
            "id" : {
              "type" : "string"
            },
            "visibleName" : {
              "type" : "string"
            },
            "description" : {
              "type" : "string"
            }
          }
        }
      }
    },
    "processId" : {
      "type" : "string"
    },
    "streamType" : {
      "type" : "integer"
    },
    "text" : {
      "type" : "string"
    }
  }
}</pre></td><td><pre>export interface ServerProcessOutput {
    server: ServerHandle;
    processId: string;
    streamType: number;
    text: string;
}

export interface ServerHandle {
    id: string;
    type: ServerType;
}

export interface ServerType {
    id: string;
    visibleName: string;
    description: string;
}</pre></td></tr></table>

This endpoint returns no value