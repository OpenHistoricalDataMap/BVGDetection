package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.JSON;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;


/**
 * Created by Johann Winter
 * <p>
 * Thanks to Carola Walter
 * <p>
 * Saves new nodes to a JSON file on external storage.
 * Path: "/IndoorPositioning/JSON/jsonFile.txt"
 */
public class JSONWriter {

    public static final String JSON_WRITER = "JSONWriter";

    /**
     * Write a JSON object to the JSON file on the external device storage
     *
     * @param node the node to save
     */
    public void writeJSON(Node node) {
        String jsonString = loadJSONFromAsset();
        String nodeId = node.getId();

        boolean idIsContained = false;

        if (jsonString != null) {
            try {
                int index = 0;
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONArray jsonNode = jsonObj.getJSONArray("Node");
                for (int i = 0; i < jsonNode.length(); i++) {
                    JSONObject jsonObjectNode = jsonNode.getJSONObject(i);
                    if (jsonObjectNode.length() > 0) {
                        String id = jsonObjectNode.getString("id");
                        if (id.equals(nodeId)) {
                            index = i;
                            idIsContained = true;
                        }
                    }
                }
                if (idIsContained) {
                    JSONObject newJsonObject = jsonNode.getJSONObject(index);

                    if (newJsonObject.has("fingerprint")) {
                        JSONArray jsonArray = newJsonObject.getJSONArray("fingerprint");
                        JSONArray jsonArrayAdd = makeJsonNode(newJsonObject, node).getJSONArray("fingerprint");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonArrayAdd.put(jsonArray.getJSONObject(i));
                        }
                        newJsonObject.put("fingerprint", jsonArrayAdd);
                    }
                    save(jsonObj);
                } else {
                    JSONObject jsonObjectNode = new JSONObject();
                    jsonNode.put(makeJsonNode(jsonObjectNode, node));

                    save(jsonObj);
                }
            } catch (final JSONException e) {
                Log.e(JSON_WRITER, "Json parsing error: " + e.getMessage());
            }
        }
    }

    /**
     * Create a new JSON object containing all information from node to save
     *
     * @param jsonObjectNode the old JSON object
     * @param node           the node to save
     * @return the new JSON object
     */
    private JSONObject makeJsonNode(JSONObject jsonObjectNode, Node node) {
        try {
            jsonObjectNode.put("id", node.getId());
            jsonObjectNode.put("description", node.getDescription());
            jsonObjectNode.put("coordinates", node.getCoordinates());
            jsonObjectNode.put("picturePath", node.getPicturePath());
            jsonObjectNode.put("additionalInfo", node.getAdditionalInfo());

            if (node.getFingerprint() != null) {
                JSONArray signalJsonArray = new JSONArray();
                for (int i = 0; i < node.getFingerprint().getSignalSampleList().size(); i++) {

                    JSONObject signalJsonObject = new JSONObject();
                    JSONArray apInfoJsonArray = new JSONArray();

                    for (int j = 0; j < node.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().size(); j++) {
                        JSONObject signalStrengthObject = new JSONObject();
                        signalStrengthObject.put("macAddress", node.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().get(j).getMacAddress());
                        signalStrengthObject.put("strength", node.getFingerprint().getSignalSampleList().get(i).getAccessPointInformationList().get(j).getRssi());
                        apInfoJsonArray.put(signalStrengthObject);
                    }
                    signalJsonObject.put("timestamp", node.getFingerprint().getSignalSampleList().get(i).getTimestamp());
                    signalJsonObject.put("signalSample", apInfoJsonArray);
                    signalJsonArray.put(signalJsonObject);
                }
                jsonObjectNode.put("fingerprint", signalJsonArray);
            }
        } catch (final JSONException e) {
            Log.e(JSON_WRITER, "parsing Error");
        }
        return jsonObjectNode;

    }

    /**
     * Save the new JSONString to file
     *
     * @param jsonObject the new json String
     */
    public void save(JSONObject jsonObject) {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/IndoorPositioning/JSON");
        dir.mkdirs();
        File file = new File(dir, "jsonFile.txt");
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.d(JSON_WRITER, e.toString());
        }
    }


    /**
     * If file exists, load .txt file from Files folder and return its content as a JSON String.
     * If no file exists, create an empty JSON String
     *
     * @return json String
     */
    private String loadJSONFromAsset() {
        String json = null;
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/IndoorPositioning/JSON");
            dir.mkdirs();
            File file = new File(dir, "jsonFile.txt");
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
            } else {
                json = "{Node: []}";
            }

        } catch (IOException ex) {
            Log.d(JSON_WRITER, ex.toString());
            return null;
        }
        return json;
    }
}
