package com.ClarifAI.main.sample.src.textualModule;

public class DetectionService {




    private static StringCallback detectionCallback;

    public static void abnormalBehaviorDetected(String behaviorBarrier) {
        String message = "I am " + behaviorBarrier;
        if (MultimodalData.IMUBehavior != null) message += ", but before I was" + MultimodalData.IMUBehavior;
        // Save abnormal Event to the current Scene Context
        prepareMessageForLLM(message);
    }


    public static void objectClassifiedCheck(String objectLabel) {
        String message = "I have been staring at " + objectLabel + ". ";
        prepareMessageForLLM(message);
    }

    public static void SoundDetected(String soundLabel) {
        String message = "I just heard " + soundLabel + ". ";
        prepareMessageForLLM(message);
    }

    public static void AbnormalSoundDetected() {
        String message = "I just heard a very loud sound! ";
        prepareMessageForLLM(message);
    }

    public static void AbnormalLightIntensityDetected() {
        String message = "Suddenly the current light intensity increased. ";
        prepareMessageForLLM(message);
    }

    private static void prepareMessageForLLM(String abnormalEventDescription) {
        String message = abnormalEventDescription +
                "\n Can you ask me about it? Only if it is abnormal in the current context: " +
                MultimodalData.getAllInfo();
        detectionCallback.onResult(message);
    }

    public static void CheckContext() {
        String message = "Check if there is anything very abnormal in this context. If there isn't, send a message " +
                "\"Nothing abnormal\". " + MultimodalData.getAllInfo();
        prepareMessageForLLM(message);
    }

    public static void setDetectionCallback(StringCallback var1) {
        detectionCallback = var1;
    }
}
