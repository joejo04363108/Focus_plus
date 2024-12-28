package com.example.focus_plus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class curriculum extends AppCompatActivity {

    private static final int COLUMN_COUNT = 5; // 星期一至星期五
    private static final int ROW_COUNT = 9;    // 9節課
    private static final String PREFS_NAME = "CoursePrefs";
    private static final String COURSES_KEY = "courses";

    private GridView gridViewSchedule;
    private ImageButton btnAddCourse;
    private ArrayList<Course> courseList = new ArrayList<>(); // 存放課程資料
    private ArrayList<String> gridData; // 用於 GridView 的資料
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        // 初始化元件
        gridViewSchedule = findViewById(R.id.gridViewSchedule);
        btnAddCourse = findViewById(R.id.btnAddCourse);

        // 綁定返回按鈕
        ImageView focusIcon = findViewById(R.id.imageFocus);
        focusIcon.setOnClickListener(v -> finish());

        // 初始化 GridView 資料 (僅包含課程欄位，節次不重複)
        gridData = new ArrayList<>();
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                gridData.add(""); // 預設課程空白
            }
        }

        // 設定 Adapter (每格高度為 208px)
        int gridItemHeight = 208;
        adapter = new CustomAdapter(this, gridData, gridItemHeight);
        gridViewSchedule.setNumColumns(COLUMN_COUNT);
        gridViewSchedule.setAdapter(adapter);

        // 加載保存的課程
        loadCourses();

        // 新增課程按鈕
        btnAddCourse.setOnClickListener(v -> showCourseDialog(-1));

        // 點擊 GridView 修改課程
        gridViewSchedule.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            int column = position % COLUMN_COUNT;
            int row = position / COLUMN_COUNT;

            for (Course course : courseList) {
                if (course.containsPosition(row, column)) {
                    showCourseDialog(courseList.indexOf(course));
                    return;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish(); // 返回主畫面
    }

    private boolean isTimeConflict(int dayOfWeek, int startPeriod, int endPeriod, int position) {
        for (int i = 0; i < courseList.size(); i++) {
            if (i == position) continue; // 跳過當前課程
            Course course = courseList.get(i);
            if (course.dayOfWeek == dayOfWeek &&
                    startPeriod <= course.endPeriod &&
                    endPeriod >= course.startPeriod) {
                return true;
            }
        }
        return false;
    }

    /**
     * 顯示新增/修改課程對話框
     */
    private void showCourseDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_course, null);
        builder.setView(dialogView);

        // 取得元件
        EditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        Spinner spDayOfWeek = dialogView.findViewById(R.id.spDayOfWeek);
        Spinner spStartPeriod = dialogView.findViewById(R.id.spStartPeriod);
        Spinner spEndPeriod = dialogView.findViewById(R.id.spEndPeriod);
        EditText etTeacherName = dialogView.findViewById(R.id.etTeacherName);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        Button btnConfirm = dialogView.findViewById(R.id.btnModify);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        // 設定 Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.periods));
        spDayOfWeek.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.days_of_week)));
        spStartPeriod.setAdapter(spinnerAdapter);
        spEndPeriod.setAdapter(spinnerAdapter);

        AlertDialog dialog = builder.create();

        // 如果是修改，填入現有資料
        if (position != -1) {
            Course course = courseList.get(position);
            etCourseName.setText(course.courseName);
            etTeacherName.setText(course.teacherName);
            etLocation.setText(course.location);
            spDayOfWeek.setSelection(course.dayOfWeek);
            spStartPeriod.setSelection(course.startPeriod);
            spEndPeriod.setSelection(course.endPeriod);
        }

        // 確認按鈕
        btnConfirm.setOnClickListener(v -> {
            String courseName = etCourseName.getText().toString();
            String teacherName = etTeacherName.getText().toString();
            String location = etLocation.getText().toString();
            int dayOfWeek = spDayOfWeek.getSelectedItemPosition();
            int startPeriod = spStartPeriod.getSelectedItemPosition();
            int endPeriod = spEndPeriod.getSelectedItemPosition();

            if (courseName.isEmpty()) {
                Toast.makeText(this, "課程名稱不能為空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startPeriod > endPeriod) {
                Toast.makeText(this, "時間區間不合理", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isTimeConflict(dayOfWeek, startPeriod, endPeriod, position)) {
                Toast.makeText(this, "時間衝突，請重新選擇", Toast.LENGTH_SHORT).show();
                return;
            }

            if (position == -1) {
                // 新增課程
                courseList.add(new Course(courseName, teacherName, location, dayOfWeek, startPeriod, endPeriod));
            } else {
                // 修改課程
                Course course = courseList.get(position);
                course.update(courseName, teacherName, location, dayOfWeek, startPeriod, endPeriod);
            }

            saveCourses(); // 保存課程列表
            updateScheduleDisplay();
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (position != -1) {
                courseList.remove(position);
                saveCourses(); // 保存課程列表
                updateScheduleDisplay();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 更新課表顯示
     */
    private void updateScheduleDisplay() {
        // 清空 GridView
        for (int i = 0; i < gridData.size(); i++) {
            gridData.set(i, "");
        }

        // 更新課程名稱
        for (Course course : courseList) {
            for (int i = course.startPeriod; i <= course.endPeriod; i++) {
                int index = i * COLUMN_COUNT + course.dayOfWeek;
                if (i == course.startPeriod) {
                    gridData.set(index, course.courseName); // 第一格填入名稱
                } else {
                    gridData.set(index, ""); // 其他格設為空字串
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * 保存課程到 SharedPreferences
     */
    private void saveCourses() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(courseList);
        editor.putString(COURSES_KEY, json);
        editor.apply();
    }

    /**
     * 從 SharedPreferences 加載課程
     */
    private void loadCourses() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(COURSES_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Course>>() {}.getType();
            List<Course> savedCourses = gson.fromJson(json, listType);

            courseList.clear();
            courseList.addAll(savedCourses);

            updateScheduleDisplay();
        }
    }

    /**
     * 課程類別
     */
    static class Course {
        String courseName, teacherName, location;
        int dayOfWeek, startPeriod, endPeriod;

        public Course(String name, String teacher, String loc, int day, int start, int end) {
            courseName = name;
            teacherName = teacher;
            location = loc;
            dayOfWeek = day;
            startPeriod = start;
            endPeriod = end;
        }

        public void update(String name, String teacher, String loc, int day, int start, int end) {
            courseName = name;
            teacherName = teacher;
            location = loc;
            dayOfWeek = day;
            startPeriod = start;
            endPeriod = end;
        }

        public boolean containsPosition(int row, int col) {
            return col == dayOfWeek && row >= startPeriod && row <= endPeriod;
        }
    }

    /**
     * 自定義 Adapter，支持設置每格高度和框線樣式
     */
    static class CustomAdapter extends ArrayAdapter<String> {
        private int itemHeight;

        public CustomAdapter(curriculum context, ArrayList<String> items, int height) {
            super(context, android.R.layout.simple_list_item_1, items);
            this.itemHeight = height;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // 設置每格高度
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            } else {
                params.height = itemHeight;
            }

            // 根據內容設置樣式
            String item = getItem(position);
            if (item != null && !item.isEmpty()) {
                // 計算課程跨行高度
                for (Course course : ((curriculum) getContext()).courseList) {
                    if (course.courseName.equals(item)) {
                        int spanCount = course.endPeriod - course.startPeriod + 1;
                        params.height = itemHeight * spanCount; // 設置跨行高度
                        break;
                    }
                }
                view.setLayoutParams(params);
                view.setBackgroundResource(R.drawable.curriculum_grid_border); // 顯示邊框
            } else {
                // 空格不顯示邊框
                view.setBackgroundResource(0);
            }

            // 設置文字置中樣式
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(16);
                textView.setTextColor(getContext().getResources().getColor(android.R.color.black)); // 設置文字顏色為白色
            }

            return view;
        }
    }
}
