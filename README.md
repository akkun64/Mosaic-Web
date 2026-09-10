# Mosaic - Web

Offline music player modeled after the Samsung Galaxy Core Prime music UI, built with Kotlin Multiplatform (Compose for Web / Wasm).

## Requirements

- JDK 17+
- [Gradle](https://gradle.org) (included via wrapper)
- Modern browser (Chrome, Firefox, Edge)

## Run

```bash
cd Mosaic-Web
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Opens a local dev server (usually `localhost:8080`). Click **Select Audio Files** and pick songs from your computer.

## Build

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
```

Output in `composeApp/build/dist/wasmJs/productionExecutable/`. Host those files on any static server.

## Notes

- No MediaStore/device library — works by selecting local audio files
- Drag-and-drop support can be added (currently file picker)
- Dark theme with Samsung blue accents
- Shuffle, repeat, next/prev, seek bar
