package com.dragclosehelper.example;

import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityOptionsCompat;

public class MainActivity extends BaseActivity implements View.OnClickListener {
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
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.d("test exit a", "onSharedElementStart");
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                Log.d("test exit a", "onSharedElementEnd");
            }

            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                super.onRejectSharedElements(rejectedSharedElements);
                Log.d("test exit a", "onRejectSharedElements");
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                Log.d("test exit a", "onMapSharedElements");
                if (updateIndex == 2) {
                    sharedElements.put("share_photo", iv3);
                } else if (updateIndex == 1) {
                    sharedElements.put("share_photo", iv2);
                } else {
                    sharedElements.put("share_photo", iv1);
                }
            }

            @Override
            public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
                Log.d("test exit a", "onCaptureSharedElementSnapshot");
                sharedElement.setAlpha(1f);
                return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
            }

            @Override
            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
                Log.d("test exit a", "onCreateSnapshotView");
                return super.onCreateSnapshotView(context, snapshot);
            }

            @Override
            public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
                Log.d("test exit a", "onSharedElementsArrived");
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener);
            }
        });
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag("updateIndex")})
    public void updateIndex(Integer integer) {
        //此处使用rxbus通知对应的共享元素键值对更新
        updateIndex = integer;
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag("updateView")})
    public void updateView(Integer integer) {
        //此处使用rxbus通知对应的view重新显示出来
        if (integer == 2) {
            iv3.setAlpha(1f);
        } else if (integer == 1) {
            iv2.setAlpha(1f);
        } else {
            iv1.setAlpha(1f);
        }
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
