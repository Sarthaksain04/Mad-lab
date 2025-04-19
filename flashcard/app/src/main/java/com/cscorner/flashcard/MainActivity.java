package com.cscorner.flashcard;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText editQuestion, editAnswer;
    Button btnAddCard, btnSpeak, btnListen;
    TextView textFlashcard;

    ArrayList<Flashcard> flashcards = new ArrayList<>();
    int currentIndex = 0;

    TextToSpeech textToSpeech;
    SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editQuestion = findViewById(R.id.editQuestion);
        editAnswer = findViewById(R.id.editAnswer);
        btnAddCard = findViewById(R.id.btnAddCard);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListen = findViewById(R.id.btnListen);
        textFlashcard = findViewById(R.id.textFlashcard);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        // Request audio permission
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        btnAddCard.setOnClickListener(v -> {
            String question = editQuestion.getText().toString();
            String answer = editAnswer.getText().toString();
            if (!question.isEmpty() && !answer.isEmpty()) {
                flashcards.add(new Flashcard(question, answer));
                Toast.makeText(this, "Flashcard added!", Toast.LENGTH_SHORT).show();
                editQuestion.setText("");
                editAnswer.setText("");
                showFlashcard();
            }
        });

        btnSpeak.setOnClickListener(v -> {
            if (!flashcards.isEmpty()) {
                Flashcard current = flashcards.get(currentIndex);
                textToSpeech.speak(current.question, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        btnListen.setOnClickListener(v -> {
            if (!flashcards.isEmpty()) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechRecognizer.startListening(intent);

                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override public void onReadyForSpeech(Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onError(int error) {
                        Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            String spokenText = matches.get(0).toLowerCase().trim();
                            String correctAnswer = flashcards.get(currentIndex).answer.toLowerCase().trim();
                            if (spokenText.equals(correctAnswer)) {
                                Toast.makeText(MainActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
                                nextFlashcard();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong! You said: " + spokenText, Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override public void onPartialResults(Bundle partialResults) {}
                    @Override public void onEvent(int eventType, Bundle params) {}
                });
            }
        });

        showFlashcard();
    }

    void showFlashcard() {
        if (flashcards.isEmpty()) {
            textFlashcard.setText("No flashcards yet.");
        } else {
            Flashcard current = flashcards.get(currentIndex);
            textFlashcard.setText("Q: " + current.question);
        }
    }

    void nextFlashcard() {
        currentIndex = (currentIndex + 1) % flashcards.size();
        showFlashcard();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    class Flashcard {
        String question, answer;
        Flashcard(String q, String a) {
            question = q;
            answer = a;
        }
    }
}
