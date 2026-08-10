# RSP Server — Runtime Server Protocol

RSP is a protocol and framework for managing application server runtimes (WildFly, JBoss EAP, Tomcat, Jetty, etc.). It is modeled after the Language Server Protocol (LSP) — same communication library (Eclipse LSP4J / JSON-RPC), but a completely different set of protocol objects and operations focused on server lifecycle management rather than language features.

The RSP server runs as an OSGi application on Apache Felix. Clients (IDE extensions, CLI tools) connect over TCP and interact via JSON-RPC request/response and notification messages.

## Build System

Eclipse Tycho (Maven + OSGi). All modules use `eclipse-plugin` packaging. Dependencies are resolved via an Eclipse target platform definition (`targetplatform/`), not Maven `<dependency>` elements. The per-bundle `pom.xml` files are typically minimal.

## Module Structure

| Module | Purpose |
|--------|---------|
| `api/` | Protocol interfaces (`RSPServer`, `RSPClient`) and ~50 DAO classes serialized as JSON over the wire |
| `framework/` | Core server framework — 10 OSGi bundles providing the SPI, server core, generic server support, launching, logging, security, etc. |
| `runtimes/` | Concrete server type implementations bundled with this repo (WildFly/EAP, Minishift/CRC, Red Hat download provider) |
| `client/` | CLI client JAR |
| `distribution/` | Felix-based distribution packaging and integration tests |
| `targetplatform/` | Eclipse target platform definition |
| `site/` | P2 update site |

### Framework Bundles (`framework/bundles/`)

| Bundle | Role |
|--------|------|
| `org.jboss.tools.rsp.server.spi` | SPI layer — all key interfaces (`IServerType`, `IServerDelegate`, `IServer`, `IServerModel`, `RSPExtensionBundle`, discovery, polling, publishing, launchers) |
| `org.jboss.tools.rsp.server` | Core server implementation — `ServerManagementServerImpl` (implements `RSPServer`), `AbstractServerDelegate`, `ServerManagementModel`, `ServerManagementServerLauncher` (TCP accept loop) |
| `org.jboss.tools.rsp.server.generic` | JSON-descriptor-driven server type framework — `GenericServerActivator`, `GenericServerBehavior`, `GenericServerExtensionModel`. Downstream bundles can define entire server types via a `servers.json` with minimal Java. |
| `org.jboss.tools.rsp.foundation.core` | Core launchers and tasks |
| `org.jboss.tools.rsp.launching` | Launch utilities, `JSONMemento` (JSON-backed config tree) |
| `org.jboss.tools.rsp.launching.java` | Java-specific launch support (VM detection, classpath) |
| `org.jboss.tools.rsp.runtime.core` | Runtime discovery/installer model, `DownloadRuntime` |
| `org.jboss.tools.rsp.secure` | Security / secure storage |
| `org.jboss.tools.rsp.logging` | Logging |
| `org.jboss.tools.rsp.stacks.core` | Stacks support |

### Runtime Bundles (`runtimes/bundles/`)

| Bundle | Role |
|--------|------|
| `org.jboss.tools.rsp.server.wildfly` | WildFly / JBoss AS / EAP / EAP XP — 38+ server type definitions, full programmatic implementation |
| `org.jboss.tools.rsp.server.minishift` | Minishift / CDK / CRC — container-based runtimes |
| `org.jboss.tools.rsp.server.redhat.download` | Red Hat download runtime provider |

## Protocol Layer

Communication uses **JSON-RPC 2.0 over raw TCP sockets**, built on `org.eclipse.lsp4j.jsonrpc`.

- **`RSPServer`** (`api/.../RSPServer.java`) — Server-side interface annotated `@JsonSegment("server")`. Methods use `@JsonRequest` (request/response returning `CompletableFuture<T>`) and `@JsonNotification` (fire-and-forget). Covers: discovery, server CRUD, attributes, launching, publishing, runtime downloads, server actions, and jobs.

- **`RSPClient`** (`api/.../RSPClient.java`) — Client-side callback interface annotated `@JsonSegment("client")`. Mostly `@JsonNotification` methods the server calls to push events: `serverAdded`, `serverRemoved`, `serverStateChanged`, `serverProcess*`, `job*`, `discoveryPath*`, `messageBox`. One `@JsonRequest`: `promptString()`.

