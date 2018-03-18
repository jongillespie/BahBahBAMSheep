public class Rectangle {

    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int area;

    private int pixelKids;

    public Rectangle(int xMin, int xMax, int yMin, int yMax, int area, int pixelKids) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.area = area;

        this.pixelKids = pixelKids;
    }

    public int getPixelKids(){
        return pixelKids;
    }


    public int getxMin() {
        return xMin;
    }

    public int getxMax() {
        return xMax;
    }

    public int getyMin() {
        return yMin;
    }

    public int getyMax() {
        return yMax;
    }

    public int getArea() {
        return area;
    }
}
