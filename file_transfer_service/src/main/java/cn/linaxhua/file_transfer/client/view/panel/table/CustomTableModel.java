package cn.linaxhua.file_transfer.client.view.panel.table;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
