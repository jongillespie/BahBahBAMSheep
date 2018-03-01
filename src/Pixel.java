public class Pixel {

    Pixel parent = null;
    private int x;
    private int y;
    private int argb;

    public Pixel(int x, int y, int argb){
        this.x = x;
        this.y = y;
        this.argb = argb;
    }

    public Pixel getParent() {
        return parent;
    }

    public void setParent(Pixel parent) {
        this.parent = parent;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getArgb() {
        return argb;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }
}
