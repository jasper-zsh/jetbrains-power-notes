package tech.ztimes.powernotes.entity;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Builder;
import lombok.Data;

import javax.swing.*;
import java.nio.file.Paths;

@Builder
@Data
public class NoteContext {
    private JBPopup popup;
    private JTextArea editorPane;
    private Project project;
    private VirtualFile file;
    private int lineNumber;
    private int endLineNumber;
}
