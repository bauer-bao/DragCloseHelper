package com.dragclosehelper.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.dragclosehelper.library.DragCloseHelper;

/**
 * @author bauer on 2019/4/18.
 */
public class ImageViewPreviewActivity extends AppCompatActivity {
    private ConstraintLayout ivPreviewCl;
    private ImageView ivPreviewIv;

    private DragCloseHelper dragCloseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview_preview);
        ivPreviewCl = findViewById(R.id.iv_preview_cl);
        ivPreviewIv = findViewById(R.id.iv_preview_iv);

        //初始化拖拽返回
        dragCloseHelper = new DragCloseHelper(this);
        dragCloseHelper.setShareElementMode(true);
        dragCloseHelper.setDragCloseViews(ivPreviewCl, ivPreviewIv);
        dragCloseHelper.setDragCloseListener(new DragCloseHelper.DragCloseListener() {
            @Override
            public boolean intercept() {
                //默认false 不拦截 如果图片是放大状态，或者处于滑动返回状态，需要拦截
                return false;
            }

            @Override
            public void dragStart() {
                //拖拽开始。可以在此额外处理一些逻辑
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
