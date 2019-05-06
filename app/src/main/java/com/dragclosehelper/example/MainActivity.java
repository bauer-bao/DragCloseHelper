package com.dragclosehelper.example;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;

    private int updateIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.get().register(this);

        iv1 = findViewById(R.id.main_iv1);
        iv2 = findViewById(R.id.main_iv2);
        iv3 = findViewById(R.id.main_iv3);
        iv1.setOnClickListener(this);
        iv2.setOnClickListener(this);
        iv3.setOnClickListener(this);

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                if (updateIndex == 2) {
                    sharedElements.put("share_photo", iv3);
                } else if (updateIndex == 1) {
                    sharedElements.put("share_photo", iv2);
                } else {
                    sharedElements.put("share_photo", iv1);
                }
            }
        });
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag("updateIndex")})
    public void updateIndex(Integer integer) {
        //此处使用rxbus通知对应的共享元素键值对更新
        updateIndex = integer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.main_iv1) {
            updateIndex = 0;
        } else if (id == R.id.main_iv2) {
            updateIndex = 1;
        } else if (id == R.id.main_iv3) {
            updateIndex = 2;
        }
        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, v, "share_photo");
        Intent intent = new Intent();
        intent.putExtra("index", updateIndex);
        intent.setClass(MainActivity.this, ImageViewPreviewActivity.class);
        startActivity(intent, compat.toBundle());
    }
}
