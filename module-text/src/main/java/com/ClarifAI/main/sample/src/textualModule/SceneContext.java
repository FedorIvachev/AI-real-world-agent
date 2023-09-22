package com.ClarifAI.main.sample.src.textualModule;

import com.ClarifAI.main.sample.src.LLMInteractionModule.ExtendedChatMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SceneContext {

    public static String abnormalEvent = "None";

    public static final List<ExtendedChatMessage> conversation = new ArrayList<>(); // should it be final?

    public static LocalDateTime timestamp;

    public static String textDescription;

    public static String correctedTextDescription;

    public static int score;

    public static String suggestedAssistantPrompt;

}

