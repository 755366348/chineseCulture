package com.example.chineseculture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeBannerAdapter extends RecyclerView.Adapter<HomeBannerAdapter.BannerViewHolder> {

    private final List<Integer> imageResIds;

    public HomeBannerAdapter(List<Integer> imageResIds) {
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        // Banner uses fixed background + overlay images; list only for paging count.
    }

    @Override
    public int getItemCount() {
        return imageResIds.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
