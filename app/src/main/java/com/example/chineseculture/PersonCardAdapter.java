package com.example.chineseculture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PersonCardAdapter extends RecyclerView.Adapter<PersonCardAdapter.PersonViewHolder> {

    public interface OnPersonSelectedListener {
        void onPersonSelected(PersonCard card, int position);
    }

    public static class PersonCard {
        public final ExamData.Person person;
        public final int imageResId;

        public PersonCard(ExamData.Person person, int imageResId) {
            this.person = person;
            this.imageResId = imageResId;
        }
    }

    private final List<PersonCard> cards;
    private final OnPersonSelectedListener listener;
    private final int selectedColor;
    private final int defaultColor;
    private int selectedIndex = -1;
    private boolean locked = false;

    public PersonCardAdapter(Context context, List<PersonCard> cards, OnPersonSelectedListener listener) {
        this.cards = cards;
        this.listener = listener;
        this.selectedColor = context.getColor(R.color.home_primary);
        this.defaultColor = context.getColor(R.color.home_hint);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_card, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        PersonCard card = cards.get(position);
        holder.name.setText(card.person.name);
        if (card.imageResId != 0) {
            holder.image.setImageResource(card.imageResId);
        } else {
            holder.image.setImageResource(R.mipmap.ic_launcher);
        }

        boolean selected = position == selectedIndex;
        holder.card.setStrokeColor(selected ? selectedColor : defaultColor);
        holder.selectedTag.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.card.setOnClickListener(v -> {
            if (locked) {
                return;
            }
            selectedIndex = position;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onPersonSelected(card, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final ImageView image;
        private final TextView name;
        private final TextView selectedTag;

        PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.personCard);
            image = itemView.findViewById(R.id.personImage);
            name = itemView.findViewById(R.id.personName);
            selectedTag = itemView.findViewById(R.id.personSelectedTag);
        }
    }
}
