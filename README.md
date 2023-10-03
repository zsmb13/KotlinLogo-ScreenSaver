# Kotlin Logo screensaver for macOS

![Screensaver GIF](/docs/KotlinLogo.gif)

## Installation

### Build the project

```
cd KotlinLogos
xcodebuild -scheme KotlinLogos build
```

### Install the screensaver:

1. Locate the output file:
   * Open `KotlinLogos` in XCode, `Product -> Show Build Folder in Finder`, browse to `Products/Debug/KotlinLogos.saver`.
   * Full path should be `~/Library/Developer/Xcode/DerivedData/KotlinLogos-[...]/Build/Products/Debug/KotlinLogos.saver`.

2. Install the screensaver by either
   * Opening it to install it in System Settings OR
   * Manually copying it to `~/Library/Screen Savers/`

### Configure parameters

Go to System Settings -> Screen Saver, select the screen saver, and then _Options_ to customize logo size, count, and speed.

### Backlog

* Optimize PNG sizes, consider using vector images
* Create proper release builds
* Support other platforms?
