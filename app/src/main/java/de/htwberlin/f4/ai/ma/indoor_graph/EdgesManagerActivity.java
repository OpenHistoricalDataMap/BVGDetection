package de.htwberlin.f4.ai.ma.indoor_graph;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.carol.bvg.R;

import java.util.ArrayList;

import de.htwberlin.f4.ai.ma.fingerprint_generator.node.Node;
import de.htwberlin.f4.ai.ma.persistence.DatabaseHandler;

/**
 * Created by Johann Winter
 */

public class EdgesManagerActivity extends Activity {

    private Spinner spinnerA;
    private Spinner spinnerB;
    private Button connectButton;
    private ListView edgesListView;
    private ArrayList<Node> allNodes;
    private ArrayList<String> itemsSpinnerA;
    private ArrayList<String> itemsSpinnerB;
    private ArrayList<String> itemsEdgesList;
    private ArrayList<Edge> allEdges;
    private DatabaseHandler databaseHandler;
    private CheckBox accessiblyCheckbox;
    private String lastSelectedItemA;
    private Integer edgesCounter;
    private final String accessiblyString = "barrierefrei";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edges_manager);

        spinnerA = (Spinner) findViewById(R.id.nodeA_spinner);
        spinnerB = (Spinner) findViewById(R.id.nodeB_spinner);
        connectButton = (Button) findViewById(R.id.connect_nodes_button);
        edgesListView = (ListView) findViewById(R.id.edges_listview);
        accessiblyCheckbox = (CheckBox) findViewById(R.id.accessibly_checkbox);

        databaseHandler = new DatabaseHandler(this);

        itemsSpinnerA = new ArrayList<>();
        itemsSpinnerB = new ArrayList<>();
        itemsEdgesList = new ArrayList<>();
        allEdges = new ArrayList<>();
        lastSelectedItemA = "";


        final SharedPreferences sharedPreferences = this.getSharedPreferences(
                getPackageName(), this.MODE_PRIVATE);

        edgesCounter = sharedPreferences.getInt("edgesCounter", -1);
        if (edgesCounter.equals(-1)) {
            edgesCounter = 1;
        }

        allNodes = databaseHandler.getAllNodes();

        for (de.htwberlin.f4.ai.ma.fingerprint_generator.node.Node node : allNodes) {
            itemsSpinnerA.add(node.getId());
            itemsSpinnerB.add(node.getId());
        }

        final ArrayAdapter<String> adapterA = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsSpinnerA);
        spinnerA.setAdapter(adapterA);

        final ArrayAdapter<String> adapterB = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsSpinnerB);
        spinnerB.setAdapter(adapterB);

        final ArrayAdapter<String> edgesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itemsEdgesList);
        edgesListView.setAdapter(edgesListAdapter);


        // Exclude on spinnerA selected item on spinnerB
        spinnerA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {

                String selectedA = spinnerA.getSelectedItem().toString();

                if (selectedA != lastSelectedItemA) {
                    if (!lastSelectedItemA.equals("")) {
                        itemsSpinnerB.add(lastSelectedItemA);
                    }
                    itemsSpinnerB.remove(selectedA);
                    adapterB.notifyDataSetChanged();
                    lastSelectedItemA = selectedA;
                }
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });


        // Save new edge
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean accessibly = false;
                if (accessiblyCheckbox.isChecked()) { accessibly = true; }

                // TODO ID!!!
                Edge edge = new EdgeImplementation(edgesCounter, spinnerA.getSelectedItem().toString(), spinnerB.getSelectedItem().toString(), accessibly );

                databaseHandler.insertEdge(edge);

                if (accessibly) {
                    itemsEdgesList.add(edge.getNodeA() + " ---> " + edge.getNodeB() + "        " + accessiblyString);
                } else {
                    itemsEdgesList.add(edge.getNodeA() + " ---> " + edge.getNodeB());
                }
                edgesListAdapter.notifyDataSetChanged();

                edgesCounter++;
                System.out.println("EdgesCounter: " + edgesCounter);
                sharedPreferences.edit().putInt("edgesCounter", edgesCounter).apply();

            }
        });


        // Load edges list
        for (Edge e : databaseHandler.getAllEdges()) {
            allEdges.add(e);
            if (e.getAccessibly()) {
                itemsEdgesList.add(e.getNodeA() + " ---> " + e.getNodeB() + "        " + accessiblyString);
            } else {
                itemsEdgesList.add(e.getNodeA() + " ---> " + e.getNodeB());
            }
        }


        // Long click on item -> delete item
        edgesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Eintrag löschen")
                        .setMessage("Möchten sie den Eintrag löschen?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                databaseHandler.deleteEdge(allEdges.get(position));
                                edgesListAdapter.remove(itemsEdgesList.get(position));
                                edgesListAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }
        });



    }
}