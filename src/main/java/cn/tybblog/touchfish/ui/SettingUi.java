package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.exception.FishException;
import cn.tybblog.touchfish.listener.EventListener;
import cn.tybblog.touchfish.ui.table.JCTable;
import cn.tybblog.touchfish.util.JtableDataUtils;
import cn.tybblog.touchfish.util.KeyMapFormatUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ly
 */
public class SettingUi {
    public JPanel mainPanel;
    private JButton searchBtn;
    private JCTable bookTable;
    private JButton delButton;
    private JCTable keyMap;
    private JButton selectBtn;
    private JLabel bookReading;
    private JButton addBtn;
    private TextFieldWithBrowseButton filePath;
    private JTextField urlField;
    private JTextField nextInfoTime;
    private JComboBox isConsoleCheck;
    private JTextArea fontStyleTextArea;
    private JTextField regexpStr;
    private static PersistentState persistentState = PersistentState.getInstance();

    private static Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");

    public SettingUi() {
        urlFieldInit();
        bookTableInit();
        keyMapInit();
        addBtnInit();
        //����̨������ʽ
        fontStyleTextArea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                persistentState.setFontStyle(fontStyleTextArea.getText());
            }
        });
        if (persistentState.getFontStyle()==null){
            persistentState.setFontStyle("{font-size: 10px;}");
        }
        fontStyleTextArea.setText(persistentState.getFontStyle());
        //����̨������
        isConsoleCheck.setSelectedIndex(persistentState.getIsConsole()?0:1);
        isConsoleCheck.addItemListener(e -> persistentState.setIsConsole("��".equals(e.getItem())));
        //�ļ�ѡ����
        filePath.addBrowseFolderListener("ѡ���鼮", null, null, new FileChooserDescriptor(true, false, false, false, false, false));
        //�����鼮��ť����
        searchBtn.addActionListener(e -> {
            BookUi dialog = new BookUi();
            dialog.initUI();
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    bookDataInit();
                    super.windowClosed(e);
                }
            });
        });
        //ɾ���鼮��ť
        delButton.addActionListener(e -> {
            if (persistentState.delBook(bookTable.getSelectedRow())) {
                updBookReading();
                bookDataInit();
                EventListener.book=null;
            }
        });
        //ѡ���鼮��ť
        selectBtn.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow > -1) {
                persistentState.setBookIndex(selectedRow);
                EventListener.book=null;
                updBookReading();
            }
        });
        //��ʱ��ʱ������
        nextInfoTime.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (!"".equals(nextInfoTime.getText())&&pattern.matcher(nextInfoTime.getText()).matches()) {
                    persistentState.setNextInfoTime(Integer.valueOf(nextInfoTime.getText()));
                    updBookReading();
                } else {
                    bookReading.setText("��������ȷ������");
                }
            }
        });
        if (persistentState.getNextInfoTime()==null||persistentState.getNextInfoTime()==0){
            persistentState.setNextInfoTime(10);
        }
        nextInfoTime.setText(persistentState.getNextInfoTime().toString());
        String[] key = persistentState.getKey();
        if (key == null) {
            key = new String[]{"Alt+��", "Alt+��", "Ctrl+1", "Ctrl+2", "Shift+��","Shift+��"};
            persistentState.setKey(key);
        }
        updBookReading();
        bookDataInit();
        updKeyMap();
        // �½���ƥ�������
        regexpStrInput();
    }

    /**
     * �½�������ƥ�������
     * @author      wyj
     * @date        2021/12/7 17:57
     */
    private void regexpStrInput() {
        //��ʱ��ʱ������
        regexpStr.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (!"".equals(regexpStr.getText())) {
                    persistentState.setRegexpStr(regexpStr.getText());
                } else {
                    persistentState.setRegexpStr("(^\\s*��)(.{1,9})[�½ھ���ƪ��](\\s{1})(.*)($\\s*)");
                }
            }
        });
        if (persistentState.getRegexpStr()==null||"".equals(persistentState.getRegexpStr())){
            persistentState.setRegexpStr("(^\\s*��)(.{1,9})[�½ھ���ƪ��](\\s{1})(.*)($\\s*)");
        }
        regexpStr.setText(persistentState.getRegexpStr());
    }

    /**
     * ���밴ť��ʼ��
     */
    private void addBtnInit(){
        addBtn.addActionListener(e -> {
            if (filePath.getText() == null || "".equals(filePath.getText()) || !filePath.getText().substring(filePath.getText().lastIndexOf('.')).equals(".txt")) {
                MessageDialogBuilder.yesNo("��ʾ", "��ѡ����ȷ���ļ�(����Ϊtxt)").show();
                return;
            }
            File file = new File(filePath.getText());
            if (file.exists()) {
                String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                Book book = new Book(filePath.getText(), fileName, "�Զ��嵼��");
                bookReading.setText("���ڽ����½ڣ�������...");
                new Thread(() -> {
                    persistentState.addBook(book);
                    updBookReading();
                    bookDataInit();
                }).start();
            } else {
                MessageDialogBuilder.yesNo("��ʾ", "�ļ������ڣ���ѡ����ȷ���ļ�").show();
            }
        });
    }

    /**
     * �ȼ�����ʼ��
     */
    private void keyMapInit(){
        //�������¼�
        keyMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = keyMap.getSelectedRow();
                if (row == -1) {
                    return;
                }
                if (e.getButton() > 3) {
                    String key = KeyMapFormatUtils.keyMapFormat(e);
                    if ("".equals(key)) {
                        return;
                    }
                    keyMap.setValueAt(key, row, 1);
                    persistentState.setKeyMap(key,row);
                }
            }
        });
        //���̼����¼�
        keyMap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int row = keyMap.getSelectedRow();
                if (row == -1) {
                    return;
                }
                String key = KeyMapFormatUtils.keyMapFormat(e);
                if ("".equals(key)) {
                    return;
                }
                persistentState.setKeyMap(key,row);
                keyMap.setValueAt(key, row, 1);
                super.keyPressed(e);
            }
        });
    }

    /**
     * �����ȼ�
     */
    private void updKeyMap() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("name", new String[]{"��һ���ȼ�", "��һ���ȼ�", "��һ���ȼ�", "��һ���ȼ�", "����/ֹͣ�Զ���ҳ","��ʾ/�����ȼ�"});
        model.addColumn("key", persistentState.getKey());
        keyMap.setModel(model);
    }

    /**
     * �����Ķ����鼮
     */
    private void updBookReading() {
        List<Book> books = persistentState.getBook();
        if (books==null||books.size()==0){
            bookReading.setText("����л�û����,�Ͻ�ȥ��Ӱɣ�");
            return;
        }
        Book book = null;
        try {
            book = persistentState.getBookByIndex();
        } catch (FishException e) {
            MessageDialogBuilder.yesNo("��ʾ", e.getMessage()).show();
            return;
        }
        String text = "<html>��ǰ�Ķ��鼮��" + book.getBookName();
        if (book.getChapters() != null && book.getChapters().size() > 0) {
            try {
                text += "�����Ķ���" + book.getChapterByIndex().getTitle() + "</html>";
            } catch (FishException e) {
            }
        }
        bookReading.setText(text);
    }

    /**
     * ��ʼ��UI
     */
    private void createUIComponents() {
        bookTable = new JCTable();
        keyMap = new JCTable();
        filePath = new TextFieldWithBrowseButton();
        urlField = new JTextField();
        nextInfoTime = new JTextField();
    }

    /**
     * ����Դ�ı����ʼ��
     */
    private void urlFieldInit() {
        if (persistentState.getUrl() == null) {
            persistentState.setUrl("http://www.xbiquge.la");
        }
        urlField.setText(persistentState.getUrl());
        urlField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                persistentState.setUrl(urlField.getText());
            }
        });
    }

    /**
     * ������ݳ�ʼ��
     */
    private void bookTableInit() {
        bookDataInit();
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bookTable.getSelectedRow();
                    LoadChapters dialog = new LoadChapters(row);
                    dialog.initUI();
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            updBookReading();
                            super.windowClosed(e);
                        }
                    });
                }
            }
        });
    }

    /**
     * �鱾���ݳ�ʼ��
     */
    private void bookDataInit() {
        List<Book> books = persistentState.getBook();
        if (books == null) {
            return;
        }
        bookTable.setModel(JtableDataUtils.bookToTableModel(books));
    }
}
