package cn.tybblog.touchfish.ui.dialog;

import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;

/**
 * @author ly
 */
public class FishDialog extends JDialog implements Disposable {
    protected void onCancel() {
        dispose();
    }
    public void initUI(){
        this.pack();
        this.setSize(300, 300);
        int x = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 2;
        int y = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 2;
        this.setLocation(x, y);
        this.setVisible(true);
    }
}
