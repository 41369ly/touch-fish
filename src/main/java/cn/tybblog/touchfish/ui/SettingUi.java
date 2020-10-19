package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.ui.field.SearchTextField;
import cn.tybblog.touchfish.ui.table.JCTable;
import com.intellij.ui.KeyStrokeAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

public class SettingUi {
    public JPanel mainPanel;
    private JButton searchBtn;
    private JCTable bookTable;
    private JButton delButton;
    private JCTable keyMap;
    private JButton selectBtn;
    private JLabel bookReading;
    private JButton refBtn;
    private PersistentState persistentState = PersistentState.getInstance();

    public SettingUi() {
        searchBtn.addActionListener(e -> {
            BookUi dialog = new BookUi();
            dialog.pack();
            dialog.setSize(300, 300);
            int x = (Toolkit.getDefaultToolkit().getScreenSize().width - dialog.getSize().width) / 2;
            int y = (Toolkit.getDefaultToolkit().getScreenSize().height - dialog.getSize().height) / 2;
            dialog.setLocation(x, y);
            dialog.setVisible(true);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    addBook();
                    super.windowClosed(e);
                }
            });
        });
        delButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow > -1) {
                persistentState.getBook().remove(selectedRow);
                if (persistentState.getBookIndex()==selectedRow) {
                    persistentState.setBookIndex(selectedRow - 1);
                    updBookReading();
                }
                addBook();
            }
        });
        refBtn.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow>=0)
                persistentState.getBook().get(selectedRow).setChapters(null);
        });
        keyMap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int row = keyMap.getSelectedRow();
                if (row == -1) return;
                String modifiersText = e.getKeyModifiersText(e.getModifiers());
                String key = "";
                if (modifiersText != null && !"".equals(modifiersText))
                    key += modifiersText + "+";
                if (key.indexOf(e.getKeyText(e.getKeyCode()))==-1)
                    key += e.getKeyText(e.getKeyCode());
                else
                    key=key.substring(0,key.length()-1);
                if (key.indexOf("��ͷ")>-1)
                    key=key.replaceAll("���ϼ�ͷ","��")
                            .replaceAll("���¼�ͷ","��")
                            .replaceAll("���Ҽ�ͷ","��")
                            .replaceAll("�����ͷ","��");
                String[] key1 = persistentState.getKey();
                key1[row] = key;
                keyMap.setValueAt(key, row, 1);
                persistentState.setKey(key1);
                super.keyPressed(e);
            }
        });
        selectBtn.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow > -1) {
                persistentState.setBookIndex(selectedRow);
                updBookReading();
            }
        });
        String[] key = persistentState.getKey();
        if (key == null) {
            key = new String[]{"Alt + ��", "Alt + ��", "Ctrl + 1", "Ctrl + 2", "Shift + ��", "Shift + ��"};
            persistentState.setKey(key);
        }
        updBookReading();
        addBook();
        updKeyMap();
    }

    private void addBook() {
        List<Book> books = this.persistentState.getBook();
        if (books == null) {
            return;
        }
        Vector bookNames = new Vector();
        Vector auths = new Vector();
        for (Book book : books) {
            bookNames.add(book.getBookName());
            auths.add(book.getAuth());
        }
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("����", bookNames);
        model.addColumn("����", auths);
        bookTable.setModel(model);
    }

    private void updKeyMap() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("name", new String[]{"��һ���ȼ�", "��һ���ȼ�", "��һ���ȼ�", "��һ���ȼ�", "��ʾ�ȼ�", "�����ȼ�"});
        model.addColumn("key", persistentState.getKey());
        keyMap.setModel(model);
    }

    private void updBookReading(){
        if (persistentState.getBook()==null||persistentState.getBook().size()==0) {
            bookReading.setText("����л�û����,�Ͻ�ȥ���");
            return;
        }
        Book book = persistentState.getBook().get(persistentState.getBookIndex());
        String text = "<html>��ǰ�Ķ��鼮��" + book.getBookName();
        if (book.getChapters()!=null&&book.getChapters().size()>0)
            text+="�����Ķ���" + book.getChapters().get(book.getIndex()).getTitle()+"</html>";
        bookReading.setText(text);
    }

    private void createUIComponents() {
        bookTable = new JCTable();
        keyMap = new JCTable();
    }
}
