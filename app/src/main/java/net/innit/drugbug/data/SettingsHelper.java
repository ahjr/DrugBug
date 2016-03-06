package net.innit.drugbug.data;

import android.content.Context;
import android.util.Log;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.ExternalStorage;
import net.innit.drugbug.util.ImageStorage;

import java.util.List;

public class SettingsHelper {
    public static final String KEY_NUM_DOSES = "NumFutureDoses";
    public static final int DEFAULT_NUM_DOSES = 5;
    public static final String KEY_KEEP_TIME_TAKEN = "KeepTimeTaken";
    public static final String DEFAULT_KEEP_TIME_TAKEN = "1:0:0";
    public static final String KEY_KEEP_TIME_MISSED = "KeepTimeMissed";
    public static final String DEFAULT_KEEP_TIME_MISSED = "0:1:0";
    public static final String KEY_IMAGE_STORAGE = "StorageLoc";

    public final String DEFAULT_IMAGE_STORAGE;

    private DBDataSource db;

    public SettingsHelper(Context context) {
        db = new DBDataSource(context);
        if (ExternalStorage.getInstance(context, ImageStorage.IMAGE_DIR).isAvailable()) {
            DEFAULT_IMAGE_STORAGE = "EXTERNAL";
        } else {
            DEFAULT_IMAGE_STORAGE = "INTERNAL";
        }
    }

    public static int[] parseKeepTime(String keepTimeString) {
        int[] a = new int[3];
        String[] s = keepTimeString.split(":");
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(s[i]);
        }
        return a;
    }

    public static String convertString(String input) {
        String[] splitString = input.split(":");
        String output = "";
        if ((Integer.parseInt(splitString[0]) > 0)) {
            if (Integer.parseInt(splitString[0]) == 1) {
                output += splitString[0] + " year ";
            } else {
                output += splitString[0] + " years ";
            }
        }
        if ((Integer.parseInt(splitString[1]) > 0)) {
            if (Integer.parseInt(splitString[1]) == 1) {
                output += splitString[1] + " month ";
            } else {
                output += splitString[1] + " months ";
            }
        }
        if ((Integer.parseInt(splitString[2]) > 0)) {
            if (Integer.parseInt(splitString[2]) == 1) {
                output += splitString[2] + " day";
            } else {
                output += splitString[2] + " days";
            }
        }

        return output;
    }

    public void numDosesChanged(int maxNumDoses, int oldNumDoses) {
        db.open();
        List<MedicationItem> medications = db.getAllMedications();
        for (MedicationItem medication : medications) {
            int difference = maxNumDoses - oldNumDoses;
            if (difference > 0) {
                // number of doses has increased
                // add $DIFFERENCE doses based on lastDose
                // Get last dose
                Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: medId " + medication.getId());
                DoseItem lastFutureDose = db.getLastDose(medication);
                if (lastFutureDose != null) {
                    int doseCount = (int) db.getFutureDoseCount(medication);
                    while (maxNumDoses > doseCount) {
                        db.generateNextFuture(medication);
                        doseCount++;
                    }
                }

            } else if (difference < 0) {
                // number of doses has decreased
                // Get current number of future doses and remove doses until the number equals new max number
                int doseCount = (int) db.getFutureDoseCount(medication);
                while (doseCount > maxNumDoses) {
                    // Get last future dose for this medication
                    DoseItem lastFutureDose = db.getLastDose(medication);
                    // getLastDose returns null if there isn't one
                    if (lastFutureDose != null) {
                        db.removeDose(lastFutureDose.getId());
                    }
                    doseCount--;
                }
            } // if difference is 0, we'll do nothing
        }

        db.close();
    }

    public void keepTimeTakenChanged(String keepTimeString) {
        db.open();
        int numRemoved = db.removeOldDoses(DoseItem.TYPE_TAKEN, keepTimeString);
        Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: " + numRemoved + " taken doses removed");
        db.close();
    }

    public void keepTimeMissedChanged(String keepTimeString) {
        db.open();
        int numRemoved = db.removeOldDoses(DoseItem.TYPE_MISSED, keepTimeString);
        Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: " + numRemoved + " missed doses removed");
        db.close();
    }

    public void imageStorageChanged(ImageStorage imageStorage) {
        // Nothing here yet, but extracted it to keep everything together
        Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: Image storage location changed to " + imageStorage.getDisplayText());
    }

}
