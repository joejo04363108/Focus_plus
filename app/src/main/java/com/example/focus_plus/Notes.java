package com.example.focus_plus;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import java.util.Calendar;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Date;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Notes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteList;

    private static final String PREFS_NAME = "NotesPrefs";
    private static final String NOTES_KEY = "noteList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // 適配 Android 13 的通知權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // 檢查是否擁有 POST_NOTIFICATIONS 權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 請求權限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        recyclerView = findViewById(R.id.recyclerView_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteList = loadNotes(); // 從 SharedPreferences 加載數據
        if (noteList == null) {
            noteList = new ArrayList<>();
        }

        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        // 返回按鈕
        ImageButton backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> saveAndReturn());

        // 新增按鈕
        ImageButton addButton = findViewById(R.id.add_btn);
        addButton.setOnClickListener(v -> showAddTaskDialog());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知權限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "通知權限被拒絕，提醒功能將無法使用", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveAndReturn() {
        saveNotes(); // 保存數據到 SharedPreferences

        // 將筆記列表作為 JSON 字符串返回給 MainActivity
        Intent intent = new Intent();
        Gson gson = new Gson();
        String noteListJson = gson.toJson(noteList);
        intent.putExtra("noteList", noteListJson);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showAddTaskDialog() {
        // 彈出新增任務的對話框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_task_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText titleInput = dialogView.findViewById(R.id.title_input);
        EditText dateTimeInput = dialogView.findViewById(R.id.date_time_input);
        EditText contentInput = dialogView.findViewById(R.id.content_input);
        Spinner typeSpinner = dialogView.findViewById(R.id.type_spinner);

        // 設置 Spinner 選項
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.note_categories, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerAdapter);


        // 日期與時間選擇器
        dateTimeInput.setOnClickListener(v -> showDateTimePicker(dateTimeInput));

        //cancelButton.setOnClickListener(v -> dialog.dismiss());

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String dateTime = dateTimeInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();

            if (!title.isEmpty() && !type.isEmpty() && !dateTime.isEmpty()) {
                NoteItem newNote = new NoteItem(title, type, dateTime, content);
                noteList.add(newNote);

                // 設定提醒
                setReminder(title, type, dateTime);

                noteAdapter.notifyDataSetChanged();
                saveNotes();
                dialog.dismiss();
            } else {
                if (title.isEmpty()) titleInput.setError("請填寫標題");
                if (dateTime.isEmpty()) dateTimeInput.setError("請填寫日期與時間");
            }
        });

    }

    private void setReminder(String title, String type, String dateTime) {
        try {
            // 將日期時間字串轉換為毫秒數
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            Date date = dateTimeFormat.parse(dateTime);
            if (date == null) throw new IllegalArgumentException("無法解析日期時間");

            long triggerTime = date.getTime();
            long currentTime = System.currentTimeMillis();

            if (triggerTime > currentTime) {
                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("title", title);
                intent.putExtra("type", type);

                // 使用唯一請求碼
                int requestCode = (title + dateTime).hashCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                    Toast.makeText(this, "提醒已設定: " + dateTime, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "無法設定過去的時間提醒", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "提醒設定失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void showDateTimePicker(EditText dateTimeInput) {
        final Calendar calendar = Calendar.getInstance();

        // 日期選擇器
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    // 時間選擇器
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // 格式化日期和時間
                                String selectedDateTime = new java.text.SimpleDateFormat(
                                        "yyyy/MM/dd HH:mm", Locale.getDefault())
                                        .format(calendar.getTime());
                                dateTimeInput.setText(selectedDateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);

                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    private void saveNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String noteListJson = gson.toJson(noteList);
        editor.putString(NOTES_KEY, noteListJson);
        editor.apply();
    }

    private List<NoteItem> loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String noteListJson = sharedPreferences.getString(NOTES_KEY, null);
        Type listType = new TypeToken<List<NoteItem>>() {}.getType();
        return gson.fromJson(noteListJson, listType);
    }

    @Override
    public void onBackPressed() {
        saveAndReturn(); // 按返回鍵時保存數據
    }

}
