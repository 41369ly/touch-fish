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
        //控制台字体样式
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
        //控制台下拉框
        isConsoleCheck.setSelectedIndex(persistentState.getIsConsole()?0:1);
        isConsoleCheck.addItemListener(e -> persistentState.setIsConsole("是".equals(e.getItem())));
        //文件选择器
        filePath.addBrowseFolderListener("选择书籍", null, null, new FileChooserDescriptor(true, false, false, false, false, false));
        //新增书籍按钮弹窗
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
        //删除书籍按钮
        delButton.addActionListener(e -> {
            if (persistentState.delBook(bookTable.getSelectedRow())) {
                updBookReading();
                bookDataInit();
                EventListener.book=null;
            }
        });
        //选中书籍按钮
        selectBtn.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow > -1) {
                persistentState.setBookIndex(selectedRow);
                EventListener.book=null;
                updBookReading();
            }
        });
        //定时器时间设置
        nextInfoTime.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (!"".equals(nextInfoTime.getText())&&pattern.matcher(nextInfoTime.getText()).matches()) {
                    persistentState.setNextInfoTime(Integer.valueOf(nextInfoTime.getText()));
                    updBookReading();
                } else {
                    bookReading.setText("请输入正确的数字");
                }
            }
        });
        if (persistentState.getNextInfoTime()==null||persistentState.getNextInfoTime()==0){
            persistentState.setNextInfoTime(10);
        }
        nextInfoTime.setText(persistentState.getNextInfoTime().toString());
        String[] key = persistentState.getKey();
        if (key == null) {
            key = new String[]{"Alt+←", "Alt+→", "Ctrl+1", "Ctrl+2", "Shift+↑","Shift+↓"};
            persistentState.setKey(key);
        }
        updBookReading();
        bookDataInit();
        updKeyMap();
        // 章节名匹配输入框
        regexpStrInput();
    }

    /**
     * 章节名正则匹配输入框
     * @author      wyj
     * @date        2021/12/7 17:57
     */
    private void regexpStrInput() {
        //定时器时间设置
        regexpStr.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (!"".equals(regexpStr.getText())) {
                    persistentState.setRegexpStr(regexpStr.getText());
                } else {
                    persistentState.setRegexpStr("(^\\s*第)(.{1,9})[章节卷集部篇回](\\s{1})(.*)($\\s*)");
                }
            }
        });
        if (persistentState.getRegexpStr()==null||"".equals(persistentState.getRegexpStr())){
            persistentState.setRegexpStr("(^\\s*第)(.{1,9})[章节卷集部篇回](\\s{1})(.*)($\\s*)");
        }
        regexpStr.setText(persistentState.getRegexpStr());
    }

    /**
     * 导入按钮初始化
     */
    private void addBtnInit(){
        addBtn.addActionListener(e -> {
            if (filePath.getText() == null || "".equals(filePath.getText()) || !filePath.getText().substring(filePath.getText().lastIndexOf('.')).equals(".txt")) {
                MessageDialogBuilder.yesNo("提示", "请选择正确的文件(必须为txt)").show();
                return;
            }
            File file = new File(filePath.getText());
            if (file.exists()) {
                String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                Book book = new Book(filePath.getText(), fileName, "自定义导入");
                bookReading.setText("正在解析章节，导入中...");
                new Thread(() -> {
                    persistentState.addBook(book);
                    updBookReading();
                    bookDataInit();
                }).start();
            } else {
                MessageDialogBuilder.yesNo("提示", "文件不存在，请选择正确的文件").show();
            }
        });
    }

    /**
     * 热键表格初始化
     */
    private void keyMapInit(){
        //鼠标监听事件
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
        //键盘监听事件
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
     * 更新热键
     */
    private void updKeyMap() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("name", new String[]{"上一行热键", "下一行热键", "上一章热键", "下一章热键", "启动/停止自动翻页","显示/隐藏热键"});
        model.addColumn("key", persistentState.getKey());
        keyMap.setModel(model);
    }

    /**
     * 正在阅读的书籍
     */
    private void updBookReading() {
        List<Book> books = persistentState.getBook();
        if (books==null||books.size()==0){
            bookReading.setText("书架中还没有书,赶紧去添加吧！");
            return;
        }
        Book book = null;
        try {
            book = persistentState.getBookByIndex();
        } catch (FishException e) {
            MessageDialogBuilder.yesNo("提示", e.getMessage()).show();
            return;
        }
        String text = "<html>当前阅读书籍：" + book.getBookName();
        if (book.getChapters() != null && book.getChapters().size() > 0) {
            try {
                text += "正在阅读：" + book.getChapterByIndex().getTitle() + "</html>";
            } catch (FishException e) {
            }
        }
        bookReading.setText(text);
    }

    /**
     * 初始化UI
     */
    private void createUIComponents() {
        bookTable = new JCTable();
        keyMap = new JCTable();
        filePath = new TextFieldWithBrowseButton();
        urlField = new JTextField();
        nextInfoTime = new JTextField();
    }

    /**
     * 数据源文本框初始化
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
     * 书架数据初始化
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
     * 书本数据初始化
     */
    private void bookDataInit() {
        List<Book> books = persistentState.getBook();
        if (books == null) {
            return;
        }
        bookTable.setModel(JtableDataUtils.bookToTableModel(books));
    }
}
