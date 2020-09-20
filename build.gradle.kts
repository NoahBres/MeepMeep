plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.0"

    // Apply the java-library plugin for API and implementation separation.
    java
    `java-library`
    `maven-publish`
}

val meepMeepVersion = "1.0-SNAPSHOT"

group = "com.noahbres.meepmeep"
version = "1.0-SNAPSHOT"

val pomUrl = "https://github.com/NoahBres/MeepMeep"
val pomScmUrl = "https://github.com/NoahBres/MeepMeep"
val pomIssueUrl = "https://github.com/NoahBres/MeepMeep/issues"
val pomDesc = "https://github.com/NoahBres/MeepMeep"

val githubRepo = "NoahBres/MeepMeep"
val githubReadme = "README.md"

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    api("com.acmerobotics.roadrunner:core:0.5.2")
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("meepmeep") {
            groupId = "com.noahbres.meepmeep"
            artifactId = "meepmeep"
            version = meepMeepVersion

            from(components["java"])
            artifact(sourcesJar)

            pom {
                packaging = "jar"
                name.set(rootProject.name)
                description.set(pomDesc)
                url.set(pomUrl)
                scm {
                    url.set(pomScmUrl)
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("$buildDir/repository")
        }
    }
}