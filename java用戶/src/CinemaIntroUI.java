import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class CinemaIntroUI extends JPanel {

    public CinemaIntroUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel titleLabel = new JLabel("歡迎來到 111 電影院", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 36));
        titleLabel.setForeground(new Color(50, 50, 50));
        add(titleLabel, BorderLayout.NORTH);

        JEditorPane contentPane = new JEditorPane();
        contentPane.setEditable(false);
        contentPane.setContentType("text/html");
        contentPane.setBackground(getBackground());
        contentPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 30, 20, 30)
        ));
        contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        String introHtml = "<html>"
                         + "<head>"
                         + "<style>"
                         + "body { font-family: 'Microsoft JhengHei', sans-serif; font-size: 16px; color: #333; line-height: 1.6; }"
                         + "h2 { color: #007bff; font-size: 24px; margin-top: 20px; }"
                         + "ul { list-style-type: disc; margin-left: 20px; }"
                         + "li { margin-bottom: 8px; }"
                         + ".highlight { color: #dc3545; font-weight: bold; }"
                         + ".note { font-size: 14px; color: #6c757d; font-style: italic; }"
                         + "</style>"
                         + "</head>"
                         + "<body>"
                         + "<p>111 電影院致力於提供最優質的觀影體驗，讓您沉浸在電影的魔力之中。我們擁有先進的設備、舒適的環境以及多元的電影選擇，是您休閒娛樂的最佳去處！</p>"
                         + "<h2>影城特色</h2>"
                         + "<ul>"
                         + "<li><span class=\"highlight\">頂級影音設備：</span> 配備最新雷射投影技術和沉浸式杜比全景聲 (Dolby Atmos®) 音響系統，為您帶來無與倫比的視聽震撼。</li>"
                         + "<li><span class=\"highlight\">豪華舒適座椅：</span> 所有影廳均採用符合人體工學的寬敞座椅，部分影廳更提供尊榮級沙發座椅，讓您輕鬆享受長達數小時的觀影時光。</li>"
                         + "<li><span class=\"highlight\">多元影廳選擇：</span> 從寬闊的主影廳到溫馨的精品小廳，滿足不同觀影人數和需求。</li>"
                         + "<li><span class=\"highlight\">便捷交通位置：</span> 影院位於市中心，交通便利，鄰近多條公車路線和捷運站，讓您輕鬆抵達。</li>"
                         + "<li><span class=\"highlight\">豐富餐飲選擇：</span> 提供多樣化的電影小吃、飲品以及精緻點心，滿足您的味蕾。</li>"
                         + "</ul>"
                         + "<h2>聯絡我們</h2>"
                         + "<p>地址：台北市信義區物導路77號 </p>"
                         + "<p>電話：(02) 5870-5870</p>"
                         + "<p>營業時間：每日 09:00 - 24:00</p>"
                         + "<p class=\"note\">期待您的光臨！</p>"
                         + "</body>"
                         + "</html>";
        contentPane.setText(introHtml);
        
        // **修改點 1: 設定 JEditorPane 的首選大小**
        // 這告訴 JEditorPane 在沒有足夠空間時，它寧願讓 JScrollPane 顯示滾動條，而不是無限擴張。
        // 這裡設定一個合理的寬度 (例如 700) 和高度 (例如 400)，你可以根據實際情況調整。
        contentPane.setPreferredSize(new Dimension(700, 400)); 
        // 也可以考慮使用 setMinimumSize 和 setMaximumSize
        // contentPane.setMinimumSize(new Dimension(600, 300));
        // contentPane.setMaximumSize(new Dimension(800, 500));


        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 避免水平滾動條，通常內容會自動換行
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);

        // --- 底部圖片 (保持原樣，或根據需要調整圖片大小) ---
        try {
            // 注意：這裡的圖片路徑需要根據您的專案結構調整
            // 如果圖片在專案根目錄下的 'images' 資料夾，路徑應為 "images/cinema_exterior.jpg"
            // 如果在 src/main/resources/images，getClass().getClassLoader().getResource() 是正確的
            URL imageUrl = getClass().getClassLoader().getResource("images/cinema_exterior.jpg"); 
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image image = originalIcon.getImage();
                
                // **修改點 2: 調整圖片的縮放邏輯，確保圖片不會過大**
                // 這裡設定圖片最大寬度為 600px，最大高度為 200px。
                // 保持比例縮放。
                int targetWidth = 600; 
                int targetHeight = 200;

                int originalWidth = image.getWidth(null);
                int originalHeight = image.getHeight(null);

                double scaleFactor = 1.0;
                if (originalWidth > targetWidth) {
                    scaleFactor = (double) targetWidth / originalWidth;
                }
                if (originalHeight * scaleFactor > targetHeight) { // 如果按寬度縮放後高度仍然過大
                    scaleFactor = (double) targetHeight / originalHeight;
                }

                int newWidth = (int) (originalWidth * scaleFactor);
                int newHeight = (int) (originalHeight * scaleFactor);
                
                Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                imageLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
                add(imageLabel, BorderLayout.SOUTH);
            } else {
                System.err.println("影院圖片未找到: images/cinema_exterior.jpg");
                JLabel placeholderLabel = new JLabel("未找到影城圖片", SwingConstants.CENTER);
                placeholderLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
                placeholderLabel.setForeground(Color.GRAY);
                add(placeholderLabel, BorderLayout.SOUTH);
            }
        } catch (Exception e) {
            System.err.println("載入影院圖片時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            JLabel errorLabel = new JLabel("載入圖片失敗", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.SOUTH);
        }
    }
}