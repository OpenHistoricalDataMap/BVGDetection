package de.htwberlin.f4.ai.ma.indoorroutefinder.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
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
 * When SettingsActivity is launched, scan for available WiFi networks which have more
 * than one access point for providing a WiFi filter selection in settings.
 */
public class SettingsFragment extends PreferenceFragment {

    DatabaseHandler databaseHandler;
    Context context;

    public SettingsFragment() {
    }

    @SuppressLint("ValidFragment")
    public SettingsFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHandler = DatabaseHandlerFactory.getInstance(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.contains("default_wifi_network")) {
            try {
                sharedPreferences.getStringSet("default_wifi_network", new HashSet<String>());
            } catch (ClassCastException e) {
                sharedPreferences.edit().remove("default_wifi_network").apply();
            }
        }

        List<AccessPointInformation> accessPointInformationList = databaseHandler.getAllAccessPoints();

        // Verwende ein TreeSet mit einem benutzerdefinierten Comparator, um doppelte Einträge zu entfernen und zu sortieren
        Set<String> wifiSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        for (AccessPointInformation accessPointInformation : accessPointInformationList) {
            wifiSet.add(accessPointInformation.getSSID());
        }

        // Konvertiere das Set zurück in ein Array
        CharSequence[] wifis = wifiSet.toArray(new CharSequence[0]);

        // Lade die Präferenzen aus einer XML-Ressource
        addPreferencesFromResource(R.xml.preference);

        // Verwende MultiSelectListPreference
        MultiSelectListPreference defaultWifiListpreference = (MultiSelectListPreference) findPreference("default_wifi_network");
        defaultWifiListpreference.setEntries(wifis);
        defaultWifiListpreference.setEntryValues(wifis);
    }
}
