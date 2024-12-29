package com.example.focus_plus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "todo_channel"; // 代辦事項頻道
    private static final String CHANNEL_ID_COURSE = "course_channel"; // 課程提醒頻道

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "接收到提醒廣播");

        // 獲取提醒的類型
        String reminderType = intent.getStringExtra("reminderType");
        String title = intent.getStringExtra("title");
        String type = intent.getStringExtra("type");

        Log.d("ReminderReceiver", "reminderType: " + reminderType + ", title: " + title + ", type: " + type);

        // 獲取通知管理器
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (reminderType == null) {
            return; // 如果類型為空，不執行操作
        }

        if (reminderType.equals("todo")) {
            // 代辦事項提醒
            createTodoNotification(context, notificationManager, title, type);
        } else if (reminderType.equals("course")) {
            // 課程提醒
            createCourseNotification(context, notificationManager, title, type);
        }

    }

    private void createTodoNotification(Context context, NotificationManager notificationManager, String title, String type) {
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

    private void createCourseNotification(Context context, NotificationManager notificationManager, String title, String location) {
        // 創建通知渠道 (僅適用於 Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_COURSE,
                    "課程提醒",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("課程相關通知");
            notificationManager.createNotificationChannel(channel);
        }

        // 創建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_COURSE)
                .setSmallIcon(R.drawable.ic_focus) // 替換為你的圖示資源
                .setContentTitle("課程提醒：" + title)
                .setContentText("地點：" + location)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 發送通知
        notificationManager.notify((title + location).hashCode(), builder.build());
    }
}
