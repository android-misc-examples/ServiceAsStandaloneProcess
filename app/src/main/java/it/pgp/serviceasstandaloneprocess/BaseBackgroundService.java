package it.pgp.serviceasstandaloneprocess;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.Serializable;

/**
onStartCommand's intent must contain at least:
 FOREGROUND_SERVICE_NOTIFICATION_ID (assigned from corresponding static field of subclasses)
 BROADCAST_ACTION (assigned from subclasses as well)
 sub-class dependent params
 */

public abstract class BaseBackgroundService extends Service {

    public static final String START_ACTION = "Start";
    public static final String PAUSE_ACTION = "Pause"; // pause, on next activity open, show results found so far
    public static final String CANCEL_ACTION = "Cancel"; // cancel, on next activity open, show results found so far
	String currentAction;

	NotificationManager notificationManager;
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;
	
	public BaseBackgroundTask task;
    public Serializable params;

//    protected BaseBackgroundService(int FOREGROUND_SERVICE_NOTIFICATION_ID, String BROADCAST_ACTION,
//                                    BaseBackgroundTask task) {
//        this.BROADCAST_ACTION = BROADCAST_ACTION;
//        this.FOREGROUND_SERVICE_NOTIFICATION_ID = FOREGROUND_SERVICE_NOTIFICATION_ID;
//        this.task = task;
//    }

    public abstract int getForegroundServiceNotificationId();
	
	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }
	
	protected void abortService() {
        task.cancel(false);
        stopForeground(true);
        stopSelf();
    }
	
	private void abortServiceWithConfirmation() {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Cancel "+this.getClass().getName()+"?");
        bld.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // no action
            }
        });
        bld.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abortService();
            }
        });
        AlertDialog alertDialog = bld.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
	
	protected abstract void prepareLabels();
	protected abstract NotificationCompat.Builder getForegroundNotificationBuilder();
	
	@Override
    public void onDestroy() {
        wakeLock.release();
        super.onDestroy();
    }
	
	@Override
    public void onCreate()
    {
        super.onCreate();
        mgr  = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
    }
	
	public void startAndShowNotificationBar() {
        wakeLock.acquire();

        switch (currentAction) {
            case START_ACTION:
                onStartAction();
                break;
            // Forbidden zone
            case CANCEL_ACTION:
            case PAUSE_ACTION:
                Toast.makeText(getApplicationContext(),
                        "Service not running, pause/cancel command should not arrive here",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                // DEBUG Forbidden zone
                Toast.makeText(getApplicationContext(),
                        "Unknown action in onStartCommand",
                        Toast.LENGTH_SHORT).show();
                break;
        }

        /************************** build notification **************************/

        Notification notification = getForegroundNotificationBuilder().build();
        startForeground(getForegroundServiceNotificationId(),notification);
    }

    protected abstract void onStartAction();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(currentAction == null) {
            currentAction = intent.getAction();

            if (!START_ACTION.equals(currentAction)) {
                throw new RuntimeException("Service not yet started, expected start action");
            }

            params = intent.getSerializableExtra("params");
//            if (params == null) {
//                throw new RuntimeException("Null params not allowed in start action");
//            }

            prepareLabels();
            startAndShowNotificationBar();
        }
        else {
            // trying to abort?
            if (intent.getAction().equals(CANCEL_ACTION)) {
                abortServiceWithConfirmation();
            }
            else if (intent.getAction().equals(PAUSE_ACTION)) {
                task.pauseTask();
                Toast.makeText(getApplicationContext(),"Service paused",Toast.LENGTH_LONG).show();
            }
            // trying to start another concurrent task?
            else {
                Toast.makeText(getApplicationContext(),
                        "Service already running!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return START_STICKY;
    }
	
}