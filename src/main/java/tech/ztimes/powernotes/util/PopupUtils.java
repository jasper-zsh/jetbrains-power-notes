package tech.ztimes.powernotes.util;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import tech.ztimes.powernotes.action.BaseNoteAction;
import tech.ztimes.powernotes.entity.CodeBlock;
import tech.ztimes.powernotes.entity.NoteContext;

import javax.swing.*;
import java.awt.*;

public class PopupUtils {
    public static JBPopup createComponent(String title, JComponent component, JComponent focusComponent) {
        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(component, focusComponent)
                .setCancelKeyEnabled(true)
                .setTitle(title)
                .setCancelButton(new IconButton("Cancel?", AllIcons.Actions.Cancel))
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setMovable(true)
                .createPopup();
    }

    public static JBPopup createNoteEditor(Editor editor, CodeBlock codeBlock, VirtualFile file, String title, String defaultVal, BaseNoteAction... actions) {
        var layoutPanel = JBUI.Panels.simplePanel();
        int minW = 300, maxW = 600, minH = 80, maxH = 120;
        var colorsScheme = editor.getColorsScheme();
        var font = UIUtil.getFontWithFallback(colorsScheme.getEditorFontName(), Font.PLAIN, colorsScheme.getEditorFontSize());

        var titleRect = SwingUtils.getTextRectangle(title, font);
        if (titleRect != null && titleRect.getWidth() > minW) {
            minW = (int) (titleRect.getWidth() + 5);
        }

        var textArea = new JBTextArea();
        textArea.setBorder(JBUI.Borders.empty(5));
//        textArea.setPreferredSize(SwingUtils.createDimension(title, font, minW, maxW, minH, maxH));
        textArea.setFont(font);
        textArea.setText(defaultVal);
        textArea.setLineWrap(true);

        var scrollPane = new JBScrollPane(textArea);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setPreferredSize(SwingUtils.createDimension(title, font, minW, maxW, minH, maxH));
        var inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("UP"), "none");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "none");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "none");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "none");
        layoutPanel.addToCenter(scrollPane);

        var actionGroup = new DefaultActionGroup();
        var toolbar = new ActionToolbarImpl("PowerNotesPopup.toolbar", actionGroup, true);
        toolbar.setTargetComponent(layoutPanel);
        toolbar.setBorder(JBUI.Borders.empty());
        toolbar.setBackground(UIUtil.getToolTipActionBackground());
        layoutPanel.addToBottom(toolbar);

        var popup = PopupUtils.createComponent(title, layoutPanel, textArea);
        var contextBuilder = NoteContext.builder()
                .project(editor.getProject())
                .file(file)
                .popup(popup)
                .editorPane(textArea);
        if (codeBlock == null) {
            contextBuilder.lineNumber(EditorUtils.getLineNumber(editor));
        } else {
            contextBuilder.lineNumber(codeBlock.getLineNumber())
                    .endLineNumber(codeBlock.getEndLineNumber());
        }
        var context = contextBuilder.build();
        for (var action : actions) {
            action.setContext(context);
            action.registerShortcut();
            actionGroup.add(action);
        }
        return popup;
    }

    public static JBPopup createConfirmation(String title, Runnable onConfirm) {
        return JBPopupFactory.getInstance().createConfirmation(title, onConfirm, 1);
    }
}
