package com.example.blogmy.cards;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.blogmy.R;

import java.util.List;


public class SliderAdapter extends RecyclerView.Adapter<SliderCard> {

    private int count;
    private Context ctx;
    private List<String> content;
    private final View.OnClickListener listener;

    public SliderAdapter(Context ctx, List<String> content, int count, View.OnClickListener listener) {
        this.ctx = ctx;
        this.content = content;
        this.count = count;
        this.listener = listener;
    }

    public void updateData(List<String> content){
        this.content = content;
    }

    @Override
    public SliderCard onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_slider_card, parent, false);

        if (listener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                }
            });
        }

        return new SliderCard(view);
    }

    @Override
    public void onBindViewHolder(SliderCard holder, int position) {
         holder.setContent(ctx, content.get(position));
        // reset view back to pos 1
    }

    @Override
    public int getItemCount() {
        return count;
    }

}
