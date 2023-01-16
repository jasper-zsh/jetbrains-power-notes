package tech.ztimes.powernotes.message;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;
import tech.ztimes.powernotes.remote.WsClient;

public interface ServerListener {
    Topic<ServerListener> TOPIC = Topic.create("PowerNotes-Server", ServerListener.class);

    default void serverChanged() {}
    default void wsConnectStatusChanged(WsClient.ConnectStatus status) {}

    static ServerListener publisher() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(TOPIC);
    }
}
