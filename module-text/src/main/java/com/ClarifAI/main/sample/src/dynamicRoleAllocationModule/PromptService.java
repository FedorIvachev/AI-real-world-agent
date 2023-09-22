package com.ClarifAI.main.sample.src.dynamicRoleAllocationModule;

import com.ClarifAI.main.sample.src.LLMInteractionModule.ChatMessageStorage;
import com.ClarifAI.main.sample.src.LLMInteractionModule.LLMService;
import com.ClarifAI.main.sample.src.textualModule.StringCallback;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatUser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PromptService {

    public static String conversationSummarizationPrompt = "Create a scene annotation including the user's " +
            "explanations " +
            "into 80 characters, " +
            "try to omit unnecessary details. " +
            "Example 1: \n" +
            "User message: I am at location: dormitory.\n" +
            "I am currently sitting.\n" +
            "Today is holiday, afternoon 2:03.\n" +
            "I am wearing headphones.\n" +
            "There are: a laptop, a mouse, a keyboard, a computer game.\n" +
            "Assistant: \"Was there a fire alarm? You should stop playing!\" " +
            "User: \"It's okay, it's a mock fire alarm. We don't need to leave.\"/n" +
            "Annotation: There is a mock fire alarm. The user continues playing a video game because he doesn't need " +
            "to leave.";
    public static final Map<String, String> roles;

    static {
        // The roles here are very basic for testing purposes
        roles = new HashMap<>();
        roles.put("LanguagePartner", "You're my language partner. Engage in continuous conversations and correct any mistakes I make.");
        roles.put("ShopAssistant", "You are the shop assistant. Be helpful in answering my queries. Use context to provide specific answers. Interact as if you're assisting me in a store.");
        roles.put("Friend", "Act as my friend. Let's chat casually.");
        roles.put("WorkAssistant", "You are my professional assistant. Avoid unnecessary disturbances and try to handle data annotations autonomously. Maintain a formal tone.");
        roles.put("CookAssistant", "Assist me in cooking. Any questions you have should relate to the cooking process. Engage with me as a fellow cook would.");

// New Roles
        roles.put("FitnessTrainer", "You're my fitness trainer. Guide me through exercises and routines, offering tips and corrections to improve my form. Communicate as if we're in a gym session.");
        roles.put("TravelAdvisor", "Act as my travel advisor. Provide recommendations, insights, and guidance about destinations and activities. Interact as if you're helping me plan a trip.");
        roles.put("StudyBuddy", "You're my study partner. Help me understand topics, clarify doubts, and discuss academic subjects. Engage as if we're studying together for an upcoming exam.");
        roles.put("BookClubMate", "You're a member of my book club. Let's discuss plots, characters, and themes from various books. Share your insights as a fellow literature enthusiast.");
        roles.put("HomeDecorator", "You are my home decorator. Offer suggestions on interior design, decor items, and arrangement ideas. Discuss as if we're planning to redecorate my living space.");

    }

    private static String summarizeRoles() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, String> entry : roles.entrySet()) {
            summary.append("Prompt name: ")
                    .append(entry.getKey())
                    .append(", prompt: ")
                    .append(entry.getValue())
                    .append(System.lineSeparator());
        }
        return summary.toString();
    }

    public static void selectPrompt(String sceneContext, StringCallback callback) {
        List<ChatMessage> messages = new ArrayList<>();
        String message = "Help me to select an appropriate prompt based on the context. Output only a suitable prompt" +
                " name. The available prompts are: " + summarizeRoles() + System.lineSeparator() + "The context is: " +
                sceneContext;
        messages.add(new ChatMessage(ChatUser.USER, message));
        LLMService.CompletionTest(messages, false, new StringCallback() {
            @Override
            public void onResult(String result) {
                if (roles.containsKey(result)) {
                    if (!Objects.equals(ChatMessageStorage.getInstance().SystemPrompt, result)) {
                        ChatMessageStorage.getInstance().SystemPrompt =
                                roles.get(result) + ChatMessageStorage.getInstance().BasePrompt;
                        callback.onResult("Changed system role to " + result);
                    } else {
                        callback.onResult("System role not changed");
                    }
                }
                // Is change of role change of context?
            }
        });
    }

    //private final static Handler handler = new Handler();
    //private final static Runnable runnableCode = new Runnable() {
        //@Override
        //public void run() {
            // Do something here on the main thread
            //PromptService.selectPrompt(MultimodalData.getAllInfo());


            // Repeat this runnable code block again every 30 seconds
            //handler.postDelayed(this, 30000);
        //}
    //};

    //public static void startPromptService() {
        //handler.post(runnableCode);
    //}

    //public static void cleanup() {
    //    handler.removeCallbacks(runnableCode);
    //}
}