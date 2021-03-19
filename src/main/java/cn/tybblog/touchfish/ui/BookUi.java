package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.ui.field.SearchTextField;
import cn.tybblog.touchfish.ui.table.JCTable;
import cn.tybblog.touchfish.util.HttpCallBack;
import cn.tybblog.touchfish.util.HttpRequest;
import cn.tybblog.touchfish.util.NetworkUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class BookUi extends JDialog implements Disposable {
    private JPanel contentPane;
    private JButton SearchButton;
    private SearchTextField myTextField;
    private JCTable bookTale;
    private List<Book> books;
    private PersistentState persistentState = PersistentState.getInstance();

    private final AsyncProcessIcon myLoadingIcon = new AsyncProcessIcon("Loading...");

    public BookUi() {
        setContentPane(contentPane);
        setModal(true);
        SearchButton.addActionListener(e -> {
            myLoadingIcon.setVisible(true);
            new Thread(() -> addBook()).start();
        });

        bookTale.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bookTale.getSelectedRow();
                    if (persistentState == null) {
                        return;
                    }
                    Book book = books.get(row);
                    List<Book> books = persistentState.getBook();
                    if (books == null) {
                        books = new ArrayList<>();
                    }
                    for (Book book1 : books) {
                        if (book1.getUrl().equals(book.getUrl())) {
                            MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("提示", "此书已在书架中！");
                            if (msg.isYes()) {
                                onCancel();
                            }
                            return;
                        }
                    }
                    books.add(book);
                    persistentState.setBook(books);
                    onCancel();
                }
            }
        });

        myLoadingIcon.setOpaque(true);
        myLoadingIcon.setPaintPassiveIcon(false);
        myLoadingIcon.setVisible(false);
        contentPane.add(myLoadingIcon, new GridConstraints());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        dispose();
    }

    private void addBook() {
        if (myTextField.getText() == null || "".equals(myTextField.getText())) {
            return;
        }
        //获取书籍
        try {
            books = NetworkUtil.SearchBook(myTextField.getText());
        }catch (Exception e){
            if (e.getMessage().equals("timeout")) {
                MessageDialogBuilder.yesNo("提示", "加载超时！");
            }
        }
        DefaultTableModel model = new DefaultTableModel();
        //表头
        model.setColumnIdentifiers(new String[]{"书名", "作者"});
        for (Book book : books) {
            String[] row = new String[]{book.getBookName(), book.getAuth()};
            model.addRow(row);
        }
        bookTale.setModel(model);
        myLoadingIcon.setVisible(false);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        myTextField = new SearchTextField();
        bookTale = new JCTable();
        myTextField.callBack(SearchButton);
    }
}
