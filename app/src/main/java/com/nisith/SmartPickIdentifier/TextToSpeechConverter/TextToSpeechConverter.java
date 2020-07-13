package com.nisith.SmartPickIdentifier.TextToSpeechConverter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import java.util.Locale;

public class TextToSpeechConverter {
    private TextToSpeech textToSpeech;
    private Context context;
    public TextToSpeechConverter(final Context context){
        this.context = context;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    textToSpeech.setLanguage(Locale.US);
                }else {
                    Toast.makeText(context, "Text to speech engine not supported.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void speak(String text){
        if (textToSpeech != null){
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void closeTextToSpeechEngine(){
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
