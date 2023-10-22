import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.apache.logging.log4j.*;
import org.jline.reader.*;
import org.jline.terminal.*;

public class App {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void keyPressAndRelease(Robot robot, int key) {
        robot.keyPress(key);
        robot.keyRelease(key);
    }

    public static void tryWaitForUserInterruptionToShutdown() throws IOException {
        Terminal terminal = TerminalBuilder.builder().jna(true).build();
        LineReader consoleLineReader = LineReaderBuilder.builder().terminal(terminal).build();
        while (true) {
            try {
                consoleLineReader.readLine("");
            } catch (UserInterruptException e) {
                // hoping to catch CTRL+C with the help of JNA here
                LOGGER.info("Shutting Down");
                Runtime.getRuntime().exit(0);
            } catch (EndOfFileException e) {
                // dumb terminals like `gradle run` can not hook CTRL+C and may not have STDIN
                LOGGER.info("STDIN Unavailable");
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException, AWTException, IllegalAccessException, NoSuchFieldException {

        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("app.properties")) {
            props.load(inputStream);
        }

        Robot robot = new Robot();
        int keyOnHeadTurnedLeft = KeyEvent.class.getField(props.getProperty("keyname.head.left")).getInt(null);
        int keyOnHeadTurnedRight = KeyEvent.class.getField(props.getProperty("keyname.head.right")).getInt(null);

        HeadPoseListener headPoseListener = new HeadPoseListener();
        headPoseListener.setPreferredCameraKeyword(props.getProperty("camera.keyword"));
        headPoseListener.setWorkerDelayInMs(Long.parseLong(props.getProperty("worker.delay")));
        headPoseListener.onHeadTurnedLeft(() -> keyPressAndRelease(robot, keyOnHeadTurnedLeft));
        headPoseListener.onHeadTurnedRight(() -> keyPressAndRelease(robot, keyOnHeadTurnedRight));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> headPoseListener.stopMonitoring()));
        headPoseListener.startMonitoring();
        tryWaitForUserInterruptionToShutdown();
    }
}
