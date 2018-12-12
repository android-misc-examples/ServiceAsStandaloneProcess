package it.pgp.serviceasstandaloneprocess;

import android.util.Log;

import java.io.Serializable;

public class ExampleTask extends BaseBackgroundTask {
    public ExampleTask(Serializable params) {
        super(params);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // Update progress
        mBuilder.setProgress(100, values[0], false);
        notificationManager.notify(service.getForegroundServiceNotificationId(),
                mBuilder.build());

        super.onProgressUpdate(values);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        for (int i=0;i<=100;i++) {
            if (isCancelled()) break;
            publishProgress(i);
            Log.d(getClass().getName(),"Progress: "+i);
            try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
        }
        return null;
    }

}
