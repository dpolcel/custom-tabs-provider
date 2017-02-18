/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.customtabsbrowser;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity implements a custom tab provider using a Webview.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int MAX_TITLE_LENGTH = 20;

    private WebView mBrowser;
    private Toolbar mToolbar;
    private ImageButton mActionButton;
    private TextView mUrlTextView;
    private TextView mTitleView;
    private CustomTabController mCustomTabController;
    private LockscreenUtils mLockscreenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        mCustomTabController = new CustomTabController(
                this,
                new CustomTabControllerCallback());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActionButton = (ImageButton) findViewById(R.id.actionButton);
        mUrlTextView = (TextView) findViewById(R.id.urlView);
        mTitleView = (TextView) findViewById(R.id.titleView);

        mBrowser = (WebView) findViewById(R.id.webView);
        clearCookies(getApplicationContext());
        mBrowser.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                mCustomTabController.onTitleChange(view.getTitle());
            }
        });
        mBrowser.setWebViewClient(new WebViewClient());
        mBrowser.getSettings().setJavaScriptEnabled(true);
        mBrowser.getSettings().setDomStorageEnabled(true);

        //toggleHideyBar();
        getWindow().getDecorView().setSystemUiVisibility(8);
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("COOKIE_CLEARING", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d("COOKIE_CLEARING", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCustomTabController.hasCustomTabIntent()) {
            mCustomTabController.launch();
        } else {
            Toast.makeText(this, R.string.error_no_custom_tab, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void finish() {
        super.finish();
        mCustomTabController.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mCustomTabController.updateMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mCustomTabController.onOptionsItemSelected(item)) {
            return true;
        }
//        if (R.id.action_open_in_browser == item.getItemId()) {
//            startActivity(new Intent(Intent.ACTION_VIEW, getIntent().getData()));
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleHideyBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        newUiOptions ^= View.SYSTEM_UI_FLAG_LOW_PROFILE;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

    /**
     * Configures our webview-based browser tab based on the custom tab intent.
     */
    private class CustomTabControllerCallback implements CustomTabController.Callback {
        @Override
        public void setTitle(String title) {
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
            mTitleView.setText(title);
        }

        public void setUrl(String url) {
            mUrlTextView.setText(url);
            mBrowser.loadUrl(url);
        }

        @Override
        public void onError(String description, Exception e) {
            Log.e(TAG, description, e);
        }

        @Override
        public void setActionButtonOnClickListener(Button.OnClickListener onClickListener) {
            mActionButton.setOnClickListener(onClickListener);
        }

        @Override
        public void setActionButtonImageDrawable(Drawable drawable) {
            mActionButton.setImageDrawable(drawable);
        }

        @Override
        public void setActionButtonContentDescription(CharSequence description) {
            mActionButton.setContentDescription(description);
        }

        @Override
        public void setActionBarCloseDrawable(Drawable drawable) {
            getSupportActionBar().setHomeAsUpIndicator(drawable);
        }

        @Override
        public void setActionBarBackgroundDrawable(Drawable drawable) {
            mToolbar.setBackground(drawable);
        }

        @Override
        public void setActionButtonVisibility(int visibility) {
            mActionButton.setVisibility(visibility);
        }
    }
}
