/**
 *
 */
public class SheepTangle {

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int area;
    private int pixelKids;
    private int sheepEstimate = 1;

    /**
     *
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     * @param area
     * @param pixelKids
     */
    public SheepTangle(int xMin, int xMax, int yMin, int yMax, int area, int pixelKids) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.area = area;
        this.pixelKids = pixelKids;
    }

    /**
     *
     * @return
     */
    public int getPixelKids(){
        return pixelKids;
    }

    /**
     *
     * @return
     */
    public int getxMin() {
        return xMin;
    }

    /**
     *
     * @return
     */
    public int getxMax() {
        return xMax;
    }

    /**
     *
     * @return
     */
    public int getyMin() {
        return yMin;
    }

    /**
     *
     * @return
     */
    public int getyMax() {
        return yMax;
    }

    /**
     *
     * @return
     */
    public int getArea() {
        return area;
    }

    /**
     *
     * @return
     */
    public int getSheepEstimate() {
        return sheepEstimate;
    }

    /**
     *
     * @param sheepEstimate
     */
    public void setSheepEstimate(int sheepEstimate) {
        this.sheepEstimate = sheepEstimate;
    }
}
