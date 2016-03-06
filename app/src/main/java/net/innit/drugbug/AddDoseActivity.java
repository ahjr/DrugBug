package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.ImageStorage;
import net.innit.drugbug.util.Reminder;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// future todo ability to change dose taken date

public class AddDoseActivity extends FragmentActivity {

    /**
     * Action String constants
     */
    public static final String ACTION_ADD = "add";
    public static final String ACTION_EDIT = "edit";

    private static final int REQUEST_TAKE_PICTURE = 300;
    private static final int REQUEST_CAPTURE_IMAGE = 400;

    private static final String MED_IMAGE_FILE_PROVIDER = "net.innit.drugbug.med_image.fileprovider";

    private final DBDataSource db = new DBDataSource(this);
    private String action;

    private File dir;
    private File tempPath;

    private DoseItem doseItem = new DoseItem();
    private String freqOrig;
    private String type;

    private boolean wasChecked;
    private boolean imageLocationOK;

    private ImageView mMedImage;
    private Button mMedImageButton;
    private TextView mMedName;
    private TextView mDosage;
    private CheckBox mReminder;
    private Spinner mFrequency;
    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            // Do something with the date. This Date object contains
            // the date and time that the user has selected.
            doseItem.setDate(date);
            Log.d(MainActivity.LOGTAG, "onDateTimeSet: Date set to " + date.toString());
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
            Log.d(MainActivity.LOGTAG, "onDateTimeSet: Date change cancelled");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        setupViews();

        imageLocationOK = setupImageLocation();

        Bundle bundle = getIntent().getExtras();
        action = bundle.getString("action", ACTION_ADD);
        type = bundle.getString("type", DoseItem.TYPE_NONE);

