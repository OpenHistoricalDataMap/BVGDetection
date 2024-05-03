package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

public class FingerprintListAdapter extends ArrayAdapter {

    private final Activity context;
    private final List<String> names;
    private final List<String> strength;
    private final List<String> ssid;

    public FingerprintListAdapter(Activity context, List<String> names, List<String> strength, List<String> ssid) {
        super(context, R.layout.item_fingerprint_listview);

        this.context = context;
        this.names = names;
        this.strength = strength;
        this.ssid = ssid;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("FingerprintListAdapter", String.valueOf(position));
        Log.d("FingerprintListAdapter", String.valueOf(convertView));
        Log.d("FingerprintListAdapter", String.valueOf(parent));
        View row = convertView;
        FingerprintViewHolder viewHolder;

        if (row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate(R.layout.item_fingerprint_listview, parent, false);

            viewHolder = new FingerprintViewHolder(row);
            row.setTag(viewHolder);
        } else {
            viewHolder = (FingerprintViewHolder) row.getTag();
        }

        Log.d("FingerprintListAdapter", "getView: " + names.get(position) + " " + strength.get(position) + " " + ssid.get(position));

        viewHolder.name.setText(names.get(position));
        viewHolder.strength.setText(strength.get(position));
        viewHolder.ssid.setText(ssid.get(position));

        return row;
    }

    @Override
    public int getCount() {
        return names.size();
    }

}
