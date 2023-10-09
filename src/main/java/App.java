import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class App {

    public static void keyPressAndRelease(Robot robot, int key) {
        robot.keyPress(key);
        robot.keyRelease(key);
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
        headPoseListener.setWorkerDelayInMs(Long.parseLong(props.getProperty("worker.delay")));
        headPoseListener.onHeadTurnedLeft(() -> keyPressAndRelease(robot, keyOnHeadTurnedLeft));
        headPoseListener.onHeadTurnedRight(() -> keyPressAndRelease(robot, keyOnHeadTurnedRight));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> headPoseListener.stopMonitoring()));
        headPoseListener.startMonitoring();
    }
}
