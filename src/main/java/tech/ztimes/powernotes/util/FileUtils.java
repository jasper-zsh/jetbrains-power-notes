package tech.ztimes.powernotes.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

public class FileUtils {
    public static String getFilePathToProject(@NotNull Project project, VirtualFile file) {
        return project.getBaseDir().toNioPath().toUri().relativize(file.toNioPath().toUri()).getPath();
    }
}
