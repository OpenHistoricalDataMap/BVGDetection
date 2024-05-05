package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.JSON;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;
import de.htwberlin.f4.ai.ma.indoorroutefinder.room.Room;

/**
 * Created by Johann Winter
 * <p>
 * Converts a list of SignalSamples to JSON strings for being stored in the database and
 * JSON strings to a list of SignalSample.
 */
public class JSONConverter {

    public static final String JSON_CONVERTER = "JSONConverter";

    /**
     * Convert a List<SignalSample> to JSON-String (for database storing).
     *
     * @param signalSampleList a list of SignalSamples
     * @return JSON-String containing the signal data for the database
     */
    public String convertSignalSampleListToJSON(List<SignalSample> signalSampleList) {

        JSONObject jsonObject = new JSONObject();
        JSONArray signalJsonArray = new JSONArray();

        if (signalSampleList != null) {
            try {
                for (int i = 0; i < signalSampleList.size(); i++) {

                    JSONObject signalJsonObject = new JSONObject();
                    JSONArray accessPointInfoArray = new JSONArray();

                    for (int j = 0; j < signalSampleList.get(i).getAccessPointInformationList().size(); j++) {

                        JSONObject accessPointInformation = new JSONObject();
                        accessPointInformation.put("bssid", signalSampleList.get(i).getAccessPointInformationList().get(j).getBSSID());
                        accessPointInformation.put("rssi", signalSampleList.get(i).getAccessPointInformationList().get(j).getRSSI());
                        accessPointInformation.put("ssid", signalSampleList.get(i).getAccessPointInformationList().get(j).getSSID());
                        accessPointInfoArray.put(accessPointInformation);
                    }
                    signalJsonObject.put("timestamp", signalSampleList.get(i).getTimestamp());
                    signalJsonObject.put("accessPointInfoList", accessPointInfoArray);
                    signalJsonArray.put(signalJsonObject);
                }
                jsonObject.put("signalSample", signalJsonArray);
            } catch (JSONException e) {
                Log.d(JSON_CONVERTER, e.toString());
            }
        }
        return jsonObject.toString();
    }


    /**
     * Convert JSON-String to a List<SignalSample>
     *
     * @param jsonString the JSON-String from the database
     * @return the list of SignalSamples
     */
    public List<SignalSample> convertJsonToSignalSampleList(String jsonString) {

        List<SignalSample> signalSampleList = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            if (jsonObj.has("signalSample")) {
                JSONArray signalSampleArray = jsonObj.getJSONArray("signalSample");

                for (int j = 0; j < signalSampleArray.length(); j++) {

                    JSONObject jsonSignalSample = signalSampleArray.getJSONObject(j);
                    long timestamp = jsonSignalSample.getLong("timestamp");

                    JSONArray accessPointInfoArray = jsonSignalSample.getJSONArray("accessPointInfoList");
                    List<AccessPointInformation> accessPointInformations = new ArrayList<>();

                    for (int k = 0; k < accessPointInfoArray.length(); k++) {
                        JSONObject accessPointInfo = accessPointInfoArray.getJSONObject(k);
                        String macAddress = accessPointInfo.getString("bssid");
                        int signalStrength = accessPointInfo.getInt("rssi");
                        String ssid = accessPointInfo.getString("ssid");
                        AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(macAddress, signalStrength, ssid);
                        accessPointInformations.add(accessPointInformation);
                    }
                    SignalSample signalSample = new SignalSample(timestamp, accessPointInformations);
                    signalSampleList.add(signalSample);
                }
            }
        } catch (JSONException e) {
            Log.d(JSON_CONVERTER, e.toString());
        }
        return signalSampleList;
    }

    public String convertRoomListToJSONArray(List<Room> rooms) {
        JSONArray nodeJsonArray = new JSONArray();
        try {
            for (Room room : rooms) {

                if (room.getFingerprint().getSignalSampleList().isEmpty()) continue;
                JSONObject jsonObjectNode = new JSONObject();
                String roomName = room.getRoomName();
                long timestamp = room.getFingerprint().getSignalSampleList().get(0).getTimestamp();
                jsonObjectNode.put("room", roomName);
                jsonObjectNode.put("timestamp", timestamp);

                if (room.getFingerprint() != null) {
                    JSONArray signalJsonArray = new JSONArray();
                    for (int i = 0; i < room.getFingerprint().getSignalSampleList().size(); i++) {
                        for (int j = 0; j < room.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().size(); j++) {
                            JSONObject signalJsonObject = new JSONObject();
                            signalJsonObject.put("bssid", room.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().get(j).getBSSID());
                            signalJsonObject.put("ssid", room.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().get(j).getSSID());
                            signalJsonObject.put("level", room.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().get(j).getRSSI());
                            signalJsonArray.put(signalJsonObject);
                        }

                    }
                    jsonObjectNode.put("fingerprint", signalJsonArray);
                }
                nodeJsonArray.put(jsonObjectNode);
            }
        } catch (final JSONException e) {
            Log.d(JSON_CONVERTER, "Error while converting rooms to JSON.");
        }
        return nodeJsonArray.toString();
    }
}
