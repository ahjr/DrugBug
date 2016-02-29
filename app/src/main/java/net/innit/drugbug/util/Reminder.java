package net.innit.drugbug.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.innit.drugbug.R;
import net.innit.drugbug.data.DataSource;
import net.innit.drugbug.model.DoseItem;

import java.util.Calendar;

/**
 * Object class used for creating a system reminder
 */
public class Reminder {
    public static final int REQUEST_HEADER_FUTURE_DOSE = 10;

    private final DataSource db;

    private final Context context;

    public Reminder(Context context) {
        this.context = context;
        db = new DataSource(context);
    }

    /**
     * @param header Type of reminder as int
     * @param id     id of object
     * @return header concatenated with id to provide a unique code for this reminder
     */
    private int getRequestCode(int header, long id) {
        return Integer.parseInt("" + header + id);
    }

    /**
     * @param header   Type of reminder as int
     * @param doseItem Dose to be reminded of
     */
    public void startReminder(int header, DoseItem doseItem) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(doseItem.getDate());

        // Set some defaults
        int requestCode = 0;
        String title = "Generic title";
        String text = "Generic text";

        // Create the notify intent
        Intent intent = new Intent(context, NotifyService.class);

        switch (header) {
            case REQUEST_HEADER_FUTURE_DOSE: {
                // Get the request code
                requestCode = getRequestCode(REQUEST_HEADER_FUTURE_DOSE, doseItem.getId());
                title = context.getString(R.string.reminder_title);
                db.open();
                text = doseItem.getMedication().getName() + "(" + doseItem.getDosage() + ")";
                db.close();
                break;
            }
        }

        // Put title and text into the intent
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("dose_id", doseItem.getId());

        // Add the intent to the alarm manager
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Test code to make sure alarms are working
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000, pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void killReminder(int header, DoseItem doseItem) {
        // Create the intent
        Intent intent = new Intent(context, NotifyService.class);
        int requestCode = 0;

        switch (header) {
            case REQUEST_HEADER_FUTURE_DOSE: {
                long id = doseItem.getId();
                requestCode = getRequestCode(header, id);
                break;
            }
        }

        // Remove the intent from the alarm manager
        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}

