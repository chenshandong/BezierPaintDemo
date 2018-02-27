package com.example.sean.bezierpaintdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.sean.model.PathModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private StrokesView strokeView;
    private EditText mEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        strokeView = findViewById(R.id.strokeView);
        mEdt = findViewById(R.id.edtContent);

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
//                strokeView.drawStrokes(null, null);
                getPath();
            }
        });

    }

    private void getPath() {
        if (TextUtils.isEmpty(mEdt.getText().toString())){
            return;
        }
        OkHttpClient client = new OkHttpClient();
        //Form表单格式的参数传递
        FormBody formBody = new FormBody
                .Builder()
                .add("char",mEdt.getText().toString())//设置参数名称和参数值
                .build();
        //创建一个Request
        Request request = new Request.Builder()
                .post(formBody)
                .url("http://192.168.2.130:8001/debug/character/get_stroken")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String msg = response.body().string();
                final PathModel pathModel = JSON.parseObject(msg, PathModel.class);
                Log.e("onResponse", msg);
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int status = pathModel.getResult().get(0).getStatus();
                        if (status == 0) {
                            Toast.makeText(MainActivity.this, "找不到该字", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        strokeView.drawStrokes(pathModel.getResult().get(0).getStrokens(), pathModel.getResult().get(0).getPath());
                    }
                });
            }
        });
    }

}
