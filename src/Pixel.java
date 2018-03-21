/**
 *
 */
public class Pixel {

    Pixel parent = null;
    private int x;
    private int y;
    private int argb;

    /**
     *
     * @param x
     * @param y
     * @param argb
     */
    public Pixel(int x, int y, int argb){
        this.x = x;
        this.y = y;
        this.argb = argb;
    }

    /**
     *
     * @return
     */
    public Pixel getParent() {
        return parent;
    }

    /**
     *
     * @param parent
     */
    public void setParent(Pixel parent) {
        this.parent = parent;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @return
     */
    public int getArgb() {
        return argb;
    }

    /**
     *
     * @param argb
     */
    public void setArgb(int argb) {
        this.argb = argb;
    }
}
