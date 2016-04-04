package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.util.OnListUpdatedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_EDIT;
import static net.innit.drugbug.data.Constants.IMAGE_HEIGHT_PREVIEW;
import static net.innit.drugbug.data.Constants.IMAGE_WIDTH_PREVIEW;
import static net.innit.drugbug.data.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
import static net.innit.drugbug.data.Constants.SORT_DOSE;
import static net.innit.drugbug.data.Constants.SOURCE_DETAIL_FUTURE;
import static net.innit.drugbug.data.Constants.SOURCE_DETAIL_TAKEN;
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TAG_DETAIL;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_FUTURE;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

/**
 * Fragment for displaying the detail of a dose
 */
public class DetailFragment extends DialogFragment {
    private long id;
    private DatabaseDAO db;
    private DoseItem dose;
    private String sortOrder;
    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseDAO(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        final String type = bundle.getString(TYPE, TYPE_FUTURE);
        id = bundle.getLong(INTENT_DOSE_ID);
        sortOrder = bundle.getString(SORT_DOSE);

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

            Drawable image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_content_alarm_off);
            if (new Date().after(dose.getDate())) {
                // dose was missed so who cares if reminder is set or not
                textView.setVisibility(View.GONE);
            } else if (dose.isReminderSet()) {
                image = ContextCompat.getDrawable(getActivity(), R.drawable.ic_content_alarm_on);
                textView.setText(R.string.reminder_list_set_positive);
            }
            final float density = getResources().getDisplayMetrics().density;
            image.setBounds(0, 0, Math.round(24 * density), Math.round(24 * density));
            textView.setCompoundDrawables(image, null, null, null);

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
        DateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
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
                Bundle b = new Bundle();
                b.putLong(INTENT_DOSE_ID, id);
                b.putString(ACTION, ACTION_EDIT);
                b.putString(TYPE, type);
                b.putString(SORT_DOSE, sortOrder);
                Fragment fragment = new AddDoseFragment();
                fragment.setArguments(b);
                getFragmentManager().beginTransaction().add(fragment, TAG_ADD).commit();
            }
        });

        // Delete the dose, but confirm first
        button = (Button) view.findViewById(R.id.btnDetailDelete);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dose.confirmDelete(getActivity(), new OnListUpdatedListener() {
                    @Override
                    public void onListUpdated() {
                        dismiss();
                    }
                });
            }
        });

        button = (Button) view.findViewById(R.id.btnDetailTaken);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_TAKEN);
                intent.putExtra(SORT_DOSE, sortOrder);
                if (type.equals(TYPE_SINGLE))
                    intent.putExtra(INTENT_MED_ID, dose.getMedication().getId());
                dose.confirmTaken(DetailFragment.this, intent);
            }
        });

        // Replace the detail image with the medication's image, if there is one
        if (dose.getMedication().hasImage()) {
            ImageView imageView = (ImageView) view.findViewById(R.id.ivDetailImage);

            dose.getMedication().getBitmap(getActivity(), imageView, IMAGE_WIDTH_PREVIEW, IMAGE_HEIGHT_PREVIEW);

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
                    ft.add(imageFragment, TAG_DETAIL);
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
                    HelpFragment.showHelp(getFragmentManager(), (type.equals(TYPE_TAKEN)) ? SOURCE_DETAIL_TAKEN : SOURCE_DETAIL_FUTURE);
                }
            });
        }

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