- **`SocketLauncher<T>`** — Wraps LSP4J's `Launcher` to bind JSON-RPC to a `Socket`.

- **`ServerManagementServerLauncher`** — Main entry point. Opens a `ServerSocket`, accepts connections in a loop, creates an `RSPServerSocketLauncher` per client. Each client gets its own proxy of `RSPClient` for callbacks. Subclassed by downstream distributions to provide a `main()`.

- **`ServerManagementServerImpl`** — Implements `RSPServer`, delegates all operations to `IServerManagementModel`.

- **DAO objects** (`api/.../dao/`) — ~50 POJOs (e.g., `ServerType`, `ServerHandle`, `ServerState`, `ServerBean`, `Attributes`, `LaunchParameters`, `DeployableReference`, `WorkflowResponse`) that are serialized/deserialized as JSON. JSON Schema definitions exist under `api/docs/`.

## Core Abstractions

### Server Types and Delegates

The type system is a factory pattern: `IServerType` defines what a server type *is*, and creates `IServerDelegate` instances that define how a server *behaves*.

- **`IServerType`** — Declares: id, name, description, required/optional attributes, launch modes. Factory method `createServerDelegate(IServer)` creates the behavioral delegate for a server instance.

- **`AbstractServerType`** — Base implementation with id/name/desc fields and default workflow handling.

- **`IServerDelegate`** — Behavioral contract for a server instance. Lifecycle: `start(mode)`, `stop(force)`. State: `getServerRunState()`, `getServerState()`. Publishing: `canAddDeployable()`, `publish()`. Actions: `listServerActions()`, `executeServerAction()`. Validation: `validate()`. Defaults: `setDefaults()`, `setDependentDefaults()`.

- **`AbstractServerDelegate`** — Large abstract base in the server core bundle. Manages state transitions, process monitoring, debug event listening, publish model lifecycle, stream listeners, and polling coordination.

- **`IServer`** / **`IServerWorkingCopy`** — The server data model. Holds attributes as typed key-value pairs. `IServer` is read-only; `IServerWorkingCopy` allows mutation and `save()`.

### Server State

Integer constants: `STATE_UNKNOWN(0)`, `STATE_STARTING(1)`, `STATE_STARTED(2)`, `STATE_STOPPING(3)`, `STATE_STOPPED(4)`. State transitions are managed by `AbstractServerDelegate`, typically driven by pollers.

**Polling**: `IServerStatePoller` implementations check whether a server is up or down. `PollThread` runs a poller in the background and notifies an `IPollResultListener` when a result is available. Built-in pollers include `WebPortPoller` (HTTP probe). The generic framework adds JSON-configured pollers: `webPoller`, `automaticSuccess`, `delayedSuccess`, `noOpPoller`.

### Discovery

Discovery detects installed server runtimes on the filesystem.

- **`ServerBeanType`** (abstract) — Given a filesystem path, determines if it's a server installation of a particular type. Key methods: `isServerRoot(File)`, `getFullVersion(File)`, `getServerAdapterTypeId(String version)`.

- **`IServerBeanTypeProvider`** — Returns an array of `ServerBeanType[]` contributed by a bundle.

- **`IServerBeanTypeManager`** — Registry. Bundles call `addTypeProvider()` / `removeTypeProvider()`.

- **`ServerBeanLoader`** — Iterates all registered `ServerBeanType`s against a path to find matches.

The generic framework provides three JSON-configured discovery strategies: `ExplodedManifestDiscovery` (reads exploded JAR manifest), `JarManifestDiscovery` (reads manifest from a JAR), `PropertiesFileDiscovery` (reads `.properties` files).

### Publishing

- **`IPublishController`** — Per-server-type publish strategy: `publishModule()`, `canAddDeployable()`, etc.

- **`AbstractFilesystemPublishController`** — Base for controllers that copy deployments to a filesystem location.

- **`IServerPublishModel`** — Per-server-instance model tracking deployables, their states, and resource deltas.

### Launching

- **`IServerStartLauncher`** / **`IServerShutdownLauncher`** — Contracts for starting/stopping a server process.

