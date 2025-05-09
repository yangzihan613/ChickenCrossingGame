import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChickenCrossingGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int chickenX, chickenY;
    private int chickenWidth = 40;
    private int objectHeight; // 每條道路的高度
    private int carWidth = 50;
    private int carSpeed = 5;
    private int timeElapsed = 0;
    private int level = 1;
    private boolean gameOver = false;
    private JFrame frame;
    private Random random = new Random();
    private List<Road> roads = new ArrayList<>();
    private int roadCount = 20; // 總共 21 條道路
    private int roadSpacing = 0;

    public ChickenCrossingGame() {
        frame = new JFrame("小雞過馬路");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);

        int screenWidth = frame.getWidth();
        int screenHeight = frame.getHeight();

        chickenX = screenWidth / 2 - chickenWidth / 2;

        // 道路的高度現在根據畫面高度和道路數量計算
        objectHeight = screenHeight / roadCount;

        // 創建佈滿畫面的道路，最後一條是綠色安全道路
        for (int i = 0; i < roadCount; i++) {
            int roadY = i * objectHeight;
            Color roadColor = (i == roadCount - 1) ? Color.GREEN : ((i % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY); // 最後一條是綠色
            int carX = (i == roadCount - 1) ? -carWidth - 1 : (random.nextBoolean() ? screenWidth : -carWidth); // 綠色道路沒有車
            int carSpeed = this.carSpeed + random.nextInt(3);
            boolean goingRight = random.nextBoolean();
            roads.add(new Road(roadY, roadColor, carX, (i == roadCount - 1) ? 0 : carSpeed, (i == roadCount - 1) ? false : goingRight)); // 綠色道路車速為 0
        }

        // 設定小雞的初始 Y 軸位置在綠色安全道路上
        chickenY = roads.get(roadCount - 1).y;

        timer = new Timer(30, this);
        timer.start();
    }

    private class Road {
        int y;
        Color color;
        int carX;
        int carSpeed;
        boolean goingRight;

        public Road(int y, Color color, int carX, int carSpeed, boolean goingRight) {
            this.y = y;
            this.color = color;
            this.carX = carX;
            this.carSpeed = carSpeed;
            this.goingRight = goingRight;
        }

        public void moveCar() {
            if (goingRight) {
                carX += carSpeed;
                if (carX > getWidth() + carWidth) carX = -carWidth;
            } else {
                carX -= carSpeed;
                if (carX < -carWidth) carX = getWidth();
            }
        }

        public Rectangle getCarBounds() {
            return new Rectangle(carX, y, carWidth, objectHeight);
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(0, y, getWidth(), objectHeight);
            if (carSpeed > 0) { // 只在非綠色道路上繪製車輛
                g.setColor((goingRight) ? Color.BLUE : Color.RED);
                g.fillRect(carX, y, carWidth, objectHeight);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (Road road : roads) {
            road.draw(g);
        }

        g.setColor(Color.YELLOW);
        g.fillRect(chickenX, chickenY, chickenWidth, objectHeight);

        Font font14 = new Font("Dialog", Font.PLAIN, 14);
        Font font16 = new Font("Dialog", Font.PLAIN, 16);
        Font font30 = new Font("Dialog", Font.BOLD, 30);

        g.setColor(Color.BLACK);
        g.setFont(font14);
        g.drawString("等級：" + level, 30, 30);
        g.drawString("車速：" + carSpeed, width - 100, 30);

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
            for (int i = 0; i < roads.size() - 1; i++) { // 除了最後一條綠色道路，其餘道路移動車輛
                roads.get(i).moveCar();
            }

            timeElapsed++;
            if (timeElapsed % 150 == 0) carSpeed += 1;

            Rectangle chicken = new Rectangle(chickenX, chickenY, chickenWidth, objectHeight);
            for (int i = 0; i < roads.size() - 1; i++) { // 除了最後一條綠色道路，其餘道路檢查碰撞
                if (chicken.intersects(roads.get(i).getCarBounds())) {
                    gameOver = true;
                    timer.stop();
                    break;
                }
            }

            // 過馬路成功 (到達畫面頂部)
            if (chickenY < roads.get(0).y) {
                level++;
                chickenX = getWidth() / 2 - chickenWidth / 2;
                chickenY = roads.get(roadCount - 1).y; // 重新設定在底部綠色安全道路
                carSpeed = 5 + level;
                timeElapsed = 0;

                roads.clear();
                int screenHeight = getHeight();
                objectHeight = screenHeight / roadCount;
                for (int i = 0; i < roadCount; i++) {
                    int roadY = i * objectHeight;
                    Color roadColor = (i == roadCount - 1) ? Color.GREEN : ((i % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                    int carX = (i == roadCount - 1) ? -carWidth - 1 : (random.nextBoolean() ? getWidth() : -carWidth);
                    int currentCarSpeed = this.carSpeed + random.nextInt(3);
                    boolean goingRight = random.nextBoolean();
                    roads.add(new Road(roadY, roadColor, carX, (i == roadCount - 1) ? 0 : currentCarSpeed, (i == roadCount - 1) ? false : goingRight));
                }
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
            if (chickenY < roads.get(0).y) chickenY = roads.get(0).y; // 防止小雞跑到最上面的道路上方
            if (chickenY > roads.get(roadCount - 1).y + objectHeight) chickenY = roads.get(roadCount - 1).y + objectHeight; // 防止小雞跑到綠色道路下方
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
        carSpeed = 5;
        timeElapsed = 0;
        level = 1;
        gameOver = false;

        objectHeight = screenHeight / roadCount;
        roads.clear();
        for (int i = 0; i < roadCount; i++) {
            int roadY = i * objectHeight;
            Color roadColor = (i == roadCount - 1) ? Color.GREEN : ((i % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            int carX = (i == roadCount - 1) ? -carWidth - 1 : (random.nextBoolean() ? getWidth() : -carWidth);
            int currentCarSpeed = this.carSpeed + random.nextInt(3);
            boolean goingRight = random.nextBoolean();
            roads.add(new Road(roadY, roadColor, carX, (i == roadCount - 1) ? 0 : currentCarSpeed, (i == roadCount - 1) ? false : goingRight));
        }
        chickenY = roads.get(roadCount - 1).y;

        timer.start();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChickenCrossingGame());
    }
}