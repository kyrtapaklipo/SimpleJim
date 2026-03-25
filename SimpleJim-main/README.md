# SimpleJim

SimpleJim is a bare-bones Android gym tracker aimed at fast workout logging on a phone.

## What is in this first version

- Kotlin + Jetpack Compose Android app
- Simple workout logger with exercises, sets, weight, reps, and optional notes
- Local offline persistence with `SharedPreferences`
- Session history screen with exercise breakdown and rough training volume
- Lightweight visual style that works well on a Google Pixel-sized device

## Project structure

- `app/` contains the Android application module
- `app/src/main/java/com/simplejim/tracker/` contains the activity, view model, UI, and persistence code
- `app/src/main/res/` contains the manifest resources and app theme

## Opening the project

1. Open this workspace root in Android Studio.
2. Use JDK 17 for the project.
3. Let Android Studio sync the Gradle files and run the `app` configuration on your Pixel.

## Notes

- The current environment did not have `java`, `gradle`, or Android SDK tools available on `PATH`, so the project files were scaffolded but not compiled here.
- A Gradle wrapper was not generated in this environment because there was no local Gradle installation available to create it.
- Weight is currently labeled in pounds to match a typical US gym setup.
