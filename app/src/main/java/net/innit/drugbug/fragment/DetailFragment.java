package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.innit.drugbug.AddDoseActivity;
import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.model.DoseItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.innit.drugbug.util.Constants.ACTION;
import static net.innit.drugbug.util.Constants.ACTION_EDIT;
import static net.innit.drugbug.util.Constants.IMAGE_HEIGHT_PREVIEW;
import static net.innit.drugbug.util.Constants.IMAGE_WIDTH_PREVIEW;
import static net.innit.drugbug.util.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.util.Constants.INTENT_MED_ID;
import static net.innit.drugbug.util.Constants.SORT;
import static net.innit.drugbug.util.Constants.SOURCE;
import static net.innit.drugbug.util.Constants.SOURCE_DETAIL_FUTURE;
import static net.innit.drugbug.util.Constants.SOURCE_DETAIL_TAKEN;
import static net.innit.drugbug.util.Constants.TYPE;
import static net.innit.drugbug.util.Constants.TYPE_FUTURE;
import static net.innit.drugbug.util.Constants.TYPE_SINGLE;
import static net.innit.drugbug.util.Constants.TYPE_TAKEN;

/**
 * Fragment for displaying the detail of a dose
 */
public class DetailFragment extends DialogFragment {
    private long id;
    private DBDataSource db;
    private Context context;
    private DoseItem dose;
    private String sortOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        db = new DBDataSource(context);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        final String type = bundle.getString(TYPE, TYPE_FUTURE);
        id = bundle.getLong(INTENT_DOSE_ID);
        sortOrder = bundle.getString(SORT);

        db.open();
        dose = db.getDose(id);
        db.close();

        View view = inflater.inflate(R.layout.activity_detail, container, false);

        // Dose is taken
        if (dose.isTaken()) {
            // Remove reminder text view
            TextView textView = (TextView) view.findViewById(R.id.tvDetailReminder);
            textView.setVisibility(View.GONE);

            textView = (TextView) view.findViewById(R.id.tvDetailDateLabel);
            textView.setText(R.string.detail_date_label_taken);

            // Remove all the buttons
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.llButtonBar);
            linearLayout.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.tvDetailReminder);
            if (new Date().after(dose.getDate())) {
                // dose was missed so who cares if reminder is set or not
                textView.setVisibility(View.GONE);
            } else if (dose.isReminderSet()) {
                Drawable image = ContextCompat.getDrawable(context, R.drawable.ic_action_alarm_on);
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(image, null, null, null);
                textView.setText(R.string.reminder_list_set_positive);
            }

            textView = (TextView) view.findViewById(R.id.tvDetailDateLabel);
            textView.setText(R.string.detail_date_label_future);
        }

        // Set title of the popup to name of the medication
        getDialog().setTitle(dose.getMedication().getName());


        TextView textView = (TextView) view.findViewById(R.id.tvDetailDosage);
        textView.setText(dose.getDosage());

        textView = (TextView) view.findViewById(R.id.tvDetailFrequency);
        textView.setText(dose.getMedication().getFrequency());

        // Show the date all pretty instead of a long number
        // Set it red if the date has passed on taken doses
        DateFormat simpleDateFormat = SimpleDateFormat.getDateTimeInstance();
        String dateString = simpleDateFormat.format(dose.getDate());
        textView = (TextView) view.findViewById(R.id.tvDetailDate);
        textView.setText(dateString);
        if (type.equals(TYPE_FUTURE) && new Date().after(dose.getDate())) {
            textView.setTextColor(Color.RED);
        }

        // Open the edit screen if we click on the edit button
        Button button = (Button) view.findViewById(R.id.btnDetailEdit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddDoseActivity.class);
                intent.putExtra(ACTION, ACTION_EDIT);
                intent.putExtra(TYPE, type);
                intent.putExtra(INTENT_DOSE_ID, id);
                intent.putExtra(SORT, sortOrder);

                startActivity(intent);
                dismiss();
            }
        });

        // Delete the dose, but confirm first
        button = (Button) view.findViewById(R.id.btnDetailDelete);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DoseListActivity.class);
                intent.putExtra(TYPE, type);
                intent.putExtra(SORT, sortOrder);
                if (type.equals(TYPE_SINGLE))
                    intent.putExtra(INTENT_MED_ID, dose.getMedication().getId());
                dose.confirmDelete(context, intent);
            }
        });

        button = (Button) view.findViewById(R.id.btnDetailTaken);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_TAKEN);
                intent.putExtra(SORT, sortOrder);
                if (type.equals(TYPE_SINGLE))
                    intent.putExtra(INTENT_MED_ID, dose.getMedication().getId());
                dose.confirmTaken(context, intent);
            }
        });

        // Replace the detail image with the medication's image, if there is one
        if (dose.getMedication().hasImage()) {
            ImageView imageView = (ImageView) view.findViewById(R.id.ivDetailImage);

            dose.getMedication().getBitmap(context, imageView, IMAGE_WIDTH_PREVIEW, IMAGE_HEIGHT_PREVIEW);

            // Make the image thumbnail clickable and show it full size when it's clicked on
            imageView.setClickable(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(INTENT_MED_ID, dose.getMedication().getId());

                    ImageFragment imageFragment = new ImageFragment();
                    imageFragment.setArguments(bundle);

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.add(imageFragment, "Detail Fragment");
                    ft.commit();
                }
            });

        }

        ImageView imageView = (ImageView) view.findViewById(R.id.ivDetailHelp);
        if (type.equals(TYPE_TAKEN)) {
            imageView.setVisibility(View.INVISIBLE);
        } else {
            imageView.setClickable(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int source;
                    switch (type) {
                        case TYPE_TAKEN:
                            source = SOURCE_DETAIL_TAKEN;
                            break;
                        default:
                            source = SOURCE_DETAIL_FUTURE;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt(SOURCE, source);
                    HelpFragment fragment = new HelpFragment();
                    fragment.setArguments(bundle);
                    fragment.show(getFragmentManager(), "Help Fragment");
                }
            });
        }

        return view;
    }

}
