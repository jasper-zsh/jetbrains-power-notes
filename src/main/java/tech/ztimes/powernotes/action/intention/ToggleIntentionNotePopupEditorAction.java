package tech.ztimes.powernotes.action.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.action.ToggleNotePopupEditorAction;

public class ToggleIntentionNotePopupEditorAction extends BaseIntentionAction implements ToggleNotePopupEditorAction {
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return ToggleIntentionNotePopupEditorAction.class.getName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && editor != null;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Add/Edit note";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        actionPerformed(editor);
    }
}
