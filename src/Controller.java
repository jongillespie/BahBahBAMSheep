
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.image.*;
import org.omg.CORBA.PUBLIC_MEMBER;

import javax.imageio.ImageIO;
import javax.swing.text.Style;
import javax.tools.Tool;

import java.awt.MouseInfo;

/**
 * Main Controller manages all image loading, filter effects and display execution.
 * Additionally, in section two - contains all relevant Union-Find-Compression methods to
 * determine sheep quantity and shape outlines.
 */
public class Controller {

    @FXML private ImageView imageDisp, imageAffect;
    @FXML private javafx.scene.text.Text textFileName, textSize, textDimensions, outlierDispText, sheepCountDisp;
    @FXML private Slider luminanceSlider, sensitivitySlider, outlierSlider;

    private BufferedImage bufferedImage;
    private WritableImage buffWritableImg, colorImage, workableImage;
    private Boolean colorActive, greenFilter = false;
    private ArrayList<Pixel> field;
    private ArrayList<Rectangle> rectangles, sheepTangles;
    private ArrayList<Integer> rectsArea, pixelKids;
    private int sensitivity, outlier, luminance, Q1, Q2, Q3, IQR;

    private double Q1area, Q2area, Q3area, IQRarea;

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
        // Runs the "Field Initializer"
        fieldInitializer(workableImage);
    }

    /**
     * Adjusts the working image on the right to isolate black and white
     */
    @FXML
    public void luminanceControl() {
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
                    //Old: Calculation of Simple Average: int avg = (r+g+b)/3;
                    //New: Luminance calculation
                    double lum = (0.299 * r) + (0.587 * g) + (0.114 * b);
                    int minMax = lum > sliderValue ? 255 : 0;
                    int argb = (a<<24) | (minMax<<16) | (minMax<<8) |  minMax;

                    //TODO  TRY doing a new image write after the green filter!!

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
                    //Old: Calculation of Simple Average: int avg = (r+g+b)/3;
                    //New: Luminance calculation
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
    private void colorSensitivity(){
        colorActive = true;
        imageAffect.setImage(buffWritableImg);
        System.out.println("Colour Sensitivity: " + sensitivitySlider.getValue());
        sliderTips(sensitivitySlider, sensitivitySlider.getValue());
    }


    @FXML
    private void sliderTips(Slider slider, Double sliderValue){
        String value = String.valueOf(sliderValue.intValue());
        Tooltip tip = new Tooltip();
        tip.setText(value);
        slider.setTooltip(tip);
    }

    /**
     * Color Sensitivity sets the value of the pixel to be included. How WHITE is WHITE...
     * @return the sensitivity value from the slider with range 0 - 255.
     */
    private int getSensitivity(){
        sensitivity = (int)sensitivitySlider.getValue();
        System.out.println("Colour Sensitivity: " + sensitivity);
        return sensitivity;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SHEEP IDENTIFICATION CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Outlier Filter sets the value of the size of the sheep to be min area range
     * @return the value from the slider with range 0 - 500.
     */
    private int getOutlierFilter(){
        outlier = (int)outlierSlider.getValue();
        sliderTips(outlierSlider, outlierSlider.getValue());
        return outlier;
    }

    /**
     * Button to execute the complete sheep calculation and display - depending on colour or B&W modes.
     */
    @FXML
    private void exeBahBahBAMCounter() {
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

    /**
     * Initialises an ArrayList containing all updated pixels from any tweaks of
     * filters to the working image.
     * @param img
     */
    @FXML
    public void fieldInitializer(WritableImage img){
        field = new ArrayList<>();
        rectangles = new ArrayList<>();
        rectsArea = new ArrayList<>();
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
        int sensitivity = getSensitivity(); // TODO look into this , the argb value is being a pain
        int width = (int) img.getWidth();
        // Goes through the entire array of pixels
        for (int i = 0; i < field.size(); i++) {
            // Checks the pixel to see if it is white / a sheep.
            // System.out.println("the ARGB value is:" + field.get(i).getArgb());
            if (argbChecker(field.get(i).getArgb(), sensitivity)) { // Am 'I' a White Pixel?
//                System.out.println("ARGB: " + field.get(i).getArgb());


                field.get(i).setParent(findBAM(field.get(i)));
//                for (int t = 1; t < 2; t++){
//                    // TO THE RIGHT
//                    if (i < field.size() - t) {
//                        if (argbChecker(field.get(i + t).getArgb(), sensitivity)) {
//                            unionBAM(field.get(i), field.get(i + t));
//                        }
//                    }
//                    // TO THE LEFT
//                    if (i > t) {
//                        if (argbChecker(field.get(i - t).getArgb(), sensitivity)) {
//                            unionBAM(field.get(i), field.get(i - t));
//                        }
//                    }
//                }
//                for (int t = width; t < width * 2; width++){
//                    // ABOVE
//                    if (i > field.size() + t) {
//                        if (argbChecker(field.get(i - t).getArgb(), sensitivity)) {
//                            unionBAM(field.get(i), field.get(i - t));
//                        }
//                    }
//                    // BELOW
//                    if (i < field.size() - t) {
//                        if (argbChecker(field.get(i + t).getArgb(), sensitivity)) {
//                            unionBAM(field.get(i), field.get(i + t));
//                        }
//                    }
//                }

                // TO THE RIGHT
                if (i < field.size() - 1) {
                    if (argbChecker(field.get(i + 1).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i + 1));
                    }
                }
                // BELOW
                if (i < field.size() - width) {
                    if (argbChecker(field.get(i + width).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i + width));
                    }
                }
                // TO THE LEFT
                if (i > 0) {
                    if (argbChecker(field.get(i - 1).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i - 1));
                    }
                }
                 // ABOVE
                if (i > field.size() + width) {
                    if (argbChecker(field.get(i - width).getArgb(), sensitivity)) {
                        unionBAM(field.get(i), field.get(i - width));
                    }
                }
            }
        }
    }

    private Pixel findBAM(Pixel pixel){
        // Returns the original pixel if it is an orphan, or if it is a root. Otherwise, recursively seeks the root.
        return pixel.getParent() == null || pixel.getParent() == pixel ? pixel : findBAM(pixel.getParent());
    }

    public void unionBAM(Pixel target, Pixel surrounding){
        Pixel surroundTemp = findBAM(surrounding);
        if (surroundTemp.getParent() == null) surroundTemp.setParent(findBAM(target));
    }

    /**
     * ARGB Tool for checking the pixels against the sensitivity feature. COLOUR
     * @param argb Takes in the ARGB value of the target pixel
     * @param bamSensitivity Takes in the sensitivity of the pixel to be identified.
     * @return true if the pixel is whiter than the sensitivity value, or false if it is darker.
     */
    private boolean argbChecker(int argb, int bamSensitivity){
        int p = argb;
        int a = (p>>24) & 0xff;
        int r = (p>>16) & 0xff;
        int g = (p>>8) & 0xff;
        int b =  p & 0xff;
//        double lum = (0.299 * r) + (0.587 * g) + (0.114 * b);
//        return lum > bamSensitivity ? true : false;
        return r > bamSensitivity && g > bamSensitivity && b > bamSensitivity;
    }

    //TODO delete below if not going to display the number outlier on screen
