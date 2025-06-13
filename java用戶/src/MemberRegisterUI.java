import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream; // 新增：引入 FileInputStream
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // 用於美化輸出

// 1. 引入 Bcrypt 庫
import org.mindrot.jbcrypt.BCrypt;

public class MemberRegisterUI extends JPanel {

    private JTextField yearField;
    private JTextField monthField;
    private JTextField dayField;
    private JTextField phoneNumberField; // 新增：電話號碼輸入框

    // 定義儲存會員資料的 JSON 檔案路徑
    private static final String MEMBERS_DB_FILE = "../shared_data/members.json"; // 修正為 shared_data

    // 用於驗證 Gmail 格式的正規表達式
    private static final String GMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";
    private static final Pattern GMAIL_PATTERN = Pattern.compile(GMAIL_REGEX);

    // 用於驗證台灣手機號碼格式的正規表達式 (以09開頭，共10位數字)
    private static final String PHONE_NUMBER_REGEX = "^09\\d{8}$";
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);


    public MemberRegisterUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(0xF0F0F0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 帳號 (電子郵件)
        JLabel accountLabel = new JLabel("帳號:");
        accountLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(accountLabel, gbc);

        JTextField accountField = new JTextField(20);
        accountField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        accountField.setToolTipText("請輸入電子郵件");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(accountField, gbc);

        // 密碼
        JLabel passwordLabel = new JLabel("密碼:");
        passwordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        passwordField.setToolTipText("請輸入密碼");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // 再次確認密碼
        JLabel confirmPasswordLabel = new JLabel("再次確認密碼:");
        confirmPasswordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(confirmPasswordLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        confirmPasswordField.setToolTipText("請再次確認密碼");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);

        // 手機號碼 (新增此部分)
        JLabel phoneNumberLabel = new JLabel("手機號碼:");
        phoneNumberLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3; // 將 gridY 調整
        gbc.anchor = GridBagConstraints.EAST;
        add(phoneNumberLabel, gbc);

        phoneNumberField = new JTextField(20); // 初始化電話號碼輸入框
        phoneNumberField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        phoneNumberField.setToolTipText("請輸入手機號碼 (例如: 09xxxxxxxx)");
        gbc.gridx = 1;
        gbc.gridy = 3; // 將 gridY 調整
        gbc.anchor = GridBagConstraints.WEST;
        add(phoneNumberField, gbc);

        // 出生日期 (年、月、日 三個輸入框) - 調整 gridY
        JLabel dobLabel = new JLabel("出生日期:");
        dobLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4; // 將 gridY 調整
        gbc.anchor = GridBagConstraints.EAST;
        add(dobLabel, gbc);

        JPanel dateInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateInputPanel.setBackground(new Color(0xF0F0F0));

        yearField = new JTextField(4);
        yearField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        monthField = new JTextField(2);
        monthField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        dayField = new JTextField(2);
        dayField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));

        dateInputPanel.add(yearField);
        dateInputPanel.add(new JLabel("年"));
        dateInputPanel.add(monthField);
        dateInputPanel.add(new JLabel("月"));
        dateInputPanel.add(new JLabel("日")); // 將 dayField 放在最後以符合邏輯順序
        dateInputPanel.add(dayField); // 這裡你把日放錯位置了，我把它移到 JLabel "日" 的後面

        gbc.gridx = 1;
        gbc.gridy = 4; // 將 gridY 調整
        gbc.anchor = GridBagConstraints.WEST;
        add(dateInputPanel, gbc);

        // 註冊按鈕 - 調整 gridY
        JButton registerButton = new JButton("註冊");
        registerButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        registerButton.setPreferredSize(new Dimension(120, 35));
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText().trim(); // 使用 trim() 移除前後空白
                String plainTextPassword = new String(passwordField.getPassword()); // 用戶輸入的明文密碼
                String confirmPassword = new String(confirmPasswordField.getPassword());
                String phoneNumber = phoneNumberField.getText().trim(); // 新增：獲取電話號碼

                String yearStr = yearField.getText().trim();
                String monthStr = monthField.getText().trim();
                String dayStr = dayField.getText().trim();

                if (account.isEmpty() || plainTextPassword.isEmpty() || confirmPassword.isEmpty() ||
                    phoneNumber.isEmpty() || // 新增：檢查電話號碼是否為空
                    yearStr.isEmpty() || monthStr.isEmpty() || dayStr.isEmpty()) {
                    JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                "所有欄位都必須填寫！",
                                "註冊錯誤",
                                JOptionPane.WARNING_MESSAGE);
                } else if (!plainTextPassword.equals(confirmPassword)) { // 比對明文密碼
                    JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                "兩次密碼輸入不一致！",
                                "註冊錯誤",
                                JOptionPane.WARNING_MESSAGE);
                } else if (!isValidGmail(account)) {
                    JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                "帳號必須是有效的 @gmail.com 電子郵件地址！",
                                "註冊錯誤",
                                JOptionPane.WARNING_MESSAGE);
                } else if (!isValidPhoneNumber(phoneNumber)) { // 新增：驗證電話號碼格式
                    JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                "手機號碼格式不正確！請輸入10位數字，以09開頭。",
                                "註冊錯誤",
                                JOptionPane.WARNING_MESSAGE);
                }
                else {
                    try {
                        int year = Integer.parseInt(yearStr);
                        int month = Integer.parseInt(monthStr);
                        int day = Integer.parseInt(dayStr);

                        // 簡易日期有效性檢查
                        if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR) || // 今年不能超過當前年份
                            month < 1 || month > 12 ||
                            day < 1 || day > 31) {
                            JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                            "請輸入有效的出生日期 (年、月、日)！",
                                            "註冊錯誤",
                                            JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        // 更嚴格的日期驗證 (例如 2月沒有30日)
                        String dobString = String.format("%04d-%02d-%02d", year, month, day);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false); // 嚴格模式，會捕獲無效日期 (例如 2月30日)
                        Date dob = sdf.parse(dobString); // 如果日期無效會拋出 ParseException

                        if (dob.after(new Date())) { // 出生日期不能在今天之後
                            JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                            "出生日期不能在今天之後！",
                                            "註冊錯誤",
                                            JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // 檢查帳號是否已存在
                        if (isAccountExist(account)) {
                            JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                            "此帳號 (電子郵件) 已被註冊！",
                                            "註冊失敗",
                                            JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // 新增：檢查電話號碼是否已存在
                        if (isPhoneNumberExist(phoneNumber)) {
                            JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                            "此手機號碼已被註冊！",
                                            "註冊失敗",
                                            JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // 2. 對明文密碼進行雜湊處理
                        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));

                        // 創建 Member 物件，傳入雜湊後的密碼和電話號碼
                        Member newMember = new Member(account, hashedPassword, dobString, phoneNumber); // 新增 phoneNumber

                        // 儲存會員資料到 JSON 檔案
                        saveMember(newMember);

                        JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                "帳號: " + account + "\n手機號碼: " + phoneNumber + "\n出生日期: " + dobString, // 顯示手機號碼
                                "註冊成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        // 清空所有輸入欄位
                        accountField.setText("");
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                        phoneNumberField.setText(""); // 清空電話號碼欄位
                        yearField.setText("");
                        monthField.setText("");
                        dayField.setText("");
                        
                        // 註冊成功後，可以返回登入頁面或主頁
                        // 假設您有一個主框架的引用，並且可以切換面板
                        // 例如：
                        // if (getParent() instanceof MainForm) {
                        //     ((MainForm) getParent()).showPanel("MemberLogin");
                        // }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                        "出生日期 (年、月、日) 必須是數字！",
                                        "註冊錯誤",
                                        JOptionPane.WARNING_MESSAGE);
                    } catch (java.text.ParseException ex) {
                        JOptionPane.showMessageDialog(MemberRegisterUI.this,
                                        "請輸入有效的出生日期！ (例如: 2月沒有30天)",
                                        "註冊錯誤",
                                        JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5; // 將 gridY 調整
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);
    }

    /**
     * 驗證帳號是否為有效的 @gmail.com 格式。
     */
    private boolean isValidGmail(String email) {
        Matcher matcher = GMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * 驗證手機號碼是否符合台灣格式 (09開頭，共10位數字)。
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        Matcher matcher = PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        return matcher.matches();
    }

    /**
     * 從 JSON 檔案載入所有會員資料。
     * 如果檔案不存在或讀取失敗，返回空列表。
     */
    private List<Member> loadMembers() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(MEMBERS_DB_FILE); // 這裡已經正確地使用 File 類別

        // 檢查檔案是否存在且不為空
        if (!file.exists() || file.length() == 0) {
            System.out.println("會員資料檔案不存在或為空，將創建新檔案或返回空列表。路徑: " + file.getAbsolutePath());
            System.out.println("當前工作目錄: " + System.getProperty("user.dir")); // 打印當前工作目錄幫助調試
            return new ArrayList<>();
        }
        
        try (FileInputStream fis = new FileInputStream(file)) { // 使用 FileInputStream 讀取檔案
            return mapper.readValue(fis, new TypeReference<List<Member>>() {});
        } catch (IOException e) {
            System.err.println("讀取會員資料失敗: " + e.getMessage());
            e.printStackTrace();
            // 在這裡可以選擇彈出一個錯誤訊息給用戶
            JOptionPane.showMessageDialog(this, "讀取會員資料時發生錯誤。", "錯誤", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>(); // 返回空列表，不阻止程序運行
        }
    }

    /**
     * 檢查帳號是否已存在。
     */
    private boolean isAccountExist(String account) {
        List<Member> members = loadMembers();
        for (Member member : members) {
            if (member.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 新增：檢查電話號碼是否已存在。
     */
    private boolean isPhoneNumberExist(String phoneNumber) {
        List<Member> members = loadMembers();
        for (Member member : members) {
            // 檢查 member.getPhoneNumber() 是否存在且與傳入的 phoneNumber 相同
            // 注意：如果您的舊數據沒有 phoneNumber 欄位，這裡可能需要額外處理 NullPointerException
            if (member.getPhoneNumber() != null && member.getPhoneNumber().equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 將新的會員資料儲存到 JSON 檔案中。
     * 會讀取現有資料，加入新會員，再寫回檔案。
     */
    private void saveMember(Member newMember) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化輸出，讓 JSON 檔案可讀性更好

        List<Member> members = loadMembers(); // 載入現有會員資料
        members.add(newMember); // 添加新會員

        try {
            mapper.writeValue(new File(MEMBERS_DB_FILE), members);
            System.out.println("會員 '" + newMember.getAccount() + "' 資料已儲存到 " + MEMBERS_DB_FILE);
        } catch (IOException e) {
            System.err.println("儲存會員資料失敗: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "儲存會員資料時發生錯誤！",
                    "儲存失敗",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}