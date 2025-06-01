import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Road {
    private int y;
    private Color color;
    private int roadHeight;
    private List<Car> cars;
    private Image blueCarImage;
    private Image redCarImage;
    private Random random = new Random();

    // 修改建構子，添加 carSpacing (兩隻雞的距離) 參數
    public Road(int y, Color color, int roadHeight, int initialCarSpeed, int numberOfCars, Image blueCarImage, Image redCarImage, int panelWidth, int carSpacing) {
        this.y = y;
        this.color = color;
        this.roadHeight = roadHeight;
        this.blueCarImage = blueCarImage;
        this.redCarImage = redCarImage;
        this.cars = new ArrayList<>();

        if (numberOfCars > 0) {
            // 1. 決定這條道路上車子的統一方向
            boolean goingRight = random.nextBoolean(); // true為向右，false為向左

            // 根據方向選擇統一的車輛圖片
            Image carImg = goingRight ? blueCarImage : redCarImage;
            int carWidth = 50; // 確保與 GamePanel 中的 carWidth 保持一致

            // 2. 計算車輛間距並生成初始位置
            // 每個車輛需要佔用的空間 = 車寬 + 兩隻雞的距離
            int totalRequiredSpacePerCar = carWidth + carSpacing;

            // 隨機初始偏移量，讓不同道路的車輛起始位置不一致
            // 讓第一輛車的起點在一個比 panelWidth 大一點的範圍內隨機
            // 例如：從 -panelWidth 延伸到 panelWidth*2 的範圍內
            int startingOffset = random.nextInt(panelWidth * 3) - panelWidth;

            for (int i = 0; i < numberOfCars; i++) {
                int initialCarX;

                if (goingRight) {
                    // 對於向右行駛的車輛，它們從左側進入
                    // 初始位置 = 隨機偏移 + (每輛車所需空間 * 當前車輛索引)
                    initialCarX = startingOffset - (totalRequiredSpacePerCar * i);
                } else {
                    // 對於向左行駛的車輛，它們從右側進入
                    // 初始位置 = panelWidth - 隨機偏移 + (每輛車所需空間 * 當前車輛索引)
                    // 或者更直觀地，從畫面右側一個範圍外開始倒退
                    initialCarX = startingOffset + panelWidth + (totalRequiredSpacePerCar * i);
                }

                // 隨機決定速度，比初始車速快一點
                int speed = initialCarSpeed + random.nextInt(3);

                cars.add(new Car(initialCarX, y, carWidth, roadHeight, speed, goingRight, carImg));
            }
        }
    }

    // ... (moveCars, draw, getCarBounds, getY, updateCarY 保持不變) ...

    public void moveCars(int panelWidth) {
        for (Car car : cars) {
            car.move(panelWidth);
        }
    }

    public void draw(Graphics g, int panelWidth) {
        g.setColor(color);
        g.fillRect(0, y, panelWidth, roadHeight);

        for (Car car : cars) {
            car.draw(g);
        }
    }

    public List<Rectangle> getCarBounds() {
        List<Rectangle> bounds = new ArrayList<>();
        for (Car car : cars) {
            bounds.add(car.getBounds());
        }
        return bounds;
    }

    public int getY() {
        return y;
    }

    public void updateCarY(int newY) {
        this.y = newY;
        for (Car car : cars) {
            car.setY(newY);
        }
    }
}