package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence;

import android.content.Context;

/**
 * Created by Johann Winter
 * <p>
 * Factory for creating and retrieving a DatabaseHandler singleton object.
 */
public class DatabaseHandlerFactory {

    private static DatabaseHandler databaseHandler;

    public static DatabaseHandler getInstance(Context context) {
        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandlerImpl(context);
        }
        return databaseHandler;
    }
}
