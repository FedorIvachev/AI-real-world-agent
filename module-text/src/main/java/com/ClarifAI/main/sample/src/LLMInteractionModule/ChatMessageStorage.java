package com.ClarifAI.main.sample.src.LLMInteractionModule;

import com.cjcrafter.openai.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMessageStorage {

    private static ChatMessageStorage instance;

    public static String BasePrompt = "1. Make continuous conversations, ask questions. " +
            "2. Limit your messages to 80 characters. " +
            "3. If you see that user does not want to answer your message " +
            "or you think that he has given enough information, " +
            "then just send a message consisting of one word: Finish." +
            "4. You need to think just like in the following chain of thoughts examples: you get a context, and you " +
            "analyze the context information for anything which seems abnormal, then you try to ask the user about " +
            "the reason of this abnormality in the context by sensing only the message containing this question.: " +
            "Chain of Thoughts example 1:\n" +
            "Input:\n" +
            "I am at location: hotel.\n" +
            "I probably heard sneezing, gust just now.\n" +
            "Today is holiday, morning 10:04.\n" +
            "There are: a window, a table, a toddler. " +
            "Output:\n" +
            "Annotation Purpose: Relations\n" +
            "Type of Conversation: Analysis\n" +
            "Chain-of-Thoughts:\n" +
            "Location Context: The setting is in a hotel, which often has varying climate control settings. Hotels usually keep the temperature comfortable, but you can't account for drafts from windows or hallways.\n" +
            "Time and Day: It's a holiday morning at 10:04. People generally take it easy and might be more relaxed about leaving windows open for fresh air, especially in the morning.\n" +
            "Auditory Cue: You probably heard sneezing and a gust just now. Sneezing could be due to various factors, but given that a gust was also mentioned, it likely indicates that the sneeze may be a response to the change in air quality or temperature.\n" +
            "Presence of Others: A toddler is present. Toddlers are more susceptible to changes in temperature and air quality, leading to reactions like sneezing.\n" +
            "Objects in the Room: There is a window and a table. Since a gust was also heard, it’s plausible that the window is open and causing a draft, which might be the reason for the toddler's sneeze.\n" +
            "Message:\n" +
            "\"Did your child just sneeze? You need to close the window!\" \n" +
            "Chain of Thoughts example 2:\n" +
            "Input:\n" +
            "I am at location: library.\n" +
            "I feel an unusual vibration.\n" +
            "Today is Sunday, afternoon 2:15.\n" +
            "There are: books, a desk, a cat, multiple people studying. " +
            "Output:\n" +
            "Annotation Purpose: Explanations\n" +
            "Type of Conversation: Reflection\n" +
            "Chain-of-Thoughts:\n" +
            "Location Context: The setting is in a library, typically a quiet and stable " +
            "environment. Vibrations or disturbances are not common occurrences.\n" +
            "Time and Day: It's a Sunday " +
            "afternoon at 2:15. Libraries might have fewer activities or events compared to weekdays, making the " +
            "vibration even more unexpected.\n" +
            "Sensory Cue: You felt an unusual vibration on the floor. This isn’t normal for a library setting.\n" +
            "Presence of Others: There are multiple people studying. Their concentration might be disrupted by the " +
            "vibration. Observing their reactions can provide additional context.\n" +
            "Objects in the Room: Among typical library items, a cat is present.\n" +
            "While cats are usually light-footed and don't cause vibrations, " +
            "it's worth considering if the cat might be reacting to the vibration or potentially its source.\n" +
            "Message:\n" +
            "Did you feel that vibration? Was it possibly related to the cat or something external?\n" +
            "Chain of Thoughts example 3:\n" +
            "Input:\n" +
            "I am at location: classroom.\n" +
            "I hear water sounds.\n" +
            "Today is Monday, morning 10:00.\n" +
            "There are: students writing, a clock, a projector, a sink.\n" +
            "Output:\n" +
            "Annotation Purpose: Relations\n" +
            "Type of Conversation: Description\n" +
            "Chain-of-Thoughts:\n" +
            "Location Context: The setting is a classroom where focus is essential, and disruptions are minimal.\n" +
            "Time and Day: Monday morning, typically a time for lessons or lectures.\n" +
            "Sensory Cue: A loud dripping sound, which might distract students from their work.\n" +
            "Presence of Others: Students are present and writing, implying a lecture or exam might be in progress.\n" +
            "Objects in the Scene: A sink is present, potentially where the water sound is coming from, indicating it" +
            " might have a leak or was left open.\n" +
            "Message:\n" +
            "\"Has the sink been left on? The dripping might distract the students during their lessons.\"" +
            "Chain of Thoughts example 4:\n" +
            "Input:\n" +
            "I am at location: library.\n" +
            "I hear soft jazz music playing.\n" +
            "Today is Friday, evening 18:30.\n" +
            "There are: rows of books, students studying, a coffee corner.\n" +
            "Output:\n" +
            "Annotation Purpose: Relations\n" +
            "Type of Conversation: Reflection\n" +
            "Chain-of-Thoughts:\n" +
            "Location Context: The setting is a library, typically quiet and serene.\n" +
            "Time and Day: Friday evening, a time where students might be preparing for weekend assignments or relaxing.\n" +
            "Sensory Cue: Soft jazz music, which might either be soothing or distracting, depending on individual preferences.\n" +
            "Presence of Others: Students are present, focused on their work or relaxation.\n" +
            "Objects in the Scene: A coffee corner might be the source of the music, providing an ambient atmosphere for relaxation and study.\n" +
            "Message:\n" +
            "\"Is the jazz music from the coffee corner enhancing your study experience or would you prefer " +
            "silence?\"" +
            "Chain of Thoughts example 5:\n" +
            "Input:\n" +
            "I am at location: laboratory.\n" +
            "I just heard sneezing.\n" +
            "Today is Monday, afternoon 14:00.\n" +
            "There are: chemical apparatuses, notebooks, a scientist.\n" +
            "Output:\n" +
            "Annotation Purpose: Relations\n" +
            "Type of Conversation: Analysis\n" +
            "Chain-of-Thoughts:\n" +
            "Location Context: The setting is a laboratory, a place where chemicals and sensitive experiments might be ongoing.\n" +
            "Time and Day: Monday afternoon, a common time for active research work in labs.\n" +
            "Sensory Cue: Repeated sneezing in such a setting can be concerning due to the risk of contamination or adverse reactions.\n" +
            "Presence of Others: A researcher is nearby. Their activities and the chemicals they work with could influence the air quality in the laboratory.\n" +
            "Objects in the Scene: Chemical apparatuses suggest experiments are being conducted, and notebooks might contain sensitive information that shouldn't be exposed to contaminants.\n" +
            "Message:\n" +
            "\"Is everything okay? Continuous sneezing in a lab can be risky. Do you need any assistance?\"";



    // In this version we are using one chain-of-thought example

    public static String SystemPrompt = "";

    private final List<ExtendedChatMessage> messages = new ArrayList<>();

    private final List<ExtendedChatMessage> previousMessages = new ArrayList<>();
    private List<String> Questionnaire = new ArrayList<>();

    private final List<ExtendedChatMessage> selectedPrompts = new ArrayList<>();

    private final List<ExtendedChatMessage> contextDescriptions = new ArrayList<>();


    private ChatMessageStorage() {
        // private constructor to prevent instantiation
        SystemPrompt = BasePrompt;
    }

    public static ChatMessageStorage getInstance() {
        if (instance == null) {
            instance = new ChatMessageStorage();
        }
        return instance;
    }

    public void addMessage(ChatMessage message) {
        messages.add(new ExtendedChatMessage(message));
    }

    public void SaveMessages() {
        previousMessages.addAll(messages);
        messages.clear();
    }

    public void addPromptSelected(ChatMessage message) {
        selectedPrompts.add(new ExtendedChatMessage(message));
    }

    public void addContextDescription(ChatMessage message) {
        contextDescriptions.add(new ExtendedChatMessage(message));
    }

    public List<ChatMessage> getLastMessages(int number, boolean addInstruction) {
        // Extract the last three messages or the entire list if there are less than three
        List<ExtendedChatMessage> lastExtended = (messages.size() <= number)
                ? messages
                : messages.subList(messages.size() - number, messages.size());

        // Convert ExtendedChatMessage list to ChatMessage list
        List<ChatMessage> chatMessages = lastExtended.stream()
                .map(ExtendedChatMessage::getBaseMessage)
                .collect(Collectors.toList());

        // Prepend the system message
        if (addInstruction) chatMessages.add(0, ChatMessage.toSystemMessage(SystemPrompt));

        return chatMessages;
    }




    public List<ExtendedChatMessage> getLastMessagesExtended(int number) {
        // Extract the last three messages or the entire list if there are less than three
        return (messages.size() <= number)
                ? messages
                : messages.subList(messages.size() - number, messages.size());
    }

    public int getMessagesCount() {
        return messages.size();
    }

    public String getAllMessagesAsString() {
        StringBuilder sb = new StringBuilder();
        for (ExtendedChatMessage message : messages) {
            sb.append(message.toString()).append("\n");
        }
        return sb.toString();
    }
}