package com.ClarifAI.main.sample.src.textualModule;

import android.util.Log;

import com.google.gson.Gson;
import com.ClarifAI.main.sample.models.ReverseGeocodingResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeocodingService {
    public static final String ROOT_URL = "https://siteapi.cloud.huawei.com/mapApi/v1/siteService/reverseGeocode";

    public static final String connection = "?key=";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void reverseGeocoding(String apiKey, double latitude, double longitude, ReverseGeocodingCallback callback) throws UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        JSONObject location = new JSONObject();
        try {
            location.put("lng", longitude);
            location.put("lat", latitude);
            json.put("location", location);
            json.put("radius", 10);
        } catch (JSONException e) {
            Log.e("error", e.getMessage());
        }
        RequestBody body = RequestBody.create(JSON, String.valueOf(json));

        OkHttpClient client = new OkHttpClient();
        Request request =
                new Request.Builder().url(ROOT_URL + connection + URLEncoder.encode(apiKey, "UTF-8"))
                        .post(body)
                        .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ReverseGeocoding", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("ReverseGeocoding", responseBody);
                Gson gson = new Gson();
                ReverseGeocodingResponseBody parsedResponse = gson.fromJson(responseBody, ReverseGeocodingResponseBody.class);

                callback.onResult(parsedResponse);
            }
        });
    }

    public interface ReverseGeocodingCallback {
        void onResult(ReverseGeocodingResponseBody result);

    }

}

