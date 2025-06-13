import java.util.List;

public class Order {
    private String orderId;
    private String username;     // 下訂單的會員名稱
    private String movieTitle;   // 電影名稱
    private String showtime;     // 場次時間 (簡化為字串)
    private String hallName;     // 影廳名稱 (Hall A 或 Hall B)
    private List<String> seats;  // 已選座位列表
    private String bookingTime; // 訂票時間，改為 String 型別
    private String orderStatus; // 新增：用於表示訂單狀態，例如 "已付款", "已退票"
    private double totalAmount;

    // 無參建構子 (Jackson 需要)
    public Order() {
    }

    // 完整的建構子
    public Order(String orderId, String username, String movieTitle, String showtime, String hallName, List<String> seats, String bookingTime,String orderStatus, double totalAmount) {
        this.orderId = orderId;
        this.username = username;
        this.movieTitle = movieTitle;
        this.showtime = showtime;
        this.hallName = hallName;
        this.seats = seats;
        this.bookingTime = bookingTime;
        this.orderStatus = (orderStatus != null && !orderStatus.isEmpty()) ? orderStatus : "已付款";
        this.totalAmount = totalAmount;
    }

    // Getters and Setters (注意 bookingTime 的型別)
    public String getOrderId() { return orderId; } // <-- 新增 getter
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public String getBookingTime() { // Getter 變為 String
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) { // Setter 變為 String
        this.bookingTime = bookingTime;
    }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
   
    @Override
    public String toString() {
        return "Order{" +
               "username='" + username + '\'' +
               ", movieTitle='" + movieTitle + '\'' +
               ", showtime='" + showtime + '\'' +
               ", hallName='" + hallName + '\'' +
               ", seats=" + seats +
               ", bookingTime='" + bookingTime + '\'' + // 注意這裡也變成 String
               '}';
    }
}