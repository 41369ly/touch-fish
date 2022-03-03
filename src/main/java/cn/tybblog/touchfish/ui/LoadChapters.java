package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.ui.dialog.FishDialog;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
        searchName.setText("");
        searchBtn.addActionListener(e -> {
            new Thread(() -> searchTitle(bookIndex)).start();
        });
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

    private void searchTitle(int bookIndex) {
        String titleTxt = searchName.getText();

        // ƥ���½�����
        List<Chapter> list = persistentState.getBook().get(bookIndex).getChapters();
        if (titleTxt == null || "".equals(titleTxt)) {
            chaptersJList.setListData(list.toArray());
            return;
        }
        List<Chapter> titleList = new ArrayList<>();
        for (Chapter cp : list) {
            if (cp.getTitle().contains(titleTxt)) {
                titleList.add(cp);
            }
        }
        chaptersJList.setListData(titleList.toArray());
    }
}
