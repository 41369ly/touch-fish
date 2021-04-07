package cn.tybblog.touchfish.util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * 按钮格式化工具类
 *
 * @author ly
 */
public class KeyMapFormatUtils {
    public static String keyMapFormat(AWTEvent e){
        if (e instanceof KeyEvent) {
            KeyEvent key= (KeyEvent) e;
            String modifiersText = KeyEvent.getKeyModifiersText(key.getModifiers());
            String keyStr = "";
            if (modifiersText != null && !"".equals(modifiersText)) {
                keyStr += modifiersText + "+";
            }
            if (keyStr.indexOf(KeyEvent.getKeyText(key.getKeyCode())) == -1) {
                keyStr += KeyEvent.getKeyText(key.getKeyCode());
            } else {
                keyStr = keyStr.substring(0, keyStr.length() - 1);
            }
            if (keyStr.indexOf("箭头") > -1) {
                keyStr = keyStr.replaceAll("向上箭头", "↑")
                        .replaceAll("向下箭头", "↓")
                        .replaceAll("向右箭头", "→")
                        .replaceAll("向左箭头", "←");
            }
            return keyStr;
        }else if (e instanceof MouseEvent){
            MouseEvent event = (MouseEvent) e;
            if (event.getButton()<4) {
                return "";
            }
            return "鼠标侧键"+(event.getButton()-3);
        }
        return "";
    }
}
