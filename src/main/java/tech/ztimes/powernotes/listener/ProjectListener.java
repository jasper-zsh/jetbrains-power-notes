package tech.ztimes.powernotes.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.service.FlowService;

public class ProjectListener implements ProjectManagerListener {
    @Override
    public void projectOpened(@NotNull Project project) {
        FlowService.getInstance(project).openProject(project);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        FlowService.getInstance(project).closeProject(project.getName());
    }
}
