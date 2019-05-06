# DragCloseHelper

[![](https://jitpack.io/v/bauer-bao/dragclosehelper.svg)](https://jitpack.io/#bauer-bao/dragclosehelper)

## 效果图：加载略慢，见谅

1.demo效果

   ![image](https://github.com/bauer-bao/DragCloseHelper/blob/master/screenshoots/3.gif)

2.视频效果，非demo

   ![image](https://github.com/bauer-bao/DragCloseHelper/blob/master/screenshoots/1.gif)

## 使用步骤：

0.依赖

        allprojects {
        		repositories {
        			...
        			maven { url 'https://jitpack.io' }
        		}
        	}
        implementation 'com.github.bauer-bao:dragclosehelper:0.0.9'

1.activity主题设为透明

        <item name="android:windowIsTranslucent">true</item>
2.初始化

        DragCloseHelper dragCloseHelper = new DragCloseHelper(this);
3.如果是共享元素启动的页面，需要如下设置（强烈建议和共享元素一起使用，否则是没有灵魂的）

        dragCloseHelper.setShareElementMode(true);
4.设置需要进行拖拽的View/ViewGroup，以及背景ViewGroup（必须要设置背景色）

        dragCloseHelper.setDragCloseViews(parentV, childV);
5.设置监听

        dragCloseHelper.setDragCloseListener(new DragCloseHelper.DragCloseListener() {
            @Override
            public boolean intercept() {
                //默认false 不拦截。比如图片在放大状态，是不需要执行拖拽动画的等等。
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
6.处理touch事件

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (dragCloseHelper.handleEvent(event)) {
            return true;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }
7.可以自定义最大拖拽距离和最小缩放尺寸

    setMaxExitY(int maxExitY)
    setMinScale(@FloatRange(from = 0.1f, to = 1.0f) float minScale)