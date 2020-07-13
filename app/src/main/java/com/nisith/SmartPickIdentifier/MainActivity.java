package com.nisith.SmartPickIdentifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.nisith.SmartPickIdentifier.TextToSpeechConverter.TextToSpeechConverter;
import com.nisith.SmartPickIdentifier.shared_preference.MySharedPreference;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Indian_Currency_202071303620

    private ImageView galleryImageView;
    private CameraView cameraView;
    private TextView resultTextView;
    private ImageView clickPhotoImageIcon, galleryImageIcon, cameraImageIcon;
    private static final int REQUEST_CODE = 101;
    private ImageLabeler imageLabelerForFlowerModel, imageLabelerForCurrencyModel;
    private ProgressBar progressBar;
    private ImageView settingImageView, flowerImageView, currencyImageView;
    private Uri selectedImageUri;
    private boolean isGalleryOpen = false;
    private MySharedPreference mySharedPreference;
    private TextToSpeechConverter textToSpeechConverter;
    //Which model is selected currently
    private static final int FLOWER_MODEL_OPTION = 110;
    private static final int CURRENCY_MODEL_OPTION = 111;
    private int selectedOption;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseViews();
        progressBar.setVisibility(View.GONE);
        galleryImageView.setVisibility(View.INVISIBLE);
        inatilazedCameraKitListener();
        clickPhotoImageIcon.setOnClickListener(new MyClickListener());
        galleryImageIcon.setOnClickListener(new MyClickListener());
        cameraImageIcon.setOnClickListener(new MyClickListener());
        settingImageView.setOnClickListener(new MyClickListener());
        flowerImageView.setOnClickListener(new MyClickListener());
        currencyImageView.setOnClickListener(new MyClickListener());
        mySharedPreference = new MySharedPreference(this);
        textToSpeechConverter = new TextToSpeechConverter(this);
        //By default flower model will work
        flowerImageView.setBackgroundColor(Color.rgb(240, 56, 19));
        currencyImageView.setBackgroundColor(Color.rgb(255,255,255));
        initialiseLocalFlowerModel(mySharedPreference.getConfidenceValueForFlowerModel());
        initialiseLocalCurrencyModel(mySharedPreference.getConfidenceValueForCurrencyModel());
        selectedOption = FLOWER_MODEL_OPTION;

    }


    private void initialiseViews(){
        galleryImageView = findViewById(R.id.gallery_image_view);
        resultTextView = findViewById(R.id.result_text_view);
        clickPhotoImageIcon = findViewById(R.id.click_photo_image_icon);
        galleryImageIcon = findViewById(R.id.gallery_image_icon);
        cameraImageIcon = findViewById(R.id.camera_image_view);
        progressBar = findViewById(R.id.progress_bar);
        cameraView = findViewById(R.id.camera_view);
        settingImageView = findViewById(R.id.setting_image_view);
        flowerImageView = findViewById(R.id.flower_image_view);
        currencyImageView = findViewById(R.id.currency_image_view);
    }

    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            resultTextView.setText("");
            switch (view.getId()){
                case R.id.click_photo_image_icon:
                    photoImageIconClicked();
                    break;
                case R.id.camera_image_view:
                    cameraImageIconClicked();
                    break;
                case R.id.gallery_image_icon:
                    galleryImageIconClicked();
                    break;
                case R.id.setting_image_view:
                    settingImageIconClicked(view);
                    break;
                case R.id.flower_image_view:
                    flowerImageIconClicked();
                    break;
                case R.id.currency_image_view:
                    currencyImageIconClicked();
                    break;
            }
        }
    }


    private void photoImageIconClicked(){
        progressBar.setVisibility(View.VISIBLE);
        clickPhotoImageIcon.setEnabled(false);
        galleryImageIcon.setEnabled(false);
        cameraImageIcon.setEnabled(false);
            if (galleryImageView.getVisibility() == View.VISIBLE) {
                //means User pick an image from Gallery
                if (selectedImageUri != null) {
                    if (selectedOption == FLOWER_MODEL_OPTION) {
                        processingImageLabeling(selectedImageUri, imageLabelerForFlowerModel);
                    }else if (selectedOption == CURRENCY_MODEL_OPTION){
                        processingImageLabeling(selectedImageUri, imageLabelerForCurrencyModel);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    clickPhotoImageIcon.setEnabled(true);
                    galleryImageIcon.setEnabled(true);
                    cameraImageIcon.setEnabled(true);
                    Toast.makeText(this, "First select image from Gallery", Toast.LENGTH_SHORT).show();
                }
            } else {
                //means User open the camera
                cameraView.captureImage();
            }

    }

    private void cameraImageIconClicked(){
        if (! cameraView.isStarted()){
            cameraView.start();
        }
        cameraView.setVisibility(View.VISIBLE);
        galleryImageView.setVisibility(View.INVISIBLE);
    }

    private void galleryImageIconClicked(){
        isGalleryOpen = true;
        cameraView.setVisibility(View.INVISIBLE);
        galleryImageView.setVisibility(View.VISIBLE);
        cameraView.stop();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void flowerImageIconClicked(){
        flowerImageView.setBackgroundColor(Color.rgb(240, 56, 19));
        currencyImageView.setBackgroundColor(Color.rgb(255,255,255));
        selectedOption = FLOWER_MODEL_OPTION;
        Toast.makeText(this, "Now you can identify Flowers", Toast.LENGTH_SHORT).show();
    }


    private void currencyImageIconClicked(){
        flowerImageView.setBackgroundColor(Color.rgb(255,255,255));
        currencyImageView.setBackgroundColor(Color.rgb(240, 56, 19));
        selectedOption = CURRENCY_MODEL_OPTION;
        Toast.makeText(this, "Now you can identify Indian Currency", Toast.LENGTH_SHORT).show();
    }





    private void settingImageIconClicked(View view){
        EditText editText = new EditText(view.getContext());
        editText.setHint("Enter Value");
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
       if (selectedOption == FLOWER_MODEL_OPTION){
           editText.setText(String.valueOf(mySharedPreference.getConfidenceValueForFlowerModel()));
           showAlertDialog("Set confidence for Flower model", view, editText);
       }else if (selectedOption == CURRENCY_MODEL_OPTION){
           editText.setText(String.valueOf(mySharedPreference.getConfidenceValueForCurrencyModel()));
           showAlertDialog("Set confidence for Currency model", view, editText);
       }
    }

    private void showAlertDialog(String dialogTitle, View view, final EditText editText){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext())
                .setTitle(dialogTitle)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        if (text.isEmpty()){
                            Toast.makeText(MainActivity.this, "Text field is empty", Toast.LENGTH_LONG).show();
                        }else {
                            text = text+"f";
                            float value = Float.parseFloat(text);
                            if (value > 1){
                                Toast.makeText(MainActivity.this, "Value not Saved. Value must be in between 0 and 1", Toast.LENGTH_LONG).show();
                                editText.setError("");
                                editText.requestFocus();
                            }else {
                                if (selectedOption == FLOWER_MODEL_OPTION) {
                                    initialiseLocalFlowerModel(value);
                                    mySharedPreference.setConfidenceValueForFlowerModel(value);
                                }else if (selectedOption == CURRENCY_MODEL_OPTION){
                                    initialiseLocalCurrencyModel(value);
                                    mySharedPreference.setConfidenceValueForCurrencyModel(value);
                                }
                            }
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setView(editText);
        dialogBuilder.create().show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            galleryImageView.setImageURI(selectedImageUri);
            resultTextView.setText("");
        }

    }

    private void initialiseLocalFlowerModel(float value){
        AutoMLImageLabelerLocalModel localModel = new AutoMLImageLabelerLocalModel.Builder()
                .setAssetFilePath("flower_model/manifest.json")
                .build();
        AutoMLImageLabelerOptions autoMLImageLabelerOptions = new AutoMLImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(value)
                .build();
        imageLabelerForFlowerModel = ImageLabeling.getClient(autoMLImageLabelerOptions);
    }


    private void initialiseLocalCurrencyModel(float value){
        AutoMLImageLabelerLocalModel localModel = new AutoMLImageLabelerLocalModel.Builder()
                .setAssetFilePath("currency_model/manifest.json")
                .build();
        AutoMLImageLabelerOptions autoMLImageLabelerOptions = new AutoMLImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(value)
                .build();
        imageLabelerForCurrencyModel = ImageLabeling.getClient(autoMLImageLabelerOptions);
    }

    private void processingImageLabeling(Uri selectedImageUri, ImageLabeler imageLabeler){
        InputImage inputImage;
        try {
            inputImage = InputImage.fromFilePath(getApplicationContext(), selectedImageUri);
            imageLabeler.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> imageLabels) {
                            extractData(imageLabels);
                            cameraView.start();
                            clickPhotoImageIcon.setEnabled(true);
                            galleryImageIcon.setEnabled(true);
                            cameraImageIcon.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cameraView.start();
                            clickPhotoImageIcon.setEnabled(true);
                            galleryImageIcon.setEnabled(true);
                            cameraImageIcon.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private void processingImageLabeling(Bitmap imageBitmap, ImageLabeler imageLabeler){
        progressBar.setVisibility(View.VISIBLE);
        InputImage inputImage;
            inputImage = InputImage.fromBitmap(imageBitmap, 0);
        imageLabeler.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> imageLabels) {
                            extractData(imageLabels);
                            cameraView.start();
                            clickPhotoImageIcon.setEnabled(true);
                            galleryImageIcon.setEnabled(true);
                            cameraImageIcon.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            cameraView.start();
                            clickPhotoImageIcon.setEnabled(true);
                            galleryImageIcon.setEnabled(true);
                            cameraImageIcon.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

    }

    private void extractData(List<ImageLabel> imageLabels){
        String result = "";
        if (imageLabels.size() > 0) {
            String modelName;
            if (selectedOption == FLOWER_MODEL_OPTION){
                modelName = "Flower Name: ";
            }else {
                modelName = "Note Name: ";
            }
            for (ImageLabel label : imageLabels) {
                result = result + modelName + label.getText() + "  and  Confidence Level: " + label.getConfidence()*100 + "%, ";
            }
        }else {
           result = "Sorry, Image not identified";
        }
        if (textToSpeechConverter != null){
            textToSpeechConverter.speak(result);
        }
        resultTextView.setText(result);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (! isGalleryOpen) {
            cameraView.start();
        }else {
            isGalleryOpen = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeechConverter != null){
            textToSpeechConverter.closeTextToSpeechEngine();
        }
    }

    private void inatilazedCameraKitListener(){
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }
            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap imageBitmap = cameraKitImage.getBitmap();
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                if (selectedOption == FLOWER_MODEL_OPTION) {
                    processingImageLabeling(imageBitmap, imageLabelerForFlowerModel);
                }else if (selectedOption == CURRENCY_MODEL_OPTION){
                    processingImageLabeling(imageBitmap, imageLabelerForCurrencyModel);
                }
                cameraView.stop();
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }


}









