
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
    @FXML private javafx.scene.text.Text textFileName, textSize, textDimensions, sheepCountDisp;
    @FXML private Slider luminanceSlider;
    @FXML private Slider bamSizeSlider;

    private BufferedImage bufferedImage;
    private WritableImage colorImage, workableImage;
    private Boolean colorActive, greenFilter = false;
    private ArrayList<Pixel> field;
    private int bamSensitivity;

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
        System.out.println("Bam-o-lator moved to: " + sliderValue);
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
                    if (r <= sliderValue && g <= sliderValue && b <= sliderValue){
                        r = 0;
                        g = 0;
                        b = 0;
                    } else if (r > sliderValue && g > sliderValue && b > sliderValue){
                        r = 255;
                        g = 255;
                        b = 255;
                    }
                    int argb = (a<<24) | (r<<16) | (g<<8) |  b;
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
                    // green filter on so green stays at 0
                    int g = 0;
                    int b = p & 0xff;
                    if (r <= sliderValue && b <= sliderValue){
                        System.out.println("> Red: " + r);
                        r = 0;
                       // g = 0;
                        b = 0;
                    } else if (r > sliderValue && b > sliderValue){
                        System.out.println("< Red: " + r);
                        r = 255;
                        // if needs to be white, green goes back to 255
                        g = 255;
                        b = 255;
                    }
                    int argb = (a<<24) | (r<<16) | (g<<8) | b;
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
     * Trigger for the movement of the Bam-sensitivity slider.
     */
    @FXML
    public void bamSensitivity(){
        findSheep(workableImage);
    }

    /**
     * Bam Sensitivity sets the value of the pixel to be included. How WHITE is WHITE...
     * @return the sensitivity value from the slider with range 0 - 255.
     */
    private int getBamSensitivity(){
        bamSensitivity = (int)bamSizeSlider.getValue();
        System.out.println("Sensitivity: " + bamSensitivity);
        return bamSensitivity;
    }

    /**
     * Initialises an ArrayList containing all updated pixels from any tweaks of
     * filters to the working image.
     * @param img
     */
    public void fieldInitializer(WritableImage img){
        field = new ArrayList<>();
        for (int x = 0; x < img.getWidth(); x ++){
            for (int y = 0; y < img.getHeight(); y ++){
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
        int sensitivity = getBamSensitivity();
        int width = (int) img.getWidth();
        // Goes through the entire array of pixels
        for (int i = 0; i < field.size(); i++) {
            // Checks the pixel to see if it is white / a sheep.
            // System.out.println("the ARGB value is:" + field.get(i).getArgb());
            if (argbChecker(field.get(i).getArgb(), sensitivity)) {
                // If Pixel has no parent, it becomes a root.
                if (field.get(i).getParent() == null) {
                    field.get(i).setParent(field.get(i));
                }
                if (i < field.size() - 1) {
                    if (argbChecker(field.get(i + 1).getArgb(), sensitivity)) {
                        field.get(i + 1).setParent(field.get(i).getParent());
                    }
                }
                if (i < field.size() - width) {
                    if (argbChecker(field.get(i + width).getArgb(), sensitivity)) {
                        field.get(i + width).setParent(field.get(i).getParent());
                    }
                }
            }
//        System.out.println("End of find sheep");
//        for (int i = 0; i < field.size(); i ++){
//            if (field.get(i).getParent() == field.get(i)){
//                System.out.println("Root Pixel");
//            }
//        }
        }
    }

    /**
     * ARGB Tool for checking the pixels against the sensitivity feature.
     * @param argb Takes in the ARGB value of the target pixel
     * @param bamSensitivity Takes in the sensitivity of the pixel to be identified.
     * @return true if the pixel is whiter than the sensitivity value, or false if it is darker.
     */
    private boolean argbChecker(int argb, int bamSensitivity){
        int p = argb;
        //int a = (p>>24) & 0xff;
        int r = (p>>16) & 0xff;
        int g = (p>>8) & 0xff;
        int b =  p & 0xff;
        return r > bamSensitivity && g > bamSensitivity && b > bamSensitivity;
    }

    /**
     * Finds the root pixels and their children, counting the number of sheep and outlining them in blue.
     */
    @FXML
    public void sheepOutline() { //int size){
        int sheepCount = 0;
        // for each pixel in the field
        for (int i = 0; i < field.size(); i ++) {
            // if the pixel is a root
            if (field.get(i).getParent() == field.get(i)) {
                // sets the root to be the square
                int xMin = field.get(i).getX();
                int yMin = field.get(i).getY();
                int xMax = field.get(i).getX();
                int yMax = field.get(i).getY();
                // find all the children
                for (int p = 0; p < field.size(); p ++) {
                    // checks the child is of the root
                    if (field.get(p).getParent() == field.get(i)){
                        // if the child's x max is bigger than the previous max, make it the max.
                        if (field.get(p).getX() > xMax){
                            xMax = field.get(p).getX();
                        }
                        // if the child's y max is bigger than the previous max, make it the max.
                        if (field.get(p).getY() > yMax){
                            yMax = field.get(p).getY();
                        }
                    }
                }
                // calculate the size of the square
                int length = xMax - xMin;
                int height = yMax - yMin;
                int area = length * height;
                if (area > 0){
                    System.out.println("Sheep size: " + area);
                    sheepCount += 1;
                }
                // draw the box
                PixelWriter rects = workableImage.getPixelWriter();
                for (int x = xMin; x <= xMax; x++){
                    rects.setColor(x, yMin, Color.BLUE);
                    rects.setColor(x, yMax, Color.BLUE);
                }
                for (int y = yMin; y <= yMax; y++){
                    rects.setColor(xMin, y, Color.BLUE);
                    rects.setColor(xMax, y, Color.BLUE);
                }
            }
        }
        System.out.println("Total Sheep: " + sheepCount);
        sheepCountDisp.setText(String.valueOf(sheepCount));
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
