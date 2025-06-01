import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChickenCrossingGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("小雞過馬路遊戲");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        GamePanel gamePanel = new GamePanel(frame);
        frame.add(gamePanel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                gamePanel.requestFocusInWindow();
                gamePanel.start(); // 在此處啟動遊戲
            }
        });

        if (gd.isFullScreenSupported()) {
            frame.setUndecorated(true);
            gd.setFullScreenWindow(frame);
        } else {
            System.err.println("全螢幕模式不受支援，將使用最大化視窗。");
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setMinimumSize(new Dimension(800, 600));
            frame.setLocationRelativeTo(null);
        }

        frame.setVisible(true);
    }
}