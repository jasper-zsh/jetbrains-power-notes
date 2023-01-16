package tech.ztimes.powernotes.listener;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.NoteService;
import tech.ztimes.powernotes.util.EditorUtils;
import tech.ztimes.powernotes.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class EditorManagerListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        var project = source.getProject();
        AfterLineInlayListener.getInstance(project).startListening();

        var notes = NoteRepository.getInstance().list(project.getName(), FileUtils.getFilePathToProject(project, file));
        var fileEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        var editors = new ArrayList<Editor>();
        for (var fileEditor : fileEditors) {
            var editor = EditorUtils.getEditor(fileEditor);
            if (editor != null) {
                editors.add(editor);
            }
        }
        for (var note : notes) {
            for (var editor : editors) {
                EditorUtils.addNote(editor, note);
            }
        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {

    }
}
