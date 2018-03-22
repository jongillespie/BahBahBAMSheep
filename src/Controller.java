import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javafx.scene.image.*;
import javax.imageio.ImageIO;

/**
 * Main Controller manages all image loading, filter effects and display execution.
 * Additionally, in section two - contains all relevant Union-Find-Compression methods to
 * determine sheep quantity and shape outlines.
 */
public class Controller {

    @FXML private ImageView imageDisp, imageAffect;
    @FXML private javafx.scene.text.Text textFileName, textSize, textDimensions, sheepCountDisp;
    @FXML public Slider luminanceSlider, sensitivitySlider, outlierSlider;

    public BufferedImage bufferedImage;
    private WritableImage buffWritableImg, colorImage, workableImage;
    private Boolean colorActive, greenFilter = false;
    private ArrayList<Pixel> field;
    private ArrayList<SheepTangle> rectangles, sheepTangles;
    private ArrayList<Integer> pixelKids;
    private int sensitivity, outlier, Q1, Q3;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMAGE LOAD & FILTER CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Opens an Image and Displays it on both screens.
     */
    @FXML
    public void openImage() {
        // Opens a file chooser dialog box.
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );
        File selectedFile = fc.showOpenDialog(null);
        try {
            // Sets the selected file to a buffered image.
            bufferedImage = ImageIO.read(selectedFile);
            // Creates a color version for reference (Displays Left) and a 'working' image for manipulation (Displays Right)
            colorImage = SwingFXUtils.toFXImage(bufferedImage, null);
            buffWritableImg = SwingFXUtils.toFXImage(bufferedImage, null);
            workableImage = SwingFXUtils.toFXImage(bufferedImage, null);
            // Displays the details of the file selected.
            textFileName.setText(selectedFile.getName());
            textDimensions.setText(String.valueOf(colorImage.getWidth()) + " x " + String.valueOf(colorImage.getHeight()));
            double fileSizeMB = selectedFile.length() / ( 1024);
            textSize.setText(String.valueOf(fileSizeMB) + " KB");
            // Initialises the color active flag to "on"
            colorActive = true;
            // Displays Image in both sides of the window.
            imageDisp.setImage(colorImage);
            imageAffect.setImage(workableImage);
        } catch (IOException e) {
            System.out.println("Image failed to load.");
        }
        // Removes any previous instance of the rectangles outlining sheep.
        Main.display.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
        // Runs the "Field Initializer" with the imported and converted from buffered image.
        fieldInitializer(workableImage);
    }

    /**
     * Adjusts the working image on the right to isolate black and white
     */
    @FXML
    public void luminanceControl() {
        // Removes any previous instance of the rectangles outlining sheep.
        Main.display.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
        sliderTips(luminanceSlider, luminanceSlider.getValue());
        colorActive = false;
        double sliderValue = luminanceSlider.getValue();
        System.out.println("Luminance moved to: " + sliderValue);
        // If the Green Filter is off:
        if (!greenFilter) {
            // for each pixel in the buffered image, check it's value against the luminance controller
            for (int x = 0; x < bufferedImage.getWidth(); x++){
                for (int y = 0; y < bufferedImage.getHeight(); y++){
                    int p = bufferedImage.getRGB(x, y);
                    int a = (p>>24) & 0xff;
                    int r = (p>>16) & 0xff;
                    int g = (p>>8) & 0xff;
                    int b = p & 0xff;
                    // Luminance Calculation
                    double lum = (0.299 * r) + (0.587 * g) + (0.114 * b);
                    int minMax = lum > sliderValue ? 255 : 0;
                    int argb = (a<<24) | (minMax<<16) | (minMax<<8) |  minMax;
                    workableImage.getPixelWriter().setArgb(x, y, argb);
                }
            }
            imageAffect.setImage(workableImage);
            fieldInitializer(workableImage);
        }
        // if the green filter is on, the black and white slider performs:
        if (greenFilter) {
            for (int x = 0; x < bufferedImage.getWidth(); x++){
                for (int y = 0; y < bufferedImage.getHeight(); y++){
                    int p = bufferedImage.getRGB(x, y);
                    int a = (p>>24) & 0xff;
                    int r = (p>>16) & 0xff;
                    int g = 0;
                    int b = p & 0xff;
                    // Luminance calculation
                    double lum = (0.299 * r) + (0.587 * g) + (0.114 * b);
                    int minMax = lum > sliderValue ? 255 : 0;
                    int argb = (a<<24) | (minMax<<16) | (minMax<<8) |  minMax;
                    workableImage.getPixelWriter().setArgb(x, y, argb);
                }
            }
            imageAffect.setImage(workableImage);
            fieldInitializer(workableImage);
        }
    }

    /**
     * Removes the green channel from each pixel in the working image on the right.
     */
    @FXML
    public void greenChannel(){
        // Removes any previous instance of the rectangles outlining sheep.
        Main.display.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
        if (!greenFilter){
            System.out.println("Green Grass Killer Active");
            // apply green filter
            for (int x = 0; x < bufferedImage.getWidth(); x++){
                for (int y = 0; y < bufferedImage.getHeight(); y++){
                    int p = bufferedImage.getRGB(x, y);
                    int a = (p>>24) & 0xff;
                    int r = (p>>16) & 0xff;
                    int g = 0;
                    int b = p & 0xff;
                    int argb = (a<<24) | (r<<16) | (g<<8) |  b;
                    workableImage.getPixelWriter().setArgb(x, y, argb);
                }
            }
            imageAffect.setImage(workableImage);
            fieldInitializer(workableImage);
            greenFilter = true;
        }
        else if (greenFilter){
            System.out.println("Green Grass Killer DE-ACTIVE");
            // remove green filter
            for (int x = 0; x < bufferedImage.getWidth(); x++){
                for (int y = 0; y < bufferedImage.getHeight(); y++){
                    int p = bufferedImage.getRGB(x, y);
                    int a = (p>>24) & 0xff;
                    int r = (p>>16) & 0xff;
                    int g = (p>>8) & 0xff;
                    int b = p & 0xff;
                    int argb = (a<<24) | (r<<16) | (g<<8) |  b;
                    workableImage.getPixelWriter().setArgb(x, y, argb);
                }
            }
            imageAffect.setImage(workableImage);
            fieldInitializer(workableImage);
            greenFilter = false;
        }
    }

    /**
     * Removes all luminance and green filtering - replacing the image in original colour.
     */
    @FXML
    public void colorSensitivity(){
        // Removes any previous instance of the rectangles outlining sheep.
        Main.display.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
        colorActive = true;
        imageAffect.setImage(buffWritableImg);
        System.out.println("Colour Sensitivity: " + sensitivitySlider.getValue());
        sliderTips(sensitivitySlider, sensitivitySlider.getValue());
    }

    /**
     * Color Sensitivity sets the value of the pixel to be included. How WHITE is WHITE...
     * @return the sensitivity value from the slider with range 0 - 255.
     */
    public int getSensitivity(){
        sensitivity = (int)sensitivitySlider.getValue();
        System.out.println("Colour Sensitivity: " + sensitivity);
        return sensitivity;
    }

    /**
     * Adds a tool tip to each slider.
     * @param slider
     * @param sliderValue
     */
    @FXML
    public void sliderTips(Slider slider, Double sliderValue){
        String value = String.valueOf(sliderValue.intValue());
        Tooltip tip = new Tooltip();
        tip.setText(value);
        slider.setTooltip(tip);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MAIN EXECUTABLE METHOD FOR THE FOLLOWING THREE SECTIONS - PIXEL TO SHEEP METHODS AND SHEEP COMPUTATION/DISPLAY
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Button to execute the complete sheep calculation and display - depending on colour or B&W modes.
     */
    @FXML
    public void exeBahBahBAMCounter() {
        // Removes any previous instances of the rectangles.
        Main.display.getChildren().removeIf((x)->x.getClass()==Rectangle.class);
        try {
            if (colorActive) workableImage = SwingFXUtils.toFXImage(bufferedImage, null);
            if (!colorActive) luminanceControl();
            fieldInitializer(workableImage);
            findSheep(workableImage);
            sheepOutline();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PIXEL TO SHEEP METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialises an ArrayList containing all updated pixels from any tweaks of
     * filters to the working image.
     * @param img
     */
    @FXML
    public void fieldInitializer(WritableImage img){
        field = new ArrayList<>();
        rectangles = new ArrayList<>();
        pixelKids = new ArrayList<>();
        for (int y = 0; y < img.getHeight(); y ++){
            for (int x = 0; x < img.getWidth(); x ++){
                int argb = img.getPixelReader().getArgb(x, y);
                Pixel pixel = new Pixel(x, y, argb);
                field.add(pixel);
            }
        }
    }

    /**
     * Sheep Finder identifies sheep based on the sensitivity of the pixels in question
     * @param img Takes in the working image to be analysed.
     */
    public void findSheep(WritableImage img) {
        int sensitivity = getSensitivity();
        int width = (int) img.getWidth();
        // Goes through the entire array of pixels
        for (int i = 0; i < field.size(); i++) {
            // Checks the pixel to see if it is white / a sheep.
            if (argbChecker(field.get(i).getArgb(), sensitivity)) { // Am 'I' a White Pixel?
                // Sets the parent of the target pixel using the find method.
                field.get(i).setParent(findBAM(field.get(i)));
                // TO THE RIGHT > checks the pixel for white against the sensitivity and unions with the target.
                if (i < field.size() - 1) {
                    if (argbChecker(field.get(i + 1).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i + 1));
                    }
                }
                // BELOW > checks the pixel for white against the sensitivity and unions with the target.
                if (i < field.size() - width) {
                    if (argbChecker(field.get(i + width).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i + width));
                    }
                }
                // TO THE LEFT > checks the pixel for white against the sensitivity and unions with the target.
                if (i > 0) {
                    if (argbChecker(field.get(i - 1).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i - 1));
                    }
                }
                 // ABOVE > checks the pixel for white against the sensitivity and unions with the target.
                if (i > field.size() + width) {
                    if (argbChecker(field.get(i - width).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i - width));
                    }
                }
            }
        }
    }

    /**
     * A ternary, tail recursive approach to finding the parent of the parameterised pixel.
     * @param pixel
     * @return If the pixel has no parent or it's parent is itself, it is returned,
     *          otherwise, it has a parent, which is sent back into the method as a tail call recursion
     *          to find the eldest parent.
     */
    public Pixel findBAM(Pixel pixel){
        // Returns the original pixel if it is an orphan, or if it is a root. Otherwise, recursively seeks the root.
        return pixel.getParent() == null || pixel.getParent() == pixel ? pixel : findBAM(pixel.getParent());
    }

    /**
     * A Union method utilising the above find recursion to join adjacent white pixels.
     * @param target
     * @param surrounding
     */
    public void unionBAM(Pixel target, Pixel surrounding){
        Pixel surroundTemp = findBAM(surrounding);
        if (surroundTemp.getParent() == null) surroundTemp.setParent(findBAM(target));
    }

    /**
     * ARGB Tool for checking the pixels against the sensitivity feature.
     * @param argb Takes in the ARGB value of the target pixel
     * @param bamSensitivity Takes in the sensitivity (slider setting) of the pixel to be identified.
     * @return true if the pixel is whiter than the sensitivity value, or false if it is darker.
     */
    public boolean argbChecker(int argb, int bamSensitivity){
        int p = argb;
        int r = (p>>16) & 0xff;
        int g = (p>>8) & 0xff;
        int b =  p & 0xff;
        return r > bamSensitivity && g > bamSensitivity && b > bamSensitivity;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SHEEP COMPUTATION AND DISPLAY
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Outlier Filter sets the value of the size of the sheep to be min area range
     * @return the value from the slider with range 0 - 500.
     */
    public int getOutlierFilter(){
        outlier = (int)outlierSlider.getValue();
        sliderTips(outlierSlider, outlierSlider.getValue());
        return outlier;
    }

    /**
     * Finds the root pixels and their children, counting the number of sheep and outlining them in blue.
     */
    @FXML
    public void sheepOutline() {
        int sheepRedBoxCount = 0;
        int sheepBlueBoxCount = 0;
        int totalSheep;
        sheepTangles = new ArrayList<>();
        // for each pixel in the field
        for (int p = 0; p < field.size(); p ++) {
            if (field.get(p).getParent() == field.get(p)) { // This pixel is a root pixel
                // adds the first pixel to the count
                int sheepPixelKids = 1;
                // sets the root to be the square
                int xMin = field.get(p).getX();
                int yMin = field.get(p).getY();
                int xMax = field.get(p).getX();
                int yMax = field.get(p).getY();
                // find all the children
                for (int c = 0; c < field.size(); c ++) {
                    if (findBAM(field.get(c)) == field.get(p)) { // recursive call to see if a child matches to this root
                        // counts the number of children - method for sheep size estimates
                        sheepPixelKids += 1;
                        // if the child's x min is less than the previous min, make it the min.
                        if (field.get(c).getX() < xMin) {
                            xMin = field.get(c).getX();
                        }
                        // if the child's y min is less than the previous min, make it min.
                        if (field.get(c).getY() < yMin) {
                            yMin = field.get(c).getY();
                        }
                        // if the child's x max is bigger than the previous max, make it the max.
                        if (field.get(c).getX() > xMax) {
                            xMax = field.get(c).getX();
                        }
                        // if the child's y max is bigger than the previous max, make it the max.
                        if (field.get(c).getY() > yMax) {
                            yMax = field.get(c).getY();
                        }
                    }
                }
                // if the area of the square is bigger than the set min outlier, it is captured, else ignored.
                if (sheepPixelKids > getOutlierFilter()){
                    SheepTangle rectangle = new SheepTangle(xMin, xMax, yMin, yMax, sheepPixelKids);
                    rectangles.add(rectangle);
                    // creates an array of the pixel count groupings needed for Standard Deviation and IQR
                    pixelKids.add(sheepPixelKids);
                }
            }
        }
        System.out.println("Outlier Slider: " + outlier);
        // Compute the IQR
        IQR();
        // Compute the Mean and Standard Deviation
        int total = 0;
        for (int i = Q1; i < Q3; i ++){
            total += pixelKids.get(i);
        }
        double mean = total / pixelKids.size();
        System.out.println("Mean: " + mean);
        double sd = 0;
        for (int i = 0; i < pixelKids.size(); i++)
        {
            sd += Math.pow(pixelKids.get(i) - mean, 2) / pixelKids.size();
        }
        double standDev = Math.sqrt(sd);
        System.out.println("StandDev: " + standDev);

        for (int i = 0; i < rectangles.size(); i ++){
            int pixCount = rectangles.get(i).getPixelKids();
            if (pixCount > (mean - standDev) && pixCount <= (mean + standDev)) { // one sheep size in the IQR
                rectangles.get(i).setSheepEstimate(1);
                rectangleGenerator(rectangles.get(i).getxMin(),
                        rectangles.get(i).getxMax(),
                        rectangles.get(i).getyMin(),
                        rectangles.get(i).getyMax(),
                        rectangles.get(i).getSheepEstimate(),
                        Color.RED);
                sheepRedBoxCount += 1;
                sheepTangles.add(rectangles.get(i));
            }
            if (pixCount > (mean + standDev) && pixCount < (mean + (10 * standDev))) {
                // Determines the amount of sheep inside a blue box by dividing the total pixels by the standard deviation.
                int blueEstimate = (int) (pixCount / standDev);
                rectangles.get(i).setSheepEstimate(blueEstimate);
                rectangleGenerator(rectangles.get(i).getxMin(),
                        rectangles.get(i).getxMax(),
                        rectangles.get(i).getyMin(),
                        rectangles.get(i).getyMax(),
                        rectangles.get(i).getSheepEstimate(),
                        Color.BLUE);
                sheepBlueBoxCount += blueEstimate;
                sheepTangles.add(rectangles.get(i));
            }
        }
        totalSheep = sheepRedBoxCount + sheepBlueBoxCount;
        sheepCountDisp.setText(String.valueOf(totalSheep));
        imageAffect.setImage(workableImage);
    }

    /**
     * Determines the working image's location for rectangles x, y correction within the pane.
     * @return
     */
    public double getImgMinY(){
        double parentY = 152;
        double paneY = imageAffect.getBoundsInParent().getMinY();
        return parentY + paneY;
    }

    /**
     * Creates a rectangle to display onscreen including tooltips to show estimates when multiple sheep cluster.
     * @param xMin min value to generate the rectangle
     * @param xMax max value to generate the rectangle
     * @param yMin min value to generate the rectangle
     * @param yMax max value to generate the rectangle
     * @param sheepEstimate Piped in for use in the tooltip
     * @param color Red for singles and blue for multiple sheep
     */
    public void rectangleGenerator(int xMin, int xMax, int yMin, int yMax, int sheepEstimate, Color color){
        double heightScale = imageAffect.getBoundsInParent().getHeight() / imageAffect.getImage().getHeight();
        double widthScale = imageAffect.getBoundsInParent().getWidth() / imageAffect.getImage().getWidth();
        double imgXmin = 772; // Hard code setting for the xmin in the pane as per FXML file.
        double imgYmin = getImgMinY();
        Rectangle r = new Rectangle();
        r.setX((xMin * widthScale) + imgXmin);
        r.setY((yMin * heightScale) + imgYmin);
        double width = (xMax - xMin) * widthScale;
        double height = (yMax - yMin) * heightScale;
        r.setWidth(width);
        r.setHeight(height);
        r.setStroke(color);
        r.setFill(Color.TRANSPARENT);
        r.setVisible(true);
        Tooltip tip = new Tooltip();
        tip.setText("Sheep Estimated: " + String.valueOf(sheepEstimate));
        Tooltip.install(r, tip);
        Main.display.getChildren().add(r);
    }


    /**
     * Determines the Inter-Quartile Range among the pixel kids list.
     */
    public void IQR(){
        Collections.sort(pixelKids);
        int length = pixelKids.size();
        Q1 = (int) (.25 * length);
        Q3 = (int) (.75 * length);
    }

    /**
     * Exits the program
     */
    @FXML
    public void exitProgram() {
        Platform.exit();
        System.exit(0);
    }
}
