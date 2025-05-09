import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChickenCrossingGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int chickenX = 200, chickenY = 270; // 調整初始 Y 軸位置

    private int car1X = 500, car1Y = 170; // 紅車（右→左）
    private int car2X = -100, car2Y = 210; // 藍車（左→右），稍微調整初始 Y 軸

    private int objectHeight = 40; // 設定物件高度
    private int chickenWidth = 40; // 同時調整寬度，讓小雞看起來更方正
    private int carWidth = 60; // 車子寬度可以比高度大一些
    private int carSpeed = 5;
    private int timeElapsed = 0;
    private int level = 1;

    private boolean gameOver = false;
    private JFrame frame; // 將 JFrame 設為類別變數，方便之後操作

    public ChickenCrossingGame() {
        // 創建 JFrame
        frame = new JFrame("小雞過馬路");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);

        // 設置全螢幕
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(frame); // 將 JFrame 設置為全螢幕視窗

        // 取得螢幕的寬度和高度，用於之後的繪製和邊界判斷
        int screenWidth = frame.getWidth();
        int screenHeight = frame.getHeight();

        // 調整小雞的初始位置，讓它在全螢幕下看起來更合理
        chickenX = screenWidth / 2 - chickenWidth / 2;
        chickenY = (int) (screenHeight * 0.8); // 保持在底部附近

        // 調整車道的 Y 軸位置，讓車子看起來在路上
        car1Y = (int) (screenHeight * 0.3);
        car2Y = (int) (screenHeight * 0.45);

        timer = new Timer(30, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 馬路 1 (與紅車切齊)
        g.setColor(Color.LIGHT_GRAY); // 你可以選擇你想要的顏色
        g.fillRect(0, car1Y, width, objectHeight);

        // 馬路 2 (與藍車切齊)
        g.setColor(Color.DARK_GRAY); // 你可以選擇你想要的顏色
        g.fillRect(0, car2Y, width, objectHeight);

        // 小雞
        g.setColor(Color.YELLOW);
        g.fillRect(chickenX, chickenY, chickenWidth, objectHeight);

        // 車1（紅）
        g.setColor(Color.RED);
        g.fillRect(car1X, car1Y, carWidth, objectHeight);

        // 車2（藍）
        g.setColor(Color.BLUE);
        g.fillRect(car2X, car2Y, carWidth, objectHeight);

        // 設定中文字型
        Font font14 = new Font("Dialog", Font.PLAIN, 14);
        Font font16 = new Font("Dialog", Font.PLAIN, 16);
        Font font30 = new Font("Dialog", Font.BOLD, 30);

        // 顯示關卡資訊
        g.setColor(Color.BLACK);
        g.setFont(font14);
        g.drawString("等級：" + level, width - 100, 30);
        g.drawString("車速：" + carSpeed, 30, 30);

        // 遊戲結束畫面
        if (gameOver) {
            g.setFont(font30);
            g.drawString("遊戲結束！", width / 2 - 100, height / 2 - 30);
            g.setFont(font16);
            g.drawString("按 R 鍵重新開始，Esc 離開", width / 2 - 150, height / 2 + 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // 移動車子
            car1X -= carSpeed;
            if (car1X < -carWidth) car1X = getWidth();

            car2X += carSpeed;
            if (car2X > getWidth() + carWidth) car2X = -carWidth;

            // 車速成長
            timeElapsed++;
            if (timeElapsed % 150 == 0) carSpeed += 1;

            // 碰撞判斷
            Rectangle chicken = new Rectangle(chickenX, chickenY, chickenWidth, objectHeight);
            Rectangle car1 = new Rectangle(car1X, car1Y, carWidth, objectHeight);
            Rectangle car2 = new Rectangle(car2X, car2Y, carWidth, objectHeight);

            if (chicken.intersects(car1) || chicken.intersects(car2)) {
                gameOver = true;
                timer.stop();
            }

            // 過馬路成功（進下一關）
            if (chickenY < (int) (getHeight() * 0.25)) {
                level++;
                chickenX = getWidth() / 2 - chickenWidth / 2;
                chickenY = (int) (getHeight() * 0.9);
                car1X = getWidth();
                car2X = -carWidth;
                carSpeed = 5 + level;
                timeElapsed = 0;
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int moveDistance = objectHeight;

        if (!gameOver) {
            if (key == KeyEvent.VK_LEFT) chickenX -= moveDistance;
            if (key == KeyEvent.VK_RIGHT) chickenX += moveDistance;
            if (key == KeyEvent.VK_UP) chickenY -= moveDistance;
            if (key == KeyEvent.VK_DOWN) chickenY += moveDistance;

            // 邊界檢查
            if (chickenX < 0) chickenX = 0;
            if (chickenX > getWidth() - chickenWidth) chickenX = getWidth() - chickenWidth;
            if (chickenY < 0) chickenY = 0;
            if (chickenY > getHeight() - objectHeight) chickenY = getHeight() - objectHeight;
        } else {
            if (key == KeyEvent.VK_R) {
                restartGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }

    private void restartGame() {
        int screenWidth = frame.getWidth();
        int screenHeight = frame.getHeight();
        chickenX = screenWidth / 2 - chickenWidth / 2;
        chickenY = (int) (screenHeight * 0.9);
        car1X = getWidth();
        car2X = -carWidth;
        carSpeed = 5;
        timeElapsed = 0;
        level = 1;
        gameOver = false;
        timer.start();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChickenCrossingGame());
    }
}