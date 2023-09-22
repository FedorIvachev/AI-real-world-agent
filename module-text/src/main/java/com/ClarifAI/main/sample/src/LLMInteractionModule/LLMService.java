package com.ClarifAI.main.sample.src.LLMInteractionModule;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.ClarifAI.main.sample.src.textualModule.StringCallback;
import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LLMService {

    public static boolean isUserTriggered = false;

    private static boolean useAzureOpenAI = false;

    public static void CompletionTest(List<ChatMessage> messages, boolean isConversation, StringCallback callback)
    {
        // Create a Handler for the current thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> new Thread(() -> {
            if (useAzureOpenAI) {
                OkHttpClient client = new OkHttpClient();

                String apiKey = "";
                String base_url = "";
                String deployment_name = "";
                String apiVersion = "2023-05-15";

                String url = base_url + "" +
                        deployment_name +
                        "" +
                        apiVersion;

                JSONObject rootObject = new JSONObject();
                JSONArray messagesArray = new JSONArray();
                try {
                    for (ChatMessage message : messages) {
                        String role = "";
                        JSONObject jsonObject = new JSONObject();
                        switch (message.getRole().toString()) {
                            case "USER":
                                role = "user";
                                break;
                            case "ASSISTANT":
                                role = "assistant";
                                break;
                            case "SYSTEM":
                                role = "system";
                                break;
                            default:
                                break;
                        }
                        jsonObject.put("role", role);
                        jsonObject.put("content", message.getContent());
                        messagesArray.put(jsonObject);
                    }
                    rootObject.put("messages", messagesArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }



                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(rootObject.toString(), mediaType);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("api-key", apiKey)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseBody = response.body().string();
                        if (responseBody != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                JSONArray choicesArray = jsonObject.getJSONArray("choices");
                                if (choicesArray.length() > 0) {
                                    JSONObject choiceObject = choicesArray.getJSONObject(0);
                                    JSONObject messageObject = choiceObject.getJSONObject("message");
                                    String textResult = messageObject.getString("content");
                                    callback.onResult(textResult);
                                } else {
                                }
                            } catch (Exception e) {
                            }
                        } else {
                        }
                    }
                });
            } else {
                ChatRequest request = ChatRequest.builder()
                        .model("gpt-4")
                        .messages(messages).build();

                String key = "";
                OpenAI openai = new OpenAI(key);



                final AtomicReference<String> outputMsg = new AtomicReference<>("");

                openai.streamChatCompletion(request, message -> {
                    System.out.print(message.get(0).getDelta());
                    if (isConversation) callback.onUpdate(message.get(0).getDelta());
                    outputMsg.set(outputMsg.get() + message.get(0).getDelta());

                });

                String output = outputMsg.get();
                callback.onResult(output);
            }
        }).start());
    }


}
