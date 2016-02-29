package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;

// TODO really need to add comments everywhere
// TODO Extract string resources

public class MainActivity extends Activity {

    public static final String LOGTAG = "DrugBug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_add:
                Intent intent = new Intent(MainActivity.this, AddDoseActivity.class);
                intent.putExtra("action", AddDoseActivity.ACTION_ADD);
                startActivity(intent);
                return true;
            case R.id.menu_main_prefs:
                intent = new Intent(MainActivity.this, PrefScreenActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_main_help:
                Bundle bundle = new Bundle();
                bundle.putInt("source", HelpFragment.SOURCE_MAIN);

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

    /**
     * Click handler to launch future list activity
     *
     * @param view The view that was clicked
     */
    public void onClickMainFuture(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", DoseItem.TYPE_FUTURE);
        startActivity(intent);
    }

    /**
     * Click handler to launch taken list activity
     *
     * @param view The view that was clicked
     */
    public void onClickMainTaken(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", DoseItem.TYPE_TAKEN);
        startActivity(intent);
    }

    /**
     * Click handler to launch reminder list
     *
     * @param view The view that was clicked
     */
    public void onClickMainReminders(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", DoseItem.TYPE_REMINDER);
        startActivity(intent);
    }

    /**
     * Click handler to launch medication list
     *
     * @param view The view that was clicked
     */
    public void onClickMainMedications(View view) {
        Intent intent = new Intent(this, MedicationListActivity.class);
        startActivity(intent);
    }
}
