package it.pgp.serviceasstandaloneprocess;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

public class ExampleService extends BaseBackgroundService {
    private static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 0x12AB34CD;
    private static final String BROADCAST_ACTION = "EXAMPLE_BROADCAST_ACTION";

    private String foreground_content_text;
    private String foreground_ticker;
    private String foreground_pause_action_label;
    private String foreground_stop_action_label;

    @Override
    public int getForegroundServiceNotificationId() {
        return FOREGROUND_SERVICE_NOTIFICATION_ID;
    }

    @Override
    protected void prepareLabels() {
        foreground_ticker="Example";
        foreground_content_text="Example...";
        foreground_pause_action_label="Pause example";
        foreground_stop_action_label="Stop example";
    }

    @Override
    protected NotificationCompat.Builder getForegroundNotificationBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(BROADCAST_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent pauseIntent = new Intent(this, this.getClass());
        pauseIntent.setAction(PAUSE_ACTION);
        PendingIntent ppauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        Intent stopIntent = new Intent(this, this.getClass());
        stopIntent.setAction(CANCEL_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Bitmap icon = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(
                        getResources(),
                        android.R.drawable.sym_def_app_icon),
                128, 128, false);

        return new NotificationCompat.Builder(this)
                .setContentTitle("ExampleService")
                .setTicker(foreground_ticker)
                .setContentText(foreground_content_text)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setLargeIcon(icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, foreground_pause_action_label,
                        ppauseIntent)
                .addAction(android.R.drawable.ic_media_pause, foreground_stop_action_label,
                        pstopIntent);
    }


//    @Override
//    protected NotificationCompat.Builder getForegroundNotificationBuilder() {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("Example")
//                .setContentText("Example progress...")
//                .setSmallIcon(android.R.drawable.sym_def_app_icon);
//        return mBuilder;
//    }

    @Override
    protected void onStartAction() {
        task = new ExampleTask(null);
        task.init(this);
        task.execute((Void[])null);
    }
}
