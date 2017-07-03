package com.softensy.webapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.thefinestartist.finestwebview.FinestWebView;
import com.thefinestartist.finestwebview.listeners.WebViewListener;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = SplashActivity.class.getSimpleName();

    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isNetworkConnected()) {
            new MaterialDialog.Builder(SplashActivity.this)
                    .title(getString(R.string.splash_act_wifi_disabled_content))
                    .theme(Theme.LIGHT)
                    .positiveColorRes(R.color.permissions_green)
                    .negativeColorRes(R.color.permissions_green)
                    .icon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_wifi_green))
                    .positiveText(getString(R.string.splash_act_wifi_disabled_positive))
                    .negativeText(getString(R.string.splash_act_wifi_disabled_negative))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(myIntent);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            launchWebActivity();
        }
    }

    private void launchWebActivity() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        int firebaseFetchTimeOut = 3600;

        if (BuildConfig.DEBUG) {
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            mFirebaseRemoteConfig.setConfigSettings(configSettings);

            firebaseFetchTimeOut = 1;
        }


        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetch(firebaseFetchTimeOut)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        }

                        Locale l = Locale.getDefault();


                        String url = mFirebaseRemoteConfig.getString(l.getLanguage());

                        if (url == null || url.isEmpty()) {
                            url = mFirebaseRemoteConfig.getString("default");
                        }
                        startWebActivity(url);
                    }
                });
    }

    private void startWebActivity(String url) {

        new FinestWebView.Builder(SplashActivity.this)
                .titleDefault(getString(R.string.app_name))
                //.titleColorRes(R.color.white)
                .toolbarScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
                .webViewJavaScriptEnabled(true)
                .showDivider(false)
                .toolbarColorRes(R.color.colorPrimary)
                .statusBarColorRes(R.color.colorPrimaryDark)
//                .iconDefaultColorRes(R.color.white)
//                .iconDisabledColorRes(R.color.grey)
//                .iconPressedColorRes(R.color.white)
                .progressBarHeight(convertDipToPixel(SplashActivity.this, 3))
                .progressBarColorRes(R.color.colorAccent)
//                .stringResRefresh(R.string.offer_web_refresh)
//                .stringResShareVia(R.string.offer_web_share_via)
//                .stringResCopyLink(R.string.offer_web_copy_link)
//                .stringResOpenWith(R.string.offer_web_open_with)
                .backPressToClose(false)
                .setCustomAnimations(R.anim.activity_open_enter, R.anim.activity_open_exit, R.anim.activity_close_enter, R.anim.activity_close_exit)
                .setLimitParameter("wikipedia")
                .show(url);
                finish();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static float convertDipToPixel(Context context, int dip) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
    }
}
