package com.falcon.car;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Brand screen: the mark fades in, a red line sweeps, then the app opens. */
public class SplashActivity extends AppCompatActivity {

    private static final long HOLD_MS = 1900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launch = this::openMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView version = findViewById(R.id.splash_version);
        version.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));

        animateIn();
        handler.postDelayed(launch, HOLD_MS);
    }

    private void animateIn() {
        View mark = findViewById(R.id.splash_mark);
        View wordmark = findViewById(R.id.splash_wordmark);
        View sub = findViewById(R.id.splash_sub);
        final View scanLine = findViewById(R.id.splash_scan_line);

        mark.setAlpha(0f);
        mark.setScaleX(0.86f);
        mark.setScaleY(0.86f);
        mark.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(520L).start();

        wordmark.setAlpha(0f);
        wordmark.animate().alpha(1f).setStartDelay(220L).setDuration(420L).start();

        sub.setAlpha(0f);
        sub.animate().alpha(1f).setStartDelay(320L).setDuration(420L).start();

        // Sweep the scan line across its 180dp track, twice.
        scanLine.post(() -> {
            View track = (View) scanLine.getParent();
            float distance = track.getWidth() - scanLine.getWidth();
            scanLine.setTranslationX(0f);
            scanLine.animate()
                    .translationX(distance)
                    .setDuration(760L)
                    .withEndAction(() -> scanLine.animate()
                            .translationX(0f)
                            .setDuration(760L)
                            .start())
                    .start();
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(launch);
        super.onDestroy();
    }
}