- **`AbstractJavaLauncher`** — Implements `IServerStartLauncher`. Provides the full Java launch lifecycle (VM runner, classpath, args, env). Subclasses implement: `getWorkingDirectory()`, `getMainTypeName()`, `getVMArguments()`, `getProgramArguments()`, `getClasspath()`.

- The generic framework reads launch configuration from JSON (`launchType: "java-launch"`, `"noOp"`, `"terminateProcess"`).

### Central Registry

**`IServerManagementModel`** — Top-level model facade (code comment notes it "should probably be called IRSPModel"). Provides access to all sub-models:

- `getServerModel()` — Server type/instance CRUD
- `getServerBeanTypeManager()` — Discovery registration
- `getDownloadRuntimeModel()` — Downloadable runtimes
- `getDiscoveryPathModel()` — Filesystem paths to scan
- `getVMInstallModel()` — JVM installations
- `getCapabilityManagement()` — Client capabilities
- `getSecureStorageProvider()` — Secure storage
- `getJobManager()` — Background jobs
- `getFileWatcherService()` — Filesystem monitoring

**`LauncherSingleton`** — Global accessor: `LauncherSingleton.getDefault().getLauncher().getModel()` gives any bundle access to the `IServerManagementModel`.

## Extension Mechanism

### How Bundles Register Extensions

All extension bundles subclass **`RSPExtensionBundle`** (which implements `BundleActivator`). The mechanism handles OSGi startup ordering:

1. Bundle's `start(BundleContext)` calls `addExtensions(targetBundleId, context)`.
2. If the target bundle (typically `org.jboss.tools.rsp.server`) is already `ACTIVE`, `addExtensions()` runs immediately.
3. If not yet active, the bundle registers with `DelayedExtensionManager`. The server core's `ServerCoreActivator.start()` processes all delayed extensions once it's ready.

Every extension registers up to three things into `IServerManagementModel`:
- **Server types** via `model.getServerModel().addServerType()`
- **Discovery providers** via `model.getServerBeanTypeManager().addTypeProvider()`
- **Download providers** via `model.getDownloadRuntimeModel().addDownloadRuntimeProvider()`

### Two Approaches to Defining Server Types

#### 1. Programmatic (Direct Java Implementation)

Used by the WildFly bundle and any server type needing deep behavioral customization. The bundle:

- Extends `RSPExtensionBundle` directly for its activator
- Defines `IServerType` subclasses with hardcoded attributes, launch modes, etc.
- Defines `IServerDelegate` subclasses (via `AbstractServerDelegate`) with custom start/stop, publish, action, and discovery logic
- Registers everything in an `ExtensionHandler`

**WildFly class hierarchy:**
```
IServerType → AbstractServerType → BaseJBossServerType → WildFlyServerType / JBossASServerType / EapXpServerType
IServerDelegate → AbstractServerDelegate → AbstractJBossServerDelegate → WildFlyServerDelegate / JBossASServerDelegate / EapXpServerDelegate
ServerBeanType → ServerBeanTypeWildfly80 / ServerBeanTypeEAP70 / ... (30+ subclasses)
```

The WildFly bundle defines 38+ server type constants in `WildFlyServerTypes` covering WildFly 7–38, JBoss AS 3.2–7.1, EAP 4.3–8.0, and EAP XP. It includes deep customization: version-specific launch argument generators, extended properties per server version, custom publish controllers, server actions (edit config, open browser), and download runtime providers.

#### 2. JSON-Descriptor-Driven (Generic Framework)

Used by downstream projects that define server types primarily through a `servers.json` file with minimal Java. The bundle:

- Extends **`GenericServerActivator`** (which extends `RSPExtensionBundle`) for its activator
- Provides `getBundleId()` and `getServerTypeModelStream()` (returns `servers.json` as an `InputStream`)
- Optionally provides `getDelegateProvider()` returning an `IServerBehaviorFromJSONProvider` factory
- Optionally extends **`GenericServerBehavior`** (which extends `AbstractServerDelegate`) for custom behavior

**`GenericServerActivator.addExtensions()`** creates a `GenericServerExtensionModel` which parses the JSON and registers all server types, discovery providers, and download providers.

