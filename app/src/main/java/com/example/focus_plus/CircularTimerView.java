package com.example.focus_plus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircularTimerView extends View {

    private Paint backgroundPaint, progressPaint, textPaint, titlePaint;
    private int progress = 0; // 當前進度
    private int maxProgress = 100; // 總進度
    private String centerText = "00:00"; // 中心顯示的文字（默認為剩餘總時間）

    public CircularTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化背景圓形畫筆
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFF9A9590); // 背景色 (灰褐色)
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);
        backgroundPaint.setAntiAlias(true);

        // 初始化進度圓弧畫筆
        progressPaint = new Paint();
        progressPaint.setColor(0xFFEEEAE7); // 進度色 (淺米色)
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setAntiAlias(true);

        // 初始化文字畫筆（中心文字）
        textPaint = new Paint();
        textPaint.setColor(0xFFFFFFFF); // 白色文字
        textPaint.setTextSize(120f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // 初始化標題畫筆（上方文字）
        titlePaint = new Paint();
        titlePaint.setColor(0xFFFFFFFF); // 白色文字
        titlePaint.setTextSize(60f); // 調整上方文字的字體大小
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setAntiAlias(true);
        titlePaint.setFakeBoldText(true); // 啟用粗體
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 20;

        // 繪製背景圓形
        canvas.drawCircle(width / 2, height / 2, radius, backgroundPaint);

        // 繪製進度圓弧
        float sweepAngle = 360f * progress / maxProgress;
        canvas.drawArc(
                width / 2 - radius, height / 2 - radius,
                width / 2 + radius, height / 2 + radius,
                -90, sweepAngle, false, progressPaint
        );

        // 繪製上方文字「剩餘總時間」
        canvas.drawText("剩餘總時間", width / 2, height / 2 - 70, titlePaint);

        // 繪製中心文字
        canvas.drawText(centerText, width / 2, height / 2 + 100, textPaint);
    }

    // 更新進度
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate(); // 重繪
    }

    // 設置總進度
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        invalidate(); // 重繪
    }

    // 設置中心文字
    public void setCenterText(String text) {
        this.centerText = text;
        invalidate(); // 重繪
    }
}
