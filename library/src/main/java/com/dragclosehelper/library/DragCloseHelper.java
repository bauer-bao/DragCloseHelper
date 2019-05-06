package com.dragclosehelper.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.FloatRange;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

/**
 * @author bauer on 2019/4/17.
 */
public class DragCloseHelper {
    private ViewConfiguration viewConfiguration;

    /**
     * 动画执行时间
     */
    private final static long DURATION = 100;
    /**
     * 滑动边界距离
     */
    private final static int MAX_EXIT_Y = 500;
    private int maxExitY = MAX_EXIT_Y;
    /**
     * 最小的缩放尺寸
     */
    private static final float MIN_SCALE = 0.4F;
    private float minScale = MIN_SCALE;
    /**
     * 是否在滑动关闭中，手指还在触摸中
     */
    private boolean isSwipingToClose;
    /**
     * 上次触摸坐标
     */
    private float mLastY, mLastRawY, mLastX, mLastRawX;
    /**
     * 上次触摸手指id
     */
    private int lastPointerId;
    /**
     * 当前位移距离
     */
    private float mCurrentTranslationY, mCurrentTranslationX;
    /**
     * 上次位移距离
     */
    private float mLastTranslationY, mLastTranslationX;
    /**
     * 正在恢复原位中
     */
    private boolean isResetingAnimate = false;
    /**
     * 共享元素模式
     */
    private boolean isShareElementMode = false;

    private View parentV, childV;

    private DragCloseListener dragCloseListener;
    private Context mContext;

    private boolean isDebug = false;

    public DragCloseHelper(Context mContext) {
        this.mContext = mContext;
        viewConfiguration = ViewConfiguration.get(mContext);
    }

    public void setDragCloseListener(DragCloseListener dragCloseListener) {
        this.dragCloseListener = dragCloseListener;
    }

    /**
     * 设置共享元素模式
     *
     * @param shareElementMode
     */
    public void setShareElementMode(boolean shareElementMode) {
        isShareElementMode = shareElementMode;
    }

    /**
     * 设置拖拽关闭的view
     *
     * @param parentV
     * @param childV
     */
    public void setDragCloseViews(View parentV, View childV) {
        this.parentV = parentV;
        this.childV = childV;
    }

    /**
     * 设置最大退出距离
     *
     * @param maxExitY
     */
    public void setMaxExitY(int maxExitY) {
        this.maxExitY = maxExitY;
    }

    /**
     * 设置最小缩放尺寸
     *
     * @param minScale
     */
    public void setMinScale(@FloatRange(from = 0.1f, to = 1.0f) float minScale) {
        this.minScale = minScale;
    }

