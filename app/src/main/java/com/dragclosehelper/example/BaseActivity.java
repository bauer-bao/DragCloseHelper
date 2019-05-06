package com.dragclosehelper.example;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author bauer on 2019/5/6.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                //保证当状态栏或者导航栏显示或者隐藏的时候，布局不会变化
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}
