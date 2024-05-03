package de.htwberlin.f4.ai.ma.indoorroutefinder;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.htwberlin.f4.ai.ma.indoorroutefinder.android.BaseActivity;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandlerFactory;

/**
 * Created by Johann Winter
 * <p>
 * Activity for import / export of the database
 */
public class ImportExportActivity extends BaseActivity {

    public static final String IMPORT_EXPORT_ACTIVITY = "ImportExportActivity";
    ImageButton importButton;
    ImageButton exportButton;
    private DatabaseHandler databaseHandler;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.title_activity_importexport));

        final FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_import_export, contentFrameLayout);

        context = getApplicationContext();

        importButton = (ImageButton) findViewById(R.id.import_button);
        exportButton = (ImageButton) findViewById(R.id.export_button);

        databaseHandler = DatabaseHandlerFactory.getInstance(this);

        importButton.setImageResource(R.drawable.import_icon);
        exportButton.setImageResource(R.drawable.export_icon);

        importButton.setOnClickListener(view -> importDatabase());
        exportButton.setOnClickListener(view -> exportDatabase());
    }


    /**
     * Ask the user if he or she wants to overwrite the application's database
     * and then copy the file "/IndoorPositioning/Exported/indoor_data.db" over the internal database.
     */
    private void importDatabase() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.import_title_question))
                .setMessage(getString(R.string.import_database_warining))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        boolean importSuccessful = DatabaseHandlerFactory.getInstance(context).importDatabase();
                        if (importSuccessful) {
                            Toast.makeText(context, getString(R.string.database_imported_toast), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        Log.d(IMPORT_EXPORT_ACTIVITY, "Error importing database: " + e);
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /**
     * If already existent, ask the user if he wants to overwrite the exported database file "/IndoorPositioning/Exported/indoor_data.db".
     * If the user agrees, or the file is non-existent, it will be overwritten by the database dump which
     * will be created from the application's database.
     */
    private void exportDatabase() {
        File exportFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/IndoorPositioning/Exported/indoor_data.db");

        if (exportFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.export_title_question))
                    .setMessage(getString(R.string.export_database_warning))
                    .setCancelable(true)
                    .setPositiveButton(getString(android.R.string.yes), (dialog, which) -> {
                        boolean exportSuccessful = databaseHandler.exportDatabase();
                        if (exportSuccessful) {
                            Toast.makeText(context, getString(R.string.database_exported_toast), Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            boolean exportSuccessful = databaseHandler.exportDatabase();
            if (exportSuccessful) {
                Toast.makeText(context, getString(R.string.database_exported_toast), Toast.LENGTH_LONG).show();
            }
        }
    }

}
