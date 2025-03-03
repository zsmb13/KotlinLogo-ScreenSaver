# Kotlin Logo screensaver for macOS

A screen saver to bounce Kotlin logos, Kodees, or anything else around your screen when your machine is idle.

![Screensaver GIF](/docs/KotlinLogo.gif)

## Learn how it works

The journey and technical details of creating this screen saver is documented in [Building a macOS screen saver in Kotlin on zsmb.co](https://zsmb.co/building-a-macos-screen-saver-in-kotlin/).

If you want to build your own screen saver in Kotlin, check out the [Kotlin-macOS-ScreenSaver-Template](https://github.com/zsmb13/Kotlin-macOS-ScreenSaver-Template) repository for a starter project.

## Installation

Release builds are currently not signed and notarized, however, you can still [install these builds](#use-an-unsigned-release-build) by following the steps below. Alternatively, you can [build the screensaver](#build-it-yourself) from source.

### Use an unsigned release build

1. Download the latest release build from [Releases](https://github.com/zsmb13/KotlinLogo-ScreenSaver/releases).
2. Unzip the downloaded file.
3. Open `KotlinLogos.saver`, install it as prompted, this will take you to Screen Saver within System Settings.
4. When selecting the screen saver, you'll be warned that Apple can not check it for malicious software. Choose "OK", and then select another screen saver to stop the popup from repeatedly showing up.

   <details>
      <summary>See image</summary>
      
      ![](/docs/install1.png)
   </details>

5. Go to Privacy & Security in System Settings, scroll down to Security, and click "Open Anyway" under the message about `KotlinLogos.saver`

   <details>
      <summary>See image</summary>
      
      ![](/docs/install2.png)
   </details>

6. Go back to Screen Saver, select the screensaver again, and choose "Open" which should now be available.

   <details>
      <summary>See image</summary>

      ![](/docs/install3.png)
   </details>

7. Enjoy!

### Build it yourself

Build the artifact:

```
cd KotlinLogos
xcodebuild -scheme KotlinLogosRelease build
```

Then, to install it:

1. Locate the output file:
   * Open `KotlinLogos` in XCode, `Product -> Show Build Folder in Finder`, browse to `Products/Debug/KotlinLogos.saver`.
   * Full path should be `~/Library/Developer/Xcode/DerivedData/KotlinLogos-[...]/Build/Products/Debug/KotlinLogos.saver`.

2. Install the screensaver by either
   * Opening it to install it in System Settings OR
   * Manually copying it to `~/Library/Screen Savers/`

## Customization

To customize the screen saver, go to System Settings, Screen Saver, select it, and then click *Options...*

![Use the Options button](/docs/config1.png)

You can change the following options:

![Customize parameters](/docs/config2.png)

* Logo set
    * Kotlin logos over time
    * Kodee images
    * Browsing for your own folder, containing PNG, JPG, or SVG files\*
* Logo size
* Logo count
* Logo speed
* Renderer
    * AppKit: uses `NSImageView`s
    * Compose: renders using [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
    * Demo mode: switches back-and-forth between the above implementations
* Debug mode

\* *Use a folder with accessible permissions, for example, create a `~/screensaver-images` folder, and place your images there. The screen saver looks for PNG and SVG images in the custom folder. Folders located in places like your Downloads folder will likely show their images in the preview, but not work with the actual screen saver. If this happens, try to place the folder in a different location.*

## Demo apps

To try the code without having to build and set a screen saver, you can also run it as a windowed desktop application, either on the JVM, or natively (macOS-only).

### JVM demo

This JVM Desktop app runs the Compose Multiplatform implementation of the screen saver. The configuration for this app also includes [Compose Hot Reload](https://github.com/JetBrains/compose-hot-reload).

Run the application with 

```
./gradlew runJvm
```

Use `-PcustomFolder` to specify the folder of images that should be used, for example: 

```
./gradlew runJvm -Pcustomfolder="/Users/zsmb/screensaver-images"
```

If no folder is specified, the app will use the contents of the `sampleImages` folder.

### macOS native demo

You can run the screen saver as a native macOS application in a window using

```
./gradlew runDebugExecutableMacosArm64`
```

This implementation is configured to run in demo mode, switching between the Appkit-based and Compose Multiplatform implementations of the screen saver every 2 seconds. 

> If the window doesn't appear when you run the task, run it again.
