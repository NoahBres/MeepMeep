# MeepMeep
[![Release](https://jitpack.io/v/NoahBres/MeepMeep.svg)](https://jitpack.io/#NoahBres/MeepMeep)

Path creation/visualization tool for Road Runner

# 🔨 Installing 

## Gradle Snippet:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.NoahBres:MeepMeep:1.0.0'
}
```

## Installation Video

[![YouTube Installation Video](/images/readme/thumbnail-half.jpg?raw=true)](https://www.youtube.com/watch?v=dQw4w9WgXcQ)

## Run Sample
```java
package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep mm = new MeepMeep(800)
                .setBackground(MeepMeep.Background.FIELD_ULTIMATE_GOAL_DARK)
                .setTheme(new ColorSchemeRedDark())
                .setBackgroundAlpha(1f)
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
                )
                .start();
    }
}
```