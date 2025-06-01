import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.ImageIcon;
import java.awt.Image;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Image chickenImage;
    private Timer timer;//安排週期性的事件
    private int chickenX;//雞角色的當前 X  座標
    private int chickenY;//雞角色的當前 Y 座標
    private int chickenWidth = 40;//小雞的寬度
    private int chickenHeight; // Add chickenHeight
    private int objectHeight;//儲存遊戲中各種物件（如道路區塊、小雞
    private int carWidth = 50;//汽車的寬度
    private int carSpeed = 5;//汽車的基本移動速度
    private int timeElapsed = 0;//記錄遊戲在當前等級中經過的時間
    private int level = 1;//等級
    private boolean gameOver = false;//布林變數，指示遊戲是否已經結束

    private JFrame frame;
    private Random random = new Random();//生成偽隨機數 決定汽車在道路上的初始位置。決定汽車的行進方向（向左或向右）隨機選擇哪些道路上會有汽車 為汽車的速度增加隨機的微小變動。
    private List<Road> roads = new ArrayList<>();//管理遊戲世界中所有道路（或行）資訊的核心數據結構
    private int roadCount = 20;//20條路

    private List<Integer> carLanes = new ArrayList<>();//決定了每一關遊戲的交通分佈
    private int initialCarLanes = 5;//開始時,車輛的路段數量
    private int initialChickenX;//存小雞角色的初始 X 座標

    private Set<Integer> pressedKeys = new HashSet<>();//追蹤當前所有被按住的鍵的鍵碼
    private boolean spaceBarHeld = false;//指示空白鍵是否正在被按住
    private long lastMoveTime = 0;//小雞上次移動的系統時間戳
    private int moveInterval = 150; // 調整一般移動間隔 (毫秒)
    private int fastMoveInterval = 10; // 空白鍵加速移動間隔 (毫秒)
    private int lastDirection = -1; // 記錄最後按下的方向鍵 (KeyEvent.VK_UP, DOWN, LEFT, RIGHT)
    private boolean continuousMove = false; // 標記是否需要持續移動
    //偵測鍵盤
    public GamePanel(JFrame frame) {
        this.frame = frame;
        setFocusable(true); // 使 JPanel 可以接收鍵盤事件
        addKeyListener(this);
        loadChickenImage("C:/Users/yangz/Desktop/ChickenCrossingGame/final/0520/ChickenCrossingGame/image-removebg-preview (1).png");
    }

    private void loadChickenImage(String path) {
        ImageIcon icon = new ImageIcon(path);
        chickenImage = icon.getImage();
        // Set chickenHeight based on objectHeight to maintain proportion
        chickenHeight = objectHeight;
    }

    //初始化遊戲狀態
    public void start() {
        int screenWidth = frame.getWidth();// 從遊戲主視窗取得當前面板的寬度
        int screenHeight = frame.getHeight();// 從遊戲主視窗取得當前面板的高度

        objectHeight = screenHeight / roadCount;// 計算每個物件（如道路和雞）的標準高度，確保它們能均勻分佈在螢幕上
        chickenHeight = objectHeight;


        roads.clear();// 清空現有的所有道路物件，準備重新生成
        carLanes.clear();// 清空現有的有車輛的路段索引，準備重新隨機設定
        // 迴圈直到選定足夠數量的有車輛路段（達到 initialCarLanes 設定的數量）
        while (carLanes.size() < initialCarLanes) {
            int index = random.nextInt(roadCount - 1);  // 隨機生成一個路段索引（不包括最底層的安全區，即 roadCount - 1）
            if (!carLanes.contains(index))// 如果該索引尚未被選中（避免重複）
                carLanes.add(index); // 將該索引加入到有車輛的路段列表中
        }
        // 迴圈 roadCount 次，為每一行創建一個 Road 物件
        for (int i = 0; i < roadCount; i++) {
            roads.add(createRoad(i, screenWidth));  // 呼叫 createRoad 方法，根據索引和螢幕寬度生成 Road 物件，並加入到 roads 列表中
        }

        initialChickenX = screenWidth / 2 - chickenWidth / 2;   // 計算小雞的初始 X 座標，使其位於螢幕水平中央
        chickenX = initialChickenX; // 將小雞的當前 X 座標設定為初始值
        chickenY = roads.get(roadCount - 1).getY();  // 將小雞的 Y 座標設定為最底層（安全區）道路的 Y 座標，使其從底部開始

        timer = new Timer(30, this); // 初始化一個 Timer 物件，設定每 30 毫秒觸發一次事件，並指定由當前 GamePanel 物件處理事件（因為它實作了 ActionListener）
        timer.start(); // 啟動 Timer，開始遊戲循環
    }
    //製造路
    private Road createRoad(int i, int screenWidth) {
        // 根據路段的索引 'i' 和每個物件的高度 'objectHeight'，計算這條道路的 Y 座標（垂直位置）。
        int roadY = i * objectHeight;
        // 判斷這條道路的顏色。
        // 如果是最後一條路段（即最底層的安全區），顏色設為綠色。
        // 否則，根據索引 'i' 的奇偶性，交替設為淺灰色或深灰色，模擬斑馬線或不同車道。
        Color color = (i == roadCount - 1) ? Color.GREEN : ((i % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        // 判斷這條道路是否會有車輛。
        // 檢查 'carLanes' 列表是否包含當前路段的索引 'i'。
        boolean hasCar = carLanes.contains(i);
        // 設置汽車的初始 X 座標。
        // 如果這條路有車 (hasCar 為 true)，則在螢幕寬度範圍內隨機設定一個 X 座標。
        // 如果沒有車 (hasCar 為 false)，則將汽車放在螢幕左側完全看不到的位置 (-carWidth - 1)，確保它不會顯示出來。
        int carX = hasCar ? (random.nextInt(screenWidth)) : -carWidth - 1;
        // 設置汽車的速度。
        // 如果這條路有車，則汽車速度為基本車速 'carSpeed' 加上一個 0 到 2 的隨機值，增加速度變化。
        // 如果沒有車，速度為 0。
        int speed = hasCar ? (carSpeed + random.nextInt(3)) : 0;
        // 設置汽車的行進方向。
        // 如果這條路有車，則隨機決定汽車是向右走 (true) 還是向左走 (false)。
        // 如果沒有車，則方向不重要，預設為 false。
        boolean goingRight = hasCar ? random.nextBoolean() : false;
        // 創建並返回一個新的 Road 物件，將之前計算的所有屬性傳入其建構子。
        return new Road(roadY, color, carX, speed, goingRight, carWidth, objectHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);// 呼叫 JPanel 的
        int width = getWidth();  // 取得當前面板的寬度
        int height = getHeight();  // 取得當前面板的高度

        g.setColor(Color.WHITE);//背景顏色
        g.fillRect(0, 0, width, height);//填充大小

        // 遍歷 'roads' 列表中的每一個 Road 物件
        for (Road road : roads) {
            // 呼叫每個 Road 物件的 draw 方法，將自己繪製到面板上
            // 並傳入 Graphics 物件 (g) 和面板寬度 (getWidth())
            road.draw(g, getWidth());
        }

        if (chickenImage != null) {
            g.drawImage(chickenImage, chickenX, chickenY, chickenWidth, chickenHeight, this);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(chickenX, chickenY, chickenWidth, objectHeight);
        }

        g.setColor(Color.BLACK);// 設定繪圖顏色為黑色，用於繪製文字資訊
        g.setFont(new Font("Dialog", Font.PLAIN, 14)); // 設定文字字體為 "Dialog"，普通樣式，大小為 14
        g.drawString("等級：" + level, 30, 30);// 在指定位置 (30, 30) 繪製當前遊戲等級的文字資訊
        g.drawString("車速：" + carSpeed, width - 100, 30);// 在指定位置 (面板寬度 - 100, 30) 繪製當前汽車速度的文字資訊

        // 檢查遊戲是否結束
        if (gameOver) {
            //遊戲結束出現的字
            int textWidthGameOver = g.getFontMetrics(new Font("Dialog", Font.BOLD, 30)).stringWidth("遊戲結束！");
            int textHeightGameOver = g.getFontMetrics(new Font("Dialog", Font.BOLD, 30)).getHeight();
            int textWidthRestart = g.getFontMetrics(new Font("Dialog", Font.PLAIN, 16)).stringWidth("Press R to restart, Esc to exit");
            int textHeightRestart = g.getFontMetrics(new Font("Dialog", Font.PLAIN, 16)).getHeight();

            //訊息框大小
            int padding = 20;
            int rectX = width / 2 - Math.max(textWidthGameOver, textWidthRestart) / 2 - padding;
            int rectY = height / 2 - textHeightGameOver / 2 - textHeightRestart / 2 - padding;
            int rectWidth = Math.max(textWidthGameOver, textWidthRestart) + 2 * padding;
            int rectHeight = textHeightGameOver + textHeightRestart + 2 * padding;

            g.setColor(Color.BLACK);// 設定繪圖顏色為黑色，用於填充訊息框背景
            g.fillRect(rectX, rectY, rectWidth, rectHeight);// 繪製黑色的實心矩形作為遊戲結束訊息框的背景

            g.setColor(Color.WHITE);// 設定繪圖顏色為白色，用於繪製訊息框的邊框
            g.drawRect(rectX, rectY, rectWidth, rectHeight); // 繪製白色的矩形邊框

            //繪製遊戲結束的
            g.setColor(Color.WHITE);
            g.setFont(new Font("Dialog", Font.BOLD, 30));
            g.drawString("遊戲結束！", width / 2 - textWidthGameOver / 2 + 10, height / 2 - textHeightGameOver / 2 + 20);
            g.setFont(new Font("Dialog", Font.PLAIN, 16));
            g.drawString("Press R to restart, Esc to exit", width / 2 - textWidthRestart / 2, height / 2 + textHeightRestart + 10);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 檢查遊戲是否仍在進行中
        if (!gameOver) {
            // 遍歷所有道路（除了最底層的綠色安全區，因為它沒有車輛）
            for (int i = 0; i < roads.size() - 1; i++) {
                roads.get(i).moveCar(getWidth());
                // 呼叫該 Road 物件的 moveCar 方法，更新其上汽車的位置
                // getWidth() 傳入面板寬度，以便汽車在超出邊界時能正確循環出現
            }

            timeElapsed++;// 遊戲時間計數器增加

            // 每當 timeElapsed 達到 150 的倍數時（例如 150, 300, 450...）
            if (timeElapsed % 150 == 0) carSpeed++;// 增加汽車的基本速度，使遊戲難度逐漸升高



            // 創建一個 Rectangle 物件，代表小雞當前的碰撞邊界
            // 這用於後續的碰撞檢測
            Rectangle chicken = new Rectangle(chickenX, chickenY, chickenWidth, chickenHeight);

            // 再次遍歷所有有車輛的路段（除了最底層的安全區）
            for (int i = 0; i < roads.size() - 1; i++) {
                // 檢查小雞的碰撞邊界是否與當前道路上汽車的碰撞邊界發生交集（即碰撞）
                if (chicken.intersects(roads.get(i).getCarBounds())) {
                    gameOver = true;// 如果發生碰撞，將遊戲結束標誌設為 true
                    timer.stop();// 由於已經檢測到碰撞，跳出迴圈，不需要再檢查其他道路
                    break;
                }
            }

            if (chickenY < 0) { // 檢查小雞的 Y 座標是否小於 0 (即小雞已經成功穿越所有道路，到達螢幕頂部)
                level++; // 遊戲等級提升 1
                // 將小雞的 Y 座標重置到倒數第二條路的底部。
                // 這樣，小雞就會出現在新一關的起始位置，準備再次穿越。
                chickenY = roads.get(roadCount - 2).getY() + objectHeight;
                carSpeed = 5 + level;// 等級越高，車速越快，增加遊戲難度。
                timeElapsed = 0;// 重置時間計數器，為新等級的車速遞增重新計時

                // 如果當前有車輛的路段數量還沒有達到總路段數減一（即除了最底層安全區以外的所有路段）
                if (initialCarLanes < roadCount - 1) initialCarLanes++;// 增加下一關會有車輛的路段數量，進一步提升難度

                carLanes.clear();// 清空當前有車輛的路段索引列表
                // 重新隨機選擇新的有車輛路段。
                // 迴圈直到選定的路段數量達到新的 initialCarLanes。
                while (carLanes.size() < initialCarLanes) {
                    int index = random.nextInt(roadCount - 1);// 隨機生成一個路段索引（不包括最底層安全區）
                    if (!carLanes.contains(index)) carLanes.add(index);// 如果該索引尚未被選中，則將其加入列表
                }

                roads.clear();// 清空所有現有的 Road 物件
                // 重新創建所有 Road 物件，包含新的車輛分佈和可能的更高速度
                for (int i = 0; i < roadCount; i++) {
                    roads.add(createRoad(i, getWidth()));
                }
            }

            if (!spaceBarHeld) {
                processSingleKeyPress();
            } else {
                processContinuousKeyPress();
            }

            repaint();
        }
    }

    private void processSingleKeyPress() {
        int move = objectHeight;
        if (!pressedKeys.isEmpty()) {
            int key = pressedKeys.iterator().next(); // 取得按下的第一個按鍵
            if (key == KeyEvent.VK_LEFT) chickenX -= move;
            if (key == KeyEvent.VK_RIGHT) chickenX += move;
            if (key == KeyEvent.VK_UP) chickenY -= move;
            if (key == KeyEvent.VK_DOWN) chickenY += move;

            pressedKeys.clear(); // 處理完一次按鍵後清空集合
            ensureChickenBounds();
        }
    }

    private void processContinuousKeyPress() {
        if (!continuousMove) return; // 如果不需要持續移動，則直接返回

        long currentTime = System.currentTimeMillis();
        int currentMoveInterval = spaceBarHeld ? fastMoveInterval : moveInterval; // 根據空白鍵是否按下選擇移動間隔
        if (currentTime - lastMoveTime > currentMoveInterval && lastDirection != -1) {
            int move = objectHeight;
            if (lastDirection == KeyEvent.VK_LEFT) chickenX -= move;
            if (lastDirection == KeyEvent.VK_RIGHT) chickenX += move;
            if (lastDirection == KeyEvent.VK_UP) chickenY -= move;
            if (lastDirection == KeyEvent.VK_DOWN) chickenY += move;

            ensureChickenBounds();
            lastMoveTime = currentTime;
        }
    }

    private void ensureChickenBounds() {
        if (chickenX < 0) chickenX = 0;
        if (chickenX > getWidth() - chickenWidth) chickenX = getWidth() - chickenWidth;
        if (chickenY > roads.get(roadCount - 1).getY() + objectHeight) chickenY = roads.get(roadCount - 1).getY() + objectHeight;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!gameOver) {
            if (key == KeyEvent.VK_SPACE) {
                spaceBarHeld = true;
                continuousMove = true; // 按下空白鍵時開始持續移動
                lastMoveTime = System.currentTimeMillis(); // 開始連擊計時
            } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ||
                    key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                lastDirection = key; // 記錄最後按下的方向鍵
                if (!spaceBarHeld && !pressedKeys.contains(key)) {
                    pressedKeys.add(key); // 如果沒有按住空白鍵，仍然處理單次按鍵
                }
            }
        } else {
            if (key == KeyEvent.VK_R) {
                restartGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE) {
            spaceBarHeld = false;
            if (pressedKeys.isEmpty()) {
                continuousMove = false; // 鬆開空白鍵且沒有方向鍵按住時停止持續移動
            }
            pressedKeys.clear(); // 鬆開空白鍵時停止單次按鍵的累積
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ||
                key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            if (!spaceBarHeld) {
                pressedKeys.remove(key); // 如果沒有按住空白鍵，移除鬆開的按鍵
            }
            // 當鬆開方向鍵時，如果空白鍵仍然按住，則保持持續移動，方向由 lastDirection 決定
            // 如果空白鍵也鬆開了，則 continuousMove 會在 spaceBar released 中設為 false (如果 pressedKeys 為空)
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 不需要實作
    }
    //重製遊戲
    private void restartGame() {
        chickenX = initialChickenX;// 將小雞的 X 座標重置回其初始的水平中央位置
        carSpeed = 5;
        timeElapsed = 0;
        level = 1;
        gameOver = false;
        initialCarLanes = 5;
        pressedKeys.clear();
        spaceBarHeld = false;
        lastDirection = -1;
        continuousMove = false;

        objectHeight = getHeight() / roadCount;
        chickenHeight = objectHeight;
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
        requestFocusInWindow(); // 重新開始遊戲後請求焦點，確保可以接收按鍵事件
        repaint();
    }
}