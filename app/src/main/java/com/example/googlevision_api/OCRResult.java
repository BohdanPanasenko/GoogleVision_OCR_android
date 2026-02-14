package com.example.googlevision_api;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ocr_results")
public class OCRResult {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "text_content")
    public String textContent;

    @ColumnInfo(name = "timestamp")
    public String timestamp;

    public OCRResult(String textContent, String timestamp) {
        this.textContent = textContent;
        this.timestamp = timestamp;
    }
}
