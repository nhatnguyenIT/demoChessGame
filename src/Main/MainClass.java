package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainClass {
    public static void main(String[] args){
        JFrame window = new JFrame("Chess Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setPreferredSize(new Dimension(1300, 900)); // kich thuoc cua so
        menuPanel.setBackground(new Color(131, 86, 60));

        window.add(menuPanel);
        window.pack();
        window.setLocationRelativeTo((Component)null);
        window.setVisible(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Đặt padding giữa các thành phần

        JButton playWithAIButton = new JButton("Play With Computer");
        JButton twoPlayerButton = new JButton("Two Player");
        JButton historyButton = new JButton("History");
        // Thiết lập kích thước cho các nút
        Dimension buttonSize = new Dimension(400, 50); //kich thuoc nut
        playWithAIButton.setPreferredSize(buttonSize);
        playWithAIButton.setBackground(new Color(255, 255, 153));
        twoPlayerButton.setPreferredSize(buttonSize);
        twoPlayerButton.setBackground(new Color(255, 255, 153));
        historyButton.setPreferredSize(buttonSize);
        historyButton.setBackground(new Color(255, 255, 153));

        playWithAIButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code để bắt sự kiện khi người dùng click vào nút "Chơi với máy
            }
        });

        twoPlayerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code để bắt sự kiện khi người dùng click vào nút "Hai người chơi"
                JFrame windowgame = new JFrame("Two Player");
                windowgame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                windowgame.setResizable(true);
                GamePanel gp = new GamePanel();
                windowgame.add(gp);
                windowgame.pack();
                windowgame.setLocationRelativeTo((Component)null);
                windowgame.setVisible(true);
                gp.launchGame();
            }
        });

        historyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code để bắt sự kiện khi người dùng click vào nút "Lịch sử"
            }
        });

        // Đặt nút các nút trong menu vào chính giữa cửa sổ
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        menuPanel.add(playWithAIButton, gbc);
        gbc.gridy = 1;
        menuPanel.add(twoPlayerButton, gbc);
        gbc.gridy = 2;
        menuPanel.add(historyButton, gbc);
    }
}
