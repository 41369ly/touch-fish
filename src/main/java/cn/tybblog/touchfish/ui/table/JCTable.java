package cn.tybblog.touchfish.ui.table;

import com.intellij.ui.table.JBTable;

public class JCTable extends JBTable {
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
}