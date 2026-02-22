package com.example.administrator.dsgroupware2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.content.pm.ServiceInfo;
import android.util.Log;
import android.content.pm.PackageManager;
import android.Manifest;

public class GpsService extends Service {
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("GpsService", "서비스 시작됨");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("GpsService", "위치 권한 없음. 서비스 종료.");
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("GpsService", "FOREGROUND_SERVICE_LOCATION 권한 없음. 서비스 종료.");
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        createNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification.build());
        }

        return START_REDELIVER_INTENT;
    }

    private void createNotification(){
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,0,mainIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    "channelMain",
                    "GPS 서비스 알림",
                    NotificationManager.IMPORTANCE_LOW // IMPORTANCE_NONE 사용 금지
            );

            channel.setDescription("GPS 서비스가 실행 중입니다.");

            notificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
            notificationManager.createNotificationChannel(channel);

            // setSlient 메서드에 true를 넘기면 서비스가 올라올 때 알림음(진동)이 발생되지 않는다.
            notification =
                    new NotificationCompat.Builder(getApplicationContext(), "channelMain")
                            .setSmallIcon(R.drawable.notify_icon_ds)
                            .setContentTitle("동신툴피아")
                            .setContentIntent(pendingIntent)
                            .setContentText("동신툴피아 그룹웨어 앱이 실행중입니다.")
                            .setSilent(true);

            notificationManager.notify(1, notification.build());
        }
    }
}
