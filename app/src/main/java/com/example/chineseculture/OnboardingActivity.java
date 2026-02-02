package com.example.chineseculture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "onboarding_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private View controlsLayout;
    private List<OnboardingItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if (isOnboardingDone()) {
            openLogin();
            return;
        }
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.onboardingPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        controlsLayout = findViewById(R.id.onboardingControls);

        items = buildItems();
        OnboardingAdapter adapter = new OnboardingAdapter(items, this::openMain);
        viewPager.setAdapter(adapter);

        setupDots(items.size());
        updateControls(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateControls(position);
            }
        });
    }

    private List<OnboardingItem> buildItems() {
        List<OnboardingItem> data = new ArrayList<>();
        data.add(new OnboardingItem(
                R.string.onboarding_title_qin,
                R.string.onboarding_desc_qin,
                R.drawable.qin,
                ContextCompat.getColor(this, R.color.onboarding_page_one)
        ));
        data.add(new OnboardingItem(
                R.string.onboarding_title_qi,
                R.string.onboarding_desc_qi,
                R.drawable.qi,
                ContextCompat.getColor(this, R.color.onboarding_page_two)
        ));
        data.add(new OnboardingItem(
                R.string.onboarding_title_shu,
                R.string.onboarding_desc_shu,
                R.drawable.shu,
                ContextCompat.getColor(this, R.color.onboarding_page_three)
        ));
        data.add(new OnboardingItem(
                R.string.onboarding_title_hua,
                R.string.onboarding_desc_hua,
                R.drawable.hua,
                ContextCompat.getColor(this, R.color.onboarding_page_four)
        ));
        return data;
    }

    private void setupDots(int count) {
        dotsLayout.removeAllViews();
        int size = dpToPx(8);
        int margin = dpToPx(6);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.onboarding_dot_inactive);
            dotsLayout.addView(dot);
        }
    }

    private void updateDots(int position) {
        int count = dotsLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = dotsLayout.getChildAt(i);
            dot.setBackgroundResource(i == position
                    ? R.drawable.onboarding_dot_active
                    : R.drawable.onboarding_dot_inactive);
        }
    }

    private void updateControls(int position) {
        if (position == items.size()) {
            controlsLayout.setVisibility(View.GONE);
            markOnboardingDone();
        } else {
            controlsLayout.setVisibility(View.VISIBLE);
            updateDots(position);
        }
    }

    private void markOnboardingDone() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_ONBOARDING_DONE, false)) {
            prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
        }
    }

    private boolean isOnboardingDone() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void openMain() {
        markOnboardingDone();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
