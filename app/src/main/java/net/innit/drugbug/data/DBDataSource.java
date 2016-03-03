package net.innit.drugbug.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBDataSource {
    /**
     * Array of columns in medication table
     */
    private static final String[] medicationsAllColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_NAME,
            DBHelper.COLUMN_FREQUENCY,
            DBHelper.COLUMN_IMAGE_PATH
    };
    /**
     * Array of columns in doses table
     */
    private static final String[] dosesAllColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_MEDICATION_ID,
            DBHelper.COLUMN_DATE,
            DBHelper.COLUMN_REMINDER,
            DBHelper.COLUMN_TAKEN,
            DBHelper.COLUMN_DOSAGE
    };
    /**
     * Map of frequencies
     * Initialized in a static block
     */
    private static final Map<String, Long> frequencies = new HashMap<>();

    static {
        long second = 1;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;

        frequencies.put("Every hour", hour);
        frequencies.put("Every 2 hours", 2 * hour);
        frequencies.put("Every 4 hours", 4 * hour);
        frequencies.put("2x per day", 12 * hour);
        frequencies.put("3x per day", 8 * hour);
        frequencies.put("Daily", day);
    }

    private final SQLiteOpenHelper dbhelper;
    private SQLiteDatabase database;

    /**
     * @param context Context for this database
     */
    public DBDataSource(Context context) {
        Log.d(MainActivity.LOGTAG, "DBDataSource: created");
        dbhelper = new DBHelper(context);
    }

    /**
     * Open database for reading and writing
     */
    public void open() {
        database = dbhelper.getWritableDatabase();
    }

    /**
     * Close database
     */
    public void close() {
        dbhelper.close();
    }

    /**
     * Returns a List object with all medications in the database as MedicationItem objects
     *
     * @return list of MedicationItem objects
     */
    public List<MedicationItem> getAllMedications() {
        Log.d(MainActivity.LOGTAG, "getAllMedications: start");
        List<MedicationItem> returnList = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, null, null, null, null, null);
        Log.d(MainActivity.LOGTAG, "getAllMedications: query returned " + cursor.getCount() + " medications");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));
                Log.d(MainActivity.LOGTAG, "getAllMedications: medication id " + medication.getId());
                returnList.add(medication);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Medication methods
     */

    /**
     * Retrieves medication from database and returns values in a MedicationItem object
     *
     * @param medId Id of the medication to retrieve
     * @return MedicationItem object containing database values for id
     */
    // Get one
    public MedicationItem getMedication(long medId) {
        Log.d(MainActivity.LOGTAG, "getMedication: start");
        String selection = DBHelper.COLUMN_ID + "=" + medId;
        Cursor cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, null, null, null);
        Log.d(MainActivity.LOGTAG, "getMedication: query returned " + cursor.getCount() + " medications");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            MedicationItem medication = new MedicationItem();
            medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
            medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));
            Log.d(MainActivity.LOGTAG, "getMedication: medication id " + medication.getId());
            return medication;
        }
        cursor.close();
        return null;
    }

    /**
     * Retrieves medication from database and returns values in a MedicationItem object
     *
     * @param doseId Id of dose
     * @return MedicationItem object containing database values for id
     */
    // Get one
    private MedicationItem getMedicationForDose(long doseId) {
        Log.d(MainActivity.LOGTAG, "getMedicationForDose: start");
        DoseItem dose = getDose(doseId);
        String selection = DBHelper.COLUMN_ID + "=" + dose.getMedication().getId();
        Cursor cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, null, null, null);
        Log.d(MainActivity.LOGTAG, "getMedicationForDose: query returned " + cursor.getCount() + " medications");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            MedicationItem medication = new MedicationItem();
            medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
            medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));
            Log.d(MainActivity.LOGTAG, "getMedicationForDose: medication id " + medication.getId());
            return medication;
        }
        cursor.close();
        return null;
    }

    /**
     * Add medication to the database using a MedicationItem object
     *
     * @param medication MedicationItem to add to database
     * @return MedicationItem with newly created id added
     */
    // Create
    public MedicationItem createMedication(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "createMedication: start");
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, medication.getName());
        values.put(DBHelper.COLUMN_FREQUENCY, medication.getFrequency());
        values.put(DBHelper.COLUMN_IMAGE_PATH, medication.getImagePath());
        long insertId = database.insert(DBHelper.TABLE_MEDICATIONS, null, values);
        medication.setId(insertId);
        Log.d(MainActivity.LOGTAG, "createMedication: id is " + insertId);
        return medication;
    }

    /**
     * Update medication in the database
     *
     * @param medication MedicationItem object to update
     * @return true if update successful, false if unsuccessful
     */
    // Update
    public boolean updateMedication(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "updateMedication: start");
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, medication.getName());
        values.put(DBHelper.COLUMN_FREQUENCY, medication.getFrequency());
        values.put(DBHelper.COLUMN_IMAGE_PATH, medication.getImagePath());
        String selection = DBHelper.COLUMN_ID + "=" + medication.getId();
        int rows = database.update(DBHelper.TABLE_MEDICATIONS, values, selection, null);
        Log.d(MainActivity.LOGTAG, "updateMedication: updated " + rows + " rows");
        return rows > 0;
    }

    /**
     * Remove medication from database
     *
     * @param medication Medication to remove
     * @return RESULT_OK if successful
     */
    // Delete
    private Result removeMedication(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "removeMedication: start");
        String where = DBHelper.COLUMN_ID + "=" + medication.getId();
        int result = database.delete(DBHelper.TABLE_MEDICATIONS, where, null);
        if (result == 1) {
            Log.d(MainActivity.LOGTAG, "removeMedication: medication " + medication.getId() + " removed");
            return Result.RESULT_OK;
        } else {
            Log.d(MainActivity.LOGTAG, "removeMedication: medication " + medication.getId() + " not removed");
            return Result.ERROR_UNKNOWN_ERROR;
        }
    }

    /**
     * Dose methods
     */

    public List<DoseItem> getAllDosesForMed(long medId) {
        Log.d(MainActivity.LOGTAG, "getAllDosesForMed: start");
        List<DoseItem> returnList = new ArrayList<>();
        String selection = DBHelper.COLUMN_MEDICATION_ID + "=" + medId;
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, orderBy);
        Log.d(MainActivity.LOGTAG, "getAllDosesForMed: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getAllDosesForMed: dose id " + dose.getId());
                returnList.add(dose);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Returns all future doses
     *
     * @param context Context for this method
     * @return List of future doses as DoseItem objects
     */
    public List<DoseItem> getAllFuture(Context context) {
        Log.d(MainActivity.LOGTAG, "getAllFuture: start");
        // Get taken keep time from SharedPreferences and convert it into an array: [y, m, d]
        String keepTimeString = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SettingsHelper.KEY_KEEP_TIME_MISSED, SettingsHelper.DEFAULT_KEEP_TIME_MISSED);
        int numDeleted = removeOldDoses(DoseItem.TYPE_MISSED, keepTimeString);
        Log.d(MainActivity.LOGTAG, "getAllFuture: " + numDeleted + " missed doses removed");

        List<DoseItem> returnList = new ArrayList<>();
        String selection = DBHelper.COLUMN_TAKEN + "=" + 0;
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, orderBy);
        Log.d(MainActivity.LOGTAG, "getAllFuture: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getAllFuture: dose id " + dose.getId());
                returnList.add(dose);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Return a list of all future doses for a single medication id
     *
     * @param medication medication to retrieve doses for
     * @return List of DoseItem objects
     */
    public List<DoseItem> getAllFutureForMed(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "getAllFutureForMed: start");
        List<DoseItem> returnList = new ArrayList<>();
        String selection = DBHelper.COLUMN_TAKEN + "=" + 0 + " AND " + DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId();
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, orderBy);
        Log.d(MainActivity.LOGTAG, "getAllFutureForMed: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getAllFutureForMed: dose id " + dose.getId());
                returnList.add(dose);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Returns a list of all future doses with reminders
     *
     * @return List of DoseItem objects
     */
    public List<DoseItem> getAllFutureWithReminder() {
        Log.d(MainActivity.LOGTAG, "getAllFutureWithReminder: start");
        List<DoseItem> returnList = new ArrayList<>();
        long date = new Date().getTime();
        String selection = DBHelper.COLUMN_TAKEN + " = " + 0 +
                " AND " + DBHelper.COLUMN_REMINDER + " = " + 1 +
                " AND " + DBHelper.COLUMN_DATE + " >= " + date;
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, orderBy);
        Log.d(MainActivity.LOGTAG, "getAllFutureWithReminder: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getAllFutureWithReminder: dose id " + dose.getId());
                returnList.add(dose);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Returns a list of all taken doses
     *
     * @param context Context for this method
     * @return List of DoseItem objects
     */
    public List<DoseItem> getAllTaken(Context context) {
        Log.d(MainActivity.LOGTAG, "getAllTaken: start");
        // Get taken keep time from SharedPreferences and convert it into an array: [y, m, d]
        String keepTimeString = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SettingsHelper.KEY_KEEP_TIME_TAKEN, SettingsHelper.DEFAULT_KEEP_TIME_TAKEN);
        int numDeleted = removeOldDoses(DoseItem.TYPE_TAKEN, keepTimeString);
        Log.d(MainActivity.LOGTAG, "getAllTaken: " + numDeleted + " old doses removed");

        List<DoseItem> returnList = new ArrayList<>();
        String selection = DBHelper.COLUMN_TAKEN + "=" + 1;
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, orderBy);
        Log.d(MainActivity.LOGTAG, "getAllTaken: query returned " + cursor.getCount() + " taken doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getAllTaken: dose id " + dose.getId());
                returnList.add(dose);
            }
        }
        cursor.close();
        return returnList;
    }

    /**
     * Remove doses older than keep time
     *
     * @param type           String "taken" for taken doses.  Anything else results in future doses
     * @param keepTimeString String in the form Y:M:D
     * @return number of doses removed
     */
    public int removeOldDoses(String type, String keepTimeString) {
        Log.d(MainActivity.LOGTAG, "removeOldDoses: start");
        int[] keepTimeArray = SettingsHelper.parseKeepTime(keepTimeString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.YEAR, keepTimeArray[0] * -1);
        cal.add(Calendar.MONTH, keepTimeArray[1] * -1);
        cal.add(Calendar.DAY_OF_MONTH, keepTimeArray[2] * -1);


        String selection = DBHelper.COLUMN_TAKEN + " = " + ((type.equals(DoseItem.TYPE_TAKEN)) ? 1 : 0) + " AND " +
                DBHelper.COLUMN_DATE + " < " + cal.getTime().getTime();
        int numDeleted = database.delete(DBHelper.TABLE_DOSES, selection, null);
        Log.d(MainActivity.LOGTAG, "removeOldDoses: " + numDeleted + " old doses deleted");
        return numDeleted;

    }

    /**
     * Get dose by id
     *
     * @param id id of the dose to retrieve
     * @return DoseItem object
     */
    // Get one
    public DoseItem getDose(long id) {
        Log.d(MainActivity.LOGTAG, "getDose: start");
        String selection = DBHelper.COLUMN_ID + "=" + id;
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, null);
        Log.d(MainActivity.LOGTAG, "getDose: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();

            MedicationItem medication = new MedicationItem();
            medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
            medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
            medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

            DoseItem dose = new DoseItem();
            dose.setMedication(medication);
            dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
            dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
            dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
            dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
            Log.d(MainActivity.LOGTAG, "getDose: dose id " + dose.getId());
            return dose;
        }
        cursor.close();
        return null;
    }

    /**
     * Retrieve untaken dose furthest in the future
     *
     * @param medication medication to find the future dose for
     * @return DoseItem with latest future dose
     */
    public DoseItem getLastDose(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "getLastDose: medication is " + medication.getName());
        DoseItem dose = new DoseItem();
        String selection = DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId();
        // Changed logic: now will return the dose with the highest date, untaken or not
        //+ " AND " + DBHelper.COLUMN_TAKEN + "=" + 0;
        String orderBy = DBHelper.COLUMN_DATE + " DESC";
        String limit = "1";
        Cursor cursor = database.query(DBHelper.TABLE_DOSES, dosesAllColumns, selection, null, null, null, orderBy, limit);
        Log.d(MainActivity.LOGTAG, "getLastDose: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            dose.setMedication(medication);
            dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
            dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
            dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
            dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
            Log.d(MainActivity.LOGTAG, "getLastDose: dose id is " + dose.getId());
            return dose;
        }
        cursor.close();
        return null;
    }

    /**
     * Retrieve untaken dose with lowest date
     *
     * @param medication medication to find the future dose for
     * @return DoseItem object with lowest future dose
     */
    public DoseItem getFirstFutureDose(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "getFirstFutureDose: medication id is " + medication.getId());
        DoseItem dose = new DoseItem();
        String selection = DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId() + " AND " + DBHelper.COLUMN_TAKEN + "=" + 0;
        String orderBy = DBHelper.COLUMN_DATE + " ASC";
        String limit = "1";
        Cursor cursor = database.query(DBHelper.TABLE_DOSES, dosesAllColumns, selection, null, null, null, orderBy, limit);
        Log.d(MainActivity.LOGTAG, "getFirstFutureDose: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            dose.setMedication(medication);
            dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
            dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
            dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
            dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
            Log.d(MainActivity.LOGTAG, "getFirstFutureDose: dose id is " + dose.getId());
            return dose;
        }
        cursor.close();
        return null;
    }

    /**
     * Return count of untaken doses
     *
     * @param medication medication to count untaken doses for
     * @return count of untaken doses
     */
    public long getFutureDoseCount(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "getFutureDoseCount: start");
        String selection = DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId() + " AND " + DBHelper.COLUMN_TAKEN + "=" + 0;
        long numEntries = DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_DOSES, selection);
        Log.d(MainActivity.LOGTAG, "getFutureDoseCount: " + numEntries + " future doses");
        return numEntries;
    }

    /**
     * Generate next future dose
     *
     * @param medication medication to generate a future dose for
     * @return DoseItem object for new dose
     */
    public DoseItem generateNextFuture(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "generateNextFuture: start");

        // After successful update, add another future dose so we keep the same number in our future list
        // Get last future dose for medication
        DoseItem newFutureItem = getLastDose(medication);
        newFutureItem.setTaken(false);
        Log.d(MainActivity.LOGTAG, "generateNextFuture: newFutureItem is " + newFutureItem);
        Log.d(MainActivity.LOGTAG, "generateNextFuture: medication is " + medication);
        // Get frequency
        long interval = getInterval(medication.getFrequency());
        Calendar calendar = Calendar.getInstance();
        Date lastFutureDate = newFutureItem.getDate();
        Date nowDate = new Date();
        // Compare date of last future date to now
        if (lastFutureDate.getTime() > nowDate.getTime()) {
            // last future date is later than now
            // Add frequency to future date
            calendar.setTime(lastFutureDate);
        } else {
            // Add frequency to now
            calendar.setTime(nowDate);
        }
        calendar.add(Calendar.SECOND, (int) interval);
        newFutureItem.setDate(calendar.getTime());
        return createDose(newFutureItem);

    }

    public List<DoseItem> getDosesForDate(Date date) {
        Log.d(MainActivity.LOGTAG, "getDosesForDate: start");
        List<DoseItem> doses = new ArrayList<>();
        // Get day from date
        SimpleDateFormat daySDF = new SimpleDateFormat("yyyyMMdd");
        String day = daySDF.format(date);
        String midnight = day + " 00:00:00";
        String endOfDay = day + " 23:59:59";

        // Calculate epoch seconds for midnight and 11:59pm
        SimpleDateFormat datetimeSDF = new SimpleDateFormat("yyyyMMdd kk:mm:ss");
        long midnightDate;
        long endOfDayDate;
        try {
            midnightDate = datetimeSDF.parse(midnight).getTime();
            endOfDayDate = datetimeSDF.parse(endOfDay).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        // Set selection
        String selection = DBHelper.COLUMN_DATE + ">" + midnightDate + " AND " +
                            DBHelper.COLUMN_DATE + "<" + endOfDayDate;

        // Build the dose list to return
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_WITH_MED, null, selection, null, null, null, null);
        Log.d(MainActivity.LOGTAG, "getDosesForDate: query returned " + cursor.getCount() + " doses");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                MedicationItem medication = new MedicationItem();
                medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MEDICATION_ID)));
                medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
                medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));

                DoseItem dose = new DoseItem();
                dose.setMedication(medication);
                dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
                dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
                dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
                dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
                Log.d(MainActivity.LOGTAG, "getDosesForDate: dose id " + dose.getId());
                doses.add(dose);
            }
        }
        cursor.close();



        return doses;
    }

    /**
     * Create a new untaken dose
     *
     * @param dose DoseItem object with values to store in database
     * @return DoseItem object with newly created id
     */
    // Create
    public DoseItem createDose(DoseItem dose) {
        Log.d(MainActivity.LOGTAG, "createDose: start");
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MEDICATION_ID, dose.getMedication().getId());
        values.put(DBHelper.COLUMN_DATE, dose.getDate().getTime());
        values.put(DBHelper.COLUMN_REMINDER, dose.isReminderSet());
        values.put(DBHelper.COLUMN_TAKEN, dose.isTaken());
        values.put(DBHelper.COLUMN_DOSAGE, dose.getDosage());
        long insertId = database.insert(DBHelper.TABLE_DOSES, null, values);
        Log.d(MainActivity.LOGTAG, "createDose: dose id " + insertId);
        dose.setId(insertId);
        return dose;
    }

    /**
     * Update a dose
     *
     * @param dose DoseItem object to update
     * @return true if update was successful
     */
    // Update
    public boolean updateDose(DoseItem dose) {
        Log.d(MainActivity.LOGTAG, "updateDose: start");
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MEDICATION_ID, dose.getMedication().getId());
        values.put(DBHelper.COLUMN_DATE, dose.getDate().getTime());
        values.put(DBHelper.COLUMN_REMINDER, dose.isReminderSet());
        values.put(DBHelper.COLUMN_TAKEN, dose.isTaken());
        values.put(DBHelper.COLUMN_DOSAGE, dose.getDosage());
        String selection = DBHelper.COLUMN_ID + "=" + dose.getId();
        int rows = database.update(DBHelper.TABLE_DOSES, values, selection, null);
        Log.d(MainActivity.LOGTAG, "updateDose: " + rows + " doses updated");
        return rows > 0;
    }

    /**
     * Remove medication if it has no doses attached to it
     *
     * @param medication medication to remove
     * @return true if medication was removed
     */
    private boolean removeMedicationIfNoDoses(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "removeMedicationIfNoDoses: start");
        // Remove medication if no doses are associated with it
        String selection = DBHelper.COLUMN_ID + "=" + medication.getId();
        Log.d(MainActivity.LOGTAG, "removeMedicationIfNoDoses: doses associated with this medication: " + DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_DOSES, selection));
        if (DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_DOSES, selection) < 1) {
            Log.d(MainActivity.LOGTAG, "removeMedicationIfNoDoses: medication deleted");
            Result medResult = removeMedication(medication);
            if (medResult == Result.RESULT_OK) {
                return true;
            }
        }
        // Return false if there are still doses or there was an error deleting the medication
        return false;
    }

    /**
     * Remove a dose
     *
     * @param id id of dose to remove
     * @return RESULT_OK if dose was removed successfully
     */
    // Delete one dose
    public Result removeDose(long id) {
        Log.d(MainActivity.LOGTAG, "removeDose: start");
        // FUTURE TODO (re: medication list) Keep medications but need to be able to archive/remove them
        String where = DBHelper.COLUMN_ID + "=" + id;
        // Get medication
        MedicationItem medication = getMedicationForDose(id);

        boolean deleteOK = (database.delete(DBHelper.TABLE_DOSES, where, null) == 1);
        if (deleteOK) {
            Log.d(MainActivity.LOGTAG, "removeDose: dose " + id + " removed");
            if (medication != null) {
                long medId = medication.getId();
                boolean medRemoved = removeMedicationIfNoDoses(medication);
                if (medRemoved)
                    Log.d(MainActivity.LOGTAG, "removeDose: medication " + medId + " removed");
                return Result.RESULT_OK;
            } else {
                return Result.ERROR_UNKNOWN_ERROR;
            }
        } else {
            return Result.ERROR_UNKNOWN_ERROR;
        }
    }

    /**
     * Remove all untaken doses for a medication
     *
     * @param medication medication to remove all future doses for
     * @return number of deleted doses
     */
    public int removeAllFutureDosesForMed(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "removeAllFutureDosesForMed: start");
        // FUTURE TODO (re: medication list) Keep medications but need to be able to archive/remove them
        String where = DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId() + " AND " + DBHelper.COLUMN_TAKEN + "=" + 0;
        int numDeleted = database.delete(DBHelper.TABLE_DOSES, where, null);
        Log.d(MainActivity.LOGTAG, "removeAllFutureDosesForMed: " + numDeleted + " doses deleted");
        boolean medRemoved = removeMedicationIfNoDoses(medication);
        Log.d(MainActivity.LOGTAG, "removeAllFutureDosesForMed: medication removed " + medRemoved);
        return numDeleted;
    }

    /**
     * Remove all doses for a medication
     *
     * @param medication medication to remove all doses for
     * @return number of deleted doses
     */
    public int removeAllDosesForMed(MedicationItem medication) {
        Log.d(MainActivity.LOGTAG, "removeAllDosesForMed: start");
        // FUTURE TODO (re: medication list) Keep medications but need to be able to archive/remove them
        String where = DBHelper.COLUMN_MEDICATION_ID + "=" + medication.getId();
        int numDeleted = database.delete(DBHelper.TABLE_DOSES, where, null);
        Log.d(MainActivity.LOGTAG, "removeAllDosesForMed: " + numDeleted + " doses deleted");
        boolean medRemoved = removeMedicationIfNoDoses(medication);
        Log.d(MainActivity.LOGTAG, "removeAllDosesForMed: medication removed " + medRemoved);
        return numDeleted;
    }

    /**
     * Return a list of frequency labels
     *
     * @return List of String objects
     */
    public List<String> getFreqLabels() {
        List<String> labels = new ArrayList<>(frequencies.keySet());
        Collections.sort(labels, new FrequencyComparator());
        return labels;
    }

    /**
     * Frequency methods
     */

    /**
     * Convert frequency label string to an interval in seconds
     *
     * @param label label string to convert
     * @return interval between doses in seconds
     */
    public long getInterval(String label) {
        return frequencies.get(label);
    }

    /**
     * Result enum
     * - To give greater granularity of response than true or false
     */
    public enum Result {
        RESULT_OK,
        ERROR_UNKNOWN_ERROR
    }

    /**
     * Comparator to order frequencies alphabetically by label
     */
    private static class FrequencyComparator implements Comparator<String> {
        @Override
        public int compare(String lhs, String rhs) {
            return frequencies.get(lhs).compareTo(frequencies.get(rhs));
        }
    }

}
