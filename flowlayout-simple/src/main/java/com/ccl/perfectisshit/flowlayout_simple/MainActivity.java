package com.ccl.perfectisshit.flowlayout_simple;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ccl.perfectisshit.flowlayout.listener.OnFlowLayoutClickListener;
import com.ccl.perfectisshit.flowlayout.widget.FlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    private List<String> data = new ArrayList<>();
    private String [] textRandomArray = {"Android ", "Welcome ", "Hello World ", "Perfect ", "Hi ", "Come On ", "Good Game "};
    private FlowLayout mFL;
    private TextView mTvNotify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        initView();
        initData();
        setView();
    }

    private void setView() {
        mFL.setDataList(data);
    }

    private void initView() {
        mFL = findViewById(R.id.fl);
        mTvNotify = findViewById(R.id.tv_notify);

        mFL.setOnClickListener(new OnFlowLayoutClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, ((TextView) v).getText().toString()+ "------" + v.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        mTvNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random random = new Random();
                data.clear();
                for (int i = 0; i < 1000; i++) {
                    int i1 = random.nextInt(textRandomArray.length - 1);
                    data.add(textRandomArray[i1] + i);
                }
                mFL.notifyDataSetChanged();
            }
        });
    }

    private void initData() {
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            int i1 = random.nextInt(textRandomArray.length - 1);
            data.add(textRandomArray[i1] + i);
        }
    }
}
