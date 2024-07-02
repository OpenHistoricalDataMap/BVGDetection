package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    public static final String ROOM_LIST_ACTIVITY = "RoomListActivity";
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

        roomListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), MeasurementsListActivity.class);
            // TODO: Change nodeId to roomDatabaseID
            intent.putExtra("roomID", allRooms.get(position).getRoomName());
            Log.d(ROOM_LIST_ACTIVITY, "Room ID: " + allRooms.get(position).getRoomName());
            startActivity(intent);
        });

        roomListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), NodeRecordEditActivity.class);
            intent.putExtra("nodeId", allRooms.get(position).getRoomName());
            Log.d("nodeId", allRooms.get(position).getRoomName());
            startActivity(intent);

            return true;
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
            int numberOfFingerprints = room.getFingerprint().getSignalSampleList().size();

            roomNames.add(room.getRoomName() + " (" + numberOfFingerprints + ")");
        }

        Log.d("RoomListActivity", String.valueOf(allRooms));

        roomListAdapter.notifyDataSetChanged();
    }
}