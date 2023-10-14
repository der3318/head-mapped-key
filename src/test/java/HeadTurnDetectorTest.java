import java.awt.image.*;
import javax.imageio.*;
import org.junit.*;

public class HeadTurnDetectorTest {

    @Test
    public void evaluateLeft() {
        try (HeadTurnDetector headTurnDetector = new HeadTurnDetector("model.onnx")) {
            BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("left.png"));
            HeadTurnDetector.HeadTurn result = headTurnDetector.detect(image);
            Assert.assertEquals(HeadTurnDetector.HeadTurn.LEFT, result);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertNull(e.toString());
        }
    }

    @Test
    public void evaluateRight() {
        try (HeadTurnDetector headTurnDetector = new HeadTurnDetector("model.onnx")) {
            BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("right.png"));
            HeadTurnDetector.HeadTurn result = headTurnDetector.detect(image);
            Assert.assertEquals(HeadTurnDetector.HeadTurn.RIGHT, result);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertNull(e.toString());
        }
    }
}