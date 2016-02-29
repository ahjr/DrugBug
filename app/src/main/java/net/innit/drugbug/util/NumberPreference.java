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
 * Preference type with a single number spinner
 */
public class NumberPreference extends DialogPreference {
    private static final int DEFAULT_VALUE = 10;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 30;
    private NumberPicker picker;
    private int value;

    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("OK");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(picker.getValue());
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            value = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            value = (Integer) defaultValue;
            persistInt(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_number_picker, null);

        picker = (NumberPicker) view.findViewById(R.id.number_picker);

        // Initialize state
        picker.setMaxValue(MAX_VALUE);
        picker.setMinValue(MIN_VALUE);
        picker.setValue(value);
        picker.setWrapSelectorWheel(false);

        return view;
    }

}
