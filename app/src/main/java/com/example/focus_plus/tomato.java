package com.example.focus_plus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class tomato extends AppCompatActivity {

    private Spinner cycleSpinner, focusSpinner, breakSpinner, musicSpinner;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato);

        // 綁定 XML 中的 Spinner 和 Button
        cycleSpinner = findViewById(R.id.cycleSpinner);
        focusSpinner = findViewById(R.id.focusSpinner);
        breakSpinner = findViewById(R.id.breakSpinner);
        musicSpinner = findViewById(R.id.musicSpinner);
        startButton = findViewById(R.id.startButton);

        // 綁定返回按鈕
        ImageView focusIcon = findViewById(R.id.focusIcon);

        // 返回主畫面
        focusIcon.setOnClickListener(v -> finish());

        // 設置數據源和自定義樣式
        ArrayAdapter<CharSequence> cycleAdapter = ArrayAdapter.createFromResource(this, R.array.cycle_array, R.layout.tomato_spinner_item);
        cycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cycleSpinner.setAdapter(cycleAdapter);

        ArrayAdapter<CharSequence> focusAdapter = ArrayAdapter.createFromResource(this, R.array.focus_time_array, R.layout.tomato_spinner_item);
        focusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        focusSpinner.setAdapter(focusAdapter);

        ArrayAdapter<CharSequence> breakAdapter = ArrayAdapter.createFromResource(this, R.array.break_time_array, R.layout.tomato_spinner_item);
        breakAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breakSpinner.setAdapter(breakAdapter);

        ArrayAdapter<CharSequence> musicAdapter = ArrayAdapter.createFromResource(this, R.array.music_options, R.layout.tomato_spinner_item);
        musicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        musicSpinner.setAdapter(musicAdapter);

        // 設定按鈕點擊事件
        startButton.setOnClickListener(v -> {
            // 獲取選擇的值
            int cycles = Integer.parseInt(cycleSpinner.getSelectedItem().toString());
            int focusTime = Integer.parseInt(focusSpinner.getSelectedItem().toString());
            int breakTime = Integer.parseInt(breakSpinner.getSelectedItem().toString());
            String selectedMusic = musicSpinner.getSelectedItem().toString();

            // 傳遞數據到下一個 Activity
            Intent intent = new Intent(tomato.this, TimerActivity.class);
            intent.putExtra("cycles", cycles);
            intent.putExtra("focusTime", focusTime);
            intent.putExtra("breakTime", breakTime);
            intent.putExtra("selectedMusic", selectedMusic);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        finish(); // 返回主畫面
    }
}


