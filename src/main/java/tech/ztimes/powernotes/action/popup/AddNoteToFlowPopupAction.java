package tech.ztimes.powernotes.action.popup;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.action.BaseNoteAction;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.repository.FlowNoteRelationRepository;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.FlowNoteRelationService;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.util.FileUtils;

import javax.swing.*;

public class AddNoteToFlowPopupAction extends BaseNoteAction {
    public AddNoteToFlowPopupAction() {
        super("Add note to flow", AllIcons.Actions.AddToDictionary);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var note = NoteRepository.getInstance().get(context.getProject().getName(), FileUtils.getFilePathToProject(context.getProject(), context.getFile()), context.getLineNumber(), context.getEndLineNumber());
        if (note == null) return;
        var flow = FlowService.getInstance(context.getProject()).getActivated();
        if (flow == null) return;
        var rel = new FlowNoteRelation();
        rel.setFlowId(flow.getId());
        rel.setNoteId(note.getId());
        rel.setPosition(FlowNoteRelationRepository.getInstance().getMaxPosition(flow.getId()));
        FlowNoteRelationService.getInstance(context.getProject()).save(rel);

        if (context.getPopup() != null) {
            context.getPopup().dispose();
        }
    }
}
