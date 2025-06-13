import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector; // 為了在表格中使用
import java.util.Comparator; // 為了排序

public class MovieManagementPanel extends JPanel {

    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public MovieManagementPanel() {
        setBackground(new Color(230, 240, 255)); // 淺藍色背景
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel headerLabel = new JLabel("電影管理");
        headerLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(headerLabel, BorderLayout.NORTH);

        // 表格設定
        // 根據 Movie.java 的所有屬性定義列名，移除 "圖片路徑"
        String[] columnNames = {"ID", "標題", "片長 (分)", "介紹", "評級", "演員", "導演", "影廳類型", "場次數量"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 使表格不可編輯
            }
        };
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        movieTable.setRowHeight(25);
        movieTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        movieTable.setAutoCreateRowSorter(true); // 允許排序

        JScrollPane scrollPane = new JScrollPane(movieTable);
        add(scrollPane, BorderLayout.CENTER);

        // 按鈕面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(230, 240, 255));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        addButton = createStyledButton("新增電影");
        editButton = createStyledButton("編輯電影");
        deleteButton = createStyledButton("刪除電影");
        refreshButton = createStyledButton("刷新列表");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 載入電影數據
        loadMovieData();

        // 監聽器
        addButton.addActionListener(e -> openMovieDetailDialog(null)); // 新增電影

