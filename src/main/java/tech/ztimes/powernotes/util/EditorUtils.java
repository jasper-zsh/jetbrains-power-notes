package tech.ztimes.powernotes.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayProperties;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import tech.ztimes.powernotes.entity.CodeBlock;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.renderer.NoteRenderer;

import java.awt.*;

public class EditorUtils {
    public static int getLineNumber(Editor editor) {
        return editor.getDocument().getLineNumber(editor.getCaretModel().getOffset());
    }

    public static CodeBlock getSelectedCodeBlock(Editor editor) {
        var doc = editor.getDocument();
        var start = doc.getLineNumber(editor.getSelectionModel().getSelectionStart());
        var end = doc.getLineNumber(editor.getSelectionModel().getSelectionEnd());
        return CodeBlock.builder()
                .lineNumber(start)
                .endLineNumber(end)
                .build();
    }

    public static VirtualFile getVirtualFile(Editor editor) {
        if (editor instanceof EditorEx) {
            return ((EditorEx) editor).getVirtualFile();
        }
        return null;
    }
    public static void addNote(Editor editor, Note note) {
        try {
            Runnable job = () -> {
                clearTextAfterLine(editor, note.getLineNumber(), note.getEndLineNumber());

                int startOffset = editor.getDocument().getLineStartOffset(note.getLineNumber());
                int blockEndOffset = editor.getDocument().getLineStartOffset(note.getEndLineNumber());

                var aboveProps = new InlayProperties();
                aboveProps.showAbove(true);
                editor.getInlayModel().addBlockElement(startOffset, aboveProps, new NoteRenderer(note, NoteRenderer.Mode.BLOCK_BEGIN));
                if (note.getEndLineNumber() > note.getLineNumber()) {
                    // 多行块
                    var belowProps = new InlayProperties();
                    editor.getInlayModel().addBlockElement(blockEndOffset, belowProps, new NoteRenderer(note, NoteRenderer.Mode.BLOCK_END));
                }
            };
            if (EventQueue.isDispatchThread()) {
                job.run();
            } else {
                EventQueue.invokeAndWait(job);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void clearTextAfterLine(Editor editor, int lineNumber, int endLineNumber) {
        try {
            Runnable job = () -> {
                var startOffset = editor.getDocument().getLineStartOffset(lineNumber);
                var endOffset = editor.getDocument().getLineStartOffset(endLineNumber);
                editor.getInlayModel().getBlockElementsInRange(startOffset, startOffset, NoteRenderer.class).forEach(Disposer::dispose);
                if (endLineNumber > lineNumber) {
                    editor.getInlayModel().getBlockElementsInRange(endOffset, endOffset, NoteRenderer.class).forEach(Disposer::dispose);
                }
            };
            if (EventQueue.isDispatchThread()) {
                job.run();
            } else {
                EventQueue.invokeAndWait(job);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Editor getEditor(FileEditor fileEditor) {
        if (fileEditor instanceof TextEditor) {
            return ((TextEditor) fileEditor).getEditor();
        }
        return null;
    }
}
