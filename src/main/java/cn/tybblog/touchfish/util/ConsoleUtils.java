package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.PersistentState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ly
 */
public class ConsoleUtils {
    private static PersistentState persistentState=PersistentState.getInstance();
    private static StatusBar statusBar;
    private static Project project;
    /** 存放控制台JLabel */
    private static Map<Project, JLabel> map=new HashMap<>();


    public static void setStatusBar(StatusBar statusBar) {
        ConsoleUtils.statusBar = statusBar;
        ConsoleUtils.project = statusBar.getProject();
    }

    /**
     * 输出字符串
     * @param str
     */
    public static void info(String str){
        if (persistentState.getIsConsole()) {
            JLabel label = map.get(project);
            if (label == null) {
                if (statusBar != null){
                    statusBar.setInfo("未知的异常无法使用控制台");
                }
                return;
            }
            StringBuilder html =new StringBuilder();
            html.append("<html><head><style type=\"text/css\">body");
            html.append(persistentState.getFontStyle()==null?"{font-size: 10px;}":persistentState.getFontStyle());
            html.append("</style></head><body>");
            html.append(str);
            html.append("</body></html>");
            label.setText(html.toString());
        } else {
            if (statusBar==null) {
                return;
            }
            statusBar.setInfo(str);
        }
    }

    public static void putJLabel(Project project,JLabel jLabel){
        map.put(project,jLabel);
    }
}
