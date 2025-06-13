import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 螢幕廳 (Hall A) 的座位選擇面板，根據提供的圖片佈局。
 */
public class HallASeatSelectionPanel extends JPanel {

    private static final int SEAT_SIZE = 30; // 座位按鈕大小
    private static final int GAP = 5; // 座位間距
    private static final int AISLE_WIDTH = 2; // 走道寬度，佔用 GridBagLayout 的列數

    // 用於存放選定座位的列表，例如 "A1", "C5"
    private List<String> selectedSeats = new ArrayList<>();
    // 用於儲存每個座位的實際狀態 (true: 已售/不可選, false: 可選)
    private Map<String, Boolean> seatOccupancy = new HashMap<>();

    // 定義座位排的標識
    private String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};

    private JPanel seatGridPanel; // 實際繪製座位的網格面板

    public HallASeatSelectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xD0E0F0)); // Light blue background

        // 螢幕標示
        JLabel screenLabel = new JLabel("螢幕SCREEN幕", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.DARK_GRAY);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(screenLabel, BorderLayout.NORTH);

        seatGridPanel = new JPanel(new GridBagLayout());
        seatGridPanel.setBackground(new Color(0xD0E0F0));
        // 添加邊界以提供一些間距
        seatGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeSeatOccupancy(); // 初始化所有座位為可選狀態
        drawSeats(); // 首次繪製座位

        // 將座位網格面板放入一個滾動面板，以防內容超出可視範圍
        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除滾動面板的邊框
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 初始化所有座位為未被佔用（可選）狀態。
     * 在實際應用中，這應該從資料庫載入特定場次的已售座位。
     */
    private void initializeSeatOccupancy() {
        for (String row : rows) {
            // Block 1: 1-4
            for (int i = 1; i <= 4; i++) {
                seatOccupancy.put(row + i, false);
            }
            // Block 2: 5-12
            for (int i = 5; i <= 12; i++) {
                seatOccupancy.put(row + i, false);
            }
            // Block 3: 13-16
            for (int i = 13; i <= 16; i++) {
                seatOccupancy.put(row + i, false);
            }
        }
    }

    /**
     * 根據 current seatOccupancy 繪製或重新繪製座位。
     */
    private void drawSeats() {
        seatGridPanel.removeAll(); // 移除所有舊元件
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP, GAP, GAP, GAP); // 設置元件間距

        for (int r = 0; r < rows.length; r++) {
            String rowId = rows[r];
            int currentGridX = 0; // 用於 GridBagLayout 的列索引



            // 繪製 Block 1 的座位 (1-4)
            for (int seatNum = 1; seatNum <= 4; seatNum++) {
                String seatId = rowId + seatNum;
                addSeatButton(seatId, gbc, currentGridX++, r);
            }

            // 添加 Block 1 和 Block 2 之間的走道 (空列)
            currentGridX += AISLE_WIDTH;

            // 中間行號標籤 (位於 5-12 區塊左側)
            gbc.gridx = currentGridX++;
            gbc.gridy = r;
            JLabel rowLabelMiddle = new JLabel(rowId, SwingConstants.CENTER);
            rowLabelMiddle.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
            seatGridPanel.add(rowLabelMiddle, gbc);

            // 繪製 Block 2 的座位 (5-12)
            for (int seatNum = 5; seatNum <= 12; seatNum++) {
                String seatId = rowId + seatNum;
                addSeatButton(seatId, gbc, currentGridX++, r);
            }

            // 添加 Block 2 和 Block 3 之間的走道 (空列)
            currentGridX += AISLE_WIDTH;

            // 右側行號標籤 (位於 13-16 區塊左側)
            gbc.gridx = currentGridX++;
            gbc.gridy = r;
            JLabel rowLabelRight = new JLabel(rowId, SwingConstants.CENTER);
            rowLabelRight.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
            seatGridPanel.add(rowLabelRight, gbc);

            // 繪製 Block 3 的座位 (13-16)
            for (int seatNum = 13; seatNum <= 16; seatNum++) {
                String seatId = rowId + seatNum;
                addSeatButton(seatId, gbc, currentGridX++, r);
            }
        }
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    /**
     * 輔助方法：創建並添加一個座位按鈕。
     */
    private void addSeatButton(String seatId, GridBagConstraints gbc, int gridX, int gridY) {
        JButton seatButton = new JButton(seatId.substring(seatId.length() - (seatId.length() > 2 ? 2 : 1))); // 只顯示座位號碼
        seatButton.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
        seatButton.setMargin(new Insets(0, 0, 0, 0)); // 移除內部邊距
        seatButton.setFont(new Font("Arial", Font.PLAIN, 10));
        seatButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 根據座位狀態設定顏色和啟用/禁用
        if (seatOccupancy.getOrDefault(seatId, false)) { // 已售
            seatButton.setBackground(Color.RED);
            seatButton.setForeground(Color.WHITE);
            seatButton.setEnabled(false);
        } else if (selectedSeats.contains(seatId)) { // 已選
            seatButton.setBackground(Color.BLUE); // 選中後的顏色
            seatButton.setForeground(Color.WHITE);
            seatButton.setEnabled(true);
        } else { // 可選
            seatButton.setBackground(Color.LIGHT_GRAY);
            seatButton.setForeground(Color.BLACK);
            seatButton.setEnabled(true);
        }

        // 添加點擊事件
        if (seatButton.isEnabled()) {
            seatButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedSeats.contains(seatId)) {
                        selectedSeats.remove(seatId);
                        ((JButton) e.getSource()).setBackground(Color.LIGHT_GRAY); // 取消選中
                    } else {
                        selectedSeats.add(seatId);
                        ((JButton) e.getSource()).setBackground(Color.BLUE); // 選中
                    }
                }
            });
        }

        gbc.gridx = gridX;
        gbc.gridy = gridY;
        seatGridPanel.add(seatButton, gbc);
    }

    /**
     * 模擬從資料庫載入座位狀態，並更新 UI。
     * 實際應用中，會從資料庫查詢指定場次的座位資訊。
     * @param showtimeId 傳入的場次 ID 或其他識別符，用於模擬不同場次的已售狀態。
     */
    public void simulateLoadSeats(String showtimeId) {
        System.out.println("Loading Hall A seats for showtime: " + showtimeId);
        selectedSeats.clear(); // 清空之前選擇的座位

        // 重置所有座位為未佔用
        initializeSeatOccupancy();

        // 根據 showtimeId 模擬某些座位為已售
        // 這是假數據，實際應從資料庫查詢
        if (showtimeId.contains("場次1")) {
            seatOccupancy.put("A1", true);
            seatOccupancy.put("B5", true);
            seatOccupancy.put("C12", true);
        } else if (showtimeId.contains("場次2")) {
            seatOccupancy.put("D4", true);
            seatOccupancy.put("E6", true);
            seatOccupancy.put("F8", true);
            seatOccupancy.put("F9", true);
        }
        drawSeats(); // 重新繪製座位以反映新的狀態
    }

    /**
     * 獲取當前選定的座位列表。
     * @return 已選座位的字串列表，例如 ["A1", "A2"]
     */
    public List<String> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }

    public void clearSelectedSeats() {
    selectedSeats.clear();
    drawSeats(); // 重新繪製以清除選中狀態
}
}