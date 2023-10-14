import java.io.*;
import java.util.*;
import org.junit.*;

public class AppPropertiesTest {

    @Test
    public void evaluateLoad() {
        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("app.properties")) {
            props.load(inputStream);
            Assert.assertFalse(props.getProperty("camera.keyword").isEmpty());
            Assert.assertFalse(props.getProperty("worker.delay").isEmpty());
            Assert.assertFalse(props.getProperty("keyname.head.left").isEmpty());
            Assert.assertFalse(props.getProperty("keyname.head.right").isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertNull(e.toString());
        }
    }
}