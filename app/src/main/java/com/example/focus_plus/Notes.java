package com.example.focus_plus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.util.Calendar;

public class Notes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        // 返回按鈕
        ImageButton backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> {
            // 返回到 MainActivity
            Intent intent = new Intent(Notes.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 新增按鈕
        ImageButton addButton = findViewById(R.id.add_btn);
        addButton.setOnClickListener(v -> showAddTaskDialog());

        noteList.add(new NoteItem("完成報告", "課業", "2024/12/20 18:00", "需要完成結案報告"));
        noteList.add(new NoteItem("買菜", "生活", "2024/12/21 10:00", "購買下週的食材"));
        noteAdapter.notifyDataSetChanged();
    }

    private void showAddTaskDialog() {
        // 建立彈出視窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_task_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 初始化彈窗中的元件
        EditText titleInput = dialogView.findViewById(R.id.title_input);
        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.type_radioGroup);
        EditText dateTimeInput = dialogView.findViewById(R.id.date_time_input);
        EditText contentInput = dialogView.findViewById(R.id.content_input);

        // 日期選擇器功能
        dateTimeInput.setOnClickListener(v -> showDatePicker(dateTimeInput));

        // 按鈕功能
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            // 收集使用者輸入的資料
            String title = titleInput.getText().toString().trim();
            String dateTime = dateTimeInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();
            String type = "";

            // 確認選擇的類別
            int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
            if (selectedTypeId != -1) {
                RadioButton selectedTypeButton = dialogView.findViewById(selectedTypeId);
                type = selectedTypeButton.getText().toString();
            }

            // 檢查輸入資料是否有效
            if (!title.isEmpty() && !type.isEmpty() && !dateTime.isEmpty()) {
                // 新增到清單中
                noteList.add(new NoteItem(title, type, dateTime, content));
                noteAdapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                // 提示使用者填寫完整資料
                titleInput.setError("請填寫標題");
                dateTimeInput.setError("請填寫日期與時間");
            }
        });
    }
    private void showDatePicker(EditText dateInput) {
        // 取得目前日期
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 建立日期選擇器
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 更新輸入欄位文字
                    String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                    dateInput.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
}
