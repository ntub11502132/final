import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class TicketInfoUI extends JPanel {

    public TicketInfoUI() {
        setLayout(new BorderLayout(10, 10)); // 使用 BorderLayout，設定元件間距
        setBackground(new Color(0x95B0D0)); // 背景色使用圖片中的淡藍色

        // 表格區域
        String[] columnNames = {"廳別", "早鳥票", "全票", "優待票"};
        Object[][] data = {
                {"Hall A", "275", "300", "250"},
                {"Hall B", "275", "300", "250"}
        };

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有單元格都不可編輯
            }
        };

        JTable ticketTable = new JTable(tableModel);
        ticketTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16)); // 表格內容字體
        ticketTable.setRowHeight(85); // 調整行高
        
        // **關鍵修改：移除網格線**
        ticketTable.setShowGrid(false); // 不顯示網格線
        ticketTable.setIntercellSpacing(new Dimension(0, 0)); // 單元格之間沒有間距
        
        // 設定表格背景色，讓它與 JPanel 背景色一致
        ticketTable.setBackground(new Color(0x95B0D0));
        ticketTable.setForeground(Color.BLACK); // 文字顏色

        // 表頭設定
        JTableHeader tableHeader = ticketTable.getTableHeader();
        tableHeader.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18)); // 表頭字體
        tableHeader.setBackground(new Color(0x95B0D0)); // 表頭背景色
        tableHeader.setForeground(Color.BLACK); // 表頭文字顏色
        tableHeader.setReorderingAllowed(false); // 禁止拖動列
        tableHeader.setResizingAllowed(false); // 禁止調整列寬

        // **關鍵修改：設置自定義單元格渲染器，確保背景透明或與表格背景一致**
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(0x95B0D0)); // 確保單元格背景色與表格背景色一致
                setHorizontalAlignment(JLabel.CENTER); // 水平置中
                return c;
            }
        };
        ticketTable.setDefaultRenderer(Object.class, centerRenderer);

        // 設定第一列（廳別/票種）靠左對齊，並加粗
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(0x95B0D0)); // 確保背景色一致
                setHorizontalAlignment(JLabel.LEFT); // 水平靠左
                return c;
            }
        };
        // 重新設定第一列的字體，以確保粗體生效
        leftRenderer.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        ticketTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);


        JScrollPane scrollPane = new JScrollPane(ticketTable);
        // **關鍵修改：移除 JScrollPane 的邊框**
        scrollPane.setBorder(null); // 移除 JScrollPane 的邊框
        // 為了讓背景色延伸到ScrollPane的空白處，設定Viewport的背景色
        scrollPane.getViewport().setBackground(new Color(0x95B0D0));
        
        add(scrollPane, BorderLayout.CENTER);

        // 下方說明文字面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // 垂直佈局
        infoPanel.setBackground(new Color(0x95B0D0));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); // 內邊距

        JLabel note1 = new JLabel("早鳥票：早上 7:00 - 中午 12:00");
        JLabel note2 = new JLabel("全票/優待票：中午 12:01 - 早上 6:59");
        JLabel note3 = new JLabel("優待票限制 12歲以下或 65歲以上");

        Font noteFont = new Font("Microsoft JhengHei", Font.PLAIN, 14);
        note1.setFont(noteFont);
        note2.setFont(noteFont);
        note3.setFont(noteFont);

        // 靠左對齊
        note1.setAlignmentX(Component.LEFT_ALIGNMENT);
        note2.setAlignmentX(Component.LEFT_ALIGNMENT);
        note3.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(note1);
        infoPanel.add(Box.createVerticalStrut(5)); // 間距
        infoPanel.add(note2);
        infoPanel.add(Box.createVerticalStrut(5)); // 間距
        infoPanel.add(note3);

        add(infoPanel, BorderLayout.SOUTH);
    }
}