    /**
     * 设置debug模式
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    /**
     * 处理touch事件
     *
     * @param event
     * @return
     */
    public boolean handleEvent(MotionEvent event) {
        if (dragCloseListener != null && dragCloseListener.intercept()) {
            //拦截
            log("action dispatch--->");
            isSwipingToClose = false;
            return false;
        } else {
            //不拦截
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                log("action down--->");
                //初始化数据
                lastPointerId = event.getPointerId(0);
                reset(event);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                log("action move--->" + event.getPointerCount() + "---" + isSwipingToClose);
                if (event.getPointerCount() > 1) {
                    //如果有多个手指
                    if (isSwipingToClose) {
                        //已经开始滑动关闭，恢复原状，否则需要派发事件
                        isSwipingToClose = false;
                        resetCallBackAnimation();
                        return true;
                    }
                    reset(event);
                    return false;
                }
                if (lastPointerId != event.getPointerId(0)) {
                    //手指不一致，恢复原状
                    if (isSwipingToClose) {
                        resetCallBackAnimation();
                    }
                    reset(event);
                    return true;
                }
                float currentY = event.getY();
                float currentX = event.getX();
                if (isSwipingToClose ||
                        (Math.abs(currentY - mLastY) > 2 * viewConfiguration.getScaledTouchSlop() && Math.abs(currentY - mLastY) > Math.abs(currentX - mLastX) * 1.5)) {
                    //已经触发或者开始触发，更新view
                    mLastY = currentY;
                    mLastX = currentX;
                    log("action move---> start close");
                    float currentRawY = event.getRawY();
                    float currentRawX = event.getRawX();
                    if (!isSwipingToClose) {
                        //准备开始
                        isSwipingToClose = true;
                        if (dragCloseListener != null) {
                            dragCloseListener.dragStart();
                        }
                    }
                    //已经开始，更新view
                    mCurrentTranslationY = currentRawY - mLastRawY + mLastTranslationY;
                    mCurrentTranslationX = currentRawX - mLastRawX + mLastTranslationX;
                    float percent = 1 - Math.abs(mCurrentTranslationY / (maxExitY + childV.getHeight()));
                    if (percent > 1) {
                        percent = 1;
                    } else if (percent < 0) {
                        percent = 0;
                    }
                    parentV.getBackground().mutate().setAlpha((int) (percent * 255));
                    if (dragCloseListener != null) {
                        dragCloseListener.dragging(percent);
                    }
                    childV.setTranslationY(mCurrentTranslationY);
                    childV.setTranslationX(mCurrentTranslationX);
                    if (percent < minScale) {
                        percent = minScale;
                    }
                    childV.setScaleX(percent);
                    childV.setScaleY(percent);
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                log("action up--->" + isSwipingToClose);
                //手指抬起事件
                if (isSwipingToClose) {
                    if (mCurrentTranslationY > maxExitY) {
                        if (isShareElementMode) {
                            //会执行共享元素的离开动画
                            if (dragCloseListener != null) {
                                dragCloseListener.dragClose(true);
                            }
                        } else {
                            //会执行定制的离开动画
                            exitWithTranslation(mCurrentTranslationY);
                        }
                    } else {
                        resetCallBackAnimation();
                    }
                    isSwipingToClose = false;
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                //取消事件
                if (isSwipingToClose) {
                    resetCallBackAnimation();
                    isSwipingToClose = false;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 退出动画
     *
     * @param currentY
     */
    public void exitWithTranslation(float currentY) {
        int targetValue = currentY > 0 ? childV.getHeight() : -childV.getHeight();
        ValueAnimator anim = ValueAnimator.ofFloat(mCurrentTranslationY, targetValue);
        anim.addUpdateListener(animation -> updateChildView(mCurrentTranslationX, (float) animation.getAnimatedValue()));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (dragCloseListener != null) {
                    dragCloseListener.dragClose(false);
                }
                ((Activity) mContext).finish();
                ((Activity) mContext).overridePendingTransition(R.anim.dchlib_anim_empty, R.anim.dchlib_anim_alpha_out_long_time);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(DURATION);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    /**
     * 重置数据
     *
     * @param event
     */
    private void reset(MotionEvent event) {
        isSwipingToClose = false;
        mLastY = event.getY();
        mLastX = event.getX();
        mLastRawY = event.getRawY();
        mLastRawX = event.getRawX();
        mLastTranslationY = 0;
        mLastTranslationX = 0;
    }

    /**
     * 更新缩放的view
     */
    private void updateChildView(float transX, float transY) {
        childV.setTranslationY(transY);
        childV.setTranslationX(transX);
        float percent = Math.abs(transY / (maxExitY + childV.getHeight()));
        float scale = 1 - percent;
        if (scale < minScale) {
            scale = minScale;
        }
        childV.setScaleX(scale);
        childV.setScaleY(scale);
    }

    /**
     * 恢复到原位动画
     */
    private void resetCallBackAnimation() {
        if (isResetingAnimate || mCurrentTranslationY == 0) {
            return;
        }
        float ratio = mCurrentTranslationX / mCurrentTranslationY;
        ValueAnimator animatorY = ValueAnimator.ofFloat(mCurrentTranslationY, 0);
        animatorY.addUpdateListener(valueAnimator -> {
            if (isResetingAnimate) {
                mCurrentTranslationY = (float) valueAnimator.getAnimatedValue();
                mCurrentTranslationX = ratio * mCurrentTranslationY;
                mLastTranslationY = mCurrentTranslationY;
                mLastTranslationX = mCurrentTranslationX;
                updateChildView(mLastTranslationX, mCurrentTranslationY);
            }
        });
        animatorY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isResetingAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isResetingAnimate) {
                    parentV.getBackground().mutate().setAlpha(255);
                    mCurrentTranslationY = 0;
                    mCurrentTranslationX = 0;
                    isResetingAnimate = false;
                    if (dragCloseListener != null) {
                        dragCloseListener.dragCancel();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorY.setDuration(DURATION).start();
    }

    /**
     * 打印日志
     *
     * @param msg
     */
    private void log(String msg) {
        if (isDebug) {
            Log.d(getClass().getName(), msg);
        }
    }

    public interface DragCloseListener {
        /**
         * 是否有拦截
         *
         * @return
         */
        boolean intercept();

        /**
         * 开始拖拽
         */
        void dragStart();

        /**
         * 拖拽中
         *
         * @param percent
         */
        void dragging(float percent);

        /**
         * 取消拖拽
         */
        void dragCancel();

        /**
         * 拖拽结束并且关闭
         *
         * @param isShareElementMode
         */
        void dragClose(boolean isShareElementMode);
    }
}
