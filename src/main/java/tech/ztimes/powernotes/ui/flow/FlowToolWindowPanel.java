package tech.ztimes.powernotes.ui.flow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import lombok.extern.slf4j.Slf4j;
import tech.ztimes.powernotes.action.CreateFlowAction;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.entity.FlowNoteRelation;
import tech.ztimes.powernotes.message.FlowListener;
import tech.ztimes.powernotes.message.FlowNoteRelationListener;
import tech.ztimes.powernotes.repository.FlowNoteRelationRepository;
import tech.ztimes.powernotes.repository.FlowRepository;
import tech.ztimes.powernotes.repository.NoteRepository;
import tech.ztimes.powernotes.service.FlowNoteRelationService;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.util.PopupUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

@Slf4j
public class FlowToolWindowPanel extends BorderLayoutPanel implements Disposable {
    private Project project;
    private JBList<Flow> flowList;
    private DefaultListModel<Flow> flowListModel;
    private JBList<FlowNoteRelation> noteList;
    private CollectionListModel<FlowNoteRelation> noteListModel;

    public FlowToolWindowPanel(Project project) {
        this.project = project;

        var splitPane = new JBSplitter(0.15f);
        this.addToCenter(splitPane);
        var flowListPanel = JBUI.Panels.simplePanel();
        splitPane.setFirstComponent(flowListPanel);

        var actionGroup = new DefaultActionGroup();
        var toolbar = new ActionToolbarImpl("PowerNotes.ToolWindow.Flow.flowList", actionGroup, true);
        toolbar.setTargetComponent(flowListPanel);
        flowListPanel.addToTop(toolbar);
        actionGroup.add(new CreateFlowAction(project));

        this.flowListModel = new DefaultListModel<>();
        this.flowList = new JBList<>(flowListModel);
        this.flowList.setCellRenderer(new FlowCellRenderer());
        this.flowList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.flowList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                var flow = flowList.getSelectedValue();
                FlowService.getInstance(project).setActivated(flow);
                FlowListener.publisher(project).activatedChanged();
            }
        });
        flowListPanel.add(this.flowList);
        this.loadFlowList();

        noteListModel = new CollectionListModel<>();
        noteList = new JBList<>(noteListModel);
        noteList.setCellRenderer(new NoteCellRenderer());
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            var rel = noteList.getSelectedValue();
            if (rel == null) return;
            var note = NoteRepository.getInstance().get(rel.getNoteId());
            if (note == null) return;
            note.navigate(true);
        });
        var noteListDecorator = ToolbarDecorator.createDecorator(noteList, noteListModel).setRemoveAction(anActionButton -> {
            var rel = noteList.getSelectedValue();
            if (rel == null) {
                return;
            }
            PopupUtils.createConfirmation("Remove note from flow?", () -> {
                FlowNoteRelationService.getInstance(project).remove(rel.getFlowId(), rel.getNoteId());
            }).showInFocusCenter();
        }).setMoveUpAction(anActionButton -> {
                var rel = noteList.getSelectedValue();
                if (rel == null) return;
                FlowNoteRelationService.getInstance(project).moveUp(rel);
        }).setMoveDownAction(anActionButton -> {
                var rel = noteList.getSelectedValue();
                if (rel == null) return;
                FlowNoteRelationService.getInstance(project).moveDown(rel);
        });
        splitPane.setSecondComponent(noteListDecorator.createPanel());

        project.getMessageBus().connect(this).subscribe(FlowListener.TOPIC, new FlowListener() {

            @Override
            public void save(Flow flow) {
                flowListModel.addElement(flow);
            }

            @Override
            public void remove(Flow flow) {
                flowListModel.removeElement(flow);
            }

            @Override
            public void clear() {
                flowListModel.clear();
            }

            @Override
            public void activatedChanged() {
                loadNoteList();
            }
        });

        project.getMessageBus().connect(this).subscribe(FlowNoteRelationListener.TOPIC, new FlowNoteRelationListener() {
            @Override
            public void save(FlowNoteRelation relation) {
                refresh(relation);
            }

            @Override
            public void remove(FlowNoteRelation relation) {
                refresh(relation);
            }

            private void refresh(FlowNoteRelation relation) {
                var flowService = FlowService.getInstance(project);
                if (flowService.getActivated() != null && flowService.getActivated().getId().equals(relation.getFlowId())) {
                    loadNoteList();
                }
            }
        });
    }

    public void loadNoteList() {
        var selected = noteList.getSelectedValue();
        noteListModel.removeAll();
        var flowService = FlowService.getInstance(project);
        var activatedFlow = flowService.getActivated();
        if (activatedFlow == null) {
            return;
        }
        var rels = FlowNoteRelationRepository.getInstance().list(activatedFlow.getId());
        noteListModel.add(rels);
//        if (selected != null) {
//            for (int i = 0; i < noteListModel.getSize(); i ++) {
//                var rel = noteListModel.getElementAt(i);
//                if (rel.getFlowId().equals(selected.getFlowId()) && rel.getNoteId().equals(selected.getNoteId())) {
//                    final int index = i;
//                    SwingUtilities.invokeLater(() -> {
//                        noteList.setSelectedIndex(index);
//                    });
//                    break;
//                }
//            }
//        }
    }

    public void loadFlowList() {
        flowListModel.clear();
        flowListModel.addAll(FlowRepository.getInstance().list());
    }

    @Override
    public void dispose() {

    }

    private static class NoteCellRenderer extends SimpleColoredComponent implements ListCellRenderer<FlowNoteRelation> {

        @Override
        public Component getListCellRendererComponent(JList<? extends FlowNoteRelation> list, FlowNoteRelation value, int index, boolean isSelected, boolean cellHasFocus) {
            clear();
            var note = NoteRepository.getInstance().get(value.getNoteId());
            if (note == null) {
                append("[Data Corrupted]");
            } else {
                append(String.format("%s:%d %s", note.getFileName(), note.getLineNumber(), note.getText()));
            }

            setBackground(UIUtil.getListBackground(isSelected, cellHasFocus));
            setForeground(UIUtil.getListForeground(isSelected, cellHasFocus));
            return this;
        }
    }

    private static class FlowCellRenderer extends SimpleColoredComponent implements ListCellRenderer<Flow> {
        public FlowCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Flow> list, Flow value, int index, boolean isSelected, boolean cellHasFocus) {
            clear();
            append(value.getName());

            setBackground(UIUtil.getListBackground(isSelected, cellHasFocus));
            setForeground(UIUtil.getListForeground(isSelected, cellHasFocus));
            return this;
        }
    }
}
