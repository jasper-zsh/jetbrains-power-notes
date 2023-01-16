package tech.ztimes.powernotes.action;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import tech.ztimes.powernotes.action.popup.AddNoteToFlowPopupAction;
import tech.ztimes.powernotes.action.popup.RemoveNotePopupAction;
import tech.ztimes.powernotes.action.popup.SaveNotePopupAction;
import tech.ztimes.powernotes.entity.CodeBlock;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.util.EditorUtils;
import tech.ztimes.powernotes.util.FileUtils;
import tech.ztimes.powernotes.util.PopupUtils;

import java.util.ArrayList;
import java.util.List;

public interface ToggleNotePopupEditorAction {
    default void actionPerformed(Editor editor) {
        if (editor == null) {
            return;
        }

        var project = editor.getProject();
        var file = EditorUtils.getVirtualFile(editor);
        var codeBlock = EditorUtils.getSelectedCodeBlock(editor);

        if (file != null && project != null) {
            var note = NoteRepository.getInstance().get(project.getName(), FileUtils.getFilePathToProject(project, file), codeBlock.getLineNumber(), codeBlock.getEndLineNumber());
            var activetedFlow = FlowService.getInstance(project).getActivated();
            List<BaseNoteAction> actions = new ArrayList<>();
            String title;
            String val = null;
            if (note == null) {
                title = "New note";
                if (activetedFlow != null) {
                    title += " in " + activetedFlow.getName();
                }
                actions.add(new SaveNotePopupAction(true));
            } else {
                if (codeBlock.getEndLineNumber() > codeBlock.getLineNumber()) {
                    title = String.format("Edit Note at %s %d:%d", file.getName(), codeBlock.getLineNumber() + 1, codeBlock.getEndLineNumber() + 1);
                } else {
                    title = String.format("Edit Note at %s:%d", file.getName(), codeBlock.getLineNumber() + 1);
                }
                val = note.getText();
                actions.add(new SaveNotePopupAction(false));
                actions.add(new RemoveNotePopupAction());
                actions.add(new AddNoteToFlowPopupAction());
                codeBlock = CodeBlock.builder()
                        .lineNumber(note.getLineNumber())
                        .endLineNumber(note.getEndLineNumber())
                        .build();
            }
            PopupUtils.createNoteEditor(editor, codeBlock, file, title, val, actions.toArray(new BaseNoteAction[0])).showInBestPositionFor(editor);
        }
    }
}
