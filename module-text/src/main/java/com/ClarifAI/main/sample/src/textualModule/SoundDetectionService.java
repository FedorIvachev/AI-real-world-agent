package com.ClarifAI.main.sample.src.textualModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SoundDetectionService {


    public static final List<SoundEvent> soundEvents = new ArrayList<>();

    public static String getSoundsFromLastTenMinutes() {
        StringBuilder sb = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        Iterator<SoundEvent> iterator = soundEvents.iterator();
        while (iterator.hasNext()) {
            SoundEvent event = iterator.next();
            if (currentTime - event.timestamp <= 60000) { // get the sounds happened last minute
                String formattedTime = sdf.format(new Date(event.timestamp));
                sb.append("Sound: ").append(event.soundType)
                        .append(", at ").append(formattedTime)
                        .append("\n");
            } else {
                iterator.remove();
            }
        }

        return sb.toString();
    }


    private static class SoundEvent {
        static String soundType;
        static long timestamp;

        SoundEvent(String soundType, long timestamp) {
            this.soundType = soundType;
            this.timestamp = timestamp;
        }
    }


    public static void AddSound(String detectedSound) {
        soundEvents.add(new SoundEvent(detectedSound, System.currentTimeMillis()));
    }


}
