import javax.swing.*;
import javax.swing.border.Border;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;        // 引入 File 類別
import java.io.FileInputStream; // 引入 FileInputStream 類別
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieInfoUI extends JPanel {

    private MainForm parentFrame; // 引用主視窗，用於切換面板
    private JPanel moviesPanel; // 用於存放電影海報的面板
    private CardLayout moviesCardLayout; // 管理電影海報顯示的 CardLayout

    private List<Movie> allMovies = new ArrayList<>();
    private List<Movie> nowPlayingMovies = new ArrayList<>();
    private List<Movie> upcomingMovies = new ArrayList<>();

    public MovieInfoUI(MainForm parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10)); // 整個面板的佈局
        setBackground(new Color(0x95B0D0)); // 背景色

        // 這裡調用 loadMoviesFromJson，傳入相對路徑
        loadMoviesFromJson("../shared_data/movies.json");

        // 將所有電影都放入現正熱映（或根據你的邏輯區分）
        nowPlayingMovies.addAll(allMovies);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // 左對齊，間距10
        buttonPanel.setBackground(new Color(0x95B0D0));

        JToggleButton nowPlayingButton = new JToggleButton("現正熱映");
        JToggleButton upcomingButton = new JToggleButton("即將上映");

        ButtonGroup movieStatusGroup = new ButtonGroup();
        movieStatusGroup.add(nowPlayingButton);
        movieStatusGroup.add(upcomingButton);

        Font buttonFont = new Font("Microsoft JhengHei", Font.PLAIN, 16);
        nowPlayingButton.setFont(buttonFont);
        upcomingButton.setFont(buttonFont);
        nowPlayingButton.setPreferredSize(new Dimension(120, 35));
        upcomingButton.setPreferredSize(new Dimension(120, 35));

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

        moviesCardLayout = new CardLayout();
        moviesPanel = new JPanel(moviesCardLayout);
        moviesPanel.setBackground(new Color(0x95B0D0)); // 背景色

        // 使用 JScrollPane 包裹 GridLayout 面板
        JScrollPane nowPlayingScrollPane = new JScrollPane(createMovieGridPanel(nowPlayingMovies));
        nowPlayingScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        nowPlayingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        nowPlayingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        moviesPanel.add(nowPlayingScrollPane, "NowPlaying");

        JScrollPane upcomingScrollPane = new JScrollPane(createMovieGridPanel(upcomingMovies));
        upcomingScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        upcomingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        upcomingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        moviesPanel.add(upcomingScrollPane, "Upcoming");

        add(moviesPanel, BorderLayout.CENTER);

        nowPlayingButton.setSelected(true);
        nowPlayingButton.setBackground(selectedColor);
        showMovies("NowPlaying");
    }

    // 修改此方法，使其使用 File 和 FileInputStream 讀取檔案系統中的檔案
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

    private JPanel createMovieGridPanel(List<Movie> movieDetails) {
        int cols = 5; // 每行固定顯示 5 個
        // 計算行數，至少1行。即使沒有電影，GridLayout 也需要至少1行1列
        int rows = movieDetails.isEmpty() ? 1 : (movieDetails.size() + cols - 1) / cols;

        // 使用 GridLayout，會強制組件填滿其單元格
        JPanel gridPanel = new JPanel(new GridLayout(rows, cols, 20, 20)); // 行, 列, 水平間距, 垂直間距
        gridPanel.setBackground(new Color(0x95B0D0));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); // 內邊距

        if (movieDetails.isEmpty()) {
            JLabel noMoviesLabel = new JLabel("目前沒有電影可顯示。", SwingConstants.CENTER);
            noMoviesLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            noMoviesLabel.setForeground(Color.DARK_GRAY);
            gridPanel.add(noMoviesLabel);
            return gridPanel;
        }

        for (Movie movie : movieDetails) {
            JPanel moviePosterContainer = new JPanel();
            moviePosterContainer.setLayout(new BorderLayout());
            moviePosterContainer.setBackground(new Color(0x95B0D0));
            moviePosterContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY.darker(), 1));

            // 使用我們的自定義 ScalableImageIconLabel
            ScalableImageIconLabel posterLabel = new ScalableImageIconLabel();

            try {
                // 注意：這裡假設圖片仍然是 Classpath 資源（例如放在 src/main/resources/images/）
                // 如果你的圖片也放在外部檔案系統中（例如 shared_data/images/），
                // 那麼這裡也需要類似 loadMoviesFromJson 的方式使用 File 和 FileInputStream
                URL imageUrl = getClass().getClassLoader().getResource(movie.getImagePath());
                if (imageUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imageUrl);
                    posterLabel.setIcon(originalIcon); // 設定原始 Icon
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

            JLabel titleLabel = new JLabel(movie.getTitle(), SwingConstants.CENTER);
            titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setOpaque(true);
            titleLabel.setBackground(Color.DARK_GRAY);
            // 確保標題高度固定，不要被拉伸太多
            titleLabel.setPreferredSize(new Dimension(1, 20)); // 給定一個最小高度

            moviePosterContainer.add(posterLabel, BorderLayout.CENTER); // 海報放中間
            moviePosterContainer.add(titleLabel, BorderLayout.SOUTH); // 標題放底部

            moviePosterContainer.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final Movie finalMovie = movie;
            moviePosterContainer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(MovieInfoUI.this,
                            "電影名稱: " + finalMovie.getTitle() + "\n"
                            + "片長: " + finalMovie.getRuntime() + " 分鐘\n"
                            + "簡介: " + finalMovie.getIntroduction() + "\n"
                            + "分級: " + finalMovie.getRating(),
                            "電影資訊",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    moviePosterContainer.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    moviePosterContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY.darker(), 1));
                }
            });
            gridPanel.add(moviePosterContainer);
        }

        // 如果電影數量不足 5 個的倍數，填充空白佔位符，讓 GridLayout 網格看起來更完整
        int totalCells = rows * cols;
        int emptySlots = totalCells - movieDetails.size();
        for (int i = 0; i < emptySlots; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(0x95B0D0)); // 與背景色一致
            gridPanel.add(emptyPanel);
        }

        return gridPanel;
    }

    private void showMovies(String type) {
        moviesCardLayout.show(moviesPanel, type);
    }
}