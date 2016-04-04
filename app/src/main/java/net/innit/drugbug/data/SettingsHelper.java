package net.innit.drugbug.data;

import android.content.Context;

import net.innit.drugbug.R;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.ImageStorage;

import java.util.List;

import static net.innit.drugbug.data.Constants.TYPE_MISSED;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

public class SettingsHelper {
    private final DatabaseDAO db;

    public SettingsHelper(Context context) {
        db = new DatabaseDAO(context);
    }

    public static int[] parseKeepTime(String keepTimeString) {
        int[] a = new int[3];
        String[] s = keepTimeString.split(":");
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(s[i]);
        }
        return a;
    }

    public static String convertString(Context context, String input) {
        String[] splitString = input.split(":");
        String output = "";
        String[] singleStrings = new String[] {
                context.getString(R.string.settings_convert_string_year),
                context.getString(R.string.settings_convert_string_month),
                context.getString(R.string.settings_convert_string_day)
        };
        String[] pluralStrings = new String[] {
                context.getString(R.string.settings_convert_string_years),
                context.getString(R.string.settings_convert_string_months),
                context.getString(R.string.settings_convert_string_days)
        };

        for (int i = 0; i < 3; i++) {
            if ((Integer.parseInt(splitString[i]) > 0)) {
                output += splitString[i] + (Integer.parseInt(splitString[i]) == 1 ? singleStrings[i] : pluralStrings[i]);
            }
        }

        return output;
    }

    public void numDosesChanged(Context context, int maxNumDoses, int oldNumDoses) {
        db.open();
        List<MedicationItem> medications = db.getAllMedicationsActive();
        db.close();
        for (MedicationItem medication : medications) {
            int difference = maxNumDoses - oldNumDoses;
            if (difference > 0) {
                increaseDoses(context, maxNumDoses, medication);

            } else if (difference < 0) {
                decreaseDoses(context, maxNumDoses, medication);
            } // if difference is 0, we'll do nothing
        }
    }

    private void decreaseDoses(Context context, int maxNumDoses, MedicationItem medication) {
        int doseCount = (int) medication.getNumFutures(context);
        while (doseCount > maxNumDoses) {
            DoseItem lastFutureDose = medication.getLastFuture(context);
            if (lastFutureDose != null) {
                db.open();
                db.removeDose(context, lastFutureDose.getId(), false);
                db.close();
            }
            doseCount--;
        }
    }

    private void increaseDoses(Context context, int maxNumDoses, MedicationItem medication) {
        DoseItem lastFutureDose = medication.getLastFuture(context);
        if (lastFutureDose != null) {
            int doseCount = (int) medication.getNumFutures(context);
            while (maxNumDoses > doseCount) {
                medication.createNextFuture(context);
                doseCount++;
            }
        }
    }

    public void keepTimeTakenChanged(String keepTimeString) {
        db.open();
        db.removeOldDoses(TYPE_TAKEN, keepTimeString);
        db.close();
    }

    public void keepTimeMissedChanged(String keepTimeString) {
        db.open();
        db.removeOldDoses(TYPE_MISSED, keepTimeString);
        db.close();
    }

    public void imageStorageChanged(ImageStorage imageStorage) {
        // Nothing here yet, but extracted it to keep everything together
    }

}
