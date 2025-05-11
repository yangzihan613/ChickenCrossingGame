import javax.swing.*;


    public class ChickenCrossingGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String playerName = JOptionPane.showInputDialog(null, "請輸入你的名稱：", "歡迎", JOptionPane.PLAIN_MESSAGE);
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "玩家";
            }

            JFrame frame = new JFrame("小雞過馬路");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GamePanel gamePanel = new GamePanel(frame, playerName); // 傳入名稱
            frame.add(gamePanel);
            frame.addKeyListener(gamePanel);

            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);

            gamePanel.start();
        });
    }
}
