/*
package com.example.focus_plus;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}*/

package com.example.focus_plus;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TimerActivity extends AppCompatActivity {

    private CircularTimerView circularTimerView; // 圓形倒數計時器
    private TextView cycleText, focusRemainingText, breakRemainingText;
    private Button pauseButton, startButton, exitButton;

    private CardView focusTimeCard, breakTimeCard; // 剩餘專注時間與休息時間的 CardView

    private CountDownTimer timer;
    private boolean isPaused = false;
    private boolean isRunning = false;

    private long totalTimeRemaining;
    private long phaseTimeRemaining; // 當前階段剩餘時間
    private long focusTime, breakTime;

    private int currentCycle = 1, totalCycles;
    private boolean isFocusPhase = true; // 用於判斷專注或休息階段

    private MediaPlayer biteSound; // 結束音效播放器
    private MediaPlayer backgroundMusicPlayer; // 背景音樂播放器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // 綁定 UI 元件
        circularTimerView = findViewById(R.id.circularTimerView);
        cycleText = findViewById(R.id.cycleText);
        focusRemainingText = findViewById(R.id.focusRemainingText);
        breakRemainingText = findViewById(R.id.breakRemainingText);
        pauseButton = findViewById(R.id.pauseButton);
        startButton = findViewById(R.id.startButton);
        exitButton = findViewById(R.id.exitButton);
        focusTimeCard = findViewById(R.id.focusTimeCard);
        breakTimeCard = findViewById(R.id.breakTimeCard);

        // 加載結束音效
        Log.d("TimerActivity", "Initializing MediaPlayer for bite_sound");
        biteSound = MediaPlayer.create(this, R.raw.bite_sound);
        if (biteSound == null) {
            Log.e("TimerActivity", "Failed to initialize MediaPlayer for bite_sound");
        }

        // 從 Intent 中獲取數據
        totalCycles = getIntent().getIntExtra("cycles", 4);
        focusTime = getIntent().getIntExtra("focusTime", 25) * 60 * 1000; // 將分鐘轉換為毫秒
        breakTime = getIntent().getIntExtra("breakTime", 5) * 60 * 1000;
        totalTimeRemaining = totalCycles * (focusTime + breakTime); // 總倒數時間
        phaseTimeRemaining = focusTime; // 初始為專注時間

        // 獲取音樂選項
        String selectedMusic = getIntent().getStringExtra("selectedMusic");
        Log.d("TimerActivity", "Selected music: " + selectedMusic);

        // 根據選項設置背景音樂
        if (selectedMusic != null) {
            switch (selectedMusic) {
                case "輕音樂":
                    backgroundMusicPlayer = MediaPlayer.create(this, R.raw.cafe_music);
                    break;
                case "鋼琴音樂":
                    backgroundMusicPlayer = MediaPlayer.create(this, R.raw.piano_music);
                    break;
                case "白噪音":
                    backgroundMusicPlayer = MediaPlayer.create(this, R.raw.white_noise_music);
                    break;
                default:
                    backgroundMusicPlayer = null; // 無背景音樂
                    break;
            }
        }

        // 開始背景音樂
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setLooping(true); // 背景音樂循環播放
            backgroundMusicPlayer.start();
        }

        circularTimerView.setMaxProgress(100); // 設置最大進度

        // 初始化按鈕事件
        pauseButton.setOnClickListener(v -> pauseTimer());
        startButton.setOnClickListener(v -> resumeTimer());
        exitButton.setOnClickListener(v -> finish());

        // 開始計時
        startTimer(phaseTimeRemaining);
    }

    private void startTimer(long time) {
        isRunning = true;

        // 恢復背景音樂 (僅在專注階段開始時)
        if (isFocusPhase && backgroundMusicPlayer != null) {
            backgroundMusicPlayer.start();
        }

        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                phaseTimeRemaining = millisUntilFinished; // 更新當前階段時間
                totalTimeRemaining -= 1000; // 更新總時間

                // 更新圓形計時器進度和中心文字
                int progress = (int) ((totalTimeRemaining * 100) / (totalCycles * (focusTime + breakTime)));
                circularTimerView.setProgress(progress);
                circularTimerView.setCenterText(formatTime(totalTimeRemaining)); // 設置剩餘總時間

                // 更新專注或休息時間的文字和背景
                if (isFocusPhase) {
                    focusRemainingText.setText(formatTime(phaseTimeRemaining));
                    breakRemainingText.setText(formatTime(breakTime));
                    updateCardBackgrounds(true);
                } else {
                    breakRemainingText.setText(formatTime(phaseTimeRemaining));
                    focusRemainingText.setText(formatTime(focusTime));
                    updateCardBackgrounds(false);
                }
            }

            @Override
            public void onFinish() {
                // 暫停背景音樂
                if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
                    backgroundMusicPlayer.pause();
                }

                // 播放結束音效
                if (biteSound != null) {
                    Log.d("TimerActivity", "Playing sound for phase end");
                    biteSound.start();
                } else {
                    Log.e("TimerActivity", "MediaPlayer for bite_sound is null, cannot play sound");
                }

                // 切換專注與休息階段
                if (isFocusPhase) {
                    isFocusPhase = false; // 進入休息階段
                    phaseTimeRemaining = breakTime;
                } else {
                    isFocusPhase = true; // 進入專注階段
                    phaseTimeRemaining = focusTime;

                    // 進入下一週期
                    if (currentCycle < totalCycles) {
                        currentCycle++;
                        cycleText.setText("第 " + currentCycle + " 週期");
                    } else {
                        // 所有週期完成
                        cycleText.setText("已完成所有週期!");
                        circularTimerView.setProgress(0); // 設置進度為 0
                        circularTimerView.setCenterText("00:00:00"); // 設置中心文字為完成時間
                        return;
                    }
                }

                // 重新開始下一階段計時
                startTimer(phaseTimeRemaining);
            }
        }.start();
    }

    private void pauseTimer() {
        if (isRunning) {
            timer.cancel();
            isPaused = true;
            isRunning = false;

            // 暫停背景音樂
            if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
                backgroundMusicPlayer.pause();
            }
        }
    }

    private void resumeTimer() {
        if (isPaused) {
            isPaused = false;

            // 恢復背景音樂
            if (backgroundMusicPlayer != null && isFocusPhase) {
                backgroundMusicPlayer.start();
            }

            startTimer(phaseTimeRemaining);
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private void updateCardBackgrounds(boolean isFocusPhase) {
        if (isFocusPhase) {
            focusTimeCard.setCardBackgroundColor(0xFFFFFFFF); // 白色背景
            breakTimeCard.setCardBackgroundColor(0xFF777777); // #EEEAE7
        } else {
            breakTimeCard.setCardBackgroundColor(0xFFFFFFFF); // 白色背景
            focusTimeCard.setCardBackgroundColor(0xFF777777); // #EEEAE7
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 釋放結束音效資源
        if (biteSound != null) {
            Log.d("TimerActivity", "Releasing MediaPlayer for bite_sound");
            biteSound.release();
        }

        // 釋放背景音樂資源
        if (backgroundMusicPlayer != null) {
            Log.d("TimerActivity", "Releasing MediaPlayer for background music");
            backgroundMusicPlayer.release();
        }
    }
}
