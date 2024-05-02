package de.htwberlin.f4.ai.ma.indoorroutefinder.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import de.htwberlin.f4.ai.ma.indoorroutefinder.R;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.calibrate.CalibrateViewImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.MeasureViewImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.measure.edges.EdgeDetailsViewImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.android.record.RecordViewImpl;
import de.htwberlin.f4.ai.ma.indoorroutefinder.EdgesManagerActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.LocationActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.RouteFinderActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.NodeListActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.ImportExportActivity;
//import de.htwberlin.f4.ai.ma.indoorroutefinder.NodeRecordActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.NodeRecordEditActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.SettingsActivity;

/**
 * BasicActivity Class
 *
 * All Activities inherit from this class. That's required because of the NavigationDrawer
 */

public class BaseActivity extends AppCompatActivity {

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    // for passing start and targetnode to our edgedetails activity
    protected static final String STARTNODE_KEY = "startnode";
    protected static final String TARGETNODE_KEY = "targetnode";
    protected static final String EDGE_DETAILS_BUNDLE = "nodeinfos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.nav_location_record:
                        loadNodeRecordEdit();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_location_manager:
                        loadNodelist();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_edges_manager:
                        loadEdgesManager();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_location_detect:
                        loadLocation();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_route_finder:
                        loadRouteFinder();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_import:
                        loadImportExport();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_settings:
                        loadSettings();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_measurement:
                        loadMeasurement();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_calibrate:
                        loadCalibrate();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_record:
                        loadRecord();
                        drawerLayout.closeDrawers();
                        break;

                    default:
                        break;

                }
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    public void loadNodeRecordEdit() {
        Intent intent = new Intent(getApplicationContext(), NodeRecordEditActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadNodelist() {
        Intent intent = new Intent(getApplicationContext(), NodeListActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadEdgesManager() {
        Intent intent = new Intent(getApplicationContext(), EdgesManagerActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadLocation() {
        Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadRouteFinder() {
        Intent intent = new Intent(getApplicationContext(), RouteFinderActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadImportExport() {
        Intent intent = new Intent(getApplicationContext(), ImportExportActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        finish();
        startActivity(intent);
    }

    public void loadMeasurement() {
        Intent intent = new Intent(getApplicationContext(), MeasureViewImpl.class);
        finish();
        startActivity(intent);
    }

    public void loadCalibrate() {
        Intent intent = new Intent(getApplicationContext(), CalibrateViewImpl.class);
        finish();
        startActivity(intent);
    }

    public void loadRecord() {
        Intent intent = new Intent(getApplicationContext(), RecordViewImpl.class);
        finish();
        startActivity(intent);
    }

    public void loadEdgeDetails(String startNodeID, String targetNodeID) {
        Bundle bundle = new Bundle();
        bundle.putString(STARTNODE_KEY, startNodeID);
        bundle.putString(TARGETNODE_KEY, targetNodeID);

        Intent intent = new Intent(getApplicationContext(), EdgeDetailsViewImpl.class);
        intent.putExtra(EDGE_DETAILS_BUNDLE, bundle);
        startActivity(intent);
    }
}
