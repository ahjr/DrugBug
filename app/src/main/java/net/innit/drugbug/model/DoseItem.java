package net.innit.drugbug.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DBDataSource;

import java.util.Comparator;
import java.util.Date;

public class DoseItem implements Comparable<DoseItem> {
    public static final String TYPE_TAKEN = "taken";
    public static final String TYPE_FUTURE = "future";
    public static final String TYPE_MISSED = "missed";
    public static final String TYPE_REMINDER = "reminder";
    public static final String TYPE_SINGLE = "single";
    public static final String TYPE_NONE = "none";

    private long id;
    private Date date;
    private boolean reminder;
    private boolean taken;
    private String dosage;
    private MedicationItem medication;

    public DoseItem() {
    }

    public DoseItem(MedicationItem medication, Date date, boolean reminder, boolean taken, String dosage) {
        this.medication = medication;
        this.date = date;
        this.reminder = reminder;
        this.taken = taken;
        this.dosage = dosage;
    }

    // Unused constructors - keeping it here in case it's needed in the future
//    public DoseItem(long id, MedicationItem medication, Date date, boolean reminder, boolean taken, String dosage) {
//        this.id = id;
//        this.medication = medication;
//        this.date = date;
//        this.reminder = reminder;
//        this.taken = taken;
//        this.dosage = dosage;
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MedicationItem getMedication() {
        return medication;
    }

    public void setMedication(MedicationItem medication) {
        this.medication = medication;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isReminderSet() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public void toggleReminder() {
        this.reminder = !this.reminder;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Convert this dose to taken
     */
    private void convertToTaken() {
        this.taken = true;
    }

    /**
     * Default sort - by date ascending
     *
     * @param another DoseItem to compare this DateItem to
     * @return a negative integer if this instance is less than another; a positive integer if this instance is greater than another; 0 if this instance has the same order as another.
     */
    @Override
    public int compareTo(@NonNull DoseItem another) {
        return date.compareTo(another.getDate());
    }

    /**
     * Deletes dose after confirmation from user
     *
     * @param context context for the alert dialog
     * @param intent  intent to start after confirmation
     */
    public void confirmDelete(final Context context, final Intent intent) {
        final DBDataSource db = new DBDataSource(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_delete_dose_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_dose_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_dose_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                switch (db.removeDose(id)) {
                    case RESULT_OK:
                        db.generateNextFuture(medication);
                        Toast.makeText(context, R.string.dose_list_toast_removed_ok, Toast.LENGTH_SHORT).show();
                        context.startActivity(intent);
                        break;
                    case ERROR_UNKNOWN_ERROR:
                        Toast.makeText(context, R.string.dose_list_toast_removed_error, Toast.LENGTH_SHORT).show();
                        break;
                }
                db.close();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_dose_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    /**
     * Converts dose to taken after confirmation from user
     *
     * @param context context for the alert dialog
     * @param intent  intent to start after confirmation
     */
    public void confirmTaken(final Context context, final Intent intent) {
        final DBDataSource db = new DBDataSource(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.taken_dialog_title);
        alertDialogBuilder.setMessage(R.string.dialog_confirm);
        alertDialogBuilder.setPositiveButton(R.string.taken_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                convertToTaken();
                db.open();
                if (db.updateDose(DoseItem.this)) {
                    DoseItem newFutureItem = db.generateNextFuture(DoseItem.this.getMedication());
                    Log.d(MainActivity.LOGTAG, "TakenButton:onClick: newFutureItem generated with id " + newFutureItem.getId());
                    DoseItem firstFutureDose = db.getFirstFutureDose(DoseItem.this.getMedication());

                    while ((firstFutureDose != null) && (firstFutureDose.getDate().getTime() <= DoseItem.this.getDate().getTime())) {
                        // First future dose date is before taken dose date
                        Log.d(MainActivity.LOGTAG, "onClick: firstFutureDose id " + firstFutureDose.getId());
                        db.removeDose(firstFutureDose.getId());
                        newFutureItem = db.generateNextFuture(DoseItem.this.getMedication());
                        Log.d(MainActivity.LOGTAG, "TakenButton:onClick: newFutureItem generated with id " + newFutureItem.getId());
                        firstFutureDose = db.getFirstFutureDose(DoseItem.this.getMedication());

                    }

                    context.startActivity(intent);
                }
                db.close();

                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.taken_dialog_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    /**
     * A comparator so we can sort dosages by date, descending
     */
    public static class ReverseDateComparator implements Comparator<DoseItem> {

        @Override
        public int compare(DoseItem lhs, DoseItem rhs) {
            return rhs.getDate().compareTo(lhs.getDate());
        }
    }

    /**
     * A comparator so we can sort dosages by name, ascending
     */
    public static class NameComparator implements Comparator<DoseItem> {

        @Override
        public int compare(DoseItem lhs, DoseItem rhs) {
            return lhs.getMedication().getName().compareTo(rhs.getMedication().getName());
        }
    }


}
