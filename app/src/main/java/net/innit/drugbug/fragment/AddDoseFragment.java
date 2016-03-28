package net.innit.drugbug.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.BitmapHelper;
import net.innit.drugbug.util.DateUtil;
import net.innit.drugbug.util.ImageStorage;
import net.innit.drugbug.util.Reminder;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_ADD;
import static net.innit.drugbug.data.Constants.ACTION_EDIT;
import static net.innit.drugbug.data.Constants.ACTION_REACTIVATE;
import static net.innit.drugbug.data.Constants.ACTION_RESTORE;
import static net.innit.drugbug.data.Constants.FILTER_DOSE;
import static net.innit.drugbug.data.Constants.IMAGE_HEIGHT_PREVIEW;
import static net.innit.drugbug.data.Constants.IMAGE_WIDTH_PREVIEW;
import static net.innit.drugbug.data.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
import static net.innit.drugbug.data.Constants.LOG;
import static net.innit.drugbug.data.Constants.SORT;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_NONE;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;

public class AddDoseFragment extends DialogFragment {
    private static final int REQUEST_TAKE_PICTURE = 300;

    private DatabaseDAO db;
    private Context context;

    private String action;
    private String type;
    private String sortOrder;
    private String filter;

    private File dir;
    private File tempPath;

    private DoseItem doseItem = new DoseItem();
    private Date origDate;
    private String origFreq;

    private boolean wasChecked;
    private boolean imageLocationOK;
    private ImageStorage imageStorage;

    private ImageButton mMedImage;
    private TextView mMedName;
    private TextView mDosage;
    private TextView mDateTimeLabel;
    private EditText mDateTime;
    private CheckBox mReminder;
    private Spinner mFrequency;

    private DateFormat sdf;

    private final SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            mDateTime.setText(sdf.format(date));
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        db = new DatabaseDAO(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_edit, container, false);

