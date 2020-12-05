package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoadChapters extends JDialog implements Disposable {
    private JPanel panel1;
    private JTextField searchName;
    private JButton searchBtn;
    private JList chaptersJList;
    private JBScrollPane JBScrollPane1;
    private static PersistentState persistentState = PersistentState.getInstance();

    public LoadChapters(int bookIndex){
        setContentPane(panel1);
        setModal(true);
        if (bookIndex<0||bookIndex>=persistentState.getBook().size()) {
            MessageDialogBuilder.yesNo("提示", "此书已被删除！").show();
            onCancel();
        }
        if(persistentState.getBook().get(bookIndex).getChapters()==null){
            MessageDialogBuilder.yesNo("提示", "请先阅读获取章节后再选择章节").show();
            onCancel();
        }
        searchName.setText("暂未开发搜索功能，敬请期待");
        chaptersJList.setListData(persistentState.getBook().get(bookIndex).getChapters().toArray());
        chaptersJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("提示", "确定要切换至 "+chaptersJList.getSelectedValue()+" 吗？");
                    if(msg.isYes()) {
                        persistentState.getBook().get(bookIndex).setIndex(chaptersJList.getSelectedIndex());
                        onCancel();
                    }

                }
            }
        });
    }

    private void onCancel() {
        dispose();
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
        chaptersJList=new JBList();
        JBScrollPane1=new JBScrollPane();
    }
}
