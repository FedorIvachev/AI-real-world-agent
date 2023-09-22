package com.ClarifAI.main.sample.src.textualModule;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AzureSTTService {

    private static final String SpeechRegion = "eastasia";
    private static SpeechConfig speechConfig;
    private static boolean continuousListeningStarted = false;
    private static SpeechRecognizer reco = null;
    private static AudioConfig audioInput = null;
    private static ArrayList<String> content = new ArrayList<>();

    private static final String SpeechSubscriptionKey = "";

    public static void InitAzureSpeech(Activity activity) {
        try {
            // a unique number within the application to allow
            // correlating permission request responses with the request.
            int permissionRequestId = 5;

            // Request permissions needed for speech recognition
            ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO, INTERNET, READ_EXTERNAL_STORAGE}, permissionRequestId);
        }
        catch(Exception ex) {
            Log.e("SpeechSDK", "could not init sdk, " + ex.toString());
        }

        // create config
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
    }

    private static MicrophoneStream microphoneStream;
    private static MicrophoneStream createMicrophoneStream() {
        releaseMicrophoneStream();

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }
    private static void releaseMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }
    }


    private static <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    public static void AzureContinuousSpeech(StringCallback callback)
    {

        final String logTag = "Azure continuous reco";

        try {
            content.clear();

            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
            reco = new SpeechRecognizer(speechConfig, audioInput);

            reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                final String s = speechRecognitionResultEventArgs.getResult().getText();
                Log.i(logTag, "Intermediate result received: " + s);
                content.add(s);
                callback.onUpdate(TextUtils.join(" ", content));
                content.remove(content.size() - 1);
            });

            reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                final String s = speechRecognitionResultEventArgs.getResult().getText();
                Log.i(logTag, "Final result received: " + s);
                content.add(s);
                callback.onUpdate(TextUtils.join(" ", content));
                // Start the countdown to after 5 seconds
                // If the user starts talking, remove the countdown
                // If the countdown finishes, StopContinuousSpeech()
            });

            final Future<Void> task = reco.startContinuousRecognitionAsync();
            setOnTaskCompletedListener(task, result -> {
                continuousListeningStarted = true;
            });
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static void AzureIntermediateSpeech(StringCallback callback)
    {
        StopContinuousBackground(new StringCallback() {
            @Override
            public void onResult(String result1) {
                final String logTag = "Azure intermediate reco";
                try {
                    final AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                    final SpeechRecognizer reco = new SpeechRecognizer(speechConfig, audioInput);

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);
                        callback.onUpdate(s);
                    });

                    final Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
                    setOnTaskCompletedListener(task, result -> {
                        final String s = result.getText();
                        reco.close();
                        Log.i(logTag, "Recognizer returned: " + s);
                        AzureContinuousSpeechBackground(new StringCallback() {
                        });
                        callback.onResult(s);
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

    }

    private static void StopContinuousBackground(StringCallback callback) {
        if (continuousListeningStarted) {
            if (reco != null) {
                final Future<Void> task = reco.stopContinuousRecognitionAsync();
                setOnTaskCompletedListener(task, result -> {
                    callback.onResult("Finished");
                    continuousListeningStarted = false;
                });
            } else {
                continuousListeningStarted = false;
            }
        }
    }

    public static void AzureContinuousSpeechBackground(StringCallback callback) {
        final String logTag = "Azure continuous recognition background";

        try {
            content.clear();

            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
            reco = new SpeechRecognizer(speechConfig, audioInput);

            reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                final String s = speechRecognitionResultEventArgs.getResult().getText();
                Log.i(logTag, "Intermediate result received: " + s);
                content.add(s);
                content.remove(content.size() - 1);
            });

            reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                final String s = speechRecognitionResultEventArgs.getResult().getText();
                content.add(s);
            });

            final Future<Void> task = reco.startContinuousRecognitionAsync();
            setOnTaskCompletedListener(task, result -> {
                continuousListeningStarted = true;
            });
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
