import com.fasterxml.jackson.annotation.JsonProperty;

public class Seat {
    private String number;
    private boolean isOccupied; // 注意這裡的 JSON 鍵名是 "isOccupied"

    // 構造函數
    public Seat() {}

    // Getter 和 Setter 方法
    @JsonProperty("number")
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    @JsonProperty("isOccupied") // 確保屬性名稱與 JSON 鍵名匹配
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}