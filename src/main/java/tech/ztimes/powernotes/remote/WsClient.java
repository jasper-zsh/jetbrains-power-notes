package tech.ztimes.powernotes.remote;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.intellij.openapi.application.ApplicationManager;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import tech.ztimes.powernotes.config.ApplicationSettings;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.message.FlowListener;
import tech.ztimes.powernotes.message.FlowNoteRelationListener;
import tech.ztimes.powernotes.message.NoteListener;
import tech.ztimes.powernotes.message.ServerListener;
import tech.ztimes.powernotes.util.MessageUtils;
import tech.ztimes.powernotes.util.ProjectUtils;

import java.net.URI;

@Slf4j
public class WsClient {
    private Client client;
    private ConnectStatus status = ConnectStatus.DISCONNECTED;

    public WsClient() {
        connect();
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(ServerListener.TOPIC, new ServerListener() {
            @Override
            public void serverChanged() {
                connect();
            }
        });
    }

    public void connect() {
        if (client != null) {
            client.close();
        }
        var state = ApplicationManager.getApplication().getService(ApplicationSettings.class).getState();
        String server = state != null ? state.getServer() : null;
        if (server != null) {
            setConnectStatus(ConnectStatus.CONNECTING);
            client = new Client(URI.create(String.format("ws://%s/ws", server)));
            try {
                client.connectBlocking();
            } catch (InterruptedException e) {
                log.error("Failed to connect to server", e);
            }
        } else {
            setConnectStatus(ConnectStatus.DISCONNECTED);
        }
    }

    private void setConnectStatus(ConnectStatus status) {
        this.status = status;
        ServerListener.publisher().wsConnectStatusChanged(status);
    }

    public ConnectStatus status() {
        return this.status;
    }

    private boolean ensureClientConnected() {
        if (client == null) {
            connect();
            if (client == null) return false;
        }
        return client.isOpen();
    }

    public void send(String eventName, Object payload) {
        if (!ensureClientConnected()) return;
        var event = Event.builder().event(eventName).payload(payload).build();
        var raw = JSON.toJSONString(event);
        client.send(raw);
    }

    private class Client extends WebSocketClient {
        public Client(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            setConnectStatus(ConnectStatus.CONNECTED);
        }

        @Override
        public void onMessage(String message) {
            var obj = JSON.parseObject(message);
            switch (obj.getString("event")) {
                case "note":
                    Event<Note> noteEvent = JSON.parseObject(message, new TypeReference<Event<Note>>(){});
                    NoteListener.publisher().save(noteEvent.getPayload());
                    break;
                case "remove_note":
                    Event<Note> removeNoteEvent = JSON.parseObject(message, new TypeReference<Event<Note>>(){});
                    NoteListener.publisher().remove(removeNoteEvent.getPayload());
                    break;
                case "flow":
                    Event<Flow> flowEvent = JSON.parseObject(message, new TypeReference<Event<Flow>>(){});
                    var project = ProjectUtils.getProject(flowEvent.getPayload().getProjectName());
                    if (project != null) {
                        FlowListener.publisher(project).save(flowEvent.getPayload());
                    }
                    break;
                case "remove_flow":
                    Event<Flow> disposeFlowEvent = JSON.parseObject(message, new TypeReference<Event<Flow>>(){});
                    project = ProjectUtils.getProject(disposeFlowEvent.getPayload().getProjectName());
                    if (project != null) {
                        FlowListener.publisher(project).remove(disposeFlowEvent.getPayload());
                    }
                    break;
                case "flow_note_relation":
                    Event<FlowNoteRelation> relEvent = JSON.parseObject(message, new TypeReference<Event<FlowNoteRelation>>(){});
                    FlowNoteRelationListener.publisher().save(relEvent.getPayload());
                    break;
                case "remove_flow_note_relation":
                    Event<FlowNoteRelation> removeRelEvent = JSON.parseObject(message, new TypeReference<Event<FlowNoteRelation>>(){});
                    FlowNoteRelationListener.publisher().remove(removeRelEvent.getPayload());
                    break;
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            setConnectStatus(ConnectStatus.DISCONNECTED);
        }

        @Override
        public void onError(Exception ex) {

        }
    }

    public enum ConnectStatus {
        DISCONNECTED, CONNECTING, CONNECTED
    }
}
