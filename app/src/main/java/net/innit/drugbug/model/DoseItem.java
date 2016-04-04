package net.innit.drugbug.model;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;

import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.util.OnChoiceSelectedListener;
import net.innit.drugbug.util.OnListUpdatedListener;
import net.innit.drugbug.util.Reminder;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DoseItem implements Comparable<DoseItem> {
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

    public Date nextDate(Context context) {
        DatabaseDAO db = new DatabaseDAO(context);
        long secs = date.getTime() + db.getInterval(medication.getFrequency()) * 1000;
        return new Date(secs);
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
     */
//    public void confirmDelete(final Context context, final Intent intent) {
    public void confirmDelete(final Context context, final OnListUpdatedListener listener) {
        final DatabaseDAO db = new DatabaseDAO(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_delete_dose_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_dose_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_dose_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                db.removeDose(context, id, true);
                db.close();
                if (listener != null) {
                    listener.onListUpdated();
                }
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
     * @param fragment context for the alert dialog
     * @param intent  intent to start after confirmation
     */
    public void confirmTaken(final Fragment fragment, final Intent intent) {
        final DatabaseDAO db = new DatabaseDAO(fragment.getActivity());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getActivity());
        alertDialogBuilder.setTitle(R.string.taken_dialog_title);
        alertDialogBuilder.setMessage(R.string.dialog_confirm);
        alertDialogBuilder.setPositiveButton(R.string.taken_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                convertToTaken();
                db.open();
                if (db.updateDose(DoseItem.this)) {
                    // TODO: 4/3/16 add toast to say "X missed doses removed" or similar
                    DoseItem firstFutureDose = DoseItem.this.getMedication().getFirstFuture(fragment.getActivity());

                    while ((firstFutureDose != null) && (firstFutureDose.getDate().getTime() <= DoseItem.this.getDate().getTime())) {
                        // First future dose date is before taken dose date
                        db.removeDose(fragment.getActivity(), firstFutureDose.getId(), true);
                        firstFutureDose = DoseItem.this.getMedication().getFirstFuture(fragment.getActivity());
                    }

                    DoseItem.this.getMedication().createNextFuture(fragment.getActivity());

                    if (fragment instanceof DialogFragment) {
                        // Dismiss the calling dialog fragment
                        ((DialogFragment) fragment).dismiss();
                    }
                    fragment.getActivity().startActivity(intent);
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

    public void reminderAllOrOne(final Context context, final OnChoiceSelectedListener listener) {
        final DatabaseDAO db = new DatabaseDAO(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Toggle all reminders?");
        alertDialogBuilder.setMessage("Should all reminders for this medication be changed?");
        alertDialogBuilder.setPositiveButton("Yes, change all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Change all
                    DoseItem.this.toggleReminder();
                    DoseItem.this.setOrKillAlarm(context);
                    db.open();
                    db.updateDose(DoseItem.this);
                    List<DoseItem> doses = medication.getAllFuture(context);
                    for (DoseItem dose : doses) {
                        dose.setReminder(DoseItem.this.isReminderSet());
                        db.updateDose(dose);
                        dose.setOrKillAlarm(context);
                    }

                    db.close();

                if (listener != null) {
                    listener.onChoiceSelected("all");
                }
            }
        });
        alertDialogBuilder.setNegativeButton("No, just this dose", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                    // Change just this dose
                    DoseItem.this.toggleReminder();
                    db.open();
                    db.updateDose(DoseItem.this);
                    DoseItem.this.setOrKillAlarm(context);
                    db.close();

                if (listener != null) {
                    listener.onChoiceSelected("one");
                }
            }
        }).create().show();

    }

    public void setOrKillAlarm(Context context) {
        if (this.isReminderSet()) {
            // create a reminder
            new Reminder(context).startReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, this);
        } else {
            // remove reminder
            new Reminder(context).killReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, this);
        }
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
