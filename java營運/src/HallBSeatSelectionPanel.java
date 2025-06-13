import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 銀幕廳 (Hall B) 的座位選擇面板，根據提供的最新圖片佈局。
 */
public class HallBSeatSelectionPanel extends JPanel {

    private static final int SEAT_SIZE = 30; // 座位按鈕大小
    private static final int GAP = 5; // 座位間距
    private static final int AISLE_COL_WIDTH = 2; // 走道在GridBagLayout中佔用的額外列數
    private static final int VERTICAL_AISLE_ROW_HEIGHT = 1; // K和L之間走道佔用的額外行數

    private List<String> selectedSeats = new ArrayList<>();
    private Map<String, Boolean> seatOccupancy = new HashMap<>(); // true: 已售, false: 可選

    // 定義所有座位排的標識
    private String[] allRows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"};

    // 儲存每個排的座位佈局，Key: 排號 (e.g., "A"), Value: 該排的所有座位塊 (int[] 包含起始和結束座位號)
    private Map<String, List<int[]>> seatLayoutMap = new HashMap<>();

    // 用於映射邏輯座位號到 GridBagLayout 的實際列索引
    private Map<Integer, Integer> logicalSeatNumToGridX = new HashMap<>();

    private JPanel seatGridPanel; // 實際繪製座位的網格面板

