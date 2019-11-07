import { Protocol } from './protocol';
import { Messages } from './messages';
import { MessageConnection } from 'vscode-jsonrpc';
import { EventEmitter } from 'events';

/**
 * Server incoming
 */
export class Incoming {

    private connection: MessageConnection;
    private emitter: EventEmitter;

    /**
     * Constructs a new discovery handler
     * @param connection message connection to the RSP
     * @param emitter event emitter to handle notification events
     */
    constructor(connection: MessageConnection, emitter: EventEmitter) {
        this.connection = connection;
        this.emitter = emitter;
        this.listen();
    }
    /**
     * Subscribes to notifications sent by the server
     */
    private listen() {
        this.connection.onNotification(Messages.Client.MessageBoxNotification.type, param => {
            this.emitter.emit('messageBox', param);
        });

        this.connection.onNotification(Messages.Client.DiscoveryPathAddedNotification.type, param => {
            this.emitter.emit('discoveryPathAdded', param);
        });

        this.connection.onNotification(Messages.Client.DiscoveryPathRemovedNotification.type, param => {
            this.emitter.emit('discoveryPathRemoved', param);
        });

        this.connection.onNotification(Messages.Client.ServerAddedNotification.type, param => {
            this.emitter.emit('serverAdded', param);
        });

        this.connection.onNotification(Messages.Client.ServerRemovedNotification.type, param => {
            this.emitter.emit('serverRemoved', param);
        });

        this.connection.onNotification(Messages.Client.ServerAttributesChangedNotification.type, param => {
            this.emitter.emit('serverAttributesChanged', param);
        });

        this.connection.onNotification(Messages.Client.ServerStateChangedNotification.type, param => {
            this.emitter.emit('serverStateChanged', param);
        });

        this.connection.onNotification(Messages.Client.ServerProcessCreatedNotification.type, param => {
            this.emitter.emit('serverProcessCreated', param);
        });

        this.connection.onNotification(Messages.Client.ServerProcessTerminatedNotification.type, param => {
            this.emitter.emit('serverProcessTerminated', param);
        });

        this.connection.onNotification(Messages.Client.ServerProcessOutputAppendedNotification.type, param => {
            this.emitter.emit('serverProcessOutputAppended', param);
        });

        this.connection.onNotification(Messages.Client.JobAddedNotification.type, param => {
            this.emitter.emit('jobAdded', param);
        });

        this.connection.onNotification(Messages.Client.JobRemovedNotification.type, param => {
            this.emitter.emit('jobRemoved', param);
        });

        this.connection.onNotification(Messages.Client.JobChangedNotification.type, param => {
            this.emitter.emit('jobChanged', param);
        });

    }

    onPromptString(listener: (arg: Protocol.StringPrompt) => Promise<string>): void {
        this.connection.onRequest(Messages.Client.PromptStringRequest.type, listener);
    }

    onMessageBox(listener: (arg: Protocol.MessageBoxNotification) => void): void {
        this.emitter.on('messageBox', listener);
    }

    removeOnMessageBox(listener: (arg: Protocol.MessageBoxNotification) => void): void {
        this.emitter.removeListener('messageBox', listener);
    }

    onDiscoveryPathAdded(listener: (arg: Protocol.DiscoveryPath) => void): void {
        this.emitter.on('discoveryPathAdded', listener);
    }

    removeOnDiscoveryPathAdded(listener: (arg: Protocol.DiscoveryPath) => void): void {
        this.emitter.removeListener('discoveryPathAdded', listener);
    }

    onDiscoveryPathRemoved(listener: (arg: Protocol.DiscoveryPath) => void): void {
        this.emitter.on('discoveryPathRemoved', listener);
    }

    removeOnDiscoveryPathRemoved(listener: (arg: Protocol.DiscoveryPath) => void): void {
        this.emitter.removeListener('discoveryPathRemoved', listener);
    }

    onServerAdded(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.on('serverAdded', listener);
    }

    removeOnServerAdded(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.removeListener('serverAdded', listener);
    }

    onServerRemoved(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.on('serverRemoved', listener);
    }

    removeOnServerRemoved(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.removeListener('serverRemoved', listener);
    }

    onServerAttributesChanged(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.on('serverAttributesChanged', listener);
    }

    removeOnServerAttributesChanged(listener: (arg: Protocol.ServerHandle) => void): void {
        this.emitter.removeListener('serverAttributesChanged', listener);
    }

    onServerStateChanged(listener: (arg: Protocol.ServerState) => void): void {
        this.emitter.on('serverStateChanged', listener);
    }

    removeOnServerStateChanged(listener: (arg: Protocol.ServerState) => void): void {
        this.emitter.removeListener('serverStateChanged', listener);
    }

    onServerProcessCreated(listener: (arg: Protocol.ServerProcess) => void): void {
        this.emitter.on('serverProcessCreated', listener);
    }

    removeOnServerProcessCreated(listener: (arg: Protocol.ServerProcess) => void): void {
        this.emitter.removeListener('serverProcessCreated', listener);
    }

    onServerProcessTerminated(listener: (arg: Protocol.ServerProcess) => void): void {
        this.emitter.on('serverProcessTerminated', listener);
    }

    removeOnServerProcessTerminated(listener: (arg: Protocol.ServerProcess) => void): void {
        this.emitter.removeListener('serverProcessTerminated', listener);
    }

    onServerProcessOutputAppended(listener: (arg: Protocol.ServerProcessOutput) => void): void {
        this.emitter.on('serverProcessOutputAppended', listener);
    }

    removeOnServerProcessOutputAppended(listener: (arg: Protocol.ServerProcessOutput) => void): void {
        this.emitter.removeListener('serverProcessOutputAppended', listener);
    }

    onJobAdded(listener: (arg: Protocol.JobHandle) => void): void {
        this.emitter.on('jobAdded', listener);
    }

    removeOnJobAdded(listener: (arg: Protocol.JobHandle) => void): void {
        this.emitter.removeListener('jobAdded', listener);
    }

    onJobRemoved(listener: (arg: Protocol.JobRemoved) => void): void {
        this.emitter.on('jobRemoved', listener);
    }

    removeOnJobRemoved(listener: (arg: Protocol.JobRemoved) => void): void {
        this.emitter.removeListener('jobRemoved', listener);
    }

    onJobChanged(listener: (arg: Protocol.JobProgress) => void): void {
        this.emitter.on('jobChanged', listener);
    }

    removeOnJobChanged(listener: (arg: Protocol.JobProgress) => void): void {
        this.emitter.removeListener('jobChanged', listener);
    }
}