        editButton.addActionListener(e -> {
            int selectedRow = movieTable.getSelectedRow();
            if (selectedRow != -1) {
                String movieId = (String) movieTable.getValueAt(selectedRow, 0);
                Movie selectedMovie = JsonDataManager.getMovieById(movieId); // 獲取完整 Movie 對象
                if (selectedMovie != null) {
                    openMovieDetailDialog(selectedMovie); // 編輯電影
                } else {
                    JOptionPane.showMessageDialog(this, "無法找到選定的電影數據。", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "請選擇要編輯的電影。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = movieTable.getSelectedRow();
            if (selectedRow != -1) {
                String movieId = (String) movieTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "確定要刪除電影 ID: " + movieId + " 嗎？", "確認刪除", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // 執行刪除操作：從 JsonDataManager 獲取所有電影，移除，然後保存
                    List<Movie> currentMovies = JsonDataManager.getAllMovies();
                    boolean removed = currentMovies.removeIf(m -> m.getId().equals(movieId));
                    if (removed) {
                        JsonDataManager.saveMoviesToJson(); // 保存更改
                        loadMovieData(); // 重新載入數據
                        JOptionPane.showMessageDialog(this, "電影刪除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "刪除失敗：找不到電影。", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "請選擇要刪除的電影。", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> loadMovieData()); // 刷新按鈕動作
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180)); // 鋼藍色
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // 載入電影數據到表格
    private void loadMovieData() {
        tableModel.setRowCount(0); // 清空現有數據
        List<Movie> movies = JsonDataManager.getAllMovies(); // 從 JsonDataManager 獲取電影列表

        if (movies.isEmpty()) {
            System.out.println("沒有電影數據可載入。");
            tableModel.addRow(new Object[]{"", "", "無電影數據", "", "", "", "", "", ""}); // 填充所有列，移除圖片路徑對應的空字串
            return;
        }

        for (Movie movie : movies) {
            Vector<Object> row = new Vector<>();
            row.add(movie.getId());
            row.add(movie.getTitle());
            row.add(movie.getRuntime());
            row.add(movie.getIntroduction());
            row.add(movie.getRating());
            // row.add(movie.getImagePath()); // 移除圖片路徑
            row.add(movie.getActors());
            row.add(movie.getDirector());
            row.add(movie.getHallTypes() != null ? String.join(", ", movie.getHallTypes()) : ""); // 顯示影廳類型列表
            row.add(movie.getShowtimes() != null ? movie.getShowtimes().size() : 0); // 顯示場次數量
            tableModel.addRow(row);
        }
    }

    // 打開電影詳細資訊對話框（用於新增或編輯）
    private void openMovieDetailDialog(Movie movieToEdit) {
        // 使用 JDialog 來創建一個更像樣的編輯界面
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                                     (movieToEdit == null ? "新增電影" : "編輯電影: " + movieToEdit.getTitle()),
                                     Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);
        dialog.setPreferredSize(new Dimension(500, 550)); // 調整對話框大小，因為移除了圖片路徑所以可以縮小一些

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(25);
        JTextField runtimeField = new JTextField(5);
        JTextArea introductionArea = new JTextArea(5, 25);
        introductionArea.setLineWrap(true);
        introductionArea.setWrapStyleWord(true);
        JScrollPane introductionScrollPane = new JScrollPane(introductionArea);
        JTextField ratingField = new JTextField(10);
        // JTextField imagePathField = new JTextField(25); // 移除圖片路徑的輸入框
        JTextField actorsField = new JTextField(25);
        JTextField directorField = new JTextField(25);

        // -- 添加標籤和輸入框 --
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("標題:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("片長 (分):"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(runtimeField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("介紹:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(introductionScrollPane, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("評級 (如: 輔12):"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(ratingField, gbc);

        // 移除圖片路徑的標籤和輸入框
        // row++;
        // gbc.gridx = 0; gbc.gridy = row;
        // formPanel.add(new JLabel("圖片路徑:"), gbc);
        // gbc.gridx = 1; gbc.gridy = row;
        // formPanel.add(imagePathField, gbc);

        row++; 
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("演員 (逗號分隔):"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(actorsField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("導演:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(directorField, gbc);

        // 如果是編輯模式，載入現有數據
        String originalId = null;
        if (movieToEdit != null) {
            originalId = movieToEdit.getId();
            titleField.setText(movieToEdit.getTitle());
            runtimeField.setText(String.valueOf(movieToEdit.getRuntime()));
            introductionArea.setText(movieToEdit.getIntroduction());
            ratingField.setText(movieToEdit.getRating());
            // imagePathField.setText(movieToEdit.getImagePath()); // 移除圖片路徑的設定
            actorsField.setText(movieToEdit.getActors());
            directorField.setText(movieToEdit.getDirector());
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = createStyledButton("保存");
        JButton cancelButton = createStyledButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 監聽器
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 獲取輸入的數據
                String title = titleField.getText().trim();
                int runtime;
                try {
                    runtime = Integer.parseInt(runtimeField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "片長必須是有效的數字！", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String introduction = introductionArea.getText().trim();
                String rating = ratingField.getText().trim();
                // String imagePath = imagePathField.getText().trim(); // 移除圖片路徑的獲取
                String actors = actorsField.getText().trim();
                String director = directorField.getText().trim();

                // 檢查必填欄位，移除 imagePath 的檢查
                if (title.isEmpty() || introduction.isEmpty() || rating.isEmpty() || actors.isEmpty() || director.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "所有欄位都必須填寫！", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Movie movie;
                List<Movie> currentMovies = JsonDataManager.getAllMovies(); // 獲取可修改的副本

                if (movieToEdit == null) {
                    // 新增模式：生成新的 ID
                    String newId = generateNextMovieId();
                    movie = new Movie(); // 使用無參建構子
                    movie.setId(newId);
                    movie.setTitle(title);
                    movie.setRuntime(runtime);
                    movie.setIntroduction(introduction);
                    movie.setRating(rating);
                    movie.setImagePath(""); // 將圖片路徑設定為空字串或 null，依據 Movie 類別的定義
                    movie.setActors(actors);
                    movie.setDirector(director);
                    movie.setHallTypes(new ArrayList<>()); // 預設為空列表
                    movie.setShowtimes(new ArrayList<>()); // 預設為空列表
                    currentMovies.add(movie); // 添加新電影
                } else {
                    // 編輯模式：更新現有電影
                    movie = movieToEdit; // 取得原始對象
                    movie.setTitle(title);
                    movie.setRuntime(runtime);
                    movie.setIntroduction(introduction);
                    movie.setRating(rating);
                    // movie.setImagePath(imagePath); // 移除圖片路徑的更新
                    movie.setActors(actors);
                    movie.setDirector(director);
                    // HallTypes 和 Showtimes 不在此處編輯，保持原樣
                }

                JsonDataManager.saveMoviesToJson(); // 保存所有電影數據
                loadMovieData(); // 刷新表格顯示
                JOptionPane.showMessageDialog(dialog, "電影資料已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose(); // 關閉對話框
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose()); // 關閉對話框

        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(MovieManagementPanel.this)); // 居中顯示
        dialog.setVisible(true);
    }

    // 輔助方法：生成下一個電影 ID
    // 假設 Movie ID 格式為 M001, M002 等
    private String generateNextMovieId() {
        List<Movie> movies = JsonDataManager.getAllMovies();
        int maxIdNum = 0;
        for (Movie movie : movies) {
            try {
                String idStr = movie.getId();
                if (idStr != null && idStr.startsWith("M") && idStr.length() > 1) {
                    int num = Integer.parseInt(idStr.substring(1));
                    if (num > maxIdNum) {
                        maxIdNum = num;
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid movie ID format: " + movie.getId());
            }
        }
        return String.format("M%03d", maxIdNum + 1);
    }
}