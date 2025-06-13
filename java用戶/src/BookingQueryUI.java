import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent; // 可能需要，但在此版本中直接用lambda表達式
import java.awt.event.ActionListener; // 可能需要，但在此版本中直接用lambda表達式
import java.util.EventObject;
import java.util.List;
import java.time.LocalDateTime; // 引入 LocalDateTime
import java.time.format.DateTimeFormatter; // 引入 DateTimeFormatter

// 確保 Order 和 OrderManager 類可以被訪問
// import your_package.Order; // 根據您的專案結構調整
// import your_package.OrderManager; // 根據您的專案結構調整
// import your_package.SessionManager; // 根據您的專案結構調整

public class BookingQueryUI extends JPanel {

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JTextField queryTextField;
    private JButton searchButton;
    private JLabel noteLabel;
    private JScrollPane scrollPane;

    private OrderManager orderManager; // 引入 OrderManager 實例

    public BookingQueryUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0x95B0D0));

        orderManager = new OrderManager(); // 初始化 OrderManager

        // 頂部查詢區域面板
        JPanel topQueryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topQueryPanel.setBackground(new Color(0x95B0D0));

        JLabel queryLabel = new JLabel("訂單查詢");
        queryLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        topQueryPanel.add(queryLabel);

        queryTextField = new JTextField(20); // 增加輸入框寬度以適應 GUID 訂單號
        queryTextField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        topQueryPanel.add(queryTextField);

        searchButton = new JButton("查詢");
        searchButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        searchButton.addActionListener(e -> performSearch());
        topQueryPanel.add(searchButton);

        noteLabel = new JLabel("注意：退票須於電影場次開始前30分鐘以前");
        noteLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        noteLabel.setForeground(Color.RED);
        topQueryPanel.add(noteLabel);

        add(topQueryPanel, BorderLayout.NORTH);

        // 表格區域 - **更新列名以匹配 Order 類屬性**
        String[] columnNames = {"*", "訂單ID", "電影名稱", "影廳", "場次時間", "座位", "張數", "訂單金額", "訂單狀態", "訂票時間", "退票"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String status = (String) getValueAt(row, findColumn("訂單狀態"));
                // 只有「退票」列可編輯，且只有在登入狀態下，並且訂單狀態是「已付款」
                return SessionManager.isLoggedIn() &&
                       column == getColumnCount() - 1 &&
                       "已付款".equals(status); // 確保只有已付款的訂單才能退票
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));

        // 設置列寬度，特別是訂單ID可能較長
        orderTable.getColumn("訂單ID").setPreferredWidth(150); // GUID 較長
        orderTable.getColumn("電影名稱").setPreferredWidth(120);
        orderTable.getColumn("場次時間").setPreferredWidth(120);
        orderTable.getColumn("訂票時間").setPreferredWidth(150); // 訂票時間也可能較長

        // 設定「退票」列為按鈕渲染器和編輯器
        orderTable.getColumn("退票").setCellRenderer(new ButtonRenderer());
        orderTable.getColumn("退票").setCellEditor(new ButtonEditor(new JCheckBox()));

        this.scrollPane = new JScrollPane(orderTable);
        add(this.scrollPane, BorderLayout.CENTER);

        // 初始狀態：禁用查詢相關元件，清空表格
        queryTextField.setEnabled(false);
        searchButton.setEnabled(false);
        noteLabel.setEnabled(false);
        tableModel.setRowCount(0);
        orderTable.setEnabled(false);
        scrollPane.getViewport().setBackground(Color.LIGHT_GRAY); // 視覺上表示禁用
    }

    /**
     * 當父容器的面板切換到此 BookingQueryUI 時，調用此方法來更新UI狀態。
     */
    public void updateUIBasedOnLoginStatus() {
        checkLoginStatus();
    }

    /**
     * 檢查登入狀態並更新UI。
     */
    private void checkLoginStatus() {
        // 請根據您的 SessionManager 實作，使用 SessionManager.getInstance().isLoggedIn() 或 SessionManager.isLoggedIn()
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "請先登入會員才能查詢訂單。", "未登入會員", JOptionPane.WARNING_MESSAGE);

            queryTextField.setEnabled(false);
            searchButton.setEnabled(false);
            noteLabel.setEnabled(false);

            tableModel.setRowCount(0); // 清空所有行
            orderTable.setEnabled(false); // 禁用表格交互
            scrollPane.getViewport().setBackground(Color.LIGHT_GRAY); // 讓表格區域看起來像禁用狀態
        } else {
            // 已登入，啟用元件
            queryTextField.setEnabled(true);
            searchButton.setEnabled(true);
            noteLabel.setEnabled(true);
            orderTable.setEnabled(true);
            scrollPane.getViewport().setBackground(orderTable.getBackground()); // 恢復表格背景

            // **主要變更：登入後直接載入所有訂單**
            // 請根據您的 SessionManager 實作，使用 SessionManager.getInstance().getLoggedInUsername() 或 SessionManager.getLoggedInUserName()
            loadOrdersForLoggedInUser(SessionManager.getLoggedInUserId());
        }
    }

    /**
     * 執行查詢操作，根據輸入框的內容篩選訂單。
     */
    private void performSearch() {
        // 請根據您的 SessionManager 實作，使用 SessionManager.getInstance().isLoggedIn() 或 SessionManager.isLoggedIn()
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "請先登入會員才能進行查詢操作。", "未登入會員", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String searchText = queryTextField.getText().trim();
        // 請根據您的 SessionManager 實作，使用 SessionManager.getInstance().getLoggedInUsername() 或 SessionManager.getLoggedInUserName()
        String loggedInUsername = SessionManager.getLoggedInUserId();

        // 清空現有數據
        tableModel.setRowCount(0);

        if (searchText.isEmpty()) {
            // 如果搜尋框為空，重新顯示所有訂單
            loadOrdersForLoggedInUser(loggedInUsername);
            // 這裡可以選擇不彈出對話框，因為這是預設行為
            // JOptionPane.showMessageDialog(this, "顯示所有訂單。", "查詢資訊", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 執行按訂單ID、電影名稱或場次關鍵字的篩選
            List<Order> allUserOrders = orderManager.getOrdersByUsername(loggedInUsername);
            List<Order> filteredOrders = new java.util.ArrayList<>();

            for (Order order : allUserOrders) {
                // 搜尋邏輯：判斷訂單ID、電影名稱或場次時間是否包含搜尋文本
                if (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(searchText.toLowerCase()) ||
                    order.getMovieTitle() != null && order.getMovieTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                    order.getShowtime() != null && order.getShowtime().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredOrders.add(order);
                }
            }
            updateTableWithOrders(filteredOrders);
            if (filteredOrders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未找到符合 '" + searchText + "' 的訂單。", "查詢結果", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "找到 " + filteredOrders.size() + " 筆訂單。", "查詢結果", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 載入指定會員的所有訂單，並更新表格。
     * @param username 要查詢的會員名。
     */
    private void loadOrdersForLoggedInUser(String username) {
        // 清空現有數據
        tableModel.setRowCount(0);

        if (username == null || username.isEmpty()) {
            System.err.println("Error: No logged in username to load orders.");
            return;
        }

        List<Order> userOrders = orderManager.getOrdersByUsername(username);
        updateTableWithOrders(userOrders);
    }

    /**
     * 使用給定的訂單列表更新表格。
     * @param orders 要顯示的訂單列表。
     */
    private void updateTableWithOrders(List<Order> orders) {
        tableModel.setRowCount(0); // 清空現有數據

        int displayOrderNumber = 1; // 用於顯示表格中的編號

        for (Order order : orders) {
            int numberOfTickets = order.getSeats() != null ? order.getSeats().size() : 0;
            // 假設每張票 300 元 (這裡可以從 Movie 或其他配置獲取真實價格)
            double orderAmount = numberOfTickets * 300.0;
            
            tableModel.addRow(new Object[]{
                displayOrderNumber++, // 顯示遞增的編號
                order.getOrderId(), // 顯示實際的訂單 ID
                order.getMovieTitle(),
                order.getHallName(), // 影廳名稱
                order.getShowtime(), // 場次時間
                String.join(", ", order.getSeats()), // 座位列表
                numberOfTickets,
                String.format("%.2f", orderAmount), // 格式化金額
                order.getOrderStatus(),
                order.getBookingTime(), // 訂票時間
                // 根據訂單狀態設定按鈕文本
                order.getOrderStatus() != null && order.getOrderStatus().equals("已退票") ? "已退票" : "退票"
            });
        }
        if (orders.isEmpty()) {
            // 如果沒有訂單，可以顯示一個提示
            // JOptionPane.showMessageDialog(this, "您目前沒有任何訂單紀錄。", "無訂單", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // JTable 欄位渲染器 (ButtonRenderer)
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            setBackground(new Color(0xDDDDDD));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            String status = (String) table.getValueAt(row, table.getColumn("訂單狀態").getModelIndex());
            if ("已退票".equals(status)) {
                setEnabled(false);
                setBackground(Color.LIGHT_GRAY);
                setText("已退票");
            } else {
                setEnabled(true);
                setBackground(new Color(0xDDDDDD));
                setText("退票");
            }
            return this;
        }
    }

    // JTable 欄位編輯器 (ButtonEditor)
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow; // 記錄當前操作的行

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            // 當按鈕被點擊時，通知編輯停止。這會觸發 getCellEditorValue 方法。
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row; // 保存當前行
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(0xDDDDDD));

            // 根據訂單狀態設定按鈕是否啟用和文本
            String status = (String) table.getValueAt(row, table.getColumn("訂單狀態").getModelIndex());
            if ("已退票".equals(status)) {
                button.setEnabled(false);
                button.setBackground(Color.LIGHT_GRAY);
                button.setText("已退票");
            } else {
                button.setEnabled(true);
                button.setBackground(new Color(0xDDDDDD));
                button.setText("退票");
            }
            isPushed = true; // 標記按鈕已被 "推" 出來準備編輯
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 在按鈕被點擊時執行退票操作
                String orderId = (String) orderTable.getValueAt(currentRow, tableModel.findColumn("訂單ID")); // 從表格中獲取訂單ID
                String currentStatus = (String) orderTable.getValueAt(currentRow, tableModel.findColumn("訂單狀態"));

                // 進一步檢查退票時間是否符合規定 (場次開始前30分鐘)
                String showtimeStr = (String) orderTable.getValueAt(currentRow, tableModel.findColumn("場次時間"));
                
                // 您需要解析 showtimeStr 來判斷時間。假設 showtimeStr 格式為 "YYYY/MM/DD HH:mm (Hall X)"
                // 或者更精確的 ISO 格式如 "YYYY-MM-DDTHH:mm:ss"
                // 這裡是一個簡化的時間判斷示例，您可能需要更健壯的解析邏輯
                LocalDateTime showtime = null;
                try {
                    // 假設 showtime 格式是 "YYYY/MM/DD HH:mm (Hall X)"，我們需要提取日期時間部分
                    // 您可能需要根據實際 showtime 格式調整這裡的解析
                    String timePart = showtimeStr.split(" \\(")[0]; // 提取 "YYYY/MM/DD HH:mm"
                    showtime = LocalDateTime.parse(timePart, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                } catch (Exception e) {
                    System.err.println("Error parsing showtime: " + showtimeStr + " - " + e.getMessage());
                    JOptionPane.showMessageDialog(orderTable, "無法解析電影場次時間，請聯繫客服。", "退票失敗", JOptionPane.ERROR_MESSAGE);
                    isPushed = false;
                    return label;
                }

                LocalDateTime refundDeadline = showtime.minusMinutes(30);
                LocalDateTime currentTime = LocalDateTime.now(); // 獲取當前時間

                if (currentTime.isAfter(refundDeadline)) {
                    JOptionPane.showMessageDialog(orderTable, "已超過退票期限（電影場次開始前30分鐘），無法退票。", "退票失敗", JOptionPane.WARNING_MESSAGE);
                    isPushed = false;
                    return label;
                }


                if ("已付款".equals(currentStatus)) { // 確保只有已付款的訂單才能退票
                    int confirm = JOptionPane.showConfirmDialog(orderTable,
                            "確定要退訂訂單號: " + orderId + " 嗎？",
                            "確認退票", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        // 調用 OrderManager 更新實際的訂單數據
                        boolean success = orderManager.updateOrderStatus(orderId, "已退票");

                        if (success) {
                            JOptionPane.showMessageDialog(orderTable, "訂單號: " + orderId + " 已成功退票。", "退票成功", JOptionPane.INFORMATION_MESSAGE);
                            // 更新表格中的狀態
                            tableModel.setValueAt("已退票", currentRow, tableModel.findColumn("訂單狀態"));
                            tableModel.setValueAt("已退票", currentRow, tableModel.findColumn("退票")); // 更新按鈕文本
                            orderTable.repaint(); // 重新繪製表格以更新按鈕狀態
                        } else {
                            JOptionPane.showMessageDialog(orderTable, "退票失敗，請稍後再試。", "退票失敗", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(orderTable, "該訂單狀態為 '" + currentStatus + "'，無法執行退票操作。", "退票失敗", JOptionPane.WARNING_MESSAGE);
                }
            }
            isPushed = false;
            return label; // 返回按鈕文本
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false; // 停止編輯時重置狀態
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return super.isCellEditable(anEvent);
        }
    }
}