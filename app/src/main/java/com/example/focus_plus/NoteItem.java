package com.example.focus_plus;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteItem {

    private String title;
    private String type;
    private String dateTime;
    private String content;

    public NoteItem(String title, String type, String dateTime, String content) {
        this.title = title;
        this.type = type;
        this.dateTime = dateTime;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getContent() {
        return content;
    }
}