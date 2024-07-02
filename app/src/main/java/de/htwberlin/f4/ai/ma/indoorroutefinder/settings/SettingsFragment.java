package de.htwberlin.f4.ai.ma.indoorroutefinder.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * SettingsFragment is a fragment that handles the application's settings.
 * It allows users to configure various preferences related to the indoor route finder.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;

    /**
     * Default constructor.
     */
    public SettingsFragment() {
    }

    /**
     * Constructor with context.
     *
     * @param context the context of the application
     */
    @SuppressLint("ValidFragment")
    public SettingsFragment(Context context) {
        this.context = context;
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (sharedPreferences.contains("default_wifi_network")) {
            try {
                sharedPreferences.getStringSet("default_wifi_network", new HashSet<String>());
            } catch (ClassCastException e) {
                sharedPreferences.edit().remove("default_wifi_network").apply();
            }
        }

        List<AccessPointInformation> accessPointInformationList = databaseHandler.getAllAccessPoints();

        // Use a TreeSet with a custom Comparator to remove duplicates and sort entries
        Set<String> wifiSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        for (AccessPointInformation accessPointInformation : accessPointInformationList) {
            wifiSet.add(accessPointInformation.getSSID());
        }

        // Convert the set back to an array
        CharSequence[] wifis = wifiSet.toArray(new CharSequence[0]);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);

        // Use MultiSelectListPreference
        MultiSelectListPreference defaultWifiListpreference = (MultiSelectListPreference) findPreference("default_wifi_network");
        defaultWifiListpreference.setEntries(wifis);
        defaultWifiListpreference.setEntryValues(wifis);

        // Initialize preferences based on the current algorithm
        updatePreferences(sharedPreferences.getString("pref_algorithm", "svm"));

        // Initialize new preferences
        initializeNewPreferences();

        // Update the state of the preferences based on the current value of use_all_routers
        boolean useAllRouters = sharedPreferences.getBoolean("use_all_routers", true);
        setDefaultWifiNetworkEnabled(!useAllRouters);
    }

    private void initializeNewPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        CheckBoxPreference useAllRoutersPreference = (CheckBoxPreference) findPreference("use_all_routers");
        EditTextPreference routerThresholdPreference = (EditTextPreference) findPreference("router_threshold");
        EditTextPreference signalThresholdPreference = (EditTextPreference) findPreference("signal_threshold");
        ListPreference scalingStrategyPreference = (ListPreference) findPreference("scaling_strategy");
        ListPreference handleMissingPreference = (ListPreference) findPreference("handle_missing");

        if (useAllRoutersPreference != null) {
            useAllRoutersPreference.setChecked(sharedPreferences.getBoolean("use_all_routers", true));
        }

        if (routerThresholdPreference != null) {
            routerThresholdPreference.setSummary(sharedPreferences.getString("router_threshold", "0.25"));
            routerThresholdPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString());
                    return true;
                }
            });
        }

        if (signalThresholdPreference != null) {
            signalThresholdPreference.setSummary(sharedPreferences.getString("signal_threshold", "-100"));
            signalThresholdPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString());
                    return true;
                }
            });
        }

        if (scalingStrategyPreference != null) {
            scalingStrategyPreference.setValue(sharedPreferences.getString("scaling_strategy", "none"));
        }

        if (handleMissingPreference != null) {
            handleMissingPreference.setValue(sharedPreferences.getString("handle_missing", "-100"));
            handleMissingPreference.setSummary(getEntryFromValue(handleMissingPreference, handleMissingPreference.getValue()));
            handleMissingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(getEntryFromValue((ListPreference) preference, newValue.toString()));
                    return true;
                }
            });
        }
    }

    private CharSequence getEntryFromValue(ListPreference preference, String value) {
        int index = preference.findIndexOfValue(value);
        return index >= 0 ? preference.getEntries()[index] : null;
    }

    /**
     * Called when a shared preference is changed, added, or removed.
     *
     * @param sharedPreferences The SharedPreferences that received the change.
     * @param key               The key of the preference that was changed, added, or removed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "use_all_routers":
                boolean useAllRouters = sharedPreferences.getBoolean(key, true);
                setDefaultWifiNetworkEnabled(!useAllRouters);
                break;
            case "pref_algorithm":
                updatePreferences(sharedPreferences.getString(key, "svm"));
                break;
            case "router_threshold":
                EditTextPreference routerThresholdPreference = (EditTextPreference) findPreference(key);
                if (routerThresholdPreference != null) {
                    routerThresholdPreference.setSummary(sharedPreferences.getString(key, "0.25"));
                }
                break;
            case "signal_threshold":
                EditTextPreference signalThresholdPreference = (EditTextPreference) findPreference(key);
                if (signalThresholdPreference != null) {
                    signalThresholdPreference.setSummary(sharedPreferences.getString(key, "-100"));
                }
                break;
            case "scaling_strategy":
                ListPreference scalingStrategyPreference = (ListPreference) findPreference(key);
                if (scalingStrategyPreference != null) {
                    scalingStrategyPreference.setValue(sharedPreferences.getString(key, "none"));
                }
                break;
            case "handle_missing":
                ListPreference handleMissingPreference = (ListPreference) findPreference(key);
                if (handleMissingPreference != null) {
                    handleMissingPreference.setValue(sharedPreferences.getString(key, "-100"));
                    handleMissingPreference.setSummary(getEntryFromValue(handleMissingPreference, handleMissingPreference.getValue()));
                }
                break;
        }
    }

    private void setDefaultWifiNetworkEnabled(boolean enabled) {
        MultiSelectListPreference defaultWifiListpreference = (MultiSelectListPreference) findPreference("default_wifi_network");
        if (defaultWifiListpreference != null) {
            defaultWifiListpreference.setEnabled(enabled);
        }
    }

    /**
     * Updates the preferences based on the selected algorithm.
     *
     * @param algorithm the selected algorithm
     */
    private void updatePreferences(String algorithm) {
        boolean isKnn = algorithm.equals("knn");
        boolean isSvm = algorithm.equals("svm");
        boolean isRf = algorithm.equals("random_forest");

        setPreferenceEnabled("knn_settings", isKnn);
        setPreferenceEnabled("pref_knn_distance_metric", isKnn);
        setPreferenceEnabled("pref_knn_weight_type", isKnn);
        setPreferenceEnabled("pref_knn_k", isKnn);

        setPreferenceEnabled("svm_settings", isSvm);
        setPreferenceEnabled("pref_svm_c", isSvm);
        setPreferenceEnabled("pref_svm_gamma", isSvm);
        setPreferenceEnabled("pref_svm_kernel", isSvm);

        setPreferenceEnabled("rf_settings", isRf);
        setPreferenceEnabled("pref_rf_trees", isRf);

        // Optionale Anpassungen für neue Präferenzen je nach Algorithmus
        setPreferenceEnabled("use_all_routers", true);
        setPreferenceEnabled("router_threshold", true);
        setPreferenceEnabled("signal_threshold", true);
        setPreferenceEnabled("scaling_strategy", true);
        setPreferenceEnabled("handle_missing", true);
    }

    /**
     * Enables or disables a preference based on the given key.
     *
     * @param key     the key of the preference
     * @param enabled true to enable, false to disable
     */
    private void setPreferenceEnabled(String key, boolean enabled) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setEnabled(enabled);
        }
    }

    /**
     * Called when the fragment is no longer in use. Unregisters the shared preference change listener.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
