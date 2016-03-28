package net.innit.drugbug.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import net.innit.drugbug.R;

import static net.innit.drugbug.data.Constants.DEFAULT_NUM_DOSES;

/**
 * Preference type with a single number spinner
 */
public class NumberPreference extends DialogPreference {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 30;
    private NumberPicker picker;

    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(R.string.preference_positive);
        setNegativeButtonText(R.string.preference_negative);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistString(String.valueOf(picker.getValue()));
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int value;
        if (restorePersistedValue) {
            // Restore existing state
            value = Integer.parseInt(this.getPersistedString(DEFAULT_NUM_DOSES));
        } else {
            // Set default state from the XML attribute
            value = (Integer) defaultValue;
            persistString(String.valueOf(value));
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
        View view = inflater.inflate(R.layout.dialog_number_picker, null);

        picker = (NumberPicker) view.findViewById(R.id.number_picker);

        // Initialize state
        picker.setMaxValue(MAX_VALUE);
        picker.setMinValue(MIN_VALUE);
        picker.setValue(Integer.parseInt(getPersistedString(DEFAULT_NUM_DOSES)));
        picker.setWrapSelectorWheel(false);

        return view;
    }

}
