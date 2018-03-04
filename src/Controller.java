
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

public class Controller {

    @FXML private ImageView iconOpenImage, iconExit;
    @FXML private ImageView imageDisp, imageAffect;
    @FXML private javafx.scene.text.Text textFileName, textSize, textDimensions, sheepCountDisp;
//    @FXML private TextField userMinSheep, userMaxSheep;
    @FXML private Slider luminanceSlider;
    @FXML private Slider bamSizeSlider;
    @FXML private Button outlineButton;

    private BufferedImage bufferedImage;
    private WritableImage colorImage, workableImage;
    private Boolean colorActive, greenFilter = false;
    private ArrayList<Pixel> field;
    private int bamSensitivity;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMAGE CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Opens an Image and Displays it on both screens.
     */
    @FXML
    public void openImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );
        File selectedFile = fc.showOpenDialog(null);
        try {
            bufferedImage = ImageIO.read(selectedFile);
            colorImage = SwingFXUtils.toFXImage(bufferedImage, null);
            // Image Details // TODO Get the decimal back for the file size.
            textFileName.setText(selectedFile.getName());
            textDimensions.setText(String.valueOf(colorImage.getWidth()) + " x " + String.valueOf(colorImage.getHeight()));
            double fileSizeMB = selectedFile.length() / (1024 * 1024);
            textSize.setText(String.valueOf(fileSizeMB) + " MB");
//            double w = 0;
//            double h = 0;
//
//            double ratioX = imageDisp.getFitWidth() / colorImage.getWidth();
//            double ratioY = imageDisp.getFitHeight() / colorImage.getHeight();
//
//            double reducCoeff = 0;
//            if(ratioX >= ratioY) {
//                reducCoeff = ratioY;
//            } else {
//                reducCoeff = ratioX;
//            }
//
//            w = colorImage.getWidth() * reducCoeff;
//            h = colorImage.getHeight() * reducCoeff;
//
//            imageDisp.setX((imageDisp.getFitWidth() - w) / 2);
//            imageDisp.setY((imageDisp.getFitHeight() - h) / 2);


            // Display Image
            colorActive = true;
            imageDisp.setImage(colorImage);

            workableImage = SwingFXUtils.toFXImage(bufferedImage, null);
            imageAffect.setImage(workableImage);
//            workableImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    //new WritableImage((int) colorImage.getWidth(), (int) colorImage.getHeight());
           // imageAffect.setImage(workableImage);

           // luminanceControl();
        } catch (IOException e) {
            System.out.println("Image failed to load.");
        }

        fieldInitializer(workableImage, getBamSensitivity());

    }

    /**
     * Adjusts the right image to isolate black and white
     */
    @FXML
    public void luminanceControl() {
        System.out.println("Inside LUM Control");
        double sliderValue = luminanceSlider.getValue();
        System.out.println("Slider moved to:" + sliderValue);
//        if (!greenFilter) {
//            for (int x = 0; x < colorImage.getWidth(); x++) {
//                for (int y = 0; y < colorImage.getHeight(); y++) {
//                    Color color = colorImage.getPixelReader().getColor(x, y);
//                    double red = color.getRed();
//                    double green = color.getGreen();
//                    double blue = color.getBlue();
//                    Color gColor = color;
//                    if (red >= sliderValue && green >= sliderValue && blue >= sliderValue){
//                        gColor = color.WHITE;
//                    } else if (red < sliderValue && green < sliderValue && blue < sliderValue){
//                        gColor = color.BLACK;
//                    }
//                    // look up JPG Gamma formula.
//                    workableImage.getPixelWriter().setColor(x, y, gColor);
//                }
//            }
//            imageAffect.setImage(workableImage);
//        }
        if (!greenFilter) {
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
                    // TODO look up JPG Gamma formula.
                    int argb = (a<<24) | (r<<16) | (g<<8) |  b;
                    workableImage.getPixelWriter().setArgb(x, y, argb);
                }
            }
            imageAffect.setImage(workableImage);

            fieldInitializer(workableImage, getBamSensitivity());
        }
        if (greenFilter) {
            for (int x = 0; x < bufferedImage.getWidth(); x++){
                for (int y = 0; y < bufferedImage.getHeight(); y++){
                    int p = bufferedImage.getRGB(x, y);
                    int a = (p>>24) & 0xff;
                    int r = (p>>16) & 0xff;
                    // green filter on so green stays at 0
                    int g = 0;
                    int b = p & 0xff;
//                    System.out.println("Red: " + r);
//                    System.out.println("Slider: "+ sliderValue);
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

            fieldInitializer(workableImage, getBamSensitivity());
        }

    }

    /**
     * Removes the green channel from each pixel
     */
    @FXML
    public void greenChannel(){
        System.out.println("Green Tick Box");
        if (!greenFilter){
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

            fieldInitializer(workableImage, getBamSensitivity());

            greenFilter = true;
        }
        else if (greenFilter){
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

            fieldInitializer(workableImage, getBamSensitivity());

            greenFilter = false;
        }
    }

    private int getBamSensitivity(){
        bamSensitivity = (int)bamSizeSlider.getValue();
        System.out.println("Sensitivity: " + bamSensitivity);
        return bamSensitivity;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SHEEP IDENTIFICATION CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    public void bamSizeMove(){
        //fieldInitializer(workableImage, getBamSize());
        findSheep(workableImage, getBamSensitivity());
    }

    public void fieldInitializer(WritableImage img, int bamSize){
        field = new ArrayList<>();
        for (int x = 0; x < img.getWidth(); x ++){
            for (int y = 0; y < img.getHeight(); y ++){
                int argb = img.getPixelReader().getArgb(x, y);
                //if (argb > bamSize){
                    Pixel pixel = new Pixel(x, y, argb);
                    field.add(pixel);
                    System.out.println("Pixel Added to Field");
                //}
            }
        }
        System.out.println(field);

        findSheep(img, bamSize);

        //return field;
    }

    private boolean argbChecker(int argb, int bamSensitivity){
        int p = argb;
        //int a = (p>>24) & 0xff;
        int r = (p>>16) & 0xff;
        int g = (p>>8) & 0xff;
        int b =  p & 0xff;
        if (r > bamSensitivity && g > bamSensitivity && b > bamSensitivity){
            return true;
        }
        return false;
    }

    // check array starts at 0 but image pixel might be at 1?
    public void findSheep(WritableImage img, int bamSensitivity){
        int width = (int) img.getWidth();
        // Goes through the entire array of pixels
        for (int i = 0; i < field.size(); i ++){
            // Checks the pixel to see if it is white / a sheep.

         //   System.out.println("the ARGB value is:" + field.get(i).getArgb());

            if (argbChecker(field.get(i).getArgb(), bamSensitivity)){
                System.out.println("White pixel");
                // If Pixel has no parent, it becomes a root.
                if (field.get(i).getParent() == null) {
                    field.get(i).setParent(field.get(i));
                }
                if (i < field.size() - 1) {
                    if (argbChecker(field.get(i + 1).getArgb(), bamSensitivity)) {
                        System.out.println("X Child pixel");
                        field.get(i + 1).setParent(field.get(i).getParent());
                    }
                }
                if (i < field.size() - width ){
                    if (argbChecker(field.get(i + width).getArgb(), bamSensitivity)){
                        System.out.println("Y Child pixel");
                        field.get(i + width).setParent(field.get(i).getParent());
                    }
                }
            } else System.out.println("NOT A WHITE PIXEL");
        }
        System.out.println("End of find sheep");
        for (int i = 0; i < field.size(); i ++){
            if (field.get(i).getParent() == field.get(i)){
                System.out.println("Root Pixel");
            }
        }

    }

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
