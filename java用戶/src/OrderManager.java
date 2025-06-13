import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException; // 引入此異常類型以便更精確地處理
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    // 定義 JSON 檔案的路徑
    private static final String ORDERS_FILE = "../shared_data/orders.json";
    private ObjectMapper objectMapper;

    public OrderManager() {
        objectMapper = new ObjectMapper();
        // 美化輸出的 JSON 格式，使其更易讀
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 在建構子中檢查 orders.json 檔案是否存在，如果不存在則創建一個空的 JSON 檔案
        File file = new File(ORDERS_FILE);
        // 確保父目錄存在，如果不存在則創建
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // 創建所有必要的父目錄
        }

        if (!file.exists()) {
            try {
                // 寫入一個空的 JSON 陣列到檔案中，確保檔案是有效的 JSON 格式
                objectMapper.writeValue(file, new ArrayList<Order>());
                System.out.println(ORDERS_FILE + " created successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating " + ORDERS_FILE + ": " + e.getMessage());
                e.printStackTrace(); // 打印堆疊追蹤，以便調試
            }
        }
    }

    /**
     * 從 JSON 檔案載入所有訂單。
     * 如果檔案不存在、為空或格式錯誤，則返回一個空的列表。
     * @return 訂單列表。
     */
    public List<Order> loadOrders() {
        File file = new File(ORDERS_FILE);
        try {
            if (!file.exists()) {
                System.out.println(ORDERS_FILE + " does not exist. Returning empty list.");
                return new ArrayList<>(); // 檔案不存在，返回空列表
            }
            if (file.length() == 0) {
                // 如果檔案為空，可以認為它是一個空的 JSON 陣列 []
                System.out.println(ORDERS_FILE + " is empty. Returning empty list.");
                return new ArrayList<>();
            }
            // 讀取 JSON 檔案並反序列化為 List<Order>
            return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
        } catch (MismatchedInputException e) {
            // 當 JSON 格式不符合預期（例如，不是一個有效的列表）時捕獲
            System.err.println("Error parsing orders from " + ORDERS_FILE + " (MismatchedInputException): " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // 返回空列表，表示讀取失敗或檔案內容無效
        } catch (IOException e) {
            System.err.println("Error loading orders from " + ORDERS_FILE + ": " + e.getMessage());
            e.printStackTrace(); // 打印堆疊追蹤，以便調試
        }
        return new ArrayList<>(); // 發生其他 IOException 時，返回空列表
    }

    /**
     * 將所有訂單保存到 JSON 檔案中。
     * @param orders 要保存的訂單列表。
     * @return 如果成功保存則返回 true，否則返回 false。
     */
    private boolean saveAllOrders(List<Order> orders) {
        try {
            objectMapper.writeValue(new File(ORDERS_FILE), orders);
            System.out.println("Orders saved to " + ORDERS_FILE + " at: " + new File(ORDERS_FILE).getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Error saving orders to " + ORDERS_FILE + ": " + e.getMessage());
            e.printStackTrace(); // 打印堆疊追蹤，以便調試
            return false;
        }
    }

    /**
     * 添加一個新訂單到訂單列表中並保存到檔案。
     * @param newOrder 要添加的訂單。
     * @return 如果成功添加並保存則返回 true，否則返回 false。
     */
    public boolean addOrder(Order newOrder) {
        List<Order> orders = loadOrders(); // 載入現有訂單
        orders.add(newOrder); // 添加新訂單
        return saveAllOrders(orders); // 保存更新後的訂單列表 (只保存一次)
    }

    /**
     * 根據用戶名獲取訂單列表。
     * @param username 用戶名。
     * @return 該用戶的所有訂單列表。
     */
    public List<Order> getOrdersByUsername(String username) {
        List<Order> allOrders = loadOrders();
        List<Order> userOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getUsername().equals(username)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    /**
     * 更新指定訂單的狀態，現在使用 orderId 進行查找。
     * @param orderId 要更新的訂單編號。
     * @param newStatus 新的訂單狀態。
     * @return 如果成功更新則返回 true，否則返回 false。
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        List<Order> allOrders = loadOrders();
        boolean updated = false;
        for (Order order : allOrders) {
            if (order.getOrderId().equals(orderId)) { // 只需比對 orderId
                order.setOrderStatus(newStatus);
                updated = true;
                break; // 找到並更新後即可退出循環
            }
        }
        if (updated) {
            return saveAllOrders(allOrders); // 如果有更新，則保存所有訂單
        }
        return false; // 沒有找到匹配的訂單，或沒有更新
    }

    /**
     * 根據訂單 ID 獲取單個訂單。
     * @param orderId 訂單 ID。
     * @return 匹配的訂單，如果沒有找到則返回 null。
     */
    public Order getOrderById(String orderId) {
        List<Order> allOrders = loadOrders();
        for (Order order : allOrders) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }
}