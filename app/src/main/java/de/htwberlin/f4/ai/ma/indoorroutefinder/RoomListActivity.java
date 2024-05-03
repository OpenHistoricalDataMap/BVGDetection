package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;


public class RoomListActivity extends BaseActivity {

    List<Room> allRooms = new ArrayList<>();
    List<String> roomNames = new ArrayList<>();

    ListView roomListView;
    DatabaseHandler databaseHandler;
    private ArrayAdapter<String> roomListAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_roomlist));
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_room_list, contentFrameLayout);

        roomListView = findViewById(R.id.roomListListview);

        roomListAdapter = new ArrayAdapter<>(this, R.layout.item_room_listview, roomNames);

        roomListView.setAdapter(roomListAdapter);

        TextView emptyTextView = findViewById(R.id.empty_list_item);
        roomListView.setEmptyView(emptyTextView);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        loadDbData();

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MeasurementsListActivity.class);
                // TODO: Change nodeId to roomDatabaseID
                intent.putExtra("nodeId", allRooms.get(position).getRoomName());
                startActivity(intent);
            }
        });

        roomListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = allRooms.get(position);


                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.delete_entry_title_question))
                        .setMessage("Soll der Ort \"" + room.getRoomName() + "\" wirklich gelöscht werden?")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                databaseHandler.deleteRoom(room);
                                loadDbData();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;


            }
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

        allRooms.clear();
        roomNames.clear();

        allRooms = databaseHandler.getAllRooms();
        for (Room room : allRooms) {
            roomNames.add(room.getRoomName());
        }

        Log.d("RoomListActivity", String.valueOf(allRooms));

        roomListAdapter.notifyDataSetChanged();
    }
}


