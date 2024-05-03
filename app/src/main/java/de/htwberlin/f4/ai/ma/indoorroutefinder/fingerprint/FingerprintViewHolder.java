package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import android.view.View;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

class FingerprintViewHolder {

    TextView name;
    TextView strength;
    TextView ssid;


    FingerprintViewHolder(View view) {
        name = view.findViewById(R.id.fingerprint_name);
        ssid = view.findViewById(R.id.fingerprint_ssid);
        strength = view.findViewById(R.id.fingerprint_strength);
    }
}