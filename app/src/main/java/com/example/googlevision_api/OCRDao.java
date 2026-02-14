package com.example.googlevision_api;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OCRDao {
    @Query("SELECT * FROM ocr_results ORDER BY id DESC")
    List<OCRResult> getAllResults();

    @Insert
    void insert(OCRResult result);

    @Delete
    void delete(OCRResult result);
}
