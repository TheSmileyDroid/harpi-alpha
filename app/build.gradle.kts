plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven { url = uri("https://jitpack.io") } 
}

val jdaVersion = "5.0.0-beta.18"

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.1-jre")

    implementation("net.dv8tion:JDA:$jdaVersion")

    implementation("ch.qos.logback:logback-classic:1.2.8")

    implementation ("dev.arbjerg:lavaplayer:2.0.4")


}

application {
    // Define the main class for the application.
    mainClass.set("harpi.alpha.App")
    
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "1.8"

}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
