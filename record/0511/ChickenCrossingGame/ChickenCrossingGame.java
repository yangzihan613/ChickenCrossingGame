import javax.swing.*;

public class ChickenCrossingGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("小雞過馬路");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GamePanel gamePanel = new GamePanel(frame);
            frame.add(gamePanel);
            frame.addKeyListener(gamePanel);

            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);

            gamePanel.start();
        });
    }
}