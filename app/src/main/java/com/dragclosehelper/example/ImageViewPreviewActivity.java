package com.dragclosehelper.example;

import android.app.SharedElementCallback;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.dragclosehelper.library.DragCloseHelper;
import com.github.chrisbanes.photoview.PhotoView;
import com.hwangjr.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bauer on 2019/4/18.
 */
public class ImageViewPreviewActivity extends BaseActivity {
    private ConstraintLayout ivPreviewCl;
    private HackyViewPager viewPager;

    private List<View> list;

    private ArrayList<Integer> photoList;

    private DragCloseHelper dragCloseHelper;

    private int index;

    private boolean scrolling;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview_preview);

        //如果在拖拽返回关闭的时候，导航栏上又出现拖拽的view的情况，就用以下代码。就和微信的表现形式一样
        //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        ivPreviewCl = findViewById(R.id.iv_preview_cl);
        viewPager = findViewById(R.id.iv_preview_vp);

        index = getIntent().getIntExtra("index", 0);
        photoList = (ArrayList<Integer>) getIntent().getSerializableExtra("photos");

        list = new ArrayList<>();
        for (int i = 0; i < photoList.size(); i++) {
            PhotoView imageView = new PhotoView(this);
            imageView.setImageResource(photoList.get(i));
            imageView.setAdjustViewBounds(true);
            /**
             * viewpager左右滑动 + photoview缩放之间本身就存在着冲突，需要额外处理。
             * 此处使用自定义的vp，同时以com.github.chrisbanes.photoview.PhotoView为例，如果非此photoview，请自己实现对应的matrix方法
             * 思路：需要保证，1.图片缩小的情况下，vp没法左右滑动，2.图片正常的情况下，可以左右滑动，3.图片放大的情况下，在图片拖拽到边界的时候，可以左右滑动
             * 代码为了可读性，没有将ifelse进行合并处理
             */
            imageView.setOnMatrixChangeListener(rect -> {
                //因为demo中此页面是全屏处理，所以需要获取整个屏幕的宽度（包括横屏的时候虚拟按键的宽度），如果非全屏，自己更改此处的宽度
                int screenW = ScreenUtil.getWidthDpi(this);
                /**
                 * 因为发现加载完成之后，matrix会和正常情况有误差，所以需要获取误差值，此处取系统误差值getScaledTouchSlop
                 * 但是getScaledTouchSlop默认是8*系统密度，所以可以根据自己需求去设定
                 *
                 * 同时scale也会存在一定的误差，正常情况下可能在0.99-1.01之间
                 */
                int delta = ViewConfiguration.get(this).getScaledTouchSlop() / 2;
                if (rect.width() < screenW) {
                    //预览的时候，图片左右边界小于屏幕宽度 --> 此处存在三种情况，正常+缩小+放大（图片宽度还处于小于屏幕宽度的时候）
                    if (imageView.getScale() < 0.99 || imageView.getScale() > 1.01) {
                        //getscale会存在误差，如果是缩小或者放大状态，拦截vp的事件
                        viewPager.setLocked(true);
                    } else {
                        //属于正常状态，可以左右滑动
                        viewPager.setLocked(false);
                    }
                } else {
                    //预览的时候，大于或者等于屏幕宽度 --> 对应的情况，正常 + 放大
                    if (Math.abs(rect.left - 0) <= delta || Math.abs(rect.right - screenW) <= delta) {
                        //图片的左右边界，在误差范围内 --> 对应情况，正常 + 放大（图片拖拽到边界）
                        viewPager.setLocked(false);
                    } else {
                        //放大，没拖拽到边界
                        viewPager.setLocked(true);
                    }
                }
            });
            list.add(imageView);
        }

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(list.get(position));
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(list.get(position));
                return list.get(position);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //每次页面变化，都要通知上一个页面更新共享元素的键值对
                RxBus.get().post("updateIndex", position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                scrolling = state != 0;
            }
        });

        viewPager.setCurrentItem(index);

        //初始化拖拽返回
        dragCloseHelper = new DragCloseHelper(this);
        dragCloseHelper.setShareElementMode(true);
        dragCloseHelper.setDragCloseViews(ivPreviewCl, viewPager);
        dragCloseHelper.setDragCloseListener(new DragCloseHelper.DragCloseListener() {
            @Override
            public boolean intercept() {
                //默认false 不拦截 如果图片是放大状态，或者处于滑动返回状态，需要拦截
                float scale = ((PhotoView) list.get(viewPager.getCurrentItem())).getScale();
                return scrolling || (scale < 0.99 || scale > 1.01);
            }

            @Override
            public void dragStart() {
                //拖拽开始。可以在此额外处理一些逻辑
                //此处通知之前点击的view重新显示出来
                RxBus.get().post("updateView", index);
            }

            @Override
            public void dragging(float percent) {
                //拖拽中。percent当前的进度，取值0-1，可以在此额外处理一些逻辑
            }

            @Override
            public void dragCancel() {
                //拖拽取消，会自动复原。可以在此额外处理一些逻辑
            }

            @Override
            public void dragClose(boolean isShareElementMode) {
                //拖拽关闭，如果是共享元素的页面，需要执行activity的onBackPressed方法，注意如果使用finish方法，则返回的时候没有共享元素的返回动画
                if (isShareElementMode) {
                    onBackPressed();
                }
            }
        });
        dragCloseHelper.setClickListener((view, isLongClick) -> {
            int currentIndex = ((ViewPager) view).getCurrentItem();
            Log.d("test", currentIndex + (isLongClick ? "被长按" : "被点击"));
        });

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                //sharedElements 是本页面共享元素的view   sharedElementSnapshots是本页面真正执行动画的view
                Log.d("test enter b", "onSharedElementStart");
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                //sharedElements 是本页面共享元素的view   sharedElementSnapshots是本页面真正执行动画的view
                Log.d("test enter b", "onSharedElementEnd");
            }

            @Override
            public void onRejectSharedElements(List<View> rejectedSharedElements) {
                super.onRejectSharedElements(rejectedSharedElements);
                //屏蔽的view
                Log.d("test enter b", "onRejectSharedElements");
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                //sharedElements 是本页面共享元素的view
                Log.d("test enter b", "onMapSharedElements");
            }

//            @Override
//            public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
//                Log.d("test enter b", "onCaptureSharedElementSnapshot");
//                return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
//            }

            @Override
            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
                //新的iv执行动画的真正iv
                Log.d("test enter b", "onCreateSnapshotView");
                View view = super.onCreateSnapshotView(context, snapshot);
                return view;
            }

            @Override
            public void onSharedElementsArrived(List<String> sharedElementNames, List<View> sharedElements, OnSharedElementsReadyListener listener) {
                //sharedElements 是本页面共享元素的view
                Log.d("test enter b", "onSharedElementsArrived");
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (dragCloseHelper.handleEvent(event)) {
            return true;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }
}