**`servers.json` structure:**
```json
{
  "serverTypes": {
    "server.type.id": {
      "template": "template-name",
      "discoveries": [ ... ],
      "downloads": { ... },
      "type": {
        "name": "Display Name",
        "description": "...",
        "launchModes": "run,debug",
        "attributes": {
          "required": { ... },
          "optional": { ... },
          "staticDefaults": { ... }
        },
        "behavior": {
          "publish": { "deployPath": "...", "approvedSuffixes": "..." },
          "startup": {
            "launchType": "java-launch",
            "poller": "webPoller",
            "pollerProperties": { "url": "..." },
            "onProcessTerminated": "setServerStateStopped",
            ...launch args...
          },
          "shutdown": { "launchType": "terminateProcess" },
          "actions": { ... }
        }
      }
    }
  },
  "templates": {
    "template-name": { ...shared config inherited by serverTypes referencing it... }
  }
}
```

The JSON model supports a **template system** (`TemplateExtensionModelUtility.generateEffectiveMemento()`) where server type entries reference a named template and the effective configuration is a merge of template + server-type-specific overrides.

**`GenericServerBehavior`** reads all its behavior from the JSON memento: launcher type (`java-launch`, `noOp`, `terminateProcess`), poller type, publish config, process termination handling, and server actions. Subclasses can override specific methods:
- `setDependentDefaults()` — compute dependent attribute values
- `getDeploymentUrls()` — custom context root / URL generation
- `createPublishController()` — custom publish behavior
- `listServerActions()` / `executeServerAction()` — custom server actions
- `getExternalVariableResolver()` — resolve custom variables from server config files (e.g., read port from a config file)

**Variable substitution**: The generic framework supports `${varName}` substitution in JSON values. `ServerStringVariableManager` resolves variables from server attributes, and `IExternalVariableResolver` allows server-type-specific resolution (e.g., reading `jetty.port` from `start.d/http.ini`).

### Downstream Extension Pattern (Minimal)

To add a new server type via the generic framework, a downstream OSGi bundle needs:

1. **`servers.json`** — JSON descriptor defining server types, discovery, downloads, and behavior
2. **Activator** — Extends `GenericServerActivator`, implements `getBundleId()`, `getServerTypeModelStream()`, and optionally `getDelegateProvider()`
3. **`META-INF/MANIFEST.MF`** — OSGi manifest with `Bundle-Activator` and `Import-Package`
4. **`pom.xml`** — `eclipse-plugin` packaging

Optionally:
5. **Custom delegate** — Extends `GenericServerBehavior` for behavior that can't be expressed in JSON
6. **`ServerMain`** — Extends `ServerManagementServerLauncher` for standalone (non-OSGi) execution

The simplest possible server type (like a folder-deploy adapter) needs no custom delegate at all — just the JSON descriptor and a three-method activator. More complex types (like Jetty with config-file-driven ports) add a delegate subclass that overrides specific methods.

## Key Packages

- `org.jboss.tools.rsp.api` — Protocol interfaces and DAOs
- `org.jboss.tools.rsp.server.spi.servertype` — `IServerType`, `IServerDelegate`, `IServer`, `IServerWorkingCopy`
- `org.jboss.tools.rsp.server.spi.model` — `IServerManagementModel`, `IServerModel`, polling, listeners
- `org.jboss.tools.rsp.server.spi.discovery` — `ServerBeanType`, `IServerBeanTypeProvider`, `IServerBeanTypeManager`
- `org.jboss.tools.rsp.server.spi.launchers` — `AbstractJavaLauncher`, `IServerStartLauncher`, `IServerShutdownLauncher`
- `org.jboss.tools.rsp.server.spi.publishing` — `IPublishController`, `AbstractFilesystemPublishController`
- `org.jboss.tools.rsp.server.spi` — `RSPExtensionBundle`
- `org.jboss.tools.rsp.server.generic` — `GenericServerActivator`, `GenericServerExtensionModel`, `GenericServerBehavior`
- `org.jboss.tools.rsp.server.generic.servertype` — Generic server type and behavior classes
- `org.jboss.tools.rsp.server.wildfly.servertype.impl` — WildFly server types and delegates
- `org.jboss.tools.rsp.server.wildfly.beans.impl` — WildFly discovery (30+ `ServerBeanType` subclasses)
