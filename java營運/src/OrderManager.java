// File: OrderManager.java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    // 定義 JSON 檔案的路徑，使用與其他數據檔案一致的相對路徑
    private static final String ORDERS_FILE = "../shared_data/orders.json";
    private ObjectMapper objectMapper;

    public OrderManager() {
        objectMapper = new ObjectMapper();
        // 美化輸出的 JSON 格式，使其更易讀
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // --- 添加診斷：打印當前工作目錄 ---
        System.out.println("OrderManager initialized. Current working directory: " + System.getProperty("user.dir"));

        File file = new File(ORDERS_FILE);
        // 確保父目錄存在，如果不存在則創建
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs(); // 創建所有必要的父目錄
            if (created) {
                System.out.println("Created parent directory for orders file: " + parentDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create parent directory for orders file: " + parentDir.getAbsolutePath());
            }
        }

        // 如果檔案不存在，則創建一個帶有空 JSON 陣列的新檔案
        if (!file.exists()) {
            try {
                // 寫入一個空的 JSON 陣列到檔案中，確保檔案是有效的 JSON 格式
                objectMapper.writeValue(file, new ArrayList<Order>());
                System.out.println("Orders file '" + ORDERS_FILE + "' created successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating orders file '" + ORDERS_FILE + "': " + e.getMessage());
                e.printStackTrace(); // 打印堆疊追蹤，以便調試
            }
        } else {
            System.out.println("Orders file '" + ORDERS_FILE + "' already exists at: " + file.getAbsolutePath());
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
                System.out.println("[Load] Order file does not exist: " + file.getAbsolutePath() + ". Returning empty list.");
                return new ArrayList<>(); // 檔案不存在，返回空列表
            }
            if (file.length() == 0) {
                // 如果檔案為空，可以認為它是一個空的 JSON 陣列 []
                System.out.println("[Load] Order file is empty (0 bytes): " + file.getAbsolutePath() + ". Returning empty list.");
                // 為了避免 MismatchedInputException，如果檔案確實是空的，我們直接返回空列表
                // 但如果想更嚴格地處理，可以嘗試寫入一個空的[]並重試讀取
                return new ArrayList<>();
            }
            // 讀取 JSON 檔案並反序列化為 List<Order>
            System.out.println("[Load] Attempting to read orders from: " + file.getAbsolutePath());
            List<Order> loadedOrders = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
            System.out.println("[Load] Successfully loaded " + loadedOrders.size() + " orders.");
            return loadedOrders;
        } catch (MismatchedInputException e) {
            // 當 JSON 格式不符合預期（例如，不是一個有效的列表，可能是單個對象或空字串）時捕獲
            System.err.println("[Load ERROR] Error parsing orders from " + ORDERS_FILE + " (MismatchedInputException): " + e.getMessage());
            System.err.println("[Load ERROR] Problematic file path: " + file.getAbsolutePath());
            System.err.println("[Load ERROR] Check if orders.json content is valid JSON array (e.g., [], or [{}...]).");
            e.printStackTrace();
            return new ArrayList<>(); // 返回空列表，表示讀取失敗或檔案內容無效
        } catch (IOException e) {
            System.err.println("[Load ERROR] Error loading orders from " + ORDERS_FILE + ": " + e.getMessage());
            System.err.println("[Load ERROR] Problematic file path: " + file.getAbsolutePath());
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
            System.out.println("[Save] Orders saved successfully to " + ORDERS_FILE + " at: " + new File(ORDERS_FILE).getAbsolutePath() + ". Total orders: " + orders.size());
            return true;
        } catch (IOException e) {
            System.err.println("[Save ERROR] Error saving orders to " + ORDERS_FILE + ": " + e.getMessage());
            System.err.println("[Save ERROR] Problematic file path: " + new File(ORDERS_FILE).getAbsolutePath());
            e.printStackTrace(); // 打印堆疊追蹤，以便調試
            return false;
        }
    }

    /**
     * 添加一個新訂單到訂單列表中並保存到檔案。
     * @param newOrder 要添加的訂單。
     * @return 如果成功添加並保存則返回 true，否 otherwise 返回 false。
     */
    public boolean addOrder(Order newOrder) {
        System.out.println("[Add Order] Attempting to add new order: " + newOrder.getOrderId() + " for user: " + newOrder.getUsername());
        List<Order> orders = loadOrders(); // 載入現有訂單
        orders.add(newOrder); // 添加新訂單
        boolean success = saveAllOrders(orders); // 保存更新後的訂單列表 (只保存一次)
        if (success) {
            System.out.println("[Add Order] Order '" + newOrder.getOrderId() + "' successfully added and saved.");
        } else {
            System.err.println("[Add Order ERROR] Failed to add and save order '" + newOrder.getOrderId() + "'.");
        }
        return success;
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
        System.out.println("[Get Orders By Username] Found " + userOrders.size() + " orders for user: " + username);
        return userOrders;
    }

    /**
     * 更新指定訂單的狀態。
     * @param orderId 要更新的訂單編號。
     * @param newStatus 新的訂單狀態。
     * @return 如果成功更新則返回 true，否則返回 false。
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        System.out.println("[Update Order Status] Attempting to update order ID: " + orderId + " to status: " + newStatus);
        List<Order> allOrders = loadOrders();
        boolean updated = false;
        for (Order order : allOrders) {
            if (order.getOrderId().equals(orderId)) {
                order.setOrderStatus(newStatus);
                updated = true;
                break; // 找到並更新後即可退出循環
            }
        }
        if (updated) {
            boolean success = saveAllOrders(allOrders); // 如果有更新，則保存所有訂單
            if (success) {
                System.out.println("[Update Order Status] Order ID: " + orderId + " status updated successfully.");
            } else {
                System.err.println("[Update Order Status ERROR] Failed to save updated status for order ID: " + orderId + ".");
            }
            return success;
        }
        System.out.println("[Update Order Status] Order ID: " + orderId + " not found or not updated.");
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
                System.out.println("[Get Order By ID] Found order: " + orderId);
                return order;
            }
        }
        System.out.println("[Get Order By ID] Order ID: " + orderId + " not found.");
        return null;
    }
}