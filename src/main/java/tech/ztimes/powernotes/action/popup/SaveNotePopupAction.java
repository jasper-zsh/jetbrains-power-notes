package tech.ztimes.powernotes.action.popup;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.action.BaseNoteAction;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.entity.NoteContext;
import tech.ztimes.powernotes.repository.FlowNoteRelationRepository;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.FlowNoteRelationService;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.service.NoteService;
import tech.ztimes.powernotes.util.FileUtils;
import tech.ztimes.powernotes.util.ProjectUtils;
import tech.ztimes.powernotes.util.StringUtils;

public class SaveNotePopupAction extends BaseNoteAction {
    private boolean attachToFlow;

    public SaveNotePopupAction(boolean attachToFlow) {
        super("Save this note? (Alt+Enter)", AllIcons.Actions.CheckOut);
        this.attachToFlow = attachToFlow;
    }

    @Override
    protected Shortcut getShortcut() {
        return KeyboardShortcut.fromString("alt ENTER");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var text = context.getEditorPane().getText();

        if (!StringUtils.isEmpty(text)) {
            var note = NoteRepository.getInstance().get(context.getProject().getName(), FileUtils.getFilePathToProject(context.getProject(), context.getFile()), context.getLineNumber(), context.getEndLineNumber());
            if (note != null) {
                note.setText(text);
            } else {
                note = new Note(context.getProject(), context.getFile(), context.getLineNumber(), context.getEndLineNumber(), text);
            }
            note = NoteService.getInstance().save(note);
            if (note != null && attachToFlow) {
                var relService = FlowNoteRelationService.getInstance(context.getProject());
                var activatedFlow = FlowService.getInstance(context.getProject()).getActivated();
                if (activatedFlow != null) {
                    var rel = new FlowNoteRelation();
                    rel.setFlowId(activatedFlow.getId());
                    rel.setNoteId(note.getId());
                    rel.setPosition(FlowNoteRelationRepository.getInstance().getMaxPosition(activatedFlow.getId()));
                    relService.save(rel);
                }
            }
        }

        if (context.getPopup() != null) {
            context.getPopup().dispose();
        }
    }
}
