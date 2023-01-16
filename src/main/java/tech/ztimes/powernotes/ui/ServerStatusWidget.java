package tech.ztimes.powernotes.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.ztimes.powernotes.message.ServerListener;
import tech.ztimes.powernotes.remote.WsClient;

import java.awt.event.MouseEvent;

public class ServerStatusWidget extends EditorBasedWidget implements StatusBarWidget.TextPresentation {
    private WsClient wsClient;

    public ServerStatusWidget(@NotNull Project project) {
        super(project);
        var app = ApplicationManager.getApplication();
        wsClient = app.getService(WsClient.class);
        app.getMessageBus().connect(this).subscribe(ServerListener.TOPIC, new ServerListener() {
            @Override
            public void wsConnectStatusChanged(WsClient.ConnectStatus status) {
                myStatusBar.updateWidget(ID());
            }
        });
    }

    @Override
    public @NonNls
    @NotNull String ID() {
        return "powernotes_server_status";
    }

    @Override
    public @NotNull
    @NlsContexts.Label String getText() {
        switch (wsClient.status()) {
            case CONNECTED:
                return "PowerNotes connected";
            case DISCONNECTED:
                return "PowerNotes disconnected";
            case CONNECTING:
                return "PowerNotes connecting";
            default:
                return "Unknown Status";
        }
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable
    @NlsContexts.Tooltip String getTooltipText() {
        return "PowerNotes server status";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return e -> {
            ApplicationManager.getApplication().getMessageBus().syncPublisher(ServerListener.TOPIC)
                    .serverChanged();
        };
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }
}
