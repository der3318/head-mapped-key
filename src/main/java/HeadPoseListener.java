import com.github.sarxos.webcam.*;
import java.awt.image.*;
import java.util.concurrent.atomic.*;
import org.apache.logging.log4j.*;

public class HeadPoseListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private Thread workerThread = null;
    private AtomicBoolean workerActive = new AtomicBoolean(false);
    private long workerDelayInMs = 100L;
    private Runnable taskOfHeadTurnedLeft = null;
    private Runnable taskOfHeadTurnedRight = null;

    public void setWorkerDelayInMs(long delayInMs) {
        this.workerDelayInMs = delayInMs;
    }

    public void onHeadTurnedLeft(Runnable task) {
        this.taskOfHeadTurnedLeft = task;
    }

    public void onHeadTurnedRight(Runnable task) {
        this.taskOfHeadTurnedRight = task;
    }

    public void startMonitoring() {

        LOGGER.info("Start Monitoring");

        Runnable task = () -> {
            Webcam webcam = Webcam.getDefault();
            try (HeadTurnDetector headTurnDetector = new HeadTurnDetector("model.onnx")) {
                webcam.open();
                HeadTurnDetector.HeadTurn lastStatus = HeadTurnDetector.HeadTurn.CENTERED;
                while (this.workerActive.get() && !Thread.currentThread().isInterrupted()) {

                    try {
                        Thread.currentThread().sleep(this.workerDelayInMs);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }

                    BufferedImage image = webcam.getImage();
                    long startTimeInMs = System.currentTimeMillis();
                    HeadTurnDetector.HeadTurn currentStatus = headTurnDetector.detect(image);
                    LOGGER.info("Result: " + currentStatus + " (" + (System.currentTimeMillis() - startTimeInMs) + "ms)");

                    if (lastStatus == HeadTurnDetector.HeadTurn.CENTERED) {
                        if ((this.taskOfHeadTurnedLeft != null) && (currentStatus == HeadTurnDetector.HeadTurn.LEFT)) {
                            this.taskOfHeadTurnedLeft.run();
                        }
                        if ((this.taskOfHeadTurnedRight != null) && (currentStatus == HeadTurnDetector.HeadTurn.RIGHT)) {
                            this.taskOfHeadTurnedRight.run();
                        }
                    }

                    lastStatus = currentStatus;
                }
            } catch (Exception e) {
                System.err.println("Exception Caught");
                e.printStackTrace();
            } finally {
                System.err.println("Closing Webcam");
                webcam.close();
            }
        };

        this.workerThread = new Thread(task);
        this.workerActive.set(true);
        this.workerThread.start();
    }

    public void stopMonitoring() {
        LOGGER.info("Stop Monitoring");
        this.workerActive.set(false);
        if (this.workerThread != null) {
            this.workerThread.interrupt();
            try {
                this.workerThread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            this.workerThread = null;
        }
    }
}
