package com.ClarifAI.main.sample.src.textualModule;

public interface StringCallback {
    default void onResult(String result) {
        // Do nothing
    }

    default void onUpdate(String result) {
        // Do nothing
    }

}
