package com.example.focus_plus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String type = intent.getStringExtra("type");

        // 創建通知渠道 (僅適用於 Android 8.0+)
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "提醒通知",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("筆記提醒");
            notificationManager.createNotificationChannel(channel);
        }

        // 創建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_focus) // 替換為你的圖示資源
                .setContentTitle("提醒：" + title)
                .setContentText("類別：" + type)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 發送通知
        notificationManager.notify((title + type).hashCode(), builder.build());
    }
}
