package com.example.chineseculture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INTRO = 0;
    private static final int TYPE_LOGIN = 1;

    private final List<OnboardingItem> items;
    private final Runnable onLoginClick;

    public OnboardingAdapter(List<OnboardingItem> items, Runnable onLoginClick) {
        this.items = items;
        this.onLoginClick = onLoginClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LOGIN) {
            View view = inflater.inflate(R.layout.activity_login, parent, false);
            return new LoginViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OnboardingViewHolder) {
            OnboardingItem item = items.get(position);
            OnboardingViewHolder onboardingHolder = (OnboardingViewHolder) holder;
            onboardingHolder.title.setText(item.getTitleResId());
            onboardingHolder.description.setText(item.getDescriptionResId());
            onboardingHolder.image.setImageResource(item.getImageResId());
            onboardingHolder.itemView.setBackgroundColor(item.getBackgroundColor());
        } else if (holder instanceof LoginViewHolder) {
            ((LoginViewHolder) holder).bind(onLoginClick);
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == items.size() ? TYPE_LOGIN : TYPE_INTRO;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView title;
        private final TextView description;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.onboardingImage);
            title = itemView.findViewById(R.id.onboardingTitle);
            description = itemView.findViewById(R.id.onboardingDescription);
        }
    }

    static class LoginViewHolder extends RecyclerView.ViewHolder {
        private final View root;

        LoginViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
        }

        void bind(Runnable onLoginClick) {
            if (!(root.getContext() instanceof android.app.Activity)) {
                return;
            }
            android.app.Activity activity = (android.app.Activity) root.getContext();
            AuthUi.bind(activity, root, onLoginClick);
        }
    }
}
