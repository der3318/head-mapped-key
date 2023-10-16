
## ⌨️ Head Pose Keyboard Mapping

![java](https://img.shields.io/badge/java-17+-blue.svg)
[![webcam](https://img.shields.io/badge/com.github.sarxos:webcam--capture-0.3.12-yellow.svg)](https://github.com/sarxos/webcam-capture)
[![ort](https://img.shields.io/badge/onnxruntime-1.16.0-pink.svg)](https://github.com/microsoft/onnxruntime)
![portable](https://img.shields.io/badge/portable-win%20x64-green.svg)
![license](https://img.shields.io/badge/license-MIT%20%28inherited%29-blueviolet.svg)

A Java program that can trigger keyboard actions using head poses streamed from PC camera, leveraging the end-to-end landmark detection ONNX model (ref: [atksh/onnx-facial-lmk-detector](https://github.com/atksh/onnx-facial-lmk-detector)): 

![DemoUsage.gif](https://github.com/der3318/head-mapped-key/blob/main/imgs/DemoUsage.gif)


### How to Run

| Using Portable Version |
| :- |
| For Windows AMD64 devices, an all-in-one app package [head-mapped-key-portable-x64.zip](https://github.com/der3318/head-mapped-key/releases/download/2023.10.16/head-mapped-key-portable-x64.zip) is available. Unzip and double-click `launch.bat` to turn camera on and start the tool. |


| Using JRE 17+ |
| :- |
| Download fat JAR [head-mapped-key-all.jar](https://github.com/der3318/head-mapped-key/releases/download/2023.10.16/head-mapped-key-all.jar) and run `java -Duser.language=en -Dfile.encoding=UTF8 -jar head-mapped-key-all.jar` in CLI. |


| Using Gradle Wrapper |
| :- |
| Clone the repository and use `gradlew run` command. |


Debug messages will be dumpped directly to the console. Press CTRL+C to stop listening. A JVM shutdown hook will help release resouces gracefully, including camera handle.

Also noted that the responsiveness of the tracking mechanism highly depends on the actual CPU speed. For example, on a 4-Core 1.90GHz Intel laptop, each frame takes about 900ms to process, which makes the overall experience a bit lagging.


### Customizing Settings

The editable `app.properties` (plain text file) provides basic values that can be consumed during runtime:

```txt
# preferred camera (will be prioritized if any)
camera.keyword=Camera Front

# how many ms should the worker wait to take next webcam capture
worker.delay=100

# https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/event/KeyEvent.html
keyname.head.left=VK_LEFT
keyname.head.right=VK_RIGHT
```

Key mappings based on personal need would be the most useful one.


### How to Build & Redistribute

To compile the sources, JDK 17+ should be either accessible via environment variable `JAVA_HOME`, or the Java excutables are avaiable under `PATH`. This is the only prerequisite.

Command `gradlew shadowJar` will download Gradle v8.3 and use it to build the redistributable JAR: `build/libs/head-mapped-key-all.jar`, including all the dependencies.


### Inference Logic Behind the Scene

Nothing fancy. Given the horizontal locations of eyes and nose, the head turn can be roughly inferred.

![SampleFace.gif](https://github.com/der3318/head-mapped-key/blob/main/imgs/SampleFace.jpg)

For instance, `(Nose.X - LeftEye.X) : (RightEye.X - Nose.X) = (3 or higher) : 1` implies the head is facing relatively left (mirrored) to the sensor, and vice versa. Otherwise, the pose is considered centered.
