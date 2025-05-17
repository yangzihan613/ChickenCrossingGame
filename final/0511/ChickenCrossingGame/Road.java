import java.awt.*;

public class Road {
    private int y;
    private Color color;
    private int carX;
    private int carSpeed;
    private boolean goingRight;
    private int carWidth;
    private int height;

    public Road(int y, Color color, int carX, int carSpeed, boolean goingRight, int carWidth, int height) {
        this.y = y;
        this.color = color;
        this.carX = carX;
        this.carSpeed = carSpeed;
        this.goingRight = goingRight;
        this.carWidth = carWidth;
        this.height = height;
    }

    public void moveCar(int panelWidth) {
        if (carSpeed <= 0) return;

        if (goingRight) {
            carX += carSpeed;
            if (carX > panelWidth + carWidth) carX = -carWidth;
        } else {
            carX -= carSpeed;
            if (carX < -carWidth) carX = panelWidth;
        }
    }

    public void draw(Graphics g, int panelWidth) {
        g.setColor(color);
        g.fillRect(0, y, panelWidth, height);
        if (carSpeed > 0) {
            g.setColor(goingRight ? Color.BLUE : Color.RED);
            g.fillRect(carX, y, carWidth, height);
        }
    }

    public Rectangle getCarBounds() {
        return new Rectangle(carX, y, carWidth, height);
    }

    public int getY() {
        return y;
    }
}