//    @FXML void outlierDisp(){
//        workableImage = buffWritableImg;
//        findSheep(workableImage);
//        imageAffect.setImage(workableImage);
//        outlierDispText.setText(String.valueOf((int)outlierSlider.getValue()));
//    }

//    private Pixel findChildren(Pixel pixel){
//        return pixel.getParent() == null || pixel.getParent() == pixel ? pixel : findBAM(pixel.getParent());
//    }
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

                int sheepPixelKids = 1;

                // sets the root to be the square
                int xMin = field.get(p).getX();
                int yMin = field.get(p).getY();
                int xMax = field.get(p).getX();
                int yMax = field.get(p).getY();
                // find all the children
                for (int c = 0; c < field.size(); c ++) {
                    if (findBAM(field.get(c)) == field.get(p)) { // recursive call to see if a child matches to this root

                        // counts the number of children -  new method for sheep size estimates // TODO TEST!
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
                // calculate the size of the square
                int length = xMax - xMin;
                int height = yMax - yMin;
                int area = length * height;
                // if the area of the square is bigger than the set min outlier, it is captured, else ignored.
                if (sheepPixelKids > getOutlierFilter()){
                    Rectangle rectangle = new Rectangle(xMin, xMax, yMin, yMax, area, sheepPixelKids);
                    rectangles.add(rectangle);
                    // Area stored separately for Inter-quartile Range Calculations.
                    rectsArea.add(area);
                    // creates an array of the pixel count groupings needed for sd and IQR
                    pixelKids.add(sheepPixelKids);
                }
            }
        }
        System.out.println("Outlier Slider: " + outlier);

        // Compute the IQR
        IQR();

        PixelWriter rects = workableImage.getPixelWriter();

        Tooltip.install(imageAffect, new Tooltip());

        int total = 0;
        for (int i = Q1; i < pixelKids.size(); i ++){
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
            if (pixCount > (mean - standDev) && pixCount <= (mean + standDev)) { //|| pixCount < (mean + 2*standDev) && pixCount >= (mean + standDev)) { // one sheep size
                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
                    rects.setColor(x, rectangles.get(i).getyMin(), Color.RED);
                    rects.setColor(x, rectangles.get(i).getyMax(), Color.RED);
                }
                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
                    rects.setColor(rectangles.get(i).getxMin(), y, Color.RED);
                    rects.setColor(rectangles.get(i).getxMax(), y, Color.RED);
                }
                sheepRedBoxCount += 1;
                //sheepRedBoxAreaSum += pixCount;
                rectangles.get(i).setSheepEstimate(1);
                sheepTangles.add(rectangles.get(i));

            }
            if (pixCount > (mean + standDev) && pixCount < (mean + (50 * standDev))) { // more than one sheep size         //&& area <= Q3) {
                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
                    rects.setColor(x, rectangles.get(i).getyMin(), Color.BLUE);
                    rects.setColor(x, rectangles.get(i).getyMax(), Color.BLUE);
                }
                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
                    rects.setColor(rectangles.get(i).getxMin(), y, Color.BLUE);
                    rects.setColor(rectangles.get(i).getxMax(), y, Color.BLUE);
                }
                sheepBlueBoxCount += 1;
                //sheepBlueBoxAreaSum += pixCount;
                rectangles.get(i).setSheepEstimate(2);
                sheepTangles.add(rectangles.get(i));
            }
        }
        totalSheep = sheepRedBoxCount + sheepBlueBoxCount;
        sheepCountDisp.setText(String.valueOf(totalSheep));
        imageAffect.setImage(workableImage);
    }




    @FXML
    public void sheepTips() {
        Tooltip tip = new Tooltip();
        imageAffect.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double mouseX = event.getX();
                double mouseY = event.getY();
                for (Rectangle sheep : sheepTangles) {
                    if (mouseX >= sheep.getxMin()
                            && mouseX <= sheep.getxMax()
                            && mouseY >= sheep.getyMin()
                            && mouseY <= sheep.getyMax()) {
                        String sheepTip = "Estimated Sheep: " + String.valueOf(sheep.getSheepEstimate());
                        System.out.println("Estimated Sheep: " + sheep.getSheepEstimate());
                        System.out.println("inside the sheepTipCheck");
                        tip.setText(sheepTip);
                        Tooltip.install(imageAffect, tip);
                    }
                }
            }
        });
    }

    public void standardDeviation(){
        int total = 0;
        for (int i = 0; i < rectsArea.size(); i ++){
            total += rectsArea.get(i);
        }
        double mean = total / rectsArea.size();
        System.out.println("Mean: " + mean);
        double sd = 0;
        for (int i = 0; i < rectsArea.size(); i++)
        {
            sd += Math.pow(rectsArea.get(i) - mean, 2) / rectsArea.size();
        }
        double standDev = Math.sqrt(sd);
        System.out.println("StandDev: " + standDev);
    }

    //todo change the IQR to be of the average size of the boxes??
    public void IQR(){
        Collections.sort(pixelKids);
        int length = pixelKids.size();
        System.out.println("Length " + length);
        Q1 = (int) (.25 * length);
        int pix1 = pixelKids.get(Q1);
        System.out.println("Q1: " + Q1);
        System.out.println("Q1 Pix: " + pix1);
        Q3 = (int) (.75 * length);
        int pix3 = pixelKids.get(Q3);
        System.out.println("Q3: " + Q3);
        System.out.println("Q3 Pix: " + pix3);
        IQR = (pix3 - pix1); // TODO this might not be right. its is giving me the middle...
        System.out.println("IQR: " + IQR);
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


/**
 * Junit test what you can - just the methods, dont worry about it if you cant its likely the FX is giving problem
 * Seperate the load of the image in the controller and make it a public - then try it in the unit test
 *
 * controller luminance calculation needs ot change using the math form google.
 * union of sheep they could be diagonal, this logic needs to change in the union and then should fix the rectangle.
 *
 * Minimise objexts by only represnetingt the actual sheep instead of the pixels?
 *
 *
 * https://en.wikipedia.org/wiki/Relative_luminance
 */

//    /**
//     * Finds the root pixels and their children, counting the number of sheep and outlining them in blue.
//     */
//    @FXML
//    public void sheepOutline() {
//        int sheepRedBoxCount = 0;
//        int sheepBlueBoxCount = 0;
//        int sheepRedBoxAreaSum = 0;
//        int sheepBlueBoxAreaSum = 0;
//        int sheepBoxAreaAvg;
//        int totalSheep;
//
//        // for each pixel in the field
//        for (int p = 0; p < field.size(); p ++) {
//            if (field.get(p).getParent() == field.get(p)) { // This pixel is a root pixel
//                // sets the root to be the square
//                int xMin = field.get(p).getX();
//                int yMin = field.get(p).getY();
//                int xMax = field.get(p).getX();
//                int yMax = field.get(p).getY();
//                // find all the children
//                for (int c = 0; c < field.size(); c ++) {
//                    if (findBAM(field.get(c)) == field.get(p)) { // recursive call to see if a child matches to this root
//                        // if the child's x min is less than the previous min, make it the min.
//                        if (field.get(c).getX() < xMin) {
//                            xMin = field.get(c).getX();
//                        }
//                        // if the child's y min is less than the previous min, make it min.
//                        if (field.get(c).getY() < yMin) {
//                            yMin = field.get(c).getY();
//                        }
//                        // if the child's x max is bigger than the previous max, make it the max.
//                        if (field.get(c).getX() > xMax) {
//                            xMax = field.get(c).getX();
//                        }
//                        // if the child's y max is bigger than the previous max, make it the max.
//                        if (field.get(c).getY() > yMax) {
//                            yMax = field.get(c).getY();
//                        }
//                    }
//                }
//                // calculate the size of the square
//                int length = xMax - xMin;
//                int height = yMax - yMin;
//                int area = length * height;
//                // if the area of the square is bigger than the set min outlier, it is captured, else ignored.
//                if (area > getOutlierFilter()){
//                    Rectangle rectangle = new Rectangle(xMin, xMax, yMin, yMax, area);
//                    rectangles.add(rectangle);
//                    // Area stored separately for Inter-quartile Range Calculations.
//                    rectsArea.add(area);
//                }
//            }
//        }
//        System.out.println("Outlier Slider: " + outlier);
//        // Compute the IQR
//        IQR();
//
//        PixelWriter rects = workableImage.getPixelWriter();
//
////        for (int i = 0; i < rectangles.size(); i ++){
////            int area = rectangles.get(i).getArea();
////            if (area > rectangles.get(Q2).getArea() && area <= rectangles.get(Q3).getArea()) { // one sheep size
////                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
////                    rects.setColor(x, rectangles.get(i).getyMin(), Color.RED);
////                    rects.setColor(x, rectangles.get(i).getyMax(), Color.RED);
////                }
////                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
////                    rects.setColor(rectangles.get(i).getxMin(), y, Color.RED);
////                    rects.setColor(rectangles.get(i).getxMax(), y, Color.RED);
////                }
////                sheepRedBoxCount += 1;
////                sheepRedBoxAreaSum += area;
////            }
////            if (area > rectangles.get(Q3).getArea() ) { // more than one sheep size         //&& area <= Q3) {
////                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
////                    rects.setColor(x, rectangles.get(i).getyMin(), Color.BLUE);
////                    rects.setColor(x, rectangles.get(i).getyMax(), Color.BLUE);
////                }
////                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
////                    rects.setColor(rectangles.get(i).getxMin(), y, Color.BLUE);
////                    rects.setColor(rectangles.get(i).getxMax(), y, Color.BLUE);
////                }
////                sheepBlueBoxCount += 1;
////                sheepBlueBoxAreaSum += area;
////            }
////        }
////        sheepBoxAreaAvg = sheepRedBoxAreaSum / sheepRedBoxCount; // TODO fix the arithmetic exception of divide by zero.
////        totalSheep = (sheepBlueBoxAreaSum / sheepBoxAreaAvg) + (sheepRedBoxAreaSum / sheepBoxAreaAvg);
//
//
//
//        int total = 0;
//        for (int i = 0; i < rectsArea.size(); i ++){
//            total += rectsArea.get(i);
//        }
//        double mean = total / rectsArea.size();
//        System.out.println("Mean: " + mean);
//        double sd = 0;
//        for (int i = 0; i < rectsArea.size(); i++)
//        {
//            sd += Math.pow(rectsArea.get(i) - mean, 2) / rectsArea.size();
//        }
//        double standDev = Math.sqrt(sd);
//        System.out.println("StandDev: " + standDev);
//
//        for (int i = 0; i < rectangles.size(); i ++){
//            int area = rectangles.get(i).getArea();
//            if (area > mean - 2*standDev && area <= mean - standDev || area < mean + 2*standDev && area >= mean + standDev) { // one sheep size
//                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
//                    rects.setColor(x, rectangles.get(i).getyMin(), Color.RED);
//                    rects.setColor(x, rectangles.get(i).getyMax(), Color.RED);
//                }
//                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
//                    rects.setColor(rectangles.get(i).getxMin(), y, Color.RED);
//                    rects.setColor(rectangles.get(i).getxMax(), y, Color.RED);
//                }
//                sheepRedBoxCount += 1;
//                sheepRedBoxAreaSum += area;
//            }
//            if (area < mean - standDev && area > mean + standDev) { // more than one sheep size         //&& area <= Q3) {
//                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++) {
//                    rects.setColor(x, rectangles.get(i).getyMin(), Color.BLUE);
//                    rects.setColor(x, rectangles.get(i).getyMax(), Color.BLUE);
//                }
//                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++) {
//                    rects.setColor(rectangles.get(i).getxMin(), y, Color.BLUE);
//                    rects.setColor(rectangles.get(i).getxMax(), y, Color.BLUE);
//                }
//                sheepBlueBoxCount += 1;
//                sheepBlueBoxAreaSum += area;
//            }
//        }
//
//        totalSheep = sheepRedBoxCount + sheepBlueBoxCount;
//
//
////        System.out.println("----------------------------------");
////        System.out.println("Red Box Count:  " + sheepRedBoxCount);
////        System.out.println("Red Area Sum:   " + sheepRedBoxAreaSum);
////        System.out.println("Average of Red: " + sheepBoxAreaAvg);
////        System.out.println("---");
////        System.out.println("Blue Box Count: " + sheepBlueBoxCount);
////        System.out.println("Blue Area Sum:  " + sheepBlueBoxAreaSum);
//        sheepCountDisp.setText(String.valueOf(totalSheep));
//        imageAffect.setImage(workableImage);
//    }

// SHAPE FOR RECTANGLE - NEED TO SCALE... FX ID FOR THE PANE ... GETLAYOUT GETS X Y OF THE IMAGE IN THE PANE .. SET LAYOUT X FOR THE RECTANGLE THAT WAY.. DO THE HEIGHT AND LENGHT IN SIDE THE OBJECT .. ADD A STROKE ... ADD A FILL TO TRANSPARENT .. SETVISIBLE JAVAFX.SCENE.SHAPE.RECTANGLE