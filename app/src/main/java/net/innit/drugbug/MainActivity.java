package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.innit.drugbug.data.Constants;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.fragment.MainFragment;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_ADD;
import static net.innit.drugbug.data.Constants.SOURCE_MAIN;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializePrefs();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainFragment()).commit();
    }

    private void initializePrefs() {
        Settings settings = Settings.getInstance(getApplicationContext());

        // Clean SharedPreferences if constant is true
        if (Constants.CLEAR_SHARED_PREFS) {
            settings.clear();
            settings.apply();
        }

        // Set SharedPreferences to default if setting is not set yet
        settings.edit();
        for (Settings.Key setting : Settings.Key.values()) {
            if (!settings.contains(setting)) {
                if (setting == Settings.Key.NUM_DOSES) {
                    settings.put(setting);
                } else {
                    settings.put(setting);
                }
            }
        }
        settings.apply();
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
                intent.putExtra(ACTION, ACTION_ADD);
                startActivity(intent);
                return true;
            case R.id.menu_main_prefs:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_main_help:
                HelpFragment.showHelp(getFragmentManager(), SOURCE_MAIN);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
