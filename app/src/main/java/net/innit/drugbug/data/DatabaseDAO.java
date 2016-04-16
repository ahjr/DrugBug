package net.innit.drugbug.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

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

import static net.innit.drugbug.data.Constants.SORT_ARCHIVED_ASC;
import static net.innit.drugbug.data.Constants.SORT_ARCHIVED_DESC;
import static net.innit.drugbug.data.Constants.SORT_LAST_TAKEN_ASC;
import static net.innit.drugbug.data.Constants.SORT_LAST_TAKEN_DESC;
import static net.innit.drugbug.data.Constants.SORT_NEXT_FUTURE_ASC;
import static net.innit.drugbug.data.Constants.SORT_NEXT_FUTURE_DESC;
import static net.innit.drugbug.data.Constants.TYPE_MISSED;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

public class DatabaseDAO {
    /**
     * Array of columns in medication table
     */
    private static final String[] medicationsAllColumns = {
            DBHelper.COLUMN_MED_ID,
            DBHelper.COLUMN_NAME,
            DBHelper.COLUMN_FREQ_ID,
            DBHelper.COLUMN_END_DATE,
            DBHelper.COLUMN_NUM_REMINDERS,
            DBHelper.COLUMN_NIGHT_REMINDER,
            DBHelper.COLUMN_IMAGE_PATH,
            DBHelper.COLUMN_ACTIVE,
            DBHelper.COLUMN_ARCHIVED,
            DBHelper.COLUMN_ARCHIVE_DATE
    };
    /**
     * Array of columns in doses table
     */
    private static final String[] dosesAllColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_MED_ID,
            DBHelper.COLUMN_DATE,
            DBHelper.COLUMN_REMINDER,
            DBHelper.COLUMN_TAKEN,
            DBHelper.COLUMN_DOSAGE
    };
    private static final String[] frequenciesAllColumns = {
            DBHelper.COLUMN_FREQ_ID,
            DBHelper.COLUMN_LABEL,
            DBHelper.COLUMN_TIME_OF_DAY,
            DBHelper.COLUMN_INTERVAL
    };

    private static final String[] medicationsFullAllColumns = {
            DBHelper.COLUMN_MED_ID,
            DBHelper.COLUMN_NAME,
            DBHelper.COLUMN_FREQ_ID,
            DBHelper.COLUMN_LABEL,
            DBHelper.COLUMN_TIME_OF_DAY,
            DBHelper.COLUMN_INTERVAL,
            DBHelper.COLUMN_END_DATE,
            DBHelper.COLUMN_NUM_REMINDERS,
            DBHelper.COLUMN_NIGHT_REMINDER,
            DBHelper.COLUMN_IMAGE_PATH,
            DBHelper.COLUMN_ACTIVE,
            DBHelper.COLUMN_ARCHIVED,
            DBHelper.COLUMN_ARCHIVE_DATE,
    };

    private static final String[] dosesFullAllColumns = {
            DBHelper.COLUMN_MED_ID,
            DBHelper.COLUMN_NAME,
            DBHelper.COLUMN_FREQ_ID,
            DBHelper.COLUMN_LABEL,
            DBHelper.COLUMN_TIME_OF_DAY,
            DBHelper.COLUMN_INTERVAL,
            DBHelper.COLUMN_END_DATE,
            DBHelper.COLUMN_NUM_REMINDERS,
            DBHelper.COLUMN_NIGHT_REMINDER,
            DBHelper.COLUMN_IMAGE_PATH,
            DBHelper.COLUMN_ACTIVE,
            DBHelper.COLUMN_ARCHIVED,
            DBHelper.COLUMN_ARCHIVE_DATE,
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_DATE,
            DBHelper.COLUMN_REMINDER,
            DBHelper.COLUMN_TAKEN,
            DBHelper.COLUMN_DOSAGE
    };

    public enum QueuePosition {
        FIRST,
        LAST
    }

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
        frequencies.put("3x per day", day / 3);
        frequencies.put("2x per day", day / 2);
        frequencies.put("Daily", day);
    }

    private final SQLiteOpenHelper dbhelper;
    private SQLiteDatabase database;

    /**
     * @param context Context for this database
     */
    public DatabaseDAO(Context context) {
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
     * Medication methods
     */

    /**
     * Get a MedicationItem from the DB, using the given Cursor
     *
     * @param cursor database cursor
     * @return medication item cursor is pointing to
     */
    private MedicationItem getMedFromDB(Cursor cursor) {
        MedicationItem medication = new MedicationItem();
        medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MED_ID)));
        medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
        medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
        medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));
        medication.setActive(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTIVE)) == 1);
        medication.setArchived(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ARCHIVED)) == 1);
        long archiveDate = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ARCHIVE_DATE));
        medication.setArchiveDate((archiveDate>0) ? new Date(archiveDate): null);
        if (cursor.isLast()) cursor.close();
        return medication;
    }

    private MedicationItem getMedFromSelection(String selection) {
        Cursor cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            return getMedFromDB(cursor);
        } else {
            return null;
        }
    }

    /**
     * Retrieve multiple medications from the DB based on a passed in selection String
     *
     * @param selection SQL selection to retrieve from DB
     * @return ArrayList of medications
     */
    private List<MedicationItem> getMedications(String selection, String orderBy) {
        List<MedicationItem> returnList = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, null, null, orderBy);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                returnList.add(getMedFromDB(cursor));
            }
        }
