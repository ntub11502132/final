// File: operational_side/src/TicketStatusPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer; // 新增導入
import javax.swing.table.TableCellEditor; // 新增導入
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class TicketStatusPanel extends JPanel {

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private OrderManager orderManager;
    private JButton refreshButton;

    // 定義訂單狀態的選項
    private static final String[] ORDER_STATUS_OPTIONS = {"待付款", "已付款", "已取消"};

    public TicketStatusPanel() {
        orderManager = new OrderManager();

        setBackground(new Color(230, 240, 255));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel headerLabel = new JLabel("訂票狀態查詢與管理"); // 修改標題
        headerLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        // 表格設定，新增一列用於「操作」按鈕
        String[] columnNames = {"訂單ID", "會員名稱", "電影名稱", "場次時間", "影廳名稱", "座位", "訂票時間", "訂單狀態", "操作"}; // 新增「操作」列
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 使「操作」列可編輯（用於按鈕），其他列不可編輯
                return column == getColumnCount() - 1; // 只有最後一列可編輯
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        orderTable.setRowHeight(25);
        orderTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        orderTable.setAutoCreateRowSorter(true); // 允許排序

        // 為最後一列（操作列）設定自定義的渲染器和編輯器，以便顯示按鈕
        orderTable.getColumnModel().getColumn(orderTable.getColumnCount() - 1).setCellRenderer(new ButtonRenderer());
        orderTable.getColumnModel().getColumn(orderTable.getColumnCount() - 1).setCellEditor(new ButtonEditor(new JCheckBox(), this)); // 傳遞 TicketStatusPanel 實例

        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("刷新數據");
        refreshButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadOrderData();

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOrderData(); // 重新載入數據
            }
        });
    }

    // 載入訂單數據並填充表格
    private void loadOrderData() {
        tableModel.setRowCount(0); // 清空現有數據

        List<Order> allOrders = orderManager.loadOrders();

        if (allOrders.isEmpty()) {
            System.out.println("沒有訂單數據可載入。");
            tableModel.addRow(new Object[]{"", "", "無訂單數據", "", "", "", "", "", ""}); // 新增一列空值
            return;
        }

        for (Order order : allOrders) {
            Vector<Object> row = new Vector<>();
            row.add(order.getOrderId());
            row.add(order.getUsername());
            row.add(order.getMovieTitle());
            row.add(order.getShowtime());
            row.add(order.getHallName());
            String seatsString = String.join(", ", order.getSeats());
            row.add(seatsString);
            row.add(order.getBookingTime());
            row.add(order.getOrderStatus());
            row.add("修改狀態"); // 這將是顯示在表格中的按鈕文本
            tableModel.addRow(row);
        }
    }

    /**
     * 處理點擊「修改狀態」按鈕的邏輯
     * @param orderId 訂單ID
     * @param currentRow 點擊的表格行號
     */
    public void handleUpdateStatus(String orderId, int currentRow) {
        // 獲取當前訂單狀態
        String currentStatus = (String) tableModel.getValueAt(currentRow, 7); // 假設訂單狀態在第8列 (索引7)

        // 彈出選項對話框讓用戶選擇新狀態
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "請選擇訂單 ID: " + orderId + " 的新狀態:",
                "修改訂單狀態",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ORDER_STATUS_OPTIONS, // 狀態選項
                currentStatus // 預設選中當前狀態
        );

        if (newStatus != null && !newStatus.isEmpty() && !newStatus.equals(currentStatus)) {
            // 如果用戶選擇了新狀態且不同於原狀態，則更新
            boolean success = orderManager.updateOrderStatus(orderId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "訂單 ID: " + orderId + " 狀態更新成功為: " + newStatus, "更新成功", JOptionPane.INFORMATION_MESSAGE);
                // 更新表格中的顯示
                tableModel.setValueAt(newStatus, currentRow, 7); // 更新狀態列
            } else {
                JOptionPane.showMessageDialog(this, "訂單 ID: " + orderId + " 狀態更新失敗。", "更新失敗", JOptionPane.ERROR_MESSAGE);
            }
        } else if (newStatus != null && newStatus.equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "您選擇的狀態與當前狀態相同，無需更新。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // --- 自定義 JTable 按鈕渲染器 ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            setBackground(new Color(150, 150, 150)); // MediumSeaGreen
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // --- 自定義 JTable 按鈕編輯器 ---
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private String orderId; // 用於保存當前行的訂單ID
        private int currentRow; // 用於保存當前行的行號
        private TicketStatusPanel parentPanel; // 引用 TicketStatusPanel 實例

        public ButtonEditor(JCheckBox checkBox, TicketStatusPanel parentPanel) {
            super(checkBox);
            this.parentPanel = parentPanel;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            button.setBackground(new Color(60, 179, 113)); // MediumSeaGreen
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // 停止編輯
                    // 點擊按鈕時觸發的邏輯
                    if (isPushed) {
                        // 調用父面板的方法來處理狀態更新
                        parentPanel.handleUpdateStatus(orderId, currentRow);
                    }
                    isPushed = false;
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            // 獲取當前行的訂單ID，假設訂單ID在第1列 (索引0)
            orderId = (String) table.getModel().getValueAt(row, 0);
            currentRow = row; // 保存當前行號
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 這裡可以返回一個表示操作的對象，或者只是按鈕的文本
            }
            return label; // 返回按鈕的文本
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}