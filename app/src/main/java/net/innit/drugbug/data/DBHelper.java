package net.innit.drugbug.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {
    /**
     * Column name defs
     */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MED_ID = "_med_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_NAME = "name";
//    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_TAKEN = "taken";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_ARCHIVE_DATE = "archive_date";
    public static final String COLUMN_FREQ_ID = "_freq_id";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_NUM_REMINDERS = "num_reminders";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_NIGHT_REMINDER = "night_reminder";
    public static final String COLUMN_TIME_OF_DAY = "tod";

    /**
     * View name definitions
     */
    public static final String VIEW_DOSE_FULL = "doses_full";
    public static final String VIEW_MEDICATION_FULL = "medications_full";

    /**
     * Table name definitions
     */
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_DOSES = "doses";
    public static final String TABLE_FREQUENCIES = "frequencies";

    /**
     * Create table definitions
     */
    private static final String CREATE_TABLE_MEDICATIONS =
            "CREATE TABLE " + TABLE_MEDICATIONS + "(" +
                    COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_FREQ_ID + " INTEGER, " +
                    COLUMN_END_DATE + " INTEGER, " +
                    COLUMN_NUM_REMINDERS + " INTEGER, " +
                    COLUMN_NIGHT_REMINDER + " INTEGER, " +
                    COLUMN_IMAGE_PATH + " TEXT, " +
                    COLUMN_ACTIVE + " INTEGER DEFAULT 1, " +
                    COLUMN_ARCHIVED + " INTEGER DEFAULT 0, " +
                    COLUMN_ARCHIVE_DATE + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_FREQ_ID + ") " +
                    "REFERENCES " + TABLE_FREQUENCIES + "(" + COLUMN_FREQ_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_DOSES =
            "CREATE TABLE " + TABLE_DOSES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MED_ID + " INTEGER, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_REMINDER + " INTEGER, " +
                    COLUMN_TAKEN + " INTEGER, " +
                    COLUMN_DOSAGE + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_MED_ID + ") " +
                    "REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_MED_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_FREQUENCIES =
            "CREATE TABLE " + TABLE_FREQUENCIES + "(" +
                    COLUMN_FREQ_ID + " INTEGER AUTOINCREMENT, " +
                    COLUMN_LABEL + " TEXT NOT NULL, " +
                    COLUMN_TIME_OF_DAY + " TEXT, " +
                    COLUMN_INTERVAL + " INTEGER, " +
                    "PRIMARY KEY (" + COLUMN_TIME_OF_DAY + ", " + COLUMN_INTERVAL + ")" +
                    ")";

    /**
     * Create view definitions
     */
    private static final String CREATE_VIEW_MEDICATION_FULL =
            "CREATE VIEW " + VIEW_MEDICATION_FULL + " AS " +
                    "SELECT " + TABLE_MEDICATIONS + "." + COLUMN_MED_ID + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NAME + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_FREQ_ID + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_END_DATE + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NUM_REMINDERS + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NIGHT_REMINDER + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_LABEL + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_TIME_OF_DAY + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_INTERVAL + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_IMAGE_PATH + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ACTIVE + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ARCHIVED + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ARCHIVE_DATE + " " +
                    "FROM " + TABLE_MEDICATIONS + " " +
                    "JOIN " + TABLE_FREQUENCIES + " " +
                    "ON " + TABLE_MEDICATIONS + "." + COLUMN_FREQ_ID + "=" + TABLE_FREQUENCIES + "." + COLUMN_FREQ_ID;

    private static final String CREATE_VIEW_DOSE_FULL =
            "CREATE VIEW " + VIEW_DOSE_FULL + " AS " +
                    "SELECT " + TABLE_DOSES + "." + COLUMN_ID + ", " +
                    TABLE_DOSES + "." + COLUMN_DATE + ", " +
                    TABLE_DOSES + "." + COLUMN_REMINDER + ", " +
                    TABLE_DOSES + "." + COLUMN_TAKEN + ", " +
                    TABLE_DOSES + "." + COLUMN_DOSAGE + ", " +
                    TABLE_DOSES + "." + COLUMN_MED_ID + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NAME + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_FREQ_ID + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_END_DATE + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NUM_REMINDERS + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NIGHT_REMINDER + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_LABEL + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_TIME_OF_DAY + ", " +
                    TABLE_FREQUENCIES + "." + COLUMN_INTERVAL + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_IMAGE_PATH + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ACTIVE + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ARCHIVED + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_ARCHIVE_DATE + " " +
                    "FROM " + TABLE_DOSES + " " +
                    "JOIN " + TABLE_MEDICATIONS + " " +
                    "ON " + TABLE_DOSES + "." + COLUMN_MED_ID + "=" + TABLE_MEDICATIONS + "." + COLUMN_MED_ID + " " +
                    "JOIN " + TABLE_FREQUENCIES + " " +
                    "ON " + TABLE_MEDICATIONS + "." + COLUMN_FREQ_ID + "=" + TABLE_FREQUENCIES + "." + COLUMN_FREQ_ID;

    private static final String DB_NAME = "drugbug.db";
    private static final int DB_VERSION = 25;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDICATIONS);
        db.execSQL(CREATE_TABLE_DOSES);
        db.execSQL(CREATE_TABLE_FREQUENCIES);
        db.execSQL(CREATE_VIEW_DOSE_FULL);
        db.execSQL(CREATE_VIEW_MEDICATION_FULL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FREQUENCIES);
        db.execSQL("DROP VIEW IF EXISTS " + VIEW_DOSE_FULL);
        db.execSQL("DROP VIEW IF EXISTS " + VIEW_MEDICATION_FULL);
        onCreate(db);
    }
}
