package com.ClarifAI.main.sample.src.textualModule;

import com.huawei.hms.mlsdk.classification.MLImageClassification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class MLClassificationManager {

    private static final MLClassificationManager instance = new MLClassificationManager();

    // Store the classifications
    private final Map<String, MLImageClassification> classificationsMap = new HashMap<>();

    // Store the age of each classification in frames
    private final Map<String, Integer> classificationAgeMap = new HashMap<>();

    // Add this map to keep track of frames where possibility > 0.9
    private final Map<String, Integer> highProbFrameCountMap = new HashMap<>();


    // Method to register a callback
    private ClassificationCallback callback;

    public void setClassificationCallback(ClassificationCallback callback) {
        this.callback = callback;
    }

    private MLClassificationManager() {
    }

    public static MLClassificationManager getInstance() {
        return instance;
    }

    public void updateClassifications(List<MLImageClassification> newClassifications) {
        // Increment age for all stored classifications by one frame
        for (String name : classificationAgeMap.keySet()) {
            classificationAgeMap.put(name, classificationAgeMap.get(name) + 1);
        }

        for (MLImageClassification classification : newClassifications) {

            if (classification.getPossibility() > 0.7) {
                // If probability > 0.7, increase counter
                highProbFrameCountMap.put(classification.getName(), highProbFrameCountMap.getOrDefault(classification.getName(), 0) + 1);

                // Check if the frame count reached 300
                if (highProbFrameCountMap.get(classification.getName()) >= 300) {
                    // Reset the counter
                    highProbFrameCountMap.put(classification.getName(), 0);

                    // Trigger the callback
                    if (callback != null) {
                        callback.onClassificationStable(classification.getName());
                    }
                }
            } else {
                // Reset the counter if probability drops below 0.9
                highProbFrameCountMap.put(classification.getName(), 0);
            }

            if (classification.getPossibility() > 0.5) {
                MLImageClassification existing = classificationsMap.get(classification.getName());

                if (existing == null || classification.getPossibility() > existing.getPossibility()) {
                    classificationsMap.put(classification.getName(), classification);
                    classificationAgeMap.put(classification.getName(), 0);  // reset or set age to 0 for this classification
                }
            }
        }

        // Remove classifications that are older than 100 frames
        Iterator<Map.Entry<String, Integer>> ageIterator = classificationAgeMap.entrySet().iterator();
        while (ageIterator.hasNext()) {
            Map.Entry<String, Integer> entry = ageIterator.next();
            if (entry.getValue() >= 600) {
                classificationsMap.remove(entry.getKey());
                ageIterator.remove();  // remove from classificationAgeMap
            }
        }
    }

    public List<MLImageClassification> getClassifications() {
        return new ArrayList<>(classificationsMap.values());
    }

    public String getClassificationsAsString() {
        return classificationsMap.values().stream()
                .map(classification -> classification.getName() + ": " + classification.getPossibility())
                .collect(Collectors.joining(", "));
    }

    public String getClassificationsLabelsAsString() {
        return classificationsMap.values().stream()
                .map(classification -> classification.getName())
                .collect(Collectors.joining(", "));
    }

    public String getRandomClassificationName() {
        // Convert the keys of the classificationsMap to a List
        List<String> keys = new ArrayList<>(classificationsMap.keySet());

        // Check if the list is empty
        if (keys.isEmpty()) {
            return null;  // or throw an exception if preferred
        }

        // Generate a random index
        int randomIndex = new Random().nextInt(keys.size());

        // Return the key at the random index
        return keys.get(randomIndex);
    }

    public String getClassificationsInCustomFormat() {
        List<MLImageClassification> classifications = new ArrayList<>(classificationsMap.values());

        // Filter and collect classifications based on their probabilities
        List<MLImageClassification> highProbClassifications = classifications.stream()
                .filter(c -> c.getPossibility() > 0.8)
                .collect(Collectors.toList());

        List<MLImageClassification> maybeClassifications = classifications.stream()
                .filter(c -> c.getPossibility() >= 0.5 && c.getPossibility() <= 0.8)
                .collect(Collectors.toList());

        // Convert remaining highProbClassifications to comma-separated string
        String otherObjects = highProbClassifications.stream()
                .map(MLImageClassification::getName)
                .collect(Collectors.joining(", "));

        // Convert maybeClassifications to comma-separated string
        String maybeObjects = maybeClassifications.stream()
                .map(MLImageClassification::getName)
                .collect(Collectors.joining(", "));

        StringBuilder result = new StringBuilder();

        if (!otherObjects.isEmpty()) {
            if (result.length() > 0) {
                result.append(". ");
            }
            result.append("I probably see: ").append(otherObjects);
        }

        if (!maybeObjects.isEmpty()) {
            if (result.length() > 0) {
                result.append(". ");
            }
            result.append("Not sure, but there is maybe ").append(maybeObjects);
        }

        return result.toString();
    }

    // Callback interface for your new requirement
    public interface ClassificationCallback {
        void onClassificationStable(String label);
    }




}
