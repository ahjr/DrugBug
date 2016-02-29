package net.innit.drugbug;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.fragment.PrefScreenFragment;

public class PrefScreenActivity extends Activity {
    public static final String KEY_NUM_DOSES = "NumFutureDoses";
    public static final int DEFAULT_NUM_DOSES = 5;
    public static final String KEY_KEEP_TIME_TAKEN = "KeepTimeTaken";
    public static final String DEFAULT_KEEP_TIME_TAKEN = "1:0:0";
    public static final String KEY_KEEP_TIME_MISSED = "KeepTimeMissed";
    public static final String DEFAULT_KEEP_TIME_MISSED = "0:1:0";
    public static final String KEY_IMAGE_STORAGE = "StorageLoc";
    public static final String DEFAULT_IMAGE_STORAGE = "INTERNAL";

    public static int[] parseKeepTime(String keepTimeString) {
        int[] a = new int[3];
        String[] s = keepTimeString.split(":");
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(s[i]);
        }
        return a;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefScreenFragment()).commit();
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
                bundle.putInt("source", HelpFragment.SOURCE_SETTINGS);

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
