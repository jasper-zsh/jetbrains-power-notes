package tech.ztimes.powernotes.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.service.FlowService;
import tech.ztimes.powernotes.util.StringUtils;

public class CreateFlowAction extends AnAction {
    private Project project;

    public CreateFlowAction(Project project) {
        super(AllIcons.Actions.AddList);
        this.project = project;
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String flowName = Messages.showInputDialog("Input flow name", "Create flow", null, "", new InputValidator() {
            @Override
            public boolean checkInput(@NlsSafe String inputString) {
                return !StringUtils.isEmpty(inputString);
            }

            @Override
            public boolean canClose(@NlsSafe String inputString) {
                return true;
            }
        });

        if (StringUtils.isEmpty(flowName)) {
            return;
        }

        var flow = new Flow();
        flow.setProjectName(project.getName());
        flow.setName(flowName);
        FlowService.getInstance(project).save(flow);
    }
}
