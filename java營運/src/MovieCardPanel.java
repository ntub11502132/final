import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; // For checking file existence

public class MovieCardPanel extends JPanel {

    private String movieTitle;
    private ImageIcon moviePoster;
    private JLabel posterLabel;
    private JLabel titleLabel;
    private JButton addButton; // "點擊以新增" 的按鈕

    private MainForm parentFrame; // 引用 MainForm

    public MovieCardPanel(String movieTitle, MainForm parentFrame) {
        this.movieTitle = movieTitle;
        this.parentFrame = parentFrame; // 保存 MainForm 的引用
        setLayout(new BorderLayout()); // 使用 BorderLayout
        setBorder(BorderFactory.createDashedBorder(Color.GRAY, 2, 2, 2, true)); // 虛線邊框
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(180, 250)); // 設定卡片的推薦大小

        // 檢查是否為 "點擊以新增" 的卡片
        if (movieTitle == null || movieTitle.isEmpty()) {
            initAddCard(); // 初始化新增卡片
        } else {
            initMovieCard(); // 初始化電影信息卡片
        }
    }

    private void initMovieCard() {
        // 嘗試載入圖片
        // 假設圖片檔案在 /images/ 目錄下，且命名為 [電影名稱].jpg
        String imagePath = "images/" + movieTitle + ".jpg"; // 假設圖片路徑
        
        // 定義圖片和文字所佔的高度比例
        double imageHeightRatio = 0.60; // 圖片佔卡片高度的 60%
        double titleHeightRatio = 0.40; // 標題佔卡片高度的 40% (0.60 + 0.40 = 1.0)

        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(imagePath);
            if (imgURL != null) {
                moviePoster = new ImageIcon(imgURL);
                Image img = moviePoster.getImage();
                
                // 計算圖片應有的高度
                int imgTargetHeight = (int) (getPreferredSize().getHeight() * imageHeightRatio);
                // 根據目標高度計算圖片寬度，保持圖片比例
                int imgScaledWidth = (int) (img.getWidth(this) * ((double) imgTargetHeight / img.getHeight(this)));
                
                // 如果計算出的寬度大於卡片寬度，則以卡片寬度為基準重新計算高度
                if (imgScaledWidth > getPreferredSize().getWidth()) {
                    imgScaledWidth = getPreferredSize().width;
                    imgTargetHeight = (int) (img.getHeight(this) * ((double) imgScaledWidth / img.getWidth(this)));
                }

                Image scaledImg = img.getScaledInstance(imgScaledWidth, imgTargetHeight, Image.SCALE_SMOOTH);
                moviePoster = new ImageIcon(scaledImg);
            } else {
                System.err.println("找不到圖片檔案: " + imagePath);
                moviePoster = null; // 清除圖片，以便下方顯示文字
            }
        } catch (Exception e) {
            System.err.println("載入圖片時發生錯誤: " + e.getMessage());
            moviePoster = null; // 清除圖片
        }

        posterLabel = new JLabel();
        if (moviePoster != null) {
            posterLabel.setIcon(moviePoster);
        } else {
            posterLabel.setText("無海報"); // 如果沒圖片就顯示文字
            posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            posterLabel.setVerticalAlignment(SwingConstants.CENTER);
        }
        // 設定 posterLabel 的推薦大小，這裡的高度應該與圖片目標高度一致
        posterLabel.setPreferredSize(new Dimension(getPreferredSize().width, (int) (getPreferredSize().height * imageHeightRatio))); 
        add(posterLabel, BorderLayout.CENTER);

        titleLabel = new JLabel(movieTitle, SwingConstants.CENTER); // 文字置中
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        // 設定 titleLabel 的推薦大小，這裡的高度應該與文字目標高度一致
        titleLabel.setPreferredSize(new Dimension(getPreferredSize().width, (int) (getPreferredSize().height * titleHeightRatio))); 
        add(titleLabel, BorderLayout.SOUTH);

        // 添加點擊事件，切換到電影詳情面板
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("點擊了電影卡片: " + movieTitle);
                if (parentFrame != null) {
                    parentFrame.displayPanel(new MovieDetailPanel(movieTitle));
                }
            }
        });
    }

    private void initAddCard() {
        addButton = new JButton("點擊以新增");
        addButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        addButton.setBackground(new Color(240, 240, 240));
        addButton.setForeground(Color.GRAY);
        addButton.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2));
        addButton.setFocusPainted(false);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        wrapperPanel.add(addButton, gbc);

        add(wrapperPanel, BorderLayout.CENTER);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("點擊了新增電影卡片。");
                JOptionPane.showMessageDialog(MovieCardPanel.this, "彈出新增電影對話框 (待實現)", "新增電影", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}