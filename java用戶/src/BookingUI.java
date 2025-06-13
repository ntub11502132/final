import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javax.swing.Timer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class BookingUI extends JPanel {
    private MainForm parentFrame;
    private Movie selectedMovie;
    private JLabel bookingInfoLabel;

    private HallASeatSelectionPanel hallASeatSelectionPanel;
    private HallBSeatSelectionPanel hallBSeatSelectionPanel;
    private JPanel seatSelectionCardPanel;
    private CardLayout seatSelectionCardLayout;

    private String currentHallDisplayed = "";
    private String currentShowtimeId = ""; // 儲存包含電影標題、日期、時間和影廳的完整場次ID

    private JComboBox<String> showtimeComboBox;
    private JButton selectShowtimeButton;

    private OrderManager orderManager;

    private static final double TICKET_PRICE = 300.0;

    public BookingUI(MainForm parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(0xD0F0D0));

        orderManager = new OrderManager();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(new Color(0xD0F0D0));
        bookingInfoLabel = new JLabel("請選擇場次和座位");
        bookingInfoLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        topPanel.add(bookingInfoLabel);

        showtimeComboBox = new JComboBox<>();
        showtimeComboBox.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        showtimeComboBox.setPreferredSize(new Dimension(250, 30));
        topPanel.add(showtimeComboBox);

        selectShowtimeButton = new JButton("確認場次");
        selectShowtimeButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        selectShowtimeButton.addActionListener(e -> confirmShowtimeSelection());
        topPanel.add(selectShowtimeButton);

        add(topPanel, BorderLayout.NORTH);

        seatSelectionCardLayout = new CardLayout();
        seatSelectionCardPanel = new JPanel(seatSelectionCardLayout);
        seatSelectionCardPanel.setBackground(new Color(0xD0F0D0));

        hallASeatSelectionPanel = new HallASeatSelectionPanel();
        hallBSeatSelectionPanel = new HallBSeatSelectionPanel();

        seatSelectionCardPanel.add(hallASeatSelectionPanel, "HallA");
        seatSelectionCardPanel.add(hallBSeatSelectionPanel, "HallB");
        // 添加一個空的提示面板
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(new Color(0xD0F0D0));
        JLabel emptyLabel = new JLabel("請先選擇電影和場次");
        emptyLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        emptyPanel.add(emptyLabel);
        seatSelectionCardPanel.add(emptyPanel, "Empty");


        add(seatSelectionCardPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(new Color(0xD0F0D0));
        JButton confirmButton = new JButton("確認訂票");
        confirmButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        confirmButton.addActionListener(e -> confirmBooking());
        bottomPanel.add(confirmButton);
        add(bottomPanel, BorderLayout.SOUTH);

        showtimeComboBox.setVisible(false);
        selectShowtimeButton.setVisible(false);
        seatSelectionCardLayout.show(seatSelectionCardPanel, "Empty");
    }

    public void setupBooking(Movie movie) {
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "請先登入會員才能進行訂票。",
                    "未登入會員",
                    JOptionPane.WARNING_MESSAGE);
            parentFrame.showPanel("quick");
            return;
        }

        this.selectedMovie = movie;

        bookingInfoLabel.setText("您選擇了電影：《" + movie.getTitle() + "》\n請選擇場次");

        loadShowtimes(movie);

        showtimeComboBox.setVisible(true);
        selectShowtimeButton.setVisible(true);
        seatSelectionCardLayout.show(seatSelectionCardPanel, "Empty");
        clearCurrentSeats();

        currentHallDisplayed = "";
        currentShowtimeId = "";
    }

    private void loadShowtimes(Movie movie) {
        showtimeComboBox.removeAllItems();

        if (movie == null || movie.getShowtimes() == null || movie.getShowtimes().isEmpty()) {
            JOptionPane.showMessageDialog(this, "該電影沒有可用的場次資訊。", "錯誤", JOptionPane.ERROR_MESSAGE);
            selectShowtimeButton.setEnabled(false);
            return;
        }

        List<Showtime> rawShowtimes = movie.getShowtimes();
        
        // 使用一個新的列表來儲存可排序的 LocalDateTime 物件，每個物件都帶有其原始的 Showtime 參考
        List<ComparableShowtime> sortableShowtimes = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        for (Showtime st : rawShowtimes) {
            try {
                LocalDateTime showtimeDateTime = LocalDateTime.parse(st.getDate() + " " + st.getTime(), inputFormatter);
                // 將原始的 Showtime 物件和解析後的 LocalDateTime 包裝起來
                sortableShowtimes.add(new ComparableShowtime(showtimeDateTime, st));
            } catch (Exception e) {
                System.err.println("Error parsing showtime date/time for sorting: " + st.getDate() + " " + st.getTime() + " - " + e.getMessage());
            }
        }

        // 排序場次 (從早到晚)
        sortableShowtimes.sort(Comparator.naturalOrder()); // ComparableShowtime 已經實現了 Comparable

        // 將排序後的 Showtime 物件轉換為顯示字串並加入 ComboBox
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"); // 顯示格式
        for (ComparableShowtime cs : sortableShowtimes) {
            Showtime originalShowtime = cs.getOriginalShowtime();
            // 完整場次字串包含日期、時間和影廳資訊
            String fullShowtimeString = originalShowtime.getDate() + " " + originalShowtime.getTime() + " (" + originalShowtime.getHallType() + ")";
            showtimeComboBox.addItem(fullShowtimeString);
        }

        selectShowtimeButton.setEnabled(true);
    }

    // 輔助類別，用於將 Showtime 物件與其 LocalDateTime 表示連結起來，以便排序
    private static class ComparableShowtime implements Comparable<ComparableShowtime> {
        private LocalDateTime dateTime;
        private Showtime originalShowtime;

        public ComparableShowtime(LocalDateTime dateTime, Showtime originalShowtime) {
            this.dateTime = dateTime;
            this.originalShowtime = originalShowtime;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public Showtime getOriginalShowtime() {
            return originalShowtime;
        }

        @Override
        public int compareTo(ComparableShowtime other) {
            return this.dateTime.compareTo(other.dateTime);
        }
    }


    private void confirmShowtimeSelection() {
        String selectedShowtimeString = (String) showtimeComboBox.getSelectedItem();
        if (selectedShowtimeString == null || selectedShowtimeString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請選擇一個場次。", "選場次提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 從選擇的場次字串中解析出影廳名稱
        // 格式例如："2025/06/10 10:00 (Hall A)"
        String hallName = "";
        int startIndex = selectedShowtimeString.indexOf("(");
        int endIndex = selectedShowtimeString.indexOf(")");
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            hallName = selectedShowtimeString.substring(startIndex + 1, endIndex);
        } else {
            JOptionPane.showMessageDialog(this, "無法解析選擇場次的影廳資訊。", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 根據 hallName 設定 currentHallDisplayed
        if (hallName.equals("Hall A")) {
            currentHallDisplayed = "HallA";
        } else if (hallName.equals("Hall B")) {
            currentHallDisplayed = "HallB";
        } else {
             JOptionPane.showMessageDialog(this, "不支援的影廳類型：" + hallName, "錯誤", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // 更新 bookingInfoLabel
        bookingInfoLabel.setText("您選擇了電影：《" + selectedMovie.getTitle() + "》\n場次: " + selectedShowtimeString + "\n請選擇座位");

        // 將完整的場次字串保存為 currentShowtimeId
        // 格式為 "電影名稱 - 2025/06/10 10:00 (Hall A)"
        //currentShowtimeId = selectedMovie.getTitle() + " - " + selectedShowtimeString;
        currentShowtimeId = selectedShowtimeString;
        
        // 根據影廳顯示相應的座位選擇面板
        if (currentHallDisplayed.equals("HallA")) {
            hallASeatSelectionPanel.simulateLoadSeats(currentShowtimeId); // 傳遞完整的場次ID
            seatSelectionCardLayout.show(seatSelectionCardPanel, "HallA");
            if(hallBSeatSelectionPanel != null) hallBSeatSelectionPanel.clearSelectedSeats();
        } else if (currentHallDisplayed.equals("HallB")) {
            hallBSeatSelectionPanel.simulateLoadSeats(currentShowtimeId); // 傳遞完整的場次ID
            seatSelectionCardLayout.show(seatSelectionCardPanel, "HallB");
            if(hallASeatSelectionPanel != null) hallASeatSelectionPanel.clearSelectedSeats();
        }

        // 隱藏場次選擇組件，準備進行座位選擇
        showtimeComboBox.setVisible(false);
        selectShowtimeButton.setVisible(false);
    }


    private void confirmBooking() {
        if (!SessionManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "您尚未登入，請先登入會員。",
                    "未登入會員",
                    JOptionPane.WARNING_MESSAGE);
            parentFrame.showPanel("MemberLogin");
            return;
        }

        if (currentShowtimeId.isEmpty() || selectedMovie == null) {
            JOptionPane.showMessageDialog(this, "請先選擇電影和場次。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> seats;
        String hallName = "";

        if (currentHallDisplayed.equals("HallA")) {
            seats = hallASeatSelectionPanel.getSelectedSeats();
            hallName = "Hall A";
        } else if (currentHallDisplayed.equals("HallB")) {
            seats = hallBSeatSelectionPanel.getSelectedSeats();
            hallName = "Hall B";
        } else {
            JOptionPane.showMessageDialog(this, "未選擇影廳或無效的影廳狀態，請重新選擇電影和場次。", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (seats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請至少選擇一個座位。", "選位提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loggedInUsername = SessionManager.getLoggedInUserId();
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "無法獲取登入會員資訊，請重新登入。", "錯誤", JOptionPane.ERROR_MESSAGE);
            parentFrame.showPanel("MemberLogin");
            return;
        }

        double totalAmount = seats.size() * TICKET_PRICE;

        Object[] options = {"現金支付", "線上支付", "取消"};
        int paymentMethod = JOptionPane.showOptionDialog(this,
                "請選擇付款方式：\n\n您將為電影《" + selectedMovie.getTitle() + "》訂購 " + seats.size() + " 張票。\n" +
                "座位號碼：" + String.join(", ", seats) + "\n" +
                "總金額：NT$" + String.format("%.2f", totalAmount) + "\n",
                "選擇付款方式",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (paymentMethod == JOptionPane.CLOSED_OPTION || paymentMethod == 2) {
            JOptionPane.showMessageDialog(this, "您已取消訂票。", "訂票取消", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newOrderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String bookingTimeString = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // currentShowtimeId 的格式是 "電影名稱 - YYYY/MM/DD HH:mm (Hall X)"
        // Order 物件的 showtime 屬性直接使用這個完整字串
        Order newOrder = new Order(
            newOrderId,
            loggedInUsername,
            selectedMovie.getTitle(),
            currentShowtimeId, // 直接使用完整的 currentShowtimeId
            hallName,
            seats,
            bookingTimeString,
            "待付款",
            totalAmount
        );

        if (paymentMethod == 0) {
            boolean orderAdded = orderManager.addOrder(newOrder);

            if (orderAdded) {
                JOptionPane.showMessageDialog(this,
                        "訂單編號: " + newOrderId + "\n" +
                        "請至櫃台結帳，訂單狀態為「待付款」。\n" +
                        "總金額：NT$" + String.format("%.2f", totalAmount) + "\n" +
                        "感謝您的使用。",
                        "訂票成功 (現金支付)",
                        JOptionPane.INFORMATION_MESSAGE);

                clearCurrentSeats();
                resetBookingUI();
                parentFrame.showPanel("Default");
            } else {
                JOptionPane.showMessageDialog(this, "訂單保存失敗，請稍後再試。", "訂票失敗", JOptionPane.ERROR_MESSAGE);
            }
        } else if (paymentMethod == 1) {
            JDialog qrDialog = new JDialog(parentFrame, "線上支付", true);
            qrDialog.setLayout(new BorderLayout(10, 10));
            qrDialog.setSize(400, 450);
            qrDialog.setLocationRelativeTo(this);

            ImageIcon qrCodeIcon = null;
            URL imageUrl = getClass().getResource("/images/qrcode_payment.png");
            if (imageUrl != null) {
                qrCodeIcon = new ImageIcon(imageUrl);
                Image image = qrCodeIcon.getImage();
                Image newimg = image.getScaledInstance(300, 300,  java.awt.Image.SCALE_SMOOTH);
                qrCodeIcon = new ImageIcon(newimg);
            } else {
                System.err.println("QR Code 圖片未找到：/images/qrcode_payment.png");
            }

            JLabel qrCodeLabel = new JLabel();
            if (qrCodeIcon != null) {
                qrCodeLabel.setIcon(qrCodeIcon);
            } else {
                qrCodeLabel.setText("QR Code 圖片載入失敗");
            }

            qrCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrCodeLabel.setVerticalAlignment(SwingConstants.CENTER);
            qrCodeLabel.setText("<html><center>請掃描 QR Code 完成支付<br><br>" + (qrCodeIcon == null ? "QR Code 圖片未找到" : "") + "<br><br>等待支付中...</center></html>");
            qrCodeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            qrCodeLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
            qrCodeLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            qrDialog.add(qrCodeLabel, BorderLayout.CENTER);

            JProgressBar progressBar = new JProgressBar(0, 5);
            progressBar.setStringPainted(true);
            progressBar.setString("5秒後自動確認付款");
            progressBar.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            qrDialog.add(progressBar, BorderLayout.SOUTH);

            Timer timer = new Timer(1000, e -> {
                int value = progressBar.getValue();
                progressBar.setValue(value + 1);
                progressBar.setString((5 - (value + 1)) + "秒後自動確認付款");
                if (value + 1 >= 5) {
                    ((Timer) e.getSource()).stop();
                    qrDialog.dispose();

                    newOrder.setOrderStatus("已付款");
                    boolean orderAdded = orderManager.addOrder(newOrder);

                    if (orderAdded) {
                        JOptionPane.showMessageDialog(this,
                                "訂單編號: " + newOrderId + "\n" +
                                "您已成功付款並完成訂票！\n" +
                                "總金額：NT$" + String.format("%.2f", totalAmount) + "\n" +
                                "感謝您的使用。",
                                "訂票成功 (線上支付)",
                                JOptionPane.INFORMATION_MESSAGE);

                        clearCurrentSeats();
                        resetBookingUI();
                        parentFrame.showPanel("Default");
                    } else {
                        JOptionPane.showMessageDialog(this, "訂單保存失敗，請稍後再試。", "訂票失敗", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            timer.setInitialDelay(0);
            timer.start();

            qrDialog.setVisible(true);
        }
    }

    private void clearCurrentSeats() {
        if (hallASeatSelectionPanel != null) hallASeatSelectionPanel.clearSelectedSeats();
        if (hallBSeatSelectionPanel != null) hallBSeatSelectionPanel.clearSelectedSeats();
    }

    private void resetBookingUI() {
        selectedMovie = null;
        bookingInfoLabel.setText("請選擇場次和座位");
        showtimeComboBox.removeAllItems();
        showtimeComboBox.setVisible(false);
        selectShowtimeButton.setVisible(false);
        seatSelectionCardLayout.show(seatSelectionCardPanel, "Empty");
        clearCurrentSeats();
        currentHallDisplayed = "";
        currentShowtimeId = "";
    }
}