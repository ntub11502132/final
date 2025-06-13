import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainForm extends JFrame {

    private JPanel contentPanel; // The blue panel where future classes will display

    public MainForm() {
        setTitle("Form1 - 營運人員"); // Set the window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setSize(1200, 800); // 調整視窗大小以適應表格內容
        setLocationRelativeTo(null); // Center the window on the screen

        // Set up the main layout of the frame
        setLayout(new BorderLayout());

        // --- Top Panel for Buttons and Title ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 230, 230)); // Light grey background for the top panel

        JLabel titleLabel = new JLabel("營運人員");
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24)); // Set font for the title
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0)); // Add some padding
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Panel for the two buttons on the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Right alignment, with gaps
        buttonPanel.setBackground(new Color(230, 230, 230)); // Match top panel background

        JButton btnTicketQuery = new JButton("訂票狀態"); // Button 1
        customizeButton(btnTicketQuery); // Apply custom styling
        btnTicketQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action for "訂票狀態" button
                System.out.println("訂票狀態 button clicked! 顯示訂票狀態頁面。");
                // 這裡傳遞 MainForm.this 給 TicketStatusPanel
                displayPanel(new TicketStatusPanel());
            }
        });
        buttonPanel.add(btnTicketQuery);

        JButton btnMovieModify = new JButton("電影修改"); // Button 2
        customizeButton(btnMovieModify); // Apply custom styling
        btnMovieModify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action for "電影修改" button
                System.out.println("電影修改 button clicked! 顯示電影修改頁面。");
                displayPanel(new MovieManagementPanel());
            }
        });
        buttonPanel.add(btnMovieModify);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH); // Add the top panel to the NORTH of the frame

        // --- Content Panel (the blue area) ---
        contentPanel = new JPanel();
        contentPanel.setBackground(new Color(173, 209, 232)); // Light blue color as in the screenshot
        contentPanel.setLayout(new BorderLayout()); // Use BorderLayout for the content panel as well

        add(contentPanel, BorderLayout.CENTER); // Add the content panel to the CENTER of the frame
    }

    // Helper method to customize button appearance
    private void customizeButton(JButton button) {
        button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        button.setBackground(new Color(220, 220, 220)); // Light grey button background
        button.setForeground(Color.BLACK); // Black text
        button.setFocusPainted(false); // Remove focus border
        button.setBorder(BorderFactory.createRaisedBevelBorder()); // Beveled border
        button.setPreferredSize(new Dimension(120, 35)); // Set preferred size
    }

    /**
     * This method is crucial for your requirement:
     * It removes existing components from the contentPanel and adds the newPanel.
     * @param newPanel The JPanel you want to display in the blue content area.
     */
    public void displayPanel(JPanel newPanel) {
        contentPanel.removeAll(); // Clear existing content
        contentPanel.add(newPanel, BorderLayout.CENTER); // Add the new panel
        contentPanel.revalidate(); // Re-layout the components
        contentPanel.repaint();   // Repaint the panel
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainForm form = new MainForm();
                form.setVisible(true);
            }
        });
    }
}
