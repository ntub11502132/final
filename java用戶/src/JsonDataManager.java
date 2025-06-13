import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JsonDataManager {

    private static final String JSON_FILE_PATH = "movies.json";
    private static List<Movie> movies;
    private static ObjectMapper objectMapper = new ObjectMapper();

    // 靜態區塊，在類別載入時讀取 JSON 檔案
    static {
        loadMoviesFromJson();
    }

    private static void loadMoviesFromJson() {
        try {
            File file = new File(JSON_FILE_PATH);
            if (!file.exists()) {
                System.out.println("JSON file not found, creating a dummy file.");
                // 如果檔案不存在，可以創建一個空的或包含預設數據的檔案
                movies = new ArrayList<>();
                // 這裡可以添加一些預設電影數據，或者確保在應用程式啟動前手動創建 movies.json
                // 例如: generateAndSaveDummyData();
                saveMoviesToJson(); // 保存空列表或預設數據
                System.out.println("Dummy movies.json created.");
            } else {
                movies = objectMapper.readValue(file, new TypeReference<List<Movie>>() {});
                System.out.println("Movies loaded from " + JSON_FILE_PATH);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading movies from JSON: " + e.getMessage());
            movies = new ArrayList<>(); // 載入失敗則初始化為空列表
        }
    }

    // 將當前電影數據寫回 JSON 檔案
    public static void saveMoviesToJson() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_FILE_PATH), movies);
            System.out.println("Movies saved to " + JSON_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving movies to JSON: " + e.getMessage());
        }
    }

    // 查詢方法

    public static List<Movie> getAllMovies() {
        return new ArrayList<>(movies); // 返回副本防止外部修改
    }

    public static Movie getMovieById(String movieId) {
        return movies.stream()
                     .filter(m -> m.getId().equals(movieId))
                     .findFirst()
                     .orElse(null);
    }

    public static List<String> getAvailableHallTypes(String movieId) {
        Movie movie = getMovieById(movieId);
        if (movie != null) {
            return movie.getHallTypes();
        }
        return new ArrayList<>();
    }

    public static List<String> getAvailableDates(String movieId, String hallType) {
        Movie movie = getMovieById(movieId);
        if (movie != null) {
            return movie.getShowtimes().stream()
                        .filter(s -> s.getHallType().equals(hallType))
                        .map(Showtime::getDate)
                        .distinct()
                        .sorted() // 確保日期有序
                        .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<String> getAvailableTimes(String movieId, String hallType, String date) {
        Movie movie = getMovieById(movieId);
        if (movie != null) {
            return movie.getShowtimes().stream()
                        .filter(s -> s.getHallType().equals(hallType) && s.getDate().equals(date))
                        .map(Showtime::getTime)
                        .distinct()
                        .sorted() // 確保時間有序
                        .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static Showtime getShowtimeDetails(String movieId, String hallType, String date, String time) {
        Movie movie = getMovieById(movieId);
        if (movie != null) {
            return movie.getShowtimes().stream()
                        .filter(s -> s.getHallType().equals(hallType) && s.getDate().equals(date) && s.getTime().equals(time))
                        .findFirst()
                        .orElse(null);
        }
        return null;
    }

    // 更新座位狀態並保存回 JSON 檔案
    public static boolean updateSeatOccupancy(String showtimeId, List<String> seatNumbers, boolean isOccupied) {
        boolean updated = false;
        for (Movie movie : movies) {
            for (Showtime showtime : movie.getShowtimes()) {
                if (showtime.getShowtimeId().equals(showtimeId)) {
                    for (String seatNum : seatNumbers) {
                        for (Seat seat : showtime.getSeats()) {
                            if (seat.getNumber().equals(seatNum)) {
                                seat.setOccupied(isOccupied);
                                updated = true;
                            }
                        }
                    }
                    if (updated) { // 如果有座位被更新，則保存並返回
                        saveMoviesToJson();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 輔助方法：生成座位列表 (如果 JSON 中沒有預設座位，需要它來初始化)
    public static List<Seat> generateSeats(int rows, int cols) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= cols; j++) {
                Seat seat = new Seat();
                seat.setNumber(String.valueOf(rowChar) + j);
                seat.setOccupied(false); // 預設未佔用
                seats.add(seat);
            }
        }
        return seats;
    }
}