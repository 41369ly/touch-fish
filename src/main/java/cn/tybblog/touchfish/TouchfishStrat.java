package cn.tybblog.touchfish;

import cn.tybblog.touchfish.listener.EventListener;
import cn.tybblog.touchfish.util.ConsoleUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class TouchfishStrat implements StartupActivity.DumbAware {

    private static EventListener listener;
    @Override
    public void runActivity(@NotNull Project project) {
        if (listener!=null){
            return;
        }
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        listener= new EventListener(project);
        manager.addKeyEventPostProcessor(listener);
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(listener,AWTEvent.MOUSE_EVENT_MASK);
        ConsoleUtils.statusBar = WindowManager.getInstance().getStatusBar(project);
    }
}
