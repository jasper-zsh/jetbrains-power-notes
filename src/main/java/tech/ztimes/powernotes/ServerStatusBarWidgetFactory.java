package tech.ztimes.powernotes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.ui.ServerStatusWidget;

public class ServerStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NonNls
    @NotNull String getId() {
        return "powernotes_server";
    }

    @Override
    public @Nls
    @NotNull String getDisplayName() {
        return "PowerNotes Server";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new ServerStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }
}