//        cursor.close();
        return returnList;
    }

    public List<MedicationItem> getAllMedicationsByDose(String sortOrder) {
        String orderBy;
        String selection;
        Cursor cursor = null;
        if (sortOrder != null) {
            switch (sortOrder) {
                case SORT_LAST_TAKEN_ASC:
                    StringBuilder columns = getColumnsString(medicationsAllColumns);
                    cursor = database.rawQuery(
                            "SELECT " + DBHelper.TABLE_MEDICATIONS + "." + columns +
                            " FROM " + DBHelper.TABLE_MEDICATIONS + " LEFT JOIN " + DBHelper.TABLE_DOSES +
                            " ON " + DBHelper.TABLE_MEDICATIONS + "." + DBHelper.COLUMN_MED_ID + "=" + DBHelper.TABLE_DOSES + "." + DBHelper.COLUMN_MED_ID +
                            " WHERE " + DBHelper.COLUMN_ACTIVE + "=0" + " AND " + DBHelper.COLUMN_ARCHIVED + "=0" +
                            " GROUP BY " + DBHelper.TABLE_MEDICATIONS + "." + DBHelper.COLUMN_MED_ID +
                            " ORDER BY " + DBHelper.COLUMN_DATE + " ASC",
                            null
                    );
                    break;
                case SORT_LAST_TAKEN_DESC:
                    columns = getColumnsString(medicationsAllColumns);
                    cursor = database.rawQuery(
                            "SELECT " + DBHelper.TABLE_MEDICATIONS + "." + columns +
                            " FROM " + DBHelper.TABLE_MEDICATIONS + " LEFT JOIN " + DBHelper.TABLE_DOSES +
                            " ON " + DBHelper.TABLE_MEDICATIONS + "." + DBHelper.COLUMN_MED_ID + "=" + DBHelper.TABLE_DOSES + "." + DBHelper.COLUMN_MED_ID +
                            " WHERE " + DBHelper.COLUMN_ACTIVE + "=0" + " AND " + DBHelper.COLUMN_ARCHIVED + "=0" +
                            " GROUP BY " + DBHelper.TABLE_MEDICATIONS + "." + DBHelper.COLUMN_MED_ID +
                            " ORDER BY " + DBHelper.COLUMN_DATE + " DESC",
                            null
                    );
                    break;
                case SORT_NEXT_FUTURE_ASC:
                    selection = DBHelper.COLUMN_ACTIVE + "=1";
                    orderBy = DBHelper.COLUMN_DATE + " ASC";
                    cursor = database.query(DBHelper.VIEW_DOSE_FULL, medicationsAllColumns, selection, null, DBHelper.COLUMN_MED_ID, null, orderBy);
                    break;
                case SORT_NEXT_FUTURE_DESC:
                    selection = DBHelper.COLUMN_ACTIVE + "=1";
                    orderBy = DBHelper.COLUMN_DATE + " DESC";
                    cursor = database.query(DBHelper.VIEW_DOSE_FULL, medicationsAllColumns, selection, null, DBHelper.COLUMN_MED_ID, null, orderBy);
                    break;
                case SORT_ARCHIVED_ASC:
                    selection = DBHelper.COLUMN_ARCHIVED + "=1";
                    orderBy = DBHelper.COLUMN_ARCHIVE_DATE + " ASC";
                    cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, DBHelper.COLUMN_MED_ID, null, orderBy);
                    break;
                case SORT_ARCHIVED_DESC:
                    selection = DBHelper.COLUMN_ARCHIVED + "=1";
                    orderBy = DBHelper.COLUMN_ARCHIVE_DATE + " DESC";
                    cursor = database.query(DBHelper.TABLE_MEDICATIONS, medicationsAllColumns, selection, null, DBHelper.COLUMN_MED_ID, null, orderBy);
                    break;
            }
        }
        List<MedicationItem> returnList = new ArrayList<>();
        if (cursor == null) {
            cursor = database.query(DBHelper.VIEW_DOSE_FULL, medicationsAllColumns, null, null, DBHelper.COLUMN_MED_ID, null, null);
        }
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                returnList.add(getMedFromDB(cursor));
            }
        }
        return returnList;
    }

    @NonNull
    private StringBuilder getColumnsString(String[] columnArray) {
        StringBuilder columns = new StringBuilder();
        for (String n : columnArray) {
            if (columns.length() > 0) columns.append(", ");
            columns.append(n);
        }
        return columns;
    }

    /**
     * Returns a List object with all medications in the database as MedicationItem objects
     * Basically a wrapper method for the private getMedications with selection == null
     *
     * @return list of MedicationItem objects
     */
    public List<MedicationItem> getAllMedications(String sortOrder) {
//        if (sortOrder != null) {
//            switch (sortOrder) {
//                case SORT_NEXT_FUTURE_ASC:
//                    return getMedications(null, DBHelper.COLUMN_MED_ID + " ASC");
//                case SORT_NEXT_FUTURE_DESC:
//                    return getMedications(null, DBHelper.COLUMN_MED_ID + " DESC");
//            }
//        }
        return getMedications(null, null);
    }

    public List<MedicationItem> getAllMedications() {
        return getAllMedications(null);
    }

    public List<MedicationItem> getAllMedicationsInactive(String sortOrder) {
        String selection = DBHelper.COLUMN_ACTIVE + "=0" + " AND " + DBHelper.COLUMN_ARCHIVED + "=0";
//        if (sortOrder != null) {
//            switch (sortOrder) {
//                case SORT_NEXT_FUTURE_ASC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " ASC");
//                case SORT_NEXT_FUTURE_DESC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " DESC");
//            }
//        }
        return getMedications(selection, null);
    }

    public List<MedicationItem> getAllMedicationsInactive() {
        return getAllMedicationsInactive(null);
    }

    public List<MedicationItem> getAllMedicationsActive(String sortOrder) {
        String selection = DBHelper.COLUMN_ACTIVE + "=1";
//        if (sortOrder != null) {
//            switch (sortOrder) {
//                case SORT_NEXT_FUTURE_ASC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " ASC");
//                case SORT_NEXT_FUTURE_DESC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " DESC");
//
//            }
//        }
        return getMedications(selection, null);
    }

    public List<MedicationItem> getAllMedicationsActive() {
        return getAllMedicationsActive(null);
    }

    public List<MedicationItem> getAllMedicationsArchived(String sortOrder) {
        String selection = DBHelper.COLUMN_ARCHIVED + "=1";
//        if (sortOrder != null) {
//            switch (sortOrder) {
//                case SORT_NEXT_FUTURE_ASC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " ASC");
//                case SORT_NEXT_FUTURE_DESC:
//                    return getMedications(selection, DBHelper.COLUMN_MED_ID + " DESC");
//            }
//        }
        return getMedications(selection, null);
    }

    public List<MedicationItem> getAllMedicationsArchived() {
        return getAllMedicationsArchived(null);
    }

    /**
     * Retrieves medication from database and returns values in a MedicationItem object
     *
     * @param medId Id of the medication to retrieve
     * @return MedicationItem object containing database values for id
     */
    // Get one
    public MedicationItem getMedication(long medId) {
        String selection = DBHelper.COLUMN_MED_ID + "=" + medId;
        return getMedFromSelection(selection);
    }

    /**
     * Retrieves medication from database and returns values in a MedicationItem object
     *
     * @param doseId Id of dose
     * @return MedicationItem object containing database values for id
     */
    // Get one
    private MedicationItem getMedicationForDose(long doseId) {
        DoseItem dose = getDose(doseId);
        String selection = DBHelper.COLUMN_MED_ID + "=" + dose.getMedication().getId();
        return getMedFromSelection(selection);
    }

    private ContentValues createMedCV(MedicationItem medication) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, medication.getName());
        values.put(DBHelper.COLUMN_FREQ_ID, medication.getFrequency().getId());
        values.put(DBHelper.COLUMN_IMAGE_PATH, medication.getImagePath());
        values.put(DBHelper.COLUMN_ACTIVE, medication.isActive());
        values.put(DBHelper.COLUMN_ARCHIVED, medication.isArchived());
        values.put(DBHelper.COLUMN_ARCHIVE_DATE, (medication.getArchiveDate() != null) ? medication.getArchiveDate().getTime() : 0);
        return values;
    }

    /**
     * Add medication to the database using a MedicationItem object
     *
     * @param medication MedicationItem to add to database
     * @return MedicationItem with newly created id added
     */
    // Create
    public MedicationItem createMedication(MedicationItem medication) {
        ContentValues values = createMedCV(medication);
        long insertId = database.insert(DBHelper.TABLE_MEDICATIONS, null, values);
        medication.setId(insertId);
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
        ContentValues values = createMedCV(medication);
        String selection = DBHelper.COLUMN_MED_ID + "=" + medication.getId();
        int rows = database.update(DBHelper.TABLE_MEDICATIONS, values, selection, null);
        return rows > 0;
    }

    /**
     * Remove medication from database
     *
     * @param medication Medication to remove
     * @return RESULT_OK if successful
     */
    // Delete
    public Result removeMedication(Context context, MedicationItem medication) {
        String where = DBHelper.COLUMN_MED_ID + "=" + medication.getId();
        int result = database.delete(DBHelper.TABLE_MEDICATIONS, where, null);
        if (result == 1) {
            boolean imageDeleted = medication.deleteImageFile(context);
            return Result.RESULT_OK;
        } else {
            return Result.ERROR_UNKNOWN_ERROR;
        }
    }

    /**
     * Dose methods
     */

    /**
     * Given a cursor object, retrieve a dose from the database
     * @param cursor Cursor pointing to line in db to retrieve
     * @param medication (Optional) medication item for dose. Use null to get medication from db
     * @return dose item
     */
    private DoseItem getDoseFromDB(Cursor cursor, MedicationItem medication) {
        if (medication == null) {
            medication = new MedicationItem();
            medication.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_MED_ID)));
            medication.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            medication.setFrequency(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_FREQUENCY)));
            medication.setImagePath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_PATH)));
            medication.setActive(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTIVE)) == 1);
            medication.setArchived(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ACTIVE)) == 1);
            long archiveDate = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ARCHIVE_DATE));
            medication.setArchiveDate((archiveDate > 0) ? new Date(archiveDate) : null);
        }

        DoseItem dose = new DoseItem();
        dose.setMedication(medication);
        dose.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
        dose.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE))));
        dose.setReminder(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_REMINDER)) == 1);
        dose.setTaken(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_TAKEN)) == 1);
        dose.setDosage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DOSAGE)));
        if (cursor.isLast()) {
            cursor.close();
        }
        return dose;
    }

    /**
     * Get a list of doses from the db, based on selection and orderBy parameters
     * @param selection SQL selection string
     * @param orderBy SQL orderBy clause
     * @return ArrayList of doses
     */
    private List<DoseItem> getDoses(String selection, String orderBy) {
        List<DoseItem> returnList = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.VIEW_DOSE_FULL, null, selection, null, null, null, orderBy);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                returnList.add(getDoseFromDB(cursor, null));
            }
        }
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
        int[] keepTimeArray = SettingsHelper.parseKeepTime(keepTimeString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.YEAR, keepTimeArray[0] * -1);
        cal.add(Calendar.MONTH, keepTimeArray[1] * -1);
        cal.add(Calendar.DAY_OF_MONTH, keepTimeArray[2] * -1);

        String selection = DBHelper.COLUMN_TAKEN + " = " + ((type.equals(TYPE_TAKEN)) ? 1 : 0) + " AND " +
                DBHelper.COLUMN_DATE + " < " + cal.getTime().getTime();
        return database.delete(DBHelper.TABLE_DOSES, selection, null);

    }

    private void cleanDB() {
        Settings settings = Settings.getInstance();
        // Get missed keep time from SharedPreferences and convert it into an array: [y, m, d]
        int numDeleted = removeOldDoses(TYPE_MISSED, settings.getString(Settings.Key.KEEP_TIME_MISSED));

        // Get taken keep time from SharedPreferences and convert it into an array: [y, m, d]
        numDeleted = removeOldDoses(TYPE_TAKEN, settings.getString(Settings.Key.KEEP_TIME_TAKEN));
    }

    public List<DoseItem> getAllDoses(MedicationItem medication, Date date, boolean takenOnly, boolean futureOnly, boolean reminderOnly) {
        cleanDB();

        List<String> selection = new ArrayList<>();

        if (medication != null) {
            selection.add(DBHelper.COLUMN_MED_ID + "=" + medication.getId());
        }

        if (date != null) {
            // Get day from date
            @SuppressLint("SimpleDateFormat") SimpleDateFormat daySDF = new SimpleDateFormat("yyyyMMdd");
            String day = daySDF.format(date);
            String midnight = day + " 00:00:00";
            String endOfDay = day + " 23:59:59";

            // Calculate epoch seconds for midnight and 11:59pm
            @SuppressLint("SimpleDateFormat") SimpleDateFormat datetimeSDF = new SimpleDateFormat("yyyyMMdd kk:mm:ss");
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
            selection.add(DBHelper.COLUMN_DATE + ">" + midnightDate + " AND " +
                    DBHelper.COLUMN_DATE + "<" + endOfDayDate);
        }

        if (reminderOnly) {
            selection.add(DBHelper.COLUMN_REMINDER + " = " + 1);
            selection.add(DBHelper.COLUMN_DATE + " >= " + new Date().getTime());
        }

        if (!(takenOnly == futureOnly)) {
            // Both false doesn't make sense, so we'll assume if they're the same, we want all
            if (takenOnly) {
                selection.add(DBHelper.COLUMN_TAKEN + "=" + 1);
            } else {
                selection.add(DBHelper.COLUMN_TAKEN + "=" + 0);
            }
        }

        String sel = null;
        if (selection.size() > 0) {
            sel = selection.get(0);
            for (String str : selection) {
                sel += " AND " + str;
            }
        }

        String orderBy = DBHelper.COLUMN_DATE + " ASC";

        return getDoses(sel, orderBy);
    }

    /**
     * Returns all future doses
     *
     * @param context Context for this method
     * @return List of future doses as DoseItem objects
     */
    public List<DoseItem> getAllFuture(Context context) {
        return getAllDoses(null, null, false, true, false);
    }

    /**
     * Returns a list of all future doses with reminders
     *
     * @return List of DoseItem objects
     */
    public List<DoseItem> getAllFutureWithReminder(Context context) {
        return getAllDoses(null, null, false, true, true);
    }

    /**
     * Returns a list of all taken doses
     *
     * @param context Context for this method
     * @return List of DoseItem objects
     */
    public List<DoseItem> getAllTaken(Context context) {
        return getAllDoses(null, null, true, false, false);
    }

    public List<DoseItem> getAllDosesForDate(Context context, Date date) {
        return getAllDoses(null, date, false, false, false);
    }

    private DoseItem getSingleDose(MedicationItem medication, String selection, String orderBy) {
        String table;
        String[] columns;
        if (medication == null) {
            table = DBHelper.VIEW_DOSE_FULL;
            columns = dosesFullAllColumns;
        } else {
            table = DBHelper.TABLE_DOSES;
            columns = dosesAllColumns;
        }
        Cursor cursor = database.query(table, columns, selection, null, null, null, orderBy, "1");
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            return getDoseFromDB(cursor, medication);
        }
        return null;
    }

    /**
     * Get dose by id
     *
     * @param id id of the dose to retrieve
     * @return DoseItem object
     */
    // Get one
    public DoseItem getDose(long id) {
        String selection = DBHelper.COLUMN_ID + "=" + id;
        return getSingleDose(null, selection, null);
    }

    public DoseItem getDose(MedicationItem medication, boolean taken, QueuePosition position) {
        String selection = DBHelper.COLUMN_MED_ID + "=" + medication.getId() +
                " AND " + DBHelper.COLUMN_TAKEN + "=" + (taken?1:0);
        String orderBy = DBHelper.COLUMN_DATE + (position == QueuePosition.FIRST?" ASC":" DESC");
        return getSingleDose(medication, selection, orderBy);
    }

    /**
     * Return count of taken or untaken doses
     *
     * @param medication medication to count untaken doses for
     * @param taken true for count of taken doses, false for count of untaken doses
     * @return number of doses
     */
    public long getDoseCount(MedicationItem medication, boolean taken, boolean future) {
        String selection = DBHelper.COLUMN_MED_ID + "=" + medication.getId();
        if (!(taken && future)) {
            if (taken || future) {
                selection += " AND " + DBHelper.COLUMN_TAKEN + "=" + (taken ? 1 : 0);
            } else {
                throw new IllegalArgumentException("One of taken or future must be true.");
            }
        }
        return DatabaseUtils.queryNumEntries(database, DBHelper.TABLE_DOSES, selection);
    }

    private ContentValues createDoseCV(DoseItem dose) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MED_ID, dose.getMedication().getId());
        values.put(DBHelper.COLUMN_DATE, dose.getDate().getTime());
        values.put(DBHelper.COLUMN_REMINDER, dose.isReminderSet());
        values.put(DBHelper.COLUMN_TAKEN, dose.isTaken());
        values.put(DBHelper.COLUMN_DOSAGE, dose.getDosage());
        return values;
    }

    /**
     * Create a new untaken dose
     *
     * @param dose DoseItem object with values to store in database
     * @return DoseItem object with newly created id
     */
    // Create
    public DoseItem createDose(DoseItem dose) {
        ContentValues values = createDoseCV(dose);
        long insertId = database.insert(DBHelper.TABLE_DOSES, null, values);
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
        ContentValues values = createDoseCV(dose);
        String selection = DBHelper.COLUMN_ID + "=" + dose.getId();
        int rows = database.update(DBHelper.TABLE_DOSES, values, selection, null);
        return rows > 0;
    }

    /**
     * Remove a dose and generate the next future dose
     *
     * @param context Context for this method
     * @param id id of dose to remove
     * @return RESULT_OK if dose was removed successfully, MEDICATION_NULL if medication for the dose does not exist
     */
    // Delete one dose
    public Result removeDose(Context context, long id) {
        // Get medication
        MedicationItem medication = getMedicationForDose(id);
        if (medication != null) {
            medication.createNextFuture(context);
            return removeDose(id);
        } else {
            return Result.MEDICATION_NULL;
        }
    }

    public Result removeDose(long id) {
        String where = DBHelper.COLUMN_ID + "=" + id;

        boolean deleteOK = (database.delete(DBHelper.TABLE_DOSES, where, null) == 1);
        if (deleteOK) {
            return Result.RESULT_OK;
        } else {
            return Result.ERROR_UNKNOWN_ERROR;
        }

    }

    public long removeDoses(MedicationItem medication, boolean taken, boolean future) throws IllegalArgumentException {
        String where = DBHelper.COLUMN_MED_ID + "=" + medication.getId();
        if (!taken || !future) {
            if (!taken && !future) {
                throw new IllegalArgumentException("One of taken or future must be true.");
            } else {
                where += " AND " + DBHelper.COLUMN_TAKEN + "=" + (taken?1:0);
            }
        }
        return database.delete(DBHelper.TABLE_DOSES, where, null);
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
        MEDICATION_NULL,
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
