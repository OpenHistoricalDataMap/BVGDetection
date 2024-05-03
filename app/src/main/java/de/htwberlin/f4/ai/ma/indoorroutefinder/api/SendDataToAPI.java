package de.htwberlin.f4.ai.ma.indoorroutefinder.api;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDataToAPI extends AsyncTask<Void, Void, String> {

    private static final String BASE_URL = "http://141.45.212.246:5000";
    private static final String TAG = "SendDataToAPI";

    private final ApiResponseListener responseListener;
    private final Endpoint endpoint;
    private final String jsonData;
    private final RequestMethod method;

    public SendDataToAPI(Endpoint endpoint, String jsonData, RequestMethod method, ApiResponseListener listener) {
        this.endpoint = endpoint;
        this.jsonData = jsonData;
        this.method = method;
        this.responseListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String response = null;

        try {
            URL url = new URL(BASE_URL + endpoint.getPath());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method.toString());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            if (jsonData != null && !jsonData.isEmpty()) {
                OutputStream os = conn.getOutputStream();
                os.write(jsonData.getBytes());
                os.flush();
                os.close();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();
            } else {
                response = "Error: " + responseCode;
                Log.e(TAG, "Error Response: " + response);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            response = "Exception: " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing BufferedReader: " + e.getMessage());
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        if (responseListener != null) {
            responseListener.onApiResponse(result);
        }
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    public enum Endpoint {
        ADD_MEASUREMENTS("/add_measurements"),
        GET_MEASUREMENTS("/get_measurements"),
        DELETE_MEASUREMENT("/delete_measurement"),
        UPDATE_MEASUREMENT("/update_measurement");

        private final String path;

        Endpoint(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public interface ApiResponseListener {
        void onApiResponse(String response);
    }
}