        if (action.equals(ACTION_EDIT))
            populateOnEdit(bundle);

    }

    private void setupViews() {
        mMedImage = (ImageView) findViewById(R.id.ivAddMedImage);
        mMedImageButton = (Button) findViewById(R.id.btnAddMedImage);
        mMedName = (EditText) findViewById(R.id.etAddMedName);
        mDosage = (EditText) findViewById(R.id.etAddMedDosage);
        mReminder = (CheckBox) findViewById(R.id.cbAddMedReminder);
        mReminder.setChecked(true);
        mFrequency = (Spinner) findViewById(R.id.spAddMedFreq);
    }

    private void populateOnEdit(Bundle bundle) {
        db.open();
        doseItem = db.getDose(bundle.getLong("dose_id"));
        freqOrig = doseItem.getMedication().getFrequency();

        if (doseItem.getMedication().hasImage()) {
            Bitmap image = doseItem.getMedication().getBitmap(this);
            mMedImage.setImageBitmap(image);

            mMedImageButton.setText(R.string.add_dose_button_image_change);
        }

        db.close();

        setTitle(R.string.add_dose_title_edit);
    }

    private boolean setupImageLocation() {
        // Need to do some stuff with the temporary image file
        ImageStorage imageStorage = ImageStorage.getInstance(this);
        // Get the absolute path based on the current location SharedPreference setting
        dir = imageStorage.getAbsDir();
        Log.d(MainActivity.LOGTAG, "setupImageLocation: dir is " + dir);

        if (dir.canWrite()) {
            Log.d(MainActivity.LOGTAG, "onCreate: " + dir.getAbsolutePath() + " is writable");
            // Temporary filename.  We'll save it to it's permanent place when we save the MedicationItem
            tempPath = new File(dir, getString(R.string.add_dose_temp_image_filename));
            // Delete any temp file left over from a previous addition
            boolean fileDeleted = tempPath.delete();
            Log.d(MainActivity.LOGTAG, "onCreate: Temp file " + ((fileDeleted) ? " deleted" : " not deleted"));
            return true;
        } else {
            Log.d(MainActivity.LOGTAG, "onCreate: " + dir.getAbsolutePath() + " is not writable");
            // Hide the image add/edit button
            mMedImageButton.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (imageLocationOK && tempPath.isFile()) {
            Bitmap image = BitmapFactory.decodeFile(tempPath.getAbsolutePath());

            mMedImage.setImageBitmap(image);

            mMedImageButton.setText(R.string.add_dose_button_image_change);

        }

        if (action.equals(ACTION_EDIT)) {
            mMedName.setText(doseItem.getMedication().getName());

            mDosage.setText(doseItem.getDosage());

            mReminder.setChecked(doseItem.isReminderSet());
            wasChecked = doseItem.isReminderSet();

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, db.getFreqLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mFrequency.setAdapter(adapter);
        if (action.equals(ACTION_EDIT)) {
            mFrequency.setSelection(adapter.getPosition(doseItem.getMedication().getFrequency()));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default_help:
                Bundle bundle = new Bundle();
                bundle.putInt("source", (action.equals(ACTION_EDIT)) ? HelpFragment.SOURCE_EDIT_DOSE : HelpFragment.SOURCE_ADD_DOSE);

                HelpFragment fragment = new HelpFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), "Help Fragment");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", type);

        startActivity(intent);
        finish();
    }

    public void onClickAddMedAddImage(View view) {
//        Uri outputFileUri = Uri.fromFile(tempPath);
        Uri outputFileUri = FileProvider.getUriForFile(this, MED_IMAGE_FILE_PROVIDER, tempPath);
        if (hasCamera()) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickAddMedSave(View view) {
        db.open();

        MedicationItem medication = (action.equals(ACTION_ADD) ? new MedicationItem() : doseItem.getMedication());

        medication.setName(mMedName.getText().toString());

        medication.setFrequency(mFrequency.getSelectedItem().toString());

        boolean freqChanged = false;
        // If frequency has changed, remove all old future doses
        if (action.equals(ACTION_EDIT)) {
            if (!medication.getFrequency().equals(freqOrig)) {
                // frequency has changed, so delete all previous futures with this medId
                int dosesRemoved = db.removeAllFutureDosesForMed(medication);
                Log.d(MainActivity.LOGTAG, "onClickAddMedSave: " + dosesRemoved + " doses removed");
                freqChanged = true;
            }
        }

        // Rename temp image file to medication name
        if (imageLocationOK && tempPath.isFile()) {
            String filename = medication.getName().toLowerCase().replace(" ", "_") + ".jpg";
            File path = new File(dir, filename);
            boolean success = tempPath.renameTo(path);
            if (!success) {
                Log.d(MainActivity.LOGTAG, "- Unable to rename file");
            }
            medication.setImagePath(filename);
        }

        if (!medication.getName().equals("")) {
            if (action.equals(ACTION_ADD)) {
                medication = db.createMedication(medication);
            } else if (action.equals(ACTION_EDIT)) {
                if (db.updateMedication(medication)) {
                    Log.d(MainActivity.LOGTAG, "onClickAddMedSave: medication " + medication.getId() + " updated");
                } else {
                    Log.d(MainActivity.LOGTAG, "onClickAddMedSave: unable to update medication " + medication.getId());
                }
            }

            // Figure out how many future items to create
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int numFutureDoses = preferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES);

            // For # of future items, create a future item using the medication id created earlier
            doseItem.setDosage(mDosage.getText().toString());

            doseItem.setReminder(mReminder.isChecked());

            if (!doseItem.getDosage().equals("")) {
                Date doseDate = new Date();
                Calendar calendar;
                if ((action.equals(ACTION_ADD)) || freqChanged) {
                    Log.d(MainActivity.LOGTAG, "onClickAddMedSave: action is add or frequency has changed");
                    for (int i = 0; i < numFutureDoses; i++) {
                        Log.d(MainActivity.LOGTAG, "onClickAddMedSave: iteration " + i);
                        if (i == 0) {
                            // Use current datetime as a starting point in first iteration
                            // FUTURE TODO Allow user to choose first dose time, as well as ability to set meal and waking times for more accuracy
                            calendar = Calendar.getInstance();
                        } else {
                            // Add interval to last date
                            calendar = Calendar.getInstance();
                            calendar.setTime(doseDate);
                            calendar.add(Calendar.SECOND, (int) db.getInterval(medication.getFrequency()));
                        }
                        doseDate = calendar.getTime();
                        DoseItem futureItem = new DoseItem(medication, doseDate, doseItem.isReminderSet(), false, doseItem.getDosage());
                        futureItem = db.createDose(futureItem);
                        Log.d(MainActivity.LOGTAG, "onClickAddMedSave: future dose created with id " + futureItem.getId());
                        // if reminder is true set a reminder
                        if (doseItem.isReminderSet()) {
                            new Reminder(this).startReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, futureItem);
                        }
                    }
                } else {
                    List<DoseItem> doses = db.getAllFutureForMed(medication);
                    Log.d(MainActivity.LOGTAG, "onClickAddMedSave: updating all futures: " + doses.size() + " futures retrieved");
                    for (DoseItem futureDose : doses) {
                        futureDose.setDosage(doseItem.getDosage());
                        futureDose.setReminder(doseItem.isReminderSet());
                        if (db.updateDose(futureDose)) {
                            if (doseItem.isReminderSet() && !wasChecked) {
                                // if reminder is true and wasChecked is false
                                // create a reminder
                                new Reminder(this).startReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, futureDose);
                            } else if (!doseItem.isReminderSet() && wasChecked) {
                                // if reminder is false and wasChecked is true
                                // remove reminder
                                new Reminder(this).killReminder(Reminder.REQUEST_HEADER_FUTURE_DOSE, futureDose);
                            }
                        } else {
                            Toast.makeText(this, R.string.error_update_dose, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                db.close();
                Intent intent = new Intent(this, DoseListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("type", type);
                if (type.equals(DoseItem.TYPE_SINGLE))
                    intent.putExtra("med_id", medication.getId());
                startActivity(intent);
                finish(); // Call once you redirect to another activity
            } else {
                // Dosage is empty
                Toast.makeText(this, R.string.add_dose_error_blank_dosage, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Medication name is empty
            Toast.makeText(this, R.string.add_dose_error_blank_name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PICTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "User cancelled", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private boolean hasCamera() {
        final boolean deviceHasCameraFlag = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        return !(!deviceHasCameraFlag || Camera.getNumberOfCameras() == 0);
    }

    public void onClickDatetime(View view) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        new SlideDateTimePicker.Builder(fm)
                .setListener(listener)
                .setInitialDate(new Date())
                .build()
                .show();
    }
}
