package com.ClarifAI.main.sample.src;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.FileProvider;

import com.ClarifAI.main.sample.src.textualModule.AwarenessCallback;
import com.ClarifAI.main.sample.src.textualModule.AwarenessService;
import com.ClarifAI.main.sample.src.textualModule.AzureSTTService;
import com.ClarifAI.main.sample.src.LLMInteractionModule.LLMService;
import com.ClarifAI.main.sample.src.textualModule.LocalDateTimeSerializer;
import com.ClarifAI.main.sample.src.textualModule.MLClassificationManager;
import com.ClarifAI.main.sample.src.textualModule.MultimodalData;
import com.ClarifAI.main.sample.src.textualModule.SceneContext;
import com.ClarifAI.main.sample.src.textualModule.SoundDetectionService;
import com.ClarifAI.main.sample.src.camera.CameraConfiguration;
import com.ClarifAI.main.sample.src.camera.LensEngine;
import com.ClarifAI.main.sample.src.camera.LensEnginePreview;
import com.ClarifAI.main.sample.src.LLMInteractionModule.ChatMessageStorage;
import com.ClarifAI.main.sample.src.transactor.LocalImageClassificationTransactor;
import com.ClarifAI.main.sample.src.views.overlay.GraphicOverlay;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.sounddect.MLSoundDetectListener;
import com.huawei.hms.mlsdk.sounddect.MLSoundDetector;
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsLocalModel;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;
import com.ClarifAI.main.sample.activity.BaseActivity;
import com.ClarifAI.main.sample.src.textualModule.BarrierService;
import com.ClarifAI.main.sample.src.textualModule.DetectionService;
import com.ClarifAI.main.sample.src.textualModule.LocalDateTimeDeserializer;
import com.ClarifAI.main.sample.src.dynamicRoleAllocationModule.PromptService;
import com.ClarifAI.main.sample.src.textualModule.Settings;
import com.ClarifAI.main.sample.src.textualModule.StringCallback;

