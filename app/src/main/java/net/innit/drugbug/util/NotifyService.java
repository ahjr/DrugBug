package net.innit.drugbug.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;

import static net.innit.drugbug.util.Constants.FROM_REMINDER;
import static net.innit.drugbug.util.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.util.Constants.TYPE;
import static net.innit.drugbug.util.Constants.TYPE_FUTURE;
import static net.innit.drugbug.util.Constants.REMINDER_TEXT;
import static net.innit.drugbug.util.Constants.REMINDER_TITLE;

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
        title = intent.getStringExtra(REMINDER_TITLE);
        text = intent.getStringExtra(REMINDER_TEXT);
        doseId = intent.getLongExtra(INTENT_DOSE_ID, -1);

        createNotification();
        return START_STICKY;
    }

    // Build the notification
    private void createNotification() {
        Intent resultIntent = new Intent(this, DoseListActivity.class);
        resultIntent.putExtra(TYPE, TYPE_FUTURE);
        resultIntent.putExtra(INTENT_DOSE_ID, doseId);
        resultIntent.putExtra(FROM_REMINDER, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION, builder.build());
    }
}
