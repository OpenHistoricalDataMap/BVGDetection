package de.htwberlin.f4.ai.ma.indoorroutefinder.decentralized;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.Fingerprint;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.FingerprintFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.DatabaseHandler;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.RoomFactory;

public class BluetoothHelper {

    /**
     * Processes received Bluetooth messages.
     *
     * @param results         The list of received messages.
     * @param databaseHandler The database handler.
     * @param context         The context.
     * @return The count of new nodes added to the database.
     */
    public static int processReceivedMessages(List<String> results, DatabaseHandler databaseHandler, Context context) {
        int countNewNodes = 0;

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).endsWith("_MESSAGE_END_")) {
                results.set(i, results.get(i).substring(0, results.get(i).length() - "_MESSAGE_END_".length()));
            }
        }

        for (String result : results) {
            if (result.isEmpty()) continue;
            try {
                JSONObject json = new JSONObject(result);
                List<AccessPointInformation> accessPointInformationList = new ArrayList<>();
                JSONArray scan = new JSONArray(json.getString("routers"));
                for (int i = 0; i < scan.length(); i++) {
                    JSONObject scanEntry = scan.getJSONObject(i);
                    String SSID = scanEntry.getString("ssid");
                    String BSSID = scanEntry.getString("bssid");
                    int level = scanEntry.getInt("signal_strength");
                    AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(BSSID, level, SSID);
                    accessPointInformationList.add(accessPointInformation);
                }

                SignalSample signalSample = new SignalSample(json.getLong("timestamp"), accessPointInformationList);
                List<SignalSample> signalSampleList = new ArrayList<>();
                signalSampleList.add(signalSample);
                Fingerprint fingerprint = FingerprintFactory.createInstance(signalSampleList);
                fingerprint.setDeviceID(json.getString("device_id"));
                Room room = RoomFactory.createInstance(json.getString("room_name"), "", fingerprint, "", "", "");
                countNewNodes += databaseHandler.insertOrUpdateRoom(room);
            } catch (Exception ignored) {
            }
        }

        return countNewNodes;
    }

    /**
     * Processes the API response.
     *
     * @param responseArray   The response array from the API.
     * @param databaseHandler The database handler.
     * @param uiHandler       The UI handler.
     * @param statusTextView  The status text view.
     */
    public static void processApiResponse(JSONArray responseArray, DatabaseHandler databaseHandler, Handler uiHandler, TextView statusTextView) {
        for (int i = 0; i < responseArray.length(); i++) {
            final int index = i;
            uiHandler.post(() -> statusTextView.setText("Added " + (index + 1) + "/" + responseArray.length() + " Fingerprints from the API."));

            try {
                List<AccessPointInformation> accessPointInformationList = new ArrayList<>();
                JSONArray scan = new JSONArray(responseArray.getJSONObject(i).getString("routers"));
                for (int j = 0; j < scan.length(); j++) {
                    JSONObject scanEntry = scan.getJSONObject(j);
                    String SSID = scanEntry.getString("ssid");
                    String BSSID = scanEntry.getString("bssid");
                    int level = scanEntry.getInt("signal_strength");
                    AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(BSSID, level, SSID);
                    accessPointInformationList.add(accessPointInformation);
                }

                SignalSample signalSample = new SignalSample(responseArray.getJSONObject(i).getLong("timestamp"), accessPointInformationList);
                List<SignalSample> signalSampleList = new ArrayList<>();
                signalSampleList.add(signalSample);
                Fingerprint fingerprint = FingerprintFactory.createInstance(signalSampleList);
                fingerprint.setDeviceID(responseArray.getJSONObject(i).getString("device_id"));
                Room room = RoomFactory.createInstance(responseArray.getJSONObject(i).getString("room_name"), "", fingerprint, "", "", "");
                databaseHandler.insertOrUpdateRoom(room);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
