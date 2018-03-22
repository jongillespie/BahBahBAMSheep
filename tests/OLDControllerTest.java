import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OLDControllerTest {

    BufferedImage bufferedImage;
    WritableImage sheepImg;
    ArrayList<Pixel> field;

    @BeforeEach
    void setUp() {
        try {
            bufferedImage = ImageIO.read(new File("/Users/JG/OneDrive/WIT - Y2 - S4/Algorithms/BahBah BAM Sheep/BahBah BAM! Sheep/src/images/sheepTestImage.jpg"));
            System.out.println("here first");
        } catch (IOException e) {
            System.out.println("Image failed to load.");
        }
        sheepImg = SwingFXUtils.toFXImage(bufferedImage, null);
        System.out.println("Got here dude!");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void openImage() {
    }

    @Test
    void luminanceControl() {
    }

    @Test
    void greenChannel() {
    }

    @Test
    void fieldInitializer() {
        //assertEquals(sheepImg.getPixelReader().getArgb(1,1), );
        System.out.println(sheepImg.getHeight());
    }

    @Test
    void exitProgram() {
    }
}