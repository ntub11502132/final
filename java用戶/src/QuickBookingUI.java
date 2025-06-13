import javax.swing.*;
import javax.swing.border.Border;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuickBookingUI extends JPanel {

    private MainForm parentFrame; // 引用主視窗，用於切換面板
    private JPanel moviesPanel; // 用於存放電影海報的面板
    private CardLayout moviesCardLayout; // 管理電影海報顯示的 CardLayout

    private List<Movie> allMovies = new ArrayList<>();
    private List<Movie> nowPlayingMovies = new ArrayList<>();
    private List<Movie> upcomingMovies = new ArrayList<>();

    public QuickBookingUI(MainForm parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0x95B0D0));

        loadMoviesFromJson("../shared_data/movies.json");

        // 這裡將所有電影都放入現正熱映 (您可以根據實際需求修改邏輯)
        nowPlayingMovies.addAll(allMovies);

        // 上方按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(new Color(0x95B0D0));

        JToggleButton nowPlayingButton = new JToggleButton("現正熱映");
        JToggleButton upcomingButton = new JToggleButton("即將上映");

        // 將按鈕加入按鈕組，實現互斥選擇
        ButtonGroup movieStatusGroup = new ButtonGroup();
        movieStatusGroup.add(nowPlayingButton);
        movieStatusGroup.add(upcomingButton);

        // 設定按鈕樣式
        Font buttonFont = new Font("Microsoft JhengHei", Font.PLAIN, 16);
        nowPlayingButton.setFont(buttonFont);
        upcomingButton.setFont(buttonFont);
        nowPlayingButton.setPreferredSize(new Dimension(120, 35));
        upcomingButton.setPreferredSize(new Dimension(120, 35));

        // 設定按鈕的選中和未選中狀態的顏色
        Color selectedColor = new Color(0xC0C0C0); // 淺灰色

        nowPlayingButton.addActionListener(e -> {
            if (nowPlayingButton.isSelected()) {
                nowPlayingButton.setBackground(selectedColor);
                upcomingButton.setBackground(UIManager.getColor("Button.background"));
                showMovies("NowPlaying");
            }
        });
        upcomingButton.addActionListener(e -> {
            if (upcomingButton.isSelected()) {
                upcomingButton.setBackground(selectedColor);
                nowPlayingButton.setBackground(UIManager.getColor("Button.background"));
                showMovies("Upcoming");
            }
        });

        buttonPanel.add(nowPlayingButton);
        buttonPanel.add(upcomingButton);

        add(buttonPanel, BorderLayout.NORTH);

        // 電影海報顯示區域
        moviesCardLayout = new CardLayout();
        moviesPanel = new JPanel(moviesCardLayout);
        moviesPanel.setBackground(new Color(0x95B0D0));

        // 創建「現正熱映」面板
        JPanel nowPlayingPanel = createMovieGridPanel(nowPlayingMovies);
        moviesPanel.add(nowPlayingPanel, "NowPlaying");

        // 創建「即將上映」面板
        JPanel upcomingPanel = createMovieGridPanel(upcomingMovies);
        moviesPanel.add(upcomingPanel, "Upcoming");

        add(moviesPanel, BorderLayout.CENTER);

        // 預設顯示「現正熱映」
        nowPlayingButton.setSelected(true);
        nowPlayingButton.setBackground(selectedColor);
        showMovies("NowPlaying");
    }

    // 從 JSON 檔案載入電影數據
    private void loadMoviesFromJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 創建 File 對象，它會根據當前工作目錄解析相對路徑
            File jsonFile = new File(filePath);

            if (!jsonFile.exists()) {
                System.err.println("JSON file not found at: " + jsonFile.getAbsolutePath());
                System.out.println("當前工作目錄: " + System.getProperty("user.dir")); // 打印當前工作目錄幫助調試
                JOptionPane.showMessageDialog(this, "電影數據檔案未找到: " + jsonFile.getAbsolutePath(), "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 使用 FileInputStream 從 File 對象讀取數據
            try (InputStream is = new FileInputStream(jsonFile)) {
                allMovies = mapper.readValue(is, new TypeReference<List<Movie>>() {});
                System.out.println("成功載入 " + allMovies.size() + " 部電影。");
            }

        } catch (IOException e) {
            System.err.println("Error reading JSON file from path: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "讀取電影數據檔案時發生錯誤: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 輔助方法：創建電影海報網格面板
    private JPanel createMovieGridPanel(List<Movie> movieDetails) {
        int rows = (movieDetails.size() + 4) / 5;
        if (rows == 0) rows = 1;

        JPanel gridPanel = new JPanel(new GridLayout(rows, 5, 20, 20));
        gridPanel.setBackground(new Color(0x95B0D0));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        for (Movie movie : movieDetails) {
            JLabel posterLabel = new JLabel();
            posterLabel.setPreferredSize(new Dimension(120, 180));
            posterLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            try {
                URL imageUrl = getClass().getClassLoader().getResource(movie.getImagePath());
                if (imageUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imageUrl);
                    Image image = originalIcon.getImage();
                    Image scaledImage = image.getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    System.err.println("Image not found: " + movie.getImagePath());
                    posterLabel.setText(movie.getTitle());
                    posterLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                    posterLabel.setVerticalTextPosition(SwingConstants.CENTER);
                }
            } catch (Exception e) {
                System.err.println("Error loading image for movie " + movie.getTitle() + " at path: " + movie.getImagePath() + " - " + e.getMessage());
                posterLabel.setText(movie.getTitle());
                posterLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                posterLabel.setVerticalTextPosition(SwingConstants.CENTER);
            }

            posterLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // **修改這裡：點擊海報後跳轉到 BookingUI 以選擇座位**
            posterLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 獲取 MainForm 中 BookingUI 的實例，並設定其電影
                    parentFrame.getBookingUI().setupBooking(movie);
                    // 切換到 BookingUI 面板
                    parentFrame.showPanel("Booking");
                }
            });
            gridPanel.add(posterLabel);
        }

        int emptySlots = (rows * 5) - movieDetails.size();
        for (int i = 0; i < emptySlots; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(0x95B0D0));
            gridPanel.add(emptyPanel);
        }
        
        return gridPanel;
    }

    // 輔助方法：切換電影海報顯示 (現正熱映或即將上映)
    private void showMovies(String type) {
        moviesCardLayout.show(moviesPanel, type);
    }
}