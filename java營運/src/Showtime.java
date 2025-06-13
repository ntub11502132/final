import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Showtime {
    private String showtimeId;
    private String hallType;
    private String date;
    private String time;
    private List<Seat> seats;

    // 構造函數
    public Showtime() {}

    // Getter 和 Setter 方法
    @JsonProperty("showtimeId")
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }

    @JsonProperty("hallType")
    public String getHallType() { return hallType; }
    public void setHallType(String hallType) { this.hallType = hallType; }

    @JsonProperty("date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @JsonProperty("time")
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @JsonProperty("seats")
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}