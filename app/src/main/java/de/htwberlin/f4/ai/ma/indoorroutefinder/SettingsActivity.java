package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.os.Bundle;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.settings.SettingsFragment;

public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_settings));

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment(this))
                .commit();
    }
}
