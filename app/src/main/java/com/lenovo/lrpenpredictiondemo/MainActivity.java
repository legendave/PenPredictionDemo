package com.lenovo.lrpenpredictiondemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private HandWriteView handWriteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);




        handWriteView = findViewById(R.id.handwriteview);
        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handWriteView.clear();
            }
        });

        Button prediction = findViewById(R.id.prediction);
        prediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handWriteView.setPrediction(true);

            }
        });
        Button noprediction = findViewById(R.id.noprediction);
        noprediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handWriteView.setPrediction(false);

            }
        });

//        Button dda = findViewById(R.id.dda);
//        dda.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                handWriteView.setDda(true);
//            }
//        });
//        Button noDda = findViewById(R.id.nodda);
//        noDda.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                handWriteView.setDda(false);
//            }
//        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}