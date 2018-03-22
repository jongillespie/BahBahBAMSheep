import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sheep Tangles Class test of all Methods.
 */
class SheepTangleTest {

    SheepTangle sheepTangle;

    @BeforeEach
    void setUp() {
        sheepTangle = new SheepTangle(0, 20, 10, 30, 100);
        sheepTangle.setSheepEstimate(2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getPixelKids() {
        assertEquals(100, sheepTangle.getPixelKids());
    }

    @Test
    void getxMin() {
        assertEquals(0, sheepTangle.getxMin());
    }

    @Test
    void getxMax() {
        assertEquals(20, sheepTangle.getxMax());
    }

    @Test
    void getyMin() {
        assertEquals(10, sheepTangle.getyMin());
    }

    @Test
    void getyMax() {
        assertEquals(30, sheepTangle.getyMax());
    }

    @Test
    void getSheepEstimate() {
        assertEquals(2, sheepTangle.getSheepEstimate());
    }

    @Test
    void setSheepEstimate() {
        sheepTangle.setSheepEstimate(3);
        assertEquals(3, sheepTangle.getSheepEstimate());
    }
}