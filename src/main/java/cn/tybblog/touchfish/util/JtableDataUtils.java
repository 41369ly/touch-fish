package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.entity.Book;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * @author ly
 */
public class JtableDataUtils {
    /**
     * 书转表格数据模型
     * @param books 书
     * @return 数据模型
     */
    public static DefaultTableModel bookToTableModel(List<Book> books){
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"书名", "作者"});
        for (Book book : books) {
            String[] row = new String[]{book.getBookName(), book.getAuth()};
            model.addRow(row);
        }
        return model;
    }
}
