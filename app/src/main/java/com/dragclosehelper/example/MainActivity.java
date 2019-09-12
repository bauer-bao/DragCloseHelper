package com.dragclosehelper.example;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends BaseActivity {
    private Toolbar toolbar;
    private RecyclerView photosRv;
    private AlbumPhotoAdapter adapter;
    private ArrayList<Integer> photoList;

    private int updateIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.get().register(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        toolbar.setTitle("DragCloseHelper");
        toolbar.setTitleTextColor(Color.WHITE);

        photosRv = findViewById(R.id.photos_rv);
        photoList = new ArrayList<>();
        photoList.add(R.drawable.img1);
        photoList.add(R.drawable.img2);
        photoList.add(R.drawable.img3);
        photoList.add(R.drawable.img4);
        photoList.add(R.drawable.img5);
        photoList.add(R.drawable.img6);
        photoList.add(R.drawable.img7);
        photoList.add(R.drawable.img8);
        photoList.add(R.drawable.img9);
        adapter = new AlbumPhotoAdapter(photoList);
        photosRv.setLayoutManager(new GridLayoutManager(this, 3));
        photosRv.setAdapter(adapter);

        adapter.setOnItemClickListener((adapter, view, position) -> {
            updateIndex = position;
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, view.findViewById(R.id.rv_item_photo_iv), "share_photo");
            Intent intent = new Intent();
            intent.putExtra("index", updateIndex);
            intent.setClass(MainActivity.this, ImageViewPreviewActivity.class);
            intent.putExtra("photos", photoList);
            startActivity(intent, compat.toBundle());
        });

        setExitSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
//                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
//                Log.d("test exit a", "onSharedElementStart");
//            }
//
//            @Override
//            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
//                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
//                Log.d("test exit a", "onSharedElementEnd");
//            }
//
//            @Override
//            public void onRejectSharedElements(List<View> rejectedSharedElements) {
//                super.onRejectSharedElements(rejectedSharedElements);
//                Log.d("test exit a", "onRejectSharedElements");
//            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                //sharedElements 本页面指定共享元素动画的view
                Log.d("test exit a", "onMapSharedElements");
                //更新共享元素键值对
                View view = adapter.getViewByPosition(photosRv, updateIndex, R.id.rv_item_photo_iv);
                if (view != null) {
                    sharedElements.put("share_photo", view);
                }
            }

            @Override
            public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
                //sharedElement 本页面指定共享元素动画的view
                Log.d("test exit a", "onCaptureSharedElementSnapshot");
                //解决执行共享元素动画的时候，一开始显示空白的问题
                sharedElement.setAlpha(1f);
                return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
            }

//            @Override
//            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
//                Log.d("test exit a", "onCreateSnapshotView");
//                return super.onCreateSnapshotView(context, snapshot);
//            }

            @Override
            public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
                //sharedElements 本页面指定共享元素动画的view
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
        //此处使用rxbus通知对应的view重新显示出来，解决在滑动返回手指拖动的过程中，看到上一个页面点击的图片显示空白的问题
        View view = adapter.getViewByPosition(photosRv, integer, R.id.rv_item_photo_iv);
        if (view != null) {
            view.setAlpha(1f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }
}
