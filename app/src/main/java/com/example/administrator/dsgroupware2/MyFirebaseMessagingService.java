package com.example.administrator.dsgroupware2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        Log.d(TAG, "FFFFFFFFFFFFFFFFrom : " + remoteMessage.getFrom());

        if(remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload : " + remoteMessage.getData());

            if(/* 긴 작업시간을 필요로 하는지 체크*/true){
                // 10초 혹은 그 이상의 작업 시간이 필요하다면 WorkManager를 사용할 것.
                scheduleJob();
            }
            else{
                handleNow();
            }
        }

//        if(remoteMessage.getNotification() != null){
//            Log.d(TAG, "푸시 본문 : " + remoteMessage.getNotification().getBody());
//
//            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getData().get("receiveSeq"));
//        }

        if(remoteMessage.getData() != null){
            Log.d(TAG, "푸시 본문 : " + remoteMessage.getData().get("messageBody"));

            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("notificationBody"), remoteMessage.getData().get("receiveSeq"));
        }

//        sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    private void handleNow() {
        Log.d(TAG, "작은 TASK 완료");
    }

    private void scheduleJob() {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
    }

    @Override
    public void onNewToken(@NonNull @NotNull String token) {
        Log.d(TAG, "새 토큰 : " + token);

        // 굳이 여기서 처리해줄 필요가 없다.
        //sendRegistrationToServer(token);
    }



    private void sendRegistrationToServer(String token) {
    }

    private void sendNotification(String title, String messageBody, String receiveSeq){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra("title", title);
        intent.putExtra("messageBody",messageBody);
        intent.putExtra("receiveSeq", receiveSeq);

        // FLAG_ONE_SHOT으로 하지 않을 경우 다음 알림이 올 때 까지
        // MainActivity.onNewIntent 이벤트에 전달되는 indent값이 계속 지속된다.
        //
        // notification과 마찬가지로 ID를 각각 다르게 생성해줘야 여러 건의 푸시를 올바르게 식별할 수 있다.
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(), intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        //String channelId = getString(R.string.default_notification_channel_id);
        String channelId = "DstMessage";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.notify_icon_ds)
                        //.setContentTitle(getString(R.string.fcm_message))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelId, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // notifi id를 0으로 부여할 경우 단 건의 푸시는 문제가 없으나
        // 여러건의 푸시일 경우 가장 마지막의 푸시만 남게되는 문제가 있다.
        //notificationManager.notify(0, notificationBuilder.build());
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}