    public HallBSeatSelectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0xD0E0F0));

        // 螢幕標示
        JLabel screenLabel = new JLabel("銀幕SCREEN幕", SwingConstants.CENTER);
        screenLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.DARK_GRAY);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(screenLabel, BorderLayout.NORTH);

        seatGridPanel = new JPanel(new GridBagLayout());
        seatGridPanel.setBackground(new Color(0xD0E0F0));
        seatGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        defineSeatLayouts(); // 定義每個排的具體座位佈局
        initializeLogicalSeatMapping(); // 初始化邏輯座位號到 gridX 的映射
        initializeSeatOccupancy(); // 初始化所有座位為可選狀態
        drawSeats(); // 首次繪製座位

        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 定義每個排的具體座位佈局，這是根據最新圖片 手動分析的。
     */
    private void defineSeatLayouts() {
        seatLayoutMap.clear();

        // Row A: [8, 11], [14, 25], [28, 31]
        List<int[]> blocksA = new ArrayList<>();
        blocksA.add(new int[]{8, 11});
        blocksA.add(new int[]{14, 25});
        blocksA.add(new int[]{28, 31});
        seatLayoutMap.put("A", blocksA);

        // Row B: [5, 11], [14, 25], [28, 34]
        List<int[]> blocksB = new ArrayList<>();
        blocksB.add(new int[]{5, 11});
        blocksB.add(new int[]{14, 25});
        blocksB.add(new int[]{28, 34});
        seatLayoutMap.put("B", blocksB);

        // Row C-K: [1, 11], [14, 25], [28, 38]
        for (String row : new String[]{"C", "D", "E", "F", "G", "H", "I", "J", "K"}) {
            List<int[]> blocks = new ArrayList<>();
            blocks.add(new int[]{1, 11});
            blocks.add(new int[]{14, 25});
            blocks.add(new int[]{28, 38});
            seatLayoutMap.put(row, blocks);
        }

        // Row L: [1, 39] - L排現在是完整的1到39
        List<int[]> blocksL = new ArrayList<>();
        blocksL.add(new int[]{1, 39});
        seatLayoutMap.put("L", blocksL);

        // Row M: [1, 8], [31, 38]
        List<int[]> blocksM = new ArrayList<>();
        blocksM.add(new int[]{1, 8});
        blocksM.add(new int[]{31, 38});
        seatLayoutMap.put("M", blocksM);
    }

    /**
     * 初始化邏輯座位號到 GridBagLayout 實際列索引的映射。
     * 這是實現走道對齊的關鍵，基於所有排的最大範圍進行基準計算。
     */
    private void initializeLogicalSeatMapping() {
        logicalSeatNumToGridX.clear();
        int currentGridX = 1; // GridX 0 是左側排標籤

        // 1. 左側區塊座位 (1-11)
        for (int i = 1; i <= 11; i++) {
            logicalSeatNumToGridX.put(i, currentGridX++);
        }

        // 2. 第一個走道 (在 11 之後，為 14 號座位留出對齊空間)
        currentGridX += AISLE_COL_WIDTH; // 跳過走道寬度

        // 3. 中間區塊座位 (從 12 開始，一直到 30，涵蓋 L 排的連續部分和 A-K 的 14-25)
        // 為了確保 14 號座位在 A-K 排中對齊，我們需要計算 14 號座位在 L 排的邏輯序列中相對 12 號的偏移。
        // `currentGridX` 目前指向 14 號座位的第一個可視列。
        for (int i = 12; i <= 30; i++) {
            // 對於 L 排，12號座位就是這個區塊的第一個。
            // 對於 A-K 排，14號座位是這個區塊的某個中間。
            // 我們需要讓 14 號座位對齊 `currentGridX`。
            // 所以，座位 `i` 的 `gridx` = `currentGridX` + (`i` - `14`)。
            // 對於 12 號，它會是 `currentGridX - 2`。
            // 對於 13 號，它會是 `currentGridX - 1`。
            // 對於 14 號，它會是 `currentGridX`。
            // 這樣可以保證 L 排的連續性，同時也保證了 14 號座位能夠垂直對齊 A-K 排的 14 號座位。
            logicalSeatNumToGridX.put(i, currentGridX + (i - 14));
        }

        // 更新 `currentGridX` 到中間區塊的尾部，然後跳過走道
        currentGridX = logicalSeatNumToGridX.get(30) + 1; // 30號座位之後的第一列

        // 4. 第二個走道 (在 25/30 之後，為 28/31/32 號座位留出對齊空間)
        currentGridX += AISLE_COL_WIDTH; // 跳過走道寬度

        // 5. 右側區塊座位 (28-39，涵蓋所有排的最右側部分)
        for (int i = 28; i <= 39; i++) {
            logicalSeatNumToGridX.put(i, currentGridX + (i - 28)); // 以 28 為基準對齊
        }
    }


    private void initializeSeatOccupancy() {
        for (String rowId : allRows) {
            List<int[]> blocks = seatLayoutMap.get(rowId);
            if (blocks != null) {
                for (int[] block : blocks) {
                    for (int i = block[0]; i <= block[1]; i++) {
                        seatOccupancy.put(rowId + i, false); // Initialize as not occupied
                    }
                }
            }
        }
    }

    private void drawSeats() {
        seatGridPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GAP, GAP, GAP, GAP);

        int currentGridY = 0; // Current row in GridBagLayout

        // 查找最右側的 GridX 以固定右側標籤
        int maxPossibleGridX = 0;
        for (int gridX : logicalSeatNumToGridX.values()) {
            if (gridX > maxPossibleGridX) {
                maxPossibleGridX = gridX;
            }
        }
        int rightLabelGridX = maxPossibleGridX + 1; // 在所有座位後面再加一列給右側標籤

        for (int r = 0; r < allRows.length; r++) {
            String rowId = allRows[r];
            List<int[]> blocks = seatLayoutMap.get(rowId);
            if (blocks == null) continue;

            // 在 K (allRows[10]) 和 L (allRows[11]) 之間添加走道
            if (rowId.equals("L")) { // 檢查當前行是否是 L
                currentGridY += VERTICAL_AISLE_ROW_HEIGHT; // 增加額外的行間距
            }

            // 左側行號標籤
            gbc.gridx = 0; // 固定在第 0 列
            gbc.gridy = currentGridY;
            JLabel rowLabelLeft = new JLabel(rowId, SwingConstants.CENTER);
            rowLabelLeft.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
            seatGridPanel.add(rowLabelLeft, gbc);

            // 繪製座位區塊
            for (int[] block : blocks) {
                for (int seatNum = block[0]; seatNum <= block[1]; seatNum++) {
                    String seatId = rowId + seatNum;
                    Integer gridX = logicalSeatNumToGridX.get(seatNum);
                    if (gridX != null) {
                        addSeatButton(seatId, gbc, gridX, currentGridY);
                    } else {
                        // 如果某個座位號不在我們的標準映射中，這是一個錯誤。
                        System.err.println("Error: Seat " + seatId + " has no defined gridX mapping. Check defineSeatLayouts and initializeLogicalSeatMapping.");
                    }
                }
            }

            // 右側行號標籤
            gbc.gridx = rightLabelGridX; // 固定右側標籤位置
            gbc.gridy = currentGridY;
            JLabel rowLabelRight = new JLabel(rowId, SwingConstants.CENTER);
            rowLabelRight.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
            seatGridPanel.add(rowLabelRight, gbc);

            currentGridY++; // Move to the next row
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
        System.out.println("Loading Hall B seats for showtime: " + showtimeId);
        selectedSeats.clear(); // 清空之前選擇的座位

        // 重置所有座位為未佔用
        initializeSeatOccupancy();

        // 根據 showtimeId 模擬某些座位為已售
        if (showtimeId.contains("晚場")) {
            seatOccupancy.put("A8", true);
            seatOccupancy.put("B14", true);
            seatOccupancy.put("C20", true);
            seatOccupancy.put("L5", true);
            seatOccupancy.put("M35", true);
        } else if (showtimeId.contains("午場")) {
            seatOccupancy.put("G10", true);
            seatOccupancy.put("H20", true);
            seatOccupancy.put("I30", true);
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