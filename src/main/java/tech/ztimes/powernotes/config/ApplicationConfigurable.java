package tech.ztimes.powernotes.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;
import tech.ztimes.powernotes.message.ServerListener;
import tech.ztimes.powernotes.ui.ApplicationSettingsUI;

import javax.swing.*;

public class ApplicationConfigurable implements Configurable {
    private ApplicationSettingsUI ui;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "PowerNotes";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (ui == null) {
            ui = new ApplicationSettingsUI();
        }

        var state = ApplicationManager.getApplication().getService(ApplicationSettings.class).getState();
        ui.setState(state);
        return ui.getComponent();
    }

    @Override
    public boolean isModified() {
        if (ui == null) {
            return false;
        }
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (ui == null) return;
        var app = ApplicationManager.getApplication();
        app.getService(ApplicationSettings.class).loadState(ui.getState());
        app.getMessageBus().syncPublisher(ServerListener.TOPIC).serverChanged();
    }
}
