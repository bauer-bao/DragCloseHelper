package com.dragclosehelper.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_iv)
                .setOnClickListener(v -> {
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, v, "share_photo");
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ImageViewPreviewActivity.class);
                    startActivity(intent, compat.toBundle());
                });
    }
}
