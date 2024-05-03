package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.nodelist.NodeListAdapter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;


/**
 * Created by Johann Winter
 * <p>
 * This activity shows a list of all nodes from the database.
 */
public class NodeListActivity extends BaseActivity {

    ListView nodeListView;
    ArrayList<String> nodeNames;
    ArrayList<String> nodeDescriptions;
    ArrayList<String> nodePicturePaths;
    ArrayList<Node> allNodes;
    NodeListAdapter nodeListAdapter;
    DatabaseHandler databaseHandler;

    boolean nodeListIsEmpty = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_nodelist));
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_nodelist, contentFrameLayout);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        nodeListView = (ListView) findViewById(R.id.nodeListListview);

        allNodes = new ArrayList<>();
        nodeNames = new ArrayList<>();
        nodeDescriptions = new ArrayList<>();
        nodePicturePaths = new ArrayList<>();

        nodeListAdapter = new NodeListAdapter(this, nodeNames, nodeDescriptions, nodePicturePaths);
        nodeListView.setAdapter(nodeListAdapter);

        loadDbData();

        // Click on Item -> show Node in NodeEditActivity
        nodeListView.setOnItemClickListener((parent, view, position, id) -> {
            if (!nodeListIsEmpty) {
                Intent intent = new Intent(getApplicationContext(), NodeRecordEditActivity.class);
                intent.putExtra("nodeId", nodeListView.getAdapter().getItem(position).toString());
                startActivity(intent);
            }
        });


        // Long click on Node -> delete dialog
        nodeListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!nodeListIsEmpty) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.delete_entry_title_question))
                        .setMessage("Soll der Ort \"" + allNodes.get(position).getId() + "\" wirklich gelöscht werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                            if (allNodes.get(position).getPicturePath() != null) {
                                File imageFile = new File(allNodes.get(position).getPicturePath());
                                imageFile.delete();
                            }
                            databaseHandler.deleteNode(allNodes.get(position));
                            loadDbData();
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
            return false;
        });
    }


    /**
     * Refresh the list if the user comes back to this Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDbData();
    }


    /**
     * Clear and reload the nodelist
     */
    private void loadDbData() {

        nodeDescriptions.clear();
        nodeNames.clear();
        nodePicturePaths.clear();
        allNodes.clear();

        allNodes.addAll(databaseHandler.getAllNodes());

        // If no node is available
        if (allNodes.isEmpty()) {
            nodeListIsEmpty = true;
            nodeNames.add(0, "Keine gespeicherten Orte.");
            nodeDescriptions.add("");
            nodePicturePaths.add("");

        } else {
            for (Node n : allNodes) {
                nodeNames.add(n.getId());
                nodeDescriptions.add(n.getDescription());
                nodePicturePaths.add(n.getPicturePath());
            }
        }
        nodeListAdapter.notifyDataSetChanged();
    }
}
