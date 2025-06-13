import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt; // 引入 Bcrypt 庫

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // 用於美化 JSON 輸出

public class ForgotPasswordDialog extends JDialog {

    private JTextField accountField;
    private JTextField phoneNumberField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private static final String MEMBERS_FILE_PATH = "../shared_data/members.json"; // 會員數據檔案路徑

    public ForgotPasswordDialog(JFrame parentFrame) {
        super(parentFrame, "忘記密碼", true); // 設置為模態對話框
        initializeUI();
        pack(); // 自動調整大小
        setLocationRelativeTo(parentFrame); // 位於父視窗中央
        setResizable(false); // 不允許調整大小
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 248, 255)); // 淡藍色背景

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // 組件間距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 提示訊息
        JLabel instructionLabel = new JLabel("請輸入您的帳號及手機號碼以重設密碼。", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        instructionLabel.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(instructionLabel, gbc);

        // 帳號
        JLabel accountLabel = new JLabel("帳號 (電子郵件):");
        accountLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(accountLabel, gbc);

        accountField = new JTextField(20);
        accountField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(accountField, gbc);

        // 手機號碼 (身份驗證資訊)
        JLabel phoneNumberLabel = new JLabel("手機號碼:");
        phoneNumberLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(phoneNumberLabel, gbc);

        phoneNumberField = new JTextField(20);
        phoneNumberField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(phoneNumberField, gbc);

        // 新密碼
        JLabel newPasswordLabel = new JLabel("新密碼:");
        newPasswordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(newPasswordLabel, gbc);

        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(newPasswordField, gbc);

        // 確認新密碼
        JLabel confirmPasswordLabel = new JLabel("確認新密碼:");
        confirmPasswordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(confirmPasswordField, gbc);

        // 確認按鈕
        JButton confirmButton = new JButton("確認重設");
        confirmButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        confirmButton.setBackground(new Color(0x4CAF50)); // 更淺的綠色 (原 0x28A745 較深)
        confirmButton.setForeground(Color.BLACK); // 確保文字為白色
        confirmButton.setFocusPainted(false);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPassword();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(confirmButton, gbc);

        add(panel);
    }

    private void resetPassword() {
        String account = accountField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (account.isEmpty() || phoneNumber.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有欄位都不能為空。", "輸入錯誤", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "新密碼與確認密碼不符。", "密碼不匹配", JOptionPane.WARNING_MESSAGE);
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            return;
        }

        // 載入會員數據
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(MEMBERS_FILE_PATH);
        List<Member> members;

        try {
            members = mapper.readValue(jsonFile, new TypeReference<List<Member>>(){});
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "無法讀取會員數據檔案。", "錯誤", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        Member foundMember = null;
        for (Member member : members) {
            // 檢查帳號和手機號碼
            if (member.getAccount().equals(account) && member.getPhoneNumber().equals(phoneNumber)) {
                foundMember = member;
                break;
            }
        }

        if (foundMember != null) {
            // ***** START OF NEW PASSWORD CHECK *****
            // 對新密碼進行雜湊，並與舊密碼的雜湊值進行比較
            if (BCrypt.checkpw(newPass, foundMember.getPassword())) {
                JOptionPane.showMessageDialog(this, "新密碼不能與舊密碼相同，請設定不同的密碼。", "密碼重設失敗", JOptionPane.WARNING_MESSAGE);
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                return;
            }
            // ***** END OF NEW PASSWORD CHECK *****

            // 更新密碼
            String hashedNewPassword = BCrypt.hashpw(newPass, BCrypt.gensalt()); // 對新密碼進行雜湊
            foundMember.setPassword(hashedNewPassword); // 更新 Member 物件的密碼

            try {
                // 將更新後的會員列表寫回 JSON 檔案
                mapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化 JSON 輸出
                mapper.writeValue(jsonFile, members);
                JOptionPane.showMessageDialog(this, "密碼已成功重設！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 關閉對話框
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "寫入會員數據失敗，請聯繫系統管理員。", "錯誤", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "帳號或手機號碼不正確。", "驗證失敗", JOptionPane.WARNING_MESSAGE);
        }
    }
}