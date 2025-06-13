import javax.swing.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.io.FileReader; 
import java.io.IOException;

// 引入 Bcrypt 庫
import org.mindrot.jbcrypt.BCrypt;


public class MemberLoginUI extends JPanel {

    private MainForm parentFrame;
    private JTextField accountField;
    private JPasswordField passwordField;

    public MemberLoginUI(MainForm parentFrame) {
        this.parentFrame = parentFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(0xF0F0F0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel accountLabel = new JLabel("帳號:");
        accountLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(accountLabel, gbc);

        accountField = new JTextField(20);
        accountField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        accountField.setToolTipText("請輸入電子郵件");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(accountField, gbc);

        JLabel passwordLabel = new JLabel("密碼:");
        passwordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        passwordField.setToolTipText("請輸入密碼");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        JButton loginButton = new JButton("登入");
        loginButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        linkPanel.setBackground(new Color(0xF0F0F0));

        JButton registerButton = new JButton("註冊");
        registerButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        registerButton.setForeground(Color.BLUE);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.showPanel("MemberRegister");
            } else {
                JOptionPane.showMessageDialog(MemberLoginUI.this, "無法切換到註冊頁面，主框架引用丟失。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        linkPanel.add(registerButton);

        JButton forgotPasswordButton = new JButton("忘記密碼?");
        forgotPasswordButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        forgotPasswordButton.setForeground(Color.BLUE);
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setFocusPainted(false);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // ***** START OF MODIFICATION *****
        forgotPasswordButton.addActionListener(e -> {
            // 創建 ForgotPasswordDialog 的實例，並將當前的主框架作為父視窗傳入
            // 假設 MainForm 是 JFrame 的子類或者可以作為 JFrame 傳入
            ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog(parentFrame);
            forgotPasswordDialog.setVisible(true); // 顯示對話框
        });
        // ***** END OF MODIFICATION *****
        linkPanel.add(forgotPasswordButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(linkPanel, gbc);
    }

    private void performLogin() {
        String account = accountField.getText().trim();
        String plainTextPassword = new String(passwordField.getPassword()); // 用戶輸入的明文密碼

        if (account.isEmpty() || plainTextPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "帳號和密碼不能為空！",
                    "登入錯誤",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Member> members = loadMembersFromJson("../shared_data/members.json");

        if (members == null) {
            JOptionPane.showMessageDialog(this,
                    "載入會員數據失敗，請聯繫系統管理員。",
                    "系統錯誤",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Member foundMember = null;
        for (Member member : members) {
            if (member.getAccount().equals(account)) {
                foundMember = member;
                break;
            }
        }

        if (foundMember != null) {
            // 找到了帳號，現在使用 Bcrypt 檢查密碼
            // foundMember.getPassword() 現在會返回雜湊後的密碼
            if (BCrypt.checkpw(plainTextPassword, foundMember.getPassword())) {
                // 登入成功
                SessionManager.login(foundMember.getAccount(), foundMember.getPassword()); // 更新 SessionManager 狀態

                JOptionPane.showMessageDialog(this,
                        "登入成功！歡迎 " + foundMember.getAccount(),
                        "登入成功",
                        JOptionPane.INFORMATION_MESSAGE);

                accountField.setText("");
                passwordField.setText("");

                if (parentFrame != null) {
                    parentFrame.updateLoginStatusUI();
                    parentFrame.showPanel("quick");
                }
            } else {
                // 密碼不正確
                JOptionPane.showMessageDialog(this,
                        "密碼不正確，請重新輸入。",
                        "登入失敗",
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText(""); // 清空密碼欄
            }
        } else {
            // 沒找到帳號
            JOptionPane.showMessageDialog(this,
                    "無此帳號，請檢查您的輸入或註冊新帳號。",
                    "登入失敗",
                    JOptionPane.ERROR_MESSAGE);
            accountField.setText("");
            passwordField.setText("");
        }
    }

    private List<Member> loadMembersFromJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();

        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            System.err.println("文件未找到: " + jsonFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "會員數據檔案未找到，請聯繫系統管理員。",
                    "系統錯誤",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            return mapper.readValue(jsonFile, new TypeReference<List<Member>>(){});
        } catch (MismatchedInputException e) {
            System.err.println("解析 members.json 失敗 (JSON 格式錯誤): " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "會員數據檔案格式錯誤，請聯繫系統管理員。",
                    "資料錯誤",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (IOException e) {
            System.err.println("讀取 members.json 失敗: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "讀取會員數據時發生錯誤，請聯繫系統管理員。",
                    "系統錯誤",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}