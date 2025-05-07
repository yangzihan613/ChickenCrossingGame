import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChickenCrossingGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int chickenX = 200, chickenY = 320;

    private int car1X = 500, car1Y = 170; // 紅車（右→左）
    private int car2X = -100, car2Y = 200; // 藍車（左→右）

    private int carSpeed = 5;
    private int timeElapsed = 0;
    private int level = 1;

    private boolean gameOver = false;

    public ChickenCrossingGame() {
        JFrame frame = new JFrame("小雞過馬路");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);
        frame.addKeyListener(this);

        timer = new Timer(30, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 馬路
        g.setColor(Color.GRAY);
        g.fillRect(0, 170, getWidth(), 60);

        // 小雞
        g.setColor(Color.YELLOW);
        g.fillRect(chickenX, chickenY, 30, 30);

        // 車1（紅）
        g.setColor(Color.RED);
        g.fillRect(car1X, car1Y, 50, 30);

        // 車2（藍）
        g.setColor(Color.BLUE);
        g.fillRect(car2X, car2Y, 50, 30);

        // 設定中文字型
        Font font14 = new Font("Dialog", Font.PLAIN, 14);
        Font font16 = new Font("Dialog", Font.PLAIN, 16);
        Font font30 = new Font("Dialog", Font.BOLD, 30);

        // 顯示關卡資訊
        g.setColor(Color.BLACK);
        g.setFont(font14);
        g.drawString("等級：" + level, 400, 20);
        g.drawString("車速：" + carSpeed, 10, 20);

        // 遊戲結束畫面
        if (gameOver) {
            g.setFont(font30);
            g.drawString("遊戲結束！", 150, 180);
            g.setFont(font16);
            g.drawString("按 R 鍵重新開始，Esc 離開", 120, 220);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // 移動車子
            car1X -= carSpeed;
            if (car1X < -50) car1X = 500;

            car2X += carSpeed;
            if (car2X > 550) car2X = -100;

            // 車速成長
            timeElapsed++;
            if (timeElapsed % 150 == 0) carSpeed += 1;

            // 碰撞判斷
            Rectangle chicken = new Rectangle(chickenX, chickenY, 20, 20);
            Rectangle car1 = new Rectangle(car1X, car1Y, 50, 30);
            Rectangle car2 = new Rectangle(car2X, car2Y, 50, 30);

            if (chicken.intersects(car1) || chicken.intersects(car2)) {
                gameOver = true;
                timer.stop();
            }

            // 過馬路成功（進下一關）
            if (chickenY < 150) {
                level++;
                chickenX = 200;
                chickenY = 350;
                car1X = 500;
                car2X = -100;
                carSpeed = 5 + level;
                timeElapsed = 0;
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int moveDistance = 30; // 設定移動距離為車子的寬度

        if (!gameOver) {
            if (key == KeyEvent.VK_LEFT) chickenX -= moveDistance;
            if (key == KeyEvent.VK_RIGHT) chickenX += moveDistance;
            if (key == KeyEvent.VK_UP) chickenY -= moveDistance;
            if (key == KeyEvent.VK_DOWN) chickenY += moveDistance;

            // 邊界檢查，確保小雞不會移出遊戲畫面
            if (chickenX < 0) chickenX = 0;
            if (chickenX > getWidth() - 20) chickenX = getWidth() - 20;
            if (chickenY < 0) chickenY = 0;
            if (chickenY > getHeight() - 20) chickenY = getHeight() - 20;
        } else {
            if (key == KeyEvent.VK_R) {
                restartGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }

    private void restartGame() {
        chickenX = 200;
        chickenY = 350;
        car1X = 500;
        car2X = -100;
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
        new ChickenCrossingGame();
    }
}