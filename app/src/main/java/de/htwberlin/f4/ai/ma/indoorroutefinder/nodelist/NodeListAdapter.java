package de.htwberlin.f4.ai.ma.indoorroutefinder.nodelist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

/**
 * Created Johann Winter
 * <p>
 * Custom adapter for the NodeListActivity's node list.
 */
public class NodeListAdapter extends ArrayAdapter {

    private final int REGULAR_ITEM = 0;   // For "normal" Nodes
    private final Activity context;
    private final List<String> nodeNames;
    private final List<String> nodeDescriptions;
    private final List<String> nodePicturePaths;


    public NodeListAdapter(Activity context, List<String> nodeNames, List<String> nodeDescriptions, List<String> nodePicturePaths) {
        super(context, R.layout.item_nodes_listview);

        this.context = context;
        this.nodeNames = nodeNames;
        this.nodeDescriptions = nodeDescriptions;
        this.nodePicturePaths = nodePicturePaths;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder;

        if (row == null) {

            LayoutInflater layoutInflater = context.getLayoutInflater();

            if (getItemViewType(position) == REGULAR_ITEM) {
                row = layoutInflater.inflate(R.layout.item_nodes_listview, null, true);
            } else {
                row = layoutInflater.inflate(R.layout.item_navigation_distance_separator, null, true);
            }

            viewHolder = new ViewHolder(row);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) row.getTag();
        }


        if (getItemViewType(position) == REGULAR_ITEM) {
            viewHolder.nodeIdTextView.setText(nodeNames.get(position));
            viewHolder.nodeDescriptionTextView.setText(nodeDescriptions.get(position));

            // Load images
            if (nodePicturePaths.get(position) == null) {
                viewHolder.nodeImageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(R.drawable.unknown).into(viewHolder.nodeImageView);
            } else if (!nodePicturePaths.get(position).isEmpty()) {
                viewHolder.nodeImageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(nodePicturePaths.get(position)).into(viewHolder.nodeImageView);
            } else {
                viewHolder.nodeImageView.setVisibility(View.INVISIBLE);
            }
            return row;

        } else {
            viewHolder.distanceTextview.setText(nodeNames.get(position));
            Glide.with(getContext()).load(R.drawable.arrow_down).into(viewHolder.arrowImageview);
            return row;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (nodeNames.get(position).startsWith("\t")) {
            // For distances between Nodes in ListView of RouteFinderActivity
            int DISTANCE_ITEM = 1;
            return DISTANCE_ITEM;
        } else {
            return REGULAR_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        // Two different row types in RouteFinderActivity
        return 2;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return nodeNames.get(position);
    }

    @Override
    public int getCount() {
        return nodeNames.size();
    }

}

