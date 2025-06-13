import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector; // For JTable data

public class MovieDetailPanel extends JPanel {

    private String movieTitle;
    private JLabel titleLabel;
    private JComboBox<String> dateComboBox;
    private JComboBox<String> timeComboBox;
    private JPanel seatPanel;
    private Map<String, Boolean> seatStatus; // true = available, false = occupied

    private JTable scheduleTable;
    private DefaultTableModel scheduleTableModel;
    private JButton addScheduleButton;
    private JButton editScheduleButton;
    private JButton deleteScheduleButton;

    private static Map<String, List<ScheduleItem>> allMovieSchedules = new HashMap<>();

    static {
        // 初始化一些模擬數據
        List<ScheduleItem> schedulesA = new ArrayList<>();
        schedulesA.add(new ScheduleItem("2025-06-15", "10:00", 120)); // 120 minutes duration
        schedulesA.add(new ScheduleItem("2025-06-15", "14:00", 120));
        schedulesA.add(new ScheduleItem("2025-06-16", "11:00", 120));
        allMovieSchedules.put("Movie A", schedulesA);

        List<ScheduleItem> schedulesB = new ArrayList<>();
        schedulesB.add(new ScheduleItem("2025-06-15", "13:00", 150));
        schedulesB.add(new ScheduleItem("2025-06-16", "16:00", 150));
        allMovieSchedules.put("Movie B", schedulesB);
    }

    public MovieDetailPanel(String movieTitle) {
        this.movieTitle = movieTitle;
        setBackground(new Color(230, 240, 255));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- 頂部標題 ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        titleLabel = new JLabel("電影詳情: " + movieTitle);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 中間內容區塊 ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(220); // <-- 將分割線位置調整為 220 像素，上半部分更小
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setOpaque(false);

        // --- 上半部分：現有場次列表 ---
        JPanel topContentPanel = new JPanel(new BorderLayout(10, 10));
        topContentPanel.setOpaque(false);
        topContentPanel.setBorder(BorderFactory.createTitledBorder("電影場次列表"));

        String[] scheduleColumnNames = {"日期", "時間", "片長(分)", "結束時間"};
        scheduleTableModel = new DefaultTableModel(scheduleColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        scheduleTable.setRowHeight(25);
        scheduleTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));

        JScrollPane scheduleScrollPane = new JScrollPane(scheduleTable);
        topContentPanel.add(scheduleScrollPane, BorderLayout.CENTER);

        // 場次操作按鈕
        JPanel scheduleButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        scheduleButtonPanel.setOpaque(false);
        addScheduleButton = new JButton("新增場次");
        editScheduleButton = new JButton("修改場次");
        deleteScheduleButton = new JButton("刪除場次");

        customizeScheduleButton(addScheduleButton);
        customizeScheduleButton(editScheduleButton);
        customizeScheduleButton(deleteScheduleButton);

        scheduleButtonPanel.add(addScheduleButton);
        scheduleButtonPanel.add(editScheduleButton);
        scheduleButtonPanel.add(deleteScheduleButton);
        topContentPanel.add(scheduleButtonPanel, BorderLayout.SOUTH);

        mainSplitPane.setTopComponent(topContentPanel);

        // --- 下半部分：訂票功能（日期時間選擇和座位圖）---
        JPanel bottomContentPanel = new JPanel(new BorderLayout(10, 10));
        bottomContentPanel.setOpaque(false);
        bottomContentPanel.setBorder(BorderFactory.createTitledBorder("選擇場次並訂票"));

        // 日期時間選擇器 (用於訂票)
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        dateTimePanel.setOpaque(false);
        dateTimePanel.add(new JLabel("選擇日期: "));
        dateComboBox = new JComboBox<>(getSampleDates());
        dateTimePanel.add(dateComboBox);
        dateTimePanel.add(new JLabel("選擇時間: "));
        timeComboBox = new JComboBox<>(getSampleTimes());
        dateTimePanel.add(timeComboBox);

        bottomContentPanel.add(dateTimePanel, BorderLayout.NORTH);

