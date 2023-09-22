package com.ClarifAI.main.sample.src.textualModule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.ClarifAI.main.sample.src.codelab.Constant;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.capture.AmbientLightResponse;
import com.huawei.hms.kit.awareness.capture.BeaconStatusResponse;
import com.huawei.hms.kit.awareness.capture.BehaviorResponse;
import com.huawei.hms.kit.awareness.capture.BluetoothStatusResponse;
import com.huawei.hms.kit.awareness.capture.HeadsetStatusResponse;
import com.huawei.hms.kit.awareness.capture.LocationResponse;
import com.huawei.hms.kit.awareness.capture.TimeCategoriesResponse;
import com.huawei.hms.kit.awareness.capture.WeatherStatusResponse;
import com.huawei.hms.kit.awareness.status.AmbientLightStatus;
import com.huawei.hms.kit.awareness.status.BeaconStatus;
import com.huawei.hms.kit.awareness.status.BehaviorStatus;
import com.huawei.hms.kit.awareness.status.BluetoothStatus;
import com.huawei.hms.kit.awareness.status.DetectedBehavior;
import com.huawei.hms.kit.awareness.status.HeadsetStatus;
import com.huawei.hms.kit.awareness.status.TimeCategories;

public class AwarenessService {


    private static String TAG = "AwarenessService";
    public static void getTimeCategories(Context context, AwarenessCallback callback) {
        // Use getTimeCategories() to get the information about the current time of the user location.
        // Time information includes whether the current day is a workday or a holiday, and whether the current day is in the morning, afternoon, or evening, or at the night.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.getCaptureClient(context).getTimeCategories()
                    .addOnSuccessListener(new OnSuccessListener<TimeCategoriesResponse>() {
                        @Override
                        public void onSuccess(TimeCategoriesResponse timeCategoriesResponse) {
                            TimeCategories timeCategories = timeCategoriesResponse.getTimeCategories();
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int timeCode : timeCategories.getTimeCategories()) {
                                stringBuilder.append(Constant.TIME_DESCRIPTION_MAP.get(timeCode));
                            }
                            if (callback != null) {
                                callback.onSuccess(stringBuilder.toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to get time categories.", e);
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        }
    }


    public static void getHeadsetStatus(Context context, AwarenessCallback callback) {
        Awareness.getCaptureClient(context)
                .getHeadsetStatus()
                .addOnSuccessListener(new OnSuccessListener<HeadsetStatusResponse>() {
                    @Override
                    public void onSuccess(HeadsetStatusResponse headsetStatusResponse) {
                        HeadsetStatus headsetStatus = headsetStatusResponse.getHeadsetStatus();
                        int status = headsetStatus.getStatus();
                        String stateStr = "Headsets are " +
                                (status == HeadsetStatus.CONNECTED ? "connected" : "disconnected");
                        if (callback != null) {
                            callback.onSuccess(stateStr);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to get the headset capture.", e);
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public static void getLocation(Context context, AwarenessCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.getCaptureClient(context).getLocation()
                    .addOnSuccessListener(new OnSuccessListener<LocationResponse>() {
                        @Override
                        public void onSuccess(LocationResponse locationResponse) {
                            Location location = locationResponse.getLocation();
                            String logStr = "Longitude:" + location.getLongitude()
                                    + ",Latitude:" + location.getLatitude();
                            if (callback != null) {
                                callback.onSuccess(logStr);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to get the location.", e);
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        }
    }
    public static void getBehaviorStatus(Context context, AwarenessCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            return;  // Permissions should be handled appropriately
        }
        Awareness.getCaptureClient(context).getBehavior()
                .addOnSuccessListener(new OnSuccessListener<BehaviorResponse>() {
                    @Override
                    public void onSuccess(BehaviorResponse behaviorResponse) {
                        BehaviorStatus behaviorStatus = behaviorResponse.getBehaviorStatus();
                        DetectedBehavior mostLikelyBehavior = behaviorStatus.getMostLikelyBehavior();
                        String str = "Most likely behavior is " +
                                Constant.BEHAVIOR_DESCRIPTION_MAP.get(mostLikelyBehavior.getType()) +
                                ", the confidence is " + mostLikelyBehavior.getConfidence();
                        if (callback != null) {
                            callback.onSuccess(Constant.BEHAVIOR_DESCRIPTION_MAP.get(mostLikelyBehavior.getType()));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public static void getLightIntensity(Context context, AwarenessCallback callback) {
        Awareness.getCaptureClient(context).getLightIntensity()
                .addOnSuccessListener(new OnSuccessListener<AmbientLightResponse>() {
                    @Override
                    public void onSuccess(AmbientLightResponse ambientLightResponse) {
                        AmbientLightStatus ambientLightStatus = ambientLightResponse.getAmbientLightStatus();
                        String str = "Light intensity is " + ambientLightStatus.getLightIntensity() + " lux";
                        if (callback != null) {
                            callback.onSuccess(ambientLightStatus.getLightIntensity());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public static void getWeatherStatus(Context context, AwarenessCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.getCaptureClient(context).getWeatherByDevice()
                    .addOnSuccessListener(new OnSuccessListener<WeatherStatusResponse>() {
                        @Override
                        public void onSuccess(WeatherStatusResponse weatherStatusResponse) {

                            // Similar code for handling weather information
                            // Create a string and pass to callback.onSuccess(string);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        }
    }

    public static void getBluetoothStatus(Context context, int deviceType, AwarenessCallback callback) {
        Awareness.getCaptureClient(context).getBluetoothStatus(deviceType)
                .addOnSuccessListener(new OnSuccessListener<BluetoothStatusResponse>() {
                    @Override
                    public void onSuccess(BluetoothStatusResponse bluetoothStatusResponse) {
                        BluetoothStatus bluetoothStatus = bluetoothStatusResponse.getBluetoothStatus();
                        int status = bluetoothStatus.getStatus();
                        String stateStr = "The Bluetooth car stereo is " +
                                (status == BluetoothStatus.CONNECTED ? "connected" : "disconnected");
                        if (callback != null) {
                            callback.onSuccess(stateStr);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public static void getBeaconStatus(Context context, String namespace, String type, byte[] content, AwarenessCallback callback) {
        BeaconStatus.Filter filter = BeaconStatus.Filter.match(namespace, type, content);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.getCaptureClient(context).getBeaconStatus(filter)
                    .addOnSuccessListener(new OnSuccessListener<BeaconStatusResponse>() {
                        @Override
                        public void onSuccess(BeaconStatusResponse beaconStatusResponse) {
                            // Similar code for handling beacon data
                            // Create a string and pass to callback.onSuccess(string);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) {
                                callback.onFailure(e);
                            }
                        }
                    });
        }
    }





}
