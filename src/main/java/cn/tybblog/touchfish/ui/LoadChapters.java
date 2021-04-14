package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.ui.dialog.FishDialog;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Administrator
 */
public class LoadChapters extends FishDialog {
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
            MessageDialogBuilder.yesNo("��ʾ", "�����ѱ�ɾ����").show();
            onCancel();
        }
        if(persistentState.getBook().get(bookIndex).getChapters()==null){
            MessageDialogBuilder.yesNo("��ʾ", "�����Ķ���ȡ�½ں���ѡ���½�").show();
            onCancel();
        }
        searchName.setText("��δ�����������ܣ������ڴ�");
        chaptersJList.setListData(persistentState.getBook().get(bookIndex).getChapters().toArray());
        chaptersJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("��ʾ", "ȷ��Ҫ�л��� "+chaptersJList.getSelectedValue()+" ��");
                    if(msg.isYes()) {
                        persistentState.getBook().get(bookIndex).setIndex(chaptersJList.getSelectedIndex()-1);
                        onCancel();
                    }

                }
            }
        });
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
        chaptersJList=new JBList();
        JBScrollPane1=new JBScrollPane();
    }
}
