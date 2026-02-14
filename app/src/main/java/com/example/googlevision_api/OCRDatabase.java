package com.example.googlevision_api;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.googlevision_api.OCRDao;
import com.example.googlevision_api.OCRResult;

@Database(entities = {OCRResult.class}, version = 1)
public abstract class OCRDatabase extends RoomDatabase {

    public abstract OCRDao ocrDao();

    private static volatile OCRDatabase INSTANCE;

    public static OCRDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (OCRDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    OCRDatabase.class, "ocr_history_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}