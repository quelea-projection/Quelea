# Setup Guide

## Dependencies

Quelea is built on [Java 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Gradle](https://gradle.org/); you will need those tools installed locally to compile the application.

## Step-by-step

If you're not familiar with working on Java desktop applications, you can follow these steps to get started contributing to Quelea using [IntelliJ IDEA](https://www.jetbrains.com/idea/). (IntelliJ IDEA is not required to work on Quelea; these are just steps for getting started if you're not already familiar with Java IDEs)

1. Download and install the [Java SE Development Kit 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) for your platform.
2. Download and install [Gradle](https://gradle.org/install/).
3. Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download) Community version for free.
4. Fork the Quelea repository and git clone it locally.
5. Open IntelliJ IDEA.
6. Choose "Import Project" from the Welcome window.
7. Navigate to the local Quelea repository and select the `Quelea/build.gradle` file.
8. Check the box labelled "Use auto-import".
9. Ensure that the selection for "Gradle JVM" has a "java version" that starts with `1.8`.
10. Click OK to import the project.
11. Navigate to the `src/main/java/org/quelea/windows/main/Main` class.
12. Right-click on that class in the sidebar and click `Debug 'Main.main()'`

If everything's gone smoothly, you should see Quelea launch. You're now ready to make and test changes!
