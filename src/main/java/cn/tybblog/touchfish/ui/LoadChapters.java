package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.ui.field.SearchTextField;
import cn.tybblog.touchfish.ui.table.JCTable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.MessageDialogBuilder;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class LoadChapters extends JDialog implements Disposable {
    private JList chaptersJList;
    private JPanel panel1;
    private JTextField searchName;
    private JButton searchBtn;
    private static PersistentState persistentState = PersistentState.getInstance();

    public LoadChapters(int bookIndex){
        if (bookIndex<0&&bookIndex>=persistentState.getBook().size()) {
            MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("提示", "此书已被删除！");
            if (msg.isYes()) onCancel();
        }
        searchName.setText("暂未开发搜索功能，敬请期待");
        chaptersJList.setListData(persistentState.getBook().get(bookIndex).getChapters().toArray());
        chaptersJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    persistentState.getBook().get(bookIndex).setIndex(chaptersJList.getSelectedIndex());
                }
            }
        });
    }

    private void onCancel() {
        dispose();
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
