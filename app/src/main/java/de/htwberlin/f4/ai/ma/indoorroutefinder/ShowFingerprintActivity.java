package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.ShowFingerprintAdapter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Created by Johann Winter
 */
public class ShowFingerprintActivity extends BaseActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_show_fingerprint, contentFrameLayout);

        DatabaseHandler databaseHandler = DatabaseHandlerFactory.getInstance(this);
        ExpandableListView fingerprintListview = (ExpandableListView) findViewById(R.id.fingerprint_expandable_listview);

        Intent intent = getIntent();
        String nodeID = (String) intent.getExtras().get("nodeID");

        if (nodeID != null) {
            Room room = databaseHandler.getNode(nodeID);
            setTitle(room.getRoomName());
            ShowFingerprintAdapter adapter = new ShowFingerprintAdapter(this, room.getFingerprint());
            fingerprintListview.setAdapter(adapter);

            for (int i = 0; i < room.getFingerprint().getSignalSampleList().size(); i++) {
                fingerprintListview.expandGroup(i);
            }
        }
    }
}

