import ai.onnxruntime.*;
import ai.onnxruntime.OrtSession.*;
import ai.onnxruntime.OrtSession.SessionOptions.*;
import java.awt.*;
import java.awt.image.*;
import java.nio.*;
import java.util.*;
import org.apache.logging.log4j.*;

public class HeadTurnDetector implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();

    public enum HeadTurn {
        LEFT,
        RIGHT,
        CENTERED;
    }

    private OrtEnvironment env = null;
    private SessionOptions opts = null;
    private OrtSession session = null;
    private String inputName = null;

    public HeadTurnDetector(String ortModlePath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.opts = new SessionOptions();
        this.opts.setOptimizationLevel(OptLevel.BASIC_OPT);
        this.session = this.env.createSession(ortModlePath, this.opts);
        this.inputName = this.session.getInputNames().iterator().next();
        for (NodeInfo i : this.session.getInputInfo().values()) {
            LOGGER.debug("Input " + i.toString());
        }
        for (NodeInfo i : this.session.getOutputInfo().values()) {
            LOGGER.debug("Output " + i.toString());
        }
    }

    @Override
    public void close() throws Exception {
        System.err.println("Releasing ORT Session and Environment");
        if (this.session != null) {
            this.session.close();
        }
        if (this.opts != null) {
            this.opts.close();
        }
        if (this.env != null) {
            this.env.close();
        }
    }

    private BufferedImage resizeImageTo180p(BufferedImage image) {
        Image resized = image.getScaledInstance(320, 180, Image.SCALE_FAST);
        BufferedImage bi = new BufferedImage(resized.getWidth(null), resized.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(resized, 0, 0, null);
        graphics2D.dispose();
        return bi;
    }

    public HeadTurn detect(BufferedImage image) throws OrtException {

        BufferedImage bi = this.resizeImageTo180p(image);
        byte[] imageData = new byte[bi.getHeight() * bi.getWidth() * 3];
        for (int c = 0; c < bi.getWidth(); c++) {
            for (int r = 0; r < bi.getHeight(); r++) {
                int pixel = bi.getRGB(c, r);
                imageData[(r * bi.getWidth() + c) * 3 + 2] = (byte) ((pixel >> 16) & 0xff); // red
                imageData[(r * bi.getWidth() + c) * 3 + 1] = (byte) ((pixel >> 8) & 0xff);  // green
                imageData[(r * bi.getWidth() + c) * 3 + 0] = (byte) ((pixel) &0xff);        // blue
            }
        }

        long[] dataShape = new long[] {bi.getHeight(), bi.getWidth(), 3};
        try (OnnxTensor tensor = OnnxTensor.createTensor(this.env, ByteBuffer.wrap(imageData), dataShape, OnnxJavaType.UINT8);
             Result output = this.session.run(Collections.singletonMap(this.inputName, tensor))) {
            float[] scores = (float[]) output.get(0).getValue();
            long[][] bboxes = (long[][]) output.get(1).getValue();
            long[][][] kpts = (long[][][]) output.get(2).getValue();
            long[][][] lmks = (long[][][]) output.get(4).getValue();
            LOGGER.debug("Scores: " + Arrays.toString(scores));
            LOGGER.debug("BBoxes: " + Arrays.deepToString(bboxes));  // top left (col, row), bottom right (col, row)
            LOGGER.debug("KeyPts: " + Arrays.deepToString(kpts));    // left eye, right eye, nose, left mouth, right mouth
            LOGGER.debug("Output Length: " + lmks.length);

            if ((kpts.length != 1) || (kpts[0].length != 5) || (kpts[0][0].length != 2)) {
                return HeadTurn.CENTERED;
            }

            long leftEyeToRightEye = kpts[0][1][0] - kpts[0][0][0];
            long leftEyeToNose = kpts[0][2][0] - kpts[0][0][0];
            if (leftEyeToRightEye <= 0L) {
                return HeadTurn.CENTERED;
            }

            double ratio = ((double) leftEyeToNose / (double) leftEyeToRightEye);
            LOGGER.debug("Ratio: " + ratio);
            if (ratio <= 0.25D) {
                return HeadTurn.RIGHT;  // image looks like *<_*
            }
            if (ratio >= 0.75D) {
                return HeadTurn.LEFT;   // image looks like *_>*
            }
        }

        return HeadTurn.CENTERED;
    }
}
