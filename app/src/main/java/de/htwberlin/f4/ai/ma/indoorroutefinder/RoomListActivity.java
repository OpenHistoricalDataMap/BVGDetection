package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
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
            Room room = allRooms.get(position);


            new AlertDialog.Builder(view.getContext())
                    .setTitle(getString(R.string.delete_entry_title_question))
                    .setMessage("Soll der Ort \"" + room.getRoomName() + "\" wirklich gelöscht werden?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        databaseHandler.deleteRoom(room);
                        loadDbData();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
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

            roomNames.add(room.getRoomName());
        }

        Log.d("RoomListActivity", String.valueOf(allRooms));

        roomListAdapter.notifyDataSetChanged();
    }
}

/*
bssid='da:bf:c0:0e:1e:17', rssi=-47, ssid='MicroPython-0e1e17'}, AccessPointInformationImpl{
bssid='dc:b8:08:c9:04:a0', rssi=-54, ssid='eduroam'}, AccessPointInformationImpl{
bssid='dc:b8:08:c9:04:a1', rssi=-54, ssid='HowToUseEduroam'}, AccessPointInformationImpl{
bssid='dc:b8:08:c9:04:a2', rssi=-54, ssid='Gast@HTW'}, AccessPointInformationImpl{
bssid='e4:fa:c4:fc:34:26', rssi=-66, ssid='Rechnernetze'}, AccessPointInformationImpl{
bssid='dc:b8:08:c9:01:b0', rssi=-61, ssid='eduroam'}, AccessPointInformationImpl{
bssid='00:09:9a:00:b6:43', rssi=-78, ssid='ELTX1001901'}, AccessPointInformationImpl{
bssid='dc:b8:08:c8:fe:e2', rssi=-88, ssid='Gast@HTW'}]}]}, roomName='test'}
 */
