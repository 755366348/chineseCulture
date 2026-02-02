package com.example.chineseculture;

public class OnboardingItem {
    private final int titleResId;
    private final int descriptionResId;
    private final int imageResId;
    private final int backgroundColor;

    public OnboardingItem(int titleResId, int descriptionResId, int imageResId, int backgroundColor) {
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
        this.imageResId = imageResId;
        this.backgroundColor = backgroundColor;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }
}
