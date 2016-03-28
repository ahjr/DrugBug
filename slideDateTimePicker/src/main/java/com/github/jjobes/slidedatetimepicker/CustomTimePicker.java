package com.github.jjobes.slidedatetimepicker;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

/**
 * A subclass of {@link android.widget.TimePicker} that uses
 * reflection to allow for customization of the default blue
 * dividers.
 *
 * @author jjobes
 */
public class CustomTimePicker extends TimePicker {
    private static final String TAG = "CustomTimePicker";

    public CustomTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        Class<?> idClass = null;
        Class<?> numberPickerClass = null;
        Field selectionDividerField = null;
        Field hourField = null;
        Field minuteField = null;
        Field amPmField = null;
        NumberPicker hourNumberPicker = null;
        NumberPicker minuteNumberPicker = null;
        NumberPicker amPmNumberPicker = null;

        try {
            // Create an instance of the id class
            idClass = Class.forName("com.android.internal.R$id");

            // Get the fields that store the resource IDs for the hour, minute and amPm NumberPickers
            hourField = idClass.getField("hour");
            minuteField = idClass.getField("minute");
            amPmField = idClass.getField("amPm");

            // Use the resource IDs to get references to the hour, minute and amPm NumberPickers
            hourNumberPicker = (NumberPicker) findViewById(hourField.getInt(null));
            minuteNumberPicker = (NumberPicker) findViewById(minuteField.getInt(null));
            amPmNumberPicker = (NumberPicker) findViewById(amPmField.getInt(null));

            numberPickerClass = Class.forName("android.widget.NumberPicker");

            // Set the value of the mSelectionDivider field in the hour, minute and amPm NumberPickers
            // to refer to our custom drawables
            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(hourNumberPicker, ContextCompat.getDrawable(context, R.drawable.selection_divider));
            selectionDividerField.set(minuteNumberPicker, ContextCompat.getDrawable(context, R.drawable.selection_divider));
            selectionDividerField.set(amPmNumberPicker, ContextCompat.getDrawable(context, R.drawable.selection_divider));
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException in CustomTimePicker", e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "NoSuchFieldException in CustomTimePicker", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException in CustomTimePicker", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in CustomTimePicker", e);
        }
    }

    @SuppressWarnings("deprecation")
    public void setHour(int hour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            super.setHour(hour);
        else
            super.setCurrentHour(hour);
    }

    @SuppressWarnings("deprecation")
    public void setMinute(int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            super.setMinute(minute);
        else
            super.setCurrentMinute(minute);
    }

    @SuppressWarnings("deprecation")
    public int getHour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return super.getHour();
        else
            return super.getCurrentHour();
    }

    @SuppressWarnings("deprecation")
    public int getMinute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return super.getMinute();
        else
            return super.getCurrentMinute();
    }
}
