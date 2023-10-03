# Kotlin Logo screensaver for macOS

![Screensaver GIF](/docs/KotlinLogo.gif)

Logo size, count, and speed can be customized by going to System Settings, Screen Saver, and *Options...* with this screensaver selected.

![Use the Options button](/docs/config1.png)
![Customize parameters](/docs/config2.png)

## Installation

Release builds are currently not signed and notarized, however you can still [install these builds](#use-an-unsigned-release-build) by following the steps below. Alternatively, you can [build the screensaver](#build-it-yourself) from source.

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
