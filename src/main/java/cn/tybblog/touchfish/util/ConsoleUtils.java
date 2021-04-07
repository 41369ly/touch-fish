package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.PersistentState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author ly
 */
public class ConsoleUtils {
    private static PersistentState persistentState=PersistentState.getInstance();
    public static StatusBar statusBar;

    public void setStatusBar(Project project){
        statusBar = WindowManager.getInstance().getStatusBar(project);
    }

    /**
     * Êä³ö×Ö·û´®
     * @param str
     */
    public static void info(String str){
        if (persistentState.getIsConsole()) {

        } else {
            if (statusBar==null) {
                return;
            }
            statusBar.setInfo(str);
        }
    }
}
