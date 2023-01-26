package tech.ztimes.powernotes.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.service.FlowService;

public class ProjectListener implements ProjectManagerListener, StartupActivity {

    @Override
    public void projectClosed(@NotNull Project project) {
        FlowService.getInstance(project).closeProject(project.getName());
    }

    @Override
    public void runActivity(@NotNull Project project) {
        FlowService.getInstance(project).openProject(project);
    }
}
