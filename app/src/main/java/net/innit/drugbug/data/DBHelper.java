package net.innit.drugbug.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.innit.drugbug.MainActivity;

class DBHelper extends SQLiteOpenHelper {
    /**
     * Column name defs
     */
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEDICATION_ID = "medication_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_TAKEN = "taken";

    /**
     * View name definitions
     */
    public static final String VIEW_DOSE_WITH_MED = "doses_with_med";

    /**
     * Table name definitions
     */
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_DOSES = "doses";

    /**
     * Create table definitions
     */
    private static final String CREATE_TABLE_MEDICATIONS =
            "CREATE TABLE " + TABLE_MEDICATIONS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_FREQUENCY + " TEXT, " +
                    COLUMN_IMAGE_PATH + " TEXT " +
                    ")";

    private static final String CREATE_TABLE_DOSES =
            "CREATE TABLE " + TABLE_DOSES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MEDICATION_ID + " INTEGER, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_REMINDER + " INTEGER, " +
                    COLUMN_TAKEN + " INTEGER, " +
                    COLUMN_DOSAGE + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_MEDICATION_ID + ") " +
                    "REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_ID + ")" +
                    ")";

    /**
     * Create view definitions
     */
    private static final String CREATE_VIEW_DOSE_WITH_MED =
            "CREATE VIEW " + VIEW_DOSE_WITH_MED + " AS " +
                    "SELECT " + TABLE_DOSES + "." + COLUMN_ID + ", " +
                    TABLE_DOSES + "." + COLUMN_DATE + ", " +
                    TABLE_DOSES + "." + COLUMN_REMINDER + ", " +
                    TABLE_DOSES + "." + COLUMN_TAKEN + ", " +
                    TABLE_DOSES + "." + COLUMN_DOSAGE + ", " +
                    TABLE_DOSES + "." + COLUMN_MEDICATION_ID + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_NAME + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_FREQUENCY + ", " +
                    TABLE_MEDICATIONS + "." + COLUMN_IMAGE_PATH + " " +
                    "FROM " + TABLE_DOSES + " JOIN " + TABLE_MEDICATIONS + " " +
                    "ON " + TABLE_DOSES + "." + COLUMN_MEDICATION_ID + "=" + TABLE_MEDICATIONS + "." + COLUMN_ID;


    private static final String DB_NAME = "drugbug.db";
    private static final int DB_VERSION = 17;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDICATIONS);
        db.execSQL(CREATE_TABLE_DOSES);
        db.execSQL(CREATE_VIEW_DOSE_WITH_MED);
        Log.i(MainActivity.LOGTAG, "Tables & Views have been created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOSES);
        db.execSQL("DROP VIEW IF EXISTS " + VIEW_DOSE_WITH_MED);
        onCreate(db);
        Log.i(MainActivity.LOGTAG, "Database has been upgraded from v" + oldVersion + " to v" + newVersion);
    }
}
