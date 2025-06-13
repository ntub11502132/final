public class SessionManager {
    private static String loggedInUserId = null; // 儲存登入的會員ID
    private static String loggedInUserName = null; // 儲存登入的會員名稱 (可選)

    public static void login(String userId, String userName) {
        loggedInUserId = userId;
        loggedInUserName = userName;
    }

    public static void logout() {
        loggedInUserId = null;
        loggedInUserName = null;
    }

    public static boolean isLoggedIn() {
        return loggedInUserId != null;
    }

    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static String getLoggedInUserName() {
        return loggedInUserName;
    }
}