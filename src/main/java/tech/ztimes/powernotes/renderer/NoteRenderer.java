package tech.ztimes.powernotes.renderer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.paint.EffectPainter;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;
import tech.ztimes.powernotes.action.popup.AddNoteToFlowPopupAction;
import tech.ztimes.powernotes.action.popup.RemoveNotePopupAction;
import tech.ztimes.powernotes.action.popup.SaveNotePopupAction;
import tech.ztimes.powernotes.entity.CodeBlock;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.util.EditorUtils;
import tech.ztimes.powernotes.util.PopupUtils;
import tech.ztimes.powernotes.util.StringUtils;

import javax.swing.*;
import java.awt.*;

public class NoteRenderer implements EditorCustomElementRenderer {
    private final static int RENDERER_TEXT_MAX_LENGTH = 50;

    private int textStartX;
    private boolean hover;
    private boolean showEditor;
    private Mode mode;
    private Note note;
    public NoteRenderer(Note note, Mode mode) {
        this.note = note;
        this.mode = mode;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        var editor = inlay.getEditor();
        var font = getFont(editor);
        var metrics = getFontMetrics(font, editor);
        return metrics.stringWidth(getRenderText());
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        var editor = inlay.getEditor();
        var lines = getRenderText().split("\n");
        return editor.getLineHeight() * lines.length;
    }

    public void onMouseMoved(Inlay inlay, EditorMouseEvent event) {
        setHover(event.getMouseEvent().getX() >= textStartX, inlay, event.getEditor());
    }

    public void onMouseExited(Inlay inlay, EditorMouseEvent event) {
        setHover(false, inlay, event.getEditor());
    }

    public void onMouseClicked(Inlay inlay, EditorMouseEvent event) {
        if (showEditor) {
            return;
        }

        var file = EditorUtils.getVirtualFile(event.getEditor());
        if (file != null) {
            showEditor = true;
            String title;
            if (note.getEndLineNumber() > note.getLineNumber()) {
                title = String.format("Edit Note for %s %d:%d", file.getName(), note.getLineNumber() + 1, note.getEndLineNumber() + 1);
            } else {
                title = String.format("Edit Note at %s:%d", file.getName(), note.getLineNumber() + 1);
            }
            var codeBlock = CodeBlock.builder().lineNumber(note.getLineNumber()).endLineNumber(note.getEndLineNumber()).build();
            var popup = PopupUtils.createNoteEditor(event.getEditor(), codeBlock, file, title, note.getText(), new SaveNotePopupAction(false), new RemoveNotePopupAction(), new AddNoteToFlowPopupAction());
            popup.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull LightweightWindowEvent event) {
                    showEditor = false;
                }
            });
            popup.showInBestPositionFor(event.getEditor());
        }
    }

    private void setHover(boolean hover, Inlay inlay, Editor editor) {
        if (editor instanceof EditorEx) {
            boolean old = this.hover;
            var cursor = hover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
            ((EditorEx) editor).setCustomCursor(NoteRenderer.class, cursor);
            this.hover = hover;

            if (old != hover) {
                inlay.update();
            }
        }
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
        if (StringUtils.isEmpty(getText())) {
            return;
        }

        var editor = (EditorImpl)inlay.getEditor();
        var inlineAttributes = getAttributes(editor);
        if (inlineAttributes == null || inlineAttributes.getForegroundColor() == null) {
            return;
        }

        var font = getFont(editor);
        g.setFont(font);
        var metrics = getFontMetrics(font, editor);

        var curX = targetRegion.x;
        textStartX = curX;

        // draw text
        var text = getRenderText();
        switch (mode) {
            case BLOCK_BEGIN:
                text = "↓↓↓ " + text;
                break;
            case BLOCK_END:
                text = "↑↑↑ " + text;
                break;
        }
        g.setColor(inlineAttributes.getForegroundColor());
        var lineHeight = editor.getLineHeight();
        var lines = text.split("\n");
        for (int i = 0; i < lines.length; i ++) {
            g.drawString(lines[i], textStartX, targetRegion.y + inlay.getEditor().getAscent() + lineHeight * i);
            var lineWidth = metrics.stringWidth(text);
            if (lineWidth > curX) {
                curX = lineWidth;
            }
        }

        if (hover) {
            var icon = AllIcons.General.LinkDropTriangle;
            icon.paintIcon(inlay.getEditor().getComponent(), g, curX, getIconY(icon, targetRegion));
        }

        paintEffects(g, targetRegion, editor, inlineAttributes, font, metrics);
    }

    private static void paintEffects(@NotNull final Graphics g,
                                     @NotNull final Rectangle r,
                                     final EditorImpl editor,
                                     final TextAttributes inlineAttributes,
                                     final Font font,
                                     final FontMetrics metrics) {
        final Color effectColor = inlineAttributes.getEffectColor();
        final EffectType effectType = inlineAttributes.getEffectType();
        if (effectColor != null) {
            g.setColor(effectColor);
            final Graphics2D g2d = (Graphics2D) g;
            final int xStart = r.x;
            final int xEnd = r.x + r.width;
            final int y = r.y + metrics.getAscent();
            if (effectType == EffectType.LINE_UNDERSCORE) {
                EffectPainter.LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.BOLD_LINE_UNDERSCORE) {
                EffectPainter.BOLD_LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.STRIKEOUT) {
                EffectPainter.STRIKE_THROUGH.paint(g2d, xStart, y, xEnd - xStart, editor.getCharHeight(), font);
            } else if (effectType == EffectType.WAVE_UNDERSCORE) {
                EffectPainter.WAVE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.BOLD_DOTTED_LINE) {
                EffectPainter.BOLD_DOTTED_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            }
        }
    }

    public String getText() {
        return note.getText();
    }

    private String getRenderText() {
        switch (mode) {
            case BLOCK_END:
            case LINE_TAILER:
                return StringUtils.maxLength(getText(), RENDERER_TEXT_MAX_LENGTH);
            default:
                return getText();
        }
    }

    private static int getIconY(Icon icon, Rectangle r) {
        return r.y + r.height / 2 - icon.getIconHeight() / 2;
    }

    private static Font getFont(Editor editor) {
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        return UIUtil.getFontWithFallback(colorsScheme.getEditorFontName(), Font.PLAIN, colorsScheme.getEditorFontSize());
    }

    private static FontMetrics getFontMetrics(Font font, Editor editor) {
        return FontInfo.getFontMetrics(font, FontInfo.getFontRenderContext(editor.getContentComponent()));
    }

    private TextAttributes getAttributes(Editor editor) {
        if (hover) {
            return editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);
        }
        return editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES);
    }

    public enum Mode {
        BLOCK_BEGIN, BLOCK_END, LINE_TAILER
    }
}
