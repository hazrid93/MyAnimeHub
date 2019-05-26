package com.example.blogmy.cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogmy.ProfileActivity;
import com.example.blogmy.R;
import com.example.blogmy.utils.DecodeBitmapTask;
import com.squareup.picasso.Picasso;

public class SliderCard extends RecyclerView.ViewHolder{

    private static int viewWidth = 0;
    private static int viewHeight = 0;

    private final ImageView imageView;

    private DecodeBitmapTask task;

    public SliderCard(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }

    void setContent(final Context ctx, final String resId) {

        if (viewWidth == 0) {
            itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    viewWidth = itemView.getWidth();
                    viewHeight = itemView.getHeight();

                    Log.d("SearchAnime", "URL: " + resId);
                    Picasso.with(ctx).load(resId).into(imageView);
                  //  loadBitmap(resId);
                }
            });
        } else {
            Log.d("SearchAnime", "URL: " + resId);
            Picasso.with(ctx).load(resId).into(imageView);
        }

    }



}