package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.util.ConsoleUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class FishConsoleWindow implements ToolWindowFactory {

    /**
     * 正文内容显示
     **/
    private JLabel bookReadingText;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(initPanel(), "FishConsole", false);
        toolWindow.getContentManager().addContent(content);
        ConsoleUtils.putJLabel(project,bookReadingText);
    }

    /**
     * 初始化
     **/
    private JPanel initPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        bookReadingText = new JLabel();
        panel.add(bookReadingText, BorderLayout.CENTER);
        return panel;
    }
}
