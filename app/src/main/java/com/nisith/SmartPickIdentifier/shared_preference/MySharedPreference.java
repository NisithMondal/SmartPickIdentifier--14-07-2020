package com.nisith.SmartPickIdentifier.shared_preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;


public class MySharedPreference {
    private SharedPreferences sharedPreferences;
    private Context context;

    public MySharedPreference(Context context){
        sharedPreferences = context.getSharedPreferences("confidence_file", Context.MODE_PRIVATE);
        this.context = context;
    }

    public void setConfidenceValueForFlowerModel(float value){
        if (sharedPreferences != null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("confidence_threshold_value_flower_model", value);
            editor.apply();
            Toast.makeText(context, "Value is Saved.", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Confidence threshold value not saved. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public float getConfidenceValueForFlowerModel(){
        return sharedPreferences.getFloat("confidence_threshold_value_flower_model",0.8f);
    }

    public void setConfidenceValueForCurrencyModel(float value){
        if (sharedPreferences != null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("confidence_threshold_value_currency_model", value);
            editor.apply();
            Toast.makeText(context, "Value is Saved.", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Confidence threshold value not saved. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public float getConfidenceValueForCurrencyModel(){
        return sharedPreferences.getFloat("confidence_threshold_value_currency_model",0.8f);
    }

}
