package com.example.googlevision_api;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 102;
    private static final String KEY_OCR_RESULT = "key_ocr_result";
    private static final String KEY_PHOTO_URI = "key_photo_uri";

    private TextView resultTextView;
    private OCRDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = OCRDatabase.getDatabase(this);
        resultTextView = findViewById(R.id.tv_result);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());

        if (savedInstanceState != null) {
            resultTextView.setText(savedInstanceState.getString(KEY_OCR_RESULT));
            photoURI = savedInstanceState.getParcelable(KEY_PHOTO_URI);
        }

        findViewById(R.id.btn_capture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });

        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });

        findViewById(R.id.btn_history).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_OCR_RESULT, resultTextView.getText().toString());
        if (photoURI != null) {
            outState.putParcelable(KEY_PHOTO_URI, photoURI);
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this,
                    "com.example.googlevision_api.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            try {
                if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
                    if (photoURI != null) {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), photoURI);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                } else if (requestCode == GALLERY_REQUEST_CODE) {
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                }
                if (bitmap != null) {
                    recognizeText(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizeText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String scannedText = visionText.getText();
                    resultTextView.setText(scannedText);
                    saveOcrResult(scannedText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "OCR failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveOcrResult(final String text) {
        if (text == null || text.trim().isEmpty()) return;
        executorService.execute(() -> {
            db.ocrDao().insert(new OCRResult(text, new Date().toString()));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
