# Kotlin Logo screensaver for macOS

## Installation

### Configure parameters

Configure parameters by editing the values at the top of `Main.kt`:

* `LOGO_AREA`: logo size in square pixels
  * `25 * 25` - `500 * 500`
* `LOGO_COUNT`: the number of logos to display
  * `1` - `1000`
* `SPEED`: logo speed in pixels / frame
  * `1.0` - `5.0`

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

### Backlog

* Move parameters to a proper ScreenSaver config
* Optimize PNG sizes, consider using vector images
* Create proper release builds
