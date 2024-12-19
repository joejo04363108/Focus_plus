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
                noteList.add(new NoteItem(title, type, dateTime, content));
                //filterNotes("全部"); // 更新清單
                dialog.dismiss();
            } else {
                // 提示用戶填寫完整資料
                if (title.isEmpty()) titleInput.setError("請填寫標題");
                if (dateTime.isEmpty()) dateTimeInput.setError("請填寫日期與時間");
            }
        });
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
