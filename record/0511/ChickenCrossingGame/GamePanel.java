import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int chickenX, chickenY;
    private int chickenWidth = 40;
    private int objectHeight;
    private int carWidth = 50;
    private int carSpeed = 5;
    private int timeElapsed = 0;
    private int level = 1;
    private boolean gameOver = false;

    private JFrame frame;
    private Random random = new Random();
    private List<Road> roads = new ArrayList<>();
    private int roadCount = 20;

    private List<Integer> carLanes = new ArrayList<>();
    private int initialCarLanes = 5;

    public GamePanel(JFrame frame) {
        this.frame = frame;
    }

    public void start() {
        int screenWidth = frame.getWidth();
        int screenHeight = frame.getHeight();

        objectHeight = screenHeight / roadCount;

        roads.clear();
        carLanes.clear();
        while (carLanes.size() < initialCarLanes) {
            int index = random.nextInt(roadCount - 1); // exclude the last road (grass)
            if (!carLanes.contains(index)) carLanes.add(index);
        }

        for (int i = 0; i < roadCount; i++) {
            roads.add(createRoad(i, screenWidth));
        }

        chickenX = random.nextInt(screenWidth - chickenWidth);
        chickenY = roads.get(roadCount - 1).getY();

        timer = new Timer(30, this);
        timer.start();
    }

    private Road createRoad(int i, int screenWidth) {
        int roadY = i * objectHeight;
        Color color = (i == roadCount - 1) ? Color.GREEN : ((i % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY);

        boolean hasCar = carLanes.contains(i);
        int carX = hasCar ? (random.nextInt(screenWidth)) : -carWidth - 1;
        int speed = hasCar ? (carSpeed + random.nextInt(3)) : 0;
        boolean goingRight = hasCar ? random.nextBoolean() : false;

        return new Road(roadY, color, carX, speed, goingRight, carWidth, objectHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (Road road : roads) {
            road.draw(g, getWidth());
        }

        g.setColor(Color.YELLOW);
        g.fillRect(chickenX, chickenY, chickenWidth, objectHeight);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        g.drawString("等級：" + level, 30, 30);
        g.drawString("車速：" + carSpeed, width - 100, 30);

        if (gameOver) {
            g.setFont(new Font("Dialog", Font.BOLD, 30));
            g.drawString("遊戲結束！", width / 2 - 100, height / 2 - 30);
            g.setFont(new Font("Dialog", Font.PLAIN, 16));
            g.drawString("Press R to restart, Esc to exit", width / 2 - 150, height / 2 + 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            for (int i = 0; i < roads.size() - 1; i++) {
                roads.get(i).moveCar(getWidth());
            }

            timeElapsed++;
            if (timeElapsed % 150 == 0) carSpeed++;

            Rectangle chicken = new Rectangle(chickenX, chickenY, chickenWidth, objectHeight);
            for (int i = 0; i < roads.size() - 1; i++) {
                if (chicken.intersects(roads.get(i).getCarBounds())) {
                    gameOver = true;
                    timer.stop();
                    break;
                }
            }

            if (chickenY < 0) {
                level++;
                chickenX = random.nextInt(getWidth() - chickenWidth);
                chickenY = roads.get(roadCount - 2).getY() + objectHeight;
                carSpeed = 5 + level;
                timeElapsed = 0;

                if (initialCarLanes < roadCount - 1) initialCarLanes++;

                carLanes.clear();
                while (carLanes.size() < initialCarLanes) {
                    int index = random.nextInt(roadCount - 1);
                    if (!carLanes.contains(index)) carLanes.add(index);
                }

                roads.clear();
                for (int i = 0; i < roadCount; i++) {
                    roads.add(createRoad(i, getWidth()));
                }
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int move = objectHeight;
        int key = e.getKeyCode();

        if (!gameOver) {
            if (key == KeyEvent.VK_LEFT) chickenX -= move;
            if (key == KeyEvent.VK_RIGHT) chickenX += move;
            if (key == KeyEvent.VK_UP) chickenY -= move;
            if (key == KeyEvent.VK_DOWN) chickenY += move;

            if (chickenX < 0) chickenX = 0;
            if (chickenX > getWidth() - chickenWidth) chickenX = getWidth() - chickenWidth;
            if (chickenY > roads.get(roadCount - 1).getY() + objectHeight) chickenY = roads.get(roadCount - 1).getY() + objectHeight;
        } else {
            if (key == KeyEvent.VK_R) {
                restartGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }

    private void restartGame() {
        chickenX = random.nextInt(getWidth() - chickenWidth);
        carSpeed = 5;
        timeElapsed = 0;
        level = 1;
        gameOver = false;
        initialCarLanes = 5;

        objectHeight = getHeight() / roadCount;
        carLanes.clear();
        while (carLanes.size() < initialCarLanes) {
            int index = random.nextInt(roadCount - 1);
            if (!carLanes.contains(index)) carLanes.add(index);
        }

        roads.clear();
        for (int i = 0; i < roadCount; i++) {
            roads.add(createRoad(i, getWidth()));
        }
        chickenY = roads.get(roadCount - 2).getY() + objectHeight;

        timer.start();
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}