package tech.ztimes.powernotes.service;

import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.message.FlowListener;
import tech.ztimes.powernotes.message.ServerListener;
import tech.ztimes.powernotes.remote.RestClient;
import tech.ztimes.powernotes.remote.WsClient;
import tech.ztimes.powernotes.repository.FlowRepository;
import tech.ztimes.powernotes.util.NotificationUtils;
import tech.ztimes.powernotes.util.ProjectUtils;

public class FlowService {
    private RestClient restClient;
    private WsClient wsClient;
    private Flow activated;

    public FlowService() {
        var app = ApplicationManager.getApplication();
        restClient = app.getService(RestClient.class);
        wsClient = app.getService(WsClient.class);
        app.getMessageBus().connect().subscribe(ServerListener.TOPIC, new ServerListener() {
            @Override
            public void wsConnectStatusChanged(WsClient.ConnectStatus status) {
                if (status != WsClient.ConnectStatus.CONNECTED) return;
                var projects = ProjectManager.getInstance().getOpenProjects();
                for (var project : projects) {
                    openProject(project);
                }
            }
        });
    }

    public static FlowService getInstance(Project project) {
        return project.getService(FlowService.class);
    }

    public void openProject(Project project) {
        FlowListener.publisher(project).clear();
        wsClient.send("open_project", new ProjectRequest(project.getName()));
    }

    public void closeProject(String projectName) {
        wsClient.send("close_project", new ProjectRequest(projectName));
    }

    public void save(Flow flow) {
        var project = ProjectUtils.getProject(flow.getProjectName());
        if (project == null) {
            return;
        }

        try {
            restClient.saveFlow(flow);
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
    }

    public Flow getActivated() {
        return activated;
    }

    public void setActivated(Flow activated) {
        this.activated = activated;
    }

    @Data
    @AllArgsConstructor
    public static class ProjectRequest {
        @JSONField(name = "project_name")
        private String projectName;
    }
}
