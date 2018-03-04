import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PixelTest {

    Pixel pixelParent = new Pixel(1, 1, 255);
    Pixel pixel = new Pixel(2, 2, 255);

    @Test
    void getParent() {
        assertEquals(null, pixel.getParent());
    }

    @Test
    void setParent() {
        pixel.setParent(pixelParent);
        assertEquals(pixelParent, pixel.getParent());
    }

    @Test
    void getX() {
        assertEquals(2, pixel.getX());
        assertEquals(1, pixelParent.getX());
    }

    @Test
    void setX() {
        pixel.setX(5);
        assertEquals(5, pixel.getX());
    }

    @Test
    void getY() {
        pixel.setY(10);
        assertEquals(10, pixel.getY());
    }

    @Test
    void setY() {
        pixel.setY(20);
        assertEquals(20, pixel.getY());
    }

    @Test
    void getArgb() {
        assertEquals(255, pixel.getArgb());
    }

    @Test
    void setArgb() {
        pixel.setArgb(200);
        assertEquals(200, pixel.getArgb());
    }
}