        setupViews(view);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());

        imageLocationOK = setupImageLocation();

        Bundle bundle = getArguments();
        action = bundle.getString(ACTION, ACTION_ADD);
        type = bundle.getString(TYPE, TYPE_NONE);
        sortOrder = bundle.getString(SORT);
        filter = bundle.getString(FILTER_DOSE);

        mDateTime.setText(sdf.format(new Date()));

        switch (action) {
            case ACTION_EDIT:
                onCreateEdit(bundle);
                break;
            case ACTION_RESTORE:
                onCreateRestore(bundle);
                break;
            case ACTION_REACTIVATE:
                onCreateReactivate(bundle);
                break;
        }

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (imageLocationOK && tempPath.isFile()) {
            Bitmap image = BitmapHelper.decodeSampledBitmapFromFile(tempPath.getAbsolutePath(), IMAGE_WIDTH_PREVIEW, IMAGE_HEIGHT_PREVIEW);
            mMedImage.setImageBitmap(image);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, db.getFreqLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mFrequency.setAdapter(adapter);

        switch (action) {
            case ACTION_EDIT:
                mMedName.setText(doseItem.getMedication().getName());
                mDosage.setText(doseItem.getDosage());
                mReminder.setChecked(doseItem.isReminderSet());
                wasChecked = doseItem.isReminderSet();
            case ACTION_RESTORE:
                mMedName.setText(doseItem.getMedication().getName());
            case ACTION_REACTIVATE:
                mFrequency.setSelection(adapter.getPosition(doseItem.getMedication().getFrequency()));
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        tempPath.delete();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.dismiss();
    }

    private void onCreateEdit(Bundle bundle) {
        db.open();
        doseItem = db.getDose(bundle.getLong(INTENT_DOSE_ID));
        db.close();

        onCreateAll();

        mDateTimeLabel.setText(R.string.add_dose_datetime_label);
        mDateTime.setText(sdf.format(doseItem.getDate()));
        origDate = doseItem.getDate();
    }

    private void onCreateReactivate(Bundle bundle) {
        db.open();
        doseItem = db.getDose(bundle.getLong(INTENT_DOSE_ID));
        db.close();

        onCreateAll();
    }

    private void onCreateRestore(Bundle bundle) {
        db.open();
        MedicationItem medicationItem = db.getMedication(bundle.getLong(INTENT_MED_ID));
        doseItem.setMedication(medicationItem);
        db.close();

        onCreateAll();
    }

    private void onCreateAll() {
        origFreq = doseItem.getMedication().getFrequency();

        if (doseItem.getMedication().hasImage()) {
            doseItem.getMedication().getBitmap(context, mMedImage, IMAGE_WIDTH_PREVIEW, IMAGE_HEIGHT_PREVIEW);
        }

    }

    private void setupViews(View v) {
        mMedImage = (ImageButton) v.findViewById(R.id.ivAddMedImage);
        mMedName = (EditText) v.findViewById(R.id.etAddMedName);
        mDosage = (EditText) v.findViewById(R.id.etAddMedDosage);
        mDateTimeLabel = (TextView) v.findViewById(R.id.tvAddMedDateTimeLabel);
        mDateTime = (EditText) v.findViewById(R.id.etAddMedDateTime);
        mReminder = (CheckBox) v.findViewById(R.id.cbAddMedReminder);
        mReminder.setChecked(true);
        mFrequency = (Spinner) v.findViewById(R.id.spAddMedFreq);

        mMedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddMedAddImage(v);
            }
        });

        ImageButton imageButton = (android.widget.ImageButton) v.findViewById(R.id.btnAddMedDate);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDatetime(v);
            }
        });

        Button button = (Button) v.findViewById(R.id.btnAddMedSave);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddMedSave(v);
            }
        });
    }

    private boolean setupImageLocation() {
        // Need to do some stuff with the temporary image file
        imageStorage = ImageStorage.getInstance(context);
        // Get the absolute path based on the current location SharedPreference setting
        dir = imageStorage.getAbsDir();

        if (dir.canWrite()) {
            // Temporary filename.  We'll save it to it's permanent place when we save the MedicationItem
            tempPath = new File(dir, getString(R.string.add_dose_temp_image_filename));
            return true;
        } else {
            // Hide the image button
            mMedImage.setVisibility(View.GONE);
        }
        return false;
    }

    private void onClickAddMedAddImage(View view) {
        // Delete any temp file left over from a previous addition
        boolean fileDeleted = tempPath.delete();
        if (fileDeleted) {
            Log.i(LOG, "onClickAddMedAddImage: Temporary file deleted: " + tempPath.toString());
        }
        Uri outputFileUri = imageStorage.getStorageUri(tempPath);
        if (hasCamera()) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        } else {
            Toast.makeText(context, R.string.camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickAddMedSave(View view) {

        MedicationItem medication = getMedicationItem();

        db.open();
        if (medication.getName().equals("")) {
            // Medication name is empty
            Toast.makeText(context, R.string.add_dose_error_blank_name, Toast.LENGTH_SHORT).show();
        } else {
            if (action.equals(ACTION_ADD)) {
                medication = db.createMedication(medication);
            } else {
                db.updateMedication(medication);
            }

            doseItem.setDosage(mDosage.getText().toString());

            doseItem.setReminder(mReminder.isChecked());

            if (!setDoseDate()) {
                Toast.makeText(context, R.string.add_dose_error_invalid_date, Toast.LENGTH_SHORT).show();
            } else {
                if (doseItem.getDosage().equals("")) {
                    // Dosage is empty
                    Toast.makeText(context, R.string.add_dose_error_blank_dosage, Toast.LENGTH_SHORT).show();
                } else {
                    if (action.equals(ACTION_EDIT) && !checkFreqChanged(medication)) {
                        List<DoseItem> doses = db.getAllFutureForMed(context, medication);
                        DoseItem nextDose = doseItem;
                        for (DoseItem futureDose : doses) {
                            nextDose = updateFutureDose(nextDose, futureDose);
                        }
                    } else {
                        DoseItem futureItem = doseItem;
                        int numFutureDoses = Integer.parseInt(Settings.getInstance().getString(Settings.Key.NUM_DOSES));
                        Calendar calendar = Calendar.getInstance();
                        for (int i = 0; i < numFutureDoses; i++) {
                            calendar.setTime(futureItem.getDate());
                            if (i > 0) {
                                // Add interval to last date
                                calendar.add(Calendar.SECOND, (int) db.getInterval(medication.getFrequency()));
                            }
                            futureItem = new DoseItem(medication, calendar.getTime(), doseItem.isReminderSet(), false, doseItem.getDosage());
                            futureItem = db.createDose(futureItem);

                            handleReminder(futureItem);
                        }
                    }

                    Intent intent = new Intent(context, DoseListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(TYPE, type);
                    if (!action.equals(ACTION_RESTORE)) {
                        // We want the dose list to use default values for sorting and filtering
                        // if we are coming from the medication list
                        intent.putExtra(SORT, sortOrder);
                        intent.putExtra(FILTER_DOSE, filter);
                    }
                    if (type.equals(TYPE_SINGLE)) {
                        intent.putExtra(INTENT_MED_ID, medication.getId());
                    }
                    startActivity(intent);
                    dismiss(); // Call once you redirect to another activity
                }
            }
        }
        db.close();
    }

    private boolean setDoseDate() {
        String format = DateUtil.determineDateFormat(mDateTime.getText().toString());
        if (format != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            try {
                doseItem.setDate(formatter.parse(mDateTime.getText().toString()));
                return true;
            } catch (ParseException e) {
                Log.e(LOG, "onClickAddMedSave: Unable to parse date");
            }
        }
        return false;
    }

    private DoseItem updateFutureDose(DoseItem nextDose, DoseItem thisDose) {
        if (thisDose.getId() != doseItem.getId()) {
            // Only do the setting if this is not the dose currently being edited
            if (thisDose.getDate().getTime() > origDate.getTime()) {
                thisDose.setDate(nextDose.nextDate(context));
                nextDose = thisDose;
            }
            thisDose.setDosage(doseItem.getDosage());
            thisDose.setReminder(doseItem.isReminderSet());
        } else {
            thisDose = doseItem;
        }

        if (db.updateDose(thisDose)) {
            handleReminder(thisDose);
        } else {
            Toast.makeText(context, R.string.error_update_dose, Toast.LENGTH_SHORT).show();
        }
        return nextDose;
    }

    private void handleReminder(DoseItem mDose) {
        if (doseItem.isReminderSet() && !wasChecked) {
            // if reminder is true and wasChecked is false
            // create a reminder
            new Reminder(context).startReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, mDose);
        } else if (!doseItem.isReminderSet() && wasChecked) {
            // if reminder is false and wasChecked is true
            // remove reminder
            new Reminder(context).killReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, mDose);
        }
    }

    @NonNull
    private MedicationItem getMedicationItem() {
        MedicationItem medication = (action.equals(ACTION_ADD) ? new MedicationItem() : doseItem.getMedication());
        medication.setArchived(false);
        medication.setActive(true);

        medication.setName(mMedName.getText().toString());

        medication.setFrequency(mFrequency.getSelectedItem().toString());

        // Rename temp image file to medication name
        if (imageLocationOK && tempPath.isFile()) {
            renameTempImage(medication);
        }
        return medication;
    }

    private boolean checkFreqChanged(MedicationItem medication) {
        // If frequency has changed, remove all old future doses
        if (action.equals(ACTION_EDIT)) {
            if (!medication.getFrequency().equals(origFreq)) {
                // frequency has changed, so delete all previous futures with this medId
                db.removeAllFutureDosesForMed(medication);
                return true;
            }
        }
        return false;
    }

    private void renameTempImage(MedicationItem medication) {
        String filename = medication.getName().toLowerCase().replace(" ", "_") + ".jpg";
        File path = new File(dir, filename);
        boolean b = tempPath.renameTo(path);
        if (!b) {
            Log.e(LOG, "Unable to rename " + tempPath.toString() + " to " + path.toString());
        }
        medication.setImagePath(filename);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PICTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, R.string.user_canceled, Toast.LENGTH_SHORT).show();
                }
        }
    }

    private boolean hasCamera() {
        final boolean deviceHasCameraFlag = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        //noinspection deprecation
        return !(!deviceHasCameraFlag || Camera.getNumberOfCameras() == 0);
    }

    private void onClickDatetime(View view) {
        android.support.v4.app.FragmentManager fm = ((FragmentActivity)getActivity()).getSupportFragmentManager();
        new SlideDateTimePicker.Builder(fm)
                .setListener(listener)
                .setInitialDate(doseItem.getDate())
                .build()
                .show();
    }
}
