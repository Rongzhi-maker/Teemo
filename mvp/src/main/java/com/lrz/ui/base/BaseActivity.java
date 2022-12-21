package com.lrz.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_CACHE_KEY = "INTENT_EXTRA_CACHE_KEY";

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_EXTRA_CACHE_KEY)) {
            String cacheKey = intent.getStringExtra(INTENT_EXTRA_CACHE_KEY);
            Bundle realExtra = (Bundle) IntentDataContainer.getInstance().getAndRemoveData(cacheKey);
            intent.replaceExtras(realExtra);
        }
        super.onCreate(savedInstanceState);
    }

    @CallSuper
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && intent.hasExtra(INTENT_EXTRA_CACHE_KEY)) {
            String cacheKey = intent.getStringExtra(INTENT_EXTRA_CACHE_KEY);
            Bundle realExtra = (Bundle) IntentDataContainer.getInstance().getAndRemoveData(cacheKey);
            intent.replaceExtras(realExtra);
        }
        super.onNewIntent(intent);

    }

    @Override
    public void startActivity(Intent intent) {
        pollingExtraFromIntent(this, intent);
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        pollingExtraFromIntent(this, intent);
        super.startActivityForResult(intent, requestCode);
    }

    private void pollingExtraFromIntent(Activity activity, Intent intent) {
        boolean isInstance = false;
        try {
            Class c = Class.forName(intent.getComponent().getClassName());
            isInstance = BaseActivity.class.isAssignableFrom(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isInstance) {
            return;
        }
        Bundle extra = intent.getExtras();

        if (extra != null && !extra.isEmpty() && !(extra.size() == 1 && extra.containsKey(INTENT_EXTRA_CACHE_KEY))) {
            String cacheKey = activity.getClass().getSimpleName() + activity.hashCode();
            IntentDataContainer.getInstance().saveData(cacheKey, extra);
            Bundle replaceExtra = new Bundle();
            replaceExtra.putString(INTENT_EXTRA_CACHE_KEY, cacheKey);
            intent.replaceExtras(replaceExtra);
        }
    }

    /**
     * 是否启用大数据传递
     * 要传递数据的activity重新并返回true表示启用
     *
     * @return
     */
    protected boolean enableHugeIntentStartActivity() {
        return false;
    }
}
