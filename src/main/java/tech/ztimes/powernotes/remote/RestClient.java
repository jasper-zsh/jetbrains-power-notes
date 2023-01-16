package tech.ztimes.powernotes.remote;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.ApplicationManager;
import lombok.Builder;
import lombok.Data;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import tech.ztimes.powernotes.config.ApplicationSettings;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.message.ServerListener;

import java.io.IOException;

public class RestClient {
    private HttpClient client;
    private String server;

    public enum Method {
        GET, POST, DELETE
    }

    public RestClient() {
        client = HttpClientBuilder.create().build();
        var app = ApplicationManager.getApplication();
        var state = app.getService(ApplicationSettings.class).getState();
        server = state != null ? state.getServer() : null;
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(ServerListener.TOPIC, new ServerListener() {
            @Override
            public void serverChanged() {
                RestClient.this.server = ApplicationSettings.getAppState().getServer();
            }
        });
    }

    public Note saveNote(Note note) {
        var res = request(Method.POST, "/note", note);
        return JSON.parseObject(res, Note.class);
    }

    public Note removeNote(Note note) {
        var res = request(Method.DELETE, String.format("/note/%d", note.getId()), null);
        return JSON.parseObject(res, Note.class);
    }

    public Flow saveFlow(Flow flow) {
        var res = request(Method.POST, "/flow", flow);
        return JSON.parseObject(res, Flow.class);
    }

    public FlowNoteRelation saveFlowNoteRelation(FlowNoteRelation relation) {
        var res = request(Method.POST, "/flow_note_relation", relation);
        return JSON.parseObject(res, FlowNoteRelation.class);
    }

    public void removeFlowNoteRelation(Long flowId, Long noteId) {
        request(Method.DELETE, String.format("/flow/%d/note_relation/%d", flowId, noteId), null);
    }

    public void swapFlowNoteRelation(SwapFlowNoteRelationRequest request) {
        request(Method.POST, "/flow_note_relation/swap", request);
    }

    @Data
    @Builder
    public static class SwapFlowNoteRelationRequest {
        @JSONField(name = "flow_id")
        private Long flowId;
        @JSONField(name = "note_id")
        private Long noteId;
        private Integer position;
        private Integer offset;
    }

    protected byte[] request(Method method, String path, Object payload) {
        HttpUriRequest req = null;
        var url = String.format("http://%s%s", server, path);
        switch (method) {
            case GET:
                req = new HttpGet(url);
                break;
            case POST:
                var postreq = new HttpPost(url);
                var body = JSON.toJSONBytes(payload);
                var entity = new ByteArrayEntity(body, ContentType.APPLICATION_JSON);
                postreq.setEntity(entity);
                req = postreq;
                break;
            case DELETE:
                req = new HttpDelete(url);
                break;
            default:
                throw new RuntimeException("Illegal http method");
        }
        try {
            var res = client.execute(req);
            if (res.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("HTTP Error " + res.getStatusLine().getStatusCode());
            }
            return EntityUtils.toByteArray(res.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
