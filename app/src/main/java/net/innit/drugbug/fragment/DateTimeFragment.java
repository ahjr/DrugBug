package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TimePicker;

import net.innit.drugbug.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_time_picker, container, false);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                timePicker.getCurrentHour(),
                timePicker.getCurrentMinute());

        long time = calendar.getTimeInMillis();

        return view;
    }
}
