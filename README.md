# Google Vision OCR App

This is a simple Android application that demonstrates how to use Google's ML Kit Text Recognition (OCR) to extract text from images. Users can either capture a new image with the camera or select an existing one from the gallery. The recognized text is then displayed on the screen and saved to a local history for later review.

## Features

- **Text Recognition (OCR):** Extracts text from images.
- **Camera Integration:** Capture images directly within the app.
- **Gallery Selection:** Pick images from the device's photo gallery.
- **Local History:** All OCR results are saved to a local database using Room.
- **History View:** Browse, view, and delete past OCR scans.

## Technologies Used

- **Android SDK (Java):** The core application is built using native Android with Java.
- **Google ML Kit Text Recognition:** The on-device OCR engine for recognizing text in images.
- **Room Persistence Library:** Used for creating and managing the local SQLite database to store the OCR history.
- **View Components:** The UI is built with standard Android components like `ConstraintLayout`, `RecyclerView`, and `CardView`.
- **ExecutorService:** Used for handling background database operations.

## How to Run

1.  Open the project in Android Studio.
2.  Build the project to resolve all dependencies.
3.  Run the app on an Android emulator or a physical device.

### Testing the OCR

- To test with a real image, you can **drag and drop** an image file from your computer directly onto the emulator window. It will be saved to the emulator's gallery.
- Once the image is in the gallery, use the **"Select from Gallery"** button in the app to choose it and run the OCR process.
