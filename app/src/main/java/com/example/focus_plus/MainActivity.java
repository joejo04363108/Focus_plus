package com.example.focus_plus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView timeTextView;
    private TextView dateTextView;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    private FloatingActionButton fab;
    private Button b2;
    private RecyclerView recyclerView;

    private NoteAdapter noteAdapter;
    private List<NoteItem> noteList = new ArrayList<>();

    private static final String PREFS_NAME = "NotePrefs";
    private static final String NOTES_KEY = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        fab = findViewById(R.id.fab);
        b2 = findViewById(R.id.note_btn);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        // 加載保存的筆記
        loadNotes();

        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTimeAndDate();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timeUpdater);

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent note = new Intent(MainActivity.this, Notes.class);
                startActivityForResult(note, 1);
            }
        });
    }

    private void updateTimeAndDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        timeTextView.setText(currentTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateTextView.setText(currentDate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeUpdater);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String noteListJson = data.getStringExtra("noteList");
            if (noteListJson != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<NoteItem>>() {}.getType();
                List<NoteItem> updatedNotes = gson.fromJson(noteListJson, listType);

                noteList.clear();
                noteList.addAll(updatedNotes);
                noteAdapter.notifyDataSetChanged();

                // 保存筆記清單
                saveNotes();
            }
        }
    }

    private void saveNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString(NOTES_KEY, json);
        editor.apply();
    }

    private void loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(NOTES_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<NoteItem>>() {}.getType();
            List<NoteItem> savedNotes = gson.fromJson(json, listType);
            noteList.addAll(savedNotes);
        }
    }
}
