package tech.ztimes.powernotes.service;

import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.ProjectManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.message.ServerListener;
import tech.ztimes.powernotes.remote.RestClient;
import tech.ztimes.powernotes.remote.WsClient;
import tech.ztimes.powernotes.util.FileUtils;
import tech.ztimes.powernotes.util.NotificationUtils;

public class NoteService {
    private WsClient wsClient;
    private RestClient restClient;

    public NoteService() {
        var app = ApplicationManager.getApplication();
        wsClient = app.getService(WsClient.class);
        restClient = app.getService(RestClient.class);
        app.getMessageBus().connect().subscribe(ServerListener.TOPIC, new ServerListener() {
            @Override
            public void wsConnectStatusChanged(WsClient.ConnectStatus status) {
                if (status != WsClient.ConnectStatus.CONNECTED) return;
                var projects = ProjectManager.getInstance().getOpenProjects();
                for (var project : projects) {
                    var files = FileEditorManager.getInstance(project).getOpenFiles();
                    for (var file : files) {
                        openFile(project.getName(), FileUtils.getFilePathToProject(project, file));
                    }
                }
            }
        });
    }

    public static NoteService getInstance() {
        return ApplicationManager.getApplication().getService(NoteService.class);
    }

    public void openFile(String projectName, String filePath) {
        wsClient.send("open_file", new FileRequest(projectName, filePath));
    }

    public void closeFile(String projectName, String filePath) {
        wsClient.send("close_file", new FileRequest(projectName, filePath));
    }

    public Note save(Note note) {
        try {
            return restClient.saveNote(note);
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
        return null;
    }

    public void remove(Note note) {
        try {
            restClient.removeNote(note);
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    static class FileRequest {
        @JSONField(name = "project_name")
        private String projectName;
        @JSONField(name = "file_path")
        private String filePath;
    }
}
