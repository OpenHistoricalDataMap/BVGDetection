package de.htwberlin.f4.ai.ma.indoorroutefinder.nodelist;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

/**
 * Created by Johann Winter
 * <p>
 * A viewholder for the NodeListAdapter
 */
class ViewHolder {
    ImageView nodeImageView;
    TextView nodeIdTextView;
    TextView nodeDescriptionTextView;

    ImageView arrowImageview;
    TextView distanceTextview;

    ViewHolder(View view) {
        nodeImageView = (ImageView) view.findViewById(R.id.preview_imageview);
        nodeIdTextView = (TextView) view.findViewById(R.id.node_name);
        nodeDescriptionTextView = (TextView) view.findViewById(R.id.node_description);

        arrowImageview = (ImageView) view.findViewById(R.id.arrow_imageview);
        distanceTextview = (TextView) view.findViewById(R.id.distance_textview);
    }
}