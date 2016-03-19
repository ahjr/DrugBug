package net.innit.drugbug.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import net.innit.drugbug.R;

/**
 * Preference type with spinners for year, month and day
 */
public class KeepTimePreference extends DialogPreference {
    private static final int YEAR_DEFAULT_VALUE = 1;
    private static final int YEAR_MIN_VALUE = 0;
    private static final int YEAR_MAX_VALUE = 5;
    private static final int MONTH_DEFAULT_VALUE = 0;
    private static final int MONTH_MIN_VALUE = 0;
    private static final int MONTH_MAX_VALUE = 11;
    private static final int DAY_DEFAULT_VALUE = 0;
    private static final int DAY_MIN_VALUE = 0;
    private static final int DAY_MAX_VALUE = 30;
    private static final String DEFAULT_VALUE = "" + YEAR_DEFAULT_VALUE + ":" + MONTH_DEFAULT_VALUE + ":" + DAY_DEFAULT_VALUE;
    private NumberPicker yearPicker;
    private NumberPicker monthPicker;
    private NumberPicker dayPicker;
    private String value;

    public KeepTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("OK");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            value = "" + yearPicker.getValue() + ":" + monthPicker.getValue() + ":" + dayPicker.getValue();
            persistString(value);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            value = this.getPersistedString(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            value = (String) defaultValue;
            persistString(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.preference_keep_time, null);

        yearPicker = (NumberPicker) view.findViewById(R.id.npYear);
        monthPicker = (NumberPicker) view.findViewById(R.id.npMonth);
        dayPicker = (NumberPicker) view.findViewById(R.id.npDay);

        int[] defaultKeep = splitString(value);

        // Initialize state
        yearPicker.setMaxValue(YEAR_MAX_VALUE);
        yearPicker.setMinValue(YEAR_MIN_VALUE);
        yearPicker.setValue(defaultKeep[0]);
        yearPicker.setWrapSelectorWheel(false);
        monthPicker.setMaxValue(MONTH_MAX_VALUE);
        monthPicker.setMinValue(MONTH_MIN_VALUE);
        monthPicker.setValue(defaultKeep[1]);
        monthPicker.setWrapSelectorWheel(false);
        dayPicker.setMaxValue(DAY_MAX_VALUE);
        dayPicker.setMinValue(DAY_MIN_VALUE);
        dayPicker.setValue(defaultKeep[2]);
        dayPicker.setWrapSelectorWheel(false);

        return view;
    }


    /**
     * Split a keep time String from Y:M:D into an int array
     *
     * @param string keep time in the form Y:M:D
     * @return int array containing year, month, and day values
     */
    private int[] splitString(String string) {
        String[] orig = string.split(":");
        int[] a = new int[3];
        for (int i = 0; i < 3; i++) {
            a[i] = Integer.parseInt(orig[i]);
        }

        return a;
    }
}
