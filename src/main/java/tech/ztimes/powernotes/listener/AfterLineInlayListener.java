package tech.ztimes.powernotes.listener;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.renderer.NoteRenderer;

public class AfterLineInlayListener implements EditorMouseMotionListener, EditorMouseListener, Disposable {
    private Inlay lastHoverInlay;

    public static AfterLineInlayListener getInstance(Project project) {
        return project.getService(AfterLineInlayListener.class);
    }

    public void startListening() {
        var multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addEditorMouseListener(this, this);
        multicaster.addEditorMouseMotionListener(this, this);
    }

    @Override
    public void mouseClicked(@NotNull EditorMouseEvent event) {
        if (!event.isConsumed()) {
            var inlay = getInlay(event);
            if (inlay != null) {
                var renderer = inlay.getRenderer();
                if (renderer instanceof NoteRenderer) {
                    ((NoteRenderer) renderer).onMouseClicked(inlay, event);
                }
                event.consume();
            }
        }
    }

    @Override
    public void mouseMoved(@NotNull EditorMouseEvent e) {
        var inlay = getInlay(e);
        if (lastHoverInlay != null) {
            var renderer = lastHoverInlay.getRenderer();
            if (renderer instanceof NoteRenderer) {
                if (lastHoverInlay != inlay) {
                    ((NoteRenderer) renderer).onMouseExited(lastHoverInlay, e);
                }
                lastHoverInlay = null;
            }
        }

        if (inlay != null) {
            var renderer = inlay.getRenderer();
            if (renderer instanceof NoteRenderer) {
                ((NoteRenderer) renderer).onMouseMoved(inlay, e);
                lastHoverInlay = inlay;
            } else {
                lastHoverInlay = null;
            }
        }
    }

    private Inlay getInlay(EditorMouseEvent event) {
        var editor = event.getEditor();
        var inlayModel = editor.getInlayModel();
        var mouseEvent = event.getMouseEvent();
        var point = mouseEvent.getPoint();
        var area = event.getArea();

        Inlay<? extends NoteRenderer> inlay = null;
        if (area == EditorMouseEventArea.EDITING_AREA) {
            inlay = inlayModel.getElementAt(point, NoteRenderer.class);
        }
        if (inlay == null || ((inlay.getPlacement() == Inlay.Placement.BELOW_LINE || inlay.getPlacement() == Inlay.Placement.ABOVE_LINE) && inlay.getWidthInPixels() <= point.getX())) {
            return null;
        }
        return inlay;
    }

    @Override
    public void dispose() {

    }
}
