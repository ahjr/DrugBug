package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.innit.drugbug.R;

public class HelpFragment extends DialogFragment {
    public static final int SOURCE_MAIN = 3001001;
    public static final int SOURCE_ADD_DOSE = 3001002;
    public static final int SOURCE_EDIT_DOSE = 3001003;
    public static final int SOURCE_SETTINGS = 3001004;
    public static final int SOURCE_LIST_FUTURE = 3001005;
    public static final int SOURCE_LIST_TAKEN = 3001006;
    public static final int SOURCE_LIST_REMINDERS = 3001007;
    public static final int SOURCE_LIST_MEDICATIONS = 3001008;
    public static final int SOURCE_LIST_SINGLE_MED = 3001009;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        int source = bundle.getInt("source", SOURCE_MAIN);

        View view = inflater.inflate(R.layout.activity_help, container, false);

        SpannedString content;
        String title;

        switch (source) {
            case SOURCE_ADD_DOSE:
                content = (SpannedString) getText(R.string.help_add_dose);
                title = "Help - Add dose";
                break;
            case SOURCE_EDIT_DOSE:
                content = (SpannedString) getText(R.string.help_edit_dose);
                title = "Help - Edit dose";
                break;
            case SOURCE_SETTINGS:
                content = (SpannedString) getText(R.string.help_settings);
                title = "Help - Settings";
                break;
            case SOURCE_LIST_FUTURE:
                content = (SpannedString) getText(R.string.help_list_future);
                title = "Help - Future doses list";
                break;
            case SOURCE_LIST_TAKEN:
                content = (SpannedString) getText(R.string.help_list_taken);
                title = "Help - Taken doses list";
                break;
            case SOURCE_LIST_REMINDERS:
                content = (SpannedString) getText(R.string.help_reminders);
                title = "Help - Reminders list";
                break;
            case SOURCE_LIST_MEDICATIONS:
                content = (SpannedString) getText(R.string.help_medications);
                title = "Help - Medication list";
                break;
            case SOURCE_LIST_SINGLE_MED:
                content = (SpannedString) getText(R.string.help_single_med);
                title = "Help - Single medication list";
                break;
            default:
                content = (SpannedString) getText(R.string.help_main);
                title = "Help";
        }

        getDialog().setTitle(title);

        TextView textView = (TextView) view.findViewById(R.id.tvHelp);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        textView.setText(content);

        return view;

    }
}
