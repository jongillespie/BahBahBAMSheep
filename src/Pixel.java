/**
 * The Pixel Class captures relevant pixel data from each imported image for color comparisons
 * and re-writing of Pixels related to each Filter.
 */
public class Pixel {

    Pixel parent = null;
    private int x;
    private int y;
    private int argb;

    /**
     * Pixel Constructor
     * @param x pixel's x location
     * @param y pixel's y location
     * @param argb the pixel color byte
     */
    public Pixel(int x, int y, int argb){
        this.x = x;
        this.y = y;
        this.argb = argb;
    }

    /**
     * Get Parent
     * @return parent address
     */
    public Pixel getParent() {
        return parent;
    }

    /**
     * Set Parent
     * @param parent address assignment
     */
    public void setParent(Pixel parent) {
        this.parent = parent;
    }

    /**
     * Get X Value
     * @return the x Value of the Pixel
     */
    public int getX() {
        return x;
    }

    /**
     * Set X Value
     * @param x is set for the Pixel
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get Y Value
     * @return the Y value.
     */
    public int getY() {
        return y;
    }

    /**
     * Set Y Value
     * @param y is set for the Pixel
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get ARGB
     * @return the ARGB Color Byte
     */
    public int getArgb() {
        return argb;
    }

    /**
     * Set ARGB
     * @param argb sets the ARGB Byte Value
     */
    public void setArgb(int argb) {
        this.argb = argb;
    }
}