import com.ClarifAI.main.sample.src.util.Constant;
import com.huawei.mlkit.sample.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EyesGPTActivity extends BaseActivity
        implements OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "EyesGPTActivity";

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private enum ConversationStage {
        FREE_TALK,
        ANNOTATION,
        EVALUATION
    }

    ConversationStage stage = ConversationStage.FREE_TALK;

    private String recognizedSpeech = "";

    private LensEngine lensEngine = null;
    private LensEnginePreview preview;
    private GraphicOverlay graphicOverlay;
    private ToggleButton facingSwitch;
    CameraConfiguration cameraConfiguration = null;
    private int facing = CameraConfiguration.CAMERA_FACING_BACK;
    private Camera mCamera;
    private boolean isInitialization = false;

    private TextView testLabel;

    private TextView logLabel;

    private boolean hasToEvaluate = false;

    private ScrollView logScrollView;

    // State variable to check if the TextView is expanded
    private boolean isLogViewExpanded = false;

    private String mLanguage = "en-US";

    private boolean mIsRecording = false;

    // TTS fields

    private static final String NO_NETWORK = "0104";
    private static final String SPEAK_ABNORMAL = "7002";

    private static final int MESSAGE_TYPE_INFO = 1;
    private static final int MESSAGE_TYPE_RANGE = 2;

    public static String AUDIO_PATH;
    private static String AUDIO_FILE_NAME_PCM;
    private static String AUDIO_FILE_NAME_WAV;
    private MediaPlayer mediaPlayer;

    private boolean isFlush = false;

    private float speedVal = 1.0f;
    private float volumeVal = 1.0f;

    private Map<String, String> temp = new HashMap<>();


    // Sound detection
    private MLSoundDetector soundDector;

    private static String[] type;


    private Handler mainLoopHandler = new Handler(Looper.getMainLooper());


    // Detection service

    private BarrierService barrierService;

    private boolean isUserEvaluating = false;


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void InitDetectionService() {
        DetectionService.setDetectionCallback(detectionCallback);
    }

    public void callDetectionServiceAbnormalEventDetection(String eventmsg) {
        // If abnormal event happened while the conversation - skip it
        if (Objects.equals(SceneContext.abnormalEvent, "None")) {
            DetectionService.abnormalBehaviorDetected(eventmsg);
        }
    }

    StringCallback detectionCallback = new StringCallback() {
        @Override
        public void onResult(String result) {
            LLMService.isUserTriggered = false;
            if (Objects.equals(SceneContext.abnormalEvent, "None")) {
                SceneContext.abnormalEvent = result;
                stage = ConversationStage.ANNOTATION;
                SendToLLM(result);
            }
        }
    };

    MLClassificationManager.ClassificationCallback detectedUnknownObject = new MLClassificationManager.ClassificationCallback() {
        @Override
        public void onClassificationStable(String label) {
            // Your code to handle the stable classification
            if (Objects.equals(SceneContext.abnormalEvent, "None"))
                DetectionService.objectClassifiedCheck(label);
        }
    };

    // Detection service end



    // TTS
    MLTtsConfig mlTtsConfig;
    MLTtsEngine mlTtsEngine;
    private void downloadModel(String person) {
        // Create an on-device TTS model manager.
        MLLocalModelManager localModelManager = MLLocalModelManager.getInstance();
        // Create an MLTtsLocalModel instance and pass the speaker (indicating by person) to download the language model corresponding to the speaker.
        final MLTtsLocalModel model = new MLTtsLocalModel.Factory(person)
                .create();
        // Create a download policy configurator. You can set that when any of the following conditions is met, the model can be downloaded: 1. The device is charging; 2. Wi-Fi is connected; 3. The device is idle.
        MLModelDownloadStrategy request = new MLModelDownloadStrategy.Factory().needWifi().create();
        // Create a download progress listener for the on-device model.
        MLModelDownloadListener modelDownloadListener = new MLModelDownloadListener() {
            @Override
            public void onProcess(long alreadyDownLength, long totalLength) {
                // Display the download progress of the on-device language model.
                LogTextUI(alreadyDownLength + " ", false);
            }
        };
        // Call the download API of the on-device model manager.
        localModelManager.downloadModel(model, request, modelDownloadListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Download success.
                        // Call updateConfig to update the engine model configuration.
                        mlTtsEngine.updateConfig(mlTtsConfig);
                        // Call the speak API to perform TTS. sourceText indicates the text to be synthesized. For details about the speak API, please refer to step 6.
                        mlTtsEngine.speak("Model downloaded", MLTtsEngine.QUEUE_APPEND | MLTtsEngine.OPEN_STREAM);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Download failure.
                    }
                });
    }


    void speakText(String sourceText) {
        // Create an on-device TTS model manager.
        MLLocalModelManager localModelManager = MLLocalModelManager.getInstance();
// Create an MLTtsLocalModel instance to set the speaker so that the language model corresponding to the speaker can be downloaded through the model manager.
        MLTtsLocalModel model = new MLTtsLocalModel.Factory(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_FEMALE_BEE).create();
        localModelManager.isModelExist(model).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                // If the model is not downloaded, call the download API. Otherwise, call the TTS API of the on-device engine.
                if (aBoolean) {
                    // Call the speak API to perform TTS. sourceText indicates the text to be synthesized. For details about the speak API, please refer to step 6.
                    mlTtsEngine.speak(sourceText, MLTtsEngine.QUEUE_APPEND | MLTtsEngine.OPEN_STREAM);
                } else {
                    // Call the API for downloading the on-device TTS model in step 5.
                    downloadModel(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_FEMALE_BEE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Query error message.
            }
        });
    }

    void InitTTS() {
        // Use customized parameter settings to create a TTS engine.
// For details about the speaker names, please refer to the Timbres section.
        mlTtsConfig = new MLTtsConfig()
                // Set the text converted from speech to English.
                .setLanguage(MLTtsConstants.TTS_EN_US)
                // Set the speaker with the English female voice (bee).
                .setPerson(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_FEMALE_BEE)
                // Set the TTS mode to on-device mode. The default mode is real-time mode.
                .setSynthesizeMode(MLTtsConstants.TTS_OFFLINE_MODE);
        mlTtsEngine = new MLTtsEngine(mlTtsConfig);
// Update the configuration when the engine is running.
        mlTtsEngine.updateConfig(mlTtsConfig);

        mlTtsEngine.setTtsCallback(callback);

    }

    MLTtsCallback callback = new MLTtsCallback() {
        @Override
        public void onError(String taskId, MLTtsError err) {
            // Processing logic for TTS failure.
        }

        @Override
        public void onWarn(String taskId, MLTtsWarn warn) {
            // Alarm handling without affecting service logic.
        }

        @Override
        // Return the mapping between the currently played segment and text. start: start position of the audio segment in the input text; end (excluded): end position of the audio segment in the input text.
        public void onRangeStart(String taskId, int start, int end) {
            // Process the mapping between the currently played segment and text.
        }

        @Override
        // taskId: ID of a TTS task corresponding to the audio.
        // audioFragment: audio data.
        // offset: offset of the audio segment to be transmitted in the queue. One TTS task corresponds to a TTS queue.
        // range: text area where the audio segment to be transmitted is located; range.first (included): start position; range.second (excluded): end position.
        public void onAudioAvailable(String taskId, MLTtsAudioFragment audioFragment, int offset,
                                     Pair<Integer, Integer> range, Bundle bundle) {
            // Audio stream callback API, which is used to return the synthesized audio data to the app.
        }

        @Override
        public void onEvent(String taskId, int eventId, Bundle bundle) {
            // Callback method of a TTS event. eventId indicates the event name.
            boolean isInterrupted;
            switch (eventId) {
                case MLTtsConstants.EVENT_PLAY_START:
                    // Called when playback starts.
                    break;
                case MLTtsConstants.EVENT_PLAY_STOP:
                    // Called when playback stops.
                    isInterrupted = bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED);
                    recognizeSpeech();
                    break;
                case MLTtsConstants.EVENT_PLAY_RESUME:
                    // Called when playback resumes.
                    break;
                case MLTtsConstants.EVENT_PLAY_PAUSE:
                    // Called when playback pauses.
                    break;
                // Pay attention to the following callback events when you focus on only synthesized audio data but do not use the internal player for playback:
                case MLTtsConstants.EVENT_SYNTHESIS_START:
                    // Called when TTS starts.
                    break;
                case MLTtsConstants.EVENT_SYNTHESIS_END:
                    // Called when TTS ends.
                    break;
                case MLTtsConstants.EVENT_SYNTHESIS_COMPLETE:
                    // TTS is complete. All synthesized audio streams are passed to the app.
                    isInterrupted = bundle.getBoolean(MLTtsConstants.EVENT_SYNTHESIS_INTERRUPTED);
                    break;
                default:
                    break;
            }
        }
    };


    // TTS END



    private void getLastLocationEyesGPT() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        MultimodalData.latitude =
                                location.getLatitude();
                        MultimodalData.longitude =
                                location.getLongitude();
                        // Now latitude and longitude are set to the retrieved coordinates
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location Error", "Failed to get location");
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_eyes_gpt);
        if (savedInstanceState != null) {
            this.facing = savedInstanceState.getInt(Constant.CAMERA_FACING);
        }
        this.preview = this.findViewById(R.id.eyesgpt_preview);
        this.findViewById(R.id.classification_back).setOnClickListener(this);
        this.graphicOverlay = this.findViewById(R.id.classification_overlay);
        this.cameraConfiguration = new CameraConfiguration();
        this.cameraConfiguration.setCameraFacing(this.facing);
        this.facingSwitch = this.findViewById(R.id.classification_facingSwitch);
        this.facingSwitch.setOnCheckedChangeListener(this);
        this.testLabel = this.findViewById(R.id.test_Label);
        this.logLabel = this.findViewById(R.id.log_label);
        this.logScrollView = findViewById(R.id.log_scroll_view);
        if (Camera.getNumberOfCameras() == 1) {
            this.facingSwitch.setVisibility(View.GONE);
        }

        findViewById(R.id.icon_record).setOnClickListener(this);
        findViewById(R.id.save_button).setOnClickListener(this);

        ImageView settingsButton = findViewById(R.id.setting_img);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsDialog();
            }
        });



        // Sound Detection init
        type = getResources().getStringArray(R.array.sound_dect_voice_type);

        soundDector = MLSoundDetector.createSoundDetector();
        soundDector.setSoundDetectListener(listener);

        // Location init
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocationEyesGPT();


        checkAndRequestPermissions();

        SetAnimationForLogTextViewClick();
        AzureSTTService.InitAzureSpeech(this);



        multimodalInputStart();


        // Main loop init

        // Schedule the task to run immediately and then every 2 seconds
        mainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                // Call your function here
                getData();

                // Reschedule the same runnable to run again at a later time
                mainLoopHandler.postDelayed(this, 5000); // 5000 milliseconds = 5 seconds
            }
        });

        InitTTS();

        barrierService = new BarrierService(this);

        InitDetectionService();

        // Adding object detection

        MLClassificationManager.getInstance().setClassificationCallback(detectedUnknownObject);

        // Adding prompt service with an updatable prompt every 3000 seconds
        //PromptService.startPromptService();

        AzureSTTService.AzureContinuousSpeechBackground(new StringCallback() {
        });
        this.createLensEngine();
        this.setStatusBar();
    }

    private void openSettingsDialog() {
        // Create a view containing an EditText
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        final EditText editText = dialogView.findViewById(R.id.edit_text); // Assume you have an EditText with id 'edit_text' inside dialog_settings.xml
        editText.setText(String.valueOf(Settings.abnormalSoundThreshold));

        // Build the dialog
        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setView(dialogView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Save value to Settings static class
                        try {
                            Settings.abnormalSoundThreshold = Integer.parseInt(editText.getText().toString());
                        } catch (NumberFormatException e) {
                            // Handle parsing error here, e.g., show a toast or dialog
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static final int PERMISSION_REQUEST_CODE = 940;
    private final String[] mPermissionsOnHigherVersion = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};
    private final String[] mPermissionsOnLowerVersion = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            "com.huawei.hms.permission.ACTIVITY_RECOGNITION"};


    private void checkAndRequestPermissions() {
        List<String> permissionsDoNotGrant = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (String permission : mPermissionsOnHigherVersion) {
                if (ActivityCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsDoNotGrant.add(permission);
                }
            }
        } else {
            for (String permission : mPermissionsOnLowerVersion) {
                if (ActivityCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsDoNotGrant.add(permission);
                }
            }
        }

        if (permissionsDoNotGrant.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsDoNotGrant.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean hasPermissionDenied = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDenied = true;
                }
            }
            if (hasPermissionDenied) {
                Toast.makeText(this, "grant permission failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "grant permission success", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void RunToast(String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message,  Toast.LENGTH_LONG).show();
            }
        });
    }


    void multimodalInputStart() {
        soundDector.start(EyesGPTActivity.this);
    }

    void multimodalInputStop() {
        soundDector.stop();
    }


    private void conversationSegmentFinished() {
        // ToDo: add evaluation by LLM (with score)
        // Set to abnormalEvent to None since we finished describing the context change and switched to the next
        // context
        PromptService.selectPrompt(MultimodalData.getAllInfo(), new StringCallback() {
            @Override
            public void onResult(String result) {
                RunToast("Changed system role to " + result);
                ChatMessageStorage.getInstance().addPromptSelected(new ChatMessage(ChatUser.ASSISTANT, result));
            }
        });

        if (stage == ConversationStage.FREE_TALK) {
            ChatMessageStorage.getInstance().SaveMessages();
            SceneContext.abnormalEvent = "None";
        } else {
            // Run annotation procedure
            List<ChatMessage> messages = new ArrayList<>();

            String message =
                    "The conversation is: " + ChatMessageStorage.getInstance().getAllMessagesAsString() + ". " +
                            PromptService.conversationSummarizationPrompt;
            messages.add(new ChatMessage(ChatUser.USER, message));
            LLMService.CompletionTest(messages, false, new StringCallback() {
                @Override
                public void onResult(String result) {
                    stage = ConversationStage.EVALUATION;
                    SetMessage(result);
                    ChatMessageStorage.getInstance().addContextDescription(new ChatMessage(ChatUser.ASSISTANT, result));
                    //ChatMessageStorage.getInstance().addMessage(new ChatMessage(ChatUser.ASSISTANT, result));
                    //LogLastMessages();
                    speakText(result);
                    // Is change of role change of context?
                }
            });
        }
    }

    private void SendToLLM(String userMessage) {
        if (ChatMessageStorage.getInstance().getMessagesCount() >= 4) {
            if (stage == ConversationStage.ANNOTATION) {
                ChatMessageStorage.getInstance().addMessage(new ChatMessage(ChatUser.USER, userMessage));
                conversationSegmentFinished();
                return;
            } if (stage == ConversationStage.FREE_TALK ||
            stage == ConversationStage.EVALUATION) {

                if (stage == ConversationStage.EVALUATION) {
                    ChatMessageStorage.getInstance().addContextDescription(new ChatMessage(ChatUser.USER, userMessage));
                }
                stage = ConversationStage.FREE_TALK;
                ChatMessageStorage.getInstance().SaveMessages();
                SceneContext.abnormalEvent = "None";
                return;
            }
        }


        if (Objects.equals(SceneContext.abnormalEvent, "User started the conversation")) {
            userMessage += ". \nScene context: " +
                    MultimodalData.getAllInfo();
        }

        if (LLMService.isUserTriggered) userMessage += ". \nScene context: " +
                MultimodalData.getAllInfo();

        ChatMessageStorage.getInstance().addMessage(new ChatMessage(ChatUser.USER, userMessage));

        // Log the messages
        List<ChatMessage> messages = ChatMessageStorage.getInstance().getLastMessages(5, true);
        LLMService.CompletionTest(messages, true, new StringCallback() {
            @Override
            public void onResult(String result) {
                if (Objects.equals(result, "Nothing abnormal") || Objects.equals(result, "\"Nothing abnormal\"")) {
                    stage = ConversationStage.FREE_TALK;
                    ChatMessageStorage.getInstance().SaveMessages();
                    SceneContext.abnormalEvent = "None";
                    speakText("");
                }
                int messageStartIndex = result.indexOf("Message:\n");
                if (messageStartIndex != -1) {
                    result = result.substring(messageStartIndex + 9).trim();
                }
                LLMService.isUserTriggered = false;
                SetMessage(result);
                ChatMessageStorage.getInstance().addMessage(new ChatMessage(ChatUser.ASSISTANT, result));
                LogLastMessages();



                if (!Objects.equals(result, "Finish.") && !Objects.equals(result, "Finish") ) {
                    speakText(result);
                } else {
                    stage = ConversationStage.FREE_TALK;
                    ChatMessageStorage.getInstance().SaveMessages();
                    SceneContext.abnormalEvent = "None";
                    speakText("");
                }
            }
            @Override
            public void onUpdate(String result) {
                runOnUiThread(() -> testLabel.append(result));
            }
        });
    }

    private void getData() {
        // Uncomment to check the context for abnormalities every 5 seconds
        // DetectionService.CheckContext();
        getLastLocationEyesGPT();
        MultimodalData.selectedObjectClassifications =
                MLClassificationManager.getInstance().getClassificationsInCustomFormat();
        LogTextUI(MLClassificationManager.getInstance().getClassificationsInCustomFormat(), true);
        MultimodalData.getClosestSite(
                new StringCallback() {
                    @Override
                    public void onResult(String result) {
                        LogTextUI(result, true);
                    }
                });

        AwarenessService.getLightIntensity(this, new AwarenessCallback() {
                    @Override
                    public void onSuccess(Object result) {

                        if ((float) result > MultimodalData.lightIntensity + 500f) {
                            DetectionService.AbnormalLightIntensityDetected();
                        }
                        MultimodalData.lightIntensity = (float) result;

                        LogTextUI((float) result + " ", true);
                    }
                });

        AwarenessService.getBehaviorStatus(this, new AwarenessCallback() {
            @Override
            public void onSuccess(Object result) {
                MultimodalData.IMUBehavior = (String) result;


                LogTextUI((String) result, true);
            }
        });

        AwarenessService.getTimeCategories(this, new AwarenessCallback() {
            @Override
            public void onSuccess(Object result) {
                MultimodalData.timeCategory = (String) result;
                LogTextUI((String) result, true);
            }
        });


    }

    public void LogTextUI(String message, boolean needToAddText) {
        EyesGPTActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (needToAddText)  logLabel.append("\n" + message);
                else logLabel.setText(message);
            }
        });

    }

    private void updateTTSConfig() {
        MLTtsConfig mlTtsConfig = new MLTtsConfig().setVolume(volumeVal)
                .setSpeed(speedVal)
                .setLanguage(mLanguage)
                .setPerson(mlTtsEngine.getSpeaker(mLanguage).get(0).getName());
        mlTtsEngine.updateConfig(mlTtsConfig);
    }


    private void sendMsg(int id, String str) {
        Message msg = new Message();
        msg.what = id;
        msg.obj = str;
        TTShandler.sendMessage(msg);
    }

    private void sendRangeMsg(String str, int start, int end) {
        Message msg = new Message();
        msg.what = MESSAGE_TYPE_RANGE;
        Bundle bundle = new Bundle();
        bundle.putString("taskId", str);
        bundle.putInt("start", start);
        bundle.putInt("end", end);
        msg.setData(bundle);
        TTShandler.sendMessage(msg);
    }

    private void restartPlayer(String path) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void showFailedDialog(int res) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(res)
                .setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.button_background));
    }


    private Handler TTShandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MESSAGE_TYPE_INFO:
                    String extension = (String) message.obj;
                    if (extension == null) {
                        break;
                    }
                    if (NO_NETWORK.equals(extension)) {
                        showFailedDialog(R.string.nonetwork);
                    } else if (SPEAK_ABNORMAL.equals(extension)) {
                        showFailedDialog(R.string.speak_abnormal);
                    } else {
                        showFailedDialog(R.string.abnormal);
                    }
                    break;
                case MESSAGE_TYPE_RANGE:
                    if (testLabel.getText().toString().isEmpty()) {
                        break;
                    }
                    Bundle bundle = message.getData();
                    String taskId = bundle.getString("taskId");
                    int start = bundle.getInt("start");
                    int end = bundle.getInt("end");
                    String text1 = temp.get(taskId);
                    SpannableStringBuilder style = new SpannableStringBuilder(text1);
                    // Set the background color of the specified position of textView
                    style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.button_background)),
                            start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    testLabel.setText(style);
                    //startRecodingOnPlugin();
                    break;
                default:
                    break;
            }
            return false;
        }
    });



    private void SetAnimationForLogTextViewClick() {
        logLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startHeight;
                int endHeight;
                float startAlpha;
                float endAlpha;

                // Check the state and determine the start and end heights for the animation
                if (isLogViewExpanded) {
                    // If expanded, we collapse
                    startHeight = logScrollView.getMeasuredHeight();
                    endHeight = getResources().getDimensionPixelSize(R.dimen.collapsed_height); // assuming 60dp is stored in dimens.xml as collapsed_height
                    startAlpha = 1f;
                    endAlpha = 0.3f; // fade to 30% opacity
                    // Stop TTS Service

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // your code here
                            mlTtsEngine.resume();
                        }
                    }).start();


                    // Stop recording video
                    //lensEngine.stopRecording();

                } else {
                    // If collapsed, we expand
                    startHeight = getResources().getDimensionPixelSize(R.dimen.collapsed_height);
                    endHeight = logLabel.getMeasuredHeight(); // maximum height for the text view
                    startAlpha = 0.3f; // start from 30% opacity
                    endAlpha = 1f; // fade in to full opacity
                    // Stop TTS Service

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // your code here
                            mlTtsEngine.pause();
                        }
                    }).start();


                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String outputFileName = "video_" + timeStamp + ".mp4";
                    String outputFilePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/" + outputFileName;
                    // Start recording video
                    //lensEngine.startRecording(outputFilePath);

                }

                // Animate the ScrollView height change
                ValueAnimator heightAnimation = ValueAnimator.ofInt(startHeight, endHeight);
                heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int animatedValue = (int) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = logScrollView.getLayoutParams();
                        layoutParams.height = animatedValue;
                        logScrollView.setLayoutParams(layoutParams);
                    }
                });

                // Animate the alpha value of the TextView
                ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(logLabel, "alpha", startAlpha, endAlpha);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(200); // set a duration of 200 milliseconds
                animatorSet.playTogether(heightAnimation, alphaAnimation); // play both animations at the same time
                animatorSet.start();

                // Toggle the state
                isLogViewExpanded = !isLogViewExpanded;
            }
        });


    }



    private void recognizeSpeech() {
        playNotificationSound();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                AzureSTTService.AzureIntermediateSpeech(new StringCallback() {
                    @Override
                    public void onResult(String result) {
                        playNotificationSound();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SetMessage(result);  // Assuming SetMessage() updates the UI
                            }
                        });
                        // ToDo Add current context info
                        SendToLLM(result);  // Assuming SendToLLM() is a long-running task
                    }

                    @Override
                    public void onUpdate(String result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SetMessage(result);  // Assuming SetMessage() updates the UI
                            }
                        });
                    }
                });
            }
        });
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.classification_back) {
            releaseLensEngine();
            this.finish();
        }else if (view.getId() == R.id.save_button) {
            ShareMessageData();
        } else if (view.getId() == R.id.icon_record) {
            if (Objects.equals(SceneContext.abnormalEvent, "None")) {
                stage = ConversationStage.FREE_TALK;
                final TextView recordText = findViewById(R.id.tv_record);
                LLMService.isUserTriggered = true;
                SceneContext.abnormalEvent = "User started the conversation";
                recognizeSpeech();

                // Apply the new layout parameters to the button
                ValueAnimator animWidth = ValueAnimator.ofInt(view.getWidth(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
                ValueAnimator animHeight = ValueAnimator.ofInt(view.getHeight(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));

                animWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.width = val;
                        view.setLayoutParams(layoutParams);
                    }
                });

                animHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        layoutParams.height = val;
                        view.setLayoutParams(layoutParams);
                    }
                });

                ObjectAnimator animMoveRight = ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0);
                ObjectAnimator animMoveBottom = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), 0);

                // Animator set to play all animations together
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(animWidth, animHeight, animMoveRight, animMoveBottom);
                animSet.setDuration(200); // Duration 0.2 seconds


                animSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        // Set the final layout properties after animation ends
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                        layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
                        view.setLayoutParams(layoutParams);
                    }
                });

                animSet.start();

                final ImageView recordimage = findViewById(R.id.iv_record);
                int widthheightInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                int marginTopInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

                // Get the current layout parameters and update the width
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) recordimage.getLayoutParams();
                layoutParams.width = widthheightInPx;
                layoutParams.height = widthheightInPx;
                layoutParams.topMargin = marginTopInPx;

                // Set the updated layout parameters back to the ImageView
                recordimage.setLayoutParams(layoutParams);


                int widthInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

                // Get the current layout parameters and update the width
                ViewGroup.MarginLayoutParams layoutParams1 = (ViewGroup.MarginLayoutParams) recordText.getLayoutParams();
                layoutParams1.width = widthInPx;
                layoutParams1.topMargin = marginInPx;

                // Set the updated layout parameters back to the ImageView
                recordText.setLayoutParams(layoutParams1);
                recordText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.lensEngine != null) {
            if (isChecked) {
                this.facing = CameraConfiguration.CAMERA_FACING_FRONT;
                this.cameraConfiguration.setCameraFacing(this.facing);
            } else {
                this.facing = CameraConfiguration.CAMERA_FACING_BACK;
                this.cameraConfiguration.setCameraFacing(this.facing);
            }
        }
        this.preview.stop();
        restartLensEngine();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constant.CAMERA_FACING, this.facing);
        super.onSaveInstanceState(outState);
    }


    private void createLensEngine() {
        if (this.lensEngine == null) {
            this.lensEngine = new LensEngine(this, this.cameraConfiguration, this.graphicOverlay);
        }
        try {
            this.lensEngine.setMachineLearningFrameTransactor(new LocalImageClassificationTransactor(this.getApplicationContext()));
            isInitialization = true;
        } catch (Exception e) {
            Toast.makeText(
                            this,
                            "Can not create image transactor: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void restartLensEngine() {
        this.startLensEngine();
        if (null != this.lensEngine) {
            this.mCamera = this.lensEngine.getCamera();
            try {
                this.mCamera.setPreviewDisplay(this.preview.getSurfaceHolder());
            } catch (IOException e) {
                Log.d(EyesGPTActivity.TAG, "initViews IOException");
            }
        }
    }

    private void startLensEngine() {

        if (this.lensEngine != null) {
            try {
                this.preview.start(this.lensEngine, false);
            } catch (IOException e) {
                Log.e(EyesGPTActivity.TAG, "Unable to start lensEngine.", e);
                this.lensEngine.release();
                this.lensEngine = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInitialization){
            createLensEngine();
        }
        this.startLensEngine();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.preview.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseLensEngine();
    }

    private void releaseLensEngine(){
        if (this.lensEngine != null) {
            this.lensEngine.release();
            this.lensEngine = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLensEngine();
        soundDector.destroy();
        // Remove any callbacks and messages that were posted to the handler.
        mainLoopHandler.removeCallbacksAndMessages(null);
        barrierService.cleanup();
        executorService.shutdown();
    }


    public void SetMessage(String message) {
        EyesGPTActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update your UI component here.
                // Get a reference to the parent layout
                RelativeLayout parentLayout = findViewById(R.id.parent_layout);

// Before changing the text (or any other property that will change the view's size)
                TransitionManager.beginDelayedTransition(parentLayout, new ChangeBounds());

// Now change the text of the TextView
                testLabel.setText(message);
            }
        });
    }


    public void AddToMessage(String textToAdd) {
        EyesGPTActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update your UI component here.
                // Get a reference to the parent layout
                RelativeLayout parentLayout = findViewById(R.id.parent_layout);

// Before changing the text (or any other property that will change the view's size)
                TransitionManager.beginDelayedTransition(parentLayout, new ChangeBounds());

// Now change the text of the TextView
                testLabel.append(textToAdd);
            }
        });
    }

    public void playNotificationSound() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100); //100 is the volume (0-100)
        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150); //150 is the duration in ms
        toneGen1.release();
    }


    private void ShareMessageData() {
        Gson gson = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                    .create();
        }
        String json = gson.toJson(ChatMessageStorage.getInstance());
        try {
            File jsonFile = saveJsonToFile(json, "data.json");
            shareFile(jsonFile);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error
        }
    }


    private File saveJsonToFile(String json, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
        return file;
    }

    private void shareFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("application/json"); // MIME type of the file

        startActivity(Intent.createChooser(shareIntent, "Share JSON"));
    }

    private MLSoundDetectListener listener = new MLSoundDetectListener() {
        @Override
        public void onSoundSuccessResult(Bundle result) {
            int voiceType = result.getInt(MLSoundDetector.RESULTS_RECOGNIZED);
            if (voiceType > 0 && voiceType < 13) {
                String soundType = type[voiceType];
                SoundDetectionService.AddSound(soundType);
                DetectionService.SoundDetected(soundType);
            }
        }

        @Override
        public void onSoundFailResult(int errCode) {
            Log.e(TAG, "FailResult errCode: " + errCode);
            Toast.makeText(EyesGPTActivity.this, getString(R.string.sound_dect_error) + errCode, Toast.LENGTH_LONG).show();
        }
    };

    private void LogLastMessages() {
        List<ChatMessage> lastMessages = ChatMessageStorage.getInstance().
                getLastMessages(3, false);

        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : lastMessages) {
            sb.append(message.toString()).append("\n");
        }

        LogTextUI(sb.toString(), false);
    }
}