package com.dazzle.whiteboard;

import android.os.Bundle;

import com.cjz.jnidrawfb.DrawFrameBuffer;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eraser_example);
        //setContentView(R.layout.drawing_example);

        //findViewById(R.id.frg_root).setBackgroundResource(Config.is4K() ? R.drawable.bg_grid : R.drawable.bg_grid_2k);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawFrameBuffer.initFb();
    }

    @Override
    protected void onPause() {
        DrawFrameBuffer.closeFb();
        super.onPause();
    }

}