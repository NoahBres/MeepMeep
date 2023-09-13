# MeepMeep

[![Release](https://jitpack.io/v/NoahBres/MeepMeep.svg)](https://jitpack.io/#NoahBres/MeepMeep)

Path creation/visualization tool for Road Runner

<img src="/images/readme/screen-recording.gif" width="500" height="500"/>

# Table of Contents

- [Installing (Android Studio)](#installing-android-studio)
- [Extra Documentation](#extra-documentation)
  - [Custom Background](#custom-background)
  - [Adding a second bot](#adding-a-second-bot)
  - [Pulling Specific Jitpack Commits](#pulling-specific-jitpack-commits)
- [Misc](#misc)
  - [Poor Performance?](#poor-performance)
  - [Misc. Notes](#notes)

# Installing (Android Studio)

**Video instructions found here: https://youtu.be/vdn1v404go8. Please note that it was recorded in 2021 and may be outdated at the time of viewing**

1.  In Android Studio, click on the "FtcRobotController" Module, then right click on the FtcRobotController folder and click `New > Module`
    <img src="/images/readme/installationStep1.png" width="751" height="287"/>
2.  On the left part of this window, select "Java or Kotlin Library"
    <img src="/images/readme/installationStep2.png" width="544" height="382"/>

3.  From here, remove the `:ftcrobotcontroller:lib` in the "Library Name" section, and rename it to `MeepMeepTesting`. You may use whatever name you wish but the rest of the instructions will assume you have chosen the name `MeepMeepTesting`. Ensure that you also change the "class name" section to match.

4.  Hit "Finish" at the bottom right of the Module Create window.

5.  Open up the `build.gradle` file for the MeepMeepTesting module (or whatever you chose to name it prior). In this file, change all instances `JavaVersion.VERSION_1_7` to `JavaVersion.VERSION_1_8`
    <img src="/images/readme/installationStep5.png" width="566" height="274"/>

6.  At the bottom of the file add the following gradle snippet:

```
repositories {
    maven { url = 'https://jitpack.io' }
    maven { url = 'https://maven.brott.dev/' }
}

dependencies {
    implementation 'com.github.NoahBres:MeepMeep:2.0.2'
}
```

7.  When android studio prompts you to make a gradle sync, click "Sync Now".
    <img src="/images/readme/installationStep7.png" width="644" height="20"/>

8.  Create a class for your MeepMeepTesting java module if it does not yet exist. Paste the following sample in it. Feel free to change this later.

**🚨 The code snippet and explanation within the video is currently not up-to-date with the latest MeepMeep 2.0.x API 🚨**

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

9. Create a run configuration for Android Studio.
   1. First, click on the drop down menu on the top bar of Android Studio, where it says "TeamCode" with a little Android logo next to it.
      <img src="/images/readme/installationStep9i.png" width="649" height="188"/>
   2. Click `Edit Configurations`

      <img src="/images/readme/installationStep9ii.png" width="550" height="238"/>
   3. Click on the "+" symbol in the top left of the window, and when it prompts you, select "Application".

      <img src="/images/readme/installationStep9iii.png" width="518" height="334"/>
   4. Change the name to your liking (ex. meepmeep-run)

      <img src="/images/readme/installationStep9iv.png" width="578" height="480"/>
   5. Where it says "cp <no module>" click it to open the dropdown, and then select FtcRobotController.MeepMeepTesting.main

      <img src="/images/readme/installationStep9v.png" width="719" height="380"/>
   6. Where it says "Main Class", click the little "file" icon to the right of the text and then select the name of the main class for your MeepMeepTesting module.

      <img src="/images/readme/installationStep9vi.png" width="705" height="359"/>
   7. From here, in the bottom right of the window, press "Apply" then "Ok".

      <img src="/images/readme/installationStep9vii.png" width="710" height="630"/>
   8. It will now automatically switch to that Run/Debug Configuration profile.
10. If at any point you would like to build code onto your Control Hub or Phone, then click the Run/Debug configuration profile at the top to open the dropdown menu and select TeamCode. Perform the same steps to switch back to MeepMeepRun.

# Extra Documentation
### Custom Background
Before the `meepmeep.setBackground(MeepMeep.Background.FIELD_POWERPLAY_OFFICIAL)`, add the following lines of code and update the setBackground() command:
```java
Image img = null;
try { img = ImageIO.read(new File("<PATH TO IMAGE>")); }
catch (IOException e) {}

meepMeep.setBackground(img)
//  <following code you were using previously>
```
where <PATH TO IMAGE> is your path to the image you want to use for example:
    - On MacOs: `/Users/<username>/Documents/field.png`
    - On Windows: `C:\Users\<username>\Documents\field.png`

### Adding a second bot:

MeepMeep version 2.x introduces a new API and updated entity handling, allowing one to run and coordinate multiple trajectories.
Declare a new `RoadRunnerBotEntity` and add it via `MeepMeep#addEntity(Entity)`.

<img src="/images/readme/two-bot-demo.gif" width="500" height="500"/>

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

### Pulling Specific Jitpack Commits

MeepMeep is hosted on JitPack. This allows the user to pull dependencies from any Git commit. Change the dependency version in `build.gradle` to do so.

- Pull from a specific tagged version (same as install instructions)
  - `implementation 'com.github.NoahBres:MeepMeep:2.0.2'`
  - `2.0.2` can be replaced with whatever version specified on the [GitHub releases page](https://github.com/NoahBres/MeepMeep/releases)
- Pull from whatever the latest commit on the master branch is
  - `implementation 'com.github.NoahBres:MeepMeep:-SNAPSHOT'`
- Pull from a specific commit
  - `implementation 'com.github.NoahBres:MeepMeep:<commit version ID>'`
  - `<commit ID>` is replaced with ID of commit. For example "79d123f0c1"
  - This is not the full commit hash. It is the first 10 characters of the comit hash
 
# Misc

### Poor Performance?

On some systems, hardware acceleration may not be enabled by default.
To enable hardware acceleration use the cli flag: `-Dsun.java2d.opengl=true`.

Or, enable it _before_ initializing your `MeepMeep` instance with the following snippet:
`System.setProperty("sun.java2d.opengl", "true");`

### Notes:

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
