package com.ClarifAI.main.sample.src.textualModule;

public interface AwarenessCallback {
    void onSuccess(Object result);

    default void onFailure(Exception e) {

    }
}
