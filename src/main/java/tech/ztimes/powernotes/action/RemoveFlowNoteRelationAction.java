package tech.ztimes.powernotes.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.service.FlowNoteRelationService;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.util.PopupUtils;

import javax.swing.*;

public class RemoveFlowNoteRelationAction extends AnAction {
    private Project project;
    private JList<Note> noteList;

    public RemoveFlowNoteRelationAction(Project project, JList<Note> noteList) {
        super("Remove note from flow", "Remove note from flow", AllIcons.Actions.DeleteTag);
        this.project = project;
        this.noteList = noteList;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var flowService = FlowService.getInstance(project);
        var flow = flowService.getActivated();
        var note = noteList.getSelectedValue();
        if (flow == null || note == null) {
            return;
        }
        PopupUtils.createConfirmation(getTemplateText(), () -> {
            FlowNoteRelationService.getInstance(project).remove(flow.getId(), note.getId());
        }).showInFocusCenter();
    }
}
