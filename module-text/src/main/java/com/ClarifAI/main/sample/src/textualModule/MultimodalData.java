package com.ClarifAI.main.sample.src.textualModule;

import com.ClarifAI.main.sample.src.LLMInteractionModule.ChatMessageStorage;
import com.ClarifAI.main.sample.models.ReverseGeocodingResponseBody;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MultimodalData {
    public static GPSSite site = new GPSSite();

    public static double longitude;
    public static double latitude;
    public static String selectedObjectClassifications;

    public static float lightIntensity;

    public static String IMUBehavior;

    public static String timeCategory;

    public static String headsetStatus;


    // and so on

    // multimodalData should handle its updates

    public static void getClosestSite(StringCallback callback) {
        try {
            GeocodingService.reverseGeocoding("", latitude, longitude, new GeocodingService.ReverseGeocodingCallback() {
                @Override
                public void onResult(ReverseGeocodingResponseBody result) {
                    if (result.sites != null && result.sites.size() > 0) {
                        site.siteName = result.sites.get(0).name;

                        callback.onResult(result.sites.get(0).name);
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAllInfo() {
        String info = "";
        // remove location since it returns Chinese
        if (!Objects.equals(site.siteName, "")) info += "I am at location named: " + site.siteName;
        if (SoundDetectionService.getSoundsFromLastTenMinutes() != "") {
            info += ". I probably heard: " + SoundDetectionService.getSoundsFromLastTenMinutes();
        }
        if (IMUBehavior != null && !IMUBehavior.equals("unknown")) info += ". I am currently " + IMUBehavior;

        if (timeCategory != null) info += ". " + timeCategory;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        info += ". It is " + sdf.format(new Date(System.currentTimeMillis())) + " now";

        if (headsetStatus != null) info += ". " + headsetStatus;

        // ToDo: Add VLM output here

        info += "." + MLClassificationManager.getInstance().getClassificationsInCustomFormat();

        info +=  ". Conversation with user was: " + ChatMessageStorage.getInstance().getAllMessagesAsString();

        return info;

    }
}

