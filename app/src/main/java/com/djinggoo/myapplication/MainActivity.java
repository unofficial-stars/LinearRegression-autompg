package com.djinggoo.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCylinders;
    private EditText editTextDisplacement;
    private EditText editTextWeight;
    private EditText editTextAcceleration;
    private EditText editTextHorsepower;
    private EditText editTextModelYear;
    private Button buttonPredict;
    private EditText editTextResult;
    private RadioGroup radioGroup;
    private Interpreter interpreter;
    private final String modelPath = "model_dnn_regression.tflite";


    float japanVal = 0.0f;
    float europeVal = 0.0f;
    float usaVal = 0.0f;

    private void initInterpreter(){
         Interpreter.Options options = new Interpreter.Options();
         options.setNumThreads(5);
         options.setUseNNAPI(true);
         interpreter = new Interpreter(loadModelFile(getAssets(), modelPath), options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCylinders = findViewById(R.id.edittext_cylinders);
        editTextDisplacement = findViewById(R.id.edittext_displacement);
        editTextWeight = findViewById(R.id.edittext_weight);
        editTextAcceleration = findViewById(R.id.edittext_acceleration);
        editTextHorsepower = findViewById(R.id.edittext_horsepower);
        editTextModelYear = findViewById(R.id.edittext_modelyear);
        buttonPredict = findViewById(R.id.button_predict);
        editTextResult = findViewById(R.id.result);
        radioGroup = findViewById(R.id.radioGroupOrigin);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                europeVal = 0.0f;
                japanVal = 0.0f;
                usaVal = 0.0f;

                switch (checkedId) {
                    case R.id.rb_europe :
                        europeVal = 1.0f;
                        break;
                    case R.id.rb_japan:
                        japanVal = 1.0f;
                        break;
                    case R.id.rb_usa:
                        usaVal = 1.0f;
                        break;
                    default:
                        break;
                }
            }
        });

        buttonPredict.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedRadio = radioGroup.getCheckedRadioButtonId();



                Float result = doInference(
                        Float.parseFloat(editTextCylinders.getText().toString()),
                        Float.parseFloat(editTextDisplacement.getText().toString()),
                        Float.parseFloat(editTextHorsepower.getText().toString()),
                        Float.parseFloat(editTextWeight.getText().toString()),
                        Float.parseFloat(editTextAcceleration.getText().toString()),
                        Float.parseFloat(editTextModelYear.getText().toString()),
                        europeVal,
                        japanVal,
                        usaVal);



                editTextResult.setText(String.valueOf(result));
            }
        });
        initInterpreter();
    }

    private Float doInference(float cylinder,
                              float displacement,
                              float horsepower,
                              float weight,
                              float acceleration,
                              float model,
                              float originEurope,

                              float originJapan,
                              float originUsa){
        float[][] input = new float[][]{{cylinder, displacement, horsepower, weight, acceleration, model, originEurope, originJapan, originUsa}};
        float[][] output = new float[1][1];
        interpreter.run(input, output);
        return output[0][0];
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath){
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            Long startOffset = fileDescriptor.getStartOffset();
            Long declareLength = fileDescriptor.getDeclaredLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}