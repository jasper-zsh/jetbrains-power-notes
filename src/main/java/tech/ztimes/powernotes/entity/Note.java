package tech.ztimes.powernotes.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.Navigatable;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ztimes.powernotes.util.FileUtils;
import tech.ztimes.powernotes.util.ProjectUtils;

import java.nio.file.Paths;

@Data
@NoArgsConstructor
public class Note implements Navigatable {
    private Long id;
    @JSONField(name = "project_name")
    private String projectName;
    @JSONField(name = "file_name")
    private String fileName;
    @JSONField(name = "file_path")
    private String filePath;
    private String contentHash;
    @JSONField(name = "line_number")
    private int lineNumber;
    @JSONField(name = "end_line_number")
    private int endLineNumber;
    private String text;
    @JSONField(serialize = false, deserialize = false)
    private OpenFileDescriptor target;

    public Note(Project project, VirtualFile file, int lineNumber, int endLineNumber, String text) {
        this.projectName = project.getName();
        this.fileName = file.getName();
        this.filePath = FileUtils.getFilePathToProject(project, file);
        this.lineNumber = lineNumber;
        this.endLineNumber = endLineNumber;
        this.text = text;
    }

    @Override
    public void navigate(boolean requestFocus) {
        var target = getTarget();
        if (target == null) return;
        target.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        var target = getTarget();
        if (target == null) return false;
        return target.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        var target = getTarget();
        if (target == null) return false;
        return target.canNavigateToSource();
    }

    private OpenFileDescriptor getTarget() {
        if (target == null) {
            var project = ProjectUtils.getProject(projectName);
            if (project == null) return null;
            var file = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(project.getBaseDir().getPath(), filePath));
            if (file == null) return null;

            target = new OpenFileDescriptor(project, file, lineNumber, -1, true);
        }
        return target;
    }
}
