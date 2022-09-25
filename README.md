# MeepMeep
[![Release](https://jitpack.io/v/NoahBres/MeepMeep.svg)](https://jitpack.io/#NoahBres/MeepMeep)

Path creation/visualization tool for Road Runner

<img src="/images/readme/screen-recording.gif" width="500" height="500"/>

# 🔨 Installing (Android Studio)
**You can also find a Video version of Similar instructions here (https://youtu.be/vdn1v404go8), However please do note that it may be outdated**
1. In android studio, first click on the "FtcRobotController" Module, then right click on the Project Window and click New > Module
    
2. On the left part of this window, instead of having "Phone & Tablet" Selected, select "Java or Kotlin Library" (See below image)
        <img src="/images/readme/installationStep2.png" width="544" height="381.818181818"/>

3. From here, remove the ":ftcrobotcontroller:lib" in the "Library Name" section, and rename it whatever you want (ex. MeepMeepTesting). At this point you should also change the "class name" section to whatever you want too (ex. MeepMeepTesting).

4. Hit "Finish" at the bottom right of the Module Create window.

5. Open up the "build.gradle" file for the MeepMeepTesting module (or whatever you called it above). In this file you should change `JavaVersion.VERSION_1_7` to `JavaVersion.VERSION_1_8` in both instances.
        <img src="/images/readme/installationStep5.png" width="565.714285714 " height="274.285714286"/>

6. At the bottom of the file add the following gradle snippet:
   
    ```groovy
    repositories {
        maven { url 'https://jitpack.io' }
        maven { url = 'https://maven.brott.dev/' }
    }

    dependencies {
        implementation 'com.github.NoahBres:MeepMeep:2.0.2'
    }
    ```
NOTE: If the `repositories {...}` or `dependencies {...}` section already exists, feel free to add the contents of the above snippet to them, just for some cleaner code :)

7. When android studio prompts you to make a gradle sync, click "Sync Now".
        <img src="/images/readme/installationStep7.png" width="643.636363636" height="20.3636363636"/>

8. From here, you can go into the whatever you called the main class of the MeepMeep module, and either use the run sample here:

**🚨 The code snippet and explanation within the video is currently not up-to-date with the latest MeepMeep 2.0.x API  🚨**

```java
package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .build()
                );

        meepMeep.setBackground(MeepMeep.Background.FIELD_POWERPLAY_OFFICIAL)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}
```
9. From here, you need to creat a run configuration for android studio. To do this:
    1. First, click on the drop down menu on the top bar of Android Studio, where it says "TeamCode" with a little android logo next to it.
    2. Click "Edit Configurations"
    3. Click on the "+" symbol in the top left of the window, and when it prompts you, select "Application".
    4. Change the name to your liking (ex. meepmeep-run)
    5. Where it says "module not specified", click to open the dropdown, then select your JRE.
    6. Where it says "cp <no module>" click it to open the dropdown, and then select FtcRobotController.MeepMeepTesting.main (This could be different if you named your module differently)
    7. Where it says "Main Class", click the little "file" icon to the right of the text, and then select the name of the main class for your MeepMeepTesting (or other name) module.
    8. From here, in the bottom right of the window, press "Apply" then "Ok".
    9. It will now automatically switch to that Run/Debug Configuration profile.
    
10. Your now done! If at any point you would like to build code onto your Control Hub or Phone, then click the Run/Debug configuration profile at the top to open the dropdown menu and select TeamCode, or viceversa to go back to a MeepMeepRun. Extra documentation can be found below, and if you run into any problems you can reach out on the Utah FTC Discord. I'm sure people would be happy to help :)

### Adding a second bot:

MeepMeep version 2.x introduces a new API and updated entity handling, allowing one to run and coordinate multiple trajectories.
Declare a new `RoadRunnerBotEntity` and add it via `MeepMeep#addEntity(Entity)`.

![Two bot demo](/images/readme/two-bot-demo.gif?raw=true)


```java
package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        // Declare our first bot
        RoadRunnerBotEntity myFirstBot = new DefaultBotBuilder(meepMeep)
                // We set this bot to be blue
                .setColorScheme(new ColorSchemeBlueDark())
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .build()
                );

        // Declare out second bot
        RoadRunnerBotEntity mySecondBot = new DefaultBotBuilder(meepMeep)
                // We set this bot to be red
                .setColorScheme(new ColorSchemeRedDark())
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(new Pose2d(30, 30, Math.toRadians(180)))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .forward(30)
                                .turn(Math.toRadians(90))
                                .build()
                );

        meepMeep.setBackground(MeepMeep.Background.FIELD_FREIGHTFRENZY_ADI_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)

                // Add both of our declared bot entities
                .addEntity(myFirstBot)
                .addEntity(mySecondBot)
                .start();
    }
}
```

## Poor Performance?
On some systems, hardware acceleration may not be enabled by default.
To enable hardware acceleration use the cli flag: `-Dsun.java2d.opengl=true`.

Or, enable it _before_ initializing your `MeepMeep` instance with the following snippet:
`System.setProperty("sun.java2d.opengl", "true");`

## Notes:
Default Bot Settings:
- Constraints
- Max Vel: 30in/s
- Max Accel: 30in/s/s
- Max Ang Vel: 60deg/s
- Max Ang Accel: 60deg/s/s
- Track Width: 15in
- Bot Width: 18in
- Bot Width: 18in
- Start Pose: (x: 0in, y: 0in, heading: 0rad)
- Color Scheme: Inherited from MeepMeep.colorManager unless overriden
- Drive Train Type: Mecanum


<!-- [![YouTube Installation Video](/images/readme/thumbnail-half.jpg?raw=true)](https://youtu.be/vdn1v404go8) -->
