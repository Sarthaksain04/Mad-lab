package com.cscorner.stories;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Locale;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    private EditText etPrompt;
    private Button btnSpeak, btnListen;
    private TextView tvStory;
    private TextToSpeech textToSpeech;
    private boolean isListening = false;

    private static final String API_KEY = "AIzaSyDJTwC01Lw4bxeDaOIzQ4Raxd_u0IVt7PY"; // Replace with your actual API key
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPrompt = findViewById(R.id.etPrompt);
        Button btnGenerate = findViewById(R.id.btnGenerate);
        tvStory = findViewById(R.id.tvStory);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListen = findViewById(R.id.btnListen);

        btnSpeak.setVisibility(View.GONE);
        btnListen.setVisibility(View.GONE);

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        btnGenerate.setOnClickListener(v -> generateStory(etPrompt.getText().toString()));
        btnSpeak.setOnClickListener(v -> speakStory());
        btnListen.setOnClickListener(v -> listenStory());
    }

    @SuppressLint("SetTextI18n")
    private void generateStory(String prompt) {
        if (prompt.isEmpty()) {
            tvStory.setText("Please enter a prompt.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JsonObject requestJson = new JsonObject();
        JsonArray contentsArray = new JsonArray();
        JsonObject contentObject = new JsonObject();
        JsonArray partsArray = new JsonArray();
        JsonObject partObject = new JsonObject();

        partObject.addProperty("text", prompt);
        partsArray.add(partObject);
        contentObject.add("parts", partsArray);
        contentsArray.add(contentObject);
        requestJson.add("contents", contentsArray);

        RequestBody body = RequestBody.create(requestJson.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("API Response", responseData);

                    JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
                    JsonArray candidates = jsonObject.getAsJsonArray("candidates");

                    if (candidates != null && candidates.size() > 0) {
                        JsonObject outputObject = candidates.get(0).getAsJsonObject();
                        JsonObject contentObj = outputObject.getAsJsonObject("content");
                        JsonArray parts = contentObj.getAsJsonArray("parts");

                        if (parts != null && parts.size() > 0) {
                            String generatedText = parts.get(0).getAsJsonObject().get("text").getAsString().trim();

                            runOnUiThread(() -> {
                                tvStory.setText(generatedText);
                                btnSpeak.setVisibility(View.VISIBLE);
                                btnListen.setVisibility(View.VISIBLE);
                            });
                        }
                    } else {
                        runOnUiThread(() -> tvStory.setText("Error: No story generated."));
                    }
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("API Error", errorMessage);
                    runOnUiThread(() -> tvStory.setText("Error: " + errorMessage));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> tvStory.setText("Error generating story. Try again!"));
            }
        }).start();
    }

    private void speakStory() {
        String storyText = tvStory.getText().toString();
        if (!storyText.isEmpty()) {
            textToSpeech.speak(storyText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void listenStory() {
        String storyText = tvStory.getText().toString();
        if (!storyText.isEmpty()) {
            if (isListening) {
                textToSpeech.stop();
                btnListen.setText("Listen");
            } else {
                textToSpeech.speak(storyText, TextToSpeech.QUEUE_FLUSH, null, null);
                btnListen.setText("Stop");
            }
            isListening = !isListening;
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
