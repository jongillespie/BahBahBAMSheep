/**
 * Sheep Tangles are created when sheep are found and their pixel counts are above the outlier filters.
 * Used to keep track of their location and for estimating the sheep count based on pixel numbers in clusters.
 */
public class SheepTangle {

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int pixelKids;
    private int sheepEstimate = 1;

    /**
     * Sheep Tangle main constructor.
     * @param xMin pixel location
     * @param xMax pixel location
     * @param yMin pixel location
     * @param yMax pixel location
     * @param pixelKids quantity of adjacent qualifying sheep pixels
     */
    public SheepTangle(int xMin, int xMax, int yMin, int yMax, int pixelKids) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.pixelKids = pixelKids;
    }

    /**
     * Provides the pixel count for individual sheep clusters.
     * @return
     */
    public int getPixelKids(){
        return pixelKids;
    }

    /**
     * Provides the x min location
     * @return
     */
    public int getxMin() {
        return xMin;
    }

    /**
     * Provides the x max location
     * @return
     */
    public int getxMax() {
        return xMax;
    }

    /**
     * Provides the y min location.
     * @return
     */
    public int getyMin() {
        return yMin;
    }

    /**
     * Provides the y max location
     * @return
     */
    public int getyMax() {
        return yMax;
    }

    /**
     * provides the sheep estimated in the sheep cluster
     * @return
     */
    public int getSheepEstimate() {
        return sheepEstimate;
    }

    /**
     * Sets the estimation based on IQR and Standard Deviation from the controller.
     * @param sheepEstimate
     */
    public void setSheepEstimate(int sheepEstimate) {
        this.sheepEstimate = sheepEstimate;
    }
}
