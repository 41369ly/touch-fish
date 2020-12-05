package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.ui.table.JCTable;
import cn.tybblog.touchfish.util.FileCode;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.table.JBTable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private JButton addBtn;
    private TextFieldWithBrowseButton filePath;
    private static PersistentState persistentState = PersistentState.getInstance();

    public SettingUi() {
        bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bookTable.getSelectedRow();
                    LoadChapters dialog=new LoadChapters(row);
                    dialog.pack();
                    dialog.setSize(300, 300);
                    int x = (Toolkit.getDefaultToolkit().getScreenSize().width - dialog.getSize().width) / 2;
                    int y = (Toolkit.getDefaultToolkit().getScreenSize().height - dialog.getSize().height) / 2;
                    dialog.setLocation(x, y);
                    dialog.setVisible(true);
                }
            }
        });
        addBtn.addActionListener(e -> {
            if (filePath.getText()==null||"".equals(filePath.getText())||!filePath.getText().substring(filePath.getText().lastIndexOf('.')).equals(".txt")){
                MessageDialogBuilder.yesNo("提示", "请选择正确的文件(必须为txt)").show();
                return;
            }
            for (Book b : persistentState.getBook()) {
                if (b.getUrl().equals(filePath.getText())) {
                   MessageDialogBuilder.yesNo("提示", "此书已在书架中！").show();
                   return;
                }
            }
            File file = new File(filePath.getText());
            if (file.exists()) {
                splitFile(file);
                addBook();
            } else
                MessageDialogBuilder.yesNo("提示", "文件不存在，请选择正确的文件").show();
        });
        filePath.addBrowseFolderListener("选择书籍",null, null,new FileChooserDescriptor(true,false,false,false,false,false));
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
                Book book = persistentState.getBook().get(selectedRow);
                if ("自定义导入".equals(book.getAuth())) {
                    try {
                        FileUtils.deleteDirectory(new File(book.getUrl().substring(0,book.getUrl().lastIndexOf('\\'))+"\\temp"));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
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
                if (key.indexOf("箭头")>-1)
                    key=key.replaceAll("向上箭头","↑")
                            .replaceAll("向下箭头","↓")
                            .replaceAll("向右箭头","→")
                            .replaceAll("向左箭头","←");
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
            key = new String[]{"Alt+←", "Alt+→", "Ctrl+1", "Ctrl+2", "Shift+↑", "Shift+↓"};
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
        model.addColumn("书名", bookNames);
        model.addColumn("作者", auths);
        bookTable.setModel(model);
    }

    private void updKeyMap() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("name", new String[]{"上一行热键", "下一行热键", "上一章热键", "下一章热键", "显示热键", "隐藏热键"});
        model.addColumn("key", persistentState.getKey());
        keyMap.setModel(model);
    }

    private void updBookReading(){
        if (persistentState.getBook()==null||persistentState.getBook().size()==0) {
            bookReading.setText("书架中还没有书,赶紧去添加");
            return;
        }
        if(persistentState.getBook().size()>=persistentState.getBookIndex()) persistentState.setBookIndex(persistentState.getBook().size()-1);
        Book book = persistentState.getBook().get(persistentState.getBookIndex());
        String text = "<html>当前阅读书籍：" + book.getBookName();
        if (book.getChapters()!=null&&book.getChapters().size()>0)
            text+="正在阅读：" + book.getChapters().get(book.getIndex()).getTitle()+"</html>";
        bookReading.setText(text);
    }

    public static void splitFile(File file){
        String fileName = file.getName().substring(0,file.getName().lastIndexOf('.'));
        Book book = new Book(file.getPath(),fileName,"自定义导入");
        List<Chapter> chapters=new ArrayList<>();
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(file, FileCode.codeString(file.getPath()));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int j=0;
        while (it.hasNext()) {
            String strs="";
            for (int i = 0; i < 99; i++) {
                if (it.hasNext())
                    strs+=it.nextLine()+"\n";
                else
                    break;
            }
            String filePath = file.getPath().substring(0,file.getPath().lastIndexOf('\\')) + "\\temp\\" + fileName + j + ".txt";
            try {
                FileUtils.write(new File(filePath),strs,"UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Chapter chapter=new Chapter();
            chapter.setUrl(filePath);
            chapter.setTitle("");
            chapter.setRow(-1);
            chapters.add(chapter);
            j++;
        }
        book.setChapters(chapters);
        List<Book> books = persistentState.getBook();
        if (books == null) books=new ArrayList<>();
        books.add(book);
        persistentState.setBook(books);
        try {
            it.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        bookTable = new JCTable();
        keyMap = new JCTable();
        filePath=new TextFieldWithBrowseButton();
    }
}
