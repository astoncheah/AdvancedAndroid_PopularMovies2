package com.example.popular_movies.remote;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteEndpointUtil{
    private static final String TAG = "RemoteEndpointUtil";

    private RemoteEndpointUtil() {
    }

    public static JSONArray fetchJsonArray(URL url) {
        String itemsJson = null;
        try {
            itemsJson = fetchPlainText(url);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching items JSON", e);
            return null;
        }
        // Parse JSON
        try {
            //JSONTokener tokener = new JSONTokener(itemsJson);
            //Object val = tokener.nextValue();

            JSONObject jsnobject = new JSONObject(itemsJson);
            JSONArray val = jsnobject.getJSONArray("results");
            if (!(val instanceof JSONArray)) {
                throw new JSONException("Expected JSONArray");
            }
            return val;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing items JSON", e);
        }

        return null;
    }

    static String fetchPlainText(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
