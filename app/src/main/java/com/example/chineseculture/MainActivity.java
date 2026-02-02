package com.example.chineseculture;

import android.icu.text.Transliterator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private View pageHome;
    private View pageTest;
    private View pageProfile;
    private View navHome;
    private View navTest;
    private View navProfile;
    private ViewPager2 bannerPager;
    private HomeBannerAdapter bannerAdapter;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerPager == null || bannerAdapter == null || bannerAdapter.getItemCount() == 0) {
                return;
            }
            int next = (bannerPager.getCurrentItem() + 1) % bannerAdapter.getItemCount();
            bannerPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(this, 3500);
        }
    };

    private TextView testTitle;
    private TextView testSubtitle;
    private TextView testProgressText;
    private ProgressBar testProgressBar;
    private ProgressBar testLoading;
    private View testQuestionCard;
    private TextView testQuestionIndex;
    private TextView testQuestionText;
    private View testAnswerContainer;
    private MaterialButton testAnswerA;
    private MaterialButton testAnswerB;
    private MaterialButton testAnswerC;
    private MaterialButton testAnswerD;
    private MaterialButton testAnswerE;
    private MaterialButton testSubmitButton;
    private View testResultCard;
    private TextView testResultType;
    private TextView testResultSubType;
    private TextView testResultDesc;
    private TextView testResultFinish;
    private ViewPager2 testPersonPager;
    private PersonCardAdapter personCardAdapter;
    private TextView profileNameText;
    private View profileNameCard;

    private ExamData examData;
    private int currentQuestionIndex = 0;
    private int[] scores = new int[5];
    private boolean isAnimating = false;
    private boolean resultLocked = false;
    private boolean introShown = false;
    private boolean testStarted = false;
    private String introText = "";

    private static final Transliterator PINYIN_TRANSLITERATOR =
            Transliterator.getInstance("Han-Latin; NFD; [:Nonspacing Mark:] Remove; NFC");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init();
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        setContentView(R.layout.activity_main);

        pageHome = findViewById(R.id.pageHome);
        pageTest = findViewById(R.id.pageTest);
        pageProfile = findViewById(R.id.pageProfile);
        navHome = findViewById(R.id.navHome);
        navTest = findViewById(R.id.navTest);
        navProfile = findViewById(R.id.navProfile);
        bannerPager = findViewById(R.id.homeBannerPager);
        View homeSearchCard = findViewById(R.id.homeSearchCard);
        EditText homeSearchInput = findViewById(R.id.homeSearchInput);
        profileNameText = findViewById(R.id.profileNameText);
        profileNameCard = findViewById(R.id.profileNameCard);

        navHome.setOnClickListener(v -> selectTab(0));
        navTest.setOnClickListener(v -> selectTab(1));
        navProfile.setOnClickListener(v -> selectTab(2));
        if (homeSearchCard != null) {
            homeSearchCard.setOnClickListener(v -> {
                if (homeSearchInput != null) {
                    homeSearchInput.requestFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(homeSearchInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        }
        if (profileNameCard != null) {
            profileNameCard.setOnClickListener(v -> showProfileNameDialog());
        }

        setupBanner();
        setupTestPage();
        selectTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBannerAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    private void selectTab(int index) {
        navHome.setSelected(index == 0);
        navTest.setSelected(index == 1);
        navProfile.setSelected(index == 2);

        pageHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        pageTest.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        pageProfile.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        if (index == 1) {
            showIntroIfNeeded();
        }
        if (index == 2) {
            loadProfileInfo();
        }
    }

    private void setupBanner() {
        if (bannerPager == null) {
            return;
        }
        List<Integer> images = Arrays.asList(
                R.drawable.home_page_bg,
                R.drawable.home_page_bg,
                R.drawable.home_page_bg
        );
        bannerAdapter = new HomeBannerAdapter(images);
        bannerPager.setAdapter(bannerAdapter);
    }

    private void startBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, 3500);
    }

    private void stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    private void setupTestPage() {
        testTitle = findViewById(R.id.testTitle);
        testSubtitle = findViewById(R.id.testSubtitle);
        testProgressText = findViewById(R.id.testProgressText);
        testProgressBar = findViewById(R.id.testProgressBar);
        testLoading = findViewById(R.id.testLoading);
        testQuestionCard = findViewById(R.id.testQuestionCard);
        testQuestionIndex = findViewById(R.id.testQuestionIndex);
        testQuestionText = findViewById(R.id.testQuestionText);
        testAnswerContainer = findViewById(R.id.testAnswerContainer);
        testAnswerA = findViewById(R.id.testAnswerA);
        testAnswerB = findViewById(R.id.testAnswerB);
        testAnswerC = findViewById(R.id.testAnswerC);
        testAnswerD = findViewById(R.id.testAnswerD);
        testAnswerE = findViewById(R.id.testAnswerE);
        testSubmitButton = findViewById(R.id.testSubmitButton);
        testResultCard = findViewById(R.id.testResultCard);
        testResultType = findViewById(R.id.testResultType);
        testResultSubType = findViewById(R.id.testResultSubType);
        testResultDesc = findViewById(R.id.testResultDesc);
        testResultFinish = findViewById(R.id.testResultFinish);
        testPersonPager = findViewById(R.id.testPersonPager);

        testAnswerA.setOnClickListener(v -> onAnswerSelected(0));
        testAnswerB.setOnClickListener(v -> onAnswerSelected(1));
        testAnswerC.setOnClickListener(v -> onAnswerSelected(2));
        testAnswerD.setOnClickListener(v -> onAnswerSelected(3));
        testAnswerE.setOnClickListener(v -> onAnswerSelected(4));

        testSubmitButton.setOnClickListener(v -> submitExam());

        loadExamAsync();
    }

    private void loadProfileInfo() {
        if (profileNameText == null) {
            return;
        }
        String currentUser = AuthUi.getCurrentUser(this);
        if (currentUser == null || currentUser.isEmpty()) {
            profileNameText.setText(getString(R.string.profile_name_display, ""));
            return;
        }
        UserDbHelper dbHelper = new UserDbHelper(this);
        String displayName = dbHelper.getDisplayName(currentUser);
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "用户" + currentUser;
        }
        profileNameText.setText(getString(R.string.profile_name_display, displayName));
    }

    private void showProfileNameDialog() {
        String currentUser = AuthUi.getCurrentUser(this);
        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(this, R.string.login_toast_need_login, Toast.LENGTH_SHORT).show();
            return;
        }
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_profile_name);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        EditText input = dialog.findViewById(R.id.profileNameDialogInput);
        View cancel = dialog.findViewById(R.id.profileNameDialogCancel);
        View confirm = dialog.findViewById(R.id.profileNameDialogConfirm);

        UserDbHelper dbHelper = new UserDbHelper(this);
        String displayName = dbHelper.getDisplayName(currentUser);
        if (displayName != null) {
            input.setText(displayName);
            input.setSelection(displayName.length());
        }

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String name = input.getText() == null ? "" : input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.profile_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.updateDisplayName(currentUser, name)) {
                if (profileNameText != null) {
                    profileNameText.setText(getString(R.string.profile_name_display, name));
                }
                Toast.makeText(this, R.string.profile_name_saved, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void loadExamAsync() {
        testLoading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ExamData data = ExamJsonParser.parse(this, "exam.json");
                runOnUiThread(() -> bindExam(data));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    testLoading.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.test_load_failed, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void bindExam(ExamData data) {
        examData = data;
        testLoading.setVisibility(View.GONE);
        if (data == null || data.questions == null || data.questions.isEmpty()) {
            Toast.makeText(this, R.string.test_load_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String desc = data.examDesc == null || data.examDesc.trim().isEmpty()
                ? getString(R.string.test_subtitle)
                : data.examDesc.trim();
        introText = desc.replace("\\n", "\n");
        testSubtitle.setText(getString(R.string.test_subtitle));

        scores = new int[5];
        currentQuestionIndex = 0;
        resultLocked = false;
        introShown = false;
        testStarted = false;
        testResultFinish.setVisibility(View.GONE);
        testResultCard.setVisibility(View.GONE);
        testSubmitButton.setVisibility(View.GONE);
        testQuestionCard.setVisibility(View.GONE);
        testAnswerContainer.setVisibility(View.VISIBLE);
        testProgressBar.setMax(data.questions.size());
        testProgressBar.setProgress(0);
        testProgressText.setText(getString(R.string.test_progress, 0, data.questions.size()));
        showIntroIfNeeded();
    }

    private void showQuestion(int index) {
        if (examData == null || examData.questions == null || index < 0
                || index >= examData.questions.size()) {
            return;
        }
        testQuestionCard.setVisibility(View.VISIBLE);
        ExamData.Question question = examData.questions.get(index);
        testQuestionIndex.setText(getString(R.string.test_question_index, index + 1));
        testQuestionText.setText(question.question);
        updateAnswers(question.answers);
        testProgressBar.setProgress(index + 1);
        testProgressText.setText(getString(R.string.test_progress, index + 1, examData.questions.size()));
    }

    private void updateAnswers(List<String> answers) {
        String[] labels = new String[] {"A", "B", "C", "D", "E"};
        MaterialButton[] buttons = new MaterialButton[] {
                testAnswerA, testAnswerB, testAnswerC, testAnswerD, testAnswerE
        };
        for (int i = 0; i < buttons.length; i++) {
            String text = "";
            if (answers != null && i < answers.size()) {
                text = labels[i] + ". " + answers.get(i);
            }
            buttons[i].setText(text);
            buttons[i].setEnabled(true);
            buttons[i].setVisibility(View.VISIBLE);
        }
    }

    private void onAnswerSelected(int optionIndex) {
        if (examData == null || isAnimating) {
            return;
        }
        if (optionIndex >= 0 && optionIndex < scores.length) {
            scores[optionIndex] += 1;
        }
        setAnswerButtonsEnabled(false);
        if (currentQuestionIndex < examData.questions.size() - 1) {
            int nextIndex = currentQuestionIndex + 1;
            animateQuestionChange(() -> {
                currentQuestionIndex = nextIndex;
                showQuestion(nextIndex);
            });
        } else {
            showCompletionState();
        }
    }

    private void animateQuestionChange(Runnable update) {
        isAnimating = true;
        float distance = dpToPx(24);
        testQuestionCard.animate()
                .alpha(0f)
                .translationX(-distance)
                .setDuration(180)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    update.run();
                    testQuestionCard.setTranslationX(distance);
                    testQuestionCard.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(220)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> {
                                isAnimating = false;
                                setAnswerButtonsEnabled(true);
                            })
                            .start();
                })
                .start();
    }

    private void showCompletionState() {
        testQuestionIndex.setText(getString(R.string.test_completed_title));
        testQuestionText.setText(getString(R.string.test_completed_desc));
        testAnswerContainer.setVisibility(View.GONE);
        testProgressBar.setProgress(examData.questions.size());
        testProgressText.setText(getString(R.string.test_progress,
                examData.questions.size(), examData.questions.size()));
        testSubmitButton.setAlpha(0f);
        testSubmitButton.setVisibility(View.VISIBLE);
        testSubmitButton.animate().alpha(1f).setDuration(220).start();
        isAnimating = false;
    }

    private void setAnswerButtonsEnabled(boolean enabled) {
        testAnswerA.setEnabled(enabled);
        testAnswerB.setEnabled(enabled);
        testAnswerC.setEnabled(enabled);
        testAnswerD.setEnabled(enabled);
        testAnswerE.setEnabled(enabled);
    }

    private void submitExam() {
        if (examData == null || examData.rules == null) {
            return;
        }
        int dominantIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[dominantIndex]) {
                dominantIndex = i;
            }
        }
        int dominantScore = scores[dominantIndex];
        String dominantType = mapElementType(dominantIndex);
        ExamData.RuleType ruleType = findRuleType(dominantType);
        ExamData.SubType subType = findSubType(ruleType, dominantScore);

        String typeText = getString(R.string.test_result_type, dominantType);
        testResultType.setText(typeText);
        if (subType != null) {
            testResultSubType.setText(getString(R.string.test_result_subtype, subType.name));
            testResultDesc.setText(subType.desc);
        } else {
            testResultSubType.setText("");
            testResultDesc.setText("");
        }

        List<PersonCardAdapter.PersonCard> cards = buildPersonCards(subType);
        personCardAdapter = new PersonCardAdapter(this, cards, (card, position) -> {
            if (resultLocked) {
                return;
            }
            showPersonDialog(card, position);
        });
        testPersonPager.setAdapter(personCardAdapter);
        testPersonPager.setOffscreenPageLimit(3);
        testPersonPager.setPageTransformer((page, position) -> {
            float scale = 0.9f + (1f - Math.abs(position)) * 0.1f;
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(0.7f + (1f - Math.abs(position)) * 0.3f);
        });

        testQuestionCard.animate().alpha(0f).setDuration(160).withEndAction(() -> {
            testQuestionCard.setVisibility(View.GONE);
            testSubmitButton.setVisibility(View.GONE);
            testResultCard.setAlpha(0f);
            testResultCard.setVisibility(View.VISIBLE);
            testResultCard.animate().alpha(1f).setDuration(220).start();
        }).start();
    }

    private String mapElementType(int index) {
        switch (index) {
            case 0:
                return "\u6728\u578b\u4eba\u683c";
            case 1:
                return "\u706b\u578b\u4eba\u683c";
            case 2:
                return "\u571f\u578b\u4eba\u683c";
            case 3:
                return "\u91d1\u578b\u4eba\u683c";
            case 4:
                return "\u6c34\u578b\u4eba\u683c";
            default:
                return "\u6728\u578b\u4eba\u683c";
        }
    }

    private ExamData.RuleType findRuleType(String type) {
        if (examData == null || examData.rules == null) {
            return null;
        }
        for (ExamData.RuleType ruleType : examData.rules) {
            if (type.equals(ruleType.type)) {
                return ruleType;
            }
        }
        return null;
    }

    private ExamData.SubType findSubType(ExamData.RuleType ruleType, int score) {
        if (ruleType == null || ruleType.subTypes == null || ruleType.subTypes.isEmpty()) {
            return null;
        }
        for (ExamData.SubType subType : ruleType.subTypes) {
            if (score >= subType.minScore && score <= subType.maxScore) {
                return subType;
            }
        }
        return ruleType.subTypes.get(0);
    }

    private List<PersonCardAdapter.PersonCard> buildPersonCards(ExamData.SubType subType) {
        List<PersonCardAdapter.PersonCard> cards = new ArrayList<>();
        if (subType == null || subType.persons == null) {
            return cards;
        }
        for (ExamData.Person person : subType.persons) {
            String pinyin = toPinyin(person.name);
            int resId = getResources().getIdentifier(pinyin, "drawable", getPackageName());
            cards.add(new PersonCardAdapter.PersonCard(person, resId));
        }
        return cards;
    }

    private String toPinyin(String name) {
        if (name == null) {
            return "";
        }
        String pinyin = PINYIN_TRANSLITERATOR.transliterate(name);
        pinyin = pinyin.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
        return pinyin;
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void showIntroIfNeeded() {
        if (introShown || examData == null || testStarted) {
            return;
        }
        if (pageTest == null || pageTest.getVisibility() != View.VISIBLE) {
            return;
        }
        introShown = true;
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_exam_intro);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        TextView content = dialog.findViewById(R.id.testIntroContent);
        MaterialButton button = dialog.findViewById(R.id.testIntroButton);
        content.setText(introText);
        button.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnDismissListener(d -> startTest());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void startTest() {
        if (testStarted || examData == null) {
            return;
        }
        testStarted = true;
        currentQuestionIndex = 0;
        scores = new int[5];
        testQuestionCard.setAlpha(0f);
        showQuestion(0);
        testQuestionCard.animate().alpha(1f).setDuration(220).start();
    }

    private void showPersonDialog(PersonCardAdapter.PersonCard card, int position) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_person_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        TextView name = dialog.findViewById(R.id.personDialogName);
        TextView desc = dialog.findViewById(R.id.personDialogDesc);
        TextView title = dialog.findViewById(R.id.personDialogTitle);
        TextView hint = dialog.findViewById(R.id.personDialogHint);
        MaterialButton cancel = dialog.findViewById(R.id.personDialogCancel);
        MaterialButton confirm = dialog.findViewById(R.id.personDialogConfirm);
        android.widget.ImageView image = dialog.findViewById(R.id.personDialogImage);

        title.setText(getString(R.string.test_person_dialog_title));
        hint.setText(getString(R.string.test_person_confirm_hint));
        name.setText(card.person.name);
        desc.setText(card.person.desc);
        if (card.imageResId != 0) {
            image.setImageResource(card.imageResId);
        } else {
            image.setImageResource(R.mipmap.ic_launcher);
        }

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            dialog.dismiss();
            resultLocked = true;
            testResultFinish.setText(getString(R.string.test_result_finish, card.person.name));
            testResultFinish.setVisibility(View.VISIBLE);
            if (personCardAdapter != null) {
                personCardAdapter.setSelectedIndex(position);
                personCardAdapter.setLocked(true);
            }
            testPersonPager.setUserInputEnabled(false);
        });
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
