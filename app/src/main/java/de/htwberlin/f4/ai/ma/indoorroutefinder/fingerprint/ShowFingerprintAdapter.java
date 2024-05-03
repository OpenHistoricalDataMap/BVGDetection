package de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Created by Johann Winter
 * <p>
 * Adapter to fill the ListView in ShowFingerprintActivity with fingerprint data
 */
public class ShowFingerprintAdapter extends BaseExpandableListAdapter {

    private final Fingerprint fingerprint;
    private final Context context;

    public ShowFingerprintAdapter(Context context, Fingerprint fingerprint) {
        this.context = context;
        this.fingerprint = fingerprint;
    }


    @Override
    public int getGroupCount() {
        return fingerprint.getSignalSampleList().size();
    }

    @Override
    public int getChildrenCount(int i) {
        return fingerprint.getSignalSampleList().get(i).getAccessPointInformationList().size();
    }

    @Override
    public Object getGroup(int i) {
        return fingerprint.getSignalSampleList().get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return fingerprint.getSignalSampleList().get(i).getAccessPointInformationList().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        TextView textView = new TextView(context);
        textView.setText((i + 1) + ". Sekunde");
        textView.setPadding(100, 0, 0, 0);
        textView.setTextSize(20);
        return textView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        textView.setText(fingerprint.getSignalSampleList().get(i).getAccessPointInformationList().get(i1).getBSSID() +
                "   " + fingerprint.getSignalSampleList().get(i).getAccessPointInformationList().get(i1).getRSSI() + " dBm");
        textView.setPadding(140, 0, 0, 0);
        return textView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
