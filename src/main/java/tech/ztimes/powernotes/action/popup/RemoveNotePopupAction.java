package tech.ztimes.powernotes.action.popup;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.action.BaseNoteAction;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.NoteService;
import tech.ztimes.powernotes.util.EditorUtils;
import tech.ztimes.powernotes.util.FileUtils;
import tech.ztimes.powernotes.util.PopupUtils;

public class RemoveNotePopupAction extends BaseNoteAction {
    public RemoveNotePopupAction() {
        super("Remove this note?", AllIcons.Actions.Rollback);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PopupUtils.createConfirmation(getTemplateText(), () -> {
            var note = NoteRepository.getInstance().get(context.getProject().getName(), FileUtils.getFilePathToProject(context.getProject(), context.getFile()), context.getLineNumber(), context.getEndLineNumber());
            if (note != null) {
                NoteService.getInstance().remove(note);
            } else {
                var fileEditors = FileEditorManager.getInstance(context.getProject()).getAllEditors(context.getFile());
                for (var fileEditor : fileEditors) {
                    var editor = EditorUtils.getEditor(fileEditor);
                    if (editor != null) {
                        EditorUtils.clearTextAfterLine(editor, context.getLineNumber(), context.getEndLineNumber());
                    }
                }
            }

            if (context.getPopup() != null) {
                context.getPopup().dispose();
            }
        }).showInFocusCenter();
    }
}
