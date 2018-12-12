package it.pgp.serviceasstandaloneprocess;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import java.io.Serializable;

public abstract class BaseBackgroundTask extends AsyncTask<Object,Integer,Object> {
	
	protected NotificationCompat.Builder mBuilder;
    // for notifying progress on foreground service progress bar
    protected NotificationManager notificationManager;
	protected BaseBackgroundService service;

    public Serializable params; // to be down-casted in subclasses

    public BaseBackgroundTask(Serializable params) {
        this.params = params;
    }

    // FIXME circular dependency (task <-> service), cannot set in constructor, since service constructor needs task instance as input param
    /* invocation order:
    t = new Task();
    s = new Service(t);
    t.init(s);
     */
	public void init(BaseBackgroundService service) {
        this.service = service;
        mBuilder = service.getForegroundNotificationBuilder();
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }
	
	public void pauseTask() {}
	
	@Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBuilder.setProgress(100,0,false);
        notificationManager.notify(service.getForegroundServiceNotificationId(), mBuilder.build());
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        service.stopForeground(true);
        service.stopSelf();
    }
}