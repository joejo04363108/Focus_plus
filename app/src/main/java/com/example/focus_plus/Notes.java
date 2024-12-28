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
import android.os.CountDownTimer;

public class Notes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteList;

    private static final String PREFS_NAME = "NotesPrefs";
    private static final String NOTES_KEY = "noteList";
    private List<NoteItem> allNotes = new ArrayList<>(); // 保存所有的筆記數據

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

        noteList = loadNotes();
        if (noteList == null) {
            noteList = new ArrayList<>();
        }
        allNotes = new ArrayList<>(noteList); // 初始化完整清單

        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        // 返回按鈕
        ImageButton backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> saveAndReturn());

        // 新增按鈕
        ImageButton addButton = findViewById(R.id.add_btn);
        addButton.setOnClickListener(v -> showAddTaskDialog());

        noteAdapter.setOnItemLongClickListener(position -> showDeleteDialog(position));

        Spinner filterSpinner = findViewById(R.id.filter_spinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                filterNotes(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 預設顯示所有筆記
                filterNotes("全部");
            }
        });

    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("刪除筆記");
        builder.setMessage("確定要刪除此筆記嗎？");
        builder.setPositiveButton("確定", (dialog, which) -> {
            // 刪除選中筆記
            NoteItem removedNote = noteList.get(position);
            noteList.remove(position);          // 更新當前顯示的清單
            allNotes.remove(removedNote);       // 從完整清單中刪除
            noteAdapter.notifyItemRemoved(position);
            saveNotes();                        // 保存到 SharedPreferences

            Toast.makeText(this, "筆記已刪除", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
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

                allNotes.add(newNote);  // 添加到完整清單
                noteList.add(newNote);  // 更新當前顯示的清單
                noteAdapter.notifyDataSetChanged();
                saveNotes();

                setReminder(title, type, dateTime);

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
            Date targetDate = dateTimeFormat.parse(dateTime);
            if (targetDate == null) throw new IllegalArgumentException("無法解析日期時間");

            long currentTime = System.currentTimeMillis(); // 當前時間
            long triggerTime = targetDate.getTime(); // 設定的目標時間
            long delay = triggerTime - currentTime; // 計算延遲時間（毫秒）

            if (delay > 0) {
                // 使用 CountDownTimer 進行倒數計時
                new CountDownTimer(delay, 1000) { // 每秒更新一次
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 可選：顯示倒數時間（例如用於除錯或視覺化）
                    }

                    @Override
                    public void onFinish() {
                        // 倒數結束，觸發通知
                        triggerNotification(title, type);
                    }
                }.start();

                Toast.makeText(this, "提醒已設定，將在 " + delay / 1000 + " 秒後觸發", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "無法設定過去的時間提醒", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "提醒設定失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void triggerNotification(String title, String type) {
        // 創建通知渠道 (Android 8.0+)
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "提醒通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("筆記提醒");
            notificationManager.createNotificationChannel(channel);
        }

        // 創建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "reminder_channel")
                .setSmallIcon(R.drawable.ic_focus) // 替換為你的圖示資源
                .setContentTitle("提醒：" + title)
                .setContentText("類別：" + type)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 發送通知
        notificationManager.notify((title + type).hashCode(), builder.build());
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
        String noteListJson = gson.toJson(allNotes); // 保存完整數據
        editor.putString(NOTES_KEY, noteListJson);
        editor.apply();
    }


    private List<NoteItem> loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String noteListJson = sharedPreferences.getString(NOTES_KEY, null);
        Type listType = new TypeToken<List<NoteItem>>() {}.getType();
        List<NoteItem> notes = gson.fromJson(noteListJson, listType);

        if (notes != null) {
            allNotes.clear();
            allNotes.addAll(notes); // 保存所有筆記
        }
        return notes != null ? notes : new ArrayList<>();
    }


    private void filterNotes(String category) {
        if (category.equals("全部")) {
            // 顯示所有筆記
            noteAdapter.updateNotes(allNotes);
        } else {
            // 基於 allNotes 進行篩選
            List<NoteItem> filteredNotes = new ArrayList<>();
            for (NoteItem note : allNotes) {
                if (note.getType().equals(category)) {
                    filteredNotes.add(note);
                }
            }
            noteAdapter.updateNotes(filteredNotes);
        }
    }



    @Override
    public void onBackPressed() {
        saveAndReturn(); // 按返回鍵時保存數據
    }

}
