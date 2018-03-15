
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
    private WritableImage colorImage, workableImage;
    private Boolean colorActive, greenFilter = false;
    private ArrayList<Pixel> field;
    private ArrayList<Rectangle> rectangles;
    private ArrayList<Integer> rectsArea;
    private int sensitivity;

    private double Q1, Q2, Q3, IQR;

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
            System.out.println("Green Grass Killer");
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
            System.out.println("Green Grass Grower");
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SHEEP IDENTIFICATION CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Trigger for the movement of the sensitivity slider.
     */
    @FXML
    public void sensitivity(){
        findSheep(workableImage);
    }

    /**
     * Bam Sensitivity sets the value of the pixel to be included. How WHITE is WHITE...
     * @return the sensitivity value from the slider with range 0 - 255.
     */
    private int getSensitivity(){
        sensitivity = (int)sensitivitySlider.getValue();
//        System.out.println("Sensitivity: " + sensitivity);
        return sensitivity;
    }

    /**
     * Initialises an ArrayList containing all updated pixels from any tweaks of
     * filters to the working image.
     * @param img
     */
    public void fieldInitializer(WritableImage img){
        field = new ArrayList<>();
        rectangles = new ArrayList<>();
        rectsArea = new ArrayList<>();
        for (int y = 0; y < img.getHeight(); y ++){
        for (int x = 0; x < img.getWidth(); x ++){

                int argb = img.getPixelReader().getArgb(x, y);
                Pixel pixel = new Pixel(x, y, argb);
                field.add(pixel);
            }
        }
        findSheep(img);
    }

    /**
     * Sheep Finder identifies sheep based on the sensitivity of the pixels in question
     * @param img Takes in the working image to be analysed.
     */
    public void findSheep(WritableImage img) {
        int sensitivity = getSensitivity();// * 16670704; // TODO look into this , the argb value is being a pain
        int width = (int) img.getWidth();
        // Goes through the entire array of pixels
        for (int i = 0; i < field.size(); i++) {
            // Checks the pixel to see if it is white / a sheep.
            // System.out.println("the ARGB value is:" + field.get(i).getArgb());
            if (argbChecker(field.get(i).getArgb(), sensitivity)) { // Am 'I' a White Pixel?

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
//        System.out.println("End of find sheep");
//        for (int M = 0; M < field.size(); M++) {
//            if (findBAM(field.get(M)) == field.get(M)) {
//                System.out.println("-----------------------------------------");
//                System.out.println("INDEX: " + M + " Root Pixel" + field.get(M) + " - X: " + field.get(M).getX() + " Y: " + field.get(M).getY());
//
//            }
//            for (int N = 0; N < field.size(); N++) {
//                if (findBAM(field.get(N)) == field.get(M)) {
//                    System.out.println("INDEX: " + N + " Child Pixel" + field.get(N) + " - X: " + field.get(N).getX() + " Y: " + field.get(N).getY() + " PARENT: " + field.get(M));
//                }
//            }
//        }
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

    @FXML void outlierDisp(){ //TODO add in an image reset!!
        findSheep(workableImage);
        outlierDispText.setText(String.valueOf((int)outlierSlider.getValue()));
    }

//    public Pixel childParentRecurse(Pixel parent, Pixel child){
//        return child.getParent() == parent ? child.getParent() : childParentRecurse(parent, child.getParent().getParent());
//        }


        //return child.getParent() == parent ? child : null;

    private Pixel findChildren(Pixel pixel){
        return pixel.getParent() == null || pixel.getParent() == pixel ? pixel : findBAM(pixel.getParent());
    }
    /**
     * Finds the root pixels and their children, counting the number of sheep and outlining them in blue.
     */
    @FXML
    public void sheepOutline() { //int size){
        int outlier = (int)outlierSlider.getValue();
        int sheepCount = 0;
        PixelWriter rects = workableImage.getPixelWriter();
        // for each pixel in the field
        for (int p = 0; p < field.size(); p ++) {
            if (field.get(p).getParent() == field.get(p)) { // This pixel is a root pixel
                // sets the root to be the square
                int xMin = field.get(p).getX();
                int yMin = field.get(p).getY();
                int xMax = field.get(p).getX();
                int yMax = field.get(p).getY();
                // find all the children
                for (int c = 0; c < field.size(); c ++) {
                    if (findBAM(field.get(c)) == field.get(p)) { // recursive call to see if a child matches to this root
                        // if the child's x max is bigger than the previous max, make it the max.
                        if (field.get(c).getX() > xMax) {
                            xMax = field.get(c).getX();
//                              System.out.println("xMaxChild:" + xMax +  "  NODE: " + field.get(p) + "  ChiledNOde: " + field.get(c));
                        }
                        // if the child's y max is bigger than the previous max, make it the max.
                        if (field.get(c).getY() > yMax) {
                            yMax = field.get(c).getY();
//                               System.out.println("xMaxChild:" + xMax  + "  NODE: "+ field.get(p) + "  ChiledNOde: "  + field.get(c) );
                        }
                    }
                }
                // calculate the size of the square
                int length = xMax - xMin;
                int height = yMax - yMin;
                int area = length * height;
//                                        System.out.println("xMax: " + xMax + "  xMin: " + xMin + "||  yMin: " + yMin + "  yMax: " + yMax);
//                                        System.out.println("Length: " + length + "  Height: " + height);
//                                        System.out.println("Area: " + area);
//                                        System.out.println("--------------------------------------------------------------------------------");
                if (area > outlier){
//                    System.out.println("Sheep size: " + area);
                    sheepCount += 1;

                    Rectangle rectangle = new Rectangle(xMin, xMax, yMin, yMax, area);
                    rectangles.add(rectangle);
                    rectsArea.add(area);
                    System.out.println("Area: " + area);
//                    for (int x = xMin; x <= xMax; x++){
//                        rects.setColor(x, yMin, Color.RED);
//                        rects.setColor(x, yMax, Color.YELLOW);
//                    }
//                    for (int y = yMin; y <= yMax; y++){
//                        rects.setColor(xMin, y, Color.BLUE);
//                        rects.setColor(xMax, y, Color.GREEN);
//                    }

                }



                // draw the box

            }
        }
        // TEST THE RECTS
//        int xMa = 400;
//        int yMa = 400;
//        int xMi = 100;
//        int yMi = 100;
//                for (int x = xMi; x <= xMa; x++){
//                    rects.setColor(x, yMi, Color.BLUE);
//                    rects.setColor(x, yMa, Color.BLUE);
//                }
//                for (int y = yMi; y <= yMa; y++){
//                    rects.setColor(xMi, y, Color.BLUE);
//                    rects.setColor(xMa, y, Color.BLUE);
//                }


//        System.out.println("Total Sheep: " + sheepCount);

        // Compute the IQR
        IQR();
        for (int i = 0; i < rectangles.size(); i ++){
            if (rectangles.get(i).getArea() > Q1)
                for (int x = rectangles.get(i).getxMin(); x <= rectangles.get(i).getxMax(); x++){
                    rects.setColor(x, rectangles.get(i).getyMin(), Color.RED);
                    rects.setColor(x, rectangles.get(i).getyMax(), Color.YELLOW);
                }
                for (int y = rectangles.get(i).getyMin(); y <= rectangles.get(i).getyMax(); y++){
                    rects.setColor(rectangles.get(i).getxMin(), y, Color.BLUE);
                    rects.setColor(rectangles.get(i).getxMax(), y, Color.GREEN);
            }
        }


        sheepCountDisp.setText(String.valueOf(sheepCount));
    }



    public void IQR(){
        Collections.sort(rectsArea);
        int length = rectsArea.size();
        System.out.println("Length: " + length);
        Q1 = .25 * length;
        //double Q2 = .5 * length;
        Q3 = .75 * length;
        IQR = (int) (Q3 - Q1);
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