import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Movie {
private String id;
    private String title;
    private int runtime;
    private String introduction;
    private String rating;
    private String imagePath;
    private String actors;
    private String director;
    private List<String> hallTypes;
    private List<Showtime> showtimes;

    // 構造函數 (可選，但建議提供)
    public Movie() {}

    // Getter 和 Setter 方法
    // 使用 @JsonProperty 註解來確保 JSON 鍵與 Java 屬性名稱匹配
    @JsonProperty("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonProperty("title")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonProperty("runtime")
    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    @JsonProperty("introduction")
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    @JsonProperty("rating")
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    @JsonProperty("imagePath")
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @JsonProperty("actors")
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    @JsonProperty("director")
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    @JsonProperty("hallTypes")
    public List<String> getHallTypes() { return hallTypes; }
    public void setHallTypes(List<String> hallTypes) { this.hallTypes = hallTypes; }

    @JsonProperty("showtimes")
    public List<Showtime> getShowtimes() { return showtimes; }
    public void setShowtimes(List<Showtime> showtimes) { this.showtimes = showtimes; }

}
