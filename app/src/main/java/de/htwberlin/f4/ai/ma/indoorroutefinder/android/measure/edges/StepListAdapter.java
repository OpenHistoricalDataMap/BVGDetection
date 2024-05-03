package de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.edges;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

/**
 * StepListAdapter Class
 * <p>
 * Stores detailed data for each step of an edge
 * <p>
 * Author: Benjamin Kneer
 */

public class StepListAdapter extends ArrayAdapter<StepData> {

    public StepListAdapter(@NonNull Context context, List<StepData> stepList) {
        super(context, 0, stepList);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // load layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_edge_details_listview_step, parent, false);
        }
        // find ui elements
        TextView stepNumberView = (TextView) convertView.findViewById(R.id.listview_group_stepname);
        TextView stepCoordsView = (TextView) convertView.findViewById(R.id.listview_group_step_coords);
        // update step number
        stepNumberView.setText("# " + (position + 1));
        // retrieve stepdata from list
        StepData stepData = getItem(position);
        // round coordinates
        float roundX = Math.round(stepData.getCoords()[0] * 100.0f) / 100.0f;
        float roundY = Math.round(stepData.getCoords()[1] * 100.0f) / 100.0f;
        float roundZ = Math.round(stepData.getCoords()[2] * 100.0f) / 100.0f;
        // update textview with rounded coordinates
        stepCoordsView.setText(roundX + " | " + roundY + " | " + roundZ);

        return convertView;
    }
}
