package net.innit.drugbug.util;

public class Constants {
    /**
     * Private constructor to restrict instantiation
     */
    private Constants() {}

    public static final String LOGTAG = "DrugBug";

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
    public static final String TYPE_NONE = "none";

    /**
     * Sort String constants
     */
    public static final String SORT = "sort";
    public static final String SORT_DATE_DESC = "dateDsc";
    public static final String SORT_DATE_ASC = "dateAsc";
    public static final String SORT_NAME = "name";
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

}
