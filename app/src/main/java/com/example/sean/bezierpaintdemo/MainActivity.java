package com.example.sean.bezierpaintdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private StrokesView strokeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        strokeView = findViewById(R.id.strokeView);

        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strokeView.doNextStroke();
            }
        });

        findViewById(R.id.btnAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strokeView.doAllStrokes();
            }
        });

        findViewById(R.id.btnRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPath();
            }
        });

    }

    private void getPath() {
        OkHttpClient client = new OkHttpClient();
        //创建一个Request
        Request request = new Request.Builder()
                .get()
                .url("http://192.168.2.130:8282")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String msg = response.body().string();
                Log.e("onResponse", msg);
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        strokeView.drawStrokes(msg);
                    }
                });
            }
        });
    }

}
