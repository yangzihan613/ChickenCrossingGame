import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Car {
    private int x;            // 車子的 X 座標
    private int y;            // 車子的 Y 座標 (這將由 Road 類別設定)
    private int width;        // 車子的寬度
    private int height;       // 車子的高度 (與道路高度相同)
    private int speed;        // 車子的速度
    private boolean goingRight; // 車子是否向右行駛
    private Image carImage;   // 車子的圖片

    public Car(int x, int y, int width, int height, int speed, boolean goingRight, Image carImage) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.goingRight = goingRight;
        this.carImage = carImage;
    }

    // 移動車子
    public void move(int panelWidth) {
        if (goingRight) {
            x += speed;
            // 如果車子移出畫面右側，則將其重置到畫面左側之外
            if (x > panelWidth) {
                x = -width;
            }
        } else {
            x -= speed;
            // 如果車子移出畫面左側，則將其重置到畫面右側之外
            if (x < -width) {
                x = panelWidth;
            }
        }
    }

    // 繪製車子
    public void draw(Graphics g) {
        if (carImage != null) {
            g.drawImage(carImage, x, y, width, height, null);
        }
        // 如果圖片沒載入，這裡可以選擇繪製一個顏色矩形作為替代，但通常有圖片會更好
    }

    // 取得車子的邊界矩形，用於碰撞偵測
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // 設定車子的 Y 座標 (當 Road 的 Y 座標改變時更新)
    public void setY(int y) {
        this.y = y;
    }
}