package tech.ztimes.powernotes.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "PowerNotes", storages = {
        @Storage("powernotes.xml")
})
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings.State> {
    private State state;

    @Override
    public @Nullable ApplicationSettings.State getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull ApplicationSettings.State state) {
        this.state = state;
    }

    @Data
    public static class State {
        private String server;
    }

    public static State getAppState() {
        return ApplicationManager.getApplication().getService(ApplicationSettings.class).getState();
    }
}
