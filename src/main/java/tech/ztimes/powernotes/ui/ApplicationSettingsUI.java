package tech.ztimes.powernotes.ui;

import com.intellij.openapi.application.ApplicationManager;
import tech.ztimes.powernotes.config.ApplicationSettings;

import javax.swing.*;

public class ApplicationSettingsUI {
    private JTextField txtServer;
    private JPanel mainPanel;
    private JLabel lblServer;

    public ApplicationSettingsUI() {

    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public ApplicationSettings.State getState() {
        var state = new ApplicationSettings.State();
        state.setServer(txtServer.getText());
        return state;
    }

    public void setState(ApplicationSettings.State state) {
        if (state == null) return;
        txtServer.setText(state.getServer());
    }
}
