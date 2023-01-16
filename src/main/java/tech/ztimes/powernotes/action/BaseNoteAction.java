package tech.ztimes.powernotes.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.application.ApplicationManager;
import tech.ztimes.powernotes.entity.NoteContext;
import tech.ztimes.powernotes.message.NoteListener;

import javax.swing.*;

public abstract class BaseNoteAction extends AnAction {
    protected NoteContext context;

    public BaseNoteAction(String text, Icon icon) {
        super(text, text, icon);
    }

    public void setContext(NoteContext context) {
        this.context = context;
    }

    protected Shortcut getShortcut() {
        return null;
    }

    public void registerShortcut() {
        var shortcut = getShortcut();
        if (shortcut == null) return;

        registerCustomShortcutSet(() -> new Shortcut[]{shortcut}, context.getEditorPane());
    }

    public NoteListener getPublisher() {
        if (context == null) throw new IllegalStateException("absent context");
        if (context.getProject() == null) throw new IllegalStateException("absent context.project");
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(NoteListener.TOPIC);
    }
}
