package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;

/**
 * Created by Johann Winter
 * <p>
 * Provides functions for file management (creating and copying).
 */
public class FileUtilities {

    /**
     * Copies a File from one to another path.
     *
     * @param fromFile FileInputStream for the file to copy from.
     * @param toFile   FileInputStream for the file to copy to.
     */
    static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }


    /**
     * Creates the folder /IndoorPositioning/Pictures in SDCARD path,
     * then creates a new imagefile.
     *
     * @param name the filename
     * @return File object of the image file
     */
    public static File getFile(String name, Timestamp timestamp) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File folder = new File(externalStorageDirectory.getAbsolutePath() + "/IndoorPositioning/Pictures");
        long realTimestamp = timestamp.getTime();

        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (!success) {
                Log.d("FileUtilities", "Der Ordner konnte nicht angelegt werden");
            }
        }
        return new File(folder, name + "_" + realTimestamp + ".jpg");
    }
}