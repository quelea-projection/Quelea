# Setup Guide

## Dependencies

Quelea is built on [Java 1.11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html) and [Gradle](https://gradle.org/); you will need those tools installed locally to compile the application.

## Step-by-step

If you're not familiar with working on Java desktop applications, you can follow these steps to get started contributing to Quelea using [IntelliJ IDEA](https://www.jetbrains.com/idea/). IntelliJ IDEA is not required to work on Quelea; these are just steps for getting started if you're not already familiar with Java IDEs.

The instructions below use IntelliJ version 2022.2.2.

1. Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download) Community version for free. Gradle is bundled with it. A Java JDK 11 might be downloaded from within IntelliJ as well - see step 11.
2. Fork the Quelea repository and git clone it locally.
3. Open IntelliJ IDEA.

![](images/IntellijWelcome.png)

4. Choose _Get from VCS_ from the top right of the welcome window.

  ![](images/IntellijGetFromVcs.png)

5. In the _URL_ box paste the URL to your Quelea Github fork
6. In the _Directory_ box either type the path you want to store the files (`/home/user/development/Quelea` in the screenshot) or click the folder icon on the right to use the file browser
7. Click _Clone_ and IntelliJ will copy down the relevant files.  This will take a few minutes
8. Once IntelliJ has copied the files it will open and present you with the readme file.
9. In the bottom right you should see a pop-up message stating that Gradle script files have been found.  Click _Load_ to import these.

  ![](images/IntellijLoadGradleBuildScript.png)

10. IntelliJ will ask if you want to trust this Gradle project.  If you do, click  _Trust Project_.  Note the import will not continue if you choose to stay in safe mode.

  ![](images/IntellijTrustGradleProject.png)

11. Open Project Structure settings (File -> Project Structure... -> Project) and make sure that you have a Java JDK 11 selected as SDK and that language level is set to 11.

  ![](images/IntelliJSelectedJDK11.png)

If you have no java JDK 11 installed yet, you can download one (e.g. amazon corretto 11) in the same window

  ![](images/IntelliJDownloadJDK11.png)

12. Open IntelliJ's _Gradle JVM_ settings (File > Settings > Build, Execution, Deployment > Gradle) and ensure that _Gradle JVM_ has the downloaded Java JDK 11 selected.  _OK_ this window.

  ![](images/IntelliJGradleJVM.PNG)

13. Search for IntelliJ's Gradle tab and unfold Quelea -> Tasks -> application. Right-click on 'run' and select Debug 'Quelea [run]'

  ![](images/IntelliJGradleTab.PNG)

If everything went smoothly, you should see Quelea launch. You're now ready to make and test changes!

### Getting your GitHub fork URL
While looking at your GitHub fork there is a green _Code_ button.  Click this and you will be offered several options to checkout your fork (SSH is shown).

![](images/GithubCodeButton.png)

Click the copy icon to the right of the text box.  This is what you'll paste into IntelliJ.