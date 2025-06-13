import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SeatPanelTester extends JFrame {

    private JPanel mainPanel; // 用於切換顯示不同座位面板的容器
    private CardLayout cardLayout; // 管理 mainPanel 的佈局

    private HallASeatSelectionPanel hallAPanel;
    private HallBSeatSelectionPanel hallBPanel;

    public SeatPanelTester() {
        setTitle("座位廳測試程式");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); // 調整視窗大小以容納複雜的座位佈局
        setLocationRelativeTo(null); // 視窗置中

        // 初始化 CardLayout 和 mainPanel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 初始化兩個座位面板
        hallAPanel = new HallASeatSelectionPanel();
        hallBPanel = new HallBSeatSelectionPanel();

        // 將兩個座位面板添加到 mainPanel 中，並給它們命名
        mainPanel.add(hallAPanel, "HallA");
        mainPanel.add(hallBPanel, "HallB");

        // 創建一個按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton showHallAButton = new JButton("顯示螢幕廳 (Hall A)");
        JButton showHallBButton = new JButton("顯示銀幕廳 (Hall B)");

        // 為按鈕添加事件監聽器
        showHallAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "HallA"); // 顯示 Hall A 的面板
                // 模擬載入不同場次的座位數據
                hallAPanel.simulateLoadSeats("電影A 場次1");
            }
        });

        showHallBButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "HallB"); // 顯示 Hall B 的面板
                // 模擬載入不同場次的座位數據
                hallBPanel.simulateLoadSeats("電影B 晚場");
            }
        });

        buttonPanel.add(showHallAButton);
        buttonPanel.add(showHallBButton);

        // 將按鈕面板和主面板添加到 JFrame 中
        add(buttonPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // 預設顯示 Hall A
        cardLayout.show(mainPanel, "HallA");
        hallAPanel.simulateLoadSeats("電影A 場次1");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SeatPanelTester().setVisible(true);
            }
        });
    }
}