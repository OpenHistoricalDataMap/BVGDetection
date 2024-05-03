package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.Edge;
import de.htwberlin.f4.ai.ma.indoorroutefinder.edge.EdgeFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Created by Johann Winter
 * <p>
 * This activity is used to manage (create and delete) edges ("Wege verwalten").
 */
public class EdgesManagerActivity extends BaseActivity {

    private Spinner spinnerA;
    private Spinner spinnerB;
    private ImageButton connectNodesButton;
    private List<String> itemsSpinnerB;
    private List<String> itemsEdgesList;
    private List<Edge> allEdges;
    private DatabaseHandler databaseHandler;
    private CheckBox accessibilityCheckbox;
    private String lastSelectedItemA;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_edgesmanager));
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_edges_manager, contentFrameLayout);

        spinnerA = (Spinner) findViewById(R.id.nodeA_spinner);
        spinnerB = (Spinner) findViewById(R.id.nodeB_spinner);
        connectNodesButton = (ImageButton) findViewById(R.id.connect_nodes_imagebutton);
        ListView edgesListView = (ListView) findViewById(R.id.edges_listview);
        accessibilityCheckbox = (CheckBox) findViewById(R.id.accessibility_checkbox);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        connectNodesButton.setImageResource(R.drawable.ways);

        List<String> itemsSpinnerA = new ArrayList<>();
        itemsSpinnerB = new ArrayList<>();
        itemsEdgesList = new ArrayList<>();
        allEdges = new ArrayList<>();
        lastSelectedItemA = "";

        List<Node> allNodes = databaseHandler.getAllNodes();

        // Fill the spinners with Nodes
        for (Node node : allNodes) {
            itemsSpinnerA.add(node.getId());
            itemsSpinnerB.add(node.getId());
        }

        // Disable connect-button if spinnerB has no elements (spinnerA has one or less elements)
        if (itemsSpinnerA.size() < 2) {
            connectNodesButton.setEnabled(false);
        }

        // Set adapters to spinners and the Edges-list
        final ArrayAdapter<String> adapterA = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsSpinnerA);
        spinnerA.setAdapter(adapterA);

        final ArrayAdapter<String> adapterB = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsSpinnerB);
        spinnerB.setAdapter(adapterB);

        final ArrayAdapter<String> edgesListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsEdgesList);
        edgesListView.setAdapter(edgesListAdapter);


        // Exclude on spinnerA selected item on spinnerB
        spinnerA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {

                String selectedA = spinnerA.getSelectedItem().toString();

                if (!selectedA.equals(lastSelectedItemA)) {
                    if (!lastSelectedItemA.isEmpty()) {
                        itemsSpinnerB.add(lastSelectedItemA);
                    }
                    itemsSpinnerB.remove(selectedA);
                    adapterB.notifyDataSetChanged();
                    lastSelectedItemA = selectedA;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        // Load edges list
        for (Edge e : databaseHandler.getAllEdges()) {
            allEdges.add(e);
            if (e.getAccessibility()) {
                itemsEdgesList.add(e.getNodeA().getId() + " <---> " + e.getNodeB().getId() + ",     " + getString(R.string.accessibility_checkbox_text));
            } else {
                itemsEdgesList.add(e.getNodeA().getId() + " <---> " + e.getNodeB().getId());
            }
        }


        // Save new edge
        connectNodesButton.setOnClickListener(view -> {
            connectNodesButton.setImageResource(R.drawable.ways_inactive);
            boolean accessible = accessibilityCheckbox.isChecked();

            Node nodeA = databaseHandler.getNode(spinnerA.getSelectedItem().toString());
            Node nodeB = databaseHandler.getNode(spinnerB.getSelectedItem().toString());

            Edge edge = EdgeFactory.createInstance(nodeA, nodeB, accessible, 0);

            if (databaseHandler.checkIfEdgeExists(edge)) {
                Toast.makeText(getApplicationContext(), getString(R.string.edge_already_exists), Toast.LENGTH_SHORT).show();
            } else {
                databaseHandler.insertEdge(edge);
                allEdges.add(edge);

                if (accessible) {
                    itemsEdgesList.add(edge.getNodeA().getId() + " <---> " + edge.getNodeB().getId() + ",     " + getString(R.string.accessibility_checkbox_text));
                } else {
                    itemsEdgesList.add(edge.getNodeA().getId() + " <---> " + edge.getNodeB().getId());
                }
                edgesListAdapter.notifyDataSetChanged();
            }
            connectNodesButton.setImageResource(R.drawable.ways);
        });


        // Long click on item -> delete item
        edgesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle(getString(R.string.delete_entry_title_question))
                    .setMessage(getString(R.string.delete_entry_question))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        databaseHandler.deleteEdge(allEdges.get(position));
                        allEdges.remove(position);
                        itemsEdgesList.remove(position);
                        edgesListAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        });
    }
}
