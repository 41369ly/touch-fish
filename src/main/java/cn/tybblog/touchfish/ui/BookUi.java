package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.ui.dialog.FishDialog;
import cn.tybblog.touchfish.ui.field.SearchTextField;
import cn.tybblog.touchfish.ui.table.JCTable;
import cn.tybblog.touchfish.util.JtableDataUtils;
import cn.tybblog.touchfish.util.NetworkUtil;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class BookUi extends FishDialog {
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
                    if (persistentState == null || row < 0) {
                        return;
                    }
                    Book book = books.get(row);
                    if (!persistentState.addBook(book)) {
                        MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("提示", "此书已在书架中！");
                        if (!msg.isYes()) {
                            return;
                        }
                    }
                    onCancel();
                }
            }
        });
        //加载中图标初始化
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


    private void addBook() {
        if (myTextField.getText() == null || "".equals(myTextField.getText())) {
            return;
        }
        //获取书籍
        books = NetworkUtil.SearchBook(myTextField.getText());
        bookTale.setModel(JtableDataUtils.bookToTableModel(books));
        myLoadingIcon.setVisible(false);
    }



    private void createUIComponents() {
        // TODO: place custom component creation code here
        myTextField = new SearchTextField();
        bookTale = new JCTable();
        myTextField.callBack(SearchButton);
    }
}
