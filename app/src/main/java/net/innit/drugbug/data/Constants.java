package net.innit.drugbug.data;

public class Constants {
    /**
     * Private constructor to restrict instantiation
     */
    private Constants() {}

    public static final String LOG = "DrugBug";

    public static final int REQUEST_TAKE_PICTURE = 300;

    /**
     * Set to true to clear SharedPreferences and reset to defaults
     */
    public static final boolean CLEAR_SHARED_PREFS = false;

    /**
     * Action String constants
     */
    public static final String ACTION = "action";
    public static final String ACTION_ADD = "add";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_RESTORE = "restore";
    public static final String ACTION_REACTIVATE = "reactivate";

    /**
     * Type String constants
     */
    public static final String TYPE = "type";
    public static final String TYPE_TAKEN = "taken";
    public static final String TYPE_FUTURE = "future";
    public static final String TYPE_MISSED = "missed";
    public static final String TYPE_REMINDER = "reminder";
    public static final String TYPE_SINGLE = "single";
    public static final String TYPE_MEDICATION = "medication";
    public static final String TYPE_NONE = "none";

    /**
     * Sort String constants
     */
    public static final String SORT = "sort";
    public static final String SORT_DATE_DESC = "dateDsc";
    public static final String SORT_DATE_ASC = "dateAsc";
    public static final String SORT_NAME = "name";
    public static final String SORT_CREATION = "creation";
    public static final String SORT_NAME_DESC = "nameDsc";
    public static final String SORT_NAME_ASC = "nameAsc";

    /**
     * Dose list filter String constants
     */
    public static final String FILTER_DOSE = "filter";
    public static final String FILTER_NONE = "none";
    public static final String FILTER_TAKEN = "taken";
    public static final String FILTER_FUTURE = "future";

    /**
     * Medication list filter String constants
     */
    public static final String FILTER_MED = "filter_med";
    public static final String FILTER_ALL = "all";
    public static final String FILTER_ACTIVE = "active";
    public static final String FILTER_INACTIVE = "inactive";
    public static final String FILTER_ARCHIVED = "archived";

    /**
     * Reminder String constants
     */
    public static final String FROM_REMINDER = "fromReminder";
    public static final String REMINDER_TITLE = "title";
    public static final String REMINDER_TEXT = "text";

    public static final String INTENT_MED_ID = "med_id";
    public static final String INTENT_DOSE_ID = "dose_id";

    /**
     * Source constants
     */
    public static final String SOURCE = "source";
    public static final int SOURCE_MAIN = 3001001;
    public static final int SOURCE_ADD_DOSE = 3001002;
    public static final int SOURCE_EDIT_DOSE = 3001003;
    public static final int SOURCE_SETTINGS = 3001004;
    public static final int SOURCE_LIST_FUTURE = 3001005;
    public static final int SOURCE_LIST_TAKEN = 3001006;
    public static final int SOURCE_LIST_REMINDERS = 3001007;
    public static final int SOURCE_LIST_MEDICATIONS = 3001008;
    public static final int SOURCE_LIST_SINGLE_MED = 3001009;
    public static final int SOURCE_DETAIL_TAKEN = 3001010;
    public static final int SOURCE_DETAIL_FUTURE = 3001011;

    /**
     * Settings defaults constants
     */
    public static final String DEFAULT_NUM_DOSES = "5";
    public static final String DEFAULT_KEEP_TIME_TAKEN = "1:0:0";
    public static final String DEFAULT_KEEP_TIME_MISSED = "0:1:0";

    /**
     * Image size constants
     */
    public static final int IMAGE_HEIGHT_FULL = 0;
    public static final int IMAGE_WIDTH_FULL = 0;
    public static final int IMAGE_HEIGHT_LIST = 50;
    public static final int IMAGE_WIDTH_LIST = 50;
    public static final int IMAGE_HEIGHT_PREVIEW = 70;
    public static final int IMAGE_WIDTH_PREVIEW = 70;

    public static final String TAG_DETAIL = "Detail Fragment";
    public static final String TAG_HELP = "Help Fragment";
    public static final String TAG_ADD = "Add Fragment";

}
