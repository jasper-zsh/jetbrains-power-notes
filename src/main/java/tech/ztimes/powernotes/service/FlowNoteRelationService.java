package tech.ztimes.powernotes.service;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.remote.RestClient;
import tech.ztimes.powernotes.util.NotificationUtils;

public class FlowNoteRelationService {
    private RestClient restClient;

    public FlowNoteRelationService() {
        var app = ApplicationManager.getApplication();
        restClient = app.getService(RestClient.class);
    }

    public static FlowNoteRelationService getInstance(Project project) {
        return project.getService(FlowNoteRelationService.class);
    }

    public FlowNoteRelation save(FlowNoteRelation relation) {
        try {
            return restClient.saveFlowNoteRelation(relation);
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
        return null;
    }

    public void remove(Long flowId, Long noteId) {
        try {
            restClient.removeFlowNoteRelation(flowId, noteId);
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
    }

    public void moveUp(FlowNoteRelation relation) {
        try {
            restClient.swapFlowNoteRelation(RestClient.SwapFlowNoteRelationRequest.builder()
                            .flowId(relation.getFlowId())
                            .noteId(relation.getNoteId())
                            .position(relation.getPosition())
                            .offset(-1)
                    .build());
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
    }

    public void moveDown(FlowNoteRelation relation) {
        try {
            restClient.swapFlowNoteRelation(RestClient.SwapFlowNoteRelationRequest.builder()
                            .flowId(relation.getFlowId())
                            .noteId(relation.getNoteId())
                            .position(relation.getPosition())
                            .offset(1)
                    .build());
        } catch (Throwable e) {
            NotificationUtils.fire(NotificationType.ERROR, e.getMessage());
        }
    }
}
