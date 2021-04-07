package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.entity.Book;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * @author ly
 */
public class JtableDataUtils {
    /**
     * ��ת�������ģ��
     * @param books ��
     * @return ����ģ��
     */
    public static DefaultTableModel bookToTableModel(List<Book> books){
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"����", "����"});
        for (Book book : books) {
            String[] row = new String[]{book.getBookName(), book.getAuth()};
            model.addRow(row);
        }
        return model;
    }
}
