package com.example.focus_plus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView timeTextView;
    private TextView dateTextView;
    private TextView currentCourseTextView;
    private TextView nextCourseTextView;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    private FloatingActionButton fab;
    private Button b1, b2;
    private RecyclerView recyclerView;

    private NoteAdapter noteAdapter;
    private List<NoteItem> noteList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();

    private static final String PREFS_NAME = "CoursePrefs";
    private static final String COURSES_KEY = "courses";
    private static final String NOTES_KEY = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化元件
        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        currentCourseTextView = findViewById(R.id.currentCourseTextView);
        nextCourseTextView = findViewById(R.id.nextCourseTextView);
        fab = findViewById(R.id.fab);
        b1 = findViewById(R.id.class_btn);
        b2 = findViewById(R.id.note_btn);
        recyclerView = findViewById(R.id.recyclerView);

        // 設置 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        // 加載課程與筆記
        loadCourses();
        loadNotes();

        // 設定時間更新
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTimeAndDate();
                updateCourseStatus(); // 更新課程狀態
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timeUpdater);

        // 按鈕事件
        b1.setOnClickListener(v -> {
            Intent cur = new Intent(MainActivity.this, curriculum.class);
            startActivity(cur);
        });

        b2.setOnClickListener(v -> {
            Intent note = new Intent(MainActivity.this, Notes.class);
            startActivityForResult(note, 1);
        });

        fab.setOnClickListener(v -> {
            Intent tmt = new Intent(MainActivity.this, tomato.class);
            startActivity(tmt);
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

    private void updateCourseStatus() {
        // 獲取當前時間
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        //int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1

        // 調整星期格式（將 Sunday=1 調整為 Monday=1, Sunday=7）
        //int adjustedDayOfWeek = currentDayOfWeek == Calendar.SUNDAY ? 7 : currentDayOfWeek - 1;

        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1
        // 將星期調整為 Monday = 0, Sunday = 6
        int adjustedDayOfWeek = (currentDayOfWeek + 5) % 7;

        // 判斷當前時間屬於哪一節
        int currentPeriod;
        if (currentHour >= 8 && currentHour < 9) {
            currentPeriod = 0;
        } else if (currentHour >= 9 && currentHour < 10) {
            currentPeriod = 1;
        } else if (currentHour >= 10 && currentHour < 11) {
            currentPeriod = 2;
        } else if (currentHour >= 11 && currentHour < 12) {
            currentPeriod = 3;
        } else if (currentHour >= 13 && currentHour < 14) {
            currentPeriod = 4;
        } else if (currentHour >= 14 && currentHour < 15) {
            currentPeriod = 5;
        } else if (currentHour >= 15 && currentHour < 16) {
            currentPeriod = 6;
        } else if (currentHour >= 16 && currentHour < 17) {
            currentPeriod = 7;
        } else if (currentHour >= 17 && currentHour < 18) {
            currentPeriod = 8;
        } else {
            currentPeriod = -1; // 非課堂時間
        }

        Log.d("MainActivity", "當前時間: " + currentHour + ":" + currentMinute +
                ", 星期: " + adjustedDayOfWeek + ", 節次: " + currentPeriod);

        // 確認是否有課程
        Course currentCourse = null;
        Course nextCourse = null;

        for (Course course : courseList) {
            if (course.dayOfWeek == adjustedDayOfWeek) {
                if (course.startPeriod <= currentPeriod && currentPeriod <= course.endPeriod) {
                    currentCourse = course;
                } else if (course.startPeriod > currentPeriod) {
                    if (nextCourse == null || course.startPeriod < nextCourse.startPeriod) {
                        nextCourse = course;
                    }
                }
            }
        }

        // 更新 TextView
        if (currentCourse != null) {
            currentCourseTextView.setText(currentCourse.courseName);
        } else {
            currentCourseTextView.setText("當前無課程");
        }

        if (nextCourse != null) {
            nextCourseTextView.setText(nextCourse.courseName);
        } else {
            nextCourseTextView.setText("本日已無課程");
        }
    }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeUpdater);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新載入課表
        loadCourses();
        // 更新課程狀態
        updateCourseStatus();
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
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString(NOTES_KEY, json);
        editor.apply();
    }

    private void loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = sharedPreferences.getString(NOTES_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<NoteItem>>() {}.getType();
            List<NoteItem> savedNotes = gson.fromJson(json, listType);
            noteList.addAll(savedNotes);
        }
    }

    private void loadCourses() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = sharedPreferences.getString(COURSES_KEY, null);

        if (json != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Course>>() {}.getType();

            try {
                List<Course> savedCourses = gson.fromJson(json, listType);

                if (savedCourses != null) {
                    courseList.clear();
                    courseList.addAll(savedCourses);

                    // 日誌輸出，確認課程清單是否正確讀取
                    for (Course course : courseList) {
                        Log.d("MainActivity", "課程讀取成功: " +
                                "名稱: " + course.courseName +
                                ", 星期: " + course.dayOfWeek +
                                ", 開始時間: " + course.startPeriod +
                                ", 結束時間: " + course.endPeriod);
                    }
                } else {
                    Log.e("MainActivity", "課程清單讀取失敗，資料為 null");
                }
            } catch (Exception e) {
                Log.e("MainActivity", "解析課程資料失敗: " + e.getMessage());
            }
        } else {
            Log.d("MainActivity", "尚無課程資料儲存");
        }
    }


    static class Course {
        String courseName;
        int dayOfWeek;
        int startPeriod;
        int endPeriod;

        public Course(String courseName, int dayOfWeek, int startPeriod, int endPeriod) {
            this.courseName = courseName;
            this.dayOfWeek = dayOfWeek;
            this.startPeriod = startPeriod;
            this.endPeriod = endPeriod;
        }
    }
}
