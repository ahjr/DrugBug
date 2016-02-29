package net.innit.drugbug.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.MainActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.model.DoseItem;

/**
 * Dose notification service
 */
public class NotifyService extends Service {
    private static final int NOTIFICATION = 100;

    private String title;
    private String text;
    private long doseId;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        title = intent.getStringExtra("title");
        text = intent.getStringExtra("text");
        doseId = intent.getLongExtra("dose_id", -1);
        Log.d(MainActivity.LOGTAG, "onStartCommand: title is " + title);

        createNotification();
        return START_STICKY;
    }

    // Build the notification
    private void createNotification() {
        Intent resultIntent = new Intent(this, DoseListActivity.class);
        resultIntent.putExtra("type", DoseItem.TYPE_FUTURE);
        resultIntent.putExtra("dose_id", doseId);
        resultIntent.putExtra("fromReminder", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(MainActivity.LOGTAG, "createNotification: notification should be built");
        manager.notify(NOTIFICATION, builder.build());
    }
}
