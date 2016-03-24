package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.innit.drugbug.R;

import static net.innit.drugbug.data.Constants.SOURCE;
import static net.innit.drugbug.data.Constants.SOURCE_ADD_DOSE;
import static net.innit.drugbug.data.Constants.SOURCE_DETAIL_FUTURE;
import static net.innit.drugbug.data.Constants.SOURCE_EDIT_DOSE;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_FUTURE;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_MEDICATIONS;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_REMINDERS;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_SINGLE_MED;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_TAKEN;
import static net.innit.drugbug.data.Constants.SOURCE_MAIN;
import static net.innit.drugbug.data.Constants.SOURCE_SETTINGS;

public class HelpFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        int source = bundle.getInt(SOURCE, SOURCE_MAIN);
        int layoutID, llID;

        switch (source) {
            case SOURCE_ADD_DOSE:
                layoutID = R.layout.help_add_dose;
                llID = R.id.ll_help_add;
                break;
            case SOURCE_EDIT_DOSE:
                layoutID = R.layout.help_add_dose;
                llID = R.id.ll_help_add;
                break;
            case SOURCE_SETTINGS:
                layoutID = R.layout.help_settings;
                llID = R.id.ll_help_settings;
                break;
            case SOURCE_LIST_FUTURE:
                layoutID = R.layout.help_list_future;
                llID = R.id.ll_help_list_future;
                break;
            case SOURCE_LIST_TAKEN:
                layoutID = R.layout.help_list_taken;
                llID = R.id.ll_help_list_taken;
                break;
            case SOURCE_LIST_REMINDERS:
                layoutID = R.layout.help_list_future;
                llID = R.id.ll_help_list_future;
                break;
            case SOURCE_LIST_SINGLE_MED:
                layoutID = R.layout.help_list_single;
                llID = R.id.ll_help_list_single;
                break;
            case SOURCE_DETAIL_FUTURE:
                layoutID = R.layout.help_detail_future;
                llID = R.id.ll_help_detail_future;
                break;
            case SOURCE_LIST_MEDICATIONS:
                layoutID = R.layout.help_list_med;
                llID = R.id.ll_help_list_med;
                break;
            default:
                layoutID = R.layout.help_main;
                llID = R.id.ll_help_main;
                break;
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(layoutID, container, false);
        makeLayoutChanges(view, source);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(llID);
        linearLayout.setClickable(true);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void makeLayoutChanges(View view, int source) {
        switch (source) {
            case SOURCE_EDIT_DOSE:
                ((TextView) view.findViewById(R.id.tv_help_add_time_label)).setText(R.string.add_dose_datetime_label);
                ((TextView) view.findViewById(R.id.tv_help_add_time_text1)).setText("Set time of this dose");
                break;
            case SOURCE_LIST_REMINDERS:
                ((TextView)view.findViewById(R.id.tv_help_list_future_title)).setText("Help - Reminders list");
                break;
        }
    }

    public static void showHelp(FragmentManager fm, int source) {
        Bundle bundle = new Bundle();
        bundle.putInt(SOURCE, source);

        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(bundle);
        fragment.show(fm, "Help Fragment");

    }
}
