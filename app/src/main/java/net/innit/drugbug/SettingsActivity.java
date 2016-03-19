package net.innit.drugbug;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.fragment.SettingsFragment;

import static net.innit.drugbug.util.Constants.SOURCE;
import static net.innit.drugbug.util.Constants.SOURCE_SETTINGS;

public class SettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default_help:
                Bundle bundle = new Bundle();
                bundle.putInt(SOURCE, SOURCE_SETTINGS);

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
}
