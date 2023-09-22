package com.ClarifAI.main.sample.src.textualModule;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ClarifAI.main.sample.src.EyesGPTActivity;
import com.ClarifAI.main.sample.src.codelab.Utils;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.hms.kit.awareness.barrier.BehaviorBarrier;

public class BarrierService {
    private static final String BEGINNING_BARRIER_WALKING = "began walking";
    private static final String BEGINNING_BARRIER_STILL = "sat down";
    private static final String BEGINNING_BARRIER_BICYCLE = "started riding a bicycle";
    private static final String BEGINNING_BARRIER_ONVEHICLE = "started driving a vehicle";
    private static final String BEGINNING_BARRIER_ONFOOT = "stood on foot";
    private static final String BEGINNING_BARRIER_RUNNING = "started running";

    private PendingIntent mPendingIntent;
    private BehaviorBarrierReceiver mBarrierReceiver;

    private final Context context;

    public BarrierService(Context context) {
        this.context = context;
    }


    public void setupBarrier() {

        final String barrierReceiverAction = context.getPackageName() + "BEHAVIOR_BARRIER_RECEIVER_ACTION";
        Intent intent = new Intent(barrierReceiverAction);
        // You can also create PendingIntent with getActivity() or getService().
        // This depends on what action you want Awareness Kit to trigger when the barrier status changes.
        mPendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Register a broadcast receiver to receive the broadcast sent by Awareness Kit when the barrier status changes.
        mBarrierReceiver = new BehaviorBarrierReceiver();
        context.registerReceiver(mBarrierReceiver, new IntentFilter(barrierReceiverAction));
        addUserBeginningBarriers();
    }

    private void addUserBeginningBarriers() {
        // Walking
        @SuppressLint("MissingPermission") AwarenessBarrier beginWalkingBarrier = BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_WALKING);
        Utils.addBarrier(context, BEGINNING_BARRIER_WALKING, beginWalkingBarrier, mPendingIntent);

        // Still
        @SuppressLint("MissingPermission") AwarenessBarrier beginStillBarrier =
                BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_STILL);
        Utils.addBarrier(context, BEGINNING_BARRIER_STILL, beginStillBarrier, mPendingIntent);

        // Bicycle
        @SuppressLint("MissingPermission") AwarenessBarrier beginBicycleBarrier =
                BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_ON_BICYCLE);
        Utils.addBarrier(context, BEGINNING_BARRIER_BICYCLE, beginWalkingBarrier, mPendingIntent);

        // Vehicle
        @SuppressLint("MissingPermission") AwarenessBarrier beginOnVehicleBarrier =
                BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_IN_VEHICLE);
        Utils.addBarrier(context, BEGINNING_BARRIER_ONVEHICLE, beginWalkingBarrier, mPendingIntent);

        // On Foot
        @SuppressLint("MissingPermission") AwarenessBarrier beginOnFootBarrier =
                BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_ON_FOOT);
        Utils.addBarrier(context, BEGINNING_BARRIER_ONFOOT, beginWalkingBarrier, mPendingIntent);


        // Running
        @SuppressLint("MissingPermission") AwarenessBarrier beginRunningBarrier =
                BehaviorBarrier.beginning(BehaviorBarrier.BEHAVIOR_RUNNING);
        Utils.addBarrier(context, BEGINNING_BARRIER_RUNNING, beginWalkingBarrier, mPendingIntent);
    }

    private void updateActivity(String newActivity) {
        //MultimodalData.IMUBehavior = newActivity;
        //SceneContext.abnormalEvent = newActivity;
        String message = "Barrier: The user " + newActivity + ".";
        // Call trigger
        if (context instanceof EyesGPTActivity) {
            ((EyesGPTActivity) context).LogTextUI(message, true);
            ((EyesGPTActivity) context).callDetectionServiceAbnormalEventDetection(newActivity);
        }
    }

    final class BehaviorBarrierReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BarrierStatus barrierStatus = BarrierStatus.extract(intent);
            String label = barrierStatus.getBarrierLabel();
            int barrierPresentStatus = barrierStatus.getPresentStatus();
            if (label == null) {
                ////mLogView.printLog("label is null.");
                return;
            }
            switch (label) {
                case BEGINNING_BARRIER_WALKING:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_WALKING);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                case BEGINNING_BARRIER_STILL:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_STILL);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                case BEGINNING_BARRIER_BICYCLE:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_BICYCLE);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                case BEGINNING_BARRIER_ONVEHICLE:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_ONVEHICLE);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                case BEGINNING_BARRIER_ONFOOT:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_ONFOOT);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                case BEGINNING_BARRIER_RUNNING:
                    if (barrierPresentStatus == BarrierStatus.TRUE) {
                        updateActivity(BEGINNING_BARRIER_RUNNING);
                    } else if (barrierPresentStatus == BarrierStatus.FALSE) {
                        //mLogView.printLog("The beginning barrier status is false.");
                    } else {
                        //mLogView.printLog("The user behavior status is unknown.");
                    }
                    break;

                default:
                    break;
            }
            //mScrollView.postDelayed(()-> mScrollView.smoothScrollTo(0,mScrollView.getBottom()),200);
        }
    }

    public void cleanup() {
        if (mBarrierReceiver != null) {
            context.unregisterReceiver(mBarrierReceiver);
        }
    }
}
