package tech.ztimes.powernotes.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class ProjectUtils {
    public static Project getProject(String projectName) {
        var projects = ProjectManager.getInstance().getOpenProjects();
        for (var project : projects) {
            if (project.getName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }
}
