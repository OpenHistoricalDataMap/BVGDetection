package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.ShowFingerprintAdapter;
import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;
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
            Node node = databaseHandler.getNode(nodeID);
            setTitle(node.getId());
            ShowFingerprintAdapter adapter = new ShowFingerprintAdapter(this, node.getFingerprint());
            fingerprintListview.setAdapter(adapter);

            for (int i = 0; i < node.getFingerprint().getSignalSampleList().size(); i++) {
                fingerprintListview.expandGroup(i);
            }
        }
    }
}

