import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Dimension;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // ... (其他變數保持不變) ...
    private Image chickenImage;
    private Image blueCarImage;
    private Image redCarImage;
    private Timer timer;
    private int chickenX;
    private int chickenY;
    private int chickenWidth = 40; // 小雞的寬度
    private int chickenHeight;
    private int objectHeight;
    private int carWidth = 50;
    private int currentCarSpeed = 5;
    private int timeElapsed = 0;
    private int level = 1;
    private boolean gameOver = false;
    private JFrame frame;
    private Random random = new Random();
    private List<Road> roads = new ArrayList();
    private int roadCount = 20;
    private List<Integer> carLanesIndices = new ArrayList();

    private int initialCarsPerLane = 2;
    private final int MAX_CARS_PER_LANE = 6;

    // 新增：兩隻雞的距離
    private int carSpacing = 0; // 初始化為0，會在 start() 中計算

    private int initialChickenX;
    private Set<Integer> pressedKeys = new HashSet();
    private boolean spaceBarHeld = false;
    private long lastMoveTime = 0L;
    private int moveInterval = 150;
    private int fastMoveInterval = 10;
    private int lastDirection = -1;
    private boolean continuousMove = false;


    public GamePanel(JFrame frame) {
        this.frame = frame;
        this.setFocusable(true);
        this.addKeyListener(this);
        this.loadChickenImage("C:/Users/yangz/Desktop/ChickenCrossingGame/final/0520/ChickenCrossingGame/image-removebg-preview (1).png");
        this.loadCarImages("C:/Users/yangz/Desktop/過馬路小雞/0520/ChickenCrossingGame/blue.png", "C:/Users/yangz/Desktop/過馬路小雞/0520/ChickenCrossingGame/red.png");
    }

    private void loadChickenImage(String path) {
        ImageIcon icon = new ImageIcon(path);
        this.chickenImage = icon.getImage();
    }

    private void loadCarImages(String bluePath, String redPath) {
        ImageIcon blueIcon = new ImageIcon(bluePath);
        this.blueCarImage = blueIcon.getImage();
        ImageIcon redIcon = new ImageIcon(redPath);
        this.redCarImage = redIcon.getImage();
    }


    public void start() {
        int screenWidth = this.frame.getWidth();
        int screenHeight = this.frame.getHeight();
        this.objectHeight = screenHeight / this.roadCount;
        this.chickenHeight = this.objectHeight;
        // 計算兩隻雞的距離 (2 * 小雞寬度)
        this.carSpacing = 2 * this.chickenWidth;

        this.roads.clear();
        this.carLanesIndices.clear();

        int numCarLanes = 5 + (level - 1) / 3 * 2;
        if (numCarLanes >= roadCount -1) {
            numCarLanes = roadCount - 1;
        }

        while(this.carLanesIndices.size() < numCarLanes) {
            int index = this.random.nextInt(this.roadCount - 1);
            if (!this.carLanesIndices.contains(index)) {
                this.carLanesIndices.add(index);
            }
        }

        for(int i = 0; i < this.roadCount; ++i) {
            this.roads.add(this.createRoad(i, screenWidth));
        }

        this.initialChickenX = screenWidth / 2 - this.chickenWidth / 2;
        this.chickenX = this.initialChickenX;
        this.chickenY = ((Road)this.roads.get(this.roadCount - 1)).getY();
        this.timer = new Timer(30, this);
        this.timer.start();
    }

    private Road createRoad(int i, int screenWidth) {
        int roadY = i * this.objectHeight;
        Color color = i == this.roadCount - 1 ? Color.GREEN : (i % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        boolean hasCarLane = this.carLanesIndices.contains(i);

        int carsOnThisRoad = 0;
        if (hasCarLane) {
            carsOnThisRoad = initialCarsPerLane + (level - 1) / 3 * 2;
            if (carsOnThisRoad > MAX_CARS_PER_LANE) {
                carsOnThisRoad = MAX_CARS_PER_LANE;
            }
        }

        // *** 修改這裡：傳遞 carSpacing 參數給 Road 建構子 ***
        return new Road(roadY, color, this.objectHeight, currentCarSpeed, carsOnThisRoad, blueCarImage, redCarImage, screenWidth, carSpacing);
    }

    // ... (paintComponent, actionPerformed, processSingleKeyPress, processContinuousKeyPress, ensureChickenBounds, keyPressed, keyReleased, keyTyped 方法保持不變) ...
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = this.getWidth();
        int height = this.getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for(Road road : this.roads) {
            road.draw(g, this.getWidth());
        }

        if (this.chickenImage != null) {
            g.drawImage(this.chickenImage, this.chickenX, this.chickenY, this.chickenWidth, this.chickenHeight, this);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(this.chickenX, this.chickenY, this.chickenWidth, this.objectHeight);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Dialog", 0, 14));
        g.drawString("等級：" + this.level, 30, 30);
        g.drawString("車速：" + this.currentCarSpeed, width - 100, 30);
        if (this.gameOver) {
            int textWidthGameOver = g.getFontMetrics(new Font("Dialog", 1, 30)).stringWidth("遊戲結束！");
            int textHeightGameOver = g.getFontMetrics(new Font("Dialog", 1, 30)).getHeight();
            int textWidthRestart = g.getFontMetrics(new Font("Dialog", 0, 16)).stringWidth("Press R to restart, Esc to exit");
            int textHeightRestart = g.getFontMetrics(new Font("Dialog", 0, 16)).getHeight();
            int padding = 20;
            int rectX = width / 2 - Math.max(textWidthGameOver, textWidthRestart) / 2 - padding;
            int rectY = height / 2 - textHeightGameOver / 2 - textHeightRestart / 2 - padding;
            int rectWidth = Math.max(textWidthGameOver, textWidthRestart) + 2 * padding;
            int rectHeight = textHeightGameOver + textHeightRestart + 2 * padding;
            g.setColor(Color.BLACK);
            g.fillRect(rectX, rectY, rectWidth, rectHeight);
            g.setColor(Color.WHITE);
            g.drawRect(rectX, rectY, rectWidth, rectHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Dialog", 1, 30));
            g.drawString("遊戲結束！", width / 2 - textWidthGameOver / 2 + 10, height / 2 - textHeightGameOver / 2 + 20);
            g.setFont(new Font("Dialog", 0, 16));
            g.drawString("Press R to restart, Esc to exit", width / 2 - textWidthRestart / 2, height / 2 + textHeightRestart + 10);
        }
    }


    public void actionPerformed(ActionEvent e) {
        if (!this.gameOver) {
            for(int i = 0; i < this.roads.size() - 1; ++i) {
                ((Road)this.roads.get(i)).moveCars(this.getWidth());
            }

            Rectangle chicken = new Rectangle(this.chickenX, this.chickenY, this.chickenWidth, this.chickenHeight);

            for(int i = 0; i < this.roads.size() - 1; ++i) {
                List<Rectangle> carBounds = ((Road)this.roads.get(i)).getCarBounds();
                for (Rectangle carRect : carBounds) {
                    if (chicken.intersects(carRect)) {
                        this.gameOver = true;
                        this.timer.stop();
                        break;
                    }
                }
                if (this.gameOver) break;
            }

            if (this.chickenY < 0) {
                ++this.level;
                this.chickenY = ((Road)this.roads.get(this.roadCount - 1)).getY();

                currentCarSpeed = 5 + (level - 1) / 2;

                this.timeElapsed = 0;

                int numCarLanes = 5 + (level - 1) / 3 * 2;
                if (numCarLanes >= roadCount -1) {
                    numCarLanes = roadCount - 1;
                }

                this.carLanesIndices.clear();

                while(this.carLanesIndices.size() < numCarLanes) {
                    int index = this.random.nextInt(this.roadCount - 1);
                    if (!this.carLanesIndices.contains(index)) {
                        this.carLanesIndices.add(index);
                    }
                }

                this.roads.clear();

                for(int i = 0; i < this.roadCount; ++i) {
                    this.roads.add(this.createRoad(i, this.getWidth()));
                }
            }

            if (!this.spaceBarHeld) {
                this.processSingleKeyPress();
            } else {
                this.processContinuousKeyPress();
            }

            this.repaint();
        }
    }


    private void processSingleKeyPress() {
        int move = this.objectHeight;
        if (!this.pressedKeys.isEmpty()) {
            int key = (Integer)this.pressedKeys.iterator().next();
            if (key == KeyEvent.VK_LEFT) {
                this.chickenX -= move;
            }

            if (key == KeyEvent.VK_RIGHT) {
                this.chickenX += move;
            }

            if (key == KeyEvent.VK_UP) {
                this.chickenY -= move;
            }

            if (key == KeyEvent.VK_DOWN) {
                this.chickenY += move;
            }

            this.pressedKeys.clear();
            this.ensureChickenBounds();
        }
    }

    private void processContinuousKeyPress() {
        if (this.continuousMove) {
            long currentTime = System.currentTimeMillis();
            int currentMoveInterval = this.spaceBarHeld ? this.fastMoveInterval : this.moveInterval;
            if (currentTime - this.lastMoveTime > (long)currentMoveInterval && this.lastDirection != -1) {
                int move = this.objectHeight;
                if (this.lastDirection == KeyEvent.VK_LEFT) {
                    this.chickenX -= move;
                }

                if (this.lastDirection == KeyEvent.VK_RIGHT) {
                    this.chickenX += move;
                }

                if (this.lastDirection == KeyEvent.VK_UP) {
                    this.chickenY -= move;
                }

                if (this.lastDirection == KeyEvent.VK_DOWN) {
                    this.chickenY += move;
                }

                this.ensureChickenBounds();
                this.lastMoveTime = currentTime;
            }
        }
    }

    private void ensureChickenBounds() {
        if (this.chickenX < 0) {
            this.chickenX = 0;
        }

        if (this.chickenX > this.getWidth() - this.chickenWidth) {
            this.chickenX = this.getWidth() - this.chickenWidth;
        }

        if (this.chickenY > ((Road)this.roads.get(this.roadCount - 1)).getY() + this.objectHeight) {
            this.chickenY = ((Road)this.roads.get(this.roadCount - 1)).getY() + this.objectHeight;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!this.gameOver) {
            if (key == KeyEvent.VK_SPACE) {
                this.spaceBarHeld = true;
                this.continuousMove = true;
                this.lastMoveTime = System.currentTimeMillis();
            } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                this.lastDirection = key;
                if (!this.spaceBarHeld && !this.pressedKeys.contains(key)) {
                    this.pressedKeys.add(key);
                }
            }
        } else if (key == KeyEvent.VK_R) {
            this.restartGame();
        } else if (key == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE) {
            this.spaceBarHeld = false;
            if (this.pressedKeys.isEmpty()) {
                this.continuousMove = false;
            }

            this.pressedKeys.clear();
        } else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) && !this.spaceBarHeld) {
            this.pressedKeys.remove(key);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    private void restartGame() {
        this.chickenX = this.initialChickenX;
        this.currentCarSpeed = 5;
        this.timeElapsed = 0;
        this.level = 1;
        this.gameOver = false;
        this.initialCarsPerLane = 2;
        this.pressedKeys.clear();
        this.spaceBarHeld = false;
        this.lastDirection = -1;
        this.continuousMove = false;

        this.objectHeight = this.getHeight() / this.roadCount;
        this.chickenHeight = this.objectHeight;
        // 重新計算間距
        this.carSpacing = 2 * this.chickenWidth;


        this.carLanesIndices.clear();

        int numCarLanes = 5 + (level - 1) / 3 * 2;
        if (numCarLanes >= roadCount -1) {
            numCarLanes = roadCount - 1;
        }
        while(this.carLanesIndices.size() < numCarLanes) {
            int index = this.random.nextInt(this.roadCount - 1);
            if (!this.carLanesIndices.contains(index)) {
                this.carLanesIndices.add(index);
            }
        }

        this.roads.clear();

        for(int i = 0; i < this.roadCount; ++i) {
            this.roads.add(this.createRoad(i, this.getWidth()));
        }

        this.chickenY = ((Road)this.roads.get(this.roadCount - 1)).getY();
        this.timer.start();
        this.requestFocusInWindow();
        this.repaint();
    }
}