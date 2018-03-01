
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
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
    @FXML private javafx.scene.text.Text textFileName, textSize, textDimensions;
//    @FXML private TextField userMinSheep, userMaxSheep;
    @FXML private Slider luminanceSlider;

    BufferedImage bufferedImage;
    WritableImage colorImage, workableImage;
    Boolean colorActive, greenFilter = false;

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
            greenFilter = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SHEEP IDENTIFICATION CONTROL METHODS
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void fieldInitializer(){
        ArrayList field = new ArrayList();
        for (int x = 0; x < workableImage.getWidth(); x ++){
            for (int y = 0; y < workableImage.getHeight(); y ++){
                int argb = workableImage.getPixelReader().getArgb(x, y);
                Pixel pixel = new Pixel(x, y, argb);
                field.add(pixel);
            }
        }
// TODO Add a test now to check the creation of the Pixel and addition to the array.
        System.out.println(field);

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
