package cn.tybblog.touchfish.ui;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.ui.field.SearchTextField;
import cn.tybblog.touchfish.ui.table.JCTable;
import cn.tybblog.touchfish.util.HttpRequest;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ((JBTable) e.getSource()).getSelectedRow();
                    if (persistentState != null) {
                        Book book = books.get(row);
                        List<Book> books = persistentState.getBook();
                        if (books == null) books = new ArrayList<>();
                        boolean flag = true;
                        for (Book b : books) {
                            if (b.getUrl().equals(book.getUrl())) {
                                flag = false;
                                MessageDialogBuilder.YesNo msg = MessageDialogBuilder.yesNo("提示", "此书已在书架中！");
                                if (msg.isYes()) onCancel();
                            }
                        }
                        if (flag) {
                            books.add(book);
                            persistentState.setBook(books);
                            onCancel();
                        }
                    }
                }
            }
        });

        myLoadingIcon.setOpaque(true);
        myLoadingIcon.setPaintPassiveIcon(false);
        myLoadingIcon.setVisible(false);
        contentPane.add(myLoadingIcon, new GridConstraints());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        dispose();
    }

    private void addBook() {
        if (myTextField.getText() == null || "".equals(myTextField.getText())) return;
        String html = HttpRequest.sendPost("http://www.xbiquge.la/modules/article/waps.php", myTextField.getText());
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("#checkform table tr");
        books = new ArrayList<Book>();
        Vector bookNames = new Vector();
        Vector auths = new Vector();
        int i = 0;
        for (Element tr : trs) {
            if (i != 0) {
                Element bookName = tr.child(0);
                Element auth = tr.child(2);
                bookNames.add(bookName.text());
                auths.add(auth.text());
                books.add(new Book(tr.selectFirst("td a").attr("href"), bookName.text(), auth.text()));
            } else {
                i++;
            }
        }
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("书名", bookNames);
        model.addColumn("作者", auths);
        bookTale.setShowColumns(true);
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