        // 座位圖面板
        seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(5, 10, 5, 5)); // 5 行 10 列的座位圖，間距 5
        seatPanel.setOpaque(false);
        seatPanel.setBorder(BorderFactory.createTitledBorder("座位選擇 (綠色為可選)"));

        initializeSeats(); // 初始化座位狀態和按鈕

        bottomContentPanel.add(seatPanel, BorderLayout.CENTER); // 座位面板放在 CENTER，會自動擴展

        // 底部確認按鈕 (訂票用)
        JPanel orderFooterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        orderFooterPanel.setOpaque(false);
        JButton confirmButton = new JButton("確認選座");
        confirmButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        confirmButton.addActionListener(e -> {
            String selectedDate = (String) dateComboBox.getSelectedItem();
            String selectedTime = (String) timeComboBox.getSelectedItem();
            System.out.println("您為電影 [" + movieTitle + "] 選擇了日期: " + selectedDate + ", 時間: " + selectedTime);
            JOptionPane.showMessageDialog(this, "電影: " + movieTitle + "\n日期: " + selectedDate + "\n時間: " + selectedTime + "\n選中座位: (實際開發中應顯示具體座位號)", "選座確認", JOptionPane.INFORMATION_MESSAGE);
        });
        orderFooterPanel.add(confirmButton);
        bottomContentPanel.add(orderFooterPanel, BorderLayout.SOUTH);

        mainSplitPane.setBottomComponent(bottomContentPanel);

        add(mainSplitPane, BorderLayout.CENTER);

        // --- 事件監聽器 ---
        addScheduleButton.addActionListener(e -> showScheduleDialog(null));
        editScheduleButton.addActionListener(e -> {
            int selectedRow = scheduleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "請選擇要修改的場次。", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String date = (String) scheduleTableModel.getValueAt(selectedRow, 0);
            String time = (String) scheduleTableModel.getValueAt(selectedRow, 1);
            String durationStr = (String) scheduleTableModel.getValueAt(selectedRow, 2);
            int duration = Integer.parseInt(durationStr.replaceAll("[^\\d]", ""));

            ScheduleItem selectedSchedule = new ScheduleItem(date, time, duration);
            showScheduleDialog(selectedSchedule);
        });
        deleteScheduleButton.addActionListener(e -> {
            int selectedRow = scheduleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "請選擇要刪除的場次。", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "確定要刪除選定的場次嗎？", "確認刪除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String date = (String) scheduleTableModel.getValueAt(selectedRow, 0);
                String time = (String) scheduleTableModel.getValueAt(selectedRow, 1);
                String durationStr = (String) scheduleTableModel.getValueAt(selectedRow, 2);
                int duration = Integer.parseInt(durationStr.replaceAll("[^\\d]", ""));

                ScheduleItem itemToDelete = new ScheduleItem(date, time, duration);
                deleteSchedule(itemToDelete);
            }
        });

        loadMovieSchedules();
    }

    private static class ScheduleItem {
        String date;
        String time;
        int durationMinutes;

        public ScheduleItem(String date, String time, int durationMinutes) {
            this.date = date;
            this.time = time;
            this.durationMinutes = durationMinutes;
        }

        public LocalTime getEndTime() {
            LocalTime startTime = LocalTime.parse(time);
            return startTime.plusMinutes(durationMinutes);
        }

        public boolean conflictsWith(ScheduleItem other) {
            if (!this.date.equals(other.date)) {
                return false;
            }
            LocalTime thisStart = LocalTime.parse(this.time);
            LocalTime thisEnd = this.getEndTime();
            LocalTime otherStart = LocalTime.parse(other.time);
            LocalTime otherEnd = other.getEndTime();
            return !(thisEnd.isBefore(otherStart) || thisStart.isAfter(otherEnd) || thisEnd.equals(otherStart) || thisStart.equals(otherEnd));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScheduleItem that = (ScheduleItem) o;
            return durationMinutes == that.durationMinutes && date.equals(that.date) && time.equals(that.time);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(date, time, durationMinutes);
        }
    }

    private void loadMovieSchedules() {
        scheduleTableModel.setRowCount(0);
        List<ScheduleItem> schedules = allMovieSchedules.getOrDefault(movieTitle, new ArrayList<>());
        for (ScheduleItem item : schedules) {
            addScheduleRow(item);
        }
    }

    private void addScheduleRow(ScheduleItem item) {
        Vector<Object> row = new Vector<>();
        row.add(item.date);
        row.add(item.time);
        row.add(item.durationMinutes + " 分鐘");
        row.add(item.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        scheduleTableModel.addRow(row);
    }

    private void showScheduleDialog(ScheduleItem itemToEdit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                itemToEdit == null ? "新增電影場次" : "修改電影場次", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField dateField = new JTextField(itemToEdit == null ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : itemToEdit.date, 15);
        JTextField timeField = new JTextField(itemToEdit == null ? "HH:MM" : itemToEdit.time, 10);
        JTextField durationField = new JTextField(itemToEdit == null ? "120" : String.valueOf(itemToEdit.durationMinutes), 5);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("日期 (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; dialog.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("時間 (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; dialog.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("片長 (分鐘):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; dialog.add(durationField, gbc);

        JButton saveButton = new JButton(itemToEdit == null ? "新增" : "保存");
        saveButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        saveButton.addActionListener(e -> {
            try {
                String dateStr = dateField.getText().trim();
                String timeStr = timeField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());

                LocalDate parsedDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                LocalTime parsedTime = LocalTime.parse(timeStr);

                ScheduleItem newOrModifiedItem = new ScheduleItem(dateStr, timeStr, duration);

                if (itemToEdit == null) {
                    if (isConflict(newOrModifiedItem)) {
                        JOptionPane.showMessageDialog(dialog, "新場次與現有場次時間衝突，請檢查！", "衝突警告", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    allMovieSchedules.computeIfAbsent(movieTitle, k -> new ArrayList<>()).add(newOrModifiedItem);
                    JOptionPane.showMessageDialog(dialog, "場次新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    List<ScheduleItem> currentSchedules = allMovieSchedules.getOrDefault(movieTitle, new ArrayList<>());
                    currentSchedules.remove(itemToEdit);

                    if (isConflict(newOrModifiedItem)) {
                        JOptionPane.showMessageDialog(dialog, "修改後的場次與其他場次時間衝突，請檢查！", "衝突警告", JOptionPane.WARNING_MESSAGE);
                        currentSchedules.add(itemToEdit);
                        return;
                    }
                    currentSchedules.add(newOrModifiedItem);
                    JOptionPane.showMessageDialog(dialog, "場次修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
                loadMovieSchedules();
                dialog.dispose();

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "日期或時間格式錯誤，請使用YYYY-MM-DD 和 HH:MM 格式。", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "片長必須是數字。", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "發生錯誤: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; dialog.add(saveButton, gbc);

        dialog.setVisible(true);
    }

    private boolean isConflict(ScheduleItem newItem) {
        List<ScheduleItem> existingSchedules = allMovieSchedules.getOrDefault(movieTitle, new ArrayList<>());
        for (ScheduleItem existingItem : existingSchedules) {
            if (newItem.conflictsWith(existingItem)) {
                return true;
            }
        }
        return false;
    }

    private void deleteSchedule(ScheduleItem itemToDelete) {
        List<ScheduleItem> schedules = allMovieSchedules.getOrDefault(movieTitle, new ArrayList<>());
        if (schedules.remove(itemToDelete)) {
            JOptionPane.showMessageDialog(this, "場次刪除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            loadMovieSchedules();
        } else {
            JOptionPane.showMessageDialog(this, "未找到要刪除的場次。", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void customizeScheduleButton(JButton button) {
        button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(100, 30));
    }


    private String[] getSampleDates() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 E");
        String[] dates = new String[5];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            dates[i] = today.plusDays(i).format(dateFormatter);
        }
        return dates;
    }

    private String[] getSampleTimes() {
        return new String[]{"10:00", "13:30", "16:00", "19:30", "22:00"};
    }

    private void initializeSeats() {
        seatStatus = new HashMap<>();
        char row = 'A';
        for (int i = 0; i < 5; i++) { // 5 排
            for (int j = 1; j <= 10; j++) { // 每排 10 個座位
                String seatId = row + String.format("%02d", j);
                JButton seatButton = new JButton(seatId);
                seatButton.setPreferredSize(new Dimension(60, 40));
                seatButton.setFont(new Font("Arial", Font.PLAIN, 12));
                seatButton.setMargin(new Insets(0,0,0,0));

                boolean isAvailable = Math.random() > 0.3;
                seatStatus.put(seatId, isAvailable);

                updateSeatButtonColor(seatButton, isAvailable);

                seatButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean currentStatus = seatStatus.get(seatId);
                        if (currentStatus) {
                            seatStatus.put(seatId, false);
                            updateSeatButtonColor(seatButton, false);
                            System.out.println("您選中了座位: " + seatId);
                        } else {
                            System.out.println("座位 " + seatId + " 不可用或已被選中。");
                        }
                    }
                });
                seatPanel.add(seatButton);
            }
            row++;
        }
    }

    private void updateSeatButtonColor(JButton button, boolean isAvailable) {
        if (isAvailable) {
            button.setBackground(new Color(144, 238, 144)); // 綠色 (可用)
            button.setEnabled(true);
        } else {
            button.setBackground(new Color(255, 99, 71)); // 紅色 (已佔用/已選)
            button.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("電影詳情面板預覽");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.add(new MovieDetailPanel("Movie B")); // 傳入一個測試電影名稱
        frame.setVisible(true);
    }
}