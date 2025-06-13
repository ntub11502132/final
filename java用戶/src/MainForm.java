import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainForm extends JFrame {

    private JPanel cardPanel; // 用於存放不同界面的卡片面板
    private CardLayout cardLayout; // CardLayout 管理器
    private BookingUI bookingUI; // 聲明 BookingUI 實例
    private BookingQueryUI bookingQueryUI; // 聲明 BookingQueryUI 實例
    private JPanel buttonPanel; // **將 buttonPanel 聲明為成員變數**
    //------------------------------------------
    // ... 其他成員變數 ...
// ...

    public MainForm() {
        // 1. 主視窗設定
        setTitle("111電影院售票系統");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0)); // JFrame 使用 BorderLayout，間距為0

        // 2. 頂部面板 (topPanel)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 上下左右邊距

        // 3. 左側標籤 ("111")
        JLabel titleLabel = new JLabel("111");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36)); // 設定字體和大小
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 4. 右側按鈕面板 (buttonPanel)
        this.buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // 靠右對齊，按鈕間水平間距10
        buttonPanel.setBackground(Color.WHITE);

        String[] buttonTexts = {"線上訂票", "電影資訊", "影城介紹", "會員登入/註冊"}; // 移除 "重要資訊"
        Border buttonBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC)), // 淺灰色邊框
                BorderFactory.createEmptyBorder(5, 15, 5, 15) // 按鈕內邊距
        );

        for (String text : buttonTexts) {
            JButton button = new JButton(text);
            button.setBackground(Color.WHITE);
            button.setOpaque(true); // 必須設定為true才能顯示背景色 (在某些 Look & Feel 下)
            button.setBorder(buttonBorder);
            button.setFocusPainted(false); // 移除焦點繪製的虛線框 (可選)
            button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12)); // 微軟正黑體或適合的字體

            if (text.equals("線上訂票")) {
                // 為線上訂票按鈕創建 JPopupMenu (下拉選單)
                JPopupMenu onlineBookingMenu = new JPopupMenu();
                JMenuItem queryItem = new JMenuItem("訂票查詢");
                JMenuItem infoItem = new JMenuItem("訂票資訊");
                JMenuItem quickItem = new JMenuItem("快速訂票");

                // 為菜單項添加監聽器
                queryItem.addActionListener(e -> {
                   if (SessionManager.isLoggedIn()) {
                        // 如果已登入，則顯示訂票查詢面板
                        this.bookingQueryUI.updateUIBasedOnLoginStatus(); // 確保 UI 狀態更新
                        showPanel("BookingQuery");
                    } else {
                        // 未登入，彈出錯誤訊息
                        JOptionPane.showMessageDialog(MainForm.this,
                                "請先登入會員才能查詢訂單。",
                                "未登入會員",
                                JOptionPane.WARNING_MESSAGE);
                                // 可以選擇跳轉到登入頁面
                                showPanel("MemberLogin");
                    }
                });
                infoItem.addActionListener(e -> {
                    showPanel("TicketInfo");
                });
                quickItem.addActionListener(e -> {
                    if (SessionManager.isLoggedIn()) {
                        // 如果已登入，則顯示訂票查詢面板
                        this.bookingQueryUI.updateUIBasedOnLoginStatus(); // 確保 UI 狀態更新
                        showPanel("quick"); 
                    } else {
                        // 未登入，彈出錯誤訊息
                        JOptionPane.showMessageDialog(MainForm.this,
                                "請先登入會員才能查詢訂單。",
                                "未登入會員",
                                JOptionPane.WARNING_MESSAGE);
                                // 可以選擇跳轉到登入頁面
                                showPanel("MemberLogin");
                    }
                });

                onlineBookingMenu.add(queryItem);
                onlineBookingMenu.add(infoItem);
                onlineBookingMenu.add(quickItem);

                // 當線上訂票按鈕被點擊時顯示菜單
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onlineBookingMenu.show(button, 0, button.getHeight());
                    }
                });
            } else if (text.equals("會員登入/註冊")) {
                // 為「會員登入/註冊」按鈕添加特殊處理
                button.addActionListener(e -> {
                    if (SessionManager.isLoggedIn()) {
                        // 如果已登入，顯示登出確認或會員中心
                        int choice = JOptionPane.showConfirmDialog(MainForm.this,
                                "您已登入為 " + SessionManager.getLoggedInUserId() + "。\n要登出嗎？",
                                "會員已登入", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            SessionManager.logout();
                            JOptionPane.showMessageDialog(MainForm.this, "您已成功登出。", "登出成功", JOptionPane.INFORMATION_MESSAGE);
                            updateLoginStatusUI(); // 更新相關 UI
                            showPanel("Default"); // 登出後返回首頁
                        }
                    } else {
                        // 未登入，顯示登入/註冊面板
                        showPanel("MemberLogin");
                    }
                });
            } else {
                // 其他按鈕仍然使用 handleNavButtonClick 方法處理
                button.addActionListener(e -> handleNavButtonClick(e.getActionCommand()));
            }
            buttonPanel.add(button);
        }
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // 將 topPanel 添加到 JFrame 的 NORTH 位置
        add(topPanel, BorderLayout.NORTH);

        // 5. 下方主內容面板 (mainContentPanel)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(0xF0F0F0)); // 淺灰色背景

        // 初始化並添加各個主要內容面板
        JPanel defaultPanel = createDefaultPanel(); // 預設的首頁或歡迎頁
        MemberLoginUI memberLoginPanel = new MemberLoginUI(this);
        MemberRegisterUI memberRegisterPanel = new MemberRegisterUI(); // 註冊面板
        this.bookingQueryUI = new BookingQueryUI();
        // **在這裡初始化 bookingUI 和 quickBookingUI，並將 this (MainForm) 傳遞給它們**
        this.bookingUI = new BookingUI(this); // 將 MainForm 實例傳遞給 BookingUI
        QuickBookingUI quickBookingUI = new QuickBookingUI(this); // 將 MainForm 實例傳遞給 QuickBookingUI

        TicketInfoUI ticketInfoPanel = new TicketInfoUI();
        MovieInfoUI movieInfoPanel = new MovieInfoUI(this);
        CinemaIntroUI cinemaIntroUI = new CinemaIntroUI();
        
        cardPanel.add(defaultPanel, "Default");
        cardPanel.add(memberLoginPanel, "MemberLogin");
        cardPanel.add(memberRegisterPanel, "MemberRegister"); // 新增註冊面板
        cardPanel.add(this.bookingQueryUI, "BookingQuery"); // 新增訂票查詢面板
        cardPanel.add(ticketInfoPanel, "TicketInfo"); // 新增票價資訊面板
        cardPanel.add(movieInfoPanel, "movieInfo");
        cardPanel.add(quickBookingUI, "quick");
        cardPanel.add(bookingUI, "Booking");
        cardPanel.add(cinemaIntroUI, "CinemaIntro");

        // 將 cardPanel 添加到 JFrame 的 CENTER 位置
        add(cardPanel, BorderLayout.CENTER);

        // 顯示預設面板
        cardLayout.show(cardPanel, "Default");

        // 6. 顯示視窗
        pack();
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 公開方法，用於外部（例如 MemberLoginUI, QuickBookingUI）切換面板
    public void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }

    // 處理頂部導航按鈕點擊事件 (非線上訂票按鈕)
    private void handleNavButtonClick(String command) {
        switch (command) {
            case "會員登入/註冊":
                showPanel("MemberLogin"); // 調用 showPanel 方法
                break;
            case "電影資訊":
                showPanel("movieInfo"); // 調用 showPanel 方法
                break;
            case "影城介紹":
                showPanel("CinemaIntro");
                break;
            default:
                showPanel("Default"); // 預設回到首頁
                break;
        }
    }

    public void updateLoginStatusUI() {
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().equals("會員登入/註冊") || btn.getText().startsWith("歡迎 ")) {
                    if (SessionManager.isLoggedIn()) {
                        btn.setText("歡迎 " + SessionManager.getLoggedInUserId() + " / 登出");
                    } else {
                        btn.setText("會員登入/註冊");
                    }
                    break;
                }
            }
        }
    }

    // 預設的首頁或歡迎面板
    private JPanel createDefaultPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0xF0F0F0));
        JLabel label = new JLabel("歡迎來到 111 電影院售票系統！");
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    // 新增此方法，讓 QuickBookingUI 能夠取得 BookingUI 實例
    public BookingUI getBookingUI() {
        return bookingUI;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new MainForm();
            }
        });
    }
}