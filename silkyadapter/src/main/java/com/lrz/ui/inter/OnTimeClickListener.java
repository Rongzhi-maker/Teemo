package com.lrz.ui.inter;

import android.view.View;

/**
 * Author And Date: liurongzhi on 2020/4/27.
 * Description: com.yilan.sdk.common.ui.listener
 */
public abstract class OnTimeClickListener implements View.OnClickListener {
    private long time = 1000;

    public OnTimeClickListener() { }


    public OnTimeClickListener(long time) {
        this.time = time;
    }

    private long lastTime = 0;

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - lastTime>time){
            onTimeClick(v);
            lastTime = System.currentTimeMillis();
        }
    }

    public abstract void onTimeClick(View v);
}
